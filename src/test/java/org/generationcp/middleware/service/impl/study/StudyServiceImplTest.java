
package org.generationcp.middleware.service.impl.study;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.manager.api.StudyDataManager;
import org.generationcp.middleware.manager.api.UserDataManager;
import org.generationcp.middleware.service.api.study.MeasurementDto;
import org.generationcp.middleware.service.api.study.MeasurementVariableDto;
import org.generationcp.middleware.service.api.study.MeasurementVariableService;
import org.generationcp.middleware.service.api.study.ObservationDto;
import org.generationcp.middleware.service.api.study.StudyDetailsDto;
import org.generationcp.middleware.service.api.study.StudyGermplasmListService;
import org.generationcp.middleware.service.api.study.StudyMetadata;
import org.generationcp.middleware.service.api.study.StudySearchParameters;
import org.generationcp.middleware.service.api.study.StudySummary;
import org.generationcp.middleware.service.api.user.UserDto;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import com.beust.jcommander.internal.Lists;

/**
 * The class <code>StudyServiceImplTest</code> contains tests for the class <code>{@link StudyServiceImpl}</code>.
 *
 * @author Akhil
 */
public class StudyServiceImplTest {
	
	@Mock
	private Session mockSession;

	@Mock
	private SQLQuery mockSqlQuery;

	@Mock
	private HibernateSessionProvider mockSessionProvider;

