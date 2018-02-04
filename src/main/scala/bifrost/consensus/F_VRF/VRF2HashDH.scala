package bifrost.consensus.F_VRF

import scorex.core.transaction.proof.Proof

/**
  *
  * @param k  private key selected from Z_q
  * @param g  generator for group of prime order q
  * @param q  prime order
  */
class VRF2HashDH(k: BigInt, g: BigInt, q: BigInt) extends VerifiablyRandomFunction[Array[Byte], DiscreteLogProposition] {

  override def apply(params: BigInt*): (Array[Byte], DiscreteLogProof) = {
    val m = params(0)
    (DiscreteLogProof.hash(m, DiscreteLogProof.groupHash(m)^k), DiscreteLogProof(m, g, DiscreteLogProof.groupHash(m), k, q))
  }

  override def verify(proposition: DiscreteLogProposition,
                      proof: Proof[DiscreteLogProposition],
                      sample: Array[Byte]): Boolean = proof.isValid(proposition, sample)
}
