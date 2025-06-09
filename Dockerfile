# == Build stage. Includes JDK, src folder, and everything else to build the jar

FROM maven:3.9.9-eclipse-temurin-21-alpine AS builder

WORKDIR /build

COPY ./pom.xml ./
COPY ./src ./src
COPY ./build_ssl.sh ./

RUN mvn clean package -DskipTests
RUN chmod 777 ./build_ssl.sh && ./build_ssl.sh

# == Runtime environment. Includes JRE and jar file

FROM eclipse-temurin:21.0.5_11-jre-alpine-3.21

WORKDIR /auth

COPY --from=builder /build/target/*.jar ./auth.jar
COPY --from=builder /build/springboot.crt ./
COPY --from=builder /build/springboot.p12 ./

RUN keytool -importcert \
    -noprompt \
    -trustcacerts \
    -alias springboot \
    -file springboot.crt \
    -keystore /etc/ssl/certs/java/cacerts \
    -storepass changeit

EXPOSE 8080

CMD ["java", "-jar", "auth.jar"]