package org.couchbase.scala.quickstart.controllers

import cats.Id
import cats.effect.IO
import org.couchbase.scala.quickstart.models.{Profile, ProfileInput}

import java.util.UUID

class FakeProfileControllerIO(profileController: ProfileController[Id])
    extends ProfileController[IO] {
  override def getProfile(pid: UUID): IO[Either[String, Profile]] =
    IO.pure(profileController.getProfile(pid))

  override def postProfile(
      profileInput: ProfileInput
  ): IO[Either[String, Profile]] =
    IO.pure(profileController.postProfile(profileInput))

  override def putProfile(
      pid: UUID,
      profileInput: ProfileInput
  ): IO[Either[String, Profile]] =
    IO.pure(profileController.putProfile(pid, profileInput))

  override def deleteProfile(pid: UUID): IO[Either[String, UUID]] =
    IO.pure(profileController.deleteProfile(pid))

  override def profileListing(
      limit: Option[Int],
      skip: Option[Int],
      search: String
  ): IO[Either[String, List[Profile]]] =
    IO.pure(profileController.profileListing(limit, skip, search))
}
