package org.generationcp.middleware.manager.test;

import com.mchange.v2.c3p0.DriverManagerDataSourceFactory;
import org.generationcp.middleware.exceptions.MiddlewareQueryException;
import org.generationcp.middleware.manager.DatabaseConnectionParameters;
import org.generationcp.middleware.manager.ManagerFactory;
import org.generationcp.middleware.manager.api.MBDTDataManager;
import org.generationcp.middleware.pojos.mbdt.MBDTGeneration;
import org.generationcp.middleware.pojos.mbdt.MBDTProjectData;
import org.generationcp.middleware.pojos.mbdt.SelectedGenotype;
import org.generationcp.middleware.utils.test.TestOutputFormatter;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Created by IntelliJ IDEA.
 * User: Daniel Villafuerte
 */


public class MBDTDataManagerTest extends TestOutputFormatter {

    private static MBDTDataManager dut;
    private DataSource dataSource;

    public static final Integer SAMPLE_PROJECT_ID = -1;
    public static final String SAMPLE_PROJECT_NAME = "SAMPLE_PROJECT";
    public static final String SAMPLE_GENERATION_NAME = "SAMPLE GENERATION";
    public static final int SAMPLE_DATASET_ID = -1;
    public static final int SAMPLE_GENERATION_ID = -1;
    public static final int[] SAMPLE_SELECTED_MARKER_IDS = new int[]{-1, -2, -3};
    public static final int[] SAMPLE_SELECTED_ACCESSION_GIDS = new int[]{1, 2, 3, 4, 5, 6};
    public static final List<Integer> SAMPLE_PARENT_GIDS = new ArrayList<Integer>();


    @BeforeClass
    public static void setUp() throws Exception {
        DatabaseConnectionParameters local = new DatabaseConnectionParameters(
                "testDatabaseConfig.properties", "local");

        DatabaseConnectionParameters central = new DatabaseConnectionParameters(
                "testDatabaseConfig.properties", "central");
        ManagerFactory managerFactory = new ManagerFactory(local, central);
        dut = managerFactory.getMbdtDataManager();
        SAMPLE_PARENT_GIDS.add(4);
        SAMPLE_PARENT_GIDS.add(5);
        SAMPLE_PARENT_GIDS.add(6);
    }

    @Before
    public void prepareDatabaseItems() throws Exception {
        DatabaseConnectionParameters local = new DatabaseConnectionParameters(
                "testDatabaseConfig.properties", "local");

        dataSource = DriverManagerDataSourceFactory.create(local.getDriverName(), local.getUrl(), local.getUsername(), local.getPassword());
    }

    protected void executeUpdate(String sql) throws Exception {
        Connection conn = null;
        Statement stmt = null;

        try {
            conn = dataSource.getConnection();
            stmt = conn.createStatement();

            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            fail(e.getMessage());
        } finally {
            closeDatabaseResources(conn, stmt, null);
        }
    }

    protected void insertSampleProjectData() throws Exception {
        executeUpdate("INSERT INTO MBDT_PROJECT VALUES (" + SAMPLE_PROJECT_ID + ", '" + SAMPLE_PROJECT_NAME + "', 0, null, null, null, null, null, null)");
    }

    protected void insertSampleGenerationData() throws Exception {
        executeUpdate("INSERT INTO mbdt_generations VALUES (" + SAMPLE_GENERATION_ID + ", '" + SAMPLE_PROJECT_NAME + "', " + SAMPLE_PROJECT_ID + ", " + SAMPLE_DATASET_ID + ")");
    }

    protected void deleteSampleGenerationData() throws Exception {
        executeUpdate("DELETE FROM mbdt_generations WHERE generation_id = " + SAMPLE_GENERATION_ID);
    }

    protected void deleteSampleProjectData() throws Exception {
        executeUpdate("DELETE FROM mbdt_project WHERE project_id = " + SAMPLE_PROJECT_ID);
    }

    protected void insertSampleMarkerData() throws Exception {
        int i = -1;
        for (int markerId : SAMPLE_SELECTED_MARKER_IDS) {
            executeUpdate("INSERT INTO mbdt_selected_markers VALUES(" + i + ", " + SAMPLE_GENERATION_ID + ", " + markerId + ")");
            i--;
        }
    }

