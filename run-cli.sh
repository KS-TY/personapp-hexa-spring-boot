#!/bin/bash

echo "=== Ejecutando PersonApp CLI ==="

# Verificar que las bases de datos estén corriendo
echo "1. Verificando servicios de base de datos..."
if ! docker-compose ps mariadb | grep -q "Up"; then
    echo "Iniciando MariaDB..."
    docker-compose up -d mariadb
fi

if ! docker-compose ps mongodb | grep -q "Up"; then
    echo "Iniciando MongoDB..."
    docker-compose up -d mongodb
fi

echo "2. Esperando que las bases de datos estén listas..."
sleep 10

echo "3. Ejecutando aplicación CLI..."
docker-compose run --rm personapp-cli

echo "=== CLI terminado ==="