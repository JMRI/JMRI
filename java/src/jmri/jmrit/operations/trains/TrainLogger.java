package jmri.jmrit.operations.trains;

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
import jmri.jmrit.operations.setup.Control;
import jmri.jmrit.operations.setup.OperationsSetupXml;
import jmri.jmrit.operations.setup.Setup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Logs train movements and status to a file.
 *
 * @author Daniel Boudreau Copyright (C) 2010, 2013
 */
public class TrainLogger extends XmlFile implements InstanceManagerAutoDefault, PropertyChangeListener {

    File _fileLogger;
    private boolean _trainLog = false; // when true logging train movements
    static final String DEL = ","; // delimiter
    static final String ESC = "\""; // escape // NOI18N

    public TrainLogger() {
    }

    /**
     * Get the default instance of this class.
     *
     * @return the default instance of this class
     * @deprecated since 4.9.2; use
     * {@link jmri.InstanceManager#getDefault(java.lang.Class)} instead
     */
    @Deprecated
    public static synchronized TrainLogger instance() {
        return InstanceManager.getDefault(TrainLogger.class);
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
            log.error("Exception while making logging directory: " + e);
        }

    }

    private void store(Train train) {
        // create train file if needed
        createFile();
        // Note that train status can contain a comma
        String line = ESC +
                train.getName() +
                ESC +
                DEL +
                ESC +
                train.getDescription() +
                ESC +
                DEL +
                ESC +
                train.getCurrentLocationName() +
                ESC +
                DEL +
                ESC +
                train.getNextLocationName() +
                ESC +
                DEL +
                ESC +
                train.getStatus() +
                ESC +
                DEL +
                ESC +
                train.getBuildFailedMessage() +
                ESC +
                DEL +
                getTime();
        fileOut(line);
    }

    private String getHeader() {
        String header = Bundle.getMessage("Name") +
                DEL +
                Bundle.getMessage("Description") +
                DEL +
                Bundle.getMessage("Current") +
                DEL +
                Bundle.getMessage("NextLocation") +
                DEL +
                Bundle.getMessage("Status") +
                DEL +
                Bundle.getMessage("BuildMessages") +
                DEL +
                Bundle.getMessage("DateAndTime");
        return header;
    }

    /*
     * Appends one line to file.
     */
    private void fileOut(String line) {
        if (_fileLogger == null) {
            log.error("Log file doesn't exist");
            return;
        }

        PrintWriter fileOut = null;

        try {
            // FileOutputStream is set to append
            fileOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(_fileLogger, true), "UTF-8")), true); // NOI18N
        } catch (IOException e) {
            log.error("Exception while opening log file: " + e.getLocalizedMessage());
            return;
        }

        log.debug("Log: " + line);

        fileOut.println(line);
        fileOut.flush();
        fileOut.close();
    }

    private void addTrainListeners() {
        if (Setup.isTrainLoggerEnabled() && !_trainLog) {
            log.debug("Train Logger adding train listerners");
            _trainLog = true;
            List<Train> trains = InstanceManager.getDefault(TrainManager.class).getTrainsByIdList();
            for (Train train : trains) {
                train.addPropertyChangeListener(this);
            }
            // listen for new trains being added
            InstanceManager.getDefault(TrainManager.class).addPropertyChangeListener(this);
        }
    }

    private void removeTrainListeners() {
        log.debug("Train Logger removing train listerners");
        if (_trainLog) {
            List<Train> trains = InstanceManager.getDefault(TrainManager.class).getTrainsByIdList();
            for (Train train : trains) {
                train.removePropertyChangeListener(this);
            }
            InstanceManager.getDefault(TrainManager.class).removePropertyChangeListener(this);
        }
        _trainLog = false;
    }

    public void dispose() {
        removeTrainListeners();
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals(Train.STATUS_CHANGED_PROPERTY) ||
                e.getPropertyName().equals(Train.TRAIN_LOCATION_CHANGED_PROPERTY)) {
            if (Control.SHOW_PROPERTY) {
                log.debug("Train logger sees property change for train " + e.getSource());
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

    private final static Logger log = LoggerFactory.getLogger(TrainLogger.class);
}
