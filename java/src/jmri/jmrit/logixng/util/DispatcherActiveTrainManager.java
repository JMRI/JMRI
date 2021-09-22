package jmri.jmrit.logixng.util;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.*;

import jmri.*;
import jmri.beans.Bean;
import jmri.jmrit.dispatcher.*;

/**
 * The Dispatcher support in LogixNG provides the ability to start, control, and terminate trains.
 * <p>
 * The ActiveTrain class represents a train.  An ActiveTrain can be started manually using the
 * ActivateTrainFrame panel.  The panel also supports starting predefined trains that are stored
 * as TrainInfo XML files.  A TrainInfo file can also be used to start a train using a program such
 * as a Jython script, or in this case, LogixNG.
 * <p>
 * Since an active train is a temporary object, the Actions and Expressions use the train info file
 * name as the reference to possible ActiveTrains.
 * <p>
 * The contents of a train info file are not unique.  While only one active train can be using a
 * transit, multiple files can refer to the transit.
 * <p>
 * This class extends Bean so that ExpressionDispatcher objects can listen for changes to the
 * _activeTrainMap.  These events are used to add and remove ActiveTrain listeners that capture
 * ActiveTrain status and mode changes.
 * <p>
 * This class provides the following features:
 * <ul>
 * <li>Provides a list of active train info files.
 * <li>Provides a map to link train info files names to the current ActiveTrain, if any.
 * <li>Creates an ActiveTrain on behalf of the Dispatcher Load Action.
 * <li>Provide the current ActiveTrain object, if any, for the a specific file name.
 * <li>Gracefully terminate an active train that was started by a LogixNG action.
 * </ul>
 */
public class DispatcherActiveTrainManager extends Bean implements InstanceManagerAutoDefault {

    private final HashMap<String, ActiveTrain> _activeTrainMap;

    public DispatcherActiveTrainManager() {
        InstanceManager.setDefault(DispatcherActiveTrainManager.class, this);
        _activeTrainMap = new HashMap<>();
    }

    /**
     * Get a list of existing train info file names.
     * @return an array of file names.
     */
    public List<String> getTrainInfoFileNames() {
        TrainInfoFile tiFiles = new TrainInfoFile();
        return new ArrayList<>(Arrays.asList(tiFiles.getTrainInfoFileNames()));
    }

    /**
     * Get the current ActiveTrain for the specified file name.
     * If the Dispatcher train no longer exists, remove the hash map entry.
     * Notify ExpressionDispatcher objects when an ActiveTrain no longer exists.
     * @param fileName The file name to be used for the lookup.
     * @return the ActiveTrain instance or null.  Null can mean no related active train or no file name match.
     */
    public ActiveTrain getActiveTrain(String fileName) {
        log.debug("1.1 -- getActiveTrain: {}", fileName);
        if (fileName == null) return null;
        if (! _activeTrainMap.containsKey(fileName)) return null;

        ActiveTrain currentTrain = getDispatcherActiveTrain(fileName);
        ActiveTrain hashTrain = _activeTrainMap.get(fileName);
        log.debug("1.2 -- getActiveTrain: found {}, current = {}", hashTrain, currentTrain);

        if (hashTrain != null && hashTrain != currentTrain) {
            log.debug("1.3 -- getActiveTrain: Remove active train");
            _activeTrainMap.remove(fileName);   // Update hash map
            firePropertyChange("ActiveTrain", fileName, "");
            hashTrain = null;
        }
        log.debug("1.4 -- getActiveTrain: return {}", hashTrain);
        return hashTrain;
    }

    /**
     * Create an ActiveTrain using the requested train info file.
     * <p>
     * If the create was successful, then any dispatcher active train with the same
     * transit name as the train info file is by defintion the train that was just requested.
     * @param fileName  The train info file name.
     * @return the active train or null if the create failed.
     */
    public ActiveTrain createActiveTrain(String fileName) {
        if (fileName == null) return null;

        ActiveTrain oldTrain = getActiveTrain(fileName);
        if (oldTrain != null) {
            log.warn("2.1 -- createActiveTrain: train already exists");
            return null;
        }

        DispatcherFrame df = InstanceManager.getDefault(DispatcherFrame.class);

        ActiveTrain at = null;
        int result = df.loadTrainFromTrainInfo(fileName);
        if (result == 0) {
            at = getDispatcherActiveTrain(fileName);
            _activeTrainMap.put(fileName, at);
            firePropertyChange("ActiveTrain", "", fileName);
        }

        log.debug("2.2 -- AT is {}", at);
        return at;

    }

    /**
     * Terminated the LogxiNG related active train if it still exists.
     * @param fileName The train info file name.
     */
    public void terminateActiveTrain(String fileName) {
        if (fileName == null) return;

        ActiveTrain oldTrain = getActiveTrain(fileName);
        if (oldTrain == null) return;

        oldTrain.setTerminateWhenDone(true);
        oldTrain.setStatus(ActiveTrain.DONE);

        _activeTrainMap.remove(fileName);
        firePropertyChange("ActiveTrain", fileName, "");
    }

    /**
     * Get the Dispatcher active train for the transit in the train info file.
     * <p>
     * The active train may or may not be related to the train info file or the ActiveTrain in
     * the hash map.
     * @param fileName The train info file name.
     * @return the ActiveTrain if a train has the same transit as the file, null if there is no match
     */
    public ActiveTrain getDispatcherActiveTrain(String fileName) {
        log.debug("4.1 -- getDispatcherActiveTrain: {}", fileName);
        TrainInfo tif = getTrainInfoFile(fileName);
        if (tif == null) return null;

        String transitName = tif.getTransitName();

        DispatcherFrame df = InstanceManager.getDefault(DispatcherFrame.class);
        for (ActiveTrain train : df.getActiveTrainsList()) {
            if (train.getTransitName().equals(transitName)) {
                log.debug("4.2 -- getDispatcherActiveTrain: file = {}, train = {}", fileName, train);
                return train;
            }
        }
        return null;
    }

    /**
     * Get the TrainInfo object for the requested file name.
     * @param fileName The name of the train info file.
     * @return a TrainInfo object or null if not found or invalid.
     */
    public TrainInfo getTrainInfoFile(String fileName) {
        if (fileName == null) return null;

        TrainInfoFile tiFiles = new TrainInfoFile();
        try {
            return tiFiles.readTrainInfo(fileName);
        } catch (org.jdom2.JDOMException | java.io.IOException ex) {
            log.warn("Unable to read the train info file for {}", fileName);
        }
        return null;
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DispatcherActiveTrainManager.class);
}

