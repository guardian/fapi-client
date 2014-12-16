package com.gu.facia.api

import com.gu.contentapi.client.model.Content
import org.joda.time.DateTime


case class Front(
  id: String,
  webTitle: Option[String],
  title: Option[String],
  description: Option[String],
  onPageDescription: Option[String],
  collectionIds: List[String]
//  priority: ??   // is this important?
  // etc
)

case class Collection(
  id: String,
  cards: List[FaciaCard],
  updated: Updated,
  displayName: String,
  backfillQuery: Option[String],
  href: Option[String],
  collectionOptions: CollectionOptions
)

private[api] case class RawCollection(
  id: String,
  rawCards: List[RawFaciaCard],
  updated: Updated
)

private[api] case class SemiRawCollection(
  id: String,
  rawCards: List[RawFaciaCard],
  updated: Updated,
  displayName: String,
  backfillQuery: Option[String],
  href: Option[String],
  `type`: Option[String],
  collectionOptions: CollectionOptions
)

case class FaciaCard(
  id: String,
  content: Content,
  frontPublicationDate: Option[DateTime],
  headline: Option[String],
  trailText: Option[String],
  supporting: List[FaciaCard],
  imageOverride: Option[ImageOverride],
  kicker: Option[Kicker],
  group: Group,
  cardOptions: CardOptions
)

private[api] case class RawFaciaCard(
  id: String,
  frontPublicationDate: Option[DateTime],
  headline: Option[String],
  trailText: Option[String],
  supporting: List[RawFaciaCard],
  imageOverride: Option[ImageOverride],
  kicker: Option[Kicker],
  group: Group,
  cardOptions: CardOptions
)

case class Updated(
  date: Option[DateTime],
  name: String,
  email: String
)

case class CardOptions(
  isBoosted: Boolean = false,
  showByline: Boolean = false,
  showQuotedHeadline: Boolean = false
)

case class ImageOverride(
  src: String,
  width: Int,
  height: Int
)

sealed trait Group
object Huge extends Group
object VeryBig extends Group
object Big extends Group
object Standard extends Group
object Group {
  def fromGroupId(groupId: String): Group = groupId match {
    case "3" => Huge
    case "2" => VeryBig
    case "1" => Big
    case _ => Standard
  }
}

sealed trait Kicker
case class TagKicker(tag: String) extends Kicker
case class SectionKicker(section: String) extends Kicker
case class CustomKicker(text: String) extends Kicker

case class CollectionOptions(
  hideKickers: Boolean = false,
  showTags: Boolean = false,
  uneditable: Boolean = false,
  showDateHeader: Boolean = false
)
