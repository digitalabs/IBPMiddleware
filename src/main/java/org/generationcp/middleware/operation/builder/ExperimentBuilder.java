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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.generationcp.middleware.domain.dms.Experiment;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.Variable;
import org.generationcp.middleware.domain.dms.VariableList;
import org.generationcp.middleware.domain.dms.VariableType;
import org.generationcp.middleware.domain.dms.VariableTypeList;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.pojos.dms.ExperimentModel;
import org.generationcp.middleware.pojos.dms.ExperimentProject;
import org.generationcp.middleware.pojos.dms.ExperimentProperty;
import org.generationcp.middleware.pojos.dms.ExperimentStock;
import org.generationcp.middleware.pojos.dms.Geolocation;
import org.generationcp.middleware.pojos.dms.GeolocationProperty;
import org.generationcp.middleware.pojos.dms.Phenotype;
import org.generationcp.middleware.pojos.dms.StockModel;
import org.generationcp.middleware.pojos.dms.StockProperty;

public class ExperimentBuilder extends Builder {

	public ExperimentBuilder(HibernateSessionProvider sessionProviderForLocal) {
		super(sessionProviderForLocal);
	}

	public long count(int dataSetId) throws MiddlewareQueryException {
		return this.getExperimentProjectDao().count(dataSetId);
	}

	public List<Experiment> build(int projectId, TermId type, int start, int numOfRows, VariableTypeList variableTypes)
			throws MiddlewareQueryException {
		List<Experiment> experiments = new ArrayList<Experiment>();
		List<ExperimentProject> experimentProjects =
				this.getExperimentProjectDao().getExperimentProjects(projectId, type.getId(), start, numOfRows);
		Map<Integer, StockModel> stockModelMap = this.getStockModelMap(experimentProjects);
		for (ExperimentProject experimentProject : experimentProjects) {
			experiments.add(this.createExperiment(experimentProject.getExperiment(), variableTypes, stockModelMap));
		}
		return experiments;
	}

	public List<Experiment> build(int projectId, TermId type, int start, int numOfRows, VariableTypeList variableTypes,
			boolean hasVariableType) throws MiddlewareQueryException {
		List<Experiment> experiments = new ArrayList<Experiment>();
		List<ExperimentProject> experimentProjects =
				this.getExperimentProjectDao().getExperimentProjects(projectId, type.getId(), start, numOfRows);
		for (ExperimentProject experimentProject : experimentProjects) {
			experiments.add(this.createExperiment(experimentProject.getExperiment(), variableTypes, hasVariableType));
		}
		return experiments;
	}

	private Map<Integer, StockModel> getStockModelMap(List<ExperimentProject> experimentProjects) throws MiddlewareQueryException {
		Map<Integer, StockModel> stockModelMap = new HashMap<Integer, StockModel>();
		List<Integer> stockIds = new ArrayList<Integer>();
		for (ExperimentProject experimentProject : experimentProjects) {
			List<ExperimentStock> experimentStocks = experimentProject.getExperiment().getExperimentStocks();
			if (experimentStocks != null && experimentStocks.size() == 1) {
				stockIds.add(experimentStocks.get(0).getStock().getStockId());
			}
		}
		stockModelMap = this.getStockBuilder().get(stockIds);

		return stockModelMap;
	}

	public List<Experiment> build(int projectId, List<TermId> types, int start, int numOfRows, VariableTypeList variableTypes)
			throws MiddlewareQueryException {
		List<Experiment> experiments = new ArrayList<Experiment>();
		List<ExperimentProject> experimentProjects =
				this.getExperimentProjectDao().getExperimentProjects(projectId, types, start, numOfRows);
		// to improve, we will get all the stocks already and saved it in a map and pass it as a parameter to avoid multiple query in DB
		Map<Integer, StockModel> stockModelMap = this.getStockModelMap(experimentProjects);

		for (ExperimentProject experimentProject : experimentProjects) {
			experiments.add(this.createExperiment(experimentProject.getExperiment(), variableTypes, stockModelMap));
		}
		return experiments;
	}

	public Experiment buildOne(int projectId, TermId type, VariableTypeList variableTypes) throws MiddlewareQueryException {
		List<Experiment> experiments = this.build(projectId, type, 0, 1, variableTypes);
		if (experiments != null && experiments.size() > 0) {
			return experiments.get(0);
		}
		return null;
	}

