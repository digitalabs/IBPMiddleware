/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/

package org.generationcp.middleware.dao;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.generationcp.middleware.IntegrationTestBase;
import org.generationcp.middleware.dao.ims.LotDAO;
import org.generationcp.middleware.dao.ims.TransactionDAO;
import org.generationcp.middleware.data.initializer.GermplasmTestDataInitializer;
import org.generationcp.middleware.data.initializer.InventoryDetailsTestDataInitializer;
import org.generationcp.middleware.domain.gms.search.GermplasmSearchParameter;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.ListDataProject;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.ims.Lot;
import org.generationcp.middleware.pojos.ims.Transaction;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.collect.Lists;

public class GermplasmDAOTest extends IntegrationTestBase {

	private static final String DUMMY_STOCK_ID = "USER-1-1";
    private static final Integer TEST_PROJECT_ID = 1;

	private static final Integer GROUP_ID = 10;

	private boolean testDataSetup = false;

	private GermplasmDAO dao;

	private LotDAO lotDAO;

	private TransactionDAO transactionDAO;

	private Integer germplasmGID;

	private Name preferredName;

	private ListDataProjectDAO listDataProjectDAO;

	private GermplasmListDAO germplasmListDAO;

	@Autowired
	private InventoryDataManager inventoryDM;

	@Autowired
	private GermplasmDataManager germplasmDataDM;

	@Before
	public void setUp() throws Exception {
		if (this.dao == null) {
			this.dao = new GermplasmDAO();
			this.dao.setSession(this.sessionProvder.getSession());

			this.lotDAO  = new LotDAO();
			this.lotDAO.setSession(this.sessionProvder.getSession());

			this.transactionDAO = new TransactionDAO();
			this.transactionDAO.setSession(this.sessionProvder.getSession());

			this.listDataProjectDAO = new ListDataProjectDAO();
			this.listDataProjectDAO.setSession(this.sessionProvder.getSession());

			this.germplasmListDAO = new GermplasmListDAO();
			this.germplasmListDAO.setSession(this.sessionProvder.getSession());

		}

		if (!this.testDataSetup) {
			this.updateInventory();
			this.testDataSetup = true;
		}
		this.initializeGermplasms();
	}

	private void updateInventory() throws MiddlewareQueryException {
		final List<Transaction> transactions = this.inventoryDM.getAllTransactions(0, 1);
		if (transactions != null && !transactions.isEmpty()) {
			final Transaction transaction = transactions.get(0);
			transaction.setInventoryID(GermplasmDAOTest.DUMMY_STOCK_ID);
			this.inventoryDM.updateTransaction(transaction);
		}
	}

	@Test
	public void testGetDerivativeChildren() throws Exception {
		final Germplasm parentGermplsm = GermplasmTestDataInitializer.createGermplasm(20150101, 1, 1, -1, 0, 0 , 1 ,1 ,0, 1 ,1 , "MethodName",
				"LocationName");
		this.germplasmDataDM.addGermplasm(parentGermplsm, parentGermplsm.getPreferredName());

		final Germplasm childDerivativeGermplsm = GermplasmTestDataInitializer.createGermplasm(20150101, 1, parentGermplsm.getGid(), -1, 0, 0
				, 1	,1 ,0, 1 ,1 , "MethodName", "LocationName");
		this.germplasmDataDM.addGermplasm(childDerivativeGermplsm, childDerivativeGermplsm.getPreferredName());

		final List<Germplasm> results = this.dao.getChildren(parentGermplsm.getGid(), 'D');
		Assert.assertNotNull(results);
		Assert.assertEquals(childDerivativeGermplsm.getGid(), results.get(0).getGid());
	}

