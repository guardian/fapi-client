package com.gu.facia.api

import scala.concurrent.{ExecutionContext, Future}


case class Response[A] private (underlying: Future[Either[ApiError, A]]) {
  def map[B](f: A => B)(implicit ec: ExecutionContext): Response[B] =
    flatMap(a => Response.Right(f(a)))

  def flatMap[B](f: A => Response[B])(implicit ec: ExecutionContext): Response[B] = Response {
    asFuture.flatMap {
      case scala.util.Right(a) => f(a).asFuture
      case scala.Left(e) => Future.successful(scala.Left(e))
    }
  }

  def fold[B](failure: ApiError => B, success: A => B)(implicit ec: ExecutionContext): Future[B] = {
    asFuture.map(_.fold(failure, success))
  }

  def asFuture(implicit ec: ExecutionContext) = {
    underlying recover { case err =>
      val apiError = ApiError.unexpected(err.getMessage)
      scala.Left(apiError)
    }
  }
}
object Response {
  def Right[A](a: A): Response[A] =
    Response(Future.successful(scala.Right(a)))

  def Left[A](err: ApiError): Response[A] =
    Response(Future.successful(scala.Left(err)))

  def fromOption[A](optA: Option[A], orLeft: ApiError): Response[A] =
    optA.map(a => Right(a)).getOrElse(Left(orLeft))

  object Async {
    def Right[A](fa: Future[A])(implicit ec: ExecutionContext): Response[A] =
      Response(fa.map(scala.Right(_)))

    def Left[A](ferr: Future[ApiError])(implicit ec: ExecutionContext): Response[A] =
      Response(ferr.map(scala.Left(_)))
  }
}

case class ApiError(message: String, details: String, statusCode: Int)
object ApiError {
  val notFound = ApiError("Not found", "Not found", 404)
  def unexpected(details: String) = ApiError("Unexpected error", details, 500)
}
