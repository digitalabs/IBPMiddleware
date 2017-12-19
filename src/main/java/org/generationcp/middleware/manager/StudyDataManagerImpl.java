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

package org.generationcp.middleware.manager;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.dao.dms.DmsProjectDao;
import org.generationcp.middleware.dao.dms.InstanceMetadata;
import org.generationcp.middleware.dao.dms.PhenotypeOutlierDao;
import org.generationcp.middleware.domain.dms.DMSVariableType;
import org.generationcp.middleware.domain.dms.DataSet;
import org.generationcp.middleware.domain.dms.DataSetType;
import org.generationcp.middleware.domain.dms.DatasetReference;
import org.generationcp.middleware.domain.dms.DatasetValues;
import org.generationcp.middleware.domain.dms.Experiment;
import org.generationcp.middleware.domain.dms.ExperimentType;
import org.generationcp.middleware.domain.dms.ExperimentValues;
import org.generationcp.middleware.domain.dms.FolderReference;
import org.generationcp.middleware.domain.dms.Reference;
import org.generationcp.middleware.domain.dms.Stocks;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.StudyReference;
import org.generationcp.middleware.domain.dms.StudySummary;
import org.generationcp.middleware.domain.dms.StudyValues;
import org.generationcp.middleware.domain.dms.TrialEnvironments;
import org.generationcp.middleware.domain.dms.VariableList;
import org.generationcp.middleware.domain.dms.VariableTypeList;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.fieldbook.FieldMapDatasetInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapInfo;
import org.generationcp.middleware.domain.fieldbook.FieldMapLabel;
import org.generationcp.middleware.domain.fieldbook.FieldMapTrialInstanceInfo;
import org.generationcp.middleware.domain.fieldbook.FieldmapBlockInfo;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.search.StudyResultSet;
import org.generationcp.middleware.domain.search.StudyResultSetByGid;
import org.generationcp.middleware.domain.search.StudyResultSetByNameStartDateSeasonCountry;
import org.generationcp.middleware.domain.search.StudyResultSetByParentFolder;
import org.generationcp.middleware.domain.search.filter.BrowseStudyQueryFilter;
import org.generationcp.middleware.domain.search.filter.GidStudyQueryFilter;
import org.generationcp.middleware.domain.search.filter.ParentFolderStudyQueryFilter;
import org.generationcp.middleware.domain.search.filter.StudyQueryFilter;
import org.generationcp.middleware.domain.workbench.StudyNode;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Person;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.generationcp.middleware.pojos.dms.ExperimentModel;
import org.generationcp.middleware.pojos.dms.Geolocation;
import org.generationcp.middleware.pojos.dms.Phenotype;
import org.generationcp.middleware.pojos.dms.PhenotypeOutlier;
import org.generationcp.middleware.pojos.dms.ProjectProperty;
import org.generationcp.middleware.service.api.PedigreeService;
import org.generationcp.middleware.service.api.study.StudyFilters;
import org.generationcp.middleware.service.api.study.StudyMetadata;
import org.generationcp.middleware.service.pedigree.PedigreeFactory;
import org.generationcp.middleware.util.CrossExpansionProperties;
import org.generationcp.middleware.util.PlotUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Transactional
public class StudyDataManagerImpl extends DataManager implements StudyDataManager {

	private static final Logger LOG = LoggerFactory.getLogger(StudyDataManagerImpl.class);
	private PedigreeService pedigreeService;
	private LocationDataManager locationDataManager;

	public StudyDataManagerImpl() {
	}

	public StudyDataManagerImpl(final HibernateSessionProvider sessionProvider, final String databaseName) {
		super(sessionProvider, databaseName);
		this.locationDataManager = new LocationDataManagerImpl(sessionProvider);
		this.pedigreeService = this.getPedigreeService();
	}

	public StudyDataManagerImpl(final HibernateSessionProvider sessionProvider) {
		super(sessionProvider);
		this.locationDataManager = new LocationDataManagerImpl(sessionProvider);
		this.pedigreeService = this.getPedigreeService();
	}

	private PedigreeService getPedigreeService() {
		if (ManagerFactory.getCurrentManagerFactoryThreadLocal().get() != null) {
			return ManagerFactory.getCurrentManagerFactoryThreadLocal().get().getPedigreeService();
		}
		// we will just return default pedigree service
		return PedigreeFactory.getPedigreeService(this.sessionProvider, null, null);
	}

	@Override
	public Study getStudy(final int studyId) throws MiddlewareException {
		return this.getStudyBuilder().createStudy(studyId);
	}

	@Override
	public Study getStudy(final int studyId, final boolean hasVariableType) throws MiddlewareException {
		return this.getStudyBuilder().createStudy(studyId, hasVariableType);
	}

	@Override
	public Integer getStudyIdByNameAndProgramUUID(final String studyName, final String programUUID) {
		return this.getDmsProjectDao().getProjectIdByNameAndProgramUUID(studyName, programUUID, TermId.IS_STUDY);
	}

	@Override
	public boolean checkIfProjectNameIsExistingInProgram(final String name, final String programUUID) {
		return this.getDmsProjectDao().checkIfProjectNameIsExistingInProgram(name, programUUID);
	}

	@Override
	public List<Reference> getRootFolders(final String programUUID, final List<StudyType> studyTypes) {
		return this.getDmsProjectDao().getRootFolders(programUUID, studyTypes);
	}

	@Override
	public List<Reference> getChildrenOfFolder(final int folderId, final String programUUID, final List<StudyType> studyTypes)
			 {
		return this.getDmsProjectDao().getChildrenOfFolder(folderId, programUUID, studyTypes);
	}

