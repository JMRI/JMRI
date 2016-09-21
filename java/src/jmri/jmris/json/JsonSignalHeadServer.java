package jmri.jmris.json;

import static jmri.server.json.JSON.DATA;
import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.STATE;
import static jmri.server.json.JSON.TYPE;
import static jmri.server.json.JSON.UNKNOWN;
import static jmri.server.json.JsonException.CODE;
import static jmri.server.json.JsonException.ERROR;
import static jmri.server.json.JsonException.MESSAGE;
import static jmri.server.json.signalHead.JsonSignalHead.SIGNAL_HEAD;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmris.AbstractSignalHeadServer;
import jmri.jmris.JmriConnection;
import jmri.server.json.JsonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON Web Socket interface between the JMRI SignalHead manager and a network
 * connection
 *
 * @author Paul Bender Copyright (C) 2010
 */
public class JsonSignalHeadServer extends AbstractSignalHeadServer {

    private final JmriConnection connection;
    private final ObjectMapper mapper;
    private final static Logger log = LoggerFactory.getLogger(JsonSignalHeadServer.class.getName());

    public JsonSignalHeadServer(JmriConnection connection) {
        super();
        this.connection = connection;
        this.mapper = new ObjectMapper();
    }

    /*
     * Protocol Specific Abstract Functions
     */
    @Override
    public void sendStatus(String signalHeadName, int status) throws IOException {
        ObjectNode root = this.mapper.createObjectNode();
        root.put(TYPE, SIGNAL_HEAD);
        ObjectNode data = root.putObject(DATA);
        data.put(NAME, signalHeadName);
        data.put(STATE, status);
        this.connection.sendMessage(this.mapper.writeValueAsString(root));
    }

    @Override
    public void sendErrorStatus(String signalHeadName) throws IOException {
        ObjectNode root = this.mapper.createObjectNode();
        root.put(TYPE, ERROR);
        ObjectNode data = root.putObject(ERROR);
        data.put(NAME, signalHeadName);
        data.put(CODE, -1);
        data.put(MESSAGE, Bundle.getMessage("ErrorObject", SIGNAL_HEAD, signalHeadName));
        this.connection.sendMessage(this.mapper.writeValueAsString(root));
    }

    @Override
    public void parseStatus(String statusString) throws JmriException, IOException, JsonException {
        this.parseRequest(Locale.getDefault(), this.mapper.readTree(statusString).path(DATA));
    }

    public void parseRequest(Locale locale, JsonNode data) throws JmriException, IOException, JsonException {
        String name = data.path(NAME).asText();
        int state = data.path(STATE).asInt(UNKNOWN);
        if (state == UNKNOWN) {  //if unknown, retrieve current and respond
            try {   
                state = InstanceManager.getDefault(jmri.SignalHeadManager.class).getSignalHead(name).getAppearance();
            } catch (NullPointerException e) {
                log.error("Unable to get signalHead [{}].", name);
                throw new JsonException(404, Bundle.getMessage(locale, "ErrorObject", SIGNAL_HEAD, name));
            }
            this.sendStatus(name, state);
        } else { //else set the appearance to the state passed-in
            this.setSignalHeadAppearance(name, state);
        }
        this.addSignalHeadToList(name);
    }
}
