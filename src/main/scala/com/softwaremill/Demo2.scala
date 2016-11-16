package com.softwaremill

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.softwaremill.session.{SessionConfig, SessionManager}

import scala.io.StdIn

object Demo2 extends App {

  implicit val system = ActorSystem("example")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  val sessionConfig = SessionConfig.default("verylongstringverylongstringverylongstringverylongstringverylongstring")
  implicit val sessionManager = new SessionManager[String](sessionConfig)

  val routes =
    path("") {
      redirect("/site/index.html", Found)
    }

  val bindingFuture = Http().bindAndHandle(routes, "localhost", 8080)

  println("Server started, press enter to stop. Visit http://localhost:8080 to see the demo.")
  StdIn.readLine()

  import system.dispatcher
  bindingFuture
    .flatMap(_.unbind())
    .onComplete { _ =>
      system.terminate()
      println("Server stopped")
    }
}
