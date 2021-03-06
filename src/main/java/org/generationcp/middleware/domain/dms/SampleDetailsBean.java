package org.generationcp.middleware.domain.dms;

import java.util.Date;

public class SampleDetailsBean {

	public SampleDetailsBean() {

	}

	private Integer sampleId;
	private Integer entryNumber;
	private Integer gid;
	private String designation;
	private Integer sampleNumber;
	private String sampleName;
	private String sampleBusinessKey;
	private Integer takenBy;
	private String sampleList;
	private Date samplingDate;
	private String plateId;
	private String well;
	private String datasetTypeName;
	private boolean subObservationDatasetType;
	private String studyName;
	private Integer studyId;
	private Integer observationUnitNo;
	private String plotNo;
	private String observationUnitId;
	private Integer gdmsDatasetId;
	private String gdmsDatasetName;

	public Integer getTakenBy() {
		return this.takenBy;
	}

	public void setTakenBy(final Integer takenBy) {
		this.takenBy = takenBy;
	}

	public Integer getSampleId() {
		return this.sampleId;
	}

	public void setSampleId(final Integer sampleId) {
		this.sampleId = sampleId;
	}

	public Integer getGid() {
		return this.gid;
	}

	public void setGid(final Integer gid) {
		this.gid = gid;
	}

	public String getDesignation() {
		return this.designation;
	}

	public void setDesignation(final String designation) {
		this.designation = designation;
	}

	public String getSampleName() {
		return this.sampleName;
	}

	public void setSampleName(final String sampleName) {
		this.sampleName = sampleName;
	}

	public String getSampleBusinessKey() {
		return this.sampleBusinessKey;
	}

	public void setSampleBusinessKey(final String sampleBusinessKey) {
		this.sampleBusinessKey = sampleBusinessKey;
	}

	public String getSampleList() {
		return this.sampleList;
	}

	public void setSampleList(final String sampleList) {
		this.sampleList = sampleList;
	}

	public Date getSamplingDate() {
		return this.samplingDate;
	}

	public void setSamplingDate(final Date samplingDate) {
		this.samplingDate = samplingDate;
	}

	public String getPlateId() {
		return this.plateId;
	}

	public void setPlateId(final String plateId) {
		this.plateId = plateId;
	}

	public String getWell() {
		return this.well;
	}

	public void setWell(final String well) {
		this.well = well;
	}

	public String getDatasetTypeName() {
		return this.datasetTypeName;
	}

	public void setDatasetTypeName(final String datasetTypeName) {
		this.datasetTypeName = datasetTypeName;
	}

	public Integer getObservationUnitNo() {
		return this.observationUnitNo;
	}

	public void setObservationUnitNo(final Integer observationUnitNo) {
		this.observationUnitNo = observationUnitNo;
	}

	public String getPlotNo() {
		return this.plotNo;
	}

	public void setPlotNo(final String plotNo) {
		this.plotNo = plotNo;
	}

	public String getStudyName() {
		return studyName;
	}

	public void setStudyName(final String studyName) {
		this.studyName = studyName;
	}

	public Integer getStudyId() {
		return studyId;
	}

	public void setStudyId(final Integer studyId) {
		this.studyId = studyId;
	}

	public String getObservationUnitId() {
		return this.observationUnitId;
	}

	public void setObservationUnitId(final String observationUnitId) {
		this.observationUnitId = observationUnitId;
	}

	public Integer getGdmsDatasetId() {
		return this.gdmsDatasetId;
	}

	public void setGdmsDatasetId(final Integer gdmsDatasetId) {
		this.gdmsDatasetId = gdmsDatasetId;
	}

	public String getGdmsDatasetName() {
		return this.gdmsDatasetName;
	}

	public void setGdmsDatasetName(final String gdmsDatasetName) {
		this.gdmsDatasetName = gdmsDatasetName;
	}

	public Integer getSampleNumber() {
		return this.sampleNumber;
	}

	public void setSampleNumber(final Integer sampleNumber) {
		this.sampleNumber = sampleNumber;
	}

	public Integer getEntryNumber() {
		return this.entryNumber;
	}

	public void setEntryNumber(final Integer entryNumber) {
		this.entryNumber = entryNumber;
	}

	public boolean isSubObservationDatasetType() {
		return this.subObservationDatasetType;
	}

	public void setSubObservationDatasetType(final boolean subObservationDatasetType) {
		this.subObservationDatasetType = subObservationDatasetType;
	}
}
