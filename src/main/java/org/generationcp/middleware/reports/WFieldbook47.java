
package org.generationcp.middleware.reports;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.pojos.report.GermplasmEntry;
import org.generationcp.middleware.pojos.report.Occurrence;

public class WFieldbook47 extends AbstractWheatTrialReporter {

	/**
	 * Enforces obtaining instances through the Factory
	 */
	protected WFieldbook47() {
		this.setParentInfoRequired(true);
	}

	@Override
	public Reporter createReporter() {
		Reporter r = new WFieldbook47();
		r.setFileNameExpression("CC_NalMov_byEntry_{tid}");
		return r;
	}

	@Override
	public String getReportCode() {
		return "WFb47";
	}

	@Override
	public String getTemplateName() {
		return "WFb47_header.jasper";
	}

	@SuppressWarnings("unchecked")
	@Override
	public JRDataSource buildJRDataSource(Collection<?> args) {

		List<GermplasmEntry> entries = new ArrayList<>();

		// this null record is added because in Jasper, the record pointer in the data source is incremented by every element that receives
		// it.
		// since the datasource used in entry, is previously passed from occ to entry subreport.
		entries.add(null);

		for (MeasurementRow row : (Collection<MeasurementRow>) args) {
			GermplasmEntry entry = new GermplasmEntry();
			for (MeasurementData dataItem : row.getDataList()) {
				switch (dataItem.getLabel()) {
					case "ENTRY_NO":
						entry.setEntryNum(Integer.valueOf(dataItem.getValue()));
					break;
					case "CROSS":
						entry.setCrossname(dataItem.getValue());
					break;
					case "DESIGNATION":
						entry.setSel_hist(dataItem.getValue());
					break;
					case "f_cross_name":
						entry.setF_cross_name(dataItem.getValue());
					break;
					case "f_selHist":
						entry.setF_sel_hist(dataItem.getValue());
					break;
					case "f_tabbr":
						entry.setF_tabbr(dataItem.getValue());
					break;
					case "f_locycle":
						entry.setFlocycle(dataItem.getValue());
					break;
					case "f_ent":
						entry.setF_ent(Integer.valueOf(dataItem.getValue()));
					break;
					case "f_lid":
						entry.setF_lid(dataItem.getValue());
					break;
					case "m_cross_name":
						entry.setM_cross_name(dataItem.getValue());
					break;
					case "m_selHist":
						entry.setM_sel_hist(dataItem.getValue());
					break;
					case "m_tabbr":
						entry.setM_tabbr(dataItem.getValue());
					break;
					case "m_locycle":
						entry.setMlocycle(dataItem.getValue());
					break;
					case "m_ent":
						entry.setM_ent(Integer.valueOf(dataItem.getValue()));
					break;
					case "m_lid":
						entry.setM_lid(dataItem.getValue());
						break;

					// TODO: pending mappings
					default:
						entry.setRep1(0);
						entry.setRep2(0);
						entry.setRep3(0);
						entry.setRep4(0);
				}
			}

			entries.add(entry);
		}

		JRDataSource dataSource = new JRBeanCollectionDataSource(Arrays.asList(new Occurrence(entries)));
		return dataSource;
	}
}
