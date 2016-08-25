
package org.generationcp.middleware.pojos;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.generationcp.middleware.interfaces.GermplasmExportSource;
import org.hibernate.annotations.NotFound;
import org.hibernate.annotations.NotFoundAction;

@Entity
@Table(name = "listdata_project")
public class ListDataProject implements Serializable, GermplasmExportSource {

	private static final long serialVersionUID = 1L;
	public static final String PEDIGREE_DUPE = "Pedigree Dupe";
	public static final String PLOT_DUPE = "Plot Dupe";
	public static final String PEDIGREE_RECIP = "Pedigree Recip";
	public static final String PLOT_RECIP = "Plot Recip";

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(name = "listdata_project_id")
	private Integer listDataProjectId;

	@ManyToOne(targetEntity = GermplasmList.class)
	@JoinColumn(name = "list_id", nullable = false)
	@NotFound(action = NotFoundAction.IGNORE)
	private GermplasmList list;

	@Basic(optional = false)
	@Column(name = "germplasm_id")
	private Integer germplasmId;

	@Basic(optional = false)
	@Column(name = "check_type")
	private Integer checkType;

	@Basic(optional = false)
	@Column(name = "entry_id")
	private Integer entryId;

	@Basic(optional = false)
	@Column(name = "entry_code")
	private String entryCode;

	@Column(name = "seed_source")
	private String seedSource;

	@Basic(optional = false)
	@Column(name = "designation")
	private String designation;

	@Column(name = "group_name")
	private String groupName;

	@Column(name = "duplicate_notes")
	private String duplicate;

	@Column(name = "notes")
	private String notes;

	@Column(name = "crossing_date")
	private Integer crossingDate;

	/***
	 * The following will only be field when getListDataProjectWithParents() is called, otherwise, it will always be null
	 */
	@Transient
	private String femaleParent = null;

	@Transient
	private Integer fgid = null;

	@Transient
	private String maleParent = null;

	@Transient
	private Integer mgid = null;

	@Transient
	private Integer groupId = null;

	@Transient
	private String checkTypeDescription = null;

	/**
	 * @return the listDataProjectId
	 */
	public Integer getListDataProjectId() {
		return this.listDataProjectId;
	}

	/**
	 * @param listDataProjectId the listDataProjectId to set
	 */
	public void setListDataProjectId(final Integer listDataProjectId) {
		this.listDataProjectId = listDataProjectId;
	}

	/**
	 * @return the list
	 */
	public GermplasmList getList() {
		return this.list;
	}

	/**
	 * @param list the list to set
	 */
	public void setList(final GermplasmList list) {
		this.list = list;
	}

	/**
	 * @return the germplasmId
	 */
	@Override
	public Integer getGermplasmId() {
		return this.germplasmId;
	}

	/**
	 * @param germplasmId the germplasmId to set
	 */
	public void setGermplasmId(final Integer germplasmId) {
		this.germplasmId = germplasmId;
	}

	/**
	 * @return the checkType
	 */
	@Override
	public Integer getCheckType() {
		return this.checkType;
	}

	/**
	 * @param checkType the checkType to set
	 */
	public void setCheckType(final Integer checkType) {
		this.checkType = checkType;
	}

	/**
	 * @return the entryId
	 */
	@Override
	public Integer getEntryId() {
		return this.entryId;
	}

	/**
	 * @param entryId the entryId to set
	 */
	public void setEntryId(final Integer entryId) {
		this.entryId = entryId;
	}

	/**
	 * @return the entryCode
	 */
	@Override
	public String getEntryCode() {
		return this.entryCode;
	}

	/**
	 * @param entryCode the entryCode to set
	 */
	public void setEntryCode(final String entryCode) {
		this.entryCode = entryCode;
	}

	/**
	 * @return the seedSource
	 */
	@Override
	public String getSeedSource() {
		return this.seedSource;
	}

	/**
	 * @param seedSource the seedSource to set
	 */
	public void setSeedSource(final String seedSource) {
		this.seedSource = seedSource;
	}

	/**
	 * @return the designation
	 */
	@Override
	public String getDesignation() {
		return this.designation;
	}

	/**
	 * @param designation the designation to set
	 */
	public void setDesignation(final String designation) {
		this.designation = designation;
	}

	/**
	 * @return the groupName
	 */
	@Override
	public String getGroupName() {
		return this.groupName;
	}

	/**
	 * @param groupName the groupName to set
	 */
	public void setGroupName(final String groupName) {
		this.groupName = groupName;
	}

	public String getDuplicate() {
		return this.duplicate;
	}

	public void setDuplicate(final String duplicate) {
		this.duplicate = duplicate;
	}

	public String getNotes() {
		return this.notes;
	}

	public void setNotes(final String notes) {
		this.notes = notes;
	}

	public Integer getCrossingDate() {
		return this.crossingDate;
	}

	public void setCrossingDate(final Integer crossingDate) {
		this.crossingDate = crossingDate;
	}

	@Override
	public String getFemaleParent() {
		return this.femaleParent;
	}

