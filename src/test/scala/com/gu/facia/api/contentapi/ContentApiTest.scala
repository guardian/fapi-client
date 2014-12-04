package com.gu.facia.api.contentapi

import com.gu.contentapi.client.GuardianContentClient
import org.scalatest.{FreeSpec, OptionValues, ShouldMatchers}

class ContentApiTest extends FreeSpec with ShouldMatchers with OptionValues {
  val testClient = new GuardianContentClient("test")

  "buildHydrateQuery" - {
    "should do a search query with the provided ids" in {
      ContentApi.buildHydrateQuery(testClient, List("1", "2")).parameters.get("ids").value should equal("1,2")
    }
  }

  "backfillQuery" - {
    "when given a search backfill" - {
      val backfill = "search?tag=tone/analysis&section=world|us-news|australia-news"

      "should produce a searchQuery instance" ignore {}
      "should wrap the tag parameter in brackets" ignore {}
      "should replace funny characters in the url" ignore {}
    }

    "when given an item backfill" - {
      val backfill = "search?tag=tone/analysis&section=world|us-news|australia-news"

      "should produce an itemQuery instance" ignore {}
      "should use the given path for the itemQuery" ignore {}
      "should replace funny characters in the url" ignore {}
    }
  }
}
