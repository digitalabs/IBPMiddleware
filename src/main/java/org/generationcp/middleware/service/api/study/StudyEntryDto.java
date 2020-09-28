package org.generationcp.middleware.service.api.study;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@AutoProperty
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StudyEntryDto implements Serializable {

	private Integer entryId;

	private Integer entryNumber;

	private String entryCode;

	private Integer gid;

	private String designation;

	private Integer lotCount;

	private String availableBalance;

	private String unit;

	// TODO rename to properties
	private Map<Integer, StudyEntryPropertyData> variables = new HashMap<>();

	public StudyEntryDto(){

	}

	public StudyEntryDto(final Integer entryId, final Integer gid, final String designation) {
		this.entryId = entryId;
		this.gid = gid;
		this.designation = designation;
	}

	public StudyEntryDto(final Integer entryId, final Integer entryNumber, final String entryCode, final Integer gid, final String designation, final Integer lotCount, final String availableBalance, final String unit){
		this.entryId = entryId;
		this.entryNumber = entryNumber;
		this.entryCode = entryCode;
		this.gid = gid;
		this.designation = designation;
		this.lotCount = lotCount;
		this.availableBalance = availableBalance;
		this.unit = unit;
	}

	public Integer getEntryId() {
		return entryId;
	}

	public void setEntryId(final Integer entryId) {
		this.entryId = entryId;
	}

	public Integer getGid() {
		return gid;
	}

	public void setGid(final Integer gid) {
		this.gid = gid;
	}

	public String getDesignation() {
		return designation;
	}

	public void setDesignation(final String designation) {
		this.designation = designation;
	}

	public Integer getEntryNumber() {
		return entryNumber;
	}

	public void setEntryNumber(final Integer entryNumber) {
		this.entryNumber = entryNumber;
	}

	public String getEntryCode() {
		return entryCode;
	}

	public void setEntryCode(final String entryCode) {
		this.entryCode = entryCode;
	}

	public Integer getLotCount() {
		return lotCount;
	}

	public void setLotCount(final Integer lotCount) {
		this.lotCount = lotCount;
	}

	public String getAvailableBalance() {
		return availableBalance;
	}

	public void setAvailableBalance(final String availableBalance) {
		this.availableBalance = availableBalance;
	}

	public String getUnit() {
		return unit;
	}

	public void setUnit(final String unit) {
		this.unit = unit;
	}

	public Map<Integer, StudyEntryPropertyData> getVariables() {
		return variables;
	}

	public void setVariables(final Map<Integer, StudyEntryPropertyData> variables) {
		this.variables = variables;
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


	public Optional<String> getStudyEntryPropertyValue(final Integer variableId) {
		if (this.variables.containsKey(variableId)) {
			return Optional.of(this.variables.get(variableId).getValue());
		}
		return Optional.empty();
	}

}
