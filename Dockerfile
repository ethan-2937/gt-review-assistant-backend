FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -q -DskipTests package

FROM eclipse-temurin:21-jre
WORKDIR /app

RUN apt-get update \
    && apt-get install -y --no-install-recommends python3 python3-pip fonts-noto-cjk \
    && python3 -m pip install --break-system-packages --no-cache-dir openpyxl PyMuPDF Pillow \
    && rm -rf /var/lib/apt/lists/*

COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 18081

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
