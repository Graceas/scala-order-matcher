package io.ordermatcher

import ie.ordermatcher._
import ie.ordermatcher.model.{Client, Order, OrderBook, OrderBookHistory}
import ie.ordermatcher.types.{InstrumentType, OrderType}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

import scala.collection.mutable

class OrderBookTest extends AnyFlatSpec with Matchers {
  "Order" should "be declined [BUY, user.balance < price * volume]" in {
    val orderMatcher: OrderMatcher = new OrderMatcher(InstrumentType.A, OrderBook(), OrderBookHistory())
    val client: Client = Client("A1", 10, mutable.Map.empty)
    val order: Order = Order(
      client     = client,
      orderType  = OrderType.BUY,
      price      = 100,
      volume     = 5,
      instrument = InstrumentType.A,
    )

    orderMatcher.addOrder(order) should be (false)
  }

  "Order" should "be declined [SELL, user.instrumentBalance < volume]" in {
    val orderMatcher: OrderMatcher = new OrderMatcher(InstrumentType.A, OrderBook(), OrderBookHistory())
    val client: Client = Client("A1", 10, mutable.Map.empty)
    val order: Order = Order(
      client     = client,
      orderType  = OrderType.SELL,
      price      = 100,
      volume     = 5,
      instrument = InstrumentType.A,
    )

    orderMatcher.addOrder(order) should be (false)
  }

  "Order" should "be declined [SELL, order.instrument <> matcher.instrument]" in {
    val orderMatcher: OrderMatcher = new OrderMatcher(InstrumentType.A, OrderBook(), OrderBookHistory())
    val client: Client = Client("A1", 10, mutable.Map(InstrumentType.B -> 999))
    val order: Order = Order(
      client     = client,
      orderType  = OrderType.SELL,
      price      = 100,
      volume     = 5,
      instrument = InstrumentType.B,
    )

    orderMatcher.addOrder(order) should be (false)
  }

  "Order" should "be accepted [BUY, user.balance >= price * volume]" in {
    val orderMatcher: OrderMatcher = new OrderMatcher(InstrumentType.A, OrderBook(), OrderBookHistory())
    val client: Client = Client("A1", 1000, mutable.Map.empty)
    val order: Order = Order(
      client     = client,
      orderType  = OrderType.BUY,
      price      = 100,
      volume     = 5,
      instrument = InstrumentType.A,
    )

    orderMatcher.addOrder(order) should be (true)
  }

  "Order" should "be declined [BUY, negative price]" in {
    val orderMatcher: OrderMatcher = new OrderMatcher(InstrumentType.A, OrderBook(), OrderBookHistory())
    val client: Client = Client("A1", 1000, mutable.Map.empty)
    val order: Order = Order(
      client     = client,
      orderType  = OrderType.BUY,
      price      = -100,
      volume     = 5,
      instrument = InstrumentType.A,
    )

    orderMatcher.addOrder(order) should be (false)
  }

  "Order" should "be declined [BUY, negative volume]" in {
    val orderMatcher: OrderMatcher = new OrderMatcher(InstrumentType.A, OrderBook(), OrderBookHistory())
    val client: Client = Client("A1", 1000, mutable.Map.empty)
    val order: Order = Order(
      client     = client,
      orderType  = OrderType.BUY,
      price      = 100,
      volume     = -5,
      instrument = InstrumentType.A,
    )

    orderMatcher.addOrder(order) should be (false)
  }

  "Order" should "be accepted [SELL, user.instrumentBalance >= volume]" in {
    val orderMatcher: OrderMatcher = new OrderMatcher(InstrumentType.A, OrderBook(), OrderBookHistory())
    val client: Client = Client("A1", 1000, mutable.Map(InstrumentType.A -> 10))
    val order: Order = Order(
      client     = client,
      orderType  = OrderType.BUY,
      price      = 100,
      volume     = 5,
      instrument = InstrumentType.A,
    )

    orderMatcher.addOrder(order) should be (true)
  }

