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

  /**
   * Fetches the collection information for the given id by resolving info out of the fronts config
   * and the collection's own config JSON.
   */
  def getCollection(id: String, adjustSearchQuery: AdjustSearchQuery = identity)
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

  /**
   * Fetches content for the given backfill query. The query can be manipulated for different
   * requirements by providing adjustment functions. The results then have their facia metadata
   * resolved using the collection information.
   */
  def backfill(backfillQuery: String, collection: Collection,
               adjustSearchQuery: AdjustSearchQuery = identity, adjustItemQuery: AdjustItemQuery = identity)
              (implicit capiClient: GuardianContentClient, ec: ExecutionContext): Response[List[FaciaCard]] = {
    val query = ContentApi.buildBackfillQuery(capiClient, backfillQuery)
      .right.map(adjustSearchQuery)
      .left.map(adjustItemQuery)
    val response = ContentApi.getBackfillResponse(capiClient, query)
    for {
      backfillContent <- ContentApi.backfillContentFromResponse(response)
    } yield {
      // resolve facia metadata to convert content list -> facia card list
      Nil
    }
  }
}
