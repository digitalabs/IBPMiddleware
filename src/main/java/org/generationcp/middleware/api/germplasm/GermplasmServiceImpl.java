package org.generationcp.middleware.api.germplasm;

import com.google.common.base.Functions;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.brapi.v1.germplasm.GermplasmDTO;
import org.generationcp.middleware.api.brapi.v2.germplasm.ExternalReferenceDTO;
import org.generationcp.middleware.api.brapi.v2.germplasm.GermplasmImportRequest;
import org.generationcp.middleware.api.brapi.v2.germplasm.GermplasmUpdateRequest;
import org.generationcp.middleware.api.brapi.v2.germplasm.Synonym;
import org.generationcp.middleware.api.germplasmlist.GermplasmListService;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeService;
import org.generationcp.middleware.dao.AttributeDAO;
import org.generationcp.middleware.dao.GermplasmDAO;
import org.generationcp.middleware.dao.GermplasmListDataDAO;
import org.generationcp.middleware.dao.NameDAO;
import org.generationcp.middleware.dao.ims.LotDAO;
import org.generationcp.middleware.domain.germplasm.GermplasmBasicDetailsDto;
import org.generationcp.middleware.domain.germplasm.GermplasmDto;
import org.generationcp.middleware.domain.germplasm.GermplasmNameDto;
import org.generationcp.middleware.domain.germplasm.GermplasmUpdateDTO;
import org.generationcp.middleware.domain.germplasm.PedigreeDTO;
import org.generationcp.middleware.domain.germplasm.ProgenitorsDetailsDto;
import org.generationcp.middleware.domain.germplasm.ProgenitorsUpdateRequestDto;
import org.generationcp.middleware.domain.germplasm.ProgenyDTO;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmImportDTO;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmImportRequestDto;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmImportResponseDto;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmMatchRequestDto;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.domain.search_request.brapi.v1.GermplasmSearchRequestDto;
import org.generationcp.middleware.exceptions.MiddlewareRequestException;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.manager.DaoFactory;
import org.generationcp.middleware.manager.api.OntologyDataManager;
import org.generationcp.middleware.manager.api.WorkbenchDataManager;
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
import org.generationcp.middleware.pojos.Progenitor;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.generationcp.middleware.service.api.user.UserService;
import org.generationcp.middleware.util.Util;
import org.generationcp.middleware.util.VariableValueUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@Service
@Transactional
public class GermplasmServiceImpl implements GermplasmService {

	@Value("${germplasm.edition.max.recursion}")
	public int maxRecursiveQueries;


	// This enum is used to define the required action given a germplasm pedigree change
	private enum UpdateGroupSourceAction {
		NONE,
		//Update the old group source by the new group source using a simple query
		DIRECT,
		//A recursive function to get all the derivative germplasm to be changed is required
		RECURSIVE
	}

	public static final String PLOT_CODE = "PLOTCODE_AP_text";

	private static final String DEFAULT_BIBREF_FIELD = "-";
	public static final String PROGENITOR_1 = "PROGENITOR 1";
	public static final String PROGENITOR_2 = "PROGENITOR 2";
	private static final String DEFAULT_METHOD = "UDM";
	public static final List<VariableType> ATTRIBUTE_TYPES =
		Arrays.asList(VariableType.GERMPLASM_ATTRIBUTE, VariableType.GERMPLASM_PASSPORT);

	private final DaoFactory daoFactory;

	@Autowired
	private WorkbenchDataManager workbenchDataManager;

	@Autowired
	private GermplasmListService germplasmListService;

	@Autowired
	private OntologyDataManager ontologyDataManager;

	@Autowired
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Autowired
	private UserService userService;

	@Autowired
	private GermplasmAttributeService germplasmAttributeService;

	@Autowired
	private GermplasmNameTypeService germplasmNameTypeService;

	private final GermplasmMethodValidator germplasmMethodValidator;

	private final HibernateSessionProvider sessionProvider;

	public GermplasmServiceImpl(final HibernateSessionProvider sessionProvider) {
		this.sessionProvider = sessionProvider;
		this.daoFactory = new DaoFactory(sessionProvider);
		this.germplasmMethodValidator = new GermplasmMethodValidator();
	}

	@Override
	public List<Germplasm> getGermplasmByGUIDs(final List<String> guids) {
		return this.daoFactory.getGermplasmDao().getGermplasmByGUIDs(guids);
	}

	@Override
	public List<Germplasm> getGermplasmByGIDs(final List<Integer> gids) {
		return this.daoFactory.getGermplasmDao().getByGIDList(gids);
	}

	@Override
	public String getPlotCodeValue(final Integer gid) {
		final Term plotCodeVariable = this.getPlotCodeField();
		final Optional<Attribute> plotCode = germplasmAttributeService.getAttributesByGID(gid)
			.stream()
			.filter(attribute -> attribute.getTypeId().equals(plotCodeVariable.getId()))
			.findFirst();
		if (plotCode.isPresent()) {
			return plotCode.get().getAval();
		}
		return GermplasmListDataDAO.SOURCE_UNKNOWN;
	}

	@Override
	public Map<Integer, String> getPlotCodeValues(final Set<Integer> gids) {
		final Term plotCodeVariable = this.getPlotCodeField();
		final Map<Integer, String> plotCodeValuesByGids =
			this.daoFactory.getAttributeDAO().getAttributeValuesByTypeAndGIDList(plotCodeVariable.getId(), new ArrayList<>(gids))
				.stream()
				.collect(Collectors.toMap(Attribute::getGermplasmId, Attribute::getAval));

		final Map<Integer, String> plotCodesIndexedByGIDs = new HashMap<>();
		gids.forEach(gid -> {
			final String plotCodeValue = plotCodeValuesByGids.get(gid);
			plotCodesIndexedByGIDs.put(gid, Objects.isNull(plotCodeValue) ? GermplasmListDataDAO.SOURCE_UNKNOWN : plotCodeValue);
		});
		return plotCodesIndexedByGIDs;
	}

	@Override
	public Term getPlotCodeField() {
		return this.ontologyDataManager.findTermByName(PLOT_CODE, CvId.VARIABLES.getId());
	}

