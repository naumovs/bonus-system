package loyalty

import zio.http.{RequestHandlerMiddleware, RequestHandlerMiddlewares}

package object api {

  val loggingMiddleware: RequestHandlerMiddleware[Nothing, Any, Nothing, Any] =
    RequestHandlerMiddlewares.requestLogging(
    logRequestBody = true,
    logResponseBody = true
  )
}
