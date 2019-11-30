package ie.ordermatcher.model

case class TradeEntry(
  tradingTime: Long,
  client:      Client,
  client2:     Client,
  order:       Order,
  volume:      Int,
  price:       Int,
)
