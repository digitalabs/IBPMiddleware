/*******************************************************************************
 * Copyright (c) 2013, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package org.generationcp.middleware.pojos.gdms;

/**
 * POJO corresponding to SNP Genotyping data row used in setSNP.
 *
 */
public class SNPDataRow {

	private AccMetadataSet accMetadataSet;

	private CharValues charValues;

	public SNPDataRow() {
	}

	public SNPDataRow(AccMetadataSet accMetadataSet, CharValues charValues) {
		this.accMetadataSet = accMetadataSet;
		this.charValues = charValues;
	}

	public AccMetadataSet getAccMetadataSet() {
		return this.accMetadataSet;
	}

	public void setAccMetadataSet(AccMetadataSet accMetadataSet) {
		this.accMetadataSet = accMetadataSet;
	}

	public CharValues getCharValues() {
		return this.charValues;
	}

	public void setCharValues(CharValues charValues) {
		this.charValues = charValues;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.accMetadataSet == null ? 0 : this.accMetadataSet.hashCode());
		result = prime * result + (this.charValues == null ? 0 : this.charValues.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		SNPDataRow other = (SNPDataRow) obj;
		if (this.accMetadataSet == null) {
			if (other.accMetadataSet != null) {
				return false;
			}
		} else if (!this.accMetadataSet.equals(other.accMetadataSet)) {
			return false;
		}
		if (this.charValues == null) {
			if (other.charValues != null) {
				return false;
			}
		} else if (!this.charValues.equals(other.charValues)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SNPDataRow [accMetadataSet=");
		builder.append(this.accMetadataSet);
		builder.append(", charValues=");
		builder.append(this.charValues);
		builder.append("]");
		return builder.toString();
	}

}
