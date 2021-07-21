package org.generationcp.middleware.brapi;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.IntegrationTestBase;
import org.generationcp.middleware.api.brapi.GermplasmServiceBrapi;
import org.generationcp.middleware.api.brapi.v1.germplasm.GermplasmDTO;
import org.generationcp.middleware.api.brapi.v2.germplasm.ExternalReferenceDTO;
import org.generationcp.middleware.api.brapi.v2.germplasm.GermplasmImportRequest;
import org.generationcp.middleware.api.brapi.v2.germplasm.GermplasmUpdateRequest;
import org.generationcp.middleware.api.brapi.v2.germplasm.Synonym;
import org.generationcp.middleware.api.germplasm.GermplasmServiceImpl;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.exceptions.MiddlewareRequestException;
import org.generationcp.middleware.manager.DaoFactory;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.daoElements.VariableFilter;
import org.generationcp.middleware.pojos.Attribute;
import org.generationcp.middleware.pojos.Bibref;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmExternalReference;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.MethodType;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.UDTableType;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.util.Util;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class GermplasmServiceBrapiImplTest extends IntegrationTestBase {

	private static final String DRVNM = "DRVNM";
	private static final String NOLOC = "NOLOC";
	private static final String NOTE = "NOTE_AA_text";

	private DaoFactory daoFactory;

	@Autowired
	private GermplasmServiceBrapi germplasmServiceBrapi;

	@Autowired
	private OntologyVariableDataManager ontologyVariableDataManager;

	private Integer noLocationId, userId, puiNameTypeId;
	private String creationDate, germplasmPUI, germplasmUUID;
	private Method derivativeMethod, generativeMethod;

	@Before
	public void setUp() {
		this.daoFactory = new DaoFactory(this.sessionProvder);
		this.noLocationId = this.daoFactory.getLocationDAO().getByAbbreviations(Arrays.asList(NOLOC)).get(0).getLocid();
		this.derivativeMethod = this.createBreedingMethod(MethodType.DERIVATIVE.getCode(), -1);
		this.generativeMethod = this.createBreedingMethod(MethodType.GENERATIVE.getCode(), 2);
		this.creationDate = "20201212";
		this.germplasmPUI = RandomStringUtils.randomAlphabetic(20);
		this.germplasmUUID = RandomStringUtils.randomAlphabetic(10);
		this.userId = this.findAdminUser();
		this.puiNameTypeId = this.daoFactory.getUserDefinedFieldDAO().getByTableTypeAndCode("NAMES", "NAME", "PUI").getFldno();

	}

	@Test
	public void test_createGermplasm_Ok() {
		final String creationDate = "2020-10-24";
		final GermplasmImportRequest request = new GermplasmImportRequest(RandomStringUtils.randomAlphabetic(20), creationDate,
			this.derivativeMethod.getMid().toString(), RandomStringUtils.randomAlphabetic(20), "UKN",
			RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20));
		final String germplasmPUI = RandomStringUtils.randomAlphabetic(40);
		request.setGermplasmPUI(germplasmPUI);
		request.getSynonyms().add(new Synonym(RandomStringUtils.randomAlphabetic(20), DRVNM));
		request.getAdditionalInfo().put(NOTE, RandomStringUtils.randomAlphabetic(20));

		final ExternalReferenceDTO externalReferenceDTO = new ExternalReferenceDTO();
		externalReferenceDTO.setReferenceID(UUID.randomUUID().toString());
		externalReferenceDTO.setReferenceSource(UUID.randomUUID().toString());
		request.setExternalReferences(Arrays.asList(externalReferenceDTO));

		final List<GermplasmDTO> germplasmDTOList =
			this.germplasmServiceBrapi.createGermplasm(ContextHolder.getCurrentCrop(), Collections.singletonList(request));
		assertThat(germplasmDTOList.size(), is(1));

		final GermplasmDTO germplasmDTO = germplasmDTOList.get(0);
		assertThat(germplasmDTO.getGid(), notNullValue());
		final Integer gid = Integer.parseInt(germplasmDTO.getGid());
		assertThat(germplasmDTO.getBreedingMethodDbId(), equalTo(this.derivativeMethod.getMid().toString()));
		assertThat(germplasmDTO.getCountryOfOriginCode(), equalTo("UKN"));
		assertThat(germplasmDTO.getAcquisitionDate(), equalTo(Util.tryParseDate(creationDate, Util.FRONTEND_DATE_FORMAT)));
		assertThat(germplasmDTO.getGermplasmDbId(), notNullValue());
		assertThat(germplasmDTO.getEntryNumber(), nullValue());
		assertThat(germplasmDTO.getDefaultDisplayName(), equalTo(request.getDefaultDisplayName()));
		assertThat(germplasmDTO.getGermplasmName(), equalTo(request.getDefaultDisplayName()));
		assertThat(germplasmDTO.getGermplasmPUI(), equalTo(germplasmPUI));

		this.verifyGermplasmNamesSaved(request, germplasmDTO, gid);
		this.verifyGermplasmAttributesSaved(request, germplasmDTO, gid);
		assertTrue(germplasmDTO.getSynonyms().size() > 0);
		assertTrue(germplasmDTO.getAdditionalInfo().size() > 0);

		final Germplasm germplasm = this.daoFactory.getGermplasmDao().getById(gid);
		assertThat(germplasm.getGnpgs(), equalTo(0));
		assertThat(germplasm.getGpid1(), equalTo(0));
		assertThat(germplasm.getGpid2(), equalTo(0));
		assertThat(germplasm.getCreatedBy(), equalTo(this.userId));

		assertThat(germplasm.getExternalReferences(), hasSize(1));
		final GermplasmExternalReference externalReference = germplasm.getExternalReferences().get(0);
		assertThat(externalReference.getCreatedBy(), is(this.userId));
		assertNotNull(externalReference.getCreatedDate());
	}

	@Test
	public void test_createGermplasm_SetPUISynonym_Ok() {
		final String creationDate = "2020-10-24";
		final GermplasmImportRequest request = new GermplasmImportRequest(RandomStringUtils.randomAlphabetic(20), creationDate,
			this.derivativeMethod.getMid().toString(), RandomStringUtils.randomAlphabetic(20), "UKN",
			RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20));
		final String germplasmPUI = RandomStringUtils.randomAlphabetic(40);
		request.getSynonyms().add(new Synonym(RandomStringUtils.randomAlphabetic(20), DRVNM));
		request.getSynonyms().add(new Synonym(germplasmPUI, GermplasmImportRequest.PUI_NAME_TYPE));
		request.getAdditionalInfo().put(NOTE, RandomStringUtils.randomAlphabetic(20));

		final ExternalReferenceDTO externalReferenceDTO = new ExternalReferenceDTO();
		externalReferenceDTO.setReferenceID(UUID.randomUUID().toString());
		externalReferenceDTO.setReferenceSource(UUID.randomUUID().toString());
		request.setExternalReferences(Arrays.asList(externalReferenceDTO));

		final List<GermplasmDTO> germplasmDTOList =
			this.germplasmServiceBrapi.createGermplasm(ContextHolder.getCurrentCrop(), Collections.singletonList(request));
		assertThat(germplasmDTOList.size(), is(1));

		final GermplasmDTO germplasmDTO = germplasmDTOList.get(0);
		assertThat(germplasmDTO.getGid(), notNullValue());
		final Integer gid = Integer.parseInt(germplasmDTO.getGid());
		assertThat(germplasmDTO.getBreedingMethodDbId(), equalTo(this.derivativeMethod.getMid().toString()));
		assertThat(germplasmDTO.getCountryOfOriginCode(), equalTo("UKN"));
		assertThat(germplasmDTO.getAcquisitionDate(), equalTo(Util.tryParseDate(creationDate, Util.FRONTEND_DATE_FORMAT)));
		assertThat(germplasmDTO.getGermplasmDbId(), notNullValue());
		assertThat(germplasmDTO.getEntryNumber(), nullValue());
		assertThat(germplasmDTO.getDefaultDisplayName(), equalTo(request.getDefaultDisplayName()));
		assertThat(germplasmDTO.getGermplasmName(), equalTo(request.getDefaultDisplayName()));
		assertThat(germplasmDTO.getGermplasmPUI(), equalTo(germplasmPUI));

		this.verifyGermplasmNamesSaved(request, germplasmDTO, gid);
		this.verifyGermplasmAttributesSaved(request, germplasmDTO, gid);
		assertTrue(germplasmDTO.getSynonyms().size() > 0);
		assertTrue(germplasmDTO.getAdditionalInfo().size() > 0);

		final Germplasm germplasm = this.daoFactory.getGermplasmDao().getById(gid);
		assertThat(germplasm.getGnpgs(), equalTo(0));
		assertThat(germplasm.getGpid1(), equalTo(0));
		assertThat(germplasm.getGpid2(), equalTo(0));
		assertThat(germplasm.getCreatedBy(), equalTo(this.userId));

		assertThat(germplasm.getExternalReferences(), hasSize(1));
		final GermplasmExternalReference externalReference = germplasm.getExternalReferences().get(0);
		assertThat(externalReference.getCreatedBy(), is(this.userId));
		assertNotNull(externalReference.getCreatedDate());
	}

	private void verifyGermplasmAttributesSaved(final GermplasmImportRequest request, final GermplasmDTO germplasmDTO, final Integer gid) {
		final VariableFilter variableFilter = new VariableFilter();
		GermplasmServiceImpl.ATTRIBUTE_TYPES.forEach(variableFilter::addVariableType);
		GermplasmImportRequest.BRAPI_SPECIFIABLE_ATTRTYPES.forEach(variableFilter::addName);

		final Map<String, Integer> existingAttrTypes = this.ontologyVariableDataManager.getWithFilter(variableFilter)
			.stream().collect(Collectors.toMap(Variable::getName, Variable::getId));

		final Map<Integer, String> germplasmAttributes =
			this.daoFactory.getAttributeDAO().getAttributeValuesGIDList(Collections.singletonList(gid)).stream()
				.collect(Collectors.toMap(Attribute::getTypeId, Attribute::getAval));
		if (existingAttrTypes.containsKey(GermplasmImportRequest.PLOTCODE_ATTR)) {
			assertThat(germplasmDTO.getSeedSource(), equalTo(request.getSeedSource()));
			assertThat(germplasmAttributes.get(existingAttrTypes.get(GermplasmImportRequest.PLOTCODE_ATTR)),
				equalTo(request.getSeedSource()));
			assertThat(germplasmDTO.getAdditionalInfo().get(GermplasmImportRequest.PLOTCODE_ATTR), equalTo(request.getSeedSource()));
		}
		if (existingAttrTypes.containsKey(GermplasmImportRequest.ORIGIN_ATTR)) {
			assertThat(germplasmDTO.getGermplasmOrigin(), equalTo(request.getGermplasmOrigin()));
			assertThat(germplasmAttributes.get(existingAttrTypes.get(GermplasmImportRequest.ORIGIN_ATTR)),
				equalTo(request.getGermplasmOrigin()));
			assertThat(germplasmDTO.getAdditionalInfo().get(GermplasmImportRequest.ORIGIN_ATTR), equalTo(request.getGermplasmOrigin()));
		}
		if (existingAttrTypes.containsKey(GermplasmImportRequest.CROPNM_ATTR)) {
			assertThat(germplasmDTO.getCommonCropName(), equalTo(request.getCommonCropName()));
			assertThat(germplasmAttributes.get(existingAttrTypes.get(GermplasmImportRequest.CROPNM_ATTR)),
				equalTo(request.getCommonCropName()));
			assertThat(germplasmDTO.getAdditionalInfo().get(GermplasmImportRequest.CROPNM_ATTR), equalTo(request.getCommonCropName()));
		}
		if (existingAttrTypes.containsKey(GermplasmImportRequest.SPECIES_ATTR)) {
			assertThat(germplasmDTO.getSpecies(), equalTo(request.getSpecies()));
			assertThat(germplasmAttributes.get(existingAttrTypes.get(GermplasmImportRequest.SPECIES_ATTR)), equalTo(request.getSpecies()));
			assertThat(germplasmDTO.getAdditionalInfo().get(GermplasmImportRequest.SPECIES_ATTR), equalTo(request.getSpecies()));
		}
		if (existingAttrTypes.containsKey(GermplasmImportRequest.SPECIES_AUTH_ATTR)) {
			assertThat(germplasmDTO.getSpeciesAuthority(), equalTo(request.getSpeciesAuthority()));
			assertThat(germplasmAttributes.get(existingAttrTypes.get(GermplasmImportRequest.SPECIES_AUTH_ATTR)),
				equalTo(request.getSpeciesAuthority()));
			assertThat(germplasmDTO.getAdditionalInfo().get(GermplasmImportRequest.SPECIES_AUTH_ATTR),
				equalTo(request.getSpeciesAuthority()));
		}
		if (existingAttrTypes.containsKey(GermplasmImportRequest.SUBTAX_ATTR)) {
			assertThat(germplasmDTO.getSubtaxa(), equalTo(request.getSubtaxa()));
			assertThat(germplasmAttributes.get(existingAttrTypes.get(GermplasmImportRequest.SUBTAX_ATTR)), equalTo(request.getSubtaxa()));
			assertThat(germplasmDTO.getAdditionalInfo().get(GermplasmImportRequest.SUBTAX_ATTR), equalTo(request.getSubtaxa()));
		}
		if (existingAttrTypes.containsKey(GermplasmImportRequest.SUBTAX_AUTH_ATTR)) {
			assertThat(germplasmDTO.getSubtaxaAuthority(), equalTo(request.getSubtaxaAuthority()));
			assertThat(germplasmAttributes.get(existingAttrTypes.get(GermplasmImportRequest.SUBTAX_AUTH_ATTR)),
				equalTo(request.getSubtaxaAuthority()));
			assertThat(germplasmDTO.getAdditionalInfo().get(GermplasmImportRequest.SUBTAX_AUTH_ATTR),
				equalTo(request.getSubtaxaAuthority()));
		}
	}

	private void verifyGermplasmNamesSaved(final GermplasmImportRequest request, final GermplasmDTO germplasmDTO, final Integer gid) {
		final Map<String, Integer> existingNameTypes = this.daoFactory.getUserDefinedFieldDAO()
			.getByCodes(UDTableType.NAMES_NAME.getTable(),
				Collections.singleton(UDTableType.NAMES_NAME.getType()), new HashSet<>(GermplasmImportRequest.BRAPI_SPECIFIABLE_NAMETYPES))
			.stream().collect(Collectors.toMap(UserDefinedField::getFcode, UserDefinedField::getFldno));

		final Map<Integer, String> germplasmNames = this.daoFactory.getNameDao().getNamesByGids(Collections.singletonList(gid)).stream()
			.collect(Collectors.toMap(Name::getTypeId, Name::getNval));
		if (existingNameTypes.containsKey(GermplasmImportRequest.ACCNO_NAME_TYPE)) {
			assertThat(germplasmDTO.getAccessionNumber(), equalTo(request.getAccessionNumber()));
			assertThat(germplasmNames.get(existingNameTypes.get(GermplasmImportRequest.ACCNO_NAME_TYPE)), equalTo(request.getAccessionNumber()));
			assertThat(
				germplasmDTO.getSynonyms().stream().filter(synonym -> synonym.getType().equals(GermplasmImportRequest.ACCNO_NAME_TYPE)).findAny()
					.get().getSynonym(), equalTo(request.getAccessionNumber()));
		}
		if (existingNameTypes.containsKey(GermplasmImportRequest.GENUS_NAME_TYPE)) {
			assertThat(germplasmDTO.getGenus(), equalTo(request.getGenus()));
			assertThat(germplasmNames.get(existingNameTypes.get(GermplasmImportRequest.GENUS_NAME_TYPE)), equalTo(request.getGenus()));
			assertThat(
				germplasmDTO.getSynonyms().stream().filter(synonym -> synonym.getType().equals(GermplasmImportRequest.GENUS_NAME_TYPE)).findAny()
					.get().getSynonym(), equalTo(request.getGenus()));
		}
		if (existingNameTypes.containsValue(GermplasmImportRequest.PED_NAME_TYPE)) {
			assertThat(germplasmNames.get(existingNameTypes.get(GermplasmImportRequest.PED_NAME_TYPE)), equalTo(request.getPedigree()));
			assertThat(
				germplasmDTO.getSynonyms().stream().filter(synonym -> synonym.getType().equals(GermplasmImportRequest.PED_NAME_TYPE)).findAny().get()
					.getSynonym(), equalTo(request.getPedigree()));
		}
		if (existingNameTypes.containsValue(GermplasmImportRequest.PUI_NAME_TYPE)) {
			assertThat(germplasmNames.get(existingNameTypes.get(GermplasmImportRequest.PUI_NAME_TYPE)), equalTo(request.getGermplasmPUI()));
			assertThat(
				germplasmDTO.getSynonyms().stream().filter(synonym -> synonym.getType().equals(GermplasmImportRequest.PUI_NAME_TYPE)).findAny().get()
					.getSynonym(), equalTo(request.getGermplasmPUI()));
		}
	}

	@Test
	public void test_createGermplasm_ThrowsException_WhenGermplasmPUISynonymExistsAlready() {
		final Germplasm germplasm = this.createGermplasm(this.generativeMethod, this.germplasmPUI, null, 0, 0, 0, this.germplasmPUI);

		final String creationDate = "2020-10-24";
		final GermplasmImportRequest request = new GermplasmImportRequest(RandomStringUtils.randomAlphabetic(20), creationDate,
			this.derivativeMethod.getMid().toString(), RandomStringUtils.randomAlphabetic(20), "UKN",
			RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20));
		request.getSynonyms().add(new Synonym(this.germplasmPUI, GermplasmImportRequest.PUI_NAME_TYPE));
		final List<GermplasmImportRequest> germplasmImportRequestList = Collections.singletonList(request);
		try {
			this.germplasmServiceBrapi.createGermplasm(ContextHolder.getCurrentCrop(), germplasmImportRequestList);
			Assert.fail("Expected to throw exception for existing PUI but did not.");
		} catch (final MiddlewareRequestException exception) {
			assertTrue(exception.getErrorCodeParamsMultiMap().containsKey("brapi.import.germplasm.pui.exists"));
		}
	}

	@Test
	public void test_updateGermplasm_Ok() {
		final Method method = this.createBreedingMethod("GEN", 2);
		final Germplasm germplasm = this.createGermplasm(method, this.germplasmUUID, null, 2, 0, 0, this.germplasmPUI);

		final String newCreationDate = "2020-10-24";
		final Method methodNew = this.createBreedingMethod("GEN", 2);
		final String location = "ARG";
		final GermplasmUpdateRequest request = new GermplasmUpdateRequest(RandomStringUtils.randomAlphabetic(20), newCreationDate,
			methodNew.getMid().toString(), RandomStringUtils.randomAlphabetic(20), location,
			RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20));
		request.getSynonyms().add(new Synonym(RandomStringUtils.randomAlphabetic(20), DRVNM));
		request.getAdditionalInfo().put(NOTE, RandomStringUtils.randomAlphabetic(20));
		final String newPUI = RandomStringUtils.randomAlphabetic(100);
		request.setGermplasmPUI(newPUI);

		final GermplasmDTO germplasmDTO = this.germplasmServiceBrapi.updateGermplasm(germplasm.getGermplasmUUID(), request);
		final Integer gid = germplasm.getGid();
		assertThat(germplasmDTO.getGid(), equalTo(gid.toString()));
		assertThat(germplasmDTO.getGermplasmDbId(), equalTo(germplasm.getGermplasmUUID()));
		assertThat(germplasmDTO.getBreedingMethodDbId(), equalTo(methodNew.getMid().toString()));
		assertThat(germplasmDTO.getCountryOfOriginCode(), equalTo(location));
		assertThat(germplasmDTO.getAcquisitionDate(), equalTo(Util.tryParseDate(newCreationDate, Util.FRONTEND_DATE_FORMAT)));
		assertThat(germplasmDTO.getEntryNumber(), nullValue());
		assertThat(germplasmDTO.getDefaultDisplayName(), equalTo(request.getDefaultDisplayName()));
		assertThat(germplasmDTO.getGermplasmName(), equalTo(request.getDefaultDisplayName()));
		assertThat(germplasmDTO.getGermplasmPUI(), equalTo(newPUI));

		this.verifyGermplasmNamesSaved(request, germplasmDTO, gid);
		this.verifyGermplasmAttributesSaved(request, germplasmDTO, gid);
		assertTrue(germplasmDTO.getSynonyms().size() > 0);
		assertTrue(germplasmDTO.getAdditionalInfo().size() > 0);

		// Verify that originally saved field values are unmodified
		final Germplasm germplasmLatest = this.daoFactory.getGermplasmDao().getById(gid);
		assertThat(germplasmLatest.getGnpgs(), equalTo(germplasm.getGnpgs()));
		assertThat(germplasmLatest.getGpid1(), equalTo(germplasm.getGpid1()));
		assertThat(germplasmLatest.getGpid2(), equalTo(germplasm.getGpid2()));
	}

	@Test
	public void test_updateGermplasm_RetainOldPUI_Ok() {
		final Method method = this.createBreedingMethod("GEN", 2);
		final Germplasm germplasm = this.createGermplasm(method, this.germplasmPUI, null, 2, 0, 0, this.germplasmPUI);

		final String newCreationDate = "2020-10-24";
		final Method methodNew = this.createBreedingMethod("GEN", 2);
		final String location = "ARG";
		final GermplasmUpdateRequest request = new GermplasmUpdateRequest(RandomStringUtils.randomAlphabetic(20), newCreationDate,
			methodNew.getMid().toString(), RandomStringUtils.randomAlphabetic(20), location,
			RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20));
		request.getSynonyms().add(new Synonym(RandomStringUtils.randomAlphabetic(20), DRVNM));
		request.getAdditionalInfo().put(NOTE, RandomStringUtils.randomAlphabetic(20));
		request.setGermplasmPUI(this.germplasmPUI);

		final GermplasmDTO germplasmDTO = this.germplasmServiceBrapi.updateGermplasm(germplasm.getGermplasmUUID(), request);
		final Integer gid = germplasm.getGid();
		assertThat(germplasmDTO.getGid(), equalTo(gid.toString()));
		assertThat(germplasmDTO.getGermplasmDbId(), equalTo(germplasm.getGermplasmUUID()));
		assertThat(germplasmDTO.getBreedingMethodDbId(), equalTo(methodNew.getMid().toString()));
		assertThat(germplasmDTO.getCountryOfOriginCode(), equalTo(location));
		assertThat(germplasmDTO.getAcquisitionDate(), equalTo(Util.tryParseDate(newCreationDate, Util.FRONTEND_DATE_FORMAT)));
		assertThat(germplasmDTO.getEntryNumber(), nullValue());
		assertThat(germplasmDTO.getDefaultDisplayName(), equalTo(request.getDefaultDisplayName()));
		assertThat(germplasmDTO.getGermplasmName(), equalTo(request.getDefaultDisplayName()));
		assertThat(germplasmDTO.getGermplasmPUI(), equalTo(this.germplasmPUI));

		this.verifyGermplasmNamesSaved(request, germplasmDTO, gid);
		this.verifyGermplasmAttributesSaved(request, germplasmDTO, gid);
		assertTrue(germplasmDTO.getSynonyms().size() > 0);
		assertTrue(germplasmDTO.getAdditionalInfo().size() > 0);

		// Verify that originally saved field values are unmodified
		final Germplasm germplasmLatest = this.daoFactory.getGermplasmDao().getById(gid);
		assertThat(germplasmLatest.getGnpgs(), equalTo(germplasm.getGnpgs()));
		assertThat(germplasmLatest.getGpid1(), equalTo(germplasm.getGpid1()));
		assertThat(germplasmLatest.getGpid2(), equalTo(germplasm.getGpid2()));
	}

	@Test
	public void test_updateGermplasm_OnlyAddSynonymsAndAttributes_Ok() {
		final Method method = this.createBreedingMethod("GEN", 2);
		final Germplasm germplasm = this.createGermplasm(method, this.germplasmUUID, null, 2, 0, 0, this.germplasmPUI);

		// Specify null for those fields we don't want to update like breeding method, location, germplasm date
		final GermplasmUpdateRequest request = new GermplasmUpdateRequest(RandomStringUtils.randomAlphabetic(20), null,
			null, RandomStringUtils.randomAlphabetic(20), null,
			null,
			RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20));
		request.getSynonyms().add(new Synonym(RandomStringUtils.randomAlphabetic(20), DRVNM));
		// It should be okay to specify the same existing PUI through the synonym
		request.getSynonyms().add(new Synonym(this.germplasmPUI, GermplasmImportRequest.PUI_NAME_TYPE));

		request.getAdditionalInfo().put(NOTE, RandomStringUtils.randomAlphabetic(20));

		final GermplasmDTO germplasmDTO = this.germplasmServiceBrapi.updateGermplasm(germplasm.getGermplasmUUID(), request);
		final Integer gid = germplasm.getGid();
		assertThat(germplasmDTO.getGid(), equalTo(gid.toString()));
		// Germplasm details remain unchanged
		assertThat(germplasmDTO.getGermplasmDbId(), equalTo(germplasm.getGermplasmUUID()));
		assertThat(germplasmDTO.getBreedingMethodDbId(), equalTo(germplasm.getMethodId().toString()));
		assertThat(germplasmDTO.getCountryOfOriginCode(), equalTo("UKN"));
		assertThat(germplasmDTO.getAcquisitionDate(), equalTo(Util.tryParseDate(this.creationDate, Util.DATE_AS_NUMBER_FORMAT)));
		assertThat(germplasmDTO.getDefaultDisplayName(), equalTo(request.getDefaultDisplayName()));
		assertThat(germplasmDTO.getGermplasmName(), equalTo(request.getDefaultDisplayName()));

		this.verifyGermplasmNamesSaved(request, germplasmDTO, gid);
		this.verifyGermplasmAttributesSaved(request, germplasmDTO, gid);
		assertTrue(germplasmDTO.getSynonyms().size() > 0);
		assertTrue(germplasmDTO.getAdditionalInfo().size() > 0);

		// Verify that originally saved field values are unmodified
		final Germplasm germplasmLatest = this.daoFactory.getGermplasmDao().getById(gid);
		assertThat(germplasmLatest.getGnpgs(), equalTo(germplasm.getGnpgs()));
		assertThat(germplasmLatest.getGpid1(), equalTo(germplasm.getGpid1()));
		assertThat(germplasmLatest.getGpid2(), equalTo(germplasm.getGpid2()));
	}

	@Test
	public void test_updateGermplasm_UpdateSynonymsAndAttributes_Ok() {
		final Method method = this.createBreedingMethod("GEN", 2);
		final Germplasm germplasm = this.createGermplasm(method, this.germplasmUUID, null, 2, 0, 0, this.germplasmPUI);

		// Specify null for those fields we don't want to update like breeding method, location, germplasm date
		// Save the first version of names and attributes
		final GermplasmUpdateRequest request1 = new GermplasmUpdateRequest(RandomStringUtils.randomAlphabetic(20), null,
			null, RandomStringUtils.randomAlphabetic(20), null,
			null,
			RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20));
		request1.getSynonyms().add(new Synonym(RandomStringUtils.randomAlphabetic(20), DRVNM));
		final String newPUI = RandomStringUtils.randomAlphabetic(40);
		request1.getSynonyms().add(new Synonym(newPUI, GermplasmImportRequest.PUI_NAME_TYPE));
		request1.getAdditionalInfo().put(NOTE, RandomStringUtils.randomAlphabetic(20));
		this.germplasmServiceBrapi.updateGermplasm(germplasm.getGermplasmUUID(), request1);

		// Update the names and attributes with new values
		final GermplasmUpdateRequest request2 = new GermplasmUpdateRequest(RandomStringUtils.randomAlphabetic(20), null,
			null, RandomStringUtils.randomAlphabetic(20), null,
			null,
			RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20));
		request2.getSynonyms().add(new Synonym(RandomStringUtils.randomAlphabetic(20), DRVNM));
		request2.getAdditionalInfo().put(NOTE, RandomStringUtils.randomAlphabetic(20));

		final GermplasmDTO germplasmDTO = this.germplasmServiceBrapi.updateGermplasm(germplasm.getGermplasmUUID(), request2);
		final Integer gid = germplasm.getGid();
		assertThat(germplasmDTO.getGid(), equalTo(gid.toString()));
		// Germplasm details remain unchanged
		assertThat(germplasmDTO.getGermplasmDbId(), equalTo(germplasm.getGermplasmUUID()));
		assertThat(germplasmDTO.getBreedingMethodDbId(), equalTo(germplasm.getMethodId().toString()));
		assertThat(germplasmDTO.getCountryOfOriginCode(), equalTo("UKN"));
		assertThat(germplasmDTO.getAcquisitionDate(), equalTo(Util.tryParseDate(this.creationDate, Util.DATE_AS_NUMBER_FORMAT)));
		assertThat(germplasmDTO.getDefaultDisplayName(), equalTo(request2.getDefaultDisplayName()));
		assertThat(germplasmDTO.getGermplasmName(), equalTo(request2.getDefaultDisplayName()));
		assertThat(germplasmDTO.getGermplasmPUI(), equalTo(newPUI));

		this.verifyGermplasmNamesSaved(request2, germplasmDTO, gid);
		this.verifyGermplasmAttributesSaved(request2, germplasmDTO, gid);
		assertTrue(germplasmDTO.getSynonyms().size() > 0);
		assertTrue(germplasmDTO.getAdditionalInfo().size() > 0);

		// Verify that originally saved field values are unmodified
		final Germplasm germplasmLatest = this.daoFactory.getGermplasmDao().getById(gid);
		assertThat(germplasmLatest.getGnpgs(), equalTo(germplasm.getGnpgs()));
		assertThat(germplasmLatest.getGpid1(), equalTo(germplasm.getGpid1()));
		assertThat(germplasmLatest.getGpid2(), equalTo(germplasm.getGpid2()));
	}

	@Test
	public void test_updateGermplasm_InvalidGUID() {
		final Method method = this.createBreedingMethod("GEN", 2);
		this.createGermplasm(method, this.germplasmUUID, null, 2, 0, 0, null);

		final String creationDate = "2020-10-24";
		final GermplasmUpdateRequest request = new GermplasmUpdateRequest(RandomStringUtils.randomAlphabetic(20), creationDate, null,
			RandomStringUtils.randomAlphabetic(20), "UKN",
			RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20));

		final String germplasmDbId = UUID.randomUUID().toString();
		try {
			this.germplasmServiceBrapi.updateGermplasm(germplasmDbId, request);
			Assert.fail("Expected to throw exception that germplasm with GUID does not exist but did not");
		} catch (final MiddlewareRequestException exception) {
			assertTrue(exception.getErrorCodeParamsMultiMap().containsKey("germplasm.invalid.guid"));
		}


	}

	@Test
	public void test_updateGermplasm_PUIAlreadyExists() {
		final Method method = this.createBreedingMethod("GEN", 2);
		final Germplasm germplasm = this.createGermplasm(method, this.germplasmUUID, null, 2, 0, 0, this.germplasmPUI);
		// Create another germplasm with given PUI
		final String pui2 = RandomStringUtils.randomAlphabetic(40);
		final Germplasm germplasm2 = this.createGermplasm(method, null, null, 2, 0, 0, pui2);

		final String creationDate = "2020-10-24";
		final GermplasmUpdateRequest request = new GermplasmUpdateRequest(RandomStringUtils.randomAlphabetic(20), creationDate, null,
			RandomStringUtils.randomAlphabetic(20), "UKN",
			RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20));
		request.setGermplasmPUI(pui2);
		try {
			this.germplasmServiceBrapi.updateGermplasm(this.germplasmUUID, request);
			Assert.fail("Expected to throw exception that germplasm PUI already exists but did not");
		} catch (final MiddlewareRequestException exception) {
			assertTrue(exception.getErrorCodeParamsMultiMap().containsKey("brapi.update.germplasm.pui.exists"));
		}
	}

	@Test
	public void test_updateGermplasm_PUISynonymAlreadyExists() {
		final Method method = this.createBreedingMethod("GEN", 2);
		final Germplasm germplasm = this.createGermplasm(method, this.germplasmUUID, null, 2, 0, 0, this.germplasmPUI);
		// Create another germplasm with given PUI
		final String pui2 = RandomStringUtils.randomAlphabetic(40);
		final Germplasm germplasm2 = this.createGermplasm(method, null, null, 2, 0, 0, pui2);

		final String creationDate = "2020-10-24";
		final GermplasmUpdateRequest request = new GermplasmUpdateRequest(RandomStringUtils.randomAlphabetic(20), creationDate, null,
			RandomStringUtils.randomAlphabetic(20), "UKN",
			RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20));
		request.getSynonyms().add(new Synonym(pui2, "PUI"));
		try {
			this.germplasmServiceBrapi.updateGermplasm(this.germplasmUUID, request);
			Assert.fail("Expected to throw exception that germplasm PUI already exists but did not");
		} catch (final MiddlewareRequestException exception) {
			assertTrue(exception.getErrorCodeParamsMultiMap().containsKey("brapi.update.germplasm.pui.exists"));
		}
	}

	@Test(expected = MiddlewareRequestException.class)
	public void test_updateGermplasm_MethodMutation() {
		final Method method = this.createBreedingMethod("GEN", 2);
		this.createGermplasm(method, this.germplasmUUID, null, 2, 0, 0, null);

		final String creationDate = "2020-10-24";
		// New method is "DER" type while old one is "GEN" type. Expecting to cause method mutation validation error
		final Method methodNew = this.createBreedingMethod("DER", 2);
		final GermplasmUpdateRequest request = new GermplasmUpdateRequest(RandomStringUtils.randomAlphabetic(20), creationDate,
			methodNew.getMid().toString(), RandomStringUtils.randomAlphabetic(20), "UKN",
			RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20));

		this.germplasmServiceBrapi.updateGermplasm(this.germplasmUUID, request);
		Assert.fail("Expected to throw exception that breeding method type is invalid but did not");
	}

	@Test(expected = MiddlewareRequestException.class)
	public void test_updateGermplasm_MethodProgenitorsError() {
		final Method method = this.createBreedingMethod("GEN", 2);
		this.createGermplasm(method, this.germplasmUUID, null, 2, 0, 0, null);

		final String creationDate = "2020-10-24";
		// New method has mpgrn = 1 while old one has mpgrn = 2. Expecting to cause validation error that mpgrn should be the same
		final Method methodNew = this.createBreedingMethod("GEN", 1);
		final GermplasmUpdateRequest request = new GermplasmUpdateRequest(RandomStringUtils.randomAlphabetic(20), creationDate,
			methodNew.getMid().toString(), RandomStringUtils.randomAlphabetic(20), "UKN",
			RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20),
			RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(20));

		this.germplasmServiceBrapi.updateGermplasm(this.germplasmUUID, request);
		Assert.fail("Expected to throw exception that breeding method.mpgrn should be the same");
	}

	private Germplasm createGermplasm(final Method method, final String germplasmUUID, final Location location, final Integer gnpgs,
		final Integer gpid1, final Integer gpid2, final String germplasmPUI) {
		return this.createGermplasm(method, germplasmUUID, location, gnpgs, gpid1, gpid2, null, germplasmPUI);
	}

	private Germplasm createGermplasm(final Method method, final String germplasmUUID, final Location location, final Integer gnpgs,
		final Integer gpid1, final Integer gpid2, final Bibref reference, final String germplasmPUI) {
		final Germplasm germplasm = new Germplasm(null, method.getMid(), gnpgs, gpid1, gpid2,
			0, (location == null) ? 0 : location.getLocid(), Integer.parseInt(this.creationDate), 0,
			0, 0, null, null, method);
		if (StringUtils.isNotEmpty(germplasmUUID)) {
			germplasm.setGermplasmUUID(germplasmUUID);
		}
		germplasm.setBibref(reference);
		this.daoFactory.getGermplasmDao().save(germplasm);
		if (StringUtils.isNotEmpty(germplasmPUI)) {
			this.addName(germplasm, this.puiNameTypeId, germplasmPUI, this.noLocationId, this.creationDate, 0);
		}
		this.sessionProvder.getSession().flush();

		assertThat(germplasm.getCreatedBy(), is(this.userId));
		assertNotNull(germplasm.getCreatedBy());
		assertNull(germplasm.getModifiedBy());
		assertNull(germplasm.getModifiedDate());

		return germplasm;
	}

	private Method createBreedingMethod(final String breedingMethodType, final int numberOfProgenitors) {
		final Method method =
			new Method(null, breedingMethodType, "G", RandomStringUtils.randomAlphanumeric(4).toUpperCase(),
				RandomStringUtils.randomAlphanumeric(10),
				RandomStringUtils.randomAlphanumeric(10), 0, numberOfProgenitors, 1, 0, 1490, 1, 0, 19980708, "");
		this.daoFactory.getMethodDAO().save(method);
		this.sessionProvder.getSession().flush();
		this.daoFactory.getMethodDAO().refresh(method);
		return method;
	}

	private Location createLocation() {
		final Location location = new Location(null, 1,
			1, RandomStringUtils.randomAlphabetic(20), RandomStringUtils.randomAlphabetic(8),
			1, 1, 1,
			1, 1);
		location.setLdefault(false);
		this.daoFactory.getLocationDAO().saveOrUpdate(location);
		this.sessionProvder.getSession().flush();
		this.daoFactory.getLocationDAO().refresh(location);
		return location;
	}

	private Name addName(final Germplasm germplasm, final Integer nameId, final String nameVal, final Integer locId, final String date,
		final int preferred) {
		final Name name = new Name(null, germplasm, nameId, preferred, nameVal, locId, Integer.valueOf(date), 0);
		this.daoFactory.getNameDao().save(name);
		this.sessionProvder.getSession().flush();
		this.daoFactory.getNameDao().refresh(name);

		assertNotNull(name.getCreatedDate());
		assertThat(name.getCreatedBy(), is(this.userId));
		assertNull(name.getModifiedDate());
		assertNull(name.getModifiedBy());

		return name;
	}



}
