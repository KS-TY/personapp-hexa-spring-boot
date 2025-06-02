package co.edu.javeriana.as.personapp.mongo.mapper;

import java.util.ArrayList;

import co.edu.javeriana.as.personapp.common.annotations.Mapper;
import co.edu.javeriana.as.personapp.domain.Profession;
import co.edu.javeriana.as.personapp.mongo.document.ProfesionDocument;

@Mapper
public class ProfesionMapperMongo {

	public ProfesionDocument fromDomainToAdapter(Profession profession) {
		ProfesionDocument profesionDocument = new ProfesionDocument();
		profesionDocument.setId(profession.getIdentification());
		profesionDocument.setNom(profession.getName());
		profesionDocument.setDes(validateDes(profession.getDescription()));
		// No mapear estudios para evitar dependencias circulares
		return profesionDocument;
	}

	private String validateDes(String description) {
		return description != null ? description : "";
	}

	public Profession fromAdapterToDomain(ProfesionDocument profesionDocument) {
		Profession profession = new Profession();
		profession.setIdentification(profesionDocument.getId());
		profession.setName(profesionDocument.getNom());
		profession.setDescription(validateDescription(profesionDocument.getDes()));
		// Inicializar lista vacía para evitar null pointer
		profession.setStudies(new ArrayList<>());
		return profession;
	}

	private String validateDescription(String des) {
		return des != null ? des : "";
	}
}