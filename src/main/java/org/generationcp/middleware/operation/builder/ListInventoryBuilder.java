
package org.generationcp.middleware.operation.builder;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.generationcp.middleware.domain.inventory.ListDataInventory;
import org.generationcp.middleware.domain.inventory.ListEntryLotDetails;
import org.generationcp.middleware.domain.inventory.LotDetails;
import org.generationcp.middleware.domain.inventory.util.LotTransformer;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.pojos.GermplasmListData;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.ims.Lot;
import org.generationcp.middleware.pojos.oms.CVTerm;

public class ListInventoryBuilder extends Builder {

	public ListInventoryBuilder(final HibernateSessionProvider sessionProviderForLocal) {
		super(sessionProviderForLocal);
	}

	/**
	 * Return list of GermplasmListData objects for given list, with counts for number of lots with available balance and number of lots
	 * with reserved seed per entry
	 * 
	 * @param listEntries list of {@link GermplasmListData} to which we append lot counts.
	 * @return a germplasm list with the lot count
	 */
	public List<GermplasmListData> retrieveLotCountsForList(final List<GermplasmListData> listEntries) {
		final List<Integer> listEntryIds = new ArrayList<Integer>();
		final List<Integer> gids = new ArrayList<Integer>();
		final List<Integer> lrecIds = new ArrayList<Integer>();
		for (final GermplasmListData entry : listEntries) {
			listEntryIds.add(entry.getId());
			gids.add(entry.getGid());
			entry.setInventoryInfo(new ListDataInventory(entry.getId(), entry.getGid()));
			lrecIds.add(entry.getId());
		}

		if (listEntries != null && !listEntries.isEmpty()) {
			this.retrieveLotCounts(listEntryIds, listEntries, gids, lrecIds);
		}
		return listEntries;
	}

	public List<GermplasmListData> retrieveLotCountsForListEntries(final Integer listId, final List<Integer> entryIds)
			throws MiddlewareQueryException {
		List<GermplasmListData> listEntries = null;
		listEntries = this.getGermplasmListDataDAO().getByIds(entryIds);
		final List<Integer> gids = new ArrayList<Integer>();
		final List<Integer> lrecIds = new ArrayList<Integer>();
		for (final GermplasmListData entry : listEntries) {
			gids.add(entry.getGid());
			entry.setInventoryInfo(new ListDataInventory(entry.getId(), entry.getGid()));
			lrecIds.add(entry.getId());
		}
		this.retrieveLotCounts(entryIds, listEntries, gids, lrecIds);
		return listEntries;
	}

	private void retrieveLotCounts(final List<Integer> entryIds, final List<GermplasmListData> listEntries, final List<Integer> gids,
			final List<Integer> lrecIds) throws MiddlewareQueryException {

		// NEED to pass specific GIDs instead of listdata.gid because of handling for CHANGES table
		// where listdata.gid may not be the final germplasm displayed
		this.retrieveAvailableBalLotCounts(listEntries, gids);
		this.retrieveReservedLotCounts(listEntries, entryIds);
		this.retrieveStockIds(listEntries, lrecIds);
	}

	private void retrieveStockIds(final List<GermplasmListData> listEntries, final List<Integer> lrecIds) {
		final Map<Integer, String> stockIDs = this.getTransactionDao().retrieveStockIds(lrecIds);
		for (final GermplasmListData entry : listEntries) {
			final ListDataInventory inventory = entry.getInventoryInfo();
			if (inventory != null) {
				inventory.setStockIDs(stockIDs.get(entry.getId()));
			}
		}
	}

	public Integer countLotsWithAvailableBalanceForGermplasm(final Integer gid) throws MiddlewareQueryException {
		Integer lotCount = null;
		final Map<Integer, BigInteger> lotCounts = this.getLotDao().countLotsWithAvailableBalance(Collections.singletonList(gid));
		final BigInteger lotCountBigInt = lotCounts.get(gid);
		if (lotCounts != null && lotCountBigInt != null) {
			lotCount = lotCountBigInt.intValue();
		}
		return lotCount;
	}

	public List<LotDetails> retrieveInventoryLotsForGermplasm(final Integer gid) throws MiddlewareQueryException {
		List<LotDetails> lotDetails = null;
		final List<Lot> lots = this.getLotDao().getLotAggregateDataForGermplasm(gid);
		lotDetails = LotTransformer.extraLotDetails(lots);
		this.setLocationsAndScales(lotDetails);
		return lotDetails;
	}

	/**
	 * Return list of GermplasmListData objects for given list with list of lots associated per germplasm entry
	 * 
	 * @param listId
	 * @param start
	 * @param numOfRows
	 * @return
	 * @throws MiddlewareQueryException
	 */
	public List<GermplasmListData> retrieveInventoryLotsForList(final Integer listId, final int start, final int numOfRows,
			final List<GermplasmListData> listEntries) throws MiddlewareQueryException {

		final List<Integer> listEntryIds = new ArrayList<Integer>();
		final List<Integer> gids = new ArrayList<Integer>();
		for (final GermplasmListData entry : listEntries) {
			listEntryIds.add(entry.getId());
			gids.add(entry.getGid());
			entry.setInventoryInfo(new ListDataInventory(entry.getId(), entry.getGid()));
		}

		if (listEntries != null && !listEntries.isEmpty()) {
			// retrieve inventory information from local db

			// NEED to pass specific GIDs instead of listdata.gid because of handling for CHANGES table
			// where listdata.gid may not be the final germplasm displayed
			final List<Lot> lots = this.getLotDao().getLotAggregateDataForList(listId, gids);

			// add to each list entry related lot information
			final List<ListEntryLotDetails> lotRows = LotTransformer.extractLotRowsForList(listEntries, lots);
			this.setLocationsAndScales(lotRows);
		}
		return listEntries;
	}

