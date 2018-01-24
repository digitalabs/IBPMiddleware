
package org.generationcp.middleware.service.api;

import org.generationcp.middleware.exceptions.InvalidGermplasmNameSettingException;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.germplasm.GermplasmNameSetting;

public interface GermplasmNamingService {
	
	String getNextNameInSequence(GermplasmNameSetting setting) throws InvalidGermplasmNameSettingException;

	GermplasmGroupNamingResult applyGroupName(Integer gid, GermplasmNameSetting setting, UserDefinedField nameType, Integer userId, Integer locationId);

}
