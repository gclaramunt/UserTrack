package narrative.track

import java.time.Instant
import java.time.temporal.ChronoUnit

import cats.effect.Effect

case class UserEvent(ts: Long, userId: String, event: String)
object EventType {
  val CLICK = "click"
  val IMPRESSION = "impression"
}

class UserTrack[F[_]:Effect](eventsDB: EventsDB[F]) {


  def storeEvent(ts: Long, userId: String, event: String) = {
    eventsDB.addEvent(UserEvent(ts, userId, event)) //we actually can get rid of UserEvent and trade clarity for memory
  }

  def aggregateEvents(ts: Long) = {
    val instant = Instant.ofEpochSecond(ts)
    val start = instant.truncatedTo(ChronoUnit.HOURS)
    val end = start.plus(1, ChronoUnit.HOURS)
    val eventsStream = eventsDB.getEvents(start.toEpochMilli/1000, end.toEpochMilli/1000-1)
    eventsStream.fold(Set[String](), 0, 0) {
      case ((users, clicks, impressions), event) => event.event match {
        case EventType.CLICK => (users + event.userId, clicks +1, impressions)
        case EventType.IMPRESSION => (users + event.userId, clicks, impressions +1)
      }
    }
  }


}
