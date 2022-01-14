package org.couchbase.scala.quickstart.servers
import org.couchbase.scala.quickstart.models.{Profile, ProfileInput}

import java.util.UUID
import scala.collection.mutable
import scala.util.{Failure, Success}

object ProfileController {

  val newProfile = Profile("firstName", "lastName", "email@domain.com", "password123").get

  var profileMap = mutable.HashMap[UUID, Profile](
    newProfile.pid -> newProfile
  )

  def getProfile(pid: UUID): Either[String, Profile] = {
    profileMap.get(pid) match {
      case None => Left(s"Missing profile ID: $pid")
      case Some(s) => Right(s)
    }
  }

  def postProfile(profileInput: ProfileInput): Either[String, Profile] = {
    Profile.fromProfileInput(profileInput) match {
      case Failure(exception) => Left(exception.toString)
      case Success(profile: Profile) =>
        Right {
          profileMap += (profile.pid -> profile)
          profile
        }
    }
  }

  def deleteProfile(pid: UUID): Either[String, Profile] = {
    profileMap.remove(pid) match {
      case None => Left(s"Profile ID: $pid was not found.")
      case Some(profile) => Right(profile)
    }
  }

  def profileListing(): Either[Unit, List[Profile]] = {
    Right(profileMap.values.toList)
  }

}
