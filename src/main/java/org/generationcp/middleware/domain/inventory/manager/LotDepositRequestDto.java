package org.generationcp.middleware.domain.inventory.manager;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.Map;

@AutoProperty
public class LotDepositRequestDto {

	private SearchCompositeDto selectedLots;

	private Map<String, Double> depositsPerUnit;

	private String notes;

	public SearchCompositeDto getSelectedLots() {
		return selectedLots;
	}

	public void setSelectedLots(final SearchCompositeDto selectedLots) {
		this.selectedLots = selectedLots;
	}

	public Map<String, Double> getDepositsPerUnit() {
		return depositsPerUnit;
	}

	public void setDepositsPerUnit(final Map<String, Double> depositsPerUnit) {
		this.depositsPerUnit = depositsPerUnit;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(final String notes) {
		this.notes = notes;
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
	public boolean equals(final Object o) {
		return Pojomatic.equals(this, o);
	}

}
