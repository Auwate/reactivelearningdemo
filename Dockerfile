# == Build stage. Includes JDK, src folder, and everything else to build the jar

FROM 3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

COPY ./pom.xml ./
COPY ./src ./src

RUN mvn clean package -Dskiptests

# == Runtime environment. Includes JRE and jar file

FROM eclipse-temurin:21.0.5_11-jre-alpine-3.21

WORKDIR /auth

COPY --from=builder /build/target/*.jar ./auth.jar

EXPOSE 8080

CMD ["java", "-jar", "auth.jar"]