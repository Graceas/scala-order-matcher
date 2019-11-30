package ie.ordermatcher.types

object OrderType extends Enumeration {
  type OrderType   = Value
  val  BUY:  Value = Value("b")
  val  SELL: Value = Value("s")
}
