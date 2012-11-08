//JsonMemoryServer.java

package jmri.jmris.json;

import java.io.IOException;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmris.AbstractMemoryServer;
import jmri.jmris.JmriConnection;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * JSON Web Socket interface between the JMRI Memory manager and a
 * network connection
 * @author          mstevetodd Copyright (C) 2012 (copied from JsonSensorServer)
 * @version         $Revision:  $
 */

public class JsonMemoryServer extends AbstractMemoryServer {

	private JmriConnection connection;
	private ObjectMapper mapper;
	static Logger log = Logger.getLogger(JsonMemoryServer.class.getName());

	public JsonMemoryServer(JmriConnection connection) {
		super();
		this.connection = connection;
    	this.mapper = new ObjectMapper();
	}

	/*
	 * Protocol Specific Abstract Functions
	 */
	@Override
	public void sendStatus(String memoryName, String status) throws IOException {
    	ObjectNode root = this.mapper.createObjectNode();
    	root.put("type", "memory");
    	ObjectNode data = root.putObject("data");
    	data.put("name", memoryName);
    	data.put("state", status);
    	this.connection.sendMessage(this.mapper.writeValueAsString(root));
	}

	@Override
	public void sendErrorStatus(String memoryName) throws IOException {
		ObjectNode root = this.mapper.createObjectNode();
		root.put("type", "error");
		ObjectNode data = root.putObject("error");
		data.put("name", memoryName);
		data.put("code", -1);
		data.put("message", "Error accessing memory: " + memoryName);
		this.connection.sendMessage(this.mapper.writeValueAsString(root));
	}

	@Override
	public void parseStatus(String statusString) throws JmriException, IOException {
		this.parseRequest(this.mapper.readTree(statusString).path("data"));
	}
	
	public void parseRequest(JsonNode data) throws JmriException, IOException {
    	String name = data.path("name").asText();
    	String state = data.path("state").asText();
    	if (state == "") {  //if not passed, retrieve current and respond
    		state = InstanceManager.memoryManagerInstance().provideMemory(name).getValue().toString();
    		this.sendStatus(name, state);
    	} else { //else set the value to the value passed in
    		this.setMemoryValue(name,  state);
    	}
       this.addMemoryToList(name);
	}

}
