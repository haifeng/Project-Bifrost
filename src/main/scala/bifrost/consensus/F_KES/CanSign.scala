package bifrost.consensus.F_KES

trait CanSign {
  def sign(message: Array[Byte]): Array[Byte]
}
