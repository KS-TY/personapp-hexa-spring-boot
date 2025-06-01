package co.edu.javeriana.as.personapp.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import co.edu.javeriana.as.personapp.application.port.in.PhoneInputPort;
import co.edu.javeriana.as.personapp.application.port.out.PhoneOutputPort;
import co.edu.javeriana.as.personapp.application.usecase.PhoneUseCase;
import co.edu.javeriana.as.personapp.common.annotations.Adapter;
import co.edu.javeriana.as.personapp.common.exceptions.InvalidOptionException;
import co.edu.javeriana.as.personapp.common.exceptions.NoExistException;
import co.edu.javeriana.as.personapp.common.setup.DatabaseOption;
import co.edu.javeriana.as.personapp.domain.Phone;
import co.edu.javeriana.as.personapp.mapper.PhoneMapperRest;
import co.edu.javeriana.as.personapp.model.request.PhoneRequest;
import co.edu.javeriana.as.personapp.model.response.PhoneResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Adapter
public class PhoneInputAdapterRest {

    @Autowired
    @Qualifier("phoneOutputAdapterMaria")
    private PhoneOutputPort phoneOutputPortMaria;

    @Autowired
    @Qualifier("phoneOutputAdapterMongo")
    private PhoneOutputPort phoneOutputPortMongo;

    @Autowired
    private PhoneMapperRest phoneMapperRest;

    PhoneInputPort phoneInputPort;

    private String setPhoneOutputPortInjection(String dbOption) throws InvalidOptionException {
        log.info("=== SETTING PHONE OUTPUT PORT ===");
        log.info("Original dbOption received: '{}'", dbOption);
        
        if (dbOption == null || dbOption.trim().isEmpty()) {
            log.error("Database option is null or empty");
            throw new InvalidOptionException("Database option cannot be null or empty");
        }
        
        // Limpiar y normalizar la opción de base de datos
        String cleanDbOption = dbOption.trim().toUpperCase();
        log.info("Cleaned and normalized dbOption: '{}'", cleanDbOption);
        
        // Verificar las opciones válidas
        if (cleanDbOption.equals("MARIA") || cleanDbOption.equals("MARIADB")) {
            log.info("Setting Phone output port to MARIA");
            phoneInputPort = new PhoneUseCase(phoneOutputPortMaria);
            return DatabaseOption.MARIA.toString();
        } else if (cleanDbOption.equals("MONGO") || cleanDbOption.equals("MONGODB")) {
            log.info("Setting Phone output port to MONGO");
            phoneInputPort = new PhoneUseCase(phoneOutputPortMongo);
            return DatabaseOption.MONGO.toString();
        } else {
            log.error("Invalid database option: '{}'. Valid options are: MARIA, MARIADB, MONGO, MONGODB", dbOption);
            throw new InvalidOptionException("Invalid database option: " + dbOption + ". Must be MARIA, MARIADB, MONGO, or MONGODB");
        }
    }

    public List<PhoneResponse> historial(String database) {
        log.info("Into historial PhoneEntity in Input Adapter - Database: {}", database);
        try {
            String usedDatabase = setPhoneOutputPortInjection(database);
            List<Phone> phones = phoneInputPort.findAll();
            
            if (usedDatabase.equalsIgnoreCase(DatabaseOption.MARIA.toString())) {
                return phones.stream().map(phoneMapperRest::fromDomainToAdapterRestMaria)
                        .collect(Collectors.toList());
            } else {
                return phones.stream().map(phoneMapperRest::fromDomainToAdapterRestMongo)
                        .collect(Collectors.toList());
            }

        } catch (InvalidOptionException e) {
            log.warn(e.getMessage());
            return new ArrayList<PhoneResponse>();
        }
    }

    public PhoneResponse crearPhone(PhoneRequest request) {
        log.info("=== CREAR PHONE START ===");
        log.info("PhoneRequest received: {}", request);
        log.info("Request database field: '{}'", request.getDatabase());
        log.info("Request number: '{}'", request.getNumber());
        log.info("Request company: '{}'", request.getCompany());
        log.info("Request ownerId: '{}'", request.getOwnerId());
        
        try {
            // Validar que el request tenga database
            if (request.getDatabase() == null || request.getDatabase().trim().isEmpty()) {
                log.error("Database field is null or empty in request");
                return new PhoneResponse("", "", "", "", "ERROR: Database es requerido");
            }
            
            // Debug: Verificar el valor exacto del database antes de procesarlo
            String rawDatabase = request.getDatabase();
            log.info("Raw database value: '{}' (length: {})", rawDatabase, rawDatabase.length());
            
            // Configurar el output port según la base de datos seleccionada
            String database = setPhoneOutputPortInjection(rawDatabase);
            log.info("Database configuration completed successfully: {}", database);
            
            // Mapear el request a dominio
            Phone phone = phoneMapperRest.fromAdapterToDomain(request);
            log.info("Phone mapped from request: Number={}, Company={}, Owner={}", 
                phone.getNumber(), phone.getCompany(), phone.getOwner().getIdentification());
            
            // Crear el teléfono
            Phone createdPhone = phoneInputPort.create(phone);
            log.info("Phone created successfully in database: {}", database);
            log.info("Created phone details: Number={}, Company={}, Owner={}", 
                createdPhone.getNumber(), createdPhone.getCompany(), 
                createdPhone.getOwner().getIdentification());

            // Retornar la respuesta según la base de datos utilizada
            PhoneResponse response;
            if (database.equalsIgnoreCase(DatabaseOption.MARIA.toString())) {
                log.info("Mapping response for MARIA database");
                response = phoneMapperRest.fromDomainToAdapterRestMaria(createdPhone);
            } else {
                log.info("Mapping response for MONGO database");
                response = phoneMapperRest.fromDomainToAdapterRestMongo(createdPhone);
            }
            
            log.info("Final response created: {}", response);
            return response;
            
        } catch (InvalidOptionException e) {
            log.error("Invalid database option error: {}", e.getMessage());
            return new PhoneResponse("", "", "", "", "ERROR: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error creating phone: {}", e.getMessage(), e);
            return new PhoneResponse("", "", "", "", "ERROR: " + e.getMessage());
        } finally {
            log.info("=== CREAR PHONE END ===");
        }
    }

