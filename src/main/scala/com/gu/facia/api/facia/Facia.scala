package com.gu.facia.api.facia

import com.gu.facia.api.Response.Async.Right
import com.gu.facia.api.config.FaciaConfig
import com.gu.facia.api.http.HttpResponse
import com.gu.facia.api.json.Json
import com.gu.facia.api.{ApiError, Front, Response}
import org.json4s.JsonAST.{JObject, JValue}

import scala.concurrent.ExecutionContext


object Facia {
  implicit val formats = org.json4s.DefaultFormats

  def extractFronts(frontsJson: JValue): Response[Set[Front]] = {
    Response.Right((frontsJson \ "fronts").extract[Map[String, JValue]].map { case (frontPath, frontJson) =>
      Front(
        id = frontPath,
        webTitle = (frontJson \ "webTitle").extractOpt[String],
        title = (frontJson \ "title").extractOpt[String],
        description = (frontJson \ "description").extractOpt[String],
        onPageDescription = (frontJson \ "onPageDescription").extractOpt[String],
        collectionIds = (frontJson \ "collections").extract[List[String]]
      )
    } toSet)
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
        onPageDescription = (frontJson \ "onPageDescription").extractOpt[String],
        collectionIds = (frontJson \ "collections").extract[List[String]]
      )
    }
    Response.fromOption(frontOpt, ApiError.notFound)
  }
}
