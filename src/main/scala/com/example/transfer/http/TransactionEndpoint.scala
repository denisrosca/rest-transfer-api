package com.example.transfer.http

import java.time.Instant
import java.util.UUID

import cats.effect.Effect
import cats.syntax.applicativeError._
import cats.syntax.flatMap._
import com.example.transfer.account.Account.{Id => AccountId}
import com.example.transfer.http.TransactionEndpoint._
import com.example.transfer.transaction.Transaction.{Id => TransactionId}
import com.example.transfer.transaction.{TransactionFailed, TransactionSuccessful, Transfer, TransferService}
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._
import org.http4s.circe.{jsonEncoderOf, jsonOf}
import org.http4s.dsl.Http4sDsl
import org.http4s.{DecodeFailure, EntityDecoder, EntityEncoder, HttpService, QueryParamDecoder}
import com.example.transfer.http.JsonUtils._
import com.example.transfer.http.QueryExtractors.UuidVar
import org.http4s.dsl.impl.OptionalQueryParamDecoderMatcher

class TransactionEndpoint[F[_]: Effect](transferService: TransferService[F]) extends Http4sDsl[F] {

  implicit val transferResponseEncoder: Encoder[TransferResponse] = deriveEncoder
  implicit val transferResponseEntityEncoder: EntityEncoder[F, TransferResponse] = jsonEncoderOf[F, TransferResponse]

  implicit val transferEntityEncoder: EntityEncoder[F, Transfer] = jsonEncoderOf[F, Transfer]
  implicit val transferListEntityEncoder: EntityEncoder[F, List[Transfer]] = jsonEncoderOf[F, List[Transfer]]

  implicit val newTransferRequestDecoder: Decoder[TransferRequest] = deriveDecoder
  implicit val newTransferRequestEntityDecoder: EntityDecoder[F, TransferRequest] = jsonOf[F, TransferRequest]

  val service: HttpService[F] = HttpService{
    case GET -> Path("transfers") / UuidVar(transferId) =>
      transferService.byId(transferId)
        .flatMap{
          case Some(transfer) => Ok(transfer)
          case None => NotFound()
        }
        .handleErrorWith(_ => InternalServerError())

    case GET -> Path("transfers") :? SourceAccountMatcher(sourceOpt) +& DestinationAccountMatcher(destinationOpt) =>
      val result = (sourceOpt, destinationOpt) match {
        case (Some(source), Some(destination)) => transferService.between(source, destination)
        case (Some(source), None) => transferService.bySource(source)
        case (None, Some(destination)) => transferService.byDestination(destination)
        case (None, None) => transferService.all
      }
      result
        .flatMap(transfers => Ok(transfers))
        .handleErrorWith(_ => InternalServerError())
    case req@POST -> Path("transfers") =>
      req.as[TransferRequest]
        .flatMap{request =>
          val transfer = request.toDataModel
          transferService.transfer(transfer)
            .flatMap{
              case TransactionSuccessful => Created(TransferSuccessful(transfer.id): TransferResponse)
              case e: TransactionFailed => Ok(TransferFailed(e.reason): TransferResponse)
            }
        }
        .handleErrorWith{
          case _: DecodeFailure => BadRequest()
          case e => InternalServerError(e.toString)
        }
  }

}

object TransactionEndpoint {

  case class TransferRequest(source: AccountId, destination: AccountId, amount: BigDecimal, description: Option[String]) {
    def toDataModel: Transfer = Transfer(
      UUID.randomUUID(),
      source,
      destination,
      amount,
      description,
      Instant.now()
    )
  }

  sealed trait Status
  case object Success extends Status
  case object Failed extends Status

  sealed trait TransferResponse {
    def status: Status
  }

  case class TransferSuccessful(id: TransactionId) extends TransferResponse {
    override def status: Status = Success
  }
  case class TransferFailed(reason: String) extends TransferResponse {
    override def status: Status = Failed
  }

  implicit val accountIdParamDecoder: QueryParamDecoder[AccountId] = {
    QueryParamDecoder.fromUnsafeCast[UUID](p => UUID.fromString(p.value))("AccountId")
  }
  object SourceAccountMatcher extends OptionalQueryParamDecoderMatcher[AccountId]("source")
  object DestinationAccountMatcher extends OptionalQueryParamDecoderMatcher[AccountId]("destination")

}