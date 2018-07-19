package com.example.transfer

import cats.effect.IO
import com.example.transfer.customer.{CustomerRepo, CustomerServiceImpl}
import com.example.transfer.http.CustomerEndpoint
import doobie.h2.H2Transactor
import doobie.implicits._
import doobie.util.log.LogHandler
import fs2.StreamApp
import org.http4s.server.blaze.BlazeBuilder

object Main extends StreamApp[IO] {

  import scala.concurrent.ExecutionContext.Implicits.global

  override def stream(args: List[String], requestShutdown: IO[Unit]): fs2.Stream[IO, StreamApp.ExitCode] = {
    val databaseConnectionString = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;INIT=runscript from './database-schema.sql'"
    for {
      transactor <- H2Transactor.stream[IO](databaseConnectionString, "", "")

      customerService = new CustomerServiceImpl[IO](new CustomerRepo(LogHandler.nop), transactor)

      exitCode <- BlazeBuilder[IO]
        .bindHttp(8080, "0.0.0.0")
        .mountService(new CustomerEndpoint[IO](customerService).service, "/api/")
        .serve
    } yield exitCode
  }

}