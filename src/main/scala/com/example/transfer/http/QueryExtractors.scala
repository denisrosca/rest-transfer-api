package com.example.transfer.http

import java.util.UUID

import scala.util.Try

object QueryExtractors {

  object UuidVar{
    def unapply(str: String): Option[UUID] =
      if (!str.isEmpty)
        Try(UUID.fromString(str)).toOption
      else
        None
  }

}
