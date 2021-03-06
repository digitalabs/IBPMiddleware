package org.generationcp.middleware.operation.saver;

import org.generationcp.middleware.IntegrationTestBase;
import org.generationcp.middleware.dao.oms.CVTermDao;
import org.generationcp.middleware.data.initializer.StandardVariableTestDataInitializer;
import org.generationcp.middleware.domain.dms.Enumeration;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.manager.DaoFactory;
import org.generationcp.middleware.pojos.oms.CVTerm;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StandardVariableSaverTest extends IntegrationTestBase {

	private StandardVariableSaver stdVarSaver;
	private CVTermDao cvtermDao;
	private DaoFactory daoFactory;

	private final Integer CVID = 2050;
	private final String TESTNAME = "Test Name";
	private final String TESTDEFINITION = "Test Definition";

	@Before
	public void setUp() {
		daoFactory = new DaoFactory(sessionProvder);
		this.stdVarSaver = new StandardVariableSaver(super.sessionProvder);
		this.cvtermDao = daoFactory.getCvTermDao();
	}

	@Test
	public void testSaveCheckType() {
		final StandardVariable stdVar = StandardVariableTestDataInitializer.createStandardVariable();
		final Enumeration testEnum = new Enumeration(null, this.TESTNAME, this.TESTDEFINITION, 0);
		this.stdVarSaver.saveEnumeration(stdVar, testEnum, this.CVID);

		final CVTerm cvTerm = this.cvtermDao.getByName("Test Name");
		Assert.assertNotNull("The newly created CvTerm should exist in the DB", cvTerm);
		Assert.assertEquals("The newly created CvTerm's name should be " + this.TESTNAME, this.TESTNAME,
				cvTerm.getName());
		Assert.assertEquals("The newly created CvTerm's description should be " + this.TESTDEFINITION,
				this.TESTDEFINITION, cvTerm.getDefinition());
	}
}
