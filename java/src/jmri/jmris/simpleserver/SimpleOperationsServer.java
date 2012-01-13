//SimpleOperationsServer.java

package jmri.jmris.simpleserver;

import java.io.*;

/**
 * Simple interface between the JMRI operations and a network connection
 * 
 * @author Paul Bender Copyright (C) 2010
 * @author Dan Boudreau Copyright (C) 2012 (Documented the code, changed reply format, and some minor refactoring)
 * @version $Revision$
 */

public class SimpleOperationsServer extends jmri.jmris.AbstractOperationsServer {
	
	/**
	 * All operation messages start with the key word "OPERATIONS" followed by a command like
	 * "TRAINS".  The reply message also starts with the key word "OPERATIONS" followed by
	 * the original command followed by the desired results. 
	 */
	public static final String OPERATIONS = "OPERATIONS";
	
	// The supported commands for operations
	/**
	 * Returns a list of trains.  One reply message for each train in the list.
	 */
	public static final String TRAINS = "TRAINS";
	/**
	 * Returns a list of locations that the trains visit. One reply
	 * message for each location in the list.
	 */
	public static final String LOCATIONS = "LOCATIONS";
	/**
	 * Returns the train's length.  The train's name is required.
	 * Proper message format: "OPERATIONS TRAINLENGTH train_name"
	 * Returns train length if train exists, otherwise an error message.
	 * <P>
	 * Reply: "OPERATIONS TRAINLENGTH train_name , train_length"
	 */
	public static final String TRAINLENGTH = "TRAINLENGTH";
	/**
	 * Returns the train's weight.  The train's name is required.
	 */
	public static final String TRAINWEIGHT = "TRAINWEIGHT";
	/**
	 * Returns the number of cars in the train.  The train's name is required.
	 */
	public static final String TRAINCARS = "TRAINCARS";
	/**
	 * Returns the the train's status.  The train's name is required.
	 */	
	public static final String TRAINSTATUS = "TRAINSTATUS";
	/**
	 * Terminates the train and returns the train's status.  The train's name is required.
	 */	
	public static final String TERMINATE = "TERMINATE";
	/**
	 * Sets the train's location or gets the train's current location.  
	 * <P>
	 * To get the train's location, use this format:
	 * <P>
	 * "OPERATIONS TRAINLOCATION train_name"
	 * <P>
	 * Reply: "OPERATIONS TRAINLOCATION train_name , location_name"
	 * <P>
	 * To set the train's location include the location name in the message,
	 * both the train's name and location name are required.
	 * The train's name and location name must be separated by space comma space.
	 * <P>
	 * Correct format: "OPERATIONS TRAINLOCATION train_name , location_name"
	 */	
	public static final String TRAINLOCATION = "TRAINLOCATION";
	
	public static final String DELIMITER = " , "; // delimiter

	private DataOutputStream output;

	public SimpleOperationsServer(DataInputStream inStream,
			DataOutputStream outStream) {
		super();
		output = outStream;
	}

	/*
	 * Protocol Specific Simple Functions
	 */

	public void sendInfoString(String statusString) throws IOException {
		output.writeBytes(statusString + "\n");
	}

	public void sendErrorStatus() throws IOException {
	}

	/**
	 * Parse operation commands. They all start with "OPERATIONS" followed by a
	 * command like "LOCATIONS". A command like "TRAINLENGTH" requires a train
	 * name. The delimiter is the space character.
	 */
	public void parseStatus(String statusString) throws jmri.JmriException {
		try {
			if (statusString.contains(LOCATIONS))
				sendLocationList();
			else if (statusString.contains(TRAINLENGTH))
				sendTrainLength(getName(statusString));
			else if (statusString.contains(TRAINWEIGHT))
				sendTrainWeight(getName(statusString));
			else if (statusString.contains(TRAINCARS))
				sendTrainNumberOfCars(getName(statusString));
			else if (statusString.contains(TRAINSTATUS))
				sendTrainStatus(getName(statusString));
			else if (statusString.contains(TRAINS))
				sendTrainList();
			else if (statusString.contains(TERMINATE))
				terminateTrain(getName(statusString));
			else if (statusString.contains(TRAINLOCATION)) {
				int index, index2;
				index = statusString.indexOf(" ") + 1;
				index = statusString.indexOf(" ", index) + 1;
				// new message format, uses (space comma space) between train name and location name
				// this allows the train name to consist of several words
				if (statusString.contains(" , ")) {
					index2 = statusString.indexOf(DELIMITER, index);
					setTrainLocation(statusString.substring(index, index2),
							statusString.substring(index2 + 1));
				// old message format
				/*} else if ((index2 = statusString.indexOf(" ", index)) > 0) {
					// set the location.
					log.debug("setting location index = " + index
							+ "index 2 = " + index2 + " String " + statusString);
					// this code incorrectly assumes that the train name is
					// only one word.
					setTrainLocation(statusString.substring(index, index2),
							statusString.substring(index2 + 1));
							*/
				} else {
					// get the location.
					sendTrainLocation(getName(statusString));
				}
			} else
				throw new jmri.JmriException();
		} catch (java.io.IOException ioe) {
			throw new jmri.JmriException();
		}
	}
	
	/*
	 * Skips the first two words in the status string
	 * and returns the remainder of the string.
	 */
	private String getName(String statusString){
		int index;
		index = statusString.indexOf(" ") + 1;
		index = statusString.indexOf(" ", index) + 1;
		return statusString.substring(index);
	}

	static org.apache.log4j.Logger log = org.apache.log4j.Logger
			.getLogger(SimpleOperationsServer.class.getName());

}
