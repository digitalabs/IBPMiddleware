
package org.generationcp.middleware.data.initializer;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.generationcp.middleware.domain.dms.PhenotypicType;
import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.StudyDetails;
import org.generationcp.middleware.domain.etl.TreatmentVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.pojos.Location;

public class WorkbookTestDataInitializer {

	private static final int GW100_G_ID = 51496;
	private static final String GRAIN_WEIGHT = "100 grain weight";
	private static final String GW_MEASUREMENT = "100GW measurement";
	private static final String G = "g";
	private static final String GW100_G2 = "GW100_g";
	public static final int DAY_OBS = 8284;
	public static final int ASPERGILLUS_FLAVUSPPB = 20369;
	public static final int ASPERGILLUS_FLAVUS1_5 = 20368;
	public static final String NURSERY_NAME = "Nursery_";
	public static final String TRIAL_NAME = "Trial_";

	// STUDY DETAILS
	public static final String TITLE = "Nursery Workbook";
	public static final String OBJECTIVE = "To evaluate the Population 114";
	public static final String START_DATE = "20130805";
	public static final String END_DATE = "20130805";
	public static final int FOLDER_ID = 1;

	// PROPERTIES
	public static final String PERSON = "PERSON";
	public static final String TRIAL_INSTANCE = "TRIAL";
	public static final String LOCATION = "LOCATION";
	public static final String GERMPLASM_ENTRY = "GERMPLASM ENTRY";
	public static final String GERMPLASM_ID = "GERMPLASM ID";
	public static final String CROSS_HISTORY = "CROSS HISTORY";
	public static final String SEED_SOURCE = "SEED SOURCE";
	public static final String FIELD_PLOT = "FIELD PLOT";
	public static final String REPLICATION = "REPLICATION";
	public static final String REPLICATION_FACTOR = "REPLICATION FACTOR";
	public static final String BLOCKING_FACTOR = "BLOCKING FACTOR";
	public static final String SOIL_ACIDITY = "Soil Acidity";
	public static final String YIELD = "Yield";

	// SCALES
	public static final String DBCV = "DBCV";
	public static final String DBID = "DBID";
	public static final String NUMBER = "NUMBER";
	public static final String PEDIGREE_STRING = "PEDIGREE STRING";
	public static final String NAME = "NAME";
	public static final String NESTED_NUMBER = "NESTED NUMBER";
	public static final String KG_HA = "kg/ha";
	public static final String PH = "ph";
	public static final String SCORE_1_5 = "Score (1-5)";
	public static final String VISUAL_SCORING = "Visual scoring";
	public static final String COMMON_RUST = "Common rust";
	public static final String TEXT = "Text";

	// METHODS
	public static final String ASSIGNED = "ASSIGNED";
	public static final String ENUMERATED = "ENUMERATED";
	public static final String CONDUCTED = "CONDUCTED";
	public static final String SELECTED = "SELECTED";
	public static final String PH_METER = "PH Meter";
	public static final String DRY_AND_WEIGH = "Dry and Weigh";
	public static final String MEASURED = "MEASURED";
	public static final String NUM_VALUES = "NumValues";
	public static final String MIN = "MIN";
	public static final String MAX = "MAX";

	// LABELS
	public static final String STUDY = "STUDY";
	public static final String TRIAL = "TRIAL";
	public static final String ENTRY = "ENTRY";
	public static final String PLOT = "PLOT";
	public static final String PLOT_ID = "PLOT_ID";
	// DATA TYPES
	public static final String CHAR = "C";
	public static final String NUMERIC = "N";

	// FACTORS
	public static final String GID = "GID";
	public static final String DESIG = "DESIG";
	public static final String CROSS = "CROSS";
	public static final String SOURCE = "SOURCE";
	public static final String BLOCK = "BLOCK";
	public static final String REP = "REP";

	// CONSTANTS
	public static final int GRAIN_SIZE_ID = 18110;
	public static final int SOILPH_ID = 8270;
	public static final String GRAIN_SIZE_PROPERTY = "Grain size";
	public static final String DRY_GRAINS = "Weigh 1000 dry grains";
	public static final String GRAIN_SIZE_SCALE = WorkbookTestDataInitializer.G;

	// VARIATES
	public static final String GYLD = "GYLD";
	public static final int GYLD_ID = 18000;
	public static final String CRUST = "CRUST";
	public static final int CRUST_ID = 20310;

	// CONDITIONS
	public static final String PI_NAME = "PI Name";
	public static final String PI_ID = "PI Id";
	public static final String COOPERATOR = "COOPERATOR";
	public static final String COOPERATOR_ID = "COOPERATOR ID";
	public static final String NUMERIC_VALUE = "1";
	public static final String SITE = "Site";
	public static final String SITE_ID = "Site Id";

	public static final Integer LOCATION_ID_1 = 1;
	public static final Integer LOCATION_ID_2 = 2;
	public static final Integer LTYPE = 1;
	public static final Integer NLLP = 1;
	public static final String LNAME = "Location";
	public static final String LABBR = "LOC2";
	public static final Integer SNL3ID = 1;
	public static final Integer SNL2ID = 1;
	public static final Integer SNL1ID = 1;
	public static final Integer CNTRYID = 1;
	public static final Integer LRPLCE = 1;

	public static final String VALID_CRUST_VALUE = "1";
	public static final String VALID_CRUST_CVALUE_ID = "50723";
	public static final String INVALID_CRUST_VALUE_NUMERIC = "11";
	public static final String INVALID_CRUST_VALUE_CHAR = "test";
	public static final String INVALID_CRUST_VALUE_MISSING = "missing";

	public static final int DEFAULT_NO_OF_OBSERVATIONS = 10;
	public static final int EXPT_DESIGN_ID = 8135;
	public static final String EXPERIMENT_DESIGN = "Experimental design";
	public static final String TYPE = "Type";
	public static final int SITE_SOIL_PH_ID = 8270;
	public static final String SITE_SOIL_PH = "SITE_SOIL_PH";
	private static final String PLANT_HEIGHT = "Plant height";
	private static final String ERROR_ESTIMATE = "ERROR ESTIMATE";
	private static final String CM = "cm";
	private static final String LS_MEAN = "LS_MEAN";
	private static final String PLANT_HEIGHT_UNIT_ERRORS_NAME = "PLANT_HEIGHT_UnitErrors";
	private static final String PLANT_HEIGHT_MEAN = "PLANT_HEIGHT_MEAN";
	public static final int PLANT_HEIGHT_UNIT_ERRORS_ID = 18210;
	public static final int PLANT_HEIGHT_MEAN_ID = 18180;
	private static final String CREATED_BY = "1";

	public Workbook createWorkbook(final StudyType studyType) {
		final Workbook workbook = new Workbook();
		final StudyDetails studyDetails = new StudyDetails();
		studyDetails.setStudyType(studyType);
		workbook.setStudyDetails(studyDetails);
		return workbook;
	}

	public static final String[] G_NAMES = {"TIANDOUGOU-9", "KENINKENI-27", "SM114-1A-1-1-1B", "SM114-1A-14-1-1B", "SM114-1A-361-1-1B",
			"SM114-1A-86-1-1B", "SM114-1A-115-1-1B", "SM114-1A-281-1-1B", "SM114-1A-134-1-1B", "SM114-1A-69-1-1B", "SM114-1A-157-1-1B",
			"SM114-1A-179-1-1B", "TIANDOUGOU-9", "SM114-1A-36-1-1B", "SM114-1A-201-1-1B", "SM114-1A-31-1-1B", "SM114-1A-353-1-1B",
			"SM114-1A-26-1-1B", "SM114-1A-125-1-1B", "SM114-1A-384-1-1B"};

