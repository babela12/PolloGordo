
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends unzip \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/*.jar app.jar

RUN mkdir -p /app/wallet

EXPOSE 8083

ENTRYPOINT ["sh", "-c", "printf '%s' \"$ORACLE_WALLET_BASE64\" | base64 -d > /tmp/wallet.zip && unzip -q /tmp/wallet.zip -d /app/wallet && rm /tmp/wallet.zip && exec java -jar app.jar"]
