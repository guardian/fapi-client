package com.gu.facia.api.json

import com.gu.facia.api.Response.{Left, Right}
import com.gu.facia.api.{ApiError, Response}
import org.json4s.JsonAST.JValue
import org.json4s.ParserUtil.ParseException
import org.json4s._
import org.json4s.native.JsonMethods._


object Json {
  def toJson(string: String): Response[JValue] = {
    try {
      Right(parse(string))
    } catch {
      case e: ParseException =>
        Left(ApiError("Error parsing response JSON", "", 500))
    }
  }
}
