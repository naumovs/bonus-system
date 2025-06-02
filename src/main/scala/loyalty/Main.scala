package loyalty

import loyalty.api.LoyaltyApi
import loyalty.core.repository.{BonusRepository, RuleEngine, TransactionRepository}
import loyalty.core.service.{BonusService, TransactionService}
import tofu.logging.zlogs.TofuZLogger
import zio._
import zio.config.typesafe.TypesafeConfigProvider
import zio.http.Server

object Main extends ZIOAppDefault {

  override val bootstrap: ZLayer[ZIOAppArgs, Any, Any] = {
    Runtime.removeDefaultLoggers >>>
      TofuZLogger.addToRuntime >>>
        Runtime.setConfigProvider(TypesafeConfigProvider.fromResourcePath())
  }

  private val httpApp = for {
    api <- ZIO.service[LoyaltyApi]
    _   <- Server.serve(api.loggedRoutes)
  } yield ()

  override def run: ZIO[Any, Throwable, Unit] = httpApp.provide(
    TransactionRepository.live,
    TransactionService.live,
    BonusRepository.live,
    BonusService.live,
    RuleEngine.live,
    LoyaltyApi.live,
    Server.default
  )
}