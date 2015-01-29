/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/
package org.generationcp.middleware.operation.builder;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.DataManagerIntegrationTest;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.dms.Variable;
import org.generationcp.middleware.domain.dms.VariableList;
import org.generationcp.middleware.domain.dms.VariableType;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.etl.WorkbookTest;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.operation.transformer.etl.MeasurementVariableTransformer;
import org.generationcp.middleware.pojos.ErrorCode;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.service.api.FieldbookService;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkbookBuilderTest extends DataManagerIntegrationTest {
    
    private static final Logger LOG = LoggerFactory.getLogger(WorkbookBuilderTest.class);
	
    private static DataImportService dataImportService;
    
    private static FieldbookService fieldbookService;
    
    private static WorkbookBuilder workbookBuilder;
    
    private static MeasurementVariableTransformer measurementVariableTransformer;

    private static StandardVariableBuilder standardVariableBuilder;
    
    private static final int SITE_SOIL_PH = 8270;
    private static final int CRUST = 20310;
    private static final int CUSTOM_VARIATE = 18020;
    
    @BeforeClass
	public static void setUp() throws Exception {
    	HibernateSessionProvider sessionProviderForLocal = 
    			managerFactory.getSessionProviderForLocal();
    	HibernateSessionProvider sessionProviderForCentral = 
    			managerFactory.getSessionProviderForCentral();
    	workbookBuilder = new WorkbookBuilder(sessionProviderForLocal, 
    			sessionProviderForCentral);
    	dataImportService = managerFactory.getDataImportService();
    	fieldbookService = managerFactory.getFieldbookMiddlewareService();
    	measurementVariableTransformer = new MeasurementVariableTransformer(
    			sessionProviderForLocal, sessionProviderForCentral);
    	standardVariableBuilder = new StandardVariableBuilder(sessionProviderForLocal, sessionProviderForCentral);
    }
 
    @Test
    public void testCheckingOfMeasurementDatasetWithError() {
    	try {
    		workbookBuilder.checkMeasurementDataset(null);
    	} catch (MiddlewareQueryException e) {
    		LOG.error(e.getMessage(), e);
    		Assert.assertEquals("Expected code ", ErrorCode.STUDY_FORMAT_INVALID.getCode(), e.getCode());
    	}
    }
    
    @Test
    public void testCheckingOfMeasurementDatasetWithoutError() {
    	boolean hasError = false;
    	try {
    		workbookBuilder.checkMeasurementDataset(1);
    	} catch (MiddlewareQueryException e) {
    		LOG.error(e.getMessage(), e);
    		Assert.fail("Expected no error but got one");
    		hasError = true;
    	}
    	Assert.assertFalse("Expected no error but got one", hasError);
    }
    
    @Test
    public void testGetTrialObservationsForNursery() throws MiddlewareQueryException {
    	WorkbookTest.setTestWorkbook(null);
    	Workbook workbook = WorkbookTest.getTestWorkbook(10, StudyType.N);
    	
    	int id = dataImportService.saveDataset(workbook);
    	
    	Workbook createdWorkbook = fieldbookService.getNurseryDataSet(id);
    	
    	Assert.assertTrue("Expected correct values for constants but did not match with old workbook.", 
    			areConstantsMatch(workbook.getConstants(), createdWorkbook.getConstants()));
    	
    }
	
    private boolean areConstantsMatch(List<MeasurementVariable> constants,
			List<MeasurementVariable> constants2) {
    	if (constants != null && constants2 != null) {
    		for (MeasurementVariable var : constants) {
    			if (!isMactchInNewConstantList(constants2, var.getTermId(), var.getValue())) {
    				return false;
    			}
    		}
    	}
    	return true;
	}

	private boolean isMactchInNewConstantList(List<MeasurementVariable> constants, int termId,
			String value) {
		if (constants != null) {
			for (MeasurementVariable var : constants) {
				if (var.getTermId() == termId) {
					return var.getValue().equals(value);
				}
			}
		}
		return false;
	}

	@Test
    public void testGetTrialObservationsForTrial() throws MiddlewareQueryException {
		WorkbookTest.setTestWorkbook(null);
		Workbook workbook = WorkbookTest.getTestWorkbook(10, StudyType.T);
    	
    	int id = dataImportService.saveDataset(workbook);
    	
    	Workbook createdWorkbook = fieldbookService.getTrialDataSet(id);
    	
    	Assert.assertTrue("Expected correct values for trial observations but did not match with old workbook.", 
    			areConstantsCorrect(createdWorkbook.getConstants(), createdWorkbook.getTrialObservations()));
    }

	private boolean areConstantsCorrect(List<MeasurementVariable> constants, List<MeasurementRow> trialObservations) {
		if (trialObservations != null && constants != null) {
			for (MeasurementRow row : trialObservations) {
				return areConstantsInRow(row.getDataList(), constants); 
			}
		}
		return false;
	}

	private boolean areConstantsInRow(List<MeasurementData> dataList,
			List<MeasurementVariable> constants) {
		for (MeasurementVariable var : constants) {
			for (MeasurementData data : dataList) {
				if (data.getMeasurementVariable().getTermId() == var.getTermId() && !data.getValue().equals("1")) {
					return false;
				}
			}
		}
		return true;
	}
	
	@Test
	public void testPopulateMeasurementData_AllEmptyList() throws MiddlewareQueryException {
		List<MeasurementVariable> measurementVariableList = new ArrayList<MeasurementVariable>();
		VariableList variableList = new VariableList();
		List<MeasurementData> measurementDataList = new ArrayList<MeasurementData>();
		workbookBuilder.populateMeasurementData(
				measurementVariableList, variableList, measurementDataList);
		Assert.assertTrue("Measurement data should be empty",measurementDataList.isEmpty());
	}
	
	@Test
	public void testPopulateMeasurementData_EmptyMeasurementVariableList() throws MiddlewareQueryException {
		List<MeasurementVariable> measurementVariableList = new ArrayList<MeasurementVariable>();
		VariableList variableList = createVariableList(createMeasurementVariableList());
		List<MeasurementData> measurementDataList = new ArrayList<MeasurementData>();
		workbookBuilder.populateMeasurementData(
				measurementVariableList, variableList, measurementDataList);
		Assert.assertTrue("Measurement data should be empty",measurementDataList.isEmpty());
	}
	
	@Test
	public void testPopulateMeasurementData_EmptyVariableList() throws MiddlewareQueryException {
		List<MeasurementVariable> measurementVariableList = createMeasurementVariableList();
		VariableList variableList = new VariableList();
		List<MeasurementData> measurementDataList = new ArrayList<MeasurementData>();
		workbookBuilder.populateMeasurementData(
				measurementVariableList, variableList, measurementDataList);
		Assert.assertFalse("Measurement data should not be empty",measurementDataList.isEmpty());
		for (MeasurementData measurementData : measurementDataList) {
			Assert.assertEquals("Measurement data value should be empty","",measurementData.getValue());
		}
	}
	
	@Test
	public void testPopulateMeasurementData() throws MiddlewareQueryException {
		List<MeasurementVariable> measurementVariableList = createMeasurementVariableList();
		VariableList variableList = createVariableList(measurementVariableList);
		List<MeasurementData> measurementDataList = new ArrayList<MeasurementData>();
		workbookBuilder.populateMeasurementData(
				measurementVariableList, variableList, measurementDataList);
		Assert.assertFalse("Measurement data should not be empty",measurementDataList.isEmpty());
		for (MeasurementData measurementData : measurementDataList) {
			if(TermId.CATEGORICAL_VARIATE.getId() != measurementData.getMeasurementVariable().getStoredIn()) {
				Assert.assertNull("Categorical value id should be null",measurementData.getcValueId());
			} else if(CUSTOM_VARIATE == measurementData.getMeasurementVariable().getTermId()){
				Assert.assertNull("Categorical value id should be null",measurementData.getcValueId());
			} else {
				Assert.assertEquals("Categorical value id should equal to "+
						measurementData.getValue(),
						measurementData.getValue(),
						measurementData.getcValueId());
			}
		}
	}

	private VariableList createVariableList(
			List<MeasurementVariable> measurementVariableList) throws MiddlewareQueryException {
		VariableList variableList = new VariableList();
		int count = 0;
		for (MeasurementVariable measurementVariable : measurementVariableList) {
			count++;
			String value = Integer.toString(count);
			if(TermId.CHARACTER_VARIABLE.getId() == measurementVariable.getDataTypeId().intValue()) {
				value = "CODE_"+value;
			}
			Variable variable = createVariable(measurementVariable,value);
			variableList.add(variable);
			if(count==measurementVariableList.size()) {
				variable.setCustomValue(true);
			}
		}
		return variableList;
	}

	private Variable createVariable(MeasurementVariable measurementVariable,
			String value) throws MiddlewareQueryException {
		Variable variable = new Variable();
		VariableType variableType = createVariableType(measurementVariable);
		variable.setVariableType(variableType);
		variable.setValue(value);
		return variable;
	}
	
	private VariableType createVariableType(MeasurementVariable measurementVariable) 
			throws MiddlewareQueryException {
		VariableType variableType = transformMeasurementVariable(measurementVariable,
				getStandardVariable(measurementVariable.getTermId()));
		return variableType;
	}

	private List<MeasurementVariable> createMeasurementVariableList() throws MiddlewareQueryException {
		List<MeasurementVariable> measurementVariableList = new ArrayList<MeasurementVariable>();
		measurementVariableList.add(getMeasurementVariable(TermId.ENTRY_CODE.getId(),true));
		measurementVariableList.add(getMeasurementVariable(TermId.ENTRY_NO.getId(),true));
		measurementVariableList.add(getMeasurementVariable(TermId.GID.getId(),true));
		measurementVariableList.add(getMeasurementVariable(TermId.REP_NO.getId(),true));
		measurementVariableList.add(getMeasurementVariable(TermId.PLOT_NO.getId(),true));
		measurementVariableList.add(getMeasurementVariable(SITE_SOIL_PH,false));
		measurementVariableList.add(getMeasurementVariable(CRUST,false));
		measurementVariableList.add(getMeasurementVariable(CUSTOM_VARIATE,false));
		return measurementVariableList;
	}

	private MeasurementVariable getMeasurementVariable(int termId, boolean isFactor) throws MiddlewareQueryException {
		return measurementVariableTransformer.transform(
				getStandardVariable(termId), isFactor);
	}
	
	private StandardVariable getStandardVariable(int id) throws MiddlewareQueryException {
		return standardVariableBuilder.create(id);
	}
	
	private VariableType transformMeasurementVariable(
			MeasurementVariable measurementVariable, StandardVariable standardVariable) {
		return new VariableType(
				measurementVariable.getName(), 
				measurementVariable.getDescription(), 
				standardVariable, 0);
	}

}
