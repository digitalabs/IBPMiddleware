
package org.generationcp.middleware.dao.ims;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.dao.GenericDAO;
import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.ims.StockTransaction;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.criterion.Restrictions;

/**
 * Created by Daniel Villafuerte on 5/5/2015.
 */
public class StockTransactionDAO extends GenericDAO<StockTransaction, Integer> {

	public List<StockTransaction> getTransactionsForListDataProjectIDs(final List<Integer> listDataProjectIDList)
			throws MiddlewareQueryException {
		try {
			final Criteria criteria = this.getSession().createCriteria(StockTransaction.class);
			criteria.createAlias("listDataProject", "ldp");
			criteria.add(Restrictions.in("ldp.listDataProjectId", listDataProjectIDList));

			return criteria.list();
		} catch (final HibernateException e) {
			this.logAndThrowException(
					"Error with getTransactionsForListDataProjectIDs() query from StockTransactionDAO: " + e.getMessage(), e);
			return new ArrayList<StockTransaction>();
		}
	}

	public boolean listDataProjectListHasStockTransactions(final Integer listDataProjectListId) throws MiddlewareQueryException {
		final String sql =
				"select count(*) from ims_stock_transaction ist "
						+ "WHERE EXISTS (select 1 from listdata_project ldp where ldp.listdata_project_id = ist.listdata_project_id"
						+ " AND ldp.list_id = :listId)";

		try {
			final Query query = this.getSession().createSQLQuery(sql);
			query.setInteger("listId", listDataProjectListId);

			final Number number = (Number) query.uniqueResult();
			return number.intValue() > 0;
		} catch (final HibernateException e) {
			this.logAndThrowException(
					"Error with listDataProjectListHasStockTransactions() query from StockTransactionDAO: " + e.getMessage(), e);
			return false;
		}
	}

	public List<InventoryDetails> retrieveInventoryDetailsForListDataProjectListId(final Integer listDataProjectListId)
			throws MiddlewareQueryException {

		final List<InventoryDetails> detailsList = new ArrayList<>();

		final String sql =
				"select lot.lotid, lot.locid, lot.scaleid, lot.userid, "
						+ "d.germplasm_id, d.entry_id, d.seed_source, d.designation, d.group_name, "
						+ "loc.lname, loc.labbr, scale.name, tran.trnqty, tran.comments,tran.inventory_id, tran.sourceid, "
						+ "d.duplicate_notes, tran.bulk_with, tran.bulk_compl, "
						+ "ist.listdata_project_id, ist.trnid, tran.recordid, lot.eid, ist.recordid as stockSourceRecordId, "
						+ "instanceattr.aval as instanceNumber, plotattr.aval as plotNumber, repattr.aval as repNumber  "
						+ "FROM listdata_project d INNER JOIN ims_stock_transaction ist ON d.listdata_project_id = ist.listdata_project_id "
						+ "INNER JOIN listnms ON d.list_id = listnms.listid "
						+ "INNER JOIN ims_transaction tran ON tran.trnid = ist.trnid INNER JOIN ims_lot lot ON lot.lotid = tran.lotid "
						+ "LEFT JOIN location loc ON lot.locid = loc.locid LEFT JOIN cvterm scale ON scale.cvterm_id = lot.scaleid "
						+ "LEFT JOIN atributs plotattr ON plotattr.gid = lot.eid AND plotattr.atype = (select fldno from udflds where ftable='ATRIBUTS' and ftype='PASSPORT' and fcode='PLOT_NUMBER') "
						+ "LEFT JOIN atributs repattr ON repattr.gid = lot.eid AND repattr.atype = (select fldno from udflds where ftable='ATRIBUTS' and ftype='PASSPORT' and fcode='REP_NUMBER') "
						+ "LEFT JOIN atributs instanceattr ON instanceattr.gid = lot.eid AND instanceattr.atype = (select fldno from udflds where ftable='ATRIBUTS' and ftype='PASSPORT' and fcode='INSTANCE_NUMBER') "
						+ "WHERE listnms.listid = :listId ORDER BY d.entry_id";

		try {
			final Query query = this.setupInventoryDetailQueryObject(sql);
			query.setInteger("listId", listDataProjectListId);

			final List<Object[]> results = query.list();

			if (!results.isEmpty()) {
				for (final Object[] row : results) {
					detailsList.add(this.convertSQLResultsToInventoryDetails(row));
				}
			}
		} catch (final HibernateException e) {
			this.logAndThrowException(
					"Error with retrieveInventoryDetailsForListDataProjectListId() query from StockTransactionDAO: " + e.getMessage(), e);
		}

		return detailsList;
	}