    public PhoneResponse actualizarPhone(String number, PhoneRequest request) {
        log.info("Into actualizarPhone PhoneEntity in Input Adapter - Number: {}", number);
        try {
            String database = setPhoneOutputPortInjection(request.getDatabase());
            Phone phoneToUpdate = phoneMapperRest.fromAdapterToDomain(request);
            phoneToUpdate.setNumber(number);

            Phone updatedPhone = phoneInputPort.edit(number, phoneToUpdate);

            if (database.equalsIgnoreCase(DatabaseOption.MARIA.toString())) {
                return phoneMapperRest.fromDomainToAdapterRestMaria(updatedPhone);
            } else {
                return phoneMapperRest.fromDomainToAdapterRestMongo(updatedPhone);
            }
        } catch (InvalidOptionException e) {
            log.warn("Invalid option: " + e.getMessage());
            return new PhoneResponse("", "", "", "", "ERROR: " + e.getMessage());
        } catch (NoExistException e) {
            log.warn("Phone not found: " + e.getMessage());
            return new PhoneResponse("", "", "", "", "ERROR: Teléfono no encontrado");
        } catch (Exception e) {
            log.error("Unexpected error: " + e.getMessage(), e);
            return new PhoneResponse("", "", "", "", "ERROR: " + e.getMessage());
        }
    }

    public PhoneResponse eliminarPhone(String number, String database) {
        log.info("=== ADAPTER DELETE START ===");
        log.info("Received number: {}", number);
        log.info("Received database: {}", database);

        if (number == null || number.trim().isEmpty()) {
            log.error("Number is null or empty");
            return new PhoneResponse("", "", "", database, "ERROR: Número no puede ser vacío");
        }

        if (database == null || database.trim().isEmpty()) {
            log.error("Database is null or empty");
            return new PhoneResponse(number, "", "", "", "ERROR: Database es requerido");
        }

        try {
            String dbUsed = setPhoneOutputPortInjection(database);
            log.info("Database option set to: {}", dbUsed);

            log.info("Searching for phone with number: {}", number);
            Phone existingPhone;
            try {
                existingPhone = phoneInputPort.findOne(number);
                if (existingPhone == null) {
                    log.warn("Phone with number {} not found", number);
                    return new PhoneResponse(number, "", "", database, "ERROR: Teléfono no encontrado");
                }
                log.info("Found phone: {} - {}", existingPhone.getNumber(), existingPhone.getCompany());
            } catch (NoExistException e) {
                log.warn("Phone with number {} does not exist: {}", number, e.getMessage());
                return new PhoneResponse(number, "", "", database, "ERROR: Teléfono no encontrado");
            }

            log.info("Attempting to delete phone with number: {}", number);
            Boolean deleted;
            try {
                deleted = phoneInputPort.drop(number);
                log.info("Delete operation result: {}", deleted);
            } catch (NoExistException e) {
                log.error("Error during delete - phone not found: {}", e.getMessage());
                return new PhoneResponse(number, "", "", database, "ERROR: Teléfono no encontrado para eliminar");
            }

            if (deleted != null && deleted) {
                PhoneResponse response;
                if (dbUsed.equalsIgnoreCase(DatabaseOption.MARIA.toString())) {
                    response = phoneMapperRest.fromDomainToAdapterRestMaria(existingPhone);
                } else {
                    response = phoneMapperRest.fromDomainToAdapterRestMongo(existingPhone);
                }
                response.setStatus("DELETED");
                log.info("Success response created with status: {}", response.getStatus());
                return response;
            } else {
                log.warn("Delete operation returned false for number: {}", number);
                return new PhoneResponse(number, "", "", database, "ERROR: No se pudo eliminar el teléfono");
            }

        } catch (InvalidOptionException e) {
            log.error("Invalid database option: {}", e.getMessage());
            return new PhoneResponse(number, "", "", database,
                    "ERROR: Opción de base de datos inválida: " + e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error during deletion: {}", e.getMessage(), e);
            return new PhoneResponse(number, "", "", database, "ERROR: Error inesperado: " + e.getMessage());
        } finally {
            log.info("=== ADAPTER DELETE END ===");
        }
    }

    public List<PhoneResponse> getPhonesByOwner(Integer personId, String database) {
        log.info("Into getPhonesByOwner - PersonId: {}, Database: {}", personId, database);
        try {
            String dbUsed = setPhoneOutputPortInjection(database);
            List<Phone> phones = phoneInputPort.findByOwner(personId);

            if (dbUsed.equalsIgnoreCase(DatabaseOption.MARIA.toString())) {
                return phones.stream().map(phoneMapperRest::fromDomainToAdapterRestMaria)
                        .collect(Collectors.toList());
            } else {
                return phones.stream().map(phoneMapperRest::fromDomainToAdapterRestMongo)
                        .collect(Collectors.toList());
            }

        } catch (InvalidOptionException e) {
            log.warn(e.getMessage());
            return new ArrayList<PhoneResponse>();
        } catch (NoExistException e) {
            log.warn("No phones found for person: " + e.getMessage());
            return new ArrayList<PhoneResponse>();
        }
    }
}