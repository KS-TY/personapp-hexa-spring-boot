package co.edu.javeriana.as.personapp.mongo.adapter;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.mongodb.MongoWriteException;

import co.edu.javeriana.as.personapp.application.port.out.StudyOutputPort;
import co.edu.javeriana.as.personapp.common.annotations.Adapter;
import co.edu.javeriana.as.personapp.domain.Study;
import co.edu.javeriana.as.personapp.mongo.document.EstudiosDocument;
import co.edu.javeriana.as.personapp.mongo.mapper.EstudiosMapperMongo;
import co.edu.javeriana.as.personapp.mongo.repository.EstudiosRepositoryMongo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Adapter("studyOutputAdapterMongo")
public class StudyOutputAdapterMongo implements StudyOutputPort {
	
	@Autowired
    private EstudiosRepositoryMongo estudiosRepositoryMongo;
	
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
		return estudiosRepositoryMongo.findAll().stream().map(estudiosMapperMongo::fromAdapterToDomain)
				.collect(Collectors.toList());
	}

	@Override
	public Study findById(Integer personId, Integer professionId) {
		log.debug("Into findById on Adapter MongoDB");
		String id = personId + "-" + professionId;
		if (estudiosRepositoryMongo.findById(id).isEmpty()) {
			return null;
		} else {
			return estudiosMapperMongo.fromAdapterToDomain(estudiosRepositoryMongo.findById(id).get());
		}
	}

	@Override
	public List<Study> findByPersonId(Integer personId) {
		log.debug("Into findByPersonId on Adapter MongoDB");
		return estudiosRepositoryMongo.findByPersonId(personId).stream()
				.map(estudiosMapperMongo::fromAdapterToDomain)
				.collect(Collectors.toList());
	}

	@Override
	public List<Study> findByProfessionId(Integer professionId) {
		log.debug("Into findByProfessionId on Adapter MongoDB");
		return estudiosRepositoryMongo.findByProfessionId(professionId).stream()
				.map(estudiosMapperMongo::fromAdapterToDomain)
				.collect(Collectors.toList());
	}
}