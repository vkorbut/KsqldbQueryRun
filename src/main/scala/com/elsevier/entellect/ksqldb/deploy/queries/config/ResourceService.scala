package com.elsevier.entellect.ksqldb.deploy.queries.config

import cats.effect.{IO, Resource}
import com.elsevier.entellect.ksqldb.deploy.queries.config.ConfigDataTypes.AppConfig
import org.http4s.client.Client
import org.http4s.ember.client.EmberClientBuilder

import scala.concurrent.duration.DurationInt

trait ResourceModule {
  def createResourceService: ResourceService
}

trait ResourceService {
  def createHttpClient(config: AppConfig): Resource[IO, Client[IO]]
}

trait ResourceModuleProd extends ResourceModule {
  def createResourceService: ResourceService = new ResourceServiceProd {}
}

trait ResourceServiceProd extends ResourceService {
  override def createHttpClient(config: AppConfig): Resource[IO, Client[IO]] = {
    EmberClientBuilder.default[IO].withTimeout(1.minute).build
  }
}
