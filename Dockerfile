# Imagen del backend para Render (o cualquier servidor con Docker).
# Etapa 1: compila el artefacto con Maven y Java 21.
FROM maven:3.9-eclipse-temurin-21 AS compilacion
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src ./src
RUN mvn -q -B package -DskipTests

# Etapa 2: solo el JRE y el jar.
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=compilacion /app/target/centro-control-drones-1.0.0.jar app.jar
ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
