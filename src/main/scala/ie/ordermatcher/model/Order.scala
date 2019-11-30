package ie.ordermatcher.model

import java.util.UUID

import ie.ordermatcher.types.InstrumentType.InstrumentType
import ie.ordermatcher.types.OrderType.OrderType

case class Order(
  id:         String = UUID.randomUUID.toString,
  client:     Client,
  orderType:  OrderType,
  price:      Int,
  volume:     Int,
  instrument: InstrumentType,
  orderTime:  Long,
)
