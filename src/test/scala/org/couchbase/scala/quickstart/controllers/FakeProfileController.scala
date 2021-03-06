package org.couchbase.scala.quickstart.controllers

import cats.Id
import org.couchbase.scala.quickstart.models.{Profile, ProfileInput}

import java.util.UUID
import scala.collection.mutable
import scala.util.{Failure, Success}

/** Fake controller that uses an in memory map to keep track of profiles.
  */
object FakeProfileController extends ProfileController[Id] {

  val exampleProfile: Profile =
    Profile("firstName", "lastName", "email@domain.com", "password123").get

  var profileMap: mutable.Map[UUID, Profile] = mutable.HashMap[UUID, Profile](
    exampleProfile.pid -> exampleProfile
  )

  override def getProfile(pid: UUID): Either[String, Profile] = {
    profileMap.get(pid) match {
      case None    => Left(s"Missing profile ID: $pid")
      case Some(s) => Right(s)
    }
  }

  override def postProfile(
      profileInput: ProfileInput
  ): Either[String, Profile] = {
    Profile.fromProfileInput(profileInput) match {
      case Failure(exception) => Left(exception.toString)
      case Success(profile: Profile) =>
        Right {
          profileMap += (profile.pid -> profile)
          profile
        }
    }
  }

  override def putProfile(
      pid: UUID,
      profileInput: ProfileInput
  ): Either[String, Profile] = {
    Profile.fromProfileInput(profileInput).map(_.copy(pid = pid)) match {
      case Failure(exception) => Left(exception.toString)
      case Success(profile: Profile) =>
        Right {
          profileMap += (pid -> profile)
          profile
        }
    }
  }

  override def deleteProfile(pid: UUID): Either[String, UUID] = {
    profileMap.remove(pid) match {
      case None    => Left(s"Profile ID: $pid was not found.")
      case Some(_) => Right(pid)
    }
  }

  override def profileListing(
      limit: Option[Int],
      skip: Option[Int],
      search: String
  ): Either[String, List[Profile]] = {
    Right(
      profileMap.values.toList
        .filter(p =>
          (p.firstName.toLowerCase contains search.toLowerCase) || (p.lastName.toLowerCase contains search.toLowerCase)
        )
        .slice(skip.getOrElse(0), skip.getOrElse(0) + limit.getOrElse(5))
    )
  }
}
