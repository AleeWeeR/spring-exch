FROM harbor.fido.uz/devops-images/maven:3.9-amazoncorretto-21-alpine AS build

WORKDIR /app
COPY . .

RUN echo "Checking if Reposilite server is up..." && if curl --output /dev/null --silent --head --fail http://10.50.50.168:8200; then echo "Reposilite server is up. Using settings_reposilite.xml." && mkdir -p /root/.m2 && cp settings.xml /root/.m2/; else echo "Reposilite server is down. Using default settings.xml."; fi
RUN mvn clean package -B -e -DskipTests


FROM eclipse-temurin:21.0.7_6-jre-alpine-3.21

WORKDIR /app

RUN adduser -D -s /sbin/nologin pf

RUN mkdir -p /var/log/app/archive && \
    chown -R pf:pf /var/log/app && \
    chmod -R 755 /var/log/app

ENV LOG_PATH=/var/log/app
ENV LOG_FILE=pfexchange
ENV TZ=Asia/Tashkent

COPY --chown=pf:pf --from=build /app/target/*.jar /app/app.jar

USER pf

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
