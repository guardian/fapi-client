package com.gu.facia.api.http

import com.gu.facia.api.{ApiError, Response}
import com.gu.facia.api.Response.{Right, Left}
import com.ning.http.client.{Response => DispatchResponse}
import dispatch.FunctionHandler
import org.json4s.JsonAST.JValue
import org.json4s.ParserUtil.ParseException
import org.json4s._
import org.json4s.native.JsonMethods._



object DispatchHandlers {
  val asHttpResponse = new FunctionHandler({ response =>
    HttpResponse(response.getResponseBody("utf-8"), response.getStatusCode, response.getStatusText)
  })
}
