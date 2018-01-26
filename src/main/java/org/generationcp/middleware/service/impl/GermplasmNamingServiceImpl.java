
package org.generationcp.middleware.service.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.dao.GermplasmDAO;
import org.generationcp.middleware.dao.NameDAO;
import org.generationcp.middleware.exceptions.InvalidGermplasmNameSettingException;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.pojos.germplasm.GermplasmNameSetting;
import org.generationcp.middleware.service.api.GermplasmGroupNamingResult;
import org.generationcp.middleware.service.api.GermplasmNamingService;
import org.generationcp.middleware.service.api.KeySequenceRegisterService;
import org.generationcp.middleware.util.Util;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

public class GermplasmNamingServiceImpl implements GermplasmNamingService {

	private GermplasmDAO germplasmDAO;
	private NameDAO nameDAO;

	private KeySequenceRegisterService keySequenceRegisterService;

	public GermplasmNamingServiceImpl() {

	}

	public GermplasmNamingServiceImpl(final HibernateSessionProvider sessionProvider) {
		this.germplasmDAO = new GermplasmDAO();
		this.germplasmDAO.setSession(sessionProvider.getSession());

		this.nameDAO = new NameDAO();
		this.nameDAO.setSession(sessionProvider.getSession());

		this.keySequenceRegisterService = new KeySequenceRegisterServiceImpl(sessionProvider);
	}

