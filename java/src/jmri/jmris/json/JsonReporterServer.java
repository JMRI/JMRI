//JsonReporterServer.java
package jmri.jmris.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import jmri.JmriException;
import jmri.jmris.AbstractReporterServer;
import jmri.jmris.JmriConnection;
import static jmri.jmris.json.JSON.DATA;
import static jmri.jmris.json.JSON.METHOD;
import static jmri.jmris.json.JSON.NAME;
import static jmri.jmris.json.JSON.PUT;
import static jmri.jmris.json.JSON.REPORT;
import static jmri.jmris.json.JSON.REPORTER;
import static jmri.jmris.json.JSON.TYPE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON Server interface between the JMRI reporter manager and a network
 * connection
 *
 * This server sends a message containing the reporter state whenever a reporter
 * that has been previously requested changes state. When a client requests or
 * updates a reporter, the server replies with all known reporter details, but
 * only sends the new reporter state when sending a status update.
 *
 * @author Paul Bender Copyright (C) 2011
 * @author Randall Wood Copyright (C) 2013
 * @version $Revision: 21313 $
 */
public class JsonReporterServer extends AbstractReporterServer {

    private final JmriConnection connection;
    private final ObjectMapper mapper;
    static Logger log = LoggerFactory.getLogger(JsonReporterServer.class);

    public JsonReporterServer(JmriConnection connection) {
        super();
        this.connection = connection;
        this.mapper = new ObjectMapper();
    }

    /*
     * Protocol Specific Abstract Functions
     */
    @Override
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

    @Override
    public void sendErrorStatus(String reporterName) throws IOException {
        this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.handleError(500, Bundle.getMessage("ErrorObject", REPORTER, reporterName))));
    }

    @Override
    public void parseStatus(String statusString) throws JmriException, IOException {
        throw new JmriException("Overridden but unsupported method"); // NOI18N
    }

    public void parseRequest(JsonNode data) throws JmriException, IOException, JsonException {
        String name = data.path(NAME).asText();
        if (data.path(METHOD).asText().equals(PUT)) {
            JsonUtil.putReporter(name, data);
        } else {
            JsonUtil.setReporter(name, data);
        }
        this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.getReporter(name)));
        this.addReporterToList(name);
    }
}
