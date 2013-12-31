//AbstractOperationsServer.java

package jmri.jmris;

import java.beans.PropertyChangeListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

import javax.management.Attribute;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jmri.JmriException;
import jmri.jmris.simpleserver.SimpleOperationsServer;
import jmri.jmrit.operations.rollingstock.engines.Engine;
import jmri.jmrit.operations.trains.*;
import jmri.jmrit.operations.locations.*;

/**
 * Abstract interface between the JMRI operations and a network connection
 * 
 * @author Paul Bender Copyright (C) 2010
 * @author Dan Boudreau Copyright (C) 2012 (added checks for null train)
 * @author Rodney Black Copyright (C) 2012
 * @author Randall Wood Copyright (C) 2012 
 * @version $Revision$
 */

abstract public class AbstractOperationsServer implements PropertyChangeListener {

	TrainManager tm = null;
	LocationManager lm = null;

	public AbstractOperationsServer() {
		tm = TrainManager.instance();
		tm.addPropertyChangeListener(this);
		lm = LocationManager.instance();
		lm.addPropertyChangeListener(this);
		addPropertyChangeListeners();
	}

	/**
	 * send a list of trains known by Operations to the client
	 */
	public void sendTrainList() {
		List<String> trainList = tm.getTrainsByNameList();
		ArrayList<Attribute> aTrain;
		for (String trainID : trainList) {
		    aTrain = new ArrayList<Attribute>(1);
		    aTrain.add(new Attribute(SimpleOperationsServer.TRAINS, tm.getTrainById(trainID).getName()));
		    try {
		        sendMessage(aTrain);
		    }
		    catch (IOException ioe) {
		        log.debug("could not send train " + tm.getTrainById(trainID).getName());
		    }
		}
	}

	/**
	 *  send a list of locations known by Operations to the client
	 */
	public void sendLocationList() {
		List<Location> locationList = lm.getLocationsByNameList();
		ArrayList<Attribute> location;
		for (Location loc : locationList) {
		    location = new ArrayList<Attribute>(1);
		    location.add(new Attribute(SimpleOperationsServer.LOCATIONS, loc));
            try {
                sendMessage(location);
            }
            catch (IOException ioe) {
                log.debug("could not send train " + loc.getName());
            }
		}
	}

	/**
	 * constructs a String containing the status of a train
	 * @param trainName is the name of the train.  If not found in Operations, an error message
	 * is sent to the client.
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
	 * @param trainName is the name of the desired train.  If not found in Operations, an
	 * error message is sent to the client
	 * @return the train's location, as known by Operations
	 * @throws IOException on failure to send an error message ot the client
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
     * @param trainName is the name of the desired train.  If not found in Operations, an
     * error message is sent to the client
     * @return the train's location, as known by Operations
     * @throws IOException on failure to send an error message ot the client
	 */
	public String setTrainLocation(String trainName, String locationName)
			throws IOException {
		log.debug("Set train " + trainName + " Location " + locationName);
		Train train = tm.getTrainByName(trainName);
		if (train != null) {
			if (!exactLocationName && train.move(locationName)
					|| exactLocationName && train.moveToNextLocation(locationName)) {
				return constructTrainLocation(trainName);
			}
			else {
				sendErrorStatus("WARNING move of " + trainName + " to location " + locationName 
						+ " failed. Train's current location " +train.getCurrentLocationName()
						+ " next location " + train.getNextLocationName());
			}
		} else {
			sendErrorStatus("ERROR train name doesn't exist " + trainName);
		}
		return null;
	}
	
	private static boolean exactLocationName = true;
	public static void setExactLocationName(boolean enabled){
		exactLocationName = enabled;
	}
	public static boolean isExactLoationNameEnabled(){
		return exactLocationName;
	}

	/**
	 * constructs a String containing the length of a train
	 * @param trainName is the name of the desired train.  If not found in Operations, an
	 * error message is sent to the client
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
     * @param trainName is the name of the desired train.  If not found in Operations, an
     * error message is sent to the client
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
     * @param trainName is the name of the desired train.  If not found in Operations, an
     * error message is sent to the client
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
	 * Constructs a String containing the road and number of lead loco, if there's one assigned to the train.
	 * @param trainName is the name of the desired train.  If not found in Operations, an
     * error message is sent to the client
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
	    }
	    sendErrorStatus("ERROR train name doesn't exist " + trainName);
	    return null;
	}

    /**
     * constructs a String containing the caboose on a train
     * @param trainName is the name of the desired train.  If not found in Operations, an
     * error message is sent to the client
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
	 * tells Operations that a train has terminated.  If not found in Operations, an
     * error message is sent to the client
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
	 * @param trainName is the name of the desired train.  If not found, an error is sent to
	 * the client
	 * @throws IOException on failure to send an error message
	 */
	public void sendFullStatus(String trainName) throws IOException {
	    Train train = tm.getTrainByName(trainName);
	    if (train != null)
	    	sendFullStatus(train);
	    else 
	        sendErrorStatus("ERROR train name doesn't exist " + trainName);
	}
	
	/**
	 * sends the full status for a train to a client
	 * @param train The desired train.  
	 * @throws IOException on failure to send an error message
	 */
	public void sendFullStatus(Train train) throws IOException {
	    ArrayList<Attribute> status = new ArrayList<Attribute>();
	    if (train != null) {
	        status.add(new Attribute(SimpleOperationsServer.TRAIN, train.getName()));
	        status.add(new Attribute(SimpleOperationsServer.TRAINLOCATION, train.getCurrentLocationName()));
            status.add(new Attribute(SimpleOperationsServer.TRAINLENGTH, String.valueOf(train.getTrainLength())));
            status.add(new Attribute(SimpleOperationsServer.TRAINWEIGHT, String.valueOf(train.getTrainWeight())));
            status.add(new Attribute(SimpleOperationsServer.TRAINCARS, String.valueOf(train.getNumberCarsInTrain())));
            status.add(new Attribute(SimpleOperationsServer.TRAINLEADLOCO, constructTrainLeadLoco(train.getName())));
            status.add(new Attribute(SimpleOperationsServer.TRAINCABOOSE, constructTrainCaboose(train.getName())));
            sendMessage(status);
	    }
	}
	
	private void addPropertyChangeListeners(){
		java.util.List<String> trainList = tm.getTrainsByNameList();		
		for (String trainID : trainList) {
			tm.getTrainById(trainID).addPropertyChangeListener(this);
		}
	}
	
	private void removePropertyChangeListeners(){
		java.util.List<String> trainList = tm.getTrainsByNameList();		
		for (String trainID : trainList) {
			tm.getTrainById(trainID).removePropertyChangeListener(this);
		}
	}
	
	public void propertyChange(java.beans.PropertyChangeEvent e) {
		log.debug("property change: "+ e.getPropertyName()+" old: "+e.getOldValue()+" new: "+e.getNewValue());
	if (e.getPropertyName().equals(Train.BUILT_CHANGED_PROPERTY))
		try {
			sendFullStatus((Train)e.getSource());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void dispose() {
		if (tm != null)
			tm.removePropertyChangeListener(this);
		if (lm != null)
			lm.removePropertyChangeListener(this);
		removePropertyChangeListeners();
	}



	/*
	 * Protocol Specific Abstract Functions
	 */

	abstract public void sendMessage(ArrayList<Attribute> contents) throws IOException;

	abstract public void sendErrorStatus(String errorStatus) throws IOException;

	abstract public void parseStatus(String statusString) throws JmriException, IOException;

	static Logger log = LoggerFactory.getLogger(AbstractOperationsServer.class.getName());

}
