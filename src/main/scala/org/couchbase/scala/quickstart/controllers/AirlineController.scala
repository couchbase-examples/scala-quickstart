package org.couchbase.scala.quickstart.controllers

import org.couchbase.scala.quickstart.models.{Airline, AirlineInput, AirlineListingInput, PutAirlineInput}

import java.util.UUID

trait AirlineController[F[_]] {

  def getAirline(pid: UUID): F[Either[String, Airline]]

  def postAirline(airlineInput: AirlineInput): F[Either[String, Airline]]

  def putAirline(args: PutAirlineInput): F[Either[String, Airline]]

  def deleteAirline(id: UUID): F[Either[String, UUID]]

  def airlineListing(args: AirlineListingInput): F[Either[String, List[Airline]]]

}
