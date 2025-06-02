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
import co.edu.javeriana.as.personapp.mariadb.mapper.EstudiosMapperMaria;
import co.edu.javeriana.as.personapp.mariadb.repository.EstudiosRepositoryMaria;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Adapter("studyOutputAdapterMaria")
@Transactional
public class StudyOutputAdapterMaria implements StudyOutputPort {

	@Autowired
	private EstudiosRepositoryMaria estudiosRepositoryMaria;

	@Autowired
	private EstudiosMapperMaria estudiosMapperMaria;

	@Override
	public Study save(Study study) {
		log.debug("=== MARIA ADAPTER SAVE START ===");
		log.debug("Study to save: PersonId={}, ProfessionId={}, Date={}, University={}", 
			study.getPerson().getIdentification(),
			study.getProfession().getIdentification(),
			study.getGraduationDate(),
			study.getUniversityName());
		
		try {
			EstudiosEntity estudiosEntity = estudiosMapperMaria.fromDomainToAdapter(study);
			log.debug("Mapped to EstudiosEntity: PK={}", estudiosEntity.getEstudiosPK());
			
			EstudiosEntity persistedEstudio = estudiosRepositoryMaria.save(estudiosEntity);
			log.debug("Persisted EstudiosEntity: PK={}", persistedEstudio.getEstudiosPK());
			
			Study result = estudiosMapperMaria.fromAdapterToDomain(persistedEstudio);
			log.debug("=== MARIA ADAPTER SAVE SUCCESS ===");
			return result;
		} catch (Exception e) {
			log.error("=== MARIA ADAPTER SAVE ERROR ===", e);
			throw e;
		}
	}

	@Override
	public Boolean delete(Integer personId, Integer professionId) {
		log.debug("=== MARIA ADAPTER DELETE START ===");
		log.debug("Delete request: PersonId={}, ProfessionId={}", personId, professionId);
		
		try {
			EstudiosEntityPK pk = new EstudiosEntityPK(professionId, personId);
			log.debug("Created PK for delete: {}", pk);
			
			// Verificar que existe antes de eliminar
			boolean exists = estudiosRepositoryMaria.existsById(pk);
			log.debug("Study exists before delete: {}", exists);
			
			if (exists) {
				estudiosRepositoryMaria.deleteById(pk);
				boolean stillExists = estudiosRepositoryMaria.existsById(pk);
				log.debug("Study exists after delete: {}", stillExists);
				log.debug("=== MARIA ADAPTER DELETE SUCCESS ===");
				return !stillExists;
			} else {
				log.warn("Study with PersonId={} and ProfessionId={} not found for delete", personId, professionId);
				return false;
			}
		} catch (Exception e) {
			log.error("=== MARIA ADAPTER DELETE ERROR ===", e);
			throw e;
		}
	}

	@Override
	public List<Study> find() {
		log.debug("=== MARIA ADAPTER FIND ALL START ===");
		
		try {
			List<EstudiosEntity> entities = estudiosRepositoryMaria.findAll();
			log.debug("Found {} EstudiosEntity records", entities.size());
			
			List<Study> studies = entities.stream()
				.map(entity -> {
					try {
						Study study = estudiosMapperMaria.fromAdapterToDomain(entity);
						log.debug("Mapped entity to study: PersonId={}, ProfessionId={}", 
							study.getPerson().getIdentification(),
							study.getProfession().getIdentification());
						return study;
					} catch (Exception e) {
						log.error("Error mapping entity to domain: {}", entity, e);
						return null;
					}
				})
				.filter(study -> study != null)
				.collect(Collectors.toList());
			
			log.debug("=== MARIA ADAPTER FIND ALL SUCCESS: {} studies ===", studies.size());
			return studies;
		} catch (Exception e) {
			log.error("=== MARIA ADAPTER FIND ALL ERROR ===", e);
			throw e;
		}
	}

	@Override
	public Study findById(Integer personId, Integer professionId) {
		log.debug("=== MARIA ADAPTER FIND BY ID START ===");
		log.debug("Find request: PersonId={}, ProfessionId={}", personId, professionId);
		
		try {
			EstudiosEntityPK pk = new EstudiosEntityPK(professionId, personId);
			log.debug("Created PK for find: {}", pk);
			
			if (estudiosRepositoryMaria.findById(pk).isEmpty()) {
				log.debug("Study not found with PK: {}", pk);
				return null;
			} else {
				EstudiosEntity entity = estudiosRepositoryMaria.findById(pk).get();
				Study result = estudiosMapperMaria.fromAdapterToDomain(entity);
				log.debug("=== MARIA ADAPTER FIND BY ID SUCCESS ===");
				return result;
			}
		} catch (Exception e) {
			log.error("=== MARIA ADAPTER FIND BY ID ERROR ===", e);
			throw e;
		}
	}

	@Override
	public List<Study> findByPersonId(Integer personId) {
		log.debug("=== MARIA ADAPTER FIND BY PERSON ID START ===");
		log.debug("Find by PersonId: {}", personId);
		
		try {
			List<EstudiosEntity> entities = estudiosRepositoryMaria.findByCcPer(personId);
			log.debug("Found {} entities for PersonId={}", entities.size(), personId);
			
			List<Study> studies = entities.stream()
				.map(estudiosMapperMaria::fromAdapterToDomain)
				.filter(study -> study != null)
				.collect(Collectors.toList());
			
			log.debug("=== MARIA ADAPTER FIND BY PERSON ID SUCCESS: {} studies ===", studies.size());
			return studies;
		} catch (Exception e) {
			log.error("=== MARIA ADAPTER FIND BY PERSON ID ERROR ===", e);
			throw e;
		}
	}

	@Override
	public List<Study> findByProfessionId(Integer professionId) {
		log.debug("=== MARIA ADAPTER FIND BY PROFESSION ID START ===");
		log.debug("Find by ProfessionId: {}", professionId);
		
		try {
			List<EstudiosEntity> entities = estudiosRepositoryMaria.findByIdProf(professionId);
			log.debug("Found {} entities for ProfessionId={}", entities.size(), professionId);
			
			List<Study> studies = entities.stream()
				.map(estudiosMapperMaria::fromAdapterToDomain)
				.filter(study -> study != null)
				.collect(Collectors.toList());
			
			log.debug("=== MARIA ADAPTER FIND BY PROFESSION ID SUCCESS: {} studies ===", studies.size());
			return studies;
		} catch (Exception e) {
			log.error("=== MARIA ADAPTER FIND BY PROFESSION ID ERROR ===", e);
			throw e;
		}
	}
}