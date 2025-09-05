# Build với Maven + JDK 17
FROM maven:3.9.8-eclipse-temurin-17 AS builder
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Run với JRE 17 nhẹ hơn
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

# mount volume cho uploads
VOLUME /opt/uploads

EXPOSE 8080
CMD ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
