// RollingStockLogger.java

package jmri.jmrit.operations.rollingstock;

import java.beans.PropertyChangeEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Calendar;
import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
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
public class RollingStockLogger extends XmlFile implements java.beans.PropertyChangeListener {

	File fileLogger;
	private boolean engLog = false; // when true logging engine movements
	private boolean carLog = false; // when true logging car movements
	static final String DEL = ","; // delimiter
	static final String ESC = "\""; // escape character NOI18N

	public RollingStockLogger() {
	}

	/** record the single instance **/
	private static RollingStockLogger _instance = null;

	public static synchronized RollingStockLogger instance() {
		if (_instance == null) {
			if (log.isDebugEnabled())
				log.debug("RollingStockLogger creating instance");
			// create and load
			_instance = new RollingStockLogger();
		}
		if (Control.showInstance && log.isDebugEnabled())
			log.debug("RollingStockLogger returns instance " + _instance);
		return _instance;
	}

	public void enableCarLogging(boolean enable) {
		if (enable) {
			addCarListeners();
		} else {
			removeCarListeners();
		}
	}

	public void enableEngineLogging(boolean enable) {
		if (enable) {
			addEngineListeners();
		} else {
			removeEngineListeners();
		}
	}

	private void createFile() {
		if (!Setup.isEngineLoggerEnabled() && !Setup.isCarLoggerEnabled())
			return;
		if (fileLogger != null)
			return; // log file has already been created
		// create the logging file for this session
		try {
			if (!checkFile(getFullLoggerFileName())) {
				// The file/directory does not exist, create it before writing
				fileLogger = new java.io.File(getFullLoggerFileName());
				File parentDir = fileLogger.getParentFile();
				if (!parentDir.exists()) {
					if (!parentDir.mkdirs()) {
						log.error("logger directory not created");
					}
				}
				if (fileLogger.createNewFile()) {
					log.debug("new file created");
					// add header
					fileOut(getHeader());
				}
			} else {
				fileLogger = new java.io.File(getFullLoggerFileName());
			}
		} catch (Exception e) {
			log.error("Exception while making logging directory: " + e);
		}
	}

	private String getHeader() {
		String header = Bundle.getString("Number") + DEL + Bundle.getString("Road") + DEL
				+ Bundle.getString("Type") + DEL + Bundle.getString("Load") + DEL
				+ Bundle.getString("Location") + DEL + Bundle.getString("Track") + DEL
				+ Bundle.getString("FinalDestination") + DEL + Bundle.getString("Track") + DEL
				+ Bundle.getString("Train") + DEL + Bundle.getString("Moves") + DEL
				+ Bundle.getString("DateAndTime");
		return header;
	}

	private boolean mustHaveTrack = true; // when true only updates that have a track are saved

	private void store(RollingStock rs) {
		// create the log file if needed
		createFile();

		if (rs.getTrack() == null && mustHaveTrack)
			return;

		String rsType = rs.getType();
		if (rsType.contains(DEL)) {
			log.debug("RS (" + rs.toString() + ") has delimiter in type field: " + rsType);
			rsType = ESC + rs.getType() + ESC;
		}
		String rsLocationName = rs.getLocationName();
		if (rsLocationName.contains(DEL)) {
			log.debug("RS (" + rs.toString() + ") has delimiter in location field: "
					+ rsLocationName);
			rsLocationName = ESC + rs.getLocationName() + ESC;
		}
		String rsTrackName = rs.getTrackName();
		if (rsTrackName.contains(DEL)) {
			log.debug("RS (" + rs.toString() + ") has delimiter in track field: " + rsTrackName);
			rsTrackName = ESC + rs.getTrackName() + ESC;
		}
		String carLoad = " ";
		String carFinalDest = " ";
		String carFinalDestTrack = " ";
		if (rs.getClass().equals(Car.class)) {
			Car car = (Car) rs;
			carLoad = car.getLoad();
			if (carLoad.contains(DEL)) {
				log.debug("RS (" + rs.toString() + ") has delimiter in car load field: " + carLoad);
				carLoad = ESC + car.getLoad() + ESC;
			}
			carFinalDest = car.getNextDestinationName();
			if (carFinalDest.contains(DEL)) {
				log.debug("RS (" + rs.toString()
						+ ") has delimiter in car final destination field: " + carFinalDest); // NOI18N
				carFinalDest = ESC + car.getNextDestinationName() + ESC;
			}
			carFinalDestTrack = car.getNextDestTrackName();
			if (carFinalDestTrack.contains(DEL)) {
				log.debug("RS (" + rs.toString()
						+ ") has delimiter in car final destination track field: " // NOI18N
						+ carFinalDestTrack);
				carFinalDestTrack = ESC + car.getNextDestTrackName() + ESC;
			}
		}

		String line = rs.getNumber() + DEL + rs.getRoad() + DEL + rsType + DEL + carLoad + DEL
				+ rsLocationName + DEL + rsTrackName + DEL + carFinalDest + DEL + carFinalDestTrack
				+ DEL + rs.getTrainName() + DEL + rs.getMoves() + DEL + getTime();

		// append line to file
		fileOut(line);
	}

