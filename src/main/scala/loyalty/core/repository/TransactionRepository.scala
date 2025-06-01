package loyalty.core.repository

import io.getquill.{PostgresZioJdbcContext, SnakeCase}
import zio._
import loyalty.core.domain._
import loyalty.database.Database

import javax.sql.DataSource


trait TransactionRepository {
  def create(transaction: Transaction): IO[AppError, TransactionId]
  def find(id: TransactionId): IO[AppError, Option[Transaction]]
  def updateStatus(id: TransactionId, status: String): IO[AppError, Unit]
  def findByClient(clientId: ClientId): IO[AppError, List[Transaction]]
}

class TransactionRepositoryLive(ds: DataSource)
  extends TransactionRepository {

  private val dsLayer = ZLayer.succeed(ds)

  private val ctx = new PostgresZioJdbcContext(SnakeCase)

  import ctx._

  private val transactions = quote(query[Transaction])

  override def create(transaction: Transaction): IO[AppError, TransactionId] =
    run(transactions.insertValue(lift(transaction))).mapBoth(DatabaseError, _ => transaction.id).provide(dsLayer)

  override def find(id: TransactionId): IO[AppError, Option[Transaction]] =
    run(transactions.filter(_.id == lift(id))).mapBoth(DatabaseError, _.headOption).provide(dsLayer)

  override def updateStatus(id: TransactionId, status: String): IO[AppError, Unit] =
    run(
      transactions
        .filter(_.id == lift(id))
        .update(_.status -> lift(status))
    ).unit.mapError(DatabaseError).provide(dsLayer)

  override def findByClient(clientId: ClientId): IO[AppError, List[Transaction]] =
    run(transactions.filter(_.clientId == lift(clientId)))
      .mapError(DatabaseError).provide(dsLayer)
}

object TransactionRepository {
  val live:  ZLayer[Any, Throwable, TransactionRepository] = Database.live >>> ZLayer
    .fromFunction[DataSource => TransactionRepository](ds => new TransactionRepositoryLive(ds))
}
