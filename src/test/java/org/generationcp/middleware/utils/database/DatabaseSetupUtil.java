package org.generationcp.middleware.utils.database;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang.SystemUtils;
import org.generationcp.middleware.exceptions.ConfigException;
import org.generationcp.middleware.manager.DatabaseConnectionParameters;
import org.generationcp.middleware.util.ResourceFinder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.wc.SVNWCUtil;
import org.tmatesoft.svn.core.wc2.SvnCheckout;
import org.tmatesoft.svn.core.wc2.SvnOperationFactory;
import org.tmatesoft.svn.core.wc2.SvnTarget;
import org.junit.Test;

import com.hazelcast.util.StringUtil;

public class DatabaseSetupUtil{

	protected static final Logger LOG = LoggerFactory.getLogger(DatabaseSetupUtil.class);

	private static final String TEST_DATABASE_CONFIG_PROPERTIES = "testDatabaseConfig.properties";
	private static final String prefixDirectory = getResourcePath("updatedIbdbScripts");
	private static String SQL_SCRIPTS_FOLDER = "./sql";
	private static String WORKBENCH_SCRIPT = "/workbench";
	private static String CROP_SCRIPT = "/merged";
    private static String TEST_DB_REQUIRED_PREFIX = "test_";
    
	private static final String DEFAULT_IBDB_GIT_URL = "https://github.com/IntegratedBreedingPlatform/IBDBScripts/trunk";

	private static DatabaseConnectionParameters cropConnectionParameters, workbenchConnectionParameters;

	private static String MYSQL_PATH = "";

	private static String gitUrl;
	private static String gitUserName;
	private static String gitPassword;


	@Test
	public void testSetupDatabase() throws Exception{
		setupTestDatabases();
	}

	private static void setUpMysqlConfig() throws Exception{
		Class.forName("com.mysql.jdbc.Driver");
		
		cropConnectionParameters = new DatabaseConnectionParameters(TEST_DATABASE_CONFIG_PROPERTIES, "crop");
		workbenchConnectionParameters = new DatabaseConnectionParameters(TEST_DATABASE_CONFIG_PROPERTIES, "workbench");

		InputStream in = new FileInputStream(new File(ResourceFinder.locateFile(TEST_DATABASE_CONFIG_PROPERTIES).toURI()));
		Properties prop = new Properties();
		prop.load(in);

		MYSQL_PATH = prop.getProperty("mysql.path", "mysql");

		LOG.debug("  >>> MYSQL_PATH:" + MYSQL_PATH);
	}

	private static Map<String, List<File>> setupScripts() throws FileNotFoundException, URISyntaxException{
		Map<String, List<File>> scriptsMap = new HashMap<>();

        try{
			File wbFile = new File(ResourceFinder.locateFile(SQL_SCRIPTS_FOLDER +WORKBENCH_SCRIPT).toURI());
			if(wbFile.isDirectory()){
				scriptsMap.put(WORKBENCH_SCRIPT,  Arrays.asList(wbFile.listFiles()));
			}
		}catch(FileNotFoundException e){
			scriptsMap.put(WORKBENCH_SCRIPT, new ArrayList<File>());
		}

		try{
			File cropFile = new File(ResourceFinder.locateFile(SQL_SCRIPTS_FOLDER + CROP_SCRIPT).toURI());
			if(cropFile.isDirectory()){
				scriptsMap.put(CROP_SCRIPT, Arrays.asList(cropFile.listFiles()));
			}
		}catch(FileNotFoundException e){
			scriptsMap.put(CROP_SCRIPT, new ArrayList<File>());
		}

		return scriptsMap;
	}

	private static boolean isTestDatabase(String dbName){
        return dbName != null && dbName.startsWith(TEST_DB_REQUIRED_PREFIX);
	}

	private static void initializeWorkbenchDatabase() throws Exception {
		// copy and execute workbench scripts
		String checkoutURL = prefixDirectory+"/database/workbench";
		String workbenchGitURL = gitUrl + "/workbench";
		checkoutAndRunIBDBScripts(checkoutURL, workbenchGitURL, workbenchConnectionParameters);

		LOG.debug("  >>> Workbench DB initialized - all scripts from IBDBScripts ran successfully.");
	}

	private static void initializeCropDatabase() throws Exception {
		// copy and execute merged/common scripts
		String checkoutURL = prefixDirectory+"/database/merged/common";
		String centralCommonGitURL = gitUrl + "/merged/common";
		checkoutAndRunIBDBScripts(checkoutURL, centralCommonGitURL, cropConnectionParameters);

		//copy and execute merged/common-update scripts
		checkoutURL = prefixDirectory+"/database/merged/common-update";
		centralCommonGitURL = gitUrl + "/merged/common-update";
		checkoutAndRunIBDBScripts(checkoutURL, centralCommonGitURL, cropConnectionParameters);

		//copy and execute merged/common-migration-410 scripts
		checkoutURL = prefixDirectory+"/database/merged/common-migration-410";
		centralCommonGitURL = gitUrl + "/merged/common-migration-410";
		checkoutAndRunIBDBScripts(checkoutURL, centralCommonGitURL, cropConnectionParameters);

		LOG.debug("  >>> Central DB initialized - all scripts from IBDBScripts ran successfully.");
	}

