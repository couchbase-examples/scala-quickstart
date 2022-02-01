package org.couchbase.scala.quickstart.models

import com.couchbase.client.scala.kv.GetResult
import io.circe.Decoder

import scala.util.{Failure, Success}

object CirceGetResult {
  implicit class CirceGetResult(result: GetResult) {
    def contentAsCirceJson[T](implicit decoder: Decoder[T]): Either[String, T] = {
      result.contentAs[io.circe.Json] match {
        case Failure(exception) => Left(exception.getMessage)
        case Success(json) => json.as[T].left.map(_.toString)
      }
    }
  }
}
