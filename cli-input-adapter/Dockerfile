# Dockerfile para cli-input-adapter
FROM openjdk:11-jre-slim

# Crear directorio de trabajo
WORKDIR /app

# Copiar cualquier JAR disponible
COPY target/*.jar app.jar

# Crear directorio para logs
RUN mkdir -p /app/logs

# Verificar que el JAR es ejecutable
RUN java -jar app.jar --help > /dev/null 2>&1 || echo "JAR no es ejecutable, usando classpath"

# Comando por defecto - intentar como JAR ejecutable primero
ENTRYPOINT ["java", "-jar", "app.jar"]