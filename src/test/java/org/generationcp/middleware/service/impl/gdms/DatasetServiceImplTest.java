package org.generationcp.middleware.service.impl.gdms;

import org.apache.commons.lang.RandomStringUtils;
import org.generationcp.middleware.dao.gdms.CharValuesDAO;
import org.generationcp.middleware.dao.gdms.DatasetDAO;
import org.generationcp.middleware.dao.gdms.MarkerDAO;
import org.generationcp.middleware.domain.sample.SampleDTO;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.pojos.gdms.Dataset;
import org.generationcp.middleware.pojos.gdms.Marker;
import org.generationcp.middleware.service.api.SampleService;
import org.generationcp.middleware.service.api.gdms.DatasetRetrieveDto;
import org.generationcp.middleware.service.api.gdms.DatasetUploadDto;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

/**
 * Created by clarysabel on 11/9/17.
 */
public class DatasetServiceImplTest {

	@Mock
	private HibernateSessionProvider session;

	@Mock
	private SampleService sampleService;

	@Mock
	private DatasetDAO datasetDAO;

	@Mock
	private MarkerDAO markerDAO;

	@Mock
	private CharValuesDAO charValuesDAO;

	private DatasetServiceImpl datasetService;

	@Before
	public void setUp() {
		MockitoAnnotations.initMocks(this);
		this.datasetService = new DatasetServiceImpl(this.session);
		this.datasetService.setSampleService(this.sampleService);
		this.datasetService.setDatasetDAO(this.datasetDAO);
		this.datasetService.setMarkerDAO(this.markerDAO);
		this.datasetService.setCharValuesDAO(this.charValuesDAO);
	}

	@Test (expected = Exception.class)
	public void testSaveDataset_NullName() throws Exception {
		final DatasetUploadDto datasetUploadDto = new DatasetUploadDto();
		datasetUploadDto.setName(null);
		this.datasetService.saveDataset(datasetUploadDto);
	}


	@Test (expected = Exception.class)
	public void testSaveDataset_NullMarkers() throws Exception {
		final DatasetUploadDto datasetUploadDto = new DatasetUploadDto();
		this.datasetService.saveDataset(datasetUploadDto);
	}

	@Test (expected = Exception.class)
	public void testSaveDataset_NullSamples() throws Exception {
		final DatasetUploadDto datasetUploadDto = new DatasetUploadDto();
		datasetUploadDto.setSampleAccessions(null);
		this.datasetService.saveDataset(datasetUploadDto);
	}


	@Test (expected = Exception.class)
	public void testSaveDataset_NullDataset() throws Exception {
		this.datasetService.saveDataset(null);
	}

	@Test (expected = Exception.class)
	public void testSaveDataset_LongName() throws Exception {
		final DatasetUploadDto datasetUploadDto = new DatasetUploadDto();
		datasetUploadDto.setName(RandomStringUtils.random(31));
		datasetUploadDto.setSampleAccessions(new LinkedHashSet<DatasetUploadDto.SampleKey>());
		datasetUploadDto.setMarkers(new ArrayList<String>());
		this.datasetService.saveDataset(datasetUploadDto);
	}


	@Test (expected = Exception.class)
	public void testSaveDataset_DatasetNameExists() throws Exception {
		final DatasetUploadDto datasetUploadDto = new DatasetUploadDto();
		final List<String> markers = new ArrayList<>();
		markers.add("a");
		markers.add("a");
		datasetUploadDto.setName("Dataset");
		datasetUploadDto.setMarkers(markers);
		final Dataset datasetFromDB = new Dataset();
		datasetFromDB.setDatasetName("Dataset");
		Mockito.when(this.datasetDAO.getByName(datasetUploadDto.getName())).thenReturn(datasetFromDB);
		datasetUploadDto.setSampleAccessions(new LinkedHashSet<DatasetUploadDto.SampleKey>());
		this.datasetService.saveDataset(datasetUploadDto);
	}

	@Test (expected = Exception.class)
	public void testSaveDataset_DuplicatedMarkers() throws Exception {
		final DatasetUploadDto datasetUploadDto = new DatasetUploadDto();
		datasetUploadDto.setSampleAccessions(new LinkedHashSet<DatasetUploadDto.SampleKey>());
		final List<String> markers = new ArrayList<>();
		markers.add("a");
		markers.add("a");
		datasetUploadDto.setName("Dataset");
		datasetUploadDto.setMarkers(markers);
		Mockito.when(this.datasetDAO.getByName(datasetUploadDto.getName())).thenReturn(null);
		this.datasetService.saveDataset(datasetUploadDto);
	}


