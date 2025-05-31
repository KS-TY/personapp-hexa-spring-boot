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
		
		String personId = person.getIdentification() != null ? person.getIdentification().toString() : "0";
		String firstName = person.getFirstName() != null ? person.getFirstName() : "";
		String lastName = person.getLastName() != null ? person.getLastName() : "";
		String age = person.getAge() != null ? person.getAge().toString() : "";
		String gender = person.getGender() != null ? person.getGender().toString() : "OTHER";
		
		log.debug("Mapped values: ID={}, Name={} {}, Age={}, Gender={}", 
			personId, firstName, lastName, age, gender);
		
		return new PersonaResponse(personId, firstName, lastName, age, gender, database, "OK");
	}

	public Person fromAdapterToDomain(PersonaRequest request) {
		log.debug("Mapping request to domain: DNI={}, Name={} {}", 
			request.getDni(), request.getFirstName(), request.getLastName());
		
		Person person = new Person();
		
		// Validar y convertir DNI
		Integer identification = null;
		try {
			if (request.getDni() != null && !request.getDni().trim().isEmpty()) {
				identification = Integer.valueOf(request.getDni().trim());
			}
		} catch (NumberFormatException e) {
			log.warn("Invalid DNI format: {}", request.getDni());
		}
		
		person.setIdentification(identification);
		person.setFirstName(request.getFirstName() != null ? request.getFirstName().trim() : "");
		person.setLastName(request.getLastName() != null ? request.getLastName().trim() : "");
		
		// Validar y convertir edad
		Integer age = null;
		try {
			if (request.getAge() != null && !request.getAge().trim().isEmpty()) {
				age = Integer.valueOf(request.getAge().trim());
			}
		} catch (NumberFormatException e) {
			log.warn("Invalid age format: {}", request.getAge());
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