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

import co.edu.javeriana.as.personapp.adapter.StudyInputAdapterRest;
import co.edu.javeriana.as.personapp.model.request.StudyRequest;
import co.edu.javeriana.as.personapp.model.response.StudyResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/study")
public class StudyControllerV1 {
	
	@Autowired
	private StudyInputAdapterRest studyInputAdapterRest;
	
	@ResponseBody
	@GetMapping(path = "/{database}", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<StudyResponse> studies(@PathVariable String database) {
		log.info("Into studies REST API - Database: {}", database);
		return studyInputAdapterRest.historial(database.toUpperCase());
	}
	
	@ResponseBody
	@GetMapping(path = "/person/{personId}/{database}", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<StudyResponse> studiesByPerson(@PathVariable Integer personId, @PathVariable String database) {
		log.info("Into studiesByPerson REST API - PersonId: {}, Database: {}", personId, database);
		return studyInputAdapterRest.getStudiesByPerson(personId, database.toUpperCase());
	}
	
	@ResponseBody
	@GetMapping(path = "/profession/{professionId}/{database}", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<StudyResponse> studiesByProfession(@PathVariable Integer professionId, @PathVariable String database) {
		log.info("Into studiesByProfession REST API - ProfessionId: {}, Database: {}", professionId, database);
		return studyInputAdapterRest.getStudiesByProfession(professionId, database.toUpperCase());
	}
	
	@ResponseBody
	@PostMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public StudyResponse crearStudy(@RequestBody StudyRequest request) {
		log.info("=== CONTROLLER CREATE STUDY START ===");
		log.info("Request received: {}", request);
		log.info("Request personId: '{}'", request.getPersonId());
		log.info("Request professionId: '{}'", request.getProfessionId());
		log.info("Request graduationDate: '{}'", request.getGraduationDate());
		log.info("Request universityName: '{}'", request.getUniversityName());
		log.info("Request database: '{}'", request.getDatabase());
		
		if (request.getDatabase() == null || request.getDatabase().trim().isEmpty()) {
			log.error("Database field is null or empty!");
			StudyResponse errorResponse = new StudyResponse("", "", "", "", "", "ERROR: Database es requerido");
			return errorResponse;
		}
		
		StudyResponse response = studyInputAdapterRest.crearStudy(request);
		log.info("=== CONTROLLER CREATE STUDY END ===");
		log.info("Response: {}", response);
		return response;
	}
	
	@ResponseBody
	@PutMapping(path = "/{personId}/{professionId}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public StudyResponse actualizarStudy(@PathVariable Integer personId, @PathVariable Integer professionId, @RequestBody StudyRequest request) {
		log.info("Into actualizarStudy REST API - PersonId: {}, ProfessionId: {}, University: {}", personId, professionId, request.getUniversityName());
		return studyInputAdapterRest.actualizarStudy(personId, professionId, request);
	}
	
	@ResponseBody
	@DeleteMapping(path = "/{personId}/{professionId}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<StudyResponse> eliminarStudy(
			@PathVariable String personId, 
			@PathVariable String professionId,
			@RequestParam("database") String database) {
		
		log.info("=== DELETE CONTROLLER START ===");
		log.info("Raw PathVariable personId: '{}'", personId);
		log.info("Raw PathVariable professionId: '{}'", professionId);
		log.info("RequestParam database: '{}'", database);
		
		if (personId == null || personId.trim().isEmpty()) {
			log.error("PersonId is null or empty");
			StudyResponse errorResponse = new StudyResponse("", "", "", "", database, "ERROR: PersonId es requerido");
			return ResponseEntity.badRequest().body(errorResponse);
		}
		
		if (professionId == null || professionId.trim().isEmpty()) {
			log.error("ProfessionId is null or empty");
			StudyResponse errorResponse = new StudyResponse("", "", "", "", database, "ERROR: ProfessionId es requerido");
			return ResponseEntity.badRequest().body(errorResponse);
		}
		
		if ("null".equalsIgnoreCase(personId.trim())) {
			log.error("Received string 'null' as personId");
			StudyResponse errorResponse = new StudyResponse("", "", "", "", database, "ERROR: PersonId no puede ser 'null'");
			return ResponseEntity.badRequest().body(errorResponse);
		}
		
		if ("null".equalsIgnoreCase(professionId.trim())) {
			log.error("Received string 'null' as professionId");
			StudyResponse errorResponse = new StudyResponse("", "", "", "", database, "ERROR: ProfessionId no puede ser 'null'");
			return ResponseEntity.badRequest().body(errorResponse);
		}
		
		Integer personIdInt;
		Integer professionIdInt;
		try {
			personIdInt = Integer.valueOf(personId.trim());
			professionIdInt = Integer.valueOf(professionId.trim());
			log.info("Parsed IDs - PersonId: {}, ProfessionId: {}", personIdInt, professionIdInt);
		} catch (NumberFormatException e) {
			log.error("Invalid number format for IDs: personId='{}', professionId='{}'", personId, professionId);
			StudyResponse errorResponse = new StudyResponse(personId, professionId, "", "", database, "ERROR: Los IDs deben ser números válidos");
			return ResponseEntity.badRequest().body(errorResponse);
		}
		
		if (database == null || database.trim().isEmpty()) {
			log.error("Database parameter is null or empty");
			StudyResponse errorResponse = new StudyResponse(personId, professionId, "", "", "", "ERROR: Database es requerido");
			return ResponseEntity.badRequest().body(errorResponse);
		}
		
		String dbUpper = database.toUpperCase().trim();
		if (!dbUpper.equals("MARIA") && !dbUpper.equals("MONGO")) {
			log.error("Invalid database option: '{}'", database);
			StudyResponse errorResponse = new StudyResponse(personId, professionId, "", "", database, "ERROR: Database debe ser MARIA o MONGO");
			return ResponseEntity.badRequest().body(errorResponse);
		}
		
		log.info("Processing delete for PersonId: {} and ProfessionId: {} in database: {}", personIdInt, professionIdInt, dbUpper);
		
		try {
			StudyResponse response = studyInputAdapterRest.eliminarStudy(personIdInt, professionIdInt, dbUpper);
			
			log.info("Delete response status: {}", response != null ? response.getStatus() : "null response");
			
			if (response != null && response.getStatus() != null && response.getStatus().startsWith("ERROR")) {
				log.warn("Delete operation failed: {}", response.getStatus());
				return ResponseEntity.badRequest().body(response);
			}
			
			log.info("Delete operation successful");
			return ResponseEntity.ok(response);
			
		} catch (Exception e) {
			log.error("Unexpected error during delete operation", e);
			StudyResponse errorResponse = new StudyResponse(personId, professionId, "", "", database, "ERROR: " + e.getMessage());
			return ResponseEntity.internalServerError().body(errorResponse);
		} finally {
			log.info("=== DELETE CONTROLLER END ===");
		}
	}
}