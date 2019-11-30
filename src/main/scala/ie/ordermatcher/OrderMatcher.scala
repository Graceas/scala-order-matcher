package ie.ordermatcher

import ie.ordermatcher.model._
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

    while (tick()) {
      // execute all orders
    }
  }

  def getAskPrice: Option[Int] = getEntryFromOtherPull(OrderType.BUY).map(_.price)

  def getBidPrice: Option[Int] = getEntryFromOtherPull(OrderType.SELL).map(_.price)

  def addOrder(order: Order): Boolean = {
    order.orderType match {
      case OrderType.BUY  => addBuyOrder(order)
      case OrderType.SELL => addSellOrder(order)
      case _              => false
    }
  }

  def cancelAllOrders(): Unit = {
    // release user money and instruments balance
    orderBook.sellOrders.foreach(e => {
      updateClientInstrumentBalance(e._2, e._2.volume)
    })
    orderBook.buyOrders.foreach(e => {
      updateClientBalance(e._2, e._2.price * e._2.volume)
    })

    orderBook.sellOrders.clear()
    orderBook.buyOrders.clear()
  }

  private def tick(): Boolean = {

    if (orderBook.sellOrders.isEmpty || orderBook.buyOrders.isEmpty) {
      // sell or buy orders is not found
      return false
    }

    Boot.log(s"$instrument: Ask=$getAskPrice, Bid=$getBidPrice, Spread=${(getAskPrice.getOrElse(0) - getBidPrice.getOrElse(0)).abs}")
    trade(getEntryFromOtherPull(OrderType.BUY).get)
  }

  private def trade(order: Order): Boolean = {
    // get order from other pull
    val otherOrder: Order = getEntryFromOtherPull(order.orderType).get
    if (canTrade(order.orderType, order.price, otherOrder.price)) {
      val delta: Int = order.volume - otherOrder.volume

      val tradeVolume: Int = if (delta <= 0) order.volume else otherOrder.volume
      // update users balances
      updateClientBalance(order, tradeVolume * order.price)
      updateClientInstrumentBalance(otherOrder, tradeVolume)
      // update volume for both orders
      updateEntryVolume(order, volume = -tradeVolume)
      updateEntryVolume(otherOrder, volume = -tradeVolume)
      if (order.volume == 0) {
        // remove sell filled order
        removeEntry(order)
      }
      if (otherOrder.volume == 0) {
        // remove buy filled order
        removeEntry(otherOrder)
      }

      // add trade entry to history
      orderBookHistory.tradeEntries.append(TradeEntry(
        order   = order,
        order2  = otherOrder,
        volume  = tradeVolume,
        price   = order.price,
      ))

      true
    } else {
      false
    }
  }

  private def canTrade(orderType: OrderType, orderPrice: Int, entryPrice: Int): Boolean = {
    if (orderType == OrderType.BUY) {
      orderPrice >= entryPrice
    } else {
      orderPrice <= entryPrice
    }
  }

  private def getEntryFromOtherPull(orderType: OrderType): Option[Order] = {
    if (orderType == OrderType.BUY) {
      // sort by time asc, then price asc
      orderBook.sellOrders
        .values
        .toList
        .sortBy(order => order.nonce + order.orderTime)(Ordering[Long])
        .sortBy(_.price)(Ordering[Int])
        .headOption
    } else {
      // sort by time asc, then price desc
      orderBook.buyOrders
        .values
        .toList
        .sortBy(order => order.nonce + order.orderTime)(Ordering[Long])
        .sortBy(_.price)(Ordering[Int].reverse)
        .headOption
    }
  }

  private def removeEntry(order: Order): Unit = {
    if (order.orderType == OrderType.BUY) {
      orderBook.buyOrders -= order.id
    } else {
      orderBook.sellOrders -= order.id
    }
  }

  private def updateEntryVolume(order: Order, volume: Int): Unit = {
    if (order.orderType == OrderType.BUY) {
      orderBook.buyOrders(order.id).volume += volume
    } else {
      orderBook.sellOrders(order.id).volume += volume
    }
  }

  private def updateClientBalance(order: Order, balance: Int): Unit = {
    if (order.orderType == OrderType.BUY) {
      orderBook.buyOrders(order.id).client.balance += balance
    } else {
      orderBook.sellOrders(order.id).client.balance += balance
    }
  }

  private def updateClientInstrumentBalance(order: Order, balance: Int): Unit = {
    if (order.orderType == OrderType.BUY) {
      if (!orderBook.buyOrders(order.id).client.instrumentBalances.contains(instrument)) {
        orderBook.buyOrders(order.id).client.instrumentBalances(instrument) = 0
      }
      orderBook.buyOrders(order.id).client.instrumentBalances(instrument) += balance
    } else {
      if (!orderBook.sellOrders(order.id).client.instrumentBalances.contains(instrument)) {
        orderBook.sellOrders(order.id).client.instrumentBalances(instrument) = 0
      }
      orderBook.sellOrders(order.id).client.instrumentBalances(instrument) += balance
    }
  }

  private def addBuyOrder(order: Order): Boolean = {
    if (
      order.volume <= 0 ||
      order.price <= 0  ||
      order.instrument != instrument ||
      order.client.balance < order.price * order.volume ||
      orderBook.buyOrders.contains(order.id)
    ) {
      false
    } else {
      orderBook.buyOrders(order.id) = order
      updateClientBalance(order, -1 * order.price * order.volume)

      true
    }
  }

  private def addSellOrder(order: Order): Boolean = {
    if (
      order.volume <= 0 ||
      order.price <= 0  ||
      order.instrument != instrument ||
      !order.client.instrumentBalances.contains(order.instrument) ||
      order.client.instrumentBalances(order.instrument) < order.volume ||
      orderBook.sellOrders.contains(order.id)
    ) {
      false
    } else {
      orderBook.sellOrders(order.id) = order
      updateClientInstrumentBalance(order, -1 * order.volume)

      true
    }
  }
}
