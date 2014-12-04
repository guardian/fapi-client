package com.gu.facia.api.facia

import com.gu.facia.api._
import lib.{ExecutionContext, ResourcesHelper}
import org.joda.time.{DateTimeZone, DateTime}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{OptionValues, ShouldMatchers, FreeSpec}
import org.json4s.native.JsonMethods._
import org.json4s._


class FaciaTest extends FreeSpec with ShouldMatchers with ResourcesHelper with OptionValues with ScalaFutures with ExecutionContext {

  val frontsConfig = parse(slurpOrDie("config.json"))
  implicit val formats = org.json4s.DefaultFormats

  "frontForPath" - {
    "will return a 'not found' response if no front exists for the path" in {
      Facia.frontForPath("test-path-does-not-exist", frontsConfig).asFuture.futureValue.fold(
        { err => err.statusCode should equal(404) },
        { front => fail("Unexpectedly found a front") }
      )
    }

    "returns the correct front for a path" in {
      Facia.frontForPath("us-news", frontsConfig).fold(
        { err => fail(s"expected to find front, instead got error: $err") },
        { front => front.id should equal("us-news") }
      )
    }

    "extracts the metadata from the fronts config JSON" in {
      Facia.frontForPath("us-news", frontsConfig).asFuture.futureValue.fold(
        { err => fail(s"expected to find front, instead got error: $err") },
        { front =>
          front.webTitle.value should equal("US news")
          front.title.value should equal("Latest US news and comment")
          front.description.value should equal("Latest news, breaking news and current affairs coverage from across the US from theguardian.com")
        }
      )
    }
  }

  "extractFronts" - {
    "extracts fronts from the config JSON" in {
      Facia.extractFronts(frontsConfig).asFuture.futureValue.fold(
        { err => fail(s"expected fronts, got error $err") },
        { fronts =>
          fronts.size should equal(353)
        }
      )
    }
  }

  "getCollection" - {
    val json = parse(
      """
        |{
        |  "lastUpdated": "2014-12-01T16:55:25.139Z",
        |  "updatedBy": "Tim Hill",
        |  "updatedEmail": "tim.hill@guardian.co.uk",
        |  "live": [
        |    {
        |      "id": "internal-code/content/452866346",
        |      "frontPublicationDate": 1417439846171
        |    }
        |  ]
        |}
      """.stripMargin)

    "extracts the content" in {
      Facia.getCollection("958c641f-dd09-46bd-abc2-b32d6be2caaa", json).updated should have(
        'date (Some(new DateTime(2014, 12, 1, 16, 55, 25, 139, DateTimeZone.UTC))),
        'name ("Tim Hill"),
        'email ("tim.hill@guardian.co.uk")
      )
    }

    "extracts the updated data" in {
      Facia.getCollection("958c641f-dd09-46bd-abc2-b32d6be2caaa", json).updated should have(
        'date (Some(new DateTime(2014, 12, 1, 16, 55, 25, 139, DateTimeZone.UTC))),
        'name ("Tim Hill"),
        'email ("tim.hill@guardian.co.uk")
      )
    }
  }