	private static void setupIBDBScriptsConfig() throws URISyntaxException, IOException {
		InputStream in = new FileInputStream(new File(ResourceFinder.locateFile(TEST_DATABASE_CONFIG_PROPERTIES).toURI()));
		Properties prop = new Properties();
		prop.load(in);

		String ibdbScriptsGitUrl = prop.getProperty("test.ibdb.scripts.git.url", null);
		String ibdbScriptsGitUserName = prop.getProperty("test.ibdb.scripts.git.username", null);
		String ibdbScriptsGitPassword = prop.getProperty("test.ibdb.scripts.git.password", null);
		
		if(ibdbScriptsGitUrl == null) {
			//we use the default url
			gitUrl = DEFAULT_IBDB_GIT_URL;
		} else {
			gitUrl = ibdbScriptsGitUrl;
			gitUserName = ibdbScriptsGitUserName;
			gitPassword = ibdbScriptsGitPassword;
		}
	}

	private static void checkoutAndRunIBDBScripts(String checkoutURL, String gitUrl, DatabaseConnectionParameters connection) throws Exception {

		File scriptsDir = new File(checkoutURL);
		
		SvnOperationFactory svnOperationFactory = new SvnOperationFactory();
		
		if (!StringUtil.isNullOrEmpty(gitUserName) && !StringUtil.isNullOrEmpty(gitPassword)){
			ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager(gitUserName, gitPassword);
			svnOperationFactory.setAuthenticationManager(authManager);
		}
		
		try {

			SvnCheckout checkout = svnOperationFactory.createCheckout();
			checkout.setSingleTarget(SvnTarget.fromFile(scriptsDir));

			SVNURL url = SVNURL.parseURIEncoded(gitUrl);
			checkout.setSource(SvnTarget.fromURL(url));
			checkout.run();
		} catch (Exception e) {
			LOG.error(" >>> checkout failed ", e);
			throw e;
		} finally {
			svnOperationFactory.dispose();
		}
		LOG.debug("  >>> Checkout from " + gitUrl + " successful.");

		File[] files = scriptsDir.listFiles();
		Arrays.sort(files, new Comparator<File>() {
			public int compare(File a, File b) {
				return a.getName().compareTo(b.getName());
			}
		});
		runAllSetupScripts(Arrays.asList(files), connection);
	}

	/**
	 * This function will help to delete directory recursively
	 * @param srcFile Source file to examine
	 * @throws IOException if File not found
	 */
	private static void deleteFileTree(File srcFile) throws IOException {
		// Checks if file is a directory
		if (srcFile.isDirectory()) {
			//Gathers files in directory
			File[] files = srcFile.listFiles();
			for (File file : files) {
				//Recursively deletes all files and sub-directories
				deleteFileTree(file);
			}
			// Deletes original sub-directory file
			srcFile.delete();
		} else {
			srcFile.delete();
		}
	}
	
	/**
	 * Creates test central, local and workbench databases with creation scripts coming from IBDBScripts
	 * along with init data files in src\test\resources\sql
	 *
	 * @return true if all test databases created successfully
	 * @throws Exception
	 */
	public static void setupTestDatabases() throws Exception {
		setUpMysqlConfig();
		setupIBDBScriptsConfig();

		// drop database to ensure fresh DB
		dropTestDatabases();

		// delete old prefix directory
		File scriptsDir = new File(prefixDirectory);
		if(scriptsDir.exists()){
			if(scriptsDir.isFile()) throw new Exception("File name conflict: a file with directory name exist. Please remove file or change directory name.");
			deleteFileTree(scriptsDir);
		}

		Map<String, List<File>> scriptsMap = setupScripts();

		createTestDatabase(workbenchConnectionParameters, scriptsMap.get(WORKBENCH_SCRIPT));
		createTestDatabase(cropConnectionParameters, scriptsMap.get(CROP_SCRIPT));
	}


	private static void createTestDatabase(DatabaseConnectionParameters connectionParams, List<File> initDataFiles) throws Exception {

		if(isTestDatabase(connectionParams.getDbName())) {
			runSQLCommand("CREATE DATABASE  IF NOT EXISTS `"+connectionParams.getDbName()+"`; USE `"+connectionParams.getDbName()+"`;", connectionParams);

			if (connectionParams.equals(workbenchConnectionParameters)) {
				LOG.info("Creating WORKBENCH db .......");
				initializeWorkbenchDatabase();
			} else {
				LOG.info("Creating CROP db ......");
				initializeCropDatabase();
			}

			//NOTE: This will run additional scripts if required while setup
			if (!initDataFiles.isEmpty()){
				runAllSetupScripts(initDataFiles, connectionParams);
				LOG.info("  >>> Ran init data scripts successfully");
			}

		} else {
			throw new Exception("Test Database is not setup, please use a prefix 'test_' ");
		}
	}


