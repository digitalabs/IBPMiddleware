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

import java.util.List;
import java.util.Set;

import org.generationcp.middleware.domain.dms.VariableType;
import org.generationcp.middleware.domain.dms.VariableTypeList;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.helper.VariableInfo;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.pojos.dms.ProjectProperty;

public class VariableTypeBuilder extends Builder {

	public VariableTypeBuilder(HibernateSessionProvider sessionProviderForLocal) {
		super(sessionProviderForLocal);
	}

	public VariableTypeList create(List<ProjectProperty> properties, String programUUID) 
			throws MiddlewareException {
		Set<VariableInfo> variableInfoList = this.getVariableInfoBuilder().create(properties);

		VariableTypeList variableTypes = new VariableTypeList();
		for (VariableInfo variableInfo : variableInfoList) {
			variableTypes.add(this.create(variableInfo,programUUID));
		}
		return variableTypes.sort();
	}

	public VariableType create(VariableInfo variableInfo, String programUUID) throws MiddlewareException {
		VariableType variableType = new VariableType();

		variableType.setLocalName(variableInfo.getLocalName());
		variableType.setLocalDescription(variableInfo.getLocalDescription());
		variableType.setRank(variableInfo.getRank());
		variableType.setStandardVariable(this.getStandardVariableBuilder().create(
				variableInfo.getStdVariableId(),programUUID));
		variableType.setTreatmentLabel(variableInfo.getTreatmentLabel());

		return variableType;
	}
}
