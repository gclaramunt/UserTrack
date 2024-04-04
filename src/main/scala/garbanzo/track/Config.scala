package garbanzo.track

import com.zaxxer.hikari.HikariConfig
import pureconfig.ConfigSource
import pureconfig.generic.auto._

object Config {
  lazy val userTrackConfig: UserTrackConfig = ConfigSource
    .default
    .at("user-track-api")
    .loadOrThrow[UserTrackConfig]

  case class UserTrackConfig(db: DbConfig)

  case class DbConfig(driver: String, jdbcUrl: String, user: Option[String], password: Option[String], threads: Int)
  lazy val hikariConfig: HikariConfig = {
    val cfg = new HikariConfig()
    cfg.setDriverClassName(userTrackConfig.db.driver)
    cfg.setJdbcUrl(userTrackConfig.db.jdbcUrl)
    cfg.setUsername(userTrackConfig.db.user.getOrElse(""))
    cfg.setPassword(userTrackConfig.db.password.getOrElse(""))
    cfg
  }

}
