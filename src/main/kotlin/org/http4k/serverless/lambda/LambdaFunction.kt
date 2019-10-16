package org.http4k.serverless.lambda

import com.amazonaws.services.lambda.runtime.Context
import org.http4k.core.Body
import org.http4k.core.Filter
import org.http4k.core.MemoryBody
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.RequestContexts
import org.http4k.core.Response
import org.http4k.core.Uri
import org.http4k.core.then
import org.http4k.core.toUrlFormEncoded
import org.http4k.filter.ServerFilters
import org.http4k.serverless.ApiGatewayRequest
import org.http4k.serverless.ApiGatewayResponse
import org.http4k.serverless.BootstrapAppLoader

const val LAMBDA_CONTEXT_KEY = "HTTP4K_LAMBDA_CONTEXT"
const val LAMBDA_REQUEST_KEY = "HTTP4K_LAMBDA_REQUEST"

/**
 * This is the main entry point for the lambda. It uses the local environment
 * to instantiate the Http4k handler which can be used for further invocations.
 */
class LambdaFunction(env: Map<String, String> = System.getenv()) {
  private val contexts = RequestContexts()
  private val app = BootstrapAppLoader(env, contexts)
  private val initializeRequestContext = ServerFilters.InitialiseRequestContext(contexts)

  fun handle(request: ApiGatewayRequest, lambdaContext: Context? = null): ApiGatewayResponse =
    initializeRequestContext
      .then(lambdaContextAndRequest(lambdaContext, request, contexts))
      .then(app)(request.asHttp4k())
      .asApiGateway()
}

internal fun Response.asApiGateway() = ApiGatewayResponse(
  statusCode = status.code,
  headers = headers.fold(emptyList<Pair<String, String>>()) { acc, (k, v) ->
    v?.let { acc + (k to it) } ?: acc
  }.toMap(),
  body = bodyString()
)

fun ApiGatewayRequest.asHttp4k() =
  Request(Method.valueOf(httpMethod ?: ""), uri())
    .body(body?.let(::MemoryBody) ?: Body.EMPTY)
    .headers(headers.toList())

internal fun ApiGatewayRequest.uri() = Uri.of(path ?: "")
  .query((queryStringParameters)?.toList()?.toUrlFormEncoded() ?: "")

internal fun lambdaContextAndRequest(lambdaContext: Context?, request: ApiGatewayRequest, contexts: RequestContexts) = Filter { next ->
  {
    lambdaContext?.apply { contexts[it][LAMBDA_CONTEXT_KEY] = lambdaContext }
    contexts[it][LAMBDA_REQUEST_KEY] = request
    next(it)
  }
}
