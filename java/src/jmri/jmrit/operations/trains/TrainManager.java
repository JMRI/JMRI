package jmri.jmrit.operations.trains;

import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;

import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import org.jdom2.Attribute;
import org.jdom2.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.InstanceManagerAutoInitialize;
import jmri.jmrit.operations.locations.Location;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarLoad;
import jmri.jmrit.operations.routes.Route;
import jmri.jmrit.operations.routes.RouteLocation;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.setup.Setup;
import jmri.jmrit.operations.trains.excel.TrainCustomManifest;
import jmri.jmrit.operations.trains.excel.TrainCustomSwitchList;
import jmri.jmrit.operations.trains.schedules.TrainScheduleManager;
import jmri.script.JmriScriptEngineManager;
import jmri.util.ColorUtil;

/**
 * Manages trains.
 *
 * @author Bob Jacobsen Copyright (C) 2003
 * @author Daniel Boudreau Copyright (C) 2008, 2009, 2010, 2011, 2012, 2013,
 * 2014
 */
public class TrainManager implements InstanceManagerAutoDefault, InstanceManagerAutoInitialize, PropertyChangeListener {

    static final String NONE = "";

    // Train frame attributes
    private String _trainAction = TrainsTableFrame.MOVE; // Trains frame table button action
    private boolean _buildMessages = true; // when true, show build messages
    private boolean _buildReport = false; // when true, print/preview build reports
    private boolean _printPreview = false; // when true, preview train manifest
    private boolean _openFile = false; // when true, open CSV file manifest
    private boolean _runFile = false; // when true, run CSV file manifest
    
    // Conductor attributes
    private boolean _showLocationHyphenName = false;

    // Trains window row colors
    private boolean _rowColorManual = true; // when true train colors are manually assigned
    private String _rowColorBuilt = NONE; // row color when train is built
    private String _rowColorBuildFailed = NONE; // row color when train build failed
    private String _rowColorTrainEnRoute = NONE; // row color when train is en route
    private String _rowColorTerminated = NONE; // row color when train is terminated

    // Scripts
    protected List<String> _startUpScripts = new ArrayList<>(); // list of script pathnames to run at start up
    protected List<String> _shutDownScripts = new ArrayList<>(); // list of script pathnames to run at shut down

    // property changes
    public static final String LISTLENGTH_CHANGED_PROPERTY = "TrainsListLength"; // NOI18N
    public static final String PRINTPREVIEW_CHANGED_PROPERTY = "TrainsPrintPreview"; // NOI18N
    public static final String OPEN_FILE_CHANGED_PROPERTY = "TrainsOpenFile"; // NOI18N
    public static final String RUN_FILE_CHANGED_PROPERTY = "TrainsRunFile"; // NOI18N
    public static final String TRAIN_ACTION_CHANGED_PROPERTY = "TrainsAction"; // NOI18N
//    public static final String ACTIVE_TRAIN_SCHEDULE_ID = "ActiveTrainScheduleId"; // NOI18N
    public static final String ROW_COLOR_NAME_CHANGED_PROPERTY = "TrainsRowColorChange"; // NOI18N
    public static final String TRAINS_BUILT_CHANGED_PROPERTY = "TrainsBuiltChange"; // NOI18N

    public TrainManager() {
    }

    private int _id = 0; // train ids

    /**
     * Get the default instance of this class.
     *
     * @return the default instance of this class
     * @deprecated since 4.9.2; use
     * {@link jmri.InstanceManager#getDefault(java.lang.Class)} instead
     */
    @Deprecated
    public static synchronized TrainManager instance() {
        jmri.util.Log4JUtil.deprecationWarning(log, "instance");        
        return InstanceManager.getDefault(TrainManager.class);
    }

