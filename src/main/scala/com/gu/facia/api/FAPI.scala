package com.gu.facia.api

import com.gu.contentapi.client.GuardianContentClient
import com.gu.contentapi.client.model.{Content, SearchResponse}
import com.gu.facia.api.config.FaciaConfig
import com.gu.facia.api.contentapi.ContentApi
import com.gu.facia.api.contentapi.ContentApi.{AdjustItemQuery, AdjustSearchQuery}
import com.gu.facia.client.ApiClient
import com.gu.facia.client.models.{Collection, CollectionConfig, Front, Trail}

import scala.concurrent.ExecutionContext

case class FaciaContent(trail: Trail, content: Content, config: Option[CollectionConfig])

case class FrontWithId(id: String, front: Front)

object FAPI {

  def getFronts()(implicit capiClient: GuardianContentClient, faciaClient: ApiClient, config: FaciaConfig, ec: ExecutionContext): Response[Map[String, Front]] = {
    Response.Async.Right(
      for {
        config <- faciaClient.config
      } yield config.fronts)
  }

  def frontForPath(path: String)(implicit capiClient: GuardianContentClient, faciaClient: ApiClient, config: FaciaConfig, ec: ExecutionContext): Response[FrontWithId] = {
    for {
      fronts <- getFronts
      front <- Response.fromOption(fronts.find(_._1 == path), NotFound(s"Not front found for $path"))
    } yield FrontWithId(front._1, front._2)
  }

  def getCollection(id: String, adjustSearchQuery: AdjustSearchQuery = identity)
                   (implicit capiClient: GuardianContentClient, faciaClient: ApiClient, config: FaciaConfig, ec: ExecutionContext): Response[List[FaciaContent]] = {
    val futureCollection = Response.Async.Right(faciaClient.collection(id))
    for {
      collection <- futureCollection
      itemIds = collection.live.map(_.id)
      hydrateQuery = adjustSearchQuery(ContentApi.buildHydrateQuery(capiClient, itemIds))
      hydrateResponse <- ContentApi.getHydrateResponse(capiClient, hydrateQuery)
      faciaContent = groupTrailAndContent(collection, hydrateResponse)
      // do hydration of collection data
      // do resolution of Facia metadata
    } yield faciaContent
  }

  def backfill(collection: Collection, adjustSearchQuery: AdjustSearchQuery = identity, adjustItemQuery: AdjustItemQuery = identity)
              (implicit capiClient: GuardianContentClient, ec: ExecutionContext): Response[List[Trail]] = {
    // create backfill query
    // execute backfill query
    // resolve facia metadata from collection
    // return results
    ???
  }

  def groupTrailAndContent(collection: Collection, searchResponse: SearchResponse): List[FaciaContent] = {
    val contentList: Map[String, Content] = searchResponse.results.map(c => c.id -> c).toMap
    for {
      trail <- collection.live
      content <- contentList.get(trail.id)
    } yield FaciaContent(trail, content, None)
  }
}
