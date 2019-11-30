package ie.ordermatcher.model

case class TradeEntry(
  tradingTime: Long = System.currentTimeMillis(),
  order:       Order,
  order2:      Order,
  volume:      Int,
  price:       Int,
)
