package com.example.transfer.http

import cats.effect.Effect
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import com.example.transfer.customer.{Customer, CustomerService}
import com.example.transfer.http.CustomerEndpoint.NewCustomerRequest
import com.example.transfer.http.JsonUtils._
import com.example.transfer.http.QueryExtractors._
import io.circe.Decoder
import io.circe.generic.semiauto._
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.dsl.Http4sDsl
import org.http4s.{DecodeFailure, EntityDecoder, EntityEncoder, HttpService}

class CustomerEndpoint[F[_]: Effect](customerService: CustomerService[F]) extends Http4sDsl[F] {

  implicit val customerEntityEncoder: EntityEncoder[F, Customer] = jsonEncoderOf[F, Customer]
  implicit val customerListEntityEncoder: EntityEncoder[F, List[Customer]] = jsonEncoderOf[F, List[Customer]]

  private implicit val customerRequestEntityDecoder: EntityDecoder[F, NewCustomerRequest] = jsonOf[F, NewCustomerRequest]

  val service: HttpService[F] = HttpService{
    case GET -> Path("customers") =>
      customerService.all
        .flatMap(customers => Ok(customers))
        .handleErrorWith(_ => InternalServerError())

    case GET -> Path("customers") / UuidVar(id)=>
      customerService.findById(id)
        .flatMap{
          case Some(customer) => Ok(customer)
          case None => NotFound()
        }
        .handleErrorWith(_ => InternalServerError())

    case req@POST -> Path("customers") =>
      req
        .as[NewCustomerRequest]
        .flatMap(newCustomerRequest => customerService.newCustomer(newCustomerRequest.name))
        .flatMap(client => Created(client))
        .handleErrorWith{
          case _: DecodeFailure => BadRequest()
          case _ => InternalServerError()
        }
  }

}

object CustomerEndpoint {

  private case class NewCustomerRequest(name: String)
  private implicit val newCustomerRequestDecoder: Decoder[NewCustomerRequest] = deriveDecoder
}