package co.edu.javeriana.as.personapp.mapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import co.edu.javeriana.as.personapp.application.port.in.PersonInputPort;
import co.edu.javeriana.as.personapp.application.port.in.ProfessionInputPort;
import co.edu.javeriana.as.personapp.application.port.out.PersonOutputPort;
import co.edu.javeriana.as.personapp.application.port.out.ProfessionOutputPort;
import co.edu.javeriana.as.personapp.application.usecase.PersonUseCase;
import co.edu.javeriana.as.personapp.application.usecase.ProfessionUseCase;
import co.edu.javeriana.as.personapp.common.annotations.Mapper;
import co.edu.javeriana.as.personapp.common.exceptions.NoExistException;
import co.edu.javeriana.as.personapp.common.setup.DatabaseOption;
import co.edu.javeriana.as.personapp.domain.Person;
import co.edu.javeriana.as.personapp.domain.Profession;
import co.edu.javeriana.as.personapp.domain.Study;
import co.edu.javeriana.as.personapp.model.request.StudyRequest;
import co.edu.javeriana.as.personapp.model.response.StudyResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Mapper
public class StudyMapperRest {
	
	@Autowired
	@Qualifier("personOutputAdapterMaria")
	private PersonOutputPort personOutputPortMaria;
	
	@Autowired
	@Qualifier("personOutputAdapterMongo")
	private PersonOutputPort personOutputPortMongo;
	
	@Autowired
	@Qualifier("professionOutputAdapterMaria")
	private ProfessionOutputPort professionOutputPortMaria;
	
	@Autowired
	@Qualifier("professionOutputAdapterMongo")
	private ProfessionOutputPort professionOutputPortMongo;
	
	private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
	
	public StudyResponse fromDomainToAdapterRestMaria(Study study) {
		return fromDomainToAdapterRest(study, "MariaDB");
	}
	
	public StudyResponse fromDomainToAdapterRestMongo(Study study) {
		return fromDomainToAdapterRest(study, "MongoDB");
	}
	
	public StudyResponse fromDomainToAdapterRest(Study study, String database) {
		log.debug("Mapping study to response: PersonId={}, ProfessionId={}, Database={}", 
			study.getPerson().getIdentification(), study.getProfession().getIdentification(), database);
		
		if (study == null) {
			log.error("Study object is null");
			return new StudyResponse("", "", "", "", database, "ERROR: Study is null");
		}
		
		String personId = "";
		String professionId = "";
		
		if (study.getPerson() != null && study.getPerson().getIdentification() != null) {
			personId = study.getPerson().getIdentification().toString();
		} else {
			log.warn("Study person or person ID is null");
		}
		
		if (study.getProfession() != null && study.getProfession().getIdentification() != null) {
			professionId = study.getProfession().getIdentification().toString();
		} else {
			log.warn("Study profession or profession ID is null");
		}
		
		String graduationDate = "";
		if (study.getGraduationDate() != null) {
			graduationDate = study.getGraduationDate().format(DATE_FORMATTER);
		}
		
		String universityName = study.getUniversityName() != null ? study.getUniversityName() : "";
		
		log.debug("Mapped values: PersonId={}, ProfessionId={}, GraduationDate={}, University={}", 
			personId, professionId, graduationDate, universityName);
		
		StudyResponse response = new StudyResponse(personId, professionId, graduationDate, universityName, database, "OK");
		
		return response;
	}

