// TrainManager.java

package jmri.jmrit.operations.trains;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JComboBox;

import org.jdom.Attribute;
import org.jdom.Element;

import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarLoad;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.setup.Setup;

/**
 * Manages trains.
 * 
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Daniel Boudreau Copyright (C) 2008, 2009, 2010, 2011, 2012, 2013, 2014
 * @version $Revision$
 */
public class TrainManager implements java.beans.PropertyChangeListener {

	// Train frame attributes
	private String _trainAction = TrainsTableFrame.MOVE; // Trains frame table button action
	private boolean _buildMessages = true; // when true, show build messages
	private boolean _buildReport = false; // when true, print/preview build reports
	private boolean _printPreview = false; // when true, preview train manifest
	private boolean _openFile = false; // when true, open CSV file manifest
	private boolean _runFile = false; // when true, run CSV file manifest

	// Train frame table column widths (12), starts with Time column and ends with Edit
	private int[] _tableColumnWidths = { 50, 50, 72, 100, 140, 120, 120, 120, 120, 120, 90, 70 };

	private int[] _tableScheduleColumnWidths = { 50, 70, 120 };
	private String _trainScheduleActiveId = "";

	// Scripts
	protected List<String> _startUpScripts = new ArrayList<String>(); // list of script pathnames to run at start up
	protected List<String> _shutDownScripts = new ArrayList<String>(); // list of script pathnames to run at shut down

	// property changes
	public static final String LISTLENGTH_CHANGED_PROPERTY = "TrainsListLength"; // NOI18N
	public static final String PRINTPREVIEW_CHANGED_PROPERTY = "TrainsPrintPreview"; // NOI18N
	public static final String OPEN_FILE_CHANGED_PROPERTY = "TrainsOpenFile"; // NOI18N
	public static final String RUN_FILE_CHANGED_PROPERTY = "TrainsRunFile"; // NOI18N
	public static final String TRAIN_ACTION_CHANGED_PROPERTY = "TrainsAction"; // NOI18N
	public static final String GENERATE_CSV_CHANGED_PROPERTY = "TrainsGenerateCSV"; // NOI18N
	public static final String ACTIVE_TRAIN_SCHEDULE_ID = "ActiveTrainScheduleId"; // NOI18N
	

	public TrainManager() {
	}

	/** record the single instance **/
	private static TrainManager _instance = null;
	private int _id = 0; // train ids

	public static synchronized TrainManager instance() {
		if (_instance == null) {
			if (log.isDebugEnabled())
				log.debug("TrainManager creating instance");
			// create and load
			_instance = new TrainManager();
			OperationsSetupXml.instance(); // load setup
			TrainManagerXml.instance(); // load trains
		}
		if (Control.showInstance && log.isDebugEnabled())
			log.debug("TrainManager returns instance " + _instance);
		return _instance;
	}

	/**
	 * 
	 * @return true if build messages are enabled
	 */
	public boolean isBuildMessagesEnabled() {
		return _buildMessages;
	}

	public void setBuildMessagesEnabled(boolean enable) {
		boolean old = _buildMessages;
		_buildMessages = enable;
		firePropertyChange("BuildMessagesEnabled", enable, old); // NOI18N
	}

	/**
	 * 
	 * @return true if build reports are enabled
	 */
	public boolean isBuildReportEnabled() {
		return _buildReport;
	}

	public void setBuildReportEnabled(boolean enable) {
		boolean old = _buildReport;
		_buildReport = enable;
		firePropertyChange("BuildReportEnabled", enable, old); // NOI18N
	}

	/**
	 * 
	 * @return true if open file is enabled
	 */
	public boolean isOpenFileEnabled() {
		return _openFile;
	}

	public void setOpenFileEnabled(boolean enable) {
		boolean old = _openFile;
		_openFile = enable;
		firePropertyChange(OPEN_FILE_CHANGED_PROPERTY, old ? "true" : "false", enable ? "true" // NOI18N
				: "false"); // NOI18N
	}
	
	/**
	 * 
	 * @return true if open file is enabled
	 */
	public boolean isRunFileEnabled() {
		return _runFile;
	}

	public void setRunFileEnabled(boolean enable) {
		boolean old = _runFile;
		_runFile = enable;
		firePropertyChange(RUN_FILE_CHANGED_PROPERTY, old ? "true" : "false", enable ? "true" // NOI18N
				: "false"); // NOI18N
	}

