package ie.ordermatcher.model

import scala.collection.mutable.ListBuffer

case class OrderBook(
  buyOrders:  ListBuffer[Order] = ListBuffer.empty,
  sellOrders: ListBuffer[Order] = ListBuffer.empty,
)
