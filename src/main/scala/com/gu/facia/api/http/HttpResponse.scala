package com.gu.facia.api.http

import dispatch.FunctionHandler

case class HttpResponse(body: String, statusCode: Int, statusMessage: String)
object HttpResponse {
  val dispatchHandler = new FunctionHandler({ response =>
    HttpResponse(response.getResponseBody("utf-8"), response.getStatusCode, response.getStatusText)
  })
}
