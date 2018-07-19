package com.example.transfer

import cats.effect.IO
import org.http4s.{EntityDecoder, Response, Status}
import org.scalatest.{Assertion, Matchers}

trait HttpChecker {

  self: Matchers =>

  def check[A]
    (actual: IO[Response[IO]], expectedStatus: Status, expectedBody: Option[A])
    (implicit ev: EntityDecoder[IO, A]): Assertion =
  {
    val actualResp = actual.unsafeRunSync
    actualResp.status shouldBe expectedStatus
    expectedBody.fold[Assertion](
      actualResp.body.compile.toVector.unsafeRunSync.isEmpty shouldBe true)( // Verify Response's body is empty.
      expected => actualResp.as[A].unsafeRunSync shouldBe expected
    )
  }

  def check[A]
    (actual: IO[Response[IO]], expectedStatus: Status, payloadAssertion: A => Assertion)
    (implicit ev: EntityDecoder[IO, A]): Assertion =
  {
    val actualResp = actual.unsafeRunSync
    actualResp.status shouldBe expectedStatus
    payloadAssertion(actualResp.as[A].unsafeRunSync())
  }

}