	@Override
	public Map<Integer, GermplasmImportResponseDto> importGermplasm(final String cropName, final String programUUID,
		final GermplasmImportRequestDto germplasmImportRequestDto) {
		final Map<Integer, GermplasmImportResponseDto> results = new HashMap<>();
		final List<GermplasmImportDTO> germplasmDtoList = germplasmImportRequestDto.getGermplasmList();
		final Map<String, Method> methodsMapByAbbr = this.getBreedingMethodsMapByAbbr(germplasmDtoList);
		final Map<String, Integer> locationsMapByAbbr = this.getLocationsMapByAbbr(germplasmDtoList);
		final Set<String> attributesKeys = new HashSet<>();
		germplasmDtoList.forEach(g -> {
			if (g.getAttributes() != null && !g.getAttributes().isEmpty()) {
				attributesKeys.addAll(g.getAttributes().keySet());
			}
		});
		final Map<String, Variable> attributesMapByName = this.getAttributesMap(programUUID, attributesKeys);
		final Map<String, Integer> nameTypesMapByName = this.getNameTypesMapByName(germplasmDtoList);
		final CropType cropType = this.workbenchDataManager.getCropTypeByName(cropName);

		final Map<String, Germplasm> progenitorsMap = this.loadProgenitors(germplasmImportRequestDto);
		final List<GermplasmDto> germplasmMatches = this.loadGermplasmMatches(germplasmImportRequestDto);
		final Map<String, List<Integer>> gidMatchByUUID =
			germplasmMatches.stream().collect(Collectors.toMap(GermplasmDto::getGermplasmUUID, g -> Collections.singletonList(g.getGid())));
		final Map<String, List<Integer>> gidsMatchesByName = new HashMap<>();
		germplasmMatches.forEach(g ->
			g.getNames().forEach(n -> {
				if (gidsMatchesByName.containsKey(n.getName())) {
					gidsMatchesByName.get(n.getName()).add(g.getGid());
				} else {
					gidsMatchesByName.put(n.getName(), Lists.newArrayList(g.getGid()));
				}
			})
		);

		for (final GermplasmImportDTO germplasmDto : germplasmDtoList) {

			if (germplasmImportRequestDto.isSkipIfExists()) {
				if (gidMatchByUUID.containsKey(germplasmDto.getGermplasmUUID())) {
					results.put(germplasmDto.getClientId(),
						new GermplasmImportResponseDto(GermplasmImportResponseDto.Status.FOUND,
							gidMatchByUUID.get(germplasmDto.getGermplasmUUID())));
					continue;
				}
				final Set<Integer> gidSet = new HashSet<>();
				germplasmDto.getNames().values().forEach(n -> {
					if (gidsMatchesByName.containsKey(n)) {
						gidSet.addAll(gidsMatchesByName.get(n));
					}
				});
				if (!gidSet.isEmpty()) {
					results.put(germplasmDto.getClientId(),
						new GermplasmImportResponseDto(GermplasmImportResponseDto.Status.FOUND, new ArrayList<>(gidSet)));
					continue;
				}
			}

			final Germplasm germplasm = new Germplasm();

			final Method method = methodsMapByAbbr.get(germplasmDto.getBreedingMethodAbbr().toUpperCase());
			germplasm.setMethodId(method.getMid());

			germplasm.setGnpgs(this.calculateGnpgs(method, germplasmDto.getProgenitor1(), germplasmDto.getProgenitor2(), null));
			final Multimap<String, Object[]> progenitorsErrors = ArrayListMultimap.create();
			this.setProgenitors(germplasm, method, germplasmDto.getProgenitor1(), germplasmDto.getProgenitor2(), progenitorsMap,
				progenitorsErrors);
			if (!progenitorsErrors.isEmpty()) {
				final Map.Entry<String, Object[]> error = progenitorsErrors.entries().iterator().next();
				throw new MiddlewareRequestException("", error.getKey(), error.getValue());
			}
			germplasm.setGrplce(0);
			germplasm.setMgid(0);
			germplasm.setLgid(0);
			germplasm.setLocationId(locationsMapByAbbr.get(germplasmDto.getLocationAbbr().toUpperCase()));
			germplasm.setDeleted(Boolean.FALSE);
			germplasm.setGdate(Integer.valueOf(germplasmDto.getCreationDate()));

			if (StringUtils.isEmpty(germplasmDto.getGermplasmUUID())) {
				GermplasmGuidGenerator.generateGermplasmGuids(cropType, Collections.singletonList(germplasm));
			} else {
				germplasm.setGermplasmUUID(germplasmDto.getGermplasmUUID());
			}

			if (!StringUtils.isEmpty(germplasmDto.getReference())) {
				final Bibref bibref =
					new Bibref(null, DEFAULT_BIBREF_FIELD, DEFAULT_BIBREF_FIELD, germplasmDto.getReference(), DEFAULT_BIBREF_FIELD,
						DEFAULT_BIBREF_FIELD, DEFAULT_BIBREF_FIELD,
						DEFAULT_BIBREF_FIELD,
						DEFAULT_BIBREF_FIELD, DEFAULT_BIBREF_FIELD, DEFAULT_BIBREF_FIELD, DEFAULT_BIBREF_FIELD);
				this.daoFactory.getBibrefDAO().save(bibref);
				germplasm.setReferenceId(bibref.getRefid());
			} else {
				germplasm.setReferenceId(0);
			}

			this.daoFactory.getGermplasmDao().save(germplasm);

			germplasmDto.getNames().forEach((k, v) -> {
				final Name name = new Name(null, germplasm, nameTypesMapByName.get(k.toUpperCase()),
					(k.equalsIgnoreCase(germplasmDto.getPreferredName())) ? 1 : 0, v, germplasm.getLocationId(),
					Util.getCurrentDateAsIntegerValue(), 0);
				this.daoFactory.getNameDao().save(name);
			});

			if (germplasmDto.getAttributes() != null) {
				germplasmDto.getAttributes().forEach((k, v) -> {
					final Variable variable = attributesMapByName.get(k.toUpperCase());
					final boolean isValidValue = VariableValueUtil.isValidAttributeValue(variable, v);
					if (isValidValue) {
						final Integer cValueId = VariableValueUtil.resolveCategoricalValueId(variable, v);
						final Attribute attribute =
							new Attribute(null, germplasm.getGid(), variable.getId(), v, cValueId,
								germplasm.getLocationId(),
								0, Util.getCurrentDateAsIntegerValue());
						this.daoFactory.getAttributeDAO().save(attribute);
					}
				});
			}
			results.put(germplasmDto.getClientId(),
				new GermplasmImportResponseDto(GermplasmImportResponseDto.Status.CREATED, Collections.singletonList(germplasm.getGid())));
		}

		return results;
	}

	@Override
	public long countGermplasmMatches(final GermplasmMatchRequestDto germplasmMatchRequestDto) {
		return this.daoFactory.getGermplasmDao().countGermplasmMatches(germplasmMatchRequestDto);
	}

	@Override
	public List<GermplasmDto> findGermplasmMatches(final GermplasmMatchRequestDto germplasmMatchRequestDto, final Pageable pageable) {
		final List<GermplasmDto> germplasmDtos = this.daoFactory.getGermplasmDao().findGermplasmMatches(germplasmMatchRequestDto, pageable);

		if (!germplasmDtos.isEmpty()) {
			final List<Integer> gids = germplasmDtos.stream().map(GermplasmDto::getGid).collect(Collectors.toList());
			final List<GermplasmNameDto> names = this.daoFactory.getNameDao().getGermplasmNamesByGids(gids);

			final Map<Integer, List<GermplasmNameDto>> namesByGid = names.stream().collect(
				groupingBy(GermplasmNameDto::getGid, HashMap::new, Collectors.toCollection(ArrayList::new))
			);
			germplasmDtos.forEach(g -> g.setNames(namesByGid.get(g.getGid())));
		}

		return germplasmDtos;
	}

	@Override
	public Set<Integer> importGermplasmUpdates(final String programUUID, final List<GermplasmUpdateDTO> germplasmUpdateDTOList) {

		final Multimap<String, Object[]> conflictErrors = ArrayListMultimap.create();

		final List<Germplasm> germplasmList = this.getGermplasmListByGIDorGermplasmUUID(germplasmUpdateDTOList);
		final Map<String, Integer> nameCodesFieldNoMap = this.getNameTypesMapByCodes(germplasmUpdateDTOList);
		final Set<String> attributeKeys = new HashSet<>();
		germplasmUpdateDTOList.forEach(
			g -> attributeKeys.addAll(g.getAttributes().keySet().stream().map(String::toUpperCase).collect(Collectors.toList())));
		final Map<String, Variable> attributeVariablesNameMap = this.getAttributesMap(programUUID, attributeKeys);
		final Map<String, Germplasm> progenitorsMapByGid = this.getGermplasmProgenitorsMapByGids(germplasmUpdateDTOList);

		final Map<String, GermplasmUpdateDTO> germplasmUpdateDTOMap = new HashMap<>();
		for (final GermplasmUpdateDTO germplasmUpdateDTO : germplasmUpdateDTOList) {
			germplasmUpdateDTOMap
				.put(StringUtils.isNotEmpty(germplasmUpdateDTO.getGermplasmUUID()) ? germplasmUpdateDTO.getGermplasmUUID() :
						String.valueOf(germplasmUpdateDTO.getGid()),
					germplasmUpdateDTO);
		}

		// Retrieve location and method IDs in one go
		final Map<String, Integer> locationAbbreviationIdMap = this.getLocationAbbreviationIdMap(germplasmUpdateDTOList);
		final Map<String, Method> codeBreedingMethodDTOMap =
			this.getCodeBreedingMethodDTOMap(germplasmUpdateDTOList);

		// Retrieve the names and attributes associated to GIDs in one go.
		final List<Integer> gids = germplasmList.stream().map(Germplasm::getGid).collect(Collectors.toList());
		final Map<Integer, List<Name>> namesMap =
			this.daoFactory.getNameDao().getNamesByGidsInMap(gids);
		final List<Attribute> attributes =
			this.daoFactory.getAttributeDAO()
				.getAttributeValuesGIDList(gids);
		final Map<Integer, List<Attribute>> attributesMap =
			attributes.stream().collect(groupingBy(Attribute::getGermplasmId, LinkedHashMap::new, Collectors.toList()));

		for (final Germplasm germplasm : germplasmList) {
			this.saveGermplasmUpdateDTO(attributeVariablesNameMap, nameCodesFieldNoMap,
				germplasmUpdateDTOMap,
				locationAbbreviationIdMap, codeBreedingMethodDTOMap, namesMap, attributesMap, germplasm,
				progenitorsMapByGid, conflictErrors);
		}

		if (!conflictErrors.isEmpty()) {
			throw new MiddlewareRequestException(null, conflictErrors);
		}
		return germplasmList.stream().map(Germplasm::getGid).collect(Collectors.toSet());

	}

