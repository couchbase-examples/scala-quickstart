package org.couchbase.scala.quickstart

import org.couchbase.scala.quickstart.servers.ProfileServerAkkaHttp

import scala.io.StdIn

object Main {
  def main(args: Array[String]): Unit = {
    println("Hello, World!")
    ProfileServerAkkaHttp.startAkkaHttpServer()

    println("Akka running, see http://localhost:8081/docs for the Swagger UI")
    StdIn.readLine()
    ProfileServerAkkaHttp.stopAkkaHttpServer()


  }
}
