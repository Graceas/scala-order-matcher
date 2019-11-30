package ie.ordermatcher

import java.io.{BufferedWriter, File, FileWriter}

import ie.ordermatcher.helper.DataParser
import ie.ordermatcher.model.{Client, Order, OrderBook, OrderBookHistory}
import ie.ordermatcher.types.InstrumentType

import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration

object Boot extends App {

  def start(): Unit = {

    val clients: List[Client] = DataParser.clientsParse("data/clients.txt")
    val orders:  List[Order]  = DataParser.ordersParse("data/orders.txt", clients)

    // initialize order matcher by instrument
    val orderMatchers: List[OrderMatcher] = InstrumentType.values.toList.map(instrument =>
      new OrderMatcher(instrument, OrderBook(), OrderBookHistory())
    )

    if (args.length > 0 && args(0) == "async") {
      // add orders to order books
      orderMatchers.foreach(orderMatcher => {
        orders
          .filter(order => order.instrument == orderMatcher.instrument)
          .foreach(order => {
            val status = if (orderMatcher.addOrder(order)) "accepted" else "declined"
            log(s"${order.orderType} order by client ${order.client.name} is $status")
          })
      })

      // run all matchers async
      val futures = orderMatchers.map(orderMatcher => Future {
        orderMatcher.start()
      })

      Await.result(Future.sequence(futures), Duration.Inf)
    } else {
      // add orders to order books and process
      orderMatchers.foreach(orderMatcher => {
        orders
          .filter(order => order.instrument == orderMatcher.instrument)
          .foreach(order => {
            val status = orderMatcher.addOrder(order)
            log(s"add ${order.orderType} order by client ${order.client.name} is $status")

            if (status) {
              orderMatcher.start()
            }
          })
      })
    }

    // cancel all orders for release clients funds
    orderMatchers.foreach(orderMatcher => orderMatcher.cancelAllOrders())

    // save results to file
    val file = new File("result.txt")
    val bw = new BufferedWriter(new FileWriter(file))
    bw.write(clients.map(client => {
      s"${client.name}\t${client.balance}\t${client.instrumentBalances(InstrumentType.A)}\t" +
      s"${client.instrumentBalances(InstrumentType.B)}\t${client.instrumentBalances(InstrumentType.C)}\t" +
      s"${client.instrumentBalances(InstrumentType.D)}"
    }).mkString("\n"))
    bw.close()
  }

  def log(str: String): Unit = println(s"${System.currentTimeMillis()}: $str")

  start()
}
