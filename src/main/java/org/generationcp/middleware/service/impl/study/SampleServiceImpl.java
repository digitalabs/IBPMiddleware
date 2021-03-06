package org.generationcp.middleware.service.impl.study;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.sample.SampleDTO;
import org.generationcp.middleware.domain.sample.SampleDetailsDTO;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.manager.DaoFactory;
import org.generationcp.middleware.pojos.Sample;
import org.generationcp.middleware.pojos.SampleList;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.generationcp.middleware.pojos.dms.ExperimentModel;
import org.generationcp.middleware.pojos.dms.ExperimentProperty;
import org.generationcp.middleware.pojos.dms.GeolocationProperty;
import org.generationcp.middleware.pojos.dms.ProjectProperty;
import org.generationcp.middleware.pojos.dms.StockModel;
import org.generationcp.middleware.service.api.SampleService;
import org.generationcp.middleware.service.api.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@Transactional
public class SampleServiceImpl implements SampleService {

	private static final String SAMPLE_KEY_PREFIX = "S";

	private final HibernateSessionProvider sessionProvider;

	@Autowired
	private UserService userService;

	private final DaoFactory daoFactory;

	public SampleServiceImpl(final HibernateSessionProvider sessionProvider) {
		this.sessionProvider = sessionProvider;
		this.daoFactory = new DaoFactory(this.sessionProvider);
	}

	@Override
	public Sample buildSample(final String cropName, final String cropPrefix, final Integer entryNumber,
		final String sampleName, final Date samplingDate, final Integer experimentId, final SampleList sampleList, final Integer createdBy,
		final Date createdDate, final Integer takenBy, final Integer sampleNumber) {

		final Sample sample = new Sample();
		sample.setTakenBy(takenBy);
		sample.setEntryNumber(entryNumber);
		sample.setSampleName(sampleName);
		sample.setCreatedDate(new Date());
		sample.setSamplingDate(samplingDate);
		sample.setSampleBusinessKey(this.getSampleBusinessKey(cropPrefix));
		sample.setSampleList(sampleList);
		sample.setCreatedDate(createdDate);
		sample.setCreatedBy(createdBy);
		sample.setExperiment(this.daoFactory.getExperimentDao().getById(experimentId));
		sample.setSampleNumber(sampleNumber);

		return sample;
	}

	private String getSampleBusinessKey(final String cropPrefix) {
		String sampleBusinessKey = cropPrefix;
		sampleBusinessKey = sampleBusinessKey + SampleServiceImpl.SAMPLE_KEY_PREFIX;
		sampleBusinessKey = sampleBusinessKey + RandomStringUtils.randomAlphanumeric(8);

		return sampleBusinessKey;
	}

	@Override
	public List<SampleDTO> filter(final String obsUnitId, final Integer listId, final Pageable pageable) {
		Integer ndExperimentId = null;
		final ExperimentModel experiment = this.daoFactory.getExperimentDao().getByObsUnitId(obsUnitId);
		if (experiment != null) {
			ndExperimentId = experiment.getNdExperimentId();
		}
		final List<SampleDTO> sampleDTOS = this.daoFactory.getSampleDao().filter(ndExperimentId, listId, pageable);
		this.populateTakenBy(sampleDTOS);
		return sampleDTOS;
	}

	@Override
	public long countFilter(final String obsUnitId, final Integer listId) {

		Integer ndExperimentId = null;
		final ExperimentModel experiment = this.daoFactory.getExperimentDao().getByObsUnitId(obsUnitId);
		if (experiment != null) {
			ndExperimentId = experiment.getNdExperimentId();
		}
		return this.daoFactory.getSampleDao().countFilter(ndExperimentId, listId);
	}

	public SampleDetailsDTO getSampleObservation(final String sampleId) {
		final Sample sample = this.daoFactory.getSampleDao().getBySampleBk(sampleId);

		return this.getSampleDetailsDTO(sample);
	}

	@Override
	public Map<String, SampleDTO> getSamplesBySampleUID(final Set<String> sampleUIDs) {
		final List<SampleDTO> sampleDTOs = this.daoFactory.getSampleDao().getBySampleBks(sampleUIDs);
		return Maps.uniqueIndex(sampleDTOs, new Function<SampleDTO, String>() {

			public String apply(final SampleDTO from) {
				return from.getSampleBusinessKey();
			}
		});
	}