	private StudyServiceImpl studyServiceImpl;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		this.studyServiceImpl = new StudyServiceImpl(this.mockSessionProvider);
		Mockito.when(this.mockSessionProvider.getSession()).thenReturn(this.mockSession);
		Mockito.when(this.mockSession.createSQLQuery(Matchers.anyString())).thenReturn(this.mockSqlQuery);
		Mockito.when(this.mockSqlQuery.addScalar(Matchers.anyString())).thenReturn(this.mockSqlQuery);
	}

	@Test
	public void testHasMeasurementDataOnEnvironmentAssertTrue() throws Exception {
		Mockito.when(this.mockSqlQuery.uniqueResult()).thenReturn(1);

		final Session mockSession = Mockito.mock(Session.class);
		final HibernateSessionProvider mockSessionProvider = Mockito.mock(HibernateSessionProvider.class);
		Mockito.when(mockSessionProvider.getSession()).thenReturn(mockSession);
		Mockito.when(mockSessionProvider.getSession().createSQLQuery(StudyServiceImpl.SQL_FOR_COUNT_TOTAL_OBSERVATION_UNITS_NO_NULL_VALUES))
				.thenReturn(mockSqlQuery);

		StudyServiceImpl studyServiceImpl = new StudyServiceImpl(mockSessionProvider);

		Assert.assertTrue(studyServiceImpl.hasMeasurementDataOnEnvironment(123, 4));
	}

	@Test
	public void testHasMeasurementDataOnEnvironmentAssertFalse() throws Exception {
		Mockito.when(this.mockSqlQuery.uniqueResult()).thenReturn(0);

		final Session mockSession = Mockito.mock(Session.class);
		final HibernateSessionProvider mockSessionProvider = Mockito.mock(HibernateSessionProvider.class);
		Mockito.when(mockSessionProvider.getSession()).thenReturn(mockSession);
		Mockito.when(mockSessionProvider.getSession().createSQLQuery(StudyServiceImpl.SQL_FOR_COUNT_TOTAL_OBSERVATION_UNITS_NO_NULL_VALUES))
				.thenReturn(mockSqlQuery);

		StudyServiceImpl studyServiceImpl = new StudyServiceImpl(mockSessionProvider);

		Assert.assertFalse(studyServiceImpl.hasMeasurementDataOnEnvironment(123, 4));
	}

	@Test
	public void testHasMeasurementDataEnteredAssertTrue() throws Exception {
		final Object[] testDBRow = {2503,51547, "AleuCol_E_1to5", 43};
		final List<Object[]> testResult = Arrays.<Object[]>asList(testDBRow);
		Mockito.when(this.mockSqlQuery.list()).thenReturn(testResult);

		final Session mockSession = Mockito.mock(Session.class);
		final HibernateSessionProvider mockSessionProvider = Mockito.mock(HibernateSessionProvider.class);
		Mockito.when(mockSessionProvider.getSession()).thenReturn(mockSession);
		Mockito.when(mockSessionProvider.getSession().createSQLQuery(StudyServiceImpl.SQL_FOR_HAS_MEASUREMENT_DATA_ENTERED))
			.thenReturn(mockSqlQuery);

		StudyServiceImpl studyServiceImpl = new StudyServiceImpl(mockSessionProvider);
		List<Integer> ids = Arrays.asList(1000,1002);
		assertThat(true, is(equalTo(studyServiceImpl.hasMeasurementDataEntered(ids, 4))));
	}

	@Test
	public void testHasMeasurementDataEnteredAssertFalse() throws Exception {
		final List<Object[]> testResult = Arrays.<Object[]>asList();

		Mockito.when(this.mockSqlQuery.list()).thenReturn(testResult);

		final Session mockSession = Mockito.mock(Session.class);
		final HibernateSessionProvider mockSessionProvider = Mockito.mock(HibernateSessionProvider.class);
		Mockito.when(mockSessionProvider.getSession()).thenReturn(mockSession);
		Mockito.when(mockSessionProvider.getSession().createSQLQuery(StudyServiceImpl.SQL_FOR_HAS_MEASUREMENT_DATA_ENTERED))
			.thenReturn(mockSqlQuery);

		StudyServiceImpl studyServiceImpl = new StudyServiceImpl(mockSessionProvider);
		List<Integer> ids = Arrays.asList(1000,1002);
		assertThat(false,is(equalTo(studyServiceImpl.hasMeasurementDataEntered(ids, 4))));
	}

	/**
	 * Run the StudyServiceImpl(HibernateSessionProvider) constructor test.
	 *
	 */
	@Test
	public void listMeasurementData() throws Exception {
		final MeasurementVariableService mockTrialTraits = Mockito.mock(MeasurementVariableService.class);
		final StudyMeasurements mockTrailMeasurements = Mockito.mock(StudyMeasurements.class);
		final StudyGermplasmListService mockStudyGermplasmListService = Mockito.mock(StudyGermplasmListService.class);
		final GermplasmDescriptors germplasmDescriptorService = Mockito.mock(GermplasmDescriptors.class);

		final StudyServiceImpl result =
				new StudyServiceImpl(mockTrialTraits, mockTrailMeasurements, mockStudyGermplasmListService, germplasmDescriptorService);

		final List<MeasurementVariableDto> projectTraits = Arrays.<MeasurementVariableDto>asList(new MeasurementVariableDto(1, "Trait1"), new MeasurementVariableDto(1, "Trait2"));
		final List<String> germplasmDescriptors = Lists.newArrayList("STOCK_ID");
		Mockito.when(mockTrialTraits.getVariables(1234)).thenReturn(projectTraits);
		final List<MeasurementDto> traits = new ArrayList<MeasurementDto>();
		traits.add(new MeasurementDto(new MeasurementVariableDto(1, "traitName"), 9999, "triatValue"));
		final ObservationDto measurement = new ObservationDto(1, "trialInstance", "entryType", 1234, "designation", "entryNo", "seedSource",
				"repitionNumber", "plotNumber", "blockNumber", traits);
		final List<ObservationDto> testMeasurements = Collections.<ObservationDto>singletonList(measurement);
		Mockito.when(mockTrailMeasurements.getAllMeasurements(1234, projectTraits, germplasmDescriptors, 1, 1, 100, null, null))
				.thenReturn(testMeasurements);
		result.getObservations(1234, 1, 1, 100, null, null);

		final List<ObservationDto> allMeasurements =
				mockTrailMeasurements.getAllMeasurements(1234, projectTraits, germplasmDescriptors, 1, 1, 100, null, null);
		Assert.assertEquals(allMeasurements, testMeasurements);
	}

	@Test
	public void testlistAllStudies() throws MiddlewareQueryException {

		final Object[] testDBRow = {2007, "Wheat Trial 1", "Wheat Trial 1 Title", "c996de54-3ebb-41ca-8fed-160a33ffffd4", StudyType.T.getName(),
				"Wheat Trial 1 Objective", "20150417", "20150422", "Mr. Breeder", "Auckland", "Summer"};
		final List<Object[]> testResult = Arrays.<Object[]>asList(testDBRow);

		Mockito.when(this.mockSqlQuery.list()).thenReturn(testResult);

		final StudySearchParameters searchParameters = new StudySearchParameters();
		searchParameters.setProgramUniqueId("c996de54-3ebb-41ca-8fed-160a33ffffd4");
		final List<StudySummary> studySummaries = this.studyServiceImpl.search(searchParameters);
		Assert.assertNotNull(studySummaries);
		Assert.assertEquals(1, studySummaries.size());

		final StudySummary studySummary = studySummaries.get(0);

		Assert.assertEquals(testDBRow[0], studySummary.getId());
		Assert.assertEquals(testDBRow[1], studySummary.getName());
		Assert.assertEquals(testDBRow[2], studySummary.getTitle());
		Assert.assertEquals(testDBRow[3], studySummary.getProgramUUID());
		Assert.assertEquals(testDBRow[4], studySummary.getType().getName());
		Assert.assertEquals(testDBRow[5], studySummary.getObjective());
		Assert.assertEquals(testDBRow[6], studySummary.getStartDate());
		Assert.assertEquals(testDBRow[7], studySummary.getEndDate());
		Assert.assertEquals(testDBRow[8], studySummary.getPrincipalInvestigator());
		Assert.assertEquals(testDBRow[9], studySummary.getLocation());
		Assert.assertEquals(testDBRow[10], studySummary.getSeason());

	}

	@Test
	public void testGetStudyInstances() throws Exception {

		final Object[] testDBRow = {12345, "Gujarat, India", "GUJ", 1};
		final List<Object[]> testResult = Arrays.<Object[]>asList(testDBRow);
		Mockito.when(this.mockSqlQuery.list()).thenReturn(testResult);

		final List<StudyInstance> studyInstances = this.studyServiceImpl.getStudyInstances(123);

		Assert.assertTrue(studyInstances.size() == 1);
		Assert.assertEquals(testDBRow[0], studyInstances.get(0).getInstanceDbId());
		Assert.assertEquals(testDBRow[1], studyInstances.get(0).getLocationName());
		Assert.assertEquals(testDBRow[2], studyInstances.get(0).getLocationAbbreviation());
		Assert.assertEquals(testDBRow[3], studyInstances.get(0).getInstanceNumber());
	}

	@Test
	public void testGetStudyDetailsForANursery() {
		List<String> seasons = new ArrayList<>();
		seasons.add("WET");
		final StudyMetadata metadata =
				new StudyMetadata(2, 2, 4, Boolean.TRUE, "20160101", "20170101", 8, seasons, "trialName", StudyType.N.getName(),
					"studyName");

		UserDto user = new UserDto();
		user.setEmail("a@a.com");
		user.setFirstName("name");
		user.setLastName("last");
		user.setRole("ADMIN");
		user.setUserId(1);
		List<UserDto> users = new ArrayList<>();
		users.add(user);

		Map<String, String> properties = new HashMap<>();
		properties.put("p1", "v1");

		final Session mockSession = Mockito.mock(Session.class);
		final HibernateSessionProvider mockSessionProvider = Mockito.mock(HibernateSessionProvider.class);
		Mockito.when(mockSessionProvider.getSession()).thenReturn(mockSession);

		final StudyDataManager studyDataManager = Mockito.mock(StudyDataManager.class);
		final UserDataManager userDataManager = Mockito.mock(UserDataManager.class);

		StudyServiceImpl studyServiceImpl = new StudyServiceImpl(mockSessionProvider);
		studyServiceImpl.setStudyDataManager(studyDataManager);
		studyServiceImpl.setUserDataManager(userDataManager);

		Mockito.when(studyDataManager.getStudyMetadata(metadata.getStudyDbId())).thenReturn(metadata);
		Mockito.when(userDataManager.getUsersAssociatedToStudy(metadata.getNurseryOrTrialId())).thenReturn(users);
		Mockito.when(studyDataManager.getProjectPropsAndValuesByStudy(metadata.getNurseryOrTrialId())).thenReturn(properties);

		final StudyDetailsDto studyDetailsDto = studyServiceImpl.getStudyDetails(metadata.getStudyDbId());

		assertThat(studyDetailsDto.getMetadata().getActive(), equalTo(metadata.getActive()));
		assertThat(studyDetailsDto.getMetadata().getEndDate(), equalTo(metadata.getEndDate()));
		assertThat(studyDetailsDto.getMetadata().getLocationId(), equalTo(metadata.getLocationId()));
		assertThat(studyDetailsDto.getMetadata().getNurseryOrTrialId(), equalTo(metadata.getNurseryOrTrialId()));
		assertThat(studyDetailsDto.getMetadata().getSeasons().size(), equalTo(metadata.getSeasons().size()));
		assertThat(studyDetailsDto.getMetadata().getStudyDbId(), equalTo(metadata.getStudyDbId()));
		assertThat(studyDetailsDto.getMetadata().getStudyName(), equalTo(metadata.getStudyName()));
		assertThat(studyDetailsDto.getMetadata().getTrialDbId(), equalTo(metadata.getTrialDbId()));
		assertThat(studyDetailsDto.getMetadata().getStartDate(), equalTo(metadata.getStartDate()));
		assertThat(studyDetailsDto.getMetadata().getTrialName(), equalTo(metadata.getTrialName()));
		assertThat(studyDetailsDto.getMetadata().getStudyType(), equalTo(metadata.getStudyType()));
		assertThat(studyDetailsDto.getAdditionalInfo().size(), equalTo(properties.size()));
		assertThat(studyDetailsDto.getContacts().size(), equalTo(users.size()));

	}

	@Test
	public void testGetStudyDetailsForATrial() {
		List<String> seasons = new ArrayList<>();
		seasons.add("WET");
		final StudyMetadata metadata =
				new StudyMetadata(2, 2, 4, Boolean.TRUE, "20160101", "20170101", 8, seasons, "trialName", StudyType.T.getName(),
					"studyName");

		UserDto user = new UserDto();
		user.setEmail("a@a.com");
		user.setFirstName("name");
		user.setLastName("last");
		user.setRole("ADMIN");
		user.setUserId(1);
		List<UserDto> users1 = new ArrayList<>();
		users1.add(user);

		List<UserDto> users2 = new ArrayList<>();


		Map<String, String> properties1 = new HashMap<>();
		properties1.put("p1", "v1");

		Map<String, String> properties2 = new HashMap<>();
		properties2.put("p2", "v2");

		final Session mockSession = Mockito.mock(Session.class);
		final HibernateSessionProvider mockSessionProvider = Mockito.mock(HibernateSessionProvider.class);
		Mockito.when(mockSessionProvider.getSession()).thenReturn(mockSession);

		final StudyDataManager studyDataManager = Mockito.mock(StudyDataManager.class);
		final UserDataManager userDataManager = Mockito.mock(UserDataManager.class);

		StudyServiceImpl studyServiceImpl = new StudyServiceImpl(mockSessionProvider);
		studyServiceImpl.setStudyDataManager(studyDataManager);
		studyServiceImpl.setUserDataManager(userDataManager);

		Mockito.when(studyDataManager.getStudyMetadata(metadata.getStudyDbId())).thenReturn(metadata);
		Mockito.when(userDataManager.getUsersAssociatedToStudy(metadata.getNurseryOrTrialId())).thenReturn(users1);
		Mockito.when(studyDataManager.getProjectPropsAndValuesByStudy(metadata.getNurseryOrTrialId())).thenReturn(properties1);
		Mockito.when(userDataManager.getUsersForEnvironment(metadata.getNurseryOrTrialId())).thenReturn(users2);
		Mockito.when(studyDataManager.getGeolocationPropsAndValuesByStudy(metadata.getNurseryOrTrialId())).thenReturn(properties2);


		final StudyDetailsDto studyDetailsDto = studyServiceImpl.getStudyDetails(metadata.getStudyDbId());

		assertThat(studyDetailsDto.getMetadata().getActive(), equalTo(metadata.getActive()));
		assertThat(studyDetailsDto.getMetadata().getEndDate(), equalTo(metadata.getEndDate()));
		assertThat(studyDetailsDto.getMetadata().getLocationId(), equalTo(metadata.getLocationId()));
		assertThat(studyDetailsDto.getMetadata().getNurseryOrTrialId(), equalTo(metadata.getNurseryOrTrialId()));
		assertThat(studyDetailsDto.getMetadata().getSeasons().size(), equalTo(metadata.getSeasons().size()));
		assertThat(studyDetailsDto.getMetadata().getStudyDbId(), equalTo(metadata.getStudyDbId()));
		assertThat(studyDetailsDto.getMetadata().getStudyName(), equalTo(metadata.getStudyName()));
		assertThat(studyDetailsDto.getMetadata().getTrialDbId(), equalTo(metadata.getTrialDbId()));
		assertThat(studyDetailsDto.getMetadata().getStartDate(), equalTo(metadata.getStartDate()));
		assertThat(studyDetailsDto.getMetadata().getTrialName(), equalTo(metadata.getTrialName()));
		assertThat(studyDetailsDto.getMetadata().getStudyType(), equalTo(metadata.getStudyType()));
		assertThat(studyDetailsDto.getAdditionalInfo().size(), equalTo(properties1.size() + properties2.size()));
		assertThat(studyDetailsDto.getContacts().size(), equalTo(users1.size() + users2.size()));

	}
}