	/**
	 * Drops all test databases (central, local, workbench)
	 *
	 * @return boolean IsSuccess
	 * @throws Exception
	 */
	public static boolean dropTestDatabases() throws Exception{
		try {
			setUpMysqlConfig();

			dropTestDatabase(cropConnectionParameters);
			dropTestDatabase(workbenchConnectionParameters);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (ConfigException e) {
			e.printStackTrace();
			return false;
		}catch (IOException e) {
			e.printStackTrace();
			return false;
		} catch (InterruptedException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private static void dropTestDatabase(DatabaseConnectionParameters connectionParams) throws Exception {
		if(isTestDatabase(connectionParams.getDbName())){
			runSQLCommand("DROP DATABASE IF EXISTS `" + connectionParams.getDbName()+ "`; ", connectionParams);
		}else{
			throw new Exception("Test Database is not setup, please use a prefix 'test_' ");
		}
	}

	private static void runAllSetupScripts(List<File> fileList, DatabaseConnectionParameters connectionParams) throws Exception{
		if(fileList != null && !fileList.isEmpty()){
            for (File sqlFile : fileList) {
                if (sqlFile.getName().endsWith(".sql")) {
                    if (!runScriptFromFile(sqlFile, connectionParams)) {
                        throw new Exception("Error in executing " + sqlFile.getAbsolutePath());
                    }
                }
            }
		}
	}

	private static boolean runScriptFromFile(File sqlFile, DatabaseConnectionParameters connectionParams) throws IOException, InterruptedException {
		ProcessBuilder pb;
		
		String mysqlAbsolutePath;
		if (SystemUtils.IS_OS_WINDOWS){
			mysqlAbsolutePath = new File(MYSQL_PATH).getAbsolutePath();
		}else{
			mysqlAbsolutePath = "mysql";
		}
		
		LOG.info("Executing script: " + sqlFile.getName());

		if (connectionParams.getPassword() == null || connectionParams.getPassword().equalsIgnoreCase("")) {

			pb = new ProcessBuilder(mysqlAbsolutePath
					,"--host=" + connectionParams.getHost()
					,"--port=" + connectionParams.getPort()
					,"--user=" + connectionParams.getUsername()
					,"--default-character-set=utf8"
					,connectionParams.getDbName()
					,"--execute=source " + sqlFile.getAbsoluteFile()
			);
		}
		else {
			pb = new ProcessBuilder(mysqlAbsolutePath
					,"--host=" + connectionParams.getHost()
					,"--port=" + connectionParams.getPort()
					,"--user=" + connectionParams.getUsername()
					, "--password=" + connectionParams.getPassword()
					,"--default-character-set=utf8"
					,connectionParams.getDbName()
					,"--execute=source " + sqlFile.getAbsoluteFile()
			);
		}

		Process mysqlProcess = pb.start();
		readProcessInputAndErrorStream(mysqlProcess);
		int exitValue = mysqlProcess.waitFor();
		return exitValue == 0;
	}
	
	private static boolean runSQLCommand(String sqlCommand, DatabaseConnectionParameters connectionParams) throws IOException, InterruptedException {
		ProcessBuilder pb;
		String mysqlAbsolutePath = new File(MYSQL_PATH).getAbsolutePath();

		if (connectionParams.getPassword() == null || connectionParams.getPassword().equalsIgnoreCase("")) {
			pb = new ProcessBuilder(mysqlAbsolutePath
					,"--host=" + connectionParams.getHost()
					,"--port=" + connectionParams.getPort()
					,"--user=" + connectionParams.getUsername()
					,"--default-character-set=utf8"
					,"-e"
					,sqlCommand

			);
		}
		else {
			pb = new ProcessBuilder(mysqlAbsolutePath
					,"--host=" + connectionParams.getHost()
					,"--port=" + connectionParams.getPort()
					,"--user=" + connectionParams.getUsername()
					, "--password=" + connectionParams.getPassword()
					,"--default-character-set=utf8"
					,"-e"
					,sqlCommand
			);
		}

		Process mysqlProcess = pb.start();
		readProcessInputAndErrorStream(mysqlProcess);
		int exitValue = mysqlProcess.waitFor();
		return exitValue == 0;
	}
	private static String readProcessInputAndErrorStream(Process process) throws IOException {
    	/* Added while loop to get input stream because process.waitFor() has a problem
         * Reference: 
         * http://stackoverflow.com/questions/5483830/process-waitfor-never-returns
         */
		BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
		String line;

		StringBuilder stdOut = new StringBuilder();
		while ( (line = reader.readLine()) != null) {
			stdOut.append(line);
		}
        
        LOG.debug(stdOut.toString());
        
		reader.close();
        /* When the process writes to stderr the output goes to a fixed-size buffer. 
         * If the buffer fills up then the process blocks until the buffer gets emptied. 
         * So if the buffer doesn't empty then the process will hang.
         * http://stackoverflow.com/questions/10981969/why-is-going-through-geterrorstream-necessary-to-run-a-process
         */
		BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
		StringBuilder errorOut = new StringBuilder();
		while ((line = errorReader.readLine()) != null) {
			errorOut.append(line);
		}
		errorReader.close();
		return errorOut.toString();
	}
	
	private static String getResourcePath(String name){
		return System.getProperty("user.dir") + File.separator + name;
	}
}
