package org.generationcp.middleware.api.brapi;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.brapi.v1.germplasm.GermplasmDTO;
import org.generationcp.middleware.api.brapi.v2.germplasm.ExternalReferenceDTO;
import org.generationcp.middleware.api.brapi.v2.germplasm.GermplasmImportRequest;
import org.generationcp.middleware.api.brapi.v2.germplasm.GermplasmUpdateRequest;
import org.generationcp.middleware.api.brapi.v2.germplasm.Synonym;
import org.generationcp.middleware.api.germplasm.GermplasmGuidGenerator;
import org.generationcp.middleware.api.germplasm.GermplasmMethodValidator;
import org.generationcp.middleware.api.germplasm.GermplasmServiceImpl;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeService;
import org.generationcp.middleware.dao.AttributeDAO;
import org.generationcp.middleware.dao.GermplasmDAO;
import org.generationcp.middleware.dao.NameDAO;
import org.generationcp.middleware.domain.germplasm.PedigreeDTO;
import org.generationcp.middleware.domain.germplasm.ProgenyDTO;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.search_request.brapi.v1.GermplasmSearchRequestDto;
import org.generationcp.middleware.exceptions.MiddlewareRequestException;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.manager.DaoFactory;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.manager.ontology.daoElements.VariableFilter;
import org.generationcp.middleware.pojos.Attribute;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmExternalReference;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.Method;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.util.Util;
import org.generationcp.middleware.util.VariableValueUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
@Transactional
public class GermplasmServiceBrapiImpl implements GermplasmServiceBrapi {

	private static final String DEFAULT_METHOD = "UDM";

	@Autowired
	private GermplasmNameTypeService germplasmNameTypeService;

	@Autowired
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	private final DaoFactory daoFactory;

	private final GermplasmMethodValidator germplasmMethodValidator;

	private final HibernateSessionProvider sessionProvider;

	public GermplasmServiceBrapiImpl(final HibernateSessionProvider sessionProvider) {
		this.sessionProvider = sessionProvider;
		this.daoFactory = new DaoFactory(sessionProvider);
		this.germplasmMethodValidator = new GermplasmMethodValidator();
	}

	@Override
	public PedigreeDTO getPedigree(final Integer gid, final String notation, final Boolean includeSiblings) {
		return this.daoFactory.getGermplasmDao().getPedigree(gid, notation, includeSiblings);
	}

	@Override
	public ProgenyDTO getProgeny(final Integer gid) {
		return this.daoFactory.getGermplasmDao().getProgeny(gid);
	}

