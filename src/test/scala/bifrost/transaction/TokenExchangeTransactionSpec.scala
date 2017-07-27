package bifrost.transaction

import bifrost.{BifrostGenerators, ValidGenerators}
import bifrost.state.BifrostState
import org.scalatest.prop.{GeneratorDrivenPropertyChecks, PropertyChecks}
import org.scalatest.{Matchers, PropSpec}

class TokenExchangeTransactionSpec extends PropSpec
  with PropertyChecks
  with GeneratorDrivenPropertyChecks
  with Matchers
  with BifrostGenerators
  with ValidGenerators {

  forAll(validTokenExchangeTxGen) {
    tex: TokenExchangeTransaction =>
      if (BifrostState.semanticValidity(tex).isFailure) {
        val error = BifrostState.semanticValidity(tex).failed.get
        error.printStackTrace()
        throw error
      }
      BifrostState.semanticValidity(tex).isSuccess shouldBe true
  }
}