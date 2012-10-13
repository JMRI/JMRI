package jmri.jmris.json;

import java.io.IOException;
import java.util.HashMap;

import jmri.JmriException;
import jmri.jmris.JmriConnection;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class JsonThrottleServer {

	private ObjectMapper mapper;
	protected JmriConnection connection;
	private HashMap<String, JsonThrottle> throttles;
	static final Logger log = Logger.getLogger(JsonThrottleServer.class.getName());
	
	public JsonThrottleServer(JmriConnection connection) {
		this.connection = connection;
		this.mapper = new ObjectMapper();
		this.throttles = new HashMap<String, JsonThrottle>(0);
	}

	public void onClose() {
		for (String throttleId : this.throttles.keySet()) {
			this.throttles.get(throttleId).close();
			this.throttles.remove(throttleId);
		}
	}
	
	public void sendMessage(String throttleId, ObjectNode data) throws IOException {
		ObjectNode root = this.mapper.createObjectNode();
		root.put("type", "throttle");
		data.put("throttle", throttleId);
		root.put("data", data);
		this.connection.sendMessage(this.mapper.writeValueAsString(root));
	}

	public void sendErrorMessage(int code, String message) throws IOException {
		ObjectNode root = this.mapper.createObjectNode();
		root.put("type", "error");
		root.put("code", code);
		root.put("message", message);
		this.connection.sendMessage(this.mapper.writeValueAsString(root));
	}

	public void parseRequest(JsonNode data) throws JmriException, IOException {
		String id = data.path("throttle").asText();
		if (id == null) {
			this.sendErrorMessage(-1, "missing throttle ID");
			return;
		}
		JsonThrottle throttle = this.throttles.get(id);
		if (!this.throttles.containsKey(id)) {
			try {
				throttle = new JsonThrottle(id, data, this);
				this.throttles.put(id, throttle);
			} catch (JmriException je) {
				this.sendErrorMessage(-1, je.getMessage());
				return;
			}
		}
		throttle.parseRequest(data);
	}
}
