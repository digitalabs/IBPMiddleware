package org.generationcp.middleware.api.germplasm;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeService;
import org.generationcp.middleware.dao.AttributeDAO;
import org.generationcp.middleware.dao.BibrefDAO;
import org.generationcp.middleware.dao.GermplasmDAO;
import org.generationcp.middleware.dao.GermplasmListDataDAO;
import org.generationcp.middleware.dao.LocationDAO;
import org.generationcp.middleware.dao.MethodDAO;
import org.generationcp.middleware.dao.NameDAO;
import org.generationcp.middleware.dao.UserDefinedFieldDAO;
import org.generationcp.middleware.domain.germplasm.GermplasmDto;
import org.generationcp.middleware.domain.germplasm.GermplasmNameDto;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmImportDTO;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmImportRequestDto;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmMatchRequestDto;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareRequestException;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.manager.DaoFactory;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.pojos.Attribute;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.MethodType;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class GermplasmServiceImplTest {

	private static final Integer GID = new Random().nextInt(Integer.MAX_VALUE);
	private static final Integer FIELD_NUMBER = ThreadLocalRandom.current().nextInt();

	@InjectMocks
	private GermplasmServiceImpl germplasmService;

	@Mock
	private DaoFactory daoFactory;

	@Mock
	private AttributeDAO attributeDAO;

	@Mock
	private MethodDAO methodDAO;

	@Mock
	private LocationDAO locationDAO;

	@Mock
	private UserDefinedFieldDAO userDefinedFieldDAO;

	@Mock
	private GermplasmDAO germplasmDAO;

	@Mock
	private NameDAO nameDAO;

	@Mock
	private BibrefDAO bibrefDAO;

	@Mock
	private OntologyDataManager ontologyDataManager;

	@Mock
	private WorkbenchDataManager workbenchDataManager;

	@Mock
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Mock
	private GermplasmAttributeService germplasmAttributeService;

	@Captor
	private ArgumentCaptor<List<Integer>> integerListArgumentCaptor;

	@Mock
	private GermplasmNameTypeService germplasmNameTypeService;

	private final String locationAbbreviation = RandomStringUtils.randomAlphabetic(3);

	private final String methodAbbreviation = RandomStringUtils.randomAlphabetic(3);

	private final String germplasmPUI = RandomStringUtils.randomAlphabetic(36);

	private final String cropName = "maize";

	private final String programUUID = RandomStringUtils.randomAlphabetic(16);

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		//Mock DaoFactory
		ReflectionTestUtils.setField(this.germplasmService, "daoFactory", this.daoFactory);
		Mockito.when(this.daoFactory.getAttributeDAO()).thenReturn(this.attributeDAO);
		Mockito.when(this.daoFactory.getUserDefinedFieldDAO()).thenReturn(this.userDefinedFieldDAO);
		Mockito.when(this.daoFactory.getGermplasmDao()).thenReturn(this.germplasmDAO);
		Mockito.when(this.daoFactory.getMethodDAO()).thenReturn(this.methodDAO);
		Mockito.when(this.daoFactory.getLocationDAO()).thenReturn(this.locationDAO);
		Mockito.when(this.daoFactory.getNameDao()).thenReturn(this.nameDAO);
		Mockito.when(this.daoFactory.getBibrefDAO()).thenReturn(this.bibrefDAO);

		this.germplasmService.setWorkbenchDataManager(this.workbenchDataManager);
		this.germplasmService.setOntologyDataManager(this.ontologyDataManager);
		this.germplasmService.setOntologyVariableDataManager(this.ontologyVariableDataManager);
		this.germplasmService.setGermplasmAttributeService(this.germplasmAttributeService);
		this.germplasmService.setGermplasmNameTypeService(this.germplasmNameTypeService);

	}

	@Test
	public void testGetPlotCodeValue() {
		final GermplasmServiceImpl unitToTest = new GermplasmServiceImpl(Mockito.mock(HibernateSessionProvider.class));
		unitToTest.setGermplasmAttributeService(this.germplasmAttributeService);

		// We want to mock away calls to other methods in same unit.
		final GermplasmServiceImpl partiallyMockedUnit = Mockito.spy(unitToTest);

		// First set up data such that no plot code attribute is associated.
		Mockito.doReturn(null).when(partiallyMockedUnit).getPlotCodeField();
		final List<Attribute> attributes = new ArrayList<>();
		Mockito.doReturn(attributes).when(this.germplasmAttributeService).getAttributesByGID(ArgumentMatchers.anyInt());

		final String plotCode1 = partiallyMockedUnit.getPlotCodeValue(GID);
		assertThat("getPlotCodeValue() should never return null.", plotCode1, is(notNullValue()));
		assertThat("Expected `Unknown` returned when there is no plot code attribute present.", "Unknown", is(plotCode1));
		// Now setup data so that gid has plot code attribute associated with it.
		final Term plotCodeVariable = Mockito.mock(Term.class);
		Mockito.when(plotCodeVariable.getName()).thenReturn(GermplasmServiceImpl.PLOT_CODE);

		Mockito.when(partiallyMockedUnit.getPlotCodeField()).thenReturn(plotCodeVariable);
		final Attribute plotCodeAttr = new Attribute();
		plotCodeAttr.setTypeId(plotCodeVariable.getId());
		plotCodeAttr.setAval("The PlotCode Value");
		attributes.add(plotCodeAttr);
		Mockito.when(this.germplasmAttributeService.getAttributesByGID(GID)).thenReturn(attributes);

		final String plotCode2 = partiallyMockedUnit.getPlotCodeValue(GID);
		assertThat("getPlotCodeValue() should never return null.", plotCode2, is(notNullValue()));
		assertThat("Expected value of plot code attribute returned when plot code attribute is present.", plotCodeAttr.getAval(),
			is(equalTo(plotCode2)));
	}

	@Test
	public void test_getPlotCodeValues_OK() {
		final int unknownPlotCodeGid = new Random().nextInt();
		final String plotCodeValue = UUID.randomUUID().toString();

		//Mock attribute
		final Attribute attribute = Mockito.mock(Attribute.class);
		Mockito.when(attribute.getGermplasmId()).thenReturn(GID);
		Mockito.when(attribute.getAval()).thenReturn(plotCodeValue);
		final List<Attribute> attributes = Collections.singletonList(attribute);
		Mockito.when(this.attributeDAO.getAttributeValuesByTypeAndGIDList(ArgumentMatchers.eq(FIELD_NUMBER), ArgumentMatchers.anyList()))
			.thenReturn(attributes);

		//Create partial mock for the unit to be tested
		final GermplasmServiceImpl partiallyMockedUnit =
			Mockito.spy(new GermplasmServiceImpl(Mockito.mock(HibernateSessionProvider.class)));
		ReflectionTestUtils.setField(partiallyMockedUnit, "daoFactory", this.daoFactory);

		//Mock GermplasmServiceImpl#getPlotCodeField);
		final Term term = Mockito.mock(Term.class);
		Mockito.when(term.getId()).thenReturn(FIELD_NUMBER);
		Mockito.doReturn(term).when(partiallyMockedUnit).getPlotCodeField();

		final Map<Integer, String> plotCodeValues = partiallyMockedUnit.getPlotCodeValues(ImmutableSet.of(GID, unknownPlotCodeGid));
		assertNotNull(plotCodeValues);
		assertThat(plotCodeValues.size(), is(2));
		assertTrue(plotCodeValues.containsKey(GID));
		assertThat(plotCodeValues.get(GID), is(plotCodeValue));

		assertTrue(plotCodeValues.containsKey(unknownPlotCodeGid));
		assertThat(plotCodeValues.get(unknownPlotCodeGid), is(GermplasmListDataDAO.SOURCE_UNKNOWN));

		Mockito.verify(this.attributeDAO)
			.getAttributeValuesByTypeAndGIDList(ArgumentMatchers.eq(FIELD_NUMBER), this.integerListArgumentCaptor
				.capture());
		final List<Integer> actualGIDs = this.integerListArgumentCaptor.getValue();
		assertNotNull(actualGIDs);
		assertThat(actualGIDs.size(), is(2));
		assertThat(actualGIDs, hasItems(GID, unknownPlotCodeGid));
	}

	@Test
	public void test_getPlotCodeField_OK() {
		final Term plotCodeMock = Mockito.mock(Term.class);
		Mockito.when(plotCodeMock.getName()).thenReturn(GermplasmServiceImpl.PLOT_CODE);
		this.mockOntologyServiceFindByName(plotCodeMock);

		final Term actualPlotCodeVariable = this.germplasmService.getPlotCodeField();
		assertThat(actualPlotCodeVariable, is(plotCodeMock));
	}

	@Test(expected = MiddlewareRequestException.class)
	public void test_importGermplasm_ThrowsExceptions_WhenProgenitorsAreInvalid() {
		final GermplasmImportRequestDto germplasmImportRequestDto = new GermplasmImportRequestDto();
		germplasmImportRequestDto.setConnectUsing(GermplasmImportRequestDto.PedigreeConnectionType.GID);
		final GermplasmImportDTO germplasmImportDTO = this.createGermplasmImportDto();
		germplasmImportDTO.setProgenitor1("1");
		germplasmImportDTO.setProgenitor2("2");
		germplasmImportRequestDto.setGermplasmList(Collections.singletonList(germplasmImportDTO));

		Mockito.when(this.germplasmDAO.getByGIDList(Mockito.anyList())).thenReturn(Collections.singletonList(new Germplasm()));
		Mockito.when(this.methodDAO.getByCode(Mockito.anyList())).thenReturn(Collections.emptyList());
		Mockito.when(this.locationDAO.getByAbbreviations(Mockito.anyList())).thenReturn(Collections.emptyList());
		Mockito.when(this.workbenchDataManager.getCropTypeByName(this.cropName)).thenReturn(new CropType());

		this.germplasmService.importGermplasm(this.cropName, this.programUUID, germplasmImportRequestDto);
	}

	@Test
	public void test_importGermplasm_MatchesAreNotLoaded_WhenSkipWhenMatchesIsFalse() {
		final GermplasmServiceImpl unitToTest = new GermplasmServiceImpl(Mockito.mock(HibernateSessionProvider.class));
		unitToTest.setWorkbenchDataManager(this.workbenchDataManager);
		unitToTest.setGermplasmNameTypeService(this.germplasmNameTypeService);

		final GermplasmServiceImpl partiallyMockedUnit = Mockito.spy(unitToTest);
		ReflectionTestUtils.setField(partiallyMockedUnit, "daoFactory", this.daoFactory);

		final GermplasmImportRequestDto germplasmImportRequestDto = new GermplasmImportRequestDto();
		germplasmImportRequestDto.setConnectUsing(GermplasmImportRequestDto.PedigreeConnectionType.NONE);

		germplasmImportRequestDto.setGermplasmList(Collections.singletonList(this.createGermplasmImportDto()));

		Mockito.when(this.germplasmDAO.getByGIDList(Mockito.anyList())).thenReturn(Collections.singletonList(new Germplasm()));
		Mockito.when(this.methodDAO.getByCode(Mockito.anyList())).thenReturn(Collections.singletonList(this.createMethod()));
		Mockito.when(this.locationDAO.getByAbbreviations(Mockito.anyList())).thenReturn(Collections.emptyList());
		Mockito.when(this.workbenchDataManager.getCropTypeByName(this.cropName)).thenReturn(new CropType());

		partiallyMockedUnit.importGermplasm(this.cropName, this.programUUID, germplasmImportRequestDto);
		Mockito.verify(partiallyMockedUnit, Mockito.times(0)).findGermplasmMatches(Mockito.any(), Mockito.isNull());
	}

	@Test
	public void test_importGermplasm_SaveGermplasmIsNeverCalled_WhenAMatchIsFound() {
		final GermplasmServiceImpl unitToTest = new GermplasmServiceImpl(Mockito.mock(HibernateSessionProvider.class));
		unitToTest.setWorkbenchDataManager(this.workbenchDataManager);
		unitToTest.setGermplasmNameTypeService(this.germplasmNameTypeService);

		final GermplasmServiceImpl partiallyMockedUnit = Mockito.spy(unitToTest);
		ReflectionTestUtils.setField(partiallyMockedUnit, "daoFactory", this.daoFactory);

		final GermplasmImportRequestDto germplasmImportRequestDto = new GermplasmImportRequestDto();
		germplasmImportRequestDto.setConnectUsing(GermplasmImportRequestDto.PedigreeConnectionType.NONE);
		germplasmImportRequestDto.setSkipIfExists(true);
		germplasmImportRequestDto.setGermplasmList(Collections.singletonList(this.createGermplasmImportDto()));

		Mockito.when(this.germplasmDAO.getByGIDList(Mockito.anyList())).thenReturn(Collections.singletonList(new Germplasm()));
		Mockito.when(this.methodDAO.getByCode(Mockito.anyList())).thenReturn(Collections.singletonList(this.createMethod()));
		Mockito.when(this.locationDAO.getByAbbreviations(Mockito.anyList())).thenReturn(Collections.emptyList());
		Mockito.when(this.workbenchDataManager.getCropTypeByName(this.cropName)).thenReturn(new CropType());

		Mockito.doReturn(Collections.singletonList(this.createGermplasmDto())).when(partiallyMockedUnit)
			.findGermplasmMatches(Mockito.any(GermplasmMatchRequestDto.class), ArgumentMatchers.isNull());

		partiallyMockedUnit.importGermplasm(this.cropName, this.programUUID, germplasmImportRequestDto);
		Mockito.verify(this.germplasmDAO, Mockito.times(0)).save(Mockito.any());
	}

	@Test
	public void test_importGermplasm_ReferenceIsSet_WhenAReferenceIsSpecified() {
		final GermplasmServiceImpl unitToTest = new GermplasmServiceImpl(Mockito.mock(HibernateSessionProvider.class));
		unitToTest.setWorkbenchDataManager(this.workbenchDataManager);
		unitToTest.setGermplasmNameTypeService(this.germplasmNameTypeService);

		final GermplasmServiceImpl partiallyMockedUnit = Mockito.spy(unitToTest);
		ReflectionTestUtils.setField(partiallyMockedUnit, "daoFactory", this.daoFactory);

		final GermplasmImportRequestDto germplasmImportRequestDto = new GermplasmImportRequestDto();
		germplasmImportRequestDto.setConnectUsing(GermplasmImportRequestDto.PedigreeConnectionType.NONE);
		germplasmImportRequestDto.setGermplasmList(Collections.singletonList(this.createGermplasmImportDto()));

		Mockito.when(this.germplasmDAO.getByGIDList(Mockito.anyList())).thenReturn(Collections.singletonList(new Germplasm()));
		Mockito.when(this.methodDAO.getByCode(Mockito.anyList())).thenReturn(Collections.singletonList(this.createMethod()));
		Mockito.when(this.locationDAO.getByAbbreviations(Mockito.anyList())).thenReturn(Collections.singletonList(this.createLocation()));
		Mockito.when(this.workbenchDataManager.getCropTypeByName(this.cropName)).thenReturn(new CropType());
		Mockito.when(this.germplasmNameTypeService.filterGermplasmNameTypes(Mockito.anySet())).thenReturn(Collections.emptyList());

		partiallyMockedUnit.importGermplasm(this.cropName, this.programUUID, germplasmImportRequestDto);
		Mockito.verify(this.bibrefDAO, Mockito.times(1)).save(Mockito.any());
	}

	private void mockOntologyServiceFindByName(final Term term) {
		Mockito.when(this.ontologyDataManager.findTermByName(ArgumentMatchers.eq("PLOTCODE_AP_text"),
			ArgumentMatchers.eq(1040))).thenReturn(term);
	}

	private GermplasmImportDTO createGermplasmImportDto() {
		final GermplasmImportDTO germplasmImportDTO = new GermplasmImportDTO();
		final Map<String, String> names = new HashMap<>();
		names.put("LNAME", "N");
		germplasmImportDTO.setNames(names);
		germplasmImportDTO.setBreedingMethodAbbr(this.methodAbbreviation);
		germplasmImportDTO.setLocationAbbr(this.locationAbbreviation);
		germplasmImportDTO.setCreationDate("20201212");
		germplasmImportDTO.setReference("Reference");
		germplasmImportDTO.setGermplasmPUI(this.germplasmPUI);
		return germplasmImportDTO;
	}

	private Method createMethod() {
		final Method method = new Method();
		method.setMid(1);
		method.setMprgn(0);
		method.setMcode(this.methodAbbreviation.toUpperCase());
		method.setMtype(MethodType.GENERATIVE.getCode());
		return method;
	}

	private Location createLocation() {
		final Location location = new Location();
		location.setLocid(1);
		location.setLabbr(this.locationAbbreviation.toUpperCase());
		return location;
	}

	private GermplasmDto createGermplasmDto() {
		final GermplasmDto germplasmDto = new GermplasmDto();
		germplasmDto.setGermplasmPUI(this.germplasmPUI);
		germplasmDto.setGid(1);
		final GermplasmNameDto germplasmNameDto = new GermplasmNameDto();
		germplasmNameDto.setName("name");
		germplasmDto.setNames(Lists.newArrayList(germplasmNameDto));
		return germplasmDto;
	}

}