	public Experiment buildOne(int projectId, TermId type, VariableTypeList variableTypes, boolean hasVariableType)
			throws MiddlewareQueryException {
		List<Experiment> experiments = this.build(projectId, type, 0, 1, variableTypes, hasVariableType);
		if (experiments != null && experiments.size() > 0) {
			return experiments.get(0);
		}
		return null;
	}

	private Experiment createExperiment(ExperimentModel experimentModel, VariableTypeList variableTypes,
			Map<Integer, StockModel> stockModelMap) throws MiddlewareQueryException {
		Experiment experiment = new Experiment();
		experiment.setId(experimentModel.getNdExperimentId());
		experiment.setFactors(this.getFactors(experimentModel, variableTypes, stockModelMap));
		experiment.setVariates(this.getVariates(experimentModel, variableTypes));
		experiment.setLocationId(experimentModel.getGeoLocation().getLocationId());
		return experiment;
	}

	private Experiment createExperiment(ExperimentModel experimentModel, VariableTypeList variableTypes, boolean hasVariableType)
			throws MiddlewareQueryException {
		Experiment experiment = new Experiment();
		experiment.setId(experimentModel.getNdExperimentId());
		experiment.setFactors(this.getFactors(experimentModel, variableTypes, hasVariableType));
		experiment.setVariates(this.getVariates(experimentModel, variableTypes));
		experiment.setLocationId(experimentModel.getGeoLocation().getLocationId());
		return experiment;
	}

	private VariableList getVariates(ExperimentModel experimentModel, VariableTypeList variableTypes) throws MiddlewareQueryException {
		VariableList variates = new VariableList();

		this.addPlotVariates(experimentModel, variates, variableTypes);

		return variates.sort();
	}

	private void addPlotVariates(ExperimentModel experimentModel, VariableList variates, VariableTypeList variableTypes)
			throws MiddlewareQueryException {
		this.addVariates(experimentModel, variates, variableTypes);
	}

	private void addVariates(ExperimentModel experiment, VariableList variates, VariableTypeList variableTypes)
			throws MiddlewareQueryException {
		this.getExperimentDao().refresh(experiment);
		if (experiment.getPhenotypes() != null) {
			for (Phenotype phenotype : experiment.getPhenotypes()) {
				VariableType variableType = variableTypes.findById(phenotype.getObservableId());
				// TODO: trial constants are currently being saved in the measurement effect dataset
				// added this validation for now, to handle the said scenario, otherwise, and NPE is thrown
				// in the future, trial constant will no longer be saved at the measurements level
				if (variableType != null) {
					if (variableType.getStandardVariable().getStoredIn().getId() == TermId.CATEGORICAL_VARIATE.getId()
							&& variableType.getStandardVariable().getDataType().getId() == TermId.CATEGORICAL_VARIABLE.getId()) {
						Variable var = new Variable(phenotype.getPhenotypeId(), variableType, phenotype.getcValueId());
						if (phenotype.getcValueId() == null && phenotype.getValue() != null) {
							var.setValue(phenotype.getValue());
							var.setCustomValue(true);
						}
						variates.add(var);
					} else {
						variates.add(new Variable(phenotype.getPhenotypeId(), variableType, phenotype.getValue()));

					}
				}
			}
		}
	}

	private VariableList getFactors(ExperimentModel experimentModel, VariableTypeList variableTypes, Map<Integer, StockModel> stockModelMap)
			throws MiddlewareQueryException {
		VariableList factors = new VariableList();

		this.addPlotExperimentFactors(factors, experimentModel, variableTypes, stockModelMap);

		this.addLocationFactors(experimentModel, factors, variableTypes);

		return factors.sort();
	}

	private VariableList getFactors(ExperimentModel experimentModel, VariableTypeList variableTypes, boolean hasVariableType)
			throws MiddlewareQueryException {
		VariableList factors = new VariableList();

		this.addPlotExperimentFactors(factors, experimentModel, variableTypes, hasVariableType);

		this.addLocationFactors(experimentModel, factors, variableTypes);

		return factors.sort();
	}

	private void addLocationFactors(ExperimentModel experimentModel, VariableList factors, VariableTypeList variableTypes) {
		for (VariableType variableType : variableTypes.getVariableTypes()) {
			if (this.isLocationFactor(variableType)) {
				factors.add(this.createLocationFactor(experimentModel.getGeoLocation(), variableType));
			}
		}
	}