	@Override
	public List<GermplasmDTO> createGermplasm(final String cropname,
		final List<GermplasmImportRequest> germplasmImportRequestList) {
		final Map<String, Integer> locationsMap = this.getLocationsMapByLocAbbr(germplasmImportRequestList);
		final Map<String, Variable> attributesMap = this.getAttributesMapByVariableName(germplasmImportRequestList);
		final Map<String, Integer> nameTypesMap = this.getNameTypesMapByNameTypeCode(germplasmImportRequestList);
		final CropType cropType = this.workbenchDataManager.getCropTypeByName(cropname);

		//Set Unknown derivative method as default when no breeding method is specified
		Method unknownDerivativeMethod = null;
		if (germplasmImportRequestList.stream().anyMatch(g -> StringUtils.isEmpty(g.getBreedingMethodDbId()))) {
			final List<Method> unknownDerivativeMethods = this.daoFactory.getMethodDAO().getByCode(
				Collections.singletonList(DEFAULT_METHOD));
			if (unknownDerivativeMethods.isEmpty()) {
				throw new MiddlewareRequestException("", "brapi.import.germplasm.no.default.method.found");
			}
			unknownDerivativeMethod = unknownDerivativeMethods.get(0);
		}
		// Raise an error if any of the germplasmPUIs already exist
		this.enforcePUIUniqueness(germplasmImportRequestList);

		final List<String> createdGermplasmUUIDs = new ArrayList<>();
		for (final GermplasmImportRequest germplasmDto : germplasmImportRequestList) {

			final Germplasm germplasm = new Germplasm();
			final Integer methodId =
				(StringUtils.isNotEmpty(germplasmDto.getBreedingMethodDbId())) ? Integer.parseInt(germplasmDto.getBreedingMethodDbId()) :
					unknownDerivativeMethod.getMid();
			germplasm.setMethodId(methodId);

			germplasm.setGrplce(0);
			germplasm.setMgid(0);
			germplasm.setLgid(0);
			germplasm.setGnpgs(0);
			germplasm.setGpid1(0);
			germplasm.setGpid2(0);
			germplasm.setLocationId(locationsMap.get(germplasmDto.getCountryOfOriginCode()));

			germplasm.setDeleted(Boolean.FALSE);
			germplasm
				.setGdate(Util.convertDateToIntegerValue(Util.tryParseDate(germplasmDto.getAcquisitionDate(), Util.FRONTEND_DATE_FORMAT)));
			germplasm.setReferenceId(0);

			GermplasmGuidGenerator.generateGermplasmGuids(cropType, Collections.singletonList(germplasm));
			this.daoFactory.getGermplasmDao().saveOrUpdate(germplasm);

			this.addCustomNameFieldsToSynonyms(germplasmDto);
			germplasmDto.getSynonyms().forEach(synonym -> {
				final Integer typeId = nameTypesMap.get(synonym.getType().toUpperCase());
				if (typeId != null) {
					final Name name = new Name(null, germplasm, typeId,
						0, synonym.getSynonym(), germplasm.getLocationId(), Util.getCurrentDateAsIntegerValue(), 0);
					if (GermplasmImportRequest.LNAME_NAME_TYPE.equals(synonym.getType())) {
						name.setNstat(1);
					}
					this.daoFactory.getNameDao().save(name);
				}
			});

			if (germplasmDto.getExternalReferences() != null) {
				final List<GermplasmExternalReference> references = new ArrayList<>();
				germplasmDto.getExternalReferences().forEach(reference -> {
					final GermplasmExternalReference germplasmExternalReference =
						new GermplasmExternalReference(germplasm, reference.getReferenceID(), reference.getReferenceSource());
					references.add(germplasmExternalReference);
				});
				germplasm.setExternalReferences(references);
			}

			this.addCustomAttributeFieldsToAdditionalInfo(germplasmDto);
			germplasmDto.getAdditionalInfo().forEach((k, v) -> {
				final Variable variable = attributesMap.get(k.toUpperCase());
				if (variable != null) {
					final boolean isValidValue = VariableValueUtil.isValidAttributeValue(variable, v);
					if (isValidValue) {
						final Integer cValueId = VariableValueUtil.resolveCategoricalValueId(variable, v);
						final Attribute attribute = new Attribute(null, germplasm.getGid(), variable.getId(), v, cValueId,
							germplasm.getLocationId(),
							0, Util.getCurrentDateAsIntegerValue());
						this.daoFactory.getAttributeDAO().save(attribute);
					}
				}
			});

			createdGermplasmUUIDs.add(germplasm.getGermplasmUUID());
		}
		if (!createdGermplasmUUIDs.isEmpty()) {
			final GermplasmSearchRequestDto searchRequestDto = new GermplasmSearchRequestDto();
			searchRequestDto.setGermplasmDbIds(createdGermplasmUUIDs);
			return this.searchGermplasmDTO(searchRequestDto, null);
		}
		return Collections.emptyList();
	}

