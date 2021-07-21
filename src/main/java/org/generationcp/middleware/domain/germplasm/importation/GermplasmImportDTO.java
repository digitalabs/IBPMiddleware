package org.generationcp.middleware.domain.germplasm.importation;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.brapi.v2.germplasm.GermplasmImportRequest;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@AutoProperty
@JsonPropertyOrder({
	"clientId", "germplasmUUID", "locationAbbr", "breedingMethodAbbr", "reference", "preferredName", "names", "attributes",
	"creationDate", "progenitor1", "progenitor2"})
public class GermplasmImportDTO {

	private Integer clientId;

	private String germplasmPUI;

	private String locationAbbr;

	private String breedingMethodAbbr;

	private String reference;

	private String preferredName;

	private Map<String, String> names;

	private Map<String, String> attributes;

	private String creationDate;

	private String progenitor1;

	private String progenitor2;

	public GermplasmImportDTO() {
	}

	public GermplasmImportDTO(final Integer clientId, final String germplasmPUI, final String locationAbbr,
		final String breedingMethodAbbr,
		final String reference,
		final String preferredName, final Map<String, String> names, final Map<String, String> attributes, final String creationDate,
		final String progenitor1, final String progenitor2) {
		this.clientId = clientId;
		this.germplasmPUI = germplasmPUI;
		this.locationAbbr = locationAbbr;
		this.breedingMethodAbbr = breedingMethodAbbr;
		this.reference = reference;
		this.preferredName = preferredName;
		this.names = names;
		this.attributes = attributes;
		this.creationDate = creationDate;
		this.progenitor1 = progenitor1;
		this.progenitor2 = progenitor2;
	}

	public Integer getClientId() {
		return this.clientId;
	}

	public void setClientId(final Integer clientId) {
		this.clientId = clientId;
	}

	public String getLocationAbbr() {
		return this.locationAbbr;
	}

	public void setLocationAbbr(final String locationAbbr) {
		this.locationAbbr = locationAbbr;
	}

	public String getBreedingMethodAbbr() {
		return this.breedingMethodAbbr;
	}

	public void setBreedingMethodAbbr(final String breedingMethodAbbr) {
		this.breedingMethodAbbr = breedingMethodAbbr;
	}

	public String getGermplasmPUI() {
		return this.germplasmPUI;
	}

	public void setGermplasmPUI(final String germplasmPUI) {
		this.germplasmPUI = germplasmPUI;
	}

	public String getReference() {
		return this.reference;
	}

	public void setReference(final String reference) {
		this.reference = reference;
	}

	public String getPreferredName() {
		return this.preferredName;
	}

	public void setPreferredName(final String preferredName) {
		this.preferredName = preferredName;
	}

	public Map<String, String> getNames() {
		return this.names;
	}

	public void setNames(final Map<String, String> names) {
		this.names = names;
	}

	public Map<String, String> getAttributes() {
		return this.attributes;
	}

	public void setAttributes(final Map<String, String> attributes) {
		this.attributes = attributes;
	}

	public String getCreationDate() {
		return this.creationDate;
	}

	public void setCreationDate(final String creationDate) {
		this.creationDate = creationDate;
	}

	public String getProgenitor1() {
		return this.progenitor1;
	}

	public void setProgenitor1(final String progenitor1) {
		this.progenitor1 = progenitor1;
	}

	public String getProgenitor2() {
		return this.progenitor2;
	}

	public void setProgenitor2(final String progenitor2) {
		this.progenitor2 = progenitor2;
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
	public boolean equals(final Object o) {
		return Pojomatic.equals(this, o);
	}

	public boolean isGermplasmPUIExisting(final Collection<String> existingGermplasmPUIs){
		if (!existingGermplasmPUIs.contains(this.getGermplasmPUI())) {
			final Optional<String> germplasmPUIFromNames = this.getGermplasmPUIFromNames();
			return existingGermplasmPUIs.contains(germplasmPUIFromNames.orElse(""));
		}
		return true;
	}

	public Optional<String> getGermplasmPUIFromNames() {
		return this.names.entrySet().stream().filter(s -> GermplasmImportRequest.PUI_NAME_TYPE.equalsIgnoreCase(s.getKey()))
			.map(Map.Entry::getValue).collect(Collectors.toList()).stream().findFirst();
	}

	public List<String> collectGermplasmPUIs() {
		final List<String> puisList = new ArrayList<>();
		if (!StringUtils.isEmpty(this.getGermplasmPUI())) {
			puisList.add(this.getGermplasmPUI());
		}
		final Optional<String> puiFromNames = this.getGermplasmPUIFromNames();
		puiFromNames.ifPresent(puisList::add);
		return puisList;
	}

}