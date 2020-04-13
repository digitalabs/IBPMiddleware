
package org.generationcp.middleware.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.generationcp.middleware.IntegrationTestBase;
import org.generationcp.middleware.dao.GermplasmDAO;
import org.generationcp.middleware.dao.KeySequenceRegisterDAO;
import org.generationcp.middleware.dao.KeySequenceRegisterDAOTest;
import org.generationcp.middleware.dao.NameDAO;
import org.generationcp.middleware.data.initializer.GermplasmTestDataInitializer;
import org.generationcp.middleware.data.initializer.NameTestDataInitializer;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.KeySequenceRegister;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.service.api.KeySequenceRegisterService;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.orm.hibernate3.SessionFactoryUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;


/**
 * This test is just an integration test harness that enables testing of the service without worrying about deploying the user interface
 * that calls it in real-life, but still simulating the way the UI calls it. Must disable the auto transaction rollback
 * (@TransactionConfiguration(defaultRollback = false)) in IntegrationTestBase to use this test.
 *
 * Intentionally not added to Middleware pom so that it does not run as part of maven builds.
 */
public class KeySequenceRegisterServiceImplIntegrationTest extends IntegrationTestBase {

	private static final Logger LOG = LoggerFactory.getLogger(KeySequenceRegisterServiceImplIntegrationTest.class);
	private static final String PREFIX = "QWERTY" + new Random().nextInt();
	private static final Integer LAST_SEQUENCE_USED = 9;

	@Autowired
	private PlatformTransactionManager platformTransactionManager;

	private KeySequenceRegisterDAO keySequenceRegisterDao;
	private GermplasmDAO germplasmDAO;
	private NameDAO nameDAO;


	@Before
	public void setup() {
		final Session session = this.sessionProvder.getSession();
		this.keySequenceRegisterDao = new KeySequenceRegisterDAO();
		this.keySequenceRegisterDao.setSession(session);
		this.nameDAO = new NameDAO();
		this.nameDAO.setSession(session);
		this.germplasmDAO = new GermplasmDAO();
		this.germplasmDAO.setSession(session);
	}

	@Test
	public void testIncrementAndGetNextSequence() throws Exception {

		final int threads = 10;
		final List<Future<Integer>> results = new ArrayList<>();

		final ExecutorService threadPool = Executors.newFixedThreadPool(threads);

		// Simulating how vaadin client components use the middleware service.
		// Also simulating multiple parallel users/threads invoking same operation.

		for (int i = 1; i <= threads; i++) {
			final Future<Integer> result = threadPool.submit(new Callable<Integer>() {
				@Override
				public Integer call() {
					return new AssignCodeVaadinComponent().assignCodes();
				}
			});
			results.add(result);
		}

		threadPool.shutdown();
		while (!threadPool.isTerminated()) {
		}

		final Set<Integer> uniqueSequences = new HashSet<>();
		for (final Future<Integer> future : results) {
			final Integer generatedSequence = future.get();
			uniqueSequences.add(generatedSequence);
			LOG.info("Sequence returned: {}.", generatedSequence);
		}
		Assert.assertEquals("Each thread must return a unique sequence.", threads, uniqueSequences.size());
	}

	@Test
	public void testGetNextSequence() {
		final KeySequenceRegister keyRegister = new KeySequenceRegister();
		keyRegister.setKeyPrefix(KeySequenceRegisterServiceImplIntegrationTest.PREFIX);
		keyRegister.setLastUsedSequence(KeySequenceRegisterServiceImplIntegrationTest.LAST_SEQUENCE_USED);
		this.keySequenceRegisterDao.save(keyRegister);

		final KeySequenceRegisterService keySequenceRegisterService = new KeySequenceRegisterServiceImpl(this.sessionProvder);
		final int nextSequence = keySequenceRegisterService.getNextSequence(PREFIX);
		Assert.assertEquals(LAST_SEQUENCE_USED + 1, nextSequence);
	}

	@Test
	public void testSaveLastSequenceUsed() {
		final KeySequenceRegister keyRegister = new KeySequenceRegister();
		keyRegister.setKeyPrefix(KeySequenceRegisterServiceImplIntegrationTest.PREFIX);
		keyRegister.setLastUsedSequence(KeySequenceRegisterServiceImplIntegrationTest.LAST_SEQUENCE_USED);
		this.keySequenceRegisterDao.save(keyRegister);

		final KeySequenceRegisterService keySequenceRegisterService = new KeySequenceRegisterServiceImpl(this.sessionProvder);
		final Integer newLastSequenceUsed = 51;
		keySequenceRegisterService.saveLastSequenceUsed(KeySequenceRegisterServiceImplIntegrationTest.PREFIX, newLastSequenceUsed);
		Assert.assertEquals(newLastSequenceUsed + 1, keySequenceRegisterService.getNextSequence(KeySequenceRegisterServiceImplIntegrationTest.PREFIX));
	}

	@Test
	public void testGetByKeyPrefixes() {
		final KeySequenceRegister keyRegister = new KeySequenceRegister();
		keyRegister.setKeyPrefix(KeySequenceRegisterServiceImplIntegrationTest.PREFIX);
		keyRegister.setLastUsedSequence(KeySequenceRegisterServiceImplIntegrationTest.LAST_SEQUENCE_USED);
		this.keySequenceRegisterDao.save(keyRegister);

		final KeySequenceRegisterService keySequenceRegisterService = new KeySequenceRegisterServiceImpl(this.sessionProvder);
		final List<KeySequenceRegister> keySequenceRegisters = keySequenceRegisterService
			.getKeySequenceRegistersByPrefixes(Collections.singletonList(keyRegister.getKeyPrefix()));
		Assert.assertEquals(1, keySequenceRegisters.size());
		Assert.assertEquals(KeySequenceRegisterServiceImplIntegrationTest.PREFIX, keySequenceRegisters.get(0).getKeyPrefix());
		Assert.assertEquals(KeySequenceRegisterServiceImplIntegrationTest.LAST_SEQUENCE_USED.intValue(),
			keySequenceRegisters.get(0).getLastUsedSequence());
	}

	/**
	 * Represents a vaadin component like class which is an example client of the service under test.
	 */
	class AssignCodeVaadinComponent {

		int assignCodes() {

			synchronized (AssignCodeVaadinComponent.class) {

				final TransactionTemplate tx = new TransactionTemplate(
					KeySequenceRegisterServiceImplIntegrationTest.this.platformTransactionManager);
				return tx.execute(new TransactionCallback<Integer>() {

					@Override
					public Integer doInTransaction(final TransactionStatus status) {
						final KeySequenceRegisterService keySequenceRegisterService =
								new KeySequenceRegisterServiceImpl(KeySequenceRegisterServiceImplIntegrationTest.this.sessionProvder);
						return keySequenceRegisterService.incrementAndGetNextSequence("CML");
					}
				});
			}
		}
	}
}
