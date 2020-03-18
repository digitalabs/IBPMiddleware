package org.generationcp.middleware.domain.inventory.manager;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

@AutoProperty
public class LotUpdateRequestDto {

	private Integer gid;
	private Integer locationId;
	private Integer unitId;
	private String notes;
	private SearchCompositeDto searchComposite;

	public Integer getGid() {
		return gid;
	}

	public void setGid(final Integer gid) {
		this.gid = gid;
	}

	public Integer getLocationId() {
		return locationId;
	}

	public void setLocationId(final Integer locationId) {
		this.locationId = locationId;
	}

	public Integer getUnitId() {
		return unitId;
	}

	public void setUnitId(final Integer unitId) {
		this.unitId = unitId;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(final String notes) {
		this.notes = notes;
	}

	public SearchCompositeDto getSearchComposite() {
		return searchComposite;
	}

	public void setSearchComposite(final SearchCompositeDto searchComposite) {
		this.searchComposite = searchComposite;
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