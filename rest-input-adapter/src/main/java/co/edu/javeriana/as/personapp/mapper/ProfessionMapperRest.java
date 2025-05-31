package co.edu.javeriana.as.personapp.mapper;

import co.edu.javeriana.as.personapp.common.annotations.Mapper;
import co.edu.javeriana.as.personapp.domain.Profession;
import co.edu.javeriana.as.personapp.model.request.ProfessionRequest;
import co.edu.javeriana.as.personapp.model.response.ProfessionResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Mapper
public class ProfessionMapperRest {
	
	public ProfessionResponse fromDomainToAdapterRestMaria(Profession profession) {
		return fromDomainToAdapterRest(profession, "MariaDB");
	}
	
	public ProfessionResponse fromDomainToAdapterRestMongo(Profession profession) {
		return fromDomainToAdapterRest(profession, "MongoDB");
	}
	
	public ProfessionResponse fromDomainToAdapterRest(Profession profession, String database) {
		log.debug("Mapping profession to response: ID={}, Name={}, Database={}", 
			profession.getIdentification(), profession.getName(), database);
		
		// Validar que la profesión no sea null
		if (profession == null) {
			log.error("Profession object is null");
			return new ProfessionResponse("", "", "", database, "ERROR: Profession is null");
		}
		
		// Validar y obtener ID
		String professionId = "";
		if (profession.getIdentification() != null) {
			professionId = profession.getIdentification().toString();
		} else {
			log.warn("Profession has null identification: {}", profession);
			professionId = "";
		}
		
		String name = profession.getName() != null ? profession.getName() : "";
		String description = profession.getDescription() != null ? profession.getDescription() : "";
		
		log.debug("Mapped values: ID={}, Name={}, Description={}", 
			professionId, name, description);
		
		ProfessionResponse response = new ProfessionResponse(professionId, name, description, database, "OK");
		
		return response;
	}

	public Profession fromAdapterToDomain(ProfessionRequest request) {
		log.debug("Mapping request to domain: ID={}, Name={}", 
			request.getId(), request.getName());
		
		if (request == null) {
			throw new IllegalArgumentException("ProfessionRequest no puede ser null");
		}
		
		Profession profession = new Profession();
		
		// Validar y convertir ID
		Integer identification = null;
		try {
			if (request.getId() != null && !request.getId().trim().isEmpty()) {
				String idStr = request.getId().trim();
				if (!"null".equalsIgnoreCase(idStr) && !"undefined".equalsIgnoreCase(idStr)) {
					identification = Integer.valueOf(idStr);
				}
			}
		} catch (NumberFormatException e) {
			log.warn("Invalid ID format: {}", request.getId());
			throw new IllegalArgumentException("ID debe ser un número válido: " + request.getId());
		}
		
		if (identification == null || identification <= 0) {
			throw new IllegalArgumentException("ID es obligatorio y debe ser un número positivo");
		}
		
		profession.setIdentification(identification);
		profession.setName(request.getName() != null ? request.getName().trim() : "");
		profession.setDescription(request.getDescription() != null ? request.getDescription().trim() : "");
		
		log.debug("Mapped profession domain: ID={}, Name={}, Description={}", 
			profession.getIdentification(), profession.getName(), profession.getDescription());
		
		return profession;
	}
}