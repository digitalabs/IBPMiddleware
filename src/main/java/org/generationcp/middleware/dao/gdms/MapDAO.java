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
package org.generationcp.middleware.dao.gdms;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.generationcp.middleware.dao.GenericDAO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.pojos.gdms.Map;
import org.generationcp.middleware.pojos.gdms.MapDetailElement;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;

/**
 * <b>Description</b>: DAO for Map table
 * 
 * <br>
 * <br>
 * 
 * <b>Author</b>: Michael Blancaflor
 * <br>
 * <b>File Created</b>: Jul 9, 2012.
 */
public class MapDAO extends GenericDAO<Map, Integer>{
    

    @SuppressWarnings("rawtypes")
    public List<MapDetailElement> getMapDetailsByName(String nameLike, int start, int numOfRows) throws MiddlewareQueryException {
    	
    	nameLike = nameLike.toLowerCase();
    	//nameLike = nameLike.replaceAll("'", "");
    	
        SQLQuery query = getSession().createSQLQuery(Map.GET_MAP_DETAILS_BY_NAME);
        query.setString("nameLike", nameLike);
        query.setFirstResult(start);
        query.setMaxResults(numOfRows);
        
        List<MapDetailElement> maps = new ArrayList<MapDetailElement>();
        
        try {

            List results = query.list();

            for (Object o : results) {
                Object[] result = (Object[]) o;
                if (result != null) {
                    int markerCount = ((BigInteger) result[0]).intValue();
                    Float maxStartPosition = (Float) result[1];
                    String linkageGroup = (String) result[2];
                    String mapName = (String) result[3];
                    String mapType = (String) result[4];
                    
                    MapDetailElement map = new MapDetailElement(markerCount, maxStartPosition, linkageGroup, mapName, mapType);
                    maps.add(map);
                }
            }
            
            return maps;
                    
        } catch (HibernateException e) {
            throw new MiddlewareQueryException("Error with getMapDetailsByName() query from Map: " + e.getMessage(), e);
        }	
        
    }

    
    public Long countMapDetailsByName(String nameLike) throws MiddlewareQueryException {

    	nameLike = nameLike.toLowerCase();
    	//nameLike = nameLike.replaceAll("'", "");
    	
        SQLQuery query = getSession().createSQLQuery(Map.COUNT_MAP_DETAILS_BY_NAME);
        query.setString("nameLike", nameLike);
        
        try {
            BigInteger result = (BigInteger) query.uniqueResult();
            return result.longValue();
        } catch (HibernateException e) {
            throw new MiddlewareQueryException("Error with countMapDetailsByName() query from Map: " + e.getMessage(), e);
        }
    }    
    
    @SuppressWarnings("rawtypes")
    public List<MapDetailElement> getAllMapDetails(int start, int numOfRows) throws MiddlewareQueryException {
        try {
            SQLQuery query = getSession().createSQLQuery(Map.GET_MAP_DETAILS);
            query.setFirstResult(start);
            query.setMaxResults(numOfRows);
            List results = query.list();

            List<MapDetailElement> values = new ArrayList<MapDetailElement>();

            for (Object o : results) {
                Object[] result = (Object[]) o;
                if (result != null) {
                    BigInteger markerCount = (BigInteger) result[0];
                    Float max  = (Float) result[1];
                    String linkageGroup = (String) result[2];
                    String mapName2 = (String) result[3];
                    String mapType = (String) result[4];
                    MapDetailElement element = new MapDetailElement(markerCount.intValue(), max, linkageGroup, mapName2, mapType);
                    values.add(element);
                }
            }

            return values;
        } catch (HibernateException e) {
            throw new MiddlewareQueryException("Error with getAllMapDetails() query from Map & Mapping Data: " + e.getMessage(), e);
        }
    }
    
    public long countAllMapDetails() throws MiddlewareQueryException {
        try {
            SQLQuery query = getSession().createSQLQuery(Map.COUNT_MAP_DETAILS);
            BigInteger result = (BigInteger) query.uniqueResult();
            if (result != null) {
                return result.longValue();
            }
            return 0;
        } catch (HibernateException e) {
            throw new MiddlewareQueryException("Error with countAllMapDetails() query from Map & Mapping Data: " + e.getMessage(), e);
        }

    }
    
    
}