	@Override
	public void deleteGermplasm(final List<Integer> gids) {
		this.germplasmListService.performGermplasmListEntriesDeletion(gids);
		this.daoFactory.getGermplasmDao().deleteGermplasm(gids);
	}

	@Override
	public Set<Integer> getCodeFixedGidsByGidList(final List<Integer> gids) {
		final List<Germplasm> germplasmList = this.daoFactory.getGermplasmDao().getByGIDList(gids);
		return germplasmList.stream().filter(germplasm -> germplasm.getMgid() > 0).map(Germplasm::getGid).collect(Collectors.toSet());
	}

	@Override
	public Set<Integer> getGidsWithOpenLots(final List<Integer> gids) {
		final LotDAO dao = this.daoFactory.getLotDao();
		return dao.getGermplasmsWithOpenLots(gids);
	}

	@Override
	public Set<Integer> getGidsOfGermplasmWithDescendants(final List<Integer> gids) {
		return this.daoFactory.getGermplasmDao().getGidsOfGermplasmWithDescendants(Sets.newHashSet(gids));
	}

	@Override
	public Set<Integer> getGermplasmUsedInLockedList(final List<Integer> gids) {
		return new HashSet<>(this.daoFactory.getGermplasmListDAO().getGermplasmUsedInLockedList(gids));
	}

	@Override
	public Set<Integer> getGermplasmUsedInStudies(final List<Integer> gids) {
		return new HashSet<>(this.daoFactory.getStockDao().getGermplasmUsedInStudies(gids));
	}

	@Override
	public PedigreeDTO getPedigree(final Integer gid, final String notation, final Boolean includeSiblings) {
		return this.daoFactory.getGermplasmDao().getPedigree(gid, notation, includeSiblings);
	}

	@Override
	public ProgenyDTO getProgeny(final Integer gid) {
		return this.daoFactory.getGermplasmDao().getProgeny(gid);
	}

	private void saveGermplasmUpdateDTO(final Map<String, Variable> attributeVariablesMap,
		final Map<String, Integer> nameCodes,
		final Map<String, GermplasmUpdateDTO> germplasmUpdateDTOMap, final Map<String, Integer> locationAbbreviationIdMap,
		final Map<String, Method> codeBreedingMethodDTOMap, final Map<Integer, List<Name>> namesMap,
		final Map<Integer, List<Attribute>> attributesMap, final Germplasm germplasm,
		final Map<String, Germplasm> progenitorsMapByGid,
		final Multimap<String, Object[]> conflictErrors) {
		final Optional<GermplasmUpdateDTO> optionalGermplasmUpdateDTO =
			this.getGermplasmUpdateDTOByGidOrUUID(germplasm, germplasmUpdateDTOMap);
		if (optionalGermplasmUpdateDTO.isPresent()) {
			final GermplasmUpdateDTO germplasmUpdateDTO = optionalGermplasmUpdateDTO.get();
			this.updateGermplasm(germplasm, germplasmUpdateDTO, locationAbbreviationIdMap, codeBreedingMethodDTOMap, progenitorsMapByGid,
				conflictErrors);
			this.saveAttributesAndNames(attributeVariablesMap, nameCodes, namesMap, attributesMap, germplasm,
				conflictErrors,
				germplasmUpdateDTO);
			this.updatePreferredName(nameCodes, namesMap, germplasm, germplasmUpdateDTO, conflictErrors);
		}
	}

	private void saveAttributesAndNames(final Map<String, Variable> attributeVariablesMap,
		final Map<String, Integer> nameCodes, final Map<Integer, List<Name>> namesMap, final Map<Integer, List<Attribute>> attributesMap,
		final Germplasm germplasm, final Multimap<String, Object[]> conflictErrors, final GermplasmUpdateDTO germplasmUpdateDTO) {
		for (final Map.Entry<String, String> codeValuesEntry : germplasmUpdateDTO.getNames().entrySet()) {
			final String code = codeValuesEntry.getKey();
			final String value = codeValuesEntry.getValue();
			this.saveOrUpdateName(nameCodes, namesMap, germplasm, code, value,
				conflictErrors);
		}
		for (final Map.Entry<String, String> codeValuesEntry : germplasmUpdateDTO.getAttributes().entrySet()) {
			final String variableNameOrAlias = codeValuesEntry.getKey().toUpperCase();
			final String value = codeValuesEntry.getValue();
			this.saveOrUpdateAttribute(attributeVariablesMap, attributesMap, germplasm,
				variableNameOrAlias, value, conflictErrors);
		}

	}

	private void updatePreferredName(final Map<String, Integer> nameCodes, final Map<Integer, List<Name>> namesMap,
		final Germplasm germplasm, final GermplasmUpdateDTO germplasmUpdateDTO, final Multimap<String, Object[]> conflictErrors) {
		// Update preferred name
		final Integer preferredNameTypeId = nameCodes.get(
			StringUtils.isNotEmpty(germplasmUpdateDTO.getPreferredNameType()) ? germplasmUpdateDTO.getPreferredNameType().toUpperCase() :
				StringUtils.EMPTY);
		final List<Name> names = namesMap.getOrDefault(germplasm.getGid(), new ArrayList<>());
		final List<Name> preferredNames =
			names.stream().filter(n -> n.getTypeId().equals(preferredNameTypeId)).collect(Collectors.toList());

		if (preferredNames.size() == 1) {
			for (final Name name : names) {
				if (preferredNameTypeId != null) {
					name.setNstat(name.getTypeId().equals(preferredNameTypeId) ? 1 : 0);
					this.daoFactory.getNameDao().save(name);
				}
			}
		} else if (preferredNames.size() > 1) {
			conflictErrors.put("germplasm.update.preferred.name.duplicate.names", new String[] {
				germplasmUpdateDTO.getPreferredNameType(),
				String.valueOf(germplasm.getGid())});
		} else if (!liquibase.util.StringUtils.isEmpty(germplasmUpdateDTO.getPreferredNameType())) {
			conflictErrors.put("germplasm.update.preferred.name.doesnt.exist", new String[] {
				germplasmUpdateDTO.getPreferredNameType(),
				String.valueOf(germplasm.getGid())});
		}
	}

	private void updateGermplasm(final Germplasm germplasm, final GermplasmUpdateDTO germplasmUpdateDTO,
		final Map<String, Integer> locationAbbreviationIdMap,
		final Map<String, Method> codeBreedingMethodDTOMap,
		final Map<String, Germplasm> progenitorsMapByGid,
		final Multimap<String, Object[]> conflictErrors) {

		final Optional<Method> breedingMethodOptional =
			Optional.ofNullable(codeBreedingMethodDTOMap.getOrDefault(germplasmUpdateDTO.getBreedingMethodAbbr(), null));
		final Optional<Integer> locationIdOptional =
			Optional.ofNullable(locationAbbreviationIdMap.getOrDefault(
				StringUtils.isNotEmpty(germplasmUpdateDTO.getLocationAbbreviation()) ?
					germplasmUpdateDTO.getLocationAbbreviation().toUpperCase() : StringUtils.EMPTY, null));
		final Optional<Integer> germplasmDateOptional =
			StringUtils.isEmpty(germplasmUpdateDTO.getCreationDate()) ? Optional.empty() :
				Optional.of(Integer.parseInt(germplasmUpdateDTO.getCreationDate()));
		final Optional<String> referenceOptional = Optional.ofNullable(germplasmUpdateDTO.getReference());

		locationIdOptional.ifPresent(germplasm::setLocationId);
		germplasmDateOptional.ifPresent(germplasm::setGdate);

		this.saveOrUpdateReference(germplasm, referenceOptional);
		this.updateBreedingMethodAndProgenitors(germplasmUpdateDTO, germplasm, breedingMethodOptional, progenitorsMapByGid,
			conflictErrors);

		this.daoFactory.getGermplasmDao().update(germplasm);
	}

