print("Creando colecciones y insertando datos en persona_db");

const dbPersona = db.getSiblingDB("persona_db");

// Crear todas las colecciones explícitamente
print("Creando colecciones...");
dbPersona.createCollection("persona");
dbPersona.createCollection("profesion");
dbPersona.createCollection("telefono");
dbPersona.createCollection("estudios");

print("Insertando datos en colección persona...");
dbPersona.persona.insertMany([
  {
    "_id": NumberInt(123456789),
    "nombre": "Pepe",
    "apellido": "Perez",
    "genero": "M",
    "edad": NumberInt(30),
    "_class": "co.edu.javeriana.as.personapp.mongo.document.PersonaDocument"
  },
  {
    "_id": NumberInt(987654321),
    "nombre": "Pepito",
    "apellido": "Perez",
    "genero": "M",
    "_class": "co.edu.javeriana.as.personapp.mongo.document.PersonaDocument"
  },
  {
    "_id": NumberInt(321654987),
    "nombre": "Pepa",
    "apellido": "Juarez",
    "genero": "F",
    "edad": NumberInt(30),
    "_class": "co.edu.javeriana.as.personapp.mongo.document.PersonaDocument"
  },
  {
    "_id": NumberInt(147258369),
    "nombre": "Pepita",
    "apellido": "Juarez",
    "genero": "F",
    "edad": NumberInt(10),
    "_class": "co.edu.javeriana.as.personapp.mongo.document.PersonaDocument"
  },
  {
    "_id": NumberInt(963852741),
    "nombre": "Fede",
    "apellido": "Perez",
    "genero": "M",
    "edad": NumberInt(18),
    "_class": "co.edu.javeriana.as.personapp.mongo.document.PersonaDocument"
  }
], { ordered: false });

print("Insertando datos en colección profesion...");
dbPersona.profesion.insertMany([
  {
    "_id": NumberInt(1),
    "nom": "Ingeniero de Software",
    "des": "Desarrollo de aplicaciones y sistemas",
    "_class": "co.edu.javeriana.as.personapp.mongo.document.ProfesionDocument"
  },
  {
    "_id": NumberInt(2),
    "nom": "Médico",
    "des": "Atención médica y salud",
    "_class": "co.edu.javeriana.as.personapp.mongo.document.ProfesionDocument"
  },
  {
    "_id": NumberInt(3),
    "nom": "Abogado",
    "des": "Servicios legales y jurídicos",
    "_class": "co.edu.javeriana.as.personapp.mongo.document.ProfesionDocument"
  },
  {
    "_id": NumberInt(4),
    "nom": "Profesor",
    "des": "Educación y enseñanza",
    "_class": "co.edu.javeriana.as.personapp.mongo.document.ProfesionDocument"
  },
  {
    "_id": NumberInt(5),
    "nom": "Contador",
    "des": "Contabilidad y finanzas",
    "_class": "co.edu.javeriana.as.personapp.mongo.document.ProfesionDocument"
  }
], { ordered: false });

print("Insertando datos en colección telefono...");
dbPersona.telefono.insertMany([
  {
    "_id": "3001234567",
    "oper": "Claro",
    "primaryDuenio": {
      "$ref": "persona",
      "$id": NumberInt(123456789)
    },
    "_class": "co.edu.javeriana.as.personapp.mongo.document.TelefonoDocument"
  },
  {
    "_id": "3109876543",
    "oper": "Movistar",
    "primaryDuenio": {
      "$ref": "persona", 
      "$id": NumberInt(321654987)
    },
    "_class": "co.edu.javeriana.as.personapp.mongo.document.TelefonoDocument"
  },
  {
    "_id": "3157894561",
    "oper": "Tigo",
    "primaryDuenio": {
      "$ref": "persona", 
      "$id": NumberInt(147258369)
    },
    "_class": "co.edu.javeriana.as.personapp.mongo.document.TelefonoDocument"
  },
  {
    "_id": "3208523697",
    "oper": "Claro",
    "primaryDuenio": {
      "$ref": "persona", 
      "$id": NumberInt(963852741)
    },
    "_class": "co.edu.javeriana.as.personapp.mongo.document.TelefonoDocument"
  },
  {
    "_id": "3151478523",
    "oper": "Movistar",
    "primaryDuenio": {
      "$ref": "persona", 
      "$id": NumberInt(987654321)
    },
    "_class": "co.edu.javeriana.as.personapp.mongo.document.TelefonoDocument"
  }
], { ordered: false });

