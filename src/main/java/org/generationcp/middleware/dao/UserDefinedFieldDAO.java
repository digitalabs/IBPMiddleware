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

package org.generationcp.middleware.dao;

import org.apache.commons.lang3.StringUtils;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.UDTableType;
import org.generationcp.middleware.pojos.UserDefinedField;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.NonUniqueResultException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Pageable;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * DAO class for {@link UserDefinedField}.
 */
public class UserDefinedFieldDAO extends GenericDAO<UserDefinedField, Integer> {

	private static final Logger LOG = LoggerFactory.getLogger(UserDefinedFieldDAO.class);

	public UserDefinedFieldDAO(final Session session) {
		super(session);
	}

	@SuppressWarnings("unchecked")
	public List<UserDefinedField> getByFieldTableNameAndType(final String tableName, final Set<String> fieldType) {
		try {
			if (tableName != null && fieldType != null) {
				final Criteria criteria = this.getSession().createCriteria(UserDefinedField.class);
				criteria.add(Restrictions.eq("ftable", tableName));
				criteria.add(Restrictions.in("ftype", fieldType));
				criteria.addOrder(Order.asc("fname"));
				return criteria.list();
			}
		} catch (final HibernateException e) {
			throw new MiddlewareQueryException("Error with getByFieldTableNameAndType(name=" + tableName + " type= " + fieldType
				+ " ) query from UserDefinedField: " + e.getMessage(), e);
		}
		return new ArrayList<>();
	}

	public List<UserDefinedField> getByFieldTableNameAndFTypeAndFName(final String tableName, final String fieldType,
		final String fieldName) {
		try {
			if (tableName != null && fieldType != null) {
				final Criteria criteria = this.getSession().createCriteria(UserDefinedField.class);
				criteria.add(Restrictions.eq("ftable", tableName));
				criteria.add(Restrictions.eq("ftype", fieldType));
				criteria.add(Restrictions.eq("fname", fieldName));
				criteria.addOrder(Order.asc("fname"));
				return criteria.list();
			}
		} catch (final HibernateException e) {
			throw new MiddlewareQueryException(
				"Error with getByFieldTableNameAndFTypeAndFName(tableName=" + tableName + " fieldType= " + fieldType
					+ " fieldName= " + fieldName + " ) query from UserDefinedField: " + e.getMessage(), e);
		}
		return new ArrayList<>();
	}

	public List<UserDefinedField> getByCodes(final String table, final Set<String> types, final Set<String> codes) {
		try {
			final Criteria criteria = this.getSession().createCriteria(UserDefinedField.class);
			criteria.add(Restrictions.eq("ftable", table));
			criteria.add(Restrictions.in("ftype", types));

			if (!CollectionUtils.isEmpty(codes)) {
				criteria.add(Restrictions.in("fcode", codes));
			}
			criteria.addOrder(Order.asc("fname"));
			return criteria.list();
		} catch (final HibernateException e) {
			throw new MiddlewareQueryException(
				"Error with getByCodes(name=" + table + " types= " + types + " ) query from UserDefinedField: " + e.getMessage(), e);
		}
	}

