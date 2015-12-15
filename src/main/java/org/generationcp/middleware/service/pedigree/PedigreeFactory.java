
package org.generationcp.middleware.service.pedigree;

import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.pojos.workbench.CropType.CropEnum;
import org.generationcp.middleware.service.api.PedigreeService;

public class PedigreeFactory {

	public static final String PROFILE_CIMMYT = "CIMMYT";
	public static final String PROFILE_DEFAULT = "DEFAULT";

	public static PedigreeService getPedigreeService(final HibernateSessionProvider sessionProvider, final String pedigreeProfile,
			final String cropType) {
		if (PedigreeFactory.isCimmytWheat(pedigreeProfile, cropType)) {
			return new PedigreeCimmytWheatServiceImpl(sessionProvider);
		}
		return new PedigreeDefaultServiceImpl(sessionProvider);
	}

	public static boolean isCimmytWheat(final String profile, final String crop) {
		if (profile != null && crop != null && profile.equalsIgnoreCase(PedigreeFactory.PROFILE_CIMMYT)
				&& CropEnum.WHEAT.toString().equalsIgnoreCase(crop)) {
			return true;
		}
		return false;
	}
}
