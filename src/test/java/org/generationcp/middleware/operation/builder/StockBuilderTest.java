package org.generationcp.middleware.operation.builder;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import org.apache.commons.lang.RandomStringUtils;
import org.generationcp.middleware.IntegrationTestBase;
import org.generationcp.middleware.domain.dms.DMSVariableType;
import org.generationcp.middleware.domain.dms.StandardVariable;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.dms.StockModel;
import org.generationcp.middleware.pojos.dms.StockProperty;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

public class StockBuilderTest {
	
	private StockBuilder stockBuilder;
	
	@Before
	public void setup() {
		this.stockBuilder = new StockBuilder(Mockito.mock(HibernateSessionProvider.class));
	}
	
	@Test
	public void testGetValueForEntryNo() {
		final StockModel stockModel = this.createStockModel();
		final String value = this.stockBuilder.getValue(stockModel, this.createDMSVariableType(TermId.ENTRY_NO));
		Assert.assertEquals(stockModel.getUniqueName(), value);
	}
	
	@Test
	public void testGetValueForGID() {
		final StockModel stockModel = this.createStockModel();
		final String value = this.stockBuilder.getValue(stockModel, this.createDMSVariableType(TermId.GID));
		Assert.assertEquals(stockModel.getGermplasm().getGid().toString(), value);
	}
	
	@Test
	public void testGetValueForDesignation() {
		final StockModel stockModel = this.createStockModel();
		final String value = this.stockBuilder.getValue(stockModel, this.createDMSVariableType(TermId.DESIG));
		Assert.assertEquals(stockModel.getName(), value);
	}
	
	@Test
	public void testGetValueForEntryCode() {
		final StockModel stockModel = this.createStockModel();
		final String value = this.stockBuilder.getValue(stockModel, this.createDMSVariableType(TermId.ENTRY_CODE));
		Assert.assertEquals(stockModel.getValue(), value);
	}
	
	@Test
	public void testGetValueForExistingStockProperty() {
		final StockModel stockModel = this.createStockModel();
		final String value = this.stockBuilder.getValue(stockModel, this.createDMSVariableType(TermId.ENTRY_TYPE));
		Assert.assertEquals(stockModel.getProperties().iterator().next().getValue(), value);
	}
	
	@Test
	public void testGetValueForNonExistingStockProperty() {
		final StockModel stockModel = this.createStockModel();
		final String value = this.stockBuilder.getValue(stockModel, this.createDMSVariableType(TermId.STOCKID));
		Assert.assertNull(value);
	}
	
	private StockModel createStockModel() {
		final StockModel stockModel = new StockModel();
		stockModel.setUniqueName(RandomStringUtils.randomAlphanumeric(20));
		stockModel.setGermplasm(new Germplasm(new Random().nextInt(Integer.MAX_VALUE)));
		stockModel.setName(RandomStringUtils.randomAlphanumeric(20));
		stockModel.setValue(RandomStringUtils.randomAlphanumeric(20));
		
		final Set<StockProperty> stockProperties = new HashSet<>();
		final StockProperty stockProperty = new StockProperty();
		stockProperty.setStock(stockModel);
		stockProperty.setValue(RandomStringUtils.randomAlphanumeric(20));
		stockProperty.setTypeId(TermId.ENTRY_TYPE.getId());
		stockProperties.add(stockProperty);

		stockModel.setProperties(stockProperties);
		return stockModel;
	}
	
	private DMSVariableType createDMSVariableType(final TermId termId) {
		final DMSVariableType dmsVariableType = new DMSVariableType();
		final StandardVariable standardVariable = new StandardVariable();
		standardVariable.setId(termId.getId());
		dmsVariableType.setStandardVariable(standardVariable);
		return dmsVariableType;
	}
		

}
