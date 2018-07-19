package com.example.transfer.http

import java.util.UUID

import cats.effect.IO
import com.example.transfer.HttpChecker
import com.example.transfer.customer.Customer.Id
import com.example.transfer.customer.{Customer, CustomerService}
import com.example.transfer.http.JsonUtils._
import io.circe.Json
import io.circe.literal._
import org.http4s.circe._
import org.http4s.dsl.io._
import org.http4s.{EntityDecoder, Method, Request, Response, Uri}
import org.scalatest.{FeatureSpec, Matchers}

class CustomerEndpointTest extends FeatureSpec with Matchers with HttpChecker {

  implicit val customerEntityDecoder: EntityDecoder[IO, Customer] = jsonOf[IO, Customer]
  implicit val customersEntityDecoder: EntityDecoder[IO, List[Customer]] = jsonOf[IO, List[Customer]]

  feature("Retrieving all known customers") {
    scenario("Successfully return HTTP 200 with known customers") {
      val knownCustomers = List(
        Customer("Test Customer 1"),
        Customer("Test Customer 2")
      )

      val endpoint = new CustomerEndpoint[IO](serviceWithCustomers(knownCustomers))
      val result = get(endpoint, "/customers")
      check(result, Ok, Option(knownCustomers))
    }

    scenario("Request successful with zero known customers") {
      val endpoint = new CustomerEndpoint[IO](serviceWithCustomers(List.empty))
      val result = get(endpoint, "/customers")
      check(result, Ok, Option(List.empty[Customer]))
    }

    scenario("Handles exceptions by returning HTTP 500 Internal Server Error") {
      val endpoint = new CustomerEndpoint[IO](failingService)
      val result = get(endpoint, "/customers")
      check(result, InternalServerError, Option.empty[List[Customer]])
    }
  }

  feature("Retrieve customer details by id") {
    scenario("Query with existing id") {
      val customer = Customer("Test Customer 1")

      val endpoint = new CustomerEndpoint[IO](serviceWithCustomers(List(customer)))
      val result = get(endpoint, s"/customers/${customer.id.toString}")
      check(result, Ok, Option(customer))
    }

    scenario("Query with unknown id") {
      val customer = Customer("Test Customer 1")

      val endpoint = new CustomerEndpoint[IO](serviceWithCustomers(List(customer)))
      val result = get(endpoint, s"/customers/${UUID.randomUUID().toString}")
      check(result, NotFound, Option.empty[Customer])
    }

    scenario("Invalid id format") {
      val customer = Customer("Test Customer 1")

      val endpoint = new CustomerEndpoint[IO](serviceWithCustomers(List(customer)))
      val result = get(endpoint, "/customers/123124-34523af-fdd")
      check(result, NotFound, Option("Not found"))
    }
  }

  feature("Create new customer") {
    scenario("Handle POST request") {
      val customerName = "Test Customer1"
      val endpoint = new CustomerEndpoint[IO](serviceWithCustomers(List.empty[Customer]))
      val result = post(endpoint, "/customers", json"""{"name": $customerName}""")
      check(result, Created, (c: Customer) => c.name shouldBe customerName)
    }

    scenario("POST with malformed data returns 400 BadRequest") {
      val customerName = "Test Customer1"
      val endpoint = new CustomerEndpoint[IO](serviceWithCustomers(List.empty[Customer]))
      val result = post(endpoint, "/customers", json"""{"invalidField": $customerName}""")
      check(result, BadRequest, Option.empty[Customer])
    }
  }

  private def get(endpoint: CustomerEndpoint[IO], uri: String): IO[Response[IO]] = {
    endpoint.service.orNotFound.run(
      Request(method = Method.GET, uri = Uri.unsafeFromString(uri))
    )
  }

  def post(endpoint: CustomerEndpoint[IO], uri: String, payload: Json): IO[Response[IO]] = {
    for {
      request <- Request[IO](method = Method.POST, uri = Uri.unsafeFromString(uri)).withBody(payload)
      response <- endpoint.service.orNotFound.run(request)
    } yield response
  }

  private def serviceWithCustomers(knownCustomers: List[Customer]) = {
    new CustomerService[IO] {
      override def all: IO[List[Customer]] = {
        IO(knownCustomers)
      }

      override def newCustomer(name: String): IO[Customer] = {
        IO(Customer(name))
      }

      override def findById(id: Id): IO[Option[Customer]] = IO(knownCustomers.find(customer => customer.id == id))
    }
  }

  private def failingService: CustomerService[IO] = new CustomerService[IO] {
    override def all: IO[List[Customer]] = IO.raiseError(new RuntimeException("Operation Failed"))

    override def newCustomer(name: String): IO[Customer] = IO.raiseError(new RuntimeException("Operation Failed"))

    override def findById(id: Id): IO[Option[Customer]] = IO.raiseError(new RuntimeException("Operation Failed"))
  }

}