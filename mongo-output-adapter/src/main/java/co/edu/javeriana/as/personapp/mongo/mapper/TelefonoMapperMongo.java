package co.edu.javeriana.as.personapp.mongo.mapper;

import co.edu.javeriana.as.personapp.common.annotations.Mapper;
import co.edu.javeriana.as.personapp.domain.Gender;
import co.edu.javeriana.as.personapp.domain.Person;
import co.edu.javeriana.as.personapp.domain.Phone;
import co.edu.javeriana.as.personapp.mongo.document.PersonaDocument;
import co.edu.javeriana.as.personapp.mongo.document.TelefonoDocument;
import lombok.NonNull;

@Mapper
public class TelefonoMapperMongo {

	public TelefonoDocument fromDomainToAdapter(Phone phone) {
		TelefonoDocument telefonoDocument = new TelefonoDocument();
		telefonoDocument.setId(phone.getNumber());
		telefonoDocument.setOper(phone.getCompany());
		telefonoDocument.setPrimaryDuenio(validateDuenio(phone.getOwner()));
		return telefonoDocument;
	}

	private PersonaDocument validateDuenio(@NonNull Person owner) {
		if (owner == null) return new PersonaDocument();
		
		PersonaDocument personaDocument = new PersonaDocument();
		personaDocument.setId(owner.getIdentification());
		personaDocument.setNombre(owner.getFirstName());
		personaDocument.setApellido(owner.getLastName());
		personaDocument.setGenero(owner.getGender() == Gender.FEMALE ? "F" : 
		                          owner.getGender() == Gender.MALE ? "M" : " ");
		personaDocument.setEdad(owner.getAge());
		return personaDocument;
	}

	public Phone fromAdapterToDomain(TelefonoDocument telefonoDocument) {
		Phone phone = new Phone();
		phone.setNumber(telefonoDocument.getId());
		phone.setCompany(telefonoDocument.getOper());
		phone.setOwner(validateOwner(telefonoDocument.getPrimaryDuenio()));
		return phone;
	}

	private @NonNull Person validateOwner(PersonaDocument duenio) {
		if (duenio == null) return new Person();
		
		Person person = new Person();
		person.setIdentification(duenio.getId());
		person.setFirstName(duenio.getNombre());
		person.setLastName(duenio.getApellido());
		person.setGender("F".equals(duenio.getGenero()) ? Gender.FEMALE : 
		                 "M".equals(duenio.getGenero()) ? Gender.MALE : Gender.OTHER);
		person.setAge(duenio.getEdad());
		return person;
	}
}