	@Test
	public void testGetMaintenanceChildren() throws Exception {
		final Germplasm parentGermplsm = GermplasmTestDataInitializer.createGermplasm(20150101, 1, 1, -1, 0, 0 , 1 ,1 ,0, 1 ,1 , "MethodName",
				"LocationName");
		this.germplasmDataDM.addGermplasm(parentGermplsm, parentGermplsm.getPreferredName());

		final Germplasm maintenanceChildrenGermplsm = GermplasmTestDataInitializer.createGermplasm(20150101, 1, parentGermplsm.getGid(),
				-1, 0, 0, 1	, 80 ,0, 1 ,1 , "MethodName", "LocationName");
		this.germplasmDataDM.addGermplasm(maintenanceChildrenGermplsm, maintenanceChildrenGermplsm.getPreferredName());

		final List<Germplasm> results = this.dao.getChildren(parentGermplsm.getGid(), 'M');
		Assert.assertNotNull(results);
		Assert.assertNotNull(results);
		Assert.assertEquals(maintenanceChildrenGermplsm.getGid(), results.get(0).getGid());

	}

	@Test
	public void testSearchForGermplasmsExactMatchGID() throws Exception {
		final List<Germplasm> results =
				this.dao.searchForGermplasms(this.createSearchParam(this.germplasmGID.toString(), Operation.EQUAL, false, false, false));
		Assert.assertEquals("The results should contain only one germplasm since the gid is unique.", 1, results.size());
		this.assertPossibleGermplasmFields(results);
	}

    @Test
	public void testRetrieveStudyParentGIDsKnownValuesOnly() {

		final Germplasm germplasm = GermplasmTestDataInitializer.createGermplasm(20150101, 12, 13, 1, 0, 0 , 1 ,1 ,0, 1 ,1 , "MethodName",
				"LocationName");
		this.germplasmDataDM.addGermplasm(germplasm, germplasm.getPreferredName());


		// Germplasm list
		final GermplasmList germplasmList = new GermplasmList(null, "Test Germplasm List " + 1,
				Long.valueOf(20141014), "LST", Integer.valueOf(1), "Test Germplasm List", null, 1);
		germplasmList.setProjectId(TEST_PROJECT_ID);
		this.germplasmListDAO.save(germplasmList);

		final ListDataProject listDataProject = new ListDataProject();
		listDataProject.setCheckType(0);
		listDataProject.setList(germplasmList);
		listDataProject.setGermplasmId(germplasm.getGid());
		listDataProject.setDesignation("Deignation");
		listDataProject.setEntryId(1);
		listDataProject.setEntryCode("entryCode");
		listDataProject.setSeedSource("seedSource");
		listDataProject.setGroupName("grpName");
		this.listDataProjectDAO.save(listDataProject);

		final List<Germplasm> germplasmEntries = this.dao.getGermplasmParentsForStudy(TEST_PROJECT_ID);

		Assert.assertEquals(1, germplasmEntries.size());
		Assert.assertEquals(germplasm.getGid(), germplasmEntries.get(0).getGid());
		Assert.assertEquals(germplasm.getGpid1(), germplasmEntries.get(0).getGpid1());
		Assert.assertEquals(germplasm.getGpid2(), germplasmEntries.get(0).getGpid2());
		Assert.assertEquals(germplasm.getGrplce(), germplasmEntries.get(0).getGrplce());
    }


	@Test
	public void testSearchForGermplasmsExactMatchGermplasmName() throws Exception {
		final List<Germplasm> results =
				this.dao.searchForGermplasms(this.createSearchParam(this.preferredName.getNval(), Operation.EQUAL, false, false, false));
		Assert.assertEquals(
				"The results should contain one germplasm since there's only one test data with '" + this.preferredName.getNval()
						+ "' name", 1, results.size());
		this.assertPossibleGermplasmFields(results);
	}

	@Test
	public void testSearchForGermplasmsStartsWithGID() throws Exception {
		final List<Germplasm> results =
				this.dao.searchForGermplasms(this.createSearchParam(this.germplasmGID.toString() + "%", Operation.LIKE, false, false, false));
		Assert.assertEquals("The results should contain one germplasm since there's only one test data with gid that starts with "
				+ this.germplasmGID, 1, results.size());
		this.assertPossibleGermplasmFields(results);
	}

