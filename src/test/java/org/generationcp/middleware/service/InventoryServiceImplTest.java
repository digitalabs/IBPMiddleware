
package org.generationcp.middleware.service;

import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.IntegrationTestBase;
import org.generationcp.middleware.dao.ims.TransactionDAO;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.service.api.InventoryService;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;

public class InventoryServiceImplTest extends IntegrationTestBase {

	@Autowired
	private InventoryService inventoryService;

	public static final String TEST_INVENTORY_ID = "TR1-123";

	@Test
	public void testGetCurrentNotificationNumber() throws MiddlewareException {
		Integer currentNotificationNumber = this.inventoryService.getCurrentNotationNumberForBreederIdentifier("TR");
		Assert.assertEquals(2, currentNotificationNumber.intValue());
	}

	@Test
	public void testMockedGetCurrentNotationNumberForBreederIdentifier() throws MiddlewareException {
		List<String> inventoryIDs = new ArrayList<>();
		inventoryIDs.add("PRE1-12");
		inventoryIDs.add("PRE1-13");
		inventoryIDs.add("PRE1-14");
		inventoryIDs.add("PRE2-1");
		inventoryIDs.add("PRE3-1");
		inventoryIDs.add("PRE35-1");

		TransactionDAO dao = Mockito.mock(TransactionDAO.class);
		InventoryServiceImpl dut = Mockito.spy(new InventoryServiceImpl(Mockito.mock(HibernateSessionProvider.class), ""));
		Mockito.doReturn(dao).when(dut).getTransactionDao();
		Mockito.when(dao.getInventoryIDsWithBreederIdentifier(Matchers.anyString())).thenReturn(inventoryIDs);

		Integer currentNotationNumber = dut.getCurrentNotationNumberForBreederIdentifier("PRE");
		Assert.assertEquals(35, currentNotationNumber.intValue());

	}
}
