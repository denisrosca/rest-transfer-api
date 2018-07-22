package com.example.transfer.transaction

import doobie.util.log.LogHandler
import doobie.implicits._
import doobie.h2.implicits._
import doobie.h2._
import doobie.util.query.Query0
import doobie.util.update.Update0
import com.example.transfer.account.Account.{Id => AccountId}
import com.example.transfer.transaction.Transaction.Id

class TransferRepo(implicit logHandler: LogHandler) {

  val all: Query0[Transfer] = {
    sql"""
      SELECT * FROM transfers
    """.query[Transfer]
  }

  def byId(transferId: Id): Query0[Transfer] = {
    sql"""
      SELECT * FROM transfers WHERE id = $transferId
    """.query[Transfer]
  }

  def bySource(source: AccountId): Query0[Transfer] = {
    sql"""
      SELECT * FROM transfers
      WHERE source = $source
    """.query[Transfer]
  }

  def byDestination(destination: AccountId): Query0[Transfer] = {
    sql"""
      SELECT * FROM transfers
      WHERE destination = $destination
    """.query[Transfer]
  }

  def between(source: AccountId, destination: AccountId): Query0[Transfer] = {
    sql"""
      SELECT * FROM transfers
      WHERE
        source = $source
       AND
        destination = $destination
    """.query[Transfer]
  }

  def insert(transfer: Transfer): Update0 = {
    sql"""
        INSERT INTO transfers
        VALUES(
          ${transfer.id}, ${transfer.source}, ${transfer.destination},
          ${transfer.amount}, ${transfer.description}, ${transfer.timestamp}
       )
    """.update
  }

}