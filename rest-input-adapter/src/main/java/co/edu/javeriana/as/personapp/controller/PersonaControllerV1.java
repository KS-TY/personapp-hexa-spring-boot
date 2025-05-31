package co.edu.javeriana.as.personapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import co.edu.javeriana.as.personapp.adapter.PersonaInputAdapterRest;
import co.edu.javeriana.as.personapp.model.request.PersonaRequest;
import co.edu.javeriana.as.personapp.model.response.PersonaResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/persona")
public class PersonaControllerV1 {
	
	@Autowired
	private PersonaInputAdapterRest personaInputAdapterRest;
	
	@ResponseBody
	@GetMapping(path = "/{database}", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<PersonaResponse> personas(@PathVariable String database) {
		log.info("Into personas REST API - Database: {}", database);
		return personaInputAdapterRest.historial(database.toUpperCase());
	}
	
	@ResponseBody
	@PostMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public PersonaResponse crearPersona(@RequestBody PersonaRequest request) {
		log.info("Into crearPersona REST API - DNI: {}", request.getDni());
		return personaInputAdapterRest.crearPersona(request);
	}
	
	@ResponseBody
	@PutMapping(path = "/{identification}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public PersonaResponse actualizarPersona(@PathVariable Integer identification, @RequestBody PersonaRequest request) {
		log.info("Into actualizarPersona REST API - ID: {}, DNI: {}", identification, request.getDni());
		return personaInputAdapterRest.actualizarPersona(identification, request);
	}
	
	@ResponseBody
	@DeleteMapping(path = "/{identification}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PersonaResponse> eliminarPersona(
			@PathVariable String identification, 
			@RequestParam("database") String database) {
		
		log.info("=== DELETE CONTROLLER START ===");
		log.info("Raw PathVariable identification: '{}'", identification);
		log.info("RequestParam database: '{}'", database);
		
		// Validaciones de entrada
		if (identification == null || identification.trim().isEmpty()) {
			log.error("Identification is null or empty");
			PersonaResponse errorResponse = new PersonaResponse("", "", "", "", "", database, "ERROR: ID es requerido");
			return ResponseEntity.badRequest().body(errorResponse);
		}
		
		// Validar que no sea el string "null"
		if ("null".equalsIgnoreCase(identification.trim())) {
			log.error("Received string 'null' as identification");
			PersonaResponse errorResponse = new PersonaResponse("", "", "", "", "", database, "ERROR: ID no puede ser 'null'");
			return ResponseEntity.badRequest().body(errorResponse);
		}
		
		// Validar que sea un número válido
		Integer personId;
		try {
			personId = Integer.valueOf(identification.trim());
			log.info("Parsed identification to Integer: {}", personId);
		} catch (NumberFormatException e) {
			log.error("Invalid number format for identification: '{}'", identification);
			PersonaResponse errorResponse = new PersonaResponse(identification, "", "", "", "", database, "ERROR: ID debe ser un número válido");
			return ResponseEntity.badRequest().body(errorResponse);
		}
		
		// Validar base de datos
		if (database == null || database.trim().isEmpty()) {
			log.error("Database parameter is null or empty");
			PersonaResponse errorResponse = new PersonaResponse(identification, "", "", "", "", "", "ERROR: Database es requerido");
			return ResponseEntity.badRequest().body(errorResponse);
		}
		
		String dbUpper = database.toUpperCase().trim();
		if (!dbUpper.equals("MARIA") && !dbUpper.equals("MONGO")) {
			log.error("Invalid database option: '{}'", database);
			PersonaResponse errorResponse = new PersonaResponse(identification, "", "", "", "", database, "ERROR: Database debe ser MARIA o MONGO");
			return ResponseEntity.badRequest().body(errorResponse);
		}
		
		log.info("Processing delete for ID: {} in database: {}", personId, dbUpper);
		
		try {
			PersonaResponse response = personaInputAdapterRest.eliminarPersona(personId, dbUpper);
			
			log.info("Delete response status: {}", response != null ? response.getStatus() : "null response");
			
			if (response != null && response.getStatus() != null && response.getStatus().startsWith("ERROR")) {
				log.warn("Delete operation failed: {}", response.getStatus());
				return ResponseEntity.badRequest().body(response);
			}
			
			log.info("Delete operation successful");
			return ResponseEntity.ok(response);
			
		} catch (Exception e) {
			log.error("Unexpected error during delete operation", e);
			PersonaResponse errorResponse = new PersonaResponse(identification, "", "", "", "", database, "ERROR: " + e.getMessage());
			return ResponseEntity.internalServerError().body(errorResponse);
		} finally {
			log.info("=== DELETE CONTROLLER END ===");
		}
	}
}