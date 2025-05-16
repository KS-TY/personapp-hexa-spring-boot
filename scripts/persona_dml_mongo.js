print("Insertando datos en prueba_db");

const dbPersona = db.getSiblingDB("persona_db");

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

print("Datos insertados en prueba_db");
