package org.generationcp.middleware.service.api.study.germplasm.source;

import org.generationcp.middleware.pojos.SortedPageRequest;
import org.pojomatic.Pojomatic;
import org.pojomatic.annotations.AutoProperty;

@AutoProperty
public class StudyGermplasmSourceRequest {

	private int studyId;
	private SortedPageRequest sortedRequest;
	private StudyGermplasmSourceRequest.Filter filter;

	public int getStudyId() {
		return this.studyId;
	}

	public void setStudyId(final int studyId) {
		this.studyId = studyId;
	}

	public StudyGermplasmSourceRequest.Filter getFilter() {
		return this.filter;
	}

	public void setFilter(final StudyGermplasmSourceRequest.Filter filter) {
		this.filter = filter;
	}

	public SortedPageRequest getSortedRequest() {
		return this.sortedRequest;
	}

	public void setSortedRequest(final SortedPageRequest sortedRequest) {
		this.sortedRequest = sortedRequest;
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

	public static class Filter {

		private Integer sourceId;
		private Integer gid;
		private Integer groupId;
		private String designation;
		private String cross;
		private Integer lots;
		private String breedingMethodAbbreviation;
		private String breedingMethodName;
		private String breedingMethodType;
		private String location;
		private String trialInstance;
		private Integer plotNumber;
		private Integer replicationNumber;
		private Integer germplasmDate;

		public Integer getSourceId() {
			return this.sourceId;
		}

		public void setSourceId(final Integer sourceId) {
			this.sourceId = sourceId;
		}

		public Integer getGid() {
			return this.gid;
		}

		public void setGid(final Integer gid) {
			this.gid = gid;
		}

		public Integer getGroupId() {
			return this.groupId;
		}

		public void setGroupId(final Integer groupId) {
			this.groupId = groupId;
		}

		public String getDesignation() {
			return this.designation;
		}

		public void setDesignation(final String designation) {
			this.designation = designation;
		}

		public String getCross() {
			return this.cross;
		}

		public void setCross(final String cross) {
			this.cross = cross;
		}

		public Integer getLots() {
			return this.lots;
		}

		public void setLots(final Integer lots) {
			this.lots = lots;
		}

		public String getBreedingMethodAbbreviation() {
			return this.breedingMethodAbbreviation;
		}

		public void setBreedingMethodAbbreviation(final String breedingMethodAbbreviation) {
			this.breedingMethodAbbreviation = breedingMethodAbbreviation;
		}

		public String getBreedingMethodName() {
			return this.breedingMethodName;
		}

		public void setBreedingMethodName(final String breedingMethodName) {
			this.breedingMethodName = breedingMethodName;
		}

		public String getBreedingMethodType() {
			return this.breedingMethodType;
		}

		public void setBreedingMethodType(final String breedingMethodType) {
			this.breedingMethodType = breedingMethodType;
		}

		public String getLocation() {
			return this.location;
		}

		public void setLocation(final String location) {
			this.location = location;
		}

		public String getTrialInstance() {
			return this.trialInstance;
		}

		public void setTrialInstance(final String trialInstance) {
			this.trialInstance = trialInstance;
		}

		public Integer getPlotNumber() {
			return this.plotNumber;
		}

		public void setPlotNumber(final Integer plotNumber) {
			this.plotNumber = plotNumber;
		}

		public Integer getReplicationNumber() {
			return this.replicationNumber;
		}

		public void setReplicationNumber(final Integer replicationNumber) {
			this.replicationNumber = replicationNumber;
		}

		public Integer getGermplasmDate() {
			return this.germplasmDate;
		}

		public void setGermplasmDate(final Integer germplasmDate) {
			this.germplasmDate = germplasmDate;
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

}
