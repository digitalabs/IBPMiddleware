package org.generationcp.middleware.api.brapi.v2.germplasm;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.commons.lang3.StringUtils;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@JsonInclude(JsonInclude.Include.NON_NULL)
@AutoProperty
public class GermplasmImportRequest {

	public static final String CROPNM_ATTR = "CROPNM_AP_text";
	public static final String ORIGIN_ATTR = "SORIG_AP_text";
	public static final String INSTCODE_ATTR = "INSTCODE_AP_text";
	public static final String INSTNAME_ATTR = "ORIGININST_AP_text";
	public static final String PLOTCODE_ATTR = "PLOTCODE_AP_text";
	public static final String SPECIES_ATTR = "SPNAM_AP_text";
	public static final String SPECIES_AUTH_ATTR = "SPAUTH_AP_text";
	public static final String SUBTAX_ATTR = "SUBTAX_AP_text";
	public static final String SUBTAX_AUTH_ATTR = "STAUTH_AP_text";
	public static final String ACCNO_NAME_TYPE = "ACCNO";
	public static final String PED_NAME_TYPE = "PED";
	public static final String GENUS_NAME_TYPE = "GENUS";
	public static final String PUI_NAME_TYPE = "PUI";
	public static final String LNAME_NAME_TYPE = "LNAME";
	public static final List<String> BRAPI_SPECIFIABLE_NAMETYPES = Arrays.asList(ACCNO_NAME_TYPE, PED_NAME_TYPE, GENUS_NAME_TYPE, LNAME_NAME_TYPE, PUI_NAME_TYPE);
	public static final List<String> BRAPI_SPECIFIABLE_ATTRTYPES =
		Arrays.asList(CROPNM_ATTR, ORIGIN_ATTR, INSTCODE_ATTR, INSTNAME_ATTR, PLOTCODE_ATTR, SPECIES_ATTR, SPECIES_AUTH_ATTR, SUBTAX_ATTR,
			SUBTAX_AUTH_ATTR);

	private String accessionNumber;
	private String acquisitionDate;
	private Map<String, String> additionalInfo = new HashMap<>();
	private BiologicalStatusOfAccessionCodeEnum biologicalStatusOfAccessionCode;
	private String biologicalStatusOfAccessionDescription;
	private String breedingMethodDbId;
	private String collection;
	private String commonCropName;
	private String countryOfOriginCode;
	private String defaultDisplayName;
	private String documentationURL;
	private List<ExternalReferenceDTO> externalReferences;
	private String genus;
	private String germplasmName;
	private String germplasmOrigin;
	private String germplasmPUI;
	private String germplasmPreprocessing;
	private String instituteCode;
	private String instituteName;
	private String pedigree;
	private String seedSource;
	private String seedSourceDescription;
	private String species;
	private String speciesAuthority;
	private String subtaxa;
	private String subtaxaAuthority;
	private List<Synonym> synonyms = new ArrayList<>();

	public GermplasmImportRequest() {

	}

	public GermplasmImportRequest(final String accessionNumber, final String acquisitionDate, final String breedingMethodDbId,
		final String commonCropName,
		final String countryOfOriginCode, final String defaultDisplayName, final String genus, final String germplasmOrigin,
		final String instituteCode,
		final String instituteName, final String pedigree, final String seedSource, final String species, final String speciesAuthority,
		final String subtaxa, final String subtaxaAuthority) {
		this.accessionNumber = accessionNumber;
		this.acquisitionDate = acquisitionDate;
		this.breedingMethodDbId = breedingMethodDbId;
		this.commonCropName = commonCropName;
		this.countryOfOriginCode = countryOfOriginCode;
		this.defaultDisplayName = defaultDisplayName;
		this.genus = genus;
		this.germplasmOrigin = germplasmOrigin;
		this.instituteCode = instituteCode;
		this.instituteName = instituteName;
		this.pedigree = pedigree;
		this.seedSource = seedSource;
		this.species = species;
		this.speciesAuthority = speciesAuthority;
		this.subtaxa = subtaxa;
		this.subtaxaAuthority = subtaxaAuthority;
	}

