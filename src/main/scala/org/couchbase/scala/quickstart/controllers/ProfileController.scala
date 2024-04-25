package org.couchbase.scala.quickstart.controllers

import org.couchbase.scala.quickstart.models.{Profile, ProfileInput, ProfileListingInput, PutProfileInput}

import java.util.UUID

trait ProfileController[F[_]] {

  def getProfile(pid: UUID): F[Either[String, Profile]]

  def postProfile(profileInput: ProfileInput): F[Either[String, Profile]]

  def putProfile(args: PutProfileInput): F[Either[String, Profile]]

  def deleteProfile(pid: UUID): F[Either[String, UUID]]

  def profileListing(args: ProfileListingInput): F[Either[String, List[Profile]]]

}
