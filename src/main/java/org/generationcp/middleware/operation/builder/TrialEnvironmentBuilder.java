/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * 
 * Generation Challenge Programme (GCP)
 * 
 * 
 * This software is licensed for use under the terms of the GNU General Public
 * License (http://bit.ly/8Ztv8M) and the provisions of Part F of the Generation
 * Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 * 
 *******************************************************************************/
package org.generationcp.middleware.operation.builder;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.generationcp.middleware.domain.dms.DataSet;
import org.generationcp.middleware.domain.dms.FactorType;
import org.generationcp.middleware.domain.dms.LocationDto;
import org.generationcp.middleware.domain.dms.Study;
import org.generationcp.middleware.domain.dms.TrialEnvironment;
import org.generationcp.middleware.domain.dms.TrialEnvironmentProperty;
import org.generationcp.middleware.domain.dms.TrialEnvironments;
import org.generationcp.middleware.domain.dms.Variable;
import org.generationcp.middleware.domain.dms.VariableList;
import org.generationcp.middleware.domain.dms.VariableType;
import org.generationcp.middleware.domain.dms.VariableTypeList;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.manager.Database;
import org.generationcp.middleware.pojos.dms.Geolocation;
import org.generationcp.middleware.pojos.dms.GeolocationProperty;
import org.generationcp.middleware.pojos.oms.CVTerm;

public class TrialEnvironmentBuilder extends Builder {

	public TrialEnvironmentBuilder(
			HibernateSessionProvider sessionProviderForLocal,
			HibernateSessionProvider sessionProviderForCentral) {
		super(sessionProviderForLocal, sessionProviderForCentral);
	}

	public TrialEnvironments getTrialEnvironmentsInDataset(int datasetId) throws MiddlewareQueryException {
		if (this.setWorkingDatabase(datasetId)) {
		    DataSet dataSet = getDataSetBuilder().build(datasetId);
		    Study study = getStudyBuilder().createStudy(dataSet.getStudyId());
		
		    VariableTypeList trialEnvironmentVariableTypes = getTrialEnvironmentVariableTypes(study, dataSet);
		    Set<Geolocation> locations = getGeoLocations(datasetId);
		
		    return buildTrialEnvironments(locations, trialEnvironmentVariableTypes);
		}
		return new TrialEnvironments();
	}

	private VariableTypeList getTrialEnvironmentVariableTypes(Study study, DataSet dataSet) {
		VariableTypeList trialEnvironmentVariableTypes = new VariableTypeList();
		trialEnvironmentVariableTypes.addAll(study.getVariableTypesByFactorType(FactorType.TRIAL_ENVIRONMENT));
		trialEnvironmentVariableTypes.addAll(dataSet.getFactorsByFactorType(FactorType.TRIAL_ENVIRONMENT));
		return trialEnvironmentVariableTypes;
	}

	private Set<Geolocation> getGeoLocations(int datasetId) throws MiddlewareQueryException {
		return getGeolocationDao().findInDataSet(datasetId);
	}

	private TrialEnvironments buildTrialEnvironments(Set<Geolocation> locations,
			                                         VariableTypeList trialEnvironmentVariableTypes) {
		
		TrialEnvironments trialEnvironments = new TrialEnvironments();
		for (Geolocation location : locations) {
			VariableList variables = new VariableList();
			for (VariableType variableType : trialEnvironmentVariableTypes.getVariableTypes()) {
				Variable variable = new Variable(variableType, getValue(location, variableType));
				variables.add(variable);
			}
			trialEnvironments.add(new TrialEnvironment(location.getLocationId(), variables));
		}
		return trialEnvironments;
	}

	private String getValue(Geolocation location, VariableType variableType) {
		String value = null;
		int storedInId = variableType.getStandardVariable().getStoredIn().getId();
		if (storedInId == TermId.TRIAL_INSTANCE_STORAGE.getId()) {
			value = location.getDescription();
		}
		else if (storedInId == TermId.LATITUDE_STORAGE.getId()) {
			value = location.getLatitude() == null ? null : Double.toString(location.getLatitude());
		}
		else if (storedInId == TermId.LONGITUDE_STORAGE.getId()) {
			value = location.getLongitude() == null ? null : Double.toString(location.getLongitude());
		}
	    else if (storedInId == TermId.DATUM_STORAGE.getId()) {
	    	value = location.getGeodeticDatum();
	    }
		else if (storedInId == TermId.ALTITUDE_STORAGE.getId()) {
			value = location.getAltitude() == null ? null : Double.toString(location.getAltitude());
		}
		else if (storedInId == TermId.TRIAL_ENVIRONMENT_INFO_STORAGE.getId()) {
			value = getPropertyValue(variableType.getId(), location.getProperties());
		}
		return value;
	}

	private String getPropertyValue(int id, List<GeolocationProperty> properties) {
		String value = null;
		if (properties != null) {
		    for (GeolocationProperty property : properties) {
		    	if (property.getTypeId() == id) {
		    		value = property.getValue();
		    		break;
		    	}
		    }
		}
		return value;
	}
	
	public TrialEnvironments getAllTrialEnvironments() throws MiddlewareQueryException {
		TrialEnvironments environments = new TrialEnvironments();
		setWorkingDatabase(Database.CENTRAL);
		environments.addAll(getGeolocationDao().getAllTrialEnvironments());
		
		setWorkingDatabase(Database.LOCAL);
		TrialEnvironments localEnvironments = getGeolocationDao().getAllTrialEnvironments();
		if (localEnvironments != null && localEnvironments.getTrialEnvironments() != null) {
			setWorkingDatabase(Database.CENTRAL);
			Set<Integer> ids = new HashSet<Integer>();
			for (TrialEnvironment environment : localEnvironments.getTrialEnvironments()) {
				if (environment.getLocation() != null && environment.getLocation().getId() != null 
				&& environment.getLocation().getId().intValue() >= 0) {
					ids.add(environment.getLocation().getId());
				}
			}
			List<LocationDto> newLocations = getLocationDao().getLocationDtoByIds(ids);
			for (TrialEnvironment environment : localEnvironments.getTrialEnvironments()) {
				if (environment.getLocation() != null && newLocations != null && newLocations.indexOf(environment.getLocation().getId()) > -1) {
					LocationDto newLocation = newLocations.get(newLocations.indexOf(environment.getLocation().getId()));
					environment.getLocation().setCountryName(newLocation.getCountryName());
					environment.getLocation().setLocationName(newLocation.getLocationName());
					environment.getLocation().setProvinceName(newLocation.getProvinceName());
				}
				environments.add(environment);
			}
		}
		
		return environments;
	}
	
	public List<TrialEnvironmentProperty> getPropertiesForTrialEnvironments(List<Integer> environmentIds) throws MiddlewareQueryException {
		List<TrialEnvironmentProperty> properties = new ArrayList<TrialEnvironmentProperty>();
		setWorkingDatabase(Database.CENTRAL);
		properties.addAll(getGeolocationDao().getPropertiesForTrialEnvironments(environmentIds));

		setWorkingDatabase(Database.LOCAL);
		List<TrialEnvironmentProperty> localProperties = getGeolocationDao().getPropertiesForTrialEnvironments(environmentIds);
		setWorkingDatabase(Database.CENTRAL);
		Set<Integer> ids = new HashSet<Integer>();
		for (TrialEnvironmentProperty property : localProperties) {
			if (property.getId() >= 0) {
				//CVTerm term = getCvTermDao().getById(property.getId());
				//property.setName(term.getName());
				//property.setDescription(term.getDefinition());
				ids.add(property.getId());
			}
		}
		System.out.println("IDS ARE " + ids);
		List<CVTerm> terms = getCvTermDao().getByIds(ids);
		for (TrialEnvironmentProperty property : localProperties) {
			int index = properties.indexOf(property);
			if (index > -1) {
				properties.get(index).setNumberOfEnvironments(
										properties.get(index).getNumberOfEnvironments().intValue() +
										property.getNumberOfEnvironments().intValue());
			} else {
				CVTerm term = null;
				for (CVTerm aTerm : terms) {
					if (aTerm.getCvTermId().equals(property.getId())) {
						term = aTerm;
						break;
					}
				}
				if (term != null) {
					property.setName(term.getName());
					property.setDescription(term.getDefinition());
				}
				properties.add(property);
			}
		}

		return properties;
	}
}