	@Override
	public GermplasmDTO updateGermplasm(final String germplasmDbId,
		final GermplasmUpdateRequest germplasmUpdateRequest) {
		final Multimap<String, Object[]> conflictErrors = ArrayListMultimap.create();
		final GermplasmDAO germplasmDao = this.daoFactory.getGermplasmDao();
		final List<Germplasm> germplasmByGUIDs =
			germplasmDao.getGermplasmByGUIDs(Collections.singletonList(germplasmDbId));
		if (CollectionUtils.isEmpty(germplasmByGUIDs)) {
			throw new MiddlewareRequestException("", "germplasm.invalid.guid");
		}
		// Validate that PUI, if specified, is not yet used
		final Germplasm germplasm = germplasmByGUIDs.get(0);
		final NameDAO nameDao = this.daoFactory.getNameDao();
		final Map<Integer, Name> existingNamesByType =
			nameDao.getNamesByGids(Collections.singletonList(germplasm.getGid())).stream()
				.collect(Collectors.toMap(Name::getTypeId,
					Function.identity()));
		final Map<String, Integer> nameTypesMap = this.getNameTypesMapByNameTypeCode(Collections.singletonList(germplasmUpdateRequest));
		this.enforcePUIUniqueness(germplasmUpdateRequest, existingNamesByType.get(nameTypesMap.get(GermplasmImportRequest.PUI_NAME_TYPE)));

		// Update breeding method if it is present
		if (!StringUtils.isEmpty(germplasmUpdateRequest.getBreedingMethodDbId())) {
			final Integer newBreedingMethodId = Integer.parseInt(germplasmUpdateRequest.getBreedingMethodDbId());
			final Integer oldBreedingMethodId = germplasm.getMethodId();
			if (!newBreedingMethodId.equals(oldBreedingMethodId)) {
				final Map<Integer, Method> methodMap =
					this.daoFactory.getMethodDAO().getMethodsByIds(Arrays.asList(oldBreedingMethodId, newBreedingMethodId)).stream()
						.collect(Collectors.toMap(Method::getMid, Function.identity()));
				final Method newBreedingMethod = methodMap.get(newBreedingMethodId);
				final Method oldBreedingMethod = methodMap.get(oldBreedingMethodId);
				if (this.germplasmMethodValidator
					.isNewBreedingMethodValid(oldBreedingMethod, newBreedingMethod, germplasmDbId, conflictErrors)) {
					germplasm.setMethodId(newBreedingMethodId);
				}
			}
		}
		// Update germplasm location if it is present
		if (!StringUtils.isEmpty(germplasmUpdateRequest.getCountryOfOriginCode())) {
			final List<Location> locationList =
				this.daoFactory.getLocationDAO()
					.getByAbbreviations(Collections.singletonList(germplasmUpdateRequest.getCountryOfOriginCode()));
			if (!CollectionUtils.isEmpty(locationList)) {
				germplasm.setLocationId(locationList.get(0).getLocid());
			}
		}
		if (!StringUtils.isEmpty(germplasmUpdateRequest.getAcquisitionDate())) {
			germplasm.setGdate(
				Util.convertDateToIntegerValue(Util.tryParseDate(germplasmUpdateRequest.getAcquisitionDate(), Util.FRONTEND_DATE_FORMAT)));
		}
		if (!conflictErrors.isEmpty()) {
			throw new MiddlewareRequestException(null, conflictErrors);
		}
		germplasmDao.update(germplasm);

		// Update germplasm names
		this.addCustomNameFieldsToSynonyms(germplasmUpdateRequest);
		germplasmUpdateRequest.getSynonyms().forEach(synonym -> {
			final Integer typeId = nameTypesMap.get(synonym.getType().toUpperCase());
			if (typeId != null) {
				// Create new name if none of that type exists, otherwise update name value of existing one
				if (existingNamesByType.containsKey(typeId)) {
					final Name existingName = existingNamesByType.get(typeId);
					existingName.setNval(synonym.getSynonym());
					nameDao.update(existingName);
				} else {
					final Name name = new Name(null, germplasm, typeId,
						0, synonym.getSynonym(), germplasm.getLocationId(), Util.getCurrentDateAsIntegerValue(), 0);
					if (GermplasmImportRequest.LNAME_NAME_TYPE.equals(synonym.getType())) {
						name.setNstat(1);
					}
					nameDao.save(name);
				}
			}
		});

		// Update germplasm attributes
		final AttributeDAO attributeDAO = this.daoFactory.getAttributeDAO();
		final Map<Integer, Attribute> existingAttributesByType = attributeDAO.getByGID(germplasm.getGid()).stream()
			.collect(Collectors.toMap(Attribute::getTypeId,
				Function.identity()));
		final Map<String, Variable> attributesMap = this.getAttributesMapByVariableName(Collections.singletonList(germplasmUpdateRequest));
		this.addCustomAttributeFieldsToAdditionalInfo(germplasmUpdateRequest);
		germplasmUpdateRequest.getAdditionalInfo().forEach((k, v) -> {
			final Variable variable = attributesMap.get(k.toUpperCase());
			if (variable != null) {
				final boolean isValidValue = VariableValueUtil.isValidAttributeValue(variable, v);
				if (isValidValue) {
					final Integer cValueId = VariableValueUtil.resolveCategoricalValueId(variable, v);
					// Create new attribute if none of that type exists, otherwise update value of existing one
					if (existingAttributesByType.containsKey(variable.getId())) {
						final Attribute existingAttribute = existingAttributesByType.get(variable.getId());
						existingAttribute.setAval(v);
						existingAttribute.setcValueId(cValueId);
						attributeDAO.update(existingAttribute);
					} else {
						final Attribute attribute = new Attribute(null, germplasm.getGid(), variable.getId(), v, cValueId,
							germplasm.getLocationId(),
							0, Util.getCurrentDateAsIntegerValue());
						attributeDAO.save(attribute);
					}
				}
			}
		});

		// Unless the session is flushed, the latest changes to germplasm,names and attributes are not reflected in object returned by method
		this.sessionProvider.getSession().flush();

		return this.getGermplasmDTOByGUID(germplasmDbId).get();
	}

