/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * <p/>
 * Generation Challenge Programme (GCP)
 * <p/>
 * <p/>
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *******************************************************************************/

package org.generationcp.middleware.dao;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.DataSetupTest;
import org.generationcp.middleware.IntegrationTestBase;
import org.generationcp.middleware.dao.dms.StockDao;
import org.generationcp.middleware.dao.ims.LotDAO;
import org.generationcp.middleware.dao.ims.TransactionDAO;
import org.generationcp.middleware.data.initializer.GermplasmTestDataInitializer;
import org.generationcp.middleware.domain.germplasm.GermplasmDTO;
import org.generationcp.middleware.domain.germplasm.ParentType;
import org.generationcp.middleware.domain.germplasm.PedigreeDTO;
import org.generationcp.middleware.domain.germplasm.ProgenyDTO;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.search_request.brapi.v1.GermplasmSearchRequestDto;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.GermplasmListManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.*;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.generationcp.middleware.pojos.dms.StockModel;
import org.generationcp.middleware.pojos.germplasm.GermplasmParent;
import org.generationcp.middleware.pojos.ims.Transaction;
import org.generationcp.middleware.service.api.DataImportService;
import org.generationcp.middleware.service.api.FieldbookService;
import org.generationcp.middleware.util.Util;
import org.hibernate.Session;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

import java.text.ParseException;
import java.util.*;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;

public class GermplasmDAOTest extends IntegrationTestBase {

	private static final String DUMMY_STOCK_ID = "USER-1-1";
	private static final Integer TEST_PROJECT_ID = 1;

	private static final Integer GROUP_ID = 10;

	private boolean testDataSetup = false;

	private GermplasmDAO dao;
	private LotDAO lotDAO;
	private TransactionDAO transactionDAO;
	private MethodDAO methodDAO;
	private NameDAO nameDAO;
	private UserDefinedFieldDAO userDefinedFieldDao;
	private ProgenitorDAO progenitorDao;
	private StockDao stockDao;
	private DataSetupTest dataSetupTest;

	@Autowired
	private InventoryDataManager inventoryDM;

	@Autowired
	private GermplasmDataManager germplasmDataDM;

	@Autowired
	private DataImportService dataImportService;

	@Autowired
	private GermplasmListManager germplasmListManager;

	@Autowired
	private FieldbookService middlewareFieldbookService;

	private static final int UNKNOWN_GENERATIVE_METHOD_ID = 1;

	@Before
	public void setUp() throws Exception {
		if (this.dao == null) {
			this.dao = new GermplasmDAO();
			this.dao.setSession(this.sessionProvder.getSession());

			this.lotDAO = new LotDAO();
			this.lotDAO.setSession(this.sessionProvder.getSession());

			this.transactionDAO = new TransactionDAO();
			this.transactionDAO.setSession(this.sessionProvder.getSession());

			this.methodDAO = new MethodDAO();
			this.methodDAO.setSession(this.sessionProvder.getSession());

			this.nameDAO = new NameDAO();
			this.nameDAO.setSession(this.sessionProvder.getSession());

			this.userDefinedFieldDao = new UserDefinedFieldDAO();
			this.userDefinedFieldDao.setSession(this.sessionProvder.getSession());

			this.progenitorDao = new ProgenitorDAO();
			this.progenitorDao.setSession(this.sessionProvder.getSession());

			this.stockDao = new StockDao();
			this.stockDao.setSession(this.sessionProvder.getSession());

			this.dataSetupTest = new DataSetupTest();
			this.dataSetupTest.setDataImportService(this.dataImportService);
			this.dataSetupTest.setGermplasmListManager(this.germplasmListManager);
			this.dataSetupTest.setMiddlewareFieldbookService(this.middlewareFieldbookService);
		}

		if (!this.testDataSetup) {
			this.updateInventory();
			this.testDataSetup = true;
		}
		this.initializeGermplasms();
	}

	private void updateInventory() {
		final List<Transaction> transactions = this.inventoryDM.getAllTransactions(0, 1);
		if (transactions != null && !transactions.isEmpty()) {
			final Transaction transaction = transactions.get(0);
			transaction.getLot().setStockId(GermplasmDAOTest.DUMMY_STOCK_ID);
			this.inventoryDM.updateTransaction(transaction);
		}
	}

	@Test
	public void testGetDerivativeChildren() {
		final Germplasm parentGermplsm =
				GermplasmTestDataInitializer.createGermplasm(20150101, 1, 1, -1, 0, 0, 1, 1, 0, 1, 1, "MethodName", "LocationName");
		this.germplasmDataDM.addGermplasm(parentGermplsm, parentGermplsm.getPreferredName());

		final Germplasm childDerivativeGermplsm = GermplasmTestDataInitializer
				.createGermplasm(20150101, 1, parentGermplsm.getGid(), -1, 0, 0, 1, 1, 0, 1, 1, "MethodName", "LocationName");
		this.germplasmDataDM.addGermplasm(childDerivativeGermplsm, childDerivativeGermplsm.getPreferredName());

		final List<Germplasm> results = this.dao.getChildren(parentGermplsm.getGid(), 'D');
		Assert.assertNotNull(results);
		Assert.assertEquals(childDerivativeGermplsm.getGid(), results.get(0).getGid());
	}

	@Test
	public void testGetMaintenanceChildren() {
		final Germplasm parentGermplsm =
				GermplasmTestDataInitializer.createGermplasm(20150101, 1, 1, -1, 0, 0, 1, 1, 0, 1, 1, "MethodName", "LocationName");
		this.germplasmDataDM.addGermplasm(parentGermplsm, parentGermplsm.getPreferredName());

		final List<org.generationcp.middleware.pojos.Method> maintenanceMethods = this.methodDAO.getByType("MAN", 1, 1);

		final Germplasm maintenanceChildrenGermplsm = GermplasmTestDataInitializer
				.createGermplasm(20150101, 1, parentGermplsm.getGid(), -1, 0, 0, 1, maintenanceMethods.get(0).getMid(), 0, 1, 1,
						"MethodName", "LocationName");

		this.germplasmDataDM.addGermplasm(maintenanceChildrenGermplsm, maintenanceChildrenGermplsm.getPreferredName());

		final List<Germplasm> results = this.dao.getChildren(parentGermplsm.getGid(), 'M');
		Assert.assertNotNull(results);
		Assert.assertNotNull(results);
		Assert.assertEquals(maintenanceChildrenGermplsm.getGid(), results.get(0).getGid());

	}

