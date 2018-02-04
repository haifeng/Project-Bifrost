package bifrost.consensus.F_VRF

import scorex.core.transaction.box.proposition.Proposition
import scorex.core.transaction.proof.Proof

/**
  *
  * @tparam O
  * @tparam P
  */
trait VerifiablyRandomFunction[O <: AnyVal, P <: Proposition] {

  def apply(params: BigInt*): (O, P)
  def verify(proposition: P, proof: Proof[P], sample: O): Boolean

}
