package bifrost.consensus.F_VRF

import scorex.core.transaction.proof.Proof
import scorex.core.transaction.state.PrivateKey25519

/**
  *
  */
class VRF2HashDH(k: PrivateKey25519) extends VerifiablyRandomFunction[Array[Byte], DiscreteLogProposition] {

  override def sample: (Array[Byte], Proof[DiscreteLogProposition]) = ???

  override def verify(proposition: DiscreteLogProposition,
                      proof: Proof[DiscreteLogProposition],
                      sample: Array[Byte]): Boolean = proof.isValid(proposition, sample)
}
