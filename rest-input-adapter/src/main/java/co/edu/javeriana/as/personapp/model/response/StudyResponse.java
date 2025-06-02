package co.edu.javeriana.as.personapp.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import co.edu.javeriana.as.personapp.model.request.StudyRequest;

public class StudyResponse extends StudyRequest {
	
	private String status;
	
	public StudyResponse(String personId, String professionId, String graduationDate, String universityName, String database, String status) {
		super(personId, professionId, graduationDate, universityName, database);
		this.status = status;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	// Usar _id como campo principal para MongoDB (combinación de personId-professionId)
	@JsonProperty("_id")
	public String get_id() {
		return getPersonId() + "-" + getProfessionId();
	}
	
	// Setter para _id para compatibilidad con MongoDB
	@JsonProperty("_id")
	public void set_id(String id) {
		if (id != null && id.contains("-")) {
			String[] parts = id.split("-", 2);
			if (parts.length == 2) {
				setPersonId(parts[0]);
				setProfessionId(parts[1]);
			}
		}
	}
	
	// Mantener aliases para compatibilidad con MariaDB
	@JsonProperty("cc_per")
	public String getCcPer() {
		return getPersonId();
	}
	
	@JsonProperty("cc_per")
	public void setCcPer(String ccPer) {
		setPersonId(ccPer);
	}
	
	@JsonProperty("id_prof")
	public String getIdProf() {
		return getProfessionId();
	}
	
	@JsonProperty("id_prof")
	public void setIdProf(String idProf) {
		setProfessionId(idProf);
	}
	
	@JsonProperty("fecha")
	public String getFecha() {
		return getGraduationDate();
	}
	
	@JsonProperty("fecha")
	public void setFecha(String fecha) {
		setGraduationDate(fecha);
	}
	
	@JsonProperty("univer")
	public String getUniver() {
		return getUniversityName();
	}
	
	@JsonProperty("univer")
	public void setUniver(String univer) {
		setUniversityName(univer);
	}
	
	@Override
	public String toString() {
		return String.format("StudyResponse{personId='%s', professionId='%s', graduationDate='%s', universityName='%s', database='%s', status='%s'}", 
			getPersonId(), getProfessionId(), getGraduationDate(), getUniversityName(), getDatabase(), status);
	}
}