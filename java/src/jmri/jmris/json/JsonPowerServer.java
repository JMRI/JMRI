package jmri.jmris.json;

import java.io.IOException;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.jmris.AbstractPowerServer;

import org.eclipse.jetty.websocket.WebSocket.Connection;

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
 * The JSON string is in the form: <code>{type:'power', data:{status:&lt;integer&gt;}}</code>
 * 
 * @author rhwood
 */
public class JsonPowerServer extends AbstractPowerServer {

	private Connection connection;
	private ObjectMapper mapper;

	public JsonPowerServer(Connection connection) {
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
		this.sendStatus(-1);
	}

	@Override
	public void parseStatus(String statusString) throws JmriException, IOException {
		JsonNode root = this.mapper.readTree(statusString);
		switch (root.path("data").path("state").asInt(PowerManager.UNKNOWN)) {
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