	public void setFemaleParent(final String femaleParent) {
		this.femaleParent = femaleParent;
	}

	@Override
	public Integer getFgid() {
		return this.fgid;
	}

	public void setFgid(final Integer fgid) {
		this.fgid = fgid;
	}

	@Override
	public String getMaleParent() {
		return this.maleParent;
	}

	public void setMaleParent(final String maleParent) {
		this.maleParent = maleParent;
	}

	@Override
	public Integer getMgid() {
		return this.mgid;
	}

	public void setMgid(final Integer mgid) {
		this.mgid = mgid;
	}

	@Override
	public String toString() {
		final StringBuffer sb = new StringBuffer("ListDataProject{");
		sb.append("listDataProjectId=").append(this.listDataProjectId);
		sb.append(", list=").append(this.list);
		sb.append(", germplasmId=").append(this.germplasmId);
		sb.append(", checkType=").append(this.checkType);
		sb.append(", entryId=").append(this.entryId);
		sb.append(", entryCode='").append(this.entryCode).append('\'');
		sb.append(", seedSource='").append(this.seedSource).append('\'');
		sb.append(", designation='").append(this.designation).append('\'');
		sb.append(", groupName='").append(this.groupName).append('\'');
		sb.append(", duplicate='").append(this.duplicate).append('\'');
		sb.append(", femaleParent='").append(this.femaleParent).append('\'');
		sb.append(", fgid=").append(this.fgid);
		sb.append(", maleParent='").append(this.maleParent).append('\'');
		sb.append(", mgid=").append(this.mgid);
		sb.append(", checkTypeDescription='").append(this.checkTypeDescription).append('\'');
		sb.append(", notes='").append(this.notes).append('\'');
		sb.append(", crossingDate='").append(this.crossingDate).append('\'');
		sb.append('}');
		return sb.toString();
	}

	public Boolean isPedigreeDupe() {
		if (this.duplicate != null) {
			return this.duplicate.contains(ListDataProject.PEDIGREE_DUPE);
		}
		return false;
	}

	public Boolean isPlotDupe() {
		if (this.duplicate != null) {
			return this.duplicate.contains(ListDataProject.PLOT_DUPE);
		}
		return false;
	}

	public Boolean isPedigreeRecip() {
		if (this.duplicate != null) {
			return this.duplicate.contains(ListDataProject.PEDIGREE_RECIP);
		}
		return false;
	}

	public Boolean isPlotRecip() {
		if (this.duplicate != null) {
			return this.duplicate.contains(ListDataProject.PLOT_RECIP);
		}
		return false;
	}

	public List<Integer> parsePedigreeDupeInformation() {
		final List<Integer> returnVal = new ArrayList<>();

		if (!this.isPedigreeDupe()) {
			return returnVal;
		}

		return this.parseDuplicateString(ListDataProject.PEDIGREE_DUPE);
	}

	public List<Integer> parsePlotDupeInformation() {
		if (!this.isPlotDupe()) {
			return new ArrayList<Integer>();
		}

		return this.parseDuplicateString(ListDataProject.PLOT_DUPE);
	}

	public List<Integer> parsePlotReciprocalInformation() {
		if (!this.isPlotRecip()) {
			return new ArrayList<Integer>();
		}

		return this.parseDuplicateString(ListDataProject.PLOT_RECIP);
	}

	public List<Integer> parsePedigreeReciprocalInformation() {
		if (!this.isPedigreeRecip()) {
			return new ArrayList<Integer>();
		}

		return this.parseDuplicateString(ListDataProject.PEDIGREE_RECIP);
	}

	protected List<Integer> parseDuplicateString(final String forRemoval) {
		// The duplicate string's value can be "Pedigree Dupe: 2, 3, 4 | Plot Recip: 20" or any other combinations of duplicates and recips
		// so we need to split the string using the "|" delimiter
		final String[] duplicateArray = this.duplicate.split("\\|");
		final List<Integer> returnVal = new ArrayList<>();
		for (final String duplicateString : duplicateArray) {
			if (duplicateString.contains(forRemoval)) {
				String temp = duplicateString.replace(forRemoval, "");
				temp = temp.replace(":", "");
				temp = temp.trim();

				final StringTokenizer tokenizer = new StringTokenizer(temp, ",");

				while (tokenizer.hasMoreTokens()) {
					final String token = tokenizer.nextToken().trim();

					if (token.length() == 0) {
						continue;
					}
					returnVal.add(Integer.valueOf(token));
				}
				//break, since duplicate string is unique per duplicate type
				break;
			}
		}
		return returnVal;
	}

	@Override
	public String getCheckTypeDescription() {

		return this.checkTypeDescription;
	}

	public void setCheckTypeDescription(final String value) {
		this.checkTypeDescription = value;

	}

	@Override
	public String getStockIDs() {

		return "";
	}

	@Override
	public String getSeedAmount() {

		return "";
	}

	public Integer getGroupId() {
		return this.groupId;
	}

	public void setGroupId(final Integer groupId) {
		this.groupId = groupId;
	}

}
