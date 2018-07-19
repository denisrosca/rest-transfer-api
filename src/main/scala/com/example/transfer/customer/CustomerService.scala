package com.example.transfer.customer

import cats.Monad
import cats.syntax.functor._
import com.example.transfer.customer.Customer.Id
import doobie.implicits._
import doobie.util.transactor.Transactor

trait CustomerService[F[_]] {
  def findById(id: Id): F[Option[Customer]]

  def all: F[List[Customer]]

  def newCustomer(name: String): F[Customer]
}

class CustomerServiceImpl[F[_]: Monad](repo: CustomerRepo, transactor: Transactor[F]) extends CustomerService [F]{

  override val all: F[List[Customer]] = {
    repo.all.to[List].transact(transactor)
  }

  override def findById(id: Id): F[Option[Customer]] = {
    repo.byId(id).option.transact(transactor)
  }

  override def newCustomer(name: String): F[Customer] = {
    val customer = Customer(name)
    repo.insert(customer)
      .run
      .transact(transactor)
      .map(_ => customer)
  }

}