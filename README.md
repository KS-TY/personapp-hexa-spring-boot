# 🧑‍💼 PersonApp - Hexagonal Architecture

Una aplicación completa de gestión de personas, profesiones, teléfonos y estudios construida con **Arquitectura Hexagonal (Clean Architecture)** usando Spring Boot.

## 📋 Tabla de Contenidos

- [Características](#-características)
- [Arquitectura](#-arquitectura)
- [Tecnologías](#-tecnologías)
- [Requisitos Previos](#-requisitos-previos)
- [Instalación y Configuración](#-instalación-y-configuración)
- [Modos de Ejecución](#-modos-de-ejecución)
- [Uso de la Aplicación](#-uso-de-la-aplicación)
- [API REST](#-api-rest)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Resolución de Problemas](#-resolución-de-problemas)
- [Contribuir](#-contribuir)
- [Licencia](#-licencia)

## ✨ Características

- 🏗️ **Arquitectura Hexagonal**: Separación clara entre dominio, aplicación e infraestructura
- 🗄️ **Dual Database**: Soporte para MariaDB y MongoDB con switching dinámico
- 🌐 **REST API**: API completa con documentación Swagger
- 💻 **CLI Interactive**: Interfaz de línea de comandos para operaciones CRUD
- 🎨 **Web UI**: Interfaz web moderna y responsiva
- 🔄 **CRUD Completo**: Crear, Leer, Actualizar y Eliminar para todas las entidades
- 📊 **Múltiples Entidades**: Personas, Profesiones, Teléfonos y Estudios
- 🐳 **Docker Ready**: Completamente containerizado

## 🛠️ Tecnologías

### Backend
- **Java 11** - Lenguaje de programación
- **Spring Boot 2.7.11** - Framework principal
- **Spring Data JPA** - Para MariaDB
- **Spring Data MongoDB** - Para MongoDB
- **Maven** - Gestión de dependencias
- **Lombok** - Reducción de código boilerplate

### Frontend
- **HTML5/CSS3** - Estructura y estilos
- **JavaScript (Vanilla)** - Interactividad
- **Responsive Design** - Compatible con móviles

### Bases de Datos
- **MariaDB 10.6** - Base de datos relacional
- **MongoDB 6** - Base de datos NoSQL

### DevOps
- **Docker & Docker Compose** - Containerización
- **OpenAPI/Swagger** - Documentación de API

## 📋 Requisitos Previos

- **Docker** >= 20.10
- **Docker Compose** >= 2.0
- **Maven** >= 3.6 (opcional, se puede usar Docker)
- **Java 11** (opcional, se puede usar Docker)

## 🚀 Instalación y Configuración

### 1. Clonar el Repositorio

```bash
git clone <repository-url>
cd personapp-hexa-spring-boot
```

### 2. Configurar Permisos de Scripts

```bash
chmod +x build.sh
chmod +x run-cli.sh
```

### 3. Limpiar Instalación Previa (Si es necesario)

Si ya tienes bases de datos con estos nombres y están dockerizadas:

```bash
echo "1. Deteniendo todos los contenedores..."
docker-compose down

echo "2. Eliminando contenedores del proyecto..."
docker-compose rm -f

echo "3. Eliminando volúmenes del proyecto..."
docker-compose down -v
```

### 4. Construir el Proyecto

```bash
./build.sh
```

Este script:
- 🔨 Compila el proyecto con Maven (local o Docker)
- 📦 Genera los JARs ejecutables
- 🗄️ Inicia las bases de datos
- 🚀 Despliega todas las aplicaciones

## 🎯 Modos de Ejecución

### 🌐 Modo Web (REST API + UI)

```bash
./build.sh
```

**Accesos:**
- **Aplicación Web**: http://localhost:3000
- **API REST**: http://localhost:3000/api/v1
- **Swagger UI**: http://localhost:3000/swagger-ui.html

### 💻 Modo CLI (Línea de Comandos)

```bash
./run-cli.sh
```

**Funcionalidades del CLI:**
- Navegación por menús interactivos
- CRUD completo para todas las entidades
- Selección de base de datos (MariaDB/MongoDB)
- Operaciones en tiempo real

### 🔧 Compilación Manual

```bash
# Con Maven local
mvn clean install -DskipTests

# Con Docker
docker build -f Dockerfile.build -t personapp-builder .
```

## 📱 Uso de la Aplicación

### Interfaz Web

1. **Navegación**: Usa las pestañas para cambiar entre entidades
2. **Base de Datos**: Selecciona MariaDB o MongoDB
3. **Operaciones**:
   - ➕ **Crear**: Completa el formulario y haz clic en "Crear"
   - 📖 **Leer**: Los datos se cargan automáticamente
   - ✏️ **Editar**: Haz clic en "Editar" en cualquier tarjeta
   - 🗑️ **Eliminar**: Haz clic en "Eliminar" y confirma

### CLI Interactivo

```
----------------------
1 para trabajar con el Modulo de Personas
2 para trabajar con el Modulo de Profesiones  
3 para trabajar con el Modulo de Telefonos
4 para trabajar con el Modulo de Estudios
0 para Salir
Ingrese una opción: _
```

## 🔌 API REST

### Endpoints Principales

#### Personas
- `GET /api/v1/persona/{database}` - Listar personas
- `POST /api/v1/persona` - Crear persona
- `PUT /api/v1/persona/{id}` - Actualizar persona
- `DELETE /api/v1/persona/{id}?database={db}` - Eliminar persona

#### Profesiones
- `GET /api/v1/profession/{database}` - Listar profesiones
- `POST /api/v1/profession` - Crear profesión
- `PUT /api/v1/profession/{id}` - Actualizar profesión
- `DELETE /api/v1/profession/{id}?database={db}` - Eliminar profesión

#### Teléfonos
- `GET /api/v1/phone/{database}` - Listar teléfonos
- `GET /api/v1/phone/owner/{personId}/{database}` - Teléfonos por propietario
- `POST /api/v1/phone` - Crear teléfono
- `PUT /api/v1/phone/{number}` - Actualizar teléfono
- `DELETE /api/v1/phone/{number}?database={db}` - Eliminar teléfono

#### Estudios
- `GET /api/v1/study/{database}` - Listar estudios
- `GET /api/v1/study/person/{personId}/{database}` - Estudios por persona
- `GET /api/v1/study/profession/{professionId}/{database}` - Estudios por profesión
- `POST /api/v1/study` - Crear estudio
- `PUT /api/v1/study/{personId}/{professionId}` - Actualizar estudio
- `DELETE /api/v1/study/{personId}/{professionId}?database={db}` - Eliminar estudio

### Ejemplo de Uso

```bash
# Listar todas las personas en MariaDB
curl http://localhost:3000/api/v1/persona/MARIA

# Crear una nueva persona
curl -X POST http://localhost:3000/api/v1/persona \
  -H "Content-Type: application/json" \
  -d '{
    "dni": "123456789",
    "firstName": "Juan",
    "lastName": "Pérez",
    "age": "30",
    "sex": "M",
    "database": "MARIA"
  }'
```

## 📁 Estructura del Proyecto

```
personapp-hexa-spring-boot/
├── 📁 common/                  # Configuraciones comunes
├── 📁 domain/                  # Entidades del dominio
├── 📁 application/             # Casos de uso y puertos
├── 📁 maria-output-adapter/    # Adaptador MariaDB
├── 📁 mongo-output-adapter/    # Adaptador MongoDB
├── 📁 rest-input-adapter/      # API REST y Web UI
├── 📁 cli-input-adapter/       # Interfaz CLI
├── 📁 scripts/                 # Scripts de base de datos
├── 📄 docker-compose.yml       # Configuración Docker
├── 📄 build.sh                 # Script de construcción
├── 📄 run-cli.sh              # Script CLI
└── 📄 README.md               # Esta documentación
```

### Módulos Principales

- **🏠 common**: Anotaciones y configuraciones compartidas
- **🎯 domain**: Entidades de negocio (Person, Profession, Phone, Study)
- **⚙️ application**: Puertos y casos de uso (Use Cases)
- **🗄️ maria-output-adapter**: Implementación JPA para MariaDB
- **🍃 mongo-output-adapter**: Implementación MongoDB
- **🌐 rest-input-adapter**: API REST + Interfaz Web
- **💻 cli-input-adapter**: Aplicación de línea de comandos

## 🔧 Resolución de Problemas

### Problemas Comunes

#### 🚫 Puerto ya en uso
```bash
# Verificar qué está usando el puerto
sudo lsof -i :3000
sudo lsof -i :3306
sudo lsof -i :27017

# Detener servicios conflictivos
docker-compose down
```

#### 🗄️ Problemas de Base de Datos
```bash
# Reiniciar bases de datos
docker-compose restart mariadb mongodb

# Ver logs
docker-compose logs mariadb
docker-compose logs mongodb
```

#### 🏗️ Problemas de Compilación
```bash
# Limpiar y recompilar
mvn clean
./build.sh
```

#### 📁 Permisos de Scripts
```bash
chmod +x build.sh
chmod +x run-cli.sh
```

### Logs

```bash
# Logs de aplicaciones
tail -f logs/persona.log

# Logs de Docker
docker-compose logs -f
docker-compose logs -f personapp-rest
docker-compose logs -f personapp-cli
```

### Verificar Estado

```bash
# Estado de contenedores
docker-compose ps

# Estado de bases de datos
docker-compose exec mariadb mysql -u persona_db -ppersona_db -e "SELECT COUNT(*) FROM persona_db.persona;"
docker-compose exec mongodb mongosh persona_db --eval "db.persona.countDocuments()"
```

## 👥 Autores

- **Maria Andrea Mendez** -
- **Juan David Castillo** -
- **Luis Fernando Lee** - 
- **Pontificia Universidad Javeriana** -

---

## 🎯 Quick Start

```bash
# 1. Clonar y configurar
git clone <repository-url>
cd personapp-hexa-spring-boot
chmod +x build.sh run-cli.sh

# 2. Construir y ejecutar web
./build.sh

# 3. Acceder a la aplicación
open http://localhost:3000

# 4. O ejecutar CLI
./run-cli.sh
```

**¡Listo para usar! 🎉**