    protected void insertSampleAccessionData() throws Exception {
        int i = -1;
        for (int sampleSelectedAccessionGid : SAMPLE_SELECTED_ACCESSION_GIDS) {
            if (SAMPLE_PARENT_GIDS.contains(sampleSelectedAccessionGid)) {
                executeUpdate("INSERT INTO mbdt_selected_genotypes VALUES(" + i + ", " + SAMPLE_GENERATION_ID + ", " + sampleSelectedAccessionGid + ", 'SR')");
            } else {
                executeUpdate("INSERT INTO mbdt_selected_genotypes VALUES(" + i + ", " + SAMPLE_GENERATION_ID + ", " + sampleSelectedAccessionGid + ", 'SA')");
            }
            i--;
        }


    }

    protected void deleteSampleMarkerData() throws Exception {
        for (int markerId : SAMPLE_SELECTED_MARKER_IDS) {
            executeUpdate("DELETE FROM mbdt_selected_markers WHERE marker_id = " + markerId);
        }
    }

    protected void deleteSampleAccessionData() throws Exception {
        for (int gid : SAMPLE_SELECTED_ACCESSION_GIDS) {
            executeUpdate("DELETE FROM mbdt_selected_genotypes WHERE gid = " + gid);
        }
    }

    @Test
    public void testRetrieveProject() throws Exception {
        // set up dummy data in database
        Connection conn = null;
        Statement stmt = null;

        try {
            insertSampleProjectData();

            MBDTProjectData retrieved = dut.getProjectData(SAMPLE_PROJECT_ID);

            assertNotNull(retrieved);

            assertEquals(SAMPLE_PROJECT_NAME, retrieved.getProjectName());


        } catch (Exception e) {
            fail(e.getMessage());
        } finally {
            deleteSampleProjectData();
            closeDatabaseResources(conn, stmt, null);
        }
    }

    @Test
    public void testSetProjectPositive() throws Exception {

        MBDTProjectData newProject = new MBDTProjectData(null, SAMPLE_PROJECT_NAME, 0, null, null, null);

        Integer generatedId = dut.setProjectData(newProject);
        assertNotNull(generatedId);
        assertTrue(generatedId < 0);

        // clean up operation
        Connection connection = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {
            connection = dataSource.getConnection();
            stmt = connection.createStatement();

            // check on the db side if the generated project really exists
            rs = stmt.executeQuery("SELECT pname FROM mbdt_project WHERE project_id = " + generatedId);

            if (rs.next()) {
                String retrievedName = rs.getString("pname");
                assertNotNull(retrievedName);
                assertEquals(retrievedName, SAMPLE_PROJECT_NAME);
            } else {
                fail("Unable to properly retrieve 'generated' project");
            }

            // perform clean up
            stmt.execute("DELETE FROM mbdt_project WHERE project_id = " + generatedId);

        } catch (SQLException e) {
            fail(e.getMessage());
            e.printStackTrace();
        } finally {
            closeDatabaseResources(connection, stmt, rs);
        }
    }

