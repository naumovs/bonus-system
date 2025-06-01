package loyalty.core.service

import zio._
import loyalty.core.domain._
import loyalty.core.repository.TransactionRepository

trait TransactionService {
  def processTransaction(transaction: Transaction): IO[AppError, Unit]
  def cancelTransaction(id: TransactionId): IO[AppError, Unit]
  def getClientTransactions(clientId: ClientId): IO[AppError, List[Transaction]]
}

case class TransactionServiceLive(
                                   transactionRepo: TransactionRepository,
                                   bonusService: BonusService
                                 ) extends TransactionService {

  override def processTransaction(transaction: Transaction): IO[AppError, Unit] =
    for {
      _ <- ZIO.logInfo(s"Processing transaction $transaction")
      existing <- transactionRepo.find(transaction.id)
      _ <- ZIO.when(existing.isDefined)(ZIO.fail(TransactionExists(transaction.id)))
      txId <- transactionRepo.create(transaction)
      _ <- ZIO.logInfo(s"Transaction $txId processed")
      _ <- bonusService.calculateBonus(transaction)
    } yield ()

override def cancelTransaction(id: TransactionId): IO[AppError, Unit] =
  for {
    tx <- transactionRepo.find(id).someOrFail(InvalidTransactionStatus(id))
    _ <- ZIO.unless(tx.status == "CONFIRMED")(
      ZIO.fail(InvalidTransactionStatus(id))
    )
    _ <- transactionRepo.updateStatus(id, "CANCELLED")
    _ <- bonusService.reverseBonus(id)
  } yield ()

  override def getClientTransactions(clientId: ClientId): IO[AppError, List[Transaction]] =
    transactionRepo.findByClient(clientId)
}

object TransactionService {
  val live: ZLayer[TransactionRepository with BonusService, Nothing, TransactionService] =
    ZLayer.fromFunction(TransactionServiceLive(_, _))
}
