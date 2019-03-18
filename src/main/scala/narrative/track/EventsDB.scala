package narrative.track

import cats.effect.Effect
import doobie._
import doobie.implicits._


class EventsDB[F[_]:Effect](xa: Transactor[F]) {


  def getEvents(start: Long, end: Long): fs2.Stream[F, UserEvent] = {
    sql"SELECT ts, user_id, event FROM user_events WHERE ts BETWEEN $start AND $end"
      .query[UserEvent]
      .stream
      .transact(xa)
  }

  def addEvent(ev: UserEvent): F[Int] = {
    sql"insert into user_events (ts, user_id, event ) values (${ev.ts}, ${ev.userId}, ${ev.event})"
      .update.run.transact(xa)
  }

}