    @Test
    public void testSetProjectNegative() {

        MBDTProjectData newProject = new MBDTProjectData(null, null, 0, null, null, null);

        Integer generatedId = null;
        try {
            generatedId = dut.setProjectData(newProject);

            fail("Should not allow saving of null project name");
        } catch (MiddlewareQueryException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testAddGeneration() throws Exception {

        insertSampleProjectData();
        MBDTGeneration generation = dut.addGeneration(SAMPLE_PROJECT_ID, SAMPLE_GENERATION_NAME, SAMPLE_DATASET_ID);

        assertNotNull(generation);
        assertEquals(SAMPLE_GENERATION_NAME, generation.getGenerationName());
        assertNotNull(generation.getGenerationID());
        assertTrue(generation.getGenerationID() < 0);

        // check the database for correct retrieval

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        try {
            conn = dataSource.getConnection();
            stmt = conn.createStatement();

            rs = stmt.executeQuery("SELECT gname from mbdt_generations WHERE generation_id = " + generation.getGenerationID());

            if (rs.next()) {
                String generationName = rs.getString("gname");
                assertNotNull(generationName);
                assertEquals(SAMPLE_GENERATION_NAME, generationName);
            } else {
                fail("Unable to properly create generation entry");
            }


            stmt.executeUpdate("DELETE FROM mbdt_generations where generation_id = " + generation.getGenerationID());


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            deleteSampleProjectData();
            closeDatabaseResources(conn, stmt, rs);
        }
    }

    @Test
    public void testRetrieveGeneration() throws Exception {
        insertSampleProjectData();
        insertSampleGenerationData();

        try {
            MBDTGeneration generation = dut.getGeneration(SAMPLE_PROJECT_ID, SAMPLE_DATASET_ID);

            assertNotNull(generation);
            assertEquals(SAMPLE_GENERATION_NAME, generation.getGenerationName());
            assertTrue(SAMPLE_DATASET_ID == generation.getGenotypeDatasetID());
        } catch (MiddlewareQueryException e) {
            e.printStackTrace();
        } finally {
            deleteSampleGenerationData();
            deleteSampleProjectData();
        }


    }

    @Test
    public void testSetSelectedMarker() throws Exception {
        insertSampleProjectData();
        insertSampleGenerationData();

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;

        List<Integer> testMarkerIDs = new ArrayList<Integer>();

        for (int sampleSelectedMarkerId : SAMPLE_SELECTED_MARKER_IDS) {
            testMarkerIDs.add(sampleSelectedMarkerId);
        }

        try {
            dut.setSelectedMarkers(SAMPLE_PROJECT_ID, SAMPLE_DATASET_ID, testMarkerIDs);

            conn = dataSource.getConnection();
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT marker_id FROM mbdt_selected_markers mark INNER JOIN mbdt_generations " +
                    "gen ON (mark.generation_id = gen.generation_id) INNER JOIN mbdt_project proj ON (gen.project_id = proj.project_id)");

            int recordCount = 0;

            while (rs.next()) {
                int value = rs.getInt("marker_id");
                assertTrue(SAMPLE_SELECTED_MARKER_IDS[recordCount] == value);
                recordCount++;
            }

            assertTrue(recordCount == SAMPLE_SELECTED_MARKER_IDS.length);

            StringBuffer buffer = new StringBuffer("DELETE FROM mbdt_selected_markers where marker_id IN(");

            for (int i = 0; i < SAMPLE_SELECTED_MARKER_IDS.length; i++) {
                if (i != 0) {
                    buffer.append(",");
                }

                buffer.append(SAMPLE_SELECTED_MARKER_IDS[i]);
            }

            buffer.append(")");

            stmt.executeUpdate(buffer.toString());
        } catch (MiddlewareQueryException e) {
            e.printStackTrace();
        } finally {
            deleteSampleGenerationData();
            deleteSampleProjectData();
            closeDatabaseResources(conn, stmt, rs);
        }
    }

    @Test
    public void testGetSelectedMarker() throws Exception {
        insertSampleProjectData();
        insertSampleGenerationData();
        insertSampleMarkerData();

        try {
            List<Integer> selectedMarkerIDs = dut.getSelectedMarkers(SAMPLE_PROJECT_ID, SAMPLE_DATASET_ID);

            assertNotNull(selectedMarkerIDs);
            assertTrue(SAMPLE_SELECTED_MARKER_IDS.length == selectedMarkerIDs.size());

            for (int i = 0; i < SAMPLE_SELECTED_MARKER_IDS.length; i++) {
                assertTrue(SAMPLE_SELECTED_MARKER_IDS[i] == selectedMarkerIDs.get(i));
            }
        } catch (MiddlewareQueryException e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            deleteSampleMarkerData();
            deleteSampleGenerationData();
            deleteSampleProjectData();
        }
    }

    @Test
    public void testGetSelectedAccessions() throws Exception {
        insertSampleProjectData();
        insertSampleGenerationData();
        insertSampleAccessionData();

        try {
            List<SelectedGenotype> accessions = dut.getSelectedAccession(SAMPLE_PROJECT_ID, SAMPLE_DATASET_ID);

            assertNotNull(accessions);
            assertTrue(SAMPLE_SELECTED_ACCESSION_GIDS.length == accessions.size());

            for (int i = 0; i < SAMPLE_SELECTED_ACCESSION_GIDS.length; i++) {
                assertTrue(SAMPLE_SELECTED_ACCESSION_GIDS[i] == accessions.get(i).getGid());
            }
        } catch (Exception e) {
            e.printStackTrace();
            fail(e.getMessage());
        } finally {
            deleteSampleAccessionData();
            deleteSampleGenerationData();
            deleteSampleProjectData();
        }
    }

    protected void closeDatabaseResources(Connection conn, Statement stmt, ResultSet rs) throws SQLException {
        if (conn != null) {
            conn.close();
        }

        if (stmt != null) {
            stmt.close();
        }

        if (rs != null) {
            rs.close();
        }
    }
}