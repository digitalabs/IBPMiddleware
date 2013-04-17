package org.generationcp.middleware.v2.pojos;

import java.io.Serializable;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * http://gmod.org/wiki/Chado_Tables#Table:_cvtermsynonym
 * 
 * @author tippsgo
 *
 */
@Entity
@Table(name = "cvtermprop",
		uniqueConstraints = {@UniqueConstraint(columnNames = {"cvterm_id", "type_id", "value", "rank"})})
public class CVTermProperty implements Serializable {

	private static final long serialVersionUID = -6496723408899540369L;

	@Id
	@Basic(optional = false)
	@GeneratedValue
	@Column(name = "cvtermprop_id")
	private Integer cvTermPropertyId;
	
	/**
	 * CVTerm that owns the property.
	 */
	@ManyToOne(targetEntity = CVTerm.class)
	@JoinColumn(name = "cvterm_id", nullable = false)
	private CVTerm cvTerm;

	/**
	 * Type of property.
	 */
	@ManyToOne(targetEntity = CVTerm.class)
	@JoinColumn(name = "type_id", nullable = false)
	private CVTerm type;

	/**
	 * Value of the property.
	 */
	@Column(name = "value")
	private String value;
	
	/**
	 * Rank of the property.
	 */
	@Column(name = "rank")
	private Integer rank;

	
	public Integer getCvTermPropertyId() {
		return cvTermPropertyId;
	}

	public void setCvTermPropertyId(Integer cvTermPropertyId) {
		this.cvTermPropertyId = cvTermPropertyId;
	}

	public CVTerm getCvTerm() {
		return cvTerm;
	}

	public void setCvTerm(CVTerm cvTerm) {
		this.cvTerm = cvTerm;
	}

	public CVTerm getType() {
		return type;
	}

	public void setType(CVTerm type) {
		this.type = type;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public Integer getRank() {
		return rank;
	}

	public void setRank(Integer rank) {
		this.rank = rank;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((cvTermPropertyId == null) ? 0 : cvTermPropertyId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		CVTermProperty other = (CVTermProperty) obj;
		if (cvTermPropertyId == null) {
			if (other.cvTermPropertyId != null)
				return false;
		} else if (!cvTermPropertyId.equals(other.cvTermPropertyId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "CVTermProperty [cvTermPropertyId=" + cvTermPropertyId
				+ ", cvTerm=" + cvTerm + ", type=" + type + ", value=" + value
				+ ", rank=" + rank + "]";
	}
		
}
