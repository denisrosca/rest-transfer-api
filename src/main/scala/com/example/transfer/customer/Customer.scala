package com.example.transfer.customer

import java.time.Instant
import java.util.UUID

import com.example.transfer.customer.Customer.Id

case class Customer(id: Id, name: String, registeredOn: Instant)

object Customer {

  type Id = UUID

  def apply(name: String): Customer = new Customer(UUID.randomUUID(), name, Instant.now())

}