	public static Workbook getTestWorkbook() {
		return WorkbookTestDataInitializer.createTestWorkbook(WorkbookTestDataInitializer.DEFAULT_NO_OF_OBSERVATIONS, StudyType.N, null, 1,
				false, false);
	}

	public static Workbook getTestWorkbook(final boolean isForMeansDataset) {
		return WorkbookTestDataInitializer.createTestWorkbook(WorkbookTestDataInitializer.DEFAULT_NO_OF_OBSERVATIONS, StudyType.N, null, 1,
				false, isForMeansDataset);
	}

	public static Workbook getTestWorkbook(final int noOfObservations, final StudyType studyType) {
		return WorkbookTestDataInitializer.createTestWorkbook(noOfObservations, studyType, null, 1, false, false);
	}

	public static Workbook getTestWorkbookWithErrors() {
		return WorkbookTestDataInitializer.createTestWorkbookWithErrors();
	}

	public static Workbook getTestWorkbookForWizard(final String studyName, final int trialNo) {
		return WorkbookTestDataInitializer.createTestWorkbookForWizard(studyName, trialNo);
	}

	public static List<Workbook> getTestWorkbooks(final int noOfTrial, final int noOfObservations) {
		final List<Workbook> workbooks = new ArrayList<>();
		final String studyName = "pheno_t7" + new Random().nextInt(10000);
		for (int i = 1; i <= noOfTrial; i++) {
			workbooks.add(WorkbookTestDataInitializer.createTestWorkbook(noOfObservations, StudyType.T, studyName, i, true, false));
		}
		return workbooks;
	}

	public static Workbook createTestWorkbook(final int noOfObservations, final StudyType studyType, final String studyName,
			final int trialNo, final boolean hasMultipleLocations) {
		return WorkbookTestDataInitializer.createTestWorkbook(noOfObservations, studyType, studyName, trialNo, hasMultipleLocations, false);
	}

	public static Workbook createTestWorkbook(final int noOfObservations, final StudyType studyType, final String studyName,
			final int trialNo, final boolean hasMultipleLocations, final boolean isForMeansDataset) {
		final Workbook workbook = new Workbook();
		WorkbookTestDataInitializer.setDefaultValues(workbook);
		WorkbookTestDataInitializer.createStudyDetails(workbook, studyName, studyType);
		workbook.setConditions(WorkbookTestDataInitializer.createConditions(!hasMultipleLocations, trialNo,
				WorkbookTestDataInitializer.LOCATION_ID_1, studyName));
		WorkbookTestDataInitializer.createFactors(workbook, true, hasMultipleLocations, trialNo);
		WorkbookTestDataInitializer.createConstants(workbook);
		WorkbookTestDataInitializer.createVariates(workbook, isForMeansDataset);
		WorkbookTestDataInitializer.createObservations(workbook, noOfObservations, hasMultipleLocations, trialNo, isForMeansDataset);
		return workbook;
	}

	public static void setTrialObservations(final Workbook workbook) {
		final List<MeasurementRow> trialObservations = new ArrayList<>();
		MeasurementRow row;
		List<MeasurementData> dataList;

		// Create n number of observation rows
		for (int i = 0; i < workbook.getObservations().size(); i++) {
			row = new MeasurementRow();
			dataList = new ArrayList<>();
			dataList.add(WorkbookTestDataInitializer.createMeasurementData(WorkbookTestDataInitializer.TRIAL, String.valueOf(1),
					TermId.TRIAL_INSTANCE_FACTOR.getId(), workbook.getFactors()));
			dataList.add(WorkbookTestDataInitializer.createMeasurementData("SITE", String.valueOf(1), TermId.TRIAL_LOCATION.getId(),
					workbook.getConditions()));
			dataList.add(WorkbookTestDataInitializer.createMeasurementData("SITE ID", String.valueOf(1), TermId.LOCATION_ID.getId(),
					workbook.getConditions()));
			dataList.add(WorkbookTestDataInitializer.createMeasurementData("PLTHT_UnitErrors", String.valueOf(1),
					WorkbookTestDataInitializer.PLANT_HEIGHT_UNIT_ERRORS_ID, workbook.getConditions()));
			row.setDataList(dataList);
			trialObservations.add(row);
		}
		workbook.setTrialObservations(trialObservations);
	}

	private static void setDefaultValues(final Workbook workbook) {
		workbook.setTreatmentFactors(new ArrayList<TreatmentVariable>());
		workbook.setMeasurementDatesetId(2);
	}

	public static Workbook createTestWorkbookWithErrors() {
		final Workbook workbook = new Workbook();

		final String studyName = "workbookWithErrors" + new Random().nextInt(10000);
		WorkbookTestDataInitializer.createStudyDetails(workbook, studyName, StudyType.T);
		workbook.setConditions(WorkbookTestDataInitializer.createConditions(false, 1, WorkbookTestDataInitializer.LOCATION_ID_1));
		WorkbookTestDataInitializer.createFactors(workbook, false, false, 1);
		WorkbookTestDataInitializer.createConstants(workbook);
		WorkbookTestDataInitializer.createVariatesWithDuplicatePSM(workbook);
		WorkbookTestDataInitializer.createObservations(workbook, 10, false, 1, false);

		return workbook;
	}

	public static Workbook createTestWorkbookForWizard(final String studyName, final int trialNo) {
		final Workbook wbook = new Workbook();

		WorkbookTestDataInitializer.createStudyDetails(wbook, studyName, StudyType.T);
		wbook.setConditions(WorkbookTestDataInitializer.createConditions(false, trialNo, WorkbookTestDataInitializer.LOCATION_ID_1));
		WorkbookTestDataInitializer.createFactors(wbook, true, true, trialNo);
		WorkbookTestDataInitializer.createConstants(wbook);
		WorkbookTestDataInitializer.createVariates(wbook, false);
		WorkbookTestDataInitializer.createObservations(wbook, 10, true, trialNo, false);

		return wbook;
	}

	public static void createStudyDetails(final Workbook workbook, final String studyName, final StudyType studyType) {
		final StudyDetails details = new StudyDetails();
		if (studyName != null) {
			// this is used for adding multiple locations to one study
			details.setStudyName(studyName);
		} else {
			details.setStudyName(
					(studyType.equals(StudyType.N) ? WorkbookTestDataInitializer.NURSERY_NAME : WorkbookTestDataInitializer.TRIAL_NAME)
							+ new Random().nextInt(10000));
		}
		details.setDescription(WorkbookTestDataInitializer.TITLE);
		details.setObjective(WorkbookTestDataInitializer.OBJECTIVE);
		details.setStartDate(WorkbookTestDataInitializer.START_DATE);
		details.setEndDate(WorkbookTestDataInitializer.END_DATE);
		details.setParentFolderId(WorkbookTestDataInitializer.FOLDER_ID);
		details.setStudyType(studyType);
		details.setCreatedBy(WorkbookTestDataInitializer.CREATED_BY);
		workbook.setStudyDetails(details);
	}

