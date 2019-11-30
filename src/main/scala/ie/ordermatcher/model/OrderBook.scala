package ie.ordermatcher.model

import scala.collection.mutable

case class OrderBook(
  buyOrders:  mutable.Map[String, Order] = mutable.Map.empty,
  sellOrders: mutable.Map[String, Order] = mutable.Map.empty,
)
