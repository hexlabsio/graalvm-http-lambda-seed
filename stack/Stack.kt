import io.hexlabs.kloudformation.module.serverless.Method
import io.hexlabs.kloudformation.module.serverless.serverless
import io.kloudformation.KloudFormation
import io.kloudformation.StackBuilder
import io.kloudformation.json
import io.kloudformation.unaryPlus

class Stack : StackBuilder {
    override fun KloudFormation.create(args: List<String>) {
        val (code) = args
        serverless(serviceName = "kt-graalvm-http-seed", stage = "demo", bucketName = +"lambda-cf-bucket2") {
            serverlessFunction(functionId = "graalvm-http", codeLocationKey = +code,
                    handler = +"unused",
                    runtime = +"provided") {
                lambdaFunction {
                    timeout(30)
                    memorySize(512)
                    environment { variables(json(mapOf(
                            "HTTP4K_BOOTSTRAP_CLASS" to "io.hexlabs.RootHandler"
                    ))) }
                }
                http {
                    path("/hello-runtime/{name}") {
                        Method.GET()
                    }
                    path("/ping") {
                        Method.GET()
                    }
                }
            }
        }
    }
}
