package co.edu.javeriana.as.personapp.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import co.edu.javeriana.as.personapp.application.port.in.StudyInputPort;
import co.edu.javeriana.as.personapp.application.port.out.StudyOutputPort;
import co.edu.javeriana.as.personapp.application.usecase.StudyUseCase;
import co.edu.javeriana.as.personapp.common.annotations.Adapter;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidOptionException;
import co.edu.javeriana.as.personapp.common.exceptions.NoExistException;
import co.edu.javeriana.as.personapp.common.setup.DatabaseOption;
import co.edu.javeriana.as.personapp.domain.Study;
import co.edu.javeriana.as.personapp.mapper.StudyMapperRest;
import co.edu.javeriana.as.personapp.model.request.StudyRequest;
import co.edu.javeriana.as.personapp.model.response.StudyResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Adapter
public class StudyInputAdapterRest {

	@Autowired
	@Qualifier("studyOutputAdapterMaria")
	private StudyOutputPort studyOutputPortMaria;

	@Autowired
	@Qualifier("studyOutputAdapterMongo")
	private StudyOutputPort studyOutputPortMongo;

	@Autowired
	private StudyMapperRest studyMapperRest;

	StudyInputPort studyInputPort;

	private String setStudyOutputPortInjection(String dbOption) throws InvalidOptionException {
		if (dbOption.equalsIgnoreCase(DatabaseOption.MARIA.toString())) {
			studyInputPort = new StudyUseCase(studyOutputPortMaria);
			return DatabaseOption.MARIA.toString();
		} else if (dbOption.equalsIgnoreCase(DatabaseOption.MONGO.toString())) {
			studyInputPort = new StudyUseCase(studyOutputPortMongo);
			return DatabaseOption.MONGO.toString();
		} else {
			throw new InvalidOptionException("Invalid database option: " + dbOption);
		}
	}

	public List<StudyResponse> historial(String database) {
		log.info("Into historial StudyEntity in Input Adapter");
		try {
			if(setStudyOutputPortInjection(database).equalsIgnoreCase(DatabaseOption.MARIA.toString())){
				return studyInputPort.findAll().stream().map(studyMapperRest::fromDomainToAdapterRestMaria)
						.collect(Collectors.toList());
			}else {
				return studyInputPort.findAll().stream().map(studyMapperRest::fromDomainToAdapterRestMongo)
						.collect(Collectors.toList());
			}
			
		} catch (InvalidOptionException e) {
			log.warn(e.getMessage());
			return new ArrayList<StudyResponse>();
		}
	}

	public StudyResponse crearStudy(StudyRequest request) {
		log.info("Into crearStudy StudyEntity in Input Adapter");
		try {
			String database = setStudyOutputPortInjection(request.getDatabase());
			Study study = studyInputPort.create(studyMapperRest.fromAdapterToDomain(request));
			
			if(database.equalsIgnoreCase(DatabaseOption.MARIA.toString())){
				return studyMapperRest.fromDomainToAdapterRestMaria(study);
			} else {
				return studyMapperRest.fromDomainToAdapterRestMongo(study);
			}
		} catch (InvalidOptionException e) {
			log.warn(e.getMessage());
			return new StudyResponse("", "", "", "", "", "ERROR: " + e.getMessage());
		}
	}

	public StudyResponse actualizarStudy(Integer personId, Integer professionId, StudyRequest request) {
		log.info("Into actualizarStudy StudyEntity in Input Adapter - PersonId: {}, ProfessionId: {}", personId, professionId);
		try {
			String database = setStudyOutputPortInjection(request.getDatabase());
			Study studyToUpdate = studyMapperRest.fromAdapterToDomain(request);
			
			Study updatedStudy = studyInputPort.edit(personId, professionId, studyToUpdate);
			
			if(database.equalsIgnoreCase(DatabaseOption.MARIA.toString())){
				return studyMapperRest.fromDomainToAdapterRestMaria(updatedStudy);
			} else {
				return studyMapperRest.fromDomainToAdapterRestMongo(updatedStudy);
			}
		} catch (InvalidOptionException e) {
			log.warn("Invalid option: " + e.getMessage());
			return new StudyResponse("", "", "", "", "", "ERROR: " + e.getMessage());
		} catch (NoExistException e) {
			log.warn("Study not found: " + e.getMessage());
			return new StudyResponse("", "", "", "", "", "ERROR: Estudio no encontrado");
		} catch (Exception e) {
			log.error("Unexpected error: " + e.getMessage(), e);
			return new StudyResponse("", "", "", "", "", "ERROR: " + e.getMessage());
		}
	}