	public static List<MeasurementVariable> createConditions(final boolean withTrial, final int trialNo, final int locationId) {
		// Create measurement variables and set its dataTypeId
		final List<MeasurementVariable> conditions = new ArrayList<>();

		if (withTrial) {
			conditions.add(WorkbookTestDataInitializer.createTrialInstanceMeasurementVariable(trialNo));
		}

		conditions.add(WorkbookTestDataInitializer.createMeasurementVariable(TermId.PI_NAME.getId(), "PI Name",
				"Name of Principal Investigator", WorkbookTestDataInitializer.DBCV, WorkbookTestDataInitializer.ASSIGNED,
				WorkbookTestDataInitializer.PERSON, WorkbookTestDataInitializer.CHAR, "PI Name Value", WorkbookTestDataInitializer.STUDY,
				TermId.CHARACTER_VARIABLE.getId(), withTrial ? PhenotypicType.TRIAL_ENVIRONMENT : PhenotypicType.STUDY, false));

		conditions.add(WorkbookTestDataInitializer.createMeasurementVariable(TermId.PI_ID.getId(), "PI ID", "ID of Principal Investigator",
				WorkbookTestDataInitializer.DBID, WorkbookTestDataInitializer.ASSIGNED, WorkbookTestDataInitializer.PERSON,
				WorkbookTestDataInitializer.NUMERIC, WorkbookTestDataInitializer.NUMERIC_VALUE, WorkbookTestDataInitializer.STUDY,
				TermId.NUMERIC_VARIABLE.getId(), withTrial ? PhenotypicType.TRIAL_ENVIRONMENT : PhenotypicType.STUDY, false));

		conditions.add(WorkbookTestDataInitializer.createMeasurementVariable(TermId.COOPERATOR.getId(), "COOPERATOR", "COOPERATOR NAME",
				WorkbookTestDataInitializer.DBCV, WorkbookTestDataInitializer.CONDUCTED, WorkbookTestDataInitializer.PERSON,
				WorkbookTestDataInitializer.CHAR, "John Smith", WorkbookTestDataInitializer.TRIAL, TermId.CHARACTER_VARIABLE.getId(),
				withTrial ? PhenotypicType.TRIAL_ENVIRONMENT : PhenotypicType.STUDY, false));

		conditions
				.add(WorkbookTestDataInitializer.createMeasurementVariable(TermId.COOPERATOOR_ID.getId(), "COOPERATOR ID", "COOPERATOR ID",
						WorkbookTestDataInitializer.DBID, WorkbookTestDataInitializer.CONDUCTED, WorkbookTestDataInitializer.PERSON,
						WorkbookTestDataInitializer.NUMERIC, WorkbookTestDataInitializer.NUMERIC_VALUE, WorkbookTestDataInitializer.TRIAL,
						TermId.NUMERIC_VARIABLE.getId(), withTrial ? PhenotypicType.TRIAL_ENVIRONMENT : PhenotypicType.STUDY, false));

		conditions.add(WorkbookTestDataInitializer.createMeasurementVariable(TermId.TRIAL_LOCATION.getId(), "SITE", "TRIAL SITE NAME",
				WorkbookTestDataInitializer.DBCV, WorkbookTestDataInitializer.ASSIGNED, WorkbookTestDataInitializer.LOCATION,
				WorkbookTestDataInitializer.CHAR, "SITE " + trialNo, WorkbookTestDataInitializer.TRIAL, TermId.CHARACTER_VARIABLE.getId(),
				PhenotypicType.TRIAL_ENVIRONMENT, false));

		conditions.add(WorkbookTestDataInitializer.createMeasurementVariable(TermId.LOCATION_ID.getId(), "SITE ID", "TRIAL SITE ID",
				WorkbookTestDataInitializer.DBID, WorkbookTestDataInitializer.ASSIGNED, WorkbookTestDataInitializer.LOCATION,
				WorkbookTestDataInitializer.NUMERIC, String.valueOf(locationId), WorkbookTestDataInitializer.TRIAL,
				TermId.NUMERIC_VARIABLE.getId(), PhenotypicType.TRIAL_ENVIRONMENT, false));

		conditions.add(WorkbookTestDataInitializer.createExperimentalRCBDVariable());

		conditions.add(WorkbookTestDataInitializer.createMeasurementVariable(WorkbookTestDataInitializer.PLANT_HEIGHT_UNIT_ERRORS_ID,
				WorkbookTestDataInitializer.PLANT_HEIGHT_UNIT_ERRORS_NAME, "PLTHT_UnitErrors", WorkbookTestDataInitializer.CM,
				WorkbookTestDataInitializer.ERROR_ESTIMATE, WorkbookTestDataInitializer.PLANT_HEIGHT, WorkbookTestDataInitializer.NUMERIC,
				String.valueOf(trialNo), WorkbookTestDataInitializer.TRIAL, TermId.NUMERIC_VARIABLE.getId(), PhenotypicType.VARIATE, true));

		return conditions;
	}

	public static List<MeasurementVariable> createConditions(final boolean withTrial, final int trialNo, final int locationId,
			final String studyName) {
		final List<MeasurementVariable> conditions = WorkbookTestDataInitializer.createConditions(withTrial, trialNo, locationId);

		return conditions;
	}

	public static MeasurementVariable createExperimentalRCBDVariable() {
		return WorkbookTestDataInitializer.createMeasurementVariable(WorkbookTestDataInitializer.EXPT_DESIGN_ID, "DESIGN",
				"EXPERIMENTAL DESIGN", WorkbookTestDataInitializer.TYPE, WorkbookTestDataInitializer.ASSIGNED,
				WorkbookTestDataInitializer.EXPERIMENT_DESIGN, WorkbookTestDataInitializer.CHAR,
				String.valueOf(TermId.RANDOMIZED_COMPLETE_BLOCK.getId()), WorkbookTestDataInitializer.TRIAL,
				TermId.CHARACTER_VARIABLE.getId(), PhenotypicType.TRIAL_ENVIRONMENT, false);
	}

