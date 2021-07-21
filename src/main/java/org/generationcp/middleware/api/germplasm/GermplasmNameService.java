package org.generationcp.middleware.api.germplasm;

import org.generationcp.middleware.domain.germplasm.GermplasmNameDto;
import org.generationcp.middleware.domain.germplasm.GermplasmNameRequestDto;
import org.generationcp.middleware.pojos.Name;

import java.util.List;

public interface GermplasmNameService {

	Name getNameById(Integer nameId);

	void deleteName(Integer nameId);

	void updateName(GermplasmNameRequestDto germplasmNameRequestDto, Integer gid, Integer nameId);

	Integer createName(GermplasmNameRequestDto germplasmNameRequestDto, Integer gid);

	List<GermplasmNameDto> getGermplasmNamesByGids(List<Integer> gids);

	List<String> getExistingGermplasmPUIs(List<String> germplasmPUIs);


}
