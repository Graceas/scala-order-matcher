package ie.ordermatcher

import ie.ordermatcher.helper.DataParser
import ie.ordermatcher.model.{Client, Order}

object Boot extends App {

  def start(): Unit = {

    val clients: List[Client] = DataParser.clientsParse("data/clients.txt")
    val orders:  List[Order]  = DataParser.ordersParse("data/orders.txt", clients)

    println(clients, orders)
  }

  def log(str: String): Unit = println(s"${System.currentTimeMillis()}: $str")

  start()
}
