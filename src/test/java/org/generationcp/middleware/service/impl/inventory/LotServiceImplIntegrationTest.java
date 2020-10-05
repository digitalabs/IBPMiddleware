package org.generationcp.middleware.service.impl.inventory;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.RandomStringUtils;
import org.generationcp.middleware.IntegrationTestBase;
import org.generationcp.middleware.data.initializer.GermplasmTestDataInitializer;
import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.generationcp.middleware.domain.inventory.manager.ExtendedLotDto;
import org.generationcp.middleware.domain.inventory.manager.LotMultiUpdateRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotSingleUpdateRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotUpdateRequestDto;
import org.generationcp.middleware.domain.inventory.manager.LotsSearchDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionDto;
import org.generationcp.middleware.domain.inventory.manager.TransactionsSearchDto;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.manager.DaoFactory;
import org.generationcp.middleware.manager.api.GermplasmDataManager;
import org.generationcp.middleware.manager.api.InventoryDataManager;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.LocationType;
import org.generationcp.middleware.pojos.UDTableType;
import org.generationcp.middleware.pojos.ims.EntityType;
import org.generationcp.middleware.pojos.ims.Lot;
import org.generationcp.middleware.pojos.ims.LotStatus;
import org.generationcp.middleware.pojos.ims.Transaction;
import org.generationcp.middleware.pojos.ims.TransactionStatus;
import org.generationcp.middleware.pojos.ims.TransactionType;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.util.Util;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.hasToString;

public class LotServiceImplIntegrationTest extends IntegrationTestBase {

	private TransactionServiceImpl transactionService;

	private LotServiceImpl lotService;

	private DaoFactory daoFactory;

	private Integer userId, gid;

	private Lot lot;

	private Integer storageLocationId;

	private static final int GROUP_ID = 0;

	private static final int UNIT_ID = TermId.SEED_AMOUNT_G.getId();

	@Autowired
	private GermplasmDataManager germplasmDataManager;

	@Autowired
	private LocationDataManager locationDataManager;

	@Autowired
	private OntologyVariableDataManager ontologyVariableDataManager;

	@Before
	public void setUp() {
		this.transactionService = new TransactionServiceImpl(this.sessionProvder);
		this.lotService = new LotServiceImpl(this.sessionProvder);
		this.daoFactory = new DaoFactory(this.sessionProvder);
		this.lotService.setTransactionService(this.transactionService);
		this.lotService.setOntologyVariableDataManager(this.ontologyVariableDataManager);
		this.createGermplasm();
		this.userId = this.findAdminUser();
		this.resolveStorageLocation();
		this.createLot();
		this.createTransactions();
	}

	@Test
	public void lotWithOpenBalanceClosed_Ok() {
		this.lotService.closeLots(this.userId, Collections.singletonList(this.lot.getId()));
		final LotsSearchDto searchDto = new LotsSearchDto();
		searchDto.setLotIds(Collections.singletonList(this.lot.getId()));
		final List<ExtendedLotDto> extendedLotDtos = this.lotService.searchLots(searchDto, null);

		final TransactionsSearchDto pendingTransactionSearch = new TransactionsSearchDto();
		pendingTransactionSearch.setLotIds(Collections.singletonList(this.lot.getId()));
		pendingTransactionSearch.setTransactionStatus(Collections.singletonList(TransactionStatus.PENDING.getIntValue()));
		final List<TransactionDto> pendingTransactions = this.transactionService.searchTransactions(pendingTransactionSearch, null);

		final TransactionsSearchDto discardedTransactionSearch = new TransactionsSearchDto();
		discardedTransactionSearch.setLotIds(Collections.singletonList(this.lot.getId()));
		discardedTransactionSearch.setTransactionTypes(Collections.singletonList(TransactionType.DISCARD.getId()));
		final List<TransactionDto> discardedTransactions = this.transactionService.searchTransactions(discardedTransactionSearch, null);
		discardedTransactions.sort((TransactionDto t1, TransactionDto t2) -> t2.getTransactionId().compareTo(t1.getTransactionId()));

		assertThat(extendedLotDtos.get(0).getStatus(), hasToString(LotStatus.CLOSED.name()));
		assertThat(extendedLotDtos.get(0).getAvailableBalance(), equalTo(0D));
		assertThat(pendingTransactions, hasSize(0));
		assertThat(discardedTransactions.get(0).getAmount(), equalTo(-20D));
	}

