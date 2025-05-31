package co.edu.javeriana.as.personapp.mapper;

import co.edu.javeriana.as.personapp.common.annotations.Mapper;
import co.edu.javeriana.as.personapp.domain.Gender;
import co.edu.javeriana.as.personapp.domain.Person;
import co.edu.javeriana.as.personapp.model.request.PersonaRequest;
import co.edu.javeriana.as.personapp.model.response.PersonaResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Mapper
public class PersonaMapperRest {
	
	public PersonaResponse fromDomainToAdapterRestMaria(Person person) {
		return fromDomainToAdapterRest(person, "MariaDB");
	}
	
	public PersonaResponse fromDomainToAdapterRestMongo(Person person) {
		return fromDomainToAdapterRest(person, "MongoDB");
	}
	
	public PersonaResponse fromDomainToAdapterRest(Person person, String database) {
		log.debug("Mapping person to response: ID={}, Name={} {}, Database={}", 
			person.getIdentification(), person.getFirstName(), person.getLastName(), database);
		
		// Validar que la persona no sea null
		if (person == null) {
			log.error("Person object is null");
			return new PersonaResponse("", "", "", "", "", database, "ERROR: Person is null");
		}
		
		// Validar y obtener ID
		String personId = "";
		if (person.getIdentification() != null) {
			personId = person.getIdentification().toString();
		} else {
			log.warn("Person has null identification: {}", person);
			personId = "";
		}
		
		String firstName = person.getFirstName() != null ? person.getFirstName() : "";
		String lastName = person.getLastName() != null ? person.getLastName() : "";
		String age = person.getAge() != null ? person.getAge().toString() : "";
		String gender = person.getGender() != null ? person.getGender().toString() : "OTHER";
		
		log.debug("Mapped values: ID={}, Name={} {}, Age={}, Gender={}", 
			personId, firstName, lastName, age, gender);
		
		PersonaResponse response = new PersonaResponse(personId, firstName, lastName, age, gender, database, "OK");
		
		// Para MongoDB, asegurar que _id esté disponible
		if ("MongoDB".equals(database)) {
			// El _id será manejado por los métodos @JsonProperty en PersonaResponse
			log.debug("Mapped for MongoDB with ID: {}", personId);
		}
		
		return response;
	}

	public Person fromAdapterToDomain(PersonaRequest request) {
		log.debug("Mapping request to domain: DNI={}, Name={} {}", 
			request.getDni(), request.getFirstName(), request.getLastName());
		
		if (request == null) {
			throw new IllegalArgumentException("PersonaRequest no puede ser null");
		}
		
		Person person = new Person();
		
		// Validar y convertir DNI
		Integer identification = null;
		try {
			if (request.getDni() != null && !request.getDni().trim().isEmpty()) {
				String dniStr = request.getDni().trim();
				if (!"null".equalsIgnoreCase(dniStr) && !"undefined".equalsIgnoreCase(dniStr)) {
					identification = Integer.valueOf(dniStr);
				}
			}
		} catch (NumberFormatException e) {
			log.warn("Invalid DNI format: {}", request.getDni());
			throw new IllegalArgumentException("DNI debe ser un número válido: " + request.getDni());
		}
		
		if (identification == null || identification <= 0) {
			throw new IllegalArgumentException("DNI es obligatorio y debe ser un número positivo");
		}
		
		person.setIdentification(identification);
		person.setFirstName(request.getFirstName() != null ? request.getFirstName().trim() : "");
		person.setLastName(request.getLastName() != null ? request.getLastName().trim() : "");
		
		// Validar y convertir edad
		Integer age = null;
		try {
			if (request.getAge() != null && !request.getAge().trim().isEmpty()) {
				String ageStr = request.getAge().trim();
				if (!"null".equalsIgnoreCase(ageStr) && !"undefined".equalsIgnoreCase(ageStr)) {
					age = Integer.valueOf(ageStr);
					if (age < 0 || age > 150) {
						throw new IllegalArgumentException("Edad debe estar entre 0 y 150 años");
					}
				}
			}
		} catch (NumberFormatException e) {
			log.warn("Invalid age format: {}", request.getAge());
			throw new IllegalArgumentException("Edad debe ser un número válido: " + request.getAge());
		}
		
		person.setAge(age);
		person.setGender(mapGender(request.getSex()));
		
		log.debug("Mapped person domain: ID={}, Name={} {}, Age={}, Gender={}", 
			person.getIdentification(), person.getFirstName(), person.getLastName(), 
			person.getAge(), person.getGender());
		
		return person;
	}
	
	private Gender mapGender(String sex) {
		if (sex == null) return Gender.OTHER;
		switch (sex.toUpperCase().trim()) {
			case "M":
			case "MALE":
				return Gender.MALE;
			case "F":
			case "FEMALE":
				return Gender.FEMALE;
			default:
				return Gender.OTHER;
		}
	}
}