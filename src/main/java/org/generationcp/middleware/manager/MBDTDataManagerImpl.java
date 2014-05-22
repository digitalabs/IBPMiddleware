package org.generationcp.middleware.manager;

import org.generationcp.middleware.dao.mbdt.MBDTGenerationDAO;
import org.generationcp.middleware.dao.mbdt.MBDTProjectDAO;
import org.generationcp.middleware.dao.mbdt.SelectedGenotypeDAO;
import org.generationcp.middleware.dao.mbdt.SelectedMarkerDAO;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.hibernate.HibernateSessionProvider;
import org.generationcp.middleware.manager.api.MBDTDataManager;
import org.generationcp.middleware.pojos.mbdt.MBDTGeneration;
import org.generationcp.middleware.pojos.mbdt.MBDTProjectData;
import org.generationcp.middleware.pojos.mbdt.SelectedGenotype;
import org.generationcp.middleware.pojos.mbdt.SelectedMarker;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 */
public class MBDTDataManagerImpl extends DataManager implements MBDTDataManager {

    private MBDTProjectDAO projectDAO;
    private MBDTGenerationDAO generationDAO;
    private SelectedMarkerDAO selectedMarkerDAO;
    private SelectedGenotypeDAO selectedGenotypeDAO;

    public MBDTDataManagerImpl(HibernateSessionProvider sessionProviderForLocal, HibernateSessionProvider sessionProviderForCentral) {
        super(sessionProviderForLocal, sessionProviderForCentral);
    }

    @Override
    public Integer setProjectData(MBDTProjectData projectData) throws MiddlewareQueryException {
        requireLocalDatabaseInstance();
        projectDAO = prepareProjectDAO();

        if (projectData.getProjectID() == null || projectData.getProjectID() == 0) {
            projectData.setProjectID(projectDAO.getNegativeId());
        }

        projectData = projectDAO.save(projectData);
        return projectData.getProjectID();
    }


    // this method is nullable
    @Override
    public MBDTProjectData getProjectData(Integer projectID) throws MiddlewareQueryException {

        if (projectID < 0) {
            requireLocalDatabaseInstance();
        } else {
            requireCentralDatabaseInstance();
        }

        projectDAO = prepareProjectDAO();
        MBDTProjectData data = projectDAO.getById(projectID);
        return data;
    }

    @Override
    public MBDTGeneration addGeneration(Integer projectID, String generationName, Integer datasetID) throws MiddlewareQueryException {
        MBDTProjectData project = getProjectData(projectID);
        MBDTGeneration newGeneration = new MBDTGeneration(generationName, project, datasetID);

        prepareGenerationDAO();

        Integer newId = generationDAO.getNegativeId("generationID");
        newGeneration.setGenerationID(newId);

        generationDAO.saveOrUpdate(newGeneration);

        return newGeneration;
    }

    @Override
    public MBDTGeneration getGeneration(Integer projectID, Integer datasetID) throws MiddlewareQueryException {
        prepareGenerationDAO();
        return generationDAO.getByProjectAndDatasetID(projectID, datasetID);
    }

    @Override
    public void setSelectedMarkers(Integer projectID, Integer datasetID, List<Integer> markerIDs) throws MiddlewareQueryException {
        prepareGenerationDAO();
        prepareSelectedMarkerDAO();
        MBDTGeneration generation = getGeneration(projectID, datasetID);
        List<SelectedMarker> selectedMarkers = generation.getSelectedMarkers();
        if (selectedMarkers == null) {
            selectedMarkers = new ArrayList<SelectedMarker>();
            generation.setSelectedMarkers(selectedMarkers);
        }


        for (Integer markerID : markerIDs) {
            SelectedMarker sm = new SelectedMarker(generation, markerID);
            Integer newId =  selectedMarkerDAO.getNegativeId("id");
            sm.setId(newId);

            selectedMarkerDAO.saveOrUpdate(sm);
            selectedMarkers.add(sm);
        }

        generationDAO.saveOrUpdate(generation);
    }

    @Override
    public List<Integer> getSelectedMarkers(Integer projectID, Integer datasetID) throws MiddlewareQueryException {
        prepareGenerationDAO();
        MBDTGeneration generation = generationDAO.getByProjectAndDatasetID(datasetID, projectID);

        List<SelectedMarker> markers = generation.getSelectedMarkers();

        List<Integer> returnValues = new ArrayList<Integer>();

        for (SelectedMarker marker : markers) {
            returnValues.add(marker.getMarkerID());
        }

        return returnValues;
    }

    @Override
    public List<SelectedGenotype> getSelectedAccession(Integer projectID, Integer datasetID) throws MiddlewareQueryException {
        prepareSelectedGenotypeDAO();

        try {
            return selectedGenotypeDAO.retrieveAllAccessions(projectID, datasetID);
        } catch (Exception e) {
            e.printStackTrace();
            throw new MiddlewareQueryException(e.getMessage());
        }
    }

    protected MBDTProjectDAO prepareProjectDAO() {
        if (projectDAO == null) {
            projectDAO = new MBDTProjectDAO();
        }

        projectDAO.setSession(getActiveSession());

        return projectDAO;
    }

    protected MBDTGenerationDAO prepareGenerationDAO() {
        if (generationDAO == null) {
            generationDAO = new MBDTGenerationDAO();
        }

        generationDAO.setSession(getActiveSession());

        return generationDAO;
    }

    protected SelectedMarkerDAO prepareSelectedMarkerDAO() {
        if (selectedMarkerDAO == null) {
            selectedMarkerDAO = new SelectedMarkerDAO();
        }

        selectedMarkerDAO.setSession(getActiveSession());

        return selectedMarkerDAO;
    }

    protected SelectedGenotypeDAO prepareSelectedGenotypeDAO() {
        if (selectedGenotypeDAO == null) {
            selectedGenotypeDAO = new SelectedGenotypeDAO();
        }

        selectedGenotypeDAO.setSession(getActiveSession());

        return selectedGenotypeDAO;
    }


}
