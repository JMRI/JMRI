//SimpleSignalHeadServer.java
package jmri.jmris.json;

import java.io.IOException;

import jmri.JmriException;
import jmri.SignalHead;
import jmri.jmris.AbstractSignalHeadServer;
import jmri.jmris.JmriConnection;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Simple Server interface between the JMRI Sensor manager and a network
 * connection
 *
 * @author Paul Bender Copyright (C) 2010
 * @version $Revision: 21313 $
 */
public class JsonSignalHeadServer extends AbstractSignalHeadServer {

    private JmriConnection connection;
    private ObjectMapper mapper;
    
    static Logger log = Logger.getLogger(JsonSignalHeadServer.class.getName());

    public JsonSignalHeadServer(JmriConnection connection) {
    	super();
    	this.connection = connection;
    	this.mapper = new ObjectMapper();
    }
    
    /*
     * Protocol Specific Abstract Functions
     */
    @Override
    public void sendStatus(String signalHeadName, int status) throws IOException {
    	ObjectNode root = this.mapper.createObjectNode();
    	root.put("type", "signalHead");
    	ObjectNode data = root.putObject("data");
    	data.put("name", signalHeadName);
    	data.put("state", status);
    	this.connection.sendMessage(this.mapper.writeValueAsString(root));
    }

    @Override
    public void sendErrorStatus(String signalHeadName) throws IOException {
		ObjectNode root = this.mapper.createObjectNode();
		root.put("type", "error");
		ObjectNode data = root.putObject("error");
		data.put("name", signalHeadName);
		data.put("code", -1);
		data.put("message", "Error accessing signalHead");
		this.connection.sendMessage(this.mapper.writeValueAsString(root));
    }

    @Override
	public void parseStatus(String statusString) throws JmriException, IOException {
		this.parseRequest(this.mapper.readTree(statusString).path("data"));
	}
	
	public void parseRequest(JsonNode data) throws JmriException, IOException {
		int state = data.path("state").asInt(SignalHead.UNKNOWN);
		String name = data.path("name").asText();
		this.setSignalHeadAppearance(name, state);
        this.addSignalHeadToList(name);
    }

}
