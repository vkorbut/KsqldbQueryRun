package com.elsevier.entellect.ksqldb.deploy.queries.config

import cats.effect.IO
import cats.implicits._
import com.elsevier.entellect.ksqldb.deploy.queries.config.ConfigDataTypes.{AppConfig, FailedToReadQueries, KSQLQueriesError, KSQLQueryGroup}

import java.io.{File, FileInputStream}
import scala.io.Source

/**
 *
 * Config Modules & Services interfaces
 *
 */

trait ConfigModule[AppConfig] {
  def configService: ConfigService[AppConfig]
}

trait ConfigService[AppConfig] {
  def loadConfig: IO[AppConfig]
}

trait ConfigModuleProd extends ConfigModule[AppConfig] {
  def configService: ConfigService[AppConfig] = new ConfigServiceProd {}
}

trait ConfigServiceProd extends ConfigService[AppConfig] {

  def getQueries(file: File): Either[KSQLQueriesError, Seq[KSQLQueryGroup]] =
    for {
      queries <-  getQueriesFromFile(file)
      _    = scribe.debug(s"Raw queries: $queries")
      q <- parseQueries(queries)
    } yield q

  private def getQueriesFromFile(file: File): Either[KSQLQueriesError, List[String]]  = {
    scribe.info(s"Trying to read KSQL queries from ${file.getAbsolutePath}")

    Either.catchNonFatal(
      Source.fromInputStream(new FileInputStream(file)).getLines().toList
    ).leftMap( e => FailedToReadQueries(s"Failed to read from $file with ${e.getMessage}"))
  }

  def parseQueries(lines: List[String]): Either[KSQLQueriesError, Seq[KSQLQueryGroup]] = {

    val queries = {
      lines.foldLeft(List(List.empty[String])){
        case (l, query) if query.trim == "#" => List.empty[String] :: l
        case (h ::t, query) => (query:: h) :: t
        case (l, _) => l
      }.reverse.map(_.reverse)
    }

    Either.catchNonFatal(
      queries.map(KSQLQueryGroup(_))
    ).leftMap( e => FailedToReadQueries(s"Failed to read from ${lines.mkString("\n")} with ${e.getMessage}"))
  }

  override def loadConfig: IO[AppConfig] = {
    (
      ciris.env("KSQL_DB_HOST").as[String],
      ciris.env("KSQL_DB_QUERIES").as[String].evalMap(fileName => IO.fromEither(getQueries(new File(fileName)))),
      ciris.env("PARALLEL_RUN").as[Boolean].default(true)
    ).parMapN(AppConfig).load[IO]
  }
}


