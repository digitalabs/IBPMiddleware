/*******************************************************************************
 * Copyright (c) 2014, All Rights Reserved.
 *
 * Generation Challenge Programme (GCP)
 *
 *
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *
 *******************************************************************************/

package org.generationcp.middleware.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.generationcp.middleware.dao.LocationDAO;
import org.generationcp.middleware.dao.LocdesDAO;
import org.generationcp.middleware.domain.fieldbook.FieldmapBlockInfo;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.manager.api.LocationDataManager;
import org.generationcp.middleware.pojos.Country;
import org.generationcp.middleware.pojos.Location;
import org.generationcp.middleware.pojos.LocationDetails;
import org.generationcp.middleware.pojos.LocationType;
import org.generationcp.middleware.pojos.Locdes;
import org.generationcp.middleware.pojos.LocdesType;
import org.generationcp.middleware.pojos.UDTableType;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.generationcp.middleware.service.api.location.AdditionalInfoDto;
import org.generationcp.middleware.service.api.location.LocationDetailsDto;
import org.generationcp.middleware.service.api.location.LocationFilters;
import org.springframework.transaction.annotation.Transactional;

/**
 * Implementation of the LocationDataManager interface. To instantiate this
 * class, a Hibernate Session must be passed to its constructor.
 *
 * @author Joyce Avestro
 */
@SuppressWarnings("unchecked")
@Transactional
public class LocationDataManagerImpl extends DataManager implements LocationDataManager {

	public LocationDataManagerImpl() {
	}

	public LocationDataManagerImpl(final HibernateSessionProvider sessionProvider) {
		super(sessionProvider);
	}

	@Override
	public List<Location> getAllLocations() throws MiddlewareQueryException {
		final List<Location> locations = this.getLocationDao().getAll();
		Collections.sort(locations);
		return locations;
	}

	@Override
	public List<Location> getAllLocalLocations(final int start, final int numOfRows) throws MiddlewareQueryException {
		return this.getLocationDao().getAll(start, numOfRows);
	}



	@Override
	public long countAllLocations() throws MiddlewareQueryException {
		return this.countAll(this.getLocationDao());
	}

	@Override
	public List<Location> getLocationsByUniqueID(final String programUUID) throws MiddlewareQueryException {
		final List<Location> locations = new ArrayList<Location>();
		locations.addAll(this.getLocationDao().getByUniqueID(programUUID));
		return locations;
	}

	@Override
	public long countLocationsByUniqueID(final String programUUID) throws MiddlewareQueryException {
		return this.countAllByMethod(this.getLocationDao(), "countByUniqueID", new Object[] { programUUID },
				new Class[] { String.class });
	}

	@Override
	public List<Location> getLocationsByName(final String name, final Operation op, final String programUUID)
			throws MiddlewareQueryException {
		final List<Location> locations = new ArrayList<Location>();
		locations.addAll(this.getLocationDao().getByNameAndUniqueID(name, op, programUUID));
		return locations;
	}

	@Override
	public List<Location> getLocationsByName(final String name, final int start, final int numOfRows,
			final Operation op, final String programUUID) throws MiddlewareQueryException {
		final List<String> methods = Arrays.asList("countByNameAndUniqueID", "getByNameAndUniqueID");
		return this.getFromCentralAndLocalByMethod(this.getLocationDao(), methods, start, numOfRows,
				new Object[] { name, op, programUUID }, new Class[] { String.class, Operation.class, String.class });
	}

	@Override
	public long countLocationsByName(final String name, final Operation op, final String programUUID)
			throws MiddlewareQueryException {
		return this.countAllByMethod(this.getLocationDao(), "countByNameAndUniqueID",
				new Object[] { name, op, programUUID }, new Class[] { String.class, Operation.class, String.class });
	}

	/**
	 * @deprecated
	 */
	@Override
	@Deprecated
	public List<Location> getLocationsByName(final String name, final Operation op) throws MiddlewareQueryException {
		final List<Location> locations = new ArrayList<Location>();
		locations.addAll(this.getLocationDao().getByName(name, op));
		return locations;
	}

