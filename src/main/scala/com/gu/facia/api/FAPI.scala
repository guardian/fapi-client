package com.gu.facia.api

import com.gu.contentapi.client.GuardianContentClient
import com.gu.facia.api.Response.Async._
import com.gu.facia.api.config.FaciaConfig
import com.gu.facia.api.facia.Facia
import com.gu.facia.api.http.HttpResponse
import com.gu.facia.api.json.Json

import scala.concurrent.ExecutionContext


object FAPI {
  def frontForPath(path: String)(implicit capiClient: GuardianContentClient, dispatchClient: dispatch.Http, config: FaciaConfig, ec: ExecutionContext): Response[Front] = {
    for {
      fronts <- getFronts
      front <- Response.fromOption(fronts.find(_.id == path), ApiError.notFound)
    } yield front
  }

  def getFronts()(implicit capiClient: GuardianContentClient, dispatchClient: dispatch.Http, config: FaciaConfig, ec: ExecutionContext): Response[Set[Front]] = {
    val req = dispatch.url(s"${config.root}/config/config.json")
    for {
      rawResponse <- Right(dispatchClient(req.toRequest, HttpResponse.dispatchHandler))
      json <- Json.toJson(rawResponse.body)
      fronts <- Facia.extractFronts(json)
    } yield fronts
  }

  def getCollection(id: String)(implicit capiClient: GuardianContentClient, config: FaciaConfig, ec: ExecutionContext): Response[Collection] = {
    ???
  }
}
