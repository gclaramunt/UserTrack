package garbanzo.track

import cats.effect.IO
import org.http4s._
import org.http4s.implicits._
import cats.effect.unsafe.implicits.global
import org.http4s.LiteralSyntaxMacros.uri

class UserTrackIntegrationSpec extends org.specs2.mutable.Specification {

  val mockEventsDB = new EventsDB[IO](null) {

    var lastStored: UserEvent = _

    override def getEvents(
        start: Long,
        end: Long
    ): fs2.Stream[IO, UserEvent] = {
      fs2.Stream
        .emits(
          List(
            new UserEvent(1L, "user1", EventType.CLICK),
            new UserEvent(2L, "user2", EventType.CLICK),
            new UserEvent(10L, "user1", EventType.IMPRESSION),
            new UserEvent(60 * 60L + 3, "user1", EventType.IMPRESSION)
          )
        )
        .filter(ue => start <= ue.ts && ue.ts <= end)
    }

    override def addEvent(ev: UserEvent): IO[Int] =
      IO {
        lastStored = ev
        1
      }
  }

  val userTrack = new UserTrack[IO](mockEventsDB)

  "Post user event" >> {
    "post a single event" >> {
      postEvent().status must beEqualTo(Status.NoContent)
      mockEventsDB.lastStored must beEqualTo(
        UserEvent(1L, "u2", EventType.CLICK)
      )
    }
  }

  "Get aggregated user events" >> {
    "get the events of the first hour" >> {
      getEvent(1).as[String].unsafeRunSync() must beEqualTo(
        "unique_users,2\nclicks,2\nimpressions,1"
      )
    }
    "get the events of the second hour" >> {
      getEvent(60 * 60 + 1).as[String].unsafeRunSync() must beEqualTo(
        "unique_users,1\nclicks,0\nimpressions,1"
      )
    }
  }

  private[this] def postEvent(): Response[IO] = {
    val getHW =
      Request[IO](Method.POST, uri"/analytics?timestamp=1&user=u2&event=click")
    new UserTrackWebService[IO](userTrack).service(getHW).unsafeRunSync()
  }

  private[this] def getEvent(ts: Int): Response[IO] = {
    val getHW =
      Request[IO](Method.GET, Uri.unsafeFromString(s"/analytics?timestamp=$ts"))
    new UserTrackWebService[IO](userTrack).service(getHW).unsafeRunSync()
  }

}