	public static void createFactors(final Workbook workbook, final boolean withEntry, final boolean withTrial, final int trialNo) {
		// Create measurement variables and set its dataTypeId
		final List<MeasurementVariable> factors = new ArrayList<>();

		if (withTrial) {
			factors.add(WorkbookTestDataInitializer.createTrialInstanceMeasurementVariable(trialNo));
		}

		// Entry Factors
		if (withEntry) {
			factors.add(WorkbookTestDataInitializer.createMeasurementVariable(TermId.ENTRY_NO.getId(), WorkbookTestDataInitializer.ENTRY,
					"The germplasm entry number", WorkbookTestDataInitializer.NUMBER, WorkbookTestDataInitializer.ENUMERATED,
					WorkbookTestDataInitializer.GERMPLASM_ENTRY, WorkbookTestDataInitializer.NUMERIC, WorkbookTestDataInitializer.STUDY,
					WorkbookTestDataInitializer.ENTRY, TermId.NUMERIC_VARIABLE.getId(), PhenotypicType.GERMPLASM, false));
		}

		factors.add(WorkbookTestDataInitializer.createMeasurementVariable(TermId.GID.getId(), WorkbookTestDataInitializer.GID,
				"The GID of the germplasm", WorkbookTestDataInitializer.DBID, WorkbookTestDataInitializer.ASSIGNED,
				WorkbookTestDataInitializer.GERMPLASM_ID, WorkbookTestDataInitializer.NUMERIC, WorkbookTestDataInitializer.NUMERIC_VALUE,
				WorkbookTestDataInitializer.ENTRY, TermId.NUMERIC_VARIABLE.getId(), PhenotypicType.GERMPLASM, false));

		factors.add(WorkbookTestDataInitializer.createMeasurementVariable(TermId.DESIG.getId(), WorkbookTestDataInitializer.DESIG,
				"The name of the germplasm", WorkbookTestDataInitializer.DBCV, WorkbookTestDataInitializer.ASSIGNED,
				WorkbookTestDataInitializer.GERMPLASM_ID, WorkbookTestDataInitializer.CHAR, WorkbookTestDataInitializer.STUDY,
				WorkbookTestDataInitializer.ENTRY, TermId.CHARACTER_VARIABLE.getId(), PhenotypicType.GERMPLASM, false));

		factors.add(WorkbookTestDataInitializer.createMeasurementVariable(TermId.CROSS.getId(), WorkbookTestDataInitializer.CROSS,
				"The pedigree string of the germplasm", WorkbookTestDataInitializer.PEDIGREE_STRING, WorkbookTestDataInitializer.ASSIGNED,
				WorkbookTestDataInitializer.CROSS_HISTORY, WorkbookTestDataInitializer.CHAR, WorkbookTestDataInitializer.STUDY,
				WorkbookTestDataInitializer.ENTRY, TermId.CHARACTER_VARIABLE.getId(), PhenotypicType.GERMPLASM, false));

		factors.add(
				WorkbookTestDataInitializer.createMeasurementVariable(TermId.SEED_SOURCE.getId(), WorkbookTestDataInitializer.SEED_SOURCE,
						"The seed source of the germplasm", WorkbookTestDataInitializer.TEXT, WorkbookTestDataInitializer.SELECTED,
						WorkbookTestDataInitializer.SEED_SOURCE, WorkbookTestDataInitializer.CHAR, WorkbookTestDataInitializer.STUDY,
						WorkbookTestDataInitializer.ENTRY, TermId.CHARACTER_VARIABLE.getId(), PhenotypicType.GERMPLASM, false));

		factors.add(WorkbookTestDataInitializer.createMeasurementVariable(TermId.PLOT_NO.getId(), WorkbookTestDataInitializer.PLOT,
				"Plot number ", WorkbookTestDataInitializer.NESTED_NUMBER, WorkbookTestDataInitializer.ENUMERATED,
				WorkbookTestDataInitializer.FIELD_PLOT, WorkbookTestDataInitializer.NUMERIC, WorkbookTestDataInitializer.NUMERIC_VALUE,
				WorkbookTestDataInitializer.PLOT, TermId.NUMERIC_VARIABLE.getId(), PhenotypicType.TRIAL_DESIGN, false));

		factors.add(WorkbookTestDataInitializer.createMeasurementVariable(TermId.PLOT_ID.getId(), WorkbookTestDataInitializer.PLOT_ID,
			"Field plot id - enumerated (number) ", WorkbookTestDataInitializer.TEXT, WorkbookTestDataInitializer.ASSIGNED,
			WorkbookTestDataInitializer.FIELD_PLOT, WorkbookTestDataInitializer.CHAR, WorkbookTestDataInitializer.STUDY,
			WorkbookTestDataInitializer.PLOT, TermId.CHARACTER_VARIABLE.getId(), PhenotypicType.TRIAL_DESIGN, false));
		// Plot Factors
		factors.add(WorkbookTestDataInitializer.createMeasurementVariable(TermId.BLOCK_NO.getId(), WorkbookTestDataInitializer.BLOCK,
				"INCOMPLETE BLOCK", WorkbookTestDataInitializer.NUMBER, WorkbookTestDataInitializer.ENUMERATED,
				WorkbookTestDataInitializer.BLOCKING_FACTOR, WorkbookTestDataInitializer.NUMERIC, WorkbookTestDataInitializer.NUMERIC_VALUE,
				WorkbookTestDataInitializer.PLOT, TermId.NUMERIC_VARIABLE.getId(), PhenotypicType.TRIAL_DESIGN, false));

		factors.add(WorkbookTestDataInitializer.createMeasurementVariable(TermId.REP_NO.getId(), WorkbookTestDataInitializer.REP,
				WorkbookTestDataInitializer.REPLICATION, WorkbookTestDataInitializer.NUMBER, WorkbookTestDataInitializer.ENUMERATED,
				WorkbookTestDataInitializer.REPLICATION_FACTOR, WorkbookTestDataInitializer.NUMERIC,
				WorkbookTestDataInitializer.NUMERIC_VALUE, WorkbookTestDataInitializer.PLOT, TermId.NUMERIC_VARIABLE.getId(),
				PhenotypicType.TRIAL_DESIGN, false));

		factors.add(WorkbookTestDataInitializer.createMeasurementVariable(WorkbookTestDataInitializer.DAY_OBS, "DAY_OBS",
				WorkbookTestDataInitializer.REPLICATION, WorkbookTestDataInitializer.NUMBER, WorkbookTestDataInitializer.ENUMERATED,
				WorkbookTestDataInitializer.REPLICATION_FACTOR, WorkbookTestDataInitializer.NUMERIC,
				WorkbookTestDataInitializer.NUMERIC_VALUE, WorkbookTestDataInitializer.PLOT, TermId.NUMERIC_VARIABLE.getId(),
				PhenotypicType.TRIAL_DESIGN, false));

		workbook.setFactors(factors);
	}

	public static void createConstants(final Workbook workbook) {
		// Create measurement variables and set its dataTypeId
		final List<MeasurementVariable> constants = new ArrayList<>();
		WorkbookTestDataInitializer.addConstants(constants);
		workbook.setConstants(constants);
	}

	public static void createVariates(final Workbook workbook, final boolean isForMeansDataset) {
		// Create measurement variables and set its dataTypeId
		final List<MeasurementVariable> variates = new ArrayList<>();

		if (isForMeansDataset) {
			final MeasurementVariable measurementVariable =
					WorkbookTestDataInitializer.createMeasurementVariable(WorkbookTestDataInitializer.PLANT_HEIGHT_MEAN_ID,
							WorkbookTestDataInitializer.PLANT_HEIGHT_MEAN, "Plant height - least squares mean  (CM)",
							WorkbookTestDataInitializer.CM, WorkbookTestDataInitializer.LS_MEAN, WorkbookTestDataInitializer.PLANT_HEIGHT,
							WorkbookTestDataInitializer.NUMERIC, WorkbookTestDataInitializer.NUMERIC_VALUE,
							WorkbookTestDataInitializer.PLOT, TermId.NUMERIC_VARIABLE.getId(), PhenotypicType.VARIATE, true);

			variates.add(measurementVariable);
		} else {
			final MeasurementVariable measurementVariable = WorkbookTestDataInitializer.createMeasurementVariable(
					WorkbookTestDataInitializer.GYLD_ID, WorkbookTestDataInitializer.GYLD, "Grain yield -dry and weigh (kg/ha)",
					WorkbookTestDataInitializer.KG_HA, WorkbookTestDataInitializer.DRY_AND_WEIGH, WorkbookTestDataInitializer.YIELD,
					WorkbookTestDataInitializer.NUMERIC, WorkbookTestDataInitializer.NUMERIC_VALUE, WorkbookTestDataInitializer.PLOT,
					TermId.NUMERIC_VARIABLE.getId(), PhenotypicType.VARIATE, false);

			variates.add(measurementVariable);

			final MeasurementVariable siteSoilPh = WorkbookTestDataInitializer.createMeasurementVariable(
					WorkbookTestDataInitializer.SITE_SOIL_PH_ID, WorkbookTestDataInitializer.SITE_SOIL_PH, "Soil acidity - ph meter (pH)",
					WorkbookTestDataInitializer.PH, WorkbookTestDataInitializer.MEASURED, WorkbookTestDataInitializer.SOIL_ACIDITY,
					WorkbookTestDataInitializer.NUMERIC, WorkbookTestDataInitializer.NUMERIC_VALUE, WorkbookTestDataInitializer.STUDY,
					TermId.NUMERIC_VARIABLE.getId(), PhenotypicType.VARIATE, false);
			variates.add(siteSoilPh);

			final MeasurementVariable gW100_g =
					WorkbookTestDataInitializer.createMeasurementVariable(WorkbookTestDataInitializer.GW100_G_ID,
							WorkbookTestDataInitializer.GW100_G2, "Weight of 100 grains randomly selected from the total grains.",
							WorkbookTestDataInitializer.G, WorkbookTestDataInitializer.GW_MEASUREMENT,
							WorkbookTestDataInitializer.GRAIN_WEIGHT, WorkbookTestDataInitializer.NUMERIC, "",
							WorkbookTestDataInitializer.STUDY, TermId.NUMERIC_VARIABLE.getId(), PhenotypicType.VARIATE, false);
			variates.add(gW100_g);
		}
		workbook.setVariates(variates);
	}

