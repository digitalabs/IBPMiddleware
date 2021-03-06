package org.generationcp.middleware.api.file;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import org.generationcp.middleware.api.brapi.v1.image.Image;
import org.generationcp.middleware.api.brapi.v1.image.ImageNewRequest;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareRequestException;
import org.generationcp.middleware.pojos.dms.ExperimentModel;
import org.generationcp.middleware.pojos.file.FileMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FileMetadataMapper {

	private static final Logger LOG = LoggerFactory.getLogger(FileMetadataMapper.class);

	public void map(final ImageNewRequest from, final FileMetadata to) {
		to.setCopyright(from.getCopyright());
		to.setName(from.getImageName());
		to.setDescription(from.getDescription());
		to.setFileTimestamp(from.getImageTimeStamp());
		to.setImageHeight(from.getImageHeight());
		to.setImageWidth(from.getImageWidth());
		to.setMimeType(from.getMimeType());
		to.setSize(from.getImageFileSize());

		final ObjectMapper objectMapper = new ObjectMapper();
		try {
			final Map<String, Object> imageLocation = from.getImageLocation();
			if (imageLocation != null) {
				to.setImageLocation(objectMapper.writeValueAsString(imageLocation));
			}
		} catch (final JsonProcessingException e) {
			throw new MiddlewareRequestException("", "filemetadata.brapi.location.parse.error");
		}
	}

	public Image map(final FileMetadata fileMetadata) {
		final Image image = new Image();
		image.setCopyright(fileMetadata.getCopyright());
		image.setImageName(fileMetadata.getName());
		image.setDescription(fileMetadata.getDescription());
		image.setImageTimeStamp(fileMetadata.getFileTimestamp());
		image.setImageHeight(fileMetadata.getImageHeight());
		image.setImageWidth(fileMetadata.getImageWidth());
		image.setMimeType(fileMetadata.getMimeType());
		image.setImageFileSize(fileMetadata.getSize());
		final ExperimentModel experimentModel = fileMetadata.getExperimentModel();
		Preconditions.checkNotNull(experimentModel, "Image is not linked to any observationUnit");
		image.setObservationUnitDbId(experimentModel.getObsUnitId());

		final ObjectMapper objectMapper = new ObjectMapper();
		try {
			image.setImageLocation(objectMapper.readValue(fileMetadata.getImageLocation(), HashMap.class));
		} catch (final IOException e) {
			throw new MiddlewareRequestException("", "filemetadata.brapi.location.parse.error");
		}

		image.setImageDbId(fileMetadata.getFileUUID());
		return image;
	}

	public FileMetadataDTO mapToDTO(final FileMetadata fileMetadata) {
		final FileMetadataDTO fileMetadataDTO = new FileMetadataDTO();
		fileMetadataDTO.setCopyright(fileMetadata.getCopyright());
		fileMetadataDTO.setName(fileMetadata.getName());
		fileMetadataDTO.setDescription(fileMetadata.getDescription());
		fileMetadataDTO.setFileTimestamp(fileMetadata.getFileTimestamp());
		fileMetadataDTO.setImageHeight(fileMetadata.getImageHeight());
		fileMetadataDTO.setImageWidth(fileMetadata.getImageWidth());
		fileMetadataDTO.setMimeType(fileMetadata.getMimeType());
		fileMetadataDTO.setPath(fileMetadata.getPath());
		fileMetadataDTO.setSize(fileMetadata.getSize());
		final ExperimentModel experimentModel = fileMetadata.getExperimentModel();
		if (experimentModel != null) {
			fileMetadataDTO.setObservationUnitId(experimentModel.getObsUnitId());
			fileMetadataDTO.setNdExperimentId(experimentModel.getNdExperimentId());
		}

		final ObjectMapper objectMapper = new ObjectMapper();
		try {
			fileMetadataDTO.setImageLocation(objectMapper.readValue(fileMetadata.getImageLocation(), HashMap.class));
		} catch (final IOException e) {
			throw new MiddlewareRequestException("", "filemetadata.brapi.location.parse.error");
		}

		fileMetadataDTO.setFileUUID(fileMetadata.getFileUUID());
		fileMetadataDTO.setFileId(fileMetadata.getFileId());
		return fileMetadataDTO;
	}

	public Image map(final FileMetadataDTO dto) {
		final Image image = new Image();
		image.setCopyright(dto.getCopyright());
		image.setImageName(dto.getName());
		image.setDescription(dto.getDescription());
		image.setImageTimeStamp(dto.getFileTimestamp());
		image.setImageHeight(dto.getImageHeight());
		image.setImageWidth(dto.getImageWidth());
		image.setMimeType(dto.getMimeType());
		image.setImageFileSize(dto.getSize());
		image.setImageLocation(dto.getImageLocation());
		image.setImageDbId(dto.getFileUUID());
		image.setObservationUnitDbId(dto.getObservationUnitId());
		return image;
	}

	public void map(final FileMetadataDTO from, final FileMetadata to) {
		to.setCopyright(from.getCopyright());
		to.setName(from.getName());
		to.setDescription(from.getDescription());
		to.setFileTimestamp(from.getFileTimestamp());
		to.setImageHeight(from.getImageHeight());
		to.setImageWidth(from.getImageWidth());
		to.setMimeType(from.getMimeType());
		to.setSize(from.getSize());

		final ObjectMapper objectMapper = new ObjectMapper();
		try {
			final Map<String, Object> imageLocation = from.getImageLocation();
			if (imageLocation != null) {
				to.setImageLocation(objectMapper.writeValueAsString(imageLocation));
			}
		} catch (final JsonProcessingException e) {
			throw new MiddlewareRequestException("", "filemetadata.brapi.location.parse.error");
		}
	}
}