	/*
	 * Appends one line to file.
	 */
	private void fileOut(String line) {
		if (fileLogger == null) {
			log.error("Log file doesn't exist");
			return;
		}

		PrintWriter fileOut;

		try {
			// FileWriter is set to append
			fileOut = new PrintWriter(new BufferedWriter(new FileWriter(fileLogger, true)), true);
		} catch (IOException e) {
			log.error("Exception while opening log file: " + e.getLocalizedMessage());
			return;
		}

		log.debug("Log: " + line);

		fileOut.println(line);
		fileOut.flush();
		fileOut.close();
	}

	private void addCarListeners() {
		if (Setup.isCarLoggerEnabled() && !carLog) {
			log.debug("Rolling Stock Logger adding car listerners");
			carLog = true;
			List<String> cars = CarManager.instance().getList();
			for (int i = 0; i < cars.size(); i++) {
				Car car = CarManager.instance().getById(cars.get(i));
				if (car != null)
					car.addPropertyChangeListener(this);
			}
			// listen for new rolling stock being added
			CarManager.instance().addPropertyChangeListener(this);
		}
	}

	private void addEngineListeners() {
		if (Setup.isEngineLoggerEnabled() && !engLog) {
			engLog = true;
			log.debug("Rolling Stock Logger adding engine listerners");
			List<String> engines = EngineManager.instance().getList();
			for (int i = 0; i < engines.size(); i++) {
				Engine engine = EngineManager.instance().getById(engines.get(i));
				if (engine != null)
					engine.addPropertyChangeListener(this);
			}
			// listen for new rolling stock being added
			EngineManager.instance().addPropertyChangeListener(this);
		}
	}

	private void removeCarListeners() {
		if (carLog) {
			log.debug("Rolling Stock Logger removing car listerners");
			List<String> cars = CarManager.instance().getList();
			for (int i = 0; i < cars.size(); i++) {
				Car car = CarManager.instance().getById(cars.get(i));
				if (car != null)
					car.removePropertyChangeListener(this);
			}
			CarManager.instance().removePropertyChangeListener(this);
		}
		carLog = false;
	}

	private void removeEngineListeners() {
		if (engLog) {
			log.debug("Rolling Stock Logger removing engine listerners");
			List<String> engines = EngineManager.instance().getList();
			for (int i = 0; i < engines.size(); i++) {
				Engine engine = EngineManager.instance().getById(engines.get(i));
				if (engine != null)
					engine.removePropertyChangeListener(this);
			}
			EngineManager.instance().removePropertyChangeListener(this);
		}
		engLog = false;
	}

	public void dispose() {
		removeCarListeners();
		removeEngineListeners();
		fileLogger = null;
	}

	public void propertyChange(PropertyChangeEvent e) {
		if (e.getPropertyName().equals(RollingStock.TRACK_CHANGED_PROPERTY)) {
			if (Control.showProperty && log.isDebugEnabled())
				log.debug("Logger sees property change for car " + e.getSource());
			store((RollingStock) e.getSource());
		}
		if (e.getPropertyName().equals(RollingStockManager.LISTLENGTH_CHANGED_PROPERTY)) {
			if ((Integer) e.getNewValue() > (Integer) e.getOldValue()) {
				// a car or engine has been added
				if (e.getSource().getClass().equals(CarManager.class)) {
					removeCarListeners();
					addCarListeners();
				} else if (e.getSource().getClass().equals(EngineManager.class)) {
					removeEngineListeners();
					addEngineListeners();
				}
			}
		}
	}

	public String getFullLoggerFileName() {
		return loggingDirectory + File.separator + getFileName();
	}

	private String operationsDirectory = OperationsSetupXml.getFileLocation()
			+ OperationsSetupXml.getOperationsDirectoryName();
	private String loggingDirectory = operationsDirectory + File.separator + "logger"; // NOI18N

	public String getDirectoryName() {
		return loggingDirectory;
	}

	public void setDirectoryName(String name) {
		loggingDirectory = name;
	}

	private String fileName;

	public String getFileName() {
		if (fileName == null)
			fileName = getDate() + ".csv"; // NOI18N
		return fileName;
	}

	private String getDate() {
		Calendar now = Calendar.getInstance();
		int month = now.get(Calendar.MONTH) + 1;
		String m = Integer.toString(month);
		if (month < 10) {
			m = "0" + Integer.toString(month);
		}
		int day = now.get(Calendar.DATE);
		String d = Integer.toString(day);
		if (day < 10) {
			d = "0" + Integer.toString(day);
		}
		String date = "" + now.get(Calendar.YEAR) + "_" + m + "_" + d;
		return date;
	}

	private String getTime() {
		return Calendar.getInstance().getTime().toString();
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RollingStockLogger.class
			.getName());
}
