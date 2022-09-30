package com.elsevier.entellect.ksqldb.deploy.queries.config

object ConfigDataTypes {

  case class KSQLQueryGroup(queries: Seq[String])

  sealed abstract class KSQLQueriesError extends Throwable {
    val msg: String
  }

  case class FailedToReadQueries(msg: String) extends KSQLQueriesError

  case class AppConfig(ksqlDbHost: String, groups: Seq[KSQLQueryGroup], runGroupsInParallel:Boolean = true)
}
