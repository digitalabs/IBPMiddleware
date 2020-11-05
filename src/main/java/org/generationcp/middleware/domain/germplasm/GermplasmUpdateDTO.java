package org.generationcp.middleware.domain.germplasm;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.HashMap;
import java.util.Map;

@AutoProperty
// TODO: Reuse GermplasmDto from IBP-4097
public class GermplasmUpdateDTO {

	private Integer gid;
	private String germplasmUUID;
	private String preferredName;
	private String locationAbbreviation;
	private String creationDate;
	private String breedingMethod;
	private String reference;

	// Contains Names and Attributes data
	private Map<String, String> data = new HashMap<>();

	public Integer getGid() {
		return this.gid;
	}

	public void setGid(final Integer gid) {
		this.gid = gid;
	}

	public String getGermplasmUUID() {
		return this.germplasmUUID;
	}

	public void setGermplasmUUID(final String germplasmUUID) {
		this.germplasmUUID = germplasmUUID;
	}

	public String getPreferredName() {
		return this.preferredName;
	}

	public void setPreferredName(final String preferredName) {
		this.preferredName = preferredName;
	}

	public String getLocationAbbreviation() {
		return this.locationAbbreviation;
	}

	public void setLocationAbbreviation(final String locationAbbreviation) {
		this.locationAbbreviation = locationAbbreviation;
	}

	public String getCreationDate() {
		return this.creationDate;
	}

	public void setCreationDate(final String creationDate) {
		this.creationDate = creationDate;
	}

	public String getBreedingMethod() {
		return this.breedingMethod;
	}

	public void setBreedingMethod(final String breedingMethod) {
		this.breedingMethod = breedingMethod;
	}

	public Map<String, String> getData() {
		return this.data;
	}

	public void setData(final Map<String, String> attributes) {
		this.data = attributes;
	}

	public String getReference() {
		return this.reference;
	}

	public void setReference(final String reference) {
		this.reference = reference;
	}

	@Override
	public int hashCode() {
		return Pojomatic.hashCode(this);
	}

	@Override
	public String toString() {
		return Pojomatic.toString(this);
	}

	@Override
	public boolean equals(Object o) {
		return Pojomatic.equals(this, o);
	}
}
