package narrative.track

import cats.effect.{Effect, IO}
import cats.implicits._
import doobie._
import doobie.implicits._

import scala.concurrent.ExecutionContext


class EventsDB[F[_]:Effect](xa: Transactor[F]) {

  // We need a ContextShift[IO] before we can construct a Transactor[IO]. The passed ExecutionContext
  // is where nonblocking operations will be executed.
  //implicit val cs = IO//.contextShift(ExecutionContext.global)
  //implicit val cs = IO//.contextShift(ExecutionContext.global)

  // A transactor that gets connections from java.sql.DriverManager and excutes blocking operations
  // on an unbounded pool of daemon threads. See the chapter on connection handling for more info.

  //val xa =  H2Transactor.newH2Transactor[IO]("jdbc:h2:mem:user-track;DB_CLOSE_DELAY=-1", "sa", "")



  def getEvents(start: Long, end: Long): fs2.Stream[F, UserEvent] = {
    println(s"SELECT ts, user_id, event FROM user_events WHERE ts BETWEEN $start AND $end")
    sql"SELECT ts, user_id, event FROM user_events WHERE ts BETWEEN $start AND $end"
      .query[UserEvent]
      .stream
      .transact(xa)
  }

  def addEvent(ev: UserEvent): F[Int] = {
    println(s"insert into user_events (ts, user_id, event ) values (${ev.ts}, ${ev.userId}, ${ev.event})")
    sql"insert into user_events (ts, user_id, event ) values (${ev.ts}, ${ev.userId}, ${ev.event})"
      .update.run.transact(xa)
  }

}
