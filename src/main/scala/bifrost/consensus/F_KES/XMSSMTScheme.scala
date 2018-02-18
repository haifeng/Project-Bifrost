package bifrost.consensus.F_KES

import org.bouncycastle.crypto.Digest
import org.bouncycastle.pqc.crypto.xmss.{XMSSMTParameters, XMSSMTPrivateKeyParameters, XMSSMTPublicKeyParameters, XMSSMTSigner}

/**
  * This implements the XMSS Multi-tree variant hash-based signature scheme. See the
  * following paper for optimal parameters (https://eprint.iacr.org/2017/966.pdf)
  * For our concerns, we'll probably want a tree of height 60 with 3 layers, for which
  * a key pair can generate 2^60 signatures
  *
  * @param height   height of the overall tree
  * @param layers   number of layers of XMSS trees within the overall tree
  * @param digest   the underlying hash function to use for signatures (Keccak recommended)
  */
class XMSSMTScheme(height: Int = 60, layers: Int = 3, digest: Digest) extends ForwardSecureSignatureScheme {
  val signer: XMSSMTSigner = new XMSSMTSigner()
  val verifier: XMSSMTSigner = new XMSSMTSigner()

  val params: XMSSMTParameters = new XMSSMTParameters(height, layers, digest)

  signer.init(true, new XMSSMTPrivateKeyParameters.Builder(params).build())
  verifier.init(false, new XMSSMTPublicKeyParameters.Builder(params).build())

  override def sign(message: Array[Byte]): Array[Byte] = signer.generateSignature(message)
  override def verify(message: Array[Byte], signature: Array[Byte]): Boolean = verifier.verifySignature(message, signature)
  override def updatePrivateKey(): Unit = {
    val newSK = signer.getUpdatedPrivateKey
    signer.init(true, newSK)
  }
}