	@Test
	public void testRetrieveStudyParentGIDsKnownValuesOnly() {

		final Germplasm germplasm =
				GermplasmTestDataInitializer.createGermplasm(20150101, 12, 13, 1, 0, 0, 1, 1, 0, 1, 1, "MethodName", "LocationName");
		this.germplasmDataDM.addGermplasm(germplasm, germplasm.getPreferredName());
		final StockModel stock = new StockModel();
		stock.setGermplasm(germplasm);
		stock.setUniqueName("1");
		stock.setIsObsolete(false);
		stock.setProject(new DmsProject(TEST_PROJECT_ID));
		stock.setTypeId(TermId.ENTRY_CODE.getId());
		this.stockDao.save(stock);

		final List<Germplasm> germplasmEntries = this.dao.getGermplasmParentsForStudy(GermplasmDAOTest.TEST_PROJECT_ID);

		Assert.assertEquals(1, germplasmEntries.size());
		Assert.assertEquals(germplasm.getGid(), germplasmEntries.get(0).getGid());
		Assert.assertEquals(germplasm.getGpid1(), germplasmEntries.get(0).getGpid1());
		Assert.assertEquals(germplasm.getGpid2(), germplasmEntries.get(0).getGpid2());
		Assert.assertEquals(germplasm.getGrplce(), germplasmEntries.get(0).getGrplce());
	}

	@Test
	public void testGetAllChildren() {
		final Germplasm parentGermplsm =
				GermplasmTestDataInitializer.createGermplasm(20150101, 1, 1, -1, 0, 0, 1, GermplasmDAOTest.UNKNOWN_GENERATIVE_METHOD_ID, 0, 1, 1, "MethodName", "LocationName");
		this.germplasmDataDM.addGermplasm(parentGermplsm, parentGermplsm.getPreferredName());

		final Germplasm childDerivativeGermplsm = GermplasmTestDataInitializer
				.createGermplasm(20150101, 1, parentGermplsm.getGid(), -1, 0, 0, 1, GermplasmDAOTest.UNKNOWN_GENERATIVE_METHOD_ID, 0, 1, 1, "MethodName", "LocationName");
		this.germplasmDataDM.addGermplasm(childDerivativeGermplsm, childDerivativeGermplsm.getPreferredName());

		final Germplasm maintenanceChildrenGermplsm = GermplasmTestDataInitializer
				.createGermplasm(20150101, 1, parentGermplsm.getGid(), -1, 0, 0, 1, GermplasmDAOTest.UNKNOWN_GENERATIVE_METHOD_ID, 0, 1, 1, "MethodName", "LocationName");
		this.germplasmDataDM.addGermplasm(maintenanceChildrenGermplsm, maintenanceChildrenGermplsm.getPreferredName());

		final List<Germplasm> children = this.dao.getAllChildren(parentGermplsm.getGid());
		Assert.assertNotNull("getAllChildren() should never return null.", children);

		final List<Integer> resultChildGermplasmIds = Lists.newArrayList();

		for (final Germplasm germplasm : children) {
			resultChildGermplasmIds.add(germplasm.getGid());
		}

		Assert.assertTrue("Derivative child Germplasm should be included in search result",
				resultChildGermplasmIds.contains(childDerivativeGermplsm.getGid()));
		Assert.assertTrue("Maintenance child Germplasm should be included in search result",
				resultChildGermplasmIds.contains(maintenanceChildrenGermplsm.getGid()));
	}

	@Test
	public void testGetPreviousCrosses() {
		final Germplasm female =
				GermplasmTestDataInitializer.createGermplasm(20150101, 1, 2, 2, 0, 0, 1, 1, 0, 1, 1, "MethodName", "LocationName");
		this.germplasmDataDM.addGermplasm(female, female.getPreferredName());

		final Germplasm male =
				GermplasmTestDataInitializer.createGermplasm(20150101, 1, 2, 2, 0, 0, 1, 1, 0, 1, 1, "MethodName", "LocationName");
		this.germplasmDataDM.addGermplasm(male, male.getPreferredName());

		final Germplasm currentCross =
				GermplasmTestDataInitializer.createGermplasm(20150101, 1, 2, 2, 0, 0, 1, 1, 0, 1, 1, "MethodName", "LocationName");
		currentCross.setGpid1(female.getGid());
		currentCross.setGpid2(male.getGid());

		this.germplasmDataDM.addGermplasm(currentCross, currentCross.getPreferredName());

		final Germplasm previousCross =
				GermplasmTestDataInitializer.createGermplasm(20150101, 1, 2, 2, 0, 0, 1, 1, 0, 1, 1, "MethodName", "LocationName");
		previousCross.setGpid1(female.getGid());
		previousCross.setGpid2(male.getGid());

		this.germplasmDataDM.addGermplasm(previousCross, previousCross.getPreferredName());

		final List<Germplasm> previousCrosses = this.dao.getPreviousCrosses(currentCross, female, male);
		Assert.assertNotNull("getPreviousCrosses() should never return null.", previousCrosses);

		Assert.assertEquals("There should be only one previous cross", 1, previousCrosses.size());
		Assert.assertEquals(previousCross.getGid(), previousCrosses.get(0).getGid());
	}

	@Test
	public void testLoadEntityWithNameCollection() {
		final Germplasm germplasm = this.dao.getById(1);
		if (germplasm != null) {
			Assert.assertTrue("If germplasm exists, the name collection can not be empty.", !germplasm.getNames().isEmpty());
		}
	}

