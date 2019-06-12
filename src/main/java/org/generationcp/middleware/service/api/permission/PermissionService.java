package org.generationcp.middleware.service.api.permission;

import org.generationcp.middleware.domain.workbench.PermissionDto;

import java.util.List;

public interface PermissionService {

	List<PermissionDto> getPermissionLinks(final Integer userId, final String cropName, final Integer programId);

	/**
	 * Close the sessionProvider
	 */
	void close();

}