	public static void createObservations(final Workbook workbook, final int noOfObservations, final boolean withTrial, final int trialNo,
			final boolean isForMeansDataSet) {
		final List<MeasurementRow> observations = new ArrayList<MeasurementRow>();

		MeasurementRow row;
		List<MeasurementData> dataList;
		final Random random = new Random();
		final DecimalFormat fmt = new DecimalFormat("#.##");

		// Create n number of observation rows
		for (int i = 0; i < noOfObservations; i++) {
			row = new MeasurementRow();
			dataList = new ArrayList<>();
			if (withTrial) {
				dataList.add(WorkbookTestDataInitializer.createMeasurementData(WorkbookTestDataInitializer.TRIAL, String.valueOf(trialNo),
						TermId.TRIAL_INSTANCE_FACTOR.getId(), workbook.getFactors()));
			}

			dataList.add(WorkbookTestDataInitializer.createMeasurementData(WorkbookTestDataInitializer.ENTRY, String.valueOf(i),
					TermId.ENTRY_NO.getId(), workbook.getFactors()));
			dataList.add(WorkbookTestDataInitializer.createMeasurementData(WorkbookTestDataInitializer.GID,
					WorkbookTestDataInitializer.computeGID(i), TermId.GID.getId(), workbook.getFactors()));
			dataList.add(WorkbookTestDataInitializer.createMeasurementData(WorkbookTestDataInitializer.DESIG,
					WorkbookTestDataInitializer.G_NAMES[i], TermId.DESIG.getId(), workbook.getFactors()));
			dataList.add(WorkbookTestDataInitializer.createMeasurementData(WorkbookTestDataInitializer.CROSS, "-", TermId.CROSS.getId(),
					workbook.getFactors()));
			dataList.add(WorkbookTestDataInitializer.createMeasurementData(WorkbookTestDataInitializer.SOURCE, "-",
					TermId.SEED_SOURCE.getId(), workbook.getFactors()));
			dataList.add(WorkbookTestDataInitializer.createMeasurementData(WorkbookTestDataInitializer.PLOT, String.valueOf(i),
					TermId.PLOT_NO.getId(), workbook.getFactors()));
			dataList.add(WorkbookTestDataInitializer.createMeasurementData(WorkbookTestDataInitializer.PLOT_ID, "PLOT010203P"+String.valueOf(i),
				TermId.PLOT_ID.getId(), workbook.getFactors()));
			dataList.add(WorkbookTestDataInitializer.createMeasurementData(WorkbookTestDataInitializer.BLOCK, "", TermId.BLOCK_NO.getId(),
					workbook.getFactors()));
			dataList.add(WorkbookTestDataInitializer.createMeasurementData(WorkbookTestDataInitializer.REP, "", TermId.REP_NO.getId(),
					workbook.getFactors()));
			dataList.add(WorkbookTestDataInitializer.createMeasurementData("DAY_OBS",
					WorkbookTestDataInitializer.randomizeValue(random, fmt, 5000), WorkbookTestDataInitializer.DAY_OBS,
					workbook.getFactors()));
			if (isForMeansDataSet) {
				dataList.add(WorkbookTestDataInitializer.createMeasurementData(WorkbookTestDataInitializer.PLANT_HEIGHT_MEAN,
						WorkbookTestDataInitializer.randomizeValue(random, fmt, 5000), WorkbookTestDataInitializer.PLANT_HEIGHT_MEAN_ID,
						workbook.getVariates()));
			} else {
				dataList.add(WorkbookTestDataInitializer.createMeasurementData(WorkbookTestDataInitializer.GYLD,
						WorkbookTestDataInitializer.randomizeValue(random, fmt, 5000), WorkbookTestDataInitializer.GYLD_ID,
						workbook.getVariates()));
				dataList.add(WorkbookTestDataInitializer.createMeasurementData(WorkbookTestDataInitializer.SITE_SOIL_PH, "1",
						WorkbookTestDataInitializer.SITE_SOIL_PH_ID, workbook.getVariates()));
				dataList.add(WorkbookTestDataInitializer.createMeasurementData(WorkbookTestDataInitializer.GW100_G2,
						WorkbookTestDataInitializer.randomizeValue(random, fmt, 5000), 51496, workbook.getVariates(), true));
			}
			row.setDataList(dataList);
			observations.add(row);
		}

		workbook.setObservations(observations);
	}

	public static MeasurementVariable createMeasurementVariable(final int termId, final String name, final String description,
			final String scale, final String method, final String property, final String dataType, final String value, final String label,
			final int dataTypeId, final PhenotypicType role, final VariableType variableType) {
		final MeasurementVariable variable =
				new MeasurementVariable(termId, name, description, scale, method, property, dataType, value, label);
		variable.setRole(role);
		variable.setDataTypeId(dataTypeId);
		variable.setVariableType(variableType);
		return variable;
	}
	
	public static MeasurementVariable createMeasurementVariable(final int termId, final String name, final String description,
			final String scale, final String method, final String property, final String dataType, final String value, final String label,
			final int dataTypeId, final PhenotypicType role, final boolean isAnalysisVariable) {
		final MeasurementVariable variable =
				new MeasurementVariable(termId, name, description, scale, method, property, dataType, value, label);
		variable.setRole(role);
		variable.setDataTypeId(dataTypeId);
		WorkbookTestDataInitializer.setDefaultVariableType(variable, isAnalysisVariable);
		return variable;
	}

	private static void setDefaultVariableType(final MeasurementVariable variable, final boolean isAnalysisVariable) {
		if (variable.getRole() == PhenotypicType.STUDY) {
			variable.setVariableType(VariableType.STUDY_DETAIL);
		} else if (variable.getRole() == PhenotypicType.TRIAL_ENVIRONMENT) {
			variable.setVariableType(VariableType.ENVIRONMENT_DETAIL);
		} else if (variable.getRole() == PhenotypicType.GERMPLASM) {
			variable.setVariableType(VariableType.GERMPLASM_DESCRIPTOR);
		} else if (variable.getRole() == PhenotypicType.TRIAL_DESIGN) {
			variable.setVariableType(VariableType.EXPERIMENTAL_DESIGN);
		} else if (variable.getRole() == PhenotypicType.VARIATE && isAnalysisVariable) {
			variable.setVariableType(VariableType.ANALYSIS);
		} else if (variable.getRole() == PhenotypicType.VARIATE && !isAnalysisVariable) {
			variable.setVariableType(VariableType.TRAIT);
		}
	}

