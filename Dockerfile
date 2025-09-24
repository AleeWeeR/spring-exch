FROM harbor.fido.uz/dockerhub/maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app

COPY . .

RUN mvn clean package -B -DskipTests

FROM harbor.fido.uz/dockerhub/maven:3.9-eclipse-temurin-21-alpine
WORKDIR /app

COPY --from=build /app/target/*.jar /app/app.jar

ENTRYPOINT ["sh", "-c", "java -jar /app/app.jar"]
