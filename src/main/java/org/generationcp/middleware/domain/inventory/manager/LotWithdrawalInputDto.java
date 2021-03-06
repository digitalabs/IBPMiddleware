package org.generationcp.middleware.domain.inventory.manager;

import org.generationcp.middleware.domain.inventory.common.SearchCompositeDto;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.Map;

/**
 * Created by clarysabel on 2/20/20.
 */
@AutoProperty
public class LotWithdrawalInputDto {

	@AutoProperty
	public static class WithdrawalAmountInstruction{
		private boolean reserveAllAvailableBalance;

		private Double withdrawalAmount;

		public WithdrawalAmountInstruction() {
		}

		public WithdrawalAmountInstruction(final boolean reserveAllAvailableBalance, final Double withdrawalAmount) {
			this.reserveAllAvailableBalance = reserveAllAvailableBalance;
			this.withdrawalAmount = withdrawalAmount;
		}

		public boolean isReserveAllAvailableBalance() {
			return reserveAllAvailableBalance;
		}

		public void setReserveAllAvailableBalance(final boolean reserveAllAvailableBalance) {
			this.reserveAllAvailableBalance = reserveAllAvailableBalance;
		}

		public Double getWithdrawalAmount() {
			return withdrawalAmount;
		}

		public void setWithdrawalAmount(final Double withdrawalAmount) {
			this.withdrawalAmount = withdrawalAmount;
		}

		@Override
		public int hashCode() {
			return Pojomatic.hashCode(this);
		}

		@Override
		public String toString() {
			return Pojomatic.toString(this);
		}

		@Override
		public boolean equals(Object o) {
			return Pojomatic.equals(this, o);
		}

	}


	private SearchCompositeDto<Integer, String> selectedLots;

	private Map<String, WithdrawalAmountInstruction> withdrawalsPerUnit;

	private String notes;

	public Map<String, WithdrawalAmountInstruction> getWithdrawalsPerUnit() {
		return withdrawalsPerUnit;
	}

	public void setWithdrawalsPerUnit(final Map<String, WithdrawalAmountInstruction> withdrawalsPerUnit) {
		this.withdrawalsPerUnit = withdrawalsPerUnit;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(final String notes) {
		this.notes = notes;
	}

	public SearchCompositeDto<Integer, String> getSelectedLots() {
		return selectedLots;
	}

	public void setSelectedLots(final SearchCompositeDto<Integer, String> selectedLots) {
		this.selectedLots = selectedLots;
	}

	@Override
	public int hashCode() {
		return Pojomatic.hashCode(this);
	}

	@Override
	public String toString() {
		return Pojomatic.toString(this);
	}

	@Override
	public boolean equals(Object o) {
		return Pojomatic.equals(this, o);
	}

}
