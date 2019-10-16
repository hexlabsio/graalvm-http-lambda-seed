#!/usr/bin/env bash
./gradlew shadowJar

rm -rf build/dist
mkdir build/dist

docker run --rm --name graal -v $(pwd):/working oracle/graalvm-ce:1.0.0-rc16 \
    /bin/bash -c "native-image --enable-url-protocols=http \
                    -Djava.net.preferIPv4Stack=true \
                    -H:ReflectionConfigurationFiles=/working/runtime/reflectionConfiguration.json \
                    -H:+ReportUnsupportedElementsAtRuntime --no-server -jar /working/build/libs/dist-1.0.jar; \
                    cp dist-1.0 /working/build/dist/lambda"

cp runtime/bootstrap build/dist

zip -j build/dist/lambda.zip build/dist/*