package org.generationcp.middleware.api.germplasm;

import org.apache.commons.lang.StringUtils;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO;
import org.generationcp.middleware.api.nametype.GermplasmNameTypeService;
import org.generationcp.middleware.domain.germplasm.GermplasmNameDto;
import org.generationcp.middleware.domain.germplasm.GermplasmNameRequestDto;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.manager.DaoFactory;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Name;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@Transactional
public class GermplasmNameServiceImpl implements GermplasmNameService {

	private final DaoFactory daoFactory;

	@Autowired
	private GermplasmNameTypeService germplasmNameTypeService;

	public GermplasmNameServiceImpl(final HibernateSessionProvider sessionProvider) {
		this.daoFactory = new DaoFactory(sessionProvider);
	}

	@Override
	public Name getNameById(final Integer nameId) {
		return this.daoFactory.getNameDao().getById(nameId);
	}

	private Name getPreferredNameOfGermplasm(final Integer gid) {
		final List<Name> names = this.daoFactory.getNameDao().getByGIDWithListTypeFilters(gid, 1, null);
		if (!names.isEmpty()) {
			return names.get(0);
		}
		return null;
	}

	@Override
	public void deleteName(final Integer nameId) {
		final Name name = this.daoFactory.getNameDao().getById(nameId);
		this.daoFactory.getNameDao().makeTransient(name);
	}

	@Override
	public void updateName(final GermplasmNameRequestDto germplasmNameRequestDto, final Integer gid, final Integer nameId) {
		if (germplasmNameRequestDto.isPreferredName() != null && germplasmNameRequestDto.isPreferredName()) {
			final Name preferredName = this.getPreferredNameOfGermplasm(gid);
			if (preferredName != null) {
				preferredName.setNstat(0);
				this.daoFactory.getNameDao().save(preferredName);
			}
		}

		final Name name = this.daoFactory.getNameDao().getById(nameId);
		if (!StringUtils.isBlank(germplasmNameRequestDto.getNameTypeCode())) {
		final Set<String> codes = new HashSet<>(Arrays.asList(germplasmNameRequestDto.getNameTypeCode()));
		final List<GermplasmNameTypeDTO> germplasmNameTypeDTOs = this.germplasmNameTypeService.filterGermplasmNameTypes(codes);
			name.setTypeId(germplasmNameTypeDTOs.get(0).getId());

		}

		if (!StringUtils.isBlank(germplasmNameRequestDto.getName())) {
			name.setNval(germplasmNameRequestDto.getName());
		}

		if (germplasmNameRequestDto.getDate() != null) {
			name.setNdate(Integer.valueOf(germplasmNameRequestDto.getDate()));
		}

		if (germplasmNameRequestDto.getLocationId() != null) {
			name.setLocationId(germplasmNameRequestDto.getLocationId());
		}

		if (germplasmNameRequestDto.isPreferredName() != null) {
			name.setNstat(germplasmNameRequestDto.isPreferredName() ? 1 : 0);
		}
		this.daoFactory.getNameDao().save(name);
	}

	@Override
	public Integer createName(final GermplasmNameRequestDto germplasmNameRequestDto, final Integer gid) {

		if (germplasmNameRequestDto.isPreferredName()) {
			final Name preferredName = this.getPreferredNameOfGermplasm(gid);
			if (preferredName != null) {
				preferredName.setNstat(0);
				this.daoFactory.getNameDao().save(preferredName);
			}
		}

		final Set<String> codes = new HashSet<>(Arrays.asList(germplasmNameRequestDto.getNameTypeCode()));
		final List<GermplasmNameTypeDTO> germplasmNameTypeDTOs = this.germplasmNameTypeService.filterGermplasmNameTypes(codes);

		final Name name = new Name();
		name.setGermplasm(new Germplasm(gid));
		name.setTypeId(germplasmNameTypeDTOs.get(0).getId());
		name.setNval(germplasmNameRequestDto.getName());
		name.setNdate(Integer.valueOf(germplasmNameRequestDto.getDate()));
		name.setLocationId(germplasmNameRequestDto.getLocationId());
		name.setNstat(germplasmNameRequestDto.isPreferredName() ? 1 : 0);
		name.setReferenceId(0);
		this.daoFactory.getNameDao().save(name);
		return name.getNid();
	}

	@Override
	public List<GermplasmNameDto> getGermplasmNamesByGids(final List<Integer> gids) {
		return this.daoFactory.getNameDao().getGermplasmNamesByGids(gids);
	}

	@Override
	public List<String> getExistingGermplasmPUIs(final List<String> germplasmPUIs) {
		return this.daoFactory.getNameDao().getExistingGermplasmPUIs(germplasmPUIs);
	}
}
