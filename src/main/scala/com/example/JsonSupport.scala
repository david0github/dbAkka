package com.example

import com.example.ScheduleRegistryActor.ActionPerformed
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.DefaultJsonProtocol
// using Spray JSON library, allows us to define json marshallers or farmats in a type safe way
trait JsonSupport extends SprayJsonSupport {
  // import the default encoders for primitive types (Int, String, Lists etc)
  import DefaultJsonProtocol._

  implicit val scheduleJsonFormat = jsonFormat4(Schedule)
  implicit val schedulesJsonFormat = jsonFormat1(Schedules)
  implicit val actionPerformedJsonFormat = jsonFormat1(ActionPerformed)
}
