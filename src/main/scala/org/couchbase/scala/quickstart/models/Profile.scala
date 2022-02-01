package org.couchbase.scala.quickstart.models

import io.circe.{Decoder, Encoder}
import sttp.tapir.Schema

import java.util.UUID
import scala.util.Try

final case class Profile(
    pid: UUID,
    firstName: String,
    lastName: String,
    email: String,
    saltedPassword: String
)

object Profile {
  import io.circe.generic.semiauto._
  implicit val profileDecoder: Decoder[Profile] = deriveDecoder
  implicit val profileEncoder: Encoder[Profile] = deriveEncoder
  implicit val profileSchema: Schema[Profile] = Schema.derived
  def apply(
      firstName: String,
      lastName: String,
      email: String,
      password: String
  ): Try[Profile] = {
    import com.github.t3hnar.bcrypt._
    for {
      salted <- password.bcryptSafeBounded
      profile <- Try {
        new Profile(UUID.randomUUID(), firstName, lastName, email, salted)
      }
    } yield profile
  }

  def fromProfileInput(profileInput: ProfileInput): Try[Profile] = {
    import com.github.t3hnar.bcrypt._
    profileInput match {
      case ProfileInput(firstName, lastName, email, password) =>
        for {
          salted <- password.bcryptSafeBounded
        } yield Profile(UUID.randomUUID(), firstName, lastName, email, salted)
    }
  }
}
