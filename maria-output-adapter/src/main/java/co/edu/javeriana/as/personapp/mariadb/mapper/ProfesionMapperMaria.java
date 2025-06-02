package co.edu.javeriana.as.personapp.mariadb.mapper;

import java.util.ArrayList;

import co.edu.javeriana.as.personapp.common.annotations.Mapper;
import co.edu.javeriana.as.personapp.domain.Profession;
import co.edu.javeriana.as.personapp.mariadb.entity.ProfesionEntity;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Mapper
public class ProfesionMapperMaria {

	public ProfesionEntity fromDomainToAdapter(Profession profession) {
		log.debug("Mapping Profession to ProfesionEntity: ID={}, Name={}", 
			profession.getIdentification(), profession.getName());
		
		ProfesionEntity profesionEntity = new ProfesionEntity();
		profesionEntity.setId(profession.getIdentification());
		profesionEntity.setNom(profession.getName());
		profesionEntity.setDes(validateDes(profession.getDescription()));
		
		// NO mapear estudios aquí para evitar dependencias circulares
		// Los estudios se manejan por separado
		profesionEntity.setEstudios(new ArrayList<>());
		
		log.debug("ProfesionEntity mapped: ID={}, Nom={}, Des={}", 
			profesionEntity.getId(), profesionEntity.getNom(), profesionEntity.getDes());
		
		return profesionEntity;
	}

	private String validateDes(String description) {
		return description != null ? description : "";
	}

	public Profession fromAdapterToDomain(ProfesionEntity profesionEntity) {
		log.debug("Mapping ProfesionEntity to Profession: ID={}, Nom={}", 
			profesionEntity.getId(), profesionEntity.getNom());
		
		Profession profession = new Profession();
		profession.setIdentification(profesionEntity.getId());
		profession.setName(profesionEntity.getNom());
		profession.setDescription(validateDescription(profesionEntity.getDes()));
		
		// Inicializar lista vacía para evitar null pointer
		// Los estudios se cargan por separado cuando sea necesario
		profession.setStudies(new ArrayList<>());
		
		log.debug("Profession mapped: ID={}, Name={}, Description={}", 
			profession.getIdentification(), profession.getName(), profession.getDescription());
		
		return profession;
	}

	private String validateDescription(String des) {
		return des != null ? des : "";
	}
}