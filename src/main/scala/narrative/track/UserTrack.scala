package narrative.track

import java.time.Instant
import java.time.temporal.{ChronoUnit, TemporalUnit}

import cats.effect.Effect
import doobie.util.transactor.Transactor

sealed trait Event
object Click extends Event
object Impression extends Event

case class UserEvent(ts: Long, userId: String, event: String)

class UserTrack[F[_]:Effect](eventsDB: EventsDB[F]) {


  def storeEvent(ts: Long, userId: String, event: String) = {
    eventsDB.addEvent(UserEvent(ts, userId, event)) //we actually can get rid of UserEvent and trade clarity for memory
  }


  def aggregateEvents(ts: Long) = {
    val instant = Instant.ofEpochMilli(ts)
    val start = instant.truncatedTo(ChronoUnit.HOURS)
    val end = start.plus(1, ChronoUnit.HOURS)
    val eventsStream = eventsDB.getEvents(start.toEpochMilli, end.toEpochMilli)
    eventsStream.fold(Set[String](), 0, 0) {
      case ((users, clicks, impressions), event) => event.event match {
        case "click" => (users + event.userId, clicks +1, impressions)
        case "impression" => (users + event.userId, clicks, impressions +1)
      }
    }
  }


}
