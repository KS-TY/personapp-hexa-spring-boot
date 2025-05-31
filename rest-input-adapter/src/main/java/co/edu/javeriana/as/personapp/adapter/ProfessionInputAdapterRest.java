package co.edu.javeriana.as.personapp.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import co.edu.javeriana.as.personapp.application.port.in.ProfessionInputPort;
import co.edu.javeriana.as.personapp.application.port.out.ProfessionOutputPort;
import co.edu.javeriana.as.personapp.application.usecase.ProfessionUseCase;
import co.edu.javeriana.as.personapp.common.annotations.Adapter;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidOptionException;
import co.edu.javeriana.as.personapp.common.exceptions.NoExistException;
import co.edu.javeriana.as.personapp.common.setup.DatabaseOption;
import co.edu.javeriana.as.personapp.domain.Profession;
import co.edu.javeriana.as.personapp.mapper.ProfessionMapperRest;
import co.edu.javeriana.as.personapp.model.request.ProfessionRequest;
import co.edu.javeriana.as.personapp.model.response.ProfessionResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Adapter
public class ProfessionInputAdapterRest {

	@Autowired
	@Qualifier("professionOutputAdapterMaria")
	private ProfessionOutputPort professionOutputPortMaria;

	@Autowired
	@Qualifier("professionOutputAdapterMongo")
	private ProfessionOutputPort professionOutputPortMongo;

	@Autowired
	private ProfessionMapperRest professionMapperRest;

	ProfessionInputPort professionInputPort;

	private String setProfessionOutputPortInjection(String dbOption) throws InvalidOptionException {
		if (dbOption.equalsIgnoreCase(DatabaseOption.MARIA.toString())) {
			professionInputPort = new ProfessionUseCase(professionOutputPortMaria);
			return DatabaseOption.MARIA.toString();
		} else if (dbOption.equalsIgnoreCase(DatabaseOption.MONGO.toString())) {
			professionInputPort = new ProfessionUseCase(professionOutputPortMongo);
			return DatabaseOption.MONGO.toString();
		} else {
			throw new InvalidOptionException("Invalid database option: " + dbOption);
		}
	}

	public List<ProfessionResponse> historial(String database) {
		log.info("Into historial ProfessionEntity in Input Adapter");
		try {
			if(setProfessionOutputPortInjection(database).equalsIgnoreCase(DatabaseOption.MARIA.toString())){
				return professionInputPort.findAll().stream().map(professionMapperRest::fromDomainToAdapterRestMaria)
						.collect(Collectors.toList());
			}else {
				return professionInputPort.findAll().stream().map(professionMapperRest::fromDomainToAdapterRestMongo)
						.collect(Collectors.toList());
			}
			
		} catch (InvalidOptionException e) {
			log.warn(e.getMessage());
			return new ArrayList<ProfessionResponse>();
		}
	}

	public ProfessionResponse crearProfession(ProfessionRequest request) {
		log.info("Into crearProfession ProfessionEntity in Input Adapter");
		try {
			String database = setProfessionOutputPortInjection(request.getDatabase());
			Profession profession = professionInputPort.create(professionMapperRest.fromAdapterToDomain(request));
			
			if(database.equalsIgnoreCase(DatabaseOption.MARIA.toString())){
				return professionMapperRest.fromDomainToAdapterRestMaria(profession);
			} else {
				return professionMapperRest.fromDomainToAdapterRestMongo(profession);
			}
		} catch (InvalidOptionException e) {
			log.warn(e.getMessage());
			return new ProfessionResponse("", "", "", "", "ERROR: " + e.getMessage());
		}
	}

