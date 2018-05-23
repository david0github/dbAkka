package com.example

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.Logging

import scala.concurrent.duration._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.directives.MethodDirectives.delete
import akka.http.scaladsl.server.directives.MethodDirectives.get
import akka.http.scaladsl.server.directives.MethodDirectives.post
import akka.http.scaladsl.server.directives.RouteDirectives.complete
import akka.http.scaladsl.server.directives.PathDirectives.path

import scala.concurrent.Future
import com.example.ScheduleRegistryActor._
import akka.pattern.ask
import akka.util.Timeout

trait ScheduleRoutes extends JsonSupport {


  //leave these abstract, since they will be provided by the App
  implicit def system: ActorSystem

  lazy val log = Logging(system, classOf[ScheduleRoutes])

  //other dependencies that ScheduleRoutes use
  def scheduleRegistryActor: ActorRef

  //Required by the `ask` (?) method below
  implicit lazy val timeout = Timeout(5.seconds) // usually we'd obtain the timeout from the system's configuration

  //the endpoint to retrieving and creating schedules
  lazy val scheduleRoutes: Route =
    //used to match the incoming request
    pathPrefix("schedules") {
      concat(
        //used on an inner-level to discriminate from other alternatives, this case, match on the "schedules" path
        pathEnd {
          //concatenates two or more route alternatives If a route rejects a request, the next route in the chain is attempted.
          //This continues until a route in the chain produces a response
          concat(
            //matches against GET HTTP method
            get {
              val schedules: Future[Schedules] =
                (scheduleRegistryActor ? GetSchedules).mapTo[Schedules]
              //completes a request, creating and returning a response from the arguments.
              complete(schedules)
            },
            //matches against POST HTTP method.
            post {
              //converts the HTTP request body into a domain object of type Schedule
              entity(as[Schedule]) { schedule =>
                val scheduleCreated: Future[ActionPerformed] =
                  (scheduleRegistryActor ? CreateSchedule(schedule)).mapTo[ActionPerformed]
                onSuccess(scheduleCreated) { performed =>
                  log.info("Created schedule [{}]: {}", schedule.name, performed.description)
                  complete((StatusCodes.Created, performed))
                }
              }
            }
          )
        },
        //matches against URIs of the exact format and the Segment is automatically extracted into the schedule variable
        //so that we can get to the value passed
        path(Segment) { name =>
          concat(
            get {
              //retieving schedule info
              val maybeSchedule: Future[Option[Schedule]] =
                (scheduleRegistryActor ? GetSchedule(name)).mapTo[Option[Schedule]]
              //a convenience method that automatically unwraps a future, handles an Option by converting "schedule"
              //into a successful response
              rejectEmptyResponse {
                complete(maybeSchedule)
              }

            },
            delete {
              //delete schedule
              //send an instruction to removing a schedule to the schedule registry actor, wait for the response
              //and return an appropriate HTTP status code to the client
              val scheduleDeleted: Future[ActionPerformed] =
                (scheduleRegistryActor ? DeleteSchedule(name)).mapTo[ActionPerformed]
              onSuccess(scheduleDeleted) { performed =>
                log.info("Deleted schedule [{}]: {}", name, performed.description)
                complete((StatusCodes.OK, performed))
              }

            }
          )
        }
      )

    }

}
