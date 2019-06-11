package org.generationcp.middleware.dao;

import org.generationcp.middleware.domain.workbench.PermissionDto;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.workbench.Permission;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class PermissionDAO extends GenericDAO<Permission, Integer> {

	private static final Logger LOG = LoggerFactory.getLogger(PermissionDAO.class);

	private static String SQL_FILTERED_PERMISSIONS = "select p.permission_id as id, \n"
		+ "p.name as name, p.parent_id as parentId\n"
		+ "from permission p\n"
		+ "inner join role_permission rp on p.permission_id = rp.permission_id\n"
		+ "inner join role r on rp.role_id = r.id\n"
		+ "inner join users_roles ur on r.id = ur.role_id\n"
		+ "where  (r.role_type_id = 1\n"
		+ "  or (r.role_type_id = 2 and ur.crop_name = :cropName)\n"
		+ "  or (r.role_type_id = 3 and ur.crop_name = :cropName and ur.workbench_project_id = :projectId))\n"
		+ "and ur.userid = :userId and r.active = 1";

	public List<PermissionDto> getPermissions(final Integer userId, final String cropName, final Integer programId) {

		try {
			final SQLQuery query = this.getSession().createSQLQuery(PermissionDAO.SQL_FILTERED_PERMISSIONS);
			query.setParameter("userId", userId);
			query.setParameter("cropName", cropName);
			query.setParameter("projectId", programId);
			query.addScalar("id").addScalar("name").addScalar("parentId");
			query.setResultTransformer(Transformers.aliasToBean(PermissionDto.class));
			final List<PermissionDto> results = query.list();
			final List<PermissionDto> copy = new ArrayList<>();
			copy.addAll(results);
			for (final PermissionDto permission : results) {
				for (final PermissionDto permission1 : results) {
					if (permission1.getId().equals((permission.getParentId()))) {
						copy.remove(permission);
					}
				}
			}
			return copy;
		} catch (final HibernateException e) {
			final String message = "Error with getPermissions query from RoleDAO: " + e.getMessage();
			PermissionDAO.LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
	}

}
