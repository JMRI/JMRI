package jmri.jmrit.operations.rollingstock;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.OperationsXml;
import jmri.jmrit.operations.rollingstock.cars.Car;
import jmri.jmrit.operations.rollingstock.cars.CarManager;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.rollingstock.engines.EngineManager;
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.setup.Setup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs rolling stock movements by writing their locations to a file.
 *
 * @author Daniel Boudreau Copyright (C) 2010, 2016
 */
public class RollingStockLogger extends XmlFile implements InstanceManagerAutoDefault, PropertyChangeListener {

    private boolean engLog = false; // when true logging engine movements
    private boolean carLog = false; // when true logging car movements
    static final String DEL = ","; // delimiter
    static final String ESC = "\""; // escape character NOI18N

    public RollingStockLogger() {
    }

    /**
     * Get the default instance of this class.
     *
     * @return the default instance of this class
     * @deprecated since 4.9.2; use
     * {@link jmri.InstanceManager#getDefault(java.lang.Class)} instead
     */
    @Deprecated
    public static synchronized RollingStockLogger instance() {
        return InstanceManager.getDefault(RollingStockLogger.class);
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

    private boolean mustHaveTrack = true; // when true only updates that have a track are saved

    private void store(RollingStock rs) {

        if (rs.getTrack() == null && mustHaveTrack) {
            return;
        }

        String rsRoad = rs.getRoadName();
        if (rsRoad.contains(DEL)) {
            rsRoad = ESC + rs.getRoadName() + ESC;
        }
        String rsType = rs.getTypeName();
        if (rsType.contains(DEL)) {
            rsType = ESC + rs.getTypeName() + ESC;
        }
        String rsLocationName = rs.getLocationName();
        if (rsLocationName.contains(DEL)) {
            rsLocationName = ESC + rs.getLocationName() + ESC;
        }
        String rsTrackName = rs.getTrackName();
        if (rsTrackName.contains(DEL)) {
            rsTrackName = ESC + rs.getTrackName() + ESC;
        }
        String rsTrainName = rs.getTrainName();
        if (rsTrainName.contains(DEL)) {
            rsTrainName = ESC + rs.getTrainName() + ESC;
        }
        String carLoad = " ";
        String carFinalDest = " ";
        String carFinalDestTrack = " ";
        if (rs.getClass().equals(Car.class)) {
            Car car = (Car) rs;
            carLoad = car.getLoadName();
            if (carLoad.contains(DEL)) {
                carLoad = ESC + car.getLoadName() + ESC;
            }
            carFinalDest = car.getFinalDestinationName();
            if (carFinalDest.contains(DEL)) {
                carFinalDest = ESC + car.getFinalDestinationName() + ESC;
            }
            carFinalDestTrack = car.getFinalDestinationTrackName();
            if (carFinalDestTrack.contains(DEL)) {
                carFinalDestTrack = ESC + car.getFinalDestinationTrackName() + ESC;
            }
        }

        String line = rs.getNumber() +
                DEL +
                rsRoad +
                DEL +
                rsType +
                DEL +
                carLoad +
                DEL +
                rsLocationName +
                DEL +
                rsTrackName +
                DEL +
                carFinalDest +
                DEL +
                carFinalDestTrack +
                DEL +
                rsTrainName +
                DEL +
                rs.getMoves() +
                DEL +
                getTime();

        fileOut(line); // append line to common file
        fileOut(line, rs); // append line to individual file
    }

    /*
     * Appends one line to common log file.
     */
    private void fileOut(String line) {
        fileOut(line, getFile());
    }

    /*
     * Appends one line to the rolling stock's individual file.
     */
    private void fileOut(String line, RollingStock rs) {
        fileOut(line, getFile(rs));
    }

    private void fileOut(String line, File file) {
        PrintWriter fileOut = null;
        try {
            // FileOutputStream is set to append
            fileOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true),
                    "UTF-8")), true); // NOI18N
        } catch (IOException e) {
            log.error("Exception while opening log file: {}", e.getMessage());
            return;
        }

        log.debug("Log: {}", line);

        fileOut.println(line);
        fileOut.flush();
        fileOut.close();
    }

    /*
     * Returns the common log file for all rolling stock
     */
    private File getFile() {
        File fileLogger = null;
        if (Setup.isEngineLoggerEnabled() || Setup.isCarLoggerEnabled()) {
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
            } catch (IOException e) {
                log.error("Exception while making logging directory: {}", e.getMessage());
            }
        }
        return fileLogger;
    }

    private String getHeader() {
        String header = Bundle.getMessage("Number") +
                DEL +
                Bundle.getMessage("Road") +
                DEL +
                Bundle.getMessage("Type") +
                DEL +
                Bundle.getMessage("Load") +
                DEL +
                Bundle.getMessage("Location") +
                DEL +
                Bundle.getMessage("Track") +
                DEL +
                Bundle.getMessage("FinalDestination") +
                DEL +
                Bundle.getMessage("Track") +
                DEL +
                Bundle.getMessage("Train") +
                DEL +
                Bundle.getMessage("Moves") +
                DEL +
                Bundle.getMessage("DateAndTime");
        return header;
    }

    /*
     * Gets the individual log file for a specific car or loco.
     */
    private File getFile(RollingStock rs) {
        File file = null;
        if (Setup.isEngineLoggerEnabled() || Setup.isCarLoggerEnabled()) {
            // create the logging file for this rolling stock
            try {
                if (!checkFile(getFullLoggerFileName(rs))) {
                    // The file/directory does not exist, create it before writing
                    file = new java.io.File(getFullLoggerFileName(rs));
                    File parentDir = file.getParentFile();
                    if (!parentDir.exists()) {
                        if (!parentDir.mkdirs()) {
                            log.error("logger directory not created");
                        }
                    }
                    if (file.createNewFile()) {
                        log.debug("new file created");
                        // add header
                        fileOut(getHeader(), rs);
                    }
                } else {
                    file = new java.io.File(getFullLoggerFileName(rs));
                }
            } catch (IOException e) {
                log.error("Exception while making logging directory: {}", e.getMessage());
            }
        }
        return file;
    }

    public String getFullLoggerFileName() {
        return loggingDirectory + File.separator + getFileName();
    }

    private String operationsDirectory =
            OperationsSetupXml.getFileLocation() + OperationsSetupXml.getOperationsDirectoryName();

    private String loggingDirectory = operationsDirectory + File.separator + "logger"; // NOI18N

    public String getDirectoryName() {
        return loggingDirectory;
    }

    public void setDirectoryName(String name) {
        loggingDirectory = name;
    }

    // Use the same common file even if the session crosses midnight
    private String fileName;

    public String getFileName() {
        if (fileName == null) {
            fileName = getDate() + ".csv"; // NOI18N
        }
        return fileName;
    }

    /**
     * Individual files for each rolling stock stored in a directory called
     * "rollingStock" inside the "logger" directory.
     * @param rs The RollingStock to log.
     * @return Full path name of log file.
     *
     */
    public String getFullLoggerFileName(RollingStock rs) {
        if (!OperationsXml.checkFileName(rs.toString())) { // NOI18N
            log.error("Rolling stock name ({}) must not contain reserved characters", rs);
            return loggingDirectory + File.separator + "rollingStock" + File.separator + "ERROR" + ".csv"; // NOI18N
        }
        return loggingDirectory + File.separator + "rollingStock" + File.separator + rs.toString() + ".csv"; // NOI18N
    }

    private String getDate() {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd"); // NOI18N
        return simpleDateFormat.format(date);
    }

    /**
     * Return the date and time in an MS Excel friendly format yyyy/MM/dd
     * HH:mm:ss
     *
     */
    private String getTime() {
        String time = Calendar.getInstance().getTime().toString();
        SimpleDateFormat dt = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy"); // NOI18N
        SimpleDateFormat dtout = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); // NOI18N
        try {
            return dtout.format(dt.parse(time));
        } catch (ParseException e) {
            return time; // there was an issue, use the old format
        }
    }

    private void addCarListeners() {
        if (Setup.isCarLoggerEnabled() && !carLog) {
            log.debug("Rolling Stock Logger adding car listerners");
            carLog = true;
            List<Car> cars = InstanceManager.getDefault(CarManager.class).getList();
            for (Car car : cars) {
                car.addPropertyChangeListener(this);
            }
            // listen for new rolling stock being added
            InstanceManager.getDefault(CarManager.class).addPropertyChangeListener(this);
        }
    }

    private void addEngineListeners() {
        if (Setup.isEngineLoggerEnabled() && !engLog) {
            engLog = true;
            log.debug("Rolling Stock Logger adding engine listerners");
            List<Engine> engines = InstanceManager.getDefault(EngineManager.class).getList();
            for (Engine engine : engines) {
                engine.addPropertyChangeListener(this);
            }
            // listen for new rolling stock being added
            InstanceManager.getDefault(EngineManager.class).addPropertyChangeListener(this);
        }
    }

    private void removeCarListeners() {
        if (carLog) {
            log.debug("Rolling Stock Logger removing car listerners");
            List<Car> cars = InstanceManager.getDefault(CarManager.class).getList();
            for (Car car : cars) {
                car.removePropertyChangeListener(this);
            }
            InstanceManager.getDefault(CarManager.class).removePropertyChangeListener(this);
        }
        carLog = false;
    }

    private void removeEngineListeners() {
        if (engLog) {
            log.debug("Rolling Stock Logger removing engine listerners");
            List<Engine> engines = InstanceManager.getDefault(EngineManager.class).getList();
            for (Engine engine : engines) {
                engine.removePropertyChangeListener(this);
            }
            InstanceManager.getDefault(EngineManager.class).removePropertyChangeListener(this);
        }
        engLog = false;
    }

    public void dispose() {
        removeCarListeners();
        removeEngineListeners();
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals(RollingStock.TRACK_CHANGED_PROPERTY)) {
            if (Control.SHOW_PROPERTY) {
                log.debug("Logger sees property change for car {}", e.getSource());
            }
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

    private final static Logger log = LoggerFactory.getLogger(RollingStockLogger.class);
}
