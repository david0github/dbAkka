package com.example

import scala.concurrent.Await
import scala.concurrent.duration.Duration

import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer

//main class, is runnable because it extends app
object QuickstartServer extends App with ScheduleRoutes {

  // set up ActorSystem, ActorMaterializer, with other dependencies here
  // provides a context in which actors will run, and the actor system defined in val will be picked
  // up and used by streams
  implicit val system: ActorSystem = ActorSystem("helloAkkaHttpServer")
  // interprets stream description into executable entites which are run on actors, this requires an ActorSystem
  implicit val materializer: ActorMaterializer = ActorMaterializer()


  val scheduleRegistryActor: ActorRef = system.actorOf(ScheduleRegistryActor.props, "scheduleRegistryActor")

  // ScheduleRoutes trait
  // separate out the ScheduleRoutes trait, in which we put all our route definitions
  lazy val routes: Route = scheduleRoutes

  // takes three parameters; routes, hostname, and port
  Http().bindAndHandle(routes, "localhost", 8080)

  // once run, it starts an Akka HTTP server on localhost port 8080
  println(s"Connect server online at http://localhost:8080/")

  Await.result(system.whenTerminated, Duration.Inf)

}
