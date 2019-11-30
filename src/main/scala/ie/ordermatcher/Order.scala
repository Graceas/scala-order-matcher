package ie.ordermatcher

import java.util.UUID

import ie.ordermatcher.InstrumentType.InstrumentType
import ie.ordermatcher.OrderType.OrderType

case class Order(
  id:         String = UUID.randomUUID.toString,
  client:     Client,
  orderType:  OrderType,
  price:      Int,
  volume:     Int,
  instrument: InstrumentType,
  orderTime:  Long,
)
