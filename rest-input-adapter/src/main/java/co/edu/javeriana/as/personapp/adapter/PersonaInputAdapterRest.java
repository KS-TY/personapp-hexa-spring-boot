package co.edu.javeriana.as.personapp.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import co.edu.javeriana.as.personapp.application.port.in.PersonInputPort;
import co.edu.javeriana.as.personapp.application.port.out.PersonOutputPort;
import co.edu.javeriana.as.personapp.application.usecase.PersonUseCase;
import co.edu.javeriana.as.personapp.common.annotations.Adapter;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidOptionException;
import co.edu.javeriana.as.personapp.common.exceptions.NoExistException;
import co.edu.javeriana.as.personapp.common.setup.DatabaseOption;
import co.edu.javeriana.as.personapp.domain.Person;
import co.edu.javeriana.as.personapp.mapper.PersonaMapperRest;
import co.edu.javeriana.as.personapp.model.request.PersonaRequest;
import co.edu.javeriana.as.personapp.model.response.PersonaResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Adapter
public class PersonaInputAdapterRest {

	@Autowired
	@Qualifier("personOutputAdapterMaria")
	private PersonOutputPort personOutputPortMaria;

	@Autowired
	@Qualifier("personOutputAdapterMongo")
	private PersonOutputPort personOutputPortMongo;

	@Autowired
	private PersonaMapperRest personaMapperRest;

	PersonInputPort personInputPort;

	private String setPersonOutputPortInjection(String dbOption) throws InvalidOptionException {
		if (dbOption.equalsIgnoreCase(DatabaseOption.MARIA.toString())) {
			personInputPort = new PersonUseCase(personOutputPortMaria);
			return DatabaseOption.MARIA.toString();
		} else if (dbOption.equalsIgnoreCase(DatabaseOption.MONGO.toString())) {
			personInputPort = new PersonUseCase(personOutputPortMongo);
			return DatabaseOption.MONGO.toString();
		} else {
			throw new InvalidOptionException("Invalid database option: " + dbOption);
		}
	}

	public List<PersonaResponse> historial(String database) {
		log.info("Into historial PersonaEntity in Input Adapter");
		try {
			if(setPersonOutputPortInjection(database).equalsIgnoreCase(DatabaseOption.MARIA.toString())){
				return personInputPort.findAll().stream().map(personaMapperRest::fromDomainToAdapterRestMaria)
						.collect(Collectors.toList());
			}else {
				return personInputPort.findAll().stream().map(personaMapperRest::fromDomainToAdapterRestMongo)
						.collect(Collectors.toList());
			}
			
		} catch (InvalidOptionException e) {
			log.warn(e.getMessage());
			return new ArrayList<PersonaResponse>();
		}
	}

	public PersonaResponse crearPersona(PersonaRequest request) {
		log.info("Into crearPersona PersonaEntity in Input Adapter");
		try {
			String database = setPersonOutputPortInjection(request.getDatabase());
			Person person = personInputPort.create(personaMapperRest.fromAdapterToDomain(request));
			
			if(database.equalsIgnoreCase(DatabaseOption.MARIA.toString())){
				return personaMapperRest.fromDomainToAdapterRestMaria(person);
			} else {
				return personaMapperRest.fromDomainToAdapterRestMongo(person);
			}
		} catch (InvalidOptionException e) {
			log.warn(e.getMessage());
			return new PersonaResponse("", "", "", "", "", "", "ERROR: " + e.getMessage());
		}
	}

	public PersonaResponse actualizarPersona(Integer identification, PersonaRequest request) {
		log.info("Into actualizarPersona PersonaEntity in Input Adapter - ID: {}", identification);
		try {
			String database = setPersonOutputPortInjection(request.getDatabase());
			Person personToUpdate = personaMapperRest.fromAdapterToDomain(request);
			personToUpdate.setIdentification(identification); // Asegurar que el ID sea correcto
			
			Person updatedPerson = personInputPort.edit(identification, personToUpdate);
			
			if(database.equalsIgnoreCase(DatabaseOption.MARIA.toString())){
				return personaMapperRest.fromDomainToAdapterRestMaria(updatedPerson);
			} else {
				return personaMapperRest.fromDomainToAdapterRestMongo(updatedPerson);
			}
		} catch (InvalidOptionException e) {
			log.warn("Invalid option: " + e.getMessage());
			return new PersonaResponse("", "", "", "", "", "", "ERROR: " + e.getMessage());
		} catch (NoExistException e) {
			log.warn("Person not found: " + e.getMessage());
			return new PersonaResponse("", "", "", "", "", "", "ERROR: Persona no encontrada");
		} catch (Exception e) {
			log.error("Unexpected error: " + e.getMessage(), e);
			return new PersonaResponse("", "", "", "", "", "", "ERROR: " + e.getMessage());
		}
	}

