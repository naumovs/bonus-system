package loyalty.core.domain

sealed trait AppError extends Exception
case class DatabaseError(cause: Throwable) extends AppError
case class TransactionExists(id: TransactionId) extends AppError
case class InvalidTransactionStatus(id: TransactionId) extends AppError
case class BonusRuleValidationError(message: String) extends AppError //TODO: implement validation error