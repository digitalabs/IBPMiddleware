
package org.generationcp.middleware.service.impl;

import org.generationcp.middleware.dao.KeySequenceRegisterDAO;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.service.api.KeySequenceRegisterService;
import org.hibernate.Session;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class KeySequenceRegisterServiceImpl implements KeySequenceRegisterService {

	private KeySequenceRegisterDAO keySequenceRegisterDAO;

	public KeySequenceRegisterServiceImpl() {

	}

	public KeySequenceRegisterServiceImpl(HibernateSessionProvider sessionProvider) {
		this.keySequenceRegisterDAO = new KeySequenceRegisterDAO();
		this.keySequenceRegisterDAO.setSession(sessionProvider.getSession());
	}

	public KeySequenceRegisterServiceImpl(Session session) {
		this.keySequenceRegisterDAO = new KeySequenceRegisterDAO();
		this.keySequenceRegisterDAO.setSession(session);
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public int incrementAndGetNextSequence(String keyPrefix) {
		return this.keySequenceRegisterDAO.incrementAndGetNextSequence(keyPrefix);
	}

}
