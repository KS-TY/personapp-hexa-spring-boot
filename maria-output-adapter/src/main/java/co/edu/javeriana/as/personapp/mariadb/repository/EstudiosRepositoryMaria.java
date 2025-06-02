package co.edu.javeriana.as.personapp.mariadb.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import co.edu.javeriana.as.personapp.mariadb.entity.EstudiosEntity;
import co.edu.javeriana.as.personapp.mariadb.entity.EstudiosEntityPK;

@Repository
public interface EstudiosRepositoryMaria extends JpaRepository<EstudiosEntity, EstudiosEntityPK> {
	
	// Consultas personalizadas para buscar por persona
	@Query("SELECT e FROM EstudiosEntity e WHERE e.estudiosEntityPK.ccPer = :ccPer")
	List<EstudiosEntity> findByCcPer(@Param("ccPer") Integer ccPer);
	
	// Consultas personalizadas para buscar por profesión
	@Query("SELECT e FROM EstudiosEntity e WHERE e.estudiosEntityPK.idProf = :idProf")
	List<EstudiosEntity> findByIdProf(@Param("idProf") Integer idProf);
	
	// Método adicional para verificar existencia
	@Query("SELECT COUNT(e) > 0 FROM EstudiosEntity e WHERE e.estudiosEntityPK.ccPer = :ccPer AND e.estudiosEntityPK.idProf = :idProf")
	boolean existsByPersonIdAndProfessionId(@Param("ccPer") Integer ccPer, @Param("idProf") Integer idProf);
}