	/**
	 * @deprecated
	 */
	@Override
	@Deprecated
	public List<Location> getLocationsByName(final String name, final int start, final int numOfRows,
			final Operation op) throws MiddlewareQueryException {
		final List<String> methods = Arrays.asList("countByName", "getByName");
		return this.getFromCentralAndLocalByMethod(this.getLocationDao(), methods, start, numOfRows,
				new Object[] { name, op }, new Class[] { String.class, Operation.class });
	}

	/**
	 * @deprecated
	 */
	@Override
	@Deprecated
	public long countLocationsByName(final String name, final Operation op) throws MiddlewareQueryException {
		return this.countAllByMethod(this.getLocationDao(), "countByName", new Object[] { name, op },
				new Class[] { String.class, Operation.class });
	}

	@Override
	public List<Location> getLocationsByCountry(final Country country) throws MiddlewareQueryException {
		return super.getAllByMethod(this.getLocationDao(), "getByCountry", new Object[] { country },
				new Class[] { Country.class });
	}

	@Override
	public List<Location> getLocationsByCountry(final Country country, final int start, final int numOfRows)
			throws MiddlewareQueryException {
		final List<String> methods = Arrays.asList("countByCountry", "getByCountry");
		return this.getFromCentralAndLocalByMethod(this.getLocationDao(), methods, start, numOfRows,
				new Object[] { country }, new Class[] { Country.class });
	}

	@Override
	public long countLocationsByCountry(final Country country) throws MiddlewareQueryException {
		return this.countAllByMethod(this.getLocationDao(), "countByCountry", new Object[] { country },
				new Class[] { Country.class });
	}

	@Override
	public List<Location> getLocationsByType(final Integer type) throws MiddlewareQueryException {
		return this.getAllByMethod(this.getLocationDao(), "getByType", new Object[] { type },
				new Class[] { Integer.class });
	}

	@Override
	public List<Location> getLocationsByType(final Integer type, final String programUUID)
			throws MiddlewareQueryException {
		return this.getAllByMethod(this.getLocationDao(), "getByType", new Object[] { type, programUUID },
				new Class[] { Integer.class, String.class });
	}

	@Override
	public List<Location> getLocationsByType(final Integer type, final int start, final int numOfRows)
			throws MiddlewareQueryException {
		final List<String> methods = Arrays.asList("countByType", "getByType");
		return this.getFromCentralAndLocalByMethod(this.getLocationDao(), methods, start, numOfRows,
				new Object[] { type }, new Class[] { Integer.class });
	}

	@Override
	public long countLocationsByType(final Integer type) throws MiddlewareQueryException {
		return this.countAllByMethod(this.getLocationDao(), "countByType", new Object[] { type },
				new Class[] { Integer.class });
	}

	@Override
	public long countLocationsByType(final Integer type, final String programUUID) throws MiddlewareQueryException {
		return this.countAllByMethod(this.getLocationDao(), "countByType", new Object[] { type, programUUID },
				new Class[] { Integer.class, String.class });
	}

	@Override
	public UserDefinedField getUserDefinedFieldByID(final Integer id) throws MiddlewareQueryException {
		return this.getUserDefinedFieldDao().getById(id, false);
	}

	@Override
	public Map<String, UserDefinedField> getUserDefinedFieldMapOfCodeByUDTableType(final UDTableType type)
			throws MiddlewareQueryException {
		final Map<String, UserDefinedField> types = new HashMap<String, UserDefinedField>();

		final List<UserDefinedField> dTypeFields = this.getUserDefinedFieldByFieldTableNameAndType(type.getTable(),
				type.getType());
		for (final UserDefinedField dTypeField : dTypeFields) {
			types.put(dTypeField.getFcode(), dTypeField);
		}
		return types;
	}