	/**
	 * 
	 * @return true if print preview is enabled
	 */
	public boolean isPrintPreviewEnabled() {
		return _printPreview;
	}

	public void setPrintPreviewEnabled(boolean enable) {
		boolean old = _printPreview;
		_printPreview = enable;
		firePropertyChange(PRINTPREVIEW_CHANGED_PROPERTY, old ? "Preview" : "Print", // NOI18N
				enable ? "Preview" : "Print"); // NOI18N
	}

	public String getTrainsFrameTrainAction() {
		return _trainAction;
	}

	public void setTrainsFrameTrainAction(String action) {
		String old = _trainAction;
		_trainAction = action;
		if (!old.equals(action))
			firePropertyChange(TRAIN_ACTION_CHANGED_PROPERTY, old, action);
	}

	/**
	 * 
	 * @return get an array of table column widths for the trains frame
	 */
	public int[] getTrainsFrameTableColumnWidths() {
		return _tableColumnWidths.clone();
	}

	public int[] getTrainScheduleFrameTableColumnWidths() {
		return _tableScheduleColumnWidths.clone();
	}

	/**
	 * Sets the selected schedule id
	 * 
	 * @param id
	 *            Selected schedule id
	 */
	public void setTrainSecheduleActiveId(String id) {
		String old = _trainScheduleActiveId;
		_trainScheduleActiveId = id;
		if (!old.equals(id))
			firePropertyChange(ACTIVE_TRAIN_SCHEDULE_ID, old, id);
	}

	public String getTrainScheduleActiveId() {
		return _trainScheduleActiveId;
	}

	/**
	 * Add a script to run after trains have been loaded
	 * 
	 * @param pathname
	 *            The script's pathname
	 */
	public void addStartUpScript(String pathname) {
		_startUpScripts.add(pathname);
		firePropertyChange("addStartUpScript", pathname, null); // NOI18N
	}

	public void deleteStartUpScript(String pathname) {
		_startUpScripts.remove(pathname);
		firePropertyChange("deleteStartUpScript", null, pathname); // NOI18N
	}

	/**
	 * Gets a list of pathnames to run after trains have been loaded
	 * 
	 * @return A list of pathnames to run after trains have been loaded
	 */
	public List<String> getStartUpScripts() {
		return _startUpScripts;
	}

	public void runStartUpScripts() {
		List<String> scripts = getStartUpScripts();
		for (int i = 0; i < scripts.size(); i++) {
			jmri.util.PythonInterp.runScript(jmri.util.FileUtil
					.getExternalFilename(getStartUpScripts().get(i)));
		}
	}

	/**
	 * Add a script to run at shutdown
	 * 
	 * @param pathname
	 *            The script's pathname
	 */
	public void addShutDownScript(String pathname) {
		_shutDownScripts.add(pathname);
		firePropertyChange("addShutDownScript", pathname, null); // NOI18N
	}

	public void deleteShutDownScript(String pathname) {
		_shutDownScripts.remove(pathname);
		firePropertyChange("deleteShutDownScript", null, pathname); // NOI18N
	}

	/**
	 * Gets a list of pathnames to run at shutdown
	 * 
	 * @return A list of pathnames to run at shutdown
	 */
	public List<String> getShutDownScripts() {
		return _shutDownScripts;
	}

	public void runShutDownScripts() {
		List<String> scripts = getShutDownScripts();
		for (int i = 0; i < scripts.size(); i++) {
			jmri.util.PythonInterp.runScript(jmri.util.FileUtil
					.getExternalFilename(getShutDownScripts().get(i)));
		}
	}

    @edu.umd.cs.findbugs.annotations.SuppressWarnings(value="ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD")
	public void dispose() {
		_trainHashTable.clear();
		_id = 0;
		_instance = null;	// we need to reset the instance for testing purposes
	}

	// stores known Train instances by id
	private Hashtable<String, Train> _trainHashTable = new Hashtable<String, Train>();

	/**
	 * @return requested Train object or null if none exists
	 */

