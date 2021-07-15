package org.generationcp.middleware.dao;

import org.generationcp.middleware.domain.workbench.RoleType;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.workbench.CropType;
import org.generationcp.middleware.pojos.workbench.PermissionsEnum;
import org.generationcp.middleware.pojos.workbench.UserRole;
import org.generationcp.middleware.pojos.workbench.WorkbenchUser;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UserRoleDao extends GenericDAO<UserRole, Long> {

	private static final Logger LOG = LoggerFactory.getLogger(UserRole.class);

		private static final String HAS_INSTANCE_ROLE_WITH_ADD_PROGRAM_PERMISSION_SQL = "select ur.role_id from users_roles ur " //
			+"  inner join role r on ur.role_id = r.id " //
			+"  inner join role_type type on type.role_type_id = r.role_type_id " //
			+"  inner join role_permission rp on r.id = rp.role_id " //
			+"  inner join permission p on rp.permission_id = p.permission_id " //
			+"  where (r.role_type_id = " + RoleType.INSTANCE.getId() + " and p.name in ('" + PermissionsEnum.ADMIN.toString() + "', '"+ PermissionsEnum.CROP_MANAGEMENT.toString() //
			+"','"+ PermissionsEnum.ADD_PROGRAM.toString()+"', '"+ PermissionsEnum.MANAGE_PROGRAMS+"'))" //
			+"  and ur.userid = :userId and r.active = 1";//

	/**
	 * See also {@link PermissionDAO#SQL_FILTERED_PERMISSIONS}
	 */
	private static final String GET_CROPS_WITH_ADD_PROGRAM_PERMISSION_FOR_A_CROP_ROLE_SQL = "select distinct ur.crop_name " //
		+ " from users_roles ur "//
		+ " inner join role r on ur.role_id = r.id " //
		+ " inner join role_type type on type.role_type_id = r.role_type_id " //
		+ " inner join role_permission rp on r.id = rp.role_id " //
		+ " inner join permission p on rp.permission_id = p.permission_id "//
		+ " where (r.role_type_id = " + RoleType.CROP.getId() //
		+ " and not exists( " //
		+ "      select 1  " //
		+ "      from workbench_project p1 " //
		+ "               inner join users_roles ur1 on ur1.workbench_project_id = p1.project_id " //
		+ "               inner join role r1 on ur1.role_id = r1.id " //
		+ "      where r1.role_type_id =  " + RoleType.PROGRAM.getId()  //
		+ "        and ur1.crop_name = ur.crop_name and ur1.userid = ur.userid " //
		+ " ) " //
		+ " and p.name in ('" + PermissionsEnum.CROP_MANAGEMENT.toString()
		+ "', '" + PermissionsEnum.MANAGE_PROGRAMS.toString() //
		+ "','" + PermissionsEnum.ADD_PROGRAM.toString() + "'))" //
		+ " and ur.userid = :userId and r.active = 1 "; //

	public List<UserRole> getByProgramId(final Long programId) {
		List<UserRole> toReturn;

		try {
			final Criteria criteria = this.getSession().createCriteria(UserRole.class);
			if (programId != null) {
				criteria.createAlias("workbenchProject", "workbenchProject");
				criteria.add(Restrictions.eq("workbenchProject.projectId", programId));
			}
			toReturn = criteria.list();

		} catch (final HibernateException e) {
			final String message = "Error with getByProgramId query from UserRoleDao: " + e.getMessage();
			UserRoleDao.LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
		return toReturn;
	}

	public List<WorkbenchUser> getUsersByRoleId(final Integer roleId) {
		final List<WorkbenchUser> toReturn;
		try {
			final Criteria criteria = this.getSession().createCriteria(UserRole.class);
			criteria.add(Restrictions.eq("role.id", roleId));
			criteria.setProjection(Projections.property("user"));
			toReturn = criteria.list();
		} catch (final HibernateException e) {
			final String message = "Error with getUsersByRoleId query from UserRoleDao: " + e.getMessage();
			UserRoleDao.LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
		return toReturn;
	}

	public void delete(final UserRole userRole) {

		try {
			makeTransient(userRole);

		} catch (final Exception e) {
			final String message = "Cannot delete UserRole (UserRole=" + userRole
				+ "): " + e.getMessage();
			UserRoleDao.LOG.error(message, e);
			throw new MiddlewareQueryException(message, e);
		}
	}

	public void removeUsersFromProgram(final List<Integer> workbenchUserIds, final Long projectId) {
		// Please note we are manually flushing because non hibernate based deletes and updates causes the Hibernate session to get out
		// of synch with
		// underlying database. Thus flushing to force Hibernate to synchronize with the underlying database before the delete
		// statement
		this.getSession().flush();
		final String sql = "DELETE ur FROM users_roles ur "
			+ " WHERE ur.workbench_project_id = :projectId AND ur.userid in (:workbenchUserIds)";
		final SQLQuery statement = this.getSession().createSQLQuery(sql);
		statement.setParameter("projectId", projectId);
		statement.setParameterList("workbenchUserIds", workbenchUserIds);
		statement.executeUpdate();
	}

	public boolean hasInstanceRoleWithAddProgramPermission(final int userId) {
		try {
			final SQLQuery query = this.getSession().createSQLQuery(UserRoleDao.HAS_INSTANCE_ROLE_WITH_ADD_PROGRAM_PERMISSION_SQL);
			query.setParameter("userId", userId);

			final List<Object> results = query.list();
			return results.size() == 1;

		} catch (final HibernateException e) {
			throw new MiddlewareQueryException("Error in hasInstanceRoleWithAddProgramPermission(userId=" + userId + ")", e);
		}
	}

	public Set<CropType> getCropsWithAddProgramPermissionForCropRoles(final int userId) {
		final Set<CropType> cropTypes = new HashSet<>();
		try {
			final SQLQuery query = this.getSession().createSQLQuery(GET_CROPS_WITH_ADD_PROGRAM_PERMISSION_FOR_A_CROP_ROLE_SQL);
			query.setParameter("userId", userId);
			final List<String> results = query.list();
			for (final String s : results) {
				cropTypes.add(new CropType(s));
			}
		} catch (final HibernateException e) {
			throw new MiddlewareQueryException("Error in getCropsWithAddProgramPermissionForCropRoles(userId=" + userId + ")", e);
		}
		return cropTypes;
	}
}
