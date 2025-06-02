package co.edu.javeriana.as.personapp.mongo.adapter;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.mongodb.MongoWriteException;

import co.edu.javeriana.as.personapp.application.port.out.StudyOutputPort;
import co.edu.javeriana.as.personapp.common.annotations.Adapter;
import co.edu.javeriana.as.personapp.domain.Study;
import co.edu.javeriana.as.personapp.mongo.document.EstudiosDocument;
import co.edu.javeriana.as.personapp.mongo.document.PersonaDocument;
import co.edu.javeriana.as.personapp.mongo.document.ProfesionDocument;
import co.edu.javeriana.as.personapp.mongo.mapper.EstudiosMapperMongo;
import co.edu.javeriana.as.personapp.mongo.repository.EstudiosRepositoryMongo;
import co.edu.javeriana.as.personapp.mongo.repository.PersonaRepositoryMongo;
import co.edu.javeriana.as.personapp.mongo.repository.ProfesionRepositoryMongo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Adapter("studyOutputAdapterMongo")
public class StudyOutputAdapterMongo implements StudyOutputPort {
	
	@Autowired
    private EstudiosRepositoryMongo estudiosRepositoryMongo;
	
	@Autowired
	private PersonaRepositoryMongo personaRepositoryMongo;
	
	@Autowired
	private ProfesionRepositoryMongo profesionRepositoryMongo;
	
	@Autowired
	private EstudiosMapperMongo estudiosMapperMongo;
	
	@Override
	public Study save(Study study) {
		log.debug("Into save on Adapter MongoDB");
		try {
			EstudiosDocument persistedEstudio = estudiosRepositoryMongo.save(estudiosMapperMongo.fromDomainToAdapter(study));
			return estudiosMapperMongo.fromAdapterToDomain(persistedEstudio);
		} catch (MongoWriteException e) {
			log.warn(e.getMessage());
			return study;
		}		
	}

	@Override
	public Boolean delete(Integer personId, Integer professionId) {
		log.debug("Into delete on Adapter MongoDB");
		String id = personId + "-" + professionId;
		estudiosRepositoryMongo.deleteById(id);
		return estudiosRepositoryMongo.findById(id).isEmpty();
	}

	@Override
	public List<Study> find() {
		log.debug("Into find on Adapter MongoDB");
		List<EstudiosDocument> estudios = estudiosRepositoryMongo.findAll();
		
		// Resolver las referencias manualmente para cada estudio
		return estudios.stream()
				.map(this::resolveReferences)
				.map(estudiosMapperMongo::fromAdapterToDomain)
				.collect(Collectors.toList());
	}

	@Override
	public Study findById(Integer personId, Integer professionId) {
		log.debug("Into findById on Adapter MongoDB");
		String id = personId + "-" + professionId;
		if (estudiosRepositoryMongo.findById(id).isEmpty()) {
			return null;
		} else {
			EstudiosDocument estudio = estudiosRepositoryMongo.findById(id).get();
			estudio = resolveReferences(estudio);
			return estudiosMapperMongo.fromAdapterToDomain(estudio);
		}
	}

	@Override
	public List<Study> findByPersonId(Integer personId) {
		log.debug("Into findByPersonId on Adapter MongoDB");
		return estudiosRepositoryMongo.findByPersonId(personId).stream()
				.map(this::resolveReferences)
				.map(estudiosMapperMongo::fromAdapterToDomain)
				.collect(Collectors.toList());
	}

	@Override
	public List<Study> findByProfessionId(Integer professionId) {
		log.debug("Into findByProfessionId on Adapter MongoDB");
		return estudiosRepositoryMongo.findByProfessionId(professionId).stream()
				.map(this::resolveReferences)
				.map(estudiosMapperMongo::fromAdapterToDomain)
				.collect(Collectors.toList());
	}
	
	/**
	 * Resuelve las referencias de persona y profesión cargando los datos completos
	 */
	private EstudiosDocument resolveReferences(EstudiosDocument estudio) {
		log.debug("Resolving references for study: {}", estudio.getId());
		
		// Resolver referencia de persona
		if (estudio.getPrimaryPersona() != null && estudio.getPrimaryPersona().getId() != null) {
			Integer personId = estudio.getPrimaryPersona().getId();
			log.debug("Resolving person reference for ID: {}", personId);
			
			PersonaDocument personaCompleta = personaRepositoryMongo.findById(personId).orElse(null);
			
			if (personaCompleta != null) {
				log.debug("Found complete person data: {} {}", personaCompleta.getNombre(), personaCompleta.getApellido());
				estudio.setPrimaryPersona(personaCompleta);
			} else {
				log.warn("Person not found for ID: {}, creating default", personId);
				PersonaDocument defaultPerson = new PersonaDocument();
				defaultPerson.setId(personId);
				defaultPerson.setNombre("Persona");
				defaultPerson.setApellido("ID: " + personId);
				defaultPerson.setGenero("M");
				estudio.setPrimaryPersona(defaultPerson);
			}
		} else {
			log.warn("EstudiosDocument has no person reference or person ID is null");
			PersonaDocument defaultPerson = new PersonaDocument();
			defaultPerson.setId(0);
			defaultPerson.setNombre("Sin");
			defaultPerson.setApellido("Persona");
			defaultPerson.setGenero("M");
			estudio.setPrimaryPersona(defaultPerson);
		}
		
		// Resolver referencia de profesión
		if (estudio.getPrimaryProfesion() != null && estudio.getPrimaryProfesion().getId() != null) {
			Integer professionId = estudio.getPrimaryProfesion().getId();
			log.debug("Resolving profession reference for ID: {}", professionId);
			
			ProfesionDocument profesionCompleta = profesionRepositoryMongo.findById(professionId).orElse(null);
			
			if (profesionCompleta != null) {
				log.debug("Found complete profession data: {}", profesionCompleta.getNom());
				estudio.setPrimaryProfesion(profesionCompleta);
			} else {
				log.warn("Profession not found for ID: {}, creating default", professionId);
				ProfesionDocument defaultProfession = new ProfesionDocument();
				defaultProfession.setId(professionId);
				defaultProfession.setNom("Profesión ID: " + professionId);
				defaultProfession.setDes("Profesión no encontrada");
				estudio.setPrimaryProfesion(defaultProfession);
			}
		} else {
			log.warn("EstudiosDocument has no profession reference or profession ID is null");
			ProfesionDocument defaultProfession = new ProfesionDocument();
			defaultProfession.setId(0);
			defaultProfession.setNom("Sin Profesión");
			defaultProfession.setDes("Profesión no especificada");
			estudio.setPrimaryProfesion(defaultProfession);
		}
		
		return estudio;
	}
}