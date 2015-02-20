package org.generationcp.middleware.reports;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.etl.MeasurementData;
import org.generationcp.middleware.domain.etl.MeasurementRow;
import org.generationcp.middleware.domain.etl.MeasurementVariable;
import org.generationcp.middleware.pojos.report.GermplasmEntry;
import org.generationcp.middleware.pojos.report.Occurrence;

import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public abstract class AbstractWheatTrialReporter extends AbstractReporter {

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, Object> buildJRParams(Map<String,Object> args){
		Map<String, Object> params = super.buildJRParams(args);
		
		List<MeasurementVariable> studyConditions = (List<MeasurementVariable>)args.get("studyConditions");
		MeasurementRow[] entries = {};

		entries = ((Collection<MeasurementRow>)args.get("dataSource")).toArray(entries);
		
		int firstEntry = Integer.valueOf( entries[0].getMeasurementData("ENTRY_NO").getValue());
		int lastEntry = Integer.valueOf( entries[entries.length-1].getMeasurementData("ENTRY_NO").getValue());
		int offset = firstEntry - 1;
		
		params.put("tid", args.get("studyId"));
		params.put("Ientry", firstEntry);
		params.put("Fentry", lastEntry);
		params.put("offset", offset);
		
		
		for(MeasurementVariable var : studyConditions){
			
			switch(var.getName()){
				case "BreedingProgram" : params.put("program", var.getValue());
					break;
				case "STUDY_NAME" : params.put("trial_abbr", var.getValue());
					break;
				case "STUDY_TITLE" : params.put("trial_name", var.getValue());
					break;
				case "CROP_SEASON" : params.put("cycle", var.getValue());
									 params.put("LoCycle", var.getValue());
					break;
				case "TRIAL_INSTANCE" : params.put("occ", Integer.valueOf(var.getValue()));
					break;
				case "LOCATION_NAME" : params.put("lname", var.getValue());
				    break;
				case "LOCATION_NAME_ID" : params.put("lid", var.getValue());
			    	break;
				case "STUDY_INSTITUTE" : params.put("organization", var.getValue());
			    	break;
				default : 
					params.put("dms_ip", "???");
					params.put("gms_ip", "???");
					break;
			}
		}

		return params;
	}

	@Override
	public JRDataSource buildJRDataSource(Collection<?> args){
				
		List<GermplasmEntry> entries = new ArrayList<>();
		//this null record is added because in Jasper, the record pointer in the data source is incremented by every element that receives it.
		//since the datasource used in entry, is previously passed from occ to entry subreport. 
		entries.add(null);
		
		for(MeasurementRow row : (Collection<MeasurementRow>)args){
			GermplasmEntry entry = new GermplasmEntry();
			for(MeasurementData dataItem : row.getDataList()){
				switch(dataItem.getLabel()){
					case "ENTRY_NO" : entry.setEntryNum(Integer.valueOf(dataItem.getValue()));
						break;
					case "CROSS" : entry.setLinea1(dataItem.getValue());
									 entry.setLinea2(dataItem.getValue());
						break;
					case "DESIGNATION" : entry.setLinea3(dataItem.getValue());
									 entry.setLinea4(dataItem.getValue());
						break;
					case "PLOT_NO" : entry.setPlot(Integer.valueOf(dataItem.getValue()));
					 	break;
					//TODO: pending mappings
					default : entry.setS_ent(-99);
							  entry.setS_tabbr("???");
							  entry.setSlocycle("???");
					
				}
			}
			//delete!!
			String cross = "123/456/789/10/asdsa-rg/43rfdf/ererter/354erg-hjk8-/3/ertert-ert56-y56-56y",
					desig = "56y56y-56y56y56-y56y56y-56y56y-56y-56yerg-fgt5rygh-56gyruyi-o09p89-765hg-4f3-d3";
			entry.setLinea1(cross);
			entry.setLinea2(cross);
			entry.setLinea3(desig);
			entry.setLinea4(desig);
			
			entries.add(entry);
			entries.add(entry);
			entries.add(entry);
			entries.add(entry);
			entries.add(entry);
		}
		
		JRDataSource dataSource = new JRBeanCollectionDataSource(Arrays.asList(new Occurrence(entries)));
		return dataSource;

	}

}
