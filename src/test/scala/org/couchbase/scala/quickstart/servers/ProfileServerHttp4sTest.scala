package org.couchbase.scala.quickstart.servers

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import io.circe.Json
import org.couchbase.scala.quickstart.controllers.{
  FakeProfileController,
  FakeProfileControllerIO
}
import org.couchbase.scala.quickstart.models.{Profile, ProfileInput}
import org.http4s.circe.CirceEntityCodec._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.implicits._
import org.http4s.{HttpApp, Method, Request, Status}
import org.scalatest.flatspec.AnyFlatSpec

import java.util.UUID

class ProfileServerHttp4sTest extends AnyFlatSpec {

  implicit val runtime: IORuntime = cats.effect.unsafe.IORuntime.global

  val fakeHttp4sServer: ProfileServerHttp4s[IO] = new ProfileServerHttp4s[IO](
    new FakeProfileControllerIO(FakeProfileController)
  )

  val http4sApp: HttpApp[IO] = fakeHttp4sServer.routes.orNotFound
  val client: Client[IO] = Client.fromHttpApp(http4sApp)

  "Getting a valid profile pid" should "successfully return a profile" in {
    val profile = FakeProfileController
      .postProfile(
        ProfileInput(
          "Jane",
          "Doe",
          "janedoe@example.com",
          "plaintextisbesttext"
        )
      )
      .toOption
      .get
    val request: Request[IO] = Request(
      method = Method.GET,
      uri = uri"/api/v1/profile/".withQueryParam("id", profile.pid.toString)
    )

    val profileResponse =
      client
        .expect[Json](request)
        .map(_.as[Profile].toOption.get)
        .unsafeRunSync()

    assert(profile === profileResponse)
  }

  "Posting a valid profile input" should "successfully return a profile" in {
    val profileInput =
      ProfileInput("Azure", "Diamond", "az@example.com", "hunter2")
    val request: Request[IO] = Request(
      method = Method.POST,
      uri = uri"/api/v1/profile"
    ).withEntity(profileInput)

    val response = client.expect[Json](request)
    val profile = response.unsafeRunSync().as[Profile].toOption.get

    assert(profile.firstName === profileInput.firstName)
    assert(profile.lastName === profileInput.lastName)
    assert(profile.email === profileInput.email)
    assert(profile.saltedPassword !== profileInput.password)
  }

  "Deleting a valid profile" should "succeed" in {
    // First add the Profile via POST
    val profileInput =
      ProfileInput("Azure", "Diamond", "az@example.com", "hunter2")
    val postRequest: Request[IO] = Request(
      method = Method.POST,
      uri = uri"/api/v1/profile"
    ).withEntity(profileInput)
    val postResponse = client.expect[Json](postRequest)
    val profile = postResponse.unsafeRunSync().as[Profile].toOption.get

    val deleteRequest: Request[IO] = Request(
      method = Method.DELETE,
      uri = uri"/api/v1/profile".withQueryParam("id", profile.pid.toString)
    )
    val deleteResponse = client.expect[Json](deleteRequest)
    val uuid: UUID = deleteResponse.unsafeRunSync().as[UUID].toOption.get

    assert(uuid === profile.pid)
  }

  "Deleting an invalid profile" should "fail" in {
    val randomUUID = UUID.randomUUID()
    val deleteRequest: Request[IO] = Request(
      method = Method.DELETE,
      uri = uri"/api/v1/profile".withQueryParam("id", randomUUID.toString)
    )

    val deleteResponseStatus: Status =
      client.status(deleteRequest).unsafeRunSync()

    assert(deleteResponseStatus === Status.BadRequest)
  }

}