	@Test
	public void testGetManagementGroupMembers() {
		List<Germplasm> groupMembers = this.dao.getManagementGroupMembers(1);
		Assert.assertNotNull("getManagementGroupMembers() should never return null when supplied with proper mgid.", groupMembers);

		groupMembers = this.dao.getManagementGroupMembers(null);
		Assert.assertTrue("getManagementGroupMembers() should return empty collection when supplied mgid = null.", groupMembers.isEmpty());

		groupMembers = this.dao.getManagementGroupMembers(0);
		Assert.assertTrue("getManagementGroupMembers() should return empty collection when supplied mgid = 0.", groupMembers.isEmpty());
	}

	@Test
	public void testGetPedigree() throws ParseException {

		final Method generativeMethod = this.methodDAO.getByCode("C2W");
		final Method derivativeMethod = this.methodDAO.getByCode("UDM");
		final Method maintenanceMethod = this.methodDAO.getByCode("SMP");

		final Germplasm femaleParent = GermplasmTestDataInitializer.createGermplasmWithPreferredName();
		final Germplasm maleParent = GermplasmTestDataInitializer.createGermplasmWithPreferredName();
		this.dao.save(femaleParent);
		this.dao.save(maleParent);

		final Germplasm cross = GermplasmTestDataInitializer.createGermplasmWithPreferredName();
		cross.setGpid1(femaleParent.getGid());
		cross.setGpid2(maleParent.getGid());
		cross.setGnpgs(2);
		cross.setMethodId(generativeMethod.getMid());
		this.dao.save(cross);

		final Germplasm advance = GermplasmTestDataInitializer.createGermplasmWithPreferredName();
		advance.setGpid1(cross.getGid());
		advance.setGpid2(cross.getGid());
		advance.setGnpgs(-1);
		advance.setMethodId(derivativeMethod.getMid());
		this.dao.save(advance);

		final Germplasm advance2 = GermplasmTestDataInitializer.createGermplasmWithPreferredName();
		advance2.setGpid1(cross.getGid());
		advance2.setGpid2(cross.getGid());
		advance2.setGnpgs(-1);
		advance2.setMethodId(maintenanceMethod.getMid());
		this.dao.save(advance2);

		final PedigreeDTO generativePedigree = this.dao.getPedigree(cross.getGid(), null, false);
		final PedigreeDTO derivativePedigree = this.dao.getPedigree(advance.getGid(), null, true);
		final PedigreeDTO maintenancePedigree = this.dao.getPedigree(advance2.getGid(), null, true);

		Assert.assertThat(generativePedigree.getGermplasmDbId(), is(cross.getGid()));
		Assert.assertThat(generativePedigree.getParent1DbId(), is(femaleParent.getGid()));
		Assert.assertThat(generativePedigree.getParent1Type(), is(ParentType.FEMALE.name()));
		Assert.assertThat(generativePedigree.getParent2DbId(), is(maleParent.getGid()));
		Assert.assertThat(generativePedigree.getParent2Type(), is(ParentType.MALE.name()));
		Assert.assertThat(generativePedigree.getCrossingPlan(), is(generativeMethod.getMcode() + "|" + generativeMethod.getMname() + "|" + generativeMethod.getMtype()));
		final Date gdate = Util.parseDate(String.valueOf(cross.getGdate()), Util.DATE_AS_NUMBER_FORMAT);
		final Integer year = Integer.valueOf(Util.getSimpleDateFormat("yyyy").format(gdate));
		Assert.assertThat(generativePedigree.getCrossingYear(), is(year));
		Assert.assertThat(generativePedigree.getSiblings(), nullValue());

		Assert.assertThat(derivativePedigree.getGermplasmDbId(), is(advance.getGid()));
		Assert.assertThat(derivativePedigree.getParent1DbId(), is(cross.getGid()));
		Assert.assertThat(derivativePedigree.getParent1Type(), is(ParentType.POPULATION.name()));
		Assert.assertThat(derivativePedigree.getParent2DbId(), is(cross.getGid()));
		Assert.assertThat(derivativePedigree.getParent2Type(), is(ParentType.SELF.name()));
		Assert.assertThat(derivativePedigree.getCrossingPlan(), is(derivativeMethod.getMcode() + "|" + derivativeMethod.getMname() + "|" + derivativeMethod.getMtype()));
		Assert.assertThat(derivativePedigree.getSiblings(), hasSize(1));
		Assert.assertThat(derivativePedigree.getSiblings().get(0).getGermplasmDbId(), is(advance2.getGid()));

		Assert.assertThat(maintenancePedigree.getGermplasmDbId(), is(advance2.getGid()));
		Assert.assertThat(maintenancePedigree.getParent1DbId(), is(cross.getGid()));
		Assert.assertThat(maintenancePedigree.getParent1Type(), is(ParentType.POPULATION.name()));
		Assert.assertThat(maintenancePedigree.getParent2DbId(), is(cross.getGid()));
		Assert.assertThat(maintenancePedigree.getParent2Type(), is(ParentType.SELF.name()));
		Assert.assertThat(maintenancePedigree.getCrossingPlan(), is(maintenanceMethod.getMcode() + "|" + maintenanceMethod.getMname() + "|" + maintenanceMethod.getMtype()));
		Assert.assertThat(maintenancePedigree.getSiblings(), hasSize(1));
		Assert.assertThat(maintenancePedigree.getSiblings().get(0).getGermplasmDbId(), is(advance.getGid()));
	}

