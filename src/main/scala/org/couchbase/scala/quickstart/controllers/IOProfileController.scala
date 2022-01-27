package org.couchbase.scala.quickstart.controllers

import cats.effect.IO
import org.couchbase.scala.quickstart.models.{Profile, ProfileInput}

import java.util.UUID
import scala.concurrent.Future

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

  override def profileListing(
      limit: Option[Int],
      skip: Option[Int],
      search: String,
  ): IO[Either[String, List[Profile]]] =
    IO.fromFuture(IO.blocking(profileController.profileListing(limit, skip, search)))
}
