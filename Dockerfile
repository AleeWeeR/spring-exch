FROM harbor.fido.uz/dockerhub/eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

COPY . .

RUN mvn clean package -B -DskipTests

FROM harbor.fido.uz/dockerhub/eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar /app/app.jar

ENTRYPOINT ["sh", "-c", "java -jar /app/app.jar"]
