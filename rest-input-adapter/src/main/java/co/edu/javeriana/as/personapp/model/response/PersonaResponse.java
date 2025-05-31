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
	
	// Usar _id como campo principal para MongoDB
	@JsonProperty("_id")
	public String get_id() {
		return getDni();
	}
	
	// Setter para _id para compatibilidad con MongoDB
	@JsonProperty("_id")
	public void set_id(String id) {
		setDni(id);
	}
	
	// Mantener aliases para compatibilidad
	@JsonProperty("identification")
	public String getIdentification() {
		return getDni();
	}
	
	@JsonProperty("identification")
	public void setIdentification(String identification) {
		setDni(identification);
	}
	
	@JsonProperty("id")
	public String getId() {
		return getDni();
	}
	
	@JsonProperty("id")
	public void setId(String id) {
		setDni(id);
	}
	
	@JsonProperty("cc")
	public String getCc() {
		return getDni();
	}
	
	@JsonProperty("cc")
	public void setCc(String cc) {
		setDni(cc);
	}
	
	@Override
	public String toString() {
		return String.format("PersonaResponse{dni='%s', firstName='%s', lastName='%s', age='%s', sex='%s', database='%s', status='%s'}", 
			getDni(), getFirstName(), getLastName(), getAge(), getSex(), getDatabase(), status);
	}
}