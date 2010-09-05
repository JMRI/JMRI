// Backup.java

package jmri.jmrit.operations.setup;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Calendar;

import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.locations.LocationManagerXml;
import jmri.jmrit.operations.rollingstock.cars.CarManagerXml;
import jmri.jmrit.operations.rollingstock.engines.EngineManagerXml;
import jmri.jmrit.operations.routes.RouteManagerXml;
import jmri.jmrit.operations.trains.TrainManagerXml;

/**
 * Backs up operation files. Creates the "backups" and date directories along
 * with backup files in the operations directory.
 * 
 * @author Daniel Boudreau Copyright (C) 2008
 * @version $Revision: 1.13 $
 */
public class Backup extends XmlFile {

	public Backup() {
	}
	
	/**
	 * Creates a folder named directoryName in the backups folder, and
	 * then saves all of the operations files into that folder.
	 * @param directoryName
	 * @return true if successful, false if not.
	 */
	public boolean backupFiles(String directoryName){
		setDirectoryName(directoryName);
		return backupFiles();
	}

	public boolean backupFiles() {
		try {
			if (!checkFile(fullBackupFilename(OperationsXml.getOperationsFileName()))) {
				// The file/directory does not exist, create it before writing
				File file = new java.io.File(fullBackupFilename(null));
				File parentDir = file.getParentFile();
				if (!parentDir.exists()) {
					if (!parentDir.mkdirs()) {
						log.error("backup directory not created");
						return false;
					}
				}
			}
			OperationsXml.instance().writeFile(
					fullBackupFilename(OperationsXml.getOperationsFileName()));
			CarManagerXml.instance().writeFile(
					fullBackupFilename(CarManagerXml.getOperationsFileName()));
			EngineManagerXml.instance().writeFile(
					fullBackupFilename(EngineManagerXml.getOperationsFileName()));
			TrainManagerXml.instance().writeFile(
					fullBackupFilename(TrainManagerXml.getOperationsFileName()));
			LocationManagerXml.instance().writeFile(
					fullBackupFilename(LocationManagerXml.getOperationsFileName()));
			RouteManagerXml.instance().writeFile(
					fullBackupFilename(RouteManagerXml.getOperationsFileName()));
		} catch (Exception e) {
			log.error("Exception while making backup, may not be complete: "
					+ e);
			return false;
		}
		return true;
	}

	public String[] getBackupList() {
		String[] backupDirectoryNames = {"<Empty>"};
		try {
			File file = new File(backupDirectory);
			if (!file.exists()) {
				log.error("backup directory does not exist");
				return backupDirectoryNames;
			}
			
			backupDirectoryNames = file.list();
			
		} catch (Exception e) {
			log.error("Exception while making backup list, may not be complete: "
					+ e);
		}
		return backupDirectoryNames;
	}
	
	/**
	 * Check to see if a backup operations directory already exists
	 * @param directoryName
	 * @return true if it exists
	 */
	public boolean checkDirectoryExists(String directoryName){
		try {
			File file = new File(backupDirectory + File.separator + directoryName);
			if (file.exists())
				return true;
		} catch (Exception e) {
			log.error("Exception during directory exists check");
		}
		return false;
	}
	
	public boolean restore(String directoryName) {
		return restore(backupDirectory, directoryName);
	}
	
	/**
	 * Copies operation files from directoryName
	 * 
	 * @param directoryName
	 * @return true if successful, false if not.
	 */
	public boolean restore(String directoryPath, String directoryName) {
		try {
			File file = new File(directoryPath + File.separator + directoryName);
			if (!file.exists())
				return false;
			String[] operationFileNames = file.list();
			// check for at least 6 operation files
			if (operationFileNames.length < 6){
				log.error("Only "+operationFileNames.length+" files found in directory "+backupDirectory + File.separator + directoryName);
				return false;
			}
			// TODO check for the correct operation file names
			for (int i = 0; i < operationFileNames.length; i++) {
			    // skip non-xml files
			    if ( ! operationFileNames[i].toUpperCase().endsWith(".XML") )
			         continue;
			    //
				log.debug("found file: " + operationFileNames[i]);
				file = new File(directoryPath + File.separator + directoryName
						+ File.separator + operationFileNames[i]);
				File fileout = new File(operationsDirectory + File.separator + operationFileNames[i]);

				FileReader in = new FileReader(file);
				FileWriter out = new FileWriter(fileout);
				int c;

				while ((c = in.read()) != -1)
					out.write(c);

				in.close();
				out.close();
			}
			return true;
		} catch (Exception e) {
			log.error("Exception while restoring operations files, may not be complete: "
					+ e);
			return false; 
		}
	}
	
	public boolean loadDemoFiles(){
		return restore(XmlFile.xmlDir(), "demoOperations");
	}

	private String fullBackupFilename(String name) {
		return backupDirectory + File.separator + getDirectoryName() + File.separator + name;
	}

	private String operationsDirectory = XmlFile.prefsDir() + "operations";	
	private String backupDirectory = operationsDirectory + File.separator + "backups";
	
	private String defaultDirectoryName = "";
	public String getDirectoryName(){
		if (defaultDirectoryName.equals(""))
			return getDate();
		return defaultDirectoryName;
	}
	
	public String createBackupDirectoryName(){
		String backupName = getDirectoryName();
	   	// make up to 100 backup file names
    	for (int i=0; i<100; i++){
    		if (checkDirectoryExists(backupName)){
    			log.debug("Operations backup directory "+backupName+" already exist");
    			backupName = getDirectoryName()+"_"+i;
    		} else {
    			break;
    		}
    	}
    	return backupName;
	}
	
	/**
	 * Reset operations by deleting xml files, leaves directories and backup files in place
	 */
	public void reset(){
		File files = new File(operationsDirectory);
		if (!files.exists())
			return;
		String[] operationFileNames = files.list();
		for (int i = 0; i < operationFileNames.length; i++) {
		    // skip non-xml files
		    if ( ! operationFileNames[i].toUpperCase().endsWith(".XML") )
		         continue;
		    //
			log.debug("deleting file: " + operationFileNames[i]);
			File file = new File(operationsDirectory + File.separator + operationFileNames[i]);
			if (!file.delete())
				log.debug("file not deleted");
		}
	}
	
	public void setDirectoryName(String name){
		defaultDirectoryName = name;
	}
	
	private String getDate() {
		Calendar now = Calendar.getInstance();
		int month = now.get(Calendar.MONTH) + 1;
		String m = Integer.toString(month);
		if (month < 10){
			m = "0"+Integer.toString(month);
		}
		int day = now.get(Calendar.DATE);
		String d = Integer.toString(day);
		if (day < 10){
			d = "0"+Integer.toString(day);
		}
		String date = "" + now.get(Calendar.YEAR) + "_"	+ m + "_" + d;
		return date;
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(Backup.class.getName());
}