	@Override
	public List<GermplasmDTO> searchGermplasmDTO(final GermplasmSearchRequestDto germplasmSearchRequestDTO, final Pageable pageable) {
		final List<GermplasmDTO> germplasmDTOList =
			this.daoFactory.getGermplasmDao().getGermplasmDTOList(germplasmSearchRequestDTO, pageable);
		this.populateExternalReferences(germplasmDTOList);
		this.populateSynonymsAndAttributes(germplasmDTOList);
		return germplasmDTOList;
	}

	@Override
	public long countGermplasmDTOs(final GermplasmSearchRequestDto germplasmSearchRequestDTO) {
		return this.daoFactory.getGermplasmDao().countGermplasmDTOs(germplasmSearchRequestDTO);
	}

	@Override
	public List<GermplasmDTO> getGermplasmByStudy(final Integer studyDbId, final Pageable pageable) {
		final List<GermplasmDTO> germplasmByStudy = this.daoFactory.getGermplasmDao().getGermplasmByStudy(studyDbId, pageable);
		this.populateSynonymsAndAttributes(germplasmByStudy);
		return germplasmByStudy;
	}

	@Override
	public long countGermplasmByStudy(final Integer studyDbId) {
		return this.daoFactory.getGermplasmDao().countGermplasmByStudy(studyDbId);
	}

	@Override
	public Optional<GermplasmDTO> getGermplasmDTOByGUID(final String germplasmUUID) {
		final GermplasmSearchRequestDto searchDto = new GermplasmSearchRequestDto();
		searchDto.setGermplasmDbIds(Collections.singletonList(germplasmUUID));
		final List<GermplasmDTO> germplasmDTOS = this.searchGermplasmDTO(searchDto, new PageRequest(0, 1));
		if (!CollectionUtils.isEmpty(germplasmDTOS)) {
			return Optional.of(germplasmDTOS.get(0));
		}
		return Optional.empty();
	}

	private void populateExternalReferences(final List<GermplasmDTO> germplasmDTOList) {
		final List<Integer> gids = germplasmDTOList.stream().map(g -> Integer.valueOf(g.getGid())).collect(Collectors.toList());
		if (!gids.isEmpty()) {
			final List<ExternalReferenceDTO> referenceDTOS =
				this.daoFactory.getGermplasmExternalReferenceDAO().getExternalReferencesByGids(gids);
			final Map<String, List<ExternalReferenceDTO>> referencesByGidMap = referenceDTOS.stream()
				.collect(groupingBy(ExternalReferenceDTO::getEntityId));
			for (final GermplasmDTO germplasmDTO : germplasmDTOList) {
				if (referencesByGidMap.containsKey(germplasmDTO.getGid())) {
					germplasmDTO.setExternalReferences(referencesByGidMap.get(germplasmDTO.getGid()));
				}
			}
		}
	}

