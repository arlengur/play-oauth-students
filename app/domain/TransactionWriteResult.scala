package domain

sealed trait TransactionResult
case class SuccessTransactionResult(accessToken: String) extends TransactionResult
case object AppNotUpdated extends TransactionResult
