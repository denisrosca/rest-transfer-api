package com.example.transfer.account

import java.time.Instant
import java.util.UUID

import com.example.transfer.account.Account.Id
import com.example.transfer.customer.Customer.{Id => CustomerId}

case class Account(id: Id, customerId: CustomerId, balance: BigDecimal, createdOn: Instant)

object Account {

  type Id = UUID

  def apply(customerId: CustomerId): Account = Account(UUID.randomUUID(), customerId, 100, Instant.now())

}