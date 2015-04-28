package org.generationcp.middleware.dao;

import org.generationcp.middleware.domain.gms.GermplasmListType;
import org.generationcp.middleware.domain.oms.TermId;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.GermplasmList;
import org.generationcp.middleware.pojos.ListDataProject;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import java.util.ArrayList;
import java.util.List;

public class ListDataProjectDAO extends GenericDAO<ListDataProject, Integer> {

	public void deleteByListId(int listId) throws MiddlewareQueryException {
		try {
			this.flush();
			
			SQLQuery statement = getSession().createSQLQuery("delete ldp " +
	                   "from listdata_project ldp " + 
					   "where ldp.list_id = " + listId);
			statement.executeUpdate();	   
			
	        this.flush();
	        this.clear();
	
		} catch(HibernateException e) {
			logAndThrowException("Error in deleteByListId=" + listId + " in ListDataProjectDAO: " + e.getMessage(), e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public List<ListDataProject> getByListId(int listId) throws MiddlewareQueryException {
    	List<ListDataProject> list = new ArrayList<ListDataProject>();
        try {
            Criteria criteria = getSession().createCriteria(ListDataProject.class);
            criteria.add(Restrictions.eq("list", new GermplasmList(listId)));
            criteria.addOrder(Order.asc("entryId"));
            return criteria.list();

        } catch (HibernateException e) {
            logAndThrowException("Error with getByListId(listId=" + listId + ") query from ListDataProjectDAO: "
                    + e.getMessage(), e);
        }
        return list;    	
	}

	@SuppressWarnings("unchecked")
	public ListDataProject getByListIdAndEntryNo(int listId,int entryNo) throws MiddlewareQueryException {
		ListDataProject result = null;

		try {
			Criteria criteria = getSession().createCriteria(ListDataProject.class);
			criteria.add(Restrictions.eq("list", new GermplasmList(listId)));
			criteria.add(Restrictions.eq("entryId",entryNo));
			criteria.addOrder(Order.asc("entryId"));
			result = (ListDataProject) criteria.uniqueResult();

		} catch (HibernateException e) {
			logAndThrowException("Error with getByListIdAndEntryNo(listId=" + listId + ") query from ListDataProjectDAO: "
					+ e.getMessage(), e);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public ListDataProject getByStudy(int studyId,GermplasmListType listType,int plotNo) throws MiddlewareQueryException {
		try {

			String queryStr = "SELECT ldp.*"
					+ " FROM nd_experimentprop nep,"
					+ "  nd_experiment_stock nes, stock s, listdata_project ldp,"
					+ "  listnms l, nd_experiment_project p, project_relationship pr"
					+ " WHERE nep.type_id IN (:PLOT_NO_TERM_IDS)"
					+ "      AND nep.nd_experiment_id = nes.nd_experiment_id"
					+ "      AND nes.stock_id = s.stock_id"
					+ "      AND s.uniquename = ldp.entry_id"
					+ "      AND s.dbxref_id = ldp.germplasm_id"
					+ "      AND l.listid = ldp.list_id"
					+ "      AND nep.nd_experiment_id = p.nd_experiment_id"
					+ "      AND p.project_id = pr.subject_project_id"
					+ "      AND pr.object_project_id = l.projectid"
					+ "      AND l.listtype = :LIST_TYPE"
					+ "      AND nep.VALUE = :PLOT_NO"
					+ "      AND l.projectid = :STUDY_ID";


			SQLQuery query = getSession().createSQLQuery(queryStr);
			query.addEntity("ldp",ListDataProject.class);
			query.setParameter("LIST_TYPE", listType.name());
			query.setParameter("STUDY_ID", studyId);
			query.setParameter("PLOT_NO",plotNo);
			query.setParameterList("PLOT_NO_TERM_IDS",
					new Integer[] { TermId.PLOT_NO.getId(), TermId.PLOT_NNO.getId() });

			List resultList = query.list();
			if (!resultList.isEmpty()) {
				return (ListDataProject) resultList.get(0);
			}

		} catch(HibernateException e) {
			logAndThrowException("Error in getStudy=" + studyId + " in ListDataProjectDAO: " + e.getMessage(), e);
		}

		return null;

	}


	public void deleteByListIdWithList(int listId) throws MiddlewareQueryException {
		try {
			this.flush();
			
			SQLQuery statement = getSession().createSQLQuery("delete ldp, lst " +
	                   "from listdata_project ldp, listnms lst " + 
					   "where ldp.list_id = lst.listid and ldp.list_id = " + listId);
			statement.executeUpdate();	   
			
	        this.flush();
	        this.clear();
	
		} catch(HibernateException e) {
			logAndThrowException("Error in deleteByListId=" + listId + " in ListDataProjectDAO: " + e.getMessage(), e);
		}
	}
	
    public long countByListId(Integer id) throws MiddlewareQueryException {
        try {
        	if (id != null){
	            Criteria criteria = getSession().createCriteria(ListDataProject.class);
	            criteria.createAlias("list", "l");
	            criteria.add(Restrictions.eq("l.id", id));
	            criteria.setProjection(Projections.rowCount());
	            return ((Long) criteria.uniqueResult()).longValue(); //count
        	}
        } catch (HibernateException e) {
            logAndThrowException("Error with countByListId(id=" + id + ") query from ListDataProject " + e.getMessage(), e);
        }
        return 0;
    }
	
}
