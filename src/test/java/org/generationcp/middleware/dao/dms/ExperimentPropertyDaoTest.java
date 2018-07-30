/*******************************************************************************
 *
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package org.generationcp.middleware.dao.dms;

import org.generationcp.middleware.domain.oms.TermId;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class ExperimentPropertyDaoTest {
	
	@Mock
	private Session mockSession;
	
	@Mock
	private SQLQuery mockQuery;

	private ExperimentPropertyDao dao;

	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);
		
		this.dao = new ExperimentPropertyDao();
		this.dao.setSession(this.mockSession);
		Mockito.when(this.mockSession.createSQLQuery(Matchers.anyString())).thenReturn(this.mockQuery);
		Mockito.when(this.mockQuery.addScalar(Matchers.anyString())).thenReturn(this.mockQuery);
	}

	@Test
	public void testGetFieldMapLabels() throws Exception {
		final int projectId = 112;
		this.dao.getFieldMapLabels(projectId);
		
		final ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(this.mockSession).createSQLQuery(sqlCaptor.capture());
		Assert.assertEquals(getFieldmapLabelsQuery(), sqlCaptor.getValue());
		Mockito.verify(this.mockQuery).setParameter("projectId", projectId);
	}
	
	@Test
	public void testGetAllFieldMapsInBlockByTrialInstanceId_WithBlockId() {
		final int datasetId = 11;
		final int geolocationId = 22;
		final int blockId = 33;
		this.dao.getAllFieldMapsInBlockByTrialInstanceId(datasetId, geolocationId, blockId);
		
		final String expectedSql = getFieldmapsInBlockMainQuery() + " AND blk.value = :blockId  ORDER BY e.nd_experiment_id ASC";
		final ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(this.mockSession).createSQLQuery(sqlCaptor.capture());
		Assert.assertEquals(expectedSql.replace(" ", ""), sqlCaptor.getValue().replace(" ", ""));
		Mockito.verify(this.mockQuery).setParameter("blockId", blockId);
		Mockito.verify(this.mockQuery, Mockito.never()).setParameter("datasetId", datasetId);
		Mockito.verify(this.mockQuery, Mockito.never()).setParameter("geolocationId", geolocationId);
	}
	
	@Test
	public void testGetAllFieldMapsInBlockByTrialInstanceId_WithNullBlockId() {
		final int datasetId = 11;
		final int geolocationId = 22;
		this.dao.getAllFieldMapsInBlockByTrialInstanceId(datasetId, geolocationId, null);
		
		final String expectedSql = getFieldmapsInBlockMainQuery() + 
				" AND blk.value IN (SELECT DISTINCT bval.value FROM nd_geolocationprop bval " +
				" INNER JOIN nd_experiment bexp ON bexp.nd_geolocation_id = bval.nd_geolocation_id " +
				" AND bexp.nd_geolocation_id = :geolocationId " +
				" AND bexp.project_id = :datasetId  WHERE bval.type_id = " + TermId.BLOCK_ID.getId() +
				") ORDER BY e.nd_experiment_id ASC";
		final ArgumentCaptor<String> sqlCaptor = ArgumentCaptor.forClass(String.class);
		Mockito.verify(this.mockSession).createSQLQuery(sqlCaptor.capture());
		Assert.assertEquals(expectedSql.replace(" ", ""), sqlCaptor.getValue().replace(" ", ""));
		Mockito.verify(this.mockQuery, Mockito.never()).setParameter(Matchers.eq("blockId"), Matchers.any());
		Mockito.verify(this.mockQuery).setParameter("datasetId", datasetId);
		Mockito.verify(this.mockQuery).setParameter("geolocationId", geolocationId);
	}
	
	private String getFieldmapsInBlockMainQuery() {
		return " SELECT  p.project_id AS datasetId  , p.name AS datasetName "
		+ " , st.name AS studyName , e.nd_geolocation_id AS geolocationId "
		+ " , site.value AS siteName , siteId.value AS siteId"
		+ " , e.nd_experiment_id AS experimentId , s.uniqueName AS entryNumber "
		+ " , s.name AS germplasmName , epropRep.value AS rep "
		+ " , epropPlot.value AS plotNo , row.value AS row , col.value AS col "
		+ " , blk.value AS blockId , st.project_id AS studyId "
		+ " , geo.description AS trialInstance , s.dbxref_id AS gid "
		+ " , st.start_date as startDate , gpSeason.value as season "
		+ " , epropBlock.value AS blockNo "
		+ " , e.plot_id as plotId "
		+ " FROM nd_geolocationprop blk "
		+ "  INNER JOIN nd_experiment e ON e.nd_geolocation_id = blk.nd_geolocation_id "
		+ "  INNER JOIN nd_geolocation geo ON geo.nd_geolocation_id = e.nd_geolocation_id "
		+ "  INNER JOIN project p ON p.project_id = e.project_id "
		+ "  INNER JOIN project_relationship pr ON pr.subject_project_id = p.project_id "
		+ "     AND pr.type_id = " + TermId.BELONGS_TO_STUDY.getId()
		+ "  INNER JOIN project st ON st.project_id = pr.object_project_id "
		+ "  INNER JOIN stock s ON e.stock_id = s.stock_id "
		+ "  LEFT JOIN nd_experimentprop epropRep ON epropRep.nd_experiment_id = e.nd_experiment_id "
		+ "    AND epropRep.type_id = " + TermId.REP_NO.getId() + " AND epropRep.value <> '' "
		+ "  LEFT JOIN nd_experimentprop epropBlock ON epropBlock.nd_experiment_id = e.nd_experiment_id "
		+ "    AND epropBlock.type_id = " + TermId.BLOCK_NO.getId() + " AND epropBlock.value <> '' "
		+ "  INNER JOIN nd_experimentprop epropPlot ON epropPlot.nd_experiment_id = e.nd_experiment_id "
		+ "    AND epropPlot.type_id IN (" + TermId.PLOT_NO.getId()+ ", "
		+ TermId.PLOT_NNO.getId() + ") AND epropPlot.value <> '' "
		+ "  LEFT JOIN nd_geolocationprop site ON site.nd_geolocation_id = e.nd_geolocation_id "
		+ "    AND site.type_id = " + TermId.TRIAL_LOCATION.getId()
		+ "  LEFT JOIN nd_geolocationprop siteId ON siteId.nd_geolocation_id = e.nd_geolocation_id "
		+ "    AND siteId.type_id = "+ TermId.LOCATION_ID.getId()
		+ "  LEFT JOIN nd_experimentprop row ON row.nd_experiment_id = e.nd_experiment_id "
		+ "    AND row.type_id = "+ TermId.RANGE_NO.getId()
		+ "  LEFT JOIN nd_experimentprop col ON col.nd_experiment_id = e.nd_experiment_id "
		+ "    AND col.type_id = "+ TermId.COLUMN_NO.getId()
		+ "  LEFT JOIN nd_geolocationprop gpSeason ON geo.nd_geolocation_id = gpSeason.nd_geolocation_id "
		+ "     AND gpSeason.type_id =  "+ TermId.SEASON_VAR.getId() + " "
		+ " WHERE blk.type_id = "+ TermId.BLOCK_ID.getId();
	}
	
	private String getFieldmapLabelsQuery() {
		return " SELECT " +
				" nde.project_id AS datasetId " +
				" , proj.name AS datasetName " +
				" , geo.nd_geolocation_id AS geolocationId " +
				" , site.value AS siteName " +
				" , nde.nd_experiment_id AS experimentId " +
				" , s.uniqueName AS entryNumber " +
				" , s.name AS germplasmName " +
				" , epropRep.value AS rep " +
				" , epropPlot.value AS plotNo " +
				" , row.value AS row " +
				" , col.value AS col " +
				" , blk.value AS block_id " +
				" , inst.description AS trialInstance " +
                //Casting inst.description to signed for natural sort
                " , CAST(inst.description as SIGNED) AS casted_trialInstance" +
				" , st.name AS studyName " +
				" , s.dbxref_id AS gid " +
				" , st.start_date as startDate " +
				" , gpSeason.value as season " +
				" , siteId.value AS siteId" +
				" , epropBlock.value AS blockNo " +
				" , ldp.group_name AS pedigree " +
				" , geo.plot_id as plotId " +
				" FROM " +
				" nd_experiment nde " +
				" INNER JOIN project_relationship pr ON pr.object_project_id = :projectId AND pr.type_id = " +
				TermId.BELONGS_TO_STUDY.getId() +
				"       AND nde.project_id = pr.subject_project_id " +
				" INNER JOIN project st ON st.project_id = pr.object_project_id " +
				" INNER JOIN stock s ON s.stock_id = nde.stock_id " +
				" LEFT JOIN nd_experimentprop epropRep ON nde.nd_experiment_id = epropRep.nd_experiment_id " +
				"       AND epropRep.type_id =  " + TermId.REP_NO.getId()
						+ "  AND nde.project_id = pr.subject_project_id " +
				"       AND epropRep.value IS NOT NULL  AND epropRep.value <> '' " +
				" LEFT JOIN nd_experimentprop epropBlock ON nde.nd_experiment_id = epropBlock.nd_experiment_id " +
				"       AND epropBlock.type_id =  " + TermId.BLOCK_NO.getId()
						+ "  AND nde.project_id = pr.subject_project_id " +
				"       AND epropBlock.value IS NOT NULL  AND epropBlock.value <> '' " +
				" INNER JOIN nd_experimentprop epropPlot ON nde.nd_experiment_id = epropPlot.nd_experiment_id " +
				"       AND epropPlot.type_id IN (" + TermId.PLOT_NO.getId() + ", " + TermId.PLOT_NNO.getId() + ")  " +
				"       AND nde.project_id = pr.subject_project_id " +
				"       AND epropPlot.value IS NOT NULL  AND epropPlot.value <> '' " +
				" INNER JOIN nd_experiment geo ON nde.nd_experiment_id = geo.nd_experiment_id " +
				"       AND geo.type_id = " +TermId.PLOT_EXPERIMENT.getId() +
				" INNER JOIN nd_geolocation inst ON geo.nd_geolocation_id = inst.nd_geolocation_id " +
				" LEFT JOIN nd_geolocationprop site ON geo.nd_geolocation_id = site.nd_geolocation_id " +
				"       AND site.type_id = " +TermId.TRIAL_LOCATION.getId() +
				"  LEFT JOIN nd_geolocationprop siteId ON siteId.nd_geolocation_id = geo.nd_geolocation_id " +
				"    AND siteId.type_id = " +TermId.LOCATION_ID.getId() +
				" LEFT JOIN nd_geolocationprop blk ON blk.nd_geolocation_id = geo.nd_geolocation_id " +
				"       AND blk.type_id = " +TermId.BLOCK_ID.getId() +
				" INNER JOIN project proj on proj.project_id = nde.project_id " +
				" LEFT JOIN nd_experimentprop row ON row.nd_experiment_id = nde.nd_experiment_id " +
				"       AND row.type_id = " +TermId.RANGE_NO.getId() +
				" LEFT JOIN nd_experimentprop col ON col.nd_experiment_id = nde.nd_experiment_id " +
				"       AND col.type_id = " +TermId.COLUMN_NO.getId() +
				" LEFT JOIN nd_geolocationprop gpSeason ON geo.nd_geolocation_id = gpSeason.nd_geolocation_id " +
				"       AND gpSeason.type_id =  " +TermId.SEASON_VAR.getId() + " " +
				" LEFT JOIN listnms lnms ON lnms.projectid = pr.object_project_id AND lnms.listtype in ('STUDY')" +
				" LEFT JOIN listdata_project ldp on ldp.list_id = lnms.listid AND ldp.entry_id = s.uniqueName AND ldp.germplasm_id  = s.dbxref_id" +
				" ORDER BY casted_trialInstance, inst.description, nde.nd_experiment_id ASC";
	}
}
