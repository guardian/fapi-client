package com.gu.facia.api.facia

import com.gu.facia.api._
import com.gu.facia.api.config.FaciaConfig
import com.gu.facia.api.http.HttpResponse
import org.joda.time.{DateTime, DateTimeZone}
import org.json4s.JsonAST.{JObject, JValue}
import org.json4s.MappingException

import scala.concurrent.ExecutionContext


object Facia {
  implicit val formats = org.json4s.DefaultFormats

  def getFrontsJson(implicit dispatchClient: dispatch.Http, config: FaciaConfig, ec: ExecutionContext): Response[JValue] = {
    val req = dispatch.url(s"${config.root}/config/config.json")
    HttpResponse.jsonResponse(req.toRequest, dispatchClient)
  }

  def getCollectionJson(id: String)(implicit dispatchClient: dispatch.Http, config: FaciaConfig, ec: ExecutionContext): Response[JValue] = {
    val req = dispatch.url(s"${config.root}/collection/$id/collection.json")
    HttpResponse.jsonResponse(req.toRequest, dispatchClient)
  }

  /**
   * Creates a set of front instances from the provided fronts config JSON
   */
  def extractFronts(frontsJson: JValue): Response[Set[Front]] = {
    try {
      val fronts = (frontsJson \ "fronts").extract[Map[String, JValue]].map { case (frontPath, frontJson) =>
        Front(
          id = frontPath,
          webTitle = (frontJson \ "webTitle").extractOpt[String],
          title = (frontJson \ "title").extractOpt[String],
          description = (frontJson \ "description").extractOpt[String],
          onPageDescription = (frontJson \ "onPageDescription").extractOpt[String],
          collectionIds = (frontJson \ "collections").extract[List[String]]
        )
      }.toSet
      Response.Right(fronts)
    } catch {
      case e: MappingException => Response.Left(JsonError("Unable to extract fronts from provided fronts JSON", Some(e)))
    }
  }

  /**
   * Extracts a given front from the provided fronts config
   */
  def frontForPath(path: String, frontsJson: JValue)(implicit ec: ExecutionContext): Response[Front] = {
    for {
      frontOpt <- extractFronts(frontsJson).map(_.find(_.id == path))
      front <- frontOpt.map(Response.Right).getOrElse(Response.Left(NotFound(s"Front $path not found in fronts JSON")))
    } yield front
  }

  /**
   * Gets a "raw" collection instance from the provided collection JSON. This raw instance
   * will then by hydrated using CAPI and extended using the extra collection information
   * found in the fronts' config.json file to create a FaciaCard.
   */
  def getCollection(id: String, collectionJson: JValue)(implicit ec: ExecutionContext): RawCollection = {
    val updated = Updated(
      (collectionJson \ "lastUpdated").extractOpt[String].map(new DateTime(_, DateTimeZone.UTC)),
      (collectionJson \ "updatedBy").extractOpt[String].getOrElse("name not available"),
      (collectionJson \ "updatedEmail").extractOpt[String].getOrElse("email not available")
    )
    val rawCards = (collectionJson \ "live").extract[List[JObject]].map(rawCardFromJson).flatten
    RawCollection(id, rawCards, updated)
  }

  def itemIds(rawCollection: RawCollection): List[String] = {
    rawCollection.rawCards.flatMap { rawCard =>
      rawCard.id :: rawCard.supporting.map(_.id)
    }
  }

  /**
   * Extracts a "raw" facia card instance from the collection JSON. This raw card will then be
   * hydrated using CAPI.
   */
  def rawCardFromJson(cardJson: JObject): Option[RawFaciaCard] = {
    try {
      val imageOverride = (cardJson \ "meta" \ "imageReplace").extractOpt[Boolean].flatMap {
        case false => None
        case true =>
          Some(ImageOverride(
            src = (cardJson \ "meta" \ "imageSrc").extract[String],
            width = Integer.parseInt((cardJson \ "meta" \ "imageSrcWidth").extract[String], 10),
            height = Integer.parseInt((cardJson \ "meta" \ "imageSrcHeight").extract[String], 10)
          ))
      }
      val kicker = {
        val customKickerOpt = (cardJson \ "meta" \ "showKickerCustom").extractOpt[Boolean]
          .flatMap(_ => (cardJson \ "meta" \ "customKicker").extractOpt[String].map(CustomKicker))
        val sectionKickerOpt = (cardJson \ "meta" \ "showKickerSection").extractOpt[Boolean].map(_ => SectionKicker("TMP"))
        val tagKickerOpt = (cardJson \ "meta" \ "showKickerTag").extractOpt[Boolean].map(_ => TagKicker("TMP"))
        customKickerOpt orElse sectionKickerOpt orElse tagKickerOpt
      }

      Some(RawFaciaCard(
       id = (cardJson \ "id").extract[String],
       frontPublicationDate =
         (cardJson \ "frontPublicationDate").extractOpt[Long]
           .map(new DateTime(_, DateTimeZone.UTC)),  // TODO: check if their timestamps are Europe/London or UTC
       headline = (cardJson \ "meta" \ "headline").extractOpt[String],
       trailText = (cardJson \ "meta" \ "trailText").extractOpt[String],
       supporting = (cardJson \ "meta" \ "supporting").extract[List[JObject]].map(rawCardFromJson).flatten,
       imageOverride = imageOverride,
       kicker = kicker,
       group = (cardJson \ "meta" \ "group").extractOpt[String].map(Group.fromGroupId).getOrElse(Standard),
       cardOptions = CardOptions(
         isBoosted = (cardJson \ "meta" \ "isBoosted").extractOpt[Boolean].getOrElse(false),
         showByline = (cardJson \ "meta" \ "showByline").extractOpt[Boolean].getOrElse(false),
         showQuotedHeadline = (cardJson \ "meta" \ "showQuotedHeadline").extractOpt[Boolean].getOrElse(false)
       )
      ))
    } catch {
      // TODO: really this should return a Response not an Option
      case e: NumberFormatException =>
        println(e)
        None
      case e: MappingException =>
        println(e)
        None
    }
  }

  def extendRawCollection(frontJson: JValue, rawCollection: RawCollection)(implicit ec: ExecutionContext): Response[SemiRawCollection] = {
    for {
      collectionMetadataJson <- Response.fromOption(
        (frontJson \ "collections" \ rawCollection.id).extractOpt[JObject],
        NotFound(s"Could not find collection metadata for ${rawCollection.id} in fronts config.json")
      )
      displayName <- Response.fromOption(
        (collectionMetadataJson \ "displayName").extractOpt[String],
        DataError(s"Could not find displayName for ${rawCollection.id} in fronts config")
      )
    } yield {
      SemiRawCollection(
        id = rawCollection.id,
        rawCards = rawCollection.rawCards,
        updated = rawCollection.updated,
        displayName = displayName,
        backfillQuery = (collectionMetadataJson \ "apiQuery").extractOpt[String],
        href = (collectionMetadataJson \ "href").extractOpt[String],
        `type` = (collectionMetadataJson \ "type").extractOpt[String],
        collectionOptions = CollectionOptions(
          hideKickers = (collectionMetadataJson \ "hideKickers").extractOpt[Boolean].getOrElse(false),
          showTags = (collectionMetadataJson \ "showTags").extractOpt[Boolean].getOrElse(false),
          uneditable = (collectionMetadataJson \ "uneditable").extractOpt[Boolean].getOrElse(false),
          showDateHeader = (collectionMetadataJson \ "showDateHeader").extractOpt[Boolean].getOrElse(false)
        )
      )
    }
  }
}