  "Order" should "be declined [SELL, negative price]" in {
    val orderMatcher: OrderMatcher = new OrderMatcher(InstrumentType.A, OrderBook(), OrderBookHistory())
    val client: Client = Client("A1", 1000, mutable.Map(InstrumentType.A -> 10))
    val order: Order = Order(
      client     = client,
      orderType  = OrderType.BUY,
      price      = -100,
      volume     = 5,
      instrument = InstrumentType.A,
    )

    orderMatcher.addOrder(order) should be (false)
  }

  "Order" should "be declined [SELL, negative volume]" in {
    val orderMatcher: OrderMatcher = new OrderMatcher(InstrumentType.A, OrderBook(), OrderBookHistory())
    val client: Client = Client("A1", 1000, mutable.Map(InstrumentType.A -> 10))
    val order: Order = Order(
      client     = client,
      orderType  = OrderType.BUY,
      price      = 100,
      volume     = -5,
      instrument = InstrumentType.A,
    )

    orderMatcher.addOrder(order) should be (false)
  }

  "Order" should "be declined [SELL, duplicate]" in {
    val orderMatcher: OrderMatcher = new OrderMatcher(InstrumentType.A, OrderBook(), OrderBookHistory())
    val client: Client = Client("A1", 1000, mutable.Map(InstrumentType.A -> 10))
    val order: Order = Order(
      client     = client,
      orderType  = OrderType.BUY,
      price      = 100,
      volume     = 5,
      instrument = InstrumentType.A,
    )

    orderMatcher.addOrder(order) should be (true)
    orderMatcher.addOrder(order) should be (false)
  }

  "Orders" should "be full filled [price and volume equal]" in {
    val orderMatcher: OrderMatcher = new OrderMatcher(InstrumentType.A, OrderBook(), OrderBookHistory())
    val client:  Client = Client("A1", 1000, mutable.Map.empty)
    val client2: Client = Client("A2", 1000, mutable.Map(InstrumentType.A -> 10))

    val order: Order = Order(
      client     = client,
      orderType  = OrderType.BUY,
      price      = 100,
      volume     = 5,
      instrument = InstrumentType.A,
    )

    val order2: Order = Order(
      client     = client2,
      orderType  = OrderType.SELL,
      price      = 100,
      volume     = 5,
      instrument = InstrumentType.A,
    )

    orderMatcher.addOrder(order) should be (true)
    orderMatcher.addOrder(order2) should be (true)

    orderMatcher.start()

    orderMatcher.orderBook.sellOrders should be (empty)
    orderMatcher.orderBook.buyOrders should be (empty)

    client.balance should be (500)
    client.instrumentBalances(InstrumentType.A) should be (5)
    client2.balance should be (1500)
    client2.instrumentBalances(InstrumentType.A) should be (5)
  }

  "Orders" should "be full filled [price and volume equal (two SELL and one BUY)]" in {
    val orderMatcher: OrderMatcher = new OrderMatcher(InstrumentType.A, OrderBook(), OrderBookHistory())
    val client:  Client = Client("A1", 1000, mutable.Map.empty)
    val client2: Client = Client("A2", 1000, mutable.Map(InstrumentType.A -> 10))
    val client3: Client = Client("A3", 1000, mutable.Map(InstrumentType.A -> 10))

    val order: Order = Order(
      client     = client,
      orderType  = OrderType.BUY,
      price      = 100,
      volume     = 10,
      instrument = InstrumentType.A,
    )

    val order2: Order = Order(
      client     = client2,
      orderType  = OrderType.SELL,
      price      = 100,
      volume     = 5,
      instrument = InstrumentType.A,
    )

    val order3: Order = Order(
      client     = client3,
      orderType  = OrderType.SELL,
      price      = 100,
      volume     = 5,
      instrument = InstrumentType.A,
    )

    orderMatcher.addOrder(order) should be (true)
    orderMatcher.addOrder(order2) should be (true)
    orderMatcher.addOrder(order3) should be (true)

    orderMatcher.start()

    orderMatcher.orderBook.sellOrders should be (empty)
    orderMatcher.orderBook.buyOrders should be (empty)

    client.balance should be (0)
    client.instrumentBalances(InstrumentType.A) should be (10)
    client2.balance should be (1500)
    client2.instrumentBalances(InstrumentType.A) should be (5)
    client3.balance should be (1500)
    client3.instrumentBalances(InstrumentType.A) should be (5)
  }

