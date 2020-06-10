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

import com.google.common.base.Joiner;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.api.inventory.study.StudyTransactionsDto;
import org.generationcp.middleware.api.inventory.study.StudyTransactionsRequest;
import org.generationcp.middleware.dao.GenericDAO;
import org.generationcp.middleware.domain.inventory.InventoryDetails;
import org.generationcp.middleware.domain.inventory.manager.TransactionDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionsSearchDto;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.ims.LotStatus;
import org.generationcp.middleware.pojos.ims.Transaction;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.generationcp.middleware.pojos.ims.TransactionType;
import org.generationcp.middleware.pojos.report.TransactionReportRow;
import org.generationcp.middleware.util.SqlQueryParamBuilder;
import org.generationcp.middleware.util.Util;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.AliasToBeanConstructorResultTransformer;
import org.hibernate.transform.Transformers;
import org.hibernate.type.IntegerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.lang.reflect.Constructor;
import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DAO class for {@link Transaction}.
 *
 */
public class TransactionDAO extends GenericDAO<Transaction, Integer> {

	private static final Logger LOG = LoggerFactory.getLogger(TransactionDAO.class);
	private static final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

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
		//FIXME delete because the value is never used. This query is wrong, should use gids instead of listEntryIds
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

				mapWithdrawalStatusEntryWise.put(entryId, new Object[] {withdrawalBalance, distinctWithdrawalScale, withdrawalScale});
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
					this.getSession().createSQLQuery(sql).setParameter("currentDate", Util.getCurrentDate())
							.setParameterList("entryIds", listEntryIds);
			query.executeUpdate();
		} catch (final Exception e) {
			final String message = "Error cancelUnconfirmedTransactionsForListEntries=" + listEntryIds + " query from Transaction: " + e.getMessage();
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
					this.getSession().createSQLQuery(sql).setParameter("currentDate", Util.getCurrentDate())
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
				"SELECT b.eid ,group_concat(distinct b.stock_id SEPARATOR ', ')  " + "FROM "
						+ "ims_lot b  "
						+ "left JOIN ims_transaction c ON b.lotid = c.lotid  "
						+ "WHERE cast(b.eid as UNSIGNED) in (:gIds) GROUP BY b.eid";

		final Query query = this.getSession().createSQLQuery(sql).setParameterList("gIds", gIds);

		final List<Object[]> result = query.list();
		for (final Object[] row : result) {
			final Integer gid = (Integer) row[0];
			final String stockIds = (String) row[1];

			gIdStockIdMap.put(gid, stockIds);
		}
		return gIdStockIdMap;

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
				" SELECT lot.stock_id " +
				" FROM ims_lot lot " +
				" INNER JOIN ims_transaction tran ON lot.lotid = tran.lotid " +
				" INNER join listnms l ON l.listref = tran.sourceid " +
				" WHERE sourceType = 'LIST'	AND lot.stock_id is not null AND l.listid =  :listId";
			final Query query = this.getSession().createSQLQuery(sql).setParameter("listId", listId);
			return query.list();
		} catch (final Exception e) {
			final String message = "Error with getStockIdsByListDataProjectListId(" + listId + ") query from Transaction: " + e.getMessage();
			LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
	}

	@SuppressWarnings("unchecked")
	public Map<String, Double> getStockIdsWithMultipleTransactions(final Integer listId) {
		final Map<String, Double> map = new HashMap<>();
		try {
			final String sql =
				" SELECT lot.stock_id, trnqty " +
					" FROM ims_lot lot " +
					" INNER JOIN ims_transaction tran ON lot.lotid = tran.lotid " +
					" INNER join listnms l ON l.listref = tran.sourceid " +
					" WHERE sourceType = 'LIST'	AND lot.stock_id is not null AND l.listid =  :listId " +
					" AND EXISTS (select 1 from ims_transaction tr WHERE tr.lotid = tran.lotid "
					+ "  group by lotid having count(tr.trnid) > 1)";
			final Query query = this.getSession().createSQLQuery(sql).setParameter("listId", listId);
			final List<Object> results = query.list();
			for (final Object obj : results) {
				final Object[] row = (Object[]) obj;
				map.put((String) row[0], (Double) row[1]);
			}
		} catch (final Exception e) {
			final String message = "Error with getStockIdsWithMultipleTransactions(" + listId + ") query from Transaction: " + e.getMessage();
			LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
		return map;
	}

	public List<TransactionReportRow> getTransactionDetailsForLot(final Integer lotId) {

		final List<TransactionReportRow> transactions = new ArrayList<>();
		try {
			final String sql = "SELECT i.userid,i.lotid,i.trndate,"
					+ "(CASE WHEN trnstat = " + TransactionStatus.PENDING.getIntValue() + " THEN '" + TransactionStatus.PENDING.getValue()
					+ "' WHEN trnstat = " + TransactionStatus.CONFIRMED.getIntValue() + " THEN '" + TransactionStatus.CONFIRMED.getValue()
					+ "' WHEN trnstat = " + TransactionStatus.CANCELLED.getIntValue() + " THEN '" + TransactionStatus.CANCELLED.getValue()
					+ "' END) as trnStatus, "
					+ " i.trnqty,i.sourceid,l.listname, i.comments,"
					+ "(CASE WHEN trntype = " + TransactionType.DEPOSIT.getId() + " THEN '" + TransactionType.DEPOSIT.getValue()
					+ "' WHEN trntype = " + TransactionType.WITHDRAWAL.getId() + " THEN '" + TransactionType.WITHDRAWAL.getValue()
					+ "' WHEN trntype = " + TransactionType.DISCARD.getId() + " THEN '" + TransactionType.DISCARD.getValue()
					+ "' WHEN trntype = " + TransactionType.ADJUSTMENT.getId() + " THEN '" + TransactionType.ADJUSTMENT.getValue()
					+ "' END) as trntype "
					+ "FROM ims_transaction i LEFT JOIN listnms l ON l.listid = i.sourceid "
					+ " INNER JOIN ims_lot lot ON lot.lotid = i.lotid "
					+ "WHERE i.lotid = :lotId ORDER BY i.trnid";

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
			final Date trnDate = (Date) row[2];
			final String trnStatus = (String) row[3];
			final Double trnQty = (Double) row[4];
			final Integer listId = (Integer) row[5];
			final String listName = (String) row[6];
			final String comments = (String) row[7];
			final String lotStatus = (String) row[8];

			transaction = new TransactionReportRow();
			transaction.setUserId(userId);
			transaction.setLotId(lotId);
			transaction.setDate(trnDate);
			transaction.setTrnStatus(trnStatus);
			transaction.setQuantity(trnQty);
			transaction.setListId(listId);
			transaction.setListName(listName);
			transaction.setCommentOfLot(comments);
			transaction.setLotStatus(lotStatus);

			transactionReportRows.add(transaction);
		}

	}

	//New inventory functions, please locate them below this line to help cleaning in the near future.
	private final String SEARCH_TRANSACTIONS_QUERY = "SELECT " //
		+ "    tr.trnid AS transactionId,"//
		+ "    users.uname AS createdByUsername,"//
		+ "(CASE WHEN trntype = " + TransactionType.DEPOSIT.getId() + " THEN '" + TransactionType.DEPOSIT.getValue()
		+ "' WHEN trntype = " + TransactionType.WITHDRAWAL.getId() + "  THEN '" + TransactionType.WITHDRAWAL.getValue()
		+ "' WHEN trntype = " + TransactionType.DISCARD.getId() + "  THEN '" + TransactionType.DISCARD.getValue()
		+ "' WHEN trntype = " + TransactionType.ADJUSTMENT.getId() + "  THEN '" + TransactionType.ADJUSTMENT.getValue()
		+ "' END) AS transactionType,"//
		+ "    tr.trnqty AS amount,"//
		+ "    tr.comments AS notes,"//
		+ "    tr.trndate as createdDate, "//
		+ "    lot.lotid AS lotLotId," //
		+ "    lot.lot_uuid AS lotUUID," //
		+ "    lot.eid AS lotGid,"//
		+ "    n.nval AS lotDesignation,"//
		+ "    lot.stock_id AS lotStockId,"//
		+ "    scaleid AS lotScaleId,"//
		+ "    scale.name AS lotUnitName," //
		+ "    (CASE WHEN lot.status = 0 THEN 'Active' WHEN lot.status = 1 THEN 'Closed' END) AS lotStatus, "
		+ "   (CASE WHEN trnstat = " + TransactionStatus.PENDING.getIntValue() + " THEN '" + TransactionStatus.PENDING.getValue()
		+ "' WHEN trnstat = " + TransactionStatus.CONFIRMED.getIntValue() + " THEN '" + TransactionStatus.CONFIRMED.getValue()
		+ "' WHEN trnstat = " + TransactionStatus.CANCELLED.getIntValue() + " THEN '" + TransactionStatus.CANCELLED.getValue()
		+ "' END) as transactionStatus, "
		+ " lot.locid as lotLocationId, "
		+ " loc.lname as lotLocationName, "//
		+ " loc.labbr as lotLocationAbbr, "//
		+ " lot.comments as lotComments "
		+ " FROM"//
		+ "   ims_transaction tr "//
		+ "        INNER JOIN"//
		+ "    ims_lot lot ON tr.lotid = lot.lotid "//
		+ "		   LEFT JOIN" //
		+ "	   location loc on loc.locid = lot.locid "//
		+ "        LEFT JOIN"//
		+ "    cvterm scale ON scale.cvterm_id = lot.scaleid"//
		+ "        INNER JOIN"//
		+ "    germplsm g ON g.gid = lot.eid"//
		+ "        LEFT JOIN"//
		+ "    names n ON n.gid = lot.eid AND n.nstat = 1"//
		+ "        LEFT JOIN"//
		+ "    workbench.users users ON users.userid = tr.userid"//
		+ " WHERE"//
		+ "    lot.etype = 'GERMPLSM' and g.deleted=0 "; //

	// FIXME setParameter/setParameterList (SqlQueryParamBuilder)
	private String buildSearchTransactionsQuery(final TransactionsSearchDto transactionsSearchDto) {
		final StringBuilder query = new StringBuilder(SEARCH_TRANSACTIONS_QUERY);
		if (transactionsSearchDto != null) {
			if (transactionsSearchDto.getLotIds() != null && !transactionsSearchDto.getLotIds().isEmpty()) {
				query.append(" and lot.lotid IN (").append(Joiner.on(",").join(transactionsSearchDto.getLotIds())).append(") ");
			}

			if (transactionsSearchDto.getLotUUIDs() != null && !transactionsSearchDto.getLotUUIDs().isEmpty()) {
				query.append("and lot.lot_uuid IN ('").append(Joiner.on("','").join(transactionsSearchDto.getLotUUIDs().toArray()))
					.append("') ");
			}

			if (transactionsSearchDto.getTransactionIds() != null && !transactionsSearchDto.getTransactionIds().isEmpty()) {
				query.append(" and trnid IN (").append(Joiner.on(",").join(transactionsSearchDto.getTransactionIds())).append(") ");
			}

			if (transactionsSearchDto.getGids() != null && !transactionsSearchDto.getGids().isEmpty()) {
				query.append(" and lot.eid IN (").append(Joiner.on(",").join(transactionsSearchDto.getGids())).append(") ");
			}

			if (transactionsSearchDto.getUnitIds() != null && !transactionsSearchDto.getUnitIds().isEmpty()) {
				query.append(" and lot.scaleid IN (").append(Joiner.on(",").join(transactionsSearchDto.getUnitIds())).append(") ");
			}

			if (transactionsSearchDto.getDesignation() != null) {
				query.append(" and n.nval like '%").append(transactionsSearchDto.getDesignation()).append("%' ");
			}

			if (transactionsSearchDto.getStockId() != null) {
				query.append(" and lot.stock_id like '").append(transactionsSearchDto.getStockId()).append("%' ");
			}

			if (transactionsSearchDto.getNotes() != null) {
				query.append(" and tr.comments like '%").append(transactionsSearchDto.getNotes()).append("%' ");
			}

			if (transactionsSearchDto.getCreatedDateFrom() != null) {
				query.append(" and DATE(tr.trndate) >= '").append(format.format(transactionsSearchDto.getCreatedDateFrom())).append("' ");
			}

			if (transactionsSearchDto.getCreatedDateTo() != null) {
				query.append(" and DATE(tr.trndate) <= '").append(format.format(transactionsSearchDto.getCreatedDateTo())).append("' ");
			}

			if (transactionsSearchDto.getCreatedByUsername() != null) {
				query.append(" and users.uname like '%").append(transactionsSearchDto.getCreatedByUsername()).append("%'");
			}

			if (transactionsSearchDto.getMinAmount() != null) {
				query.append("and tr.trnqty >= ")
					.append(transactionsSearchDto.getMinAmount()).append(" ");
			}

			if (transactionsSearchDto.getMaxAmount() != null) {
				query.append("and tr.trnqty <= ")
					.append(transactionsSearchDto.getMaxAmount()).append(" ");
			}

			if (transactionsSearchDto.getTransactionTypes() != null && !transactionsSearchDto.getTransactionTypes().isEmpty()) {
				query.append(" and trntype IN ( ").append(Joiner.on(",").join(transactionsSearchDto.getTransactionTypes())).append(") ");
			}

			if (transactionsSearchDto.getTransactionStatus() != null && !transactionsSearchDto.getTransactionStatus().isEmpty()) {
				query.append(" and trnstat IN ( ").append(Joiner.on(",").join(transactionsSearchDto.getTransactionStatus())).append(") ");
			}

			if (transactionsSearchDto.getStatusIds() != null && !transactionsSearchDto.getStatusIds().isEmpty()) {
				query.append(" and tr.trnstat IN (").append(Joiner.on(",").join(transactionsSearchDto.getStatusIds())).append(") ");
			}

			if (transactionsSearchDto.getLotStatus() != null) {
				query.append(" and lot.status = ").append(transactionsSearchDto.getLotStatus()).append(" ");
			}

			if (transactionsSearchDto.getGermplasmListIds() != null && !transactionsSearchDto.getGermplasmListIds().isEmpty()) {
				query.append(" and lot.eid in (select distinct (gid) from listdata where listid in (")
					.append(Joiner.on(",").join(transactionsSearchDto.getGermplasmListIds())).
					append(")) and lot.etype = 'GERMPLSM' ");
			}
		}

		return query.toString();
	}

	private String addSortToSearchTransactionsQuery(final String transactionsSearchQuery, final Pageable pageable) {
		final StringBuilder sortedTransactionsSearchQuery = new StringBuilder(transactionsSearchQuery);
		if (pageable != null) {
			if (pageable.getSort() != null) {
				final List<String> sorts = new ArrayList<>();
				for (final Sort.Order order : pageable.getSort()) {
					sorts.add(order.getProperty().replace(".", "") + " " + order.getDirection().toString());
				}
				if (!sorts.isEmpty()) {
					sortedTransactionsSearchQuery.append(" ORDER BY ").append(Joiner.on(",").join(sorts));
				}
			} else {
				sortedTransactionsSearchQuery.append(" ORDER BY lotLotId");
			}
		}
		return sortedTransactionsSearchQuery.toString();
	}

	public List<TransactionDto> searchTransactions(final TransactionsSearchDto transactionsSearchDto, final Pageable pageable) {
		try {
			final String filterTransactionsQuery =
				this.addSortToSearchTransactionsQuery(this.buildSearchTransactionsQuery(transactionsSearchDto), pageable);

			final SQLQuery query = this.getSession().createSQLQuery(filterTransactionsQuery);
			this.addSearchTransactionsQueryScalars(query);

			query.setResultTransformer(new AliasToBeanConstructorResultTransformer(this.getTransactionDtoConstructor()));

			GenericDAO.addPaginationToSQLQuery(query, pageable);

			final List<TransactionDto> transactionDtos = query.list();

			return transactionDtos;
		} catch (final HibernateException e) {
			throw new MiddlewareQueryException("Error at searchTransactions() query on TransactionDAO: " + e.getMessage(), e);
		}

	}

	public long countSearchTransactions(final TransactionsSearchDto transactionsSearchDto) {
		try {
			final StringBuilder countTransactionsQuery =
				new StringBuilder("Select count(1) from (").append(this.buildSearchTransactionsQuery(transactionsSearchDto))
					.append(") as filteredTransactions");
			final SQLQuery query = this.getSession().createSQLQuery(countTransactionsQuery.toString());
			return ((BigInteger) query.uniqueResult()).longValue();

		} catch (final HibernateException e) {
			throw new MiddlewareQueryException("Error at countTransactionsQuery() query on TransactionDAO: " + e.getMessage(), e);
		}
	}

	public List<TransactionDto> getAvailableBalanceTransactions(final Integer lotId) {
		try {

			if (lotId != null) {
				final StringBuilder sql = new StringBuilder(SEARCH_TRANSACTIONS_QUERY);
				sql.append(" and (tr.trnstat =").append(TransactionStatus.CONFIRMED.getIntValue()).append(" or (tr.trnstat = ")
					.append(TransactionStatus.PENDING.getIntValue()).
					append(" and tr.trntype = ").append(TransactionType.WITHDRAWAL.getId()).append(")) ");
				sql.append(" and tr.lotid = ").append(lotId).append(" ");
				final SQLQuery query = this.getSession().createSQLQuery(sql.toString());
				this.addSearchTransactionsQueryScalars(query);

				query.setResultTransformer(new AliasToBeanConstructorResultTransformer(this.getTransactionDtoConstructor()));

				final List<TransactionDto> transactionDtos = query.list();

				return transactionDtos;
			} else {
				return new ArrayList<>();
			}

		} catch (final HibernateException e) {
			throw new MiddlewareQueryException("Error at getAvailableBalanceTransactions() query on TransactionDAO: " + e.getMessage(), e);
		}
	}

	public List<Transaction> getByIds(final Set<Integer> transactionIds) {
		final List<Transaction> transactions = new ArrayList<>();

		if (transactionIds == null || transactionIds.isEmpty()) {
			return transactions;
		}

		try {
			final Criteria criteria = this.getSession().createCriteria(Transaction.class);
			criteria.add(Restrictions.in("id", transactionIds));
			return criteria.list();
		} catch (final HibernateException e) {
			final String message = "Error getByIds() query from Transaction: " + e.getMessage();
			LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}

	}

	private Constructor<TransactionDto> getTransactionDtoConstructor() {
		try {
			return TransactionDto.class.getConstructor(
				Integer.class, // transactionId
				String.class,    // createdByUsername
				String.class,    // transactionType
				Double.class,    // amount
				String.class,    // notes
				Date.class,      // createdDate
				Integer.class,   // lotId
				String.class,    // lotUUID
				Integer.class,   // gid
				String.class,    // designation
				String.class,    // stockId
				Integer.class,   // scaleId
				String.class,    // scaleName
				String.class,    // lotStatus
				String.class,    // transactionStatus
				Integer.class,   // locationId
				String.class,    // locationName
				String.class,    // locationAbbr
				String.class     // comments
			);
		} catch (final NoSuchMethodException ex) {
			throw new RuntimeException(ex);
		}
	}

	private Constructor<StudyTransactionsDto> getStudyTransactionsDtoConstructor() {
		try {
			return StudyTransactionsDto.class.getConstructor(
				Integer.class, // transactionId
				String.class,    // createdByUsername
				String.class,    // transactionType
				Double.class,    // amount
				String.class,    // notes
				Date.class,      // createdDate
				Integer.class,   // lotId
				String.class,    // lotUUID
				Integer.class,   // gid
				String.class,    // designation
				String.class,    // stockId
				Integer.class,   // scaleId
				String.class,    // scaleName
				String.class,    // lotStatus
				String.class,    // transactionStatus
				Integer.class,   // locationId
				String.class,    // locationName
				String.class,    // locationAbbr
				String.class     // comments
			);
		} catch (final NoSuchMethodException ex) {
			throw new RuntimeException(ex);
		}
	}

	private void addSearchTransactionsQueryScalars(final SQLQuery query) {
		query.addScalar("transactionId");
		query.addScalar("createdByUsername");
		query.addScalar("transactionType");
		query.addScalar("amount");
		query.addScalar("notes");
		query.addScalar("createdDate", Hibernate.DATE);
		query.addScalar("lotLotId");
		query.addScalar("lotUUID");
		query.addScalar("lotGid");
		query.addScalar("lotDesignation");
		query.addScalar("lotStockId");
		query.addScalar("lotScaleId");
		query.addScalar("lotUnitName");
		query.addScalar("lotStatus");
		query.addScalar("transactionStatus");
		query.addScalar("lotLocationId");
		query.addScalar("lotLocationName");
		query.addScalar("lotLocationAbbr");
		query.addScalar("lotComments");
	}


	public Transaction update(final Transaction transaction) {
		super.update(transaction);
		this.getSession().flush();
		return transaction;
	}

	public long countAllStudyTransactions(final Integer studyId, final StudyTransactionsRequest studyTransactionsRequest) {
		final StringBuilder obsUnitsQuerySql = this.buildObsUnitsQuery();
		final StringBuilder transactionsQuerySql = this.buildTransactionsQuery(null, obsUnitsQuerySql);

		final SQLQuery transactionsQuery =
			this.getSession().createSQLQuery("select count(1) from ( " + transactionsQuerySql.toString() + ") T");
		transactionsQuery.setParameter("studyId", studyId);

		return ((BigInteger) transactionsQuery.uniqueResult()).longValue();
	}

	public long countFilteredStudyTransactions(final Integer studyId, final StudyTransactionsRequest studyTransactionsRequest) {
		final StringBuilder obsUnitsQuerySql = this.buildObsUnitsQuery();
		addObsUnitFilters(new SqlQueryParamBuilder(obsUnitsQuerySql), studyTransactionsRequest);
		final StringBuilder transactionsQuerySql = this.buildTransactionsQuery(studyTransactionsRequest.getTransactionsSearch(), obsUnitsQuerySql);

		final SQLQuery transactionsQuery =
			this.getSession().createSQLQuery("select count(1) from ( " + transactionsQuerySql.toString() + ") T");
		transactionsQuery.setParameter("studyId", studyId);
		addObsUnitFilters(new SqlQueryParamBuilder(transactionsQuery), studyTransactionsRequest);

		return ((BigInteger) transactionsQuery.uniqueResult()).longValue();
	}

	public List<StudyTransactionsDto> searchStudyTransactions(final Integer studyId,
		final StudyTransactionsRequest studyTransactionsRequest) {

		final StringBuilder obsUnitsQuerySql = this.buildObsUnitsQuery();
		addObsUnitFilters(new SqlQueryParamBuilder(obsUnitsQuerySql), studyTransactionsRequest);

		final StringBuilder transactionsQuerySql = this.buildTransactionsQuery(studyTransactionsRequest.getTransactionsSearch(), obsUnitsQuerySql);

		addSortedPageRequestOrderBy(transactionsQuerySql, studyTransactionsRequest.getSortedPageRequest());

		// transactions query
		final SQLQuery transactionsQuery = this.getSession().createSQLQuery(transactionsQuerySql.toString());
		transactionsQuery.setParameter("studyId", studyId);
		addObsUnitFilters(new SqlQueryParamBuilder(transactionsQuery), studyTransactionsRequest);
		addSortedPageRequestPagination(transactionsQuery, studyTransactionsRequest.getSortedPageRequest());
		this.addSearchTransactionsQueryScalars(transactionsQuery);
		transactionsQuery.setResultTransformer(new AliasToBeanConstructorResultTransformer(this.getStudyTransactionsDtoConstructor()));
		final List<StudyTransactionsDto> transactions = transactionsQuery.list();

		// obs units query
		final SQLQuery obsUnitsQuery = this.getSession().createSQLQuery(obsUnitsQuerySql.toString());
		obsUnitsQuery.setParameter("studyId", studyId);
		addObsUnitFilters(new SqlQueryParamBuilder(obsUnitsQuery), studyTransactionsRequest);
		obsUnitsQuery.addScalar("ndExperimentId").addScalar("transactionId").addScalar("instanceNo", new IntegerType())
			.addScalar("entryType").addScalar("entryNo", new IntegerType()).addScalar("repNo", new IntegerType())
			.addScalar("blockNo", new IntegerType()).addScalar("plotNo", new IntegerType()).addScalar("obsUnitId");
		obsUnitsQuery.setResultTransformer(Transformers.aliasToBean(StudyTransactionsDto.ObservationUnitDto.class));
		final List<StudyTransactionsDto.ObservationUnitDto> obsUnits = obsUnitsQuery.list();

		// mapping

		final Map<Integer, List<StudyTransactionsDto.ObservationUnitDto>> obsUnitsByTransactionId =
			obsUnits.stream().collect(Collectors.groupingBy(StudyTransactionsDto.ObservationUnitDto::getTransactionId,
				LinkedHashMap::new, Collectors.toList()));

		for (final StudyTransactionsDto transaction : transactions) {
			if (obsUnitsByTransactionId.get(transaction.getTransactionId()) != null) {
				transaction.setObservationUnits(obsUnitsByTransactionId.get(transaction.getTransactionId()));
			}
		}

		return transactions;
	}

	private StringBuilder buildTransactionsQuery(
		final TransactionsSearchDto transactionsSearchDto,
		final StringBuilder obsUnitsQuerySql) {

		return new StringBuilder(""  //
				+ " select SEARCH_TRANSACTIONS_QUERY.* " //
				+ " from ( " //
				+ 		this.buildSearchTransactionsQuery(transactionsSearchDto) //
				+ " ) SEARCH_TRANSACTIONS_QUERY inner join ( " //
				+ 		obsUnitsQuerySql //
				+ " ) OBS_UNITS_QUERY on SEARCH_TRANSACTIONS_QUERY.transactionId = OBS_UNITS_QUERY.transactionId " //
				+ " group by SEARCH_TRANSACTIONS_QUERY.transactionId ");
	}

	private StringBuilder buildObsUnitsQuery() {
		return new StringBuilder("" //
			+ "     select iet.nd_experiment_id as ndExperimentId, " //
			+ "         iet.trnid as transactionId, " //
			+ "         cast(ndgeo.description as unsigned) as instanceNo, " //
			+ "         (SELECT iispcvt.definition FROM stockprop isp INNER JOIN cvterm ispcvt ON ispcvt.cvterm_id = isp.type_id INNER JOIN cvterm iispcvt ON iispcvt.cvterm_id = isp.value WHERE isp.stock_id = s.stock_id AND ispcvt.name = 'ENTRY_TYPE') AS entryType, "  //
			+ "         (SELECT ndep.value FROM nd_experimentprop ndep INNER JOIN cvterm ispcvt ON ispcvt.cvterm_id = ndep.type_id WHERE ndep.nd_experiment_id = plot.nd_experiment_id AND ispcvt.name = 'REP_NO') AS repNo, "  //
			+ "         (SELECT ndep.value FROM nd_experimentprop ndep INNER JOIN cvterm ispcvt ON ispcvt.cvterm_id = ndep.type_id WHERE ndep.nd_experiment_id = plot.nd_experiment_id AND ispcvt.name = 'BLOCK_NO') AS blockNo, "  //
			+ "         cast(s.uniquename as unsigned) as entryNo, " //
			+ "         cast((SELECT ndep.value FROM nd_experimentprop ndep INNER JOIN cvterm ispcvt ON ispcvt.cvterm_id = ndep.type_id WHERE ndep.nd_experiment_id = plot.nd_experiment_id AND ispcvt.name = 'PLOT_NO') as unsigned) AS plotNo, "  //
			+ "         plot.obs_unit_id as obsUnitId " //
			+ "     from ims_experiment_transaction iet " //
			+ "              inner join nd_experiment plot on iet.nd_experiment_id = plot.nd_experiment_id " //
			+ "              inner join nd_geolocation ndgeo on plot.nd_geolocation_id = ndgeo.nd_geolocation_id " //
			+ "              inner join project plotdata on plot.project_id = plotdata.project_id "
			+ "              inner join project study on plotdata.study_id = study.project_id "
			+ "	             inner join stock s on s.stock_id = plot.stock_id " //
			+ "     where study.project_id = :studyId"
			+ "     having 1 = 1 ");
	}

	/**
	 * Filter obs units following the inventory filters convention, using list of ids for numeric values
	 */
	private static void addObsUnitFilters(final SqlQueryParamBuilder paramBuilder,
		final StudyTransactionsRequest studyTransactionsRequest) {

		if (studyTransactionsRequest == null) {
			return;
		}

		final List<Integer> instanceNoList = studyTransactionsRequest.getInstanceNoList();
		if (instanceNoList != null && !instanceNoList.isEmpty()) {
			paramBuilder.append(" and instanceNo in (:instanceNoList) ");
			paramBuilder.setParameterList("instanceNoList", instanceNoList);
		}

		final List<Integer> plotNoList = studyTransactionsRequest.getPlotNoList();
		if (plotNoList != null && !plotNoList.isEmpty()) {
			paramBuilder.append(" and plotNo in (:plotNoList) ");
			paramBuilder.setParameterList("plotNoList", plotNoList);
		}

		final List<Integer> entryNoList = studyTransactionsRequest.getEntryNoList();
		if (entryNoList != null && !entryNoList.isEmpty()) {
			paramBuilder.append(" and entryNo in (:entryNoList) ");
			paramBuilder.setParameterList("entryNoList", entryNoList);
		}

		final String entryType = studyTransactionsRequest.getEntryType();
		if (!StringUtils.isBlank(entryType)) {
			paramBuilder.append(" and entryType like :entryType ");
			paramBuilder.setParameter("entryType", "%" + entryType + "%");
		}
	}
}
