package ie.ordermatcher

import ie.ordermatcher.types.InstrumentType.InstrumentType

case class Client(
  name:               String,
  balance:            Int,
  instrumentBalances: Map[InstrumentType, Int]
)
