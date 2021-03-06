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

import org.generationcp.middleware.domain.dms.ExperimentType;
import org.generationcp.middleware.domain.dms.StudyValues;
import org.generationcp.middleware.domain.dms.VariableTypeList;
import org.generationcp.middleware.domain.study.StudyTypeDto;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.manager.DaoFactory;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.generationcp.middleware.pojos.workbench.CropType;

/**
 * Saves a study (the corresponding Project, ProjectProperty, ProjectRelationship entries) to the database.
 *
 * @author Joyce Avestro
 *
 */
public class StudySaver extends Saver {

	private DaoFactory daoFactory;

	public StudySaver(final HibernateSessionProvider sessionProviderForLocal) {
		super(sessionProviderForLocal);
		this.daoFactory = new DaoFactory(sessionProvider);
	}

	/**
	 * Saves a study. Creates an entry in project and projectprop tables. Creates an entry in nd_experiment
	 * table if saveStudyExperiment is true.
	 */
	public DmsProject saveStudy(final CropType crop, final int parentId, final VariableTypeList variableTypeList, final StudyValues studyValues, final boolean saveStudyExperiment,
		final String programUUID, final StudyTypeDto studyType, final String description, final String startDate,
		final String endDate, final String objective, final String name, final String createdBy) throws Exception {

		DmsProject project = this.getProjectSaver().create(studyValues, studyType, description, startDate, endDate, objective, name,
			createdBy, parentId);

		project.setProgramUUID(programUUID);

		project = this.daoFactory.getDmsProjectDAO().save(project);
		this.getProjectPropertySaver().saveProjectProperties(project, variableTypeList, studyValues.getVariableList());
		if (saveStudyExperiment) {
			this.saveStudyExperiment(crop, project.getProjectId(), studyValues);
		}
		return project;

	}

	/**
	 * Creates an entry in nd_experiment table if saveStudyExperiment is true.
	 */
	public void saveStudyExperiment(final CropType crop, final int projectId, final StudyValues values) throws Exception {
		try {
			this.getExperimentModelSaver().addExperiment(crop, projectId, ExperimentType.STUDY_INFORMATION, values);
		} catch (final Exception e) {
			throw e;
		}
	}

}
