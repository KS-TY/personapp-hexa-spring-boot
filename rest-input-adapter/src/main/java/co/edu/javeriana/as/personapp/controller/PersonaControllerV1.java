package co.edu.javeriana.as.personapp.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
		log.info("Into personas REST API");
		return personaInputAdapterRest.historial(database.toUpperCase());
	}
	
	@ResponseBody
	@PostMapping(path = "", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public PersonaResponse crearPersona(@RequestBody PersonaRequest request) {
		log.info("Into crearPersona REST API");
		return personaInputAdapterRest.crearPersona(request);
	}
	
	@ResponseBody
	@PutMapping(path = "/{identification}", produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public PersonaResponse actualizarPersona(@PathVariable Integer identification, @RequestBody PersonaRequest request) {
		log.info("Into actualizarPersona REST API - ID: {}", identification);
		return personaInputAdapterRest.actualizarPersona(identification, request);
	}
	
	@ResponseBody
	@DeleteMapping(path = "/{identification}", produces = MediaType.APPLICATION_JSON_VALUE)
	public PersonaResponse eliminarPersona(@PathVariable Integer identification, @RequestParam String database) {
		log.info("Into eliminarPersona REST API - ID: {}, Database: {}", identification, database);
		return personaInputAdapterRest.eliminarPersona(identification, database.toUpperCase());
	}
}