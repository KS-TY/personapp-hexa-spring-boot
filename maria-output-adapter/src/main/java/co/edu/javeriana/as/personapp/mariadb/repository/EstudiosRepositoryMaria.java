package co.edu.javeriana.as.personapp.mariadb.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import co.edu.javeriana.as.personapp.mariadb.entity.EstudiosEntity;
import co.edu.javeriana.as.personapp.mariadb.entity.EstudiosEntityPK;

@Repository
public interface EstudiosRepositoryMaria extends JpaRepository<EstudiosEntity, EstudiosEntityPK> {
	List<EstudiosEntity> findByCcPer(Integer ccPer);
	List<EstudiosEntity> findByIdProf(Integer idProf);
}