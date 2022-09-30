package com.elsevier.entellect.ksqldb.deploy.queries.ksqldb

import io.circe.Encoder
import io.circe.generic.semiauto.deriveEncoder

object KsqlDbDataTypes {
  case class ReqBody(ksql: String)

  case class ResBody(error_code: String, statmentText: String, message: String, entities: List[String])

  implicit val reqBodyEncoder: Encoder[ReqBody] = deriveEncoder[ReqBody]

}
