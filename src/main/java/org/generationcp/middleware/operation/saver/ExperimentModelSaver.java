/*******************************************************************************
 *
 * Generation Challenge Programme (GCP) Copyright (c) 2012, All Rights Reserved.
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package org.generationcp.middleware.operation.saver;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.generationcp.middleware.domain.dms.DatasetReference;
import org.generationcp.middleware.domain.dms.ExperimentType;
import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.dms.StudyValues;
import org.generationcp.middleware.domain.dms.Values;
import org.generationcp.middleware.domain.dms.Variable;
import org.generationcp.middleware.domain.dms.VariableList;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.manager.DaoFactory;
import org.generationcp.middleware.operation.builder.StockModelBuilder;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.generationcp.middleware.pojos.dms.ExperimentModel;
import org.generationcp.middleware.pojos.dms.ExperimentProperty;
import org.generationcp.middleware.pojos.dms.Geolocation;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.service.api.ObservationUnitIDGenerator;
import org.generationcp.middleware.service.impl.study.ObservationUnitIDGeneratorImpl;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class ExperimentModelSaver {
	
	private DaoFactory daoFactory;
	private PhenotypeSaver phenotypeSaver;
	private GeolocationSaver geolocationSaver;
	private StockModelBuilder stockModelBuilder;

	public ExperimentModelSaver(final HibernateSessionProvider sessionProvider) {
		this.daoFactory = new DaoFactory(sessionProvider);
		this.phenotypeSaver = new PhenotypeSaver(sessionProvider);
		this.geolocationSaver = new GeolocationSaver(sessionProvider);
		this.stockModelBuilder = new StockModelBuilder(sessionProvider);
	}

	public void addExperiment(final CropType crop, final int projectId, final ExperimentType experimentType, final Values values) {
		final TermId myExperimentType = this.mapExperimentType(experimentType);
		final ExperimentModel experimentModel = this.create(crop, projectId, values, myExperimentType);

		this.daoFactory.getExperimentDao().save(experimentModel);
		this.phenotypeSaver.savePhenotypes(experimentModel, values.getVariableList());
	}

	public void addOrUpdateExperiment(final CropType crop, final int projectId, final ExperimentType experimentType, final Values values) {
		final int experimentId =
				this.daoFactory.getExperimentDao().getExperimentIdByLocationIdStockId(projectId, values.getLocationId(),
						values.getGermplasmId());
		if(experimentId != 0 ) {
			for (final Variable variable : values.getVariableList().getVariables()) {
				final int val = this.daoFactory.getPhenotypeDAO()
						.updatePhenotypesByExperimentIdAndObervableId(experimentId, variable.getVariableType().getId(), variable.getValue());
				if (val == 0) {
					this.phenotypeSaver.save(experimentId, variable);
				}
			}
		} else {
			TermId myExperimentType = null;
			if (values instanceof StudyValues) {
				myExperimentType = TermId.STUDY_EXPERIMENT;
			} else {
				myExperimentType = this.mapExperimentType(experimentType);
			}

			final ExperimentModel experimentModel = this.create(crop, projectId, values, myExperimentType);

			this.daoFactory.getExperimentDao().save(experimentModel);
			this.phenotypeSaver.savePhenotypes(experimentModel, values.getVariableList());
		}
	}

	private TermId mapExperimentType(final ExperimentType experimentType) {
		switch (experimentType) {
			case PLOT:
				return TermId.PLOT_EXPERIMENT;
			case AVERAGE:
				return TermId.AVERAGE_EXPERIMENT;
			case SUMMARY:
				return TermId.SUMMARY_EXPERIMENT;
			case SAMPLE:
				return TermId.SAMPLE_EXPERIMENT;
			case STUDY_INFORMATION:
				return TermId.STUDY_INFORMATION;
			case TRIAL_ENVIRONMENT:
				return TermId.TRIAL_ENVIRONMENT_EXPERIMENT;
		}
		return null;
	}

	private ExperimentModel create(final CropType crop, final int projectId, final Values values, final TermId expType) {
		final ExperimentModel experimentModel = new ExperimentModel();
		final DmsProject project = new DmsProject();
		project.setProjectId(projectId);
		experimentModel.setProject(project);
		experimentModel.setTypeId(expType.getId());
		experimentModel.setProperties(this.createTrialDesignExperimentProperties(experimentModel, values.getVariableList()));

		if (values.getLocationId() == null && values instanceof StudyValues) {
			experimentModel.setGeoLocation(this.createNewGeoLocation());
		} else if (values.getLocationId() != null) {
			experimentModel.setGeoLocation(this.daoFactory.getGeolocationDao().getById(values.getLocationId()));
		}
		if (values.getGermplasmId() != null) {
			experimentModel.setStock(this.stockModelBuilder.get(values.getGermplasmId()));
		}
		final ObservationUnitIDGenerator observationUnitIDGenerator = new ObservationUnitIDGeneratorImpl();
		observationUnitIDGenerator.generateObservationUnitIds(crop, Arrays.asList(experimentModel));
		return experimentModel;
	}

	// GCP-8092 Nurseries will always have a unique geolocation, no more concept of shared/common geolocation
	public Geolocation createNewGeoLocation() {
		final Geolocation location = new Geolocation();
		location.setDescription("1");
		this.daoFactory.getGeolocationDao().save(location);
		return location;
	}

	protected List<ExperimentProperty> createTrialDesignExperimentProperties(final ExperimentModel experimentModel, final VariableList factors) {

		final List<ExperimentProperty> experimentProperties = new ArrayList<>();

		if (factors != null && factors.getVariables() != null && !factors.getVariables().isEmpty()) {
			for (final Variable variable : factors.getVariables()) {
				if (PhenotypicType.TRIAL_DESIGN == variable.getVariableType().getRole()) {
					experimentProperties.add(this.createTrialDesignProperty(experimentModel, variable));
				}
			}
		}

		return experimentProperties;
	}

	protected ExperimentProperty createTrialDesignProperty(final ExperimentModel experimentModel, final Variable variable) {

		final ExperimentProperty experimentProperty = new ExperimentProperty();
		experimentProperty.setExperiment(experimentModel);
		experimentProperty.setTypeId(variable.getVariableType().getId());

		if (variable.getVariableType().getStandardVariable().getDataType().getId() == TermId.CATEGORICAL_VARIABLE.getId()) {
			// If the variable is categorical, the variable's categorical value should be saved as categorical id.
			experimentProperty.setValue(variable.getIdValue());
		} else {
			experimentProperty.setValue(variable.getValue());
		}

		experimentProperty.setRank(variable.getVariableType().getRank());

		return experimentProperty;
	}

	public int moveStudyToNewGeolocation(final int studyId) {
		final List<DatasetReference> datasets = this.daoFactory.getDmsProjectDAO().getDirectChildDatasetsOfStudy(studyId);
		final List<Integer> ids = new ArrayList<>();
		ids.add(studyId);
		if (datasets != null) {
			for (final DatasetReference dataset : datasets) {
				ids.add(dataset.getId());
			}
		}

		final Geolocation location = this.geolocationSaver.createMinimumGeolocation();
		final List<ExperimentModel> experiments = this.daoFactory.getExperimentDao().getExperimentsByProjectIds(ids);
		if (experiments != null && !experiments.isEmpty()) {
			for (final ExperimentModel experiment : experiments) {
				if (experiment.getGeoLocation().getLocationId().intValue() == 1) {
					experiment.setGeoLocation(location);
					this.daoFactory.getExperimentDao().update(experiment);
				}
			}
		}

		return location.getLocationId();
	}
}
