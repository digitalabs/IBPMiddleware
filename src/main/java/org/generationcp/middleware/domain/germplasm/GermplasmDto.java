package org.generationcp.middleware.domain.germplasm;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.List;

@AutoProperty
@JsonInclude(JsonInclude.Include.NON_NULL)
public class GermplasmDto {

	private Integer gid;

	private String germplasmUUID;

	private String germplasmPUI;

	private String preferredName;

	private String creationDate;

	private String createdBy;

	private Integer createdByUserId;

	private String reference;

	private Integer breedingLocationId;

	private String breedingLocation;

	private Integer breedingMethodId;

	private String breedingMethod;

	private boolean isGroupedLine;

	private Integer groupId;

	private Integer gpid1;

	private Integer gpid2;

	private Integer numberOfProgenitors;

	private List<Integer> otherProgenitors;

	private GermplasmOriginDto germplasmOrigin;

	private List<GermplasmNameDto> names;

	private List<GermplasmAttributeDto> attributes;

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

	public List<GermplasmNameDto> getNames() {
		return this.names;
	}

	public void setNames(final List<GermplasmNameDto> names) {
		this.names = names;
	}

	public String getCreationDate() {
		return this.creationDate;
	}

	public void setCreationDate(final String creationDate) {
		this.creationDate = creationDate;
	}

	public String getCreatedBy() {
		return this.createdBy;
	}

	public void setCreatedBy(final String createdBy) {
		this.createdBy = createdBy;
	}

	public Integer getCreatedByUserId() {
		return this.createdByUserId;
	}

	public void setCreatedByUserId(final Integer createdByUserId) {
		this.createdByUserId = createdByUserId;
	}

	public String getReference() {
		return this.reference;
	}

	public void setReference(final String reference) {
		this.reference = reference;
	}

	public List<GermplasmAttributeDto> getAttributes() {
		return this.attributes;
	}

	public void setAttributes(final List<GermplasmAttributeDto> attributes) {
		this.attributes = attributes;
	}

	public String getBreedingLocation() {
		return this.breedingLocation;
	}

	public void setBreedingLocation(final String breedingLocation) {
		this.breedingLocation = breedingLocation;
	}

	public String getBreedingMethod() {
		return this.breedingMethod;
	}

	public void setBreedingMethod(final String breedingMethod) {
		this.breedingMethod = breedingMethod;
	}

	public boolean isGroupedLine() {
		return this.isGroupedLine;
	}

	public void setGroupedLine(final boolean groupedLine) {
		this.isGroupedLine = groupedLine;
	}

	public Integer getGroupId() {
		return this.groupId;
	}

	public void setGroupId(final Integer groupId) {
		this.groupId = groupId;
	}

	public Integer getBreedingLocationId() {
		return this.breedingLocationId;
	}

	public void setBreedingLocationId(final Integer breedingLocationId) {
		this.breedingLocationId = breedingLocationId;
	}

	public Integer getBreedingMethodId() {
		return this.breedingMethodId;
	}

	public void setBreedingMethodId(final Integer breedingMethodId) {
		this.breedingMethodId = breedingMethodId;
	}

	public Integer getGpid1() {
		return this.gpid1;
	}

	public void setGpid1(final Integer gpid1) {
		this.gpid1 = gpid1;
	}

	public Integer getGpid2() {
		return this.gpid2;
	}

	public void setGpid2(final Integer gpid2) {
		this.gpid2 = gpid2;
	}

	public Integer getNumberOfProgenitors() {
		return this.numberOfProgenitors;
	}

	public void setNumberOfProgenitors(final Integer numberOfProgenitors) {
		this.numberOfProgenitors = numberOfProgenitors;
	}

	public List<Integer> getOtherProgenitors() {
		return this.otherProgenitors;
	}

	public void setOtherProgenitors(final List<Integer> otherProgenitors) {
		this.otherProgenitors = otherProgenitors;
	}

	public GermplasmOriginDto getGermplasmOrigin() {
		return this.germplasmOrigin;
	}

	public void setGermplasmOrigin(final GermplasmOriginDto germplasmOrigin) {
		this.germplasmOrigin = germplasmOrigin;
	}

	public String getGermplasmPUI() {
		return this.germplasmPUI;
	}

	public void setGermplasmPUI(final String germplasmPUI) {
		this.germplasmPUI = germplasmPUI;
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

}
