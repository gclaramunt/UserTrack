package narrative.track

import cats.effect.IO
import org.http4s._
import org.http4s.implicits._
import org.specs2.matcher.MatchResult

class UserTrackWebServiceSpec extends org.specs2.mutable.Specification {

  val mockEventsDB = new EventsDB[IO](null) {

    override def getEvents(start: Long, end: Long): fs2.Stream[IO, UserEvent] =
      fs2.Stream.emits(
        List(
          new UserEvent(1L, "user1", "click"),
          new UserEvent(2L, "user2", "click"),
          new UserEvent(10L, "user1", "impression"),
          new UserEvent(60*60L+1, "user1", "impression")
        )
      )

    override def addEvent(ev: UserEvent): IO[Int] = IO { 1 }
  }

  val userTrack = new UserTrack[IO](mockEventsDB)

  "Post user event" >> {
    "post a single event" >> {
      postEvent().status must beEqualTo(Status.NoContent)
    }
  }

  "Get aggregated user events" >> {
    "get the events of the first hour" >> {
      getEvent(1).as[String].unsafeRunSync() must beEqualTo("'unique_users,2\nclicks,2\nimpressions,2")

    }
  }

  private[this] def postEvent(): Response[IO] = {
    val getHW = Request[IO](Method.POST, Uri.uri("/analytics?timestamp=1&user=u2&event=click"))
    new UserTrackWebService[IO](userTrack).service.orNotFound(getHW).unsafeRunSync()
  }

  private[this] def getEvent(ts: Int): Response[IO] = {
    val getHW = Request[IO](Method.GET, Uri.unsafeFromString(s"/analytics?timestamp=$ts"))
    new UserTrackWebService[IO](userTrack).service.orNotFound(getHW).unsafeRunSync()
  }

}
