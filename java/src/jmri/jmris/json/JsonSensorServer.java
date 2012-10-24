//SimpleSensorServer.java

package jmri.jmris.json;

import java.io.IOException;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;
import jmri.jmris.AbstractSensorServer;
import jmri.jmris.JmriConnection;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * JSON Web Socket interface between the JMRI Sensor manager and a
 * network connection
 * @author          Paul Bender Copyright (C) 2010
 * @version         $Revision: 21313 $
 */

public class JsonSensorServer extends AbstractSensorServer {

	private JmriConnection connection;
	private ObjectMapper mapper;
	static Logger log = Logger.getLogger(JsonSensorServer.class.getName());

	public JsonSensorServer(JmriConnection connection) {
		super();
		this.connection = connection;
    	this.mapper = new ObjectMapper();
	}

	/*
	 * Protocol Specific Abstract Functions
	 */
	@Override
	public void sendStatus(String sensorName, int status) throws IOException {
    	ObjectNode root = this.mapper.createObjectNode();
    	root.put("type", "sensor");
    	ObjectNode data = root.putObject("data");
    	data.put("name", sensorName);
    	data.put("state", status);
    	this.connection.sendMessage(this.mapper.writeValueAsString(root));
	}

	@Override
	public void sendErrorStatus(String sensorName) throws IOException {
		ObjectNode root = this.mapper.createObjectNode();
		root.put("type", "error");
		ObjectNode data = root.putObject("error");
		data.put("name", sensorName);
		data.put("code", -1);
		data.put("message", "Error accessing sensor");
		this.connection.sendMessage(this.mapper.writeValueAsString(root));
	}

	@Override
	public void parseStatus(String statusString) throws JmriException, IOException {
		this.parseRequest(this.mapper.readTree(statusString).path("data"));
	}
	
	public void parseRequest(JsonNode data) throws JmriException, IOException {
		int state = data.path("state").asInt(Sensor.UNKNOWN);
		String name = data.path("name").asText();
		switch (state) {
		case Sensor.ACTIVE:
			this.setSensorActive(name);
			break;
		case Sensor.INACTIVE:
			this.setSensorInactive(name);
			break;
		default:
			this.sendStatus(name, InstanceManager.sensorManagerInstance().provideSensor(name).getKnownState());
			break;
        }
        this.addSensorToList(name);
	}

}
