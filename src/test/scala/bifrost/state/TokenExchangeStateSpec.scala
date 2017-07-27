package bifrost.state

import java.time.Instant

import com.trueaccord.scalapb.json.JsonFormat
import bifrost.blocks.BifrostBlock
import bifrost.transaction.{ConversionTransaction, TokenExchangeTransaction}
import bifrost.transaction.box.{ArbitBox, AssetBox, BifrostBox, PolyBox}
import com.google.common.primitives.{Ints, Longs}
import com.google.protobuf.ByteString
import io.iohk.iodb.ByteArrayWrapper
import scorex.core.transaction.box.proposition.PublicKey25519Proposition
import scorex.core.transaction.proof.Signature25519
import scorex.crypto.signatures.Curve25519

import scala.util.Failure

class TokenExchangeStateSpec extends BifrostStateSpec {
  private def getPreExistingBoxes(tex: TokenExchangeTransaction): Set[BifrostBox] = {
    val polyAmounts = splitAmongN(tex.buyOrder.totalInputValue, tex.buyOrder.inputBoxes.size, 0, tex.buyOrder.totalInputValue).get
    val preExistingPolyBoxes: Set[BifrostBox] = tex.buyOrder.inputBoxes.zip(polyAmounts).map { case (box, amount) =>
      PolyBox(PublicKey25519Proposition(box.publicKey.toByteArray), box.nonce, amount)}.toSet

    val assetAmounts = splitAmongN(tex.sellOrder.totalInputValue, tex.sellOrder.inputBoxes.size, 0, tex.sellOrder.totalInputValue).get
    val preExistingAssetBoxes: Set[BifrostBox] = tex.sellOrder.inputBoxes.zip(assetAmounts).map { case (box, amount) =>
      AssetBox(PublicKey25519Proposition(box.publicKey.toByteArray), box.nonce, amount,
        tex.sellOrder.token1.tokenCode,
        PublicKey25519Proposition(tex.sellOrder.token1.tokenHub.get.toByteArray))
    }.toSet
    preExistingAssetBoxes ++ preExistingPolyBoxes
  }

  property("A block with valid TokenExchangeTransaction should result in the correct number of polys and assets being swapped") {
    forAll(validTokenExchangeTxGen) {
      tex: TokenExchangeTransaction =>
        val block = BifrostBlock(
          Array.fill(BifrostBlock.SignatureLength)(-1: Byte),
          Instant.now.toEpochMilli,
          ArbitBox(PublicKey25519Proposition(Array.fill(Curve25519.KeyLength)(0: Byte)), 0L, 0L),
          Signature25519(Array.fill(BifrostBlock.SignatureLength)(0: Byte)),
          Seq(tex)
        )
        // Setup pre-existing boxes
        val allPreExistingBoxes = getPreExistingBoxes(tex)

        val necessaryBoxesSC = BifrostStateChanges(
          Set(),
          allPreExistingBoxes,
          Instant.now.toEpochMilli
        )
        println(tex.newBoxes.toList)
        // Manipulate genesisState
        val preparedState = BifrostStateSpec.genesisState.applyChanges(necessaryBoxesSC, Ints.toByteArray(1)).get
        // Validate transaction
        val validationRes = BifrostStateSpec.genesisState.validate(tex)
        if (validationRes.isFailure) validationRes.failed.get.printStackTrace()
        require(validationRes.isSuccess)
        // Apply the transaction
        val newState = preparedState.applyChanges(preparedState.changes(block).get, Ints.toByteArray(2)).get
        // Check the result
        val newBoxes = tex.newBoxes.map(box => newState.closedBox(box.id).get)
        val polyBoxes = newBoxes.collect{case box: PolyBox => box}
        val assetBoxes = newBoxes.collect{case box: AssetBox => box}

        require(polyBoxes.collect{case box if box.proposition equals PublicKey25519Proposition(tex.sellOrder.publicKey.toByteArray) => box.value}.sum + tex.fee == tex.buyOrder.token2.quantity)
        require(polyBoxes.map(_.value).sum + tex.fee == tex.buyOrder.totalInputValue)

        require(assetBoxes.collect{case box if box.proposition equals PublicKey25519Proposition(tex.buyOrder.publicKey.toByteArray) => box.value}.sum == tex.sellOrder.token1.quantity)
        require(assetBoxes.map(_.value).sum == tex.sellOrder.totalInputValue)

        BifrostStateSpec.genesisState = newState.rollbackTo(BifrostStateSpec.genesisBlockId).get
    }
  }

  property("Attempting to validate a TokenExchangeTx with a bad signature should error") {
    forAll(validTokenExchangeTxGen) {
      tex: TokenExchangeTransaction =>
        val allPreExistingBoxes = getPreExistingBoxes(tex)

        val headSig = tex.buyOrder.signatures.map(s => s.toByteArray).head
        val wrongSig: Array[Byte] = (headSig.head + 1).toByte +: headSig.tail
        val wrongSigs = ByteString.copyFrom(wrongSig) +: tex.buyOrder.signatures.tail
        val wrongBuyOrder = tex.buyOrder.copy(signatures = wrongSigs)
        val invalidTex = tex.copy(buyOrder = wrongBuyOrder)

        val necessaryBoxesSC = BifrostStateChanges(
          Set(),
          allPreExistingBoxes,
          Instant.now.toEpochMilli
        )

        val preparedState = BifrostStateSpec.genesisState.applyChanges(necessaryBoxesSC, Ints.toByteArray(1)).get

        val newState = preparedState.validate(invalidTex)

        BifrostStateSpec.genesisState = preparedState.rollbackTo(BifrostStateSpec.genesisBlockId).get

        newState shouldBe a[Failure[_]]
        newState.failed.get.getMessage shouldBe "Incorrect unlocker"
    }
  }
}
