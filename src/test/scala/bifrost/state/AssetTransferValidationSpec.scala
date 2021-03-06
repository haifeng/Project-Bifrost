package bifrost.state

import java.time.Instant

import bifrost.blocks.BifrostBlock
import bifrost.transaction.{AssetRedemption, AssetTransfer}
import bifrost.transaction.box._
import com.google.common.primitives.Ints
import io.iohk.iodb.ByteArrayWrapper
import scorex.core.transaction.box.proposition.PublicKey25519Proposition
import scorex.core.transaction.proof.Signature25519
import scorex.crypto.signatures.Curve25519

import scala.util.Failure

/**
  * Created by Matt Kindy on 6/7/2017.
  */
class AssetTransferValidationSpec extends BifrostStateSpec {

  property("A block with valid AssetTransfer should result in more tokens for receiver, fewer for sender") {
    forAll(validAssetTransferGen) {
      at: AssetTransfer =>
        val block = BifrostBlock(
          Array.fill(BifrostBlock.SignatureLength)(-1: Byte),
          Instant.now.toEpochMilli,
          ArbitBox(PublicKey25519Proposition(Array.fill(Curve25519.KeyLength)(0: Byte)), 0L, 0L),
          Signature25519(Array.fill(BifrostBlock.SignatureLength)(0: Byte)),
          Seq(at)
        )

        val preExistingAssetBoxes: Set[BifrostBox] = at.from.map(f => AssetBox(f._1, f._2, at.to.map(_._2).sum, at.assetCode, at.hub)).toSet

        val assetBoxes: Traversable[AssetBox] = at.newBoxes.map {
          case a: AssetBox => a
          case _ => throw new Exception("Was expecting AssetBoxes but found something else")
        }

        val necessaryBoxesSC = BifrostStateChanges(
          Set(),
          preExistingAssetBoxes,
          Instant.now.toEpochMilli
        )

        val preparedState = BifrostStateSpec.genesisState.applyChanges(necessaryBoxesSC, Ints.toByteArray(1)).get
        val newState = preparedState.applyChanges(preparedState.changes(block).get, Ints.toByteArray(2)).get

        at.newBoxes.forall(b => newState.storage.get(ByteArrayWrapper(b.id)) match {
          case Some(wrapper) => wrapper.data sameElements b.bytes
          case None => false
        })

        /* Expect none of the prexisting boxes to still be around */
        require(preExistingAssetBoxes.forall(pb => newState.storage.get(ByteArrayWrapper(pb.id)).isEmpty))

        BifrostStateSpec.genesisState = newState.rollbackTo(BifrostStateSpec.genesisBlockId).get

    }
  }

  property("Attempting to validate an AssetTransfer with a bad signature should error") {
    forAll(validAssetTransferGen) {
      at: AssetTransfer =>

        val headSig = at.signatures.head
        val wrongSig: Array[Byte] = (headSig.bytes.head + 1).toByte +: headSig.bytes.tail
        val wrongSigs: IndexedSeq[Signature25519] = Signature25519(wrongSig) +: at.signatures.tail
        val invalidAR = at.copy(signatures = wrongSigs)

        val preExistingAssetBoxes: Set[BifrostBox] = at.from.map(f => AssetBox(f._1, f._2, at.to.map(_._2).sum, at.assetCode, at.hub)).toSet

        val necessaryBoxesSC = BifrostStateChanges(
          Set(),
          preExistingAssetBoxes,
          Instant.now.toEpochMilli
        )

        val preparedState = BifrostStateSpec.genesisState.applyChanges(necessaryBoxesSC, Ints.toByteArray(1)).get
        val newState = preparedState.validate(invalidAR)

        BifrostStateSpec.genesisState = preparedState.rollbackTo(BifrostStateSpec.genesisBlockId).get

        newState shouldBe a[Failure[_]]
        newState.failed.get.getMessage shouldBe "Incorrect unlocker"
    }
  }

  property("Attempting to validate an AssetTransfer for an amount you do not have should error") {
    forAll(validAssetTransferGen) {
      at: AssetTransfer =>

        val preExistingAssetBoxes: Set[BifrostBox] = at.from.map(f => AssetBox(f._1, f._2, 0, at.assetCode, at.hub)).toSet

        val necessaryBoxesSC = BifrostStateChanges(
          Set(),
          preExistingAssetBoxes,
          Instant.now.toEpochMilli
        )

        val preparedState = BifrostStateSpec.genesisState.applyChanges(necessaryBoxesSC, Ints.toByteArray(1)).get
        val newState = preparedState.validate(at)

        BifrostStateSpec.genesisState = preparedState.rollbackTo(BifrostStateSpec.genesisBlockId).get

        newState shouldBe a[Failure[_]]
        newState.failed.get.getMessage shouldBe "Not enough assets"
    }
  }
}