  "Orders" should "be full filled [price and volume equal (two SELL and five BUY)]" in {
    val orderMatcher: OrderMatcher = new OrderMatcher(InstrumentType.A, OrderBook(), OrderBookHistory())
    val clients: Map[String, Client] = Map(
      // buy
      "B1" -> Client("B1", 1000, mutable.Map.empty),
      "B2" -> Client("B2", 1000, mutable.Map.empty),
      "B3" -> Client("B3", 1000, mutable.Map.empty),
      "B4" -> Client("B4", 1000, mutable.Map.empty),
      "B5" -> Client("B5", 1000, mutable.Map.empty),
      // sell
      "S1" -> Client("S1", 0, mutable.Map(InstrumentType.A -> 15)),
      "S2" -> Client("S2", 0, mutable.Map(InstrumentType.A -> 10))
    )

    val orders: Map[String, Order] = Map(
      // buy
      "B1" -> Order(client = clients("B1"), orderType = OrderType.BUY, price = 100, volume = 5, instrument = InstrumentType.A),
      "B2" -> Order(client = clients("B2"), orderType = OrderType.BUY, price = 100, volume = 5, instrument = InstrumentType.A),
      "B3" -> Order(client = clients("B3"), orderType = OrderType.BUY, price = 100, volume = 5, instrument = InstrumentType.A),
      "B4" -> Order(client = clients("B4"), orderType = OrderType.BUY, price = 100, volume = 5, instrument = InstrumentType.A),
      "B5" -> Order(client = clients("B5"), orderType = OrderType.BUY, price = 100, volume = 5, instrument = InstrumentType.A),
      // sell
      "S1" -> Order(client = clients("S1"), orderType = OrderType.SELL, price = 100, volume = 15, instrument = InstrumentType.A),
      "S2" -> Order(client = clients("S2"), orderType = OrderType.SELL, price = 100, volume = 10, instrument = InstrumentType.A)
    )

    orderMatcher.addOrder(orders("B1")) should be (true)
    orderMatcher.addOrder(orders("B2")) should be (true)
    orderMatcher.addOrder(orders("B3")) should be (true)
    orderMatcher.addOrder(orders("B4")) should be (true)
    orderMatcher.addOrder(orders("B5")) should be (true)
    orderMatcher.addOrder(orders("S1")) should be (true)
    orderMatcher.addOrder(orders("S2")) should be (true)

    orderMatcher.start()

    orderMatcher.orderBook.sellOrders should be (empty)
    orderMatcher.orderBook.buyOrders should be (empty)

    clients("B1").balance should be (500)
    clients("B1").instrumentBalances(InstrumentType.A) should be (5)

    clients("B2").balance should be (500)
    clients("B2").instrumentBalances(InstrumentType.A) should be (5)

    clients("B3").balance should be (500)
    clients("B3").instrumentBalances(InstrumentType.A) should be (5)

    clients("B4").balance should be (500)
    clients("B4").instrumentBalances(InstrumentType.A) should be (5)

    clients("B5").balance should be (500)
    clients("B5").instrumentBalances(InstrumentType.A) should be (5)

    clients("S1").balance should be (1500)
    clients("S1").instrumentBalances(InstrumentType.A) should be (0)

    clients("S2").balance should be (1000)
    clients("S2").instrumentBalances(InstrumentType.A) should be (0)
  }