	@Test
	public void testGetProgeny() {
		final Germplasm femaleParent = GermplasmTestDataInitializer.createGermplasmWithPreferredName();
		final Germplasm maleParent = GermplasmTestDataInitializer.createGermplasmWithPreferredName();
		this.dao.save(femaleParent);
		this.dao.save(maleParent);

		final Name maleParentPreferredName = maleParent.getPreferredName();
		maleParentPreferredName.setGermplasmId(maleParent.getGid());
		this.nameDAO.save(maleParentPreferredName);

		final Germplasm cross = GermplasmTestDataInitializer.createGermplasmWithPreferredName();
		cross.setGpid1(femaleParent.getGid());
		cross.setGpid2(maleParent.getGid());
		cross.setGnpgs(2);
		this.dao.save(cross);

		final Name crossPreferredName = cross.getPreferredName();
		crossPreferredName.setGermplasmId(cross.getGid());
		this.nameDAO.save(crossPreferredName);

		final Germplasm advance = GermplasmTestDataInitializer.createGermplasmWithPreferredName();
		advance.setGpid1(cross.getGid());
		advance.setGpid2(cross.getGid());
		advance.setGnpgs(-1);
		this.dao.save(advance);

		final ProgenyDTO progeny = this.dao.getProgeny(maleParent.getGid());

		Assert.assertThat(progeny.getGermplasmDbId(), is(maleParent.getGid()));
		Assert.assertThat(progeny.getDefaultDisplayName(), is(maleParentPreferredName.getNval()));
		Assert.assertThat(progeny.getProgeny(), hasSize(1));
		Assert.assertThat(progeny.getProgeny().get(0).getParentType(), is(ParentType.MALE.name()));
		Assert.assertThat(progeny.getProgeny().get(0).getDefaultDisplayName(), is(crossPreferredName.getNval()));

		final ProgenyDTO crossProgeny = this.dao.getProgeny(cross.getGid());

		Assert.assertThat(crossProgeny.getGermplasmDbId(), is(cross.getGid()));
		Assert.assertThat(crossProgeny.getProgeny(), hasSize(1));
		Assert.assertThat(crossProgeny.getProgeny().get(0).getParentType(), is(ParentType.SELF.name()));
		Assert.assertThat(crossProgeny.getProgeny().get(0).getGermplasmDbId(), is(advance.getGid()));
	}

	@Test
	public void testSaveGermplasmNamesThroughHibernateCascade() {

		final Germplasm germplasm = new Germplasm();
		germplasm.setMethodId(1);
		germplasm.setGnpgs(-1);
		germplasm.setGpid1(0);
		germplasm.setGpid2(0);
		germplasm.setUserId(1);
		germplasm.setLgid(0);
		germplasm.setLocationId(1);
		germplasm.setGdate(20160101);
		germplasm.setReferenceId(0);
		germplasm.setGrplce(0);
		germplasm.setMgid(0);

		this.dao.save(germplasm);
		Assert.assertNotNull(germplasm.getGid());

		final Name name1 = new Name();
		name1.setTypeId(5);
		name1.setNstat(1);
		name1.setUserId(1);
		name1.setNval("Name1");
		name1.setLocationId(1);
		name1.setNdate(20160101);
		name1.setReferenceId(0);

		final Name name2 = new Name();
		name2.setTypeId(5);
		name2.setNstat(1);
		name2.setUserId(1);
		name2.setNval("Name2");
		name2.setLocationId(1);
		name2.setNdate(20160101);
		name2.setReferenceId(0);

		germplasm.getNames().add(name1);
		germplasm.getNames().add(name2);

		// Name collection mapping is uni-directional OneToMany right now, so the other side of the relationship has to be managed manually.
		for (final Name name : germplasm.getNames()) {
			name.setGermplasmId(germplasm.getGid());
		}

		// In real app flush will happen automatically on tx commit. We don't commit tx in tests, so flush manually.
		this.sessionProvder.getSession().flush();

		for (final Name name : germplasm.getNames()) {
			// No explicit save of name entity anywhere but should still be saved through cascade on flush.
			Assert.assertNotNull(name.getNid());
			Assert.assertEquals(germplasm.getGid(), name.getGermplasmId());
		}
	}

	@Test
	public void testCountMatchGermplasmInListAllGidsExist() {

		final Germplasm germplasm1 =
				GermplasmTestDataInitializer.createGermplasm(20150101, 1, 2, 2, 0, 0, 1, 1, 0, 1, 1, "MethodName", "LocationName");
		final Germplasm germplasm2 =
				GermplasmTestDataInitializer.createGermplasm(20150101, 1, 2, 2, 0, 0, 1, 1, 0, 1, 1, "MethodName", "LocationName");

		this.dao.save(germplasm1);
		this.dao.save(germplasm2);

		final Set<Integer> gids = new HashSet<>();
		gids.add(germplasm1.getGid());
		gids.add(germplasm2.getGid());

		final long result = this.dao.countMatchGermplasmInList(gids);

		Assert.assertEquals("The number of gids in list should match the count of records matched in the database.", gids.size(),
			(int) result);

	}

	@Test
	public void testCountMatchGermplasmInListOnlyOneGidExists() {

		final Set<Integer> gids = new HashSet<>();
		final Integer dummyGid = Integer.MIN_VALUE + 1;

		final Germplasm germplasm1 =
				GermplasmTestDataInitializer.createGermplasm(20150101, 1, 2, 2, 0, 0, 1, 1, 0, 1, 1, "MethodName", "LocationName");
		this.dao.save(germplasm1);

		final Germplasm germplasm = this.dao.getById(dummyGid);
		Assert.assertNull("Make sure that gid " + dummyGid + " doesn't exist.", germplasm);

		gids.add(germplasm1.getGid());
		gids.add(dummyGid);

		final long result = this.dao.countMatchGermplasmInList(gids);

		Assert.assertEquals("Only one gid has a match in the database.", 1, (int) result);

	}

	@Test
	public void testCountMatchGermplasmInListNoGidExists() {

		final Integer dummyGid = Integer.MIN_VALUE + 1;

		final Set<Integer> gids = new HashSet<>();

		final Germplasm germplasm = this.dao.getById(dummyGid);

		Assert.assertNull("We're testing a gid that doesnt exist, so the germplasm should be null.", germplasm);

		// Add dummy gid that do not exist in the database
		gids.add(dummyGid);

		final long result = this.dao.countMatchGermplasmInList(gids);

		Assert.assertEquals("The count should be zero because the gid in the list doesn't exist.", 0, (int) result);

	}

	@Test
	public void testCountMatchGermplasmInListGidListIsNullOrEmpty() {

		final long result1 = this.dao.countMatchGermplasmInList(null);
		Assert.assertEquals("The count should be zero because the gid list is null", 0, (int) result1);

		final long result2 = this.dao.countMatchGermplasmInList(new HashSet<Integer>());
		Assert.assertEquals("The count should be zero because the gid list is empty", 0, (int) result2);

	}

