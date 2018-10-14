package jmri.jmris.json;

import static jmri.server.json.JSON.DATA;
import static jmri.server.json.JSON.MODE;
import static jmri.server.json.JSON.NODE_CV;
import static jmri.server.json.JSON.OP;
import static jmri.server.json.JSON.PROGRAMMER;
import static jmri.server.json.JSON.READ;
import static jmri.server.json.JSON.STATE;
import static jmri.server.json.JSON.TYPE;
import static jmri.server.json.JSON.VALUE;
import static jmri.server.json.JSON.WRITE;
import static jmri.server.json.JsonException.CODE;
import static jmri.server.json.JsonException.ERROR;
import static jmri.server.json.JsonException.MESSAGE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.Locale;
import jmri.JmriException;
import jmri.ProgListener;
import jmri.ProgrammingMode;
import jmri.jmris.AbstractProgrammerServer;
import jmri.jmris.JmriConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JMRI interface between the JMRI service mode programmer and a network
 * connection.
 *
 * @author Paul Bender Copyright (C) 2012
 * @author Randall Wood Copyright (C) 2014
 *
 */
public class JsonProgrammerServer extends AbstractProgrammerServer {

    private final JmriConnection connection;
    private final ObjectMapper mapper;
    private final static Logger log = LoggerFactory.getLogger(JsonProgrammerServer.class);

    public JsonProgrammerServer(JmriConnection connection) {
        super();
        this.connection = connection;
        this.mapper = new ObjectMapper();
    }

    /*
     * Protocol Specific Abstract Functions
     */
    @Override
    public void sendStatus(int CV, int value, int status) throws IOException {
        log.debug("sendStatus called for CV {} with value {} and status {}", CV, value, status);
        if (status == ProgListener.OK) {
            ObjectNode root = this.mapper.createObjectNode();
            root.put(TYPE, PROGRAMMER);
            ObjectNode data = root.putObject(DATA);
            data.put(NODE_CV, CV);
            data.put(VALUE, value);
            data.put(STATE, status);
            this.connection.sendMessage(this.mapper.writeValueAsString(root));
        } else {
            this.sendError(416, Bundle.getMessage(this.connection.getLocale(), "ErrorProgrammer416"));
        }
    }

    @Override
    public void sendNotAvailableStatus() throws IOException {
        this.sendError(499, Bundle.getMessage(this.connection.getLocale(), "ErrorProgrammer499"));
    }

    @Override
    public void parseRequest(String statusString) throws JmriException, IOException {
        this.parseRequest(Locale.getDefault(), this.mapper.readTree(statusString).path(DATA));
    }

    public void parseRequest(Locale locale, JsonNode data) throws JmriException, IOException {
        // get a programming mode, if possible
        ProgrammingMode mode = ProgrammingMode.REGISTERMODE;
        String requestMode = data.path(MODE).asText();
        for (ProgrammingMode check : getProgrammer().getSupportedModes()) {
            if (requestMode.equals(check.toString())) {
                mode = check;
            }
            if (requestMode.equals(check.getStandardName())) {
                mode = check;
            }
        }
        int CV = data.path(NODE_CV).asInt();
        int value = data.path(VALUE).asInt();
        if (WRITE.equals(data.path(OP).asText())) {
            this.writeCV(mode, CV, value);
        } else if (READ.equals(data.path(OP).asText())) {
            this.readCV(mode, CV);
        }
    }

    protected void sendError(int code, String message) throws IOException {
        ObjectNode root = this.mapper.createObjectNode();
        root.put(TYPE, ERROR);
        ObjectNode data = root.putObject(ERROR);
        data.put(TYPE, PROGRAMMER);
        data.put(CODE, code);
        data.put(MESSAGE, message);
        this.connection.sendMessage(this.mapper.writeValueAsString(root));
    }
}
