package com.gu.facia.api

import com.gu.contentapi.client.model.ContentType
import org.joda.time.DateTime

class ??  // for TBD stuff


case class Front(
  id: String,
  webTitle: Option[String],
  title: Option[String],
  description: Option[String],
  collectionsIds: List[String],
  priority: ??   // is this important?
  // etc
)

case class Collection(
  id: String,
  updated: Updated,
  cards: List[FaciaCard]
)

case class FaciaCard(
  id: String,
  content: ContentType,
  frontPublicationDate: DateTime,
  headline: Option[String],
  trailText: Option[String],
  supporting: List[FaciaCard],
  imageOverride: Option[ImageOverride],
  kicker: Option[Kicker],
  group: Group,
  isBoosted: Boolean
)


case class Updated(
  date: DateTime,
  name: String,
  email: String
)

case class ImageOverride(
  src: String,
  width: Int,
  height: Int,
  replace: Boolean
)

sealed trait Group
object Huge extends Group
object VeryBig extends Group
object Big extends Group
object Standard extends Group

sealed trait Kicker
class TagKicker(tag: String) extends Kicker
class SectionKicker(section: String) extends Kicker
class CustomKicker(text: String) extends Kicker


