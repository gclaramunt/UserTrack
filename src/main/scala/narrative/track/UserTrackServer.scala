package narrative.track

import cats.effect.{Async, IO, IOApp}
import doobie.util.transactor.Transactor
import org.http4s.ember.server.EmberServerBuilder
import com.comcast.ip4s._
import cats.implicits._
import doobie._
import doobie.implicits._
import fs2.io.net.Network

import scala.concurrent.ExecutionContext

object UserTrackServer extends IOApp.Simple {

  org.h2.tools.Server.createTcpServer().start()

  val run = ServerStream.stream[IO]
}

object ServerStream {

  def userTrack[F[_]: Async] = {
    val xa = Transactor.fromDriverManager[F](
      "org.h2.Driver", // driver classname
      "jdbc:h2:mem:user-track;DB_CLOSE_DELAY=-1;INIT=RUNSCRIPT FROM 'src/main/resources/createdb.sql'",
      "sa", // user
      "", // password
      None
    )
    val eventsDB = new EventsDB[F](xa)
    new UserTrack[F](eventsDB)
  }

  def userTrackWebService[F[_]: Async] =
    new UserTrackWebService[F](userTrack).service

  def stream[F[_]: Async: Network] = {
    EmberServerBuilder
      .default[F]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(userTrackWebService)
      .build
      .useForever

  }

}
