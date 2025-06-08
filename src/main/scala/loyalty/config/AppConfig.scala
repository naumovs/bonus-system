package loyalty.config

import zio._
import com.typesafe.config.ConfigFactory

//case class AppConfig()
//
//object AppConfig {
//  val live: ZLayer[Any, Config.Error, AppConfig] =
//    ZLayer(ZIO.attempt {
//      val config = ConfigFactory.load()
// TODO: implement
//    }.orElseFail(Config.Error.InvalidData(message = "Invalid config")))
//}
