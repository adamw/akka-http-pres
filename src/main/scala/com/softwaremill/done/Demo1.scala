package com.softwaremill.done

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{Directive0, Directive1, Route}

import scala.io.StdIn

object Demo1 extends App {
  implicit val system = ActorSystem("example")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  /* step2 */ val myGetParameter = get & parameter("x")
  /* step3 */ val myGetMappedParameter = myGetParameter.map(v => v + " " + v)
  /* step4 */ val myFlatMappedParameter = parameter("where").flatMap {
    case "left" => parameter("l")
    case "right" => parameter("r")
    case _ => provide("unknown")
  }

  val routes = {
    path("demo1") { /* step1 */
      get {
        parameter("x") { x =>
          headerValueByName("Cookie") { cookieHeader =>
            complete {
              s"The value of x is: $x\nThe cookie header is: $cookieHeader"
            }
          }
        }
      }
    } ~
      path("demo2") {
        myGetParameter { x =>
          complete {
            s"The value of x is: $x"
          }
        }
      } ~
      path("demo3") {
        myGetMappedParameter { x =>
          complete {
            s"The value of x is: $x"
          }
        }
      } ~
      path("demo4") {
        myFlatMappedParameter { v =>
          complete {
            s"The value is: $v"
          }
        }
      }
  }

  // Testing types
  routes: Route
  get: Directive0
  headerValueByName("Cookie"): Directive1[String]

  // Server
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
