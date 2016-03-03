
package org.generationcp.middleware.service.pedigree;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Name;

import com.google.common.base.Optional;

/**
 * In plant breeding, inbred lines are used as stocks for the creation of hybrid lines to make use of the effects of heterosis. Inbreeding
 * in plants also occurs naturally in the form of self-pollination. As far as the pedigree string generation inbreds are the the top of the
 * tree and thus no further recursion is needed.
 */
public class InbredProcessor implements BreedingMethodProcessor {

	/**
	 * Used in case we cannot determine pedigree string using the preferred name or gid
	 */
	private static final String UNKNOWN_PEDIGREE_STRING = "Unknown";

	@Override
	public PedigreeString processGermplasmNode(final GermplasmNode germplasmNode, final Integer level, FixedLineNameResolver fixedLineNameResolver) {

		final Optional<PedigreeString> fixedLineName = PedigreeStringGeneratorUtil.getFixedLineName(germplasmNode, fixedLineNameResolver);
		if(fixedLineName.isPresent()) {
			return fixedLineName.get();
		}

		final PedigreeString pedigreeStringBuilders = new PedigreeString();

		if (germplasmNode == null ||  germplasmNode.getGermplasm() == null) {
			pedigreeStringBuilders.setPedigree(UNKNOWN_PEDIGREE_STRING);
			return pedigreeStringBuilders;
		}

		final Germplasm currentGermplasm = germplasmNode.getGermplasm();
		final Name nameObject = currentGermplasm.getPreferredName();
		if (nameObject == null || StringUtils.isBlank(nameObject.getNval())) {
			pedigreeStringBuilders.setPedigree(currentGermplasm.getGid().toString());
		} else {
			pedigreeStringBuilders.setPedigree(nameObject.getNval());
		}

		return pedigreeStringBuilders;

	}

}
