package bifrost.consensus.F_VRF

import scorex.core.transaction.box.proposition.Proposition

abstract class EqualityProposition[A <: AnyVal, B <: AnyVal, F <: Function[A, B]] extends Proposition {

  def verify(input1: A, input2: A, proof: Array[Byte]): Boolean

}