	@Test
	public void testGetGermplasmDescendantByGIDs() {
		final Germplasm fParent =
				GermplasmTestDataInitializer.createGermplasm(20150101, 1, 2, 2, 0, 0, 1, 1, 0, 1, 1, "MethodName", "LocationName");
		final Integer fParentGID = this.germplasmDataDM.addGermplasm(fParent, fParent.getPreferredName());

		final Germplasm mParent =
				GermplasmTestDataInitializer.createGermplasm(20150101, 1, 2, 2, 0, 0, 1, 1, 0, 1, 1, "MethodName", "LocationName");
		final Integer mParentGID = this.germplasmDataDM.addGermplasm(mParent, mParent.getPreferredName());

		final Germplasm germplasm = GermplasmTestDataInitializer
				.createGermplasm(20150101, fParentGID, mParentGID, 2, 0, 0, 1, 1, GermplasmDAOTest.GROUP_ID, 1, 1, "MethodName",
						"LocationName");
		final Integer gid = this.germplasmDataDM.addGermplasm(germplasm, germplasm.getPreferredName());

		Assert.assertTrue(this.dao.getGermplasmOffspringByGIDs(Collections.singletonList(mParentGID)).size() > 0);
		Assert.assertTrue(this.dao.getGermplasmOffspringByGIDs(Collections.singletonList(fParentGID)).size() > 0);
		Assert.assertFalse(this.dao.getGermplasmOffspringByGIDs(Collections.singletonList(gid)).size() > 0);
	}

	@Test
	public void testGetNextSequenceNumberString() {

		final String crossNamePrefix = "ABCDEFG";
		final String existingGermplasmNameWithPrefix = crossNamePrefix + "1";

		this.insertGermplasmWithName(existingGermplasmNameWithPrefix);

		final String result = this.germplasmDataDM.getNextSequenceNumberAsString(crossNamePrefix);
		Assert.assertEquals(
				"Germplasm with prefix " + existingGermplasmNameWithPrefix + " is existing so the next sequence number should be 2", "2",
				result);
	}

	@Test
	public void testGetNextSequenceNumber() {
		final Session mockSession = Mockito.mock(Session.class);
		this.dao.setSession(mockSession);
		this.dao.getNextSequenceNumber("");
		// Verify that no query was made if the prefix is empty
		Mockito.verify(mockSession, Mockito.never()).createSQLQuery(ArgumentMatchers.anyString());
	}

	@Test
	public void testGetNextSequenceNumberStringForMixedCasePrefix() {

		final String crossNamePrefix = "aBcDeFg";
		final int lastCodeForMixedCasePrefix = 29;
		final String nameWithMixedCasePrefix = crossNamePrefix + lastCodeForMixedCasePrefix;
		final int lastCodeForUppercasePrefix = 19;
		final String nameWithUppercasePrefix = crossNamePrefix.toUpperCase() + lastCodeForUppercasePrefix;

		this.insertGermplasmWithName(nameWithMixedCasePrefix);
		this.insertGermplasmWithName(nameWithUppercasePrefix);

		final String result = this.germplasmDataDM.getNextSequenceNumberAsString(crossNamePrefix);
		final int nextCodeForPrefix = lastCodeForMixedCasePrefix + 1;
		Assert.assertEquals("Germplasm with prefix " + nameWithMixedCasePrefix + " is existing so the next sequence number should be "
				+ nextCodeForPrefix, Integer.toString(nextCodeForPrefix), result);
	}

	@Test
	public void testGetNextSequenceNumberStringForLowerCasePrefix() {

		final String crossNamePrefix = "aBcDeFgHij";
		final int lastCodeForLowercasePrefix = 49;
		final String nameWithLowercasePrefix = crossNamePrefix.toLowerCase() + lastCodeForLowercasePrefix;
		final int lastCodeForUppercasePrefix = 39;
		final String nameWithUppercasePrefix = crossNamePrefix.toUpperCase() + lastCodeForUppercasePrefix;

		this.insertGermplasmWithName(nameWithLowercasePrefix);
		this.insertGermplasmWithName(nameWithUppercasePrefix);

		final String result = this.germplasmDataDM.getNextSequenceNumberAsString(crossNamePrefix);
		final int nextCodeForPrefix = lastCodeForLowercasePrefix + 1;
		Assert.assertEquals("Germplasm with prefix " + nameWithLowercasePrefix + " is existing so the next sequence number should be "
				+ nextCodeForPrefix, Integer.toString(nextCodeForPrefix), result);
	}

	@Test
	public void testGetNextSequenceNumberStringGermplasmIsDeleted() {

		final String crossNamePrefix = "ABCDEFG";
		final String existingGermplasmNameWithPrefix = crossNamePrefix + "1";

		// Flag the germplasm as deleted
		this.insertGermplasmWithName(existingGermplasmNameWithPrefix, true);

		final String result = this.germplasmDataDM.getNextSequenceNumberAsString(crossNamePrefix);
		Assert.assertEquals(
				"Germplasm with name" + existingGermplasmNameWithPrefix + " is deleted so the next sequence number should still be 1", "1",
				result);

	}

	@Test
	public void testGermplasmWithoutGroup() {

		// Create 2 germplasm without group
		final Germplasm germplasm1 =
				GermplasmTestDataInitializer.createGermplasm(20150101, 1, 2, 2, 0, 0, 1, 1, 0, 1, 1, "MethodName", "LocationName");
		final Germplasm germplasm2 =
				GermplasmTestDataInitializer.createGermplasm(20150101, 1, 2, 2, 0, 0, 1, 1, 0, 1, 1, "MethodName", "LocationName");

		// Create 1 germplasm with group
		final Germplasm germplasm3 =
				GermplasmTestDataInitializer.createGermplasm(20150101, 1, 2, 2, 0, 0, 1, 1, 999, 1, 1, "MethodName", "LocationName");

		// Save them
		this.dao.save(germplasm1);
		this.dao.save(germplasm2);
		this.dao.save(germplasm3);

		final List<Germplasm> listOfGermplasm =
			this.dao.getGermplasmWithoutGroup(Arrays.asList(germplasm1.getGid(), germplasm2.getGid(), germplasm3.getGid()));
		Assert.assertEquals("Only 2 germplasm from the gid list which are without group", 2, listOfGermplasm.size());

	}

