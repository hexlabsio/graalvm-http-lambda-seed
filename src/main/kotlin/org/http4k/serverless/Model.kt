package org.http4k.serverless

import kotlinx.serialization.*
import kotlinx.serialization.internal.StringSerializer
import kotlinx.serialization.internal.nullable
import kotlinx.serialization.json.*
import java.io.BufferedReader
import java.io.InputStream

val json = Json(JsonConfiguration(strictMode = false))

@Serializable
data class ApiGatewayRequest(
  val path: String? = null,
  val httpMethod: String? = null,
  val headers: Map<String, String> = emptyMap(),
  val queryStringParameters: Map<String, String>? = null,
  val pathParameters: Map<String, String>? = null,
  val body: String? = null
)

fun ApiGatewayRequest.Companion.parseFromStream(jsonStream: InputStream): ApiGatewayRequest =
  json.parse(serializer(), jsonStream.bufferedReader().use(BufferedReader::readText))

@Serializable
data class ApiGatewayResponse(
  val statusCode: Int,
  val body: String? = null,
  val headers: Map<String, String> = emptyMap(),
  val isBase64Encoded: Boolean = false
)

fun ApiGatewayResponse.asJsonStr(): String =
  json.stringify(ApiGatewayResponse.serializer(), this)

val mapStrStrSerializer: KSerializer<Map<String, String?>> = (StringSerializer to StringSerializer.nullable).map
fun Map<String, String?>.asJsonStr(): String = json.stringify(mapStrStrSerializer, this)
