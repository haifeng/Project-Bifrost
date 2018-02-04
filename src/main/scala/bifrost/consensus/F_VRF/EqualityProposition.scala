package bifrost.consensus.F_VRF

import scorex.core.transaction.box.proposition.Proposition
import scorex.core.transaction.proof.Proof

abstract class EqualityProposition[A <: AnyVal, B <: AnyVal, F <: Function[A, B]] extends Proposition {

  def verify(proof: Proof[Proposition], sample: Array[Byte]): Boolean
}