	public PersonaResponse eliminarPersona(Integer identification, String database) {
		log.info("=== ADAPTER DELETE START ===");
		log.info("Received identification: {}", identification);
		log.info("Received database: {}", database);
		
		// Validaciones de entrada
		if (identification == null) {
			log.error("Identification is null");
			return new PersonaResponse("", "", "", "", "", database, "ERROR: ID no puede ser null");
		}
		
		if (identification <= 0) {
			log.error("Identification is not positive: {}", identification);
			return new PersonaResponse(identification.toString(), "", "", "", "", database, "ERROR: ID debe ser un número positivo");
		}
		
		if (database == null || database.trim().isEmpty()) {
			log.error("Database is null or empty");
			return new PersonaResponse(identification.toString(), "", "", "", "", "", "ERROR: Database es requerido");
		}
		
		try {
			// Configurar puerto de salida
			String dbUsed = setPersonOutputPortInjection(database);
			log.info("Database option set to: {}", dbUsed);
			
			// Verificar que la persona existe antes de eliminar
			log.info("Searching for person with ID: {}", identification);
			Person existingPerson;
			try {
				existingPerson = personInputPort.findOne(identification);
				if (existingPerson == null) {
					log.warn("Person with ID {} not found", identification);
					return new PersonaResponse(identification.toString(), "", "", "", "", database, "ERROR: Persona no encontrada");
				}
				log.info("Found person: {} {} (ID: {})", existingPerson.getFirstName(), existingPerson.getLastName(), existingPerson.getIdentification());
			} catch (NoExistException e) {
				log.warn("Person with ID {} does not exist: {}", identification, e.getMessage());
				return new PersonaResponse(identification.toString(), "", "", "", "", database, "ERROR: Persona no encontrada");
			}
			
			// Eliminar la persona
			log.info("Attempting to delete person with ID: {}", identification);
			Boolean deleted;
			try {
				deleted = personInputPort.drop(identification);
				log.info("Delete operation result: {}", deleted);
			} catch (NoExistException e) {
				log.error("Error during delete - person not found: {}", e.getMessage());
				return new PersonaResponse(identification.toString(), "", "", "", "", database, "ERROR: Persona no encontrada para eliminar");
			}
			
			if (deleted != null && deleted) {
				// Crear respuesta de éxito con los datos de la persona eliminada
				PersonaResponse response;
				if(dbUsed.equalsIgnoreCase(DatabaseOption.MARIA.toString())){
					response = personaMapperRest.fromDomainToAdapterRestMaria(existingPerson);
				} else {
					response = personaMapperRest.fromDomainToAdapterRestMongo(existingPerson);
				}
				response.setStatus("DELETED");
				log.info("Success response created with status: {}", response.getStatus());
				return response;
			} else {
				log.warn("Delete operation returned false for ID: {}", identification);
				return new PersonaResponse(identification.toString(), "", "", "", "", database, "ERROR: No se pudo eliminar la persona");
			}
			
		} catch (InvalidOptionException e) {
			log.error("Invalid database option: {}", e.getMessage());
			return new PersonaResponse(identification.toString(), "", "", "", "", database, "ERROR: Opción de base de datos inválida: " + e.getMessage());
		} catch (Exception e) {
			log.error("Unexpected error during deletion: {}", e.getMessage(), e);
			return new PersonaResponse(identification.toString(), "", "", "", "", database, "ERROR: Error inesperado: " + e.getMessage());
		} finally {
			log.info("=== ADAPTER DELETE END ===");
		}
	}
}