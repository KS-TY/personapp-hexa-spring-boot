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

import co.edu.javeriana.as.personapp.adapter.ProfessionInputAdapterRest;
import co.edu.javeriana.as.personapp.model.request.ProfessionRequest;
import co.edu.javeriana.as.personapp.model.response.ProfessionResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/profession")
public class ProfessionControllerV1 {
	
	@Autowired
	private ProfessionInputAdapterRest professionInputAdapterRest;
	
	@ResponseBody
	@GetMapping(path = "/{database}", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<ProfessionResponse> professions(@PathVariable String database) {
		log.info("Into professions REST API - Database: {}", database);
		return professionInputAdapterRest.historial(database.toUpperCase());
	}
	
	@ResponseBody
	@PostMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ProfessionResponse crearProfession(@RequestBody ProfessionRequest request) {
		log.info("Into crearProfession REST API - ID: {}", request.getId());
		return professionInputAdapterRest.crearProfession(request);
	}
	
	@ResponseBody
	@PutMapping(path = "/{identification}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ProfessionResponse actualizarProfession(@PathVariable Integer identification, @RequestBody ProfessionRequest request) {
		log.info("Into actualizarProfession REST API - ID: {}, Name: {}", identification, request.getName());
		return professionInputAdapterRest.actualizarProfession(identification, request);
	}
	
	@ResponseBody
	@DeleteMapping(path = "/{identification}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<ProfessionResponse> eliminarProfession(
			@PathVariable String identification, 
			@RequestParam("database") String database) {
		
		log.info("=== DELETE CONTROLLER START ===");
		log.info("Raw PathVariable identification: '{}'", identification);
		log.info("RequestParam database: '{}'", database);
		
		if (identification == null || identification.trim().isEmpty()) {
			log.error("Identification is null or empty");
			ProfessionResponse errorResponse = new ProfessionResponse("", "", "", database, "ERROR: ID es requerido");
			return ResponseEntity.badRequest().body(errorResponse);
		}
		
		if ("null".equalsIgnoreCase(identification.trim())) {
			log.error("Received string 'null' as identification");
			ProfessionResponse errorResponse = new ProfessionResponse("", "", "", database, "ERROR: ID no puede ser 'null'");
			return ResponseEntity.badRequest().body(errorResponse);
		}
		
		Integer professionId;
		try {
			professionId = Integer.valueOf(identification.trim());
			log.info("Parsed identification to Integer: {}", professionId);
		} catch (NumberFormatException e) {
			log.error("Invalid number format for identification: '{}'", identification);
			ProfessionResponse errorResponse = new ProfessionResponse(identification, "", "", database, "ERROR: ID debe ser un número válido");
			return ResponseEntity.badRequest().body(errorResponse);
		}
		
		if (database == null || database.trim().isEmpty()) {
			log.error("Database parameter is null or empty");
			ProfessionResponse errorResponse = new ProfessionResponse(identification, "", "", "", "ERROR: Database es requerido");
			return ResponseEntity.badRequest().body(errorResponse);
		}
		
		String dbUpper = database.toUpperCase().trim();
		if (!dbUpper.equals("MARIA") && !dbUpper.equals("MONGO")) {
			log.error("Invalid database option: '{}'", database);
			ProfessionResponse errorResponse = new ProfessionResponse(identification, "", "", database, "ERROR: Database debe ser MARIA o MONGO");
			return ResponseEntity.badRequest().body(errorResponse);
		}
		
		log.info("Processing delete for ID: {} in database: {}", professionId, dbUpper);
		
		try {
			ProfessionResponse response = professionInputAdapterRest.eliminarProfession(professionId, dbUpper);
			
			log.info("Delete response status: {}", response != null ? response.getStatus() : "null response");
			
			if (response != null && response.getStatus() != null && response.getStatus().startsWith("ERROR")) {
				log.warn("Delete operation failed: {}", response.getStatus());
				return ResponseEntity.badRequest().body(response);
			}
			
			log.info("Delete operation successful");
			return ResponseEntity.ok(response);
			
		} catch (Exception e) {
			log.error("Unexpected error during delete operation", e);
			ProfessionResponse errorResponse = new ProfessionResponse(identification, "", "", database, "ERROR: " + e.getMessage());
			return ResponseEntity.internalServerError().body(errorResponse);
		} finally {
			log.info("=== DELETE CONTROLLER END ===");
		}
	}
}