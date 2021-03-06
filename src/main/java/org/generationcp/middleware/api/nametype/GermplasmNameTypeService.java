package org.generationcp.middleware.api.nametype;

import org.generationcp.middleware.pojos.UserDefinedField;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface GermplasmNameTypeService {

	List<GermplasmNameTypeDTO> searchNameTypes(String name);

	Optional<GermplasmNameTypeDTO> getNameTypeByCode(String code);

	Integer createNameType(GermplasmNameTypeRequestDTO germplasmNameTypeRequestDTO);

	List<GermplasmNameTypeDTO> getNameTypes(Pageable pageable);

	long countAllNameTypes();

	List<GermplasmNameTypeDTO> filterGermplasmNameTypes(Set<String> codes);

	List<GermplasmNameTypeDTO> filterGermplasmNameTypesByName(String name);

	List<GermplasmNameTypeDTO> getNameTypesByGIDList(List<Integer> gidList);
}
