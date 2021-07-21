/*******************************************************************************
 * Copyright (c) 2012, All Rights Reserved.
 * <p/>
 * Generation Challenge Programme (GCP)
 * <p/>
 * <p/>
 * This software is licensed for use under the terms of the GNU General Public License (http://bit.ly/8Ztv8M) and the provisions of Part F
 * of the Generation Challenge Programme Amended Consortium Agreement (http://bit.ly/KQX1nL)
 *******************************************************************************/

package org.generationcp.middleware.dao;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.ContextHolder;
import org.generationcp.middleware.api.brapi.v1.germplasm.GermplasmDTO;
import org.generationcp.middleware.api.brapi.v2.germplasm.GermplasmImportRequest;
import org.generationcp.middleware.domain.germplasm.GermplasmDto;
import org.generationcp.middleware.domain.germplasm.ParentType;
import org.generationcp.middleware.domain.germplasm.PedigreeDTO;
import org.generationcp.middleware.domain.germplasm.ProgenyDTO;
import org.generationcp.middleware.domain.germplasm.importation.GermplasmMatchRequestDto;
import org.generationcp.middleware.domain.oms.CvId;
import org.generationcp.middleware.domain.oms.Term;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.domain.ontology.Method;
import org.generationcp.middleware.domain.ontology.Property;
import org.generationcp.middleware.domain.ontology.Scale;
import org.generationcp.middleware.domain.ontology.TermRelationshipId;
import org.generationcp.middleware.domain.ontology.Variable;
import org.generationcp.middleware.domain.ontology.VariableType;
import org.generationcp.middleware.domain.search_request.brapi.v1.GermplasmSearchRequestDto;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.exceptions.MiddlewareRequestException;
import org.generationcp.middleware.manager.GermplasmDataManagerUtil;
import org.generationcp.middleware.manager.GermplasmNameType;
import org.generationcp.middleware.manager.Operation;
import org.generationcp.middleware.pojos.Germplasm;
import org.generationcp.middleware.pojos.MethodType;
import org.generationcp.middleware.pojos.Name;
import org.generationcp.middleware.pojos.Progenitor;
import org.generationcp.middleware.pojos.germplasm.GermplasmParent;
import org.generationcp.middleware.util.SqlQueryParamBuilder;
import org.generationcp.middleware.util.Util;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.AliasToBeanResultTransformer;
import org.hibernate.transform.Transformers;
import org.hibernate.type.BooleanType;
import org.hibernate.type.IntegerType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.util.CollectionUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DAO class for {@link Germplasm}.
 */
public class GermplasmDAO extends GenericDAO<Germplasm, Integer> {

	private static final String GRPLCE = "grplce";
	private static final String DELETED = "deleted";

	private static final String VARIABLE_ID = "vid";
	private static final String VARIABLE_NAME = "vn";
	private static final String VARIABLE_DEFINITION = "vd";

	private static final String METHOD_ID = "mid";
	private static final String METHOD_NAME = "mn";
	private static final String METHOD_DEFINITION = "md";

	private static final String PROPERTY_ID = "pid";
	private static final String PROPERTY_NAME = "pn";
	private static final String PROPERTY_DEFINITION = "pd";

	private static final String SCALE_ID = "sid";
	private static final String SCALE_NAME = "sn";
	private static final String SCALE_DEFINITION = "sd";

	private static final String VARIABLE_ALIAS = "p_alias";
	private static final String VARIABLE_EXPECTED_MAX = "p_max_value";
	private static final String VARIABLE_EXPECTED_MIN = "p_min_value";

	private static final String PROGRAM_UUID = "programUUID";
	private static final String GIDS = "gids";

	private static final String DER_MAN = "'" + MethodType.DERIVATIVE.getCode() + "','" + MethodType.MAINTENANCE.getCode() + "'";

	private static final String GEN = "'" + MethodType.GENERATIVE.getCode() + "'";

	private static final String QUERY_FROM_GERMPLASM = ") query from Germplasm: ";

	private static final Logger LOG = LoggerFactory.getLogger(GermplasmDAO.class);

	private static final String FIND_GERMPLASM_MATCHES_MAIN_QUERY = "select " //
		+ "    g.gid as gid, " //
		+ "    g.germplsm_uuid as germplasmUUID, " //
		+ "    n.nval as preferredName, " //
		+ "    pui.nval as germplasmPUI, "
		+ "    cast(g.gdate as char) as creationDate, " //
		+ "    r.analyt as reference, " //
		+ "    l.locid as breedingLocationId, " //
		+ "    l.lname as breedingLocation, " //
		+ "    m.mid as breedingMethodId, " //
		+ "    m.mname as breedingMethod, " //
		+ "    if(g.mgid > 0, true, false) as isGroupedLine, " //
		+ "    g.mgid as groupId, " //
		+ "    g.gpid1 as gpid1, "  //
		+ "    g.gpid2 as gpid2, "
		+ "    g.gnpgs as numberOfProgenitors, "
		+ "    g.created_by as createdByUserId " //
		+ "    from " //
		+ "    germplsm g " //
		+ "        left join " //
		+ "    methods m on m.mid = g.methn " //
		+ "        left join " //
		+ "    location l on l.locid = g.glocn " //
		+ "        left join " //
		+ "    names n on n.gid = g.gid and n.nstat = 1 " //
		+ " 		left join ("
		+ " 	select p.gid, p.nval from names p INNER JOIN udflds u ON u.fldno = p.ntype AND u.ftable = 'NAMES' and u.ftype='NAME' and u.fcode = 'PUI' and p.nstat <> 9) pui on pui.gid = g.gid "
		+ "        left join " //
		+ "    bibrefs r on g.gref = r.refid ";

	public GermplasmDAO(final Session session) {
		super(session);
	}

	@Override
	public Germplasm getById(final Integer gid, final boolean lock) {
		return this.getById(gid);
	}

	@Override
	public Germplasm getById(final Integer gid) {
		try {
			final String queryString = "SELECT g.* FROM germplsm g WHERE g.deleted = 0 AND g.grplce = 0 AND gid=:gid LIMIT 1";
			final SQLQuery query = this.getSession().createSQLQuery(queryString);
			query.setParameter("gid", gid);
			query.addEntity("g", Germplasm.class);

			return (Germplasm) query.uniqueResult();

		} catch (final HibernateException e) {
			final String errorMessage = "Error with getById(gid=" + gid + GermplasmDAO.QUERY_FROM_GERMPLASM + e.getMessage();
			GermplasmDAO.LOG.error(errorMessage, e);
			throw new MiddlewareQueryException(errorMessage, e);
		}
	}

	@SuppressWarnings("unchecked")
	public List<Germplasm> getByNamePermutations(final String name, final Operation operation, final int start, final int numOfRows) {

		// Converting supplied value to combination of names that can exists in names
		final List<String> names = GermplasmDataManagerUtil.createNamePermutations(name);

		if (CollectionUtils.isEmpty(names)) {
			return new ArrayList<>();
		}

		try {

			final String originalName = names.get(0);
			final String standardizedName = names.get(1);
			final String noSpaceName = names.get(2);

			// Search using = by default
			SQLQuery query = this.getSession().createSQLQuery(Germplasm.GET_BY_NAME_ALL_MODES_USING_EQUAL);
			if (operation == Operation.LIKE) {
				query = this.getSession().createSQLQuery(Germplasm.GET_BY_NAME_ALL_MODES_USING_LIKE);
			}

			// Set the parameters
			query.setParameter("name", originalName);
			query.setParameter("noSpaceName", noSpaceName);
			query.setParameter("standardizedName", standardizedName);

			query.addEntity("g", Germplasm.class);
			query.setFirstResult(start);
			query.setMaxResults(numOfRows);

			return query.list();
		} catch (final HibernateException e) {
			final String errorMessage = "Error with getByName(names=" + names + GermplasmDAO.QUERY_FROM_GERMPLASM + e.getMessage();
			GermplasmDAO.LOG.error(errorMessage, e);
			throw new MiddlewareQueryException(errorMessage, e);
		}
	}

	public long countByNamePermutations(final String name, final Operation operation) {

		// Converting supplied value to combination of names that can exists in names
		final List<String> names = GermplasmDataManagerUtil.createNamePermutations(name);

		if (CollectionUtils.isEmpty(names)) {
			return 0;
		}

		try {
			final String originalName = names.get(0);
			final String standardizedName = names.get(1);
			final String noSpaceName = names.get(2);

			// Count using = by default
			SQLQuery query = this.getSession().createSQLQuery(Germplasm.COUNT_BY_NAME_ALL_MODES_USING_EQUAL);
			if (operation == Operation.LIKE) {
				query = this.getSession().createSQLQuery(Germplasm.COUNT_BY_NAME_ALL_MODES_USING_LIKE);
			}

			// Set the parameters
			query.setParameter("name", originalName);
			query.setParameter("noSpaceName", noSpaceName);
			query.setParameter("standardizedName", standardizedName);

			return ((BigInteger) query.uniqueResult()).longValue();

		} catch (final HibernateException e) {
			final String errorMessage = "Error with countByName(names=" + names + GermplasmDAO.QUERY_FROM_GERMPLASM + e.getMessage();
			GermplasmDAO.LOG.error(errorMessage, e);
			throw new MiddlewareQueryException(errorMessage, e);
		}
	}

	public long countMatchGermplasmInList(final Set<Integer> gids) {

		if (gids == null || gids.isEmpty()) {
			return 0;
		}

		try {

			final Query query = this.getSession().getNamedQuery(Germplasm.COUNT_MATCH_GERMPLASM_IN_LIST);
			query.setParameterList("gids", gids);
			return ((Long) query.uniqueResult()).longValue();

		} catch (final HibernateException e) {
			final String errorMessage = "Error with countMatchGermplasmInList(gids) query from Germplasm: " + e.getMessage();
			GermplasmDAO.LOG.error(errorMessage, e);
			throw new MiddlewareQueryException(errorMessage, e);
		}

	}

	@SuppressWarnings("rawtypes")
	public Germplasm getByGIDWithPrefName(final Integer gid) {
		try {
			if (gid != null) {
				final SQLQuery query = this.getSession().createSQLQuery(Germplasm.GET_BY_GID_WITH_PREF_NAME);
				query.addEntity("g", Germplasm.class);
				query.addEntity("n", Name.class);
				query.setParameter("gid", gid);
				final List results = query.list();

				if (!results.isEmpty()) {
					final Object[] result = (Object[]) results.get(0);
					if (result != null) {
						final Germplasm germplasm = (Germplasm) result[0];
						final Name prefName = (Name) result[1];
						germplasm.setPreferredName(prefName);
						return germplasm;
					}
				}
			}
		} catch (final HibernateException e) {
			final String errorMessage = "Error with getByGIDWithPrefName(gid=" + gid + ") from Germplasm: " + e.getMessage();
			GermplasmDAO.LOG.error(errorMessage, e);
			throw new MiddlewareQueryException(errorMessage, e);
		}
		return null;
	}

	public List<Germplasm> getProgenitorsByGIDWithPrefName(final Integer gid) {
		Preconditions.checkNotNull(gid);
		try {
			final List<Germplasm> progenitors = new ArrayList<>();

			final SQLQuery query = this.getSession().createSQLQuery(Germplasm.GET_PROGENITORS_BY_GIDS_WITH_PREF_NAME);
			query.addScalar("gid");
			query.addEntity("g", Germplasm.class);
			query.addEntity("n", Name.class);
			query.addScalar("malePedigree");
			query.setParameterList("gidList", Lists.newArrayList(gid));
			final List<Object[]> results = query.list();
			for (final Object[] result : results) {
				final Germplasm germplasm = (Germplasm) result[1];
				final Name prefName = (Name) result[2];
				germplasm.setPreferredName(prefName);
				progenitors.add(germplasm);
			}

			return progenitors;
		} catch (final HibernateException e) {
			final String errorMessage =
				"Error with getProgenitorsByGIDWithPrefName(gid=" + gid + GermplasmDAO.QUERY_FROM_GERMPLASM + e.getMessage();
			GermplasmDAO.LOG.error(errorMessage, e);
			throw new MiddlewareQueryException(errorMessage, e);
		}
	}

