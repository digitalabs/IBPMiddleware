package org.generationcp.middleware.api.germplasm;

import org.generationcp.middleware.domain.germplasm.GermplasmImportRequestDto;
import org.generationcp.middleware.domain.germplasm.GermplasmImportResponseDto;
import org.generationcp.middleware.pojos.Attribute;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.UserDefinedField;

import java.util.List;
import java.util.Map;

public interface GermplasmService {

	List<Germplasm> getGermplasmByGUIDs(List<String> guids);

	/**
	 * Returns all germplasm for the given germplasm ids
	 *
	 * @param gids
	 * @return a {@link List} of {@link Germplasm}
	 */
	List<Germplasm> getGermplasmByGIDs(List<Integer> gids);

	/**
	 * Returns value of the plot code (seed source) where the germplasm was created, identified by the given gid. Returns "Unknown" if plot
	 * code attribute is not present. Never returns null.
	 */
	String getPlotCodeValue(Integer gid);

	/**
	 * Returns all the attributes of the Germplasm identified by the given id.
	 *
	 * @param gid - id of the Germplasm
	 * @return a {@link List} of {@link Attribute}
	 */
	List<Attribute> getAttributesByGID(Integer gid);

	/**
	 * @return the UDFLD table record that represents "plot code": ftable=ATRIBUTS, ftype=PASSPORT, fcode=PLOTCODE. If no record matching
	 *         these critria is found, an empty record with fldno=0 is returned. Never returns null.
	 */
	UserDefinedField getPlotCodeField();

	Map<Integer, GermplasmImportResponseDto> importGermplasm(Integer userId, String cropName,
		List<GermplasmImportRequestDto> germplasmImportRequestDto);

}
