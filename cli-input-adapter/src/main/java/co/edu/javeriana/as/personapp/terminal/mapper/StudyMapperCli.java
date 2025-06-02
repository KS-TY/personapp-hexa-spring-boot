package co.edu.javeriana.as.personapp.terminal.mapper;

import co.edu.javeriana.as.personapp.common.annotations.Mapper;
import co.edu.javeriana.as.personapp.domain.Study;
import co.edu.javeriana.as.personapp.terminal.model.StudyModelCli;

@Mapper
public class StudyMapperCli {

	public StudyModelCli fromDomainToAdapterCli(Study study) {
		StudyModelCli studyModelCli = new StudyModelCli();
		studyModelCli.setPersonId(study.getPerson().getIdentification());
		studyModelCli.setPersonName(study.getPerson().getFirstName() + " " + study.getPerson().getLastName());
		studyModelCli.setProfessionId(study.getProfession().getIdentification());
		studyModelCli.setProfessionName(study.getProfession().getName());
		studyModelCli.setGraduationDate(study.getGraduationDate() != null ? study.getGraduationDate().toString() : "No especificada");
		studyModelCli.setUniversityName(study.getUniversityName() != null ? study.getUniversityName() : "No especificada");
		return studyModelCli;
	}
}