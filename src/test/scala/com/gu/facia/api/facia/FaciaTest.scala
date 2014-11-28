package com.gu.facia.api.facia

import lib.{ExecutionContext, ResourcesHelper}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{OptionValues, ShouldMatchers, FreeSpec}
import org.json4s.native.JsonMethods._


class FaciaTest extends FreeSpec with ShouldMatchers with ResourcesHelper with OptionValues with ScalaFutures with ExecutionContext {

  val frontsConfig = parse(slurpOrDie("config.json"))

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
}
