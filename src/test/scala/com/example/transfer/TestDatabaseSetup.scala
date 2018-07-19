package com.example.transfer

import cats.effect.IO
import doobie.scalatest.IOChecker
import doobie.util.transactor.Transactor

trait TestDatabaseSetup {
  self: IOChecker =>

  def databaseName: String

  override def transactor: doobie.Transactor[IO] = Transactor.fromDriverManager[IO](
    "org.h2.Driver",
    s"jdbc:h2:mem:$databaseName;DB_CLOSE_DELAY=0;INIT=runscript from './database-schema.sql'",
    "",
    ""
  )

}