#!/bin/bash

echo "=== Construyendo PersonApp ==="

# Verificar si Maven está instalado
if command -v mvn &> /dev/null; then
    echo "1. Compilando proyecto con Maven local..."
    mvn clean package -DskipTests
else
    echo "1. Maven no encontrado. Compilando con Docker..."
    
    # Compilar usando Docker
    docker build -f Dockerfile.build -t personapp-builder .
    
    # Extraer JARs compilados
    docker create --name temp-container personapp-builder
    docker cp temp-container:/output/cli/ ./cli-input-adapter/target/
    docker cp temp-container:/output/rest/ ./rest-input-adapter/target/
    docker rm temp-container
    
    # Mover JARs desde subcarpetas a la ubicación correcta
    echo "Organizando JARs..."
    
    # CLI JAR - buscar el JAR ejecutable (más grande)
    if [ -d "cli-input-adapter/target/cli" ]; then
        cd cli-input-adapter/target/cli/
        largest_jar=""
        largest_size=0
        for jar in *.jar; do
            if [ -f "$jar" ]; then
                size=$(stat -f%z "$jar" 2>/dev/null || stat -c%s "$jar" 2>/dev/null || echo 0)
                if [ "$size" -gt "$largest_size" ]; then
                    largest_size=$size
                    largest_jar="$jar"
                fi
            fi
        done
        
        if [ -n "$largest_jar" ]; then
            mv "$largest_jar" ../cli-input-adapter-0.0.1-SNAPSHOT.jar
            echo "CLI JAR movido: $largest_jar (${largest_size} bytes) -> cli-input-adapter-0.0.1-SNAPSHOT.jar"
        fi
        
        cd ../../../
        rm -rf cli-input-adapter/target/cli/
    fi
    
    # REST JAR - buscar el JAR ejecutable (más grande)
    if [ -d "rest-input-adapter/target/rest" ]; then
        cd rest-input-adapter/target/rest/
        largest_jar=""
        largest_size=0
        for jar in *.jar; do
            if [ -f "$jar" ]; then
                size=$(stat -f%z "$jar" 2>/dev/null || stat -c%s "$jar" 2>/dev/null || echo 0)
                if [ "$size" -gt "$largest_size" ]; then
                    largest_size=$size
                    largest_jar="$jar"
                fi
            fi
        done
        
        if [ -n "$largest_jar" ]; then
            mv "$largest_jar" ../rest-input-adapter-0.0.1-SNAPSHOT.jar
            echo "REST JAR movido: $largest_jar (${largest_size} bytes) -> rest-input-adapter-0.0.1-SNAPSHOT.jar"
        fi
        
        cd ../../../
        rm -rf rest-input-adapter/target/rest/
    fi
fi

# Verificar que los JARs se crearon
echo "2. Verificando JARs generados..."
if [ ! -f "rest-input-adapter/target/rest-input-adapter-0.0.1-SNAPSHOT.jar" ]; then
    echo "ERROR: JAR de REST API no encontrado"
    ls -la rest-input-adapter/target/
    exit 1
fi

if [ ! -f "cli-input-adapter/target/cli-input-adapter-0.0.1-SNAPSHOT.jar" ]; then
    echo "ERROR: JAR de CLI no encontrado"
    ls -la cli-input-adapter/target/
    exit 1
fi

echo "✅ JARs encontrados correctamente"

# Crear directorio de logs
mkdir -p logs

echo "3. Iniciando servicios de bases de datos..."
docker-compose up -d mariadb mongodb

echo "4. Esperando que las bases de datos estén listas..."
sleep 30

echo "5. Construyendo y levantando todas las aplicaciones..."
docker-compose up --build

echo "=== PersonApp desplegado correctamente ==="
echo "- REST API disponible en: http://localhost:3000"
echo "- Swagger UI en: http://localhost:3000/swagger-ui.html"
echo "- MariaDB en: localhost:3306"
echo "- MongoDB en: localhost:27017"