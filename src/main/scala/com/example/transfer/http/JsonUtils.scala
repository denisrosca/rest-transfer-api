package com.example.transfer.http

import java.time.Instant

import com.example.transfer.account.Account
import com.example.transfer.customer.Customer
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

import scala.util.Try

object JsonUtils {

  implicit val instantEncoder: Encoder[Instant] = Encoder[String].contramap(instant => instant.toString)
  implicit val instantDecoder: Decoder[Instant] = Decoder[String].emapTry(value => Try(Instant.parse(value)))


  implicit val customerEncoder: Encoder[Customer] = deriveEncoder
  implicit val customerDecoder: Decoder[Customer] = deriveDecoder

  implicit val accountEncoder: Encoder[Account] = deriveEncoder
  implicit val accountDecoder: Decoder[Account] = deriveDecoder

}