package narrative.track

import cats.Monad
import cats.effect.Effect
import doobie.util.transactor.Transactor
import org.http4s.HttpService
import org.http4s.dsl.Http4sDsl

class UserTrackWebService[F[_]: Effect](xa: Transactor[F]) extends Http4sDsl[F] {

  object TimestampQueryParamMatcher extends QueryParamDecoderMatcher[Long]("timestamp")
  object UserQueryParamMatcher extends QueryParamDecoderMatcher[String]("user")
  object EventQueryParamMatcher extends QueryParamDecoderMatcher[String]("event")

  val userTrack = new UserTrack[F](xa)
  val service: HttpService[F] = {
    HttpService[F] {
      case GET -> Root / "analytics" :? TimestampQueryParamMatcher(ts)  => {
        val responseStream: fs2.Stream[F,String] = userTrack.aggregateEvents(ts).map { case (users, clicks, impressions) =>
          s"unique_users,${users.size}\nclicks,${clicks}\nimpressions,${impressions}"}
        Ok{ responseStream }
      }
      case POST -> Root /  "analytics"  :? TimestampQueryParamMatcher(ts) +& UserQueryParamMatcher(userId) +& EventQueryParamMatcher(event) => //we can even validate event type
        val m = implicitly[Monad[F]]
        m.flatMap(userTrack.storeEvent(ts, userId, event) )( _ => NoContent() )
    }
  }
}

/*
As a part of integrating with our partners, Narrative supports collecting data on website visitors and returning some basic analytics on those visitors.
The goal of this task is to implement a basic endpoint for this use case. It should accept the following over HTTP:

POST /analytics?timestamp={millis_since_epoch}&user={user_id}&event={click|impression}
GET /analytics?timestamp={millis_since_epoch}

When the POST request is made, a 204 is returned to the client with an empty response. We simply side-effect and track the event in our data store.

When the GET request is made, we return information in the following format to the client, for the hour (assuming GMT time zone) of the requested timestamp:

unique_users,{number_of_unique_usernames}
clicks,{number_of_clicks}
impressions,{number_of_impressions}

It is worth noting that the traffic pattern is typical of time series data. The service will receive many more GET requests (~95%) for the current hour than for past hours (~5%). The same applies for POST requests.

Please ensure that the code in the submission is fully functional on a local machine, and include instructions for building and running it. It is fine for the code to not be completely production ready in the submission: for example, using local storage like in-memory H2 instead of dedicated MySQL is OK. As a guide for design decisions, treat this exercise as the initial prototype of an MVP that will need to be productionalized and scaled out in the future, and be prepared for follow-up discussion on how that would look.
 */