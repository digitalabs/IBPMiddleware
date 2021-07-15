package org.generationcp.middleware.dao;

import org.generationcp.middleware.domain.workbench.PermissionDto;
import org.generationcp.middleware.domain.workbench.RoleType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.workbench.Permission;
import org.generationcp.middleware.pojos.workbench.PermissionsEnum;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PermissionDAO extends GenericDAO<Permission, Integer> {

	private static final Logger LOG = LoggerFactory.getLogger(PermissionDAO.class);

	/**
	 * Permissions will change based on if user has crop role plus any program role assigned for the same crop
	 * In this case, user will be able to access only programs associated via program role
	 * (See {@link org.generationcp.middleware.dao.ProjectDAO#GET_PROJECTS_BY_USER_ID GET_PROJECTS_BY_USER_ID}).<br>
	 * So an user with NO instance role, CROP role and PROGRAM role (i.e. program1) for that crop
	 * wants to access program2, no permissions will be loaded. <br>
	 * For program1, the sum of all crop and program permissions will be loaded.
	 */
	private static final String SQL_FILTERED_PERMISSIONS = "select " //
		+ "p.permission_id as id, " //
		+ "p.name as name, " //
		+ "p.description as description, " //
		+ "p.parent_id as parentId," //
		+ "p.workbench_sidebar_category_link_id as workbenchCategoryLinkId " //
		+ "from permission p " //
		+ "inner join role_permission rp on p.permission_id = rp.permission_id " //
		+ "inner join role r on rp.role_id = r.id " //
		+ "inner join users_roles ur on r.id = ur.role_id " //
		+ "where  (r.role_type_id = " + RoleType.INSTANCE.getId() //
		+ "  or (r.role_type_id = " + RoleType.CROP.getId() + " and ur.crop_name = :cropName " //
		/*
		 * If there are other program roles for this crop, then crop permission cannot be loaded unless second condition matches,
		 * which is that the requested program role (projectId) exists
		 */
		+ "      and (not exists( " //
		+ "                SELECT 1  " //
		+ "                FROM workbench_project p1 " //
		+ "                         INNER JOIN users_roles ur1 ON ur1.workbench_project_id = p1.project_id " //
		+ "                         INNER JOIN role r1 ON ur1.role_id = r1.id " //
		+ "                where r1.role_type_id =  " + RoleType.PROGRAM.getId() //
		+ "                  AND ur1.crop_name = ur.crop_name AND ur1.userid = ur.userid " //
		+ "                  and ur1.workbench_project_id != :projectId " //
		+ "          ) or exists( " //
		+ "                SELECT 1  " //
		+ "                FROM workbench_project p1 " //
		+ "                         INNER JOIN users_roles ur1 ON ur1.workbench_project_id = p1.project_id " //
		+ "                         INNER JOIN role r1 ON ur1.role_id = r1.id " //
		+ "                where r1.role_type_id = " + RoleType.PROGRAM.getId() //
		+ "                  AND ur1.crop_name = ur.crop_name AND ur1.userid = ur.userid " //
		+ "                  and ur1.workbench_project_id = :projectId " //
		+ "         )) " //
		+ "  ) " //
		+ "  or (r.role_type_id = "+ RoleType.PROGRAM.getId() +" and ur.crop_name = :cropName and ur.workbench_project_id = :projectId)) " //
		+ "and ur.userid = :userId and r.active = 1";

	/**
	 * IMPORTANT!!!
	 * This is HOTFIX (band-aid fix) for v18.0.2 for showing the ADD program button from dashboard (IBP-4800)
	 * for those with permissions while the crop and program context is not yet available.
	 * It will return the CROP_MANAGEMENT, MANAGE_PROGRAMS, ADD_PROGRAM permissions if the user has a role on any crop
	 * which has those permissions and has no program restriction (a program role for that crop)
	 */
	private static final String SQL_ADD_PROGRAM_PERMISSIONS = "select " //
		+ "p.permission_id as id, " //
		+ "p.name as name, " //
		+ "p.description as description, " //
		+ "p.parent_id as parentId," //
		+ "p.workbench_sidebar_category_link_id as workbenchCategoryLinkId " //
		+ "from permission p " //
		+ "inner join role_permission rp on p.permission_id = rp.permission_id " //
		+ "inner join role r on rp.role_id = r.id " //
		+ "inner join users_roles ur on r.id = ur.role_id " //
		+ "where  (r.role_type_id = " + RoleType.CROP.getId() //
		+ " and not exists( " //
		+ "      select 1  " //
		+ "      from workbench_project p1 " //
		+ "               inner join users_roles ur1 on ur1.workbench_project_id = p1.project_id " //
		+ "               inner join role r1 on ur1.role_id = r1.id " //
		+ "      where r1.role_type_id =  " + RoleType.PROGRAM.getId()  //
		+ "        and ur1.crop_name = ur.crop_name and ur1.userid = ur.userid " //
		+ " ) " //
		+ " and p.name in ('" + PermissionsEnum.CROP_MANAGEMENT.toString()  //
		+ "', '" + PermissionsEnum.MANAGE_PROGRAMS.toString()  //
		+ "', '" + PermissionsEnum.ADD_PROGRAM.toString() + "')) " //
		+ " and ur.userid = :userId and r.active = 1";

	private static final String PERMISSION_CHILDREN = "select " //
		+ "p.permission_id as id, " //
		+ "p.name as name, " //
		+ "p.description as description, " //
		+ "p.parent_id as parentId," //
		+ "p.workbench_sidebar_category_link_id as workbenchCategoryLinkId " //
		+ "from workbench.permission p " //
		+ "where  p.parent_id = :parentId ";

	public List<PermissionDto> getPermissions(final Integer userId, final String cropName, final Integer programId) {
		//FIXME. Try an user with ADMIN and MANAGE_PROGRAM_SETTINGS, Only ADMIN should be retrieved and given that MANAGE_PROGRAMS is not a row in the result, both permissions are returned
		try {
			final SQLQuery query = this.getSession().createSQLQuery(PermissionDAO.SQL_FILTERED_PERMISSIONS);
			query.setParameter("userId", userId);
			query.setParameter("cropName", cropName);
			query.setParameter("projectId", programId);
			query.addScalar("id").addScalar("name").addScalar("parentId")
				.addScalar("description").addScalar("workbenchCategoryLinkId");
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
			/**
			 * This is HOTFIX (band-aid fix only!) for v18.0.2 for showing the ADD program button from dashboard (IBP-4800)
			 * 	 for those with permissions while the crop and program context is not yet available.
			 * 	 It will inject the CROP_MANAGEMENT, MANAGE_PROGRAMS, ADD_PROGRAM permissions - if not yet added -
			 * 	 if the user has a role on any crop/program which has those permissions.
			 * 	 CAVEAT: Code below could be improved (mainly copied pattern above) with the priority of having the RIGHT behavior over clean code
 			 */
			final SQLQuery query2 = this.getSession().createSQLQuery(PermissionDAO.SQL_ADD_PROGRAM_PERMISSIONS);
			query2.setParameter("userId", userId);
			query2.addScalar("id").addScalar("name").addScalar("parentId")
				.addScalar("description").addScalar("workbenchCategoryLinkId");
			query2.setResultTransformer(Transformers.aliasToBean(PermissionDto.class));
			final List<PermissionDto> copy2 = new ArrayList<>();
			final List<PermissionDto> results2 = query2.list();
			copy2.addAll(results2);
			for (final PermissionDto permission : results2) {
				for (final PermissionDto permission1 : results2) {
					if (permission1.getId().equals((permission.getParentId()))) {
						copy2.remove(permission);
					}
				}
			}
			copy2.stream().filter(p -> !copy.contains(p)).forEach(copy::add);
			/* END OF HOTFIX */

			return copy;
		} catch (final HibernateException e) {
			final String message = "Error with getPermissions query from RoleDAO: " + e.getMessage();
			PermissionDAO.LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
	}

	public List<PermissionDto> getChildrenOfPermission(final PermissionDto permissionDto) {
		final SQLQuery query = this.getSession().createSQLQuery(PermissionDAO.PERMISSION_CHILDREN);
		query.setParameter("parentId", permissionDto.getId());
		query.addScalar("id").addScalar("name").addScalar("description")
			.addScalar("parentId").addScalar("workbenchCategoryLinkId");
		query.setResultTransformer(Transformers.aliasToBean(PermissionDto.class));
		final List<PermissionDto> results = query.list();
		return results;
	}

	public List<Permission> getPermissions(final Set<Integer> permissionIds) {
		final List<Permission> toReturn;
		try {
			final Criteria criteria = this.getSession().createCriteria(Permission.class);
			criteria.add(Restrictions.in("permissionId", permissionIds));
			criteria.addOrder(Order.asc("id"));
			toReturn = criteria.list();

		} catch (final HibernateException e) {
			final String message = "Error with getPermissions query from PermissionDAO: " + e.getMessage();
			PermissionDAO.LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
		return toReturn;
	}
}