  "Orders" should "be part filled [price equal, SELL volume greater]" in {
    val orderMatcher: OrderMatcher = new OrderMatcher(InstrumentType.D, OrderBook(), OrderBookHistory())
    val clients: Map[String, Client] = Map(
      // buy
      "B1" -> Client("B1", 1000, mutable.Map.empty),
      "B2" -> Client("B2", 1000, mutable.Map.empty),
      "B3" -> Client("B3", 1000, mutable.Map.empty),
      "B4" -> Client("B4", 1000, mutable.Map.empty),
      "B5" -> Client("B5", 1000, mutable.Map.empty),
      // sell
      "S1" -> Client("S1", 0, mutable.Map(InstrumentType.D -> 15)),
      "S2" -> Client("S2", 0, mutable.Map(InstrumentType.D -> 30))
    )

    val orders: Map[String, Order] = Map(
      // buy
      "B1" -> Order(client = clients("B1"), orderType = OrderType.BUY, price = 100, volume = 5, instrument = InstrumentType.D),
      "B2" -> Order(client = clients("B2"), orderType = OrderType.BUY, price = 100, volume = 5, instrument = InstrumentType.D),
      "B3" -> Order(client = clients("B3"), orderType = OrderType.BUY, price = 100, volume = 5, instrument = InstrumentType.D),
      "B4" -> Order(client = clients("B4"), orderType = OrderType.BUY, price = 100, volume = 5, instrument = InstrumentType.D),
      "B5" -> Order(client = clients("B5"), orderType = OrderType.BUY, price = 100, volume = 5, instrument = InstrumentType.D),
      // sell
      "S1" -> Order(client = clients("S1"), orderType = OrderType.SELL, price = 100, volume = 15, instrument = InstrumentType.D),
      "S2" -> Order(client = clients("S2"), orderType = OrderType.SELL, price = 100, volume = 30, instrument = InstrumentType.D)
    )

    orderMatcher.addOrder(orders("B1")) should be (true)
    orderMatcher.addOrder(orders("B2")) should be (true)
    orderMatcher.addOrder(orders("B3")) should be (true)
    orderMatcher.addOrder(orders("B4")) should be (true)
    orderMatcher.addOrder(orders("B5")) should be (true)
    orderMatcher.addOrder(orders("S1")) should be (true)
    orderMatcher.addOrder(orders("S2")) should be (true)

    orderMatcher.start()

    orderMatcher.orderBook.sellOrders shouldNot be (empty)
    orderMatcher.orderBook.buyOrders should be (empty)

    clients("B1").balance should be (500)
    clients("B1").instrumentBalances(InstrumentType.D) should be (5)

    clients("B2").balance should be (500)
    clients("B2").instrumentBalances(InstrumentType.D) should be (5)

    clients("B3").balance should be (500)
    clients("B3").instrumentBalances(InstrumentType.D) should be (5)

    clients("B4").balance should be (500)
    clients("B4").instrumentBalances(InstrumentType.D) should be (5)

    clients("B5").balance should be (500)
    clients("B5").instrumentBalances(InstrumentType.D) should be (5)

    clients("S1").balance should be (1500)
    clients("S1").instrumentBalances(InstrumentType.D) should be (0)

    clients("S2").balance should be (1000)
    clients("S2").instrumentBalances(InstrumentType.D) should be (0)

    orderMatcher.cancelAllOrders()

    clients("B1").balance should be (500)
    clients("B1").instrumentBalances(InstrumentType.D) should be (5)

    clients("B2").balance should be (500)
    clients("B2").instrumentBalances(InstrumentType.D) should be (5)

    clients("B3").balance should be (500)
    clients("B3").instrumentBalances(InstrumentType.D) should be (5)

    clients("B4").balance should be (500)
    clients("B4").instrumentBalances(InstrumentType.D) should be (5)

    clients("B5").balance should be (500)
    clients("B5").instrumentBalances(InstrumentType.D) should be (5)

    clients("S1").balance should be (1500)
    clients("S1").instrumentBalances(InstrumentType.D) should be (0)

    clients("S2").balance should be (1000)
    clients("S2").instrumentBalances(InstrumentType.D) should be (20)
  }