	@Test
	public void testSearchForGermplasmsStartsWithGermplasmName() throws Exception {

		final Germplasm germplasm = GermplasmTestDataInitializer.createGermplasm(20150101, 12, 13, 1, 0, 0 , 1 ,1 ,0, 1 ,1 , "MethodName",
				"LocationName");
		germplasm.getPreferredName().setNval("GermplasmName");
		this.germplasmDataDM.addGermplasm(germplasm, germplasm.getPreferredName());

		final List<Germplasm> results =
				this.dao.searchForGermplasms(this.createSearchParam(germplasm.getPreferredName().getNval() + "%", Operation.LIKE, false, false, false));
		Assert.assertEquals("The results should contain one germplasm since there's only one test data with name that starts with "
				+ germplasm.getPreferredName().getNval(), 1, results.size());
		Assert.assertTrue(germplasm.getPreferredName().getNval().contains("GermplasmName"));
	}

	@Test
	public void testSearchForGermplasmsContainsGID() throws Exception {
		final List<Germplasm> results =
				this.dao.searchForGermplasms(this.createSearchParam("%" + this.germplasmGID.toString() + "%", Operation.LIKE, false, false,
						false));
		Assert.assertEquals("The results should contain one germplasm since there's only one test data with gid that contains "
				+ this.germplasmGID, 1, results.size());
		this.assertPossibleGermplasmFields(results);
	}

	@Test
	public void testSearchForGermplasmsContainsGermplasmName() throws Exception {
		final List<Germplasm> results =
				this.dao.searchForGermplasms(this.createSearchParam("%" + this.preferredName.getNval() + "%", Operation.LIKE, false, false,
						false));
		Assert.assertTrue("The results should contain one germplasm since there's only one test data with name that contains "
				+ this.preferredName.getNval(), results.size() == 1);
		this.assertPossibleGermplasmFields(results);
	}

	@Test
	public void testSearchForGermplasmsWithInventory() throws Exception {

		final Germplasm germplasm = GermplasmTestDataInitializer.createGermplasm(20150101, 1, 2, 2, 0, 0 , 1 ,1 ,0, 1 ,1 , "MethodName",
				"LocationName");
		final Integer germplasmId = this.germplasmDataDM.addGermplasm(germplasm, germplasm.getPreferredName());

		Lot lot = InventoryDetailsTestDataInitializer.createLot(1, "GERMPLSM", germplasmId, 1, 8124, 0, 1, "Comments");
		this.lotDAO.save(lot);

		Transaction transaction = InventoryDetailsTestDataInitializer
				.createReservationTransaction(2.0, 0, "2 reserved", lot, 1, 1, 1, "LIST");
		transaction.setInventoryID(this.preferredName.getNval());
		this.transactionDAO.save(transaction);

		final List<Germplasm> resultsWithInventoryOnly = this.dao.searchForGermplasms(this.preferredName.getNval(), Operation.LIKE, false, true, false);
		this.assertPossibleGermplasmFieldsForInventorySearch(resultsWithInventoryOnly);

		Assert.assertEquals(1, resultsWithInventoryOnly.size());
		Assert.assertEquals(1, resultsWithInventoryOnly.get(0).getInventoryInfo().getActualInventoryLotCount().intValue());

	}

