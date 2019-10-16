# Graalvm AWS lambda seed
Basic setup AWS lambda written in Kotlin and compiled as a native image using Graalvm.
Project currently only works with HTTP4K routes, the `_HANDLER` env var is ignored and instead another env
var `HTTP4K_BOOTSTRAP_CLASS` that implements `org.http4k.serverless.AppLoader` is required
The HTTP4K serverless lib has been copied into the code base due to needing a few modifications to get things working


## Building
Use `./gradlew build` to build and `./gradlew shadowJar` to produce a fat jar
To build a native image run `./dist`, this will build the fat jar and create the native image

## Deployment
### Local deployment
run `sam local start-api` to run the lambda instance locally in a docker container 
(hopefully can move this to Kloudformation)

### AWS deployment
run 
`kloudformation -v 1.1.2 -m serverless@1.1.1 deploy -stack-name YOUR_STACK_NAME -bucket CODE_DEPLOYMENT_BUCKET -location build/dist/lambda.zip`

