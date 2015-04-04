package org.generationcp.middleware.manager.ontology;

import org.generationcp.middleware.domain.oms.*;
import org.generationcp.middleware.exceptions.MiddlewareException;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.manager.DataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyMethodDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyPropertyDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyScaleDataManager;
import org.generationcp.middleware.manager.ontology.api.OntologyVariableDataManager;
import org.generationcp.middleware.pojos.dms.DmsProject;
import org.generationcp.middleware.pojos.dms.ProgramFavorite;
import org.generationcp.middleware.pojos.oms.CVTerm;
import org.generationcp.middleware.pojos.oms.CVTermAlias;
import org.generationcp.middleware.pojos.oms.CVTermProperty;
import org.generationcp.middleware.pojos.oms.CVTermRelationship;
import org.generationcp.middleware.util.ISO8601DateParser;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;

import java.math.BigInteger;
import java.util.*;

public class OntologyVariableDataManagerImpl extends DataManager implements OntologyVariableDataManager {

    private static final String PROGRAM_DOES_NOT_EXIST = "Program does not exist";
    private static final String VARIABLE_DOES_NOT_EXIST = "Variable does not exist";
    private static final String TERM_IS_NOT_VARIABLE = "Term is not Variable";
    private static final String VARIABLE_EXIST_WITH_SAME_NAME = "Variable exist with same name";


    private final OntologyMethodDataManager methodDataManager;
    private final OntologyPropertyDataManager propertyDataManager;
    private final OntologyScaleDataManager scaleDataManager;

    public OntologyVariableDataManagerImpl(OntologyMethodDataManager methodDataManager,
                                           OntologyPropertyDataManager propertyDataManager,
                                           OntologyScaleDataManager scaleDataManager,
                                           HibernateSessionProvider sessionProvider) {
        super(sessionProvider);
        this.methodDataManager = methodDataManager;
        this.propertyDataManager = propertyDataManager;
        this.scaleDataManager = scaleDataManager;
    }


