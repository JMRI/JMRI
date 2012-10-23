//SimpleOperationsServer.java

package jmri.jmris.json;

import java.io.IOException;
import java.util.ArrayList;

import javax.management.Attribute;

import jmri.JmriException;
import jmri.jmris.AbstractOperationsServer;
import jmri.jmris.JmriConnection;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Simple interface between the JMRI operations and a network connection
 * 
 * @author Paul Bender Copyright (C) 2010
 * @author Dan Boudreau Copyright (C) 2012 (Documented the code, changed reply format, and some minor refactoring)
 * @version $Revision: 21313 $
 */

public class JsonOperationsServer extends AbstractOperationsServer {

	public static final String OPERATIONS = "operations";

	// The supported commands for operations
	/**
	 * the tag identifying the train's identity
	 */
	public static final String TRAIN = "train";

	/**
	 * Returns a list of trains.  One line for each train in the list.
	 */
	public static final String TRAINS = "trains";

	/**
	 * Returns a list of locations that the trains visit. One line
	 * for each location in the list.
	 */
	public static final String LOCATIONS = "locations";

	/**
	 * Requests/returns the train's length.  The train's name is required.
	 * Proper message format: "OPERATIONS TRAIN=train_name , TRAINLENGTH"
	 * Returns train length if train exists, otherwise an error message.
	 * <P>
	 * Request: "Operations , TRAIN=train"
	 * <P>
	 * Reply: "OPERATIONS , TRAIN=train , TRAINLENGTH=train_length"
	 */
	public static final String TRAINLENGTH = "length";

	/**
	 * Requests/returns the train's weight.  The train's name is required.
	 */
	public static final String TRAINWEIGHT = "weight";

	/**
	 * Requests/returns the number of cars in the train.  The train's name is required.
	 */
	public static final String TRAINCARS = "cars";

	/**
	 * Requests/returns the road and number of the lead loco for this train.  The train's name is required.
	 */
	public static final String TRAINLEADLOCO = "leadLoco";

	/**
	 * Requests/returns the road and number of the caboose for this train if there's one assigned.  The train's name is required.
	 */
	public static final String TRAINCABOOSE = "caboose";

	/**
	 * Requests/returns the the train's status.  The train's name is required.
	 */	
	public static final String TRAINSTATUS = "status";

	/**
	 * Terminates the train and returns the train's status.  The train's name is required.
	 */	
	public static final String TERMINATE = "terminate";

	/**
	 * Sets/requests/returns the train's location or gets the train's current location.
	 * <P>
	 * Sets the train's location: "Operations , TRAIN=train_name , TRAINLOCATION=location"
	 * <P>
	 * Requests the train's location: "OPERATIONS , TRAIN=train_name"
	 * <P>
	 * Returns the train's location: "OPERATIONS , TRAIN=train_name , TRAINLOCATION=location" 
	 */	
	public static final String TRAINLOCATION = "location";

	private JmriConnection connection;
	private ObjectMapper mapper;
	static Logger log = Logger.getLogger(JsonOperationsServer.class.getName());

	public JsonOperationsServer(JmriConnection connection) {
		super();
		this.connection = connection;
		this.mapper = new ObjectMapper();
	}

	/*
	 * Protocol Specific Simple Functions
	 */

	/**
	 * send a JSON object to the connection.   The object is built from a set of attributes.
	 * @param contents is the ArrayList of Attributes to be sent.
	 */
	public void sendMessage(ArrayList<Attribute> contents) throws IOException {
		ObjectNode root = this.mapper.createObjectNode();
		root.put("type", OPERATIONS);
		ObjectNode data = root.putObject("data");
		for (Attribute attr : contents) {
			if (attr.getValue() != null) {
				data.put(attr.getName(), attr.getValue().toString());
			} else {
				data.putNull(attr.getName());
			}
		}
		this.connection.sendMessage(this.mapper.writeValueAsString(root));
	}

	/**
	 * Constructs an error message and sends it to the client as a JSON message
	 * 
	 * @param errorStatus is the error message.  It need not include any padding - this method
	 * will add it.  It should be plain text.
	 * @throws IOException if there is a problem sending the error message
	 */
	public void sendErrorStatus(String errorStatus) throws IOException {
		ObjectNode root = this.mapper.createObjectNode();
		root.put("type", "error");
		ObjectNode data = root.putObject("error");
		data.put("code", -1);
		data.put("message", errorStatus);
		this.connection.sendMessage(this.mapper.writeValueAsString(root));
	}

	/**
	 * Parse a string into a JSON structure and then parse the data node for operations commands
	 */
	@Override
	public void parseStatus(String statusString) throws JmriException, IOException {
		this.parseRequest(this.mapper.readTree(statusString).path("data"));
	}

	public void parseRequest(JsonNode data) throws JmriException, IOException {
		ArrayList<Attribute> response = new ArrayList<Attribute>();
		if (!data.path(LOCATIONS).isMissingNode()) {
			this.sendLocationList();
		} else if (!data.path(TRAINS).isMissingNode()) {
			this.sendTrainList();
		} else if (!data.path(TRAIN).isMissingNode()) {
			String train = data.path(TRAIN).asText();
			response.add(new Attribute(TRAIN, train));
			if (!data.path(TRAINLENGTH).isMissingNode()) {
				response.add(new Attribute(TRAINLENGTH, this.constructTrainLength(train)));
			}
			if (!data.path(TRAINWEIGHT).isMissingNode()) {
				response.add(new Attribute(TRAINWEIGHT, this.constructTrainWeight(train)));
			}
			if (!data.path(TRAINCARS).isMissingNode()) {
				response.add(new Attribute(TRAINCARS, this.constructTrainNumberOfCars(train)));
			}
			if (!data.path(TRAINLEADLOCO).isMissingNode()) {
				response.add(new Attribute(TRAINLEADLOCO, this.constructTrainLeadLoco(train)));
			}
			if (!data.path(TRAINCABOOSE).isMissingNode()) {
				response.add(new Attribute(TRAINCABOOSE, this.constructTrainCaboose(train)));
			}
			if (!data.path(TRAINSTATUS).isMissingNode()) {
				response.add(new Attribute(TRAINSTATUS, this.constructTrainStatus(train)));
			}
			if (!data.path(TERMINATE).isMissingNode()) {
				response.add(new Attribute(TERMINATE, this.terminateTrain(train)));
			}
			if (!data.path(TRAINLOCATION).isMissingNode()) {
				if (data.path(TRAINLOCATION).isNull()) {
					response.add(new Attribute(TRAINLOCATION, this.constructTrainLocation(train)));
				} else {
					response.add(new Attribute(TRAINLOCATION, this.setTrainLocation(train, data.path(TRAINLOCATION).asText())));
				}
			}
			if (response.size() > 1) {
				this.sendMessage(response);
			}
		} else {
			this.sendErrorStatus("required attribute train is missing");
		}
	}

}
