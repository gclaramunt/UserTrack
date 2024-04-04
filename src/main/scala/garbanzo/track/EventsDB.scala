package garbanzo.track

import cats.effect.MonadCancelThrow
import doobie._
import doobie.implicits._

class EventsDB[F[_]: MonadCancelThrow](xa: Transactor[F]) {

  def getEvents(start: Long, end: Long): fs2.Stream[F, UserEvent] = {
    sql"SELECT ts, user_id, event FROM user_events WHERE ts BETWEEN $start AND $end"
      .query[UserEvent]
      .stream
      .transact(xa)
  }


//  eventsStream.fold(Set[String](), 0, 0) {
//    case ((users, clicks, impressions), event) =>
//      event.event match {
//        case EventType.CLICK =>
//          (users + event.userId, clicks + 1, impressions)
//        case EventType.IMPRESSION =>
//          (users + event.userId, clicks, impressions + 1)
//      }
//  }
//
//  CREATE TABLE IF NOT EXISTS user_events (
//    ts   BIGINT,
//    user_id VARCHAR,
//    event VARCHAR
//  );

  def getGroupedEvents(start: Long, end: Long): fs2.Stream[F, UserEvent] = {
    sql"SELECT count distinct user_id, event FROM user_events WHERE ts BETWEEN $start AND $end"
      .query[UserEvent]
      .stream
      .transact(xa)
  }

  def addEvent(ev: UserEvent): F[Int] = {
    sql"insert into user_events (ts, user_id, event ) values (${ev.ts}, ${ev.userId}, ${ev.event})".update.run
      .transact(xa)
  }

}
