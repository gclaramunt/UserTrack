package narrative.track

import cats.effect.{Effect, IO}
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

  def userTrackWebService[F[_]: Effect](xa: Transactor[F]) = new UserTrackWebService[F](xa).service

  def stream[F[_]: Effect](implicit ec: ExecutionContext) = {
    println(s"building xa")
    val xa = Transactor.fromDriverManager[F](
      "org.h2.Driver", // driver classname
      "jdbc:h2:mem:user-track;DB_CLOSE_DELAY=-1;INIT=RUNSCRIPT FROM 'src/main/resources/createdb.sql'", // connect URL (driver-specific)
      "sa", // user
      "" // password
    )
    BlazeBuilder[F]
      .bindHttp(8080, "0.0.0.0")
      .mountService(userTrackWebService(xa), "/")
      .serve
  }
}
