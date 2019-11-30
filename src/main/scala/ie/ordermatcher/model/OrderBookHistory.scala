package ie.ordermatcher.model

import scala.collection.mutable.ListBuffer

case class OrderBookHistory(
  tradeEntries: ListBuffer[TradeEntry] = ListBuffer.empty,
)
