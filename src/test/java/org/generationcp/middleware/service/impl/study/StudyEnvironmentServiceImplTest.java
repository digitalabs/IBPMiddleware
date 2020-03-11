package org.generationcp.middleware.service.impl.study;

import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.IntegrationTestBase;
import org.generationcp.middleware.WorkbenchTestDataUtil;
import org.generationcp.middleware.data.initializer.GermplasmListTestDataInitializer;
import org.generationcp.middleware.data.initializer.StudyTestDataInitializer;
import org.generationcp.middleware.domain.dms.DMSVariableType;
import org.generationcp.middleware.domain.dms.DatasetReference;
import org.generationcp.middleware.domain.dms.DatasetValues;
import org.generationcp.middleware.domain.dms.ExperimentDesignType;
import org.generationcp.middleware.domain.dms.StudyReference;
import org.generationcp.middleware.domain.dms.VariableTypeList;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.DaoFactory;
import org.generationcp.middleware.manager.StudyDataManagerImpl;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.generationcp.middleware.pojos.dms.ExperimentModel;
import org.generationcp.middleware.pojos.dms.Geolocation;
import org.generationcp.middleware.pojos.oms.CVTerm;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.study.StudyEnvironmentService;
import org.generationcp.middleware.utils.test.IntegrationTestDataInitializer;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class StudyEnvironmentServiceImplTest extends IntegrationTestBase {

	public static final String PROGRAM_UUID = UUID.randomUUID().toString();
	public static final String TRIAL_INSTANCE = "TRIAL_INSTANCE";
	public static final String LOCATION_NAME = "LOCATION_NAME";

	private IntegrationTestDataInitializer testDataInitializer;

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private OntologyDataManager ontologyManager;

	@Autowired
	private LocationDataManager locationManager;

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Autowired
	private OntologyDataManager ontologyDataManager;

	@Autowired
	private WorkbenchTestDataUtil workbenchTestDataUtil;

	private DaoFactory daoFactory;
	private StudyDataManagerImpl studyDataManager;
	private StudyTestDataInitializer studyTestDataInitializer;
	private StudyEnvironmentService studyEnvironmentService;
	private Project commonTestProject;
	private CropType cropType;
	private StudyReference studyReference;
	private DatasetReference environmentDataset;
	private Geolocation instance1;
	private Geolocation instance2;
	private Geolocation instance3;


	@Before
	public void setup() throws Exception {

		this.studyEnvironmentService = new StudyEnvironmentServiceImpl(this.sessionProvder);
		this.studyDataManager = new StudyDataManagerImpl(this.sessionProvder);
		this.daoFactory = new DaoFactory(this.sessionProvder);

		if (this.commonTestProject == null) {
			this.commonTestProject = this.workbenchTestDataUtil.getCommonTestProject();
		}

		this.testDataInitializer = new IntegrationTestDataInitializer(this.sessionProvder, this.workbenchSessionProvider);

		this.studyTestDataInitializer =
			new StudyTestDataInitializer(this.studyDataManager, this.ontologyManager, this.commonTestProject, this.germplasmDataManager,
				this.locationManager);

		this.cropType = this.workbenchDataManager.getCropTypeByName(CropType.CropEnum.MAIZE.name());

		this.studyReference = this.studyTestDataInitializer.addTestStudy();

		// Create an environment dataset with TRIAL_INSTANCE and LOCATION_NAME properties.
		final VariableTypeList environmentVariables = new VariableTypeList();
		environmentVariables
			.add(new DMSVariableType(TRIAL_INSTANCE, "Trial instance - enumerated (number)", this.ontologyDataManager.getStandardVariable(
				TermId.TRIAL_INSTANCE_FACTOR.getId(), null), 1));
		environmentVariables.add(new DMSVariableType(LOCATION_NAME, "Location Name", this.ontologyDataManager.getStandardVariable(
			TermId.LOCATION_ID.getId(), null), 2));
		final DatasetValues datasetValues = new DatasetValues();
		datasetValues.setName(RandomStringUtils.randomAlphanumeric(10));
		datasetValues.setDescription(RandomStringUtils.randomAlphanumeric(10));
		this.environmentDataset =
			this.studyDataManager
				.addDataSet(this.studyReference.getId(), environmentVariables, datasetValues, null, DatasetTypeEnum.SUMMARY_DATA.getId());

		if (this.instance1 == null){
			this.instance1 = this.testDataInitializer.createTestGeolocation("1", 1);
			this.instance2 = this.testDataInitializer.createTestGeolocation("2", 2);
			this.instance3 = this.testDataInitializer.createTestGeolocation("3", 3);
		}

	}

	@Test
	public void testCreateStudyInstances() {

		// Create instance 1
		final Integer studyId = this.studyReference.getId();
		final List<StudyInstance> studyInstances =
			this.studyEnvironmentService.createStudyEnvironments(this.cropType, studyId, this.environmentDataset.getId(), 2);
		// Need to flush session to sync with underlying database before querying
		this.sessionProvder.getSession().flush();
		final StudyInstance studyInstance1 = studyInstances.get(0);
		assertEquals(1, studyInstance1.getInstanceNumber());
		assertNotNull(studyInstance1.getExperimentId());
		assertNotNull(studyInstance1.getLocationId());
		assertFalse(studyInstance1.isHasFieldmap());
		assertEquals("Unspecified Location", studyInstance1.getLocationName());
		assertEquals("NOLOC", studyInstance1.getLocationAbbreviation());
		assertNull(studyInstance1.getCustomLocationAbbreviation());
		assertTrue(studyInstance1.getCanBeDeleted());
		assertFalse(studyInstance1.isHasMeasurements());
		assertFalse(studyInstance1.isHasExperimentalDesign());

		final StudyInstance studyInstance2 = studyInstances.get(1);
		assertEquals(2, studyInstance2.getInstanceNumber());
		assertNotNull(studyInstance2.getExperimentId());
		assertNotNull(studyInstance2.getLocationId());
		assertFalse(studyInstance2.isHasFieldmap());
		assertEquals("Unspecified Location", studyInstance2.getLocationName());
		assertEquals("NOLOC", studyInstance2.getLocationAbbreviation());
		assertNull(studyInstance2.getCustomLocationAbbreviation());
		assertTrue(studyInstance2.getCanBeDeleted());
		assertFalse(studyInstance2.isHasMeasurements());
		assertFalse(studyInstance2.isHasExperimentalDesign());

		final List<ExperimentModel> retrievedStudyInstances =
			this.daoFactory.getEnvironmentDao().getEnvironments(studyId);
		Assert.assertEquals(2, retrievedStudyInstances.size());
	}

	@Test
	public void testGetStudyInstances() {

		final DmsProject study = this.createTestStudy();

		final List<StudyInstance> studyInstances = this.studyEnvironmentService.getStudyEnvironments(study.getProjectId());

		Assert.assertEquals(3, studyInstances.size());

		final StudyInstance studyInstance1 = studyInstances.get(0);
		Assert.assertEquals(instance1.getLocationId().intValue(), studyInstance1.getExperimentId());
		Assert.assertEquals(1, studyInstance1.getInstanceNumber());
		Assert.assertNull(studyInstance1.getCustomLocationAbbreviation());
		Assert.assertEquals("AFG", studyInstance1.getLocationAbbreviation());
		Assert.assertEquals("Afghanistan", studyInstance1.getLocationName());
		Assert.assertFalse(studyInstance1.isHasFieldmap());
		Assert.assertTrue(studyInstance1.isHasExperimentalDesign());
		// Instance deletion not allowed because instance has subobservation
		Assert.assertFalse(studyInstance1.getCanBeDeleted());
		Assert.assertTrue(studyInstance1.isHasMeasurements());

		final StudyInstance studyInstance2 = studyInstances.get(1);
		Assert.assertEquals(instance2.getLocationId().intValue(), studyInstance2.getExperimentId());
		Assert.assertEquals(2, studyInstance2.getInstanceNumber());
		Assert.assertNull(studyInstance2.getCustomLocationAbbreviation());
		Assert.assertEquals("ALB", studyInstance2.getLocationAbbreviation());
		Assert.assertEquals("Albania", studyInstance2.getLocationName());
		Assert.assertTrue(studyInstance2.isHasFieldmap());
		Assert.assertTrue(studyInstance2.isHasExperimentalDesign());
		// Instance deletion not allowed because study has advance list and design already generated for instance
		Assert.assertFalse(studyInstance2.getCanBeDeleted());
		Assert.assertFalse(studyInstance2.isHasMeasurements());

		final StudyInstance studyInstance3 = studyInstances.get(2);
		Assert.assertEquals(instance3.getLocationId().intValue(), studyInstance3.getExperimentId());
		Assert.assertEquals(3, studyInstance3.getInstanceNumber());
		Assert.assertNull(studyInstance3.getCustomLocationAbbreviation());
		Assert.assertEquals("DZA", studyInstance3.getLocationAbbreviation());
		Assert.assertEquals("Algeria", studyInstance3.getLocationName());
		Assert.assertFalse(studyInstance3.isHasFieldmap());
		Assert.assertFalse(studyInstance3.isHasExperimentalDesign());
		Assert.assertTrue(studyInstance3.getCanBeDeleted());
		Assert.assertFalse(studyInstance3.isHasMeasurements());
	}

	@Test
	public void testGetStudyInstance() {

		final DmsProject study = this.createTestStudy();

		final StudyInstance studyInstance1 = this.studyEnvironmentService.getStudyEnvironment(study.getProjectId(), instance1.getLocationId()).get();
		Assert.assertEquals(instance1.getLocationId().intValue(), studyInstance1.getExperimentId());
		Assert.assertEquals(1, studyInstance1.getInstanceNumber());
		Assert.assertNull(studyInstance1.getCustomLocationAbbreviation());
		Assert.assertEquals("AFG", studyInstance1.getLocationAbbreviation());
		Assert.assertEquals("Afghanistan", studyInstance1.getLocationName());
		Assert.assertFalse(studyInstance1.isHasFieldmap());
		Assert.assertTrue(studyInstance1.isHasExperimentalDesign());
		// Instance deletion not allowed because instance has subobservation
		Assert.assertFalse(studyInstance1.getCanBeDeleted());
		Assert.assertTrue(studyInstance1.isHasMeasurements());

		final StudyInstance studyInstance2 = this.studyEnvironmentService.getStudyEnvironment(study.getProjectId(), instance2.getLocationId()).get();
		Assert.assertEquals(instance2.getLocationId().intValue(), studyInstance2.getExperimentId());
		Assert.assertEquals(2, studyInstance2.getInstanceNumber());
		Assert.assertNull(studyInstance2.getCustomLocationAbbreviation());
		Assert.assertEquals("ALB", studyInstance2.getLocationAbbreviation());
		Assert.assertEquals("Albania", studyInstance2.getLocationName());
		Assert.assertTrue(studyInstance2.isHasFieldmap());
		Assert.assertTrue(studyInstance2.isHasExperimentalDesign());
		// Instance deletion not allowed because study has advance list and design already generated for instance
		Assert.assertFalse(studyInstance2.getCanBeDeleted());
		Assert.assertFalse(studyInstance2.isHasMeasurements());

		final StudyInstance studyInstance3 = this.studyEnvironmentService.getStudyEnvironment(study.getProjectId(), instance3.getLocationId()).get();
		Assert.assertEquals(instance3.getLocationId().intValue(), studyInstance3.getExperimentId());
		Assert.assertEquals(3, studyInstance3.getInstanceNumber());
		Assert.assertNull(studyInstance3.getCustomLocationAbbreviation());
		Assert.assertEquals("DZA", studyInstance3.getLocationAbbreviation());
		Assert.assertEquals("Algeria", studyInstance3.getLocationName());
		Assert.assertFalse(studyInstance3.isHasFieldmap());
		Assert.assertFalse(studyInstance3.isHasExperimentalDesign());
		Assert.assertTrue(studyInstance3.getCanBeDeleted());
		Assert.assertFalse(studyInstance3.isHasMeasurements());
	}

	private DmsProject createTestStudy() {
		final DmsProject study =
			this.testDataInitializer
				.createDmsProject("Study1", "Study-Description", null, this.daoFactory.getDmsProjectDAO().getById(1), null);
		final DmsProject environmentDataset =
			this.testDataInitializer
				.createDmsProject("Summary Dataset", "Summary Dataset-Description", study, study, DatasetTypeEnum.SUMMARY_DATA);
		final DmsProject plotDataset =
			this.testDataInitializer
				.createDmsProject("Plot Dataset", "Plot Dataset-Description", study, study, DatasetTypeEnum.PLOT_DATA);
		final DmsProject subObsDataset =
			this.testDataInitializer
				.createDmsProject("Subobs Dataset", "Subobs Dataset-Description", study, plotDataset, DatasetTypeEnum.QUADRAT_SUBOBSERVATIONS);
		final GermplasmList advanceList = GermplasmListTestDataInitializer.createGermplasmListWithType(null,
			GermplasmListType.ADVANCED.name());
		advanceList.setProjectId(study.getProjectId());
		this.daoFactory.getGermplasmListDAO().save(advanceList);

		this.testDataInitializer.addGeolocationProp(instance1, TermId.EXPERIMENT_DESIGN_FACTOR.getId(),
			ExperimentDesignType.RANDOMIZED_COMPLETE_BLOCK.getTermId().toString(), 1);
		this.testDataInitializer.addGeolocationProp(instance2, TermId.BLOCK_ID.getId(), RandomStringUtils.randomAlphabetic(5), 1);

		// Instance 1
		this.testDataInitializer.createTestExperiment(environmentDataset, instance1, TermId.SUMMARY_EXPERIMENT.getId(), "0", null);
		final ExperimentModel instance1PlotExperiment =
			this.testDataInitializer.createTestExperiment(plotDataset, instance1, TermId.PLOT_EXPERIMENT.getId(), "1", null);
		// Create 2 Sub-obs records
		final ExperimentModel instance1SubObsExperiment1 =
			this.testDataInitializer
				.createTestExperiment(subObsDataset, instance1, TermId.PLOT_EXPERIMENT.getId(), "1", instance1PlotExperiment);
		this.savePhenotype(instance1SubObsExperiment1);
		final ExperimentModel instance1SubObsExperiment2 = this.testDataInitializer
			.createTestExperiment(subObsDataset, instance1, TermId.PLOT_EXPERIMENT.getId(), "1", instance1PlotExperiment);
		this.savePhenotype(instance1SubObsExperiment2);

		// Instance 2
		this.testDataInitializer.createTestExperiment(environmentDataset, instance2, TermId.SUMMARY_EXPERIMENT.getId(), "0", null);
		this.testDataInitializer.createTestExperiment(plotDataset, instance2, TermId.PLOT_EXPERIMENT.getId(), "1", null);

		// Instance 3 has no plot experiments
		this.testDataInitializer.createTestExperiment(environmentDataset, instance3, TermId.SUMMARY_EXPERIMENT.getId(), "0", null);
		return study;
	}

	@Test
	public void testDeleteEnvironments() {
		final DmsProject study =
			this.testDataInitializer
				.createDmsProject("Study1", "Study-Description", null, this.daoFactory.getDmsProjectDAO().getById(1), null);
		final DmsProject environmentDataset =
			this.testDataInitializer
				.createDmsProject("Summary Dataset", "Summary Dataset-Description", study, study, DatasetTypeEnum.SUMMARY_DATA);
		final DmsProject plotDataset =
			this.testDataInitializer
				.createDmsProject("Plot Dataset", "Plot Dataset-Description", study, study, DatasetTypeEnum.PLOT_DATA);

		final Geolocation instance1 = this.testDataInitializer.createTestGeolocation("1", 1);
		final Geolocation instance2 = this.testDataInitializer.createTestGeolocation("2", 2);
		final Geolocation instance3 = this.testDataInitializer.createTestGeolocation("3", 3);
		this.testDataInitializer.addGeolocationProp(instance1, TermId.EXPERIMENT_DESIGN_FACTOR.getId(),
			ExperimentDesignType.RANDOMIZED_COMPLETE_BLOCK.getTermId().toString(), 1);
		this.testDataInitializer.addGeolocationProp(instance2, TermId.EXPERIMENT_DESIGN_FACTOR.getId(),
			ExperimentDesignType.RANDOMIZED_COMPLETE_BLOCK.getTermId().toString(), 1);
		this.testDataInitializer.addGeolocationProp(instance3, TermId.EXPERIMENT_DESIGN_FACTOR.getId(),
			ExperimentDesignType.RANDOMIZED_COMPLETE_BLOCK.getTermId().toString(), 1);

		final Integer studyExperimentId = this.createTestExperiments(study, environmentDataset, plotDataset, instance1, instance2, instance3);
		final Integer studyId = study.getProjectId();

		// Delete Instances 1 and 2
		final Integer instance1LocationId = instance1.getLocationId();
		final Integer instance2LocationId = instance2.getLocationId();
		this.studyEnvironmentService.deleteStudyEnvironments(studyId, Arrays.asList(instance1LocationId, instance2LocationId));

		List<StudyInstance> studyInstances =
			this.studyEnvironmentService.getStudyEnvironments(studyId);
		Assert.assertEquals(2, studyInstances.size());
		Assert.assertEquals(instance1LocationId, this.daoFactory.getExperimentDao().getById(studyExperimentId).getNdExperimentId());
		for (final StudyInstance instance : studyInstances) {
			Assert.assertNotEquals(2, instance.getInstanceNumber());
			Assert.assertNotEquals(instance2LocationId.intValue(), instance.getExperimentId());
		}
		// Confirm geolocation and its properties have been deleted
		Assert.assertNull(this.daoFactory.getEnvironmentDao().getById(instance2LocationId));
		Assert.assertTrue(CollectionUtils.isEmpty(this.daoFactory.getEnvironmentPropertyDao().getEnvironmentVariableNameValuesMap(instance2LocationId)));


		this.sessionProvder.getSession().flush();

		studyInstances =
			this.studyEnvironmentService.getStudyEnvironments(studyId);
		Assert.assertEquals(1, studyInstances.size());
		Assert.assertNotEquals(2, studyInstances.get(0).getInstanceNumber());
		Assert.assertNotEquals(instance2LocationId.intValue(), studyInstances.get(0).getExperimentId());
		final Integer instance3LocationId = instance3.getLocationId();
		Assert.assertEquals(instance3LocationId, this.daoFactory.getExperimentDao().getById(studyExperimentId).getNdExperimentId());
		// Confirm geolocation and its properties have been deleted
		Assert.assertNull(this.daoFactory.getEnvironmentDao().getById(instance1LocationId));
		Assert.assertTrue(CollectionUtils.isEmpty(this.daoFactory.getEnvironmentPropertyDao().getEnvironmentVariableNameValuesMap(instance1LocationId)));


		// Delete Instance 3 - should throw exception
		try {
			this.studyEnvironmentService.deleteStudyEnvironments(studyId, Collections.singletonList(instance3LocationId));
			Assert.fail("Should have thrown exception when attempting to delete last environment.");
		} catch (final MiddlewareQueryException e) {
			// Perform assertions outside
		}
		studyInstances =
			this.studyEnvironmentService.getStudyEnvironments(studyId);
		Assert.assertEquals(1, studyInstances.size());
		Assert.assertNotNull(this.daoFactory.getEnvironmentDao().getById(instance3LocationId));
		Assert.assertFalse(CollectionUtils.isEmpty(this.daoFactory.getEnvironmentPropertyDao().getEnvironmentVariableNameValuesMap(instance3LocationId)));
	}

	private Integer createTestExperiments(final DmsProject study, final DmsProject environmentDataset, final DmsProject plotDataset,
		final Geolocation instance1, final Geolocation instance2, final Geolocation instance3) {
		// Study experiment
		final ExperimentModel studyExperiment =
			this.testDataInitializer.createTestExperiment(study, instance1, TermId.STUDY_EXPERIMENT.getId(), "0", null);

		// Instance 1
		this.testDataInitializer.createTestExperiment(environmentDataset, instance1, TermId.SUMMARY_EXPERIMENT.getId(), "0", null);
		final ExperimentModel instance1PlotExperiment =
			this.testDataInitializer.createTestExperiment(plotDataset, instance1, TermId.PLOT_EXPERIMENT.getId(), "1", null);
		this.savePhenotype(instance1PlotExperiment);

		// Instance 2
		this.testDataInitializer.createTestExperiment(environmentDataset, instance2, TermId.SUMMARY_EXPERIMENT.getId(), "0", null);
		final ExperimentModel instance2PlotExperiment =
			this.testDataInitializer.createTestExperiment(plotDataset, instance2, TermId.PLOT_EXPERIMENT.getId(), "1", null);

		// Instance 3 has no plot experiments
		this.testDataInitializer.createTestExperiment(environmentDataset, instance3, TermId.SUMMARY_EXPERIMENT.getId(), "0", null);

		return studyExperiment.getNdExperimentId();
	}

	private void savePhenotype(final ExperimentModel experiment) {
		final CVTerm trait1 = this.testDataInitializer.createTrait(RandomStringUtils.randomAlphabetic(10));
		this.testDataInitializer.addPhenotypes(Collections.singletonList(experiment), trait1.getCvTermId(), "100");
	}

}