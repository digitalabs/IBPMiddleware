package org.generationcp.middleware.domain.germplasm;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;


@AutoProperty
public class GermplasmAttributeDto extends GermplasmAttributeRequestDto {

	private Integer id;

	private String variableName;

	private String variableDescription;

	private String variableTypeName;

	private String locationName;

	public GermplasmAttributeDto() {
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(final Integer id) {
		this.id = id;
	}

	public String getVariableDescription() {
		return this.variableDescription;
	}

	public void setVariableDescription(final String variableDescription) {
		this.variableDescription = variableDescription;
	}

	public String getLocationName() {
		return this.locationName;
	}

	public void setLocationName(final String locationName) {
		this.locationName = locationName;
	}

	public String getVariableTypeName() {
		return this.variableTypeName;
	}

	public void setVariableTypeName(final String variableTypeName) {
		this.variableTypeName = variableTypeName;
	}

		public String getVariableName() {
		return this.variableName;
	}

	public void setVariableName(final String variableName) {
		this.variableName = variableName;
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