	@Test
	public void lotWithNoOpenBalanceClosed_Ok() {
		final Transaction confirmedWithdrawal =
			new Transaction(null, this.userId, this.lot, Util.getCurrentDate(), TransactionStatus.CONFIRMED.getIntValue(),
				-20D, "Transaction 3", 0, null, null, null,
				Double.valueOf(0), this.userId, TransactionType.WITHDRAWAL.getId());
		this.daoFactory.getTransactionDAO().save(confirmedWithdrawal);

		final TransactionsSearchDto discardedTransactionSearch = new TransactionsSearchDto();
		discardedTransactionSearch.setLotIds(Collections.singletonList(this.lot.getId()));
		discardedTransactionSearch.setTransactionTypes(Collections.singletonList(TransactionType.DISCARD.getId()));
		final Integer discardedTrxsBeforeClosingLot = this.transactionService.searchTransactions(discardedTransactionSearch, null).size();

		this.lotService.closeLots(this.userId, Collections.singletonList(this.lot.getId()));
		final Integer discardedTrxsAfterClosingLot = this.transactionService.searchTransactions(discardedTransactionSearch, null).size();

		final LotsSearchDto searchDto = new LotsSearchDto();
		searchDto.setLotIds(Collections.singletonList(this.lot.getId()));
		final List<ExtendedLotDto> extendedLotDtos = this.lotService.searchLots(searchDto, null);

		final TransactionsSearchDto pendingTransactionSearch = new TransactionsSearchDto();
		pendingTransactionSearch.setLotIds(Collections.singletonList(this.lot.getId()));
		pendingTransactionSearch.setTransactionStatus(Collections.singletonList(TransactionStatus.PENDING.getIntValue()));
		final List<TransactionDto> pendingTransactions = this.transactionService.searchTransactions(pendingTransactionSearch, null);

		assertThat(extendedLotDtos.get(0).getStatus(), hasToString(LotStatus.CLOSED.name()));
		assertThat(extendedLotDtos.get(0).getAvailableBalance(), equalTo(0D));
		assertThat(pendingTransactions, hasSize(0));
		assertThat(discardedTrxsAfterClosingLot, equalTo(discardedTrxsBeforeClosingLot));

	}

	@Test
	public void lotSingleUpdateNotes_Ok() {
		final LotUpdateRequestDto lotUpdateRequestDto = new LotUpdateRequestDto();
		final LotSingleUpdateRequestDto singleInput = new LotSingleUpdateRequestDto();
		singleInput.setNotes("Test1");
		lotUpdateRequestDto.setSingleInput(singleInput);

		final Set<String> itemIds = Sets.newHashSet(this.lot.getLotUuId());
		SearchCompositeDto searchCompositeDto = new SearchCompositeDto();
		lotUpdateRequestDto.getSingleInput().setSearchComposite(new SearchCompositeDto());
		lotUpdateRequestDto.getSingleInput().getSearchComposite().setItemIds(itemIds);
		final LotsSearchDto searchDto = new LotsSearchDto();
		searchDto.setLotIds(Collections.singletonList(this.lot.getId()));
		List<ExtendedLotDto> extendedLotDtos = this.lotService.searchLots(searchDto, null);

		this.lotService.updateLots(extendedLotDtos, lotUpdateRequestDto);
		assertThat(this.lot.getComments(), hasToString("Test1"));
	}

