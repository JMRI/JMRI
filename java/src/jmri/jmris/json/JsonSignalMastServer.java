//JsonSignalMastServer.java
package jmri.jmris.json;

import java.io.IOException;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.SignalMast;
import jmri.jmris.AbstractSignalMastServer;
import jmri.jmris.JmriConnection;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * JSON Web Socket interface between the JMRI SignalMast manager and a
 * network connection
 *
 * @author Paul Bender Copyright (C) 2010
 * @version $Revision: 21313 $
 */
public class JsonSignalMastServer extends AbstractSignalMastServer {

    private JmriConnection connection;
    private ObjectMapper mapper;
    
    static Logger log = Logger.getLogger(JsonSignalMastServer.class.getName());

    public JsonSignalMastServer(JmriConnection connection) {
    	super();
    	this.connection = connection;
    	this.mapper = new ObjectMapper();
    }
    
    /*
     * Protocol Specific Abstract Functions
     */
    @Override
    public void sendStatus(String signalMastName, String status) throws IOException {
    	ObjectNode root = this.mapper.createObjectNode();
    	root.put("type", "signalMast");
    	ObjectNode data = root.putObject("data");
    	data.put("name", signalMastName);
    	data.put("state", status);
    	this.connection.sendMessage(this.mapper.writeValueAsString(root));
    }

    @Override
    public void sendErrorStatus(String signalMastName) throws IOException {
		ObjectNode root = this.mapper.createObjectNode();
		root.put("type", "error");
		ObjectNode data = root.putObject("error");
		data.put("name", signalMastName);
		data.put("code", -1);
		data.put("message", "Error accessing signalMast");
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
    		SignalMast sm = InstanceManager.signalMastManagerInstance().getSignalMast(name); 
    		state = sm.getAspect();
			if ((sm.getHeld()) && (sm.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.HELD)!=null)) {
	    		state = "Held";
			} else if ((sm.getLit()) && (sm.getAppearanceMap().getSpecificAppearance(jmri.SignalAppearanceMap.DARK)!=null)) {
	    		state = "Dark";
			}
    		this.sendStatus(name, state);
    	} else { //else set the aspect to the state passed in
    		this.setSignalMastAspect(name, state);
    	}
    	this.addSignalMastToList(name);
    }
}
