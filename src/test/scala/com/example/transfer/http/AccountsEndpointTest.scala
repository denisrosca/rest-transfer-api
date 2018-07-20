package com.example.transfer.http

import java.util.UUID

import cats.effect.IO
import com.example.transfer.HttpChecker
import com.example.transfer.account.Account.Id
import com.example.transfer.account.{Account, AccountService}
import com.example.transfer.customer.Customer.{Id => CustomerId}
import com.example.transfer.http.JsonUtils._
import io.circe.literal._
import org.http4s.EntityDecoder
import org.http4s.circe._
import org.http4s.dsl.io._
import org.scalatest.{FeatureSpec, Matchers}

class AccountsEndpointTest extends FeatureSpec with Matchers with HttpChecker {

  implicit val accountEntityDecoder: EntityDecoder[IO, Account] = jsonOf[IO, Account]
  implicit val accountsEntityDecoder: EntityDecoder[IO, List[Account]] = jsonOf[IO, List[Account]]

  feature("Account retrieval") {
    scenario("GET /accounts returns HTTP 200 with known accounts") {
      val knownAccounts = List(
        Account(customerId = UUID.randomUUID()),
        Account(customerId = UUID.randomUUID()),
        Account(customerId = UUID.randomUUID())
      )

      val endpoint = new AccountsEndpoint[IO](serviceWithAccounts(knownAccounts))
      val result = get(endpoint.service, "/accounts")
      check(result, Ok, Option(knownAccounts))
    }

    scenario("GET /accounts returns HTTP 500 when service fails with error") {
      val endpoint = new AccountsEndpoint[IO](failingService())
      val result = get(endpoint.service, "/accounts")
      check(result, InternalServerError, Option.empty[List[Account]])
    }

    scenario("GET /customers/$id/accounts returns HTTP 200 with customer accounts") {
      val requestingCustomer = UUID.randomUUID()
      val customerAccounts = List(
        Account(customerId = requestingCustomer),
        Account(customerId = requestingCustomer)
      )
      val otherAccounts = List(
        Account(customerId = UUID.randomUUID()),
        Account(customerId = UUID.randomUUID())
      )

      val endpoint = new AccountsEndpoint[IO](serviceWithAccounts(customerAccounts ++ otherAccounts))
      val result = get(endpoint.service, s"/customers/${requestingCustomer.toString}/accounts")
      check(result, Ok, Option(customerAccounts))
    }

    scenario("GET /accounts/$id returns HTTP 200 with account details") {
      val account = Account(customerId = UUID.randomUUID())
      val knownAccounts = List(
        account,
        Account(customerId = UUID.randomUUID()),
        Account(customerId = UUID.randomUUID())
      )

      val endpoint = new AccountsEndpoint[IO](serviceWithAccounts(knownAccounts))
      val result = get(endpoint.service, s"/accounts/${account.id.toString}")
      check(result, Ok, Option(account))
    }

    scenario("GET /customers/$cid/accounts/$aid returns HTTP 200 with account details") {
      val account = Account(customerId = UUID.randomUUID())
      val knownAccounts = List(
        account,
        Account(customerId = UUID.randomUUID()),
        Account(customerId = UUID.randomUUID())
      )

      val endpoint = new AccountsEndpoint[IO](serviceWithAccounts(knownAccounts))
      val result = get(endpoint.service, s"/customers/${account.customerId.toString}/accounts/${account.id.toString}")
      check(result, Ok, Option(account))
    }

    scenario("GET /customers/$cid/accounts/$aid returns HTTP 404 if account doesn't belong to customer") {
      val account = Account(customerId = UUID.randomUUID())
      val knownAccounts = List(
        account,
        Account(customerId = UUID.randomUUID()),
        Account(customerId = UUID.randomUUID())
      )

      val endpoint = new AccountsEndpoint[IO](serviceWithAccounts(knownAccounts))
      val result = get(endpoint.service, s"/customers/${UUID.randomUUID().toString}/accounts/${account.id.toString}")
      check(result, NotFound, Option.empty[Account])
    }
  }

  feature("Account creation") {
    scenario("POST /customers/$customerId/accounts returns HTTP 201 for valid content") {
      val customerId = UUID.randomUUID()
      val endpoint = new AccountsEndpoint[IO](serviceWithAccounts(List.empty[Account]))
      val result = post(endpoint.service, s"/customers/${customerId.toString}/accounts", json"""{}""")
      check(result, Created, (account: Account) => account.customerId shouldBe customerId)
    }

    scenario("POST /accounts returns HTTP 403") {
      val endpoint = new AccountsEndpoint[IO](serviceWithAccounts(List.empty[Account]))
      val result = post(endpoint.service, "/accounts", json"""{}""")
      check(result, Forbidden, Option.empty[Account])
    }
  }

  private def serviceWithAccounts(knownAccounts: List[Account]) = {
    new AccountService[IO] {
      override def all: IO[List[Account]] = IO(knownAccounts)

      override def byId(id: CustomerId): IO[Option[Account]] = IO(knownAccounts.find(account => account.id == id))

      override def byCustomerId(id: CustomerId): IO[List[Account]] = IO(knownAccounts.filter(account => account.customerId == id))

      override def createAccountFor(id: CustomerId): IO[Account] = IO(Account(customerId = id))
    }
  }

  def failingService(): AccountService[IO] = new AccountService[IO] {
    override def all: IO[List[Account]] = IO.raiseError(new Throwable("Test failure"))

    override def byId(id: Id): IO[Option[Account]] = IO.raiseError(new Throwable("Test failure"))

    override def byCustomerId(id: CustomerId): IO[List[Account]] = IO.raiseError(new Throwable("Test failure"))

    override def createAccountFor(customerId: Id): IO[Account] = IO.raiseError(new Throwable("Test failure"))
  }
}
