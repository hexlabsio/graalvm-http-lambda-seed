package io.hexlabs.aws

import com.amazonaws.services.lambda.runtime.ClientContext
import com.amazonaws.services.lambda.runtime.CognitoIdentity
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger

class CustomContext(val requestId: String, val functionArn: String, val expireTimeMs: Long) : Context {
  override fun getAwsRequestId(): String = requestId

  override fun getLogStreamName(): String = System.getenv("AWS_LAMBDA_LOG_STREAM_NAME")

  override fun getClientContext(): ClientContext = TODO("not implemented")

  override fun getFunctionName(): String = System.getenv("AWS_LAMBDA_FUNCTION_NAME")

  override fun getRemainingTimeInMillis(): Int = (expireTimeMs - System.currentTimeMillis()).toInt()

  override fun getLogger(): LambdaLogger = TODO("not implemented")

  override fun getInvokedFunctionArn(): String = functionArn

  override fun getMemoryLimitInMB(): Int = System.getenv("AWS_LAMBDA_FUNCTION_MEMORY_SIZE").toInt()

  override fun getLogGroupName(): String = System.getenv("AWS_LAMBDA_LOG_GROUP_NAME")

  override fun getFunctionVersion(): String = System.getenv("AWS_LAMBDA_FUNCTION_VERSION")

  override fun getIdentity(): CognitoIdentity = TODO("not implemented")
}
