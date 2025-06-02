package co.edu.javeriana.as.personapp.mariadb.adapter;

import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;

import co.edu.javeriana.as.personapp.application.port.out.StudyOutputPort;
import co.edu.javeriana.as.personapp.common.annotations.Adapter;
import co.edu.javeriana.as.personapp.domain.Study;
import co.edu.javeriana.as.personapp.mariadb.entity.EstudiosEntity;
import co.edu.javeriana.as.personapp.mariadb.entity.EstudiosEntityPK;
import co.edu.javeriana.as.personapp.mariadb.entity.PersonaEntity;
import co.edu.javeriana.as.personapp.mariadb.entity.ProfesionEntity;
import co.edu.javeriana.as.personapp.mariadb.mapper.EstudiosMapperMaria;
import co.edu.javeriana.as.personapp.mariadb.repository.EstudiosRepositoryMaria;
import co.edu.javeriana.as.personapp.mariadb.repository.PersonaRepositoryMaria;
import co.edu.javeriana.as.personapp.mariadb.repository.ProfesionRepositoryMaria;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Adapter("studyOutputAdapterMaria")
@Transactional
public class StudyOutputAdapterMaria implements StudyOutputPort {

	@Autowired
	private EstudiosRepositoryMaria estudiosRepositoryMaria;

	@Autowired
	private PersonaRepositoryMaria personaRepositoryMaria;

	@Autowired
	private ProfesionRepositoryMaria profesionRepositoryMaria;

	@Autowired
	private EstudiosMapperMaria estudiosMapperMaria;

	@Override
	public Study save(Study study) {
		log.debug("Into save on Adapter MariaDB");
		try {
			// CORRECCIÓN: Asegurar que las entidades relacionadas existen y están cargadas
			EstudiosEntity estudiosEntity = estudiosMapperMaria.fromDomainToAdapter(study);
			
			// Cargar entidades relacionadas completas
			Integer personId = study.getPerson().getIdentification();
			Integer professionId = study.getProfession().getIdentification();
			
			PersonaEntity persona = personaRepositoryMaria.findById(personId).orElse(null);
			ProfesionEntity profesion = profesionRepositoryMaria.findById(professionId).orElse(null);
			
			if (persona == null) {
				throw new IllegalArgumentException("Person with ID " + personId + " not found in MariaDB");
			}
			
			if (profesion == null) {
				throw new IllegalArgumentException("Profession with ID " + professionId + " not found in MariaDB");
			}
			
			// Establecer las entidades relacionadas completas
			estudiosEntity.setPersona(persona);
			estudiosEntity.setProfesion(profesion);
			
			EstudiosEntity persistedEstudio = estudiosRepositoryMaria.save(estudiosEntity);
			
			log.debug("Successfully saved study: PersonId={}, ProfessionId={}", personId, professionId);
			
			return estudiosMapperMaria.fromAdapterToDomain(persistedEstudio);
			
		} catch (Exception e) {
			log.error("Error saving study: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to save study", e);
		}
	}

	@Override
	public Boolean delete(Integer personId, Integer professionId) {
		log.debug("Into delete on Adapter MariaDB - PersonId: {}, ProfessionId: {}", personId, professionId);
		try {
			EstudiosEntityPK pk = new EstudiosEntityPK(professionId, personId);
			
			// Verificar que existe antes de eliminar
			if (!estudiosRepositoryMaria.existsById(pk)) {
				log.warn("Study not found for deletion: PersonId={}, ProfessionId={}", personId, professionId);
				return false;
			}
			
			estudiosRepositoryMaria.deleteById(pk);
			
			// Verificar que se eliminó correctamente
			boolean stillExists = estudiosRepositoryMaria.existsById(pk);
			log.debug("Delete result - Still exists: {}", stillExists);
			
			return !stillExists;
			
		} catch (Exception e) {
			log.error("Error deleting study: PersonId={}, ProfessionId={}, Error: {}", 
				personId, professionId, e.getMessage(), e);
			throw new RuntimeException("Failed to delete study", e);
		}
	}

	@Override
	public List<Study> find() {
		log.debug("Into find on Adapter MariaDB");
		try {
			List<EstudiosEntity> estudiosEntities = estudiosRepositoryMaria.findAll();
			log.debug("Found {} EstudiosEntity records", estudiosEntities.size());
			
			return estudiosEntities.stream()
					.map(this::safeMapToDomain)
					.filter(study -> study != null) // Filtrar estudios que no se pudieron mapear
					.collect(Collectors.toList());
					
		} catch (Exception e) {
			log.error("Error finding all studies: {}", e.getMessage(), e);
			throw new RuntimeException("Failed to find studies", e);
		}
	}

	@Override
	public Study findById(Integer personId, Integer professionId) {
		log.debug("Into findById on Adapter MariaDB - PersonId: {}, ProfessionId: {}", personId, professionId);
		try {
			EstudiosEntityPK pk = new EstudiosEntityPK(professionId, personId);
			return estudiosRepositoryMaria.findById(pk)
					.map(this::safeMapToDomain)
					.orElse(null);
					
		} catch (Exception e) {
			log.error("Error finding study by ID: PersonId={}, ProfessionId={}, Error: {}", 
				personId, professionId, e.getMessage(), e);
			throw new RuntimeException("Failed to find study by ID", e);
		}
	}

	@Override
	public List<Study> findByPersonId(Integer personId) {
		log.debug("Into findByPersonId on Adapter MariaDB - PersonId: {}", personId);
		try {
			return estudiosRepositoryMaria.findByCcPer(personId).stream()
					.map(this::safeMapToDomain)
					.filter(study -> study != null)
					.collect(Collectors.toList());
		} catch (Exception e) {
			log.error("Error finding studies by PersonId={}, Error: {}", personId, e.getMessage(), e);
			throw new RuntimeException("Failed to find studies by person ID", e);
		}
	}

	@Override
	public List<Study> findByProfessionId(Integer professionId) {
		log.debug("Into findByProfessionId on Adapter MariaDB - ProfessionId: {}", professionId);
		try {
			return estudiosRepositoryMaria.findByIdProf(professionId).stream()
					.map(this::safeMapToDomain)
					.filter(study -> study != null)
					.collect(Collectors.toList());
		} catch (Exception e) {
			log.error("Error finding studies by ProfessionId={}, Error: {}", professionId, e.getMessage(), e);
			throw new RuntimeException("Failed to find studies by profession ID", e);
		}
	}

	private Study safeMapToDomain(EstudiosEntity entity) {
		try {
			log.debug("Mapping entity to domain: PersonId={}, ProfessionId={}", 
				entity.getEstudiosPK().getCcPer(), entity.getEstudiosPK().getIdProf());
			return estudiosMapperMaria.fromAdapterToDomain(entity);
		} catch (Exception e) {
			log.error("Error mapping entity to domain: PersonId={}, ProfessionId={} - Error: {}", 
				entity.getEstudiosPK().getCcPer(), entity.getEstudiosPK().getIdProf(), e.getMessage(), e);
			return null; 
		}
	}
}