FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
COPY market-app/pom.xml market-app/
COPY payment-service/pom.xml payment-service/
RUN mvn dependency:go-offline -pl :payment-service -am

COPY payment-service/src payment-service/src
RUN mvn clean package -pl :payment-service -am -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/payment-service/target/payment-service-1.0.0.jar payment-service.jar

EXPOSE 8080

CMD ["java", "-jar", "payment-service.jar"]
