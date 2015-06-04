/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package org.generationcp.middleware.pojos;

import java.io.Serializable;
import java.util.Comparator;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * POJO for location details query.
 *
 * select locid, lname as location_name, c.isofull as country_full_name, labbr as location_abbreviation, ud.fname as location_type, ud.fdesc
 * as location_description from location l inner join cntry c on l.cntryid = c.cntryid inner join udflds ud on ud.fldno = l.ltype where
 * locid = 1
 *
 * @author Aldrich Abrogena
 */
@Entity
@Table(name = "location")
// JAXB Element Tags for JSON output
@XmlRootElement(name = "location")
@XmlType(propOrder = {"locid", "location_name", "country_full_name", "location_abbreviation", "location_type", "location_description"})
@XmlAccessorType(XmlAccessType.NONE)
public class LocationDetails implements Serializable, Comparable<LocationDetails> {

	private static final long serialVersionUID = 1L;

	public static final String GET_ALL = "getAllLocation";
	public static final String COUNT_ALL = "countAllLocation";

	@Id
	@Basic(optional = false)
	@Column(name = "locid")
	private Integer locid;

	@Basic(optional = false)
	@Column(name = "location_name")
	private String location_name;

	@Basic(optional = false)
	@Column(name = "country_full_name")
	private String country_full_name;

	@Basic(optional = false)
	@Column(name = "cntryid")
	private Integer cntryid;

	@Basic(optional = false)
	@Column(name = "location_abbreviation")
	private String location_abbreviation;

	@Basic(optional = false)
	@Column(name = "location_type")
	private String location_type;

	@Basic(optional = false)
	@Column(name = "ltype")
	private Integer ltype;

	@Basic(optional = false)
	@Column(name = "location_description")
	private String location_description;

	@Basic(optional = true)
	@Column(name = "latitude")
	private Double latitude;

	@Basic(optional = true)
	@Column(name = "longitude")
	private Double longitude;

	@Basic(optional = true)
	@Column(name = "altitude")
	private Double altitude;

	public LocationDetails() {
	}

	public LocationDetails(Integer locid) {
		this.locid = locid;
	}

	public LocationDetails(Integer locid, String location_name, String country_full_name, String location_abbreviation,
			String location_type, String location_description) {
		super();
		this.locid = locid;
		this.location_name = location_name;
		this.country_full_name = country_full_name;
		this.location_abbreviation = location_abbreviation;
		this.location_type = location_type;
		this.location_description = location_description;

	}

	@Override
	public int hashCode() {
		return this.getLocid();
	}

	public Integer getLocid() {
		return this.locid;
	}

	public void setLocid(Integer locid) {
		this.locid = locid;
	}

	public String getLocation_name() {
		return this.location_name;
	}

	public void setLocation_name(String location_name) {
		this.location_name = location_name;
	}

	public String getCountry_full_name() {
		return this.country_full_name;
	}

	public void setCountry_full_name(String country_full_name) {
		this.country_full_name = country_full_name;
	}

	public String getLocation_abbreviation() {
		return this.location_abbreviation;
	}

	public void setLocation_abbreviation(String location_abbreviation) {
		this.location_abbreviation = location_abbreviation;
	}

	public String getLocation_type() {
		return this.location_type;
	}

	public void setLocation_type(String location_type) {
		this.location_type = location_type;
	}

	public String getLocation_description() {
		return this.location_description;
	}

	public void setLocation_description(String location_description) {
		this.location_description = location_description;
	}

	public Double getLatitude() {
		return this.latitude;
	}

	public void setLatitude(Double latitude) {
		this.latitude = latitude;
	}

	public Double getLongitude() {
		return this.longitude;
	}

	public void setLongitude(Double longitude) {
		this.longitude = longitude;
	}

	public Double getAltitude() {
		return this.altitude;
	}

	public void setAltitude(Double altitude) {
		this.altitude = altitude;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}

		if (obj instanceof LocationDetails) {
			LocationDetails param = (LocationDetails) obj;
			if (this.getLocid().equals(param.getLocid())) {
				return true;
			}
		}

		return false;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Location [locid=");
		builder.append(this.locid);
		builder.append(", location_description=");
		builder.append(this.location_description);
		builder.append(", country_full_name=");
		builder.append(this.country_full_name);
		builder.append(", location_type=");
		builder.append(this.location_type);
		builder.append(", location_abbreviation=");
		builder.append(this.location_abbreviation);
		builder.append(", location_name=");
		builder.append(this.location_name);
		builder.append(this.latitude);
		builder.append(", longitude=");
		builder.append(this.longitude);
		builder.append(", altitude=");
		builder.append(this.altitude);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int compareTo(LocationDetails compareLocation) {
		String compareName = compareLocation.getLocation_name();

		// ascending order
		return this.location_name.compareTo(compareName);
	}

	public static Comparator<LocationDetails> LocationNameComparator = new Comparator<LocationDetails>() {

		@Override
		public int compare(LocationDetails location1, LocationDetails location2) {
			String locationName1 = location1.getLocation_name().toUpperCase();
			String locationName2 = location2.getLocation_name().toUpperCase();

			// ascending order
			return locationName1.compareTo(locationName2);
		}

	};

	public Integer getCntryid() {
		return this.cntryid;
	}

	public void setCntryid(Integer cntryid) {
		this.cntryid = cntryid;
	}

	public Integer getLtype() {
		return this.ltype;
	}

	public void setLtype(Integer ltype) {
		this.ltype = ltype;
	}
}
