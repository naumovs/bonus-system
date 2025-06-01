package loyalty.core.repository

import io.getquill.{PostgresZioJdbcContext, SnakeCase}
import loyalty.core.domain.{AppError, BonusRule, DatabaseError, Transaction}
import loyalty.database.Database
import zio._

import javax.sql.DataSource

trait RuleEngine {
  def getActiveRules: IO[AppError, List[BonusRule]]
  def matchesRule(rule: BonusRule, transaction: Transaction): Boolean
}

case class RuleEngineLive(ds: DataSource) extends RuleEngine {

  private val dsLayer = ZLayer.succeed(ds)

  private val ctx = new PostgresZioJdbcContext(SnakeCase)

  import ctx._

  private val rules = quote(querySchema[BonusRule]("bonus_rules"))

  override def getActiveRules: IO[AppError, List[BonusRule]] =
    run(rules.filter(_.active == lift(true)))
      .mapError(DatabaseError).provide(dsLayer)

  override def matchesRule(rule: BonusRule, tx: Transaction): Boolean = {

    val condition = ujson.read(rule.condition)
    val minAmount = condition.obj.get("minAmount").map(_.num)
    val categories = condition.obj.get("categories").map(_.arr.map(_.str))

    minAmount.forall(_ <= tx.amount.doubleValue) && categories.forall(_.contains(tx.category))
  }
}

object RuleEngine {
  val live: ZLayer[Any, Throwable, RuleEngine] = Database.live >>>
    ZLayer.fromFunction[DataSource => RuleEngine](ds => RuleEngineLive(ds))
}