	@Override
	public Integer getUserDefinedFieldIdOfCode(final UDTableType tableType, final String code)
			throws MiddlewareQueryException {
		final Map<String, UserDefinedField> types = new HashMap<String, UserDefinedField>();

		final List<UserDefinedField> dTypeFields = this.getUserDefinedFieldByFieldTableNameAndType(tableType.getTable(),
				tableType.getType());
		for (final UserDefinedField dTypeField : dTypeFields) {
			types.put(dTypeField.getFcode(), dTypeField);
		}

		return types.get(code) != null ? types.get(code).getFldno() : null;
	}

	@Override
	public List<UserDefinedField> getUserDefinedFieldByFieldTableNameAndType(final String tableName,
			final String fieldType) throws MiddlewareQueryException {
		return super.getAllByMethod(this.getUserDefinedFieldDao(), "getByFieldTableNameAndType",
				new Object[] { tableName, fieldType }, new Class[] { String.class, String.class });
	}

	@Override
	public Country getCountryById(final Integer id) throws MiddlewareQueryException {
		return this.getCountryDao().getById(id, false);
	}

	@Override
	public Location getLocationByID(final Integer id) throws MiddlewareQueryException {
		return this.getLocationDao().getById(id, false);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List<Location> getLocationsByIDs(final List<Integer> ids) throws MiddlewareQueryException {
		return this.getLocationDao().getLocationByIds(ids);
	}

	@Override
	public Integer addLocation(final Location location) throws MiddlewareQueryException {

		Integer idLocationSaved = null;
		try {

			final LocationDAO dao = this.getLocationDao();

			final Location recordSaved = dao.saveOrUpdate(location);
			idLocationSaved = recordSaved.getLocid();

		} catch (final Exception e) {

			throw new MiddlewareQueryException(
					"Error encountered while saving Location: LocationDataManager.addLocation(location=" + location
							+ "): " + e.getMessage(),
					e);
		}
		return idLocationSaved;
	}

	@Override
	public List<Integer> addLocation(final List<Location> locations) throws MiddlewareQueryException {

		final List<Integer> idLocationsSaved = new ArrayList<Integer>();
		try {

			final LocationDAO dao = this.getLocationDao();

			for (final Location location : locations) {
				final Location recordSaved = dao.saveOrUpdate(location);
				idLocationsSaved.add(recordSaved.getLocid());
			}

		} catch (final Exception e) {

			throw new MiddlewareQueryException(
					"Error encountered while saving Locations: LocationDataManager.addLocation(locations=" + locations
							+ "): " + e.getMessage(),
					e);
		}
		return idLocationsSaved;
	}

	@Override
	public int addLocationAndLocdes(final Location location, final Locdes locdes) throws MiddlewareQueryException {

		Integer idLocationSaved = null;
		try {

			// Auto-assign IDs for new DB records
			final LocationDAO locationDao = this.getLocationDao();
			final Location recordSaved = locationDao.saveOrUpdate(location);
			idLocationSaved = recordSaved.getLocid();

			final LocdesDAO locdesDao = this.getLocdesDao();
			locdes.setLocationId(idLocationSaved);
			locdesDao.saveOrUpdate(locdes);

		} catch (final Exception e) {
			throw new MiddlewareQueryException("Error encountered while saving Location: addLocationAndLocdes("
					+ "location=" + location + ", locdes=" + locdes + "): " + e.getMessage(), e);
		}
		return idLocationSaved;
	}

	@Override
	public void deleteLocation(final Location location) throws MiddlewareQueryException {

		try {

			this.getLocationDao().makeTransient(location);

		} catch (final Exception e) {

			throw new MiddlewareQueryException(
					"Error encountered while deleting Location: LocationDataManager.deleteLocation(location=" + location
							+ "): " + e.getMessage(),
					e);
		}
	}

	@Override
	public List<Country> getAllCountry() throws MiddlewareQueryException {
		return super.getAllByMethod(this.getCountryDao(), "getAllCountry", new Object[] {}, new Class[] {});
	}

	@Override
	public List<Location> getLocationsByCountryAndType(final Country country, final Integer type)
			throws MiddlewareQueryException {
		return super.getAllByMethod(this.getLocationDao(), "getByCountryAndType", new Object[] { country, type },
				new Class[] { Country.class, Integer.class });
	}

	@Override
	public List<Location> getLocationsByNameCountryAndType(final String name, final Country country, final Integer type)
			throws MiddlewareQueryException {
		return super.getAllByMethod(this.getLocationDao(), "getByNameCountryAndType",
				new Object[] { name, country, type }, new Class[] { String.class, Country.class, Integer.class });
	}

	@Override
	public List<LocationDetails> getLocationDetailsByLocId(final Integer locationId, final int start,
			final int numOfRows) throws MiddlewareQueryException {
		return super.getAllByMethod(this.getLocationDao(), "getLocationDetails",
				new Object[] { locationId, start, numOfRows },
				new Class[] { Integer.class, Integer.class, Integer.class });

	}

	@Override
	public List<Location> getAllBreedingLocations() throws MiddlewareQueryException {

		final List<Location> allLocations = this.getFromInstanceByMethod(this.getLocationDAO(), Database.LOCAL,
				"getAllBreedingLocations", new Object[] {}, new Class[] {});

		return allLocations;
	}

	@Override
	public Long countAllBreedingLocations() throws MiddlewareQueryException {
		return this.countAllByMethod(this.getLocationDAO(), "countAllBreedingLocations", new Object[] {},
				new Class[] {});
	}

	@Override
	public List<Location> getAllFieldLocations(final int locationId) throws MiddlewareQueryException {
		final Integer fieldParentFldId = this.getUserDefinedFieldIdOfCode(UDTableType.LOCDES_DTYPE,
				LocdesType.FIELD_PARENT.getCode());
		final Integer fieldLtypeFldId = this.getUserDefinedFieldIdOfCode(UDTableType.LOCATION_LTYPE,
				LocationType.FIELD.getCode());

		return super.getAllByMethod(this.getLocationDao(), "getLocationsByDTypeAndLType",
				new Object[] { String.valueOf(locationId), fieldParentFldId, fieldLtypeFldId },
				new Class[] { String.class, Integer.class, Integer.class });
	}

	@Override
	public List<Location> getAllBlockLocations(final int fieldId) throws MiddlewareQueryException {
		final Integer blockParentFldId = this.getUserDefinedFieldIdOfCode(UDTableType.LOCDES_DTYPE,
				LocdesType.BLOCK_PARENT.getCode());
		final Integer blockLtypeFldId = this.getUserDefinedFieldIdOfCode(UDTableType.LOCATION_LTYPE,
				LocationType.BLOCK.getCode());

		return super.getAllByMethod(this.getLocationDao(), "getLocationsByDTypeAndLType",
				new Object[] { String.valueOf(fieldId), blockParentFldId, blockLtypeFldId },
				new Class[] { String.class, Integer.class, Integer.class });
	}

	@Override
	public FieldmapBlockInfo getBlockInformation(final int blockId) throws MiddlewareQueryException {
		int rowsInBlock = 0;
		int rowsInPlot = 0;
		int rangesInBlock = 0;
		int plantingOrder = 0;
		int machineRowCapacity = 0;
		Integer fieldId = null;
		boolean isNew = true;

		final Map<String, UserDefinedField> dTypes = this
				.getUserDefinedFieldMapOfCodeByUDTableType(UDTableType.LOCDES_DTYPE);

		final List<Locdes> locdesOfLocation = this.getLocdesDao().getByLocation(blockId);
		final List<String> deletedPlots = new ArrayList<String>();

		for (final Locdes locdes : locdesOfLocation) {
			if (locdes != null) {
				final int typeId = locdes.getTypeId();
				final String value = locdes.getDval();
				if (typeId == dTypes.get(LocdesType.ROWS_IN_BLOCK.getCode()).getFldno()) {
					rowsInBlock = this.getNumericValue(value);
				} else if (typeId == dTypes.get(LocdesType.ROWS_IN_PLOT.getCode()).getFldno()) {
					rowsInPlot = this.getNumericValue(value);
				} else if (typeId == dTypes.get(LocdesType.RANGES_IN_BLOCK.getCode()).getFldno()) {
					rangesInBlock = this.getNumericValue(value);
				} else if (typeId == dTypes.get(LocdesType.PLANTING_ORDER.getCode()).getFldno()) {
					plantingOrder = this.getNumericValue(value);
				} else if (typeId == dTypes.get(LocdesType.MACHINE_ROW_CAPACITY.getCode()).getFldno()) {
					machineRowCapacity = this.getNumericValue(value);
				} else if (typeId == dTypes.get(LocdesType.DELETED_PLOTS.getCode()).getFldno()) {
					deletedPlots.add(value);
				} else if (typeId == dTypes.get(LocdesType.BLOCK_PARENT.getCode()).getFldno()) {
					fieldId = this.getNumericValue(value);
				}
			}
		}

		if (rowsInBlock > 0 || rangesInBlock > 0 || rowsInPlot > 0) {
			isNew = false;
		}

		return new FieldmapBlockInfo(blockId, rowsInBlock, rangesInBlock, rowsInPlot, plantingOrder, machineRowCapacity,
				isNew, deletedPlots, fieldId);
	}

	@Override
	public List<Location> getAllFields() throws MiddlewareQueryException {
		final Integer fieldLtype = this.getUserDefinedFieldIdOfCode(UDTableType.LOCATION_LTYPE,
				LocationType.FIELD.getCode());
		final Integer relationshipType = this.getUserDefinedFieldIdOfCode(UDTableType.LOCDES_DTYPE,
				LocdesType.FIELD_PARENT.getCode());
		final List<Location> locations = super.getAllByMethod(this.getLocationDao(), "getByTypeWithParent",
				new Object[] { fieldLtype, relationshipType }, new Class[] { Integer.class, Integer.class });

		final Set<Integer> parentIds = new HashSet<Integer>();
		for (final Location location : locations) {
			parentIds.add(location.getParentLocationId());
		}

		final Map<Integer, Location> namesMap = new HashMap<Integer, Location>();
		if (!parentIds.isEmpty()) {
			namesMap.putAll(this.getLocationDao().getNamesByIdsIntoMap(parentIds));
		}

		for (final Location location : locations) {
			final Location tempParent = namesMap.get(location.getParentLocationId());
			location.setParentLocationName(tempParent.getLname());
			location.setParentLocationAbbr(tempParent.getLabbr());
		}

		return locations;
	}

	@Override
	public List<Location> getAllProvincesByCountry(final Integer countryId) throws MiddlewareQueryException {
		final List<Location> provinces = super.getAllByMethod(this.getLocationDao(), "getAllProvincesByCountry",
				new Object[] { countryId }, new Class[] { Integer.class });

		return provinces;
	}

	@Override
	public List<Location> getAllProvinces() throws MiddlewareQueryException {
		final List<Location> provinces = super.getAllByMethod(this.getLocationDao(), "getAllProvinces", new Object[] {},
				new Class[] {});
		return provinces;
	}

	private int getNumericValue(final String strValue) {
		int value = 0;
		try {
			value = Integer.parseInt(strValue);
		} catch (final NumberFormatException e) {
			value = 0; // if it's nut a number, set to 0
		}
		return value;
	}

	@Override
	public List<Location> getProgramLocations(final String programUUID) throws MiddlewareQueryException {
		return this.getLocationDao().getProgramLocations(programUUID);
	}

	@Override
	public void deleteProgramLocationsByUniqueId(final String programUUID) throws MiddlewareQueryException {

		final LocationDAO locationDao = this.getLocationDao();
		try {
			final List<Location> list = this.getProgramLocations(programUUID);
			for (final Location location : list) {
				locationDao.makeTransient(location);
			}
		} catch (final Exception e) {
			throw new MiddlewareQueryException(
					"Error encountered while deleting locations: GermplasmDataManager.deleteProgramLocationsByUniqueId(uniqueId="
							+ programUUID + "): " + e.getMessage(),
					e);
		}
	}

	@Override
	public List<Locdes> getLocdesByLocId(final Integer locationId) throws MiddlewareQueryException {
		return this.getLocationDao().getLocdesByLocId(locationId);
	}

	@Override
	public void saveOrUpdateLocdesList(final Integer locId, final List<Locdes> locdesList)
			throws MiddlewareQueryException {

		if (locdesList != null && !locdesList.isEmpty()) {
			try {
				final List<Locdes> existingLocdesList = this.getLocDesDao().getByLocation(locId);
				for (final Locdes locdes : locdesList) {
					this.getLocdesSaver().saveOrUpdateLocdes(locdes.getLocationId(), existingLocdesList,
							locdes.getTypeId(), locdes.getDval(), locdes.getUserId());
				}
			} catch (final Exception e) {
				this.logAndThrowException("Error encountered with saveOrUpdateLocdesList(): " + e.getMessage(), e);
			}
		}

	}

	@Override
	public List<Location> getAllSeedingLocations(final String programUUID) {
		final Integer seedLType = this.getUserDefinedFieldIdOfCode(UDTableType.LOCATION_LTYPE,
				LocationType.SSTORE.getCode());
		return this.getLocationDao().getByType(seedLType, programUUID);
	}

	@Override
	public List<Location> getAllBreedingLocations(final List<Integer> locationIds) {
		return this.getLocationDao().getBreedingLocations(locationIds);
	}

	@Override
	public List<Location> getAllSeedingLocations(final List<Integer> locationIds) {
		final Integer seedLType = this.getUserDefinedFieldIdOfCode(UDTableType.LOCATION_LTYPE,
				LocationType.SSTORE.getCode());
		return this.getLocationDao().getSeedingLocations(locationIds, seedLType);
	}

	@Override
	public List<Location> getAllBreedingLocationsByUniqueID(final String programUUID) {
		return this.getLocationDao().getBreedingLocationsByUniqueID(programUUID);
	}
	
	@Override
	public List<LocationDetails> getFilteredLocations(final Integer countryId, final Integer locationType, final String locationName,
			final String programUUID) {
		return this.getLocationDao().getFilteredLocations(countryId, locationType, locationName, programUUID);
	}

	@Override
	public long countLocationsByFilter(final Map<LocationFilters,Object> filters) throws MiddlewareQueryException {
		return this.getLocationDao().countLocationsByFilter(filters);

	}

	@Override
	public List<LocationDetailsDto> getLocationsByFilter(final int start, final int numOfRows, final Map<LocationFilters, Object> filters)
			throws MiddlewareQueryException {
		final List<LocationDetailsDto> locationsDetailsDto = this.getLocationDao().getLocationsByFilter(start, numOfRows, filters);
		final List<String> locations = new ArrayList<String>();

		if (locationsDetailsDto.size() != 0) {
			for (LocationDetailsDto locationDto : locationsDetailsDto) {
				locations.add(locationDto.getLocationDbId().toString());

			}
			getAdditinalInfoLocation(locationsDetailsDto, locations);
		}
		return locationsDetailsDto;
	}
	
	@Override
	public Integer getUserDefinedFieldIdOfName(final UDTableType tableType, final String name)
			throws MiddlewareQueryException {
		final Map<String, UserDefinedField> types = new HashMap<String, UserDefinedField>();

		final List<UserDefinedField> dTypeFields = this.getUserDefinedFieldByFieldTableNameAndType(tableType.getTable(),
				tableType.getType());
		for (final UserDefinedField dTypeField : dTypeFields) {
			types.put(dTypeField.getFname().toUpperCase(), dTypeField);
		}

		return types.get(name.toUpperCase()) != null ? types.get(name.toUpperCase()).getFldno() : null;
	}

	private void getAdditinalInfoLocation(final List<LocationDetailsDto> locationsDetailsDto, final List<String> locations)
			throws MiddlewareQueryException {
		final Map<Integer, AdditionalInfoDto> mapAdditionalInfoDto = getAdditionalInfoFieldMap(locations);
		
		if (!mapAdditionalInfoDto.isEmpty()) {
			for (LocationDetailsDto locationDto : locationsDetailsDto) {

				if (mapAdditionalInfoDto.containsKey(locationDto.getLocationDbId())) {
					locationDto.setMapAdditionalInfo(mapAdditionalInfoDto.get(locationDto.getLocationDbId()));
				}
			}
		}
	}

	private Map<Integer, AdditionalInfoDto> getAdditionalInfoFieldMap(final List<String> listLocation) {
		String[] locations = new String[listLocation.size()];
		locations =listLocation.toArray(locations);
		final List<Locdes> listFieldParent = this.getLocdesDao().getAllLocdesByFilters(LocdesType.FIELD_PARENT.getCode(), locations);
		final Map<String, UserDefinedField> dTypes = this.getUserDefinedFieldMapOfCodeByUDTableType(UDTableType.LOCDES_DTYPE);
		final Map<Integer, AdditionalInfoDto> mapAdditionalInfoDto = new HashMap<Integer, AdditionalInfoDto>();

		if (listFieldParent.isEmpty()) {
			return new HashMap<Integer, AdditionalInfoDto>();
		}

		for (Locdes fieldParent : listFieldParent) {
			final Integer locationId = new Integer(fieldParent.getDval());
			AdditionalInfoDto additionalInfoDto = new AdditionalInfoDto(locationId);

			additionalInfoDto.addInfo(LocdesType.FIELD_PARENT.getCode(), getLocationName(fieldParent.getLocationId()));

			final List<Locdes> listblockParent =
					this.getLocdesDao().getAllLocdesByFilters(LocdesType.BLOCK_PARENT.getCode(), null, fieldParent.getLocationId().toString());

			int countBlockParent = listblockParent.size() == 1 ? 0 : 1;
			String concatNumeric = "";
			
			if(listblockParent.isEmpty()){
				mapAdditionalInfoDto.put(locationId, additionalInfoDto);
			}
			for (final Locdes lBlockParent : listblockParent) {

				if (countBlockParent > 0) {
					concatNumeric = String.valueOf(countBlockParent);
					++countBlockParent;
				}

				additionalInfoDto.addInfo(LocdesType.BLOCK_PARENT.getCode() + concatNumeric, getLocationName(lBlockParent.getLocationId()));
				final List<Locdes> locdesOfLocation = this.getLocdesDao().getByLocation(lBlockParent.getLocationId());
				for (final Locdes locdes : locdesOfLocation) {

					final int typeId = locdes.getTypeId();
					final String value = locdes.getDval();
					if (typeId == dTypes.get(LocdesType.ROWS_IN_BLOCK.getCode()).getFldno()) {
						additionalInfoDto.addInfo(LocdesType.ROWS_IN_BLOCK.getCode() + concatNumeric, value);
					} else if (typeId == dTypes.get(LocdesType.ROWS_IN_PLOT.getCode()).getFldno()) {
						additionalInfoDto.addInfo(LocdesType.ROWS_IN_PLOT.getCode() + concatNumeric, value);
					} else if (typeId == dTypes.get(LocdesType.RANGES_IN_BLOCK.getCode()).getFldno()) {
						additionalInfoDto.addInfo(LocdesType.RANGES_IN_BLOCK.getCode() + concatNumeric, value);
					} else if (typeId == dTypes.get(LocdesType.PLANTING_ORDER.getCode()).getFldno()) {
						additionalInfoDto.addInfo(LocdesType.PLANTING_ORDER.getCode() + concatNumeric, value);
					} else if (typeId == dTypes.get(LocdesType.MACHINE_ROW_CAPACITY.getCode()).getFldno()) {
						additionalInfoDto.addInfo(LocdesType.MACHINE_ROW_CAPACITY.getCode() + concatNumeric, value);
					} else if (typeId == dTypes.get(LocdesType.DELETED_PLOTS.getCode()).getFldno()) {
						additionalInfoDto.addInfo(LocdesType.DELETED_PLOTS.getCode() + concatNumeric, value);
					}

				}
				mapAdditionalInfoDto.put(locationId, additionalInfoDto);
			}
		}
		return mapAdditionalInfoDto;
	}

	private String getLocationName(Integer locationId){
		Location location=this.getLocationByID(locationId);
		return location.getLname();
	}
}
