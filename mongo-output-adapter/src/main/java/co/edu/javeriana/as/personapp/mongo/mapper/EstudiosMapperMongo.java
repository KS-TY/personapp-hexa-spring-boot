package co.edu.javeriana.as.personapp.mongo.mapper;

import java.time.LocalDate;

import org.springframework.beans.factory.annotation.Autowired;

import co.edu.javeriana.as.personapp.common.annotations.Mapper;
import co.edu.javeriana.as.personapp.domain.Person;
import co.edu.javeriana.as.personapp.domain.Profession;
import co.edu.javeriana.as.personapp.domain.Study;
import co.edu.javeriana.as.personapp.mongo.document.EstudiosDocument;
import co.edu.javeriana.as.personapp.mongo.document.PersonaDocument;
import co.edu.javeriana.as.personapp.mongo.document.ProfesionDocument;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Mapper
public class EstudiosMapperMongo {

	@Autowired
	private PersonaMapperMongo personaMapperMongo;

	@Autowired
	private ProfesionMapperMongo profesionMapperMongo;

	public EstudiosDocument fromDomainToAdapter(Study study) {
		log.debug("Mapping Study domain to EstudiosDocument");
		
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
		
		EstudiosDocument estudio = new EstudiosDocument();
		estudio.setId(validateId(study.getPerson().getIdentification(), study.getProfession().getIdentification()));
		estudio.setPrimaryPersona(validatePrimaryPersona(study.getPerson()));
		estudio.setPrimaryProfesion(validatePrimaryProfesion(study.getProfession()));
		estudio.setFecha(validateFecha(study.getGraduationDate()));
		estudio.setUniver(validateUniver(study.getUniversityName()));
		
		log.debug("Mapped EstudiosDocument: ID={}", estudio.getId());
		return estudio;
	}

	private String validateId(@NonNull Integer identificationPerson, @NonNull Integer identificationProfession) {
		return identificationPerson + "-" + identificationProfession;
	}

	private PersonaDocument validatePrimaryPersona(@NonNull Person person) {
		if (person == null) {
			log.warn("Person is null, creating empty PersonaDocument");
			return new PersonaDocument();
		}
		return personaMapperMongo.fromDomainToAdapter(person);
	}

	private ProfesionDocument validatePrimaryProfesion(@NonNull Profession profession) {
		if (profession == null) {
			log.warn("Profession is null, creating empty ProfesionDocument");
			return new ProfesionDocument();
		}
		return profesionMapperMongo.fromDomainToAdapter(profession);
	}

	private LocalDate validateFecha(LocalDate graduationDate) {
		return graduationDate; // Puede ser null
	}

	private String validateUniver(String universityName) {
		return universityName != null ? universityName : "";
	}

	public Study fromAdapterToDomain(EstudiosDocument estudiosDocument) {
		log.debug("Mapping EstudiosDocument to Study domain");
		
		if (estudiosDocument == null) {
			log.error("EstudiosDocument is null");
			return null;
		}
		
		// Verificar que las referencias estén resueltas
		if (estudiosDocument.getPrimaryPersona() == null) {
			log.error("EstudiosDocument primary persona is null for study: {}", estudiosDocument.getId());
			throw new IllegalStateException("EstudiosDocument must have a resolved person reference");
		}
		
		if (estudiosDocument.getPrimaryProfesion() == null) {
			log.error("EstudiosDocument primary profesion is null for study: {}", estudiosDocument.getId());
			throw new IllegalStateException("EstudiosDocument must have a resolved profession reference");
		}
		
		Study study = new Study();
		
		// Mapear persona
		Person person = personaMapperMongo.fromAdapterToDomain(estudiosDocument.getPrimaryPersona());
		if (person == null) {
			log.error("Failed to map person from EstudiosDocument");
			throw new IllegalStateException("Failed to map person from EstudiosDocument");
		}
		study.setPerson(person);
		
		// Mapear profesión
		Profession profession = profesionMapperMongo.fromAdapterToDomain(estudiosDocument.getPrimaryProfesion());
		if (profession == null) {
			log.error("Failed to map profession from EstudiosDocument");
			throw new IllegalStateException("Failed to map profession from EstudiosDocument");
		}
		study.setProfession(profession);
		
		study.setGraduationDate(validateGraduationDate(estudiosDocument.getFecha()));
		study.setUniversityName(validateUniversityName(estudiosDocument.getUniver()));
		
		log.debug("Mapped Study domain: PersonId={}, ProfessionId={}", 
			study.getPerson().getIdentification(), study.getProfession().getIdentification());
		
		return study;
	}

	private LocalDate validateGraduationDate(LocalDate fecha) {
		return fecha; // Puede ser null
	}

	private String validateUniversityName(String univer) {
		return univer != null ? univer : "";
	}
}