package com.example.transfer.customer

import com.example.transfer.customer.Customer.Id
import doobie.util.log.LogHandler
import doobie.implicits._
import doobie.h2.implicits._
import doobie.h2._
import doobie.util.query.Query0
import doobie.util.update.Update0

class CustomerRepo(logHandler: LogHandler) {

  val all: Query0[Customer] = {
    sql"""
      SELECT * FROM customers
    """.queryWithLogHandler[Customer](logHandler)
  }

  def byId(id: Id): Query0[Customer] = {
    sql"""
      SELECT * FROM customers where id = $id
    """.queryWithLogHandler[Customer](logHandler)
  }

  def insert(customer: Customer): Update0 = {
    sql"""
        INSERT INTO customers VALUES(${customer.id}, ${customer.name})
    """.updateWithLogHandler(logHandler)
  }

}
