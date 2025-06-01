package co.edu.javeriana.as.personapp.mongo.mapper;

import co.edu.javeriana.as.personapp.common.annotations.Mapper;
import co.edu.javeriana.as.personapp.domain.Gender;
import co.edu.javeriana.as.personapp.domain.Person;
import co.edu.javeriana.as.personapp.domain.Phone;
import co.edu.javeriana.as.personapp.mongo.document.PersonaDocument;
import co.edu.javeriana.as.personapp.mongo.document.TelefonoDocument;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Mapper
public class TelefonoMapperMongo {

	public TelefonoDocument fromDomainToAdapter(Phone phone) {
		TelefonoDocument telefonoDocument = new TelefonoDocument();
		telefonoDocument.setId(phone.getNumber());
		telefonoDocument.setOper(phone.getCompany());
		telefonoDocument.setPrimaryDuenio(validateDuenio(phone.getOwner()));
		return telefonoDocument;
	}

	private PersonaDocument validateDuenio(@NonNull Person owner) {
		if (owner == null) {
			log.warn("Owner is null, creating empty PersonaDocument");
			return new PersonaDocument();
		}
		
		PersonaDocument personaDocument = new PersonaDocument();
		personaDocument.setId(owner.getIdentification());
		personaDocument.setNombre(owner.getFirstName());
		personaDocument.setApellido(owner.getLastName());
		personaDocument.setGenero(owner.getGender() == Gender.FEMALE ? "F" : 
		                          owner.getGender() == Gender.MALE ? "M" : " ");
		personaDocument.setEdad(owner.getAge());
		return personaDocument;
	}

	public Phone fromAdapterToDomain(TelefonoDocument telefonoDocument) {
		if (telefonoDocument == null) {
			log.error("TelefonoDocument is null");
			return null;
		}
		
		log.debug("Converting TelefonoDocument to Phone: {}", telefonoDocument.getId());
		log.debug("TelefonoDocument owner: {}", telefonoDocument.getPrimaryDuenio());
		
		Phone phone = new Phone();
		phone.setNumber(telefonoDocument.getId());
		phone.setCompany(telefonoDocument.getOper());
		phone.setOwner(validateOwner(telefonoDocument.getPrimaryDuenio()));
		return phone;
	}

	private @NonNull Person validateOwner(PersonaDocument duenio) {
		Person person = new Person();
		
		if (duenio == null) {
			log.warn("PersonaDocument duenio is null, creating default person");
			person.setIdentification(0);
			person.setFirstName("Propietario");
			person.setLastName("Desconocido");
			person.setGender(Gender.OTHER);
			person.setAge(null);
			return person;
		}
		
		// Log para debug
		log.debug("PersonaDocument duenio data - ID: {}, Nombre: {}, Apellido: {}", 
		         duenio.getId(), duenio.getNombre(), duenio.getApellido());
		
		// MongoDB puede cargar solo el ID por la referencia lazy, intentar obtenerlo
		Integer personId = duenio.getId();
		
		if (personId == null || personId == 0) {
			log.warn("PersonaDocument ID is null or 0, using default ID 999999");
			personId = 999999; // ID temporal para identificar el problema
		}
		
		person.setIdentification(personId);
		person.setFirstName(duenio.getNombre() != null ? duenio.getNombre() : "Cargando...");
		person.setLastName(duenio.getApellido() != null ? duenio.getApellido() : "Cargando...");
		person.setGender("F".equals(duenio.getGenero()) ? Gender.FEMALE : 
		                 "M".equals(duenio.getGenero()) ? Gender.MALE : Gender.OTHER);
		person.setAge(duenio.getEdad());
		
		log.debug("Created Person with ID: {}, Name: {} {}", 
		         person.getIdentification(), person.getFirstName(), person.getLastName());
		
		return person;
	}
}