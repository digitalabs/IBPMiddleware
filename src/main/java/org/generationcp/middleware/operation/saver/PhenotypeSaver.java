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

package org.generationcp.middleware.operation.saver;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.PhenotypeExceptionDto;
import org.generationcp.middleware.domain.dms.Variable;
import org.generationcp.middleware.domain.dms.VariableConstraints;
import org.generationcp.middleware.domain.dms.VariableList;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.exceptions.PhenotypeException;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.pojos.dms.ExperimentModel;
import org.generationcp.middleware.pojos.dms.ExperimentPhenotype;
import org.generationcp.middleware.pojos.dms.Phenotype;
import org.generationcp.middleware.pojos.oms.CVTermRelationship;
import org.generationcp.middleware.util.DatabaseBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PhenotypeSaver extends Saver {

	private static final Logger LOG = LoggerFactory.getLogger(PhenotypeSaver.class);

	public PhenotypeSaver(HibernateSessionProvider sessionProviderForLocal) {
		super(sessionProviderForLocal);
	}

	public void savePhenotypes(ExperimentModel experimentModel, VariableList variates) throws MiddlewareQueryException {
		int i = 0;
		Map<Integer, PhenotypeExceptionDto> exceptions = null;
		if (variates != null && variates.getVariables() != null && !variates.getVariables().isEmpty()) {
			for (Variable variable : variates.getVariables()) {

				i++;

				try {
					this.save(experimentModel.getNdExperimentId(), variable);
				} catch (PhenotypeException e) {
					PhenotypeSaver.LOG.error(e.getMessage(), e);
					if (exceptions == null) {
						exceptions = new LinkedHashMap<Integer, PhenotypeExceptionDto>();
					}
					exceptions.put(e.getException().getStandardVariableId(), e.getException());
				}
				if (i % DatabaseBroker.JDBC_BATCH_SIZE == 0) {
					this.getPhenotypeDao().flush();
					this.getPhenotypeDao().clear();
				}

			}

			this.getPhenotypeDao().flush();
			this.getPhenotypeDao().clear();
		}
		if (exceptions != null) {
			throw new PhenotypeException(exceptions);
		}
	}

	public void save(int experimentId, Variable variable) throws MiddlewareQueryException {
		Phenotype phenotype = this.createPhenotype(variable);
		if (phenotype != null) {
			phenotype = this.getPhenotypeDao().save(phenotype);
			this.saveExperimentPhenotype(experimentId, phenotype.getPhenotypeId());
			variable.setPhenotypeId(phenotype.getPhenotypeId());
		}
	}

	public void saveOrUpdate(int experimentId, Integer variableId, int storedIn, String value, Phenotype phenotype)
			throws MiddlewareQueryException {
		this.saveOrUpdate(experimentId, variableId, storedIn, value, phenotype, false);
	}

	public void saveOrUpdate(int experimentId, Integer variableId, int storedIn, String value, Phenotype oldPhenotype,
			boolean isCustomCategoricalValue) throws MiddlewareQueryException {
		Phenotype phenotype = this.createPhenotype(variableId, storedIn, value, oldPhenotype, isCustomCategoricalValue);
		this.saveOrUpdate(experimentId, phenotype);
	}

	public void savePhenotype(int experimentId, Variable variable) throws MiddlewareQueryException {
		Phenotype phenotype = this.createPhenotype(variable);
		if (phenotype != null) {
			this.getPhenotypeDao().save(phenotype);
			this.saveExperimentPhenotype(experimentId, phenotype.getPhenotypeId());
		}
	}

	private Phenotype createPhenotype(Variable variable) throws MiddlewareQueryException {
		Phenotype phenotype = null;

		if (variable.getValue() != null && !"".equals(variable.getValue().trim())) {
			if (TermId.OBSERVATION_VARIATE.getId() == variable.getVariableType().getStandardVariable().getStoredIn().getId()) {
				phenotype = this.getPhenotypeObject(phenotype);
				if (variable.getValue() != null) {
					phenotype.setValue(variable.getValue().trim());
				} else {
					phenotype.setValue(null);
				}
				phenotype.setObservableId(variable.getVariableType().getId());
				phenotype.setName(String.valueOf(variable.getVariableType().getId()));
			} else if (TermId.CATEGORICAL_VARIATE.getId() == variable.getVariableType().getStandardVariable().getStoredIn().getId()) {
				phenotype = this.getPhenotypeObject(phenotype);
				if (variable.getValue() != null && !"".equals(variable.getValue())) {
					phenotype.setValue(variable.getValue());
					Term dataType = variable.getVariableType().getStandardVariable().getDataType();
					if (dataType.getId() == TermId.CATEGORICAL_VARIABLE.getId()) {
						Enumeration enumeration =
								variable.getVariableType().getStandardVariable().getEnumerationByName(variable.getValue());
						// in case the value entered is the id and not the enumeration code/name
						if (enumeration == null && NumberUtils.isNumber(variable.getValue())) {
							enumeration =
									variable.getVariableType().getStandardVariable()
									.getEnumeration(Double.valueOf(variable.getValue()).intValue());
						}
						if (enumeration != null) {
							phenotype.setcValue(enumeration.getId());
							phenotype.setValue(enumeration.getName());
						} else {
							//set it as a custom value of the categorical variate
							phenotype.setValue(variable.getValue());
							phenotype.setcValue(null);
						}
					} else if (dataType.getId() == TermId.NUMERIC_VARIABLE.getId()) {
						if (!NumberUtils.isNumber(variable.getValue())) {
							// FIXME Technical debt - throw an error that value must be numeric - should be handled by GCP-7956
						} else {
							VariableConstraints constraints = variable.getVariableType().getStandardVariable().getConstraints();
							if (constraints != null) {
								// FIXME Technical debt - Check if value is within the min and max - should be handled by GCP-7956
							}
						}

					}
				}
				phenotype.setObservableId(variable.getVariableType().getId());
				phenotype.setName(String.valueOf(variable.getVariableType().getId()));
			}
		}

		return phenotype;
	}

	private void saveOrUpdate(int experimentId, Phenotype phenotype) throws MiddlewareQueryException {
		if (phenotype != null) {
			this.getPhenotypeDao().merge(phenotype);
			this.saveOrUpdateExperimentPhenotype(experimentId, phenotype.getPhenotypeId());
		}
	}

	private Phenotype createPhenotype(Integer variableId, int storedIn, String value, Phenotype oldPhenotype,
			boolean isCustomCategoricalValue) throws MiddlewareQueryException {

		if ((value == null || "".equals(value.trim())) && (oldPhenotype == null || oldPhenotype.getPhenotypeId() == null)) {
			return null;
		}

		Phenotype phenotype = this.getPhenotypeObject(oldPhenotype);

		if (TermId.OBSERVATION_VARIATE.getId() == storedIn) {
			phenotype.setValue(value);
		} else if (TermId.CATEGORICAL_VARIATE.getId() == storedIn) {
			this.setCategoricalVariatePhenotypeValues(phenotype, value, variableId);
		}
		phenotype.setObservableId(variableId);
		phenotype.setName(String.valueOf(variableId));

		return phenotype;
	}

	private void setCategoricalVariatePhenotypeValues(Phenotype phenotype, String value, Integer variableId)
			throws MiddlewareQueryException {
		if (value == null || "".equals(value)) {
			phenotype.setcValue(null);
			phenotype.setValue(null);
		} else if (!NumberUtils.isNumber(value)) {
			phenotype.setcValue(null);
			phenotype.setValue(value);
		} else {
			Integer phenotypeValue = Double.valueOf(value).intValue();
			Map<Integer, String> possibleValuesMap = this.getPossibleValuesMap(variableId);
			if (possibleValuesMap.containsKey(phenotypeValue)) {
				phenotype.setcValue(phenotypeValue);
				phenotype.setValue(possibleValuesMap.get(phenotypeValue));
			} else {
				phenotype.setcValue(null);
				phenotype.setValue(value);
			}
		}
	}

	private Map<Integer, String> getPossibleValuesMap(int variableId) throws MiddlewareQueryException {
		Map<Integer, String> possibleValuesMap = new HashMap<Integer, String>();
		List<CVTermRelationship> relationships = this.getCvTermRelationshipDao().getBySubject(variableId);
		for (CVTermRelationship cvTermRelationship : relationships) {
			if (TermId.HAS_VALUE.getId() == cvTermRelationship.getTypeId()) {
				possibleValuesMap.put(cvTermRelationship.getObjectId(), this.getCvTermDao().getById(cvTermRelationship.getObjectId())
						.getName());
			}
		}
		return possibleValuesMap;
	}

	private Phenotype getPhenotypeObject(Phenotype oldPhenotype) throws MiddlewareQueryException {
		Phenotype phenotype = oldPhenotype;
		if (phenotype == null) {
			phenotype = new Phenotype();
		}
		return phenotype;
	}

	private void saveExperimentPhenotype(int experimentId, int phenotypeId) throws MiddlewareQueryException {
		this.getExperimentPhenotypeDao().save(this.createExperimentPhenotype(experimentId, phenotypeId));
	}

	private void saveOrUpdateExperimentPhenotype(int experimentId, int phenotypeId) throws MiddlewareQueryException {
		this.getExperimentPhenotypeDao().merge(this.createExperimentPhenotype(experimentId, phenotypeId));
	}

	private ExperimentPhenotype createExperimentPhenotype(int experimentId, int phenotypeId) throws MiddlewareQueryException {
		ExperimentPhenotype experimentPhenotype = this.getExperimentPhenotypeDao().getbyExperimentAndPhenotype(experimentId, phenotypeId);

		if (experimentPhenotype == null || experimentPhenotype.getExperimentPhenotypeId() == null) {
			experimentPhenotype = new ExperimentPhenotype();
			experimentPhenotype.setExperiment(experimentId);
			experimentPhenotype.setPhenotype(phenotypeId);
		}
		return experimentPhenotype;

	}

	public void saveOrUpdatePhenotypeValue(int projectId, int variableId, int storedIn, String value) throws MiddlewareQueryException {
		if (value != null) {
			boolean isInsert = false;
			Integer phenotypeId = this.getPhenotypeDao().getPhenotypeIdByProjectAndType(projectId, variableId);
			Phenotype phenotype = null;
			if (phenotypeId == null) {
				phenotype = new Phenotype();
				phenotypeId = phenotype.getPhenotypeId();
				phenotype.setObservableId(variableId);
				phenotype.setName(String.valueOf(variableId));
				isInsert = true;
			} else {
				phenotype = this.getPhenotypeDao().getById(phenotypeId);
			}
			if (storedIn == TermId.CATEGORICAL_VARIATE.getId() && NumberUtils.isNumber(value)) {
				phenotype.setcValue(Double.valueOf(value).intValue());
			} else {
				phenotype.setValue(value);
			}
			this.getPhenotypeDao().saveOrUpdate(phenotype);
			if (isInsert) {
				int experimentId = this.getExperimentProjectDao().getExperimentIdByProjectId(projectId);
				this.saveExperimentPhenotype(experimentId, phenotype.getPhenotypeId());
			}
		}
	}
}