	@Override
	public List<DatasetReference> getDatasetReferences(final int studyId) {
		return this.getDmsProjectDao().getDatasetNodesByStudyId(studyId);
	}

	@Override
	public DataSet getDataSet(final int dataSetId) throws MiddlewareException {
		return this.getDataSetBuilder().build(dataSetId);
	}

	@Override
	public VariableTypeList getAllStudyFactors(final int studyId) throws MiddlewareException {
		return this.getStudyFactorBuilder().build(studyId);
	}

	@Override
	public VariableTypeList getAllStudyVariates(final int studyId) throws MiddlewareException {
		return this.getStudyVariateBuilder().build(studyId);
	}

	@Override
	public StudyResultSet searchStudies(final StudyQueryFilter filter, final int numOfRows) {
		if (filter instanceof ParentFolderStudyQueryFilter) {
			return new StudyResultSetByParentFolder((ParentFolderStudyQueryFilter) filter, numOfRows, this.sessionProvider);
		} else if (filter instanceof GidStudyQueryFilter) {
			return new StudyResultSetByGid((GidStudyQueryFilter) filter, numOfRows, this.sessionProvider);
		} else if (filter instanceof BrowseStudyQueryFilter) {
			return new StudyResultSetByNameStartDateSeasonCountry((BrowseStudyQueryFilter) filter, this.sessionProvider);
		}
		return null;
	}

	@Override
	public StudyReference addStudy(final int parentFolderId, final VariableTypeList variableTypeList, final StudyValues studyValues,
		final String programUUID, final String cropPrefix, final StudyType studyType, final String description) {

		try {

			final DmsProject project = this.getStudySaver().saveStudy(parentFolderId, variableTypeList, studyValues, true, programUUID,
					cropPrefix, studyType, description);

			return new StudyReference(project.getProjectId(), project.getName(), project.getDescription());

		} catch (final Exception e) {

			throw new MiddlewareQueryException("Error encountered with addStudy(folderId=" + parentFolderId + ", variableTypeList="
					+ variableTypeList + ", studyValues=" + studyValues + "): " + e.getMessage(), e);
		}

	}

	@Override
	public DatasetReference addDataSet(final int studyId, final VariableTypeList variableTypeList, final DatasetValues datasetValues,
			final String programUUID) {

		try {

			final DmsProject datasetProject =
					this.getDatasetProjectSaver().addDataSet(studyId, variableTypeList, datasetValues, programUUID);

			return new DatasetReference(datasetProject.getProjectId(), datasetProject.getName(), datasetProject.getDescription());

		} catch (final Exception e) {

			throw new MiddlewareQueryException("error in addDataSet " + e.getMessage(), e);
		}
	}

	@Override
	public List<Experiment> getExperiments(final int dataSetId, final int start, final int numRows) throws MiddlewareException {
		final VariableTypeList variableTypes = this.getDataSetBuilder().getVariableTypes(dataSetId);
		return this.getExperimentBuilder().build(dataSetId, PlotUtil.getAllPlotTypes(), start, numRows, variableTypes);
	}

	@Override
	public List<Experiment> getExperimentsWithTrialEnvironment(final int trialDataSetId, final int dataSetId, final int start,
			final int numRows) throws MiddlewareException {
		final VariableTypeList trialVariableTypes = this.getDataSetBuilder().getVariableTypes(trialDataSetId);
		final VariableTypeList variableTypes = this.getDataSetBuilder().getVariableTypes(dataSetId);

		variableTypes.addAll(trialVariableTypes);

		return this.getExperimentBuilder().build(dataSetId, PlotUtil.getAllPlotTypes(), start, numRows, variableTypes);
	}

	@Override
	public List<Experiment> getExperiments(final int dataSetId, final int start, final int numOfRows, final VariableTypeList varTypeList)
			throws MiddlewareException {
		if (varTypeList == null) {
			return this.getExperiments(dataSetId, start, numOfRows);
		} else {
			return this.getExperimentBuilder().build(dataSetId, PlotUtil.getAllPlotTypes(), start, numOfRows, varTypeList);
		}
	}

	@Override
	public long countExperiments(final int dataSetId) {
		return this.getExperimentBuilder().count(dataSetId);
	}

	@Override
	public void addExperiment(final int dataSetId, final ExperimentType experimentType, final ExperimentValues experimentValues,
			final String cropPrefix)
			 {

		try {

			this.getExperimentModelSaver().addExperiment(dataSetId, experimentType, experimentValues, cropPrefix);

		} catch (final Exception e) {

			throw new MiddlewareQueryException("error in addExperiment " + e.getMessage(), e);
		}
	}

	@Override
	public void addOrUpdateExperiment(final int dataSetId, final ExperimentType experimentType,
			final List<ExperimentValues> experimentValuesList, final String cropPrefix) {

		try {

			for (final ExperimentValues experimentValues : experimentValuesList) {
				this.getExperimentModelSaver().addOrUpdateExperiment(dataSetId, experimentType, experimentValues, cropPrefix);
			}

		} catch (final Exception e) {

			throw new MiddlewareQueryException("error in addOrUpdateExperiment " + e.getMessage(), e);
		}

	}

	@Override
	public int addTrialEnvironment(final VariableList variableList) {

		try {

			final Geolocation geolocation = this.getGeolocationSaver().saveGeolocation(variableList, null, false);
			final int id = geolocation.getLocationId();

			return id;

		} catch (final Exception e) {

			throw new MiddlewareQueryException("error in addTrialEnvironment " + e.getMessage(), e);
		}
	}

