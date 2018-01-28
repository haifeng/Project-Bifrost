package bifrost.consensus.F_VRF

import scorex.core.transaction.proof.Proof

/**
  *
  */
class VRF2HashDH extends VerifiablyRandomFunction[Array[Byte], DiscreteLogProposition] {

  override def sample: (Array[Byte], Proof[DiscreteLogProposition]) = ???

  override def verify(proposition: DiscreteLogProposition, proof: Proof[DiscreteLogProposition], sample: Array[Byte]): Boolean = ???

}
