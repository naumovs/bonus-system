package loyalty.api

import loyalty.core.domain._
import loyalty.core.service._
import zio._
import zio.test._
import zio.test.Assertion._
import zio.http._
import zio.http.codec.HttpCodec.content
import zio.json._

import java.time.format.DateTimeFormatter
import java.time.{Instant, LocalDateTime}

object LoyaltyApiSpec extends ZIOSpecDefault {

  private val now = Instant.now.plusSeconds(3600)

  // Mocks
  val mockTransactionService: ULayer[TransactionService] = ZLayer.succeed(new TransactionService {
    override def processTransaction(transaction: Transaction): IO[AppError, Unit] =
      ZIO.unit

    override def cancelTransaction(id: TransactionId): IO[AppError, Unit] =
      ZIO.unit

    override def getClientTransactions(clientId: ClientId): IO[AppError, List[Transaction]] =
      ZIO.succeed(List.empty)
  })

  val mockBonusService: ULayer[BonusService] = ZLayer.succeed(new BonusService {
    override def calculateBonus(transaction: Transaction): IO[AppError, Unit] =
      ZIO.unit

    override def reverseBonus(transactionId: TransactionId): IO[AppError, Unit] =
      ZIO.unit

    override def getBalance(clientId: ClientId): IO[AppError, BalanceResponse] =
      ZIO.succeed(
        BalanceResponse(
          points = BigDecimal(100),
          expiresAt = now,
          currency = "RUB"
        )
      )

    override def getHistory(clientId: ClientId): IO[AppError, List[BonusLedgerEntry]] =
      ZIO.succeed(
        List(
          BonusLedgerEntry(
            clientId = clientId,
            transactionId = TransactionId("tx1"),
            points = BigDecimal(50),
            expiresAt = now
          )
        )
      )
  })

  // Tests
  val apiLayer: ULayer[LoyaltyApi] =
    (mockTransactionService ++ mockBonusService) >>> LoyaltyApi.live

  override def spec: Spec[TestEnvironment with Scope, Any] =
    suite("LoyaltyApiSpec")(
      test("POST /transactions should process a transaction") {
        val request =
          Request
            .post(
              url = URL(Root / "transactions"),
              body = Body.fromString("""{
                          |  "id": {"value": "tx1" },
                          |  "clientId": {"value": "client1" },
                          |  "amount": 100.0,
                          |  "currency": "RUB",
                          |  "category": "TRAVEL",
                          |  "source": "POS",
                          |  "status": "CONFIRMED"
                          |}""".stripMargin)
            )
        for {
          api <- ZIO.service[LoyaltyApi]
          response <- api.loggedRoutes.runZIO(request)
        } yield assertTrue(response.status == Status.Accepted)
      },
      test("GET /clients/:id/balance should return the client's balance") {
        val clientId = "client1"
        val request = Request.get(URL(Root / "clients" / clientId / "balance"))

        for {
          api <- ZIO.service[LoyaltyApi]
          response <- api.loggedRoutes.runZIO(request)
          body <- response.body.asString
        } yield assertTrue(response.status == Status.Ok) &&
          assert(body.fromJson[BalanceResponse])(
            isRight(equalTo(BalanceResponse(BigDecimal(100), now, "RUB")))
          )
      },
      test("GET /clients/:id/history should return the client's transaction history") {
        val clientId = "client1"
        val request = Request.get(URL(Root / "clients" / clientId / "history"))

        for {
          api <- ZIO.service[LoyaltyApi]
          response <- api.loggedRoutes.runZIO(request)
          body <- response.body.asString
        } yield assertTrue(response.status == Status.Ok) &&
          assert(body.fromJson[List[BonusLedgerEntry]])(
            isRight(hasSize(equalTo(1)))
          )
      },
      test("PATCH /transactions/:id/cancel should cancel a transaction") {
        val transactionId = "tx1"
        val request = Request.patch(Body.fromString("{\"reason\": \"Возврат билета\"}"),
          URL(Root / "transactions" / transactionId / "cancel"))

        for {
          api <- ZIO.service[LoyaltyApi]
          response <- api.loggedRoutes.runZIO(request)
        } yield assertTrue(response.status == Status.Ok)
      }
    ).provide(apiLayer)
}
