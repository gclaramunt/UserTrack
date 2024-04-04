package garbanzo.track

import java.time.Instant
import java.time.temporal.ChronoUnit

import cats.effect.MonadCancelThrow

case class UserEvent(ts: Long, userId: String, event: String)
object EventType {
  val CLICK = "click"
  val IMPRESSION = "impression"
}

class UserTrack[F[_]: MonadCancelThrow](eventsDB: EventsDB[F]) {

  def storeEvent(ts: Long, userId: String, event: String): F[Int] = {
    eventsDB.addEvent(
      UserEvent(ts, userId, event)
    ) // we actually can get rid of UserEvent and trade clarity for memory
  }

  def aggregateEvents(ts: Long): fs2.Stream[F, (Set[String], Int, Int)] = {
    val instant = Instant.ofEpochSecond(ts)
    val start = instant.truncatedTo(ChronoUnit.HOURS)
    val end = start.plus(1, ChronoUnit.HOURS)
    val eventsStream =
      eventsDB.getEvents(start.toEpochMilli / 1000, end.toEpochMilli / 1000 - 1)
    eventsStream.fold(Set[String](), 0, 0) {
      case ((users, clicks, impressions), event) =>
        event.event match {
          case EventType.CLICK =>
            (users + event.userId, clicks + 1, impressions)
          case EventType.IMPRESSION =>
            (users + event.userId, clicks, impressions + 1)
        }
    }
  }

}
