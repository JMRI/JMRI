package jmri.jmris;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.management.Attribute;

import jmri.JmriException;
import jmri.jmrit.operations.locations.LocationManager;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.trains.Train;
import jmri.jmrit.operations.trains.TrainManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract interface between the JMRI operations and a network connection
 *
 * @author Paul Bender Copyright (C) 2010
 * @author Dan Boudreau Copyright (C) 2012 (added checks for null train)
 * @author Rodney Black Copyright (C) 2012
 * @author Randall Wood Copyright (C) 2012, 2014
 */
abstract public class AbstractOperationsServer implements PropertyChangeListener {

    protected final TrainManager tm;
    protected final LocationManager lm;
    protected final HashMap<String, TrainListener> trains;

    @SuppressWarnings("LeakingThisInConstructor")
    public AbstractOperationsServer() {
        tm = jmri.InstanceManager.getDefault(TrainManager.class);
        tm.addPropertyChangeListener(this);
        lm = jmri.InstanceManager.getDefault(LocationManager.class);
        lm.addPropertyChangeListener(this);
        addPropertyChangeListeners();
        trains = new HashMap<>();
    }

    public abstract void sendTrainList();

    public abstract void sendLocationList();

    /**
     * constructs a String containing the status of a train
     *
     * @param trainName is the name of the train. If not found in Operations, an
     *                  error message is sent to the client.
     * @return the train's status as known by Operations
     * @throws IOException on failure to send an error message to the client
     */
    public String constructTrainStatus(String trainName) throws IOException {
        Train train = tm.getTrainByName(trainName);
        if (train != null) {
            return train.getStatus();
        }
        sendErrorStatus("ERROR train name doesn't exist " + trainName);
        return null;
    }

    /**
     * constructs a String containing the location of a train
     *
     * @param trainName is the name of the desired train. If not found in
     *                  Operations, an error message is sent to the client
     * @return the train's location, as known by Operations
     * @throws IOException on failure to send an error message to the client
     */
    public String constructTrainLocation(String trainName) throws IOException {
        Train train = tm.getTrainByName(trainName);
        if (train != null) {
            return train.getCurrentLocationName();
        }
        sendErrorStatus("ERROR train name doesn't exist " + trainName);
        return null;
    }

    /**
     * constructs a String containing the location of a train
     *
     * @param trainName    is the name of the desired train. If not found in
     *                     Operations, an error message is sent to the client
     * @param locationName is the name of the desired location.
     * @return the train's location, as known by Operations
     * @throws IOException on failure to send an error message to the client
     */
    public String setTrainLocation(String trainName, String locationName)
            throws IOException {
        log.debug("Set train " + trainName + " Location " + locationName);
        Train train = tm.getTrainByName(trainName);
        if (train != null) {
            if (!exactLocationName && train.move(locationName)
                    || exactLocationName && train.moveToNextLocation(locationName)) {
                return constructTrainLocation(trainName);
            } else {
                sendErrorStatus("WARNING move of " + trainName + " to location " + locationName
                        + " failed. Train's current location " + train.getCurrentLocationName()
                        + " next location " + train.getNextLocationName());
            }
        } else {
            sendErrorStatus("ERROR train name doesn't exist " + trainName);
        }
        return null;
    }

    private static boolean exactLocationName = true;

    public static void setExactLocationName(boolean enabled) {
        exactLocationName = enabled;
    }

    public static boolean isExactLoationNameEnabled() {
        return exactLocationName;
    }

    /**
     * constructs a String containing the length of a train
     *
     * @param trainName is the name of the desired train. If not found in
     *                  Operations, an error message is sent to the client
     * @return the train's length, as known by Operations
     * @throws IOException on failure to send an error message to the client
     */
    public String constructTrainLength(String trainName) throws IOException {
        Train train = tm.getTrainByName(trainName);
        if (train != null) {
            return String.valueOf(train.getTrainLength());
        }
        sendErrorStatus("ERROR train name doesn't exist " + trainName);
        return null;
    }

    /**
     * constructs a String containing the tonnage of a train
     *
     * @param trainName is the name of the desired train. If not found in
     *                  Operations, an error message is sent to the client
     * @return the train's tonnage, as known by Operations
     * @throws IOException on failure to send an error message to the client
     */
    public String constructTrainWeight(String trainName) throws IOException {
        Train train = tm.getTrainByName(trainName);
        if (train != null) {
            return String.valueOf(train.getTrainWeight());
        }
        sendErrorStatus("ERROR train name doesn't exist " + trainName);
        return null;
    }

    /**
     * constructs a String containing the number of cars in a train
     *
     * @param trainName is the name of the desired train. If not found in
     *                  Operations, an error message is sent to the client
     * @return the number of cars in a train, as known by Operations
     * @throws IOException on failure to send an error message to the client
     */
    public String constructTrainNumberOfCars(String trainName) throws IOException {
        Train train = tm.getTrainByName(trainName);
        if (train != null) {
            return String.valueOf(train.getNumberCarsInTrain());
        }
        sendErrorStatus("ERROR train name doesn't exist " + trainName);
        return null;
    }