	private void updateBreedingMethodAndProgenitors(final GermplasmUpdateDTO germplasmUpdateDTO, final Germplasm germplasm,
		final Optional<Method> breedingMethodOptional,
		final Map<String, Germplasm> progenitorsMapByGid,
		final Multimap<String, Object[]> conflictErrors) {

		final Integer femaleParentGid = germplasmUpdateDTO.getProgenitors().get(PROGENITOR_1);
		final Integer maleParentGid = germplasmUpdateDTO.getProgenitors().get(PROGENITOR_2);
		final List<Integer> otherProgenitors = germplasmUpdateDTO.getProgenitors().entrySet().stream()
			.filter(entry -> !entry.getKey().equals(PROGENITOR_1) && !entry.getKey().equals(PROGENITOR_2))
			.map(Map.Entry::getValue).collect(
				Collectors.toList());

		if (!breedingMethodOptional.isPresent()) {
			// If breeding method is not specified, update the progenitors based on existing method
			this.updateProgenitors(germplasm, progenitorsMapByGid, conflictErrors, femaleParentGid,
				maleParentGid,
				germplasm.getMethod(), otherProgenitors);

		} else if (this.germplasmMethodValidator
			.isNewBreedingMethodValid(germplasm.getMethod(), breedingMethodOptional.get(), String.valueOf(germplasm.getGid()),
				conflictErrors)) {

			final Method breedingMethod = breedingMethodOptional.get();

			// Only update the method if the new method has the same type as the old method.
			germplasm.setMethodId(breedingMethod.getMid());

			// Update the progenitors based on the new method
			this.updateProgenitors(germplasm, progenitorsMapByGid, conflictErrors, femaleParentGid,
				maleParentGid,
				breedingMethod, otherProgenitors);
		}

	}

	private void updateProgenitors(final Germplasm germplasm, final Map<String, Germplasm> progenitorsMapByGid,
		final Multimap<String, Object[]> conflictErrors, final Integer femaleParentGid,
		final Integer maleParentGid, final Method breedingMethod, final List<Integer> otherProgenitors) {
		if (breedingMethod.getMprgn() == 1) {
			conflictErrors.put("germplasm.update.mutation.method.is.not.supported", new String[] {
				String.valueOf(germplasm.getGid())});
		} else if (femaleParentGid != null && maleParentGid != null) {
			// Only update the progenitors if both male and female are available.
			final String femaleParentGidString = String.valueOf(femaleParentGid);
			final String maleParentGidString = String.valueOf(maleParentGid);
			germplasm.setGnpgs(
				this.calculateGnpgs(breedingMethod, femaleParentGidString, maleParentGidString, Lists.transform(otherProgenitors, Functions
					.toStringFunction())));
			this.setProgenitors(germplasm, breedingMethod, femaleParentGidString, maleParentGidString, progenitorsMapByGid, conflictErrors);
			this.setOtherProgenitors(germplasm, breedingMethod, otherProgenitors, conflictErrors);
		}
	}

	private void saveOrUpdateReference(final Germplasm germplasm, final Optional<String> referenceOptional) {
		if (referenceOptional.isPresent() && !referenceOptional.get().isEmpty()) {
			if (germplasm.getBibref() != null) {
				final Bibref bibref = germplasm.getBibref();
				bibref.setAnalyt(referenceOptional.get());
				this.daoFactory.getBibrefDAO().save(bibref);
			} else {
				final Bibref bibref =
					new Bibref(null, DEFAULT_BIBREF_FIELD, DEFAULT_BIBREF_FIELD, referenceOptional.get(), DEFAULT_BIBREF_FIELD,
						DEFAULT_BIBREF_FIELD, DEFAULT_BIBREF_FIELD,
						DEFAULT_BIBREF_FIELD,
						DEFAULT_BIBREF_FIELD, DEFAULT_BIBREF_FIELD, DEFAULT_BIBREF_FIELD, DEFAULT_BIBREF_FIELD);
				this.daoFactory.getBibrefDAO().save(bibref);
				germplasm.setReferenceId(bibref.getRefid());
			}

		}
	}

	private void saveOrUpdateName(final Map<String, Integer> nameCodes,
		final Map<Integer, List<Name>> namesMap, final Germplasm germplasm,
		final String code, final String value, final Multimap<String, Object[]> conflictErrors) {

		// Check first if the code to save is a valid Name
		if (nameCodes.containsKey(code) && liquibase.util.StringUtils.isNotEmpty(value)) {
			final Integer nameTypeId = nameCodes.get(code);
			final List<Name> germplasmNames = namesMap.getOrDefault(germplasm.getGid(), new ArrayList<>());
			final List<Name> namesByType =
				germplasmNames.stream().filter(n -> n.getTypeId().equals(nameTypeId)).collect(Collectors.toList());

			// Check if there are multiple names with same type
			if (namesByType.size() > 1) {
				conflictErrors.put("germplasm.update.duplicate.names", new String[] {
					code, String.valueOf(germplasm.getGid())});
			} else if (namesByType.size() == 1) {
				// Update if name is existing
				final Name name = namesByType.get(0);
				name.setLocationId(germplasm.getLocationId());
				name.setNdate(germplasm.getGdate());
				name.setNval(value);
				this.daoFactory.getNameDao().update(name);
			} else {
				// Create new record if name not yet exists
				final Name name = new Name(null, germplasm, nameTypeId, 0,
					value, germplasm.getLocationId(), germplasm.getGdate(), 0);
				this.daoFactory.getNameDao().save(name);
				germplasmNames.add(name);
				namesMap.putIfAbsent(germplasm.getGid(), germplasmNames);
			}
		}
	}

	private void saveOrUpdateAttribute(final Map<String, Variable> attributeVariables,
		final Map<Integer, List<Attribute>> attributesMap, final Germplasm germplasm,
		final String variableNameOrAlias, final String value, final Multimap<String, Object[]> conflictErrors) {
		// Check first if the code to save is a valid Attribute
		if (attributeVariables.containsKey(variableNameOrAlias) && StringUtils.isNotEmpty(value)) {
			final Variable variable = attributeVariables.get(variableNameOrAlias);
			final List<Attribute> germplasmAttributes = attributesMap.getOrDefault(germplasm.getGid(), new ArrayList<>());
			final List<Attribute> attributesByType =
				germplasmAttributes.stream().filter(n -> n.getTypeId().equals(variable.getId())).collect(Collectors.toList());

			// Check if there are multiple attributes with same type
			if (attributesByType.size() > 1) {
				conflictErrors.put("germplasm.update.duplicate.attributes", new String[] {
					variableNameOrAlias, String.valueOf(germplasm.getGid())});
			} else {
				final boolean isValidValue = VariableValueUtil.isValidAttributeValue(variable, value);
				if (isValidValue) {
					final Integer cValueId = VariableValueUtil.resolveCategoricalValueId(variable, value);
					if (attributesByType.size() == 1) {
						final Attribute attribute = attributesByType.get(0);
						attribute.setLocationId(germplasm.getLocationId());
						attribute.setAdate(germplasm.getGdate());
						attribute.setAval(value);
						attribute.setcValueId(cValueId);
						this.daoFactory.getAttributeDAO().update(attribute);
					} else {
						this.daoFactory.getAttributeDAO()
							.save(new Attribute(null, germplasm.getGid(), variable.getId(), value, cValueId,
								germplasm.getLocationId(),
								0, germplasm.getGdate()));
					}
				}
			}
		}
	}