	@Test
	public void resetGermplasmGroup() {

		// Create 2 germplasm with group
		final Germplasm germplasm1 =
				GermplasmTestDataInitializer.createGermplasm(20150101, 1, 2, 2, 0, 0, 1, 1, 99, 1, 1, "MethodName", "LocationName");
		final Germplasm germplasm2 =
				GermplasmTestDataInitializer.createGermplasm(20150101, 1, 2, 2, 0, 0, 1, 1, 100, 1, 1, "MethodName", "LocationName");

		// Save them
		this.dao.save(germplasm1);
		this.dao.save(germplasm2);

		// Reset the germplasm group
		this.dao.resetGermplasmGroup(Arrays.asList(germplasm1.getGid(), germplasm2.getGid()));

		this.dao.getSession().refresh(germplasm1);
		this.dao.getSession().refresh(germplasm2);

		Assert.assertEquals(0, germplasm1.getMgid().intValue());
		Assert.assertEquals(0, germplasm2.getMgid().intValue());

	}

	// TODO Add more assertions
	@Test
	public void testGetGermplasmDTOList() {

		final Germplasm germplasm =
			GermplasmTestDataInitializer.createGermplasm(20150101, 1, 2, 2, 0, 0, 1, 1, 0, 1, 1, "MethodName", "LocationName");
		final Integer germplasmGID = this.germplasmDataDM.addGermplasm(germplasm, germplasm.getPreferredName());

		final Map<String, String> fields = new HashMap<>();

		// atributs
		fields.put("ORI_COUN", "");
		fields.put("SPNAM", "");
		fields.put("SPAUTH", "");
		fields.put("SUBTAX", "");
		fields.put("STAUTH", "");
		fields.put("PROGM", "");

		for (final Map.Entry<String, String> attributEntry : fields.entrySet()) {

			UserDefinedField attributeField =
				this.userDefinedFieldDao.getByTableTypeAndCode("ATRIBUTS", "ATTRIBUTE", attributEntry.getKey());

			if (attributeField == null) {
				attributeField = new UserDefinedField(null, "ATRIBUTS", "ATTRIBUTE", attributEntry.getKey(), "", "", "", 0, 0, 0, 0);
				this.germplasmDataDM.addUserDefinedField(attributeField);
			}

			final Attribute attribute = new Attribute();
			attribute.setGermplasmId(germplasmGID);
			attribute.setTypeId(attributeField.getFldno());
			attribute.setAval(RandomStringUtils.randomAlphanumeric(50));
			attribute.setUserId(0);
			attribute.setAdate(germplasm.getGdate());

			this.germplasmDataDM.addGermplasmAttribute(attribute);

			fields.put(attributeField.getFcode(), attribute.getAval());
		}

		// names
		final Map<String, String> names = new HashMap<>();
		names.put("GENUS", "");
		names.put("ACCNO", "");

		for (final Map.Entry<String, String> nameEntry : names.entrySet()) {
			UserDefinedField attributeField =
				this.userDefinedFieldDao.getByTableTypeAndCode("NAMES", "NAME", nameEntry.getKey());

			if (attributeField == null) {
				attributeField = new UserDefinedField(null, "NAMES", "NAME", nameEntry.getKey(), "", "", "", 0, 0, 0, 0);
				this.germplasmDataDM.addUserDefinedField(attributeField);
			}

			final Name name = GermplasmTestDataInitializer.createGermplasmName(germplasmGID, RandomStringUtils.randomAlphanumeric(50));
			name.setTypeId(attributeField.getFldno());
			name.setNstat(0); // TODO Review
			this.germplasmDataDM.addGermplasmName(name);

			names.put(nameEntry.getKey(), name.getNval());
		}

		final GermplasmSearchRequestDto request = new GermplasmSearchRequestDto();
		request.setGermplasmDbIds(Lists.newArrayList(germplasmGID.toString()));
		final List<GermplasmDTO> result = this.dao.getGermplasmDTOList(request, null, null);

		final String displayName = germplasm.getPreferredName().getNval();
		final GermplasmDTO germplasmDTO = result.get(0);

		Assert.assertThat(germplasmDTO.getGermplasmDbId(), is(String.valueOf(germplasmGID)));
		Assert.assertThat(germplasmDTO.getDefaultDisplayName(), is(displayName));
		// Assert.assertThat(germplasmDTO.getAccessionNumber(), is(names.get("ACCNO"))); // FIXME
		Assert.assertThat(germplasmDTO.getGermplasmName(), is(displayName));
		// Assert.assertThat(germplasmDTO.getGermplasmPUI(), is());
		// Assert.assertThat(germplasmDTO.getPedigree(), is());
		// Assert.assertThat(germplasmDTO.getGermplasmSeedSource(), is());
		Assert.assertTrue(StringUtils.isEmpty(germplasmDTO.getCommonCropName()));
		 Assert.assertThat(germplasmDTO.getInstituteCode(), is(fields.get("PROGM")));
		 Assert.assertThat(germplasmDTO.getInstituteName(), is(fields.get("PROGM")));
		Assert.assertThat(germplasmDTO.getBiologicalStatusOfAccessionCode(), nullValue());
		Assert.assertThat(germplasmDTO.getCountryOfOriginCode(), is(fields.get("ORI_COUN")));
		Assert.assertThat(germplasmDTO.getGenus(), is(names.get("GENUS")));
		Assert.assertThat(germplasmDTO.getSpecies(), is(fields.get("SPNAM")));
		Assert.assertThat(germplasmDTO.getSpeciesAuthority(), is(fields.get("SPAUTH")));
		Assert.assertThat(germplasmDTO.getSubtaxa(), is(fields.get("SUBTAX")));
		Assert.assertThat(germplasmDTO.getSubtaxaAuthority(), is(fields.get("STAUTH")));
		// Assert.assertThat(germplasmDTO.getAcquisitionDate(), is(germplasm.getGdate()));
	}

