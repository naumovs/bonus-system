package loyalty.core.service

import loyalty.core.domain.{AppError, BalanceResponse, BonusLedgerEntry,
  BonusRule, ClientId, Transaction, TransactionId}
import loyalty.core.repository.{BonusRepository, RuleEngine}
import zio._

import java.time.Instant
import java.time.temporal.ChronoUnit

trait BonusService {
  def calculateBonus(transaction: Transaction): IO[AppError, Unit]
  def reverseBonus(transactionId: TransactionId): IO[AppError, Unit]
  def getBalance(clientId: ClientId): IO[AppError, BalanceResponse]
  def getHistory(clientId: ClientId): IO[AppError, List[BonusLedgerEntry]]
}

case class BonusServiceLive(
                             bonusRepo: BonusRepository,
                             ruleEngine: RuleEngine
                           ) extends BonusService {

  override def calculateBonus(transaction: Transaction): IO[AppError, Unit] =
    for {
      rules <- ruleEngine.getActiveRules
      applicableRules = rules.filter(ruleEngine.matchesRule(_, transaction))
      points = applicableRules.map(calculatePoints(_, transaction)).sum
      _ <- ZIO.when(points > 0)(
        bonusRepo.credit(BonusLedgerEntry(
          transaction.clientId,
          transaction.id,
          points,
          Instant.now.plus(365, ChronoUnit.DAYS)
        ))
      )
    } yield ()

  private def calculatePoints(rule: BonusRule, tx: Transaction): BigDecimal =
    rule.rewardType match {
      case "FIXED" => rule.rewardValue
      case "PERCENT" => tx.amount * rule.rewardValue / 100
      case _ => BigDecimal(0)
    }

  override def reverseBonus(transactionId: TransactionId): IO[AppError, Unit] =
    bonusRepo.reverse(transactionId).unit

  override def getBalance(clientId: ClientId): IO[AppError, BalanceResponse] =
    bonusRepo.getBalance(clientId)

  override def getHistory(clientId: ClientId): IO[AppError, List[BonusLedgerEntry]] =
    bonusRepo.getHistory(clientId)
}

object BonusService {
  val live: ZLayer[BonusRepository with RuleEngine, Nothing, BonusService] =
    ZLayer.fromFunction(BonusServiceLive(_, _))
}