# Dockerfile para rest-input-adapter
FROM openjdk:11-jre-slim

# Crear directorio de trabajo
WORKDIR /app

# Copiar el JAR ejecutable (buscar el que no es .original)
COPY target/rest-input-adapter-*.jar app.jar

# Crear directorio para logs
RUN mkdir -p /app/logs

# Exponer puerto 3000
EXPOSE 3000

# Verificar que el JAR existe y mostrar información
RUN ls -la /app/ && \
    file /app/app.jar && \
    echo "Verificando JAR..." && \
    jar tf app.jar | grep -E "(MANIFEST|Main)" | head -20 || true

# Comando por defecto
ENTRYPOINT ["java", "-jar", "app.jar"]