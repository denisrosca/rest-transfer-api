package com.example.transfer

import cats.effect.IO
import doobie.scalatest.IOChecker
import doobie.util.transactor.Transactor

trait DatabaseSetup {
  self: IOChecker =>

  override def transactor: doobie.Transactor[IO] = Transactor.fromDriverManager[IO](
    "org.h2.Driver",
    "jdbc:h2:mem:test;DB_CLOSE_DELAY=0;INIT=runscript from './database-schema.sql'",
    "",
    ""
  )

}