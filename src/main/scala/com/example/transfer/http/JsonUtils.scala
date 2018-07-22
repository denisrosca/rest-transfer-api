package com.example.transfer.http

import java.time.Instant

import com.example.transfer.account.Account
import com.example.transfer.customer.Customer
import com.example.transfer.transaction.Transfer
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

import scala.util.Try

object JsonUtils {

  implicit val instantEncoder: Encoder[Instant] = Encoder[String].contramap(instant => instant.toString)
  implicit val instantDecoder: Decoder[Instant] = Decoder[String].emapTry(value => Try(Instant.parse(value)))

  implicit val transferEncoder: Encoder[Transfer] = deriveEncoder
  implicit val transferDecoder: Decoder[Transfer] = deriveDecoder

  implicit val customerEncoder: Encoder[Customer] = deriveEncoder
  implicit val customerDecoder: Decoder[Customer] = deriveDecoder

  implicit val accountEncoder: Encoder[Account] = deriveEncoder
  implicit val accountDecoder: Decoder[Account] = deriveDecoder

}