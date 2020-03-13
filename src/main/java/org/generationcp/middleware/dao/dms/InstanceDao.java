package org.generationcp.middleware.dao.dms;

import org.apache.commons.lang3.math.NumberUtils;
import org.generationcp.middleware.dao.GenericDAO;
import org.generationcp.middleware.domain.dms.LocationDto;
import org.generationcp.middleware.domain.dms.StudyReference;
import org.generationcp.middleware.domain.dms.TrialEnvironment;
import org.generationcp.middleware.domain.dms.TrialEnvironmentProperty;
import org.generationcp.middleware.domain.dms.TrialEnvironments;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.enumeration.DatasetTypeEnum;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.dms.ExperimentModel;
import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Property;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.hibernate.type.IntegerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class InstanceDao extends GenericDAO<ExperimentModel, Integer> {
	private static final Logger LOG = LoggerFactory.getLogger(InstanceDao.class);
	private static final String ENVT_ID = "envtId";
	private static final String LOCATION_ID = "locationId";
	private static final String PROJECT_ID = "project_id";
	private static final String ISOABBR = "isoabbr";
	private static final String PROVINCE_NAME = "provinceName";
	private static final String LOCATION_NAME = "locationName";
	private static final String DESCRIPTION = "description";


	private static final String GET_ALL_ENVIRONMENTS_QUERY =
		"SELECT DISTINCT e.nd_experiment_id as envtId, l.lname AS locationName, prov.lname AS provinceName, "
			+ "       c.isoabbr, p.project_id, p.name, xp.value AS locationId, p.description AS description "
			+ "  FROM nd_experimentprop xp "
			+ " INNER JOIN nd_experiment e on e.nd_experiment_id = xp.nd_experiment_id AND e.type_id = 1020 "
			+ " INNER JOIN project ds ON ds.project_id = e.project_id "
			+ " INNER JOIN project p ON p.project_id = ds.study_id "
			+ "  LEFT JOIN location l ON l.locid = xp.value " + "  LEFT JOIN location prov ON prov.locid = l.snl1id "
			+ "  LEFT JOIN cntry c ON c.cntryid = l.cntryid " + " WHERE xp.type_id = " + TermId.LOCATION_ID.getId();



	public List<ExperimentModel> getEnvironmentsByDataset(final Integer datasetId, final boolean isEnvironmentDataset) {
		final Criteria criteria = this.getSession().createCriteria(this.getPersistentClass(), "environment");

		if (isEnvironmentDataset) {
			criteria.add(Restrictions.eq("project.projectId", datasetId));
		} else {
			final DetachedCriteria childExperimentsCriteria = DetachedCriteria.forClass(ExperimentModel.class, "childExperiment");
			childExperimentsCriteria.add(Restrictions.eq("childExperiment.project.projectId", datasetId));
			childExperimentsCriteria.add(Property.forName("environment.ndExperimentId").eqProperty("childExperiment.parent.ndExperimentId"));
			criteria.add(Subqueries.exists(childExperimentsCriteria.setProjection(Projections.property("childExperiment.ndExperimentId"))));
		}
		return criteria.list();
	}

	public List<Integer> getEnvironmentIds(final Integer datasetId) {
		final Criteria criteria = this.getSession().createCriteria(this.getPersistentClass());
		criteria.add(Restrictions.eq("project.projectId", datasetId));
		criteria.setProjection(Projections.distinct(Projections.property("ndExperimentId")));
		return criteria.list();
	}


	@SuppressWarnings("unchecked")
	public List<TrialEnvironment> getAllTrialEnvironments() {
		final List<TrialEnvironment> environments = new ArrayList<>();
		try {
			final SQLQuery query = this.getSession().createSQLQuery(InstanceDao.GET_ALL_ENVIRONMENTS_QUERY);
			query.addScalar(InstanceDao.ENVT_ID);
			query.addScalar(InstanceDao.LOCATION_NAME);
			query.addScalar(InstanceDao.PROVINCE_NAME);
			query.addScalar(InstanceDao.ISOABBR);
			query.addScalar(InstanceDao.PROJECT_ID);
			query.addScalar("name");
			query.addScalar(InstanceDao.LOCATION_ID);
			query.addScalar(InstanceDao.DESCRIPTION);
			final List<Object[]> list = query.list();
			for (final Object[] row : list) {
				// otherwise it's invalid data and should not be included
				if (NumberUtils.isNumber((String) row[6])) {
					environments.add(new TrialEnvironment(
						(Integer) row[0],
						new LocationDto(Integer.valueOf(row[6].toString()), (String) row[1], (String) row[2],
							(String) row[3]),
						new StudyReference((Integer) row[4], (String) row[5], (String) row[7])));
				}
			}

		} catch (final HibernateException e) {
			final String errorMessage = "Error at getAllTrialEnvironments at InstanceDao: " + e.getMessage();
			InstanceDao.LOG.error(errorMessage, e);
			throw new MiddlewareQueryException(errorMessage, e);
		}
		return environments;
	}

	public long countAllTrialEnvironments() {
		try {
			final String sql = "SELECT COUNT(DISTINCT nd_experiment_id) " + " FROM nd_experimentprop WHERE type_id = "
				+ TermId.LOCATION_ID.getId();
			final Query query = this.getSession().createSQLQuery(sql);
			return ((BigInteger) query.uniqueResult()).longValue();

		} catch (final HibernateException e) {
			final String errorMessage = "Error at countAllTrialEnvironments at InstanceDao: " + e.getMessage();
			InstanceDao.LOG.error(errorMessage, e);
			throw new MiddlewareQueryException(errorMessage, e);
		}
	}

	@SuppressWarnings("unchecked")
	public List<TrialEnvironmentProperty> getPropertiesForTrialEnvironments(final List<Integer> environmentIds) {
		final List<TrialEnvironmentProperty> properties = new ArrayList<>();
		try {
			// if categorical value, get related cvterm.definition as property
			// value.
			// Else, get the value as it's stored in nd_experimentprop
			final String sql = "SELECT DISTINCT xp.type_id, cvt.name, cvt.definition, nd_experiment_id, "
				+ "CASE WHEN (v.name IS NOT NULL AND cvr.cvterm_relationship_id IS NOT NULL) THEN v.definition "
				+ " ELSE xp.value END AS propvalue " + " FROM nd_experimentprop xp"
				+ " LEFT JOIN cvterm cvt ON xp.type_id = cvt.cvterm_id"
				+ " LEFT JOIN cvterm v ON v.cvterm_id = xp.value"
				+ " LEFT JOIN cvterm_relationship cvr ON cvr.subject_id = xp.type_id AND cvr.type_id = " + TermId.HAS_SCALE.getId()
				+ " WHERE nd_experiment_id IN (:environmentIds)" + " ORDER BY xp.type_id, nd_experiment_id";
			final Query query = this.getSession().createSQLQuery(sql);
			query.setParameterList("environmentIds", environmentIds);

			int lastId = 0;
			String lastName = "";
			String lastDescription = "";
			Map<Integer, String> environmentValuesMap = new HashMap<>();

			final List<Object[]> result = query.list();
			for (final Object[] row : result) {
				final Integer id = (Integer) row[0];

				if (lastId != id.intValue()) {
					final String name = (String) row[1];
					final String description = (String) row[2];

					if (lastId != 0) {
						properties.add(
							new TrialEnvironmentProperty(lastId, lastName, lastDescription, environmentValuesMap));
					}

					lastId = id;
					lastName = name;
					lastDescription = description;
					environmentValuesMap = new HashMap<>();
				}

				environmentValuesMap.put((Integer) row[3], (String) row[4]);
			}

			if (lastId != 0) {
				properties.add(new TrialEnvironmentProperty(lastId, lastName, lastDescription, environmentValuesMap));
			}

		} catch (final HibernateException e) {
			final String errorMessage = "Error at getPropertiesForTrialEnvironments=" + environmentIds
				+ " at InstanceDao: " + e.getMessage();
			InstanceDao.LOG.error(errorMessage, e);
			throw new MiddlewareQueryException(errorMessage, e);
		}
		return properties;
	}


	@SuppressWarnings("unchecked")
	public List<TrialEnvironment> getTrialEnvironmentDetails(final Set<Integer> environmentIds) {
		final List<TrialEnvironment> environmentDetails = new ArrayList<>();

		if (environmentIds.isEmpty()) {
			return environmentDetails;
		}

		try {

			// Get location name, study id and study name
			final String sql =
				"SELECT DISTINCT e.nd_experiment_id as environmentId, l.lname, xp.value, p.project_id, p.name, p.description, prov.lname as provinceName, c.isoabbr "
					+ "FROM nd_experiment e "
					+ "	LEFT JOIN nd_experimentprop xp ON e.nd_experiment_id = xp.nd_experiment_id"
					+ "	AND xp.type_id =  " + TermId.LOCATION_ID.getId()
					+ "	LEFT JOIN location l ON l.locid = xp.value "
					+ " LEFT JOIN location prov ON prov.locid = l.snl1id "
					+ "	LEFT JOIN cntry c ON l.cntryid = c.cntryid "
					+ " INNER JOIN project ds ON ds.project_id = e.project_id "
					+ "	INNER JOIN project p ON p.project_id = ds.study_id "
					+ " WHERE e.nd_experiment_id IN (:locationIds) ";

			final SQLQuery query = this.getSession().createSQLQuery(sql);
			query.setParameterList("locationIds", environmentIds);
			query.addScalar("environmentId", Hibernate.INTEGER);
			query.addScalar("lname", Hibernate.STRING);
			query.addScalar("value", Hibernate.INTEGER);
			query.addScalar(InstanceDao.PROJECT_ID, Hibernate.INTEGER);
			query.addScalar("name", Hibernate.STRING);
			query.addScalar(InstanceDao.DESCRIPTION, Hibernate.STRING);
			query.addScalar(InstanceDao.PROVINCE_NAME, Hibernate.STRING);
			query.addScalar(InstanceDao.ISOABBR, Hibernate.STRING);
			final List<Integer> locIds = new ArrayList<>();

			final List<Object[]> result = query.list();

			for (final Object[] row : result) {
				final Integer environmentId = (Integer) row[0];
				final String locationName = (String) row[1];
				final Integer locId = (Integer) row[2];
				final Integer studyId = (Integer) row[3];
				final String studyName = (String) row[4];
				final String studyDescription = (String) row[5];
				final String provinceName = (String) row[6];
				final String countryName = (String) row[7];

				environmentDetails.add(new TrialEnvironment(
					environmentId,
					new LocationDto(locId, locationName, provinceName, countryName),
					new StudyReference(studyId, studyName, studyDescription)));
				locIds.add(locId);
			}

		} catch (final HibernateException e) {
			final String errorMessage = "Error at getTrialEnvironmentDetails=" + environmentIds
				+ " at InstanceDao: " + e.getMessage();
			InstanceDao.LOG.error(errorMessage, e);
			throw new MiddlewareQueryException(errorMessage, e);
		}

		return environmentDetails;
	}

	@SuppressWarnings("unchecked")
	public TrialEnvironments getEnvironmentsForTraits(final List<Integer> traitIds, final String programUUID) {
		final TrialEnvironments environments = new TrialEnvironments();
		try {
			final String sql =
				"SELECT DISTINCT xp.nd_experiment_id as envtId, l.lname as locationName, prov.lname as provinceName, c.isoabbr, p.project_id, p.name, xp.value as locationId"
					+ " FROM nd_experiment e "
					+ " INNER JOIN project ds ON ds.project_id = e.project_id "
					+ " INNER JOIN project p ON p.project_id = ds.study_id "
					+ " INNER JOIN phenotype ph ON ph.nd_experiment_id = e.nd_experiment_id"
					+ " INNER JOIN nd_experimentprop xp ON xp.nd_experiment_id = e.nd_experiment_id AND xp.type_id = "
					+ TermId.LOCATION_ID.getId()
					+ " LEFT JOIN location l ON l.locid = xp.value"
					+ " LEFT JOIN location prov ON prov.locid = l.snl1id"
					+ " LEFT JOIN cntry c ON c.cntryid = l.cntryid"
					+ " WHERE ph.observable_id IN (:traitIds) AND p.program_uuid = :programUUID ;";
			final SQLQuery query = this.getSession().createSQLQuery(sql);
			query.addScalar(InstanceDao.ENVT_ID);
			query.addScalar(InstanceDao.LOCATION_NAME);
			query.addScalar(InstanceDao.PROVINCE_NAME);
			query.addScalar(InstanceDao.ISOABBR);
			query.addScalar(InstanceDao.PROJECT_ID);
			query.addScalar("name");
			query.addScalar(InstanceDao.LOCATION_ID);
			query.setParameterList("traitIds", traitIds);
			query.setParameter("programUUID", programUUID);
			final List<Object[]> list = query.list();
			for (final Object[] row : list) {
				// otherwise it's invalid data and should not be included
				if (NumberUtils.isNumber((String) row[6])) {
					environments.add(new TrialEnvironment(
						(Integer) row[0], new LocationDto(Integer.valueOf(row[6].toString()), (String) row[1],
						(String) row[2], (String) row[3]),
						new StudyReference((Integer) row[4], (String) row[5])));
				}
			}

		} catch (final HibernateException e) {
			final String errorMessage = "Error at getEnvironmentForTraits at InstanceDao: " + e.getMessage();
			InstanceDao.LOG.error(errorMessage, e);
			throw new MiddlewareQueryException(errorMessage, e);
		}
		return environments;
	}

	public Integer getNextInstanceNumber(final Integer datasetId) {
		final StringBuilder sb = new StringBuilder();
		sb.append("SELECT max(observation_unit_no) ");
		sb.append(" FROM nd_experiment e ");
		sb.append(" WHERE e.project_id = :datasetId and e.type_id = 1020" );
		final SQLQuery query = this.getSession().createSQLQuery(sb.toString());
		query.setParameter("datasetId", datasetId);
		final Integer maxInstanceNumber = (Integer) query.uniqueResult();
		if (maxInstanceNumber != null) {
			return maxInstanceNumber.intValue() + 1;
		}
		return 1;
	}

	public Boolean instancesExist(final Set<Integer> instanceIds) {
		for (final Integer instanceId : instanceIds) {
			if (this.getById(instanceId) == null) {
				return Boolean.FALSE;
			}
		}
		return Boolean.TRUE;
	}

	public List<InstanceMetadata> getInstanceMetadata(final int studyId, final List<Integer> locationIds) {

		final String queryString = "select \n" + "    nde.nd_experiment_id as instanceDBId, \n"
			+ "    nde.observation_unit_no as instanceNumber, \n" + "    pmain.project_id trialDbId, \n"
			+ "    pmain.name as trialName, \n" + "    proj.name as instanceDatasetName, \n"
			+ "    pmain.program_uuid as programDbId, \n"
			+ "    max(if(xprop.type_id = 8190, loc.lname, null)) as LOCATION_NAME, \n"
			+ "    max(if(xprop.type_id = 8190, xprop.value, null)) as LOCATION_ID, \n"
			+ "    max(if(xprop.type_id = 8189, xprop.value, null)) as LOCATION_ABBR, \n"
			+ "    max(if(xprop.type_id = 8370, xprop.value, null)) as CROP_SEASON \n"
			+ "    from nd_experiment nde \n"
			+ "    inner join project proj on proj.project_id = nde.project_id \n"
			+ "    inner join project pmain on pmain.project_id = proj.study_id \n"
			+ "    left join nd_experimentprop xprop on xprop.nd_experiment_id = nde.nd_experiment_id AND xprop.type_id = 8190\n"
			+ "	   left join location loc on xprop.value = loc.locid  \n"
			+ " where nde.type_id = 1020 and pmain.project_id = :studyId \n";

		final StringBuilder strBuilder = new StringBuilder(queryString);
		final boolean locationFilterSpecified = !CollectionUtils.isEmpty(locationIds);
		if (locationFilterSpecified) {
			strBuilder.append("    and xprop.value in (:locationIds) ");
		}
		strBuilder.append("    group by nde.nd_experiment_id ");
		strBuilder.append("    order by nde.nd_experiment_id asc \n");
		final SQLQuery query = this.getSession().createSQLQuery(strBuilder.toString());

		query.setParameter("studyId", studyId);
		if (locationFilterSpecified) {
			query.setParameterList("locationIds", locationIds);
		}
		query.addScalar("instanceDBId", new IntegerType());
		query.addScalar("instanceNumber");
		query.addScalar("trialDbId", new IntegerType());
		query.addScalar("trialName");
		query.addScalar("instanceDatasetName");
		query.addScalar("programDbId");
		query.addScalar("LOCATION_NAME");
		query.addScalar("LOCATION_ID", new IntegerType());
		query.addScalar("LOCATION_ABBR");
		query.addScalar("CROP_SEASON");

		@SuppressWarnings("rawtypes") final List results = query.list();

		final List<InstanceMetadata> tiMetadata = new ArrayList<>();
		for (final Object result : results) {
			final Object[] row = (Object[]) result;

			final InstanceMetadata metadata = new InstanceMetadata();
			metadata.setInstanceDbId((Integer) row[0]);
			metadata.setInstanceNumber(String.valueOf(row[1]));
			metadata.setTrialDbId((Integer) row[2]);
			metadata.setTrialName(String.valueOf(row[3]));
			metadata.setInstanceDatasetName(String.valueOf(row[4]));
			metadata.setProgramDbId(String.valueOf(row[5]));
			metadata.setLocationName(String.valueOf(row[6]));
			metadata.setLocationDbId((Integer) row[7]);
			metadata.setLocationAbbreviation(String.valueOf(row[8]));
			metadata.setSeason(String.valueOf(row[9]));
			tiMetadata.add(metadata);
		}
		return tiMetadata;
	}

	public List<ExperimentModel> getEnvironmentsForInstances(final Integer studyId, final List<Integer> instanceNumbers) {
		List<ExperimentModel> returnList = new ArrayList<>();
		if (studyId != null) {
			final String sql = "SELECT DISTINCT exp.* " + //
				" FROM nd_experiment exp " + //
				" INNER JOIN project envdataset on (envdataset.project_id = exp.project_id) " + //
				" WHERE envdataset.study_id = :studyId and envdataset.dataset_type_id = " + DatasetTypeEnum.SUMMARY_DATA.getId();
			final StringBuilder sb = new StringBuilder(sql);
			if (!CollectionUtils.isEmpty(instanceNumbers)) {
				sb.append(" AND exp.observation_unit_no IN (:instanceNumbers)");
			}
			final SQLQuery query = this.getSession().createSQLQuery(sb.toString());
			query.addEntity("exp", ExperimentModel.class);
			query.setParameter("studyId", studyId);
			if (!CollectionUtils.isEmpty(instanceNumbers)) {
				query.setParameterList("instanceNumbers", instanceNumbers);
			}
			returnList = query.list();

		}
		return returnList;
	}

	public List<ExperimentModel> getEnvironments(final Integer studyId) {
		return this.getEnvironmentsForInstances(studyId, Collections.<Integer>emptyList());
	}


	@SuppressWarnings("unchecked")
	public Integer getEnvironmentIdByStudyNameAndInstanceNumberAndProgramUUID(
		final String projectName,
		final Integer instanceNumber, final String programUUID) {
		try {
			final String sql = "SELECT DISTINCT e.nd_experiment_id"
				+ " FROM nd_experiment e "
				+ " INNER JOIN project p ON e.project_id = p.project_id "
				+ " INNER JOIN project st ON st.project_id = p.study_id "
				+ " WHERE st.name = :projectName"
				+ "   and st.program_uuid = :programUUID" + "   and e.observation_unit_no = :instanceNumber";
			final Query query = this.getSession().createSQLQuery(sql);
			query.setParameter("projectName", projectName);
			query.setParameter("instanceNumber", instanceNumber);
			query.setParameter("programUUID", programUUID);
			final List<Integer> list = query.list();
			if (list != null && !list.isEmpty()) {
				return list.get(0);
			}

		} catch (final HibernateException e) {
			final String errorMessage = "Error at getEnvironmentIdByStudyNameAndInstanceNumberAndProgramUUID with project name ="
				+ projectName + " and instance number = " + instanceNumber + e.getMessage();
			InstanceDao.LOG.error(errorMessage, e);
			throw new MiddlewareQueryException(errorMessage, e);
		}
		return null;
	}


	public Map<Integer, ExperimentModel> getExperimentIdEnvironmentMap(final Integer datasetId) {
		final StringBuilder sb = new StringBuilder();
		sb.append("select e.nd_experiment_id, env.nd_experiment_id as environmentId, env.observation_unit_no ");
		sb.append("from nd_experiment e ");
		sb.append("inner join project pr ON pr.project_id = e.project_id ");
		sb.append("inner join project env_ds ON pr.study_id = env_ds.study_id and env_ds.dataset_type_id = 3 ");
		sb.append("inner join nd_experiment env ON env.project_id = env_ds.project_id and env.type_id = 1020 ");
		sb.append("inner join nd_experiment plot ON (plot.nd_experiment_id = e.nd_experiment_id OR plot.nd_experiment_id = e.parent_id) ");
		sb.append("  and plot.parent_id = env.nd_experiment_id and plot.type_id = 1155 ");
		sb.append("where e.project_id = :datasetId ");

		final SQLQuery createSQLQuery = this.getSession().createSQLQuery(sb.toString());
		createSQLQuery.addScalar("nd_experiment_id", new IntegerType());
		createSQLQuery.addScalar("environmentId", new IntegerType());
		createSQLQuery.addScalar("environmentId", new IntegerType());
		createSQLQuery.setParameter("datasetId", datasetId);

		final List<Object[]> results = createSQLQuery.list();
		final Map<Integer, ExperimentModel> map = new HashMap<>();
		if (results != null && !results.isEmpty()) {
			for (final Object[] row : results) {
				final Integer environmentId = (Integer) row[1];
				final Integer instanceNumber = (Integer) row[2];
				final ExperimentModel experimentModel = new ExperimentModel(environmentId);
				experimentModel.setObservationUnitNo(instanceNumber);
				map.put((Integer) row[0], experimentModel);
			}
		}
		return map;

	}



}