	@Override
	public int addStock(final VariableList variableList) {

		try {

			final int id = this.getStockSaver().saveStock(variableList);

			return id;

		} catch (final Exception e) {

			throw new MiddlewareQueryException("error in addStock " + e.getMessage(), e);
		}
	}

	@Override
	public List<DataSet> getDataSetsByType(final int studyId, final DataSetType dataSetType) throws MiddlewareException {

		final List<DmsProject> datasetProjects = this.getDmsProjectDao().getDataSetsByStudyAndProjectProperty(studyId,
				TermId.DATASET_TYPE.getId(), String.valueOf(dataSetType.getId()));
		final List<DataSet> datasets = new ArrayList<>();

		for (final DmsProject datasetProject : datasetProjects) {
			datasets.add(this.getDataSetBuilder().build(datasetProject.getProjectId()));
		}

		return datasets;
	}

	@Override
	public long countExperimentsByTrialEnvironmentAndVariate(final int trialEnvironmentId, final int variateVariableId)
			 {
		long count = 0;
		count = this.getExperimentDao().countByTrialEnvironmentAndVariate(trialEnvironmentId, variateVariableId);
		return count;
	}

	@Override
	public void addDataSetVariableType(final int datasetId, final DMSVariableType variableType) {

		try {

			this.getDatasetProjectSaver().addDatasetVariableType(datasetId, variableType);

		} catch (final Exception e) {

			throw new MiddlewareQueryException("error in addDataSetVariableType " + e.getMessage(), e);
		}
	}

	@Override
	public TrialEnvironments getTrialEnvironmentsInDataset(final int datasetId) throws MiddlewareException {
		final DmsProject study = this.getProjectRelationshipDao().getObjectBySubjectIdAndTypeId(datasetId, TermId.BELONGS_TO_STUDY.getId());
		return this.getTrialEnvironmentBuilder().getTrialEnvironmentsInDataset(study.getProjectId(), datasetId);
	}

	@Override
	public Stocks getStocksInDataset(final int datasetId) throws MiddlewareException {
		return this.getStockBuilder().getStocksInDataset(datasetId);
	}

	@Override
	public long countStocks(final int datasetId, final int trialEnvironmentId, final int variateStdVarId) {
		return this.getStockDao().countStocks(datasetId, trialEnvironmentId, variateStdVarId);
	}

	@Override
	public long countObservations(final int datasetId, final int trialEnvironmentId, final int variateStdVarId)
			 {
		return this.getStockDao().countObservations(datasetId, trialEnvironmentId, variateStdVarId);
	}

	@Override
	public DataSet findOneDataSetByType(final int studyId, final DataSetType dataSetType) throws MiddlewareException {
		final List<DataSet> datasets = this.getDataSetsByType(studyId, dataSetType);
		if (datasets != null && !datasets.isEmpty()) {
			return datasets.get(0);
		}
		return null;
	}

	@Override
	public DatasetReference findOneDataSetReferenceByType(final int studyId, final DataSetType type) {
		final List<DmsProject> datasetProjects = this.getDmsProjectDao().getDataSetsByStudyAndProjectProperty(studyId,
				TermId.DATASET_TYPE.getId(), String.valueOf(type.getId()));
		if (datasetProjects != null && !datasetProjects.isEmpty()) {
			final DmsProject dataSetProject = datasetProjects.get(0);
			return new DatasetReference(dataSetProject.getProjectId(), dataSetProject.getName(), dataSetProject.getDescription());
		}
		return null;
	}

	@Override
	public void deleteDataSet(final int datasetId) {

		try {

			this.getDataSetDestroyer().deleteDataSet(datasetId);

		} catch (final Exception e) {

			throw new MiddlewareQueryException("error in deleteDataSet " + e.getMessage(), e);
		}
	}

	@Override
	public void deleteExperimentsByLocation(final int datasetId, final int locationId) {

		try {

			this.getDataSetDestroyer().deleteExperimentsByLocation(datasetId, locationId);

		} catch (final Exception e) {

			throw new MiddlewareQueryException("error in deleteExperimentsByLocation " + e.getMessage(), e);
		}
	}

	@Override
	public String getLocalNameByStandardVariableId(final Integer projectId, final Integer standardVariableId)
			 {
		DmsProject dmsProject = new DmsProject();
		dmsProject.setProjectId(projectId);
		ProjectProperty projectProperty = getProjectPropertyDao().getByStandardVariableId(dmsProject, standardVariableId);
		return (projectProperty == null) ? null : projectProperty.getAlias();
	}

	@Override
	public List<StudyNode> getAllNurseryAndTrialStudyNodes(final String programUUID) {
		return this.getDmsProjectDao().getAllNurseryAndTrialStudyNodes(programUUID);
	}

	@Override
	public List<FieldMapInfo> getFieldMapInfoOfStudy(List<Integer> studyIdList, StudyType studyType,
			CrossExpansionProperties crossExpansionProperties) {
		List<FieldMapInfo> fieldMapInfos = new ArrayList<>();
		for (Integer studyId : studyIdList) {
			FieldMapInfo fieldMapInfo = new FieldMapInfo();

			fieldMapInfo.setFieldbookId(studyId);
			fieldMapInfo.setFieldbookName(this.getDmsProjectDao().getById(studyId).getName());

			if (studyType == StudyType.T) {
				fieldMapInfo.setTrial(true);
			} else {
				fieldMapInfo.setTrial(false);
			}

			final List<FieldMapDatasetInfo> fieldMapDatasetInfos = this.getExperimentPropertyDao().getFieldMapLabels(studyId);
			fieldMapInfo.setDatasets(fieldMapDatasetInfos);

			fieldMapInfos.add(fieldMapInfo);
		}

		this.updateFieldMapInfoWithBlockInfo(fieldMapInfos);

		return fieldMapInfos;
	}

