package org.generationcp.middleware.service.api.dataset;

import org.generationcp.middleware.pojos.dms.Phenotype;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.Objects;

@AutoProperty
public class ObservationUnitData {

	private Integer observationId;

	private Integer categoricalValueId;

	private String value;

	private Phenotype.ValueStatus status;

	private Integer variableId;

	public ObservationUnitData(final Integer observationId, final Integer categoricalValueId, final String value,
		final Phenotype.ValueStatus status, final Integer variableId) {
		this.observationId = observationId;
		this.categoricalValueId = categoricalValueId;
		this.value = value;
		this.status = status;
		this.variableId = variableId;
	}

	public ObservationUnitData() {
	}

	public ObservationUnitData(final String value) {
		this.value = value;
	}

	public Integer getObservationId() {
		return this.observationId;
	}

	public void setObservationId(final Integer observationId) {
		this.observationId = observationId;
	}

	public Integer getCategoricalValueId() {
		return this.categoricalValueId;
	}

	public void setCategoricalValueId(final Integer categoricalValueId) {
		this.categoricalValueId = categoricalValueId;
	}

	public String getValue() {
		return this.value;
	}

	public void setValue(final String value) {
		this.value = value;
	}

	public Phenotype.ValueStatus getStatus() {
		return this.status;
	}

	public void setStatus(final Phenotype.ValueStatus status) {
		this.status = status;
	}

	public Integer getVariableId() {
		return this.variableId;
	}

	public void setVariableId(final Integer variableId) {
		this.variableId = variableId;
	}

	@Override
	public boolean equals(final Object o) {
		return Pojomatic.equals(this, o);
	}

	@Override
	public int hashCode() {
		return Pojomatic.hashCode(this);
	}

	@Override
	public String toString() {
		return Pojomatic.toString(this);
	}
}
