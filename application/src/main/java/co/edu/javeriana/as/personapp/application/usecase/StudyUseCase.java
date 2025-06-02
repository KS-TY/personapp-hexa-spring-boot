package co.edu.javeriana.as.personapp.application.usecase;

import java.util.List;

import org.springframework.beans.factory.annotation.Qualifier;

import co.edu.javeriana.as.personapp.application.port.in.StudyInputPort;
import co.edu.javeriana.as.personapp.application.port.out.StudyOutputPort;
import co.edu.javeriana.as.personapp.common.annotations.UseCase;
import co.edu.javeriana.as.personapp.common.exceptions.NoExistException;
import co.edu.javeriana.as.personapp.domain.Study;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UseCase
public class StudyUseCase implements StudyInputPort {

	private StudyOutputPort studyPersistence;
	
	public StudyUseCase(@Qualifier("studyOutputAdapterMaria") StudyOutputPort studyPersistence) {
		this.studyPersistence = studyPersistence;
	}
	
	@Override
	public void setPersistence(StudyOutputPort studyPersistence) {
		this.studyPersistence = studyPersistence;
	}

	@Override
	public Study create(Study study) {
		log.debug("Into create on Application Domain");
		return studyPersistence.save(study);
	}

	@Override
	public Study edit(Integer personId, Integer professionId, Study study) throws NoExistException {
		Study oldStudy = studyPersistence.findById(personId, professionId);
		if (oldStudy != null)
			return studyPersistence.save(study);
		throw new NoExistException(
				"The study with personId " + personId + " and professionId " + professionId + " does not exist into db, cannot be edited");
	}

	@Override
	public Boolean drop(Integer personId, Integer professionId) throws NoExistException {
		Study oldStudy = studyPersistence.findById(personId, professionId);
		if (oldStudy != null)
			return studyPersistence.delete(personId, professionId);
		throw new NoExistException(
				"The study with personId " + personId + " and professionId " + professionId + " does not exist into db, cannot be dropped");
	}

	@Override
	public List<Study> findAll() {
		log.info("Output: " + studyPersistence.getClass());
		return studyPersistence.find();
	}

	@Override
	public Study findOne(Integer personId, Integer professionId) throws NoExistException {
		Study oldStudy = studyPersistence.findById(personId, professionId);
		if (oldStudy != null)
			return oldStudy;
		throw new NoExistException("The study with personId " + personId + " and professionId " + professionId + " does not exist into db, cannot be found");
	}

	@Override
	public Integer count() {
		return findAll().size();
	}

	@Override
	public List<Study> findByPersonId(Integer personId) throws NoExistException {
		List<Study> studies = studyPersistence.findByPersonId(personId);
		if (studies != null && !studies.isEmpty())
			return studies;
		throw new NoExistException("No studies found for person with id " + personId);
	}

	@Override
	public List<Study> findByProfessionId(Integer professionId) throws NoExistException {
		List<Study> studies = studyPersistence.findByProfessionId(professionId);
		if (studies != null && !studies.isEmpty())
			return studies;
		throw new NoExistException("No studies found for profession with id " + professionId);
	}
}