    @Override
    public List<OntologyVariableSummary> getWithFilter(Integer programId, Boolean favorites, Integer methodId, Integer propertyId, Integer scaleId) throws MiddlewareQueryException {

        String filterClause = "";

        if(!Objects.equals(methodId, null)) {
            filterClause += " and vmr.mid = :methodId ";
        }

        if(!Objects.equals(propertyId, null)) {
            filterClause += " and vpr.pid = :propertyId ";
        }

        if(!Objects.equals(scaleId, null)) {
            filterClause += " and vsr.sid = :scaleId ";
        }

        if(!Objects.equals(favorites, null)){
            if(favorites){
                filterClause += "  and pf.id is not null ";
            } else {
                filterClause += "  and pf.id is null ";
            }
        }

        Map<Integer, OntologyVariableSummary> map = new HashMap<>();

        try {
            SQLQuery query = getActiveSession().createSQLQuery("select v.cvterm_id vid, v.name vn, v.definition vd, a.name va, vmr.mid, vmr.mn, vmr.md, vpr.pid, vpr.pn, vpr.pd, vsr.sid, vsr.sn, vsr.sd, pf.id fid from cvterm v " +
                    "left join cvterm_alias a on a.cvterm_id = v.cvterm_id and a.program_id = :programId " +
                    "left join (select mr.subject_id vid, m.cvterm_id mid, m.name mn, m.definition md from cvterm_relationship mr inner join cvterm m on m.cvterm_id = mr.object_id and mr.type_id = 1210) vmr on vmr.vid = v.cvterm_id " +
                    "left join (select pr.subject_id vid, p.cvterm_id pid, p.name pn, p.definition pd from cvterm_relationship pr inner join cvterm p on p.cvterm_id = pr.object_id and pr.type_id = 1200) vpr on vpr.vid = v.cvterm_id " +
                    "left join (select sr.subject_id vid, s.cvterm_id sid, s.name sn, s.definition sd from cvterm_relationship sr inner join cvterm s on s.cvterm_id = sr.object_id and sr.type_id = 1220) vsr on vsr.vid = v.cvterm_id " +
                    "left join (select pf.id, pf.entity_id from program_favorites pf inner join project p on p.program_uuid = pf.program_uuid and p.project_id = :programId and pf.entity_type = 'VARIABLES') pf on pf.entity_id = v.cvterm_id " +
                    "    WHERE (v.cv_id = 1040) " + filterClause + " ORDER BY v.cvterm_id")
                    .addScalar("vid").addScalar("vn").addScalar("vd")
                    .addScalar("pid").addScalar("pn").addScalar("pd")
                    .addScalar("mid").addScalar("mn").addScalar("md")
                    .addScalar("sid").addScalar("sn").addScalar("sd")
                    .addScalar("va").addScalar("fid");

            query.setParameter("programId", programId);

            if(!Objects.equals(methodId, null)) {
                query.setParameter("methodId", methodId);
            }

            if(!Objects.equals(propertyId, null)) {
                query.setParameter("propertyId", propertyId);
            }

            if(!Objects.equals(scaleId, null)) {
                query.setParameter("scaleId", scaleId);
            }

            List queryResults = query.list();

            for(Object row : queryResults) {
                Object[] items = (Object[]) row;
                OntologyVariableSummary variable = new OntologyVariableSummary(typeSafeObjectToInteger(items[0]), (String)items[1], (String) items[2]);
                variable.setPropertySummary(TermSummary.createNonEmpty(typeSafeObjectToInteger(items[3]), (String) items[4], (String) items[5]));
                variable.setMethodSummary(TermSummary.createNonEmpty(typeSafeObjectToInteger(items[6]), (String) items[7], (String) items[8]));
                variable.setScaleSummary(TermSummary.createNonEmpty(typeSafeObjectToInteger(items[9]), (String) items[10], (String) items[11]));
                variable.setAlias((String) items[12]);
                variable.setIsFavorite(items[13] != null);
                map.put(variable.getId(), variable);
            }

            //Variable Types, Created, modified, min, max from CVTermProperty
            List properties = getCvTermPropertyDao().getByCvId(CvId.VARIABLES.getId());
            for(Object p : properties){
                CVTermProperty property = (CVTermProperty) p;

                OntologyVariableSummary variableSummary = map.get(property.getCvTermId());

                if(variableSummary == null){
                    continue;
                }

                if(Objects.equals(property.getTypeId(), TermId.VARIABLE_TYPE.getId())){
                    variableSummary.addVariableType(VariableType.getByName(property.getValue()));
                } else if(Objects.equals(property.getTypeId(), TermId.MIN_VALUE.getId())){
                    variableSummary.setMinValue(property.getValue());
                } else if(Objects.equals(property.getTypeId(), TermId.MAX_VALUE.getId())){
                    variableSummary.setMaxValue(property.getValue());
                } else if(Objects.equals(property.getTypeId(), TermId.CREATION_DATE.getId())){
                    variableSummary.setDateCreated(ISO8601DateParser.tryParse(property.getValue()));
                } else if(Objects.equals(property.getTypeId(), TermId.LAST_UPDATE_DATE.getId())){
                    variableSummary.setDateLastModified(ISO8601DateParser.tryParse(property.getValue()));
                }
            }

        } catch(HibernateException e) {
            throw new MiddlewareQueryException("Error in getVariables", e);
        }

        return new ArrayList<>(map.values());
    }