	private void setPedigree(List<FieldMapDatasetInfo> fieldMapDatasetInfos, CrossExpansionProperties crossExpansionProperties,
			Map<Integer, String> pedigreeStringMap) {
		//TODO: Caching of the pedigree string is just a temporary fix. This must be properly fixed.
		for (FieldMapDatasetInfo fieldMapDatasetInfo : fieldMapDatasetInfos) {
			List<FieldMapTrialInstanceInfo> trialInstances = fieldMapDatasetInfo.getTrialInstances();
			if (trialInstances == null || trialInstances.isEmpty()) {
				continue;
			}
			for (FieldMapTrialInstanceInfo trialInstance : trialInstances) {
				List<FieldMapLabel> labels = trialInstance.getFieldMapLabels();
				for (FieldMapLabel label : labels) {
					this.setPedigree(label, crossExpansionProperties, pedigreeStringMap);
				}
			}
		}
	}

	private void setPedigree(FieldMapLabel label, CrossExpansionProperties crossExpansionProperties, Map<Integer, String> pedigreeStringMap) {
	
		final Integer gid = label.getGid();
		final String cachedPedigreeString = pedigreeStringMap.get(gid);
		if (StringUtils.isNotBlank(cachedPedigreeString)){
			label.setPedigree(cachedPedigreeString);
		} else {
			String pedigree = this.pedigreeService.getCrossExpansion(gid, crossExpansionProperties);
			label.setPedigree(pedigree);
			pedigreeStringMap.put(gid, pedigree);
		}
}

	@Override
	public void saveOrUpdateFieldmapProperties(final List<FieldMapInfo> info, final int userId, final boolean isNew)
			 {

		if (info != null && !info.isEmpty()) {

			try {

				if (isNew) {
					this.getLocdesSaver().saveLocationDescriptions(info, userId);
				} else {
					this.getLocdesSaver().updateDeletedPlots(info, userId);
				}
				this.getGeolocationPropertySaver().saveFieldmapProperties(info);
				this.getExperimentPropertySaver().saveFieldmapProperties(info);

			} catch (final Exception e) {

				throw new MiddlewareQueryException("Error encountered with saveOrUpdateFieldmapProperties(): " + e.getMessage(), e);
			}
		}

	}

	@Override
	public void saveTrialDatasetSummary(final DmsProject project, final VariableTypeList variableTypeList,
			final List<ExperimentValues> experimentValues, final List<Integer> locationIds) {

		try {

			if (variableTypeList != null && variableTypeList.getVariableTypes() != null && !variableTypeList.getVariableTypes().isEmpty()) {
				this.getProjectPropertySaver().saveProjectProperties(project, variableTypeList, null);
			}
			if (experimentValues != null && !experimentValues.isEmpty()) {
				this.updateExperimentValues(experimentValues, project.getProjectId(), locationIds);
			}

		} catch (final Exception e) {

			throw new MiddlewareQueryException("error in saveTrialDatasetSummary " + e.getMessage(), e);
		}
	}

	private void updateExperimentValues(final List<ExperimentValues> experimentValues, final Integer projectId,
			final List<Integer> locationIds) {

		for (final ExperimentValues exp : experimentValues) {
			if (exp.getVariableList() != null && !exp.getVariableList().isEmpty()) {
				final ExperimentModel experimentModel =
						this.getExperimentDao().getExperimentByProjectIdAndLocation(projectId, exp.getLocationId());
				this.getPhenotypeSaver().savePhenotypes(experimentModel, exp.getVariableList());
			}
		}
	}

	@Override
	public List<FieldMapInfo> getAllFieldMapsInBlockByTrialInstanceId(final int datasetId, final int geolocationId,
			final CrossExpansionProperties crossExpansionProperties) {
		List<FieldMapInfo> fieldMapInfos = new ArrayList<>();

		fieldMapInfos = this.getExperimentPropertyDao().getAllFieldMapsInBlockByTrialInstanceId(datasetId, geolocationId, null);

		final int blockId = this.getBlockId(fieldMapInfos);
		final FieldmapBlockInfo blockInfo = this.locationDataManager.getBlockInformation(blockId);
		this.updateFieldMapWithBlockInformation(fieldMapInfos, blockInfo, true);
		final Map<Integer, String> pedigreeStringMap = new HashMap<>();
//		 Filter those belonging to the given geolocationId
		for (final FieldMapInfo fieldMapInfo : fieldMapInfos) {
			final List<FieldMapDatasetInfo> datasetInfoList = fieldMapInfo.getDatasets();
			if (datasetInfoList != null) {
				this.setPedigree(datasetInfoList, crossExpansionProperties, pedigreeStringMap);
			}
		}

		return fieldMapInfos;
	}

	@Override
	public List<FieldMapInfo> getAllFieldMapsInBlockByBlockId(final int blockId) {

		List<FieldMapInfo> fieldMapInfos = new ArrayList<>();

		fieldMapInfos = this.getExperimentPropertyDao().getAllFieldMapsInBlockByTrialInstanceId(0, 0, blockId);

		final FieldmapBlockInfo blockInfo = this.locationDataManager.getBlockInformation(blockId);
		this.updateFieldMapWithBlockInformation(fieldMapInfos, blockInfo);

		return fieldMapInfos;
	}

	@Override
	public boolean isStudy(final int id) {
		return this.getProjectRelationshipDao().isSubjectTypeExisting(id, TermId.STUDY_HAS_FOLDER.getId());
	}

