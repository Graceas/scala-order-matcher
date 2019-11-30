package ie.ordermatcher

import ie.ordermatcher.model.{Order, OrderBook, OrderBookHistory}
import ie.ordermatcher.types.InstrumentType.InstrumentType
import ie.ordermatcher.types.OrderType
import ie.ordermatcher.types.OrderType.OrderType

class OrderMatcher(
  val instrument:       InstrumentType,
  val orderBook:        OrderBook,
  val orderBookHistory: OrderBookHistory
) {

  def start(): Unit = {
    Boot.log(s"Execute $instrument instrument")

    Boot.log(orderBook.sellOrders.toString())
    Boot.log(orderBook.buyOrders.toString())

    while (tick()) {
      // execute all orders
    }

    Boot.log(orderBook.sellOrders.toString())
    Boot.log(orderBook.buyOrders.toString())
  }

  def getAskPrice: Int = orderBook.sellOrders.head.price

  def getBidPrice: Int = orderBook.buyOrders.head.price

  def addOrder(order: Order): Boolean = {
    order.orderType match {
      case OrderType.BUY  => addBuyOrder(order)
      case OrderType.SELL => addSellOrder(order)
      case _              => false
    }
  }

  def cancelAllOrders(): Unit = {

  }

  private def tick(): Boolean = {

    if (orderBook.sellOrders.isEmpty || orderBook.buyOrders.isEmpty) {
      // sell or buy orders is not found
      return false
    }

    Boot.log(s"$instrument: Ask=$getAskPrice, Bid=$getBidPrice, Delta=${getAskPrice - getBidPrice}")
    trade(getEntry(OrderType.SELL))
  }

  private def trade(order: Order): Boolean = {

    // get order from other pull
    val otherOrder: Order = getEntry(order.orderType)
    if (canTrade(order.orderType, order.price, otherOrder.price)) {
      val delta: Int = order.volume - otherOrder.volume

      // update users balances
      order.client.balance += order.volume * order.price
      otherOrder.client.instrumentBalances(instrument) += order.volume

      if (delta < 0) { // sell order filled
        // update volume for both orders
        updateEntryVolume(order.orderType, 0)
        updateEntryVolume(otherOrder.orderType, otherOrder.volume - order.volume)
        // remove filled order
        removeEntry(order.orderType)
      }

      if (delta == 0) { // both orders filled
        // update volume for both orders
        updateEntryVolume(order.orderType, 0)
        updateEntryVolume(otherOrder.orderType, 0)
        //remove both filled orders
        removeEntry(order.orderType)
        removeEntry(otherOrder.orderType)
      }

      if (delta > 0) { // buy orders filled
        // update volume for both orders
        updateEntryVolume(order.orderType, order.volume - otherOrder.volume)
        updateEntryVolume(otherOrder.orderType, 0)
        // remove filled order
        removeEntry(otherOrder.orderType)
      }

      true
    } else {
      false
    }
  }

  private def canTrade(orderType: OrderType, orderPrice: Int, entryPrice: Int): Boolean =
    if (orderType == OrderType.BUY)
      orderPrice >= entryPrice
    else
      orderPrice <= entryPrice

  private def getEntry(orderType: OrderType): Order =
    if (orderType == OrderType.BUY)
      orderBook.sellOrders.head
    else
      orderBook.buyOrders.head

  private def updateEntryVolume(orderType: OrderType, volume: Int): Order =
    if (orderType == OrderType.BUY) {
      orderBook.buyOrders.update(0, orderBook.buyOrders.head.copy(volume = volume))
      orderBook.buyOrders.head
    } else {
      orderBook.sellOrders.update(0, orderBook.sellOrders.head.copy(volume = volume))
      orderBook.sellOrders.head
    }

  private def removeEntry(orderType: OrderType): Order =
    if (orderType == OrderType.BUY)
      orderBook.buyOrders.remove(0)
    else
      orderBook.sellOrders.remove(0)

  private def addBuyOrder(order: Order): Boolean = {
    if (
      order.volume <= 0 ||
      order.price <= 0  ||
      order.instrument != instrument ||
      order.client.balance < order.price * order.volume
    ) {
      false
    } else {
      // hold user balance
      order.client.balance -= order.price * order.volume
      orderBook.buyOrders.append(order)

      true
    }
  }

  private def addSellOrder(order: Order): Boolean = {
    if (
      order.volume <= 0 ||
      order.price <= 0  ||
      order.instrument != instrument ||
      !order.client.instrumentBalances.contains(order.instrument) ||
      order.client.instrumentBalances(order.instrument) < order.volume
    ) {
      false
    } else {
      // hold user instrument balance
      order.client.instrumentBalances(order.instrument) -= order.volume
      orderBook.sellOrders.append(order)

      true
    }
  }
}
