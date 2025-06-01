package loyalty.core.domain

import zio.json._
import java.time.Instant

case class ClientId(value: String) extends AnyVal
object ClientId {
  implicit val codec: JsonCodec[ClientId] = DeriveJsonCodec.gen
}

case class TransactionId(value: String) extends AnyVal
object TransactionId {
  implicit val codec: JsonCodec[TransactionId] = DeriveJsonCodec.gen
}

case class Transaction(
                        id: TransactionId,
                        clientId: ClientId,
                        amount: BigDecimal,
                        currency: String,
                        category: String,
                        source: String,
                        status: String,
                        createdAt: Instant = Instant.now
                      )

object Transaction {
  implicit val codec: JsonCodec[Transaction] = DeriveJsonCodec.gen
}

case class BonusLedgerEntry(
                             clientId: ClientId,
                             transactionId: TransactionId,
                             points: BigDecimal,
                             expiresAt: Instant,
                             createdAt: Instant = Instant.now
                           )
object BonusLedgerEntry {
  implicit val codec: JsonCodec[BonusLedgerEntry] = DeriveJsonCodec.gen
}

case class BalanceResponse(
                            points: BigDecimal,
                            expiresAt: Instant,
                            currency: String
                          )

object BalanceResponse {
  implicit val codec: JsonCodec[BalanceResponse] = DeriveJsonCodec.gen
}

case class BonusRule(
                      id: String,
                      condition: String,
                      rewardType: String,
                      rewardValue: BigDecimal,
                      priority: Int,
                      active: Boolean
                    )

