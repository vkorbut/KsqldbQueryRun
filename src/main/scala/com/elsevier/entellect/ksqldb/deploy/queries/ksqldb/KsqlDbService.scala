package com.elsevier.entellect.ksqldb.deploy.queries.ksqldb

import cats.effect._
import cats.implicits._
import com.elsevier.entellect.ksqldb.deploy.queries.config.ConfigDataTypes.{AppConfig, KSQLQueryGroup}
import com.elsevier.entellect.ksqldb.deploy.queries.ksqldb.KsqlDbDataTypes._
import io.circe.syntax.EncoderOps
import org.http4s.circe._
import org.http4s.client.Client
import org.http4s.{Method, Request, Status, Uri}
import scribe.cats.io._

trait KsqlDbModule {
  def createKsqlDbService: KsqlDbService
}

trait KsqlDbService {
  def runQuery(config: AppConfig, client: Client[IO], query: String): IO[Unit]

  def runQueryGroup(config: AppConfig, client: Client[IO], group: KSQLQueryGroup): IO[Unit]
}

trait KsqlDbModuleProd extends KsqlDbModule {
  def createKsqlDbService: KsqlDbService = new KsqlDbServiceProd {}
}

trait KsqlDbServiceProd extends KsqlDbService {
  def runQuery(config: AppConfig, client: Client[IO], q: String): IO[Unit] = {
    for {
      postRequest <- buildRequest(config, q)
      _ <- info(s"sending POST ${postRequest.uri} with body: \n " + ReqBody(q).asJson.toString())
      _ <- client.run(postRequest).use {
        case Status.Successful(r) =>
          r.as[String]
            .onError(e => error(e))
            .flatTap(body => info(body))
        case r =>
          r.as[String]
            .flatMap { errorBody =>
              val message = s"Request $postRequest failed with status ${r.status.code} and body $errorBody"
              error(message) *>
                IO.raiseError(new Exception(message))
            }
      }
    } yield ()
  }

  def runQueryGroup(config: AppConfig, client: Client[IO], group: KSQLQueryGroup): IO[Unit] = {
    val action = if (config.runGroupsInParallel) group.queries.parTraverse[IO, Unit](_)
    else group.queries.traverse[IO, Unit](_)

    action(runQuery(config, client, _)).as(())
  }

  private def buildRequest(config: AppConfig, query: String): IO[Request[IO]#Self] = {
    IO.fromEither(Uri.fromString(config.ksqlDbHost))
      .map(Request[IO](method = Method.POST, _))
      .map(_.withEntity(ReqBody(query).asJson))
  }
}