	@Override
	public boolean renameSubFolder(final String newFolderName, final int folderId, final String programUUID)
			 {

		// check for existing folder name
		final boolean isExisting = this.getDmsProjectDao().checkIfProjectNameIsExistingInProgram(newFolderName, programUUID);
		if (isExisting) {
			throw new MiddlewareQueryException("Folder name is not unique");
		}

		try {

			final DmsProject currentFolder = this.getDmsProjectDao().getById(folderId);
			currentFolder.setName(newFolderName);
			this.getDmsProjectDao().saveOrUpdate(currentFolder);

			return true;
		} catch (final Exception e) {

			throw new MiddlewareQueryException(
					"Error encountered with renameFolder(folderId=" + folderId + ", name=" + newFolderName + ": " + e.getMessage(), e);
		}
	}

	@Override
	public int addSubFolder(final int parentFolderId, final String name, final String description, final String programUUID)
			 {
		final DmsProject parentProject = this.getDmsProjectDao().getById(parentFolderId);
		if (parentProject == null) {
			throw new MiddlewareQueryException("DMS Project is not existing");
		}
		final boolean isExisting = this.getDmsProjectDao().checkIfProjectNameIsExistingInProgram(name, programUUID);
		if (isExisting) {
			throw new MiddlewareQueryException("Folder name is not unique");
		}

		try {

			final DmsProject project = this.getProjectSaver().saveFolder(parentFolderId, name, description, programUUID);

			return project.getProjectId();
		} catch (final Exception e) {

			throw new MiddlewareQueryException("Error encountered with addSubFolder(parentFolderId=" + parentFolderId + ", name=" + name
					+ ", description=" + description + "): " + e.getMessage(), e);
		}
	}

	@Override
	public boolean moveDmsProject(final int sourceId, final int targetId, final boolean isAStudy) {

		final DmsProject source = this.getDmsProjectDao().getById(sourceId);
		final DmsProject target = this.getDmsProjectDao().getById(targetId);
		if (source == null) {
			throw new MiddlewareQueryException("Source Project is not existing");
		}

		if (target == null) {
			throw new MiddlewareQueryException("Target Project is not existing");
		}

		try {

			// disassociate the source project from any parent it had previously
			this.getProjectRelationshipDao().deleteChildAssociation(sourceId);

			this.getProjectRelationshipSaver().saveProjectParentRelationship(source, targetId, isAStudy);

			return true;
		} catch (final MiddlewareException e) {

			StudyDataManagerImpl.LOG.error(e.getMessage(), e);
			return false;
		}
	}

	@Override
	public void deleteEmptyFolder(final int id, final String programUUID) {
		final DmsProjectDao dmsProjectDao = this.getDmsProjectDao();
		// check if folder is existing
		final DmsProject project = dmsProjectDao.getById(id);
		if (project == null) {
			throw new MiddlewareQueryException("Folder is not existing");
		}
		// check if folder has no children
		final List<Reference> children = dmsProjectDao.getChildrenOfFolder(id, programUUID, StudyType.nurseriesAndTrials());
		if (children != null && !children.isEmpty()) {
			throw new MiddlewareQueryException("Folder is not empty");
		}

		try {

			// modify the folder name
			final String name = project.getName() + "#" + Math.random();
			project.setName(name);
			dmsProjectDao.saveOrUpdate(project);
			this.getProjectRelationshipDao().deleteByProjectId(project.getProjectId());

		} catch (final Exception e) {

			throw new MiddlewareQueryException("Error encountered with deleteEmptyFolder(id=" + id + "): " + e.getMessage(), e);
		}
	}

	@Override
	public boolean isFolderEmpty(final int id, final String programUUID, final List<StudyType> studyTypes) {
		final DmsProjectDao dmsProjectDao = this.getDmsProjectDao();

		// check if folder has no children
		final List<Reference> children = dmsProjectDao.getChildrenOfFolder(id, programUUID, studyTypes);
		if (children == null || children.isEmpty()) {
			return true;
		}
		return false;
	}

	@Override
	public DmsProject getParentFolder(final int id) {

		final DmsProject folderParentFolder =
				this.getProjectRelationshipDao().getObjectBySubjectIdAndTypeId(id, TermId.HAS_PARENT_FOLDER.getId());
		final DmsProject studyParentFolder =
				this.getProjectRelationshipDao().getObjectBySubjectIdAndTypeId(id, TermId.STUDY_HAS_FOLDER.getId());
		if (studyParentFolder != null) {
			return studyParentFolder;
		}
		return folderParentFolder;
	}

	@Override
	public Integer getProjectIdByStudyDbId(final Integer studyDbId) {
		return this.getDmsProjectDao().getProjectIdByStudyDbId(studyDbId);
	}

	@Override
	public DmsProject getProject(final int id) {
		return this.getDmsProjectDao().getById(id);
	}

	@Override
	public List<StudyDetails> getStudyDetails(final StudyType studyType, final String programUUID, final int start, final int numOfRows)
			 {
		final List<StudyDetails> details = this.getDmsProjectDao().getAllStudyDetails(studyType, programUUID, start, numOfRows);
		this.populateSiteAndPersonIfNecessary(details);
		return details;
	}

	@Override
	public StudyDetails getStudyDetails(final StudyType studyType, final int studyId) {
		final StudyDetails studyDetails = this.getDmsProjectDao().getStudyDetails(studyType, studyId);
		this.populateSiteAnPersonIfNecessary(studyDetails);
		return studyDetails;
	}

