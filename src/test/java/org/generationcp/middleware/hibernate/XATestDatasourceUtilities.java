
package org.generationcp.middleware.hibernate;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Matchers;
import org.mockito.Mockito;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import com.google.common.collect.ImmutableMap;
import com.mysql.jdbc.PreparedStatement;

public class XATestDatasourceUtilities {

	private static final String PROPERTY1 = "Property1";
	private static final String PROPERTY2 = "Property2";
	private static final String PROPERTY3 = "Property3";

	private static final String PROPERTY_VALUE3 = "PropertyValue3";
	private static final String PROPERTY_VALUE2 = "PropertyValue2";
	private static final String PROPERTY_VALUE1 = "PropertyValue1";

	private static final String ATTRIBUTE1 = "Attribute1";
	private static final String ATTRIBUTE2 = "Attribute2";
	private static final String ATTRIBUTE3 = "Attribute3";

	private static final String ATTRIBUTE_VALUE1 = "AttributeValue1";
	private static final String ATTRIBUTE_VALUE2 = "AttributeValue2";
	private static final String ATTRIBUTE_VALUE3 = "AttributeValue3";

	/**
	 * Make sure that properties and attributes are set correctly into the root bean definition.
	 *
	 * @throws Exception in case of any error with the test
	 */
	@Test
	public void testCreateRootBeanDefinition() throws Exception {

		final DatasourceUtilities xaDatasourceUtilities = new DatasourceUtilities();

		final Map<String, Object> attributes =
				ImmutableMap.<String, Object>of(XATestDatasourceUtilities.ATTRIBUTE1, XATestDatasourceUtilities.ATTRIBUTE_VALUE1,
						XATestDatasourceUtilities.ATTRIBUTE2, XATestDatasourceUtilities.ATTRIBUTE_VALUE2,
						XATestDatasourceUtilities.ATTRIBUTE3, XATestDatasourceUtilities.ATTRIBUTE_VALUE3);

		final Map<String, Object> properties =
				ImmutableMap.<String, Object>of(XATestDatasourceUtilities.PROPERTY1, XATestDatasourceUtilities.PROPERTY_VALUE1,
						XATestDatasourceUtilities.PROPERTY2, XATestDatasourceUtilities.PROPERTY_VALUE2,
						XATestDatasourceUtilities.PROPERTY3, XATestDatasourceUtilities.PROPERTY_VALUE3);

		final RootBeanDefinition rootBeanDefinition =
				xaDatasourceUtilities.createRootBeanDefinition(DatasourceUtilities.class, attributes, properties);
		Assert.assertEquals("Root bean definition must have attribute 1", XATestDatasourceUtilities.ATTRIBUTE_VALUE1,
				rootBeanDefinition.getAttribute(XATestDatasourceUtilities.ATTRIBUTE1));
		Assert.assertEquals("Root bean definition must have attribute 2", XATestDatasourceUtilities.ATTRIBUTE_VALUE2,
				rootBeanDefinition.getAttribute(XATestDatasourceUtilities.ATTRIBUTE2));
		Assert.assertEquals("Root bean definition must have attribute 3", XATestDatasourceUtilities.ATTRIBUTE_VALUE3,
				rootBeanDefinition.getAttribute(XATestDatasourceUtilities.ATTRIBUTE3));

		final MutablePropertyValues propertyValues = rootBeanDefinition.getPropertyValues();
		Assert.assertEquals("Root bean definition must have attribute 1", XATestDatasourceUtilities.PROPERTY_VALUE1, propertyValues
				.getPropertyValue(XATestDatasourceUtilities.PROPERTY1).getValue());
		Assert.assertEquals("Root bean definition must have attribute 1", XATestDatasourceUtilities.PROPERTY_VALUE2, propertyValues
				.getPropertyValue(XATestDatasourceUtilities.PROPERTY2).getValue());
		Assert.assertEquals("Root bean definition must have attribute 1", XATestDatasourceUtilities.PROPERTY_VALUE3, propertyValues
				.getPropertyValue(XATestDatasourceUtilities.PROPERTY3).getValue());

	}

	@Test
	public void testGetSingleConnectionDataSource() throws Exception {
		final DatasourceUtilities xaDatasourceUtilities = new DatasourceUtilities();
		final DataSourceProperties xaDataSourceProperties = XATestUtility.mockProperties();

		final SingleConnectionDataSource singleConnectionDataSource =
				xaDatasourceUtilities.getSingleConnectionDataSource(xaDataSourceProperties);
		Assert.assertEquals("Username must be what we set it to", XATestUtility.DB_USERNAME, singleConnectionDataSource.getUsername());
		Assert.assertEquals("Password must be what we set it to", XATestUtility.DB_PASSWORD, singleConnectionDataSource.getPassword());

		Assert.assertEquals("Url must get correctly deriver", "jdbc:mysql://" + XATestUtility.DB_HOST + ":" + XATestUtility.DB_PORT + "/"
				+ XATestUtility.DB_WORKBENCH_NAME, singleConnectionDataSource.getUrl());

	}

	@Test
	public void testRetrieveMergedDatabases() throws Exception {
		final DatasourceUtilities xaDatasourceUtilities = new DatasourceUtilities();
		final SingleConnectionDataSource singleConnectionDataSource = Mockito.mock(SingleConnectionDataSource.class);
		final Connection mockConnection = Mockito.mock(Connection.class);
		final PreparedStatement mockPreparedStatement = Mockito.mock(PreparedStatement.class);
		final ResultSet mockResultSet = Mockito.mock(ResultSet.class);

		Mockito.when(singleConnectionDataSource.getConnection()).thenReturn(mockConnection);
		Mockito.when(mockConnection.prepareStatement(Matchers.anyString())).thenReturn(mockPreparedStatement);
		Mockito.when(mockPreparedStatement.executeQuery()).thenReturn(mockResultSet);
		Mockito.when(mockResultSet.next()).thenReturn(true).thenReturn(true).thenReturn(false);
		Mockito.when(mockResultSet.getString(1)).thenReturn("DB1").thenReturn("DB2");

		final List<String> retrieveMergedDatabases = xaDatasourceUtilities.retrieveCropDatabases(singleConnectionDataSource);
		Assert.assertTrue("Must contain DB1", retrieveMergedDatabases.contains("DB1"));
		Assert.assertTrue("Must contain DB2", retrieveMergedDatabases.contains("DB2"));

	}

	@Test(expected = IllegalStateException.class)
	public void testRetrieveMergedDatabasesExceptionalCase() throws Exception {
		final DatasourceUtilities xaDatasourceUtilities = new DatasourceUtilities();
		final SingleConnectionDataSource singleConnectionDataSource = Mockito.mock(SingleConnectionDataSource.class);
		Mockito.when(singleConnectionDataSource.getConnection()).thenThrow(new SQLException("Could not access the database"));

		xaDatasourceUtilities.retrieveCropDatabases(singleConnectionDataSource);
	}
}