	@Test
	public void testGetProgenitorsByGIDWithPrefName() {
		final String crossName = RandomStringUtils.randomAlphabetic(20);
		final Integer crossId = this.insertGermplasmWithName(crossName);
		final Germplasm crossGermplasm = this.dao.getById(crossId);
		Assert.assertTrue(this.dao.getProgenitorsByGIDWithPrefName(crossId).isEmpty());

		final String progenitor1Name = RandomStringUtils.randomAlphabetic(20);
		final Integer progenitor1ID = this.insertGermplasmWithName(progenitor1Name);
		final String progenitor2Name = RandomStringUtils.randomAlphabetic(20);
		final Integer progenitor2ID = this.insertGermplasmWithName(progenitor2Name);
		this.progenitorDao.save(new Progenitor(crossGermplasm, 3, progenitor1ID));
		this.progenitorDao.save(new Progenitor(crossGermplasm, 4, progenitor2ID));

		final List<Germplasm> progenitors = this.dao.getProgenitorsByGIDWithPrefName(crossId);
		Assert.assertEquals(2, progenitors.size());
		final Germplasm progenitor1FromDB = progenitors.get(0);
		Assert.assertEquals(progenitor1ID, progenitor1FromDB.getGid());
		Assert.assertEquals(progenitor1Name, progenitor1FromDB.getPreferredName().getNval());
		final Germplasm progenitor2FromDB = progenitors.get(1);
		Assert.assertEquals(progenitor2ID, progenitor2FromDB.getGid());
		Assert.assertEquals(progenitor2Name, progenitor2FromDB.getPreferredName().getNval());
	}

	@Test
	public void testGetParentsFromProgenitorsForGIDsMap() {
		final Integer cross1ID = this.insertGermplasmWithName(RandomStringUtils.randomAlphabetic(20));
		final Germplasm cross1Germplasm = this.dao.getById(cross1ID);
		Assert.assertTrue(this.dao.getProgenitorsByGIDWithPrefName(cross1ID).isEmpty());

		final Integer cross2ID = this.insertGermplasmWithName(RandomStringUtils.randomAlphabetic(20));
		final Germplasm cross2Germplasm = this.dao.getById(cross2ID);
		Assert.assertTrue(this.dao.getProgenitorsByGIDWithPrefName(cross2ID).isEmpty());

		final Integer gidNoProgenitor = this.insertGermplasmWithName(RandomStringUtils.randomAlphabetic(20));
		Assert.assertTrue(this.dao.getProgenitorsByGIDWithPrefName(gidNoProgenitor).isEmpty());

		// TODO seed data for listdata and perform assertions on pedigree
		// Create 2 progenitor records for Gid1 = Cross1
		final String cross1progenitor1Name = RandomStringUtils.randomAlphabetic(20);
		final Integer cross1progenitor1ID = this.insertGermplasmWithName(cross1progenitor1Name);
		final String cross1progenitor2Name = RandomStringUtils.randomAlphabetic(20);
		final Integer cross1progenitor2ID = this.insertGermplasmWithName(cross1progenitor2Name);
		this.progenitorDao.save(new Progenitor(cross1Germplasm, 3, cross1progenitor1ID));
		this.progenitorDao.save(new Progenitor(cross1Germplasm, 4, cross1progenitor2ID));

		// Create 3 progenitor records for Gid2 = Cross2
		final String cross2progenitor1Name = RandomStringUtils.randomAlphabetic(20);
		final Integer cross2progenitor1ID = this.insertGermplasmWithName(cross2progenitor1Name);
		final String cross2progenitor2Name = RandomStringUtils.randomAlphabetic(20);
		final Integer cross2progenitor2ID = this.insertGermplasmWithName(cross2progenitor2Name);
		final String cross2progenitor3Name = RandomStringUtils.randomAlphabetic(20);
		final Integer cross2progenitor3ID = this.insertGermplasmWithName(cross2progenitor3Name);
		this.progenitorDao.save(new Progenitor(cross2Germplasm, 3, cross2progenitor1ID));
		this.progenitorDao.save(new Progenitor(cross2Germplasm, 4, cross2progenitor2ID));
		this.progenitorDao.save(new Progenitor(cross2Germplasm, 5, cross2progenitor3ID));

		final Map<Integer, List<GermplasmParent>> progenitorsMap = this.dao.getParentsFromProgenitorsForGIDsMap(Lists.newArrayList(cross1ID, cross2ID, gidNoProgenitor));
		Assert.assertEquals(2, progenitorsMap.size());
		Assert.assertNull(progenitorsMap.get(gidNoProgenitor));
		// Verify progenitors for Cross1
		final List<GermplasmParent> cross1Progenitors = progenitorsMap.get(cross1ID);
		Assert.assertNotNull(cross1Progenitors);
		Assert.assertEquals(2, cross1Progenitors.size());
		final GermplasmParent cross1progenitor1FromDB = cross1Progenitors.get(0);
		Assert.assertEquals(cross1progenitor1ID, cross1progenitor1FromDB.getGid());
		Assert.assertEquals(cross1progenitor1Name, cross1progenitor1FromDB.getDesignation());
		final GermplasmParent cross1progenitor2FromDB = cross1Progenitors.get(1);
		Assert.assertEquals(cross1progenitor2ID, cross1progenitor2FromDB.getGid());
		Assert.assertEquals(cross1progenitor2Name, cross1progenitor2FromDB.getDesignation());

		// Verify progenitors for Cross2
		final List<GermplasmParent> cross2Progenitors = progenitorsMap.get(cross2ID);
		Assert.assertNotNull(cross2Progenitors);
		Assert.assertEquals(3, cross2Progenitors.size());
		final GermplasmParent cross2progenitor1FromDB = cross2Progenitors.get(0);
		Assert.assertEquals(cross2progenitor1ID, cross2progenitor1FromDB.getGid());
		Assert.assertEquals(cross2progenitor1Name, cross2progenitor1FromDB.getDesignation());
		final GermplasmParent cross2progenitor2FromDB = cross2Progenitors.get(1);
		Assert.assertEquals(cross2progenitor2ID, cross2progenitor2FromDB.getGid());
		Assert.assertEquals(cross2progenitor2Name, cross2progenitor2FromDB.getDesignation());
		final GermplasmParent cross2progenitor3FromDB = cross2Progenitors.get(2);
		Assert.assertEquals(cross2progenitor3ID, cross2progenitor3FromDB.getGid());
		Assert.assertEquals(cross2progenitor3Name, cross2progenitor3FromDB.getDesignation());
	}