	@Override
	public List<StudyDetails> getNurseryAndTrialStudyDetails(final String programUUID, final int start, final int numOfRows)
			 {
		final List<StudyDetails> list = this.getDmsProjectDao().getAllNurseryAndTrialStudyDetails(programUUID, start, numOfRows);
		this.populateSiteAndPersonIfNecessary(list);
		return list;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public List<StudyDetails> getAllStudyDetails(final StudyType studyType, final String programUUID) {
		final List<StudyDetails> list = new ArrayList<>();
		final List localList = this.getDmsProjectDao().getAllStudyDetails(studyType, programUUID);
		if (localList != null) {
			list.addAll(localList);
		}
		this.populateSiteAndPersonIfNecessary(list);
		return list;
	}

	@Override
	public long countAllStudyDetails(final StudyType studyType, final String programUUID) {
		long count = 0;
		count += this.getDmsProjectDao().countAllStudyDetails(studyType, programUUID);
		return count;
	}

	@Override
	public long countStudyDetails(final StudyType studyType, final String programUUID) {
		long count = 0;
		count += this.getDmsProjectDao().countAllStudyDetails(studyType, programUUID);
		return count;
	}

	@SuppressWarnings({"unchecked", "rawtypes"})
	@Override
	public List<StudyDetails> getAllNurseryAndTrialStudyDetails(final String programUUID) {
		final List<StudyDetails> list = new ArrayList<>();
		final List localList = this.getDmsProjectDao().getAllNurseryAndTrialStudyDetails(programUUID);
		if (localList != null) {
			list.addAll(localList);
		}
		this.populateSiteAndPersonIfNecessary(list);
		return list;
	}

	@Override
	public long countAllNurseryAndTrialStudyDetails(final String programUUID) {
		long count = 0;
		count += this.getDmsProjectDao().countAllNurseryAndTrialStudyDetails(programUUID);
		return count;
	}

	@Override
	public List<FolderReference> getFolderTree() {
		return this.getFolderBuilder().buildFolderTree();
	}

	@Override
	public List<FolderReference> getAllFolders() {
		return this.getDmsProjectDao().getAllFolders();
	}

	@Override
	public int countPlotsWithRecordedVariatesInDataset(final int dataSetId, final List<Integer> variateIds)
			 {
		return this.getPhenotypeDao().countRecordedVariatesOfStudy(dataSetId, variateIds);
	}

	@Override
	public String getGeolocationPropValue(final int stdVarId, final int studyId) {
		return this.getGeolocationPropertyDao().getGeolocationPropValue(stdVarId, studyId);
	}

	@Override
	public String getFolderNameById(final Integer folderId) {
		final DmsProject currentFolder = this.getDmsProjectDao().getById(folderId);
		return currentFolder.getName();
	}

	@Override
	public boolean checkIfStudyHasMeasurementData(final int datasetId, final List<Integer> variateIds) {
		return this.getPhenotypeDao().countVariatesDataOfStudy(datasetId, variateIds) > 0;
	}

	@Override
	public int countVariatesWithData(final int datasetId, final List<Integer> variateIds) {
		int variatesWithDataCount = 0;
		if (variateIds != null && !variateIds.isEmpty()) {
			final Map<Integer, Integer> map = this.getPhenotypeDao().countVariatesDataOfStudy(datasetId);
			for (final Integer variateId : variateIds) {
				final Integer count = map.get(variateId);
				if (count != null && count > 0) {
					variatesWithDataCount++;
				}
			}
		}
		return variatesWithDataCount;
	}

	private void populateSiteAnPersonIfNecessary(final StudyDetails detail) {
		if (detail != null) {
			if (detail.getSiteName() != null && !"".equals(detail.getSiteName().trim()) && detail.getSiteId() != null) {
				final Location loc = this.getLocationDao().getById(detail.getSiteId());
				if (loc != null) {
					detail.setSiteName(loc.getLname());
				}
			}
			if (detail.getPiName() != null && !"".equals(detail.getPiName().trim()) && detail.getPiId() != null) {
				final Person person = this.getPersonDao().getById(detail.getPiId());
				if (person != null) {
					detail.setPiName(person.getDisplayName());
				}
			}
		}
	}

	private void populateSiteAndPersonIfNecessary(final List<StudyDetails> studyDetails) {
		if (studyDetails != null && !studyDetails.isEmpty()) {
			final Map<Integer, String> siteMap = new HashMap<>();
			final Map<Integer, String> personMap = new HashMap<>();
			this.retrieveSitesAndPersonsFromStudyDetails(studyDetails, siteMap, personMap);
			for (final StudyDetails detail : studyDetails) {
				if (detail.getSiteId() != null) {
					detail.setSiteName(siteMap.get(detail.getSiteId()));
				}
				if (detail.getPiId() != null) {
					detail.setPiName(personMap.get(detail.getPiId()));
				}
			}
		}
	}

	private void retrieveSitesAndPersonsFromStudyDetails(final List<StudyDetails> studyDetails, final Map<Integer, String> siteMap,
			final Map<Integer, String> personMap) {
		final List<Integer> siteIds = new ArrayList<>();
		final List<Integer> personIds = new ArrayList<>();
		for (final StudyDetails detail : studyDetails) {
			if (detail.getSiteId() != null) {
				siteIds.add(detail.getSiteId());
			}
			if (detail.getPiId() != null) {
				personIds.add(detail.getPiId());
			}
		}
		if (!siteIds.isEmpty()) {
			siteMap.putAll(this.getLocationDao().getLocationNamesByLocationIDs(siteIds));
		}
		if (!personIds.isEmpty()) {
			personMap.putAll(this.getPersonDao().getPersonNamesByPersonIds(personIds));
		}
	}

	private Integer getBlockId(final List<FieldMapInfo> infos) {
		if (infos == null) {
			return null;
		}
		for (final FieldMapInfo info : infos) {
			if (info == null || info.getDatasets() == null) {
				continue;
			}
			for (final FieldMapDatasetInfo dataset : info.getDatasets()) {
				final Integer blockId = this.getBlockId(dataset);
				if (blockId != null) {
					return blockId;
				}
			}
		}
		return null;
	}

	private Integer getBlockId(final FieldMapDatasetInfo dataset) {
		if (dataset != null && dataset.getTrialInstances() != null) {
			for (final FieldMapTrialInstanceInfo trial : dataset.getTrialInstances()) {
				return trial.getBlockId();
			}
		}
		return null;
	}

	private void updateFieldMapWithBlockInformation(final List<FieldMapInfo> infos, final FieldmapBlockInfo blockInfo)
			 {
		this.updateFieldMapWithBlockInformation(infos, blockInfo, false);
	}

	protected void updateFieldMapWithBlockInformation(final List<FieldMapInfo> infos, final FieldmapBlockInfo blockInfo,
			final boolean isGetLocation) {
		if (infos == null) {
			return;
		}
		final Map<Integer, String> locationMap = new HashMap<>();
		for (final FieldMapInfo info : infos) {
			if (info != null && info.getDatasets() != null) {
				for (final FieldMapDatasetInfo dataset : info.getDatasets()) {
					this.updateFieldMapTrialInstanceInfo(dataset, isGetLocation, locationMap);
				}
			}
		}
	}

	private void updateFieldMapTrialInstanceInfo(final FieldMapDatasetInfo dataset, final boolean isGetLocation,
			final Map<Integer, String> locationMap) {
		if (dataset != null && dataset.getTrialInstances() != null) {
			for (final FieldMapTrialInstanceInfo trial : dataset.getTrialInstances()) {
				if (trial.getBlockId() != null) {
					trial.updateBlockInformation(this.locationDataManager.getBlockInformation(trial.getBlockId()));
				}
				if (isGetLocation) {
					trial.setLocationName(this.getLocationName(locationMap, trial.getLocationId()));
					trial.setSiteName(trial.getLocationName());
					trial.setFieldName(this.getLocationName(locationMap, trial.getFieldId()));
					trial.setBlockName(this.getLocationName(locationMap, trial.getBlockId()));
				}
			}
		}
	}

	private void updateFieldMapInfoWithBlockInfo(final List<FieldMapInfo> fieldMapInfos) {
		this.updateFieldMapWithBlockInformation(fieldMapInfos, null, true);
	}

	private String getLocationName(final Map<Integer, String> locationMap, final Integer id) {
		if (id != null) {
			final String name = locationMap.get(id);
			if (name != null) {
				return name;
			}
			final Location location = this.getLocationDAO().getById(id);
			if (location != null) {
				locationMap.put(id, location.getLname());
				return location.getLname();
			}
		}
		return null;
	}

	@Override
	public List<Object[]> getPhenotypeIdsByLocationAndPlotNo(final int projectId, final int locationId, final List<Integer> plotNos,
			final List<Integer> cvTermIds) {
		return this.getPhenotypeDao().getPhenotypeIdsByLocationAndPlotNo(projectId, locationId, plotNos, cvTermIds);
	}

	@Override
	public List<Object[]> getPhenotypeIdsByLocationAndPlotNo(final int projectId, final int locationId, final Integer plotNo,
			final List<Integer> cvTermIds) {
		return this.getPhenotypeDao().getPhenotypeIdsByLocationAndPlotNo(projectId, locationId, plotNo, cvTermIds);
	}

	@Override
	public void saveOrUpdatePhenotypeOutliers(final List<PhenotypeOutlier> phenotyleOutliers) {

		final PhenotypeOutlierDao phenotypeOutlierDao = this.getPhenotypeOutlierDao();
		try {

			for (final PhenotypeOutlier phenotypeOutlier : phenotyleOutliers) {
				final PhenotypeOutlier existingPhenotypeOutlier =
						phenotypeOutlierDao.getPhenotypeOutlierByPhenotypeId(phenotypeOutlier.getPhenotypeId());

				if (existingPhenotypeOutlier != null) {
					existingPhenotypeOutlier.setValue(phenotypeOutlier.getValue());
					phenotypeOutlierDao.saveOrUpdate(existingPhenotypeOutlier);
				} else {
					phenotypeOutlierDao.saveOrUpdate(phenotypeOutlier);
				}
			}

		} catch (final Exception e) {
			throw new MiddlewareQueryException("error in savePhenotypeOutlier " + e.getMessage(), e);
		}

	}

	@Override
	public Boolean containsAtLeast2CommonEntriesWithValues(final int projectId, final int locationId, final int germplasmTermId)
			 {

		return this.getPhenotypeDao().containsAtLeast2CommonEntriesWithValues(projectId, locationId, germplasmTermId);
	}

	public void setLocationDataManager(final LocationDataManager locationDataManager) {
		this.locationDataManager = locationDataManager;
	}

	@Override
	public StudyType getStudyType(final int studyId) {
		return this.getDmsProjectDao().getStudyType(studyId);
	}

	@Override
	public void deleteProgramStudies(final String programUUID) {
		final List<Integer> projectIds = this.getDmsProjectDao().getAllProgramStudiesAndFolders(programUUID);

		try {
			for (final Integer projectId : projectIds) {
				this.getStudyDestroyer().deleteStudy(projectId);
			}
		} catch (final Exception e) {
			throw new MiddlewareQueryException("Error encountered with saveMeasurementRows(): " + e.getMessage(), e);
		}
	}

	@Override
	public void updateVariableOrdering(final int datasetId, final List<Integer> variableIds) {

		try {
			this.getProjectPropertySaver().updateVariablesRanking(datasetId, variableIds);

		} catch (final Exception e) {
			throw new MiddlewareQueryException("Error in updateVariableOrdering " + e.getMessage(), e);
		}
	}

	@Override
	public Integer getGeolocationIdByProjectIdAndTrialInstanceNumber(final int projectId, final String trialInstanceNumber)
			 {
		return this.getExperimentProjectDao().getGeolocationIdByProjectIdAndTrialInstanceNumber(projectId, trialInstanceNumber);
	}

	@Override
	public String getTrialInstanceNumberByGeolocationId(final int geolocationId) {
		final Geolocation geolocation = this.getGeolocationDao().getById(geolocationId);
		if (geolocation != null) {
			return geolocation.getDescription();
		}
		return null;
	}

	@Override
	public void saveGeolocationProperty(final int geolocationId, final int typeId, final String value) {
		try {
			this.getGeolocationPropertySaver().saveOrUpdate(geolocationId, typeId, value);
		} catch (final Exception e) {
			throw new MiddlewareQueryException("Error in saveGeolocationProperty " + e.getMessage(), e);
		}

	}

	@Override
	public List<String> getAllSharedProjectNames() {
		return this.getDmsProjectDao().getAllSharedProjectNames();
	}

	@Override
	public boolean checkIfAnyLocationIDsExistInExperiments(final int studyId, final DataSetType dataSetType,
			final List<Integer> locationIds) {

		final List<DmsProject> datasetProjects = this.getDmsProjectDao().getDataSetsByStudyAndProjectProperty(studyId,
				TermId.DATASET_TYPE.getId(), String.valueOf(dataSetType.getId()));

		if (!datasetProjects.isEmpty()) {
			final int dataSetId = datasetProjects.get(0).getProjectId();
			return this.getExperimentDao().checkIfAnyLocationIDsExistInExperiments(dataSetId, locationIds);
		} else {
			return false;
		}

	}

	@Override
	public List<StudySummary> findPagedProjects(final Map<StudyFilters, String> filters,
			final Integer pageSize, final Integer pageNumber) {

		final List<DmsProject> dmsProjects = this.getDmsProjectDao().findPagedProjects(filters, pageSize, pageNumber);

		final List<StudySummary> studySummaries = Lists.newArrayList();

		for (final DmsProject dmsProject : dmsProjects) {
			final StudySummary studySummary = new StudySummary();

			studySummary.setActive(!dmsProject.isDeleted());

			final Map<String, String> additionalProps = Maps.newHashMap();

			for (final ProjectProperty prop : dmsProject.getProperties()) {

				final Integer variableId = prop.getVariableId();
				final String value = prop.getValue();

				if (variableId.equals(TermId.START_DATE.getId())) {
					studySummary.setStartDate(value);
				} else if (variableId.equals(TermId.END_DATE.getId())) {
					studySummary.setEndDate(value);
				} else if (variableId.equals(TermId.SEASON_VAR_TEXT.getId())) {
					studySummary.addSeason(value);
				} else if (variableId.equals(TermId.LOCATION_ID.getId())) {
					studySummary.setLocationId(!StringUtils.isEmpty(value) ? value : null);
				} else {
					additionalProps.put(prop.getAlias(), value);
				}
			}

			studySummary.setOptionalInfo(additionalProps)
					.setName(dmsProject.getName())
					.setProgramDbId(dmsProject.getProgramUUID())
					.setStudyDbid(dmsProject.getProjectId());
			studySummary.setInstanceMetaData(this.getInstanceMetadata(dmsProject.getProjectId()));
			studySummaries.add(studySummary);
		}
		return studySummaries;
	}

	@Override
	public Long countAllStudies(final Map<StudyFilters, String> filters)
			 {
		return this.getDmsProjectDao().countStudies(filters);
	}

	@Override
	public List<InstanceMetadata> getInstanceMetadata(int studyId) {
		return this.getGeolocationDao().getInstanceMetadata(studyId);
	}

	@Override
	public Phenotype getPhenotypeById(int phenotypeId) {
		return getPhenotypeDao().getById(phenotypeId);
	}

	@Override
	public void saveOrUpdatePhenotypeValue(int experimentId, int variableId, String value, Phenotype existingPhenotype, int dataTypeId) {
		getPhenotypeSaver().saveOrUpdate(experimentId, variableId, value, existingPhenotype, dataTypeId);
	}

	@Override
	public StudyMetadata getStudyMetadata (Integer studyId) {
		return this.getDmsProjectDao().getStudyMetadata(studyId);
	}

	@Override
	public Map<String, String> getGeolocationPropsAndValuesByStudy(final Integer studyId) {
		return this.getGeolocationPropertyDao().getGeolocationPropsAndValuesByStudy(studyId);
	}

	@Override
	public Map<String, String> getProjectPropsAndValuesByStudy(final Integer studyId) {
		return this.getProjectPropertyDao().getProjectPropsAndValuesByStudy(studyId);
	}

	@Override
	public Map<Integer, String> getExperimentSampleMap(final Integer studyDbId) {
		return this.getSampleDao().getExperimentSampleMap(studyDbId);
	}

	@Override
	public ProjectProperty getByVariableIdAndProjectID(final DmsProject project, final int variableId) throws MiddlewareQueryException {
		return this.getProjectPropertyDao().getByStandardVariableId(project, variableId);
	}

}