    /**
     * Get the number of items in the roster
     *
     * @return Number of trains in the roster
     */
    public int getNumEntries() {
        return _trainHashTable.size();
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
        setDirtyAndFirePropertyChange("BuildMessagesEnabled", enable, old); // NOI18N
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
        setDirtyAndFirePropertyChange("BuildReportEnabled", enable, old); // NOI18N
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
        setDirtyAndFirePropertyChange(OPEN_FILE_CHANGED_PROPERTY, old ? "true" : "false", enable ? "true" // NOI18N
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
        setDirtyAndFirePropertyChange(RUN_FILE_CHANGED_PROPERTY, old ? "true" : "false", enable ? "true" // NOI18N
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
        setDirtyAndFirePropertyChange(PRINTPREVIEW_CHANGED_PROPERTY, old ? "Preview" : "Print", // NOI18N
                enable ? "Preview" : "Print"); // NOI18N
    }
    
    /**
     * When true show entire location name including hyphen
     * @return true when showing entire location name
     */
    public boolean isShowLocationHyphenNameEnabled() {
        return _showLocationHyphenName;
    }

    public String getTrainsFrameTrainAction() {
        return _trainAction;
    }

    public void setTrainsFrameTrainAction(String action) {
        String old = _trainAction;
        _trainAction = action;
        if (!old.equals(action)) {
            setDirtyAndFirePropertyChange(TRAIN_ACTION_CHANGED_PROPERTY, old, action);
        }
    }

    /**
     * Sets the selected schedule id
     *
     * @param id Selected schedule id
     * Moved to TrainScheduleManager.java
     * @deprecated at or before 4.13.7
     */
    @Deprecated  // at or before 4.13.7
    public void setTrainSecheduleActiveId(String id) {
        InstanceManager.getDefault(TrainScheduleManager.class).setTrainScheduleActiveId(id);
    }

    /**
     * @deprecated at or before 4.13.7
     * Moved to TrainScheduleManager.java
     * @return active schedule id
     */
    @Deprecated // at or before 4.13.7
    public String getTrainScheduleActiveId() {
        return InstanceManager.getDefault(TrainScheduleManager.class).getTrainScheduleActiveId();
    }

    /**
     * Add a script to run after trains have been loaded
     *
     * @param pathname The script's pathname
     */
    public void addStartUpScript(String pathname) {
        _startUpScripts.add(pathname);
        setDirtyAndFirePropertyChange("addStartUpScript", pathname, null); // NOI18N
    }

    public void deleteStartUpScript(String pathname) {
        _startUpScripts.remove(pathname);
        setDirtyAndFirePropertyChange("deleteStartUpScript", null, pathname); // NOI18N
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
        // use thread to prevent object (Train) thread lock
        Thread scripts = new Thread(new Runnable() {
            @Override
            public void run() {
                for (String scriptPathName : getStartUpScripts()) {
                    try {
                        JmriScriptEngineManager.getDefault()
                                .runScript(new File(jmri.util.FileUtil.getExternalFilename(scriptPathName)));
                    } catch (Exception e) {
                        log.error("Problem with script: {}", scriptPathName);
                    }
                }
            }
        });
        scripts.setName("Startup Scripts"); // NOI18N
        scripts.start();
    }

    /**
     * Add a script to run at shutdown
     *
     * @param pathname The script's pathname
     */
    public void addShutDownScript(String pathname) {
        _shutDownScripts.add(pathname);
        setDirtyAndFirePropertyChange("addShutDownScript", pathname, null); // NOI18N
    }

    public void deleteShutDownScript(String pathname) {
        _shutDownScripts.remove(pathname);
        setDirtyAndFirePropertyChange("deleteShutDownScript", null, pathname); // NOI18N
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
        for (String scriptPathName : getShutDownScripts()) {
            try {
                JmriScriptEngineManager.getDefault()
                        .runScript(new File(jmri.util.FileUtil.getExternalFilename(scriptPathName)));
            } catch (Exception e) {
                log.error("Problem with script: {}", scriptPathName);
            }
        }
    }
    
    public boolean hasRoadRestrictions() {
        for (Train train : getList()) {
            if (!train.getRoadOption().equals(Train.ALL_ROADS)) {
                return true;
            }
        }
        return false;
    }
    
    public boolean hasLoadRestrictions() {
        for (Train train : getList()) {
            if (!train.getLoadOption().equals(Train.ALL_LOADS)) {
                return true;
            }
        }
        return false;
    }

    public void dispose() {
        _trainHashTable.clear();
        _id = 0;
    }

    // stores known Train instances by id
    private final Hashtable<String, Train> _trainHashTable = new Hashtable<>();

    /**
     * @param name The train's name.
     * @return requested Train object or null if none exists
     */
    public Train getTrainByName(String name) {
        if (!InstanceManager.getDefault(TrainManagerXml.class).isTrainFileLoaded()) {
            log.error("TrainManager getTrainByName called before trains completely loaded!");
        }
        Train train;
        Enumeration<Train> en = _trainHashTable.elements();
        while (en.hasMoreElements()) {
            train = en.nextElement();
            // windows file names are case independent
            if (train.getName().toLowerCase().equals(name.toLowerCase())) {
                return train;
            }
        }
        log.debug("Train ({}) doesn't exist", name);
        return null;
    }

    public Train getTrainById(String id) {
        if (!InstanceManager.getDefault(TrainManagerXml.class).isTrainFileLoaded()) {
            log.error("TrainManager getTrainById called before trains completely loaded!");
        }
        return _trainHashTable.get(id);
    }

    /**
     * Finds an existing train or creates a new train if needed requires train's
     * name creates a unique id for this train
     *
     * @param name The train's name.
     *
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
            setDirtyAndFirePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize,
                    Integer.valueOf(_trainHashTable.size()));
        }
        return train;
    }

    /**
     * Remember a NamedBean Object created outside the manager.
     *
     * @param train The Train to be added.
     */
    public void register(Train train) {
        Integer oldSize = Integer.valueOf(_trainHashTable.size());
        _trainHashTable.put(train.getId(), train);
        // find last id created
        int id = Integer.parseInt(train.getId());
        if (id > _id) {
            _id = id;
        }
        setDirtyAndFirePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize,
                Integer.valueOf(_trainHashTable.size()));
        // listen for name and state changes to forward
    }

    /**
     * Forget a NamedBean Object created outside the manager.
     *
     * @param train The Train to delete.
     */
    public void deregister(Train train) {
        if (train == null) {
            return;
        }
        train.dispose();
        Integer oldSize = Integer.valueOf(_trainHashTable.size());
        _trainHashTable.remove(train.getId());
        setDirtyAndFirePropertyChange(LISTLENGTH_CHANGED_PROPERTY, oldSize,
                Integer.valueOf(_trainHashTable.size()));
    }

    public void replaceLoad(String type, String oldLoadName, String newLoadName) {
        for (Train train : getTrainsByIdList()) {
            for (String loadName : train.getLoadNames()) {
                if (loadName.equals(oldLoadName)) {
                    train.deleteLoadName(oldLoadName);
                    if (newLoadName != null) {
                        train.addLoadName(newLoadName);
                    }
                }
                // adjust combination car type and load name
                String[] splitLoad = loadName.split(CarLoad.SPLIT_CHAR);
                if (splitLoad.length > 1) {
                    if (splitLoad[0].equals(type) && splitLoad[1].equals(oldLoadName)) {
                        train.deleteLoadName(loadName);
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
     * @return true if there's a built train
     */
    public boolean isAnyTrainBuilt() {
        for (Train train : getTrainsByIdList()) {
            if (train.isBuilt()) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @return true if there's a train being built
     */
    public boolean isAnyTrainBuilding() {
        for (Train train : getTrainsByIdList()) {
            if (train.getStatusCode() == Train.CODE_BUILDING) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param car         The car looking for a train.
     * @param buildReport The build report for logging.
     * @return Train that can service car from its current location to the its
     *         destination.
     */
    public Train getTrainForCar(Car car, PrintWriter buildReport) {
        return getTrainForCar(car, null, buildReport);
    }

    /**
     *
     * @param car          The car looking for a train.
     * @param excludeTrain The only train not to try.
     * @param buildReport  The build report for logging.
     * @return Train that can service car from its current location to the its
     *         destination.
     */
    public Train getTrainForCar(Car car, Train excludeTrain, PrintWriter buildReport) {
        log.debug("Find train for car (" + car.toString() + ") location (" + car.getLocationName() + ", " // NOI18N
                + car.getTrackName() + ") destination (" + car.getDestinationName() + ", " // NOI18N
                + car.getDestinationTrackName() + ")"); // NOI18N
        if (Setup.getRouterBuildReportLevel().equals(Setup.BUILD_REPORT_VERY_DETAILED)) {
            TrainCommon.addLine(buildReport, Setup.BUILD_REPORT_VERY_DETAILED, TrainCommon.BLANK_LINE);
            TrainCommon.addLine(buildReport, Setup.BUILD_REPORT_VERY_DETAILED, MessageFormat.format(Bundle
                    .getMessage("trainFindForCar"), new Object[]{car.toString(), car.getLocationName(),
                car.getTrackName(), car.getDestinationName(), car.getDestinationTrackName()}));
        }
        for (Train train : getTrainsByIdList()) {
            if (train == excludeTrain) {
                continue;
            }
            if (Setup.isOnlyActiveTrainsEnabled() && !train.isBuildEnabled()) {
                continue;
            }
            // does this train service this car?
            if (train.services(buildReport, car)) {
                return train;
            }
        }
        return null;
    }

    /**
     * Sort by train name
     *
     * @return list of trains ordered by name
     */
    public List<Train> getTrainsByNameList() {
        return getTrainsByList(getList(), GET_TRAIN_NAME);
    }

    /**
     * Sort by train departure time
     *
     * @return list of trains ordered by departure time
     */
    public List<Train> getTrainsByTimeList() {
        return getTrainsByIntList(getTrainsByNameList(), GET_TRAIN_TIME);
    }

    /**
     * Sort by train departure location name
     *
     * @return list of trains ordered by departure name
     */
    public List<Train> getTrainsByDepartureList() {
        return getTrainsByList(getTrainsByTimeList(), GET_TRAIN_DEPARTES_NAME);
    }

    /**
     * Sort by train termination location name
     *
     * @return list of trains ordered by termination name
     */
    public List<Train> getTrainsByTerminatesList() {
        return getTrainsByList(getTrainsByTimeList(), GET_TRAIN_TERMINATES_NAME);
    }

    /**
     * Sort by train route name
     *
     * @return list of trains ordered by route name
     */
    public List<Train> getTrainsByRouteList() {
        return getTrainsByList(getTrainsByTimeList(), GET_TRAIN_ROUTE_NAME);
    }

    /**
     * Sort by train status
     *
     * @return list of trains ordered by status
     */
    public List<Train> getTrainsByStatusList() {
        return getTrainsByList(getTrainsByTimeList(), GET_TRAIN_STATUS);
    }

    /**
     * Sort by train description
     *
     * @return list of trains ordered by train description
     */
    public List<Train> getTrainsByDescriptionList() {
        return getTrainsByList(getTrainsByTimeList(), GET_TRAIN_DESCRIPTION);
    }

    /**
     * Sort by train id
     *
     * @return list of trains ordered by id
     */
    public List<Train> getTrainsByIdList() {
        return getTrainsByIntList(getList(), GET_TRAIN_ID);
    }

    private List<Train> getTrainsByList(List<Train> sortList, int attribute) {
        List<Train> out = new ArrayList<>();
        for (Train train : sortList) {
            String trainAttribute = (String) getTrainAttribute(train, attribute);
            for (int j = 0; j < out.size(); j++) {
                if (trainAttribute.compareToIgnoreCase((String) getTrainAttribute(out.get(j), attribute)) < 0) {
                    out.add(j, train);
                    break;
                }
            }
            if (!out.contains(train)) {
                out.add(train);
            }
        }
        return out;
    }

    private List<Train> getTrainsByIntList(List<Train> sortList, int attribute) {
        List<Train> out = new ArrayList<>();
        for (Train train : sortList) {
            int trainAttribute = (Integer) getTrainAttribute(train, attribute);
            for (int j = 0; j < out.size(); j++) {
                if (trainAttribute < (Integer) getTrainAttribute(out.get(j), attribute)) {
                    out.add(j, train);
                    break;
                }
            }
            if (!out.contains(train)) {
                out.add(train);
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
    private static final int GET_TRAIN_DESCRIPTION = 7;

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
            case GET_TRAIN_DESCRIPTION:
                return train.getDescription();
            default:
                return "unknown"; // NOI18N
        }
    }

    private List<Train> getList() {
        if (!InstanceManager.getDefault(TrainManagerXml.class).isTrainFileLoaded()) {
            log.error("TrainManager getList called before trains completely loaded!");
        }
        List<Train> out = new ArrayList<>();
        Enumeration<Train> en = _trainHashTable.elements();
        while (en.hasMoreElements()) {
            out.add(en.nextElement());
        }
        return out;
    }

    public JComboBox<Train> getTrainComboBox() {
        JComboBox<Train> box = new JComboBox<>();
        updateTrainComboBox(box);
        return box;
    }

    public void updateTrainComboBox(JComboBox<Train> box) {
        box.removeAllItems();
        box.addItem(null);
        for (Train train : getTrainsByNameList()) {
            box.addItem(train);
        }
    }

    /**
     * Update combo box with trains that will service this car
     *
     * @param box the combo box to update
     * @param car the car to be serviced
     */
    public void updateTrainComboBox(JComboBox<Train> box, Car car) {
        box.removeAllItems();
        box.addItem(null);
        for (Train train : getTrainsByNameList()) {
            if (train.services(car)) {
                box.addItem(train);
            }
        }
    }

    /**
     * @return Number of trains
     */
    public int numEntries() {
        return _trainHashTable.size();
    }

    public boolean isRowColorManual() {
        return _rowColorManual;
    }

    public void setRowColorsManual(boolean manual) {
        boolean old = _rowColorManual;
        _rowColorManual = manual;
        setDirtyAndFirePropertyChange(ROW_COLOR_NAME_CHANGED_PROPERTY, old, manual);
    }

    public String getRowColorNameForBuilt() {
        return _rowColorBuilt;
    }

    public void setRowColorNameForBuilt(String colorName) {
        String old = _rowColorBuilt;
        _rowColorBuilt = colorName;
        setDirtyAndFirePropertyChange(ROW_COLOR_NAME_CHANGED_PROPERTY, old, colorName);
    }

    public String getRowColorNameForBuildFailed() {
        return _rowColorBuildFailed;
    }

    public void setRowColorNameForBuildFailed(String colorName) {
        String old = _rowColorBuildFailed;
        _rowColorBuildFailed = colorName;
        setDirtyAndFirePropertyChange(ROW_COLOR_NAME_CHANGED_PROPERTY, old, colorName);
    }

    public String getRowColorNameForTrainEnRoute() {
        return _rowColorTrainEnRoute;
    }

    public void setRowColorNameForTrainEnRoute(String colorName) {
        String old = _rowColorTrainEnRoute;
        _rowColorTrainEnRoute = colorName;
        setDirtyAndFirePropertyChange(ROW_COLOR_NAME_CHANGED_PROPERTY, old, colorName);
    }

    public String getRowColorNameForTerminated() {
        return _rowColorTerminated;
    }

    public void setRowColorNameForTerminated(String colorName) {
        String old = _rowColorTerminated;
        _rowColorTerminated = colorName;
        setDirtyAndFirePropertyChange(ROW_COLOR_NAME_CHANGED_PROPERTY, old, colorName);
    }

    /**
     *
     * @return the available text colors used for printing
     * @deprecated since 4.9.6 use a {@link javax.swing.JColorChooser } instead. 
     */
    @Deprecated
    public JComboBox<String> getRowColorComboBox() {
        JComboBox<String> box = new JComboBox<>();
        box.addItem(NONE);
        box.addItem(ColorUtil.ColorBlack);
        box.addItem(ColorUtil.ColorRed);
        box.addItem(ColorUtil.ColorPink);
        box.addItem(ColorUtil.ColorOrange);
        box.addItem(ColorUtil.ColorYellow);
        box.addItem(ColorUtil.ColorGreen);
        box.addItem(ColorUtil.ColorMagenta);
        box.addItem(ColorUtil.ColorCyan);
        box.addItem(ColorUtil.ColorBlue);
        box.addItem(ColorUtil.ColorGray);
        return box;
    }

    /**
     * Makes a copy of an existing train.
     *
     * @param train     the train to copy
     * @param trainName the name of the new train
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
        for (String scriptName : train.getBuildScripts()) {
            newTrain.addBuildScript(scriptName);
        }
        for (String scriptName : train.getMoveScripts()) {
            newTrain.addMoveScript(scriptName);
        }
        for (String scriptName : train.getTerminationScripts()) {
            newTrain.addTerminationScript(scriptName);
        }
        // manifest options
        newTrain.setRailroadName(train.getRailroadName());
        newTrain.setManifestLogoPathName(train.getManifestLogoPathName());
        newTrain.setShowArrivalAndDepartureTimes(train.isShowArrivalAndDepartureTimesEnabled());
        // build options
        newTrain.setAllowLocalMovesEnabled(train.isAllowLocalMovesEnabled());
        newTrain.setAllowReturnToStagingEnabled(train.isAllowReturnToStagingEnabled());
        newTrain.setAllowThroughCarsEnabled(train.isAllowThroughCarsEnabled());
        newTrain.setBuildConsistEnabled(train.isBuildConsistEnabled());
        newTrain.setSendCarsWithCustomLoadsToStagingEnabled(train.isSendCarsWithCustomLoadsToStagingEnabled());
        newTrain.setBuildTrainNormalEnabled(train.isBuildTrainNormalEnabled());
        newTrain.setSendCarsToTerminalEnabled(train.isSendCarsToTerminalEnabled());
        newTrain.setServiceAllCarsWithFinalDestinationsEnabled(train.isServiceAllCarsWithFinalDestinationsEnabled());
        // comment
        newTrain.setComment(train.getComment());
        // description
        newTrain.setDescription(train.getRawDescription());
        return newTrain;
    }

    /**
     * Provides a list of trains ordered by arrival time to a location
     *
     * @param location The location
     * @return A list of trains ordered by arrival time.
     */
    public List<Train> getTrainsArrivingThisLocationList(Location location) {
        // get a list of trains
        List<Train> out = new ArrayList<>();
        List<Integer> arrivalTimes = new ArrayList<>();
        for (Train train : getTrainsByTimeList()) {
            if (!train.isBuilt()) {
                continue; // train wasn't built so skip
            }
            Route route = train.getRoute();
            if (route == null) {
                continue; // no route for this train
            }
            for (RouteLocation rl : route.getLocationsBySequenceList()) {
                if (TrainCommon.splitString(rl.getName()).equals(TrainCommon.splitString(location.getName()))) {
                    int expectedArrivalTime = train.getExpectedTravelTimeInMinutes(rl);
                    // is already serviced then "-1"
                    if (expectedArrivalTime == -1) {
                        out.add(0, train); // place all trains that have already been serviced at the start
                        arrivalTimes.add(0, expectedArrivalTime);
                    } // if the train is in route, then expected arrival time is in minutes
                    else if (train.isTrainEnRoute()) {
                        for (int j = 0; j < out.size(); j++) {
                            Train t = out.get(j);
                            int time = arrivalTimes.get(j);
                            if (t.isTrainEnRoute() && expectedArrivalTime < time) {
                                out.add(j, train);
                                arrivalTimes.add(j, expectedArrivalTime);
                                break;
                            }
                            if (!t.isTrainEnRoute()) {
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
                            if (!t.isTrainEnRoute() && expectedArrivalTime < time) {
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

    /**
     * Loads train icons if needed
     */
    public void loadTrainIcons() {
        for (Train train : getTrainsByIdList()) {
            train.loadTrainIcon();
        }
    }

    /**
     * Sets the switch list status for all built trains. Used for switch lists
     * in consolidated mode.
     *
     * @param status Train.PRINTED, Train.UNKNOWN
     */
    public void setTrainsSwitchListStatus(String status) {
        for (Train train : getTrainsByTimeList()) {
            if (!train.isBuilt()) {
                continue; // train isn't built so skip
            }
            train.setSwitchListStatus(status);
        }
    }

    /**
     * Sets all built trains manifests to modified. This causes the train's
     * manifest to be recreated.
     */
    public void setTrainsModified() {
        for (Train train : getTrainsByTimeList()) {
            if (!train.isBuilt() || train.isTrainEnRoute()) {
                continue; // train wasn't built or in route, so skip
            }
            train.setModified(true);
        }
    }

    public void buildSelectedTrains(List<Train> trains) {
        // use a thread to allow table updates during build
        Thread build = new Thread(new Runnable() {
            @Override
            public void run() {
                for (Train train : trains) {
                    train.buildIfSelected();
                }
                setDirtyAndFirePropertyChange(TRAINS_BUILT_CHANGED_PROPERTY, false, true);
            }
        });
        build.setName("Build Trains"); // NOI18N
        build.start();
    }

    public boolean printSelectedTrains(List<Train> trains) {
        boolean status = true;
        for (Train train : trains) {
            if (train.isBuildEnabled()) {
                if (train.printManifestIfBuilt()) {
                    continue;
                }
                status = false; // failed to print all selected trains
                if (isBuildMessagesEnabled()) {
                    JOptionPane.showMessageDialog(null, MessageFormat.format(Bundle
                            .getMessage("NeedToBuildBeforePrinting"), new Object[]{train.getName(),
                        (isPrintPreviewEnabled() ? Bundle.getMessage("preview")
                        : Bundle.getMessage("print"))}),
                            MessageFormat.format(Bundle.getMessage("CanNotPrintManifest"),
                                    new Object[]{isPrintPreviewEnabled() ? Bundle.getMessage("preview") : Bundle
                                                .getMessage("print")}),
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        return status;
    }

    public boolean terminateSelectedTrains(List<Train> trains) {
        boolean status = true;
        for (Train train : trains) {
            if (train.isBuildEnabled() && train.isBuilt()) {
                if (train.isPrinted()) {
                    train.terminate();
                } else {
                    status = false;
                    int response = JOptionPane.showConfirmDialog(null, Bundle
                            .getMessage("WarningTrainManifestNotPrinted"), MessageFormat.format(Bundle
                            .getMessage("TerminateTrain"),
                            new Object[]{train.getName(), train.getDescription()}),
                            JOptionPane.YES_NO_OPTION);
                    if (response == JOptionPane.YES_OPTION) {
                        train.terminate();
                    }
                    // Quit?
                    if (response == JOptionPane.CLOSED_OPTION) {
                        break;
                    }
                }
            }
        }
        return status;
    }

    public void load(Element root) {
        if (root.getChild(Xml.OPTIONS) != null) {
            Element options = root.getChild(Xml.OPTIONS);
            InstanceManager.getDefault(TrainCustomManifest.class).load(options);
            InstanceManager.getDefault(TrainCustomSwitchList.class).load(options);
            Element e = options.getChild(Xml.TRAIN_OPTIONS);
            Attribute a;
            if (e != null) {
                if ((a = e.getAttribute(Xml.BUILD_MESSAGES)) != null) {
                    _buildMessages = a.getValue().equals(Xml.TRUE);
                }
                if ((a = e.getAttribute(Xml.BUILD_REPORT)) != null) {
                    _buildReport = a.getValue().equals(Xml.TRUE);
                }
                if ((a = e.getAttribute(Xml.PRINT_PREVIEW)) != null) {
                    _printPreview = a.getValue().equals(Xml.TRUE);
                }
                if ((a = e.getAttribute(Xml.OPEN_FILE)) != null) {
                    _openFile = a.getValue().equals(Xml.TRUE);
                }
                if ((a = e.getAttribute(Xml.RUN_FILE)) != null) {
                    _runFile = a.getValue().equals(Xml.TRUE);
                }
                // verify that the Trains Window action is valid
                if ((a = e.getAttribute(Xml.TRAIN_ACTION)) != null
                        && (a.getValue().equals(TrainsTableFrame.MOVE)
                        || a.getValue().equals(TrainsTableFrame.RESET)
                        || a.getValue().equals(TrainsTableFrame.TERMINATE)
                        || a.getValue().equals(
                                TrainsTableFrame.CONDUCTOR))) {
                    _trainAction = a.getValue();
                }
            }
            
            // Conductor options
            Element eConductorOptions = options.getChild(Xml.CONDUCTOR_OPTIONS);
            if (eConductorOptions != null) {
                if ((a = eConductorOptions.getAttribute(Xml.SHOW_HYPHEN_NAME)) != null) {
                    _showLocationHyphenName = a.getValue().equals(Xml.TRUE);
                }
            }

            // Row color options
            Element eRowColorOptions = options.getChild(Xml.ROW_COLOR_OPTIONS);
            if (eRowColorOptions != null) {
                if ((a = eRowColorOptions.getAttribute(Xml.ROW_COLOR_MANUAL)) != null) {
                    _rowColorManual = a.getValue().equals(Xml.TRUE);
                }
                if ((a = eRowColorOptions.getAttribute(Xml.ROW_COLOR_BUILD_FAILED)) != null) {
                    _rowColorBuildFailed = a.getValue();
                }
                if ((a = eRowColorOptions.getAttribute(Xml.ROW_COLOR_BUILT)) != null) {
                    _rowColorBuilt = a.getValue();
                }
                if ((a = eRowColorOptions.getAttribute(Xml.ROW_COLOR_TRAIN_EN_ROUTE)) != null) {
                    _rowColorTrainEnRoute = a.getValue();
                }
                if ((a = eRowColorOptions.getAttribute(Xml.ROW_COLOR_TERMINATED)) != null) {
                    _rowColorTerminated = a.getValue();
                }
            }

            // moved to train schedule manager
            e = options.getChild(jmri.jmrit.operations.trains.schedules.Xml.TRAIN_SCHEDULE_OPTIONS);
            if (e != null) {
                if ((a = e.getAttribute(jmri.jmrit.operations.trains.schedules.Xml.ACTIVE_ID)) != null) {
                    InstanceManager.getDefault(TrainScheduleManager.class).setTrainScheduleActiveId(a.getValue());
                }
            }
            // check for scripts
            if (options.getChild(Xml.SCRIPTS) != null) {
                List<Element> lm = options.getChild(Xml.SCRIPTS).getChildren(Xml.START_UP);
                for (Element es : lm) {
                    if ((a = es.getAttribute(Xml.NAME)) != null) {
                        addStartUpScript(a.getValue());
                    }
                }
                List<Element> lt = options.getChild(Xml.SCRIPTS).getChildren(Xml.SHUT_DOWN);
                for (Element es : lt) {
                    if ((a = es.getAttribute(Xml.NAME)) != null) {
                        addShutDownScript(a.getValue());
                    }
                }
            }
        }
        if (root.getChild(Xml.TRAINS) != null) {
            List<Element> eTrains = root.getChild(Xml.TRAINS).getChildren(Xml.TRAIN);
            log.debug("readFile sees {} trains", eTrains.size());
            for (Element eTrain : eTrains) {
                register(new Train(eTrain));
            }
        }
    }

    /**
     * Create an XML element to represent this Entry. This member has to remain
     * synchronized with the detailed DTD in operations-trains.dtd.
     *
     * @param root common Element for operations-trains.dtd.
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
        
        // Conductor options
        e = new Element(Xml.CONDUCTOR_OPTIONS);
        e.setAttribute(Xml.SHOW_HYPHEN_NAME, isShowLocationHyphenNameEnabled() ? Xml.TRUE : Xml.FALSE);
        options.addContent(e);

        // Trains table row color options
        e = new Element(Xml.ROW_COLOR_OPTIONS);
        e.setAttribute(Xml.ROW_COLOR_MANUAL, isRowColorManual() ? Xml.TRUE : Xml.FALSE);
        e.setAttribute(Xml.ROW_COLOR_BUILD_FAILED, getRowColorNameForBuildFailed());
        e.setAttribute(Xml.ROW_COLOR_BUILT, getRowColorNameForBuilt());
        e.setAttribute(Xml.ROW_COLOR_TRAIN_EN_ROUTE, getRowColorNameForTrainEnRoute());
        e.setAttribute(Xml.ROW_COLOR_TERMINATED, getRowColorNameForTerminated());
        options.addContent(e);

        if (getStartUpScripts().size() > 0 || getShutDownScripts().size() > 0) {
            // save list of shutdown scripts
            Element es = new Element(Xml.SCRIPTS);
            for (String scriptName : getStartUpScripts()) {
                Element em = new Element(Xml.START_UP);
                em.setAttribute(Xml.NAME, scriptName);
                es.addContent(em);
            }
            // save list of termination scripts
            for (String scriptName : getShutDownScripts()) {
                Element et = new Element(Xml.SHUT_DOWN);
                et.setAttribute(Xml.NAME, scriptName);
                es.addContent(et);
            }
            options.addContent(es);
        }

        InstanceManager.getDefault(TrainCustomManifest.class).store(options); // save custom manifest elements
        InstanceManager.getDefault(TrainCustomSwitchList.class).store(options); // save custom switch list elements

        root.addContent(options);

        Element trains = new Element(Xml.TRAINS);
        root.addContent(trains);
        // add entries
        for (Train train : getTrainsByIdList()) {
            trains.addContent(train.store());
        }
    }

    /**
     * Check for car type and road name replacements. Also check for engine type
     * replacement.
     */
    @Override
    public void propertyChange(java.beans.PropertyChangeEvent e) {
        log.debug("TrainManager sees property change: "
                + e.getPropertyName()
                + " old: "
                + e.getOldValue()
                + " new "
                + e.getNewValue()); // NOI18N
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

    private void setDirtyAndFirePropertyChange(String p, Object old, Object n) {
        InstanceManager.getDefault(TrainManagerXml.class).setDirty(true);
        pcs.firePropertyChange(p, old, n);
    }

    private final static Logger log = LoggerFactory.getLogger(TrainManager.class);

    @Override
    public void initialize() {
        InstanceManager.getDefault(OperationsSetupXml.class); // load setup
        InstanceManager.getDefault(TrainManagerXml.class); // load trains
    }

}
