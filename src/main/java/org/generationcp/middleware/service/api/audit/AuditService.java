package org.generationcp.middleware.service.api.audit;

import org.generationcp.middleware.service.impl.audit.GermplasmNameChangeDTO;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AuditService {

	List<GermplasmNameChangeDTO> getNameChangesByGidAndNameId(Integer gid, Integer nameId, Pageable pageable);

	long countNameChangesByGidAndNameId(Integer gid, Integer nameId);

}
