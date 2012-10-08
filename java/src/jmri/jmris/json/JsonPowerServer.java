package jmri.jmris.json;

import java.io.IOException;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.jmris.AbstractPowerServer;
import jmri.jmris.JmriConnection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Send power status over WebSockets as a JSON String
 * 
 * This server creates a JSON string for a Power object with an integer value
 * for the Power state. These states are identical to the {@link jmri.PowerManager}
 * power states, with the addition of -1 as an error indicator.
 * 
 * The JSON string is in the form: <code>{type:'power', data:{state:&lt;integer&gt;}}</code>
 * 
 * @author rhwood
 */
public class JsonPowerServer extends AbstractPowerServer {

	private JmriConnection connection;
	private ObjectMapper mapper;

	public JsonPowerServer(JmriConnection connection) {
		this.connection = connection;
		this.mapper = new ObjectMapper();
		mgrOK();
	}

	@Override
	public void sendStatus(int status) throws IOException {
		ObjectNode root = this.mapper.createObjectNode();
		root.put("type", "power");
		ObjectNode data = root.putObject("data");
		data.put("state", status);
		this.connection.sendMessage(this.mapper.writeValueAsString(root));
	}

	@Override
	public void sendErrorStatus() throws IOException {
		ObjectNode root = this.mapper.createObjectNode();
		root.put("type", "error");
		ObjectNode data = root.putObject("error");
		data.put("code", -1);
		data.put("message", "JMRI Power Manager generated an error.");
		this.connection.sendMessage(this.mapper.writeValueAsString(root));
	}

	@Override
	public void parseStatus(String statusString) throws JmriException, IOException {
		this.parseRequest(this.mapper.readTree(statusString).path("data"));
	}
	
	public void parseRequest(JsonNode data) throws JmriException, IOException {
		switch (data.path("state").asInt(PowerManager.UNKNOWN)) {
		case PowerManager.OFF:
			this.setOffStatus();
			break;
		case PowerManager.ON:
			this.setOnStatus();
			break;
		case PowerManager.UNKNOWN:
			break;
		default:
			this.sendErrorStatus();
			break;
		}
		this.sendStatus(InstanceManager.powerManagerInstance().getPower());
	}

}
