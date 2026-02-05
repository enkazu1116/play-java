FROM docker.io/bellsoft/liberica-openjdk-debian:25

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew
RUN ./gradlew dependencies --no-daemon

COPY . .

EXPOSE 8080

CMD ["./gradlew", "bootRun", "--continuous", "--no-daemon"]
