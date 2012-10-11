//SimpleLightServer.java
package jmri.jmris.json;

import java.io.IOException;

import jmri.JmriException;
import jmri.Light;
import jmri.jmris.JmriConnection;
import jmri.jmris.AbstractLightServer;

import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Simple Server interface between the JMRI light manager and a network
 * connection
 *
 * @author Paul Bender Copyright (C) 2010
 * @version $Revision: 21313 $
 */
public class JsonLightServer extends AbstractLightServer {

    private JmriConnection connection = null;
    private ObjectMapper mapper = null;
    static Logger log = Logger.getLogger(JsonLightServer.class.getName());

    public JsonLightServer(JmriConnection connection) {
    	this.connection = connection;
    	this.mapper = new ObjectMapper();
    }
    
    /*
     * Protocol Specific Abstract Functions
     */
    @Override
    public void sendStatus(String lightName, int status) throws IOException {
		ObjectNode root = this.mapper.createObjectNode();
		root.put("type", "light");
		ObjectNode data = root.putObject("data");
		data.put("name", lightName);
		data.put("state", status);
		this.connection.sendMessage(this.mapper.writeValueAsString(root));
    }

    @Override
    public void sendErrorStatus(String lightName) throws IOException {
    	this.sendStatus(lightName, -1);
    }

    @Override
	public void parseStatus(String statusString) throws JmriException, IOException {
		this.parseRequest(this.mapper.readTree(statusString).path("data"));
	}
	
	public void parseRequest(JsonNode data) throws JmriException, IOException {
		switch (data.path("state").asInt(Light.UNKNOWN)) {
		case Light.ON:
			this.lightOn(data.path("name").asText());
			break;
		case Light.OFF:
			this.lightOff(data.path("name").asText());
			break;
		case Light.UNKNOWN:
			break;
		default:
			this.sendErrorStatus(data.path("name").asText());
			break;
		}
    }
    
}