	@Test (expected = Exception.class)
	public void testSaveDataset_InvalidCharValuesSize() throws Exception {
		final DatasetUploadDto datasetUploadDto = new DatasetUploadDto();
		final List<String> markers = new ArrayList<>();
		markers.add("a");
		markers.add("b");
		datasetUploadDto.setName("Dataset");
		datasetUploadDto.setMarkers(markers);
		Mockito.when(this.datasetDAO.getByName(datasetUploadDto.getName())).thenReturn(null);

		final LinkedHashSet sampleAccesionSet = new LinkedHashSet<>();
		final DatasetUploadDto.SampleKey sampleKey1 = new DatasetUploadDto().new SampleKey();
		sampleKey1.setSampleUID("SampleUID1");
		sampleAccesionSet.add(sampleKey1);
		datasetUploadDto.setSampleAccessions(sampleAccesionSet);

		final String[][] charValues = { {"A","B"}, {"C","D"}, {"E", "F"}};
		datasetUploadDto.setCharValues(charValues);

		this.datasetService.saveDataset(datasetUploadDto);
	}

	@Test (expected = Exception.class)
	public void testSaveDataset_NotFoundSample() throws Exception {
		final Set<String> sampleUIDs = new HashSet<>();
		sampleUIDs.add("sampleKey1");
		final Map<String, SampleDTO> sampleDTOMap = new HashMap<>();
		Mockito.when(this.sampleService.getSamplesBySampleUID(sampleUIDs)).thenReturn(sampleDTOMap);

		final DatasetUploadDto datasetUploadDto = new DatasetUploadDto();
		final List<String> markers = new ArrayList<>();
		markers.add("a");
		markers.add("b");
		datasetUploadDto.setName("Dataset");
		datasetUploadDto.setMarkers(markers);

		final LinkedHashSet sampleAccesionSet = new LinkedHashSet<>();
		final DatasetUploadDto.SampleKey sampleKey1 = new DatasetUploadDto().new SampleKey();
		sampleKey1.setSampleUID("SampleUID1");
		sampleAccesionSet.add(sampleKey1);
		datasetUploadDto.setSampleAccessions(sampleAccesionSet);

		Mockito.when(this.datasetDAO.getByName(datasetUploadDto.getName())).thenReturn(null);

		final String[][] charValues = { {"A","B"}};
		datasetUploadDto.setCharValues(charValues);

		this.datasetService.saveDataset(datasetUploadDto);
	}

	@Test (expected = Exception.class)
	public void testSaveDataset_NotFoundMarker() throws Exception {
		final Set<String> sampleUIDs = new HashSet<>();
		sampleUIDs.add("SampleUID1");
		final Map<String, SampleDTO> sampleDTOMap = new HashMap<>();
		sampleDTOMap.put("SampleUID1", new SampleDTO());
		Mockito.when(this.sampleService.getSamplesBySampleUID(sampleUIDs)).thenReturn(sampleDTOMap);
		final DatasetUploadDto datasetUploadDto = new DatasetUploadDto();
		final List<String> markers = new ArrayList<>();
		markers.add("a");
		markers.add("b");
		datasetUploadDto.setName("Dataset");
		datasetUploadDto.setMarkers(markers);
		final LinkedHashSet sampleAccesionSet = new LinkedHashSet<>();
		final DatasetUploadDto.SampleKey sampleKey1 = new DatasetUploadDto().new SampleKey();
		sampleKey1.setSampleUID("SampleUID1");
		sampleAccesionSet.add(sampleKey1);
		datasetUploadDto.setSampleAccessions(sampleAccesionSet);
		Mockito.when(this.datasetDAO.getByName(datasetUploadDto.getName())).thenReturn(null);
		final List<Marker> markersFromDB = new ArrayList<>();
		Mockito.when(this.markerDAO.getByNames(datasetUploadDto.getMarkers(), 0, 0)).thenReturn(markersFromDB);
		final String[][] charValues = { {"A","B"}};
		datasetUploadDto.setCharValues(charValues);
		this.datasetService.saveDataset(datasetUploadDto);
	}

	@Test (expected = NullPointerException.class)
	public void testGetDataset_NullDatasetName() throws Exception{
		this.datasetService.getDataset(null);
	}

	@Test
	public void testGetDataset_DatasetNotExist() throws Exception{
		final String datasetName = "name";
		Mockito.when(this.datasetDAO.getByName(datasetName)).thenReturn(null);
		final DatasetRetrieveDto datasetRetrieveDto = this.datasetService.getDataset(datasetName);
		assertThat(datasetRetrieveDto, is(nullValue()));
	}

	@Test (expected = MiddlewareException.class)
	public void testGetDataset_ExceptionWhenQueryingData() throws Exception{
		final String datasetName = "name";
		final Dataset dataset = new Dataset();
		dataset.setDatasetId(1);
		Mockito.when(this.datasetDAO.getByName(datasetName)).thenReturn(dataset);
		Mockito.when(this.charValuesDAO.getCharValueElementsByDatasetId(1)).thenThrow(MiddlewareQueryException.class);
		this.datasetService.getDataset(datasetName);
	}

}
