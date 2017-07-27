package bifrost.exchange

import akka.actor.{Actor, ActorLogging, ActorRef}
import bifrost.network.PeerMessageManager
import serializer.{BuySellOrder, PeerMessage}

import scala.util.{Failure, Success}

class ExchangeManager(viewHolderRef: ActorRef, private var orderManager: PeerMessageManager = PeerMessageManager.emptyManager) extends Actor with ActorLogging {
  import ExchangeManager._

  private def handleOrder: Receive = {
    case PeerMessageReceived(p) =>
      val order = BuySellOrder.parseFrom(p.messageBytes.toByteArray)
      orderManager.put(p) match {
        case Success(updatedManager) =>
          orderManager = updatedManager
          log.debug(s"Order $p Added")
        case f: Failure[PeerMessageManager] => throw f.failed.get
      }
  }

  private def getMessageManager: Receive = {
    case GetMessageManager =>
      sender() ! MessageManager(orderManager)
  }

  override def receive: Receive = {
    case NewOrder(order: BuySellOrder) =>

  }
}

object ExchangeManager {
  case class NewOrder(buySellOrder: BuySellOrder)
  case class Cancel(timestamp: Long, order: NewOrder)
  case class Amend(timestamp: Long, order:NewOrder, newPrice:Option[Double], newQty:Option[Long])

  case class PeerMessageReceived(p: PeerMessage)

  case object GetMessageManager

  case class MessageManager(m: PeerMessageManager)
}