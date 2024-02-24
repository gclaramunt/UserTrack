package narrative.track

import cats.Monad
import cats.effect.kernel.MonadCancelThrow
import org.http4s.{HttpApp, HttpRoutes}
import org.http4s.dsl.Http4sDsl

class UserTrackWebService[F[_]: MonadCancelThrow](userTrack: UserTrack[F]) extends Http4sDsl[F] {

  object TimestampQueryParamMatcher extends QueryParamDecoderMatcher[Long]("timestamp")
  object UserQueryParamMatcher extends QueryParamDecoderMatcher[String]("user")
  object EventQueryParamMatcher extends QueryParamDecoderMatcher[String]("event")

  val service: HttpApp[F] = {
    HttpRoutes.of[F] {
      case GET -> Root / "analytics" :? TimestampQueryParamMatcher(ts)  => {
        val responseStream: fs2.Stream[F,String] = userTrack.aggregateEvents(ts).map { case (users, clicks, impressions) =>
          s"unique_users,${users.size}\nclicks,${clicks}\nimpressions,${impressions}"}
        Ok{ responseStream }
      }
      case POST -> Root /  "analytics"  :? TimestampQueryParamMatcher(ts) +& UserQueryParamMatcher(userId) +& EventQueryParamMatcher(event) =>
        val m = implicitly[Monad[F]]
        m.flatMap(userTrack.storeEvent(ts, userId, event) )( _ => NoContent() )
    }
  }.orNotFound
}
