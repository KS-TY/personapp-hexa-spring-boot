package co.edu.javeriana.as.personapp.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import co.edu.javeriana.as.personapp.model.request.ProfessionRequest;

public class ProfessionResponse extends ProfessionRequest {
	
	private String status;
	
	public ProfessionResponse(String id, String name, String description, String database, String status) {
		super(id, name, description, database);
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
		return getId();
	}
	
	// Setter para _id para compatibilidad con MongoDB
	@JsonProperty("_id")
	public void set_id(String id) {
		setId(id);
	}
	
	// Mantener aliases para compatibilidad
	@JsonProperty("identification")
	public String getIdentification() {
		return getId();
	}
	
	@JsonProperty("identification")
	public void setIdentification(String identification) {
		setId(identification);
	}
	
	@Override
	public String toString() {
		return String.format("ProfessionResponse{id='%s', name='%s', description='%s', database='%s', status='%s'}", 
			getId(), getName(), getDescription(), getDatabase(), status);
	}
}