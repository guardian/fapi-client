package com.gu.facia.api.facia

import lib.{ExecutionContext, ResourcesHelper}
import org.scalatest.{ShouldMatchers, FreeSpec}
import org.json4s.native.JsonMethods._


class FaciaTest extends FreeSpec with ShouldMatchers with ResourcesHelper with ExecutionContext {
  "frontForPath" - {
    val frontsConfig = parse(slurpOrDie("config.json"))

    "will return a 'not found' response if no front exists for the path" in {
      Facia.frontForPath("test-path-does-not-exist", frontsConfig).fold(
        { front => fail("Unexpectedly found a front") },
        { err => err.statusCode should equal(404) }
      )
    }

    "returns the correct front for a path" in {
      Facia.frontForPath("us-news", frontsConfig).fold(
        { front => front.id should equal("us-news") },
        { err => fail(s"expected to find front, instead got error: $err") }
      )
    }

    "extracts the metadata from the fronts config JSON" in {
      Facia.frontForPath("us-news", frontsConfig).fold(
        { front =>
          front.webTitle should equal("US news")
          front.title should equal("Latest US news and comment")
          front.description should equal("Latest news, breaking news and current affairs coverage from across the US from theguardian.com")
        },
        { err => fail(s"expected to find front, instead got error: $err") }
      )
    }
  }
}
