/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package org.generationcp.middleware.operation.builder;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.helper.VariableInfo;
import org.generationcp.middleware.pojos.dms.ProjectProperty;

public class VariableInfoBuilder {

	public Set<VariableInfo> create(List<ProjectProperty> properties) {
		Set<VariableInfo> variableDefs = new HashSet<>();
		for (ProjectProperty property : properties) {
			variableDefs.add(this.createVariableDef(property));
		}
		return variableDefs;
	}

	private VariableInfo createVariableDef(ProjectProperty stdVariableProperty) {

		String localNameProperty = stdVariableProperty.getAlias();
		String localDescriptionProperty = this.findLocalDescriptionProperty(stdVariableProperty);

		VariableInfo variableDef = new VariableInfo();
		variableDef.setLocalName(localNameProperty == null ? null : localNameProperty);
		variableDef.setLocalDescription(localDescriptionProperty == null ? null : localDescriptionProperty);
		variableDef.setStdVariableId(stdVariableProperty.getVariableId());
		variableDef.setRank(stdVariableProperty.getRank());

		/**
		 * TODO
		String treatmentLabelProperty = this.findTreatmentLabelProperty(stdVariableProperty);
		if (treatmentLabelProperty != null) {
			variableDef.setTreatmentLabel(treatmentLabelProperty);
		}
		 */

		VariableType varType = VariableType.getById(stdVariableProperty.getTypeId());
		if (varType != null) {
			variableDef.setRole(varType.getRole());
			variableDef.setVariableType(varType);
		}

		return variableDef;
	}

	private String findLocalDescriptionProperty(ProjectProperty properties) {
		// TODO
		return null;
	}
	
}
