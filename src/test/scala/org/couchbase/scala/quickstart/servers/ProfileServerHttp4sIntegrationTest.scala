package org.couchbase.scala.quickstart.servers

import cats.effect.IO
import cats.effect.unsafe.IORuntime
import com.couchbase.client.scala.Bucket
import com.couchbase.client.scala.manager.collection.CollectionSpec
import io.circe.Json
import org.couchbase.scala.quickstart.components.ProdCouchbaseConnection
import org.couchbase.scala.quickstart.config.QuickstartConfig
import org.couchbase.scala.quickstart.controllers.{
  CouchbaseProfileController,
  IOProfileController
}
import org.couchbase.scala.quickstart.models.{Profile, ProfileInput}
import org.http4s.circe.CirceEntityCodec._
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.implicits.http4sLiteralsSyntax
import org.http4s.{HttpApp, Method, Request, Status}
import org.scalatest.BeforeAndAfter
import org.scalatest.flatspec.AnyFlatSpec
import pureconfig.ConfigSource

import java.util.UUID
import scala.concurrent.Await
import scala.concurrent.duration.DurationInt

class ProfileServerHttp4sIntegrationTest
    extends AnyFlatSpec
    with BeforeAndAfter {

  implicit val runtime: IORuntime = cats.effect.unsafe.IORuntime.global
  lazy val quickstartConfig: QuickstartConfig =
    ConfigSource.default.load[QuickstartConfig] match {
      case Left(err) =>
        throw new Exception(
          s"Unable to load system configuration. Please check application.conf. Error: $err"
        )
      case Right(config) => config
    }

  // Set up connection to Couchbase. Note that this assumes Couchbase is already running locally!
  lazy val couchbaseConnection = new ProdCouchbaseConnection(quickstartConfig)

  lazy val profileController =
    new CouchbaseProfileController(couchbaseConnection, quickstartConfig)

  // Set up the indexes and collections. This can take a bit.
  val bucket: Bucket = Await.result(couchbaseConnection.bucket, 30.seconds)
  Await.result(profileController.setupIndexesAndCollections(), 30.seconds)

  val http4sServer = new ProfileServerHttp4s(
    new IOProfileController(profileController)
  )

  val http4sApp: HttpApp[IO] = http4sServer.routes.orNotFound
  val client: Client[IO] = Client.fromHttpApp(http4sApp)
  val collectionSpec: CollectionSpec = CollectionSpec(
    quickstartConfig.couchbase.collectionName,
    bucket.defaultScope.name
  )

  before {
    Await.result(profileController.setupIndexesAndCollections(), 30.seconds)
  }

  after {
    bucket.collections.dropCollection(collectionSpec)
  }

  "Getting a valid profile pid" should "successfully return a profile" in {
    val profileInput =
      ProfileInput("Jane", "Doe", "janedoe@example.com", "plaintextisbesttext")
    val postRequest: Request[IO] = Request(
      method = Method.POST,
      uri = uri"/api/v1/profile"
    ).withEntity(profileInput)
    val postResponse = client.expect[Json](postRequest)
    val profile = postResponse.unsafeRunSync().as[Profile].toOption.get
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

  "Getting an invalid profile" should "return a client error" in {
    val newId = UUID.randomUUID()
    val request: Request[IO] = Request(
      method = Method.GET,
      uri = uri"/api/v1/profile/".withQueryParam("id", newId.toString)
    )

    val getResponseStatus: Status =
      client.status(request).unsafeRunSync()

    assert(getResponseStatus === Status.BadRequest)
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

  "Putting a valid profile" should "succeed if the ID doesn't exist" in {
    val profileInput =
      ProfileInput("Azure", "Diamond", "az@example.com", "hunter2")
    val newId = UUID.randomUUID()
    val request: Request[IO] = Request(
      method = Method.PUT,
      uri = uri"/api/v1/profile".withQueryParam("id", newId.toString)
    ).withEntity(profileInput)

    val response = client.expect[Json](request)
    val profile = response.unsafeRunSync().as[Profile].toOption.get

    assert(profile.pid === newId)
    assert(profile.firstName === profileInput.firstName)
    assert(profile.lastName === profileInput.lastName)
    assert(profile.email === profileInput.email)
    assert(profile.saltedPassword !== profileInput.password)
  }

  it should "succeed if the ID does exist" in {
    // First add the Profile via POST
    val profileInput =
      ProfileInput("Azure", "Diamond", "az@example.com", "hunter2")
    val postRequest: Request[IO] = Request(
      method = Method.POST,
      uri = uri"/api/v1/profile"
    ).withEntity(profileInput)
    val postResponse = client.expect[Json](postRequest)
    val profile = postResponse.unsafeRunSync().as[Profile].toOption.get
    // Construct PUT with the existing ID
    val newProfileInput =
      ProfileInput("blood", "ninja", "wizard@example.com", "robeandhat")
    val request: Request[IO] = Request(
      method = Method.PUT,
      uri = uri"/api/v1/profile".withQueryParam("id", profile.pid.toString)
    ).withEntity(newProfileInput)

    val response = client.expect[Json](request)
    val newProfile = response.unsafeRunSync().as[Profile].toOption.get

    assert(newProfile.pid === profile.pid)
    assert(newProfile.firstName === newProfileInput.firstName)
    assert(newProfile.lastName === newProfileInput.lastName)
    assert(newProfile.email === newProfileInput.email)
    assert(newProfile.saltedPassword !== newProfileInput.password)
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

  "Getting a list of profiles" should "with limit and skip parameters should succeed" in {
    // Set up list of profile inputs to POST one by one.
    val exampleProfilesA: List[ProfileInput] = List(
      ProfileInput("AAA1", "AAA1", "email@domain.com", "password123"),
      ProfileInput("AAA2", "AAA2", "email@domain.com", "password123"),
      ProfileInput("AAA3", "AAA3", "email@domain.com", "password123"),
      ProfileInput("AAA4", "AAA4", "email@domain.com", "password123"),
      ProfileInput("AAA5", "AAA5", "email@domain.com", "password123"),
      ProfileInput("AAA6", "AAA6", "email@domain.com", "password123")
    )
    val exampleProfilesB: List[ProfileInput] = List(
      ProfileInput("BBB1", "BBB1", "email@domain.com", "password123"),
      ProfileInput("BBB2", "BBB2", "email@domain.com", "password123"),
      ProfileInput("BBB3", "BBB3", "email@domain.com", "password123")
    )
    // POST all profiles
    exampleProfilesA ++ exampleProfilesB foreach { profileInput =>
      val postRequest: Request[IO] = Request(
        method = Method.POST,
        uri = uri"/api/v1/profile"
      ).withEntity(profileInput)
      client.expect[Json](postRequest).unsafeRunSync()
    }
    val skip = 1
    val limit = 2
    val request: Request[IO] = Request(
      method = Method.GET,
      uri = uri"/api/v1/profile/profiles"
        .withQueryParam("search", "AA")
        .withQueryParam("skip", skip)
        .withQueryParam("limit", limit)
    )

    val response = client.expect[Json](request)
    val profiles = response.unsafeRunSync().as[List[Profile]].toOption.get

    assert(profiles.length === limit)
    assert(exampleProfilesA.map(_.firstName) contains profiles.head.firstName)
    assert(exampleProfilesA.map(_.firstName) contains profiles(1).firstName)
  }

  it should "succeed with no limit and skip parameters" in {
    // Set up list of profile inputs to POST one by one.
    val exampleProfilesA: List[ProfileInput] = List(
      ProfileInput("AAA1", "AAA1", "email@domain.com", "password123"),
      ProfileInput("AAA2", "AAA2", "email@domain.com", "password123"),
      ProfileInput("AAA3", "AAA3", "email@domain.com", "password123"),
      ProfileInput("AAA4", "AAA4", "email@domain.com", "password123"),
      ProfileInput("AAA5", "AAA5", "email@domain.com", "password123"),
      ProfileInput("AAA6", "AAA6", "email@domain.com", "password123")
    )
    val exampleProfilesB: List[ProfileInput] = List(
      ProfileInput("BBB1", "BBB1", "email@domain.com", "password123"),
      ProfileInput("BBB2", "BBB2", "email@domain.com", "password123"),
      ProfileInput("BBB3", "BBB3", "email@domain.com", "password123")
    )
    // POST all profiles
    exampleProfilesA ++ exampleProfilesB foreach { profileInput =>
      val postRequest: Request[IO] = Request(
        method = Method.POST,
        uri = uri"/api/v1/profile"
      ).withEntity(profileInput)
      client.expect[Json](postRequest).unsafeRunSync()
    }
    val request: Request[IO] = Request(
      method = Method.GET,
      uri = uri"/api/v1/profile/profiles"
        .withQueryParam("search", "AA")
    )

    val response = client.expect[Json](request)
    val profiles = response.unsafeRunSync().as[List[Profile]].toOption.get

    assert(profiles.length === 5) // limit default is 5
    assert(exampleProfilesA.map(_.firstName) contains profiles.head.firstName)
    assert(exampleProfilesA.map(_.firstName) contains profiles(1).firstName)
    assert(exampleProfilesA.map(_.firstName) contains profiles(2).firstName)
    assert(exampleProfilesA.map(_.firstName) contains profiles(3).firstName)
    assert(exampleProfilesA.map(_.firstName) contains profiles(4).firstName)
  }

  "Accessing the Swagger documentation at /docs" should "get redirected" in {
    val request: Request[IO] = Request(method = Method.GET, uri = uri"/docs")

    val responseStatus: Status =
      client.status(request).unsafeRunSync()

    assert(responseStatus === Status.PermanentRedirect)
  }
}
