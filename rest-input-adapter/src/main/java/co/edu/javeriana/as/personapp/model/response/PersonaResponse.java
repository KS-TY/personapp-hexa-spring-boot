package co.edu.javeriana.as.personapp.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import co.edu.javeriana.as.personapp.model.request.PersonaRequest;

public class PersonaResponse extends PersonaRequest{
	
	private String status;
	
	public PersonaResponse(String dni, String firstName, String lastName, String age, String sex, String database, String status) {
		super(dni, firstName, lastName, age, sex, database);
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	// Agregar alias para diferentes campos de ID
	@JsonProperty("identification")
	public String getIdentification() {
		return getDni();
	}
	
	@JsonProperty("id")
	public String getId() {
		return getDni();
	}
	
	@JsonProperty("cc")
	public String getCc() {
		return getDni();
	}
}