  "Orders" should "be part filled [price not equal, SELL volume greater]" in {
    val orderMatcher: OrderMatcher = new OrderMatcher(InstrumentType.A, OrderBook(), OrderBookHistory())
    val clients: Map[String, Client] = Map(
      // buy
      "B1" -> Client("B1", 1000, mutable.Map.empty),
      "B2" -> Client("B2", 1000, mutable.Map.empty),
      "B3" -> Client("B3", 1000, mutable.Map.empty),
      "B4" -> Client("B4", 1000, mutable.Map.empty),
      "B5" -> Client("B5", 1000, mutable.Map.empty),
      "B6" -> Client("B6", 1000, mutable.Map(InstrumentType.A -> 0)),
      // sell
      "S1" -> Client("S1", 0, mutable.Map(InstrumentType.A -> 15)),
      "S2" -> Client("S2", 0, mutable.Map(InstrumentType.A -> 30))
    )

    val orders: Map[String, Order] = Map(
      // buy
      "B1" -> Order(client = clients("B1"), orderType = OrderType.BUY, price = 100, volume = 5, instrument = InstrumentType.A),
      "B2" -> Order(client = clients("B2"), orderType = OrderType.BUY, price = 100, volume = 5, instrument = InstrumentType.A),
      "B3" -> Order(client = clients("B3"), orderType = OrderType.BUY, price = 100, volume = 5, instrument = InstrumentType.A),
      "B4" -> Order(client = clients("B4"), orderType = OrderType.BUY, price = 100, volume = 5, instrument = InstrumentType.A),
      "B5" -> Order(client = clients("B5"), orderType = OrderType.BUY, price = 100, volume = 5, instrument = InstrumentType.A),
      "B6" -> Order(client = clients("B6"), orderType = OrderType.BUY, price = 99, volume = 5, instrument = InstrumentType.A),
      // sell
      "S1" -> Order(client = clients("S1"), orderType = OrderType.SELL, price = 100, volume = 15, instrument = InstrumentType.A),
      "S2" -> Order(client = clients("S2"), orderType = OrderType.SELL, price = 100, volume = 30, instrument = InstrumentType.A)
    )

    orderMatcher.addOrder(orders("B1")) should be (true)
    orderMatcher.addOrder(orders("B2")) should be (true)
    orderMatcher.addOrder(orders("B3")) should be (true)
    orderMatcher.addOrder(orders("B4")) should be (true)
    orderMatcher.addOrder(orders("B5")) should be (true)
    orderMatcher.addOrder(orders("B6")) should be (true)
    orderMatcher.addOrder(orders("S1")) should be (true)
    orderMatcher.addOrder(orders("S2")) should be (true)

    orderMatcher.start()

    orderMatcher.orderBook.sellOrders shouldNot be (empty)
    orderMatcher.orderBook.buyOrders shouldNot be (empty)

    clients("B1").balance should be (500)
    clients("B1").instrumentBalances(InstrumentType.A) should be (5)

    clients("B2").balance should be (500)
    clients("B2").instrumentBalances(InstrumentType.A) should be (5)

    clients("B3").balance should be (500)
    clients("B3").instrumentBalances(InstrumentType.A) should be (5)

    clients("B4").balance should be (500)
    clients("B4").instrumentBalances(InstrumentType.A) should be (5)

    clients("B5").balance should be (500)
    clients("B5").instrumentBalances(InstrumentType.A) should be (5)

    clients("B6").balance should be (505)
    clients("B6").instrumentBalances(InstrumentType.A) should be (0)

    clients("S1").balance should be (1500)
    clients("S1").instrumentBalances(InstrumentType.A) should be (0)

    clients("S2").balance should be (1000)
    clients("S2").instrumentBalances(InstrumentType.A) should be (0)

    orderMatcher.cancelAllOrders()

    clients("B1").balance should be (500)
    clients("B1").instrumentBalances(InstrumentType.A) should be (5)

    clients("B2").balance should be (500)
    clients("B2").instrumentBalances(InstrumentType.A) should be (5)

    clients("B3").balance should be (500)
    clients("B3").instrumentBalances(InstrumentType.A) should be (5)

    clients("B4").balance should be (500)
    clients("B4").instrumentBalances(InstrumentType.A) should be (5)

    clients("B5").balance should be (500)
    clients("B5").instrumentBalances(InstrumentType.A) should be (5)

    clients("B6").balance should be (1000)
    clients("B6").instrumentBalances(InstrumentType.A) should be (0)

    clients("S1").balance should be (1500)
    clients("S1").instrumentBalances(InstrumentType.A) should be (0)

    clients("S2").balance should be (1000)
    clients("S2").instrumentBalances(InstrumentType.A) should be (20)
  }
}
