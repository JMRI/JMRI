package jmri.jmris.json;

import java.io.IOException;

import org.apache.log4j.Logger;

import jmri.JmriException;
import jmri.jmris.JmriConnection;
import jmri.web.server.WebServerManager;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonClientHandler {

	private JsonLightServer lightServer;
	private JsonOperationsServer operationsServer;
	private JsonPowerServer powerServer;
	private JsonProgrammerServer programmerServer;
	private JsonReporterServer reporterServer;
	private JsonSensorServer sensorServer;
	private JsonSignalHeadServer signalHeadServer;
	private JsonThrottleServer throttleServer;
	private JsonTurnoutServer turnoutServer;
	private JmriConnection connection;
	private ObjectMapper mapper;
	private static Logger log = Logger.getLogger(JsonClientHandler.class);

	public JsonClientHandler(JmriConnection connection) {
		this.connection = connection;
		this.mapper = new ObjectMapper();
		this.lightServer = new JsonLightServer(this.connection);
		this.operationsServer = new JsonOperationsServer(this.connection);
		this.powerServer = new JsonPowerServer(this.connection);
		this.programmerServer = new JsonProgrammerServer(this.connection);
		this.reporterServer = new JsonReporterServer(this.connection);
		this.sensorServer = new JsonSensorServer(this.connection);
		this.signalHeadServer = new JsonSignalHeadServer(this.connection);
		this.throttleServer = new JsonThrottleServer(this.connection);
		this.turnoutServer = new JsonTurnoutServer(this.connection);
	}

	public void onClose() {
		this.throttleServer.onClose();
	}
	
	/**
	 * Process a JSON string and handle appropriately.
	 * 
	 * Currently JSON strings in four different forms are handled by this method:<ul>
	 * <li>list requests in the form: <code>{"type":"list","list":"trains"}</code>
	 * that are passed to the JsonLister for handling.</li>
	 * <li>individual item requests in the form: <code>{"type":"turnout",data:{"name":"LT14"}</code>
	 * that are passed to type-specific handlers.</li>
	 * <li>a heartbeat in the form <code>*</code> or <code>{"type":"ping"}</code>. The <code>*</code> heartbeat
	 * gets no response, while the JSON heartbeat causes a <code>{"type":"pong"}</code> response.</li>
	 * <li>a signoff in the form: <code>{"type":"goodbye"}</code> to which an identical response
	 * is sent before the connection gets closed.</li>
	 * 
	 * @param string
	 * @throws IOException
	 */
	public void onMessage(String string) throws IOException {
		if (log.isDebugEnabled()) {
			log.debug("Received from client: " + string);
		}
		// silently accept '*' as a single-character heartbeat without replying to client
		if (string.equals("*")) {
			return;
		}
		try {
			JsonNode root = this.mapper.readTree(string);
			String type = root.path("type").asText();
			JsonNode data = root.path("data");
			if (type.equals("ping")) {
				this.connection.sendMessage(this.mapper.writeValueAsString(this.mapper.createObjectNode().put("type", "pong")));
			} else if (type.equals("goodbye")) {
				this.connection.sendMessage(this.mapper.writeValueAsString(this.mapper.createObjectNode().put("type", "goodbye")));
				this.connection.close();
			} else if (type.equals("list")) {
				JsonNode reply = null;
				String list = root.path("list").asText();
				if (list.equals("lights")) {
					reply = JsonLister.getLights();
				} else if (list.equals("locations")) {
					reply = JsonLister.getLocations();
				} else if (list.equals("memories")) {
					reply = JsonLister.getMemories();
				} else if (list.equals("metadata")) {
					reply = JsonLister.getMetadata();
				} else if (list.equals("panels")) {
					reply = JsonLister.getPanels();
				} else if (list.equals("roster")) {
					reply = JsonLister.getRoster();
				} else if (list.equals("routes")) {
					reply = JsonLister.getRoutes();
				} else if (list.equals("sensors")) {
					reply = JsonLister.getSensors();
				} else if (list.equals("signalHeads")) {
					reply = JsonLister.getSignalHeads();
				} else if (list.equals("trains")) {
					reply = JsonLister.getTrains();
				} else if (list.equals("turnouts")) {
					reply = JsonLister.getTurnouts();
				} else {
					this.sendErrorMessage(0, "unknown list requested: [" + list + "]");
					return;
				}
				//if (log.isDebugEnabled()) log.debug("Sending to client: " + this.mapper.writeValueAsString(reply));
				this.connection.sendMessage(this.mapper.writeValueAsString(reply));
			} else if (!data.isMissingNode()) {
				if (type.equals("light")) {
					this.lightServer.parseRequest(data);
				} else if (type.equals(JsonOperationsServer.OPERATIONS)) {
					this.operationsServer.parseRequest(data);
				} else if (type.equals("power")) {
					this.powerServer.parseRequest(data);
				} else if (type.equals("programmer")) {
					this.programmerServer.parseRequest(data);
				} else if (type.equals("sensor")) {
					this.sensorServer.parseRequest(data);
				} else if (type.equals("signalHead")) {
					this.signalHeadServer.parseRequest(data);
				} else if (type.equals("reporter")) {
					this.reporterServer.parseRequest(data);
				} else if (type.equals("rosterEntry")) {
					this.connection.sendMessage(this.mapper.writeValueAsString(JsonLister.getRosterEntry(data.path("name").asText())));
				} else if (type.equals("throttle")) {
					this.throttleServer.parseRequest(data);
				} else if (type.equals("turnout")) {
					this.turnoutServer.parseRequest(data);
				} else {
					this.sendErrorMessage(0, "unknown type");
				}
			} else {
				this.sendErrorMessage(0, "expected message data");
			}
		} catch (JsonProcessingException pe) {
			log.warn("Exception processing \"" + string + "\"\n" + pe.getMessage());
			this.sendErrorMessage(0, "unable to process");
		} catch (JmriException je) {
			this.sendErrorMessage(0, "unsupported operation");
		}
	}

	public void sendHello(int heartbeat) throws IOException {
		ObjectNode root = this.mapper.createObjectNode();
		root.put("type", "hello");
		ObjectNode data = root.putObject("data");
		data.put("JMRI", jmri.Version.name());
		data.put("heartbeat", Math.round(heartbeat * 0.9f));
		data.put("railroad", WebServerManager.getWebServerPreferences().getRailRoadName());
		this.connection.sendMessage(this.mapper.writeValueAsString(root));
	}
	
	private void sendErrorMessage(int code, String message) throws IOException {
		ObjectNode root = this.mapper.createObjectNode();
		root.put("type", "error");
		ObjectNode data = root.putObject("error");
		data.put("code", code);
		data.put("message", message);
		this.connection.sendMessage(this.mapper.writeValueAsString(root));
	}

}
