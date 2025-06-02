package co.edu.javeriana.as.personapp.mongo.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import co.edu.javeriana.as.personapp.mongo.document.EstudiosDocument;

@Repository
public interface EstudiosRepositoryMongo extends MongoRepository<EstudiosDocument, String> {
	
	@Query("{'primaryPersona.$id': ?0}")
	List<EstudiosDocument> findByPersonId(Integer personId);
	
	@Query("{'primaryProfesion.$id': ?0}")
	List<EstudiosDocument> findByProfessionId(Integer professionId);
}