	private boolean isLocationFactor(VariableType variableType) {
		StandardVariable standardVariable = variableType.getStandardVariable();
		if (standardVariable.getStoredIn().getId() == TermId.TRIAL_ENVIRONMENT_INFO_STORAGE.getId()) {
			return true;
		}
		if (standardVariable.getStoredIn().getId() == TermId.TRIAL_INSTANCE_STORAGE.getId()) {
			return true;
		}
		if (standardVariable.getStoredIn().getId() == TermId.LATITUDE_STORAGE.getId()) {
			return true;
		}
		if (standardVariable.getStoredIn().getId() == TermId.LONGITUDE_STORAGE.getId()) {
			return true;
		}
		if (standardVariable.getStoredIn().getId() == TermId.DATUM_STORAGE.getId()) {
			return true;
		}
		if (standardVariable.getStoredIn().getId() == TermId.ALTITUDE_STORAGE.getId()) {
			return true;
		}

		return false;
	}

	private Variable createLocationFactor(Geolocation geoLocation, VariableType variableType) {
		StandardVariable standardVariable = variableType.getStandardVariable();
		if (standardVariable.getStoredIn().getId() == TermId.TRIAL_ENVIRONMENT_INFO_STORAGE.getId()) {
			return new Variable(variableType, this.findLocationValue(variableType.getId(), geoLocation.getProperties()));
		}
		if (standardVariable.getStoredIn().getId() == TermId.TRIAL_INSTANCE_STORAGE.getId()) {
			return new Variable(variableType, geoLocation.getDescription());
		}
		if (standardVariable.getStoredIn().getId() == TermId.LATITUDE_STORAGE.getId()) {
			return new Variable(variableType, geoLocation.getLatitude());
		}
		if (standardVariable.getStoredIn().getId() == TermId.LONGITUDE_STORAGE.getId()) {
			return new Variable(variableType, geoLocation.getLongitude());
		}
		if (standardVariable.getStoredIn().getId() == TermId.DATUM_STORAGE.getId()) {
			return new Variable(variableType, geoLocation.getGeodeticDatum());
		}
		if (standardVariable.getStoredIn().getId() == TermId.ALTITUDE_STORAGE.getId()) {
			return new Variable(variableType, geoLocation.getAltitude());
		}
		return null;
	}

	private String findLocationValue(int stdVariableId, List<GeolocationProperty> properties) {
		if (properties != null) {
			for (GeolocationProperty property : properties) {
				if (property.getTypeId().equals(stdVariableId)) {
					return property.getValue();
				}
			}
		}
		return null;
	}

	private void addPlotExperimentFactors(VariableList variables, ExperimentModel experimentModel, VariableTypeList variableTypes,
			Map<Integer, StockModel> stockModelMap) throws MiddlewareQueryException {
		this.addExperimentFactors(variables, experimentModel, variableTypes);
		this.addGermplasmFactors(variables, experimentModel, variableTypes, stockModelMap);
	}

	private void addPlotExperimentFactors(VariableList variables, ExperimentModel experimentModel, VariableTypeList variableTypes,
			boolean hasVariableType) throws MiddlewareQueryException {
		this.addExperimentFactors(variables, experimentModel, variableTypes, hasVariableType);
		this.addGermplasmFactors(variables, experimentModel, variableTypes, null);
	}

	private void addGermplasmFactors(VariableList factors, ExperimentModel experimentModel, VariableTypeList variableTypes,
			Map<Integer, StockModel> stockModelMap) throws MiddlewareQueryException {
		List<ExperimentStock> experimentStocks = experimentModel.getExperimentStocks();
		if (experimentStocks != null && experimentStocks.size() == 1) {
			StockModel stockModel = null;
			if (stockModelMap != null && stockModelMap.get(experimentStocks.get(0).getStock().getStockId()) != null) {
				stockModel = stockModelMap.get(experimentStocks.get(0).getStock().getStockId());
			} else {
				stockModel = this.getStockBuilder().get(experimentStocks.get(0).getStock().getStockId());
			}

			for (VariableType variableType : variableTypes.getVariableTypes()) {
				if (this.isGermplasmFactor(variableType)) {
					factors.add(this.createGermplasmFactor(stockModel, variableType));
				}
			}
		}
	}

