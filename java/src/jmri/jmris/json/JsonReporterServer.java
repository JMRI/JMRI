//SimpleReporterServer.java

package jmri.jmris.json;

import java.io.IOException;

import jmri.JmriException;
import jmri.Reporter;
import jmri.jmris.AbstractReporterServer;
import jmri.jmris.JmriConnection;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Simple Server interface between the JMRI reporter manager and a
 * network connection
 * @author          Paul Bender Copyright (C) 2011
 * @version         $Revision: 21313 $
 */

public class JsonReporterServer extends AbstractReporterServer {

	private JmriConnection connection;
	private ObjectMapper mapper;
	static Logger log = Logger.getLogger(JsonReporterServer.class.getName());

	public JsonReporterServer(JmriConnection connection) {
		super();
		this.connection = connection;
		this.mapper = new ObjectMapper();
	}

	/*
	 * Protocol Specific Abstract Functions
	 */
	public void sendReport(String reporterName, Object r) throws IOException {
		ObjectNode root = this.mapper.createObjectNode();
		root.put("type", "reporter");
		ObjectNode data = root.putObject("data");
		data.put("name", reporterName);
		if (r != null) {
			data.put("report", r.toString());		
		} else {
			data.putNull("report");
		}
		this.connection.sendMessage(this.mapper.writeValueAsString(root));
	}

	public void sendErrorStatus(String reporterName) throws IOException {
		ObjectNode root = this.mapper.createObjectNode();
		root.put("type", "error");
		ObjectNode data = root.putObject("error");
		data.put("name", reporterName);
		data.put("code", -1);
		data.put("message", "Error handling reporter.");
		this.connection.sendMessage(this.mapper.writeValueAsString(root));
	}

	public void parseStatus(String statusString) throws JmriException, IOException {
		this.parseRequest(this.mapper.readTree(statusString).path("data"));
	}
	
	public void parseRequest(JsonNode data) throws JmriException, IOException {
		this.setReporterReport(data.path("name").asText(), data.path("report").asText());
		Reporter reporter = jmri.InstanceManager.reporterManagerInstance().provideReporter(data.path("name").asText());
		this.addReporterToList(reporter.getSystemName());
		this.sendReport(reporter.getSystemName(), reporter.getCurrentReport());
	}

}
