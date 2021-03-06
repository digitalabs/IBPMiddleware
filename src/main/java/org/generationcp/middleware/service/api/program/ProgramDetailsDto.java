package org.generationcp.middleware.service.api.program;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.io.Serializable;

public class ProgramDetailsDto implements Serializable, Comparable<ProgramDetailsDto> {

	private static final long serialVersionUID = 9163866215679883266L;
	private String programDbId;
	private String name;
	private String abbreviation;
	private String objective;
	private String leadPerson;
	private String leadPersonDbId;
	private String cropName;

	public ProgramDetailsDto() {

	}

	public ProgramDetailsDto(final String programDbId, final String name, final String abbreviation, final String objective,
			final String leadPerson, final String leadPersonDbId, final String leadPersonName, final String cropName) {

		this.programDbId = programDbId;
		this.name = name;
		this.abbreviation = abbreviation;
		this.objective = objective;
		this.leadPerson = leadPerson;
		this.cropName = cropName;
		this.leadPersonDbId = leadPersonDbId;
	}

	public String getProgramDbId() {
		return this.programDbId;
	}

	public void setProgramDbId(final String programDbId) {
		this.programDbId = programDbId;
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getAbbreviation() {
		return this.abbreviation;
	}

	public void setAbbreviation(final String abbreviation) {
		this.abbreviation = abbreviation;
	}

	public String getObjective() {
		return this.objective;
	}

	public void setObjective(final String objective) {
		this.objective = objective;
	}

	public String getLeadPerson() {
		return this.leadPerson;
	}

	public void setLeadPerson(final String leadPerson) {
		this.leadPerson = leadPerson;
	}

	public String getLeadPersonDbId() {
		return this.leadPersonDbId;
	}

	public void setLeadPersonDbId(final String leadPersonDbId) {
		this.leadPersonDbId = leadPersonDbId;
	}

	public String getCropName() {
		return this.cropName;
	}

	public void setCropName(final String cropName) {
		this.cropName = cropName;
	}

	@Override
	public boolean equals(final Object other) {
		if (!(other instanceof ProgramDetailsDto)) {
			return false;
		}
		final ProgramDetailsDto castOther = (ProgramDetailsDto) other;
		return new EqualsBuilder().append(this.programDbId, castOther.programDbId).isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.programDbId).hashCode();
	}

	@Override
	public String toString() {
		return new ReflectionToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).toString();
	}

	@Override
	public int compareTo(final ProgramDetailsDto compareProgramDetails) {
		final String id = compareProgramDetails.getProgramDbId();
		return this.programDbId.compareTo(id);
	}
}