	private Optional<GermplasmUpdateDTO> getGermplasmUpdateDTOByGidOrUUID(final Germplasm germplasm,
		final Map<String, GermplasmUpdateDTO> germplasmUpdateDTOMap) {
		if (germplasmUpdateDTOMap.containsKey(String.valueOf(germplasm.getGid()))) {
			return Optional.of(germplasmUpdateDTOMap.get(String.valueOf(germplasm.getGid())));
		} else if (germplasmUpdateDTOMap.containsKey(germplasm.getGermplasmUUID())) {
			return Optional.of(germplasmUpdateDTOMap.get(germplasm.getGermplasmUUID()));
		}
		return Optional.empty();
	}

	private Map<String, Integer> getLocationAbbreviationIdMap(final List<GermplasmUpdateDTO> germplasmUpdateDTOList) {
		final Set<String> locationAbbrs =
			germplasmUpdateDTOList.stream().map(GermplasmUpdateDTO::getLocationAbbreviation).collect(Collectors.toSet());
		return
			this.daoFactory.getLocationDAO().getByAbbreviations(new ArrayList<>(locationAbbrs)).stream()
				.collect(Collectors.toMap(Location::getLabbr, Location::getLocid));
	}

	private Map<String, Method> getCodeBreedingMethodDTOMap(final List<GermplasmUpdateDTO> germplasmUpdateDTOList) {
		final Set<String> breedingMethodsAbbrs =
			germplasmUpdateDTOList.stream().map(GermplasmUpdateDTO::getBreedingMethodAbbr).collect(Collectors.toSet());
		return this.daoFactory.getMethodDAO().getByCode(new ArrayList<>(breedingMethodsAbbrs)).stream()
			.collect(Collectors.toMap(Method::getMcode, Function.identity()));
	}

	private Map<String, Integer> getLocationsMapByAbbr(final List<GermplasmImportDTO> germplasmImportDTOList) {
		final Set<String> locationAbbreviations =
			germplasmImportDTOList.stream().map(GermplasmImportDTO::getLocationAbbr).collect(Collectors.toSet());
		return this.daoFactory.getLocationDAO().getByAbbreviations(new ArrayList<>(locationAbbreviations)).stream()
			.collect(Collectors.toMap(l -> l.getLabbr().toUpperCase(), Location::getLocid));
	}

	private List<Germplasm> getGermplasmListByGIDorGermplasmUUID(final List<GermplasmUpdateDTO> germplasmUpdateDTOList) {

		// germplasm UUID should be the priority in getting germplasm
		final Set<String> germplasmUUIDs =
			germplasmUpdateDTOList.stream().map(GermplasmUpdateDTO::getGermplasmUUID).collect(Collectors.toSet());
		// If there's no UUID, use GID
		final Set<Integer> gids =
			germplasmUpdateDTOList.stream().map(o -> liquibase.util.StringUtils.isEmpty(o.getGermplasmUUID()) ? o.getGid() : null).filter(
				Objects::nonNull)
				.collect(Collectors.toSet());

		return this.daoFactory.getGermplasmDao().getByGIDsOrUUIDListWithMethodAndBibref(gids, germplasmUUIDs);

	}

	private Map<String, Germplasm> getGermplasmProgenitorsMapByGids(final List<GermplasmUpdateDTO> germplasmUpdateDTOList) {
		final Set<Integer> progenitorGids =
			germplasmUpdateDTOList.stream().map(dto -> dto.getProgenitors().values()).flatMap(Collection::stream)
				.filter(value -> value != null).collect(Collectors.toSet());
		return this.daoFactory.getGermplasmDao().getByGIDsOrUUIDListWithMethodAndBibref(progenitorGids, Collections.emptySet()).stream()
			.collect(Collectors.toMap(g -> String.valueOf(g.getGid()), Function.identity()));
	}

	private Map<String, Method> getBreedingMethodsMapByAbbr(final List<GermplasmImportDTO> germplasmDtos) {
		final Set<String> breedingMethods =
			germplasmDtos.stream().map(GermplasmImportDTO::getBreedingMethodAbbr).collect(Collectors.toSet());
		return this.daoFactory.getMethodDAO().getByCode(new ArrayList<>(breedingMethods)).stream()
			.collect(Collectors.toMap(m -> m.getMcode().toUpperCase(), method -> method));
	}

	private Map<String, Integer> getNameTypesMapByCodes(final List<GermplasmUpdateDTO> germplasmUpdateDTOList) {
		// Get the names as well as the codes specified in the preferred name property.
		final Set<String> namesCode = new HashSet<>();
		germplasmUpdateDTOList.forEach(
			g -> namesCode.addAll(g.getNames().keySet().stream().map(String::toUpperCase).collect(Collectors.toList())));
		namesCode
			.addAll(
				germplasmUpdateDTOList.stream().map(GermplasmUpdateDTO::getPreferredNameType).filter(Objects::nonNull)
					.collect(Collectors.toSet()));
		return this.germplasmNameTypeService.filterGermplasmNameTypes(namesCode).stream().collect(Collectors.toMap(
			GermplasmNameTypeDTO::getCode, GermplasmNameTypeDTO::getId));
	}

	private Map<String, Integer> getNameTypesMapByName(final List<GermplasmImportDTO> germplasmDtos) {
		final Set<String> nameTypes = new HashSet<>();
		germplasmDtos.forEach(g -> nameTypes.addAll(g.getNames().keySet()));
		final List<GermplasmNameTypeDTO> germplasmNameTypeDTOS = this.germplasmNameTypeService.filterGermplasmNameTypes(nameTypes);
		return germplasmNameTypeDTOS.stream().collect(Collectors.toMap(germplasmNameTypeDTO -> germplasmNameTypeDTO.getCode().toUpperCase(), GermplasmNameTypeDTO::getId));
	}