	public GermplasmNamingServiceImpl(GermplasmDAO germplasmDAO, NameDAO nameDAO, KeySequenceRegisterService keySequenceRegisterService) {
		this.germplasmDAO = germplasmDAO;
		this.nameDAO = nameDAO;
		this.keySequenceRegisterService = keySequenceRegisterService;
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public GermplasmGroupNamingResult applyGroupName(final Integer gid, final GermplasmNameSetting setting, final UserDefinedField nameType,
			final Integer userId, final Integer locationId) {

		GermplasmGroupNamingResult result = new GermplasmGroupNamingResult();

		final Germplasm germplasm = this.germplasmDAO.getById(gid);

		if (germplasm.getMgid() == null || germplasm.getMgid() == 0) {
			result.addMessage(
					String.format("Germplasm (gid: %s) is not part of a management group. Can not assign group name.", germplasm.getGid()));
			return result;
		}

		final List<Germplasm> groupMembers = this.germplasmDAO.getManagementGroupMembers(germplasm.getMgid());
		final String nameWithSequence = this.generateNextNameAndIncrementSequence(setting);

		// TODO performance tuning when processing large number of group members
		for (final Germplasm member : groupMembers) {
			this.addName(member, nameWithSequence, nameType, userId, locationId, result);
		}

		return result;
	}
	
	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public Map<Integer, GermplasmGroupNamingResult> applyGroupNames(final Set<Integer> gids, final GermplasmNameSetting setting, final UserDefinedField nameType,
			final Integer userId, final Integer locationId) {
		final Map<Integer, GermplasmGroupNamingResult> assignCodesResultsMap = new LinkedHashMap<>();
		final boolean startNumberSpecified = setting.getStartNumber() != null;
		Integer startNumber = setting.getStartNumber();
		for (final Integer gid : gids) { 
			// Increment start number of succeeding germplasm processed based on initial start # specified, if any
			if (startNumberSpecified) {
				setting.setStartNumber(startNumber++);
			}
			final GermplasmGroupNamingResult result = this.applyGroupName(gid, setting, nameType, userId, locationId);
			assignCodesResultsMap.put(gid, result);
		}
		return assignCodesResultsMap;
	}

	int getNextNumberInSequence(final GermplasmNameSetting setting) {
		
		final String lastPrefixUsed = this.buildPrefixString(setting).toUpperCase();

		if (!lastPrefixUsed.isEmpty()) {
			final String suffix = this.buildSuffixString(setting);
			return this.keySequenceRegisterService.getNextSequence(lastPrefixUsed, suffix);
		}

		return 1;
	}
	
	String buildPrefixString(final GermplasmNameSetting setting) {
		final String prefix = !StringUtils.isEmpty(setting.getPrefix()) ? setting.getPrefix().trim() : "";
		if (setting.isAddSpaceBetweenPrefixAndCode()) {
			return prefix + " ";
		}
		return prefix;
	}

	String buildSuffixString(final GermplasmNameSetting setting) {
		final String suffix = setting.getSuffix();
		if (suffix != null) {
			if (setting.isAddSpaceBetweenSuffixAndCode()) {
				return " " + suffix.trim();
			}
			return suffix.trim();
		}
		return "";
	}
	
	String getNumberWithLeadingZeroesAsString(final Integer number, final GermplasmNameSetting setting) {
		final StringBuilder sb = new StringBuilder();
		final String numberString = number.toString();
		final Integer numOfDigits = setting.getNumOfDigits();

		if (numOfDigits != null && numOfDigits > 0) {
			final int numOfZerosNeeded = numOfDigits - numberString.length();
			if (numOfZerosNeeded > 0) {
				for (int i = 0; i < numOfZerosNeeded; i++) {
					sb.append("0");
				}
			}

		}
		sb.append(number);
		return sb.toString();
	}


	private void addName(final Germplasm germplasm, final String groupName, final UserDefinedField nameType, final Integer userId,
			final Integer locationId, final GermplasmGroupNamingResult result) {

		final List<Name> currentNames = germplasm.getNames();

		Name existingNameOfGivenType = null;
		if (!currentNames.isEmpty() && nameType != null) {
			for (final Name name : currentNames) {
				if (nameType.getFldno().equals(name.getTypeId())) {
					existingNameOfGivenType = name;
					break;
				}
			}
		}

		if (existingNameOfGivenType == null) {
			// Make the current preferred name as non-preferred by setting nstat = 0
			final Name currentPreferredName = germplasm.findPreferredName();
			if (currentPreferredName != null) {
				currentPreferredName.setNstat(0); // nstat = 0 means it is not a preferred name.
			}

			final Name name = new Name();
			name.setGermplasmId(germplasm.getGid());
			name.setTypeId(nameType.getFldno());
			name.setNval(groupName);
			name.setNstat(1); // nstat = 1 means it is preferred name.
			name.setUserId(userId);
			name.setLocationId(locationId);
			name.setNdate(Util.getCurrentDateAsIntegerValue());
			name.setReferenceId(0);

			germplasm.getNames().add(name);
			this.germplasmDAO.save(germplasm);
			result.addMessage(String.format("Germplasm (gid: %s) successfully assigned name %s of type %s as a preferred name.",
					germplasm.getGid(), groupName, nameType.getFcode()));
		} else {
			result.addMessage(String.format("Germplasm (gid: %s) already has existing name %s of type %s. Supplied name %s was not added.",
					germplasm.getGid(), existingNameOfGivenType.getNval(), nameType.getFcode(), groupName));
		}
	}

	@Override
	public String getNextNameInSequence(final GermplasmNameSetting setting) throws InvalidGermplasmNameSettingException {
		Integer nextNumberInSequence = this.getNextNumberInSequence(setting);

		final Integer optionalStartNumber = setting.getStartNumber();

		if (optionalStartNumber != null && optionalStartNumber > 0 && nextNumberInSequence > optionalStartNumber) {
			final String nextName = this.buildDesignationNameInSequence(nextNumberInSequence, setting);
			final String invalidStatingNumberErrorMessage =
					"Starting sequence number should be higher than or equal to next name in the sequence: "
							+ nextName + ".";
			throw new InvalidGermplasmNameSettingException(invalidStatingNumberErrorMessage);
		}

		if (optionalStartNumber != null && nextNumberInSequence < optionalStartNumber) {
			nextNumberInSequence = optionalStartNumber;
		}

		return this.buildDesignationNameInSequence(nextNumberInSequence, setting);
	}
	
	private String generateNextNameAndIncrementSequence(final GermplasmNameSetting setting) {
		Integer nextNumberInSequence = this.getNextNumberInSequence(setting);
		final Integer optionalStartNumber = setting.getStartNumber();
		if (optionalStartNumber != null && nextNumberInSequence < optionalStartNumber) {
			nextNumberInSequence = optionalStartNumber;
		}
		this.keySequenceRegisterService.saveLastSequenceUsed(this.buildPrefixString(setting).toUpperCase(),
				this.buildSuffixString(setting).toUpperCase(), nextNumberInSequence);

		return this.buildDesignationNameInSequence(nextNumberInSequence, setting);
	}

	String buildDesignationNameInSequence(final Integer number, final GermplasmNameSetting setting) {
		final StringBuilder sb = new StringBuilder();
		sb.append(this.buildPrefixString(setting));
		sb.append(this.getNumberWithLeadingZeroesAsString(number, setting));

		if (!StringUtils.isEmpty(setting.getSuffix())) {
			sb.append(this.buildSuffixString(setting));
		}
		return sb.toString();
	}
}
