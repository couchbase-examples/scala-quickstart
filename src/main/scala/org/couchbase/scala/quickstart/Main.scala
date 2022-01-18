package org.couchbase.scala.quickstart

import org.couchbase.scala.quickstart.servers.{ProfileServerAkkaHttp, ProfileServerHttp4s, ProfileServerPlay}

import scala.io.StdIn

object Main {
  def main(args: Array[String]): Unit = {
    println("Hello, World!")
    val akkaServer = ProfileServerAkkaHttp.startAkkaHttpServer()
    println("Akka running, see http://localhost:8081/docs for the Swagger UI")
    val http4sFiber = ProfileServerHttp4s.startServer()
    println("Http4s server running, see http://localhost:8082/docs")
    val playServer = ProfileServerPlay.startServer()
    println("Play server running, see http://localhost:8083/docs")

    StdIn.readLine()
    ProfileServerAkkaHttp.stopAkkaHttpServer(akkaServer)
    ProfileServerHttp4s.stopServer(http4sFiber)
    ProfileServerPlay.stopServer(playServer)

  }
}
