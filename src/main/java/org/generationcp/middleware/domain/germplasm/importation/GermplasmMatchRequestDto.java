package org.generationcp.middleware.domain.germplasm.importation;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;
import org.springframework.util.CollectionUtils;

import java.util.List;

@AutoProperty
public class GermplasmMatchRequestDto {

	private List<String> germplasmPUIs;

	private List<String> names;

	public List<String> getGermplasmPUIs() {
		return this.germplasmPUIs;
	}

	public void setGermplasmPUIs(final List<String> germplasmPUIs) {
		this.germplasmPUIs = germplasmPUIs;
	}

	public List<String> getNames() {
		return this.names;
	}

	public void setNames(final List<String> names) {
		this.names = names;
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

	public boolean isValid() {
		if (CollectionUtils.isEmpty(this.germplasmPUIs) && CollectionUtils.isEmpty(this.names)) {
			return false;
		}
		return true;
	}

}