	public String getAccessionNumber() {
		return this.accessionNumber;
	}

	public void setAccessionNumber(final String accessionNumber) {
		this.accessionNumber = accessionNumber;
	}

	public String getAcquisitionDate() {
		return this.acquisitionDate;
	}

	public void setAcquisitionDate(final String acquisitionDate) {
		this.acquisitionDate = acquisitionDate;
	}

	public Map<String, String> getAdditionalInfo() {
		return this.additionalInfo;
	}

	public void setAdditionalInfo(final Map<String, String> additionalInfo) {
		this.additionalInfo = additionalInfo;
	}

	public BiologicalStatusOfAccessionCodeEnum getBiologicalStatusOfAccessionCode() {
		return this.biologicalStatusOfAccessionCode;
	}

	public void setBiologicalStatusOfAccessionCode(final BiologicalStatusOfAccessionCodeEnum biologicalStatusOfAccessionCode) {
		this.biologicalStatusOfAccessionCode = biologicalStatusOfAccessionCode;
	}

	public String getBiologicalStatusOfAccessionDescription() {
		return this.biologicalStatusOfAccessionDescription;
	}

	public void setBiologicalStatusOfAccessionDescription(final String biologicalStatusOfAccessionDescription) {
		this.biologicalStatusOfAccessionDescription = biologicalStatusOfAccessionDescription;
	}

	public String getBreedingMethodDbId() {
		return this.breedingMethodDbId;
	}

	public void setBreedingMethodDbId(final String breedingMethodDbId) {
		this.breedingMethodDbId = breedingMethodDbId;
	}

	public String getCollection() {
		return this.collection;
	}

	public void setCollection(final String collection) {
		this.collection = collection;
	}

	public String getCommonCropName() {
		return this.commonCropName;
	}

	public void setCommonCropName(final String commonCropName) {
		this.commonCropName = commonCropName;
	}

	public String getCountryOfOriginCode() {
		return this.countryOfOriginCode;
	}

	public void setCountryOfOriginCode(final String countryOfOriginCode) {
		this.countryOfOriginCode = countryOfOriginCode;
	}

	public String getDefaultDisplayName() {
		return this.defaultDisplayName;
	}

	public void setDefaultDisplayName(final String defaultDisplayName) {
		this.defaultDisplayName = defaultDisplayName;
	}

	public String getDocumentationURL() {
		return this.documentationURL;
	}

	public void setDocumentationURL(final String documentationURL) {
		this.documentationURL = documentationURL;
	}

	public String getGenus() {
		return this.genus;
	}

	public void setGenus(final String genus) {
		this.genus = genus;
	}

	public String getGermplasmName() {
		return this.germplasmName;
	}

	public void setGermplasmName(final String germplasmName) {
		this.germplasmName = germplasmName;
	}

	public String getGermplasmOrigin() {
		return this.germplasmOrigin;
	}

	public void setGermplasmOrigin(final String germplasmOrigin) {
		this.germplasmOrigin = germplasmOrigin;
	}

	public String getGermplasmPUI() {
		return this.germplasmPUI;
	}

	public void setGermplasmPUI(final String germplasmPUI) {
		this.germplasmPUI = germplasmPUI;
	}

	public String getGermplasmPreprocessing() {
		return this.germplasmPreprocessing;
	}

	public void setGermplasmPreprocessing(final String germplasmPreprocessing) {
		this.germplasmPreprocessing = germplasmPreprocessing;
	}

	public String getInstituteCode() {
		return this.instituteCode;
	}

	public void setInstituteCode(final String instituteCode) {
		this.instituteCode = instituteCode;
	}

	public String getInstituteName() {
		return this.instituteName;
	}

	public void setInstituteName(final String instituteName) {
		this.instituteName = instituteName;
	}

	public String getPedigree() {
		return this.pedigree;
	}

	public void setPedigree(final String pedigree) {
		this.pedigree = pedigree;
	}

	public String getSeedSource() {
		return this.seedSource;
	}

	public void setSeedSource(final String seedSource) {
		this.seedSource = seedSource;
	}

	public String getSeedSourceDescription() {
		return this.seedSourceDescription;
	}