	private Integer insertGermplasmWithName(final String existingGermplasmNameWithPrefix, final boolean isDeleted) {
		final Germplasm germplasm = GermplasmTestDataInitializer
				.createGermplasmWithPreferredName(existingGermplasmNameWithPrefix);
		germplasm.setDeleted(isDeleted);
		return this.germplasmDataDM.addGermplasm(germplasm, germplasm.getPreferredName());
	}

	private Integer insertGermplasmWithName(final String existingGermplasmNameWithPrefix) {
		return this.insertGermplasmWithName(existingGermplasmNameWithPrefix, false);
	}

	private void initializeGermplasms() {
		final Germplasm fParent =
				GermplasmTestDataInitializer.createGermplasm(20150101, 1, 2, 2, 0, 0, 1, 1, 0, 1, 1, "MethodName", "LocationName");
		final Integer fParentGID = this.germplasmDataDM.addGermplasm(fParent, fParent.getPreferredName());

		final Germplasm mParent =
				GermplasmTestDataInitializer.createGermplasm(20150101, 1, 2, 2, 0, 0, 1, 1, 0, 1, 1, "MethodName", "LocationName");
		final Integer mParentGID = this.germplasmDataDM.addGermplasm(mParent, mParent.getPreferredName());

		final Germplasm mgMember = GermplasmTestDataInitializer
				.createGermplasm(20150101, fParentGID, mParentGID, 2, 0, 0, 1, 1, GermplasmDAOTest.GROUP_ID, 1, 1, "MethodName",
						"LocationName");
		this.germplasmDataDM.addGermplasm(mgMember, mgMember.getPreferredName());
	}

	@Test
	public void testReplacedGermplasm(){
		final Germplasm replacedGermplasm = GermplasmTestDataInitializer.createGermplasm(20150101, 1, 2, 2, 0, 1, 1, 1, 0, 1, 1, "MethodName", "LocationName");
		this.dao.save(replacedGermplasm);
		final Germplasm validGermplasm = GermplasmTestDataInitializer.createGermplasm(20150101, 1, 2, 2, 0, 0, 1, 1, 0, 1, 1, "MethodName", "LocationName");
		this.dao.save(validGermplasm);
		Assert.assertNotNull(replacedGermplasm.getGid());
		Assert.assertNull("Replaced Germplasm will not be retrieve", this.dao.getById(replacedGermplasm.getGid()));
		Assert.assertNotNull(validGermplasm.getGid());
		Assert.assertNotNull("Valid Germplasm will be retrieve", this.dao.getById(validGermplasm.getGid()));
	}

	@Test
	public void testHasExistingCrossesWithSingleMaleParent() {
		final Germplasm femaleParent = GermplasmTestDataInitializer.createGermplasm(20150101, 1, 2, 2, 0, 1, 1, 1, 0, 1, 1, "MethodName", "LocationName");
		this.dao.save(femaleParent);
		final Germplasm maleParent = GermplasmTestDataInitializer.createGermplasm(20150101, 1, 2, 2, 0, 0, 1, 1, 0, 1, 1, "MethodName", "LocationName");
		this.dao.save(maleParent);
		final Germplasm existingCross = GermplasmTestDataInitializer.createGermplasm(20150101, femaleParent.getGid(), maleParent.getGid(), 2, 0, 0, 1, 1, 0, 1, 1, "MethodName", "LocationName");
		this.dao.save(existingCross);
		Assert.assertTrue(this.dao.hasExistingCrosses(femaleParent.getGid().toString(), 1, Collections.singletonList(maleParent.getGid()), null));
		//Check if self is excluded
		Assert.assertFalse(this.dao.hasExistingCrosses(femaleParent.getGid().toString(), 1, Collections.singletonList(maleParent.getGid()), existingCross.getGid().toString()));
	}

	@Test
	public void testHasExistingCrossesWithMultipleMaleParents() {
		final Germplasm femaleParent = GermplasmTestDataInitializer.createGermplasm(20150101, 1, 2, 2, 0, 1, 1, 1, 0, 1, 1, "MethodName", "LocationName");
		this.dao.save(femaleParent);
		final Germplasm maleParent1 = GermplasmTestDataInitializer.createGermplasm(20150101, 1, 2, 2, 0, 0, 1, 1, 0, 1, 1, "MethodName", "LocationName");
		this.dao.save(maleParent1);
		final Germplasm existingCross = GermplasmTestDataInitializer.createGermplasm(20150101, femaleParent.getGid(), maleParent1.getGid(), 2, 0, 0, 1, 1, 0, 1, 1, "MethodName", "LocationName");
		this.dao.save(existingCross);
		final Germplasm maleParent2 = GermplasmTestDataInitializer.createGermplasm(20150101, 1, 2, 2, 0, 0, 1, 1, 0, 1, 1, "MethodName", "LocationName");
		this.dao.save(maleParent2);
		this.progenitorDao.save(new Progenitor(existingCross, 3, maleParent2.getGid()));
		Assert.assertTrue(this.dao.hasExistingCrosses(femaleParent.getGid().toString(), 1, Arrays.asList(maleParent1.getGid(), maleParent2.getGid()), null));
		//Check if self is excluded
		Assert.assertFalse(this.dao.hasExistingCrosses(femaleParent.getGid().toString(), 1, Arrays.asList(maleParent1.getGid(), maleParent2.getGid()), existingCross.getGid().toString()));
	}

}
