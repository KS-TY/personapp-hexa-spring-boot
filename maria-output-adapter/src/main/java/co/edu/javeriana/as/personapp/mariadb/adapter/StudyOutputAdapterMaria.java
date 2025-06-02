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
		log.debug("Into save on Adapter MariaDB");
		EstudiosEntity persistedEstudio = estudiosRepositoryMaria.save(estudiosMapperMaria.fromDomainToAdapter(study));
		return estudiosMapperMaria.fromAdapterToDomain(persistedEstudio);
	}

	@Override
	public Boolean delete(Integer personId, Integer professionId) {
		log.debug("Into delete on Adapter MariaDB");
		EstudiosEntityPK pk = new EstudiosEntityPK(professionId, personId);
		estudiosRepositoryMaria.deleteById(pk);
		return estudiosRepositoryMaria.findById(pk).isEmpty();
	}

	@Override
	public List<Study> find() {
		log.debug("Into find on Adapter MariaDB");
		return estudiosRepositoryMaria.findAll().stream().map(estudiosMapperMaria::fromAdapterToDomain)
				.collect(Collectors.toList());
	}

	@Override
	public Study findById(Integer personId, Integer professionId) {
		log.debug("Into findById on Adapter MariaDB");
		EstudiosEntityPK pk = new EstudiosEntityPK(professionId, personId);
		if (estudiosRepositoryMaria.findById(pk).isEmpty()) {
			return null;
		} else {
			return estudiosMapperMaria.fromAdapterToDomain(estudiosRepositoryMaria.findById(pk).get());
		}
	}

	@Override
	public List<Study> findByPersonId(Integer personId) {
		log.debug("Into findByPersonId on Adapter MariaDB");
		return estudiosRepositoryMaria.findByCcPer(personId).stream()
				.map(estudiosMapperMaria::fromAdapterToDomain)
				.collect(Collectors.toList());
	}

	@Override
	public List<Study> findByProfessionId(Integer professionId) {
		log.debug("Into findByProfessionId on Adapter MariaDB");
		return estudiosRepositoryMaria.findByIdProf(professionId).stream()
				.map(estudiosMapperMaria::fromAdapterToDomain)
				.collect(Collectors.toList());
	}
}