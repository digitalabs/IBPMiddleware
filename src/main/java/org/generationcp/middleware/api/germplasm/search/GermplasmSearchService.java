package org.generationcp.middleware.api.germplasm.search;

import org.generationcp.middleware.pojos.UserDefinedField;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

public interface GermplasmSearchService {

	List<GermplasmSearchResponse> searchGermplasm(GermplasmSearchRequest germplasmSearchRequest, Pageable pageable,
		final String programUUID);

	long countSearchGermplasm(GermplasmSearchRequest germplasmSearchRequest, String programUUID);

	List<UserDefinedField> getGermplasmNameTypes(GermplasmSearchRequest germplasmSearchRequest, String programUUID);

	Map<Integer, Map<Integer, String>> getGermplasmSearchAttributeValues(GermplasmSearchRequest germplasmSearchRequest, String programUUID);

	Map<Integer, Map<Integer, String>> getGermplasmSearchNameValues(GermplasmSearchRequest germplasmSearchRequest, String programUUID);
}
