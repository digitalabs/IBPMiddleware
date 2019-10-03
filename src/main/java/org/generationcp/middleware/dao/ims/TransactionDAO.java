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

package org.generationcp.middleware.dao.ims;

import org.generationcp.middleware.dao.GenericDAO;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.ims.LotStatus;
import org.generationcp.middleware.pojos.ims.Transaction;
import org.generationcp.middleware.pojos.report.TransactionReportRow;
import org.generationcp.middleware.util.Util;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO class for {@link Transaction}.
 *
 */
public class TransactionDAO extends GenericDAO<Transaction, Integer> {

	private static final Logger LOG = LoggerFactory.getLogger(TransactionDAO.class);

	@SuppressWarnings("unchecked")
	public List<Transaction> getAllReserve(final int start, final int numOfRows) {
		try {
			final Criteria criteria = this.getSession().createCriteria(Transaction.class);
			criteria.add(Restrictions.eq("status", 0));
			criteria.add(Restrictions.lt("quantity", 0d));
			criteria.setFirstResult(start);
			criteria.setMaxResults(numOfRows);
			return criteria.list();
		} catch (final HibernateException e) {
			final String message = "Error with getAllReserve() query from Transaction: " + e.getMessage();
			LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
	}

	public long countAllReserve() {
		try {
			final Criteria criteria = this.getSession().createCriteria(Transaction.class);
			criteria.setProjection(Projections.rowCount());
			criteria.add(Restrictions.eq("status", 0));
			criteria.add(Restrictions.lt("quantity", 0d));
			return ((Long) criteria.uniqueResult());
		} catch (final HibernateException e) {
			final String message = "Error with countAllReserve query from Transaction: " + e.getMessage();
			LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
	}

	@SuppressWarnings("unchecked")
	public List<Transaction> getAllDeposit(final int start, final int numOfRows) {
		try {
			final Criteria criteria = this.getSession().createCriteria(Transaction.class);
			criteria.add(Restrictions.eq("status", 0));
			criteria.add(Restrictions.gt("quantity", 0d));
			criteria.setFirstResult(start);
			criteria.setMaxResults(numOfRows);
			return criteria.list();
		} catch (final HibernateException e) {
			final String message = "Error with getAllDeposit query from Transaction: " + e.getMessage();
			LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
	}

	public long countAllDeposit() {
		try {
			final Criteria criteria = this.getSession().createCriteria(Transaction.class);
			criteria.setProjection(Projections.rowCount());
			criteria.add(Restrictions.eq("status", 0));
			criteria.add(Restrictions.gt("quantity", 0d));
			return ((Long) criteria.uniqueResult());
		} catch (final HibernateException e) {
			final String message = "Error with countAllDeposit() query from Transaction: " + e.getMessage();
			LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
	}

	@SuppressWarnings("unchecked")
	public List<Transaction> getAllUncommitted(final int start, final int numOfRows) {
		try {
			final Criteria criteria = this.getSession().createCriteria(Transaction.class);
			criteria.add(Restrictions.eq("status", 0));
			criteria.setFirstResult(start);
			criteria.setMaxResults(numOfRows);
			return criteria.list();
		} catch (final HibernateException e) {
			final String message = "Error with getAllUncomitted() query from Transaction: " + e.getMessage();
			LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
	}

	public long countAllUncommitted() {
		try {
			final Criteria criteria = this.getSession().createCriteria(Transaction.class);
			criteria.setProjection(Projections.rowCount());
			criteria.add(Restrictions.eq("status", 0));
			return ((Long) criteria.uniqueResult());
		} catch (final HibernateException e) {
			final String message = "Error with countAllUncommitted() query from Transaction: " + e.getMessage();
			LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
	}

	@SuppressWarnings("unchecked")
	public List<Transaction> getAllWithdrawals(final int start, final int numOfRows) {
		try {
			final Criteria criteria = this.getSession().createCriteria(Transaction.class);
			criteria.add(Restrictions.lt("quantity", 0d));
			criteria.setFirstResult(start);
			criteria.setMaxResults(numOfRows);
			return criteria.list();
		} catch (final HibernateException e) {
			final String message = "Error with getAllWithdrawals() query from Transaction: " + e.getMessage();
			LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
	}

	public long countAllWithdrawals() {
		try {
			final Criteria criteria = this.getSession().createCriteria(Transaction.class);
			criteria.setProjection(Projections.rowCount());
			criteria.add(Restrictions.lt("quantity", 0d));
			return ((Long) criteria.uniqueResult());
		} catch (final HibernateException e) {
			final String message = "Error with countAllWithdrawals() query from Transaction: " + e.getMessage();
			LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
	}

	@SuppressWarnings("unchecked")
	public List<Transaction> getEmptyLot(final int start, final int numOfRows) {
		try {
			final Query query = this.getSession().getNamedQuery(Transaction.GET_EMPTY_LOT);
			query.setFirstResult(start);
			query.setMaxResults(numOfRows);
			return query.list();
		} catch (final HibernateException e) {
			final String message = "Error with getEmptyLot() query from Transaction: " + e.getMessage();
			LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
	}

	@SuppressWarnings("unchecked")
	public List<Transaction> getLotWithMinimumAmount(final double minAmount, final int start, final int numOfRows) {
		try {
			final Query query = this.getSession().getNamedQuery(Transaction.GET_LOT_WITH_MINIMUM_AMOUNT);
			query.setFirstResult(start);
			query.setMaxResults(numOfRows);
			query.setParameter("minAmount", minAmount);
			return query.list();
		} catch (final HibernateException e) {
			final String message = "Error with getLotWithMinimumAmount(minAmount=\" + minAmount + \") query from Transaction: " + e.getMessage();
			LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
	}

	public List<InventoryDetails> getInventoryDetailsByTransactionRecordId(final List<Integer> recordIds) {
		final List<InventoryDetails> detailsList = new ArrayList<>();

		if (recordIds == null || recordIds.isEmpty()) {
			return detailsList;
		}

		try {
			final Session session = this.getSession();

			final StringBuilder sql =
					new StringBuilder().append("SELECT lot.lotid, lot.userid, lot.eid, lot.locid, lot.scaleid, ")
							.append("tran.sourceid, tran.trnqty, lot.stock_id, lot.comments, tran.recordid ")
							.append("FROM ims_transaction tran ").append("LEFT JOIN ims_lot lot ON lot.lotid = tran.lotid ")
							.append("WHERE lot.status = ").append(LotStatus.ACTIVE.getIntValue())
							.append("		 AND tran.recordid IN (:recordIds) ");
			final SQLQuery query = session.createSQLQuery(sql.toString());
			query.setParameterList("recordIds", recordIds);

			final List<Object[]> results = query.list();

			if (!results.isEmpty()) {
				for (final Object[] row : results) {
					final Integer lotId = (Integer) row[0];
					final Integer userId = (Integer) row[1];
					final Integer gid = (Integer) row[2];
					final Integer locationId = (Integer) row[3];
					final Integer scaleId = (Integer) row[4];
					final Integer sourceId = (Integer) row[5];
					final Double amount = (Double) row[6];
					final String inventoryID = (String) row[7];
					final String comment = (String) row[8];
					final Integer sourceRecordId = (Integer) row[9];

					final InventoryDetails details =
							new InventoryDetails(gid, null, lotId, locationId, null, userId, amount, sourceId, null, scaleId, null, comment);
					details.setInventoryID(inventoryID);
					details.setSourceRecordId(sourceRecordId);
					detailsList.add(details);
				}
			}

		} catch (final HibernateException e) {
			final String message = "Error with getInventoryDetailsByTransactionRecordId() query from Transaction: " + e.getMessage();
			LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}

		return detailsList;
	}

	@SuppressWarnings("unchecked")
	public Map<Integer, BigInteger> countLotsWithReservationForListEntries(final List<Integer> listEntryIds) {
		final Map<Integer, BigInteger> lotCounts = new HashMap<>();

		try {
			final String sql =
					"SELECT recordid, count(DISTINCT t.lotid) " + "FROM ims_transaction t " + "INNER JOIN ims_lot l ON l.lotid = t.lotid "
							+ "WHERE trnstat = 0 AND trnqty < 0 AND recordid IN (:entryIds) "
							+ "  AND l.status = 0 AND l.etype = 'GERMPLSM' " + "GROUP BY recordid " + "ORDER BY recordid ";
			final Query query = this.getSession().createSQLQuery(sql).setParameterList("entryIds", listEntryIds);
			final List<Object[]> result = query.list();
			for (final Object[] row : result) {
				final Integer entryId = (Integer) row[0];
				final BigInteger count = (BigInteger) row[1];

				lotCounts.put(entryId, count);
			}

		} catch (final Exception e) {
			final String message = "Error with countLotsWithReservationForListEntries=" + listEntryIds + " query from Transaction: " + e.getMessage();
			LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}

		return lotCounts;
	}

	@SuppressWarnings("unchecked")
	public Map<Integer, Object[]> retrieveWithdrawalBalanceWithDistinctScale(final List<Integer> listEntryIds) {
		final Map<Integer, Object[]> mapWithdrawalStatusEntryWise = new HashMap<>();

		try {
			final String sql =
					"SELECT recordid, sum(trnqty)*-1 as withdrawal, count(distinct l.scaleid),l.scaleid "
							+ "FROM ims_transaction t " + "INNER JOIN ims_lot l ON l.lotid = t.lotid "
							+ "WHERE trnqty < 0 AND trnstat <> 9 AND recordid IN (:entryIds) "
							+ "  AND l.status = 0 AND l.etype = 'GERMPLSM' " + "GROUP BY recordid " + "ORDER BY recordid ";
			final Query query = this.getSession().createSQLQuery(sql).setParameterList("entryIds", listEntryIds);
			final List<Object[]> result = query.list();
			for (final Object[] row : result) {
				final Integer entryId = (Integer) row[0];
				final Double withdrawalBalance = (Double) row[1];

				final BigInteger distinctWithdrawalScale = (BigInteger) row[2];
				final Integer withdrawalScale = (Integer) row[3];

				mapWithdrawalStatusEntryWise.put(entryId, new Object[]{ withdrawalBalance, 	distinctWithdrawalScale, withdrawalScale});
			}

		} catch (final Exception e) {
			final String message = "Error with retrieveWithdrawalBalanceWithDistinctScale=" + listEntryIds + " query from Transaction: " + e.getMessage();
			LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}

		return mapWithdrawalStatusEntryWise;
	}

	@SuppressWarnings("unchecked")
	public List<Object[]> retrieveWithdrawalStatus(final Integer sourceId, final List<Integer> listGids) {
		final List<Object[]> listOfTransactionStatusForGermplsm = new ArrayList<>();

		try {
			final String sql =
					"select lot.*,recordid,trnstat  from  (SELECT i.lotid, i.eid FROM ims_lot i "
							+ " LEFT JOIN ims_transaction act ON act.lotid = i.lotid AND act.trnstat <> 9 "
							+ " WHERE i.status = 0 AND i.etype = 'GERMPLSM' AND i.eid  IN (:gIds) GROUP BY i.lotid ) lot "
							+ " LEFT JOIN ims_transaction res ON res.lotid = lot.lotid   AND trnstat in (0,1) AND trnqty < 0 "
							+ " AND sourceid = :sourceid AND sourcetype = 'LIST'  ORDER by lot.eid; ";
			final Query query = this.getSession().createSQLQuery(sql);
			query.setParameterList("gIds", listGids);
			query.setParameter("sourceid", sourceId);

			final List<Object[]> result = query.list();
			for (final Object[] row : result) {

				Integer lotId = null;
				Integer germplsmId = null;
				Integer recordId = null;
				Integer tranStatus = null;

				if(row[0] != null){
					lotId = (Integer) row[0];
				}

				if(row[1] != null){
					germplsmId = (Integer) row[1];
				}
				if(row[2] != null){
					recordId = (Integer) row[2];
				}
				if(row[3] != null){
					tranStatus = (Integer) row[3];
				}

				listOfTransactionStatusForGermplsm.add(new Object[]{ lotId, germplsmId, recordId, tranStatus });
			}

		} catch (final Exception e) {
			final String message = "Error withretrieveWithdrawalStatus=" + listGids + " query from Transaction: " + e.getMessage();
			LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}

		return listOfTransactionStatusForGermplsm;
	}

	@SuppressWarnings("unchecked")
	public List<Transaction> getByLotIds(final List<Integer> lotIds) {
		final List<Transaction> transactions = new ArrayList<>();

		if (lotIds == null || lotIds.isEmpty()) {
			return transactions;
		}

		try {
			final Criteria criteria = this.getSession().createCriteria(Transaction.class);
			criteria.add(Restrictions.in("lot.id", lotIds));
			return criteria.list();
		} catch (final HibernateException e) {
			final String message = "Error getByLotIds() query from Transaction: " + e.getMessage();
			LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}

	}

	public void cancelUnconfirmedTransactionsForListEntries(final List<Integer> listEntryIds) {
		try {
			// Please note we are manually flushing because non hibernate based deletes and updates causes the Hibernate session to get out of synch with
			// underlying database. Thus flushing to force Hibernate to synchronize with the underlying database before the delete
			// statement
			this.getSession().flush();
			
			final String sql =
					"UPDATE ims_transaction " + "SET trnstat = 9, " + "trndate = :currentDate "
							+ "WHERE trnstat = 0 AND recordid IN (:entryIds) " + "AND sourceType = 'LIST'";
			final Query query =
					this.getSession().createSQLQuery(sql).setParameter("currentDate", Util.getCurrentDateAsIntegerValue())
							.setParameterList("entryIds", listEntryIds);
			query.executeUpdate();
		} catch (final Exception e) {
			final String message = "Error cancelUnconfirmedTransactionsForListEntries=" + listEntryIds + " query from Transaction: " + e.getMessage();
			LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
	}

	public void cancelReservationsForLotEntryAndLrecId(final Integer lotId, final Integer lrecId) {
		try {
			
			// Please note we are manually flushing because non hibernate based deletes and updates causes the Hibernate session to get out of synch with
			// underlying database. Thus flushing to force Hibernate to synchronize with the underlying database before the delete
			// statement
			this.getSession().flush();
			
			final String sql =
					"UPDATE ims_transaction " + "SET trnstat = 9, " + "trndate = :currentDate " + "WHERE trnstat = 0 AND lotId = :lotId "
							+ "AND recordId = :lrecId " + "AND trnqty < 0 " + "AND sourceType = 'LIST'";
			final Query query =
					this.getSession().createSQLQuery(sql).setParameter("currentDate", Util.getCurrentDateAsIntegerValue())
							.setParameter("lotId", lotId).setParameter("lrecId", lrecId);
			query.executeUpdate();
		} catch (final Exception e) {
			final String message = "Error cancelReservationsForLotEntryAndLrecId(lotId:" + lotId + ", lrecId:" + lrecId
				+ ") query from Transaction: " + e.getMessage();
			LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
	}

	public void cancelUnconfirmedTransactionsForGermplasms(final List<Integer> gids) {
		try {
			final String sql =
					"UPDATE ims_transaction " + "SET trnstat = 9, " + "trndate = :currentDate "
							+ "WHERE trnstat = 0 AND sourceType = 'LIST' " + "AND lotid in ( select lotid from ims_lot "
							+ "WHERE status = 0 AND etype = 'GERMPLSM' " + "AND eid in (:gids))";
			final Query query =
					this.getSession().createSQLQuery(sql).setParameter("currentDate", Util.getCurrentDateAsIntegerValue())
							.setParameterList("gids", gids);
			query.executeUpdate();
		} catch (final Exception e) {
			final String message = "Error cancelUnconfirmedTransactionsForGermplasms=" + gids + ") query from Transaction: " + e.getMessage();
			LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
	}

	public Map<Integer, String> retrieveStockIds(final List<Integer> gIds) {

		final Map<Integer, String> gIdStockIdMap = new HashMap<>();

		final String sql =
				"SELECT a.gid,group_concat(distinct b.stock_id SEPARATOR ', ')  " + "FROM listdata a  "
						+ "inner join ims_lot b ON a.gid = b.eid  "
						+ "INNER JOIN ims_transaction c ON b.lotid = c.lotid and a.lrecid = c.recordid "
						+ "WHERE a.gid in (:gIds) GROUP BY a.gid";

		final Query query = this.getSession().createSQLQuery(sql).setParameterList("gIds", gIds);

		final List<Object[]> result = query.list();
		for (final Object[] row : result) {
			final Integer gid = (Integer) row[0];
			final String stockIds = (String) row[1];

			gIdStockIdMap.put(gid, stockIds);
		}
		return gIdStockIdMap;

	}

	public Boolean isStockIdExists(final List<String> stockIds) {
		final List<String> result = this.getSimilarStockIds(stockIds);
		return null != result && !result.isEmpty();

	}

	public List<String> getSimilarStockIds(final List<String> stockIds) {
		if (null == stockIds || stockIds.isEmpty()) {
			return new ArrayList<>();
		}

		final String sql = "SELECT stock_id" + " FROM ims_lot" + " WHERE stock_id IN (:STOCK_ID_LIST)";
		final Query query = this.getSession().createSQLQuery(sql).setParameterList("STOCK_ID_LIST", stockIds);

		return query.list();
	}

	@SuppressWarnings("unchecked")
	public List<String> getStockIdsByListDataProjectListId(final Integer listId) {
		try {
			final String sql =
				"SELECT lot.stock_id" + " FROM ims_transaction tran, listnms l, lot " + " WHERE l.listId = :listId "
					+ " AND tran.lotid = lot.lotid AND sourceId = l.listref AND sourceType = 'LIST'" + " AND lot.stock_id IS NOT NULL";
			final Query query = this.getSession().createSQLQuery(sql).setParameter("listId", listId);
			return query.list();
		} catch (final Exception e) {
			final String message = "Error with getStockIdsByListDataProjectListId(" + listId + ") query from Transaction: " + e.getMessage();
			LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
	}

	public List<TransactionReportRow> getTransactionDetailsForLot(final Integer lotId) {

		final List<TransactionReportRow> transactions = new ArrayList<>();
		try {
			final String sql = "SELECT i.userid,i.lotid,i.trndate,i.trnstat,i.trnqty,i.sourceid,l.listname, i.comments,"
					+ "(CASE WHEN i.comments in ('Lot closed', 'Discard') THEN i.comments WHEN trnstat = 0 AND trnqty > 0 THEN 'Deposit' "
					+ "WHEN trnstat = 0 AND trnqty < 0 THEN 'Reservation' WHEN trnstat = 1 AND trnqty < 0 THEN 'Withdrawal' END) as trntype, "
					+ "lot.created_date "
					+ "FROM ims_transaction i LEFT JOIN listnms l ON l.listid = i.sourceid "
					+ " INNER JOIN ims_lot lot ON lot.lotid = i.lotid "
					+ "WHERE i.lotid = :lotId AND i.trnstat <> 9 ORDER BY i.lotid";

			final Query query = this.getSession().createSQLQuery(sql);

			query.setParameter("lotId", lotId);

			this.createTransactionRow(transactions, query);

		} catch (final Exception e) {
			final String message = "Error with ggetTransactionDetailsForLot(" + lotId + ") query from Transaction: " + e.getMessage();
			LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}

		return transactions;
	}

	private void createTransactionRow(final List<TransactionReportRow> transactionReportRows, final Query query) {

		final List<Object[]> result = query.list();
		TransactionReportRow transaction = null;
		for (final Object[] row : result) {

			final Integer userId = (Integer) row[0];
			final Integer lotId = (Integer) row[1];
			final Integer trnDate = (Integer) row[2];
			final Integer trnState = (Integer) row[3];
			final Double trnQty = (Double) row[4];
			final Integer listId = (Integer) row[5];
			final String listName = (String) row[6];
			final String comments = (String) row[7];
			final String lotStatus = (String) row[8];
			final Date lotDate = (Date) row[9];

			transaction = new TransactionReportRow();
			transaction.setUserId(userId);
			transaction.setLotId(lotId);
			transaction.setDate(trnDate);
			transaction.setTrnStatus(trnState);
			transaction.setQuantity(trnQty);
			transaction.setListId(listId);
			transaction.setListName(listName);
			transaction.setCommentOfLot(comments);
			transaction.setLotStatus(lotStatus);
			transaction.setLotDate(lotDate);

			transactionReportRows.add(transaction);
		}

	}
}
