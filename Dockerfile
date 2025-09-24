FROM harbor.fido.uz/devops-images/maven:3.9-amazoncorretto-21-alpine AS build

WORKDIR /app
COPY . .

RUN mvn clean package -B -e -DskipTests


FROM eclipse-temurin:21.0.7_6-jre-alpine-3.21

WORKDIR /app
RUN adduser -D -s /sbin/nologin card && chown -R pf:pf /app
USER pf
COPY --chown=pf:pf --from=build /app/target/*.jar /app/app.jar

ENTRYPOINT java -jar app.jar