	public Map<Integer, List<GermplasmParent>> getParentsFromProgenitorsForGIDsMap(final List<Integer> gids) {
		Preconditions.checkNotNull(gids);
		Preconditions.checkArgument(!gids.isEmpty());

		final Map<Integer, List<GermplasmParent>> map = new HashMap<>();
		try {
			final SQLQuery query = this.getSession().createSQLQuery(Germplasm.GET_PROGENITORS_BY_GIDS_WITH_PREF_NAME);
			query.addScalar("gid");
			query.addEntity("g", Germplasm.class);
			query.addEntity("n", Name.class);
			query.addScalar("malePedigree");
			query.setParameterList("gidList", gids);
			final List<Object[]> results = query.list();

			List<GermplasmParent> progenitors = new ArrayList<>();
			Integer lastGid = 0;
			for (final Object[] result : results) {
				final Integer crossGid = (Integer) result[0];
				if (lastGid == 0) {
					lastGid = crossGid;
				}
				if (!crossGid.equals(lastGid)) {
					map.put(lastGid, progenitors);
					lastGid = crossGid;
					progenitors = new ArrayList<>();
				}
				final Germplasm germplasm = (Germplasm) result[1];
				final Name prefName = (Name) result[2];
				final String pedigree = (String) result[3];
				germplasm.setPreferredName(prefName);
				progenitors.add(new GermplasmParent(germplasm.getGid(), prefName.getNval(), pedigree));
			}
			// Set last cross GID to map
			map.put(lastGid, progenitors);

			return map;
		} catch (final HibernateException e) {
			final String errorMessage =
				"Error with getProgenitorsForGIDsMap(gids=" + gids + GermplasmDAO.QUERY_FROM_GERMPLASM + e.getMessage();
			GermplasmDAO.LOG.error(errorMessage, e);
			throw new MiddlewareQueryException(errorMessage, e);
		}
	}

	@SuppressWarnings("unchecked")
	public List<Germplasm> getGermplasmDescendantByGID(final Integer gid, final int start, final int numOfRows) {
		try {
			if (gid != null) {
				final Query query = this.getSession().getNamedQuery(Germplasm.GET_DESCENDANTS);
				query.setParameter("gid", gid);
				query.setFirstResult(start);
				query.setMaxResults(numOfRows);
				return query.list();
			}
		} catch (final HibernateException e) {
			final String errorMessage =
				"Error with getGermplasmDescendantByGID(gid=" + gid + GermplasmDAO.QUERY_FROM_GERMPLASM + e.getMessage();
			GermplasmDAO.LOG.error(errorMessage, e);
			throw new MiddlewareQueryException(errorMessage, e);
		}
		return new ArrayList<>();
	}

	/**
	 * Only returns IDs of germplasm with progeny (Germplasm Id is assigned to other germplasm's gpid1, gpid2 OR progntrs.pid)
	 *
	 * @param gids - gids to be checked for descendants
	 * @return gids with progeny
	 */
	public Set<Integer> getGidsOfGermplasmWithDescendants(final Set<Integer> gids) {
		try {
			if (!CollectionUtils.isEmpty(gids)) {
				final SQLQuery query = this.getSession().createSQLQuery("SELECT g.gid as gid FROM germplsm g "
					+ "WHERE (EXISTS (SELECT 1 FROM germplsm descendant WHERE descendant.deleted = 0 AND "
					+ " (g.gid = descendant.gpid1 OR g.gid = descendant.gpid2) ) "
					+ " OR EXISTS (SELECT 1 FROM progntrs p INNER JOIN germplsm descendant ON descendant.gid = p.gid"
					+ " 					WHERE  g.gid = p.pid  AND descendant.deleted = 0)) "
					+ " AND g.gid IN (:gids) AND  g.deleted = 0 AND g.grplce = 0 ");
				query.addScalar("gid", new IntegerType());
				query.setParameterList("gids", gids);
				return Sets.newHashSet(query.list());
			}
			return Sets.newHashSet();
		} catch (final HibernateException e) {
			final String errorMessage = "Error with getGidsOfGermplasmWithDescendants(gids=" + gids + e.getMessage();
			GermplasmDAO.LOG.error(errorMessage, e);
			throw new MiddlewareQueryException(errorMessage, e);
		}
	}

	public Germplasm getProgenitorByGID(final Integer gid, final Integer proNo) {
		try {
			if (gid != null && proNo != null) {
				String progenitorQuery = "";
				if (proNo == 1) {
					progenitorQuery = Germplasm.GET_PROGENITOR1;
				} else if (proNo == 2) {
					progenitorQuery = Germplasm.GET_PROGENITOR2;
				} else if (proNo > 2) {
					progenitorQuery = Germplasm.GET_PROGENITOR;
				}

				final Query query = this.getSession().getNamedQuery(progenitorQuery);
				query.setParameter("gid", gid);

				if (proNo > 2) {
					query.setParameter("pno", proNo);
				}

				return (Germplasm) query.uniqueResult();
			}
		} catch (final HibernateException e) {
			final String errorMessage = "Error with getProgenitorByGID(gid=" + gid + GermplasmDAO.QUERY_FROM_GERMPLASM + e.getMessage();
			GermplasmDAO.LOG.error(errorMessage, e);
			throw new MiddlewareQueryException(errorMessage, e);
		}
		return null;
	}

	public long countGermplasmDescendantByGID(final Integer gid) {
		try {
			if (gid != null) {
				final Query query = this.getSession().createSQLQuery(Germplasm.COUNT_DESCENDANTS);
				query.setParameter("gid", gid);
				return ((BigInteger) query.uniqueResult()).longValue();
			}
		} catch (final HibernateException e) {
			final String errorMessage =
				"Error with countGermplasmDescendantByGID(gid=" + gid + GermplasmDAO.QUERY_FROM_GERMPLASM + e.getMessage();
			GermplasmDAO.LOG.error(errorMessage, e);
			throw new MiddlewareQueryException(errorMessage, e);
		}
		return 0;
	}

	public List<Germplasm> getManagementNeighbors(final Integer gid, final int start, final int numOfRows) {
		final List<Germplasm> toreturn = new ArrayList<>();
		try {
			if (gid != null) {
				final SQLQuery query = this.getSession().createSQLQuery(Germplasm.GET_MANAGEMENT_NEIGHBORS);
				query.addEntity("g", Germplasm.class);
				query.addEntity("n", Name.class);
				query.setParameter("gid", gid);

				query.setFirstResult(start);
				query.setMaxResults(numOfRows);

				for (final Object resultObject : query.list()) {
					final Object[] result = (Object[]) resultObject;
					final Germplasm germplasm = (Germplasm) result[0];
					final Name prefName = (Name) result[1];
					germplasm.setPreferredName(prefName);
					toreturn.add(germplasm);
				}
			}

		} catch (final HibernateException e) {
			final String errorMessage = "Error with getManagementNeighbors(gid=" + gid + GermplasmDAO.QUERY_FROM_GERMPLASM + e.getMessage();
			GermplasmDAO.LOG.error(errorMessage, e);
			throw new MiddlewareQueryException(errorMessage, e);
		}
		return toreturn;
	}

	public List<Germplasm> getGroupRelatives(final Integer gid, final int start, final int numRows) {
		final List<Germplasm> toreturn = new ArrayList<>();
		try {
			if (gid != null) {
				final SQLQuery query = this.getSession().createSQLQuery(Germplasm.GET_GROUP_RELATIVES);
				query.addEntity("g", Germplasm.class);
				query.addEntity("n", Name.class);
				query.setParameter("gid", gid);

				query.setFirstResult(start);
				query.setMaxResults(numRows);

				for (final Object resultObject : query.list()) {
					final Object[] result = (Object[]) resultObject;
					final Germplasm germplasm = (Germplasm) result[0];
					final Name prefName = (Name) result[1];
					germplasm.setPreferredName(prefName);
					toreturn.add(germplasm);
				}
			}
		} catch (final HibernateException e) {
			final String errorMessage = "Error with getGroupRelatives(gid=" + gid + GermplasmDAO.QUERY_FROM_GERMPLASM + e.getMessage();
			GermplasmDAO.LOG.error(errorMessage, e);
			throw new MiddlewareQueryException(errorMessage, e);
		}
		return toreturn;
	}

	public List<Germplasm> getDescendants(final Integer gid, final char methodType) {
		final List<Germplasm> toreturn = new ArrayList<>();
		try {
			final String queryString = methodType == 'D' ? Germplasm.GET_DERIVATIVE_CHILDREN : Germplasm.GET_MAINTENANCE_CHILDREN;
			final SQLQuery query = this.getSession().createSQLQuery(queryString);
			query.addEntity("g", Germplasm.class);
			query.addEntity("n", Name.class);
			query.setParameter("gid", gid);

			for (final Object resultObject : query.list()) {
				final Object[] result = (Object[]) resultObject;
				final Germplasm germplasm = (Germplasm) result[0];
				final Name prefName = (Name) result[1];
				germplasm.setPreferredName(prefName);
				toreturn.add(germplasm);
			}

		} catch (final HibernateException e) {
			final String errorMessage =
				"Error with getDescendants(gid=" + gid + ", methodType=" + methodType + ") query: " + e.getMessage();
			GermplasmDAO.LOG.error(errorMessage, e);
			throw new MiddlewareQueryException(errorMessage, e);
		}
		return toreturn;

	}

	public List<Germplasm> getAllChildren(final Integer gid) {
		try {
			final List<Germplasm> children = new ArrayList<>();
			// Get all derivative children
			children.addAll(this.getDescendants(gid, 'D'));

			// Get all maintenance children
			children.addAll(this.getDescendants(gid, 'M'));

			// Get all generative childern
			children.addAll(this.getGenerativeChildren(gid));
			return children;
		} catch (final HibernateException e) {
			final String message = "Error executing GermplasmDAO.getAllChildren(gid={}) : {}";
			GermplasmDAO.LOG.error(message, gid, e.getMessage());
			throw new MiddlewareQueryException(message, e);
		}
	}

	@SuppressWarnings("unchecked")
	public List<Germplasm> getGenerativeChildren(final Integer gid) {
		try {
			// Find generative children (gnpgs > 2)
			final DetachedCriteria generativeChildrenCriteria = DetachedCriteria.forClass(Germplasm.class);
			generativeChildrenCriteria.add(Restrictions.or(Restrictions.eq("gpid1", gid), Restrictions.eq("gpid2", gid)));
			// = Two or more parents
			generativeChildrenCriteria.add(Restrictions.ge("gnpgs", 2));
			// = Record is unchanged
			generativeChildrenCriteria.add(Restrictions.eq(GermplasmDAO.GRPLCE, 0));
			// = Record is not deleted or replaced.
			generativeChildrenCriteria.add(Restrictions.eq(GermplasmDAO.DELETED, Boolean.FALSE));

			final List<Germplasm> children =
				new ArrayList<>(generativeChildrenCriteria.getExecutableCriteria(this.getSession()).list());

			// Find additional children via progenitor linkage
			final DetachedCriteria otherChildrenCriteria = DetachedCriteria.forClass(Progenitor.class);
			otherChildrenCriteria.add(Restrictions.eq("progenitorGid", gid));

			final List<Progenitor> otherChildren = otherChildrenCriteria.getExecutableCriteria(this.getSession()).list();
			final Set<Integer> otherChildrenGids = new HashSet<>();
			for (final Progenitor progenitor : otherChildren) {
				otherChildrenGids.add(progenitor.getGermplasm().getGid());
			}

			if (!otherChildrenGids.isEmpty()) {
				children.addAll(this.getByGIDList(new ArrayList<>(otherChildrenGids)));
			}
			return children;
		} catch (final HibernateException e) {
			final String message = "Error executing GermplasmDAO.getGenerativeChildren(gid={}) : {}";
			GermplasmDAO.LOG.error(message, gid, e.getMessage());
			throw new MiddlewareQueryException(message, e);
		}
	}

