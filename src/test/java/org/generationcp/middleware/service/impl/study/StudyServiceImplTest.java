
package org.generationcp.middleware.service.impl.study;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.service.api.study.ObservationDto;
import org.generationcp.middleware.service.api.study.StudyGermplasmListService;
import org.generationcp.middleware.service.api.study.StudySummary;
import org.generationcp.middleware.service.api.study.TraitDto;
import org.generationcp.middleware.service.api.study.MeasurementDto;
import org.generationcp.middleware.service.api.study.TraitService;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * The class <code>StudyServiceImplTest</code> contains tests for the class <code>{@link StudyServiceImpl}</code>.
 *
 * @author Akhil
 */
public class StudyServiceImplTest {

	/**
	 * Run the StudyServiceImpl(HibernateSessionProvider) constructor test.
	 *
	 */
	@Test
	public void listMeasurementData() throws Exception {
		TraitService mockTrialTraits = Mockito.mock(TraitService.class);
		StudyMeasurements mockTrailMeasurements = Mockito.mock(StudyMeasurements.class);
		StudyGermplasmListService mockStudyGermplasmListService = Mockito.mock(StudyGermplasmListService.class);
		DataImportService mockDataImportService = Mockito.mock(DataImportService.class);
		FieldbookService mockFieldbookService = Mockito.mock(FieldbookService.class); 
		
		StudyServiceImpl result = new StudyServiceImpl(mockTrialTraits, mockTrailMeasurements,
			mockStudyGermplasmListService, mockDataImportService, mockFieldbookService);

		List<TraitDto> projectTraits = Arrays.<TraitDto>asList(new TraitDto(1, "Trait1"), new TraitDto(1, "Trait2"));
		when(mockTrialTraits.getTraits(1234)).thenReturn(projectTraits);
		final List<MeasurementDto> traits = new ArrayList<MeasurementDto>();
		traits.add(new MeasurementDto(new TraitDto(1, "traitName"), 9999, "triatValue"));
		final ObservationDto measurement =
				new ObservationDto(1, "trialInstance", "entryType", 1234, "designation", "entryNo", "seedSource", "repitionNumber",
						"plotNumber", traits);
		final List<ObservationDto> testMeasurements = Collections.<ObservationDto>singletonList(measurement);
		when(mockTrailMeasurements.getAllMeasurements(1234, projectTraits)).thenReturn(testMeasurements);
		result.getObservations(1234);

		final List<ObservationDto> allMeasurements = mockTrailMeasurements.getAllMeasurements(1234, projectTraits);
		assertEquals(allMeasurements, testMeasurements);
	}
	
	@Test
	public void testlistAllStudies() throws MiddlewareQueryException {
		Session mockSession = Mockito.mock(Session.class);
		SQLQuery mockSqlQuery = Mockito.mock(SQLQuery.class);
		
		HibernateSessionProvider mockSessionProvider = Mockito.mock(HibernateSessionProvider.class);
		Mockito.when(mockSessionProvider.getSession()).thenReturn(mockSession);
		Mockito.when(mockSession.createSQLQuery(Mockito.anyString())).thenReturn(mockSqlQuery);
		Mockito.when(mockSqlQuery.addScalar(Mockito.anyString())).thenReturn(mockSqlQuery);
		
		final Object[] testDBRow =
				{2007, "Wheat Trial 1", "Wheat Trial 1 Title", "c996de54-3ebb-41ca-8fed-160a33ffffd4", "10010", "Wheat Trial 1 Objective", "20150417", "20150422"};
		final List<Object[]> testResult = Arrays.<Object[]>asList(testDBRow);
		
		Mockito.when(mockSqlQuery.list()).thenReturn(testResult);
		
		StudyServiceImpl studyServiceImpl = new StudyServiceImpl(mockSessionProvider);
		List<StudySummary> studySummaries = studyServiceImpl.listAllStudies("c996de54-3ebb-41ca-8fed-160a33ffffd4");
		Assert.assertNotNull(studySummaries);
		Assert.assertEquals(1, studySummaries.size());
		
		StudySummary studySummary = studySummaries.get(0);
		
		Assert.assertEquals(testDBRow[0], studySummary.getId());
		Assert.assertEquals(testDBRow[1], studySummary.getName());
		Assert.assertEquals(testDBRow[2], studySummary.getTitle());
		Assert.assertEquals(testDBRow[3], studySummary.getProgramUUID());
		Assert.assertEquals(testDBRow[4], String.valueOf(studySummary.getType().getId()));
		Assert.assertEquals(testDBRow[5], studySummary.getObjective());
		Assert.assertEquals(testDBRow[6], studySummary.getStartDate());
		Assert.assertEquals(testDBRow[7], studySummary.getEndDate());
		
	}
}