	private boolean isGermplasmFactor(VariableType variableType) {
		StandardVariable standardVariable = variableType.getStandardVariable();
		if (standardVariable.getStoredIn().getId() == TermId.GERMPLASM_ENTRY_STORAGE.getId()) {
			return true;
		}
		if (standardVariable.getStoredIn().getId() == TermId.ENTRY_NUMBER_STORAGE.getId()) {
			return true;
		}
		if (standardVariable.getStoredIn().getId() == TermId.ENTRY_GID_STORAGE.getId()) {
			return true;
		}
		if (standardVariable.getStoredIn().getId() == TermId.ENTRY_DESIGNATION_STORAGE.getId()) {
			return true;
		}
		if (standardVariable.getStoredIn().getId() == TermId.ENTRY_CODE_STORAGE.getId()) {
			return true;
		}
		return false;
	}

	private Variable createGermplasmFactor(StockModel stockModel, VariableType variableType) {
		StandardVariable standardVariable = variableType.getStandardVariable();
		if (standardVariable.getStoredIn().getId() == TermId.GERMPLASM_ENTRY_STORAGE.getId()) {
			return new Variable(variableType, this.findStockValue(variableType.getId(), stockModel.getProperties()));
		}
		if (standardVariable.getStoredIn().getId() == TermId.ENTRY_NUMBER_STORAGE.getId()) {
			return new Variable(variableType, stockModel.getUniqueName());
		}
		if (standardVariable.getStoredIn().getId() == TermId.ENTRY_GID_STORAGE.getId()) {
			return new Variable(variableType, stockModel.getDbxrefId());
		}
		if (standardVariable.getStoredIn().getId() == TermId.ENTRY_DESIGNATION_STORAGE.getId()) {
			return new Variable(variableType, stockModel.getName());
		}
		if (standardVariable.getStoredIn().getId() == TermId.ENTRY_CODE_STORAGE.getId()) {
			return new Variable(variableType, stockModel.getValue());
		}
		return null;
	}

	private String findStockValue(int stdVariableId, Set<StockProperty> properties) {
		if (properties != null) {
			for (StockProperty property : properties) {
				if (stdVariableId == property.getTypeId()) {
					return property.getValue();
				}
			}
		}
		return null;
	}

	private void addExperimentFactors(VariableList variables, ExperimentModel experimentModel, VariableTypeList variableTypes)
			throws MiddlewareQueryException {
		if (experimentModel.getProperties() != null) {
			for (ExperimentProperty property : experimentModel.getProperties()) {
				variables.add(this.createVariable(property, variableTypes));
			}
		}
	}

	private void addExperimentFactors(VariableList variables, ExperimentModel experimentModel, VariableTypeList variableTypes,
			boolean hasVariableType) throws MiddlewareQueryException {
		if (experimentModel.getProperties() != null) {
			for (ExperimentProperty property : experimentModel.getProperties()) {
				Variable var = this.createVariable(property, variableTypes, hasVariableType);
				if (var.getVariableType() != null) {
					variables.add(var);
				}
			}
		}
	}

	private Variable createVariable(ExperimentProperty property, VariableTypeList variableTypes) throws MiddlewareQueryException {
		Variable variable = new Variable();
		variable.setVariableType(variableTypes.findById(property.getTypeId()));
		variable.setValue(property.getValue());
		return variable;
	}

	private Variable createVariable(ExperimentProperty property, VariableTypeList variableTypes, boolean hasVariableType)
			throws MiddlewareQueryException {
		Variable variable = new Variable();
		variable.setVariableType(variableTypes.findById(property.getTypeId()), hasVariableType);
		variable.setValue(property.getValue());
		return variable;
	}

	public ExperimentModel getExperimentModel(int experimentId) throws MiddlewareQueryException {
		return this.getExperimentDao().getById(experimentId);
	}

	public boolean hasFieldmap(int datasetId) throws MiddlewareQueryException {
		return this.getExperimentDao().hasFieldmap(datasetId);
	}

	public boolean checkIfStudyHasFieldmap(int studyId) throws MiddlewareQueryException {
		List<Integer> geolocationIdsOfStudy = this.getExperimentDao().getLocationIdsOfStudy(studyId);
		List<Integer> geolocationIdsOfStudyWithFieldmap = this.getExperimentDao().getLocationIdsOfStudyWithFieldmap(studyId);
		return geolocationIdsOfStudy.size() == geolocationIdsOfStudyWithFieldmap.size();
	}
}
