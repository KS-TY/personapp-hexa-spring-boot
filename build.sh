#!/bin/bash

echo "=== Construyendo PersonApp ==="

# Limpiar JARs anteriores
rm -f cli-input-adapter/target/*.jar
rm -f rest-input-adapter/target/*.jar

# Verificar si Maven está instalado
if command -v mvn &> /dev/null; then
    echo "1. Compilando proyecto con Maven local..."
    mvn clean install -DskipTests
    
    # Verificar que los JARs se crearon
    echo "2. Verificando JARs generados..."
    ls -la rest-input-adapter/target/
    ls -la cli-input-adapter/target/
else
    echo "1. Maven no encontrado. Compilando con Docker..."
    
    # Compilar usando Docker
    docker build -f Dockerfile.build -t personapp-builder .
    
    # Crear contenedor temporal
    docker create --name temp-container personapp-builder
    
    # Crear directorios target si no existen
    mkdir -p cli-input-adapter/target
    mkdir -p rest-input-adapter/target
    
    # Extraer JARs compilados
    echo "2. Extrayendo JARs compilados..."
    
    # Copiar todos los archivos de los directorios de salida
    docker cp temp-container:/output/cli/. ./cli-input-adapter/target/
    docker cp temp-container:/output/rest/. ./rest-input-adapter/target/
    
    # Eliminar contenedor temporal
    docker rm temp-container
    
    echo "3. JARs extraídos:"
    ls -la cli-input-adapter/target/
    ls -la rest-input-adapter/target/
fi

# Verificar que los JARs se crearon correctamente
echo "4. Verificando JARs finales..."
CLI_JAR=$(find cli-input-adapter/target -name "cli-input-adapter-*.jar" -not -name "*.original" | head -1)
REST_JAR=$(find rest-input-adapter/target -name "rest-input-adapter-*.jar" -not -name "*.original" | head -1)

if [ -z "$CLI_JAR" ]; then
    echo "ERROR: JAR de CLI no encontrado"
    exit 1
else
    echo "✅ CLI JAR encontrado: $CLI_JAR"
fi

if [ -z "$REST_JAR" ]; then
    echo "ERROR: JAR de REST API no encontrado"
    exit 1
else
    echo "✅ REST JAR encontrado: $REST_JAR"
fi

# Crear directorio de logs
mkdir -p logs

echo "5. Iniciando servicios de bases de datos..."
docker-compose up -d mariadb mongodb

echo "6. Esperando que las bases de datos estén listas..."
sleep 10

echo "7. Construyendo y levantando todas las aplicaciones..."
docker-compose up --build

echo "=== PersonApp desplegado correctamente ==="
echo "- REST API disponible en: http://localhost:3000"
echo "- Swagger UI en: http://localhost:3000/swagger-ui.html"
echo "- MariaDB en: localhost:3306"
echo "- MongoDB en: localhost:27017"