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
		log.debug("=== MARIA MAPPER FROM DOMAIN TO ADAPTER ===");
		log.debug("Study input: PersonId={}, ProfessionId={}, Date={}, University={}", 
			study.getPerson().getIdentification(),
			study.getProfession().getIdentification(),
			study.getGraduationDate(),
			study.getUniversityName());
		
		EstudiosEntityPK estudioPK = new EstudiosEntityPK();
		estudioPK.setCcPer(study.getPerson().getIdentification());
		estudioPK.setIdProf(study.getProfession().getIdentification());
		
		EstudiosEntity estudio = new EstudiosEntity();
		estudio.setEstudiosPK(estudioPK);
		estudio.setFecha(validateFecha(study.getGraduationDate()));
		estudio.setUniver(validateUniver(study.getUniversityName()));
		
		// Importante: Establecer las relaciones para que JPA funcione correctamente
		estudio.setPersona(personaMapperMaria.fromDomainToAdapter(study.getPerson()));
		estudio.setProfesion(profesionMapperMaria.fromDomainToAdapter(study.getProfession()));
		
		log.debug("EstudiosEntity created: PK={}, Fecha={}, Univer={}", 
			estudio.getEstudiosPK(), estudio.getFecha(), estudio.getUniver());
		
		return estudio;
	}

	private Date validateFecha(LocalDate graduationDate) {
		if (graduationDate != null) {
			Date result = Date.from(graduationDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
			log.debug("Converted LocalDate {} to Date {}", graduationDate, result);
			return result;
		}
		log.debug("GraduationDate is null, returning null");
		return null;
	}

	private String validateUniver(String universityName) {
		String result = universityName != null ? universityName : "";
		log.debug("University name validated: '{}'", result);
		return result;
	}

	public Study fromAdapterToDomain(EstudiosEntity estudiosEntity) {
		log.debug("=== MARIA MAPPER FROM ADAPTER TO DOMAIN ===");
		log.debug("EstudiosEntity input: PK={}, Fecha={}, Univer={}", 
			estudiosEntity.getEstudiosPK(), estudiosEntity.getFecha(), estudiosEntity.getUniver());
		
		if (estudiosEntity == null) {
			log.error("EstudiosEntity is null");
			return null;
		}
		
		if (estudiosEntity.getEstudiosPK() == null) {
			log.error("EstudiosEntity PK is null");
			return null;
		}
		
		if (estudiosEntity.getPersona() == null) {
			log.error("EstudiosEntity.persona is null");
			return null;
		}
		
		if (estudiosEntity.getProfesion() == null) {
			log.error("EstudiosEntity.profesion is null");
			return null;
		}
		
		Study study = new Study();
		study.setPerson(personaMapperMaria.fromAdapterToDomain(estudiosEntity.getPersona()));
		study.setProfession(profesionMapperMaria.fromAdapterToDomain(estudiosEntity.getProfesion()));
		study.setGraduationDate(validateGraduationDate(estudiosEntity.getFecha()));
		study.setUniversityName(validateUniversityName(estudiosEntity.getUniver()));
		
		log.debug("Study domain created: PersonId={}, ProfessionId={}, Date={}, University={}", 
			study.getPerson().getIdentification(),
			study.getProfession().getIdentification(),
			study.getGraduationDate(),
			study.getUniversityName());
		
		return study;
	}

	private LocalDate validateGraduationDate(Date fecha) {
		if (fecha != null) {
			LocalDate result = fecha.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
			log.debug("Converted Date {} to LocalDate {}", fecha, result);
			return result;
		}
		log.debug("Date is null, returning null LocalDate");
		return null;
	}

	private String validateUniversityName(String univer) {
		String result = univer != null ? univer : "";
		log.debug("University name from DB: '{}'", result);
		return result;
	}
}