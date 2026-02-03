# Mac 環境用。Windows では使用しない（参考: Dockerfile.windows.txt）
FROM bellsoft/liberica-openjdk-debian:25 AS base
WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

# 開発用
FROM base AS development
COPY . .
EXPOSE 8080
CMD ["./gradlew", "bootRun", "--continuous", "--no-daemon"]

# 本番用
FROM base AS prod
COPY . .
RUN ./gradlew clean bootJar --no-daemon
EXPOSE 8080
CMD ["java", "-jar", "build/libs/playjava.jar"]
