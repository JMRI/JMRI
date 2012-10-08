//JmriSRCPProgrammerServer.java

package jmri.jmris.json;

import java.io.*;

import org.apache.log4j.Logger;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jmri.JmriException;
import jmri.ProgListener;
import jmri.Programmer;

import jmri.jmris.AbstractProgrammerServer;
import jmri.jmris.JmriConnection;

/**
 * SRCP interface between the JMRI service mode programmer and a
 * network connection
 * @author          Paul Bender Copyright (C) 2012
 * @version         $Revision: 21286 $
 */

public class JsonProgrammerServer extends AbstractProgrammerServer {

	private JmriConnection connection;
	private ObjectMapper mapper;
	static Logger log = Logger.getLogger(JsonProgrammerServer.class.getName());

	public JsonProgrammerServer(JmriConnection connection) {
		super();
		this.connection = connection;
		this.mapper = new ObjectMapper();
	}

	/*
	 * Protocol Specific Abstract Functions
	 */
	public void sendStatus(int CV, int value, int status) throws IOException {
		if(log.isDebugEnabled()) log.debug("sendStatus called for CV " +CV + 
				" with value " + value + " and status " + status );
		if (status == ProgListener.OK) {
			ObjectNode root = this.mapper.createObjectNode();
			root.put("type", "programmer");
			ObjectNode data = root.putObject("data");
			data.put("CV", CV);
			data.put("value", value);
			data.put("state", status);
			this.connection.sendMessage(this.mapper.writeValueAsString(root));
		} else {
			this.sendError(416, "no data");
		}
	}

	public void sendNotAvailableStatus() throws IOException {
		this.sendError(499, "unspecified error");
	}

	public void parseRequest(String statusString) throws JmriException, IOException {
		this.parseRequest(this.mapper.readTree(statusString).path("data"));
	}
	
	public void parseRequest(JsonNode data) throws JmriException, IOException {
		int mode = data.path("mode").asInt(Programmer.REGISTERMODE);
		int CV = data.path("CV").asInt();
		int value = data.path("value").asInt();
		if (data.path("op").asText() == "write") {
			this.writeCV(mode, CV, value);
		} else if (data.path("op").asText() == "read") {
			this.readCV(mode, CV);
		}
	}

	protected void sendError(int code, String message) throws IOException {
		ObjectNode root = this.mapper.createObjectNode();
		root.put("type", "error");
		ObjectNode data = root.putObject("error");
		data.put("type", "programmer");
		data.put("code", code);
		data.put("message", message);
		this.connection.sendMessage(this.mapper.writeValueAsString(root));
	}
}