	public UserDefinedField getByTableTypeAndCode(final String table, final String type, final String code) {
		try {
			if (StringUtils.isNotBlank(table) && StringUtils.isNotBlank(type) && StringUtils.isNotBlank(code)) {
				final Criteria criteria = this.getSession().createCriteria(UserDefinedField.class);
				criteria.add(Restrictions.eq("ftable", table));
				criteria.add(Restrictions.eq("ftype", type));
				criteria.add(Restrictions.eq("fcode", code));
				return (UserDefinedField) criteria.uniqueResult();
			}
		} catch (final NonUniqueResultException nonUniqueResultException) {
			final String message =
				"Multiple UDFLD records were found with fTable={}, fType={}, fCode={}. Was expecting one uniqe result only : {}";
			UserDefinedFieldDAO.LOG.error(message, table, type, code, nonUniqueResultException.getMessage());
			throw new MiddlewareQueryException(message, nonUniqueResultException);

		} catch (final HibernateException e) {
			final String message = "Error executing UserDefinedFieldDAO.getByTableTypeAndCode(fTable={}, fType={}, fCode={}) : {}";
			UserDefinedFieldDAO.LOG.error(message, table, type, code, e.getMessage());
			throw new MiddlewareQueryException(message, e);
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<UserDefinedField> getNameTypesByGIDList(final List<Integer> gidList) {
		List<UserDefinedField> returnList = new ArrayList<>();
		if (gidList != null && !gidList.isEmpty()) {
			try {
				final String sql = "SELECT DISTINCT {u.*}" + " FROM names n" + " INNER JOIN udflds u" + " WHERE n.ntype=u.fldno"
					+ " AND n.gid in (:gidList)" + " ORDER BY u.fname";
				final SQLQuery query = this.getSession().createSQLQuery(sql);
				query.addEntity("u", UserDefinedField.class);
				query.setParameterList("gidList", gidList);
				returnList = query.list();

			} catch (final HibernateException e) {
				throw new MiddlewareQueryException("Error with getNameTypesByGIDList(gidList=" + gidList + "): " + e.getMessage(), e);
			}
		}
		return returnList;
	}

	public List<org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO> searchNameTypes(final String query) {
		if (StringUtils.isBlank(query)) {
			return Collections.EMPTY_LIST;
		}
		try {
			final SQLQuery sqlQuery = this.getSession().createSQLQuery("SELECT " //
					+ "   u.fcode AS code," //
					+ "   u.fldno AS id," //
					+ "   u.fname AS name" //
					+ " FROM  udflds u " //
					+ " WHERE u.ftable = '" + UDTableType.NAMES_NAME.getTable() + "'" //
					+ "   and u.ftype = '" + UDTableType.NAMES_NAME.getType() + "'"
					+ "   and (u.fname like :fname or u.fcode like :fcode)"
					+ " LIMIT 100 ");
			sqlQuery.setParameter("fname", '%' + query + '%');
			sqlQuery.setParameter("fcode", '%' + query + '%');
			sqlQuery.addScalar("code");
			sqlQuery.addScalar("id");
			sqlQuery.addScalar("name");
			sqlQuery.setResultTransformer(Transformers.aliasToBean(org.generationcp.middleware.api.nametype.GermplasmNameTypeDTO.class));

			return sqlQuery.list();
		} catch (final HibernateException e) {
			throw new MiddlewareQueryException("Error with searchNameTypes(query=" + query + "): " + e.getMessage(), e);
		}
	}

	public long countAllNameTypes() {
		final Criteria criteria = this.getSession().createCriteria(UserDefinedField.class);
		criteria.setProjection(Projections.rowCount());
		criteria.add(Restrictions.eq("ftable", UDTableType.NAMES_NAME.getTable()));
		criteria.add(Restrictions.eq("ftype", UDTableType.NAMES_NAME.getType()));
		criteria.addOrder(Order.asc("fname"));
		return (Long) criteria.uniqueResult();
	}

	public List<UserDefinedField> getNameTypes(final Pageable pageable) {
		try {

			final Criteria criteria = this.getSession().createCriteria(UserDefinedField.class);
			criteria.add(Restrictions.eq("ftable", UDTableType.NAMES_NAME.getTable()));
			criteria.add(Restrictions.eq("ftype", UDTableType.NAMES_NAME.getType()));
			
			if (pageable != null && pageable.getSort() != null) {
				addOrder(criteria, pageable);
			} else {
				criteria.addOrder(Order.asc("fname"));
			}

			addPagination(criteria, pageable);
			return criteria.list();
		} catch (final HibernateException e) {
			final String message = "Error executing UserDefinedFieldDAO.getNameTypes(pageable={}) : {}";
			UserDefinedFieldDAO.LOG.error(message, pageable,  e.getMessage());
			throw new MiddlewareQueryException(message, e);
		}
	}

	public List<UserDefinedField> getByName(final String table, final Set<String> types, final String name) {
		try {
			final Criteria criteria = this.getSession().createCriteria(UserDefinedField.class);
			criteria.add(Restrictions.eq("ftable", table));
			criteria.add(Restrictions.in("ftype", types));
			criteria.add(Restrictions.eq("fname", name));
			criteria.addOrder(Order.asc("fname"));
			return criteria.list();
		} catch (final HibernateException e) {
			final String message = "Error executing UserDefinedFieldDAO.getByName(fTable={}, fType={}, fname={}) : {}";
			UserDefinedFieldDAO.LOG.error(message, table, types, name, e.getMessage());
			throw new MiddlewareQueryException(message, e);
		}
	}

}
