package ie.ordermatcher.model

import java.util.UUID

import ie.ordermatcher.types.InstrumentType.InstrumentType
import ie.ordermatcher.types.OrderType.OrderType

case class Order(
  id:         String,
  client:     Client,
  orderType:  OrderType,
  price:      Int,
  var volume: Int,
  instrument: InstrumentType,
  orderTime:  Long,
  nonce:      Long
)

object Order {
  var nonce: Long = 0

  def apply(
    client:     Client,
    orderType:  OrderType,
    price:      Int,
    volume:     Int,
    instrument: InstrumentType
  ): Order = {
    nonce += 1

    new Order(UUID.randomUUID.toString, client, orderType, price, volume, instrument, System.currentTimeMillis(), nonce)
  }
}