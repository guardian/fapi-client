package com.gu.facia.api.facia

import com.gu.facia.api.{ApiError, Front, Json, Response}
import com.gu.facia.api.Response.Async.Right
import com.gu.facia.api.config.FaciaConfig
import com.gu.facia.api.http.DispatchHandlers
import org.json4s.JsonAST.{JObject, JValue}

import scala.concurrent.ExecutionContext

object Facia {
  implicit val formats = org.json4s.DefaultFormats

  def getFrontsConfig(implicit dispatchClient: dispatch.Http, config: FaciaConfig, ec: ExecutionContext): Response[JValue] = {
    val req = dispatch.url(s"${config.root}/config/config.json")
    for {
      rawResponse <- Right(dispatchClient(req.toRequest, DispatchHandlers.asHttpResponse))
      json <- Json.toJson(rawResponse.body)
    } yield json
  }

  /**
   * Extracts a given front from the provided fronts config
   */
  def frontForPath(path: String, frontsJson: JValue)(implicit ec: ExecutionContext): Response[Front] = {
    val frontOpt = (frontsJson \ "fronts").extract[JObject].findField(_._1 == path).map { case (frontPath, frontJson) =>
      Front(
        id = frontPath,
        webTitle = (frontJson \ "webTitle").extractOpt[String],
        title = (frontJson \ "title").extractOpt[String],
        description = (frontJson \ "description").extractOpt[String],
        collectionIds = (frontJson \ "collections").extract[List[String]]
      )
    }
    Response.fromOption(frontOpt, ApiError.notFound)
  }
}
