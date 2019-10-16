package io.hexlabs

import org.http4k.core.*
import org.http4k.routing.bind
import org.http4k.routing.path
import org.http4k.routing.routes
import org.http4k.serverless.AppLoader

object RootHandler : AppLoader {
  override fun invoke(environment: Map<String, String>): HttpHandler = routes

  val routes: HttpHandler = routes(
    "/ping" bind Method.GET to { Response(Status.OK).body("""{"ping": "pong"}""") },
    "/hello-runtime/{name}" bind Method.GET to { req: Request ->
      val path: String? = req.path("name")
      Response(Status.OK).body("""{ "greeting": "hello $path"}""")
    }
  )
}
