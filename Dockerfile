FROM bellsoft/liberica-openjdk-debian:25

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew
RUN ./gradlew dependencies

COPY . .
RUN ./gradlew clean bootJar

CMD ["java", "-jar", "build/libs/playjava.jar"]