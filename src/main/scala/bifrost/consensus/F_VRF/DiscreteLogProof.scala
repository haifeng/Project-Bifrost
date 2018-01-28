package bifrost.consensus.F_VRF

import scorex.core.serialization.Serializer
import scorex.core.transaction.box.proposition.Proposition
import scorex.core.transaction.proof.Proof
import scorex.core.transaction.state.PrivateKey25519
import scorex.crypto.hash.Blake2b256

/**
  * NIZK proof that log_g(y) = log_a(b), covered by random t
  *
  * @param w  derived as the hash: H(g, y, a, b, g&#94;t, a&#94;t)
  * @param s  dervied as t + w * k % g.mod
  */
case class DiscreteLogProof(w: BigInt, s: BigInt) extends Proof[DiscreteLogProposition] {

  override type M = DiscreteLogProof

  def serializer: Serializer[DiscreteLogProposition] = ???

  // TODO implement verification of w,s by proposition
  override def isValid(proposition: Proposition, sample: Array[Byte]): Boolean = proposition match {
    case p: DiscreteLogProposition => ???
    case _ => false
  }
}

object DiscreteLogProof {

  /**
    * @param g  generator of a group of prime order m
    * @param y  public key generated originally from g&#94;k, k in Z_m
    * @param a  H(x)&#94;r, where H: Z => {0,1}&#94;lambda
    * @param b  a&#94;k
    * @param k
    */
  def apply(g: BigInt, y: BigInt, a: BigInt, b: BigInt, k: PrivateKey25519): DiscreteLogProof = {
    val (w: BigInt, s: BigInt) = {
      val t: BigInt = ??? // randomly selected from Z_m
      val w: BigInt = BigInt(hash(g, y, a, b, g^t, a^t))
      val s: BigInt = t + w * BigInt(k.privKeyBytes) % g // needs to  be mod m, not g
      (w, s)
    }

    DiscreteLogProof(w, s)
  }

  // TODO this is just a placeholder hash!
  def hash(inputs: BigInt*): Blake2b256.Digest = scorex.crypto.hash.Blake2b256(inputs.foldLeft(new Array[Byte](0))((a: Array[Byte], b) => a ++ b.toByteArray))

}