    @Override
    public OntologyVariable getVariable(Integer programId, Integer id) throws MiddlewareQueryException, MiddlewareException {

        try {

            DmsProject project = getDmsProjectDao().getById(programId);

            if(project == null) {
                throw new MiddlewareException(PROGRAM_DOES_NOT_EXIST);
            }

            //Fetch full scale from db
            CVTerm term = getCvTermDao().getById(id);

            if (term == null) {
                throw new MiddlewareException(VARIABLE_DOES_NOT_EXIST);
            }

            if(term.getCv() != CvId.VARIABLES.getId()){
                throw new MiddlewareException(TERM_IS_NOT_VARIABLE);
            }

            try {

                OntologyVariable variable = new OntologyVariable(Term.fromCVTerm(term));

                //load scale, method and property data
                List<CVTermRelationship> relationships = getCvTermRelationshipDao().getBySubject(term.getCvTermId());
                for(CVTermRelationship  r : relationships) {
                    if(Objects.equals(r.getTypeId(), TermId.HAS_METHOD.getId())){
                        variable.setMethod(methodDataManager.getMethod(r.getObjectId()));
                    } else if(Objects.equals(r.getTypeId(), TermId.HAS_PROPERTY.getId())){
                        variable.setProperty(propertyDataManager.getProperty(r.getObjectId()));
                    } else if(Objects.equals(r.getTypeId(), TermId.HAS_SCALE.getId())) {
                        variable.setScale(scaleDataManager.getScaleById(r.getObjectId()));
                    }
                }

                //Variable Types, Created, modified, min, max from CVTermProperty
                List properties = getCvTermPropertyDao().getByCvTermId(term.getCvTermId());

                for(Object p : properties){
                    CVTermProperty property = (CVTermProperty) p;

                    if(Objects.equals(property.getTypeId(), TermId.VARIABLE_TYPE.getId())){
                        variable.addVariableType(VariableType.getByName(property.getValue()));
                    } else if(Objects.equals(property.getTypeId(), TermId.MIN_VALUE.getId())){
                        variable.setMinValue(property.getValue());
                    } else if(Objects.equals(property.getTypeId(), TermId.MAX_VALUE.getId())){
                        variable.setMaxValue(property.getValue());
                    } else if(Objects.equals(property.getTypeId(), TermId.CREATION_DATE.getId())){
                        variable.setDateCreated(ISO8601DateParser.tryParse(property.getValue()));
                    } else if(Objects.equals(property.getTypeId(), TermId.LAST_UPDATE_DATE.getId())){
                        variable.setDateLastModified(ISO8601DateParser.tryParse(property.getValue()));
                    }
                }

                //Get variable alias
                CVTermAlias cvTermAlias = getCvTermAliasDao().getByCvTermAndProgram(id, programId);
                if(cvTermAlias != null) {
                    variable.setAlias(cvTermAlias.getName());
                }

                //Get favorite from ProgramFavoriteDAO
                ProgramFavorite programFavorite = getProgramFavoriteDao().getProgramFavorite(project.getProgramUUID(), ProgramFavorite.FavoriteType.VARIABLE, term.getCvTermId());
                variable.setIsFavorite(programFavorite != null);

                //TODO: Temporary implementation. Need to discuss this with actual use cases.
                SQLQuery query = getActiveSession().createSQLQuery("select (select count(*) from projectprop where type_id = :variableId) + (select count(*) from phenotype where observable_id = :variableId)");
                query.setParameter("variableId", id);

                variable.setObservations(((BigInteger) query.uniqueResult()).intValue());

                return variable;

            } catch(HibernateException e) {
                throw new MiddlewareQueryException("Error in getVariable", e);
            }

        } catch(HibernateException e) {
            throw new MiddlewareQueryException("Error in getting standard variable summaries from standard_variable_summary view", e);
        }
    }

    @Override
    public void addVariable(OntologyVariableInfo variableInfo) throws MiddlewareQueryException, MiddlewareException {

        CVTerm term = getCvTermDao().getByNameAndCvId(variableInfo.getName(), CvId.VARIABLES.getId());

        if (term != null) {
            throw new MiddlewareException(VARIABLE_EXIST_WITH_SAME_NAME);
        }

        Session session = getActiveSession();
        Transaction transaction = null;

        try {
            transaction = session.beginTransaction();
            //Saving term to database.
            CVTerm savedTerm = getCvTermDao().save(variableInfo.getName(), variableInfo.getDescription(), CvId.VARIABLES);
            variableInfo.setId(savedTerm.getCvTermId());

            transaction.commit();

        } catch (Exception e) {
            rollbackTransaction(transaction);
            throw new MiddlewareQueryException("Error at addScale :" + e.getMessage(), e);
        }
    }

    @Override
    public void updateVariable(OntologyVariableInfo variableInfo) throws MiddlewareQueryException, MiddlewareException {

    }

    @Override
    public void deleteVariable(Integer id) throws MiddlewareQueryException, MiddlewareException {

    }
}
