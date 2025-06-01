package loyalty.core.repository

import io.getquill.{PostgresZioJdbcContext, SnakeCase}
import zio._
import loyalty.core.domain._
import loyalty.database.Database

import java.time.Instant
import javax.sql.DataSource

trait BonusRepository {
  def credit(entry: BonusLedgerEntry): IO[AppError, Unit]
  def getBalance(clientId: ClientId): IO[AppError, BalanceResponse]
  def getHistory(clientId: ClientId): IO[AppError, List[BonusLedgerEntry]]
  def reverse(transactionId: TransactionId): IO[AppError, BigDecimal]
}

class BonusRepositoryLive(ds: DataSource)
  extends BonusRepository {

  private val dsLayer = ZLayer.succeed(ds)

  private val ctx = new PostgresZioJdbcContext(SnakeCase)

  import ctx._

  private val bonusLedger = quote(query[BonusLedgerEntry])

  override def credit(entry: BonusLedgerEntry): IO[AppError, Unit] =
    run(bonusLedger.insertValue(lift(entry)))
      .unit
      .mapError(DatabaseError)
      .provide(dsLayer)

  override def getBalance(clientId: ClientId): IO[AppError, BalanceResponse] = {
    lazy val now = Instant.now
    run(
      bonusLedger
        //TODO: Вот так quill! Не хочет работать со сравнением дат... Ну что ж - чистый SQL-костыль
        //.filter(b => b.clientId == lift(clientId) && b.expiresAt.isAfter(lift(now)))
        .filter(b => sql"${b.clientId} = ${lift(clientId)} and ${b.expiresAt} > ${lift(now)}".asCondition)
        .map(b => (b.points, b.expiresAt))
    ).mapBoth(DatabaseError, {
      entries =>
        val total = entries.map(_._1).sum
        val maxExpiry = entries.map(_._2).maxOption.getOrElse(now)
        BalanceResponse(total, maxExpiry, "RUB")
    })
    .provide(dsLayer)
  }

  override def getHistory(clientId: ClientId): IO[AppError, List[BonusLedgerEntry]] =
    run(bonusLedger.filter(_.clientId == lift(clientId)))
      .mapError(DatabaseError)
      .provide(dsLayer)

  override def reverse(transactionId: TransactionId): IO[AppError, BigDecimal] = {
    for {
      entries <- run(bonusLedger.filter(_.transactionId == lift(transactionId)))
        .mapError(DatabaseError)
      points = entries.map(_.points).sum
      _ <- run(bonusLedger.filter(_.transactionId == lift(transactionId)).delete)
        .mapError(DatabaseError).provide(dsLayer)
    } yield points
  }.provide(dsLayer)
}

object BonusRepository {
  val live:  ZLayer[Any, Throwable, BonusRepository] = Database.live >>> ZLayer
      .fromFunction[DataSource => BonusRepository](ds => new BonusRepositoryLive(ds))
}
