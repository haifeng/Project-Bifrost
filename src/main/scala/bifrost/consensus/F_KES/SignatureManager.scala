package bifrost.consensus.F_KES

class SignatureManager[S <: SignatureScheme](ss: S) {
  def verify(signature: Array[Byte], message: Array[Byte]): Boolean = ss.verify(signature, message)
  def sign(message: Array[Byte]): Array[Byte] = ss.sign(message)
}
