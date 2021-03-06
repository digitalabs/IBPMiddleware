package org.generationcp.middleware.api.file;

import java.util.Date;
import java.util.Map;

public class FileMetadataDTO {

	private Integer fileId;
	private String fileUUID;
	private String name;
	private String description;
	private String path;
	private String url;
	private String copyright;
	private Integer size;
	private Integer imageHeight;
	private Integer imageWidth;
	private Map<String, Object> imageLocation;
	private String mimeType;
	private Date fileTimestamp;
	private String observationUnitId;
	private Integer ndExperimentId;

	public Integer getFileId() {
		return this.fileId;
	}

	public void setFileId(final Integer fileId) {
		this.fileId = fileId;
	}

	public String getFileUUID() {
		return this.fileUUID;
	}

	public void setFileUUID(final String fileUUID) {
		this.fileUUID = fileUUID;
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public String getPath() {
		return this.path;
	}

	public void setPath(final String path) {
		this.path = path;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(final String url) {
		this.url = url;
	}

	public String getCopyright() {
		return this.copyright;
	}

	public void setCopyright(final String copyright) {
		this.copyright = copyright;
	}

	public Integer getSize() {
		return this.size;
	}

	public void setSize(final Integer size) {
		this.size = size;
	}

	public Integer getImageHeight() {
		return this.imageHeight;
	}

	public void setImageHeight(final Integer imageHeight) {
		this.imageHeight = imageHeight;
	}

	public Integer getImageWidth() {
		return this.imageWidth;
	}

	public void setImageWidth(final Integer imageWidth) {
		this.imageWidth = imageWidth;
	}

	public Map<String, Object> getImageLocation() {
		return this.imageLocation;
	}

	public void setImageLocation(final Map<String, Object> imageLocation) {
		this.imageLocation = imageLocation;
	}

	public String getMimeType() {
		return this.mimeType;
	}

	public void setMimeType(final String mimeType) {
		this.mimeType = mimeType;
	}

	public Date getFileTimestamp() {
		return this.fileTimestamp;
	}

	public void setFileTimestamp(final Date fileTimestamp) {
		this.fileTimestamp = fileTimestamp;
	}

	public String getObservationUnitId() {
		return this.observationUnitId;
	}

	public void setObservationUnitId(final String observationUnitId) {
		this.observationUnitId = observationUnitId;
	}

	public Integer getNdExperimentId() {
		return this.ndExperimentId;
	}

	public void setNdExperimentId(final Integer ndExperimentId) {
		this.ndExperimentId = ndExperimentId;
	}
}