	@Test
	public void lotSingleUpdateNotesUnit_Ok() {
		final LotUpdateRequestDto lotUpdateRequestDto = new LotUpdateRequestDto();
		final LotSingleUpdateRequestDto singleInput = new LotSingleUpdateRequestDto();
		singleInput.setNotes("Test2");
		singleInput.setUnitId(8267);
		lotUpdateRequestDto.setSingleInput(singleInput);

		final Set<String> itemIds = Sets.newHashSet(this.lot.getLotUuId());
		SearchCompositeDto searchCompositeDto = new SearchCompositeDto();
		lotUpdateRequestDto.getSingleInput().setSearchComposite(new SearchCompositeDto());
		lotUpdateRequestDto.getSingleInput().getSearchComposite().setItemIds(itemIds);
		final LotsSearchDto searchDto = new LotsSearchDto();
		searchDto.setLotIds(Collections.singletonList(this.lot.getId()));
		List<ExtendedLotDto> extendedLotDtos = this.lotService.searchLots(searchDto, null);

		this.lotService.updateLots(extendedLotDtos, lotUpdateRequestDto);
		assertThat(this.lot.getComments(), hasToString("Test2"));
		assertThat(this.lot.getScaleId(), equalTo(8267));
	}

	@Test
	public void lotMultiUpdateNotesUnit_Ok() {
		final LotUpdateRequestDto lotUpdateRequestDto = new LotUpdateRequestDto();
		final LotMultiUpdateRequestDto multiInput = new LotMultiUpdateRequestDto();
		final List<LotMultiUpdateRequestDto.LotUpdateDto> lotList = new ArrayList<>();
		LotMultiUpdateRequestDto.LotUpdateDto lot = new LotMultiUpdateRequestDto.LotUpdateDto();
		lot.setLotUID(this.lot.getLotUuId());
		lot.setUnitName("SEED_AMOUNT_kg");
		lot.setNotes("Test3");
		lotList.add(lot);
		multiInput.setLotList(lotList);
		lotUpdateRequestDto.setMultiInput(multiInput);

		final LotsSearchDto searchDto = new LotsSearchDto();
		searchDto.setLotIds(Collections.singletonList(this.lot.getId()));
		assertThat(this.lot.getScaleId(), equalTo(8264));
		assertThat(this.lot.getComments(), hasToString("Lot"));

		List<ExtendedLotDto> extendedLotDtos = this.lotService.searchLots(searchDto, null);

		this.lotService.updateLots(extendedLotDtos, lotUpdateRequestDto);
		assertThat(this.lot.getComments(), hasToString("Test3"));
		assertThat(this.lot.getScaleId(), equalTo(8267));
	}

	private void createGermplasm() {
		final CropType cropType = new CropType();
		cropType.setUseUUID(false);
		final Germplasm germplasm = GermplasmTestDataInitializer.createGermplasm(Integer.MIN_VALUE);
		germplasm.setMgid(GROUP_ID);
		this.germplasmDataManager.addGermplasm(germplasm, germplasm.getPreferredName(), cropType);
		this.gid = germplasm.getGid();
	}

	private void createLot() {
		this.lot = new Lot(null, this.userId, EntityType.GERMPLSM.name(), this.gid, this.storageLocationId, UNIT_ID, LotStatus.ACTIVE.getIntValue(), 0,
			"Lot", RandomStringUtils.randomAlphabetic(35));
		this.lot.setLotUuId(RandomStringUtils.randomAlphabetic(35));
		this.daoFactory.getLotDao().save(this.lot);
	}

	private void createTransactions() {

		final Transaction confirmedDeposit =
			new Transaction(null, this.userId, this.lot, Util.getCurrentDate(), TransactionStatus.CONFIRMED.getIntValue(),
				20D, "Transaction 1", Util.getCurrentDateAsIntegerValue(), null, null, null,
				Double.valueOf(0), this.userId, TransactionType.DEPOSIT.getId());

		final Transaction pendingDeposit =
			new Transaction(null, this.userId, this.lot, Util.getCurrentDate(), TransactionStatus.PENDING.getIntValue(),
				20D, "Transaction 2", 0, null, null, null,
				Double.valueOf(0), this.userId, TransactionType.DEPOSIT.getId());

		this.daoFactory.getTransactionDAO().save(confirmedDeposit);
		this.daoFactory.getTransactionDAO().save(pendingDeposit);

	}

	private void resolveStorageLocation() {
		final Integer id = this.locationDataManager.getUserDefinedFieldIdOfCode(UDTableType.LOCATION_LTYPE, LocationType.SSTORE.name());
		this.storageLocationId = this.daoFactory.getLocationDAO().getDefaultLocationByType(id).getLocid();
	}

}
