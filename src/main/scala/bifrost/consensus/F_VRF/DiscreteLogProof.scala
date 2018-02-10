package bifrost.consensus.F_VRF

import java.security.SecureRandom

import org.bouncycastle.crypto.generators.DHBasicKeyPairGenerator
import org.bouncycastle.crypto.params.{DHKeyGenerationParameters, DHParameters, DHPrivateKeyParameters}
import scorex.core.serialization.Serializer
import scorex.core.transaction.box.proposition.Proposition
import scorex.core.transaction.proof.Proof

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

  /**
    * @param m  argument used as parameter for PRF
    * @param g  generator of a group of prime order q
    * @param k  private key randomly selected from Z_q
    * @param q  prime order
    */
  def apply(m: BigInt, g: BigInt, hm: BigInt, k: BigInt, q: BigInt, p: BigInt): DiscreteLogProof = {
    val (c: BigInt, s: BigInt) = {
      val keyGen: DHBasicKeyPairGenerator = new DHBasicKeyPairGenerator()
      keyGen.init(new DHKeyGenerationParameters(SecureRandom, new DHParameters(p.bigInteger, g.bigInteger, q.bigInteger)))
      val r: BigInt = BigInt(keyGen.generateKeyPair().getPrivate.asInstanceOf[DHPrivateKeyParameters].getX) // randomly selected from Z_q
      val c: BigInt = BigInt(hash(m, g^k, g^r, hm^r))
      val s: BigInt = r + (c * k) % q
      (c, s)
    }

    DiscreteLogProof(c, s)
  }

  def hash(inputs: BigInt*): Array[Byte] = scorex.crypto.hash.Blake2b512(inputs.foldLeft(new Array[Byte](0))((a: Array[Byte], b) => a ++ b.toByteArray))

  /**
    * Refer to
    *   - https://crypto.stackexchange.com/questions/39877/what-is-a-cyclic-group-of-prime-order-q-such-that-the-dlp-is-hard
    *   - https://crypto.stackexchange.com/questions/39903/how-to-construct-a-hash-function-into-a-cyclic-group-such-that-its-discrete-log/39918#39918
    *   - https://crypto.stackexchange.com/questions/17990/sha256-output-to-0-99-number-range/17994#17994
    *
    * We take a strong hash function here, mod by q-2, putting in the range [0, q-2], add 2 to put it into [2, q]
    * By squaring this mod (2q + 1), we achieve a hash uniformly distributed over the quadratic residues of Z_{2q + 1},
    * a subgroup which is of order q
    *
    * @param q        the (Sophie Germain) prime order (i.e. 2q + 1 is a safe prime)
    * @param inputs   inputs to be concatenated
    * @return         A uniformly selected element of the group with order q
    */
  def groupHash(q: BigInt, inputs: BigInt*): BigInt = (BigInt(hash(inputs:_*)) % (2*q + 1))^2

}
