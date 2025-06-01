package loyalty.database

import io.getquill.jdbczio.Quill
import zio.ZLayer

import javax.sql.DataSource

object Database {
  val live: ZLayer[Any, Throwable, DataSource] = Quill.DataSource.fromPrefix("persistence")
}