	@Test
	public void testSearchForGermplasmsIncludeParents() throws Exception {

		final Germplasm parentGermplasm = GermplasmTestDataInitializer.createGermplasm(20150101, 1, 2, 2, 0, 0 , 1 ,1 ,0, 1 ,1 ,
				"MethodName", "LocationName");
		final Integer parentGermplasmId = this.germplasmDataDM.addGermplasm(parentGermplasm, parentGermplasm.getPreferredName());

		final Germplasm childGermplasm = GermplasmTestDataInitializer.createGermplasm(20150101, parentGermplasm.getGid(), 2, 2, 0, 0 , 1 ,1 ,
				0, 1 ,1 , "MethodName", "LocationName");
		final Integer childGermplasmId = this.germplasmDataDM.addGermplasm(childGermplasm, childGermplasm.getPreferredName());

		final List<Germplasm> results =
				this.dao.searchForGermplasms(this.createSearchParam(childGermplasm.getGid().toString(), Operation.EQUAL, true, false,
						false));

		Assert.assertTrue("Result should include both child and parent germplasms", results.size() >=2 );
		List<Integer> resultGIDs = Lists.newArrayList();
		for(Germplasm germplasm : results) {
			resultGIDs.add(germplasm.getGid());
		}

		Assert.assertTrue("Parent germplasm should be included in search result", resultGIDs.contains(parentGermplasmId));
		Assert.assertTrue("Child germplasm should be included in search result", resultGIDs.contains(childGermplasmId));
	}

	@Test
	public void testSearchForGermplasmsEmptyKeyword() throws Exception {
		final List<Germplasm> results = this.dao.searchForGermplasms(this.createSearchParam("", Operation.EQUAL, false, false, false));
		Assert.assertTrue(results.isEmpty());
	}

	@Test
	public void testSearchForGermplasmsIncludeMGMembers() throws Exception {
		final List<Germplasm> results =
				this.dao.searchForGermplasms(this.createSearchParam(this.germplasmGID.toString(), Operation.EQUAL, false, false, true));
		Assert.assertEquals("The result should contain 2 germplasms (one is the actual result and the other is the MG member)", 2,
				results.size());
		this.assertPossibleGermplasmFields(results);
	}

	@Test
	public void testGetAllChildren() {
		final Germplasm parentGermplsm = GermplasmTestDataInitializer.createGermplasm(20150101, 1, 1, -1, 0, 0 , 1 ,1 ,0, 1 ,1 , "MethodName",
				"LocationName");
		this.germplasmDataDM.addGermplasm(parentGermplsm, parentGermplsm.getPreferredName());

		final Germplasm childDerivativeGermplsm = GermplasmTestDataInitializer.createGermplasm(20150101, 1, parentGermplsm.getGid(), -1, 0, 0
				, 1	,1 ,0, 1 ,1 , "MethodName", "LocationName");
		this.germplasmDataDM.addGermplasm(childDerivativeGermplsm, childDerivativeGermplsm.getPreferredName());

		final Germplasm maintenanceChildrenGermplsm = GermplasmTestDataInitializer.createGermplasm(20150101, 1, parentGermplsm.getGid(),
				-1, 0, 0, 1	, 80 ,0, 1 ,1 , "MethodName", "LocationName");
		this.germplasmDataDM.addGermplasm(maintenanceChildrenGermplsm, maintenanceChildrenGermplsm.getPreferredName());

		final List<Germplasm> children = this.dao.getAllChildren(parentGermplsm.getGid());
		Assert.assertNotNull("getAllChildren() should never return null.", children);

		List<Integer> resultChildGermplasmIds = Lists.newArrayList();

		for(Germplasm germplasm : children) {
			resultChildGermplasmIds.add(germplasm.getGid());
		}

		Assert.assertTrue("Derivative child Germplasm should be included in search result", resultChildGermplasmIds.contains
				(childDerivativeGermplsm.getGid()));
		Assert.assertTrue("Maintenance child Germplasm should be included in search result",resultChildGermplasmIds.contains
				(maintenanceChildrenGermplsm.getGid()));
	}