	private Map<String, Variable> getAttributesMap(final String programUUID, final Set<String> variableNamesOrAlias) {
		if (!variableNamesOrAlias.isEmpty()) {
			final VariableFilter variableFilter = new VariableFilter();
			variableFilter.setProgramUuid(programUUID);
			ATTRIBUTE_TYPES.forEach(variableFilter::addVariableType);
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


	private Integer calculateGnpgs(final Method method, final String progenitor1, final String progenitor2,
		final List<String> otherProgenitors) {
		if (method.isGenerative()) {
			if ((StringUtils.isEmpty(progenitor1) && StringUtils.isEmpty(progenitor2)) || ("0".equals(progenitor1) && "0"
				.equals(progenitor2))) {
				return 0;
			} else {
				if (method.getMprgn().equals(1)) {
					return 1;
				} else {
					final int otherProgenitorsSize = (otherProgenitors == null) ? 0 : otherProgenitors.size();
					return 2 + otherProgenitorsSize;
				}
			}
		} else {
			return -1;
		}
	}

	private Map<String, Germplasm> loadProgenitors(final GermplasmImportRequestDto germplasmImportRequestDto) {
		final GermplasmImportRequestDto.PedigreeConnectionType connectionType = germplasmImportRequestDto.getConnectUsing();
		if (connectionType != GermplasmImportRequestDto.PedigreeConnectionType.NONE) {
			final Set<String> progenitor1Set = germplasmImportRequestDto.getGermplasmList().stream()
				.filter(g -> StringUtils.isNotEmpty(g.getProgenitor1()) && !"0".equals(g.getProgenitor1())).map(
					GermplasmImportDTO::getProgenitor1).collect(Collectors.toSet());
			final Set<String> progenitor2Set = germplasmImportRequestDto.getGermplasmList().stream()
				.filter(g -> StringUtils.isNotEmpty(g.getProgenitor2()) && !"0".equals(g.getProgenitor2())).map(
					GermplasmImportDTO::getProgenitor2).collect(Collectors.toSet());
			final Set<String> allProgenitors = new HashSet<>(progenitor1Set);
			allProgenitors.addAll(progenitor2Set);
			final List<Germplasm> germplasmList;
			if (connectionType == GermplasmImportRequestDto.PedigreeConnectionType.GID) {
				final List<Integer> gids = allProgenitors.stream().map(Integer::parseInt).collect(Collectors.toList());
				germplasmList = this.daoFactory.getGermplasmDao().getByGIDList(gids);
			} else {
				germplasmList = this.daoFactory.getGermplasmDao().getGermplasmByGUIDs(new ArrayList<>(allProgenitors));
			}
			if (germplasmList.size() != allProgenitors.size()) {
				throw new MiddlewareRequestException("", "import.germplasm.invalid.progenitors");
			}
			return germplasmList.stream().collect(Collectors.toMap(
				g -> (connectionType == GermplasmImportRequestDto.PedigreeConnectionType.GID) ? String.valueOf(g.getGid()) :
					g.getGermplasmUUID(), gm -> gm));
		} else {
			return new HashMap<>();
		}
	}

	private List<GermplasmDto> loadGermplasmMatches(final GermplasmImportRequestDto germplasmImportRequestDto) {
		if (germplasmImportRequestDto.isSkipIfExists()) {
			final List<String> guids =
				germplasmImportRequestDto.getGermplasmList().stream().filter(g -> StringUtils.isNotEmpty(g.getGermplasmUUID()))
					.map(GermplasmImportDTO::getGermplasmUUID).collect(Collectors.toList());
			final Set<String> names = new HashSet<>();
			germplasmImportRequestDto.getGermplasmList().forEach(g -> names.addAll(g.getNames().values()));
			final GermplasmMatchRequestDto germplasmMatchRequestDto = new GermplasmMatchRequestDto();
			germplasmMatchRequestDto.setNames(new ArrayList<>(names));
			germplasmMatchRequestDto.setGermplasmUUIDs(guids);
			return this.findGermplasmMatches(germplasmMatchRequestDto, null);
		} else {
			return new ArrayList<>();
		}
	}

	private boolean isInvalidMutation(final Method method, final String progenitor2) {
		return method.isGenerative() && Integer.valueOf(1).equals(method.getMprgn()) && StringUtils.isNotEmpty(progenitor2) && !"0"
			.equals(progenitor2);
	}

	private boolean isInvalidMethodType(final Method method) {
		return !method.isGenerative() && !method.isDerivativeOrMaintenance();
	}

	private boolean isNewGermplasmATerminalAncestor(final String progenitor1, final String progenitor2) {
		return (StringUtils.isEmpty(progenitor1) && StringUtils.isEmpty(progenitor2)) || ("0".equals(progenitor1) && "0"
			.equals(progenitor2));
	}

	private void setProgenitors(final Germplasm germplasm, final Method method, final String progenitor1, final String progenitor2,
		final Map<String, Germplasm> progenitorsMap, final Multimap<String, Object[]> progenitorErrors) {

		if (this.isInvalidMethodType(method)) {
			progenitorErrors
				.put("import.germplasm.invalid.method.type", new String[] {String.valueOf(germplasm.getGid()), method.getMcode()});
			return;
		}

		if (this.isInvalidMutation(method, progenitor2)) {
			progenitorErrors.put("germplasm.gpid2.must.be.zero.for.mutations", new String[] {String.valueOf(germplasm.getGid())});
			return;
		}

		if (method.isGenerative() || this.isNewGermplasmATerminalAncestor(progenitor1, progenitor2)) {
			germplasm.setGpid1(this.resolveGpid(progenitor1, progenitorsMap));
			germplasm.setGpid2(this.resolveGpid(progenitor2, progenitorsMap));
			return;
		}

		//DERIVATIVE OR MAINTENANCE CASES
		final Germplasm progenitor1Germplasm = progenitorsMap.get(progenitor1);
		final Germplasm progenitor2Germplasm = progenitorsMap.get(progenitor2);

		//Known Immediate Source, Unknown Group Source
		if ("0".equals(progenitor1)) {
			// If Immediate Source is Terminal Ancestor or Generative, then the Group Source is Progenitor 2 GID
			// Otherwise, Group Source will be set to Progenitor 2 Group Source
			germplasm.setGpid1(this.getProgenyGroupSource(progenitor2Germplasm));
			germplasm.setGpid2(progenitor2Germplasm.getGid());
			return;
		}

		//Defined BOTH Immediate Source and Group Source, They are equals and it is either GEN or a terminal node
		if (progenitor1.equals(progenitor2) && (progenitor2Germplasm.getMethod().isGenerative() || progenitor2Germplasm
			.isTerminalAncestor())) {
			germplasm.setGpid1(progenitor1Germplasm.getGid());
			germplasm.setGpid2(progenitor2Germplasm.getGid());
			return;
		}

		//Defined BOTH Immediate Source and Group Source
		if (!"0".equals(progenitor2)) {
			if (!progenitor2Germplasm.getGpid1().equals(progenitor1Germplasm.getGid())) {
				progenitorErrors.put("import.germplasm.invalid.immediate.source.group", new String[] {
					String.valueOf(germplasm.getGid()),
					String.valueOf(progenitor2Germplasm.getGid()),
					String.valueOf(progenitor1Germplasm.getGid())});
				return;
			}
			germplasm.setGpid1(progenitor1Germplasm.getGid());
			germplasm.setGpid2(progenitor2Germplasm.getGid());
			return;
		}

		//Defined ONLY GroupSource
		if ("0".equals(progenitor2)) {
			if (progenitor1Germplasm.getMethod().isGenerative() || progenitor1Germplasm.isTerminalAncestor()) {
				germplasm.setGpid1(progenitor1Germplasm.getGid());
				germplasm.setGpid2(this.resolveGpid(progenitor2, progenitorsMap));
				return;
			}
			progenitorErrors
				.put("import.germplasm.invalid.derivative.group.source",
					new String[] {String.valueOf(germplasm.getGid()), String.valueOf(progenitor1Germplasm.getGid())});
			return;
		}
	}

	private void setOtherProgenitors(final Germplasm germplasm, final Method method, final List<Integer> otherProgenitors,
		final Multimap<String, Object[]> progenitorErrors) {
		if (method.isDerivativeOrMaintenance() && !CollectionUtils.isEmpty(otherProgenitors)) {
			progenitorErrors.put("germplasm.update.other.progenitors.can.not.be.set.for.der.man", new String[] {});
			return;
		} else {
			//Generative validations
			if (!CollectionUtils.isEmpty(otherProgenitors) && !Integer.valueOf(0).equals(method.getMprgn())) {
				progenitorErrors
					.put("germplasm.update.other.progenitors.can.not.be.set.for.gen.with.mprgn.non.equal.zero", new String[] {});
				return;
			}
		}
		if (!germplasm.otherProgenitorsGidsEquals(otherProgenitors)) {
			if (!CollectionUtils.isEmpty(otherProgenitors)) {
				//It is required to identify if germplasm and progenitor number already exists in the list
				//So we replace the progenitorId instead of adding a new element to the bag
				//This was required because Unique key progntrs_unique fails due to orphans are removed at the end of the transaction
				int progenitorNumber = 2;
				for (final Integer otherProgenitorGid : otherProgenitors) {
					progenitorNumber++;
					final Optional<Progenitor> progenitorOptional = germplasm.findByProgNo(progenitorNumber);
					if (progenitorOptional.isPresent()) {
						progenitorOptional.get().setProgenitorGid(otherProgenitorGid);
					} else {
						germplasm.getOtherProgenitors().add(new Progenitor(germplasm, progenitorNumber, otherProgenitorGid));
					}
				}
				final List<Progenitor> toRemove =
					germplasm.getOtherProgenitors().stream().filter(p -> p.getProgenitorNumber() > 2 + otherProgenitors.size()).collect(
						Collectors.toList());
				germplasm.getOtherProgenitors().removeAll(toRemove);
			} else {
				germplasm.getOtherProgenitors().clear();
			}
		}
	}

	private Integer resolveGpid(final String progenitor, final Map<String, Germplasm> progenitorsMap) {
		return ("0".equals(progenitor) || StringUtils.isEmpty(progenitor)) ? 0 : progenitorsMap.get(progenitor).getGid();
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
					if (GermplasmImportRequest.LNAME.equals(synonym.getType())) {
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
			return this.searchFilteredGermplasm(searchRequestDto, null);
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
		final Germplasm germplasm = germplasmByGUIDs.get(0);
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
		final NameDAO nameDao = this.daoFactory.getNameDao();
		final Map<Integer, Name> existingNamesByType =
			nameDao.getNamesByGids(Collections.singletonList(germplasm.getGid())).stream()
				.collect(Collectors.toMap(Name::getTypeId,
					Function.identity()));
		final Map<String, Integer> nameTypesMap = this.getNameTypesMapByNameTypeCode(Collections.singletonList(germplasmUpdateRequest));
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
					if (GermplasmImportRequest.LNAME.equals(synonym.getType())) {
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

	@Override
	public long countFilteredGermplasm(final GermplasmSearchRequestDto germplasmSearchRequestDTO) {
		return this.daoFactory.getGermplasmDao().countGermplasmDTOs(germplasmSearchRequestDTO);
	}

	@Override
	public List<GermplasmDTO> searchFilteredGermplasm(
		final GermplasmSearchRequestDto germplasmSearchRequestDTO, final Pageable pageable) {
		final List<GermplasmDTO> germplasmDTOList =
			this.daoFactory.getGermplasmDao().getGermplasmDTOList(germplasmSearchRequestDTO, pageable);
		this.populateExternalReferences(germplasmDTOList);
		this.populateSynonymsAndAttributes(germplasmDTOList);
		return germplasmDTOList;
	}

	@Override
	public Optional<GermplasmDTO> getGermplasmDTOByGUID(final String germplasmUUID) {
		final GermplasmSearchRequestDto searchDto = new GermplasmSearchRequestDto();
		searchDto.setGermplasmDbIds(Collections.singletonList(germplasmUUID));
		final List<GermplasmDTO> germplasmDTOS = this.searchFilteredGermplasm(searchDto, new PageRequest(0, 1));
		if (!CollectionUtils.isEmpty(germplasmDTOS)) {
			return Optional.of(germplasmDTOS.get(0));
		}
		return Optional.empty();
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
	public GermplasmDto getGermplasmDtoById(final Integer gid) {
		final GermplasmDto germplasmDto = this.daoFactory.getGermplasmDao().getGermplasmDtoByGid(gid);
		if (germplasmDto != null) {
			germplasmDto.setNames(this.daoFactory.getNameDao().getGermplasmNamesByGids(Collections.singletonList(gid)));
			germplasmDto.setGermplasmOrigin(this.daoFactory.getGermplasmStudySourceDAO().getGermplasmOrigin(gid));
			final List<Progenitor> progenitors = this.daoFactory.getProgenitorDao().getByGID(gid);
			germplasmDto.setOtherProgenitors(progenitors.stream().map(Progenitor::getProgenitorGid).collect(Collectors.toList()));
			this.getCreatedByWorkbenchUserName(germplasmDto.getCreatedByUserId()).ifPresent(germplasmDto::setCreatedBy);
		}
		return germplasmDto;
	}

	@Override
	public ProgenitorsDetailsDto getGermplasmProgenitorDetails(final Integer gid) {
		final GermplasmDto germplasmDto = this.daoFactory.getGermplasmDao().getGermplasmDtoByGid(gid);
		if (germplasmDto != null) {
			final List<Progenitor> progenitors = this.daoFactory.getProgenitorDao().getByGID(gid);
			germplasmDto.setOtherProgenitors(progenitors.stream().map(Progenitor::getProgenitorGid).collect(Collectors.toList()));
			final Method method = this.daoFactory.getMethodDAO().getById(germplasmDto.getBreedingMethodId());

			final ProgenitorsDetailsDto progenitorsDetailsDto = new ProgenitorsDetailsDto();
			progenitorsDetailsDto.setBreedingMethodId(germplasmDto.getBreedingMethodId());
			progenitorsDetailsDto.setBreedingMethodName(germplasmDto.getBreedingMethod());
			progenitorsDetailsDto.setBreedingMethodCode(method.getMcode());
			progenitorsDetailsDto.setBreedingMethodType(method.getMtype());
			progenitorsDetailsDto.setNumberOfDerivativeProgeny(this.daoFactory.getGermplasmDao().countGermplasmDerivativeProgeny(gid));

			final List<Integer> maleParentsGids = new ArrayList<>();
			maleParentsGids.add(germplasmDto.getGpid2());
			maleParentsGids.addAll(germplasmDto.getOtherProgenitors());

			final List<Integer> allParentsGids = new ArrayList<>();
			allParentsGids.add(germplasmDto.getGpid1());
			allParentsGids.addAll(maleParentsGids);

			final Map<Integer, GermplasmDto> germplasmDtoMap =
				this.daoFactory.getGermplasmDao().getGermplasmDtoByGids(allParentsGids).stream()
					.collect(Collectors.toMap(GermplasmDto::getGid, g -> g));
			final GermplasmDto femaleParent = germplasmDtoMap.get(germplasmDto.getGpid1());
			final List<GermplasmDto> maleParents = new ArrayList<>();
			maleParentsGids.forEach(m -> {
				if (germplasmDtoMap.containsKey(m)) {
					maleParents.add(germplasmDtoMap.get(m));
				}
			});

			if (MethodType.GENERATIVE.getCode().equals(method.getMtype())) {
				progenitorsDetailsDto.setFemaleParent(femaleParent);
				progenitorsDetailsDto.setMaleParents(maleParents);
			} else {
				progenitorsDetailsDto.setGroupSource(femaleParent);
				progenitorsDetailsDto.setImmediateSource(maleParents.isEmpty() ? null : maleParents.get(0));
			}
			return progenitorsDetailsDto;
		}
		return null;
	}

	@Override
	public void updateGermplasmBasicDetails(final Integer gid, final GermplasmBasicDetailsDto germplasmBasicDetailsDto) {
		final Germplasm germplasm = this.daoFactory.getGermplasmDao().getById(gid);
		Optional.ofNullable(germplasmBasicDetailsDto.getBreedingLocationId()).ifPresent(germplasm::setLocationId);
		Optional.ofNullable(germplasmBasicDetailsDto.getCreationDate()).ifPresent(g -> germplasm.setGdate(Integer.valueOf(g)));
		final Optional<String> referenceOptional = Optional.ofNullable(germplasmBasicDetailsDto.getReference());
		this.saveOrUpdateReference(germplasm, referenceOptional);
		this.daoFactory.getGermplasmDao().save(germplasm);
	}

	@Override
	public void updateGermplasmPedigree(final Integer gid, final ProgenitorsUpdateRequestDto progenitorsUpdateRequestDto) {
		final Germplasm germplasm = this.daoFactory.getGermplasmDao().getById(gid);

		//Request values
		final Optional<Integer> newBreedingMethodIdOptional = Optional.ofNullable(progenitorsUpdateRequestDto.getBreedingMethodId());
		final Optional<Integer> newGpid1Optional = Optional.ofNullable(progenitorsUpdateRequestDto.getGpid1());
		final Optional<Integer> newGpid2Optional = Optional.ofNullable(progenitorsUpdateRequestDto.getGpid2());
		final Optional<List<Integer>> newOtherProgenitorsOptional = Optional.ofNullable(progenitorsUpdateRequestDto.getOtherProgenitors());

		//Old germplasm values.
		final Germplasm germplasmBeforeUpdate = germplasm.clone();

		//Final values after combining request and existing germplasm data
		final Method methodFinal =
			(newBreedingMethodIdOptional.isPresent() && !newBreedingMethodIdOptional.get()
				.equals(germplasmBeforeUpdate.getMethod().getMid())) ?
				this.daoFactory.getMethodDAO().getById(newBreedingMethodIdOptional.get()) :
				germplasmBeforeUpdate.getMethod();

		final Integer gpid1Final = newGpid1Optional.orElseGet(germplasmBeforeUpdate::getGpid1);

		//For mutations, if gpid1 is set and gpid2 is null, then new gpid2 is 0
		final Integer gpid2Final = newGpid2Optional
			.orElseGet(() -> ((Integer.valueOf(1).equals(methodFinal.getMprgn()) && newGpid1Optional.isPresent()) ? 0 :
				germplasmBeforeUpdate.getGpid2()));

		final List<Integer> otherProgenitorsFinal =
			this.getOtherProgenitorsFinal(germplasmBeforeUpdate, newGpid2Optional.orElse(null), newOtherProgenitorsOptional.orElse(null));

		if (this.isPedigreeUpdateDetected(germplasmBeforeUpdate, methodFinal, gpid1Final, gpid2Final, otherProgenitorsFinal)) {
			final Multimap<String, Object[]> progenitorsErrors = ArrayListMultimap.create();
			germplasm.setMethod(methodFinal);
			germplasm.setMethodId(methodFinal.getMid());
			final Map<String, Germplasm> progenitorsMap = this.loadProgenitors(gpid1Final, gpid2Final);
			this.setProgenitors(germplasm, methodFinal, String.valueOf(gpid1Final), String.valueOf(gpid2Final), progenitorsMap,
				progenitorsErrors);
			this.setOtherProgenitors(germplasm, methodFinal, otherProgenitorsFinal, progenitorsErrors);

			if (!progenitorsErrors.isEmpty()) {
				final Map.Entry<String, Object[]> error = progenitorsErrors.entries().iterator().next();
				throw new MiddlewareRequestException("", error.getKey(), error.getValue());
			}

			germplasm.setGnpgs(this.calculateGnpgs(methodFinal, String.valueOf(gpid1Final), String.valueOf(gpid2Final),
				otherProgenitorsFinal.stream().map(String::valueOf).collect(Collectors.toList())));

			final boolean gpidsWillBeChanged = !germplasm.getGpid1().equals(germplasmBeforeUpdate.getGpid1()) || !germplasm.getGpid2()
				.equals(germplasmBeforeUpdate.getGpid2());
			final boolean hasProgeny = !this.daoFactory.getGermplasmDao().getChildren(germplasm.getGid()).isEmpty();
			if (gpidsWillBeChanged && hasProgeny) {
				final Set<Integer> gpids = new HashSet<>();
				gpids.add(germplasm.getGpid1());
				gpids.add(germplasm.getGpid2());

				final boolean isNewParentANodeChildren =
					this.daoFactory.getGermplasmDao().isNewParentANodeDescendant(gpids, germplasm.getGid(), maxRecursiveQueries);
				if (isNewParentANodeChildren) {
					throw new MiddlewareRequestException("", "germplasm.update.germplasm.new.parents.are.children", "");
				}
			}

			this.daoFactory.getGermplasmDao().save(germplasm);
			this.updateGroupSource(germplasmBeforeUpdate, germplasm);
		}

	}

	private void updateGroupSource(final Germplasm oldGermplasm, final Germplasm newGermplasm) {
		final UpdateGroupSourceAction updateGroupSourceAction = this.getUpdateGroupSourceAction(oldGermplasm, newGermplasm);
		if (updateGroupSourceAction == UpdateGroupSourceAction.NONE) {
			return;
		}
		final Integer newGroupSource = this.getProgenyGroupSource(newGermplasm);
		if (updateGroupSourceAction == UpdateGroupSourceAction.DIRECT) {
			this.daoFactory.getGermplasmDao().updateGroupSource(newGermplasm.getGid(), newGroupSource);
			return;
		}

		if (updateGroupSourceAction == UpdateGroupSourceAction.RECURSIVE) {
			this.daoFactory.getGermplasmDao()
				.updateGroupSourceTraversingProgeny(newGermplasm.getGid(), newGroupSource, maxRecursiveQueries);
		}
	}

	private Integer getProgenyGroupSource(final Germplasm germplasm) {
		//For a terminal node or a generative germplasm, the group source for any derivative progeny is itself.
		//Otherwise the group source is gpid1
		if (germplasm.isTerminalAncestor() || germplasm.getMethod().isGenerative()) {
			return germplasm.getGid();
		}
		return germplasm.getGpid1();
	}

	/**
	 * With this function we avoid to traverse the tree whenever is possible.
	 * Do not update this function if you are not clear enough about BMS germplasm tree management
	 *
	 * @param oldGermplasm Germplasm before any pedigree update
	 * @param newGermplasm Germplasm after being modified
	 * @return tree update action to be taken
	 */
	private UpdateGroupSourceAction getUpdateGroupSourceAction(final Germplasm oldGermplasm, final Germplasm newGermplasm) {
		final Method oldMethod = oldGermplasm.getMethod();
		final Method newMethod = newGermplasm.getMethod();
		if (oldMethod.isGenerative() && newMethod.isDerivativeOrMaintenance() && !newGermplasm.isTerminalAncestor()) {
			return UpdateGroupSourceAction.DIRECT;
		}
		if (oldMethod.isDerivativeOrMaintenance() && newMethod.isGenerative() && !oldGermplasm.isTerminalAncestor()) {
			return UpdateGroupSourceAction.RECURSIVE;
		}
		final boolean isGpidUpdateDetected = this.isGpidUpdateDetected(oldGermplasm, newGermplasm.getGpid1(), newGermplasm.getGpid2());
		if (oldMethod.isDerivativeOrMaintenance() && newMethod.isDerivativeOrMaintenance() && isGpidUpdateDetected) {
			if (oldGermplasm.isTerminalAncestor()) {
				return UpdateGroupSourceAction.DIRECT;
			}
			return UpdateGroupSourceAction.RECURSIVE;
		}
		return UpdateGroupSourceAction.NONE;
	}

	private boolean isPedigreeUpdateDetected(final Germplasm germplasmBeforeUpdate, final Method newMethod, final Integer newGpid1,
		final Integer newGpid2,
		final List<Integer> newOtherProgenitors) {
		return !newMethod.getMid().equals(germplasmBeforeUpdate.getMethod().getMid()) || this.isGpidUpdateDetected(germplasmBeforeUpdate,
			newGpid1, newGpid2)
			|| !germplasmBeforeUpdate.otherProgenitorsGidsEquals(newOtherProgenitors);
	}

	private boolean isGpidUpdateDetected(final Germplasm germplasmBeforeUpdate, final Integer newGpid1, final Integer newGpid2) {
		return !(germplasmBeforeUpdate.getGpid1().equals(newGpid1) && germplasmBeforeUpdate.getGpid2().equals(newGpid2));
	}

	private List<Integer> getOtherProgenitorsFinal(final Germplasm germplasmBeforeUpdate, final Integer newGpid2,
		final List<Integer> newOtherProgenitors) {
		if (Objects.nonNull(newGpid2)) {
			return (CollectionUtils.isEmpty(newOtherProgenitors)) ? new ArrayList<>() : newOtherProgenitors;
		}
		return germplasmBeforeUpdate.getOtherProgenitors().stream().map(Progenitor::getProgenitorGid).collect(Collectors.toList());
	}

	private Map<String, Germplasm> loadProgenitors(final Integer gpid1, final Integer gpid2) {
		final Map<String, Germplasm> progenitorsMap = new HashMap<>();
		if (!gpid1.equals(0)) {
			progenitorsMap.put(String.valueOf(gpid1), this.daoFactory.getGermplasmDao().getById(gpid1));
		}
		if (!gpid2.equals(0) && !progenitorsMap.containsKey(String.valueOf(gpid2))) {
			progenitorsMap.put(String.valueOf(gpid2), this.daoFactory.getGermplasmDao().getById(gpid2));
		}
		return progenitorsMap;
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

	private Optional<String> getCreatedByWorkbenchUserName(final Integer userId) {
		final WorkbenchUser workbenchUser = this.userService.getUserById(userId);
		if (workbenchUser != null) {
			return Optional.of(workbenchUser.getName());
		}
		return Optional.empty();
	}

	public void setWorkbenchDataManager(final WorkbenchDataManager workbenchDataManager) {
		this.workbenchDataManager = workbenchDataManager;
	}

	public void setOntologyDataManager(final OntologyDataManager ontologyDataManager) {
		this.ontologyDataManager = ontologyDataManager;
	}

	public void setOntologyVariableDataManager(
		final OntologyVariableDataManager ontologyVariableDataManager) {
		this.ontologyVariableDataManager = ontologyVariableDataManager;
	}

	public void setGermplasmAttributeService(final GermplasmAttributeService germplasmAttributeService) {
		this.germplasmAttributeService = germplasmAttributeService;
	}

	public void setGermplasmNameTypeService(final GermplasmNameTypeService germplasmNameTypeService) {
		this.germplasmNameTypeService = germplasmNameTypeService;
	}
}
