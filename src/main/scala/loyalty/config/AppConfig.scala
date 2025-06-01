package loyalty.config

import zio._
import com.typesafe.config.ConfigFactory

//case class DbConfig(
//                     url: String,
//                     user: String,
//                     password: String,
//                     driver: String
//                   )
//
//object AppConfig {
//  val live: ZLayer[Any, Config.Error, DbConfig] =
//    ZLayer(ZIO.attempt {
//      val config = ConfigFactory.load()
//      DbConfig(
//        config.getString("persistence.url"),
//        config.getString("persistence.user"),
//        config.getString("persistence.dataSource.password"),
//        config.getString("persistence.dataSource.driver")
//      )
//    }.orElseFail(Config.Error.InvalidData(message = "Invalid config")))
//}
