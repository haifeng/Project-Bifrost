package bifrost.consensus.F_VRF

import java.security.SecureRandom
import javax.crypto.interfaces.DHPrivateKey

import org.bouncycastle.crypto.KeyGenerationParameters
import org.bouncycastle.crypto.generators.DHBasicKeyPairGenerator
import org.bouncycastle.crypto.params.{DHKeyGenerationParameters, DHKeyParameters, DHParameters, DHPrivateKeyParameters}
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
  def apply(m: BigInt, g: BigInt, hm: BigInt, k: BigInt, q: BigInt): DiscreteLogProof = {
    val (c: BigInt, s: BigInt) = {
      val keyGen: DHBasicKeyPairGenerator = new DHBasicKeyPairGenerator()
      keyGen.init(new DHKeyGenerationParameters(SecureRandom, new DHParameters(q.bigInteger, g.bigInteger)))
      val r: BigInt = BigInt(keyGen.generateKeyPair().getPrivate.asInstanceOf[DHPrivateKeyParameters].getX) // randomly selected from Z_q
      val c: BigInt = BigInt(hash(m, g^k, g^r, hm^r))
      val s: BigInt = r + c * k % g // needs to  be mod q, not g
      (c, s)
    }

    DiscreteLogProof(c, s)
  }

  // TODO these are just placeholder hashes!
  def hash(inputs: BigInt*): Array[Byte] = scorex.crypto.hash.Blake2b256(inputs.foldLeft(new Array[Byte](0))((a: Array[Byte], b) => a ++ b.toByteArray))
  def groupHash(inputs: BigInt*): BigInt = BigInt(scorex.crypto.hash.Blake2b256(inputs.foldLeft(new Array[Byte](0))((a: Array[Byte], b) => a ++ b.toByteArray)))

}
