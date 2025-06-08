package loyalty.api

import loyalty.core.domain._
import loyalty.core.service._
import zio._
import zio.http._
import zio.json._

class LoyaltyApi(
                  transactionService: TransactionService,
                  bonusService: BonusService
                ) {

   private val routes: Http[Any, Response, Request, Response] = Http.collectZIO[Request] {

    // Создание транзакции
    case req @ Method.POST -> Root / "transactions" =>
      for {
        tx <- req.body.asString.map(_.fromJson[Transaction])
          .flatMap(ZIO.fromEither(_))
          .orElseFail(Response.text("Malformed request").withStatus(Status.BadRequest))
        result <- transactionService.processTransaction(tx)
          .foldZIO(
            error => handleErrors(error),
            _ => ZIO.succeed(Response.status(Status.Accepted))
          )
      } yield result


    // Получение баланса
    case Method.GET -> Root / "clients" / clientId / "balance" =>
      bonusService.getBalance(ClientId(clientId))
        .map(balance => Response.json(balance.toJson))
        .catchAll(handleErrors)

    // История операций
    case Method.GET -> Root / "clients" / clientId / "history" =>
      bonusService.getHistory(ClientId(clientId))
        .map(history => Response.json(history.toJson))
        .catchAll(handleErrors)

    // Отмена транзакции
    case Method.PATCH -> Root / "transactions" / transactionId / "cancel" =>
      transactionService.cancelTransaction(TransactionId(transactionId))
        .as(Response.ok)
        .catchAll(handleErrors)
  }

  val loggedRoutes = routes @@ loggingMiddleware

  private def handleErrors(error: AppError): UIO[Response] = error match {
    case DatabaseError(e) =>
      ZIO.logError(e.getMessage).as(Response.status(Status.InternalServerError))
    case TransactionExists(id) =>
      ZIO.succeed(Response.text(s"Transaction ${id.value} already exists").withStatus(Status.Conflict))
    case InvalidTransactionStatus(id) =>
      ZIO.succeed(Response.text(s"Invalid status for transaction ${id.value}").withStatus(Status.BadRequest))
    case _ =>
      ZIO.succeed(Response.status(Status.InternalServerError))
  }
}

object LoyaltyApi {
  def live: ZLayer[TransactionService with BonusService, Nothing, LoyaltyApi] =
    ZLayer.fromFunction(new LoyaltyApi(_, _))
}