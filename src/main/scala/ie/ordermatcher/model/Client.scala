package ie.ordermatcher.model

import ie.ordermatcher.types.InstrumentType.InstrumentType

case class Client(
  name:               String,
  var balance:        Int,
  instrumentBalances: Map[InstrumentType, Int]
)
