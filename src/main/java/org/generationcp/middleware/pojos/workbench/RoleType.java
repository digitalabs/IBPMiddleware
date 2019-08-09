package org.generationcp.middleware.pojos.workbench;

import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "role_type")
@AutoProperty
public class RoleType {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "role_type_id", nullable = false)
	private Integer id;

	@Column(name = "name", nullable = false)
	private String name;

	public RoleType() {
	}

	public RoleType(final String name) {
		this.name = name;
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(final Integer roleTypeId) {
		this.id = roleTypeId;
	}

	public String getName() {
		return this.name;
	}

	public void setName(final String name) {
		this.name = name;
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
