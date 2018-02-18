package bifrost.consensus.F_KES

import bifrost.blocks.BifrostBlock

class BlockSignatureManager[S <: ForwardSecureSignatureScheme](ss: S) extends SignatureManager[S](ss) {
  def updatePrivateKey(): Unit = ss.updatePrivateKey()
  def signBlock(b: BifrostBlock): Array[Byte] = ss.sign(b.bytes)
  def verifyBlock(signature: Array[Byte], b: BifrostBlock): Boolean = ss.verify(signature, b.bytes)
}