	public void setSeedSourceDescription(final String seedSourceDescription) {
		this.seedSourceDescription = seedSourceDescription;
	}

	public String getSpecies() {
		return this.species;
	}

	public void setSpecies(final String species) {
		this.species = species;
	}

	public String getSpeciesAuthority() {
		return this.speciesAuthority;
	}

	public void setSpeciesAuthority(final String speciesAuthority) {
		this.speciesAuthority = speciesAuthority;
	}

	public String getSubtaxa() {
		return this.subtaxa;
	}

	public void setSubtaxa(final String subtaxa) {
		this.subtaxa = subtaxa;
	}

	public String getSubtaxaAuthority() {
		return this.subtaxaAuthority;
	}

	public void setSubtaxaAuthority(final String subtaxaAuthority) {
		this.subtaxaAuthority = subtaxaAuthority;
	}

	public List<Synonym> getSynonyms() {
		return this.synonyms;
	}

	public void setSynonyms(final List<Synonym> synonyms) {
		this.synonyms = synonyms;
	}

	public List<ExternalReferenceDTO> getExternalReferences() {
		return this.externalReferences;
	}

	public void setExternalReferences(final List<ExternalReferenceDTO> externalReferences) {
		this.externalReferences = externalReferences;
	}

	@Override
	public int hashCode() {
		return Pojomatic.hashCode(this);
	}

	@Override
	public String toString() {
		return Pojomatic.toString(this);
	}

	@Override
	public boolean equals(final Object o) {
		return Pojomatic.equals(this, o);
	}

	public Map<String, String> getCustomAttributeFieldsMap() {
		final Map<String, String> attrMap = new HashMap<>();
		attrMap.put(CROPNM_ATTR, this.getCommonCropName());
		attrMap.put(ORIGIN_ATTR, this.getGermplasmOrigin());
		attrMap.put(INSTCODE_ATTR, this.getInstituteCode());
		attrMap.put(INSTNAME_ATTR, this.getInstituteName());
		attrMap.put(PLOTCODE_ATTR, this.getSeedSource());
		attrMap.put(SPECIES_ATTR, this.getSpecies());
		attrMap.put(SPECIES_AUTH_ATTR, this.getSpeciesAuthority());
		attrMap.put(SUBTAX_ATTR, this.getSubtaxa());
		attrMap.put(SUBTAX_AUTH_ATTR, this.getSubtaxaAuthority());
		return attrMap;
	}

	public Map<String, String> getCustomNamesFieldsMap() {
		final Map<String, String> namesMap = new HashMap<>();
		namesMap.put(ACCNO_NAME_TYPE, this.getAccessionNumber());
		namesMap.put(GENUS_NAME_TYPE, this.getGenus());
		namesMap.put(PED_NAME_TYPE, this.getPedigree());
		namesMap.put(LNAME_NAME_TYPE, this.getDefaultDisplayName());
		namesMap.put(PUI_NAME_TYPE, this.getGermplasmPUI());
		return namesMap;
	}

	public boolean isGermplasmPUIInList(final Collection<String> puiList){
		if (!puiList.contains(this.getGermplasmPUI())) {
			final Optional<String> germplasmPUIFromNames = this.getGermplasmPUIFromSynonyms();
			return puiList.contains(germplasmPUIFromNames.orElse(""));
		}
		return true;
	}

	public Optional<String> getGermplasmPUIFromSynonyms() {
		return this.synonyms.stream().filter(s -> GermplasmImportRequest.PUI_NAME_TYPE.equalsIgnoreCase(s.getType()))
			.map(Synonym::getSynonym).collect(Collectors.toList()).stream().findFirst();
	}

	public List<String> collectGermplasmPUIs() {
		final List<String> puisList = new ArrayList<>();
		if (!StringUtils.isEmpty(this.getGermplasmPUI())) {
			puisList.add(this.getGermplasmPUI());
		}
		final Optional<String> germplasmPUIFromSynonym = this.getGermplasmPUIFromSynonyms();
		germplasmPUIFromSynonym.ifPresent(puisList::add);
		return puisList;
	}

}
