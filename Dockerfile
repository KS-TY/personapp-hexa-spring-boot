# Etapa de construcción
FROM maven:3.9.6-eclipse-temurin-17 AS build

# Copiamos el código fuente
WORKDIR /app
COPY . .

# Compilar todo el proyecto (multi-módulo)
RUN mvn clean package -DskipTests

# Etapa de ejecución
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copiamos el jar del módulo deseado (ej: rest-input-adapter)
COPY --from=build /app/rest-input-adapter/target/rest-input-adapter-*.jar app.jar

# Puerto por defecto de Spring Boot
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
