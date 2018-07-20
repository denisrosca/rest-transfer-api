package com.example.transfer.account

import java.time.Instant
import java.util.UUID

import com.example.transfer.account.Account.Id
import com.example.transfer.customer.Customer.{Id => CustomerId}

case class Account(id: Id, customerId: CustomerId, createdOn: Instant)

object Account {

  type Id = UUID

  def apply(customerId: CustomerId): Account = Account(UUID.randomUUID(), customerId, Instant.now())

}