  "rawCardFromJson" - {
    "given a simple (empty) card" - {
      val simpleCardJson = parse(
        """
          |{
          |  "id": "internal-code/content/452602475",
          |  "frontPublicationDate": 1417341830724
          |}
        """.stripMargin).extract[JObject]

      "will extract a simple blank card" in {
        Facia.rawCardFromJson(simpleCardJson).value should have(
          'id("internal-code/content/452602475"),
          'frontPublicationDate(Some(new DateTime(2014, 11, 30, 10, 3, 50, 724, DateTimeZone.UTC)))
        )
      }

      "uses empty/default values for missing parameters" in {
        Facia.rawCardFromJson(simpleCardJson).value should have(
          'headline (None),
          'trailText (None),
          'supporting (Nil),
          'imageOverride (None),
          'kicker (None),
          'group (Standard),
          'cardOptions (CardOptions())
        )
      }
    }

    "given a complete card" - {
      val fullCardJson = parse(
        """
          |{
          |  "id": "internal-code/content/452755278",
          |  "frontPublicationDate": 1417272211435,
          |  "meta": {
          |    "headline": "Test headline",
          |    "trailText": "Test trail text",
          |    "imageSrcHeight": "600",
          |    "imageSrcWidth": "1000",
          |    "imageSrc": "http://media.guim.co.uk/eb401ada1f926364e4b650a58b3e31474be2057e/0_180_3499_2100/1000.jpg",
          |    "imageReplace": true,
          |    "customKicker": "Custom kicker",
          |    "showKickerCustom": true,
          |    "isBoosted": true,
          |    "showByline": true,
          |    "showQuotedHeadline": true,
          |    "supporting": [
          |      {
          |        "id": "internal-code/content/452756603",
          |        "meta": {
          |          "headline": "Supporting headline",
          |          "showQuotedHeadline": true
          |        }
          |      }
          |    ]
          |  }
          |}
        """.stripMargin).extract[JObject]

      "extracts a headline override" in {
        Facia.rawCardFromJson(fullCardJson).value.headline.value should equal("Test headline")
      }

      "extracts a trailText" in {
        Facia.rawCardFromJson(fullCardJson).value.trailText.value should equal("Test trail text")
      }

      "can extract supporting content" in {
        Facia.rawCardFromJson(fullCardJson).value.supporting.head should have(
          'id("internal-code/content/452756603")
        )
      }

      "can extract supporting content extras" in {
        val rawSupportingCard = Facia.rawCardFromJson(fullCardJson).value.supporting.head
        rawSupportingCard should have(
          'frontPublicationDate(None),
          'headline(Some("Supporting headline"))
        )
        rawSupportingCard.cardOptions should have(
          'showQuotedHeadline(true)
        )
      }

      "will correctly get the image override, if present" in {
        Facia.rawCardFromJson(fullCardJson).value.imageOverride.value should have(
          'height(600),
          'width(1000),
          'src("http://media.guim.co.uk/eb401ada1f926364e4b650a58b3e31474be2057e/0_180_3499_2100/1000.jpg")
        )
      }

      "correctly reads card options" in {
        Facia.rawCardFromJson(fullCardJson).value.cardOptions should have(
          'isBoosted (true),
          'showByline (true),
          'showQuotedHeadline (true)
        )
      }
    }

    "with Groups," - {
      def groupJson(groupId: String) = parse(
        s"""
          |{
          |  "id": "internal-code/content/452602475",
          |  "frontPublicationDate": 1417341830724,
          |  "meta": {
          |    "group": "$groupId"
          |  }
          |}
        """.stripMargin).extract[JObject]

      "resolves a huge item" in {
        Facia.rawCardFromJson(groupJson("3")).value.group should equal(Huge)
      }
      "resolves a very big item" in {
        Facia.rawCardFromJson(groupJson("2")).value.group should equal(VeryBig)
      }
      "resolves a big item" in {
        Facia.rawCardFromJson(groupJson("1")).value.group should equal(Big)
      }
      "resolves a standard item" in {
        Facia.rawCardFromJson(groupJson("0")).value.group should equal(Standard)
      }
    }

    "with kickers," - {
      "can resolve a simple custom kicker" in {
        val customKickerJson = parse(
          """
            |{
            |  "id": "internal-code/content/452602475",
            |  "frontPublicationDate": 1417341830724,
            |  "meta": {
            |    "showKickerCustom": true,
            |    "customKicker": "Custom kicker"
            |  }
            |}
          """.stripMargin).extract[JObject]
        Facia.rawCardFromJson(customKickerJson).value.kicker.value should equal(CustomKicker("Custom kicker"))
      }

      "provides a placeholder tag kicker for resolution at CAPI time" in {
        val tagKickerJson = parse(
          """
            |{
            |  "id": "internal-code/content/452602475",
            |  "frontPublicationDate": 1417341830724,
            |  "meta": {
            |    "showKickerTag": true
            |  }
            |}
          """.stripMargin).extract[JObject]
        Facia.rawCardFromJson(tagKickerJson).value.kicker.value shouldBe an[TagKicker]
      }

      "provides a placeholder section kicker for resolution at CAPI time" in {
        val sectionKickerJson = parse(
          """
            |{
            |  "id": "internal-code/content/452602475",
            |  "frontPublicationDate": 1417341830724,
            |  "meta": {
            |    "showKickerSection": true
            |  }
            |}
          """.stripMargin).extract[JObject]
        Facia.rawCardFromJson(sectionKickerJson).value.kicker.value shouldBe an[SectionKicker]
      }
    }
  }

  "extendRawCollection" - {
    "extracts the extended collection data" in {
      val rawCollection = RawCollection("uk/commentisfree/most-viewed/regular-stories", Nil, Updated(None, "name", "email"))
      Facia.extendRawCollection(frontsConfig, rawCollection).asFuture.futureValue.fold(
        err => fail(s"Expected collection, got $err"),
        { srColl =>
          srColl should have(
            'displayName ("popular in comment"),
            'backfillQuery (Some("uk/commentisfree?show-most-viewed=true&show-editors-picks=false&hide-recent-content=true")),
            'type (Some("news/most-popular"))
          )
          srColl.collectionOptions should have(
            'uneditable(true),
            'showTags(true),
            'uneditable(true),
            'showDateHeader(true)
          )
        }
      )
    }

    "fails if the collection is not found in the fronts config" in {
      val rawCollection = RawCollection("test-id-not-present", Nil, Updated(None, "name", "email"))
      Facia.extendRawCollection(frontsConfig, rawCollection).asFuture.futureValue.fold(
        err => err.statusCode should equal(404),
        c => fail(s"expected failure, instead got collection $c")
      )
    }
  }
}
