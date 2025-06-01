package co.edu.javeriana.as.personapp.mongo.adapter;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;

import com.mongodb.MongoWriteException;

import co.edu.javeriana.as.personapp.application.port.out.PhoneOutputPort;
import co.edu.javeriana.as.personapp.common.annotations.Adapter;
import co.edu.javeriana.as.personapp.domain.Phone;
import co.edu.javeriana.as.personapp.mongo.document.PersonaDocument;
import co.edu.javeriana.as.personapp.mongo.document.TelefonoDocument;
import co.edu.javeriana.as.personapp.mongo.mapper.TelefonoMapperMongo;
import co.edu.javeriana.as.personapp.mongo.repository.PersonaRepositoryMongo;
import co.edu.javeriana.as.personapp.mongo.repository.TelefonoRepositoryMongo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Adapter("phoneOutputAdapterMongo")
public class PhoneOutputAdapterMongo implements PhoneOutputPort {
	
	@Autowired
    private TelefonoRepositoryMongo telefonoRepositoryMongo;
	
	@Autowired
	private PersonaRepositoryMongo personaRepositoryMongo;
	
	@Autowired
	private TelefonoMapperMongo telefonoMapperMongo;
	
	@Override
	public Phone save(Phone phone) {
		log.debug("Into save on Adapter MongoDB");
		try {
			TelefonoDocument persistedTelefono = telefonoRepositoryMongo.save(telefonoMapperMongo.fromDomainToAdapter(phone));
			return telefonoMapperMongo.fromAdapterToDomain(persistedTelefono);
		} catch (MongoWriteException e) {
			log.warn(e.getMessage());
			return phone;
		}		
	}

	@Override
	public Boolean delete(String number) {
		log.debug("Into delete on Adapter MongoDB");
		telefonoRepositoryMongo.deleteById(number);
		return telefonoRepositoryMongo.findById(number).isEmpty();
	}

	@Override
	public List<Phone> find() {
		log.debug("Into find on Adapter MongoDB");
		List<TelefonoDocument> telefonos = telefonoRepositoryMongo.findAll();
		
		// Resolver las referencias de propietarios manualmente
		return telefonos.stream()
				.map(this::resolveOwnerReference)
				.map(telefonoMapperMongo::fromAdapterToDomain)
				.collect(Collectors.toList());
	}

	@Override
	public Phone findById(String number) {
		log.debug("Into findById on Adapter MongoDB");
		if (telefonoRepositoryMongo.findById(number).isEmpty()) {
			return null;
		} else {
			TelefonoDocument telefono = telefonoRepositoryMongo.findById(number).get();
			telefono = resolveOwnerReference(telefono);
			return telefonoMapperMongo.fromAdapterToDomain(telefono);
		}
	}

	@Override
	public List<Phone> findByPersonId(Integer personId) {
		log.debug("Into findByPersonId on Adapter MongoDB");
		List<TelefonoDocument> telefonos = telefonoRepositoryMongo.findByPrimaryDuenioId(personId);
		
		return telefonos.stream()
				.map(this::resolveOwnerReference)
				.map(telefonoMapperMongo::fromAdapterToDomain)
				.collect(Collectors.toList());
	}
	
	/**
	 * Resuelve la referencia del propietario cargando los datos completos
	 */
	private TelefonoDocument resolveOwnerReference(TelefonoDocument telefono) {
		if (telefono.getPrimaryDuenio() != null && telefono.getPrimaryDuenio().getId() != null) {
			Integer ownerId = telefono.getPrimaryDuenio().getId();
			log.debug("Resolving owner reference for ID: {}", ownerId);
			
			// Cargar la persona completa desde la base de datos
			PersonaDocument personaCompleta = personaRepositoryMongo.findById(ownerId).orElse(null);
			
			if (personaCompleta != null) {
				log.debug("Found complete owner data: {} {}", personaCompleta.getNombre(), personaCompleta.getApellido());
				telefono.setPrimaryDuenio(personaCompleta);
			} else {
				log.warn("Owner not found for ID: {}, creating default", ownerId);
				// Crear una persona con el ID pero datos por defecto
				PersonaDocument defaultOwner = new PersonaDocument();
				defaultOwner.setId(ownerId);
				defaultOwner.setNombre("Usuario");
				defaultOwner.setApellido("ID: " + ownerId);
				defaultOwner.setGenero("M");
				telefono.setPrimaryDuenio(defaultOwner);
			}
		} else {
			log.warn("TelefonoDocument has no owner reference or owner ID is null");
			// Crear un propietario por defecto
			PersonaDocument defaultOwner = new PersonaDocument();
			defaultOwner.setId(0);
			defaultOwner.setNombre("Sin");
			defaultOwner.setApellido("Propietario");
			defaultOwner.setGenero("M");
			telefono.setPrimaryDuenio(defaultOwner);
		}
		
		return telefono;
	}
}