	public List<InventoryDetails> retrieveSummedInventoryDetailsForListDataProjectListId(final Integer listDataProjectListId,
			final GermplasmListType germplasmListType) throws MiddlewareQueryException {
		final List<InventoryDetails> detailsList = new ArrayList<>();

		if (!germplasmListType.equals(GermplasmListType.ADVANCED) && !germplasmListType.equals(GermplasmListType.CROSSES)) {
			throw new IllegalArgumentException("This method should only be passed lists of type ADVANCED or CROSSES");
		}

		final String sql =
				"select lot.lotid, lot.locid, summed.scaleid, lot.userid, "
						+ "d.germplasm_id, d.entry_id, d.seed_source, d.designation, d.group_name, "
						+ "loc.lname, loc.labbr, scale.name, summed.total as trnqty, tran.comments,tran.inventory_id, tran.sourceid, "
						+ "d.duplicate_notes, tran.bulk_with, tran.bulk_compl, "
						+ "ist.listdata_project_id, ist.trnid, tran.recordid, lot.eid, ist.recordid as stockSourceRecordId, "
						+ "instanceattr.aval as instanceNumber, plotattr.aval as plotNumber, repattr.aval as repNumber  "
						+ "FROM listdata_project d INNER JOIN ims_stock_transaction ist ON d.listdata_project_id = ist.listdata_project_id "
						+ "INNER JOIN listnms ON d.list_id = listnms.listid "
						+ "INNER JOIN summed_transaction summed ON (summed.recordid = ist.recordid) "
						+ "INNER JOIN ims_transaction tran ON (tran.trnid = ist.trnid AND tran.recordid = summed.recordid) "
						+ "INNER JOIN ims_lot lot ON lot.lotid = summed.lotid "
						+ "LEFT JOIN location loc ON lot.locid = loc.locid LEFT JOIN cvterm scale ON scale.cvterm_id = summed.scaleid "
						+ "LEFT JOIN atributs plotattr ON plotattr.gid = lot.eid AND plotattr.atype = (select fldno from udflds where ftable='ATRIBUTS' and ftype='PASSPORT' and fcode='PLOT_NUMBER') "
						+ "LEFT JOIN atributs repattr ON repattr.gid = lot.eid AND repattr.atype = (select fldno from udflds where ftable='ATRIBUTS' and ftype='PASSPORT' and fcode='REP_NUMBER') "
						+ "LEFT JOIN atributs instanceattr ON instanceattr.gid = lot.eid AND instanceattr.atype = (select fldno from udflds where ftable='ATRIBUTS' and ftype='PASSPORT' and fcode='INSTANCE_NUMBER') "
						+ "WHERE listnms.listid = :listId ORDER BY d.entry_id";

		try {
			final Query query = this.setupInventoryDetailQueryObject(sql);
			query.setInteger("listId", listDataProjectListId);

			final List<Object[]> results = query.list();

			if (!results.isEmpty()) {
				for (final Object[] row : results) {
					final InventoryDetails details = this.convertSQLResultsToInventoryDetails(row);
					detailsList.add(details);
				}
			}
		} catch (final HibernateException e) {
			this.logAndThrowException(
					"Error with retrieveInventoryDetailsForListDataProjectListId() query from StockTransactionDAO: " + e.getMessage(), e);
		}

		return detailsList;
	}

