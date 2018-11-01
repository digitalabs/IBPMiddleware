package org.generationcp.middleware.service.api.dataset;

import java.util.List;

import org.generationcp.middleware.domain.ontology.VariableType;

public interface DatasetService {

	long countPhenotypes(Integer datasetId, List<Integer> traitIds);

	long countPhenotypesByInstance(Integer datasetId, Integer instanceId);

	void addVariable(Integer datasetId, Integer variableId, VariableType type, String alias);

}
