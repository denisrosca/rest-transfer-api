package com.example.transfer.account

import java.util.UUID

import com.example.transfer.TestDatabaseSetup
import com.example.transfer.customer.{Customer, CustomerRepo}
import doobie.scalatest.IOChecker
import doobie.util.log.LogHandler
import org.scalatest.{FunSuite, Matchers}
import doobie.implicits._

class AccountRepoTest extends FunSuite with Matchers with IOChecker with TestDatabaseSetup {

  val repo = new AccountRepo(LogHandler.nop)
  val customerRepo = new CustomerRepo(LogHandler.nop)

  override def databaseName: String = "accountRepoDB"

  test("Query all schema validation") {
    check(repo.all)
  }

  test("Query byId schema validation") {
    check(repo.byId(UUID.randomUUID()))
  }

  test("Insert schema validation") {
    check(repo.insert(Account(UUID.randomUUID())))
  }

  test("Inserting and then querying all accounts will return exactly one row") {
    val customer = Customer("Test Customer")
    val testAccount = Account(customer.id)

    val transaction = for {
      _ <- customerRepo.insert(customer).run
      _ <- repo.insert(testAccount).run
      account <- repo.all.unique
    } yield account

    val result = transaction.transact(transactor).unsafeRunSync()

    result shouldBe testAccount
  }

  test("Inserting and then querying by id yields the correct data") {
    val customer = Customer("Test Customer")
    val testAccount = Account(customer.id)

    val transaction = for {
      _ <- customerRepo.insert(customer).run
      _ <- repo.insert(testAccount).run
      account <- repo.byId(testAccount.id).unique
    } yield account

    val result = transaction.transact(transactor).unsafeRunSync()

    result shouldBe testAccount
  }

}