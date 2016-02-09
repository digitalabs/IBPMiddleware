
package org.generationcp.middleware.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperPrint;

import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.domain.etl.Workbook;
import org.generationcp.middleware.domain.oms.StudyType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.pojos.Country;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmPedigreeTreeNode;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.reports.AbstractReporter;
import org.generationcp.middleware.reports.BuildReportException;
import org.generationcp.middleware.reports.Reporter;
import org.generationcp.middleware.reports.ReporterFactory;
import org.generationcp.middleware.service.api.ReportService;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class ReportServiceImpl extends Service implements ReportService {

	private final ReporterFactory factory = ReporterFactory.instance();

	public ReportServiceImpl() {
		super();
	}

	public ReportServiceImpl(final HibernateSessionProvider sessionProvider) {
		super(sessionProvider);
	}

	public ReportServiceImpl(final HibernateSessionProvider sessionProvider, final String databaseName) {
		super(sessionProvider, databaseName);

	}

	@Override
	public JasperPrint getPrintReport(final String code, final Integer studyId) throws MiddlewareException, JRException, IOException,
			BuildReportException {

		final Reporter reporter = this.factory.createReporter(code);
		final Map<String, Object> dataBeans = this.extractFieldbookData(studyId, reporter.isParentsInfoRequired());

		return reporter.buildJRPrint(dataBeans);

	}

	@Override
	public Reporter getStreamReport(final String code, final Integer studyId, final String programName, final OutputStream output)
			throws MiddlewareException, JRException, IOException, BuildReportException {

		final Reporter reporter = this.factory.createReporter(code);
		final Map<String, Object> dataBeans = this.extractFieldbookData(studyId, reporter.isParentsInfoRequired());
		dataBeans.put(AbstractReporter.PROGRAM_NAME_ARG_KEY, programName);

		reporter.buildJRPrint(dataBeans);
		reporter.asOutputStream(output);

		return reporter;
	}

    @Override
    public Reporter getStreamGermplasmListReport(String code, Integer germplasmListID, String programName, final OutputStream output)
            throws MiddlewareException, JRException, IOException, BuildReportException {
        final Reporter reporter = this.factory.createReporter(code);
        final Map<String, Object> data = this.extractGermplasmListData(germplasmListID);
        data.put(AbstractReporter.PROGRAM_NAME_ARG_KEY, programName);

        reporter.buildJRPrint(data);
        reporter.asOutputStream(output);

        return reporter;
    }



	/**
	 * Creates a Map containing all information needed to generate a report.
	 * 
	 * @param studyId The study Id to which extract information from.
	 * @return a Map containing information of the study.
	 */
	private Map<String, Object> extractFieldbookData(final Integer studyId, final boolean parentsInfoRequireed) throws MiddlewareException {

		final StudyType studyType = this.getStudyDataManager().getStudyType(studyId);
		final Workbook wb = this.getWorkbookBuilder().create(studyId, studyType);
		final List<MeasurementRow> observations = wb.getObservations();
		final List<MeasurementVariable> studyConditions = this.appendCountryInformation(wb.getConditions());

		if (parentsInfoRequireed) {
			this.appendParentsInformation(studyId, observations);
		}

		final Map<String, Object> dataBeans = new HashMap<>();
		dataBeans.put("studyConditions", studyConditions);
		dataBeans.put("dataSource", observations);
		dataBeans.put("studyObservations", wb.getTrialObservations());
		dataBeans.put("studyId", studyId);

		return dataBeans;
	}

    private Map<String, Object> extractGermplasmListData(Integer germplasmListID) {
        // currently, only a blank map is returned as the current requirements for germplasm reports do not require dynamic data
        Map<String, Object> params = new HashMap<>();
        params.put(AbstractReporter.STUDY_CONDITIONS_KEY, new ArrayList<MeasurementVariable>());
        params.put(AbstractReporter.DATA_SOURCE_KEY, new ArrayList());
        return params;
    }

	protected List<MeasurementVariable> appendCountryInformation(final List<MeasurementVariable> originalConditions) {
		Integer locationId = null;

		for (final MeasurementVariable condition : originalConditions) {
			final TermId term = TermId.getById(condition.getTermId());
			locationId = this.retrieveLocationIdFromCondition(condition, term);
		}

		if (locationId == null) {
			return originalConditions;
		} else {
			final List<MeasurementVariable> variables = new ArrayList<>(originalConditions);

			final Location location = this.getLocationDataManager().getLocationByID(locationId);

			if ((location.getCntryid() != null && location.getCntryid() != 0)) {
				final Country country = this.getCountryDao().getById(location.getCntryid());

				final MeasurementVariable countryInfo = new MeasurementVariable();
				countryInfo.setName(AbstractReporter.COUNTRY_VARIABLE_NAME);
				countryInfo.setValue(country.getIsofull());

				variables.add(countryInfo);
			}

			final MeasurementVariable abbrevInfo = new MeasurementVariable();
			abbrevInfo.setName(AbstractReporter.LOCATION_ABBREV_VARIABLE_NAME);
			abbrevInfo.setValue(location.getLabbr());

			variables.add(abbrevInfo);

			return variables;

		}
	}

	/***
	 * Retrieves the Location ID from condition variable; Returns null if the condition value is an empty string
	 * 
	 * @param locationId
	 * @param condition
	 * @param termId
	 * @return
	 */
	Integer retrieveLocationIdFromCondition(final MeasurementVariable condition, final TermId termId) {
		Integer locationId = null;
		final String conditionValue = condition.getValue().trim();
		if (termId == TermId.LOCATION_ID && conditionValue.length() > 0) {
			locationId = Integer.parseInt(conditionValue);
		}
		return locationId;
	}

	@Override
	public Set<String> getReportKeys() {
		return this.factory.getReportKeys();
	}

	/**
	 * Local method to add information about male and female parents. The information is appended in the form of new {@link MeasurementData}
	 * elements for each {@link MeasurementRow} provided
	 * 
	 * @param studyId the id for the study which the observations belong to.
	 * @param observations List of rows representing entries in a study, in which parent information will be appended
	 */
	private void appendParentsInformation(final Integer studyId, final List<MeasurementRow> observations) throws MiddlewareQueryException {
		// put germNodes extraction
		final Map<Integer, GermplasmPedigreeTreeNode> germNodes = this.getGermplasmDataManager().getDirectParentsForStudy(studyId);

		for (final MeasurementRow row : observations) {
			final int gid = Integer.valueOf(row.getMeasurementDataValue("GID"));
			final GermplasmPedigreeTreeNode germNode = germNodes.get(gid);
			if (germNode != null) {
				final Germplasm germplasm = germNode.getGermplasm();

				if (germplasm.getGrplce() >= 0 & germNode.getLinkedNodes().size() == 2) { // is geneative and has parents
					final Germplasm female = germNode.getLinkedNodes().get(0).getGermplasm();
					final Germplasm male = germNode.getLinkedNodes().get(1).getGermplasm();

					// TODO: pending values for origin of the entries
					row.getDataList().add(new MeasurementData("f_selHist", female.getSelectionHistory()));
					row.getDataList().add(new MeasurementData("f_cross_name", female.getSelectionHistory()));
					row.getDataList().add(new MeasurementData("f_tabbr", "NA")); // put source trial abbreviation
					row.getDataList().add(new MeasurementData("f_locycle", "NA")); // put source trial cycle
					row.getDataList().add(new MeasurementData("f_ent", "-99")); // put source trial entry
					row.getDataList().add(new MeasurementData("f_lid", "-99")); // put source location id

					row.getDataList().add(new MeasurementData("m_selHist", male.getSelectionHistory()));
					row.getDataList().add(new MeasurementData("m_cross_name", male.getSelectionHistory()));
					row.getDataList().add(new MeasurementData("m_tabbr", "NA")); // put source trial abbreviation
					row.getDataList().add(new MeasurementData("m_locycle", "NA")); // put source trial cycle
					row.getDataList().add(new MeasurementData("m_ent", "-99")); // put source trial entry
					row.getDataList().add(new MeasurementData("m_lid", "-99")); // put source location id
				}
			}
		}
	}
}
