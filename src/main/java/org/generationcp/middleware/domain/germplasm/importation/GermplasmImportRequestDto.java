package org.generationcp.middleware.domain.germplasm.importation;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import java.util.List;

@AutoProperty
@JsonPropertyOrder({
	"connectUsing", "skipIfExists", "germplasmList"})
public class GermplasmImportRequestDto {

	public enum PedigreeConnectionType {
		NONE, GID, GUID
	}

	public GermplasmImportRequestDto() {
	}

	private PedigreeConnectionType connectUsing;
	private boolean skipIfExists = false;
	private List<GermplasmImportDTO> germplasmList;

	public PedigreeConnectionType getConnectUsing() {
		return this.connectUsing;
	}

	public void setConnectUsing(final PedigreeConnectionType connectUsing) {
		this.connectUsing = connectUsing;
	}

	public List<GermplasmImportDTO> getGermplasmList() {
		return this.germplasmList;
	}

	public void setGermplasmList(final List<GermplasmImportDTO> germplasmList) {
		this.germplasmList = germplasmList;
	}

	public boolean isSkipIfExists() {
		return this.skipIfExists;
	}

	public void setSkipIfExists(final boolean skipIfExists) {
		this.skipIfExists = skipIfExists;
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
