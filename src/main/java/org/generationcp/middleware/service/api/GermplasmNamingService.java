
package org.generationcp.middleware.service.api;

import org.generationcp.middleware.pojos.UserDefinedField;

public interface GermplasmNamingService {

	GermplasmGroupNamingResult applyGroupName(Integer gid, String groupName, UserDefinedField nameType, Integer userId, Integer locationId);

}
