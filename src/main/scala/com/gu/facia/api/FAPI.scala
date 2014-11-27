package com.gu.facia.api

import com.gu.contentapi.client.GuardianContentClient
import com.gu.facia.api.config.FaciaConfig
import com.gu.facia.api.facia.Facia

import scala.concurrent.ExecutionContext


object FAPI {
  def frontForPath(path: String)(implicit capiClient: GuardianContentClient, dispatchClient: dispatch.Http, config: FaciaConfig, ec: ExecutionContext): Response[Front] = {
    for {
      frontsConfigJson <- Facia.getFrontsConfig
      front <- Facia.frontForPath(path, frontsConfigJson)
    } yield front
  }

  def getCollection(id: String)(implicit capiClient: GuardianContentClient, config: FaciaConfig, ec: ExecutionContext): Response[Collection] = ???
}
