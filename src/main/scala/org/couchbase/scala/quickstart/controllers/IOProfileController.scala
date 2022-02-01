package org.couchbase.scala.quickstart.controllers

import cats.effect.IO
import org.couchbase.scala.quickstart.models.{Profile, ProfileInput}

import java.util.UUID
import scala.concurrent.Future

/** Takes an existing Future based profile controller and transforms the effect into [cats.effect.IO].
  *
  * This is a naive implementation, and just blocks on the Future. We can be more smart by actually trying to
  * use the streaming API and streaming IO.
  */
class IOProfileController(profileController: ProfileController[Future])
    extends ProfileController[IO] {

  override def getProfile(pid: UUID): IO[Either[String, Profile]] =
    IO.fromFuture(IO.blocking(profileController.getProfile(pid)))

  override def postProfile(
      profileInput: ProfileInput
  ): IO[Either[String, Profile]] =
    IO.fromFuture(IO.blocking(profileController.postProfile(profileInput)))

  override def deleteProfile(pid: UUID): IO[Either[String, UUID]] =
    IO.fromFuture(IO.blocking(profileController.deleteProfile(pid)))

  override def putProfile(
      pid: UUID,
      profileInput: ProfileInput
  ): IO[Either[String, Profile]] =
    IO.fromFuture(IO.blocking(profileController.putProfile(pid, profileInput)))

  override def profileListing(
      limit: Option[Int],
      skip: Option[Int],
      search: String
  ): IO[Either[String, List[Profile]]] =
    IO.fromFuture(
      IO.blocking(profileController.profileListing(limit, skip, search))
    )
}
