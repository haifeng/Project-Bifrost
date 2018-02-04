package bifrost.consensus.F_VRF

import scorex.core.serialization.Serializer
import scorex.core.transaction.box.proposition.Proposition
import scorex.core.transaction.proof.Proof

/**
  * Proposition that log_g(v) = log_(H(m))(u)
  *
  * @param m  argument used as parameter for PRF
  * @param g  generator of a group of prime order q
  * @param v  public key generated originally from g&#94;k, k in Z_m
  * @param u  H(m)&#94;k
  */
case class DiscreteLogProposition(m: BigInt, g: BigInt, v: BigInt, u: BigInt) extends EqualityProposition[(BigInt, BigInt), BigInt, DiscreteLog] {

  override type M = DiscreteLogProposition
  def serializer: Serializer[DiscreteLogProposition] = ???

  def verify(proof: Proof[Proposition], sample: Array[Byte]): Boolean = proof match {
    case d: DiscreteLogProof =>
      d.c.equals(BigInt(DiscreteLogProof.hash(m, v, g^d.s / v^d.c, DiscreteLogProof.groupHash(m)^d.s/u^d.c))) &&
        sample.sameElements(DiscreteLogProof.hash(m, u))
    case _ => false
  }
}
