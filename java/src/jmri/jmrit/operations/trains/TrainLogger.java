// TrainLogger.java

package jmri.jmrit.operations.trains;

import java.beans.PropertyChangeEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.ResourceBundle;

import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.setup.Setup;

import java.util.List;

/**
 * Logs rolling stock movements by writing their locations to a file.
 * 
 * @author Daniel Boudreau Copyright (C) 2010
 * @version $Revision$
 */
public class TrainLogger extends XmlFile implements java.beans.PropertyChangeListener{
	
	static final ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.operations.trains.JmritOperationsTrainsBundle");
	
	File _fileLogger;
	private boolean _trainLog = false;	// when true logging train movements
	private final String del = ","; 		// delimiter

	public TrainLogger() {
	}
	
	/** record the single instance **/
	private static TrainLogger _instance = null;

	public static synchronized TrainLogger instance() {
		if (_instance == null) {
			if (log.isDebugEnabled()) log.debug("TrainLogger creating instance");
			// create and load
			_instance = new TrainLogger();
		}
		if (Control.showInstance && log.isDebugEnabled()) log.debug("TrainLogger returns instance "+_instance);
		return _instance;
	}
	
	public void enableTrainLogging(boolean enable){
		if (enable){
			addTrainListeners();
		} else {
			removeTrainListeners();
		}
	}
	
	private void createFile(){
		if (!Setup.isTrainLoggerEnabled())
			return;
		if (_fileLogger != null)
			return;	// log file has already been created
		// create the logging file for this session
		try {
			if (!checkFile(getFullLoggerFileName())) {
				// The file/directory does not exist, create it before writing
				_fileLogger = new java.io.File(getFullLoggerFileName());
				File parentDir = _fileLogger.getParentFile();
				if (!parentDir.exists()) {
					if (!parentDir.mkdirs()) {
						log.error("logger directory not created");
					}
				}
				if (_fileLogger.createNewFile()){
					log.debug("new file created");
					// add header
					fileOut(getHeader());
				}
			} else {
				_fileLogger = new java.io.File(getFullLoggerFileName());
			}
		} catch (Exception e) {
			log.error("Exception while making logging directory: "+ e);
		}
		
	}
	
	private void store(Train train){
		// create train file if needed
		createFile();
		// Note that train status can contain a comma
		String line = train.getName() +del+ "\""+train.getDescription()+"\""
		+del+ "\""+train.getCurrentLocationName()+"\""
		+del+ "\""+train.getNextLocationName()+"\""
		+del+ "\""+ train.getStatus()+"\"" 
		+del+ "\""+ train.getBuildFailedMessage()+"\"" 
		+del+ getTime();
		fileOut(line);
	}
	
	private String getHeader(){
		String header = rb.getString("Name") +del+ rb.getString("Description")
		+del+ rb.getString("Current")
		+del+ rb.getString("NextLocation")
		+del+ rb.getString("Status")
		+del+ rb.getString("BuildMessages")
		+del+ rb.getString("DateAndTime");
		return header;
	}
	
	/*
	 * Appends one line to file.
	 * 
	 */
	private void fileOut(String line){
		if (_fileLogger == null){
			log.error("Log file doesn't exist");
			return;
		}

        PrintWriter fileOut;

		try {
			// FileWriter is set to append
			fileOut = new PrintWriter(new BufferedWriter(new FileWriter(_fileLogger, true)),
					true);
		} catch (IOException e) {
			log.error("Exception while opening log file: "+e.getLocalizedMessage());
			return;
		}
		
		log.debug("Log: "+line);

		fileOut.println(line);
		fileOut.flush();
		fileOut.close();
	}
	
	private void addTrainListeners(){
		if (Setup.isTrainLoggerEnabled() && !_trainLog){
			log.debug("Train Logger adding train listerners");
			_trainLog = true;
			List<String> trains = TrainManager.instance().getTrainsByIdList();
			for (int i=0; i<trains.size(); i++){
				Train train = TrainManager.instance().getTrainById(trains.get(i));
				if (train != null)
					train.addPropertyChangeListener(this);
			}
			// listen for new trains being added
			TrainManager.instance().addPropertyChangeListener(this);
		}
	}
	
	private void removeTrainListeners(){
		log.debug("Train Logger removing train listerners");
		if (_trainLog){
			List<String> trains = TrainManager.instance().getTrainsByIdList();
			for (int i=0; i<trains.size(); i++){
				Train train = TrainManager.instance().getTrainById(trains.get(i));
				if (train != null)
					train.removePropertyChangeListener(this);
			}
			TrainManager.instance().removePropertyChangeListener(this);
		}
		_trainLog = false;
	}

	public void dispose(){
		removeTrainListeners();
	}

	public void propertyChange(PropertyChangeEvent e) {
		if (e.getPropertyName().equals(Train.STATUS_CHANGED_PROPERTY) 
				|| e.getPropertyName().equals(Train.TRAIN_LOCATION_CHANGED_PROPERTY)){
			if(Control.showProperty && log.isDebugEnabled()) 
				log.debug("Train logger sees property change for train "+e.getSource());
			store((Train)e.getSource());
		}
		if (e.getPropertyName().equals(TrainManager.LISTLENGTH_CHANGED_PROPERTY)){
			if ((Integer)e.getNewValue() > (Integer)e.getOldValue()){
				// a car or engine has been added
				removeTrainListeners();
				addTrainListeners();
			}
		}
	}
	
	public String getFullLoggerFileName(){
		return loggingDirectory + File.separator + getFileName();
	}
	
	private String operationsDirectory = OperationsSetupXml.getFileLocation()+OperationsSetupXml.getOperationsDirectoryName();	
	private String loggingDirectory = operationsDirectory + File.separator + "logger";
	
	public String getDirectoryName(){
		return loggingDirectory;
	}
	
	public void setDirectoryName(String name){
		loggingDirectory = name;
	}

	private String fileName;
	public String getFileName(){
		if (fileName == null)
			fileName = "Trains_" + getDate()+".csv";
		return fileName;
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
	
	private String getTime() {
		return Calendar.getInstance().getTime().toString();
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
	.getLogger(TrainLogger.class.getName());
}