	public List<ListEntryLotDetails> retrieveInventoryLotsForListEntry(final Integer listId, final Integer recordId, final Integer gid)
			throws MiddlewareQueryException {
		List<ListEntryLotDetails> lotRows = new ArrayList<ListEntryLotDetails>();

		final List<Lot> lots = this.getLotDao().getLotAggregateDataForListEntry(listId, gid);
		lotRows = LotTransformer.extractLotDetailsForListEntry(lots, recordId);
		this.setLocationsAndScales(lotRows);

		return lotRows;
	}

	/*
	 * Retrieve the number of lots with available balance per germplasm
	 */
	private void retrieveAvailableBalLotCounts(final List<GermplasmListData> listEntries, final List<Integer> gids)
			throws MiddlewareQueryException {
		final Map<Integer, BigInteger[]> lotCounts = this.getLotDao().getLotsWithAvailableBalanceCountAndTotalLotsCount(gids);
		for (final GermplasmListData entry : listEntries) {
			final ListDataInventory inventory = entry.getInventoryInfo();
			if (inventory != null) {
				final BigInteger[] count = lotCounts.get(entry.getGid());
				if (count != null) {
					inventory.setActualInventoryLotCount(count[0].intValue());
					inventory.setLotCount(count[1].intValue());
				} else {
					inventory.setActualInventoryLotCount(0);
					inventory.setLotCount(0);
				}
			}
		}
	}

	/*
	 * Retrieve the number of lots with reserved seeds per list entry
	 */
	private void retrieveReservedLotCounts(final List<GermplasmListData> listEntries, final List<Integer> listEntryIds)
			throws MiddlewareQueryException {
		final Map<Integer, BigInteger> reservedLotCounts = this.getTransactionDao().countLotsWithReservationForListEntries(listEntryIds);
		for (final GermplasmListData entry : listEntries) {
			final ListDataInventory inventory = entry.getInventoryInfo();
			if (inventory != null) {
				final BigInteger count = reservedLotCounts.get(entry.getId());
				if (count != null) {
					inventory.setReservedLotCount(count.intValue());
				} else {
					inventory.setReservedLotCount(0);
				}
			}
		}
	}

	/*
	 * Perform one-retrieval for central/local scales and central/local locations for list of lots
	 */
	private void setLocationsAndScales(final List<? extends LotDetails> lots) throws MiddlewareQueryException {

		final List<Integer> locationIds = new ArrayList<Integer>();
		final List<Integer> scaleIds = new ArrayList<Integer>();
		final Map<Integer, List<LotDetails>> scaleLotMap = new HashMap<Integer, List<LotDetails>>();
		final Map<Integer, List<LotDetails>> locationLotMap = new HashMap<Integer, List<LotDetails>>();

		this.createScaleAndLocationMaps(lots, locationIds, scaleIds, scaleLotMap, locationLotMap);

		final List<Location> allLocations = this.getLocationDao().getByIds(locationIds);
		final List<Term> allScales = new ArrayList<Term>();
		final List<CVTerm> cvTerms = this.getCvTermDao().getByIds(scaleIds);
		for (final CVTerm cvTerm : cvTerms) {
			allScales.add(TermBuilder.mapCVTermToTerm(cvTerm));
		}

		for (final Location location : allLocations) {
			final List<LotDetails> lotList = locationLotMap.get(location.getLocid());
			for (final LotDetails lot : lotList) {
				lot.setLocationOfLot(location);
			}
		}

		for (final Term scale : allScales) {
			final List<LotDetails> lotList = scaleLotMap.get(scale.getId());
			for (final LotDetails lot : lotList) {
				lot.setScaleOfLot(scale);
			}
		}
	}

	// create maps of scale/location IDs to lots for easier setting of Terms and Locations
	private void createScaleAndLocationMaps(final List<? extends LotDetails> lots, final List<Integer> locationIds,
			final List<Integer> scaleIds, final Map<Integer, List<LotDetails>> scaleLotMap,
			final Map<Integer, List<LotDetails>> locationLotMap) {

		for (final LotDetails lot : lots) {
			final Integer locationId = lot.getLocId();
			final List<LotDetails> lotList = locationLotMap.get(locationId);
			if (lotList != null) {
				lotList.add(lot);
			} else {
				final List<LotDetails> listLot = new ArrayList<LotDetails>();
				listLot.add(lot);
				locationLotMap.put(locationId, listLot);
			}
			locationIds.add(locationId);

			final Integer scaleId = lot.getScaleId();
			if (scaleLotMap.get(scaleId) != null) {
				scaleLotMap.get(scaleId).add(lot);
			} else {
				final List<LotDetails> listLot = new ArrayList<LotDetails>();
				listLot.add(lot);
				scaleLotMap.put(scaleId, listLot);
			}
			scaleIds.add(scaleId);
		}
	}
}
