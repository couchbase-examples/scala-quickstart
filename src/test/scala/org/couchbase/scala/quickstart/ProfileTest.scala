package org.couchbase.scala.quickstart

import org.couchbase.scala.quickstart.models.{Profile, ProfileInput}
import org.scalatest.flatspec.AnyFlatSpec

class ProfileTest extends AnyFlatSpec {

  "Profile" should "have a UUID and salted password when converted from ProfileInput" in {
    val profileInput = ProfileInput("John", "Doe", "johndoe@example.com", "mysecretpassword")

    val profile: Profile = Profile.fromProfileInput(profileInput).get

    assert(profile.saltedPassword !== profileInput.password)
    assert(profile.pid.toString.nonEmpty)
  }
}
