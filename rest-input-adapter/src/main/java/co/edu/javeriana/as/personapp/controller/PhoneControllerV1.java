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

import co.edu.javeriana.as.personapp.adapter.PhoneInputAdapterRest;
import co.edu.javeriana.as.personapp.model.request.PhoneRequest;
import co.edu.javeriana.as.personapp.model.response.PhoneResponse;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/v1/phone")
public class PhoneControllerV1 {
	
	@Autowired
	private PhoneInputAdapterRest phoneInputAdapterRest;
	
	@ResponseBody
	@GetMapping(path = "/{database}", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<PhoneResponse> phones(@PathVariable String database) {
		log.info("Into phones REST API - Database: {}", database);
		return phoneInputAdapterRest.historial(database.toUpperCase());
	}
	
	@ResponseBody
	@GetMapping(path = "/owner/{personId}/{database}", produces = MediaType.APPLICATION_JSON_VALUE)
	public List<PhoneResponse> phonesByOwner(@PathVariable Integer personId, @PathVariable String database) {
		log.info("Into phonesByOwner REST API - PersonId: {}, Database: {}", personId, database);
		return phoneInputAdapterRest.getPhonesByOwner(personId, database.toUpperCase());
	}
	
	@ResponseBody
	@PostMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public PhoneResponse crearPhone(@RequestBody PhoneRequest request) {
		log.info("Into crearPhone REST API - Number: {}", request.getNumber());
		return phoneInputAdapterRest.crearPhone(request);
	}
	
	@ResponseBody
	@PutMapping(path = "/{number}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public PhoneResponse actualizarPhone(@PathVariable String number, @RequestBody PhoneRequest request) {
		log.info("Into actualizarPhone REST API - Number: {}, Company: {}", number, request.getCompany());
		return phoneInputAdapterRest.actualizarPhone(number, request);
	}
	
	@ResponseBody
	@DeleteMapping(path = "/{number}", produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<PhoneResponse> eliminarPhone(
			@PathVariable String number, 
			@RequestParam("database") String database) {
		
		log.info("=== DELETE CONTROLLER START ===");
		log.info("Raw PathVariable number: '{}'", number);
		log.info("RequestParam database: '{}'", database);
		
		if (number == null || number.trim().isEmpty()) {
			log.error("Number is null or empty");
			PhoneResponse errorResponse = new PhoneResponse("", "", "", database, "ERROR: Número es requerido");
			return ResponseEntity.badRequest().body(errorResponse);
		}
		
		if ("null".equalsIgnoreCase(number.trim())) {
			log.error("Received string 'null' as number");
			PhoneResponse errorResponse = new PhoneResponse("", "", "", database, "ERROR: Número no puede ser 'null'");
			return ResponseEntity.badRequest().body(errorResponse);
		}
		
		if (database == null || database.trim().isEmpty()) {
			log.error("Database parameter is null or empty");
			PhoneResponse errorResponse = new PhoneResponse(number, "", "", "", "ERROR: Database es requerido");
			return ResponseEntity.badRequest().body(errorResponse);
		}
		
		String dbUpper = database.toUpperCase().trim();
		if (!dbUpper.equals("MARIA") && !dbUpper.equals("MONGO")) {
			log.error("Invalid database option: '{}'", database);
			PhoneResponse errorResponse = new PhoneResponse(number, "", "", database, "ERROR: Database debe ser MARIA o MONGO");
			return ResponseEntity.badRequest().body(errorResponse);
		}
		
		log.info("Processing delete for Number: {} in database: {}", number, dbUpper);
		
		try {
			PhoneResponse response = phoneInputAdapterRest.eliminarPhone(number.trim(), dbUpper);
			
			log.info("Delete response status: {}", response != null ? response.getStatus() : "null response");
			
			if (response != null && response.getStatus() != null && response.getStatus().startsWith("ERROR")) {
				log.warn("Delete operation failed: {}", response.getStatus());
				return ResponseEntity.badRequest().body(response);
			}
			
			log.info("Delete operation successful");
			return ResponseEntity.ok(response);
			
		} catch (Exception e) {
			log.error("Unexpected error during delete operation", e);
			PhoneResponse errorResponse = new PhoneResponse(number, "", "", database, "ERROR: " + e.getMessage());
			return ResponseEntity.internalServerError().body(errorResponse);
		} finally {
			log.info("=== DELETE CONTROLLER END ===");
		}
	}
}