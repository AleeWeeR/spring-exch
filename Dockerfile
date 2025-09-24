# Stage 1: Build the application using Maven on Alpine with JDK 21
FROM maven:3.9-eclipse-temurin-21-alpine AS build

WORKDIR /app
COPY . .

# Optional: Skip tests to speed up the build
RUN mvn clean package -DskipTests

# Stage 2: Runtime image using Eclipse Temurin JRE 21 (compact & secure)
FROM eclipse-temurin:21-jre-alpine AS runtime

# Set Timezone
ENV TZ=Asia/Tashkent

# App directory and user
WORKDIR /app
ARG APPLICATION_USER=appuser

RUN adduser --no-create-home -u 1000 -D $APPLICATION_USER && \
    chown -R $APPLICATION_USER /app

USER 1000

# Copy the jar file from the build stage
COPY --chown=1000:1000 --from=build /app/target/pf-exchange.jar /app/app.jar

# Run the jar file with Spring profile
ENTRYPOINT ["java", "-jar", "app.jar"]