	@Test
	public void testGetPreviousCrosses() {
		final Germplasm female = GermplasmTestDataInitializer.createGermplasm(20150101, 1, 2, 2, 0, 0 , 1 ,1 ,0, 1 ,1 , "MethodName",
				"LocationName");
		this.germplasmDataDM.addGermplasm(female, female.getPreferredName());

		final Germplasm male = GermplasmTestDataInitializer.createGermplasm(20150101, 1, 2, 2, 0, 0 , 1 ,1 ,0, 1 ,1 , "MethodName",
				"LocationName");
		this.germplasmDataDM.addGermplasm(male, male.getPreferredName());


		final Germplasm currentCross = GermplasmTestDataInitializer.createGermplasm(20150101, 1, 2, 2, 0, 0 , 1 ,1 ,0, 1 ,1 , "MethodName",
				"LocationName");
		currentCross.setGpid1(female.getGid());
		currentCross.setGpid2(male.getGid());

		this.germplasmDataDM.addGermplasm(currentCross, currentCross.getPreferredName());

		final Germplasm previousCross = GermplasmTestDataInitializer.createGermplasm(20150101, 1, 2, 2, 0, 0 , 1 ,1 ,0, 1 ,1 , "MethodName",
				"LocationName");
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

		final Germplasm germplasm1 = GermplasmTestDataInitializer.createGermplasm(20150101, 1, 2, 2, 0, 0 , 1 ,1 ,0, 1 ,1 , "MethodName",
				"LocationName");
		final Germplasm germplasm2 = GermplasmTestDataInitializer.createGermplasm(20150101, 1, 2, 2, 0, 0 , 1 ,1 ,0, 1 ,1 , "MethodName",
				"LocationName");

		this.dao.save(germplasm1);
		this.dao.save(germplasm2);

		Set<Integer> gids = new HashSet<>();
		gids.add(germplasm1.getGid());
		gids.add(germplasm2.getGid());

		Long result = this.dao.countMatchGermplasmInList(gids);

		Assert.assertEquals("The number of gids in list should match the count of records matched in the database.", gids.size(), result.intValue());

	}

	@Test
	public void testCountMatchGermplasmInListOnlyOneGidExists() {

		Set<Integer> gids = new HashSet<>();
		Integer dummyGid = Integer.MIN_VALUE + 1;

		final Germplasm germplasm1 =GermplasmTestDataInitializer.createGermplasm(20150101, 1, 2, 2, 0, 0 , 1 ,1 ,0, 1 ,1 , "MethodName",
				"LocationName");
		this.dao.save(germplasm1);

		final Germplasm germplasm = this.dao.getById(dummyGid);
		Assert.assertNull("Make sure that gid " + dummyGid + " doesn't exist." ,germplasm);

		gids.add(germplasm1.getGid());
		gids.add(dummyGid);

		Long result = this.dao.countMatchGermplasmInList(gids);

		Assert.assertEquals("Only one gid has a match in the database.", 1, result.intValue());

	}

	@Test
	public void testCountMatchGermplasmInListNoGidExists() {

		Integer dummyGid = Integer.MIN_VALUE + 1;;

		Set<Integer> gids = new HashSet<>();

		Germplasm germplasm = this.dao.getById(dummyGid);

		Assert.assertNull("We're testing a gid that doesnt exist, so the germplasm should be null." ,germplasm);

		// Add dummy gid that do not exist in the database
		gids.add(dummyGid);

		Long result = this.dao.countMatchGermplasmInList(gids);

		Assert.assertEquals("The count should be zero because the gid in the list doesn't exist.",0, result.intValue());

	}

	@Test
	public void testCountMatchGermplasmInListGidListIsNullOrEmpty() {

		Long result1 = this.dao.countMatchGermplasmInList(null);
		Assert.assertEquals("The count should be zero because the gid list is null", 0, result1.intValue());

		Long result2 = this.dao.countMatchGermplasmInList(new HashSet<Integer>());
		Assert.assertEquals("The count should be zero because the gid list is empty", 0, result2.intValue());

	}

