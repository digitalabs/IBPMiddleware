package org.generationcp.middleware.service.api.ontology;

import liquibase.util.StringUtils;
import org.generationcp.middleware.domain.dms.ValueReference;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.ontology.DataType;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.stream.Collectors;

public class CategoricalValueValidator implements VariableValueValidator {

	@Override
	public boolean isValid(final MeasurementVariable variable) {
		this.verifyCategoricalDataType(variable);
		final List<String> possibleValues = variable.getPossibleValues().stream().map(ValueReference::getDescription).collect(
			Collectors.toList());
		return StringUtils.isEmpty(variable.getValue()) || possibleValues.contains(variable.getValue().trim());
	}


	private void verifyCategoricalDataType(final MeasurementVariable variable) {
		if (!DataType.CATEGORICAL_VARIABLE.getId().equals(variable.getDataTypeId())) {
			throw new IllegalStateException("The ensureCharacterDataType method must never be called for non character variables. "
				+ "Please report this error to your administrator.");
		} else if (CollectionUtils.isEmpty(variable.getPossibleValues())){
			throw new IllegalStateException("The categorical variable " + variable.getTermId() + " do not have possible values. "
				+ "Please report this error to your administrator.");
		}
	}

}
