
package org.generationcp.middleware.operation.transformer.etl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.VariableConstraints;
import org.generationcp.middleware.domain.oms.DataType;
import org.generationcp.middleware.domain.oms.OntologyVariableSummary;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;

public class StandardVariableTransformer extends Transformer {

	public StandardVariableTransformer(HibernateSessionProvider sessionProvider) {
		super(sessionProvider);
	}

	public StandardVariable transformVariable(Variable variable)
			throws MiddlewareQueryException {
		StandardVariable standardVariable = new StandardVariable();
		standardVariable.setId(variable.getId());
		standardVariable.setName(variable.getName());
		standardVariable.setDescription(variable.getDefinition());
		standardVariable.setProperty(variable.getProperty());
		standardVariable.setScale(variable.getScale());
		standardVariable.setMethod(variable.getMethod());
		DataType dataType = variable.getScale().getDataType();
		standardVariable.setDataType(new Term(dataType.getId(),dataType.getName(),dataType.getName()));
		standardVariable.setConstraints(new VariableConstraints(
				0, 0, Double.parseDouble(variable.getMinValue()), 
				Double.parseDouble(variable.getMaxValue())));
		standardVariable.setEnumerations(getValidValues(variable));
		standardVariable.setCropOntologyId(variable.getProperty().getCropOntologyId());
		return standardVariable;
	}

	private List<Enumeration> getValidValues(Variable variable) {
		List<Enumeration> validValues = new ArrayList<Enumeration>();
		Map<String, String> categories = variable.getScale().getCategories();
		int rank = 1;
		for (String name : categories.keySet()) {
			validValues.add(new Enumeration(null,name,categories.get(name),rank++));
		}
		return validValues;
	}
}