	protected Query setupInventoryDetailQueryObject(final String querySQL) {
		final Query query =
				this.getSession().createSQLQuery(querySQL).addScalar("lotId").addScalar("locid").addScalar("scaleid").addScalar("userid")
						.addScalar("germplasm_id").addScalar("entry_id").addScalar("seed_source").addScalar("designation")
						.addScalar("group_name").addScalar("lname").addScalar("labbr").addScalar("name").addScalar("trnqty")
						.addScalar("comments").addScalar("inventory_id").addScalar("sourceid").addScalar("duplicate_notes")
						.addScalar("bulk_with").addScalar("bulk_compl").addScalar("listdata_project_id").addScalar("trnid")
						.addScalar("recordid").addScalar("eid").addScalar("stockSourceRecordId").addScalar("instanceNumber")
						.addScalar("plotNumber").addScalar("repNumber");

		return query;
	}

	protected InventoryDetails convertSQLResultsToInventoryDetails(final Object[] resultRow) {
		final Integer lotId = (Integer) resultRow[0];
		final Integer locationId = (Integer) resultRow[1];
		final Integer scaleId = (Integer) resultRow[2];
		final Integer userId = (Integer) resultRow[3];
		final Integer gid = (Integer) resultRow[4];
		final Integer entryId = (Integer) resultRow[5];
		final String seedSource = (String) resultRow[6];
		final String designation = (String) resultRow[7];
		final String groupName = (String) resultRow[8];
		final String locationName = (String) resultRow[9];
		final String locationAbbr = (String) resultRow[10];
		final String scaleName = (String) resultRow[11];
		final Double amount = (Double) resultRow[12];
		final String comments = (String) resultRow[13];
		final String inventoryID = (String) resultRow[14];
		final Integer sourceId = (Integer) resultRow[15];
		final String duplicate = (String) resultRow[16];
		final String bulkWith = (String) resultRow[17];
		final String bulkCompl = (String) resultRow[18];
		final Integer listDataProjectId = (Integer) resultRow[19];
		final Integer trnId = (Integer) resultRow[20];
		final Integer sourceRecordId = (Integer) resultRow[21];
		final Integer lotGid = (Integer) resultRow[22];
		final Integer stockSourceRecordId = (Integer) resultRow[23];
		final Integer instanceNumber = resultRow[24] == null ? null : Integer.valueOf((String) resultRow[24]);
		final Integer plotNumber = resultRow[25] == null ? null : Integer.valueOf((String) resultRow[25]);
		final Integer replicationNumber = resultRow[26] == null ? null : Integer.valueOf((String) resultRow[26]);

		final InventoryDetails details =
				new InventoryDetails(gid, designation, lotId, locationId, locationName, userId, amount, sourceId, null, scaleId, scaleName,
						comments);
		details.setInventoryID(inventoryID);
		details.setLocationAbbr(locationAbbr);
		details.setEntryId(entryId);
		details.setSource(seedSource);
		details.setParentage(groupName);
		details.setDuplicate(duplicate);
		details.setBulkWith(bulkWith);
		details.setBulkCompl(bulkCompl);
		details.setListDataProjectId(listDataProjectId);
		details.setTrnId(trnId);
		details.setSourceRecordId(sourceRecordId);
		details.setLotGid(lotGid);
		details.setStockSourceRecordId(stockSourceRecordId);
		details.setInstanceNumber(instanceNumber);
		details.setReplicationNumber(replicationNumber);
		details.setPlotNumber(plotNumber);

		return details;
	}

	public boolean stockHasCompletedBulking(final Integer listId) throws MiddlewareQueryException {
		final StringBuilder sql = new StringBuilder();
		sql.append("SELECT COUNT(1) ");
		sql.append("FROM ims_stock_transaction ist ");
		sql.append("INNER JOIN listdata_project d ");
		sql.append("ON d.listdata_project_id = ist.listdata_project_id ");
		sql.append("INNER JOIN ims_transaction tran ");
		sql.append("ON tran.trnid = ist.trnid ");
		sql.append("WHERE d.list_id = :listId ");
		sql.append("AND tran.bulk_compl = 'Completed' ");
		final Query query = this.getSession().createSQLQuery(sql.toString());
		query.setInteger("listId", listId);
		final BigInteger numberOfRecords = (BigInteger) query.uniqueResult();
		if (numberOfRecords.intValue() > 0) {
			return true;
		}
		return false;
	}
}
