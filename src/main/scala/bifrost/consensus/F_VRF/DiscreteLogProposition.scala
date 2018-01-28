package bifrost.consensus.F_VRF

import scorex.core.serialization.Serializer

/**
  * Proposition that log_g(y) = log_a(b)
  *
  * @param g  generator of a group of prime order m
  * @param y  public key generated originally from g&#94;k, k in Z_m
  * @param a  H(x)&#94;r, where H: Z => {0,1}&#94;lambda
  * @param b  a&#94;k
  */
case class DiscreteLogProposition(g: BigInt, y: BigInt, a: BigInt, b: BigInt) extends EqualityProposition[(BigInt, BigInt), BigInt, DiscreteLog] {

  override type M = DiscreteLogProposition
  def serializer: Serializer[DiscreteLogProposition] = ???

  def verify(input1: (BigInt, BigInt), input2: (BigInt, BigInt), proof: Array[Byte]): Boolean = ???
}
