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

package org.generationcp.middleware.pojos.gdms;

import java.util.List;

import org.generationcp.middleware.IntegrationTestBase;
import org.generationcp.middleware.utils.test.Debug;
import org.hibernate.Query;
import org.hibernate.Session;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

@SuppressWarnings("rawtypes")
public class GDMSPojosSimpleTest extends IntegrationTestBase {

	private Session session;

	@Before
	public void setUp() throws Exception {
		if (this.session == null) {
			this.session = this.sessionProvder.getSession();
		}
	}

	@Test
	public void testAccMetadataSet() {
		Query query = this.session.createQuery("FROM AccMetadataSet");
		query.setMaxResults(5);
		List results = query.list();

		for (Object obj : results) {
			Assert.assertTrue(obj instanceof AccMetadataSet);
			Assert.assertTrue(obj != null);
			AccMetadataSet element = (AccMetadataSet) obj;
			Debug.println(IntegrationTestBase.INDENT, element);
		}
	}

	@Test
	public void testAlleleValues() {
		Query query = this.session.createQuery("FROM AlleleValues");
		query.setMaxResults(5);
		List results = query.list();

		for (Object obj : results) {
			Assert.assertTrue(obj instanceof AlleleValues);
			Assert.assertTrue(obj != null);
			AlleleValues element = (AlleleValues) obj;
			Debug.println(IntegrationTestBase.INDENT, element);
		}
	}

	@Test
	public void testCharValues() {
		Query query = this.session.createQuery("FROM CharValues");
		query.setMaxResults(5);
		List results = query.list();

		for (Object obj : results) {
			Assert.assertTrue(obj instanceof CharValues);
			Assert.assertTrue(obj != null);
			CharValues element = (CharValues) obj;
			Debug.println(IntegrationTestBase.INDENT, element);
		}
	}

	@Test
	public void testDataset() {
		Query query = this.session.createQuery("FROM Dataset");
		query.setMaxResults(5);
		List results = query.list();

		for (Object obj : results) {
			Assert.assertTrue(obj instanceof Dataset);
			Assert.assertTrue(obj != null);
			Dataset element = (Dataset) obj;
			Debug.println(IntegrationTestBase.INDENT, element);
		}
	}

	@Test
	public void testMap() {
		Query query = this.session.createQuery("FROM Map");
		query.setMaxResults(5);
		List results = query.list();

		for (Object obj : results) {
			Assert.assertTrue(obj instanceof Map);
			Assert.assertTrue(obj != null);
			Map element = (Map) obj;
			Debug.println(IntegrationTestBase.INDENT, element);
		}
	}

	@Test
	public void testMappingData() {
		Query query = this.session.createQuery("FROM MappingData");
		query.setMaxResults(5);
		List results = query.list();

		for (Object obj : results) {
			Assert.assertTrue(obj instanceof MappingData);
			Assert.assertTrue(obj != null);
			MappingData element = (MappingData) obj;
			Debug.println(IntegrationTestBase.INDENT, element);
		}
	}

	@Test
	public void testMappingPop() {
		Query query = this.session.createQuery("FROM MappingPop");
		query.setMaxResults(5);
		List results = query.list();

		for (Object obj : results) {
			Assert.assertTrue(obj instanceof MappingPop);
			Assert.assertTrue(obj != null);
			MappingPop element = (MappingPop) obj;
			Debug.println(IntegrationTestBase.INDENT, element);
		}
	}

	@Test
	public void testMappingPopValues() {
		Query query = this.session.createQuery("FROM MappingPopValues");
		query.setMaxResults(5);
		List results = query.list();

		for (Object obj : results) {
			Assert.assertTrue(obj instanceof MappingPopValues);
			Assert.assertTrue(obj != null);
			MappingPopValues element = (MappingPopValues) obj;
			Debug.println(IntegrationTestBase.INDENT, element);
		}
	}

	@Test
	public void testMarker() {
		Query query = this.session.createQuery("FROM Marker");
		query.setMaxResults(5);
		List results = query.list();

		for (Object obj : results) {
			Assert.assertTrue(obj instanceof Marker);
			Assert.assertTrue(obj != null);
			Marker element = (Marker) obj;
			Debug.println(IntegrationTestBase.INDENT, element);
		}
	}

	@Test
	public void testMarkerMetadataSet() {
		Query query = this.session.createQuery("FROM MarkerMetadataSet");
		query.setMaxResults(5);
		List results = query.list();

		for (Object obj : results) {
			Assert.assertTrue(obj instanceof MarkerMetadataSet);
			Assert.assertTrue(obj != null);
			MarkerMetadataSet element = (MarkerMetadataSet) obj;
			Debug.println(IntegrationTestBase.INDENT, element);
		}
	}
}
