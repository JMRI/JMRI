//SimpleReporterServer.java
package jmri.jmris.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import jmri.JmriException;
import jmri.Reporter;
import jmri.jmris.AbstractReporterServer;
import jmri.jmris.JmriConnection;
import static jmri.jmris.json.JSON.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple Server interface between the JMRI reporter manager and a network
 * connection
 *
 * @author Paul Bender Copyright (C) 2011
 * @version $Revision: 21313 $
 */
public class JsonReporterServer extends AbstractReporterServer {

    private JmriConnection connection;
    private ObjectMapper mapper;
    static Logger log = LoggerFactory.getLogger(JsonReporterServer.class);

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
        root.put(TYPE, REPORTER);
        ObjectNode data = root.putObject(DATA);
        data.put(NAME, reporterName);
        if (r != null) {
            data.put(REPORT, r.toString());
        } else {
            data.putNull(REPORT);
        }
        this.connection.sendMessage(this.mapper.writeValueAsString(root));
    }

    public void sendErrorStatus(String reporterName) throws IOException {
        ObjectNode root = this.mapper.createObjectNode();
        root.put(TYPE, ERROR);
        ObjectNode data = root.putObject(ERROR);
        data.put(NAME, reporterName);
        data.put(CODE, -1);
        data.put(MESSAGE, Bundle.getMessage("ErrorObject", REPORTER, reporterName));
        this.connection.sendMessage(this.mapper.writeValueAsString(root));
    }

    public void parseStatus(String statusString) throws JmriException, IOException {
        this.parseRequest(this.mapper.readTree(statusString).path(DATA));
    }

    public void parseRequest(JsonNode data) throws JmriException, IOException {
        this.setReporterReport(data.path(NAME).asText(), data.path(REPORT).asText());
        Reporter reporter = jmri.InstanceManager.reporterManagerInstance().provideReporter(data.path(NAME).asText());
        this.addReporterToList(reporter.getSystemName());
        this.sendReport(reporter.getSystemName(), reporter.getCurrentReport());
    }
}
