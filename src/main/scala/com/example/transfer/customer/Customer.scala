package com.example.transfer.customer

import java.util.UUID

import com.example.transfer.customer.Customer.Id

case class Customer(id: Id, name: String)

object Customer {

  type Id = UUID

  def apply(name: String): Customer = new Customer(UUID.randomUUID(), name)

}