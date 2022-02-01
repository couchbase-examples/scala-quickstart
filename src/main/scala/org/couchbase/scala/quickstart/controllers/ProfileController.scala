package org.couchbase.scala.quickstart.controllers

import org.couchbase.scala.quickstart.models.{Profile, ProfileInput}

import java.util.UUID

trait ProfileController[F[_]] {

  def getProfile(pid: UUID): F[Either[String, Profile]]

  def postProfile(profileInput: ProfileInput): F[Either[String, Profile]]

  def putProfile(pid: UUID, profileInput: ProfileInput): F[Either[String, Profile]]

  def deleteProfile(pid: UUID): F[Either[String, UUID]]

  def profileListing(limit: Option[Int], skip: Option[Int], search: String): F[Either[String, List[Profile]]]

}
