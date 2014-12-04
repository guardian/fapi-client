package com.gu.facia.api

import com.gu.contentapi.client.GuardianContentClient
import com.gu.contentapi.client.model.Content
import com.gu.facia.api.config.FaciaConfig
import com.gu.facia.api.contentapi.ContentApi
import com.gu.facia.api.contentapi.ContentApi.{AdjustItemQuery, AdjustSearchQuery}
import com.gu.facia.api.facia.Facia

import scala.concurrent.ExecutionContext


object FAPI {

  def getFronts()(implicit capiClient: GuardianContentClient, dispatchClient: dispatch.Http, config: FaciaConfig, ec: ExecutionContext): Response[Set[Front]] = {
    for {
      json <- Facia.getFrontsJson
      fronts <- Facia.extractFronts(json)
    } yield fronts
  }

  def frontForPath(path: String)(implicit capiClient: GuardianContentClient, dispatchClient: dispatch.Http, config: FaciaConfig, ec: ExecutionContext): Response[Front] = {
    for {
      fronts <- getFronts
      front <- Response.fromOption(fronts.find(_.id == path), NotFound(s"Not front found for $path"))
    } yield front
  }

  def getCollection(id: String, adjustSearchQuery: AdjustSearchQuery = ContentApi.doNotChangeSearchQuery)
                   (implicit capiClient: GuardianContentClient, dispatchClient: dispatch.Http, config: FaciaConfig, ec: ExecutionContext): Response[Collection] = {
    val fCollectionJson = Facia.getCollectionJson(id)
    val fFrontsJson = Facia.getFrontsJson
    for {
      frontsJson <- fFrontsJson
      collectionJson <- fCollectionJson
      rawCollection = Facia.getCollection(id, collectionJson)
      itemIds = Facia.itemIds(rawCollection)
      hydrateQuery = adjustSearchQuery(ContentApi.buildHydrateQuery(capiClient, itemIds))
      hydrateResponse <- ContentApi.getHydrateResponse(capiClient, hydrateQuery)
      semiRawCollection <- Facia.extendRawCollection(frontsJson, rawCollection)
      // do hydration of collection data
      // do resolution of Facia metadata
    } yield {
      1
    }
    ???
  }

  def backfill(collection: Collection, adjustSearchQuery: AdjustSearchQuery = ContentApi.doNotChangeSearchQuery, adjustItemQuery: AdjustItemQuery = ContentApi.doNotChangeItemQuery)
              (implicit capiClient: GuardianContentClient, ec: ExecutionContext): Response[List[FaciaCard]] = {
    // create backfill query
    // execute backfill query
    // resolve facia metadata from collection
    // return results
    ???
  }
}
