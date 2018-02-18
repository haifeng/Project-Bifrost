package bifrost.consensus.F_RLB

import java.security.SecureRandom

import bifrost.consensus.Epoch
import bifrost.history.BifrostHistory

class EpochNonceOracle(l: Int) {

  def generateNonce(h: BifrostHistory): Epoch.Nonce = if(h.height < 1) {
    val rnd = new Array[Byte](l)
    SecureRandom.getInstanceStrong.nextBytes(rnd)
    rnd
  } else {
    ???
  }

}
