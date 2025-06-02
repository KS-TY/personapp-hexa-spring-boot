package co.edu.javeriana.as.personapp.mongo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import co.edu.javeriana.as.personapp.mongo.document.EstudiosDocument;

@Repository
public interface EstudiosRepositoryMongo extends MongoRepository<EstudiosDocument, String> {
	
	// Buscar estudios por ID de persona usando la referencia DBRef
	@Query("{'primaryPersona.$id': ?0}")
	List<EstudiosDocument> findByPersonId(Integer personId);
	
	// Buscar estudios por ID de profesión usando la referencia DBRef
	@Query("{'primaryProfesion.$id': ?0}")
	List<EstudiosDocument> findByProfessionId(Integer professionId);
}