# Stage 1: build
FROM maven:3.9-amazoncorretto-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B -q
COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true -B -q

# Stage 2: extract layers
FROM eclipse-temurin:21-jre-alpine AS extract
WORKDIR /app
COPY --from=build /app/target/*.war app.war
RUN java -Djarmode=layertools -jar app.war extract

# Stage 3: run
FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
WORKDIR /app
COPY --from=extract /app/dependencies/ ./
COPY --from=extract /app/spring-boot-loader/ ./
COPY --from=extract /app/snapshot-dependencies/ ./
COPY --from=extract /app/application/ ./
RUN chown -R appuser:appgroup /app
USER appuser
EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.WarLauncher"]
