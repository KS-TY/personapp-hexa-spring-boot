package co.edu.javeriana.as.personapp.terminal.mapper;

import co.edu.javeriana.as.personapp.common.annotations.Mapper;
import co.edu.javeriana.as.personapp.domain.Phone;
import co.edu.javeriana.as.personapp.terminal.model.PhoneModelCli;

@Mapper
public class PhoneMapperCli {

	public PhoneModelCli fromDomainToAdapterCli(Phone phone) {
		PhoneModelCli phoneModelCli = new PhoneModelCli();
		phoneModelCli.setNumber(phone.getNumber());
		phoneModelCli.setCompany(phone.getCompany());
		phoneModelCli.setOwnerName(phone.getOwner().getFirstName() + " " + phone.getOwner().getLastName());
		phoneModelCli.setOwnerId(phone.getOwner().getIdentification());
		return phoneModelCli;
	}
}