	public List<Germplasm> getManagementGroupMembers(final Integer mgid) {
		if (mgid == null || mgid == 0) {
			return Collections.emptyList();
		}
		try {
			final DetachedCriteria criteria = DetachedCriteria.forClass(Germplasm.class);
			criteria.add(Restrictions.eq("mgid", mgid));
			// = Record is unchanged
			criteria.add(Restrictions.eq(GermplasmDAO.GRPLCE, 0));
			// = Record is not deleted or replaced.
			criteria.add(Restrictions.eq(GermplasmDAO.DELETED, Boolean.FALSE));

			@SuppressWarnings("unchecked") final List<Germplasm> groupMembers = criteria.getExecutableCriteria(this.getSession()).list();
			// Prime the names collection before returning ;)
			for (final Germplasm g : groupMembers) {
				g.getNames().size();
			}
			return groupMembers;
		} catch (final HibernateException e) {
			final String message = "Error executing GermplasmDAO.getGroupMembersByGroupId(mgid={}) : {}";
			GermplasmDAO.LOG.error(message, mgid, e.getMessage());
			throw new MiddlewareQueryException(message, e);
		}
	}

	public PedigreeDTO getPedigree(final Integer gid, final String notation, final Boolean includeSiblings) {
		try {
			final String query = "SELECT "
				+ "   g.germplsm_uuid as germplasmDbId,"
				+ "   (select n.nval from names n where n.gid = g.gid AND n.nstat = 1) as defaultDisplayName," //
				+ "   CONCAT(m.mcode, '|', m.mname, '|', m.mtype) AS crossingPlan," //
				+ "   year(str_to_date(g.gdate, '%Y%m%d')) as crossingYear," //
				+ "   femaleParent.germplsm_uuid as parent1DbId," //
				+ "   femaleParentName.nval as parent1Name," //
				// If germplasmDbId is a cross (gnpgs > 0), the parents' type should be FEMALE and MALE
				// If germplasmDbId is advanced (gnpgs < 0), the parents type should be POPULATION and SELF
				+ "   CASE WHEN femaleParent.gid is not null AND g.gnpgs > 0 THEN '" + ParentType.FEMALE.name() + "' "
				+ "	  WHEN femaleParent.gid is not null AND g.gnpgs < 0 THEN '" + ParentType.POPULATION.name()
				+ "' ELSE NULL END as parent1Type," //
				+ "   maleParent.germplsm_uuid as parent2DbId," //
				+ "   maleParentName.nval as parent2Name," //
				+ "   CASE WHEN maleParent.gid is not null AND g.gnpgs > 0 THEN '" + ParentType.MALE.name() + "' "
				+ "	  WHEN maleParent.gid is not null AND g.gnpgs < 0  THEN '" + ParentType.SELF.name()
				+ "' ELSE NULL END as parent2Type" //
				+ " FROM germplsm g" //
				+ "   LEFT JOIN methods m ON m.mid = g.methn" //
				+ "   LEFT JOIN germplsm femaleParent ON g.gpid1 = femaleParent.gid" //
				+ "   LEFT JOIN names femaleParentName ON femaleParent.gid = femaleParentName.gid AND femaleParentName.nstat = 1" //
				+ "   LEFT JOIN germplsm maleParent ON g.gpid2 = maleParent.gid" //
				+ "   LEFT JOIN names maleParentName ON maleParent.gid = maleParentName.gid AND maleParentName.nstat = 1" //
				+ " WHERE g.gid = :gid AND g.deleted = 0 AND g.grplce = 0";

			final PedigreeDTO pedigreeDTO = (PedigreeDTO) this.getSession().createSQLQuery(query) //
				.addScalar("germplasmDbId").addScalar("defaultDisplayName").addScalar("crossingPlan")
				.addScalar("crossingYear", new IntegerType()).addScalar("parent1DbId").addScalar("parent1Name").addScalar("parent1Type")
				.addScalar("parent2DbId").addScalar("parent2Name").addScalar("parent2Type")
				.setParameter("gid", gid) //
				.setResultTransformer(Transformers.aliasToBean(PedigreeDTO.class)) //
				.uniqueResult();

			if (includeSiblings == null || !includeSiblings) {
				return pedigreeDTO;
			}

			final String siblingsQuery = "SELECT" //
				+ "   sibling.germplsm_uuid AS germplasmDbId," //
				+ "   n.nval AS defaultDisplayName" //
				+ " FROM germplsm g" //
				+ "   INNER JOIN germplsm sibling ON sibling.gpid1 = g.gpid1"//
				+ "                                  AND sibling.gnpgs = -1"//
				+ "                                  AND g.gpid1 != 0"//
				+ "                                  AND sibling.gid != g.gid"//
				+ "   LEFT JOIN names n ON sibling.gid = n.gid AND n.nstat = 1" //
				+ " WHERE g.gid = :gid";

			final List<PedigreeDTO.Sibling> siblings = this.getSession().createSQLQuery(siblingsQuery) //
				.addScalar("germplasmDbId").addScalar("defaultDisplayName")
				.setParameter("gid", gid)
				.setResultTransformer(Transformers.aliasToBean(PedigreeDTO.Sibling.class)) //
				.list();

			pedigreeDTO.setSiblings(siblings);

			return pedigreeDTO;
		} catch (final HibernateException e) {
			GermplasmDAO.LOG.error(e.getMessage());
			throw new MiddlewareQueryException(e.getMessage(), e);
		}
	}

	public ProgenyDTO getProgeny(final Integer germplasmDbId) {
		try {
			final Germplasm germplasm = this.getByGIDWithPrefName(germplasmDbId);
			if (germplasm == null) {
				return null;
			}

			final String query = "SELECT" //
				+ "   progeny.germplsm_uuid as germplasmDbId," //
				+ "   name.nval as defaultDisplayName," //
				+ "   CASE" //
				+ "   WHEN progeny.gnpgs = -1" //
				+ "     THEN '" + ParentType.SELF.name() + "'" //
				+ "   WHEN progeny.gnpgs >= 2" //
				+ "     THEN" //
				+ "       CASE" //
				+ "         WHEN progeny.gpid1 = progeny.gpid2" //
				+ "           THEN '" + ParentType.SELF.name() + "'" //
				+ "         WHEN progeny.gpid1 = parent.gid" //
				+ "           THEN '" + ParentType.FEMALE.name() + "'" //
				+ "         ELSE '" + ParentType.MALE.name() + "'" //
				+ "       END" //
				+ "   ELSE ''" //
				+ "   END as parentType" //
				+ " FROM germplsm parent" //
				+ "   LEFT JOIN germplsm progeny ON (progeny.gnpgs = -1 AND progeny.gpid2 = parent.gid)" //
				+ "                                 OR (progeny.gnpgs >= 2 AND (progeny.gpid1 = parent.gid OR progeny.gpid2 = parent.gid))"
				//
				+ "   LEFT JOIN names name ON progeny.gid = name.gid AND name.nstat = 1" //
				+ " WHERE parent.gid = :gid" //
				+ "       AND parent.deleted = 0 AND parent.grplce = 0" //
				+ "       AND progeny.deleted = 0 AND progeny.grplce = 0";

			final List<ProgenyDTO.Progeny> progeny = this.getSession().createSQLQuery(query) //
				.addScalar("germplasmDbId").addScalar("defaultDisplayName").addScalar("parentType")
				.setParameter("gid", germplasmDbId) //
				.setResultTransformer(Transformers.aliasToBean(ProgenyDTO.Progeny.class)) //
				.list();

			final ProgenyDTO progenyDTO = new ProgenyDTO();
			progenyDTO.setGermplasmDbId(germplasm.getGermplasmUUID());
			progenyDTO.setDefaultDisplayName(germplasm.getPreferredName().getNval());

			progenyDTO.setProgeny(progeny);

			return progenyDTO;

		} catch (final HibernateException e) {
			GermplasmDAO.LOG.error(e.getMessage());
			throw new MiddlewareQueryException(e.getMessage(), e);
		}
	}

	/**
	 * <strong>Algorithm for checking parent groups for crosses</strong>
	 * <p>
	 * Graham provided the following thoughts on the approach for retrieving the germplasm in male and female MGID groups for crosses:
	 * <ol>
	 * <li>Get GID of all lines which have the same MGID as the female -whether the female is a cross or a line, same thing - e.g. 1,2,3
	 * (all members of the female management group)
	 * <li>Get all lines which have same MGID as male - 4,5 (all members of the male management group)
	 * <li>See if any of the crosses 1x4, 1x5, 2x4, 2x5, 3x4 or 3x5 were made before.
	 * <li>If so assign the new cross to the same group.
	 * </ol>
	 *
	 * <p>
	 * Graham also noted that this query is similar to the existing one to retrieve the management group of a germplasm in the germplasm
	 * details pop-up.
	 */
	public List<Germplasm> getPreviousCrossesBetweenParentGroups(final Germplasm currentCross) {
		final Germplasm femaleParent = this.getById(currentCross.getGpid1());
		final Germplasm maleParent = this.getById(currentCross.getGpid2());

		final List<Germplasm> femaleGroupMembers = this.getManagementGroupMembers(femaleParent.getMgid());
		final List<Germplasm> maleGroupMembers = this.getManagementGroupMembers(maleParent.getMgid());

		final List<Germplasm> previousCrossesInGroup = new ArrayList<>();
		for (final Germplasm femaleGroupMember : femaleGroupMembers) {
			for (final Germplasm maleGroupMember : maleGroupMembers) {
				previousCrossesInGroup.addAll(this.getPreviousCrosses(currentCross, femaleGroupMember, maleGroupMember));
			}
		}

		// Sort oldest to newest cross : ascending order of gid
		Collections.sort(previousCrossesInGroup, (o1, o2) -> o1.getGid() < o2.getGid() ? -1 : o1.getGid().equals(o2.getGid()) ? 0 : 1);

		return previousCrossesInGroup;
	}

	public List<Germplasm> getPreviousCrosses(final Germplasm currentCross, final Germplasm female, final Germplasm male) {
		try {
			final DetachedCriteria criteria = DetachedCriteria.forClass(Germplasm.class);

			// (female x male) is not the same as (male x female) so the order is important.
			criteria.add(Restrictions.eq("gpid1", female.getGid()));
			criteria.add(Restrictions.eq("gpid2", male.getGid()));
			// Restrict to cases where two parents are involved.
			criteria.add(Restrictions.eq("gnpgs", 2));
			// = Record is unchanged.
			criteria.add(Restrictions.eq(GermplasmDAO.GRPLCE, 0));
			// Exclude current cross. We are finding "previous" crosses.
			criteria.add(Restrictions.ne("gid", currentCross.getGid()));
			// = Record is not or replaced.
			criteria.add(Restrictions.eq(GermplasmDAO.DELETED, Boolean.FALSE));
			// Oldest created cross will be first in list.
			criteria.addOrder(Order.asc("gid"));

			@SuppressWarnings("unchecked") final List<Germplasm> previousCrosses = criteria.getExecutableCriteria(this.getSession()).list();
			return previousCrosses;
		} catch (final HibernateException e) {
			final String message = "Error executing GermplasmDAO.getPreviousCrosses(female = {}, male = {}): {}";
			GermplasmDAO.LOG.error(message, female, male, e.getMessage());
			throw new MiddlewareQueryException(message, e);
		}
	}

	public String getNextSequenceNumber(String prefix) {
		String nextInSequence = "1";

		if (!prefix.isEmpty()) {
			try {
				prefix = prefix.trim();

				final String queryString = "SELECT CONVERT(REPLACE(UPPER(nval), :prefix, ''), SIGNED)+1 as next_number "
					// We used LIKE when matching names by prefix
					+ " FROM ( " + " 	SELECT  distinct nval " + "		FROM names " + "		WHERE names.nval LIKE :prefixLike "
					+ "   	AND NOT EXISTS (select 1 from germplsm g where g.gid = names.gid and g.deleted = 1)" + " ) matches "
					+ " ORDER BY next_number desc LIMIT 1";
				final SQLQuery query = this.getSession().createSQLQuery(queryString);
				query.setParameter("prefix", prefix.toUpperCase());
				query.setParameter("prefixLike", prefix + "%");

				final BigInteger nextNumberInSequence = (BigInteger) query.uniqueResult();

				if (nextNumberInSequence != null) {
					nextInSequence = String.valueOf(nextNumberInSequence);
				}

			} catch (final HibernateException e) {
				final String message = "Error with getNextSequenceNumber(prefix=" + prefix + ") " + "query : " + e.getMessage();
				GermplasmDAO.LOG.error(message, e);
				throw new MiddlewareQueryException(message, e);
			}
		}

		return nextInSequence;
	}

