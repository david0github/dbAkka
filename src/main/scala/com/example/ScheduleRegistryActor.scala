package com.example

import akka.actor.{ Actor, ActorLogging, Props }
//keeps registered schedules in a Set, once it recieves messages it matches them to they
//defined cases to determine which action to take.
// case classes.
final case class Schedule(name: String, studentId: Int, semester: String, courses: String)
final case class Schedules(schedules: Seq[Schedule])

object ScheduleRegistryActor {
  final case class ActionPerformed(description: String)
  final case object GetSchedules
  final case class CreateSchedule(schedule: Schedule)
  final case class GetSchedule(name: String)
  final case class DeleteSchedule(name: String)

  def props: Props = Props[ScheduleRegistryActor]
}

class ScheduleRegistryActor extends Actor with ActorLogging {
  import ScheduleRegistryActor._

  var schedules = Set.empty[Schedule]

  def receive: Receive = {
    case GetSchedules =>
      sender() ! Schedules(schedules.toSeq)
    case CreateSchedule(schedule) =>
      schedules += schedule
      sender() ! ActionPerformed(s"Schedule for ${schedule.name} created.")
    case GetSchedule(name) =>
      sender() ! schedules.find(_.name == name)
    case DeleteSchedule(name) =>
      schedules.find(_.name == name) foreach { schedule => schedules -= schedule }
      sender() ! ActionPerformed(s"Schedule for ${name} deleted.")
  }
}
