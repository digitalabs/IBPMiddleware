package org.generationcp.middleware.api.brapi.v2.trial;

public class DatasetAuthorship {

	private String datasetPUI;
	private String license;
	private String publicReleaseDate;
	private String submissionDate;

	public DatasetAuthorship() {

	}

	public String getDatasetPUI() {
		return this.datasetPUI;
	}

	public void setDatasetPUI(final String datasetPUI) {
		this.datasetPUI = datasetPUI;
	}

	public String getLicense() {
		return this.license;
	}

	public void setLicense(final String license) {
		this.license = license;
	}

	public String getPublicReleaseDate() {
		return this.publicReleaseDate;
	}

	public void setPublicReleaseDate(final String publicReleaseDate) {
		this.publicReleaseDate = publicReleaseDate;
	}

	public String getSubmissionDate() {
		return this.submissionDate;
	}

	public void setSubmissionDate(final String submissionDate) {
		this.submissionDate = submissionDate;
	}

}
