package com.example.transfer.transaction

import cats.Monad
import cats.free.Free
import cats.instances.option._
import cats.syntax.apply._
import com.example.transfer.account.Account.{Id => AccountId}
import com.example.transfer.account.{Account, AccountRepo}
import doobie.free.connection.{ConnectionIO, ConnectionOp}
import doobie.implicits._
import doobie.util.transactor.Transactor

trait TransferService[F[_]] {
  def byId(transferId: AccountId): F[Option[Transfer]]

  def all: F[List[Transfer]]

  def bySource(source: AccountId): F[List[Transfer]]

  def byDestination(destination: AccountId): F[List[Transfer]]

  def between(source: AccountId, destination: AccountId): F[List[Transfer]]

  def transfer(transfer: Transfer): F[TransactionResult]

}

class TransferServiceImpl[F[_]: Monad](accountRepo: AccountRepo, transferRepo: TransferRepo, transactor: Transactor[F]) extends TransferService[F]{

  override def all: F[List[Transfer]] = {
    transferRepo.all
      .to[List]
      .transact(transactor)
  }

  override def byId(transferId: AccountId): F[Option[Transfer]] = {
    transferRepo.byId(transferId)
      .option
      .transact(transactor)
  }

  override def bySource(source: AccountId): F[List[Transfer]] = {
    transferRepo.bySource(source)
      .to[List]
      .transact(transactor)
  }

  override def byDestination(destination: AccountId): F[List[Transfer]] = {
    transferRepo.byDestination(destination)
      .to[List]
      .transact(transactor)
  }

  override def between(source: AccountId, destination: AccountId): F[List[Transfer]] = {
    transferRepo.between(source, destination)
      .to[List]
      .transact(transactor)
  }

  override def transfer(transfer: Transfer): F[TransactionResult] = {
    def processInternal(source: Account, destination: Account): ConnectionIO[TransactionResult] = {
      if(source.balance < transfer.amount) {
        pure(InsufficientFunds)
      } else {
        for {
          _ <- accountRepo.updateBalance(source.copy(balance = source.balance - transfer.amount)).run
          _ <- accountRepo.updateBalance(destination.copy(balance = destination.balance + transfer.amount)).run
          _ <- transferRepo.insert(transfer).run
        } yield TransactionSuccessful
      }
    }

    val transaction = for {
      sourceAccountOpt <- accountRepo.lock(transfer.source).option
      destinationAccountOpt <- accountRepo.lock(transfer.destination).option
      result <- (sourceAccountOpt, destinationAccountOpt)
        .mapN(processInternal)
        .getOrElse(pure[TransactionResult](InvalidAccount))
    } yield result

    transaction.transact(transactor)
  }


  private def pure[A](value: A): Free[ConnectionOp, A] = {
    Free.pure(value)
  }

}