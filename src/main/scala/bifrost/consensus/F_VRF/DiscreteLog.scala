package bifrost.consensus.F_VRF

case class DiscreteLog(b: BigInt, x: BigInt) extends Function[(BigInt, BigInt), BigInt]