	private SampleDetailsDTO getSampleDetailsDTO(final Sample sample) {
		final SampleDetailsDTO samplesDetailsDto;
		if (sample == null) {
			return new SampleDetailsDTO();
		}

		final ExperimentModel experiment = sample.getExperiment();
		final DmsProject study = experiment.getProject().getStudy();
		final Integer studyId = study.getProjectId();
		final String takenBy =
			(sample.getTakenBy() != null) ? this.userService.getUserById(sample.getTakenBy()).getPerson().getDisplayName() : null;
		final String obsUnitId = experiment.getObsUnitId();
		final String studyName = study.getName();
		final StockModel stock = experiment.getStock();
		final String entryNo = stock.getUniqueName();
		final Integer gid = (stock.getGermplasm() != null) ? stock.getGermplasm().getGid() : null;
		final String germplasmUUID = (stock.getGermplasm() != null) ? stock.getGermplasm().getGermplasmUUID() : null;

		samplesDetailsDto = new SampleDetailsDTO(studyId, obsUnitId, sample.getSampleBusinessKey());
		samplesDetailsDto.setTakenBy(takenBy);
		samplesDetailsDto.setSampleDate(sample.getSamplingDate());
		samplesDetailsDto.setStudyName(studyName);
		samplesDetailsDto.setEntryNo(Integer.valueOf(entryNo));
		samplesDetailsDto.setGid(gid);
		samplesDetailsDto.setGermplasmUUID(germplasmUUID);
		samplesDetailsDto.setPlateId(sample.getPlateId());
		samplesDetailsDto.setSampleNumber(sample.getSampleNumber());

		samplesDetailsDto.setSampleName(sample.getSampleName());
		samplesDetailsDto.setDesignation(stock.getName());

		this.fillPlotNoByExperimentProperty(experiment.getProperties(), samplesDetailsDto);
		this.fillProjectProperties(study.getProperties(), samplesDetailsDto);
		this.fillLocationByGeoLocationProperties(experiment.getGeoLocation().getProperties(), samplesDetailsDto);

		return samplesDetailsDto;
	}

	private void fillLocationByGeoLocationProperties(final List<GeolocationProperty> geolocationProperties,
		final SampleDetailsDTO samplesDetailsDto) {
		for (final GeolocationProperty properties : geolocationProperties) {
			if (properties.getTypeId().equals(TermId.TRIAL_LOCATION.getId()) && StringUtils.isNotBlank(properties.getValue())) {
				samplesDetailsDto.setLocationName(properties.getValue());
			} else if (properties.getTypeId().equals(TermId.LOCATION_ID.getId()) && StringUtils.isNotBlank(properties.getValue())) {
				samplesDetailsDto.setLocationDbId(Integer.valueOf(properties.getValue()));
			}
		}
	}

	private void fillProjectProperties(final List<ProjectProperty> projectProperties, final SampleDetailsDTO samplesDetailsDto) {

		for (final ProjectProperty projectProperty : projectProperties) {
			//SEEDING_DATE
			final String value = projectProperty.getValue();
			if (StringUtils.isBlank(value)) {
				continue;
			}
			if (projectProperty.getVariableId().equals(TermId.SEEDING_DATE.getId())) {
				final String plantingDate = value;
				samplesDetailsDto.setSeedingDate(plantingDate);
			}
			//CROP SEASON
			if (projectProperty.getVariableId().equals(TermId.SEASON_VAR_TEXT.getId())) {
				final String season = value;
				samplesDetailsDto.setSeason(season);
			}
		}
	}

	private void fillPlotNoByExperimentProperty(final List<ExperimentProperty> experimentProperty,
		final SampleDetailsDTO sampleDetailsDTO) {
		boolean foundPlotNumber = false;
		final Iterator<ExperimentProperty> experimentPropertyIterator = experimentProperty.iterator();
		while (experimentPropertyIterator.hasNext() && !foundPlotNumber) {
			final ExperimentProperty properties = experimentPropertyIterator.next();
			if (properties.getTypeId().equals(TermId.PLOT_NO.getId())) {
				final Integer plotNumber = Integer.valueOf(properties.getValue());
				sampleDetailsDTO.setPlotNo(plotNumber);
				foundPlotNumber = true;
			}
		}
	}

	@Override
	public List<SampleDTO> getByGid(final Integer gid) {
		final List<SampleDTO> sampleDTOS = this.daoFactory.getSampleDao().getByGid(gid);
		this.populateTakenBy(sampleDTOS);
		return sampleDTOS;
	}

	@Override
	public Boolean studyHasSamples(final Integer studyId) {
		return this.daoFactory.getSampleDao().hasSamples(studyId);
	}

	@Override
	public Boolean studyEntryHasSamples(final Integer studyId, final Integer entryId) {
		return this.daoFactory.getSampleDao().studyEntryHasSamples(studyId, entryId);
	}

	void populateTakenBy(final List<SampleDTO> sampleDTOS) {
		// Populate takenBy with full name of user from workbench database.
		final List<Integer> userIds = sampleDTOS.stream().map(sampleDTO -> sampleDTO.getTakenByUserId()).collect(Collectors.toList());
		final Map<Integer, String> userIDFullNameMap = this.userService.getUserIDFullNameMap(userIds);
		sampleDTOS.forEach(sampleDTO -> sampleDTO.setTakenBy(userIDFullNameMap.get(sampleDTO.getTakenByUserId())));
	}

}
