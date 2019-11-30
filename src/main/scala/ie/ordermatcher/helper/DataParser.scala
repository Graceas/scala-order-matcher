package ie.ordermatcher.helper

import ie.ordermatcher.{Client, InstrumentType, Order, OrderType}

import scala.collection.mutable.ListBuffer
import scala.io.Source

object DataParser {
  def clientsParse(resource: String): List[Client] = {
    val content: Iterator[String] = Source.fromResource(resource).getLines

    // parse objects
    val clients: ListBuffer[Client] = ListBuffer.empty
    content.foreach(line => {
      val clientDetails = line.split("\t").map(_.trim)

      clients += Client(
        name               = clientDetails(0),
        balance            = clientDetails(1).toInt,
        instrumentBalances = clientDetails
          .drop(2)
          .zip(InstrumentType.values)
          .map(value => value._2 -> value._1.toInt)
          .toMap
      )
    })

    clients.toList
  }

  def ordersParse(resource: String, clients: List[Client]): List[Order] = {
    val ordersContent:  Iterator[String] = Source.fromResource(resource).getLines

    val orders: ListBuffer[Order] = ListBuffer.empty
    ordersContent.foreach(line => {
      val orderDetails = line.split("\t").map(_.trim)

      orders += Order(
        client     = clients
          .find(_.name == orderDetails(0))
          .getOrElse(throw new Exception(s"Client ${orderDetails(0)} is not found")),
        orderType  = if (orderDetails(1) == "b") OrderType.BUY else OrderType.SELL,
        price      = orderDetails(3).toInt,
        volume     = orderDetails(4).toInt,
        instrument = InstrumentType.withName(orderDetails(2)),
        orderTime  = System.currentTimeMillis(),
      )
    })

    orders.toList
  }

}
