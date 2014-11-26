package com.gu.facia

package object api {
  type Response[T] = Either[ResponseError, T]
}