	private void populateSynonymsAndAttributes(final List<GermplasmDTO> germplasmDTOList) {
		final List<Integer> gids = germplasmDTOList.stream().map(germplasmDTO -> Integer.valueOf(germplasmDTO.getGid()))
			.collect(Collectors.toList());
		final Map<Integer, String> nameTypesMap = this.germplasmNameTypeService.getNameTypesByGIDList(gids).stream()
			.collect(Collectors.toMap(GermplasmNameTypeDTO::getId, GermplasmNameTypeDTO::getCode));
		final Map<Integer, List<Name>> gidNamesMap =
			this.daoFactory.getNameDao().getNamesByGidsAndNTypeIdsInMap(new ArrayList<>(gids), Collections.emptyList());
		final Map<Integer, Map<String, String>> gidAttributesMap = this.getAttributesNameAndValuesMapForGids(new ArrayList<>(gids));
		// Populate synonyms and attributes per germplasm DTO
		for (final GermplasmDTO germplasmDTO : germplasmDTOList) {
			final Integer gid = Integer.valueOf(germplasmDTO.getGid());
			// Set as synonyms other names, other than the preferred name, found for germplasm
			final String defaultName = germplasmDTO.getGermplasmName();
			final List<Name> names = gidNamesMap.get(gid);
			if (!CollectionUtils.isEmpty(names)) {
				final List<Synonym> synonymsList = new ArrayList<>();
				final List<Name> synonyms =
					names.stream().filter(n -> !n.getNval().equalsIgnoreCase(defaultName)).collect(Collectors.toList());
				for (final Name name : synonyms) {
					synonymsList.add(new Synonym(name.getNval(), nameTypesMap.get(name.getTypeId())));
				}
				germplasmDTO.setSynonyms(synonymsList);
			}
			germplasmDTO.setAdditionalInfo(gidAttributesMap.get(gid));
		}
	}

	private Map<String, Integer> getLocationsMapByLocAbbr(final List<GermplasmImportRequest> germplasmImportRequestList) {
		final Set<String> locationAbbreviations =
			germplasmImportRequestList.stream().filter(g -> StringUtils.isNotEmpty(g.getCountryOfOriginCode()))
				.map(GermplasmImportRequest::getCountryOfOriginCode).collect(Collectors.toSet());
		return this.daoFactory.getLocationDAO().getByAbbreviations(new ArrayList<>(locationAbbreviations)).stream()
			.collect(Collectors.toMap(l -> l.getLabbr().toUpperCase(), Location::getLocid));
	}

	private Map<String, Variable> getAttributesMapByVariableName(final List<GermplasmImportRequest> germplasmImportRequestList) {
		final Set<String> attributes = new HashSet<>(GermplasmImportRequest.BRAPI_SPECIFIABLE_ATTRTYPES);
		germplasmImportRequestList.forEach(g -> {
			if (!CollectionUtils.isEmpty(g.getAdditionalInfo())) {
				attributes.addAll(g.getAdditionalInfo().keySet());
			}
		});
		return this.getAttributesMap(null, attributes);
	}

	private Map<String, Integer> getNameTypesMapByNameTypeCode(final List<GermplasmImportRequest> germplasmImportRequestList) {
		final Set<String> namesCode = new HashSet<>(GermplasmImportRequest.BRAPI_SPECIFIABLE_NAMETYPES);
		germplasmImportRequestList.forEach(
			g -> namesCode.addAll(g.getSynonyms().stream().map(s -> s.getType().toUpperCase()).collect(Collectors.toList())));
		return this.germplasmNameTypeService.filterGermplasmNameTypes(namesCode).stream().collect(Collectors.toMap(
			GermplasmNameTypeDTO::getCode, GermplasmNameTypeDTO::getId));
	}



	// Add to attributes map to be saved the custom attribute fields in import request dto
	private void addCustomAttributeFieldsToAdditionalInfo(final GermplasmImportRequest germplasmDto) {
		final Map<String, String> customAttributeFieldsMap = germplasmDto.getCustomAttributeFieldsMap();
		for (final String attributeKey : GermplasmImportRequest.BRAPI_SPECIFIABLE_ATTRTYPES) {
			final boolean isAttributeSpecified = germplasmDto.getAdditionalInfo().containsKey(attributeKey);
			if (!StringUtils.isEmpty(customAttributeFieldsMap.get(attributeKey)) && !isAttributeSpecified) {
				germplasmDto.getAdditionalInfo().put(attributeKey, customAttributeFieldsMap.get(attributeKey));
			}
		}
	}

	// Add to names list to be saved the custom name fields in import request dto
	private void addCustomNameFieldsToSynonyms(final GermplasmImportRequest germplasmDto) {
		final Map<String, String> customNamesFieldsMap = germplasmDto.getCustomNamesFieldsMap();
		for (final String nameType : GermplasmImportRequest.BRAPI_SPECIFIABLE_NAMETYPES) {
			final Optional<Synonym> synonymOptional =
				germplasmDto.getSynonyms().stream().filter(s -> nameType.equals(s.getType().toUpperCase())).findAny();
			if (!StringUtils.isEmpty(customNamesFieldsMap.get(nameType)) && !synonymOptional.isPresent()) {
				germplasmDto.getSynonyms().add(new Synonym(customNamesFieldsMap.get(nameType), nameType));
			}
		}

	}