	public static MeasurementVariable createMeasurementVariable(final Integer termId) {
		final MeasurementVariable var = new MeasurementVariable();
		var.setTermId(termId);
		return var;
	}

	public static MeasurementData createMeasurementData(final String label, final String value, final int termId,
			final List<MeasurementVariable> variables) {
		final MeasurementData data = new MeasurementData(label, value);
		data.setMeasurementVariable(WorkbookTestDataInitializer.getMeasurementVariable(termId, variables));
		return data;
	}

	public static MeasurementData createMeasurementData(final String label, final String value, final int termId,
			final List<MeasurementVariable> variables, final boolean isEditable) {
		final MeasurementData data = new MeasurementData(label, value);
		data.setMeasurementVariable(WorkbookTestDataInitializer.getMeasurementVariable(termId, variables));
		data.setEditable(isEditable);
		return data;
	}

	public static MeasurementData createMeasurementData(final int termId) {
		final MeasurementData data = new MeasurementData();
		final MeasurementVariable measurementVariable = new MeasurementVariable();
		measurementVariable.setTermId(termId);
		data.setMeasurementVariable(measurementVariable);
		return data;
	}

	public static MeasurementVariable createTrialInstanceMeasurementVariable(final int trialNo) {
		return WorkbookTestDataInitializer.createMeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), "TRIAL", "TRIAL NUMBER",
				WorkbookTestDataInitializer.NUMBER, WorkbookTestDataInitializer.ENUMERATED, WorkbookTestDataInitializer.TRIAL_INSTANCE,
				WorkbookTestDataInitializer.NUMERIC, String.valueOf(trialNo), WorkbookTestDataInitializer.TRIAL,
				TermId.CHARACTER_VARIABLE.getId(), PhenotypicType.TRIAL_ENVIRONMENT, false);
	}

	public static String randomizeValue(final Random random, final DecimalFormat fmt, final int base) {
		final double value = random.nextDouble() * base;
		return fmt.format(value);
	}

	public static String computeGID(final int i) {
		int gid = 1000000;
		if (i < 13) {
			gid += i - 1;
		} else if (i > 13) {
			gid += i - 2;
		}
		return String.valueOf(gid);
	}

	public static MeasurementVariable getMeasurementVariable(final int termId, final List<MeasurementVariable> variables) {
		if (variables != null) {
			// get matching MeasurementVariable object given the term id
			for (final MeasurementVariable var : variables) {
				if (var.getTermId() == termId) {
					return var;
				}
			}
		}
		return null;
	}

	private static void createVariatesWithDuplicatePSM(final Workbook workbook) {
		final List<MeasurementVariable> variates = new ArrayList<>();

		final MeasurementVariable gyld =
				WorkbookTestDataInitializer.createMeasurementVariable(WorkbookTestDataInitializer.GYLD_ID, WorkbookTestDataInitializer.GYLD,
						"Grain yield -dry and weigh (kg/ha)", WorkbookTestDataInitializer.KG_HA, WorkbookTestDataInitializer.DRY_AND_WEIGH,
						WorkbookTestDataInitializer.YIELD, WorkbookTestDataInitializer.NUMERIC, WorkbookTestDataInitializer.NUMERIC_VALUE,
						WorkbookTestDataInitializer.PLOT, TermId.NUMERIC_VARIABLE.getId(), PhenotypicType.VARIATE, false);

		variates.add(gyld);

		final MeasurementVariable siteSoilPh = WorkbookTestDataInitializer.createMeasurementVariable(
				WorkbookTestDataInitializer.SITE_SOIL_PH_ID, WorkbookTestDataInitializer.SITE_SOIL_PH, "Soil acidity - ph meter (pH)",
				WorkbookTestDataInitializer.PH, WorkbookTestDataInitializer.MEASURED, WorkbookTestDataInitializer.SOIL_ACIDITY,
				WorkbookTestDataInitializer.NUMERIC, WorkbookTestDataInitializer.NUMERIC_VALUE, WorkbookTestDataInitializer.PLOT,
				TermId.NUMERIC_VARIABLE.getId(), PhenotypicType.VARIATE, false);
		variates.add(siteSoilPh);

		workbook.setVariates(variates);
	}

	public static void addVariatesAndObservations(final Workbook currentWorkbook) {

		final List<MeasurementVariable> variates = currentWorkbook.getVariates();
		final MeasurementVariable measurementVariable = WorkbookTestDataInitializer.createMeasurementVariable(
				WorkbookTestDataInitializer.CRUST_ID, WorkbookTestDataInitializer.CRUST,
				"Score for the severity of common rust, (In highlands and mid altitude, Puccinia sorghi) symptoms rated on a scale from 1 (= clean, no infection) to 5 (= severely diseased).",
				WorkbookTestDataInitializer.SCORE_1_5, WorkbookTestDataInitializer.VISUAL_SCORING, WorkbookTestDataInitializer.COMMON_RUST,
				WorkbookTestDataInitializer.CHAR, null, WorkbookTestDataInitializer.PLOT, TermId.CATEGORICAL_VARIABLE.getId(),
				PhenotypicType.VARIATE, false);

		measurementVariable.setOperation(Operation.ADD);

		variates.add(measurementVariable);

		WorkbookTestDataInitializer.addObservations(currentWorkbook);
	}

	private static void addObservations(final Workbook currentWorkbook) {
		final List<MeasurementRow> observations = currentWorkbook.getObservations();

		MeasurementRow row;
		List<MeasurementData> dataList;
		final Random random = new Random();
		final DecimalFormat fmt = new DecimalFormat("#.##");

		// Create n number of observation rows
		for (int i = 0; i < observations.size(); i++) {
			row = observations.get(i);
			dataList = row.getDataList();

			String crustValue = WorkbookTestDataInitializer.randomizeValue(random, fmt, 5000);
			String crustCValueId = null;
			switch (i) {
				case 0:
					crustValue = "";
					break;
				case 1:
					crustValue = null;
					break;
				case 2:
					crustValue = WorkbookTestDataInitializer.VALID_CRUST_VALUE;
					crustCValueId = WorkbookTestDataInitializer.VALID_CRUST_CVALUE_ID;
					break;
				case 3:
					crustValue = WorkbookTestDataInitializer.INVALID_CRUST_VALUE_NUMERIC;
					break;
				case 4:
					crustValue = WorkbookTestDataInitializer.INVALID_CRUST_VALUE_CHAR;
					break;
				case 5:
					crustValue = WorkbookTestDataInitializer.INVALID_CRUST_VALUE_MISSING;
					break;
			}
			final MeasurementData measurementData = WorkbookTestDataInitializer.createMeasurementData(WorkbookTestDataInitializer.CRUST,
					crustValue, WorkbookTestDataInitializer.CRUST_ID, currentWorkbook.getVariates());
			measurementData.setcValueId(crustCValueId);
			dataList.add(measurementData);
		}
	}

	public static List<Location> createLocationData() {
		final List<Location> locations = new ArrayList<>();
		locations.add(new Location(WorkbookTestDataInitializer.LOCATION_ID_1, WorkbookTestDataInitializer.LTYPE,
				WorkbookTestDataInitializer.NLLP, WorkbookTestDataInitializer.LNAME + " 1", WorkbookTestDataInitializer.LABBR,
				WorkbookTestDataInitializer.SNL3ID, WorkbookTestDataInitializer.SNL2ID, WorkbookTestDataInitializer.SNL1ID,
				WorkbookTestDataInitializer.CNTRYID, WorkbookTestDataInitializer.LRPLCE));
		locations.add(new Location(WorkbookTestDataInitializer.LOCATION_ID_2, WorkbookTestDataInitializer.LTYPE,
				WorkbookTestDataInitializer.NLLP, WorkbookTestDataInitializer.LNAME + " 2", WorkbookTestDataInitializer.LABBR,
				WorkbookTestDataInitializer.SNL3ID, WorkbookTestDataInitializer.SNL2ID, WorkbookTestDataInitializer.SNL1ID,
				WorkbookTestDataInitializer.CNTRYID, WorkbookTestDataInitializer.LRPLCE));
		return locations;
	}

	public static MeasurementRow createTrialObservationWithoutSite() {
		final Workbook workbook = new Workbook();

		WorkbookTestDataInitializer.createStudyDetails(workbook, null, StudyType.T);
		workbook.setConditions(WorkbookTestDataInitializer.createConditions(true, 1, WorkbookTestDataInitializer.LOCATION_ID_1));

		final MeasurementRow row = new MeasurementRow();
		final List<MeasurementData> dataList = new ArrayList<>();

		dataList.add(WorkbookTestDataInitializer.createMeasurementData(WorkbookTestDataInitializer.TRIAL_INSTANCE,
				WorkbookTestDataInitializer.NUMERIC_VALUE, TermId.TRIAL_INSTANCE_FACTOR.getId(), workbook.getConditions()));
		dataList.add(WorkbookTestDataInitializer.createMeasurementData(WorkbookTestDataInitializer.PI_NAME, "", TermId.PI_NAME.getId(),
				workbook.getConditions()));
		dataList.add(WorkbookTestDataInitializer.createMeasurementData(WorkbookTestDataInitializer.PI_ID, "", TermId.PI_ID.getId(),
				workbook.getConditions()));
		dataList.add(WorkbookTestDataInitializer.createMeasurementData(WorkbookTestDataInitializer.COOPERATOR, "",
				TermId.COOPERATOR.getId(), workbook.getConditions()));
		dataList.add(WorkbookTestDataInitializer.createMeasurementData(WorkbookTestDataInitializer.COOPERATOR_ID, "",
				TermId.COOPERATOOR_ID.getId(), workbook.getConditions()));

		row.setDataList(dataList);
		return row;
	}

	public static Workbook addEnvironmentAndConstantVariables(final Workbook createdWorkbook) {
		WorkbookTestDataInitializer.addConditions(createdWorkbook.getConditions());
		WorkbookTestDataInitializer.addConstants(createdWorkbook.getConstants());
		return createdWorkbook;
	}

	private static void addConstants(final List<MeasurementVariable> constants) {
		final MeasurementVariable variable = WorkbookTestDataInitializer.createMeasurementVariable(WorkbookTestDataInitializer.SOILPH_ID,
				"SITE_SOIL_PH", "Soil acidity - ph meter (pH)", WorkbookTestDataInitializer.PH, WorkbookTestDataInitializer.PH_METER,
				WorkbookTestDataInitializer.SOIL_ACIDITY, WorkbookTestDataInitializer.NUMERIC, WorkbookTestDataInitializer.NUMERIC_VALUE,
				WorkbookTestDataInitializer.TRIAL, TermId.NUMERIC_VARIABLE.getId(), PhenotypicType.VARIATE, false);
		variable.setOperation(Operation.ADD);
		// Add "Analysis Summary" variables
		constants.add(WorkbookTestDataInitializer.createMeasurementVariable(
				WorkbookTestDataInitializer.GYLD_ID, WorkbookTestDataInitializer.GYLD + "_" + WorkbookTestDataInitializer.NUM_VALUES, "Grain yield -dry and weigh (kg/ha)",
				WorkbookTestDataInitializer.KG_HA, WorkbookTestDataInitializer.NUM_VALUES, WorkbookTestDataInitializer.YIELD,
				WorkbookTestDataInitializer.NUMERIC, WorkbookTestDataInitializer.NUMERIC_VALUE, WorkbookTestDataInitializer.PLOT,
				TermId.NUMERIC_VARIABLE.getId(), PhenotypicType.VARIATE, VariableType.ANALYSIS_SUMMARY));
		constants.add(WorkbookTestDataInitializer.createMeasurementVariable(
				WorkbookTestDataInitializer.GYLD_ID, WorkbookTestDataInitializer.GYLD + "_" + WorkbookTestDataInitializer.MIN, "Grain yield -dry and weigh (kg/ha)",
				WorkbookTestDataInitializer.KG_HA, WorkbookTestDataInitializer.MIN, WorkbookTestDataInitializer.YIELD,
				WorkbookTestDataInitializer.NUMERIC, WorkbookTestDataInitializer.NUMERIC_VALUE, WorkbookTestDataInitializer.PLOT,
				TermId.NUMERIC_VARIABLE.getId(), PhenotypicType.VARIATE, VariableType.ANALYSIS_SUMMARY));
		constants.add(WorkbookTestDataInitializer.createMeasurementVariable(
				WorkbookTestDataInitializer.GYLD_ID, WorkbookTestDataInitializer.GYLD + "_" + WorkbookTestDataInitializer.MAX, "Grain yield -dry and weigh (kg/ha)",
				WorkbookTestDataInitializer.KG_HA, WorkbookTestDataInitializer.MAX, WorkbookTestDataInitializer.YIELD,
				WorkbookTestDataInitializer.NUMERIC, WorkbookTestDataInitializer.NUMERIC_VALUE, WorkbookTestDataInitializer.PLOT,
				TermId.NUMERIC_VARIABLE.getId(), PhenotypicType.VARIATE, VariableType.ANALYSIS_SUMMARY));
		constants.add(variable);
	}

	private static void addConditions(final List<MeasurementVariable> conditions) {
		MeasurementVariable variable = WorkbookTestDataInitializer.createMeasurementVariable(TermId.TRIAL_LOCATION.getId(), "SITE",
				"TRIAL SITE NAME", WorkbookTestDataInitializer.DBCV, WorkbookTestDataInitializer.ASSIGNED,
				WorkbookTestDataInitializer.LOCATION, WorkbookTestDataInitializer.CHAR, "", WorkbookTestDataInitializer.TRIAL,
				TermId.CHARACTER_VARIABLE.getId(), PhenotypicType.TRIAL_ENVIRONMENT, false);
		variable.setOperation(Operation.ADD);
		conditions.add(variable);

		variable = WorkbookTestDataInitializer.createMeasurementVariable(TermId.LOCATION_ID.getId(), "SITE ID", "TRIAL SITE ID",
				WorkbookTestDataInitializer.DBID, WorkbookTestDataInitializer.ASSIGNED, WorkbookTestDataInitializer.LOCATION,
				WorkbookTestDataInitializer.NUMERIC, WorkbookTestDataInitializer.NUMERIC_VALUE, WorkbookTestDataInitializer.TRIAL,
				TermId.NUMERIC_VARIABLE.getId(), PhenotypicType.TRIAL_ENVIRONMENT, false);
		variable.setOperation(Operation.ADD);
		conditions.add(variable);
	}

	public static boolean areTrialVariablesSame(final List<MeasurementVariable> trialVariablesTestData,
			final List<MeasurementVariable> trialVariablesRetrieved) {

		if (trialVariablesTestData != null && trialVariablesRetrieved != null) {
			for (final MeasurementVariable var : trialVariablesTestData) {
				if (WorkbookTestDataInitializer.notInRetrievedList(var.getTermId(), trialVariablesRetrieved)) {
					return false;
				}
			}
		}
		return true;
	}

	private static boolean notInRetrievedList(final int termId, final List<MeasurementVariable> trialVariablesRetrieved) {
		for (final MeasurementVariable var : trialVariablesRetrieved) {
			if (termId == var.getTermId()) {
				return false;
			}
		}
		return true;
	}

	public static void addNewEnvironment(final Workbook createdWorkbook) {
		WorkbookTestDataInitializer.addObservations(1, createdWorkbook.getTrialObservations());
		WorkbookTestDataInitializer.addObservations(1, createdWorkbook.getObservations());
	}

	private static void addObservations(final int newEnvironmentCount, final List<MeasurementRow> observations) {
		final List<MeasurementRow> originalObservations =
				new ArrayList<MeasurementRow>(WorkbookTestDataInitializer.getFirstTrialInstance(observations));
		int currentObsCount = observations.size() / originalObservations.size();

		for (int i = 0; i < newEnvironmentCount; i++) {
			final List<MeasurementRow> newInstance = new ArrayList<>();
			for (final MeasurementRow row : originalObservations) {
				newInstance.add(new MeasurementRow(row));
			}
			currentObsCount++;
			observations.addAll(WorkbookTestDataInitializer.setValuesPerInstance(newInstance, currentObsCount));
		}

		for (final MeasurementRow row : observations) {
			for (final MeasurementData data : row.getDataList()) {
				if (data.getMeasurementVariable().getTermId() == WorkbookTestDataInitializer.ASPERGILLUS_FLAVUS1_5
						|| data.getMeasurementVariable().getTermId() == WorkbookTestDataInitializer.ASPERGILLUS_FLAVUSPPB) {
					data.setValue(String.valueOf(new Random().nextInt(10000)));
				}
			}
		}
	}

	public static List<MeasurementRow> setValuesPerInstance(final List<MeasurementRow> newInstance, final int currentObsCount) {
		for (final MeasurementRow row : newInstance) {
			for (final MeasurementData data : row.getDataList()) {
				if (data.getMeasurementVariable().getTermId() == TermId.TRIAL_INSTANCE_FACTOR.getId()) {
					data.setValue(String.valueOf(currentObsCount));
				}
				data.setPhenotypeId(null);
			}
			row.setExperimentId(0);
			row.setLocationId(0);
			row.setStockId(0);
		}
		return newInstance;
	}

	public static List<MeasurementRow> getFirstTrialInstance(final List<MeasurementRow> observations) {
		final List<MeasurementRow> firstTrialInstance = new ArrayList<>();
		long oldLocationId = 0;
		for (final MeasurementRow row : observations) {
			final long locationId = row.getLocationId();
			if (oldLocationId != locationId && oldLocationId != 0) {
				break;
			}
			firstTrialInstance.add(row);
			oldLocationId = locationId;
		}
		return firstTrialInstance;
	}

	public static void deleteExperimentPropVar(final Workbook createdWorkbook) {
		if (createdWorkbook.getFactors() != null) {
			for (final MeasurementVariable var : createdWorkbook.getFactors()) {
				if (var.getTermId() == WorkbookTestDataInitializer.DAY_OBS) {
					var.setOperation(Operation.DELETE);
				}
			}
		}
	}

	public static void createTrialObservations(final int noOfTrialInstances, final Workbook workbook) {
		final List<MeasurementRow> trialObservations = new ArrayList<MeasurementRow>();

		MeasurementRow row;
		List<MeasurementData> dataList;

		for (int i = 0; i < noOfTrialInstances; i++) {
			row = new MeasurementRow();
			dataList = new ArrayList<>();

			MeasurementData data = new MeasurementData(WorkbookTestDataInitializer.TRIAL_INSTANCE, String.valueOf(i + 1));
			data.setMeasurementVariable(
					WorkbookTestDataInitializer.getMeasurementVariable(TermId.TRIAL_INSTANCE_FACTOR.getId(), workbook.getConditions()));
			dataList.add(data);
			data = new MeasurementData(WorkbookTestDataInitializer.PI_NAME, "");
			data.setMeasurementVariable(
					WorkbookTestDataInitializer.getMeasurementVariable(TermId.PI_NAME.getId(), workbook.getConditions()));
			dataList.add(data);
			data = new MeasurementData(WorkbookTestDataInitializer.PI_ID, "");
			data.setMeasurementVariable(WorkbookTestDataInitializer.getMeasurementVariable(TermId.PI_ID.getId(), workbook.getConditions()));
			dataList.add(data);
			data = new MeasurementData(WorkbookTestDataInitializer.COOPERATOR, "");
			data.setMeasurementVariable(
					WorkbookTestDataInitializer.getMeasurementVariable(TermId.COOPERATOR.getId(), workbook.getConditions()));
			dataList.add(data);
			data = new MeasurementData(WorkbookTestDataInitializer.COOPERATOR_ID, "");
			data.setMeasurementVariable(
					WorkbookTestDataInitializer.getMeasurementVariable(TermId.COOPERATOOR_ID.getId(), workbook.getConditions()));
			dataList.add(data);
			data = new MeasurementData(WorkbookTestDataInitializer.SITE, WorkbookTestDataInitializer.LNAME + "_" + (i + 1));
			data.setMeasurementVariable(
					WorkbookTestDataInitializer.getMeasurementVariable(TermId.TRIAL_LOCATION.getId(), workbook.getConditions()));
			dataList.add(data);
			data = new MeasurementData(WorkbookTestDataInitializer.SITE_ID, String.valueOf(i + 1));
			data.setMeasurementVariable(
					WorkbookTestDataInitializer.getMeasurementVariable(TermId.LOCATION_ID.getId(), workbook.getConditions()));
			dataList.add(data);

			// Check variables
			data = new MeasurementData("CHECK_START", String.valueOf(i + 1));
			data.setMeasurementVariable(
					WorkbookTestDataInitializer.getMeasurementVariable(TermId.CHECK_START.getId(), workbook.getConditions()));
			dataList.add(data);
			data = new MeasurementData("CHECK_INTERVAL", String.valueOf(i + 1));
			data.setMeasurementVariable(
					WorkbookTestDataInitializer.getMeasurementVariable(TermId.CHECK_PLAN.getId(), workbook.getConditions()));
			dataList.add(data);
			data = new MeasurementData("CHECK_PLAN", "1");
			data.setMeasurementVariable(
					WorkbookTestDataInitializer.getMeasurementVariable(TermId.CHECK_INTERVAL.getId(), workbook.getConditions()));
			dataList.add(data);

			row.setDataList(dataList);
			trialObservations.add(row);
		}

		workbook.setTrialObservations(trialObservations);
	}

}
