package bifrost.consensus.F_KES

trait CanVerify {
  def verify(signature: Array[Byte], message: Array[Byte]): Boolean
}
