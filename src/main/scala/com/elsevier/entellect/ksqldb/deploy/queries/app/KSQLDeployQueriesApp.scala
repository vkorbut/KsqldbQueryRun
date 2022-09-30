package com.elsevier.entellect.ksqldb.deploy.queries.app

import cats.data._
import cats.effect._
import cats.implicits._
import com.elsevier.entellect.ksqldb.deploy.queries.config.ConfigDataTypes.AppConfig
import com.elsevier.entellect.ksqldb.deploy.queries.config.{ConfigModule, ConfigModuleProd, ResourceModule, ResourceModuleProd}
import com.elsevier.entellect.ksqldb.deploy.queries.ksqldb.{KsqlDbModule, KsqlDbModuleProd}
import scribe.Scribe

object KSQLDeployQueries {
  val logger: Scribe[IO] = scribe.cats[IO]

  def askIO[T]: Kleisli[IO, T, T] = Kleisli.ask[IO, T]

  def liftIO[R](io: IO[R]): Kleisli[IO, Any, R] = Kleisli.liftF[IO, Any, R](io)

  def programLogic: ReaderT[IO, KsqlDbModule with ResourceModule with ConfigModule[AppConfig], Unit] = {

    def loadConfig: Kleisli[IO, ConfigModule[AppConfig], AppConfig] = for {
      sourceConfig <- askIO[ConfigModule[AppConfig]].flatMap { env => liftIO(env.configService.loadConfig) }

      _ <- liftIO(logger.info(s"final configuration is \n$sourceConfig"))

    } yield sourceConfig

    for {
      config          <- loadConfig
      resourceService <- askIO[ResourceModule].map(_.createResourceService)
      ksqlDbService   <- askIO[KsqlDbModule].map(_.createKsqlDbService)

      _ <- liftIO(resourceService.createHttpClient(config).use { client =>
        for {
          time <- Clock[IO].timed {
            config.groups traverse (ksqlDbService.runQueryGroup(config, client, _))
          }.map(_._1)
          _ <- logger.info(s"Processing was finished successfully\n Processing time: $time")
        } yield ()
      })

    } yield {}


  }

}

object KSQLDeployQueriesApp extends App {

  import KSQLDeployQueries._
  import cats.effect.unsafe.implicits.global

  trait Env extends ConfigModuleProd with KsqlDbModuleProd with ResourceModuleProd

  def runApp[E <: Env](env: E): Unit = programLogic.run(env).unsafeRunSync()

  runApp(new Env {})
}