	public Study fromAdapterToDomain(StudyRequest request) {
		log.debug("=== MAPPING STUDY REQUEST TO DOMAIN ===");
		log.debug("Request: {}", request);
		log.debug("Request database field: '{}'", request.getDatabase());
		
		if (request == null) {
			throw new IllegalArgumentException("StudyRequest no puede ser null");
		}
		
		Study study = new Study();
		
		// Validar PersonId
		Integer personId = null;
		try {
			if (request.getPersonId() != null && !request.getPersonId().trim().isEmpty()) {
				String personIdStr = request.getPersonId().trim();
				if (!"null".equalsIgnoreCase(personIdStr) && !"undefined".equalsIgnoreCase(personIdStr)) {
					personId = Integer.valueOf(personIdStr);
				}
			}
		} catch (NumberFormatException e) {
			log.warn("Invalid person ID format: {}", request.getPersonId());
			throw new IllegalArgumentException("ID de persona debe ser un número válido: " + request.getPersonId());
		}
		
		if (personId == null || personId <= 0) {
			throw new IllegalArgumentException("ID de persona es obligatorio y debe ser un número positivo");
		}
		
		// Validar ProfessionId
		Integer professionId = null;
		try {
			if (request.getProfessionId() != null && !request.getProfessionId().trim().isEmpty()) {
				String professionIdStr = request.getProfessionId().trim();
				if (!"null".equalsIgnoreCase(professionIdStr) && !"undefined".equalsIgnoreCase(professionIdStr)) {
					professionId = Integer.valueOf(professionIdStr);
				}
			}
		} catch (NumberFormatException e) {
			log.warn("Invalid profession ID format: {}", request.getProfessionId());
			throw new IllegalArgumentException("ID de profesión debe ser un número válido: " + request.getProfessionId());
		}
		
		if (professionId == null || professionId <= 0) {
			throw new IllegalArgumentException("ID de profesión es obligatorio y debe ser un número positivo");
		}
		
		// Usar la base de datos especificada en el request
		String databaseToUse = request.getDatabase();
		log.debug("Database specified in request for lookup: '{}'", databaseToUse);
		
		if (databaseToUse != null) {
			databaseToUse = databaseToUse.trim().toUpperCase();
		}
		
		// CORRECCIÓN: Intentar buscar en la misma base de datos, con fallback
		Person person = findPersonById(personId, databaseToUse);
		if (person == null) {
			// Fallback: intentar en la otra base de datos
			String fallbackDb = "MARIA".equals(databaseToUse) ? "MONGO" : "MARIA";
			log.warn("Person not found in {}, trying {}", databaseToUse, fallbackDb);
			person = findPersonById(personId, fallbackDb);
			
			if (person == null) {
				log.error("No se encontró persona con ID: {} en ninguna base de datos", personId);
				throw new IllegalArgumentException("No se encontró persona con ID: " + personId);
			}
		}
		
		Profession profession = findProfessionById(professionId, databaseToUse);
		if (profession == null) {
			// Fallback: intentar en la otra base de datos
			String fallbackDb = "MARIA".equals(databaseToUse) ? "MONGO" : "MARIA";
			log.warn("Profession not found in {}, trying {}", databaseToUse, fallbackDb);
			profession = findProfessionById(professionId, fallbackDb);
			
			if (profession == null) {
				log.error("No se encontró profesión con ID: {} en ninguna base de datos", professionId);
				throw new IllegalArgumentException("No se encontró profesión con ID: " + professionId);
			}
		}
		
		study.setPerson(person);
		study.setProfession(profession);
		
		// Procesar fecha de graduación con manejo mejorado
		if (request.getGraduationDate() != null && !request.getGraduationDate().trim().isEmpty()) {
			try {
				String dateStr = request.getGraduationDate().trim();
				if (!"null".equalsIgnoreCase(dateStr) && !"undefined".equalsIgnoreCase(dateStr)) {
					LocalDate graduationDate = LocalDate.parse(dateStr, DATE_FORMATTER);
					study.setGraduationDate(graduationDate);
				}
			} catch (Exception e) {
				log.warn("Invalid graduation date format: {} - Error: {}", request.getGraduationDate(), e.getMessage());
				// No lanzar excepción, solo dejar null
			}
		}
		
		study.setUniversityName(request.getUniversityName() != null ? request.getUniversityName().trim() : "");
		
		log.debug("Mapped study domain: PersonId={}, ProfessionId={}, GraduationDate={}, University={}, Database={}", 
			study.getPerson().getIdentification(), study.getProfession().getIdentification(), 
			study.getGraduationDate(), study.getUniversityName(), databaseToUse);
		
		return study;
	}
	
	private Person findPersonById(Integer personId, String database) {
		log.debug("=== FINDING PERSON BY ID ===");
		log.debug("PersonId: {}, Database: '{}'", personId, database);
		
		try {
			PersonInputPort personInputPort;
			
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
	
	private Profession findProfessionById(Integer professionId, String database) {
		log.debug("=== FINDING PROFESSION BY ID ===");
		log.debug("ProfessionId: {}, Database: '{}'", professionId, database);
		
		try {
			ProfessionInputPort professionInputPort;
			
			if ("MARIA".equalsIgnoreCase(database) || "MARIADB".equalsIgnoreCase(database)) {
				log.debug("Using MARIA database for profession lookup");
				professionInputPort = new ProfessionUseCase(professionOutputPortMaria);
			} else if ("MONGO".equalsIgnoreCase(database) || "MONGODB".equalsIgnoreCase(database)) {
				log.debug("Using MONGO database for profession lookup");
				professionInputPort = new ProfessionUseCase(professionOutputPortMongo);
			} else {
				log.warn("Unknown database '{}', defaulting to MARIA", database);
				professionInputPort = new ProfessionUseCase(professionOutputPortMaria);
			}
			
			Profession profession = professionInputPort.findOne(professionId);
			log.debug("Profession found: {} (ID: {})", 
				profession.getName(), profession.getIdentification());
			return profession;
			
		} catch (NoExistException e) {
			log.warn("Profession not found with ID: {} in database: {}", professionId, database);
			return null;
		} catch (Exception e) {
			log.error("Error finding profession with ID: {} in database: {}", professionId, database, e);
			return null;
		}
	}
}