package com.softwaremill.done

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import com.softwaremill.session.SessionDirectives._
import com.softwaremill.session.SessionOptions._
import com.softwaremill.session._

import scala.io.StdIn
import scala.util.Try

object Demo2 extends App {
  implicit val system = ActorSystem("example")
  implicit val materializer = ActorMaterializer()
  import system.dispatcher

  val sessionConfig = SessionConfig.default("verylongstringverylongstringverylongstringverylongstringverylongstring")
  implicit val sessionManager = new SessionManager[ExampleSession](sessionConfig)

  val routes =
    path("") {
      redirect("/site/index.html", Found)
    } ~
      pathPrefix("api") {
        path("do_login") {
          post {
            entity(as[String]) { body =>
              println(s"Logging in $body")

              setSession(oneOff, usingCookies, ExampleSession(body)) {
                complete("ok")
              }
            }
          }
        } ~
          // This should be protected and accessible only when logged in
          path("do_logout") {
            post {
              requiredSession(oneOff, usingCookies) { session =>
                invalidateSession(oneOff, usingCookies) { ctx =>
                  println(s"Logging out $session")
                  ctx.complete("ok")
                }
              }
            }
          } ~
          // This should be protected and accessible only when logged in
          path("current_login") {
            get {
              requiredSession(oneOff, usingCookies) { session => ctx =>
                println("Current session: " + session)
                ctx.complete(session.username)
              }
            }
          }
      } ~
      pathPrefix("site") {
        getFromResourceDirectory("")
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

case class ExampleSession(username: String)

object ExampleSession {
  implicit def serializer: SessionSerializer[ExampleSession, String] = new SingleValueSessionSerializer(
    _.username,
    (un: String) => Try { ExampleSession(un) }
  )
}
