FROM eclipse-temurin:11-jdk as builder

COPY . .
RUN ./gradlew installDist --no-daemon

FROM eclipse-temurin:16

WORKDIR /app
COPY --from=builder build/install/zeitung .

ENTRYPOINT ["/app/bin/zeitung"]