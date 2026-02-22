FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

COPY pom.xml .
COPY market-app/pom.xml market-app/
RUN mvn dependency:go-offline -pl :market-app -am

COPY market-app/src market-app/src
RUN mvn clean package -pl :market-app -am -DskipTests

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/market-app/target/market-app-1.0.0.jar market-app.jar

EXPOSE 8080

CMD ["java", "-jar", "market-app.jar"]
