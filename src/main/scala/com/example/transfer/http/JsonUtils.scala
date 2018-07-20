package com.example.transfer.http

import com.example.transfer.account.Account
import com.example.transfer.customer.Customer
import io.circe.{Decoder, Encoder}
import io.circe.generic.semiauto._

object JsonUtils {

  implicit val customerEncoder: Encoder[Customer] = deriveEncoder
  implicit val customerDecoder: Decoder[Customer] = deriveDecoder
  implicit val accountEncoder: Encoder[Account] = deriveEncoder
  implicit val accountDecoder: Decoder[Account] = deriveDecoder

}