    /**
     * Constructs a String containing the road and number of lead loco, if
     * there's one assigned to the train.
     *
     * @param trainName is the name of the desired train. If not found in
     *                  Operations, an error message is sent to the client
     * @return the lead loco
     * @throws IOException on failure to send an error message to the client
     */
    public String constructTrainLeadLoco(String trainName) throws IOException {
        Train train = tm.getTrainByName(trainName);
        if (train != null) {
            Engine leadEngine = train.getLeadEngine();
            if (leadEngine != null) {
                return leadEngine.toString();
            }
        } else { // train is null
            sendErrorStatus("ERROR train name doesn't exist " + trainName);
        }
        return null;
    }

    /**
     * constructs a String containing the caboose on a train
     *
     * @param trainName is the name of the desired train. If not found in
     *                  Operations, an error message is sent to the client
     * @return the caboose on a train, as known by Operations
     * @throws IOException on failure to send an error message to the client
     */
    public String constructTrainCaboose(String trainName) throws IOException {
        Train train = tm.getTrainByName(trainName);
        if (train != null) {
            return train.getCabooseRoadAndNumber();
        }
        sendErrorStatus("ERROR train name doesn't exist " + trainName);
        return null;
    }

    /**
     * tells Operations that a train has terminated. If not found in Operations,
     * an error message is sent to the client
     *
     * @param trainName is the name of the train
     * @return the termination String
     * @throws IOException on failure to send an error message to the client
     */
    public String terminateTrain(String trainName) throws IOException {
        Train train = tm.getTrainByName(trainName);
        if (train != null) {
            train.terminate();
            return constructTrainStatus(trainName);
        }
        sendErrorStatus("ERROR train name doesn't exist " + trainName);
        return null;
    }

    /**
     * sends the full status for a train to a client
     *
     * @param train is the name of the desired train. If not found, an error
     *                  is sent to the client
     * @throws IOException on failure to send an error message
     */
     //public void sendFullStatus(String trainName) throws IOException {
     //   Train train = tm.getTrainByName(trainName);
     //       if (train != null) {
     //           sendFullStatus(train);
     //       } else {
     //           sendErrorStatus("ERROR train name doesn't exist " + trainName);
     //       }
     //}

    /**
     * sends the full status for a train to a client
     *
     * @param train is the Train object we are sending information about.
     * @throws IOException on failure to send an error message
     */
    public abstract void sendFullStatus(Train train) throws IOException;

    private void addPropertyChangeListeners() {
        List<Train> trainList = tm.getTrainsByNameList();
        for (Train train : trainList) {
            train.addPropertyChangeListener(this);
        }
    }

    private void removePropertyChangeListeners() {
        List<Train> trainList = tm.getTrainsByNameList();
        for (Train train : trainList) {
            train.removePropertyChangeListener(this);
        }
    }

    @Override
    public abstract void propertyChange(PropertyChangeEvent e);

    synchronized protected void addTrainToList(String trainId) {
        if (!trains.containsKey(trainId)) {
            trains.put(trainId, new TrainListener(trainId));
            jmri.InstanceManager.getDefault(TrainManager.class).getTrainById(trainId).addPropertyChangeListener(trains.get(trainId));
        }
    }

    synchronized protected void removeTrainFromList(String trainId) {
        if (trains.containsKey(trainId)) {
            jmri.InstanceManager.getDefault(TrainManager.class).getTrainById(trainId).removePropertyChangeListener(trains.get(trainId));
            trains.remove(trainId);
        }
    }

    protected TrainListener getListener(String trainId) {
        return new TrainListener(trainId);
    }

    public void dispose() {
        if (tm != null) {
            tm.removePropertyChangeListener(this);
            removePropertyChangeListeners();
        }
        if (lm != null) {
            lm.removePropertyChangeListener(this);
        }
        for (Map.Entry<String, TrainListener> train : this.trains.entrySet()) {
            jmri.InstanceManager.getDefault(TrainManager.class).getTrainById(train.getKey()).removePropertyChangeListener(train.getValue());
        }
        this.trains.clear();
    }

    /*
     * Protocol Specific Abstract Functions
     */
    abstract public void sendMessage(ArrayList<Attribute> contents) throws IOException;

    abstract public void sendErrorStatus(String errorStatus) throws IOException;

    abstract public void parseStatus(String statusString) throws JmriException, IOException;

    private final static Logger log = LoggerFactory.getLogger(AbstractOperationsServer.class);

    /*
     * This isn't currently used for operations
     */
    protected class TrainListener implements PropertyChangeListener {

        private final Train train;

        protected TrainListener(String trainId) {
            this.train = jmri.InstanceManager.getDefault(TrainManager.class).getTrainById(trainId);
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            try {
                sendFullStatus(this.train);
            } catch (IOException ie) {
                log.debug("Error Sending Status");
                // if we get an error, de-register
                this.train.removePropertyChangeListener(this);
                removeTrainFromList(this.train.getId());
            }
        }
    }

}
