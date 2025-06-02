package co.edu.javeriana.as.personapp.mariadb.mapper;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;

import co.edu.javeriana.as.personapp.common.annotations.Mapper;
import co.edu.javeriana.as.personapp.domain.Study;
import co.edu.javeriana.as.personapp.mariadb.entity.EstudiosEntity;
import co.edu.javeriana.as.personapp.mariadb.entity.EstudiosEntityPK;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Mapper
public class EstudiosMapperMaria {

	@Autowired
	private PersonaMapperMaria personaMapperMaria;

	@Autowired
	private ProfesionMapperMaria profesionMapperMaria;

	public EstudiosEntity fromDomainToAdapter(Study study) {
		log.debug("Mapping Study domain to EstudiosEntity");
		
		if (study == null) {
			log.error("Study is null");
			return null;
		}
		
		if (study.getPerson() == null || study.getPerson().getIdentification() == null) {
			log.error("Study person or person identification is null");
			throw new IllegalArgumentException("Study must have a valid person with identification");
		}
		
		if (study.getProfession() == null || study.getProfession().getIdentification() == null) {
			log.error("Study profession or profession identification is null");
			throw new IllegalArgumentException("Study must have a valid profession with identification");
		}
		
		EstudiosEntityPK estudioPK = new EstudiosEntityPK();
		estudioPK.setCcPer(study.getPerson().getIdentification());
		estudioPK.setIdProf(study.getProfession().getIdentification());
		
		EstudiosEntity estudio = new EstudiosEntity();
		estudio.setEstudiosPK(estudioPK);
		estudio.setFecha(validateFecha(study.getGraduationDate()));
		estudio.setUniver(validateUniver(study.getUniversityName()));
		
		log.debug("Mapped EstudiosEntity: PersonId={}, ProfessionId={}", 
			estudioPK.getCcPer(), estudioPK.getIdProf());
		
		return estudio;
	}

	private Date validateFecha(LocalDate graduationDate) {
		if (graduationDate != null) {
			try {
				return Date.from(graduationDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
			} catch (Exception e) {
				log.warn("Error converting LocalDate to Date: {}", e.getMessage());
				return null;
			}
		}
		return null;
	}

	private String validateUniver(String universityName) {
		return universityName != null ? universityName : "";
	}

	public Study fromAdapterToDomain(EstudiosEntity estudiosEntity) {
		log.debug("Mapping EstudiosEntity to Study domain");
		
		if (estudiosEntity == null) {
			log.error("EstudiosEntity is null");
			return null;
		}
		
		try {
			Study study = new Study();
			
			// Mapear persona y profesión
			if (estudiosEntity.getPersona() != null) {
				study.setPerson(personaMapperMaria.fromAdapterToDomain(estudiosEntity.getPersona()));
			} else {
				log.error("EstudiosEntity persona is null");
				throw new IllegalStateException("EstudiosEntity must have a persona");
			}
			
			if (estudiosEntity.getProfesion() != null) {
				study.setProfession(profesionMapperMaria.fromAdapterToDomain(estudiosEntity.getProfesion()));
			} else {
				log.error("EstudiosEntity profesion is null");
				throw new IllegalStateException("EstudiosEntity must have a profesion");
			}
			
			study.setGraduationDate(validateGraduationDate(estudiosEntity.getFecha()));
			study.setUniversityName(validateUniversityName(estudiosEntity.getUniver()));
			
			log.debug("Mapped Study domain: PersonId={}, ProfessionId={}", 
				study.getPerson().getIdentification(), study.getProfession().getIdentification());
			
			return study;
			
		} catch (Exception e) {
			log.error("Error mapping EstudiosEntity to Study domain: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to map EstudiosEntity to Study domain", e);
		}
	}

	private LocalDate validateGraduationDate(Date fecha) {
		if (fecha != null) {
			try {
				// CORRECCIÓN PRINCIPAL: Manejo mejorado de conversión de fechas
				return fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			} catch (Exception e) {
				log.warn("Error converting Date to LocalDate: {} - Error: {}", fecha, e.getMessage());
				return null;
			}
		}
		return null;
	}

	private String validateUniversityName(String univer) {
		return univer != null ? univer : "";
	}
}