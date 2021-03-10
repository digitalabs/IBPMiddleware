package org.generationcp.middleware.api.attribute;

import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.manager.DaoFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Transactional
@Service
public class AttributeServiceImpl implements AttributeService {

	private final DaoFactory daoFactory;

	public AttributeServiceImpl(final HibernateSessionProvider sessionProvider) {
		this.daoFactory = new DaoFactory(sessionProvider);
	}

	@Override
	public List<AttributeDTO> searchAttributes(final String query) {
		return this.daoFactory.getAttributeDAO().searchAttributes(query);
	}
}