	public StudyResponse eliminarStudy(Integer personId, Integer professionId, String database) {
		log.info("=== ADAPTER DELETE START ===");
		log.info("Received personId: {}, professionId: {}", personId, professionId);
		log.info("Received database: {}", database);
		
		if (personId == null) {
			log.error("PersonId is null");
			return new StudyResponse("", "", "", "", database, "ERROR: PersonId no puede ser null");
		}
		
		if (professionId == null) {
			log.error("ProfessionId is null");
			return new StudyResponse("", "", "", "", database, "ERROR: ProfessionId no puede ser null");
		}
		
		if (database == null || database.trim().isEmpty()) {
			log.error("Database is null or empty");
			return new StudyResponse("", "", "", "", "", "ERROR: Database es requerido");
		}
		
		try {
			String dbUsed = setStudyOutputPortInjection(database);
			log.info("Database option set to: {}", dbUsed);
			
			log.info("Searching for study with PersonId: {} and ProfessionId: {}", personId, professionId);
			Study existingStudy;
			try {
				existingStudy = studyInputPort.findOne(personId, professionId);
				if (existingStudy == null) {
					log.warn("Study with PersonId {} and ProfessionId {} not found", personId, professionId);
					return new StudyResponse("", "", "", "", database, "ERROR: Estudio no encontrado");
				}
				log.info("Found study: Person {} - Profession {}", 
					existingStudy.getPerson().getIdentification(), 
					existingStudy.getProfession().getIdentification());
			} catch (NoExistException e) {
				log.warn("Study with PersonId {} and ProfessionId {} does not exist: {}", personId, professionId, e.getMessage());
				return new StudyResponse("", "", "", "", database, "ERROR: Estudio no encontrado");
			}
			
			log.info("Attempting to delete study with PersonId: {} and ProfessionId: {}", personId, professionId);
			Boolean deleted;
			try {
				deleted = studyInputPort.drop(personId, professionId);
				log.info("Delete operation result: {}", deleted);
			} catch (NoExistException e) {
				log.error("Error during delete - study not found: {}", e.getMessage());
				return new StudyResponse("", "", "", "", database, "ERROR: Estudio no encontrado para eliminar");
			}
			
			if (deleted != null && deleted) {
				StudyResponse response;
				if(dbUsed.equalsIgnoreCase(DatabaseOption.MARIA.toString())){
					response = studyMapperRest.fromDomainToAdapterRestMaria(existingStudy);
				} else {
					response = studyMapperRest.fromDomainToAdapterRestMongo(existingStudy);
				}
				response.setStatus("DELETED");
				log.info("Success response created with status: {}", response.getStatus());
				return response;
			} else {
				log.warn("Delete operation returned false for PersonId: {} and ProfessionId: {}", personId, professionId);
				return new StudyResponse("", "", "", "", database, "ERROR: No se pudo eliminar el estudio");
			}
			
		} catch (InvalidOptionException e) {
			log.error("Invalid database option: {}", e.getMessage());
			return new StudyResponse("", "", "", "", database, "ERROR: Opción de base de datos inválida: " + e.getMessage());
		} catch (Exception e) {
			log.error("Unexpected error during deletion: {}", e.getMessage(), e);
			return new StudyResponse("", "", "", "", database, "ERROR: Error inesperado: " + e.getMessage());
		} finally {
			log.info("=== ADAPTER DELETE END ===");
		}
	}

	public List<StudyResponse> getStudiesByPerson(Integer personId, String database) {
		log.info("Into getStudiesByPerson - PersonId: {}, Database: {}", personId, database);
		try {
			String dbUsed = setStudyOutputPortInjection(database);
			List<Study> studies = studyInputPort.findByPersonId(personId);

			if (dbUsed.equalsIgnoreCase(DatabaseOption.MARIA.toString())) {
				return studies.stream().map(studyMapperRest::fromDomainToAdapterRestMaria)
						.collect(Collectors.toList());
			} else {
				return studies.stream().map(studyMapperRest::fromDomainToAdapterRestMongo)
						.collect(Collectors.toList());
			}

		} catch (InvalidOptionException e) {
			log.warn(e.getMessage());
			return new ArrayList<StudyResponse>();
		} catch (NoExistException e) {
			log.warn("No studies found for person: " + e.getMessage());
			return new ArrayList<StudyResponse>();
		}
	}

	public List<StudyResponse> getStudiesByProfession(Integer professionId, String database) {
		log.info("Into getStudiesByProfession - ProfessionId: {}, Database: {}", professionId, database);
		try {
			String dbUsed = setStudyOutputPortInjection(database);
			List<Study> studies = studyInputPort.findByProfessionId(professionId);

			if (dbUsed.equalsIgnoreCase(DatabaseOption.MARIA.toString())) {
				return studies.stream().map(studyMapperRest::fromDomainToAdapterRestMaria)
						.collect(Collectors.toList());
			} else {
				return studies.stream().map(studyMapperRest::fromDomainToAdapterRestMongo)
						.collect(Collectors.toList());
			}

		} catch (InvalidOptionException e) {
			log.warn(e.getMessage());
			return new ArrayList<StudyResponse>();
		} catch (NoExistException e) {
			log.warn("No studies found for profession: " + e.getMessage());
			return new ArrayList<StudyResponse>();
		}
	}
}