	@SuppressWarnings("unchecked")
	public List<Germplasm> getByLocationId(final String name, final int locationID) {
		try {

			final String queryString = "SELECT {g.*} FROM germplsm g JOIN names n ON g.gid = n.gid WHERE "
				+ "n.nval = :name "
				+ "AND g.glocn = :locationID ";
			final SQLQuery query = this.getSession().createSQLQuery(queryString);
			query.setParameter("name", name);
			query.setParameter("locationID", locationID);
			query.addEntity("g", Germplasm.class);

			return query.list();

		} catch (final HibernateException e) {
			final String message = "Error with getByLocationId(name=" + name + ", locationID=" + locationID
				+ GermplasmDAO.QUERY_FROM_GERMPLASM + e.getMessage();
			GermplasmDAO.LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
	}

	@SuppressWarnings("unchecked")
	public List<Germplasm> getByGIDRange(final int startGID, final int endGID) {
		try {

			final String queryString = "SELECT {g.*} FROM germplsm g WHERE "
				+ "g.gid >= :startGID "
				+ "AND g.gid <= :endGID ";
			final SQLQuery query = this.getSession().createSQLQuery(queryString);
			query.setParameter("startGID", startGID);
			query.setParameter("endGID", endGID);
			query.addEntity("g", Germplasm.class);

			return query.list();

		} catch (final HibernateException e) {
			final String message = "Error with getByGIDRange(startGID=" + startGID + ", endGID=" + endGID
				+ GermplasmDAO.QUERY_FROM_GERMPLASM + e.getMessage();
			GermplasmDAO.LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
	}

	@SuppressWarnings("unchecked")
	public List<Germplasm> getByGIDList(final List<Integer> gids) {

		if (gids.isEmpty()) {
			return new ArrayList<>();
		}

		try {

			final String queryString = "SELECT {g.*} FROM germplsm g WHERE "
				+ "g.gid IN( :gids ) "
				+ " AND g.deleted = 0"
				+ " AND g.grplce = 0";
			final SQLQuery query = this.getSession().createSQLQuery(queryString);
			query.setParameterList("gids", gids);
			query.addEntity("g", Germplasm.class);

			return query.list();

		} catch (final HibernateException e) {
			final String message = "Error with getByGIDList(gids=" + gids + GermplasmDAO.QUERY_FROM_GERMPLASM + e.getMessage();
			GermplasmDAO.LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
	}

	public List<Germplasm> getByGIDsOrUUIDListWithMethodAndBibref(final Set<Integer> gids, final Set<String> uuids) {

		if (uuids.isEmpty() && gids.isEmpty()) {
			return new ArrayList<>();
		}

		try {
			final Criteria criteria = this.getSession().createCriteria(Germplasm.class);
			criteria.createAlias("method", "method", CriteriaSpecification.LEFT_JOIN);
			criteria.createAlias("bibref", "bibref", CriteriaSpecification.LEFT_JOIN);
			if (gids.isEmpty()) {
				criteria.add(Restrictions.in("germplasmUUID", uuids));
			} else if (uuids.isEmpty()) {
				criteria.add(Restrictions.in("gid", gids));
			} else {
				criteria.add(Restrictions.or(Restrictions.in("germplasmUUID", uuids), Restrictions.in("gid", gids)));
			}
			criteria.add(Restrictions.eq("deleted", false));
			criteria.add(Restrictions.eq("grplce", 0));

			return criteria.list();

		} catch (final HibernateException e) {
			final String message =
				"Error with getByUUIDListWithMethodAndBibref(gids=" + uuids + GermplasmDAO.QUERY_FROM_GERMPLASM + e.getMessage();
			GermplasmDAO.LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
	}

	public Map<Integer, Integer> getGermplasmDatesByGids(final List<Integer> gids) {
		final Map<Integer, Integer> resultMap = new HashMap<>();
		final SQLQuery query = this.getSession().createSQLQuery(Germplasm.GET_GERMPLASM_DATES_BY_GIDS);
		query.setParameterList("gids", gids);
		@SuppressWarnings("rawtypes") final List results = query.list();
		for (final Object result : results) {
			final Object[] resultArray = (Object[]) result;
			final Integer gid = (Integer) resultArray[0];
			final Integer gdate = (Integer) resultArray[1];
			resultMap.put(gid, gdate);
		}
		return resultMap;
	}

	public Map<Integer, Integer> getMethodIdsByGids(final List<Integer> gids) {
		final Map<Integer, Integer> resultMap = new HashMap<>();
		final SQLQuery query = this.getSession().createSQLQuery(Germplasm.GET_METHOD_IDS_BY_GIDS);
		query.setParameterList("gids", gids);
		@SuppressWarnings("rawtypes") final List results = query.list();
		for (final Object result : results) {
			final Object[] resultArray = (Object[]) result;
			final Integer gid = (Integer) resultArray[0];
			final Integer methodId = (Integer) resultArray[1];
			resultMap.put(gid, methodId);
		}
		return resultMap;
	}

	/**
	 * Returns a Map with the names of parental germplasm for a given study. These names are returned in a Map, where the key is the
	 * germplasm identifier (gid) and the value is a list with all the names ({@link Name}) for such germplasm. This method optimizes data
	 * returned, because in a study is common that many entries have common parents, so those duplicated parents are omitted in returned
	 * Map.
	 *
	 * @param studyId The ID of the study from which we need to get parents information. Usually this is the ID of a crossing block.
	 * @return
	 */
	public Map<Integer, Map<GermplasmNameType, Name>> getGermplasmParentNamesForStudy(final int studyId) {

		final SQLQuery queryNames = this.getSession().createSQLQuery(Germplasm.GET_PARENT_NAMES_BY_STUDY_ID);
		queryNames.setParameter("projId", studyId);

		@SuppressWarnings("rawtypes") final List resultNames = queryNames.list();

		Name name;
		final Map<Integer, Map<GermplasmNameType, Name>> names = new HashMap<>();
		for (final Object result : resultNames) {
			final Object[] resultArray = (Object[]) result;
			final Integer gid = Integer.valueOf(resultArray[0].toString());
			final Integer ntype = Integer.valueOf(resultArray[1].toString());
			final String nval = resultArray[2].toString();
			final Integer nid = Integer.valueOf(resultArray[3].toString());
			final Integer nstat = Integer.valueOf(resultArray[4].toString());

			name = new Name(nid);
			name.setGermplasm(new Germplasm(gid));
			name.setNval(nval);
			name.setTypeId(ntype);
			name.setNstat(nstat);

			if (!names.containsKey(gid)) {
				names.put(gid, new HashMap<>());
			}

			GermplasmNameType type = GermplasmNameType.valueOf(name.getTypeId());
			if (type == null) {
				type = GermplasmNameType.UNRESOLVED_NAME;
			}

			if (!names.get(gid).containsKey(type) || names.get(gid).get(type).getNstat() != 1) {
				names.get(gid).put(type, name);
			}

		}

		return names;
	}

	public List<Germplasm> getGermplasmParentsForStudy(final int studyId) {
		final SQLQuery queryGermplasms = this.getSession().createSQLQuery(Germplasm.GET_KNOWN_PARENT_GIDS_BY_STUDY_ID);
		queryGermplasms.setParameter("projId", studyId);

		final List<Germplasm> germplasms = new ArrayList<>();
		Germplasm g;

		@SuppressWarnings("rawtypes") final List resultGermplasms = queryGermplasms.list();
		for (final Object result : resultGermplasms) {
			final Object[] resultArray = (Object[]) result;
			g = new Germplasm(Integer.valueOf(resultArray[0].toString()));
			g.setGpid1(Integer.valueOf(resultArray[1].toString()));
			g.setGpid2(Integer.valueOf(resultArray[2].toString()));
			g.setGrplce(Integer.valueOf(resultArray[3].toString()));

			germplasms.add(g);
		}
		return germplasms;
	}

	@SuppressWarnings("unchecked")
	public Map<Integer, String[]> getParentsInfoByGIDList(final List<Integer> gidList) {
		try {
			final Map<Integer, String[]> pedigreeMap = new HashMap<>();
			final SQLQuery query = this.getSession().createSQLQuery(Germplasm.GET_PREFERRED_NAME_AND_PARENT_FOR_A_GID_LIST);
			query.setParameterList("gidList", gidList);
			query.addScalar("gid");
			query.addScalar("pedigree");
			query.addScalar("nval");

			final List<Object[]> results = query.list();
			for (final Object[] result : results) {
				pedigreeMap.put((Integer) result[0], new String[] {(String) result[1], (String) result[2]});
			}
			if (gidList.contains(0)) {
				pedigreeMap.put(0, new String[] {Name.UNKNOWN, Name.UNKNOWN});
			}
			return pedigreeMap;
		} catch (final HibernateException e) {
			final String message = "Error with getPedigreeByGIDList(GIDS=" + gidList + ") : " + e.getMessage();
			GermplasmDAO.LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
	}

	public void deleteGermplasm(final List<Integer> gids) {
		final StringBuilder queryString = new StringBuilder();

		try {
			queryString.append("UPDATE germplsm SET deleted = 1, germplsm_uuid = CONCAT (germplsm_uuid, '#', '" + Util
				.getCurrentDateAsStringValue("yyyyMMddHHmmssSSS") + "'), modified_date = CURRENT_TIMESTAMP, modified_by = :userId "
				+ "WHERE gid in (:gids)");
			final SQLQuery query = this.getSession().createSQLQuery(queryString.toString());
			query.setParameterList("gids", gids);
			query.setParameter("userId", ContextHolder.getLoggedInUserId());
			query.executeUpdate();

		} catch (final HibernateException e) {
			final String message = "Error with deleteGermplasms(GIDS=" + gids + ")  " + e.getMessage();
			GermplasmDAO.LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
	}

	/**
	 * Get the immediate descendants of a list of gids
	 *
	 * @param gids the gids
	 * @return a map of gids and its offspring @
	 */
	@SuppressWarnings("unchecked")
	public Map<Integer, Set<Integer>> getGermplasmOffspringByGIDs(final List<Integer> gids) {

		if (gids != null && !gids.isEmpty()) {
			final Map<Integer, Set<Integer>> resultMap = new HashMap<>();

			final Query query = this.getSession().createSQLQuery(Germplasm.GET_GERMPLASM_OFFSPRING_BY_GID);
			query.setParameterList("gids", gids);

			/**
			 * Returns two columns: - gid - CSV of parents of the gid (gpid1, gpid2 or progntrs.pid)
			 */
			final List<Object[]> results = query.list();

			// Transform to Map of gid -> list of offspring
			for (final Object[] result : results) {
				final String[] parentsStr = ObjectUtils.toString(result[1]).split(",");
				final Set<Integer> parents = new HashSet<>();

				for (final String parentStr : parentsStr) {
					try {
						parents.add(Integer.parseInt(parentStr));
					} catch (final NumberFormatException e) {
						GermplasmDAO.LOG.warn("Could not cast " + parentStr);
					}
				}

				final Integer offspring = (Integer) result[0];

				for (final Integer parent : parents) {
					if (!resultMap.containsKey(parent) && gids.contains(parent)) {
						resultMap.put(parent, new HashSet<>());
					} else if (gids.contains(parent)) {
						resultMap.get(parent).add(offspring);
					}
				}
			}

			return resultMap;
		}

		return new HashMap<>();
	}

	/**
	 * Only return germplasm with no group assigned (mgid = 0 or mgid is null)
	 *
	 * @param gids
	 * @return
	 */
	public List<Germplasm> getGermplasmWithoutGroup(final List<Integer> gids) {

		if (gids.isEmpty()) {
			return new ArrayList<>();
		}

		try {

			final String queryString = "SELECT {g.*} FROM germplsm g WHERE "
				+ "g.gid IN( :gids ) AND (g.mgid = 0 || g.mgid IS NULL)";
			final SQLQuery query = this.getSession().createSQLQuery(queryString);
			query.setParameterList("gids", gids);
			query.addEntity("g", Germplasm.class);

			return query.list();

		} catch (final HibernateException e) {
			final String message = "Error with getGermplasmWithoutGroup(gids=" + gids + ") " + e.getMessage();
			GermplasmDAO.LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
	}

	/**
	 * Resets the mgids of a given list of germplasm to zero.
	 *
	 * @param gids
	 */
	public void resetGermplasmGroup(final List<Integer> gids) {
		if (CollectionUtils.isEmpty(gids)) {
			return;
		}

		try {

			final SQLQuery query = this.getSession().createSQLQuery("UPDATE germplsm SET mgid = 0, "
				+ "modified_date = CURRENT_TIMESTAMP, modified_by = :userId WHERE gid IN (:gids)");
			query.setParameterList("gids", gids);
			query.setParameter("userId", ContextHolder.getLoggedInUserId());
			query.executeUpdate();

		} catch (final HibernateException e) {
			final String message = "Error with resetGermplasmGroup(gids=" + gids + ") " + e.getMessage();
			GermplasmDAO.LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}

	}

	private void addGermplasmSearchParameters(final SqlQueryParamBuilder paramBuilder,
		final GermplasmSearchRequestDto germplasmSearchRequestDTO) {
		if (StringUtils.isNoneBlank(germplasmSearchRequestDTO.getPreferredName())) {
			paramBuilder.setParameter("preferredName", germplasmSearchRequestDTO.getPreferredName() + "%"); //
		}

		if (!CollectionUtils.isEmpty(germplasmSearchRequestDTO.getAccessionNumbers())) {
			paramBuilder.setParameterList("accessionNumbers", germplasmSearchRequestDTO.getAccessionNumbers());
		}

		if (!CollectionUtils.isEmpty(germplasmSearchRequestDTO.getCommonCropNames())) {
			paramBuilder.setParameterList("commonCropNames", germplasmSearchRequestDTO.getCommonCropNames());
		}

		if (!CollectionUtils.isEmpty(germplasmSearchRequestDTO.getGermplasmDbIds())) {
			paramBuilder.setParameterList("germplasmDbIds", germplasmSearchRequestDTO.getGermplasmDbIds());
		}

		if (!CollectionUtils.isEmpty(germplasmSearchRequestDTO.getGermplasmGenus())) {
			paramBuilder.setParameterList("germplasmGenus", germplasmSearchRequestDTO.getGermplasmGenus());
		}

		if (!CollectionUtils.isEmpty(germplasmSearchRequestDTO.getGermplasmPUIs())) {
			paramBuilder.setParameterList("germplasmPUIs", germplasmSearchRequestDTO.getGermplasmPUIs());
		}

		if (!CollectionUtils.isEmpty(germplasmSearchRequestDTO.getGermplasmNames())) {
			paramBuilder.setParameterList("germplasmNames", germplasmSearchRequestDTO.getGermplasmNames());
		}

		if (!CollectionUtils.isEmpty(germplasmSearchRequestDTO.getGermplasmSpecies())) {
			paramBuilder.setParameterList("germplasmSpecies", germplasmSearchRequestDTO.getGermplasmSpecies());
		}

		if (StringUtils.isNoneBlank(germplasmSearchRequestDTO.getStudyDbId())) {
			paramBuilder.setParameter("studyDbId", germplasmSearchRequestDTO.getStudyDbId());
		}

		if (StringUtils.isNoneBlank(germplasmSearchRequestDTO.getParentDbId())) {
			paramBuilder.setParameter("parentDbId", germplasmSearchRequestDTO.getParentDbId());
		}

		if (StringUtils.isNoneBlank(germplasmSearchRequestDTO.getProgenyDbId())) {
			paramBuilder.setParameter("progenyDbId", germplasmSearchRequestDTO.getProgenyDbId());
		}

		if (StringUtils.isNoneBlank(germplasmSearchRequestDTO.getExternalReferenceSource())) {
			paramBuilder.setParameter("referenceSource", germplasmSearchRequestDTO.getExternalReferenceSource());
		}

		if (StringUtils.isNoneBlank(germplasmSearchRequestDTO.getExternalReferenceId())) {
			paramBuilder.setParameter("referenceId", germplasmSearchRequestDTO.getExternalReferenceId());
		}
	}

	// These are appended to the MAIN where clause because they are filters on (or EXISTS clause related to) the main germplasm object
	private void addGermplasmSearchRequestFilters(final SqlQueryParamBuilder paramBuilder,
		final GermplasmSearchRequestDto germplasmSearchRequestDTO) {
		if (!CollectionUtils.isEmpty(germplasmSearchRequestDTO.getGermplasmDbIds())) {
			paramBuilder.append(" AND g.germplsm_uuid IN (:germplasmDbIds) ");
		}

		// Search synonyms or non-default names
		if (!CollectionUtils.isEmpty(germplasmSearchRequestDTO.getGermplasmNames())) {
			paramBuilder.append(" AND g.gid IN ( SELECT n.gid ");
			paramBuilder.append(" FROM names n WHERE n.nval IN (:germplasmNames) and n.nstat != 9 and n.nstat != 1 ) "); //
		}

		if (StringUtils.isNoneBlank(germplasmSearchRequestDTO.getStudyDbId())) {
			paramBuilder.append(" AND g.gid IN ( "
				+ "  SELECT s.dbxref_id from stock s "
				+ "   INNER JOIN nd_experiment e ON e.stock_id = s.stock_id "
				+ "   WHERE e.nd_geolocation_id = :studyDbId ) ");
		}

		if (StringUtils.isNoneBlank(germplasmSearchRequestDTO.getParentDbId())) {
			paramBuilder.append(" AND (g.gpid1 = (select parent.gid from germplsm parent where parent.germplsm_uuid = :parentDbId) "
				+ "OR g.gpid2 = (select parent.gid from germplsm parent where parent.germplsm_uuid = :parentDbId) "
				+ "OR g.gid = (Select p.gid from progntrs p INNER JOIN germplsm parent ON p.pid = parent.gid "
				+ " WHERE parent.germplsm_uuid = :parentDbId )) AND g.gnpgs >= 2");
		}

		if (StringUtils.isNoneBlank(germplasmSearchRequestDTO.getProgenyDbId())) {
			paramBuilder
				.append(" AND ( g.gid = (SELECT gpid1 from germplsm child WHERE child.germplsm_uuid = :progenyDbId AND child.gnpgs >= 2) "
					+ "OR g. gid = (SELECT gpid2 from germplsm child WHERE child.germplsm_uuid = :progenyDbId AND child.gnpgs >= 2) "
					+ "OR g.gid = (Select p.pid from progntrs p INNER JOIN germplsm child ON p.gid = child.gid "
					+ "WHERE child.germplsm_uuid = :progenyDbId )) ");
		}

		if (StringUtils.isNoneBlank(germplasmSearchRequestDTO.getExternalReferenceId())) {
			paramBuilder.append(" AND g.gid IN ( ");
			paramBuilder.append(" SELECT ref.gid FROM external_reference_germplasm ref WHERE ref.reference_id = :referenceId) "); //
		}

		if (StringUtils.isNoneBlank(germplasmSearchRequestDTO.getExternalReferenceSource())) {
			paramBuilder.append(" AND g.gid IN (  ");
			paramBuilder.append(" SELECT ref.gid FROM external_reference_germplasm ref WHERE ref.reference_source = :referenceSource) "); //
		}

		if (StringUtils.isNoneBlank(germplasmSearchRequestDTO.getPreferredName())) {
			paramBuilder.append(" AND g.gid IN ( SELECT n.gid ");
			paramBuilder.append(" FROM names n WHERE n.nstat = 1 AND n.nval like :preferredName ) ");
		}

		if (!CollectionUtils.isEmpty(germplasmSearchRequestDTO.getAccessionNumbers())) {
			paramBuilder.append(" AND g.gid IN ( SELECT n.gid  ");
			paramBuilder.append(" FROM names n ");
			paramBuilder.append(" INNER JOIN udflds u on n.ntype = u.fldno AND u.fcode = '" + GermplasmImportRequest.ACCNO_NAME_TYPE + "'");
			paramBuilder.append(" WHERE n.nval IN (:accessionNumbers)  ) ");
		}

		if (!CollectionUtils.isEmpty(germplasmSearchRequestDTO.getCommonCropNames())) {
			paramBuilder.append(" AND g.gid IN ( SELECT a.gid  ");
			paramBuilder.append(" FROM atributs a");
			paramBuilder.append(
				" INNER JOIN cvterm c on a.atype = c.cvterm_id AND c.name = '" + GermplasmImportRequest.CROPNM_ATTR + "' and c.cv_id = "
					+ CvId.VARIABLES.getId());
			paramBuilder.append(" WHERE a.aval IN (:commonCropNames)  ) ");
		}

		if (!CollectionUtils.isEmpty(germplasmSearchRequestDTO.getGermplasmGenus())) {
			paramBuilder.append(" AND g.gid IN ( SELECT n.gid  ");
			paramBuilder.append(" FROM names n ");
			paramBuilder.append(" INNER JOIN udflds u on n.ntype = u.fldno AND u.fcode = '" + GermplasmImportRequest.GENUS_NAME_TYPE + "'");
			paramBuilder.append(" WHERE n.nval IN (:germplasmGenus)  ) ");
		}

		if (!CollectionUtils.isEmpty(germplasmSearchRequestDTO.getGermplasmPUIs())) {
			paramBuilder.append(" AND g.gid IN ( SELECT n.gid  ");
			paramBuilder.append(" FROM names n ");
			paramBuilder.append(" INNER JOIN udflds u on n.ntype = u.fldno AND u.fcode = '" + GermplasmImportRequest.PUI_NAME_TYPE + "'");
			paramBuilder.append(" WHERE n.nval IN (:germplasmPUIs)  ) ");
		}

		if (!CollectionUtils.isEmpty(germplasmSearchRequestDTO.getGermplasmSpecies())) {
			paramBuilder.append(" AND g.gid IN ( SELECT a.gid  ");
			paramBuilder.append(" FROM atributs a");
			paramBuilder.append(
				" INNER JOIN cvterm c on a.atype = c.cvterm_id AND c.name = '" + GermplasmImportRequest.SPECIES_ATTR + "' and c.cv_id = "
					+ CvId.VARIABLES.getId());
			paramBuilder.append(" WHERE a.aval IN (:germplasmSpecies)  ) ");
		}

	}

	public List<GermplasmDTO> getGermplasmDTOList(
		final GermplasmSearchRequestDto germplasmSearchRequestDTO, final Pageable pageable) {

		try {
			// Apply search filter and pagination on main germplasm entity
			final SQLQuery filterQuery = this.getSession().createSQLQuery(this.buildFilterGermplasmQuery(germplasmSearchRequestDTO));
			this.addGermplasmSearchParameters(new SqlQueryParamBuilder(filterQuery), germplasmSearchRequestDTO);
			addPaginationToSQLQuery(filterQuery, pageable);
			final List<String> gids = filterQuery.list();

			// If there is any match, build the list of  GermplasmDTO based on the matched GIDs
			if (!CollectionUtils.isEmpty(gids)) {
				final SQLQuery sqlQuery = this.getSession().createSQLQuery(this.buildGetGermplasmQuery());
				this.addScalarsForGermplasmDTO(sqlQuery).setResultTransformer(new AliasToBeanResultTransformer(GermplasmDTO.class));
				sqlQuery.setParameterList("gids", gids);
				return sqlQuery.list();
			}

		} catch (final HibernateException e) {
			final String message = "Error with getGermplasmDTOList" + e.getMessage();
			GermplasmDAO.LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}

		return new ArrayList<>();
	}

	private String getSelectClauseForFilterGermplasm() {
		return "SELECT g.germplsm_uuid AS germplasmDbId, convert(g.gid, char) as gid, " //
			+ "  g.gpid1, g.gpid2, g.gnpgs, " //
			+ "  max(if(ntype.fcode = 'ACCNO', n.nval, null)) as accessionNumber, " //
			+ "   STR_TO_DATE (convert(g.gdate,char), '%Y%m%d') AS acquisitionDate," //
			+ "   loc.labbr AS countryOfOriginCode, " //
			+ "   max(if(n.nstat = 1, n.nval, null)) as germplasmName, " //
			+ "  max(if(ntype.fcode = 'GENUS', n.nval, null)) as genus," //
			+ "  max(if(ntype.fcode = 'PUI', n.nval, null)) as germplasmPUI," //
			+ "   max(if(atype.name = 'PLOTCODE_AP_text', a.aval, null)) as germplasmSeedSource,  "
			+ "   max(if(atype.name = 'SPNAM_AP_text', a.aval, null)) as species, "
			+ "   max(if(atype.name = 'SPAUTH_AP_text', a.aval, null)) as speciesAUthority, "
			+ "   max(if(atype.name = 'SUBTAX_AP_text', a.aval, null)) as subtaxa, "
			+ "   max(if(atype.name = 'STAUTH_AP_text', a.aval, null)) as subtaxaAuthority, "
			+ "   max(if(atype.name = 'INSTCODE_AP_text', a.aval, null)) as instituteCode, "
			+ "   max(if(atype.name = 'ORIGININST_AP_text', a.aval, null)) as instituteName, "
			+ "   max(if(atype.name = 'SORIG_AP_text', a.aval, null)) as germplasmOrigin, "
			+ "   max(if(atype.name = 'CROPNM_AP_text', a.aval, null)) as commonCropName, "
			+ "   convert(g.methn, char) as breedingMethodDbId ";
	}

	private String getMainFromGermplasmClause() {
		return "  FROM germplsm g "
			+ "  	LEFT join location loc ON g.glocn = loc.locid "
			+ "  	LEFT JOIN atributs a ON a.gid = g.gid "
			+ "  	LEFT JOIN cvterm atype ON atype.cvterm_id = a.atype "
			+ "  	LEFT join names n ON n.gid = g.gid and n.nstat != 9 "
			+ "  	LEFT JOIN udflds ntype ON ntype.fldno = n.ntype ";
	}

	private String buildFilterGermplasmQuery(final GermplasmSearchRequestDto germplasmSearchRequestDTO) {
		final StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append(" SELECT g.gid ");
		queryBuilder.append(" FROM germplsm g ");
		queryBuilder.append(" WHERE g.deleted = 0 AND g.grplce = 0 ");
		this.addGermplasmSearchRequestFilters(new SqlQueryParamBuilder(queryBuilder), germplasmSearchRequestDTO);
		return queryBuilder.toString();
	}

	private String buildGetGermplasmQuery() {
		final StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append(this.getSelectClauseForFilterGermplasm());
		queryBuilder.append(this.getMainFromGermplasmClause()); //
		queryBuilder.append(" WHERE g.deleted = 0 AND g.grplce = 0 ");
		queryBuilder.append(" AND g.gid IN (:gids) ");
		queryBuilder.append(" GROUP by g.gid ");
		return queryBuilder.toString();
	}

	public long countGermplasmDTOs(final GermplasmSearchRequestDto germplasmSearchRequestDTO) {
		final SQLQuery sqlQuery = this.getSession().createSQLQuery(this.buildCountGermplasmDTOsQuery((germplasmSearchRequestDTO)));
		this.addGermplasmSearchParameters(new SqlQueryParamBuilder(sqlQuery), germplasmSearchRequestDTO);

		final long count = ((BigInteger) sqlQuery.uniqueResult()).longValue();
		/* For unfiltered search and result of initial non-deleted germplasm count is < 5000, query the actual
		   non-deleted germplasm count. We are sure at this point that the germplasm DB is small enough for "g.deleted = 0 and grplce = 0"
		   clause to run in performant manner
		 */
		if (count < 5000 && germplasmSearchRequestDTO.noFiltersSpecified()) {
			final SQLQuery sqlQuery2 = this.getSession().createSQLQuery(this.getCountFilteredGermplasmQuery((germplasmSearchRequestDTO)));
			return ((BigInteger) sqlQuery2.uniqueResult()).longValue();
		}
		return count;

	}

	/*
	 In DBs with millions of germplasm, the clause to count non-deleted germplasm (g.deleted = 0 and grplce = 0) runs very slowly
	 on unfiltered search .
	 Workaround is for unfiltered search we "limit" count results to 5000 by getting the smaller of 5000 and
	 count of ALL germplasm (We basically avoid running the "g.deleted = 0 and grplce = 0" clause for unfiltered search)
	 */
	String buildCountGermplasmDTOsQuery(final GermplasmSearchRequestDto germplasmSearchRequestDTO) {
		if (germplasmSearchRequestDTO.noFiltersSpecified()) {
			return "SELECT LEAST(count(1), 5000) FROM germplsm ";
		} else {
			return this.getCountFilteredGermplasmQuery(germplasmSearchRequestDTO);
		}
	}

	private String getCountFilteredGermplasmQuery(final GermplasmSearchRequestDto germplasmSearchRequestDTO) {
		return "SELECT COUNT(1) FROM ( " + this.buildFilterGermplasmQuery(germplasmSearchRequestDTO) + ") as T ";
	}

	public long countGermplasmByStudy(final Integer studyDbId) {
		final String queryString = "select count(1) from (" + this.getGermplasmForStudyQuery() + ") AS T ";//

		final SQLQuery sqlQuery = this.getSession().createSQLQuery(queryString);
		sqlQuery.setParameter("studyDbId", studyDbId);

		return ((BigInteger) sqlQuery.uniqueResult()).longValue();
	}

	public String getGermplasmForStudyQuery() {
		final StringBuilder queryBuilder = new StringBuilder();
		queryBuilder.append(this.getSelectClauseForFilterGermplasm());
		queryBuilder.append(" , s.uniquename AS entryNumber ");
		queryBuilder.append(this.getMainFromGermplasmClause());
		queryBuilder.append(" INNER JOIN stock s ON s.dbxref_id = g.gid "//
			+ " INNER JOIN nd_experiment e ON e.stock_id = s.stock_id "//
			+ " WHERE g.deleted = 0 AND g.grplce = 0 "//
			+ " AND e.nd_geolocation_id = :studyDbId ");
		queryBuilder.append(" GROUP by g.gid ");
		queryBuilder.append(" ORDER BY CAST(s.uniquename as SIGNED INTEGER) ");
		return queryBuilder.toString();
	}

	public List<GermplasmDTO> getGermplasmByStudy(final Integer studyDbId, final Pageable pageable) {
		try {
			final SQLQuery sqlQuery = this.getSession().createSQLQuery(this.getGermplasmForStudyQuery());
			sqlQuery.setParameter("studyDbId", studyDbId);

			this.addScalarsForGermplasmDTO(sqlQuery).addScalar("entryNumber") //
				.setResultTransformer(new AliasToBeanResultTransformer(GermplasmDTO.class));

			addPaginationToSQLQuery(sqlQuery, pageable);

			return sqlQuery.list();
		} catch (final HibernateException e) {
			final String message = "Error with getGermplasmByStudy(studyDbId=" + studyDbId + ") " + e.getMessage();
			GermplasmDAO.LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
	}

	private SQLQuery addScalarsForGermplasmDTO(final SQLQuery sqlQuery) {
		return sqlQuery.addScalar("germplasmDbId").addScalar("gid").addScalar("accessionNumber").addScalar("acquisitionDate")
			.addScalar("countryOfOriginCode").addScalar("germplasmName").addScalar("genus").addScalar("germplasmPUI").addScalar("germplasmSeedSource")
			.addScalar("species").addScalar("speciesAuthority").addScalar("subtaxa").addScalar("subtaxaAuthority")
			.addScalar("instituteCode").addScalar("instituteName").addScalar("germplasmOrigin").addScalar("commonCropName")
			.addScalar("breedingMethodDbId");
	}

	public List<Germplasm> getExistingCrosses(final Integer femaleParent, final List<Integer> maleParentIds,
		final Optional<Integer> gid) {
		try {
			final StringBuilder builder = this.buildGetExistingCrossesQueryString(maleParentIds, gid);
			final SQLQuery sqlQuery = this.getSession().createSQLQuery(builder.toString());
			sqlQuery.setParameterList("maleParentIds", maleParentIds);
			sqlQuery.setParameter("femaleParentId", femaleParent);
			gid.ifPresent(germplasmId -> sqlQuery.setParameter("gid", germplasmId));
			sqlQuery.addScalar("gid");
			sqlQuery.addScalar("nval");

			final List<Germplasm> existingCrosses = new ArrayList<>();
			final List<Object[]> results = sqlQuery.list();
			for (final Object[] result : results) {
				final Germplasm existingCross = new Germplasm();
				existingCross.setGid((Integer) result[0]);
				existingCross.setGermplasmPeferredName((String) result[1]);
				existingCrosses.add(existingCross);
			}
			return existingCrosses;
		} catch (final HibernateException e) {
			final String message = "Error with getExistingCrosses" + e.getMessage();
			GermplasmDAO.LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
	}

	public List<Germplasm> getGermplasmByGUIDs(final List<String> guids) {
		if (guids == null || guids.isEmpty()) {
			return new ArrayList<>();
		}
		try {
			final Criteria criteria = this.getSession().createCriteria(Germplasm.class);
			criteria.add(Restrictions.in("germplasmUUID", guids));
			criteria.add(Restrictions.eq("deleted", Boolean.FALSE));
			return criteria.list();
		} catch (final HibernateException e) {
			GermplasmDAO.LOG.error(e.getMessage(), e);
			throw new MiddlewareQueryException(
				this.getLogExceptionMessage("getGermplasmByGUIDs", "guids", "", e.getMessage(),
					"Germplasm"), e);
		}
	}

	public long countGermplasmMatches(final GermplasmMatchRequestDto germplasmMatchRequestDto) {
		final StringBuilder queryBuilder =
			new StringBuilder(" select count(1) from germplsm g ");
		this.addGermplasmMatchesFilter(new SqlQueryParamBuilder(queryBuilder), germplasmMatchRequestDto);
		final SQLQuery sqlQuery = this.getSession().createSQLQuery(queryBuilder.toString());
		this.addGermplasmMatchesFilter(new SqlQueryParamBuilder(sqlQuery), germplasmMatchRequestDto);
		try {
			return ((BigInteger) sqlQuery.uniqueResult()).longValue();
		} catch (final HibernateException e) {
			final String message = "Error with countGermplasmMatches " + e.getMessage();
			GermplasmDAO.LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
	}

	private void addScalarsToFindGermplasmMatchesQuery(final SQLQuery sqlQuery) {
		sqlQuery.addScalar("gid").addScalar("germplasmUUID").addScalar("preferredName").addScalar("germplasmPUI").addScalar("creationDate").addScalar("reference")
			.addScalar("breedingLocationId")
			.addScalar("breedingLocation").addScalar("breedingMethodId").addScalar("breedingMethod")
			.addScalar("isGroupedLine", new BooleanType())
			.addScalar("groupId").addScalar("gpid1").addScalar("gpid2").addScalar("numberOfProgenitors").addScalar("createdByUserId");
	}

	public List<GermplasmDto> findGermplasmMatches(final GermplasmMatchRequestDto germplasmMatchRequestDto, final Pageable pageable) {
		final StringBuilder queryBuilder =
			new StringBuilder(FIND_GERMPLASM_MATCHES_MAIN_QUERY);
		this.addGermplasmMatchesFilter(new SqlQueryParamBuilder(queryBuilder), germplasmMatchRequestDto);
		final SQLQuery sqlQuery = this.getSession().createSQLQuery(queryBuilder.toString());
		this.addScalarsToFindGermplasmMatchesQuery(sqlQuery);
		this.addGermplasmMatchesFilter(new SqlQueryParamBuilder(sqlQuery), germplasmMatchRequestDto);

		if (pageable != null) {
			addPaginationToSQLQuery(sqlQuery, pageable);
		}

		sqlQuery.setResultTransformer(Transformers.aliasToBean(GermplasmDto.class));

		try {
			return sqlQuery.list();
		} catch (final HibernateException e) {
			final String message = "Error with findGermplasmMatches" + e.getMessage();
			GermplasmDAO.LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
	}

	private void addGermplasmMatchesFilter(final SqlQueryParamBuilder sqlQueryParamBuilder,
		final GermplasmMatchRequestDto germplasmMatchRequestDto) {
		sqlQueryParamBuilder.append(" where g.deleted = 0 ");
		if (!CollectionUtils.isEmpty(germplasmMatchRequestDto.getGermplasmPUIs()) && !CollectionUtils
			.isEmpty(germplasmMatchRequestDto.getNames())) {
			sqlQueryParamBuilder.append(" and (").append(" g.gid in (select gid from names n where n.nval in (:nameList) and n.nstat <> 9)")
				.append(" or ")
				.append(" pui.nval in (:puiList) ").append(") ");
			sqlQueryParamBuilder.setParameterList("puiList", germplasmMatchRequestDto.getGermplasmPUIs());
			sqlQueryParamBuilder.setParameterList("nameList", germplasmMatchRequestDto.getNames());
		} else {
			if (!CollectionUtils.isEmpty(germplasmMatchRequestDto.getGermplasmPUIs())) {
				sqlQueryParamBuilder.append(" and ").append(" pui.nval in (:puiList) ");
				sqlQueryParamBuilder.setParameterList("puiList", germplasmMatchRequestDto.getGermplasmPUIs());
			}
			if (!CollectionUtils.isEmpty(germplasmMatchRequestDto.getNames())) {
				sqlQueryParamBuilder.append(" and ")
					.append(" g.gid in (select gid from names n where n.nval in (:nameList) and n.nstat <> 9)");
				sqlQueryParamBuilder.setParameterList("nameList", germplasmMatchRequestDto.getNames());
			}
		}
	}

	StringBuilder buildGetExistingCrossesQueryString(final List<Integer> maleParentIds, final Optional<Integer> gid) {
		final StringBuilder builder = new StringBuilder();
		builder.append("SELECT g.gid as gid, (select n.nval from names n where n.nstat=1 and n.gid = g.gid limit 1) as nval ");
		builder.append(" FROM germplsm g ");
		if (maleParentIds.size() > 1) {
			// For Polycrosses
			builder.append("INNER JOIN progntrs p ON p.gid = g.gid ");
			this.addCommonWhereConditions(gid, builder);
			// Check if all the pids in progenitors table are in the maleParentIds list
			builder.append("AND p.pid IN (:maleParentIds) ");
			builder.append("AND g.gpid2 IN ( ");
			// Create a "temporary table" that contains all the specified male parents and return the one that's not in the progenitors table
			builder.append("SELECT parentId FROM ( ");
			builder.append(maleParentIds.stream().map(maleParent -> " (SELECT " + maleParent + " AS parentId) ")
				.collect(Collectors.joining("UNION ALL ")));
			builder.append(") AS maleParentGIDs ");
			builder.append("WHERE parentId not IN (select prog.pid from progntrs prog where prog.gid = g.gid)) ");
			// Make sure that the number of progenitors are the same as the number of male parents specified
			builder.append("group by p.gid having count(distinct p.pid) = " + (maleParentIds.size() - 1));
		} else {
			this.addCommonWhereConditions(gid, builder);
			builder.append("AND g.gpid2 IN (:maleParentIds)");
		}
		return builder;
	}

	private void addCommonWhereConditions(final Optional<Integer> gid, final StringBuilder builder) {
		builder.append("WHERE g.deleted = 0 AND g.gpid1 = :femaleParentId ");
		// Crosses created using design crosses are saved before going back to the FB module,
		// this code excludes the pre-created germplasm from the results
		if (gid.isPresent()) {
			builder.append("AND g.gid <> :gid ");
		}
	}

	public boolean hasExistingCrosses(final Integer femaleParent, final List<Integer> maleParentIds,
		final Optional<Integer> gid) {
		try {
			final StringBuilder builder = new StringBuilder();
			builder.append("SELECT COUNT(1) FROM ( ");
			builder.append(this.buildGetExistingCrossesQueryString(maleParentIds, gid).toString());
			builder.append(") EXISTING_CROSSES");
			final SQLQuery sqlQuery = this.getSession().createSQLQuery(builder.toString());
			sqlQuery.setParameterList("maleParentIds", maleParentIds);
			sqlQuery.setParameter("femaleParentId", femaleParent);
			gid.ifPresent(germplasmId -> sqlQuery.setParameter("gid", germplasmId));

			return ((BigInteger) sqlQuery.uniqueResult()).longValue() > 0;
		} catch (final HibernateException e) {
			final String message = "Error with hasExistingCrosses" + e.getMessage();
			GermplasmDAO.LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
	}

	public GermplasmDto getGermplasmDtoByGid(final Integer gid) {
		final StringBuilder queryBuilder =
			new StringBuilder(FIND_GERMPLASM_MATCHES_MAIN_QUERY);
		queryBuilder.append(" WHERE ").append(" g.gid in (:gids) and g.deleted = 0 AND g.grplce = 0");
		final SQLQuery sqlQuery = this.getSession().createSQLQuery(queryBuilder.toString());
		this.addScalarsToFindGermplasmMatchesQuery(sqlQuery);
		sqlQuery.setParameterList("gids", Collections.singletonList(gid));
		sqlQuery.setResultTransformer(Transformers.aliasToBean(GermplasmDto.class));
		try {
			final Object result = sqlQuery.uniqueResult();
			return (result == null) ? null : (GermplasmDto) result;
		} catch (final HibernateException e) {
			final String message = "Error with getGermplasmDtoByGid" + e.getMessage();
			GermplasmDAO.LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
	}

	public List<GermplasmDto> getGermplasmDtoByGids(final List<Integer> gids) {
		final StringBuilder queryBuilder =
			new StringBuilder(FIND_GERMPLASM_MATCHES_MAIN_QUERY);
		queryBuilder.append(" WHERE ").append(" g.gid in (:gids) and g.deleted = 0 AND g.grplce = 0");
		final SQLQuery sqlQuery = this.getSession().createSQLQuery(queryBuilder.toString());
		this.addScalarsToFindGermplasmMatchesQuery(sqlQuery);
		sqlQuery.setParameterList("gids", gids);
		sqlQuery.setResultTransformer(Transformers.aliasToBean(GermplasmDto.class));
		try {
			return sqlQuery.list();
		} catch (final HibernateException e) {
			final String message = "Error with getGermplasmDtoByGid" + e.getMessage();
			GermplasmDAO.LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
	}

	public List<GermplasmDto> getManagementNeighbors(final Integer gid) {
		final StringBuilder queryBuilder =
			new StringBuilder(FIND_GERMPLASM_MATCHES_MAIN_QUERY);
		queryBuilder.append(" WHERE g.mgid = :gid AND  g.deleted = 0  and g.grplce = 0 ORDER BY g.gid");
		final SQLQuery sqlQuery = this.getSession().createSQLQuery(queryBuilder.toString());
		this.addScalarsToFindGermplasmMatchesQuery(sqlQuery);
		sqlQuery.setParameter("gid", gid);
		sqlQuery.setResultTransformer(Transformers.aliasToBean(GermplasmDto.class));
		try {
			return sqlQuery.list();
		} catch (final HibernateException e) {
			final String message = "Error with getManagementNeighbors" + e.getMessage();
			GermplasmDAO.LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
	}

	public List<GermplasmDto> getGroupRelatives(final Integer gid) {
		final StringBuilder queryBuilder =
			new StringBuilder(FIND_GERMPLASM_MATCHES_MAIN_QUERY);
		queryBuilder.append(" JOIN germplsm g2 ON g.gpid1 = g2.gpid1 WHERE ").append("g.gnpgs = -1 AND g.gid <> :gid AND g2.gid = :gid "
			+ "AND g.gpid1 != 0 AND  g.deleted = 0  AND g.grplce = 0");
		final SQLQuery sqlQuery = this.getSession().createSQLQuery(queryBuilder.toString());
		this.addScalarsToFindGermplasmMatchesQuery(sqlQuery);
		sqlQuery.setParameter("gid", gid);
		sqlQuery.setResultTransformer(Transformers.aliasToBean(GermplasmDto.class));
		try {
			return sqlQuery.list();
		} catch (final HibernateException e) {
			final String message = "Error with getGroupRelatives" + e.getMessage();
			GermplasmDAO.LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
	}

	public long countGermplasmDerivativeProgeny(final Integer gid) {
		try {
			final StringBuilder queryBuilder = new StringBuilder();
			queryBuilder.append("SELECT COUNT(DISTINCT g.gid) FROM germplsm g ");
			queryBuilder.append("WHERE g.gnpgs = -1 AND g.gpid2 = :gid AND g.deleted = 0  and g.grplce = 0");
			final SQLQuery sqlQuery = this.getSession().createSQLQuery(queryBuilder.toString());
			sqlQuery.setParameter("gid", gid);
			return ((BigInteger) sqlQuery.uniqueResult()).longValue();
		} catch (final HibernateException e) {
			final String errorMessage = "Error with countGermplasmDerivativeProgeny(gid=" + gid + ") " + e.getMessage();
			GermplasmDAO.LOG.error(errorMessage, e);
			throw new MiddlewareQueryException(errorMessage, e);
		}
	}

	/**
	 * Only returns IDs of germplasm with progeny (Germplasm Id is assigned to other germplasm's gpid1, gpid2 OR progntrs.pid) with Method Type
	 * DERIVATIVE AND MAINTENANCE
	 *
	 * @param gids - gids to be checked for descendants
	 * @return - gids with derivative or maintenance descendants
	 */
	public Set<Integer> getGidsOfGermplasmWithDerivativeOrMaintenanceDescendants(final Set<Integer> gids) {

		final List<String> mtypes = Arrays.asList(MethodType.DERIVATIVE.getCode(), MethodType.MAINTENANCE.getCode());
		try {
			if (!CollectionUtils.isEmpty(gids)) {
				final SQLQuery query = this.getSession().createSQLQuery("SELECT g.gid as gid FROM germplsm g "
					+ "WHERE (EXISTS "
					+ " (SELECT 1 FROM germplsm descendant "
					+ " INNER JOIN methods mtype ON mtype.mid = descendant.methn"
					+ " WHERE (g.gid = descendant.gpid1 OR g.gid = descendant.gpid2) "
					+ " AND mtype.mtype IN (:mtypes) AND descendant.deleted = 0) "
					+ ") "
					+ "AND g.gid IN (:gids) AND  g.deleted = 0 AND g.grplce = 0 ");
				query.addScalar("gid", new IntegerType());
				query.setParameterList("gids", gids);
				query.setParameterList("mtypes", mtypes);
				return Sets.newHashSet(query.list());
			}
			return Sets.newHashSet();
		} catch (final HibernateException e) {
			final String errorMessage =
				"Error with getGidsOfGermplasmWithDerivativeOrMaintenanceDescendants(gids=" + gids + " mids=" + mtypes + e.getMessage();
			GermplasmDAO.LOG.error(errorMessage, e);
			throw new MiddlewareQueryException(errorMessage, e);
		}
	}

	public long countNonDeletedGermplasm() {
		try {
			final Criteria criteria = this.getSession().createCriteria(this.getPersistentClass());
			criteria.add(Restrictions.eq("deleted", false));
			criteria.add(Restrictions.eq("grplce", 0));
			criteria.setProjection(Projections.rowCount());
			return (Long) criteria.uniqueResult();
		} catch (final HibernateException e) {
			throw new MiddlewareQueryException("Error in countAll(): " + e.getMessage(), e);
		}
	}

	public List<Integer> getChildren(final Integer gid) {
		try {
			final SQLQuery sqlQuery = this.getSession().createSQLQuery("select gid "
				+ "from germplsm "
				+ "       inner join methods m on germplsm.methn = m.mid "
				+ "where (((mtype = " + GEN + " and (gpid1 = :parent or gpid2 = :parent))) "
				+ "  or (mtype in (" + DER_MAN
				+ ") and (gpid2 = :parent or (gpid2 = 0 and gpid1 = :parent)))) and deleted = 0 AND grplce = 0 ");
			sqlQuery.setParameter("parent", gid);
			return sqlQuery.list();
		} catch (final HibernateException e) {
			final String message = "Error with getChildren" + e.getMessage();
			GermplasmDAO.LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
	}

	/**
	 * Use this update when you are sure there is no need to traverse the progeny tree.
	 *
	 * @param oldGroupSource
	 * @param newGroupSource
	 */
	public void updateGroupSource(final Integer oldGroupSource, final Integer newGroupSource) {
		final String sql =
			"UPDATE germplsm SET gpid1 = :newGroupSource, modified_date = CURRENT_TIMESTAMP, modified_by = :userId WHERE gpid1 = :oldGroupSource and "
				+ " deleted = 0 and methn in (select mid from methods where mtype in (" + DER_MAN + "))";
		final SQLQuery sqlQuery = this.getSession().createSQLQuery(sql);
		sqlQuery.setParameter("newGroupSource", newGroupSource);
		sqlQuery.setParameter("oldGroupSource", oldGroupSource);
		sqlQuery.setParameter("userId", ContextHolder.getLoggedInUserId());
		try {
			final int updatedRows = sqlQuery.executeUpdate();
			GermplasmDAO.LOG.debug("updateGroupSource has updated the group source for " + updatedRows + " rows");
		} catch (final HibernateException e) {
			final String message = "Error with updateGroupSource" + e.getMessage();
			GermplasmDAO.LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
	}

	public void updateGroupSourceTraversingProgeny(final Integer parentNode, final Integer newGroupSource, final int maxRecursiveQueries) {
		final List<Integer> derivativeNodes = this.findDerivativeNodes(Collections.singletonList(parentNode), 0, maxRecursiveQueries);
		if (!derivativeNodes.isEmpty()) {
			this.updateGroupSource(derivativeNodes, newGroupSource);
		}
	}

	public boolean isNewParentANodeDescendant(final Set<Integer> newParents, final Integer node, final int maxRecursiveQueries) {
		return this.isNewParentANodeDescendant(new HashSet<>(), newParents, node, 0, maxRecursiveQueries);
	}

	private boolean isNewParentANodeDescendant(final Set<Integer> path, final Set<Integer> parents, final Integer node, int level,
		final int maxRecursiveQueries) {

		parents.remove(0);
		parents.removeAll(path);
		if (parents.contains(node)) {
			return true;
		}
		if (parents.isEmpty()) {
			return false;
		}
		path.addAll(parents);

		if (++level > maxRecursiveQueries) {
			throw new MiddlewareRequestException("", "germplasm.update.germplasm.max.recursive.queries.reached", "");
		}

		try {
			final SQLQuery sqlQuery = this.getSession()
				.createSQLQuery("select g.gpid1, g.gpid2 from germplsm g where g.deleted = 0 and g.grplce = 0 and g.gid in :childrenNodes");
			sqlQuery.setParameterList("childrenNodes", parents);
			final List<Object[]> results = sqlQuery.list();
			final Set<Integer> newParents = new HashSet<>();
			for (final Object[] r : results) {
				newParents.add((Integer) r[0]);
				newParents.add((Integer) r[1]);
			}
			return this.isNewParentANodeDescendant(path, newParents, node, level, maxRecursiveQueries);
		} catch (final HibernateException e) {
			final String message = "Error with isNewParentANodeDescendant" + e.getMessage();
			GermplasmDAO.LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
	}

	private List<Integer> findDerivativeNodes(final List<Integer> parentNodes, int level, final int maxRecursiveQueries) {
		if (++level > maxRecursiveQueries) {
			throw new MiddlewareRequestException("", "germplasm.update.germplasm.max.recursive.queries.reached", "");
		}
		final List<Integer> result = new ArrayList<>();
		final List<Integer> immediateDerivativeProgeny = this.getImmediateDerivativeProgeny(parentNodes);
		if (!immediateDerivativeProgeny.isEmpty()) {
			result.addAll(immediateDerivativeProgeny);
			result.addAll(this.findDerivativeNodes(immediateDerivativeProgeny, level, maxRecursiveQueries));
		}
		return result;
	}

	private List<Integer> getImmediateDerivativeProgeny(final List<Integer> parentNodes) {
		try {
			final SQLQuery sqlQuery = this.getSession().createSQLQuery("select g.gid "
				+ "from germplsm g "
				+ "       inner join methods m on g.methn = m.mid "
				+ "where g.deleted = 0 and m.mtype in (" + DER_MAN + ") and g.gpid2 in :parentNodes");
			sqlQuery.setParameterList("parentNodes", parentNodes);
			return sqlQuery.list();
		} catch (final HibernateException e) {
			final String message = "Error with getImmediateDerivativeProgeny" + e.getMessage();
			GermplasmDAO.LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
	}

	private void updateGroupSource(final List<Integer> gids, final Integer newGroupSource) {
		final String sql =
			"UPDATE germplsm SET gpid1 = :newGroupSource, modified_date = CURRENT_TIMESTAMP, modified_by = :userId WHERE gid in (:gids)";
		final SQLQuery sqlQuery = this.getSession().createSQLQuery(sql);
		sqlQuery.setParameter("newGroupSource", newGroupSource);
		sqlQuery.setParameterList("gids", gids);
		sqlQuery.setParameter("userId", ContextHolder.getLoggedInUserId());
		try {
			GermplasmDAO.LOG.debug("updateGroupSource is trying to set the group source for the gids: " + gids);
			final Integer updatedRows = sqlQuery.executeUpdate();
			GermplasmDAO.LOG.debug("updateGroupSource has modified the group source for " + updatedRows + " germplasm");
		} catch (final HibernateException e) {
			final String message = "Error with updateGroupSource" + e.getMessage();
			GermplasmDAO.LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
	}

	/**
	 * Given a list of Germplasms return the Variables related with type GERMPLASM_PASSPORT, GERMPLASM_ATTRIBUTE.
	 * The programUUID is used to return the expected range and alias of the program if it exists.
	 *
	 * @param List        of gids
	 * @param programUUID program's unique id
	 * @return List of Variable or empty list if none found
	 */
	public List<Variable> getGermplasmAttributeVariables(final List<Integer> gids, final String programUUID) {
		try {
			final SQLQuery sqlQuery = this.getSession().createSQLQuery("select "
				+ " v.cvterm_id AS " + GermplasmDAO.VARIABLE_ID + ", "  //
				+ " v.name AS " + GermplasmDAO.VARIABLE_NAME + ", "  //
				+ " v.definition AS " + GermplasmDAO.VARIABLE_DEFINITION + ", "  //
				+ " vmr.mid AS " + GermplasmDAO.METHOD_ID + ", "  //
				+ " vmr.mn AS " + GermplasmDAO.METHOD_NAME + ", "  //
				+ " vmr.md AS " + GermplasmDAO.METHOD_DEFINITION + ", "  //
				+ " vpr.pid AS " + GermplasmDAO.PROPERTY_ID + ", "  //
				+ " vpr.pn AS " + GermplasmDAO.PROPERTY_NAME + ", "  //
				+ " vpr.pd AS " + GermplasmDAO.PROPERTY_DEFINITION + ", "  //
				+ " vsr.sid AS " + GermplasmDAO.SCALE_ID + ", "  //
				+ " vsr.sn AS " + GermplasmDAO.SCALE_NAME + ", "  //
				+ " vsr.sd AS " + GermplasmDAO.SCALE_DEFINITION + ", "  //
				+ " vpo.alias  AS " + GermplasmDAO.VARIABLE_ALIAS + ", "  //
				+ " vpo.expected_min AS " + GermplasmDAO.VARIABLE_EXPECTED_MIN + ", "  //
				+ " vpo.expected_max AS " + GermplasmDAO.VARIABLE_EXPECTED_MAX
				+ " FROM cvterm v INNER JOIN cvtermprop cp ON cp.type_id = " + TermId.VARIABLE_TYPE.getId()
				+ " and v.cvterm_id = cp.cvterm_id " //
				+ " INNER JOIN cvterm varType on varType.name = cp.value AND varType.cvterm_id in (" //
				+ VariableType.GERMPLASM_PASSPORT.getId() + "," //
				+ VariableType.GERMPLASM_ATTRIBUTE.getId() + ") " //
				+ " left join (select mr.subject_id vid, m.cvterm_id mid, m.name mn, m.definition md from cvterm_relationship mr inner join cvterm m on m.cvterm_id = mr.object_id and mr.type_id = "
				+ TermRelationshipId.HAS_METHOD.getId() + ") vmr on vmr.vid = v.cvterm_id "
				+ " left join (select pr.subject_id vid, p.cvterm_id pid, p.name pn, p.definition pd from cvterm_relationship pr inner join cvterm p on p.cvterm_id = pr.object_id and pr.type_id = "
				+ TermRelationshipId.HAS_PROPERTY.getId() + ") vpr on vpr.vid = v.cvterm_id "
				+ " left join (select sr.subject_id vid, s.cvterm_id sid, s.name sn, s.definition sd from cvterm_relationship sr inner join cvterm s on s.cvterm_id = sr.object_id and sr.type_id = "
				+ TermRelationshipId.HAS_SCALE.getId() + ") vsr on vsr.vid = v.cvterm_id "
				+ " left join variable_overrides vpo ON vpo.cvterm_id = v.cvterm_id AND vpo.program_uuid = :" + GermplasmDAO.PROGRAM_UUID //
				+ " inner join atributs a  on a.atype = v.cvterm_id " //
				+ " WHERE "
				+ " a.gid in (:" + GermplasmDAO.GIDS + ")" //
				+ " group by v.cvterm_id");

			sqlQuery.setParameter(GermplasmDAO.PROGRAM_UUID, programUUID);
			sqlQuery.setParameterList(GermplasmDAO.GIDS, gids);
			sqlQuery.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
			sqlQuery.addScalar(GermplasmDAO.VARIABLE_ID);
			sqlQuery.addScalar(GermplasmDAO.VARIABLE_NAME);
			sqlQuery.addScalar(GermplasmDAO.VARIABLE_DEFINITION);
			sqlQuery.addScalar(GermplasmDAO.METHOD_ID);
			sqlQuery.addScalar(GermplasmDAO.METHOD_NAME);
			sqlQuery.addScalar(GermplasmDAO.METHOD_DEFINITION);
			sqlQuery.addScalar(GermplasmDAO.PROPERTY_ID);
			sqlQuery.addScalar(GermplasmDAO.PROPERTY_NAME);
			sqlQuery.addScalar(GermplasmDAO.PROPERTY_DEFINITION);
			sqlQuery.addScalar(GermplasmDAO.SCALE_ID);
			sqlQuery.addScalar(GermplasmDAO.SCALE_NAME);
			sqlQuery.addScalar(GermplasmDAO.SCALE_DEFINITION);
			sqlQuery.addScalar(GermplasmDAO.VARIABLE_ALIAS);
			sqlQuery.addScalar(GermplasmDAO.VARIABLE_EXPECTED_MIN);
			sqlQuery.addScalar(GermplasmDAO.VARIABLE_EXPECTED_MAX);

			final List<Map<String, Object>> queryResults = (List<Map<String, Object>>) sqlQuery.list();
			final List<Variable> variables = new ArrayList<>();
			for (final Map<String, Object> item : queryResults) {
				final Variable variable =
					new Variable(new Term(Util.typeSafeObjectToInteger(item.get(GermplasmDAO.VARIABLE_ID)),
						(String) item.get(GermplasmDAO.VARIABLE_NAME), (String) item.get(GermplasmDAO.VARIABLE_DEFINITION)));
				variable.setMethod(new Method(
					new Term(Util.typeSafeObjectToInteger(item.get(GermplasmDAO.METHOD_ID)), (String) item.get(GermplasmDAO.METHOD_NAME),
						(String) item.get(GermplasmDAO.METHOD_DEFINITION))));
				variable.setProperty(new Property(new Term(Util.typeSafeObjectToInteger(item.get(GermplasmDAO.PROPERTY_ID)),
					(String) item.get(GermplasmDAO.PROPERTY_NAME), (String) item.get(GermplasmDAO.PROPERTY_DEFINITION))));
				variable.setScale(new Scale(
					new Term(Util.typeSafeObjectToInteger(item.get(GermplasmDAO.SCALE_ID)), (String) item.get(GermplasmDAO.SCALE_NAME),
						(String) item.get(GermplasmDAO.SCALE_DEFINITION))));

				// Alias, Expected Min Value, Expected Max Value
				final String pAlias = (String) item.get(GermplasmDAO.VARIABLE_ALIAS);
				final String pExpMin = (String) item.get(GermplasmDAO.VARIABLE_EXPECTED_MIN);
				final String pExpMax = (String) item.get(GermplasmDAO.VARIABLE_EXPECTED_MAX);

				variable.setAlias(pAlias);
				variable.setMinValue(pExpMin);
				variable.setMaxValue(pExpMax);

				variables.add(variable);
			}
			return variables;
		} catch (final HibernateException e) {
			final String message =
				"Error with getGermplasmAttributeVariables(gids=" + gids + ") : " + e.getMessage();
			GermplasmDAO.LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
	}
}
