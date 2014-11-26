package com.gu.facia.api

import scala.concurrent.Future


case class ApiResponse[A] private (asFuture: Future[Either[ApiErrors, A]]) {
  def map[B](f: A => B): ApiResponse[B] =
    flatMap(a => ApiResponse.ApiRight(f(a)))

  def flatMap[B](f: A => ApiResponse[B]): ApiResponse[B] = ApiResponse {
    asFuture.flatMap {
      case Right(a) => f(a).asFuture recover { case err =>
        Left(ApiErrors(List(ApiError("Unexpected error", "Unexpected error", 500))))
      }
      case Left(e) => Future.successful(Left(e))
    }
  }

  def fold[B](success: A => B, failure: ApiErrors => B): Future[B] = {
    asFuture.map(_.fold(failure, success))
  }

  private def futureErrToLeft: ApiResponse[A] = ApiResponse {
    asFuture recover { case err =>
      // log this error!
      val apiError = ApiError("Unexpected error", "Unexpected error", 500)
      Left(ApiErrors(List(apiError)))
    }
  }
}
object ApiResponse {
  def ApiRight[A](a: A): ApiResponse[A] =
    ApiResponse(Future.successful(Right(a)))

  def ApiLeft[A](err: ApiErrors): ApiResponse[A] =
    ApiResponse(Future.successful(Left(err)))

  object Async {
    def ApiRight[A](fa: Future[A]): ApiResponse[A] =
      ApiResponse(fa.map(Right(_))).futureErrToLeft

    def ApiLeft[A](ferr: Future[ApiErrors]): ApiResponse[A] =
      ApiResponse(ferr.map(Left(_)))
  }
}

case class ApiError(message: String, friendlyMessage: String,
                    statusCode: Int, context: Option[String] = None)
case class ApiErrors(errors: List[ApiError]) {
  def statusCode = errors.map(_.statusCode).max
}
