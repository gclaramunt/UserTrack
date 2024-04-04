package garbanzo.track

import cats.effect.{IO, IOApp}
import org.http4s.ember.server.EmberServerBuilder
import com.comcast.ip4s._
import doobie.hikari.HikariTransactor.fromHikariConfig

import scala.concurrent.ExecutionContext

object UserTrackServer extends IOApp.Simple {

  //org.h2.tools.Server.createTcpServer().start()

  val run = (for {
    xa <- fromHikariConfig[IO](Config.hikariConfig)

    eventsDB = new EventsDB[IO](xa)
    userTrack = new UserTrack[IO](eventsDB)
    userTrackWebService = new UserTrackWebService[IO](userTrack).service

    srv <- EmberServerBuilder
      .default[IO]
      .withHost(ipv4"0.0.0.0")
      .withPort(port"8080")
      .withHttpApp(userTrackWebService)
      .build

  } yield srv).useForever.recover { err => println(s"ERR $err")}


}