print("Insertando estudios por defecto en colección estudios...");
dbPersona.estudios.insertMany([
  // Pepe Perez (123456789) estudió Ingeniería de Software
  {
    "_id": "123456789-1",
    "primaryPersona": {
      "$ref": "persona",
      "$id": NumberInt(123456789)
    },
    "primaryProfesion": {
      "$ref": "profesion",
      "$id": NumberInt(1)
    },
    "fecha": new Date("2020-12-15"),
    "univer": "Universidad Javeriana",
    "_class": "co.edu.javeriana.as.personapp.mongo.document.EstudiosDocument"
  },
  
  // Pepa Juarez (321654987) estudió Medicina
  {
    "_id": "321654987-2",
    "primaryPersona": {
      "$ref": "persona",
      "$id": NumberInt(321654987)
    },
    "primaryProfesion": {
      "$ref": "profesion",
      "$id": NumberInt(2)
    },
    "fecha": new Date("2018-06-20"),
    "univer": "Universidad Nacional",
    "_class": "co.edu.javeriana.as.personapp.mongo.document.EstudiosDocument"
  },
  
  // Fede Perez (963852741) estudió Derecho
  {
    "_id": "963852741-3",
    "primaryPersona": {
      "$ref": "persona",
      "$id": NumberInt(963852741)
    },
    "primaryProfesion": {
      "$ref": "profesion",
      "$id": NumberInt(3)
    },
    "fecha": new Date("2021-11-10"),
    "univer": "Universidad de los Andes",
    "_class": "co.edu.javeriana.as.personapp.mongo.document.EstudiosDocument"
  },
  
  // Pepito Perez (987654321) estudió Educación
  {
    "_id": "987654321-4",
    "primaryPersona": {
      "$ref": "persona",
      "$id": NumberInt(987654321)
    },
    "primaryProfesion": {
      "$ref": "profesion",
      "$id": NumberInt(4)
    },
    "fecha": new Date("2019-05-18"),
    "univer": "Universidad Pedagógica",
    "_class": "co.edu.javeriana.as.personapp.mongo.document.EstudiosDocument"
  },
  
  // Pepita Juarez (147258369) estudió Contabilidad
  {
    "_id": "147258369-5",
    "primaryPersona": {
      "$ref": "persona",
      "$id": NumberInt(147258369)
    },
    "primaryProfesion": {
      "$ref": "profesion",
      "$id": NumberInt(5)
    },
    "fecha": new Date("2023-07-25"),
    "univer": "Universidad Minuto de Dios",
    "_class": "co.edu.javeriana.as.personapp.mongo.document.EstudiosDocument"
  },
  
  // Estudios adicionales (personas con múltiples carreras)
  
  // Pepe también estudió Contabilidad
  {
    "_id": "123456789-5",
    "primaryPersona": {
      "$ref": "persona",
      "$id": NumberInt(123456789)
    },
    "primaryProfesion": {
      "$ref": "profesion",
      "$id": NumberInt(5)
    },
    "fecha": new Date("2022-03-12"),
    "univer": "Universidad Externado",
    "_class": "co.edu.javeriana.as.personapp.mongo.document.EstudiosDocument"
  },
  
  // Pepa también estudió Derecho
  {
    "_id": "321654987-3",
    "primaryPersona": {
      "$ref": "persona",
      "$id": NumberInt(321654987)
    },
    "primaryProfesion": {
      "$ref": "profesion",
      "$id": NumberInt(3)
    },
    "fecha": new Date("2016-09-30"),
    "univer": "Universidad Javeriana",
    "_class": "co.edu.javeriana.as.personapp.mongo.document.EstudiosDocument"
  },
  
  // Fede también estudió Ingeniería
  {
    "_id": "963852741-1",
    "primaryPersona": {
      "$ref": "persona",
      "$id": NumberInt(963852741)
    },
    "primaryProfesion": {
      "$ref": "profesion",
      "$id": NumberInt(1)
    },
    "fecha": new Date("2023-01-20"),
    "univer": "Universidad Nacional",
    "_class": "co.edu.javeriana.as.personapp.mongo.document.EstudiosDocument"
  }
], { ordered: false });

// Crear índices para mejorar el rendimiento
print("Creando índices...");
dbPersona.persona.createIndex({ "nombre": 1 });
dbPersona.persona.createIndex({ "apellido": 1 });
dbPersona.profesion.createIndex({ "nom": 1 });
dbPersona.telefono.createIndex({ "primaryDuenio.$id": 1 });
dbPersona.estudios.createIndex({ "primaryPersona.$id": 1 });
dbPersona.estudios.createIndex({ "primaryProfesion.$id": 1 });

print("✅ Todas las colecciones creadas e inicializadas correctamente en persona_db");
print("📊 Colecciones disponibles:");
print("   - persona (" + dbPersona.persona.countDocuments() + " documentos)");
print("   - profesion (" + dbPersona.profesion.countDocuments() + " documentos)");
print("   - telefono (" + dbPersona.telefono.countDocuments() + " documentos)");
print("   - estudios (" + dbPersona.estudios.countDocuments() + " documentos)");