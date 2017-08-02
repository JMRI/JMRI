package jmri.jmris.json;

import static jmri.server.json.JSON.DATA;
import static jmri.server.json.JSON.METHOD;
import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.PUT;
import static jmri.server.json.JSON.TYPE;
import static jmri.server.json.reporter.JsonReporter.REPORT;
import static jmri.server.json.reporter.JsonReporter.REPORTER;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.Locale;
import jmri.JmriException;
import jmri.jmris.AbstractReporterServer;
import jmri.jmris.JmriConnection;
import jmri.server.json.JsonException;

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
 * @deprecated since 4.5.6
 */
@Deprecated
public class JsonReporterServer extends AbstractReporterServer {

    private final JmriConnection connection;
    private final ObjectMapper mapper;
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
        this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.handleError(500, Bundle.getMessage(this.connection.getLocale(), "ErrorObject", REPORTER, reporterName))));
    }

    @Override
    public void parseStatus(String statusString) throws JmriException, IOException {
        throw new JmriException("Overridden but unsupported method"); // NOI18N
    }

    public void parseRequest(Locale locale, JsonNode data) throws JmriException, IOException, JsonException {
        String name = data.path(NAME).asText();
        if (data.path(METHOD).asText().equals(PUT)) {
            JsonUtil.putReporter(locale, name, data);
        } else {
            JsonUtil.setReporter(locale, name, data);
        }
        this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.getReporter(locale, name)));
        this.addReporterToList(name);
    }
}