	public Train getTrainByName(String name) {
		if (!TrainManagerXml.instance().isTrainFileLoaded())
			log.error("TrainManager getTrainByName called before trains completely loaded!");
		Train train;
		Enumeration<Train> en = _trainHashTable.elements();
		while (en.hasMoreElements()) {
			train = en.nextElement();
			// windows file names are case independent
			if (train.getName().toLowerCase().equals(name.toLowerCase()))
				return train;
		}
		log.debug("train " + name + " doesn't exist");
		return null;
	}

	public Train getTrainById(String id) {
		if (!TrainManagerXml.instance().isTrainFileLoaded())
			log.error("TrainManager getTrainById called before trains completely loaded!");
		return _trainHashTable.get(id);
	}

	/**
	 * Finds an existing train or creates a new train if needed requires train's name creates a unique id for this train
	 * 
	 * @param name
	 * 
	 * @return new train or existing train
	 */
	public Train newTrain(String name) {
		Train train = getTrainByName(name);
		if (train == null) {
			_id++;
			train = new Train(Integer.toString(_id), name);
			Integer oldSize = Integer.valueOf(_trainHashTable.size());
			_trainHashTable.put(train.getId(), train);
			firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize,
					Integer.valueOf(_trainHashTable.size()));
		}
		return train;
	}

	/**
	 * Remember a NamedBean Object created outside the manager.
	 */
	public void register(Train train) {
		Integer oldSize = Integer.valueOf(_trainHashTable.size());
		_trainHashTable.put(train.getId(), train);
		// find last id created
		int id = Integer.parseInt(train.getId());
		if (id > _id)
			_id = id;
		firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize,
				Integer.valueOf(_trainHashTable.size()));
		// listen for name and state changes to forward
	}

	/**
	 * Forget a NamedBean Object created outside the manager.
	 */
	public void deregister(Train train) {
		if (train == null)
			return;
		train.dispose();
		Integer oldSize = Integer.valueOf(_trainHashTable.size());
		_trainHashTable.remove(train.getId());
		firePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize,
				Integer.valueOf(_trainHashTable.size()));
	}

	public void replaceLoad(String type, String oldLoadName, String newLoadName) {
		List<Train> trains = getTrainsByIdList();
		for (int i = 0; i < trains.size(); i++) {
			Train train = trains.get(i);
			String[] loadNames = train.getLoadNames();
			for (int j = 0; j < loadNames.length; j++) {
				if (loadNames[j].equals(oldLoadName)) {
					train.deleteLoadName(oldLoadName);
					if (newLoadName != null)
						train.addLoadName(newLoadName);
				}
				// adjust combination car type and load name
   				String[] splitLoad = loadNames[j].split(CarLoad.SPLIT_CHAR);
				if (splitLoad.length > 1) {
					if (splitLoad[0].equals(type) && splitLoad[1].equals(oldLoadName)) {
						train.deleteLoadName(loadNames[j]);
						if (newLoadName != null) {
							train.addLoadName(type + CarLoad.SPLIT_CHAR + newLoadName);
						}
					}
				}
			}
		}
	}

	/**
	 * 
	 * @return true if there are any trains built
	 */
	public boolean getAnyTrainBuilt() {
		List<Train> trains = getTrainsByIdList();
		for (int i = 0; i < trains.size(); i++) {
			if (trains.get(i).isBuilt())
				return true;
		}
		return false;
	}

	/**
	 * 
	 * @param car
	 * @return Train that can service car from its current location to the its destination.
	 */
	public Train getTrainForCar(Car car, PrintWriter buildReport) {
		log.debug("Find train for car (" + car.toString() + ") location (" + car.getLocationName() + ", " // NOI18N
				+ car.getTrackName() + ") destination (" + car.getDestinationName() + ", " // NOI18N
				+ car.getDestinationTrackName() + ")"); // NOI18N
		if (Setup.getRouterBuildReportLevel().equals(Setup.BUILD_REPORT_VERY_DETAILED)) {
			TrainCommon.addLine(buildReport, Setup.BUILD_REPORT_VERY_DETAILED, MessageFormat.format(Bundle
					.getMessage("trainFindForCar"), new Object[] { car.toString(), car.getLocationName(),
					car.getTrackName(), car.getDestinationName(), car.getDestinationTrackName() }));
		}
		List<Train> trains = getTrainsByIdList();
		for (int i = 0; i < trains.size(); i++) {
			Train train = trains.get(i);
			if (Setup.isOnlyActiveTrainsEnabled() && !train.isBuildEnabled())
				continue;
			// does this train service this car?
			if (train.services(buildReport, car))
				return train;
		}
		return null;
	}

	/**
	 * Sort by train name
	 * 
	 * @return list of train ids ordered by name
	 */
	public List<Train> getTrainsByNameList() {
		return getTrainsByList(getList(), GET_TRAIN_NAME);
	}

	/**
	 * Sort by train departure time
	 * 
	 * @return list of train ids ordered by departure time
	 */
	public List<Train> getTrainsByTimeList() {
		return getTrainsByIntList(getTrainsByNameList(), GET_TRAIN_TIME);
	}

	/**
	 * Sort by train departure name
	 * 
	 * @return list of train ids ordered by departure name
	 */
	public List<Train> getTrainsByDepartureList() {
		return getTrainsByList(getTrainsByNameList(), GET_TRAIN_DEPARTES_NAME);
	}

	/**
	 * Sort by train termination name
	 * 
	 * @return list of train ids ordered by termination name
	 */
	public List<Train> getTrainsByTerminatesList() {
		return getTrainsByList(getTrainsByNameList(), GET_TRAIN_TERMINATES_NAME);
	}

	/**
	 * Sort by train route name
	 * 
	 * @return list of train ids ordered by route name
	 */
	public List<Train> getTrainsByRouteList() {
		return getTrainsByList(getTrainsByNameList(), GET_TRAIN_ROUTE_NAME);
	}

	/**
	 * Sort by train route name
	 * 
	 * @return list of train ids ordered by route name
	 */
	public List<Train> getTrainsByStatusList() {
		return getTrainsByList(getTrainsByNameList(), GET_TRAIN_STATUS);
	}

	/**
	 * Sort by train id
	 * 
	 * @return list of train ids ordered by id
	 */
	public List<Train> getTrainsByIdList() {
		return getTrainsByIntList(getList(), GET_TRAIN_ID);
	}

	private List<Train> getTrainsByList(List<Train> sortList, int attribute) {
		List<Train> out = new ArrayList<Train>();
		for (int i = 0; i < sortList.size(); i++) {
			String trainAttribute = (String) getTrainAttribute(sortList.get(i), attribute);
			for (int j = 0; j < out.size(); j++) {
				if (trainAttribute.compareToIgnoreCase((String)getTrainAttribute(out.get(j), attribute)) < 0) {
					out.add(j, sortList.get(i));
					break;
				}
			}
			if (!out.contains(sortList.get(i))) {
				out.add(sortList.get(i));
			}
		}
		return out;
	}

	private List<Train> getTrainsByIntList(List<Train> sortList, int attribute) {
		List<Train> out = new ArrayList<Train>();
		for (int i = 0; i < sortList.size(); i++) {
			int trainAttribute = (Integer) getTrainAttribute(sortList.get(i), attribute);
			for (int j = 0; j < out.size(); j++) {
				if (trainAttribute < (Integer) getTrainAttribute(out.get(j), attribute)) {
					out.add(j, sortList.get(i));
					break;
				}
			}
			if (!out.contains(sortList.get(i))) {
				out.add(sortList.get(i));
			}
		}
		return out;
	}

	// the various sort options for trains
	private static final int GET_TRAIN_DEPARTES_NAME = 0;
	private static final int GET_TRAIN_NAME = 1;
	private static final int GET_TRAIN_ROUTE_NAME = 2;
	private static final int GET_TRAIN_TERMINATES_NAME = 3;
	private static final int GET_TRAIN_TIME = 4;
	private static final int GET_TRAIN_STATUS = 5;
	private static final int GET_TRAIN_ID = 6;

	private Object getTrainAttribute(Train train, int attribute) {
		switch (attribute) {
		case GET_TRAIN_DEPARTES_NAME:
			return train.getTrainDepartsName();
		case GET_TRAIN_NAME:
			return train.getName();
		case GET_TRAIN_ROUTE_NAME:
			return train.getTrainRouteName();
		case GET_TRAIN_TERMINATES_NAME:
			return train.getTrainTerminatesName();
		case GET_TRAIN_TIME:
			return train.getDepartTimeMinutes();
		case GET_TRAIN_STATUS:
			return train.getStatus();
		case GET_TRAIN_ID:
			return Integer.parseInt(train.getId());
		default:
			return "unknown"; // NOI18N
		}
	}

	private List<Train> getList() {
		if (!TrainManagerXml.instance().isTrainFileLoaded())
			log.error("TrainManager getList called before trains completely loaded!");
		List<Train> out = new ArrayList<Train>();
		Enumeration<Train> en = _trainHashTable.elements();
		while (en.hasMoreElements()) {
			out.add(en.nextElement());
		}
		return out;
	}

	public JComboBox getComboBox() {
		JComboBox box = new JComboBox();
		box.addItem("");
		List<Train> trains = getTrainsByNameList();
		for (int i = 0; i < trains.size(); i++) {
			box.addItem(trains.get(i));
		}
		return box;
	}

	public void updateComboBox(JComboBox box) {
		box.removeAllItems();
		box.addItem("");
		List<Train> trains = getTrainsByNameList();
		for (int i = 0; i < trains.size(); i++) {
			box.addItem(trains.get(i));
		}
	}

	/**
	 * Update combo box with trains that will service this car
	 * 
	 * @param box
	 *            the combo box to update
	 * @param car
	 *            the car to be serviced
	 */
	public void updateComboBox(JComboBox box, Car car) {
		box.removeAllItems();
		box.addItem("");
		List<Train> trains = getTrainsByNameList();
		for (int i = 0; i < trains.size(); i++) {
			Train train = trains.get(i);
			if (train.services(car))
				box.addItem(train);
		}
	}

	/**
	 * @return Number of trains
	 */
	public int numEntries() {
		return _trainHashTable.size();
	}

	/**
	 * Makes a copy of an existing train. Only the train's description isn't copied.
	 * 
	 * @param train
	 *            the train to copy
	 * @param trainName
	 *            the name of the new train
	 * @return a copy of train
	 */
	public Train copyTrain(Train train, String trainName) {
		Train newTrain = newTrain(trainName);
		// route, departure time and types
		newTrain.setRoute(train.getRoute());
		newTrain.setTrainSkipsLocations(train.getTrainSkipsLocations());
		newTrain.setDepartureTime(train.getDepartureTimeHour(), train.getDepartureTimeMinute());
		newTrain._typeList.clear(); // remove all types loaded by create
		newTrain.setTypeNames(train.getTypeNames());
		// set road, load, and owner options
		newTrain.setRoadOption(train.getRoadOption());
		newTrain.setRoadNames(train.getRoadNames());
		newTrain.setLoadOption(train.getLoadOption());
		newTrain.setLoadNames(train.getLoadNames());
		newTrain.setOwnerOption(train.getOwnerOption());
		newTrain.setOwnerNames(train.getOwnerNames());
		// build dates
		newTrain.setBuiltStartYear(train.getBuiltStartYear());
		newTrain.setBuiltEndYear(train.getBuiltEndYear());
		// locos start of route
		newTrain.setNumberEngines(train.getNumberEngines());
		newTrain.setEngineModel(train.getEngineModel());
		newTrain.setEngineRoad(train.getEngineRoad());
		newTrain.setRequirements(train.getRequirements());
		newTrain.setCabooseRoad(train.getCabooseRoad());
		// second leg
		newTrain.setSecondLegNumberEngines(train.getSecondLegNumberEngines());
		newTrain.setSecondLegEngineModel(train.getSecondLegEngineModel());
		newTrain.setSecondLegEngineRoad(train.getSecondLegEngineRoad());
		newTrain.setSecondLegOptions(train.getSecondLegOptions());
		newTrain.setSecondLegCabooseRoad(train.getSecondLegCabooseRoad());
		newTrain.setSecondLegStartLocation(train.getSecondLegStartLocation());
		newTrain.setSecondLegEndLocation(train.getSecondLegEndLocation());
		// third leg
		newTrain.setThirdLegNumberEngines(train.getThirdLegNumberEngines());
		newTrain.setThirdLegEngineModel(train.getThirdLegEngineModel());
		newTrain.setThirdLegEngineRoad(train.getThirdLegEngineRoad());
		newTrain.setThirdLegOptions(train.getThirdLegOptions());
		newTrain.setThirdLegCabooseRoad(train.getThirdLegCabooseRoad());
		newTrain.setThirdLegStartLocation(train.getThirdLegStartLocation());
		newTrain.setThirdLegEndLocation(train.getThirdLegEndLocation());
		// scripts
		for (int i = 0; i < train.getBuildScripts().size(); i++)
			newTrain.addBuildScript(train.getBuildScripts().get(i));
		for (int i = 0; i < train.getMoveScripts().size(); i++)
			newTrain.addMoveScript(train.getMoveScripts().get(i));
		for (int i = 0; i < train.getTerminationScripts().size(); i++)
			newTrain.addTerminationScript(train.getTerminationScripts().get(i));
		// manifest options
		newTrain.setRailroadName(train.getRailroadName());
		newTrain.setManifestLogoURL(train.getManifestLogoURL());
		newTrain.setShowArrivalAndDepartureTimes(train.isShowArrivalAndDepartureTimesEnabled());
		// build options
		newTrain.setAllowLocalMovesEnabled(train.isAllowLocalMovesEnabled());
		newTrain.setAllowReturnToStagingEnabled(train.isAllowReturnToStagingEnabled());
		newTrain.setAllowThroughCarsEnabled(train.isAllowThroughCarsEnabled());
		newTrain.setBuildConsistEnabled(train.isBuildConsistEnabled());
		newTrain.setBuildTrainNormalEnabled(train.isBuildTrainNormalEnabled());
		newTrain.setSendCarsToTerminalEnabled(train.isSendCarsToTerminalEnabled());
		newTrain.setServiceAllCarsWithFinalDestinationsEnabled(train.isServiceAllCarsWithFinalDestinationsEnabled());		
		// comment
		newTrain.setComment(train.getComment());
		// description
		newTrain.setDescription(train.getDescription());
		return newTrain;
	}
	
	/**
	 * Provides a list of trains ordered by arrival time to a location
	 * 
	 * @param location
	 *            The location
	 * @return A list of trains ordered by arrival time.
	 */
	public List<Train> getTrainsArrivingThisLocationList(Location location) {
		// get a list of trains
		List<Train> trainByTime = getTrainsByTimeList();
		List<Train> out = new ArrayList<Train>();
		List<Integer> arrivalTimes = new ArrayList<Integer>();
		for (int i = 0; i < trainByTime.size(); i++) {
			Train train = trainByTime.get(i);
			if (!train.isBuilt())
				continue; // train wasn't built so skip
			Route route = train.getRoute();
			if (route == null)
				continue; // no route for this train
			List<String> routeList = route.getLocationsBySequenceList();
			for (int r = 0; r < routeList.size(); r++) {
				RouteLocation rl = route.getLocationById(routeList.get(r));
				if (TrainCommon.splitString(rl.getName()).equals(TrainCommon.splitString(location.getName()))) {
					int expectedArrivalTime = train.getExpectedTravelTimeInMinutes(rl);
					// is already serviced then "-1"
					if (expectedArrivalTime == -1) {
						out.add(0, train); // place all trains that have already been serviced at the start
						arrivalTimes.add(0, expectedArrivalTime);
					}
					// if the train is in route, then expected arrival time is in minutes
					else if (train.isTrainInRoute()) {
						for (int j = 0; j < out.size(); j++) {
							Train t = out.get(j);
							int time = arrivalTimes.get(j);
							if (t.isTrainInRoute() && expectedArrivalTime < time) {
								out.add(j, train);
								arrivalTimes.add(j, expectedArrivalTime);
								break;
							}
							if (!t.isTrainInRoute()) {
								out.add(j, train);
								arrivalTimes.add(j, expectedArrivalTime);
								break;
							}
						}
						// Train has not departed
					} else {
						for (int j = 0; j < out.size(); j++) {
							Train t = out.get(j);
							int time = arrivalTimes.get(j);
							if (!t.isTrainInRoute() && expectedArrivalTime < time) {
								out.add(j, train);
								arrivalTimes.add(j, expectedArrivalTime);
								break;
							}
						}
					}
					if (!out.contains(train)) {
						out.add(train);
						arrivalTimes.add(expectedArrivalTime);
					}
					break; // done
				}
			}
		}
		return out;
	}
	
	public void setGenerateCsvManifestEnabled(boolean enabled) {
		boolean old = Setup.isGenerateCsvManifestEnabled();
		Setup.setGenerateCsvManifestEnabled(enabled);
		firePropertyChange(GENERATE_CSV_CHANGED_PROPERTY, old, enabled);
	}

	
	/**
	 * Loads train icons if needed
	 */
	public void loadTrainIcons() {
		List<Train> trainList = getTrainsByIdList();
		for (int i = 0; i < trainList.size(); i++) {
			trainList.get(i).loadTrainIcon();
		}
	}
	
	/**
	 * Sets the switch list status for all built trains.  Used for
	 * switch lists in consolidated mode.
	 */
	public void setTrainsSwitchListStatus(String status) {
		List<Train> trains = getTrainsByTimeList();
		for (int i = 0; i < trains.size(); i++) {
			Train train = trains.get(i);
			if (!train.isBuilt())
				continue; // train isn't built so skip
			train.setSwitchListStatus(status);
		}
	}
	
	/**
	 * Sets all built trains manifests to modified.  This causes the
	 * train's manifest to be recreated.
	 */
	public void setTrainsModified() {
		List<Train> trains = getTrainsByTimeList();
		for (int i = 0; i < trains.size(); i++) {
			Train train = trains.get(i);
			if (!train.isBuilt() || train.isTrainInRoute())
				continue; // train wasn't built or in route, so skip
			train.setModified(true);
		}
	}
	
	public void load(Element root) {
		if (root.getChild(Xml.OPTIONS) != null) {
			Element options = root.getChild(Xml.OPTIONS);
			TrainCustomManifest.load(options);
			TrainCustomSwitchList.load(options);
			Element e = options.getChild(Xml.TRAIN_OPTIONS);
			Attribute a;
			if (e != null) {
				if ((a = e.getAttribute(Xml.BUILD_MESSAGES)) != null)
					_buildMessages = a.getValue().equals(Xml.TRUE);
				if ((a = e.getAttribute(Xml.BUILD_REPORT)) != null)
					_buildReport = a.getValue().equals(Xml.TRUE);
				if ((a = e.getAttribute(Xml.PRINT_PREVIEW)) != null)
					_printPreview = a.getValue().equals(Xml.TRUE);
				if ((a = e.getAttribute(Xml.OPEN_FILE)) != null)
					_openFile = a.getValue().equals(Xml.TRUE);
				if ((a = e.getAttribute(Xml.RUN_FILE)) != null)
					_runFile = a.getValue().equals(Xml.TRUE);
				if ((a = e.getAttribute(Xml.TRAIN_ACTION)) != null)
					_trainAction = a.getValue();

				// TODO This here is for backwards compatibility, remove after next major release
				if ((a = e.getAttribute(Xml.COLUMN_WIDTHS)) != null) {
					String[] widths = a.getValue().split(" ");
					for (int i = 0; i < widths.length; i++) {
						try {
							_tableColumnWidths[i] = Integer.parseInt(widths[i]);
						} catch (NumberFormatException ee) {
							log.error("Number format exception when reading trains column widths");
						}
					}
				}
			}

			e = options.getChild(Xml.TRAIN_SCHEDULE_OPTIONS);
			if (e != null) {
				if ((a = e.getAttribute(Xml.ACTIVE_ID)) != null) {
					_trainScheduleActiveId = a.getValue();
				}
				// TODO This here is for backwards compatibility, remove after next major release
				if ((a = e.getAttribute(Xml.COLUMN_WIDTHS)) != null) {
					String[] widths = a.getValue().split(" ");
					_tableScheduleColumnWidths = new int[widths.length];
					for (int i = 0; i < widths.length; i++) {
						try {
							_tableScheduleColumnWidths[i] = Integer.parseInt(widths[i]);
						} catch (NumberFormatException ee) {
							log.error("Number format exception when reading trains column widths");
						}
					}
				}
			}
			// check for scripts
			if (options.getChild(Xml.SCRIPTS) != null) {
				@SuppressWarnings("unchecked")
				List<Element> lm = options.getChild(Xml.SCRIPTS).getChildren(Xml.START_UP);
				for (int i = 0; i < lm.size(); i++) {
					Element es = lm.get(i);
					if ((a = es.getAttribute(Xml.NAME)) != null) {
						addStartUpScript(a.getValue());
					}
				}
				@SuppressWarnings("unchecked")
				List<Element> lt = options.getChild(Xml.SCRIPTS).getChildren(Xml.SHUT_DOWN);
				for (int i = 0; i < lt.size(); i++) {
					Element es = lt.get(i);
					if ((a = es.getAttribute(Xml.NAME)) != null) {
						addShutDownScript(a.getValue());
					}
				}
			}
		}
		if (root.getChild(Xml.TRAINS) != null) {
			@SuppressWarnings("unchecked")
			List<Element> l = root.getChild(Xml.TRAINS).getChildren(Xml.TRAIN);
			if (log.isDebugEnabled())
				log.debug("readFile sees " + l.size() + " trains");
			for (int i = 0; i < l.size(); i++) {
				register(new Train(l.get(i)));
			}
		}
	}

	/**
	 * Create an XML element to represent this Entry. This member has to remain synchronized with the detailed DTD in
	 * operations-trains.dtd.
	 * 
	 */
	public void store(Element root) {
		Element options = new Element(Xml.OPTIONS);
		Element e = new Element(Xml.TRAIN_OPTIONS);
		e.setAttribute(Xml.BUILD_MESSAGES, isBuildMessagesEnabled() ? Xml.TRUE : Xml.FALSE);
		e.setAttribute(Xml.BUILD_REPORT, isBuildReportEnabled() ? Xml.TRUE : Xml.FALSE);
		e.setAttribute(Xml.PRINT_PREVIEW, isPrintPreviewEnabled() ? Xml.TRUE : Xml.FALSE);
		e.setAttribute(Xml.OPEN_FILE, isOpenFileEnabled() ? Xml.TRUE : Xml.FALSE);
		e.setAttribute(Xml.RUN_FILE, isRunFileEnabled() ? Xml.TRUE : Xml.FALSE);
		e.setAttribute(Xml.TRAIN_ACTION, getTrainsFrameTrainAction());
		options.addContent(e);
		// now save train schedule options
		e = new Element(Xml.TRAIN_SCHEDULE_OPTIONS);
		e.setAttribute(Xml.ACTIVE_ID, getTrainScheduleActiveId());
		options.addContent(e);

		if (getStartUpScripts().size() > 0 || getShutDownScripts().size() > 0) {
			// save list of shutdown scripts
			Element es = new Element(Xml.SCRIPTS);
			for (int i = 0; i < getStartUpScripts().size(); i++) {
				Element em = new Element(Xml.START_UP);
				em.setAttribute(Xml.NAME, getStartUpScripts().get(i));
				es.addContent(em);
			}
			// save list of termination scripts
			for (int i = 0; i < getShutDownScripts().size(); i++) {
				Element et = new Element(Xml.SHUT_DOWN);
				et.setAttribute(Xml.NAME, getShutDownScripts().get(i));
				es.addContent(et);
			}
			options.addContent(es);
		}
		
		TrainCustomManifest.store(options);	// save custom manifest elements
		TrainCustomSwitchList.store(options);	// save custom manifest elements
		
		root.addContent(options);

		Element trains = new Element(Xml.TRAINS);
		root.addContent(trains);
		// add entries
		List<Train> trainList = getTrainsByIdList();
		for (int i = 0; i < trainList.size(); i++) {
			trains.addContent( trainList.get(i).store());
		}
	}

	/**
	 * Check for car type and road name replacements. Also check for engine type replacement.
	 * 
	 */
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		log.debug("TrainManager sees property change: " + e.getPropertyName() + " old: "
				+ e.getOldValue() + " new " + e.getNewValue()); // NOI18N
		// TODO use listener to determine if load name has changed
		// if (e.getPropertyName().equals(CarLoads.LOAD_NAME_CHANGED_PROPERTY)){
		// replaceLoad((String)e.getOldValue(), (String)e.getNewValue());
		// }
	}

	java.beans.PropertyChangeSupport pcs = new java.beans.PropertyChangeSupport(this);

	public synchronized void addPropertyChangeListener(java.beans.PropertyChangeListener l) {
		pcs.addPropertyChangeListener(l);
	}

	public synchronized void removePropertyChangeListener(java.beans.PropertyChangeListener l) {
		pcs.removePropertyChangeListener(l);
	}

	private void firePropertyChange(String p, Object old, Object n) {
		TrainManagerXml.instance().setDirty(true);
		pcs.firePropertyChange(p, old, n);
	}

	static Logger log = LoggerFactory.getLogger(TrainManager.class
			.getName());

}

/* @(#)TrainManager.java */
