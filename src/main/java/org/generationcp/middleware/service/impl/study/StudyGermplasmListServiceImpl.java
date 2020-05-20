
package org.generationcp.middleware.service.impl.study;

import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.manager.DaoFactory;
import org.generationcp.middleware.pojos.dms.StockModel;
import org.generationcp.middleware.pojos.dms.StockProperty;
import org.generationcp.middleware.service.api.study.StudyGermplasmDto;
import org.generationcp.middleware.service.api.study.StudyGermplasmListService;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Transactional
// FIXME: IBP-3697 Merge this class to StockModelService.
public class StudyGermplasmListServiceImpl implements StudyGermplasmListService {

	private final DaoFactory daoFactory;

	public StudyGermplasmListServiceImpl(final HibernateSessionProvider sessionProvider) {
		this.daoFactory = new DaoFactory(sessionProvider);
	}

	@Override
	public List<StudyGermplasmDto> getGermplasmList(final int studyBusinessIdentifier) {

		final List<StockModel> stockModelList = this.daoFactory.getStockDao().getStocksForStudy(studyBusinessIdentifier);
		final Map<Integer, String> stockIdsMap =
			this.daoFactory.getTransactionDAO().retrieveStockIds(stockModelList.stream().map(sm -> sm.getGermplasm().getGid()).collect(
				Collectors.toList()));
		final List<StudyGermplasmDto> studyGermplasmDtos = new ArrayList<>();
		int index = 0;
		for (final StockModel stockModel : stockModelList) {
			final StudyGermplasmDto studyGermplasmDto = new StudyGermplasmDto();
			studyGermplasmDto.setCross(stockModel.getGermplasm().getCrossName());
			studyGermplasmDto.setDesignation(stockModel.getName());
			studyGermplasmDto.setEntryCode(stockModel.getValue());
			studyGermplasmDto.setEntryNumber(Integer.valueOf(stockModel.getUniqueName()));
			studyGermplasmDto.setGermplasmId(stockModel.getGermplasm().getGid());
			++index;
			studyGermplasmDto.setPosition(String.valueOf(index));
			studyGermplasmDto.setSeedSource(this.findStockPropValue(TermId.SEED_SOURCE.getId(), stockModel.getProperties()));
			studyGermplasmDto.setCheckType(Integer.valueOf(this.findStockPropValue(TermId.ENTRY_TYPE.getId(), stockModel.getProperties())));
			studyGermplasmDto.setStockIds(stockIdsMap.getOrDefault(stockModel.getGermplasm().getGid(), ""));
			studyGermplasmDto.setGroupId(stockModel.getGermplasm().getMgid());
			studyGermplasmDtos.add(studyGermplasmDto);
		}
		return studyGermplasmDtos;
	}

	@Override
	public List<StudyGermplasmDto> getGermplasmListFromPlots(final int studyBusinessIdentifier, final Set<Integer> plotNos) {
		return this.daoFactory.getStockDao().getStudyGermplasmDtoList(studyBusinessIdentifier, plotNos);
	}

	private String findStockPropValue(final int termId, final Set<StockProperty> properties) {
		if (properties != null) {
			for (final StockProperty property : properties) {
				if (termId == property.getTypeId()) {
					return property.getValue();
				}
			}
		}
		return null;
	}

}
