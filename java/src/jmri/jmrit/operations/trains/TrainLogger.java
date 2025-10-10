package jmri.jmrit.operations.trains;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.InstanceManager;
import jmri.InstanceManagerAutoDefault;
import jmri.jmrit.XmlFile;
import jmri.jmrit.operations.setup.*;
import jmri.jmrit.operations.setup.backup.AutoBackup;
import jmri.jmrit.operations.setup.backup.DefaultBackup;

/**
 * Logs train movements and status to a file.
 *
 * @author Daniel Boudreau Copyright (C) 2010, 2013, 2024
 */
public class TrainLogger extends XmlFile implements InstanceManagerAutoDefault, PropertyChangeListener {

    File _fileLogger;
    private boolean _trainLog = false; // when true logging train movements

    public TrainLogger() {
    }

    public void enableTrainLogging(boolean enable) {
        if (enable) {
            addTrainListeners();
        } else {
            removeTrainListeners();
        }
    }

    private void createFile() {
        if (!Setup.isTrainLoggerEnabled()) {
            return;
        }
        if (_fileLogger != null) {
            return; // log file has already been created
        } // create the logging file for this session
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
                if (_fileLogger.createNewFile()) {
                    log.debug("new file created");
                    // add header
                    fileOut(getHeader());
                }
            } else {
                _fileLogger = new java.io.File(getFullLoggerFileName());
            }
        } catch (Exception e) {
            log.error("Exception while making logging directory", e);
        }

    }

    private void store(Train train) {
        // create train file if needed
        createFile();
        // Note that train status can contain a comma
        List<Object> line = Arrays.asList(new Object[]{train.getName(),
                train.getDescription(),
                train.getCurrentLocationName(),
                train.getNextLocationName(),
                train.getNumberCarsInTrain(),
                train.getNumberCarsPickedUp(),
                train.getNumberCarsSetout(),
                train.getTrainLength(),
                train.getTrainWeight(),
                train.getStatus(),
                train.getBuildFailedMessage(),
                getDateAndTime()});
        fileOut(line);
    }

    ResourceBundle rb = ResourceBundle
            .getBundle("jmri.jmrit.operations.setup.JmritOperationsSetupBundle");

    /*
     * Adds a status line to the log file whenever the trains file is saved.
     */
    private void storeFileSaved() {
        List<Object> line = Arrays.asList(new Object[]{
                Bundle.getMessage("TrainLogger"), // train name
                "", // train description
                "", // current location
                "", // next location name
                "", // cars
                "", // pulls
                "", // drops
                "", // length
                "", // weight
                Setup.isAutoSaveEnabled() ? rb.getString("AutoSave") : Bundle.getMessage("Manual"), // status
                Bundle.getMessage("TrainsSaved"), // build messages
                getDateAndTime()});
        fileOut(line);
    }

    private void storeBackupChanged(PropertyChangeEvent e) {
        // create train file if needed
        createFile();
        List<Object> line = Arrays.asList(new Object[]{
                Bundle.getMessage("TrainLogger"), // train name
                "", // train description
                "", // current location
                "", // next location name
                "", // cars
                "", // pulls
                e.getPropertyName(), // drops
                "from:", // length
                e.getOldValue(), // weight
                "to:", // status
                e.getNewValue(), // build messages
                getDateAndTime()});
        fileOut(line);
    }

    private List<Object> getHeader() {
        return Arrays.asList(new Object[]{Bundle.getMessage("Name"),
                Bundle.getMessage("Description"),
                Bundle.getMessage("Current"),
                Bundle.getMessage("NextLocation"),
                Bundle.getMessage("Cars"),
                Bundle.getMessage("Pulls"),
                Bundle.getMessage("Drops"),
                Bundle.getMessage("Length"),
                Bundle.getMessage("Weight"),
                Bundle.getMessage("Status"),
                Bundle.getMessage("BuildMessages"),
                Bundle.getMessage("DateAndTime")});
    }

    /*
     * Appends one line to file.
     */
    private void fileOut(List<Object> line) {
        if (_fileLogger == null) {
            log.error("Log file doesn't exist");
            return;
        }

        // FileOutputStream is set to append
        try (CSVPrinter fileOut = new CSVPrinter(new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(_fileLogger, true), StandardCharsets.UTF_8)), CSVFormat.DEFAULT)) {
            log.debug("Log: {}", line);
            fileOut.printRecord(line);
            fileOut.flush();
            fileOut.close();
        } catch (IOException e) {
            log.error("Exception while opening log file: {}", e.getLocalizedMessage());
        }
    }

    private void addTrainListeners() {
        if (Setup.isTrainLoggerEnabled() && !_trainLog) {
            log.debug("Train Logger adding train listerners");
            _trainLog = true;
            List<Train> trains = InstanceManager.getDefault(TrainManager.class).getList();
            trains.forEach(train -> train.addPropertyChangeListener(this));
            // listen for new trains being added
            InstanceManager.getDefault(TrainManager.class).addPropertyChangeListener(this);
            // listen for backup file changes
            InstanceManager.getDefault(DefaultBackup.class).addPropertyChangeListener(this);
            InstanceManager.getDefault(AutoBackup.class).addPropertyChangeListener(this);
        }
    }

    private void removeTrainListeners() {
        log.debug("Train Logger removing train listerners");
        if (_trainLog) {
            List<Train> trains = InstanceManager.getDefault(TrainManager.class).getList();
            trains.forEach(train -> train.removePropertyChangeListener(this));
            InstanceManager.getDefault(TrainManager.class).removePropertyChangeListener(this);
            InstanceManager.getDefault(DefaultBackup.class).removePropertyChangeListener(this);
            InstanceManager.getDefault(AutoBackup.class).removePropertyChangeListener(this);
        }
        _trainLog = false;
    }

    public void dispose() {
        removeTrainListeners();
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals(Train.TRAIN_CURRENT_CHANGED_PROPERTY) && e.getNewValue() != null ||
                e.getPropertyName().equals(Train.STATUS_CHANGED_PROPERTY) &&
                        (e.getNewValue().equals(Train.TRAIN_RESET) ||
                                e.getNewValue().equals(Train.BUILDING) ||
                                e.getNewValue().equals(Train.BUILD_FAILED) ||
                                e.getNewValue().toString().startsWith(Train.TERMINATED))) {
            if (Control.SHOW_PROPERTY) {
                log.debug("Train logger sees property change for train ({}), property name: {}", e.getSource(),
                        e.getPropertyName());
            }
            store((Train) e.getSource());
        }
        if (e.getPropertyName().equals(TrainManager.LISTLENGTH_CHANGED_PROPERTY)) {
            if ((Integer) e.getNewValue() > (Integer) e.getOldValue()) {
                // a car or engine has been added
                removeTrainListeners();
                addTrainListeners();
            }
        }
        if (e.getPropertyName().equals(TrainManager.TRAINS_SAVED_PROPERTY)) {
            storeFileSaved();
        }
        if (e.getPropertyName().equals(DefaultBackup.COPY_FILES_CHANGED_PROPERTY)) {
            storeBackupChanged(e);
        }
    }

    public String getFullLoggerFileName() {
        return loggingDirectory + File.separator + getFileName();
    }

    private String operationsDirectory =
            OperationsSetupXml.getFileLocation() + OperationsSetupXml.getOperationsDirectoryName();
    private String loggingDirectory = operationsDirectory + File.separator + "logger" + File.separator + "trains"; // NOI18N

    public String getDirectoryName() {
        return loggingDirectory;
    }

    public void setDirectoryName(String name) {
        loggingDirectory = name;
    }

    private String fileName;

    public String getFileName() {
        if (fileName == null) {
            fileName = Bundle.getMessage("Trains") + "_" + getDate() + ".csv"; // NOI18N
        }
        return fileName;
    }

    private String getDate() {
        Date date = Calendar.getInstance().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy_MM_dd"); // NOI18N
        return simpleDateFormat.format(date);
    }

    /**
     * Return the date and time in an MS Excel friendly format yyyy/MM/dd
     * HH:mm:ss
     */
    private String getDateAndTime() {
        String time = Calendar.getInstance().getTime().toString();
        SimpleDateFormat dt = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy"); // NOI18N
        SimpleDateFormat dtout = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); // NOI18N
        try {
            return dtout.format(dt.parse(time));
        } catch (ParseException e) {
            return time; // there was an issue, use the old format
        }
    }

    private final static Logger log = LoggerFactory.getLogger(TrainLogger.class);
}