	private void initializeGermplasms() {
		final Germplasm fParent = GermplasmTestDataInitializer.createGermplasm(20150101, 1, 2, 2, 0, 0 , 1 ,1 ,0, 1 ,1 , "MethodName", "LocationName");
		final Integer fParentGID = this.germplasmDataDM.addGermplasm(fParent, fParent.getPreferredName());

		final Germplasm mParent = GermplasmTestDataInitializer.createGermplasm(20150101, 1, 2, 2, 0, 0 , 1 ,1 ,0, 1 ,1 , "MethodName", "LocationName");
		final Integer mParentGID = this.germplasmDataDM.addGermplasm(mParent, mParent.getPreferredName());

		final Germplasm germplasm = GermplasmTestDataInitializer.createGermplasm(20150101, fParentGID, mParentGID, 2, 0, 0 , 1 ,1 ,GermplasmDAOTest.GROUP_ID, 1 ,1 , "MethodName", "LocationName");
		this.preferredName = germplasm.getPreferredName();
		this.germplasmGID = this.germplasmDataDM.addGermplasm(germplasm, germplasm.getPreferredName());

		final Germplasm mgMember = GermplasmTestDataInitializer.createGermplasm(20150101, fParentGID, mParentGID, 2, 0, 0 , 1 ,1 ,GermplasmDAOTest.GROUP_ID, 1 ,1 , "MethodName", "LocationName");
		this.germplasmDataDM.addGermplasm(mgMember, mgMember.getPreferredName());
	}

	private GermplasmSearchParameter createSearchParam(final String searchKeyword, final Operation operation, final boolean includeParents,
			final boolean withInventoryOnly, final boolean includeMGMembers) {
		final GermplasmSearchParameter searchParam =
				new GermplasmSearchParameter(searchKeyword, operation, includeParents, withInventoryOnly, includeMGMembers);
		searchParam.setStartingRow(0);
		searchParam.setNumberOfEntries(25);
		return searchParam;
	}

	/**
	 * Method to assert fields contained by germplasm search germplasmSearchResults.
	 * Tried to assert general possible fields for Germplasm.
	 *
	 * @param germplasmSearchResults Germplasm Search Results
	 */
	private void assertPossibleGermplasmFields(List<Germplasm> germplasmSearchResults) {
		// Assert possible germplasm member fields
		for (Germplasm germplasm : germplasmSearchResults) {
			Assert.assertNotEquals("Gpid1 should not be 0", Integer.valueOf(0), germplasm.getGpid1());
			Assert.assertNotEquals("Gpid2 should not be 0", Integer.valueOf(0), germplasm.getGpid2());
			Assert.assertNotEquals("Gnpgs should not be 0", Integer.valueOf(0), germplasm.getGnpgs());
			Assert.assertEquals("Result should contain Method Name", "Unknown generative method", germplasm.getMethodName());
			Assert.assertEquals("Result should contain Location Name", "Afghanistan", germplasm.getLocationName());
			Assert.assertEquals("Result should contain Germplasm Number of Progenitor", Integer.valueOf(2), germplasm.getGnpgs());
			Assert.assertEquals("Result should contain Germplasm Date", Integer.valueOf(20150101), germplasm.getGdate());
			Assert.assertEquals("Result should contain Reference Id", Integer.valueOf(1), germplasm.getReferenceId());
		}
	}

	/**
	 * Method to assert fields contained by germplasm inventory search inventorySearchResults.
	 * Tried to assert general possible fields for Germplasm.
	 *
	 * @param inventorySearchResults Germplasm Search Results
	 */
	private void assertPossibleGermplasmFieldsForInventorySearch(List<Germplasm> inventorySearchResults) {
		for (Germplasm inventory : inventorySearchResults) {
			Assert.assertNotNull("Result should contain Inventory Info", inventory.getInventoryInfo());
			Assert.assertNotNull("Result should contain Method Name", inventory.getMethodName());
			Assert.assertNotNull("Result should contain Location Name", inventory.getLocationName());
			Assert.assertNotEquals("Gid should not be 0", Integer.valueOf(0), inventory.getGid());
			Assert.assertNotNull("Result should contain ReferenceId", inventory.getReferenceId());
			Assert.assertNotNull("Result should contain Gdate", inventory.getGdate());
		}
	}
}
