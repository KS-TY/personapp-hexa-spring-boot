package co.edu.javeriana.as.personapp.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import co.edu.javeriana.as.personapp.model.request.PhoneRequest;

public class PhoneResponse extends PhoneRequest {
	
	private String status;
	
	public PhoneResponse(String number, String company, String ownerId, String database, String status) {
		super(number, company, ownerId, database);
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
		return getNumber();
	}
	
	// Setter para _id para compatibilidad con MongoDB
	@JsonProperty("_id")
	public void set_id(String id) {
		setNumber(id);
	}
	
	// Mantener aliases para compatibilidad
	@JsonProperty("num")
	public String getNum() {
		return getNumber();
	}
	
	@JsonProperty("num")
	public void setNum(String num) {
		setNumber(num);
	}
	
	@JsonProperty("oper")
	public String getOper() {
		return getCompany();
	}
	
	@JsonProperty("oper")
	public void setOper(String oper) {
		setCompany(oper);
	}
	
	@JsonProperty("duenio")
	public String getDuenio() {
		return getOwnerId();
	}
	
	@JsonProperty("duenio")
	public void setDuenio(String duenio) {
		setOwnerId(duenio);
	}
	
	@Override
	public String toString() {
		return String.format("PhoneResponse{number='%s', company='%s', ownerId='%s', database='%s', status='%s'}", 
			getNumber(), getCompany(), getOwnerId(), getDatabase(), status);
	}
}