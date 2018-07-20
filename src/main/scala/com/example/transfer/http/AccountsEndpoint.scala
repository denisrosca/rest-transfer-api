package com.example.transfer.http

import cats.effect.Effect
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import com.example.transfer.account.{Account, AccountService}
import com.example.transfer.http.JsonUtils._
import com.example.transfer.http.QueryExtractors.UuidVar
import org.http4s.circe.jsonEncoderOf
import org.http4s.dsl.Http4sDsl
import org.http4s.{EntityEncoder, HttpService}

class AccountsEndpoint[F[_]: Effect](accountsService: AccountService[F]) extends Http4sDsl[F] {

  implicit val accountEntityEncoder: EntityEncoder[F, Account] = jsonEncoderOf[F, Account]
  implicit val accountListEntityEncoder: EntityEncoder[F, List[Account]] = jsonEncoderOf[F, List[Account]]

  val service: HttpService[F] = HttpService{
    case GET -> Path("accounts") =>
      accountsService.all
        .flatMap(accounts => Ok(accounts))
        .handleErrorWith(_ => InternalServerError())

    case _@POST -> Path("accounts") =>
      Forbidden()

    case GET -> Path("customers") / UuidVar(customerId) / "accounts" =>
      accountsService.byCustomerId(customerId)
      .flatMap(accounts => Ok(accounts))
      .handleErrorWith(_ => InternalServerError())

    case GET -> Path("customers") / UuidVar(customerId) / "accounts" / UuidVar(accountId) =>
      accountsService.byId(accountId)
        .flatMap{
          case Some(account) if account.customerId == customerId => Ok(account)
          case _ => NotFound()
        }
        .handleErrorWith(_ => InternalServerError())

    case GET -> Path("accounts") / UuidVar(accountId) =>
      accountsService.byId(accountId)
      .flatMap{
        case Some(account) => Ok(account)
        case None => NotFound()
      }
      .handleErrorWith(_ => InternalServerError())

    case _@POST -> Path("customers") / UuidVar(customerId) / "accounts" =>
      accountsService.createAccountFor(customerId)
        .flatMap(account => Created(account))
        .handleErrorWith(_ => InternalServerError())
  }
}