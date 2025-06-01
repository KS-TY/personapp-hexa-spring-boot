package co.edu.javeriana.as.personapp.mariadb.mapper;

import co.edu.javeriana.as.personapp.common.annotations.Mapper;
import co.edu.javeriana.as.personapp.domain.Gender;
import co.edu.javeriana.as.personapp.domain.Person;
import co.edu.javeriana.as.personapp.domain.Phone;
import co.edu.javeriana.as.personapp.mariadb.entity.PersonaEntity;
import co.edu.javeriana.as.personapp.mariadb.entity.TelefonoEntity;
import lombok.NonNull;

@Mapper
public class TelefonoMapperMaria {

	public TelefonoEntity fromDomainToAdapter(Phone phone) {
		TelefonoEntity telefonoEntity = new TelefonoEntity();
		telefonoEntity.setNum(phone.getNumber());
		telefonoEntity.setOper(phone.getCompany());
		telefonoEntity.setDuenio(validateDuenio(phone.getOwner()));
		return telefonoEntity;
	}

	private PersonaEntity validateDuenio(@NonNull Person owner) {
		if (owner == null) return new PersonaEntity();
		
		PersonaEntity personaEntity = new PersonaEntity();
		personaEntity.setCc(owner.getIdentification());
		personaEntity.setNombre(owner.getFirstName());
		personaEntity.setApellido(owner.getLastName());
		personaEntity.setGenero(owner.getGender() == Gender.FEMALE ? 'F' : 
		                        owner.getGender() == Gender.MALE ? 'M' : ' ');
		personaEntity.setEdad(owner.getAge());
		return personaEntity;
	}

	public Phone fromAdapterToDomain(TelefonoEntity telefonoEntity) {
		Phone phone = new Phone();
		phone.setNumber(telefonoEntity.getNum());
		phone.setCompany(telefonoEntity.getOper());
		phone.setOwner(validateOwner(telefonoEntity.getDuenio()));
		return phone;
	}

	private @NonNull Person validateOwner(PersonaEntity duenio) {
		if (duenio == null) return new Person();
		
		Person person = new Person();
		person.setIdentification(duenio.getCc());
		person.setFirstName(duenio.getNombre());
		person.setLastName(duenio.getApellido());
		person.setGender(duenio.getGenero() == 'F' ? Gender.FEMALE : 
		                 duenio.getGenero() == 'M' ? Gender.MALE : Gender.OTHER);
		person.setAge(duenio.getEdad());
		return person;
	}
}