package com.example.transfer.transaction

import java.time.Instant
import java.util.UUID

import com.example.transfer.account.Account.{Id => AccountId}
import com.example.transfer.transaction.Transaction.Id

sealed trait TransactionResult
case object TransactionSuccessful extends TransactionResult

sealed trait TransactionFailed extends TransactionResult {
  def reason: String
}
case object InvalidAccount extends TransactionFailed {
  override val reason: String = "Source or destination account is invalid"
}
case object InsufficientFunds extends TransactionFailed {
  override def reason: String = "Source account has insufficient funds"
}

trait Transaction {
  def id: Id
  def description: Option[String]
  def timestamp: Instant
}

object Transaction {
  type Id = UUID
}

case class Deposit(
  id: Id,
  account: AccountId,
  amount: BigDecimal,
  description: Option[String],
  timestamp: Instant
) extends Transaction

case class Withdrawal(
  id: Id,
  account: AccountId,
  amount: BigDecimal,
  description: Option[String],
  timestamp: Instant
) extends Transaction

case class Transfer(
  id: Id,
  source: AccountId,
  destination: AccountId,
  amount: BigDecimal,
  description: Option[String],
  timestamp: Instant
) extends Transaction