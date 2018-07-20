package com.example.transfer

import cats.effect.IO
import io.circe.Json
import org.http4s.{EntityDecoder, HttpService, Method, Request, Response, Status, Uri}
import org.scalatest.{Assertion, Matchers}
import org.http4s.dsl.io._
import org.http4s.circe._

trait HttpChecker {

  self: Matchers =>

  def get(endpoint: HttpService[IO], uri: String): IO[Response[IO]] = {
    endpoint.orNotFound.run(
      Request(method = Method.GET, uri = Uri.unsafeFromString(uri))
    )
  }

  def post(endpoint: HttpService[IO], uri: String, payload: Json): IO[Response[IO]] = {
    for {
      request <- Request[IO](method = Method.POST, uri = Uri.unsafeFromString(uri)).withBody(payload)
      response <- endpoint.orNotFound.run(request)
    } yield response
  }

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
