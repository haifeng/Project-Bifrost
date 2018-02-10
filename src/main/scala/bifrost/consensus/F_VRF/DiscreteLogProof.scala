package bifrost.consensus.F_VRF

import java.security.SecureRandom

import org.bouncycastle.crypto.digests.Blake2bDigest
import org.bouncycastle.crypto.generators.{DHBasicKeyPairGenerator, KDF2BytesGenerator}
import org.bouncycastle.crypto.params._
import scorex.core.serialization.Serializer
import scorex.core.transaction.box.proposition.Proposition
import scorex.core.transaction.proof.Proof
import scorex.crypto.hash.{Blake2b512, CryptographicHash64}

/**
  * NIZK proof that log_g(y) = log_a(b), covered by random t
  *
  * @param c  derived as the hash: H(g, y, a, b, g&#94;t, a&#94;t)
  * @param s  dervied as t + w * k % g.mod
  */
case class DiscreteLogProof(c: BigInt, s: BigInt) extends Proof[DiscreteLogProposition] {

  override type M = DiscreteLogProof

  def serializer: Serializer[DiscreteLogProposition] = ???

  override def isValid(proposition: Proposition, sample: Array[Byte]): Boolean = proposition match {
    case p: DiscreteLogProposition => p.verify(this, sample)
    case _ => false
  }
}

object DiscreteLogProof {

  val hashFunction: CryptographicHash64 = Blake2b512

  /**
    * @param l    security parameter
    * @param m    argument used as parameter for PRF
    * @param g    generator of a group of prime order q
    * @param k    private key randomly selected from Z_q
    * @param q    prime order
    */
  def apply(l: Int, m: BigInt, g: BigInt, k: BigInt, q: BigInt, p: BigInt): DiscreteLogProof = {
    val (c: BigInt, s: BigInt) = {
      val keyGen: DHBasicKeyPairGenerator = new DHBasicKeyPairGenerator()
      keyGen.init(new DHKeyGenerationParameters(SecureRandom, new DHParameters(p.bigInteger, g.bigInteger, q.bigInteger)))
      val r: BigInt = BigInt(keyGen.generateKeyPair().getPrivate.asInstanceOf[DHPrivateKeyParameters].getX) // randomly selected from Z_q
      val c: BigInt = BigInt(hash(l=l, m, g^k, g^r, groupHash(q, m)^r))
      val s: BigInt = r + (c * k) % q
      (c, s)
    }

    DiscreteLogProof(c, s)
  }

  /**
    * Hash function that maps to [0, 2&#94;l], translated to {0, 1}&#94;l
    *
    * Refer to
    *   - https://crypto.stackexchange.com/questions/37305/how-can-i-instantiate-a-generalized-hash-function
    *
    * @param l        the length of the output in bits
    * @param inputs   inputs to be concatenated and hashed
    * @return
    */
  def hash(l: Int, inputs: BigInt*): Array[Byte] = {
    val H: Array[Byte] = hashFunction(inputs.foldLeft(new Array[Byte](0))((a: Array[Byte], b) => a ++ b.toByteArray))
    val hashGen = new KDF2BytesGenerator(new Blake2bDigest(H))
    val hashOutput: Array[Byte] = Array.fill[Byte](Math.ceil(l/8).toInt)(0)
    hashGen.init(new ISO18033KDFParameters(Array[Byte]()))
    hashGen.generateBytes(hashOutput, 0, hashOutput.length)
    hashOutput
  }

  /**
    * We take a strong hash function here over [0, ~q], mod by q-2, putting in the range [0, q-2], add 2 to put it into [2, q]
    * By squaring this mod (2q + 1), we achieve a hash uniformly distributed over the quadratic residues of Z_{2q + 1},
    * a subgroup which is of order q
    *
    * Refer to
    *   - https://crypto.stackexchange.com/questions/39877/what-is-a-cyclic-group-of-prime-order-q-such-that-the-dlp-is-hard
    *   - https://crypto.stackexchange.com/questions/39903/how-to-construct-a-hash-function-into-a-cyclic-group-such-that-its-discrete-log/39918#39918
    *   - https://crypto.stackexchange.com/questions/37305/how-can-i-instantiate-a-generalized-hash-function
    *
    * @param q        the (Sophie Germain) prime order (i.e. 2q + 1 is a safe prime)
    * @param inputs   inputs to be concatenated
    * @return         A uniformly selected element of the group with order q
    */
  def groupHash(q: BigInt, inputs: BigInt*): BigInt = {
    val H = (BigInt(hash(q.bitLength, inputs:_*)) % (q - 2)) + 2
    (H % (2*q + 1))^2
  }
}
