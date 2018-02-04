package bifrost.consensus.F_VRF

import scorex.core.serialization.Serializer
import scorex.core.transaction.box.proposition.Proposition
import scorex.core.transaction.proof.Proof
import scorex.core.transaction.state.PrivateKey25519
import scorex.crypto.hash.Blake2b256

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
    * @param g  generator of a group of prime order m
    * @param v  public key generated originally from g&#94;k, k in Z_m
    * @param a  H(m)&#94;r, where H: Z => {0,1}&#94;lambda
    * @param b  a&#94;k
    * @param k
    */
  def apply(m: BigInt, g: BigInt, v: BigInt, a: BigInt, b: BigInt, k: PrivateKey25519): DiscreteLogProof = {
    val (c: BigInt, s: BigInt) = {
      val r: BigInt = ??? // randomly selected from Z_m
      val c: BigInt = BigInt(hash(m, v, g^r, a))
      val s: BigInt = r + c * BigInt(k.privKeyBytes) % g // needs to  be mod q, not g
      (c, s)
    }

    DiscreteLogProof(c, s)
  }

  // TODO these are just placeholder hashes!
  def hash(inputs: BigInt*): Blake2b256.Digest = scorex.crypto.hash.Blake2b256(inputs.foldLeft(new Array[Byte](0))((a: Array[Byte], b) => a ++ b.toByteArray))
  def groupHash(inputs: BigInt*): BigInt = BigInt(scorex.crypto.hash.Blake2b256(inputs.foldLeft(new Array[Byte](0))((a: Array[Byte], b) => a ++ b.toByteArray)))

}
