package com.example.transfer.account

import com.example.transfer.account.Account.Id
import doobie.util.log.LogHandler
import doobie.implicits._
import doobie.h2.implicits._
import doobie.h2._
import doobie.util.query.Query0
import doobie.util.update.Update0


class AccountRepo(logHandler: LogHandler) {

  val all: Query0[Account] = {
    sql"""
        SELECT * FROM accounts
      """.queryWithLogHandler(logHandler)
  }

  def byId(id: Id): Query0[Account] = {
    sql"""
        SELECT * FROM accounts where id = $id
      """.queryWithLogHandler(logHandler)
  }

  def byCustomerId(id: Id): Query0[Account] = {
    sql"""
        SELECT * FROM accounts where customerId = $id
      """.queryWithLogHandler(logHandler)
  }

  def insert(account: Account): Update0 = {
    val balance = account.balance
    sql"""
        INSERT INTO accounts
        VALUES(${account.id}, ${account.customerId}, $balance, ${account.createdOn})
      """.updateWithLogHandler(logHandler)
  }

}