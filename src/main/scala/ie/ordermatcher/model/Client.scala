package ie.ordermatcher.model

import ie.ordermatcher.types.InstrumentType.InstrumentType

import scala.collection.mutable

case class Client(
  name:               String,
  var balance:        Int,
  instrumentBalances: mutable.Map[InstrumentType, Int]
)
