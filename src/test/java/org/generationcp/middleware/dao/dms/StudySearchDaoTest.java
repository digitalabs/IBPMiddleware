
package org.generationcp.middleware.dao.dms;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.IntegrationTestBase;
import org.generationcp.middleware.WorkbenchTestDataUtil;
import org.generationcp.middleware.data.initializer.StudyTestDataInitializer;
import org.generationcp.middleware.domain.dms.StudyReference;
import org.generationcp.middleware.domain.dms.StudySearchMatchingOption;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.UnpermittedDeletionException;
import org.generationcp.middleware.manager.Season;
import org.generationcp.middleware.manager.StudyDataManagerImpl;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.pojos.workbench.Project;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class StudySearchDaoTest extends IntegrationTestBase {

	private static final String TEST_STUDY_TO_DELETE = "TEST STUDY TO DELETE";
	public static final int NO_OF_DRY_SEASON_STUDIES = 3;
	public static final int NO_OF_WET_SEASON_STUDIES = 1;
	public static final String TEST_TRIAL_NAME_1 = "1 Test Trial Sample";
	public static final String TEST_TRIAL_NAME_2 = "2 Test Trial Sample";
	public static final String TEST_TRIAL_NAME_3 = "3 Test Trial Sample";
	public static final String TEST_TRIAL_NAME_4 = "4 Test Trial Sample";
	private static final String PROGRAM_UUID = "700e62d7-09b2-46af-a79c-b19ba4850681";
	private static final int NO_OF_TEST_STUDIES = 4;
	private static final int LUXEMBOURG_COUNTRY_LOCATION_ID = 127;

	private StudySearchDao studySearchDao;

	@Autowired
	private OntologyDataManager ontologyManager;

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Autowired
	private GermplasmDataManager germplasmDataDM;

	@Autowired
	private LocationDataManager locationManager;

	@Autowired
	private FieldbookService fieldbookService;

	private final String cropPrefix = "ABCD";

	private long numberOfDrySeasonBeforeCreatingTestData = 0;
	private long numberOfWetSeasoBeforeCreatingTestData = 0;

	private Integer idOfTrialToDelete;

	@Before
	public void init() throws Exception {

		this.studySearchDao = new StudySearchDao();
		this.studySearchDao.setSession(this.sessionProvder.getSession());

		this.numberOfDrySeasonBeforeCreatingTestData =
				this.studySearchDao.countStudiesBySeason(Season.DRY, StudySearchDaoTest.PROGRAM_UUID);
		this.numberOfWetSeasoBeforeCreatingTestData = this.studySearchDao.countStudiesBySeason(Season.WET, StudySearchDaoTest.PROGRAM_UUID);

		this.createTestStudies();
	}

	@Test
	public void testGetStudiesByNameNoMatch() {

		final String studyNameSearchKeyword = "TestTrialSample";

		final List<StudyReference> studies = this.studySearchDao.getStudiesByName(studyNameSearchKeyword, StudySearchMatchingOption.EXACT_MATCHES, StudySearchDaoTest.PROGRAM_UUID);

		Assert.assertEquals("No studies should be found, study count should be zero.", 0, studies.size());

	}

	@Test
	public void testGetStudiesByNameExactMatches() {

		final String studyNameSearchKeyword = "1 Test Trial Sample";

		final List<StudyReference> studies = this.studySearchDao.getStudiesByName(studyNameSearchKeyword, StudySearchMatchingOption.EXACT_MATCHES, StudySearchDaoTest.PROGRAM_UUID);

		Assert.assertEquals("Study count should be one.", 1, studies.size());
		Assert.assertEquals("Searched keyword should exactly match the returned Study name", studyNameSearchKeyword,
				studies.get(0).getName());

	}

	@Test
	public void testGetStudiesByNameMatchesStartingWith() {

		final String studyNameSearchKeyword = "1 Test";

		final List<StudyReference> studies = this.studySearchDao.getStudiesByName(studyNameSearchKeyword, StudySearchMatchingOption.MATCHES_STARTING_WITH, StudySearchDaoTest.PROGRAM_UUID);

		Assert.assertEquals("Study count should be one.", 1, studies.size());
		Assert.assertTrue("The returned Study name should start with " + studyNameSearchKeyword,
				studies.get(0).getName().startsWith(studyNameSearchKeyword));

	}
	
	@Test
	public void testGetStudiesByNameMatchesStartingWithWhenNameIsEmpty() {
		final List<StudyReference> studies = this.studySearchDao.getStudiesByName("", StudySearchMatchingOption.MATCHES_STARTING_WITH, StudySearchDaoTest.PROGRAM_UUID);
		Assert.assertEquals("When study name is empty, STARTS WITH search should not return any record.", 0, studies.size());
	}
	
	@Test
	public void testGetStudiesByNameMatchesStartingWithWhenNameIsNull() {
		final List<StudyReference> studies = this.studySearchDao.getStudiesByName(null, StudySearchMatchingOption.MATCHES_STARTING_WITH, StudySearchDaoTest.PROGRAM_UUID);
		Assert.assertEquals("When study name is null, STARTS WITH search should not return any record.", 0, studies.size());
	}

	@Test
	public void testGetStudiesByNameMatchesContaining() {

		final String studyNameSearchKeyword = "Test Trial Sample";

		final List<StudyReference> studies = this.studySearchDao.getStudiesByName(studyNameSearchKeyword, StudySearchMatchingOption.MATCHES_CONTAINING, StudySearchDaoTest.PROGRAM_UUID);

		Assert.assertEquals("Study count should be " + StudySearchDaoTest.NO_OF_TEST_STUDIES, StudySearchDaoTest.NO_OF_TEST_STUDIES,
				studies.size());

		for (final StudyReference studyReference : studies) {
			Assert.assertTrue("The returned Study name should contain " + studyNameSearchKeyword,
					studyReference.getName().contains(studyNameSearchKeyword));
		}
	}
	
	@Test
	public void testGetStudiesByNameMatchesContainingWhenNameIsEmpty() {
		final List<StudyReference> studies = this.studySearchDao.getStudiesByName("", StudySearchMatchingOption.MATCHES_CONTAINING, StudySearchDaoTest.PROGRAM_UUID);
		Assert.assertEquals("When study name is empty, CONTAINS search should not return any record.", 0, studies.size());
	}
	
	@Test
	public void testGetStudiesByNameMatchesContainingWhenNameIsNull() {
		final List<StudyReference> studies = this.studySearchDao.getStudiesByName(null,	StudySearchMatchingOption.MATCHES_CONTAINING, StudySearchDaoTest.PROGRAM_UUID);
		Assert.assertEquals("When study name is null, CONTAINS search should not return any record.", 0, studies.size());
	}

	@Test
	public void testGetStudiesByNameExcludingDeletedStudies() throws UnpermittedDeletionException {

		final String studyNameSearchKeyword = "1 Test Trial Sample";

		List<StudyReference> studiesByName = this.studySearchDao.getStudiesByName(studyNameSearchKeyword, StudySearchMatchingOption.EXACT_MATCHES, StudySearchDaoTest.PROGRAM_UUID);
		Assert.assertEquals("Study count should be one.", 1, studiesByName.size());

		// Delete test study
		final StudyReference study = studiesByName.get(0);
		this.fieldbookService.deleteStudy(study.getId(), this.fieldbookService.getStudy(study.getId()).getUser());
		flush();

		// Check that deleted study is not retrieved
		studiesByName = this.studySearchDao.getStudiesByName(studyNameSearchKeyword, StudySearchMatchingOption.EXACT_MATCHES, StudySearchDaoTest.PROGRAM_UUID);
		Assert.assertEquals("Deleted study should not be returned. ", 0, studiesByName.size());

	}

	@Test
	public void testCountStudiesByNameNoMatch() {

		final String studyNameSearchKeyword = "TestTrialSample";

		Assert.assertEquals("No studies should be found, study count should be zero.", 0, this.studySearchDao
				.countStudiesByName(studyNameSearchKeyword, StudySearchMatchingOption.EXACT_MATCHES, StudySearchDaoTest.PROGRAM_UUID));

	}

	@Test
	public void testCountStudiesByNameExactMatches() {

		final String studyNameSearchKeyword = "1 Test Trial Sample";

		Assert.assertEquals("Study count should be one.", 1, this.studySearchDao.countStudiesByName(studyNameSearchKeyword,
				StudySearchMatchingOption.EXACT_MATCHES, StudySearchDaoTest.PROGRAM_UUID));

	}

	@Test
	public void testCountStudiesByNameMatchesStartingWith() {

		final String studyNameSearchKeyword = "1 Test";

		Assert.assertEquals("Study count should be one.", 1, this.studySearchDao.countStudiesByName(studyNameSearchKeyword,
				StudySearchMatchingOption.MATCHES_STARTING_WITH, StudySearchDaoTest.PROGRAM_UUID));

	}
	
	@Test
	public void testCountStudiesByNameMatchesStartingWithWhenNameIsEmpty() {
		Assert.assertEquals("When study name is empty, STARTS WITH search should not return any record.", 0, this.studySearchDao.countStudiesByName("",
				StudySearchMatchingOption.MATCHES_STARTING_WITH, StudySearchDaoTest.PROGRAM_UUID));
	}
	
	@Test
	public void testCountStudiesByNameMatchesStartingWithWhenNameIsNull() {
		Assert.assertEquals("When study name is null, STARTS WITH search should not return any record.", 0, this.studySearchDao.countStudiesByName("",
				StudySearchMatchingOption.MATCHES_STARTING_WITH, StudySearchDaoTest.PROGRAM_UUID));
	}

	@Test
	public void testCountStudiesByNameMatchesContaining() {

		final String studyNameSearchKeyword = "Test Trial Sample";

		Assert.assertEquals("Study count should be " + StudySearchDaoTest.NO_OF_TEST_STUDIES, StudySearchDaoTest.NO_OF_TEST_STUDIES,
				this.studySearchDao.countStudiesByName(studyNameSearchKeyword, StudySearchMatchingOption.MATCHES_CONTAINING,
						StudySearchDaoTest.PROGRAM_UUID));

	}
	
	@Test
	public void testCountStudiesByNameMatchesContainingWhenNameIsEmpty() {
		Assert.assertEquals("When study name is empty, CONTAINS search should not return any record.", 0,
				this.studySearchDao.countStudiesByName("", StudySearchMatchingOption.MATCHES_CONTAINING,
						StudySearchDaoTest.PROGRAM_UUID));
	}
	
	@Test
	public void testCountStudiesByNameMatchesContainingWhenNameIsNull() {
		Assert.assertEquals("When study name is null, CONTAINS search should not return any record.", 0,
				this.studySearchDao.countStudiesByName(null, StudySearchMatchingOption.MATCHES_CONTAINING,
						StudySearchDaoTest.PROGRAM_UUID));
	}

	@Test
	public void testCountStudiesByNameExcludingDeletedStudies() throws Exception {
		this.addStudyForDeletion();
		final String studyNameSearchKeyword = "DELETE";
		final long previousCount = this.studySearchDao.countStudiesByName(studyNameSearchKeyword,
				StudySearchMatchingOption.MATCHES_CONTAINING, StudySearchDaoTest.PROGRAM_UUID);
		Assert.assertEquals("There should be 1 study with name containing " + studyNameSearchKeyword, 1, previousCount);

		// Delete test study
		final Integer userId = this.fieldbookService.getStudy(this.idOfTrialToDelete).getUser();
		this.fieldbookService.deleteStudy(this.idOfTrialToDelete, userId);
		flush();

		Assert.assertEquals("Study count should be " + (previousCount - 1), previousCount - 1, this.studySearchDao
				.countStudiesByName(studyNameSearchKeyword, StudySearchMatchingOption.MATCHES_CONTAINING, StudySearchDaoTest.PROGRAM_UUID));
	}

	@Test
	public void testCountStudiesByLocationIds() {

		final List<Integer> locationIds = new ArrayList<>();
		locationIds.add(StudySearchDaoTest.LUXEMBOURG_COUNTRY_LOCATION_ID);

		Assert.assertEquals("There should be " + StudySearchDaoTest.NO_OF_TEST_STUDIES + " studies that are in Luxembourg",
				StudySearchDaoTest.NO_OF_TEST_STUDIES,
				this.studySearchDao.countStudiesByLocationIds(locationIds, StudySearchDaoTest.PROGRAM_UUID));

	}

	@Test
	public void testCountStudiesByLocationIdsExcludingDeletedStudies() throws Exception {
		this.addStudyForDeletion();

		final List<Integer> locationIds = new ArrayList<>();
		locationIds.add(StudySearchDaoTest.LUXEMBOURG_COUNTRY_LOCATION_ID);
		final long previousCount = this.studySearchDao.countStudiesByLocationIds(locationIds, StudySearchDaoTest.PROGRAM_UUID);
		Assert.assertEquals("There should be " + (StudySearchDaoTest.NO_OF_TEST_STUDIES + 1) + " studies that are in Luxembourg",
				StudySearchDaoTest.NO_OF_TEST_STUDIES + 1, previousCount);

		// Delete test study
		final Integer userId = this.fieldbookService.getStudy(this.idOfTrialToDelete).getUser();
		this.fieldbookService.deleteStudy(this.idOfTrialToDelete, userId);
		flush();

		Assert.assertEquals("Study count should be " + (previousCount - 1), previousCount - 1,
				this.studySearchDao.countStudiesByLocationIds(locationIds, StudySearchDaoTest.PROGRAM_UUID));
	}

	@Test
	public void testGetStudiesByLocationIds() {

		final List<Integer> locationIds = new ArrayList<>();
		locationIds.add(StudySearchDaoTest.LUXEMBOURG_COUNTRY_LOCATION_ID);

		final List<StudyReference> studyReferences =
				this.studySearchDao.getStudiesByLocationIds(locationIds, StudySearchDaoTest.PROGRAM_UUID);

		Assert.assertEquals("There should be " + StudySearchDaoTest.NO_OF_TEST_STUDIES + " studies that are in Luxembourg",
				StudySearchDaoTest.NO_OF_TEST_STUDIES, studyReferences.size());

	}

	@Test
	public void testGetStudiesByLocationIdsExcludingDeletedStudies() throws UnpermittedDeletionException {
		final List<Integer> locationIds = new ArrayList<>();
		locationIds.add(StudySearchDaoTest.LUXEMBOURG_COUNTRY_LOCATION_ID);

		List<StudyReference> studyReferences =
				this.studySearchDao.getStudiesByLocationIds(locationIds, StudySearchDaoTest.PROGRAM_UUID);
		final Integer previousCount = studyReferences.size();
		Assert.assertEquals("There should be " + StudySearchDaoTest.NO_OF_TEST_STUDIES + " studies that are in Luxembourg",
				StudySearchDaoTest.NO_OF_TEST_STUDIES, studyReferences.size());

		// Delete test study
		final StudyReference studyToDelete = studyReferences.get(0);
		this.fieldbookService.deleteStudy(studyToDelete.getId(), this.fieldbookService.getStudy(studyToDelete.getId()).getUser());
		flush();

		// Check that deleted study is not retrieved
		studyReferences = this.studySearchDao.getStudiesByLocationIds(locationIds, StudySearchDaoTest.PROGRAM_UUID);
		Assert.assertEquals("Deleted study should not be returned. ", previousCount - 1, studyReferences.size());
		for (final StudyReference study : studyReferences) {
			if (studyToDelete.equals(study)) {
				Assert.fail("Expecting deleted study not to be retrieved but was included in returned list.");
			}
		}

	}

	@Test
	public void testCountStudiesBySeason() {

		final long expectedActualDrySeasonCount =
				this.numberOfDrySeasonBeforeCreatingTestData + StudySearchDaoTest.NO_OF_DRY_SEASON_STUDIES;
		final long expectedActualWetSeasonCount = this.numberOfWetSeasoBeforeCreatingTestData + StudySearchDaoTest.NO_OF_WET_SEASON_STUDIES;

		Assert.assertEquals(expectedActualDrySeasonCount,
				this.studySearchDao.countStudiesBySeason(Season.DRY, StudySearchDaoTest.PROGRAM_UUID));
		Assert.assertEquals(expectedActualWetSeasonCount,
				this.studySearchDao.countStudiesBySeason(Season.WET, StudySearchDaoTest.PROGRAM_UUID));

	}

	@Test
	public void testCountStudiesBySeasonExcludingDeletedstudies() throws Exception {
		this.addStudyForDeletion();

		// +1 in dry season count for added study for deletion
		final long previousDrySeasonCount = this.numberOfDrySeasonBeforeCreatingTestData + StudySearchDaoTest.NO_OF_DRY_SEASON_STUDIES + 1;
		Assert.assertEquals(previousDrySeasonCount, this.studySearchDao.countStudiesBySeason(Season.DRY, StudySearchDaoTest.PROGRAM_UUID));

		// Delete test study
		final Integer userId = this.fieldbookService.getStudy(this.idOfTrialToDelete).getUser();
		this.fieldbookService.deleteStudy(this.idOfTrialToDelete, userId);
		flush();

		Assert.assertEquals("Study count should be " + (previousDrySeasonCount - 1), previousDrySeasonCount - 1,
				this.studySearchDao.countStudiesBySeason(Season.DRY, StudySearchDaoTest.PROGRAM_UUID));
	}

	@Test
	public void testGetStudiesBySeason() {

		final long expectedActualDrySeasonCount =
				this.numberOfDrySeasonBeforeCreatingTestData + StudySearchDaoTest.NO_OF_DRY_SEASON_STUDIES;
		final long expectedActualWetSeasonCount = this.numberOfWetSeasoBeforeCreatingTestData + StudySearchDaoTest.NO_OF_WET_SEASON_STUDIES;

		final List<StudyReference> drySeasonStudyReferences =
				this.studySearchDao.getStudiesBySeason(Season.DRY, StudySearchDaoTest.PROGRAM_UUID);
		final List<StudyReference> wetSeasonStudyReferences =
				this.studySearchDao.getStudiesBySeason(Season.WET, StudySearchDaoTest.PROGRAM_UUID);

		Assert.assertEquals(expectedActualDrySeasonCount, drySeasonStudyReferences.size());
		Assert.assertEquals(expectedActualWetSeasonCount, wetSeasonStudyReferences.size());

		final List<String> drySeasonStudyNames = new ArrayList<>();
		for (final StudyReference studyReference : drySeasonStudyReferences) {
			drySeasonStudyNames.add(studyReference.getName());
		}
		final List<String> wetSeasonStudyNames = new ArrayList<>();
		for (final StudyReference studyReference : wetSeasonStudyReferences) {
			wetSeasonStudyNames.add(studyReference.getName());
		}

		Assert.assertTrue(StudySearchDaoTest.TEST_TRIAL_NAME_1 + " should be in Dry Season study list",
				drySeasonStudyNames.contains(StudySearchDaoTest.TEST_TRIAL_NAME_1));
		Assert.assertTrue(StudySearchDaoTest.TEST_TRIAL_NAME_3 + " should be in Dry Season study list",
				drySeasonStudyNames.contains(StudySearchDaoTest.TEST_TRIAL_NAME_3));
		Assert.assertTrue(StudySearchDaoTest.TEST_TRIAL_NAME_2 + " should be in Wet Season study list",
				wetSeasonStudyNames.contains(StudySearchDaoTest.TEST_TRIAL_NAME_2));

	}

	@Test
	public void testGetStudiesBySeasonExcludingDeletedStudies() throws UnpermittedDeletionException {

		final long previousDrySeasonCount = this.numberOfDrySeasonBeforeCreatingTestData + StudySearchDaoTest.NO_OF_DRY_SEASON_STUDIES;

		List<StudyReference> drySeasonStudyReferences =
				this.studySearchDao.getStudiesBySeason(Season.DRY, StudySearchDaoTest.PROGRAM_UUID);
		Assert.assertEquals(previousDrySeasonCount, drySeasonStudyReferences.size());

		// Delete test study
		final StudyReference studyToDelete = drySeasonStudyReferences.get(0);
		this.fieldbookService.deleteStudy(studyToDelete.getId(), this.fieldbookService.getStudy(studyToDelete.getId()).getUser());
		flush();

		// Check that deleted study is not retrieved
		drySeasonStudyReferences =
				this.studySearchDao.getStudiesBySeason(Season.DRY, StudySearchDaoTest.PROGRAM_UUID);
		Assert.assertEquals("Deleted study should not be returned. ", previousDrySeasonCount - 1, drySeasonStudyReferences.size());
		for (final StudyReference study : drySeasonStudyReferences) {
			if (studyToDelete.equals(study)) {
				Assert.fail("Expecting deleted study not to be retrieved but was included in returned list.");
			}
		}
	}

	@Test
	public void testCountStudiesByStartDate() {

		Assert.assertEquals("There should be 3 studies created in the year 2020", 3,
				this.studySearchDao.countStudiesByStartDate(2020, StudySearchDaoTest.PROGRAM_UUID));
		Assert.assertEquals("There should be 2 studies created in January 2020 ", 2,
				this.studySearchDao.countStudiesByStartDate(202001, StudySearchDaoTest.PROGRAM_UUID));
		Assert.assertEquals("There should be 1 study created in December 1 2020 ", 1,
				this.studySearchDao.countStudiesByStartDate(20201201, StudySearchDaoTest.PROGRAM_UUID));

	}

	@Test
	public void testCountStudiesByStartDateExcludingDeletedStudies() throws Exception {
		this.addStudyForDeletion();

		final long previousCount = this.studySearchDao.countStudiesByStartDate(2017, StudySearchDaoTest.PROGRAM_UUID);
		Assert.assertEquals("There should be 1 study created in the year 2017", 1, previousCount);

		// Delete test study
		final Integer userId = this.fieldbookService.getStudy(this.idOfTrialToDelete).getUser();
		this.fieldbookService.deleteStudy(this.idOfTrialToDelete, userId);
		flush();

		Assert.assertEquals("Study count should be " + (previousCount - 1), previousCount - 1,
				this.studySearchDao.countStudiesByStartDate(2017, StudySearchDaoTest.PROGRAM_UUID));

	}

	@Test
	public void testGetStudiesByStartDate() {

		Assert.assertEquals("There should be 3 studies created in Year 2020", 3,
				this.studySearchDao.getStudiesByStartDate(2020, StudySearchDaoTest.PROGRAM_UUID).size());

		final List<StudyReference> studies =
				this.studySearchDao.getStudiesByStartDate(20201201, StudySearchDaoTest.PROGRAM_UUID);
		Assert.assertEquals("There should be 1 study created in December 1 2020", 1, studies.size());

		Assert.assertEquals(StudySearchDaoTest.TEST_TRIAL_NAME_3, studies.get(0).getName());

	}

	@Test
	public void testGetStudiesByStartDateExcludingDeletedStudies() throws UnpermittedDeletionException {
		List<StudyReference> studies =
				this.studySearchDao.getStudiesByStartDate(20201201, StudySearchDaoTest.PROGRAM_UUID);
		Assert.assertEquals("There should be 1 study created in December 1 2020", 3,
				this.studySearchDao.countStudiesByStartDate(2020, StudySearchDaoTest.PROGRAM_UUID));

		// Delete test study
		final StudyReference study = studies.get(0);
		this.fieldbookService.deleteStudy(study.getId(), this.fieldbookService.getStudy(study.getId()).getUser());
		flush();

		// Check that deleted study is not retrieved
		studies = this.studySearchDao.getStudiesByStartDate(20201201, StudySearchDaoTest.PROGRAM_UUID);
		Assert.assertEquals("Deleted study should not be returned. ", 0, studies.size());
	}

	private void flush() {
		this.sessionProvder.getSession().flush();
	}

	private void createTestStudies() throws Exception {

		final WorkbenchTestDataUtil workbenchTestDataUtil = new WorkbenchTestDataUtil(this.workbenchDataManager);
		final Project project = workbenchTestDataUtil.createTestProjectData();
		project.setUniqueID(StudySearchDaoTest.PROGRAM_UUID);

		final StudyDataManagerImpl studyDataManager = new StudyDataManagerImpl();
		studyDataManager.setSessionProvider(this.sessionProvder);

		final StudyTestDataInitializer studyTestDataInitializer =
				new StudyTestDataInitializer(studyDataManager, this.ontologyManager, project, this.germplasmDataDM, this.locationManager);

		// First 3 studies have location and season variables at study level
		// We need to add datasets to studies because search queries expect "Belongs to Study" record in project_relationship
		final StudyReference studyReference1 = studyTestDataInitializer.addTestStudy(StudySearchDaoTest.TEST_TRIAL_NAME_1, StudyType.T,
				String.valueOf(TermId.SEASON_DRY.getId()), String.valueOf(StudySearchDaoTest.LUXEMBOURG_COUNTRY_LOCATION_ID), "20200101",
				this.cropPrefix);
		studyTestDataInitializer.addTestDataset(studyReference1.getId());

		final StudyReference studyReference2 = studyTestDataInitializer.addTestStudy(StudySearchDaoTest.TEST_TRIAL_NAME_2, StudyType.T,
				String.valueOf(TermId.SEASON_WET.getId()), String.valueOf(StudySearchDaoTest.LUXEMBOURG_COUNTRY_LOCATION_ID), "20200102",
				this.cropPrefix);
		studyTestDataInitializer.addTestDataset(studyReference2.getId());

		final StudyReference studyReference3 = studyTestDataInitializer.addTestStudy(StudySearchDaoTest.TEST_TRIAL_NAME_3, StudyType.T,
				String.valueOf(TermId.SEASON_DRY.getId()), String.valueOf(StudySearchDaoTest.LUXEMBOURG_COUNTRY_LOCATION_ID), "20201201",
				this.cropPrefix);
		studyTestDataInitializer.addTestDataset(studyReference3.getId());

		// This study has season and location variables at environment level
		final StudyReference studyReference4 =
				studyTestDataInitializer.addTestStudy(StudyType.T, StudySearchDaoTest.TEST_TRIAL_NAME_4, this.cropPrefix);
		studyTestDataInitializer.addEnvironmentDataset(studyReference4.getId(),
				String.valueOf(StudySearchDaoTest.LUXEMBOURG_COUNTRY_LOCATION_ID), String.valueOf(TermId.SEASON_DRY.getId()));
	}

	private void addStudyForDeletion() throws Exception {

		final WorkbenchTestDataUtil workbenchTestDataUtil = new WorkbenchTestDataUtil(this.workbenchDataManager);
		final Project project = workbenchTestDataUtil.createTestProjectData();
		project.setUniqueID(StudySearchDaoTest.PROGRAM_UUID);

		final StudyDataManagerImpl studyDataManager = new StudyDataManagerImpl();
		studyDataManager.setSessionProvider(this.sessionProvder);

		final StudyTestDataInitializer studyTestDataInitializer =
				new StudyTestDataInitializer(studyDataManager, this.ontologyManager, project, this.germplasmDataDM, this.locationManager);

		// We need to add datasets to studies because search queries expect "Belongs to Study" record in project_relationship
		final StudyReference studyReference1 = studyTestDataInitializer.addTestStudy(StudySearchDaoTest.TEST_STUDY_TO_DELETE, StudyType.T,
				String.valueOf(TermId.SEASON_DRY.getId()), String.valueOf(StudySearchDaoTest.LUXEMBOURG_COUNTRY_LOCATION_ID), "20170101",
				this.cropPrefix);
		studyTestDataInitializer.addTestDataset(studyReference1.getId());
		this.idOfTrialToDelete = studyReference1.getId();
	}

}