	private void enforcePUIUniqueness(final List<GermplasmImportRequest> germplasmImportRequestList) {
		final List<String> puisList = new ArrayList<>();
		germplasmImportRequestList.forEach(i -> {
			if (!StringUtils.isEmpty(i.getGermplasmPUI())) {
				puisList.add(i.getGermplasmPUI());
			}
			puisList.addAll(i.getSynonyms().stream().filter(s-> GermplasmImportRequest.PUI_NAME_TYPE.equalsIgnoreCase(s.getType())).map(Synonym::getSynonym).collect(Collectors.toList()));
		});
		if (!CollectionUtils.isEmpty(puisList)) {
			final List<String> existingGermplasmPUIs = this.daoFactory.getNameDao().getExistingGermplasmPUIs(puisList);
			if (!CollectionUtils.isEmpty(existingGermplasmPUIs)) {
				throw new MiddlewareRequestException("", "brapi.import.germplasm.pui.exists", StringUtils.join(existingGermplasmPUIs, ","));
			}
		}
	}

	private void enforcePUIUniqueness(final GermplasmUpdateRequest germplasmUpdateRequest, final Name puiName) {
		final List<String> puisList = germplasmUpdateRequest.collectGermplasmPUIs();
		if (!CollectionUtils.isEmpty(puisList) ) {
			final List<String> existingGermplasmPUIs = this.daoFactory.getNameDao().getExistingGermplasmPUIs(puisList);
			if (!CollectionUtils.isEmpty(existingGermplasmPUIs) && (puiName == null || !puisList.get(0).equals(puiName.getNval()))) {
				throw new MiddlewareRequestException("", "brapi.update.germplasm.pui.exists", StringUtils.join(existingGermplasmPUIs, ","));
			}
		}
	}

	private Map<Integer, Map<String, String>> getAttributesNameAndValuesMapForGids(final List<Integer> gidList) {
		final Map<Integer, Map<String, String>> attributeMap = new HashMap<>();

		// retrieve attribute values
		final List<Attribute> attributeList = this.daoFactory.getAttributeDAO().getAttributeValuesGIDList(gidList);
		final Set<Integer> variableIds = attributeList.stream().map(Attribute::getTypeId).collect(Collectors.toSet());

		final VariableFilter variableFilter = new VariableFilter();
		variableIds.forEach(variableFilter::addVariableId);

		final List<Variable> variables = this.ontologyVariableDataManager.getWithFilter(variableFilter);
		final Map<Integer, Variable> variableMap = variables.stream().collect(Collectors.toMap(Variable::getId, Function.identity()));

		for (final Attribute attribute : attributeList) {
			Map<String, String> attrByType = attributeMap.get(attribute.getGermplasmId());
			if (attrByType == null) {
				attrByType = new HashMap<>();
			}
			final String attributeType = variableMap.get(attribute.getTypeId()).getName();
			attrByType.put(attributeType, attribute.getAval());
			attributeMap.put(attribute.getGermplasmId(), attrByType);
		}

		return attributeMap;
	}

	private Map<String, Variable> getAttributesMap(final String programUUID, final Set<String> variableNamesOrAlias) {
		if (!variableNamesOrAlias.isEmpty()) {
			final VariableFilter variableFilter = new VariableFilter();
			variableFilter.setProgramUuid(programUUID);
			GermplasmServiceImpl.ATTRIBUTE_TYPES.forEach(variableFilter::addVariableType);
			variableNamesOrAlias.forEach(variableFilter::addName);

			final List<Variable> existingAttributeVariables =
				this.ontologyVariableDataManager.getWithFilter(variableFilter);

			final Map<String, Variable> map = new HashMap<>();
			existingAttributeVariables.forEach(a -> {
				map.put(a.getName().toUpperCase(), a);
				if (StringUtils.isNotEmpty(a.getAlias())) {
					map.put(a.getAlias().toUpperCase(), a);
				}
			});
			return map;
		} else {
			return new HashMap<>();
		}
	}



}
