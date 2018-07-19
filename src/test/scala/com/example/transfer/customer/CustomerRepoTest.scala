package com.example.transfer.customer

import java.util.UUID

import com.example.transfer.TestDatabaseSetup
import doobie.scalatest._
import doobie.util.log.LogHandler
import doobie.implicits._
import org.scalatest.{FunSuite, Matchers}

class CustomerRepoTest extends FunSuite with Matchers with IOChecker with TestDatabaseSetup {

  val repo = new CustomerRepo(LogHandler.nop)
  override def databaseName: String = "customerRepoDB"

  test("Query all schema validation") {
    check(repo.all)
  }

  test("Query byId schema validation") {
    check(repo.byId(UUID.randomUUID()))
  }

  test("Insert schema validation") {
    check(repo.insert(Customer("Test Customer")))
  }

  test("Inserting and then querying all customers should return a single inserted customer") {
    val testCustomer = Customer("Test Customer")

    val transaction = for {
      _ <- repo.insert(testCustomer).run
      customerOpt <- repo.all.option
    } yield customerOpt

    val result = transaction.transact(transactor).unsafeRunSync()

    result shouldBe Some(testCustomer)
  }

  test("Inserting and then querying by id should return the correct customer") {
    val testCustomer = Customer("Test Customer")

    val transaction = for {
      _ <- repo.insert(testCustomer).run
      customerOpt <- repo.byId(testCustomer.id).unique
    } yield customerOpt

    val result = transaction.transact(transactor).unsafeRunSync()

    result shouldBe testCustomer
  }

}