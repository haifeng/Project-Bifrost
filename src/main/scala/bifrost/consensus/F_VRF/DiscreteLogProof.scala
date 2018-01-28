package bifrost.consensus.F_VRF

import scorex.core.serialization.Serializer
import scorex.core.transaction.box.proposition.Proposition
import scorex.core.transaction.proof.Proof

/**
  * NIZK proof that log_g(y) = log_a(b)
  *
  * @param g  generator of a group of prime order m
  * @param y  public key generated originally from g&#94;k, k in Z_m
  * @param a  H(x)&#94;r, where H: Z => {0,1}&#94;lambda
  * @param b  a&#94;k
  */
class DiscreteLogProof(g: BigInt, y: BigInt, a: BigInt, b: BigInt) extends Proof[DiscreteLogProposition] {

  override type M = DiscreteLogProof
  def serializer: Serializer[DiscreteLogProposition] = ???
  override def isValid(proposition: Proposition, message: Array[Byte]): Boolean = ???
}
