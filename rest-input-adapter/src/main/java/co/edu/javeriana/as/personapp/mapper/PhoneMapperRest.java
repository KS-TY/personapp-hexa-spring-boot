package co.edu.javeriana.as.personapp.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import co.edu.javeriana.as.personapp.application.port.in.PersonInputPort;
import co.edu.javeriana.as.personapp.application.port.out.PersonOutputPort;
import co.edu.javeriana.as.personapp.application.usecase.PersonUseCase;
import co.edu.javeriana.as.personapp.common.annotations.Mapper;
import co.edu.javeriana.as.personapp.common.exceptions.NoExistException;
import co.edu.javeriana.as.personapp.common.setup.DatabaseOption;
import co.edu.javeriana.as.personapp.domain.Person;
import co.edu.javeriana.as.personapp.domain.Phone;
import co.edu.javeriana.as.personapp.model.request.PhoneRequest;
import co.edu.javeriana.as.personapp.model.response.PhoneResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Mapper
public class PhoneMapperRest {
	
	@Autowired
	@Qualifier("personOutputAdapterMaria")
	private PersonOutputPort personOutputPortMaria;
	
	@Autowired
	@Qualifier("personOutputAdapterMongo")
	private PersonOutputPort personOutputPortMongo;
	
	public PhoneResponse fromDomainToAdapterRestMaria(Phone phone) {
		return fromDomainToAdapterRest(phone, "MariaDB");
	}
	
	public PhoneResponse fromDomainToAdapterRestMongo(Phone phone) {
		return fromDomainToAdapterRest(phone, "MongoDB");
	}
	
	public PhoneResponse fromDomainToAdapterRest(Phone phone, String database) {
		log.debug("Mapping phone to response: Number={}, Company={}, Database={}", 
			phone.getNumber(), phone.getCompany(), database);
		
		if (phone == null) {
			log.error("Phone object is null");
			return new PhoneResponse("", "", "", database, "ERROR: Phone is null");
		}
		
		String phoneNumber = phone.getNumber() != null ? phone.getNumber() : "";
		String company = phone.getCompany() != null ? phone.getCompany() : "";
		
		// Obtener el ID del propietario
		String ownerId = "";
		if (phone.getOwner() != null && phone.getOwner().getIdentification() != null) {
			ownerId = phone.getOwner().getIdentification().toString();
			log.debug("Owner ID found: {}", ownerId);
		} else {
			log.warn("Phone owner or owner ID is null for phone: {}", phoneNumber);
			ownerId = "0";
		}
		
		log.debug("Mapped values: Number={}, Company={}, OwnerId={}, Database={}", 
			phoneNumber, company, ownerId, database);
		
		PhoneResponse response = new PhoneResponse(phoneNumber, company, ownerId, database, "OK");
		
		return response;
	}

	public Phone fromAdapterToDomain(PhoneRequest request) {
		log.debug("=== MAPPING PHONE REQUEST TO DOMAIN ===");
		log.debug("Request: {}", request);
		log.debug("Request database field: '{}'", request.getDatabase());
		
		if (request == null) {
			throw new IllegalArgumentException("PhoneRequest no puede ser null");
		}
		
		Phone phone = new Phone();
		
		// Validar número
		if (request.getNumber() == null || request.getNumber().trim().isEmpty()) {
			throw new IllegalArgumentException("Número de teléfono es obligatorio");
		}
		phone.setNumber(request.getNumber().trim());
		
		// Validar compañía
		if (request.getCompany() == null || request.getCompany().trim().isEmpty()) {
			throw new IllegalArgumentException("Compañía es obligatoria");
		}
		phone.setCompany(request.getCompany().trim());
		
		// Validar y obtener owner
		Integer ownerId = null;
		try {
			if (request.getOwnerId() != null && !request.getOwnerId().trim().isEmpty()) {
				String ownerIdStr = request.getOwnerId().trim();
				if (!"null".equalsIgnoreCase(ownerIdStr) && !"undefined".equalsIgnoreCase(ownerIdStr)) {
					ownerId = Integer.valueOf(ownerIdStr);
				}
			}
		} catch (NumberFormatException e) {
			log.warn("Invalid owner ID format: {}", request.getOwnerId());
			throw new IllegalArgumentException("ID del propietario debe ser un número válido: " + request.getOwnerId());
		}
		
		if (ownerId == null || ownerId <= 0) {
			throw new IllegalArgumentException("ID del propietario es obligatorio y debe ser un número positivo");
		}
		
		// CORRECCIÓN CRÍTICA: Usar la base de datos especificada en el request
		String databaseToUse = request.getDatabase();
		log.debug("Database specified in request for owner lookup: '{}'", databaseToUse);
		
		// Normalizar el valor de la base de datos
		if (databaseToUse != null) {
			databaseToUse = databaseToUse.trim().toUpperCase();
		}
		
		// Buscar el propietario en la base de datos correcta
		Person owner = findPersonById(ownerId, databaseToUse);
		if (owner == null) {
			log.error("No se encontró persona con ID: {} en database: {}", ownerId, databaseToUse);
			throw new IllegalArgumentException("No se encontró persona con ID: " + ownerId + " en " + databaseToUse);
		}
		
		phone.setOwner(owner);
		
		log.debug("Mapped phone domain: Number={}, Company={}, Owner={}, Database={}", 
			phone.getNumber(), phone.getCompany(), phone.getOwner().getIdentification(), databaseToUse);
		
		return phone;
	}
	
	private Person findPersonById(Integer personId, String database) {
		log.debug("=== FINDING PERSON BY ID ===");
		log.debug("PersonId: {}, Database: '{}'", personId, database);
		
		try {
			PersonInputPort personInputPort;
			
			// CORRECCIÓN: Asegurar que se use la base de datos correcta
			if ("MARIA".equalsIgnoreCase(database) || "MARIADB".equalsIgnoreCase(database)) {
				log.debug("Using MARIA database for person lookup");
				personInputPort = new PersonUseCase(personOutputPortMaria);
			} else if ("MONGO".equalsIgnoreCase(database) || "MONGODB".equalsIgnoreCase(database)) {
				log.debug("Using MONGO database for person lookup");
				personInputPort = new PersonUseCase(personOutputPortMongo);
			} else {
				log.warn("Unknown database '{}', defaulting to MARIA", database);
				personInputPort = new PersonUseCase(personOutputPortMaria);
			}
			
			Person person = personInputPort.findOne(personId);
			log.debug("Person found: {} {} (ID: {})", 
				person.getFirstName(), person.getLastName(), person.getIdentification());
			return person;
			
		} catch (NoExistException e) {
			log.warn("Person not found with ID: {} in database: {}", personId, database);
			return null;
		} catch (Exception e) {
			log.error("Error finding person with ID: {} in database: {}", personId, database, e);
			return null;
		}
	}
}