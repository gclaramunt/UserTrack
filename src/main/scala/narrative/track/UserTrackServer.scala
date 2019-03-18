package narrative.track

import cats.effect.{Async, Effect, IO}
import doobie.util.transactor.Transactor
import fs2.StreamApp
import org.http4s.server.blaze.BlazeBuilder

import scala.concurrent.ExecutionContext

object UserTrackServer extends StreamApp[IO] {
  import scala.concurrent.ExecutionContext.Implicits.global

  org.h2.tools.Server.createTcpServer().start()
  org.h2.tools.Server.createWebServer().start() //just for debugging

  def stream(args: List[String], requestShutdown: IO[Unit]) = ServerStream.stream[IO]
}

object ServerStream {

  def userTrack[F[_]: Effect: Async] = {
    val xa = Transactor.fromDriverManager[F](
      "org.h2.Driver", // driver classname
      "jdbc:h2:mem:user-track;DB_CLOSE_DELAY=-1;INIT=RUNSCRIPT FROM 'src/main/resources/createdb.sql'", // connect URL (driver-specific)
      "sa", // user
      "" // password
    )
    val eventsDB = new EventsDB[F](xa)
    new UserTrack[F](eventsDB)
  }

  def userTrackWebService[F[_]: Effect] = new UserTrackWebService[F](userTrack).service

  def stream[F[_]: Effect](implicit ec: ExecutionContext) = {
    BlazeBuilder[F]
      .bindHttp(8080, "0.0.0.0")
      .mountService(userTrackWebService, "/")
      .serve
  }
}
