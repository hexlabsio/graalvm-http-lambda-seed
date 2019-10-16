package io.hexlabs

import com.amazonaws.services.lambda.runtime.Context
import io.hexlabs.aws.CustomContext
import org.http4k.client.JavaHttpClient
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.serverless.ApiGatewayRequest
import org.http4k.serverless.ApiGatewayResponse
import org.http4k.serverless.asJsonStr
import org.http4k.serverless.parseFromStream
import org.http4k.serverless.lambda.LambdaFunction
import java.io.PrintWriter
import java.io.StringWriter

const val requestIdHeaderName = "lambda-runtime-aws-request-id"
const val functionArnHeaderName = "lambda-runtime-invoked-function-arn"
const val deadlineMsHeaderName = "lambda-runtime-deadline-ms"
const val runtimeApiEndpointVariableName = "AWS_LAMBDA_RUNTIME_API"
// const val handlerVariableName = "_HANDLER"

val client: HttpHandler = JavaHttpClient()

@Suppress("UNCHECKED_CAST")
fun main() {
  val runtimeApiEndpoint = System.getenv(runtimeApiEndpointVariableName)
  val handlerInstance = LambdaFunction()
  eventLoop(runtimeApiEndpoint) { apiEndpoint ->
    handleInvocation(handlerInstance, getInvocation(apiEndpoint))
  }
}

internal tailrec fun eventLoop(runtimeApiEndpoint: String, block: (String) -> Unit) {
  try {
    block(runtimeApiEndpoint)
  } catch (t: Throwable) {
    t.printStackTrace()
    postInvocationError(t, runtimeApiEndpoint)
  }
  eventLoop(runtimeApiEndpoint, block)
}

internal fun getInvocation(runtimeApiEndpoint: String): Invocation {
  val invocationResponse: Response = nextEvent(runtimeApiEndpoint)
  return Invocation(
    runtimeApiEndpoint = runtimeApiEndpoint,
    context = CustomContext(
      requestId = invocationResponse.header(requestIdHeaderName)!!,
      functionArn = invocationResponse.header(functionArnHeaderName)!!,
      expireTimeMs = invocationResponse.header(deadlineMsHeaderName)!!.toLong()
    ),
    request = invocationResponse.let {
      ApiGatewayRequest.parseFromStream(it.body.stream)
    }
  )
}

data class Invocation(val runtimeApiEndpoint: String, val context: Context, val request: ApiGatewayRequest)

internal fun handleInvocation(requestHandler: LambdaFunction, invocation: Invocation) {
  val requestId = invocation.context.awsRequestId ?: "Request id unknown"
  try {
    val functionResponse = requestHandler.handle(invocation.request, invocation.context)
    postSucess(functionResponse, invocation.runtimeApiEndpoint, requestId)
  } catch (t: Throwable) {
    val stacktrace = PrintWriter(StringWriter()).also { t.printStackTrace(it) }.toString()
    println("function handle failed for requestId $requestId with '${t.message}' $stacktrace")
    postFunctionError(t, invocation.runtimeApiEndpoint, requestId)
  }
}

internal fun nextEvent(runtimeApiEndpoint: String): Response =
  client(Request(GET, "http://$runtimeApiEndpoint/2018-06-01/runtime/invocation/next"))

internal fun postFunctionError(t: Throwable, runtimeApiEndpoint: String, requestId: String) {
  println("Invocation failed with ${t.message}")
  client(Request(POST, "http://$runtimeApiEndpoint/2018-06-01/runtime/invocation/$requestId/error")
    .body(mapOf("errorMessage" to t.message).asJsonStr()))
}

internal fun postInvocationError(t: Throwable, runtimeApiEndpoint: String) =
  client(Request(POST, "http://$runtimeApiEndpoint/2018-06-01/runtime/init/error")
    .body(mapOf("errorMessage" to t.message).asJsonStr()))

internal fun postSucess(response: ApiGatewayResponse, runtimeApiEndpoint: String, requestId: String) {
  client(Request(POST, "http://$runtimeApiEndpoint/2018-06-01/runtime/invocation/$requestId/response")
    .body(response.asJsonStr()))
}