	public ProfessionResponse actualizarProfession(Integer identification, ProfessionRequest request) {
		log.info("Into actualizarProfession ProfessionEntity in Input Adapter - ID: {}", identification);
		try {
			String database = setProfessionOutputPortInjection(request.getDatabase());
			Profession professionToUpdate = professionMapperRest.fromAdapterToDomain(request);
			professionToUpdate.setIdentification(identification);
			
			Profession updatedProfession = professionInputPort.edit(identification, professionToUpdate);
			
			if(database.equalsIgnoreCase(DatabaseOption.MARIA.toString())){
				return professionMapperRest.fromDomainToAdapterRestMaria(updatedProfession);
			} else {
				return professionMapperRest.fromDomainToAdapterRestMongo(updatedProfession);
			}
		} catch (InvalidOptionException e) {
			log.warn("Invalid option: " + e.getMessage());
			return new ProfessionResponse("", "", "", "", "ERROR: " + e.getMessage());
		} catch (NoExistException e) {
			log.warn("Profession not found: " + e.getMessage());
			return new ProfessionResponse("", "", "", "", "ERROR: Profesión no encontrada");
		} catch (Exception e) {
			log.error("Unexpected error: " + e.getMessage(), e);
			return new ProfessionResponse("", "", "", "", "ERROR: " + e.getMessage());
		}
	}

	public ProfessionResponse eliminarProfession(Integer identification, String database) {
		log.info("=== ADAPTER DELETE START ===");
		log.info("Received identification: {}", identification);
		log.info("Received database: {}", database);
		
		if (identification == null) {
			log.error("Identification is null");
			return new ProfessionResponse("", "", "", database, "ERROR: ID no puede ser null");
		}
		
		if (identification <= 0) {
			log.error("Identification is not positive: {}", identification);
			return new ProfessionResponse(identification.toString(), "", "", database, "ERROR: ID debe ser un número positivo");
		}
		
		if (database == null || database.trim().isEmpty()) {
			log.error("Database is null or empty");
			return new ProfessionResponse(identification.toString(), "", "", "", "ERROR: Database es requerido");
		}
		
		try {
			String dbUsed = setProfessionOutputPortInjection(database);
			log.info("Database option set to: {}", dbUsed);
			
			log.info("Searching for profession with ID: {}", identification);
			Profession existingProfession;
			try {
				existingProfession = professionInputPort.findOne(identification);
				if (existingProfession == null) {
					log.warn("Profession with ID {} not found", identification);
					return new ProfessionResponse(identification.toString(), "", "", database, "ERROR: Profesión no encontrada");
				}
				log.info("Found profession: {} (ID: {})", existingProfession.getName(), existingProfession.getIdentification());
			} catch (NoExistException e) {
				log.warn("Profession with ID {} does not exist: {}", identification, e.getMessage());
				return new ProfessionResponse(identification.toString(), "", "", database, "ERROR: Profesión no encontrada");
			}
			
			log.info("Attempting to delete profession with ID: {}", identification);
			Boolean deleted;
			try {
				deleted = professionInputPort.drop(identification);
				log.info("Delete operation result: {}", deleted);
			} catch (NoExistException e) {
				log.error("Error during delete - profession not found: {}", e.getMessage());
				return new ProfessionResponse(identification.toString(), "", "", database, "ERROR: Profesión no encontrada para eliminar");
			}
			
			if (deleted != null && deleted) {
				ProfessionResponse response;
				if(dbUsed.equalsIgnoreCase(DatabaseOption.MARIA.toString())){
					response = professionMapperRest.fromDomainToAdapterRestMaria(existingProfession);
				} else {
					response = professionMapperRest.fromDomainToAdapterRestMongo(existingProfession);
				}
				response.setStatus("DELETED");
				log.info("Success response created with status: {}", response.getStatus());
				return response;
			} else {
				log.warn("Delete operation returned false for ID: {}", identification);
				return new ProfessionResponse(identification.toString(), "", "", database, "ERROR: No se pudo eliminar la profesión");
			}
			
		} catch (InvalidOptionException e) {
			log.error("Invalid database option: {}", e.getMessage());
			return new ProfessionResponse(identification.toString(), "", "", database, "ERROR: Opción de base de datos inválida: " + e.getMessage());
		} catch (Exception e) {
			log.error("Unexpected error during deletion: {}", e.getMessage(), e);
			return new ProfessionResponse(identification.toString(), "", "", database, "ERROR: Error inesperado: " + e.getMessage());
		} finally {
			log.info("=== ADAPTER DELETE END ===");
		}
	}
}