package com.example.transfer.account

import cats.Monad
import cats.syntax.functor._
import com.example.transfer.account.Account.Id
import com.example.transfer.customer.Customer.{Id => CustomerId}
import doobie.implicits._
import doobie.util.transactor.Transactor

trait AccountService[F[_]] {

  def all: F[List[Account]]

  def byId(id: Id): F[Option[Account]]

  def byCustomerId(id: CustomerId): F[List[Account]]

  def createAccountFor(customerId: Id): F[Account]

}

class AccountServiceImpl[F[_]: Monad](repo: AccountRepo, transactor: Transactor[F]) extends AccountService[F] {

  override def all: F[List[Account]] = {
    repo.all.to[List].transact(transactor)
  }

  override def byId(id: Id): F[Option[Account]] = {
    repo.byId(id).option.transact(transactor)
  }

  override def byCustomerId(id: CustomerId): F[List[Account]] = {
    repo.byCustomerId(id).to[List].transact(transactor)
  }

  override def createAccountFor(customerId: Id): F[Account] = {
    val account = Account(customerId)
    repo.insert(account)
      .run
      .transact(transactor)
      .map(_ => account)
  }
}