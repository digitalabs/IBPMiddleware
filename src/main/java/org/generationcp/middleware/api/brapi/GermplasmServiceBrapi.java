package org.generationcp.middleware.api.brapi;

import org.generationcp.middleware.api.brapi.v1.germplasm.GermplasmDTO;
import org.generationcp.middleware.api.brapi.v2.germplasm.GermplasmImportRequest;
import org.generationcp.middleware.api.brapi.v2.germplasm.GermplasmUpdateRequest;
import org.generationcp.middleware.domain.germplasm.PedigreeDTO;
import org.generationcp.middleware.domain.germplasm.ProgenyDTO;
import org.generationcp.middleware.domain.search_request.brapi.v1.GermplasmSearchRequestDto;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface GermplasmServiceBrapi {

	List<GermplasmDTO> createGermplasm(String cropname, List<GermplasmImportRequest> germplasmImportRequestList);

	GermplasmDTO updateGermplasm(String germplasmDbId, GermplasmUpdateRequest germplasmUpdateRequest);
	List<GermplasmDTO> searchGermplasmDTO(GermplasmSearchRequestDto germplasmSearchRequestDTO, Pageable pageable);

	long countGermplasmDTOs(GermplasmSearchRequestDto germplasmSearchRequestDTO);

	PedigreeDTO getPedigree(Integer gid, String notation, Boolean includeSiblings);

	ProgenyDTO getProgeny(Integer gid);

	long countGermplasmByStudy(Integer studyDbId);

	List<GermplasmDTO> getGermplasmByStudy(Integer studyDbId, Pageable pageable);

	Optional<GermplasmDTO> getGermplasmDTOByGUID(String germplasmUUID);

}
