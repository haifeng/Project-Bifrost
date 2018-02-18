package bifrost.consensus.F_KES

trait SignatureScheme extends CanSign with CanVerify {
  def verify(signature: Array[Byte], message: Array[Byte]): Boolean
  def sign(message: Array[Byte]): Array[Byte]
}
