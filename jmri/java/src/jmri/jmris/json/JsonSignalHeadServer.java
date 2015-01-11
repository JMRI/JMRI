//JsonSignalHeadServer.java
package jmri.jmris.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmris.AbstractSignalHeadServer;
import jmri.jmris.JmriConnection;
import static jmri.jmris.json.JSON.CODE;
import static jmri.jmris.json.JSON.DATA;
import static jmri.jmris.json.JSON.ERROR;
import static jmri.jmris.json.JSON.MESSAGE;
import static jmri.jmris.json.JSON.NAME;
import static jmri.jmris.json.JSON.SIGNAL_HEAD;
import static jmri.jmris.json.JSON.STATE;
import static jmri.jmris.json.JSON.TYPE;
import static jmri.jmris.json.JSON.UNKNOWN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON Web Socket interface between the JMRI SignalHead manager and a network
 * connection
 *
 * @author Paul Bender Copyright (C) 2010
 * @version $Revision: 21313 $
 */
public class JsonSignalHeadServer extends AbstractSignalHeadServer {

    private final JmriConnection connection;
    private final ObjectMapper mapper;
    static Logger log = LoggerFactory.getLogger(JsonSignalHeadServer.class.getName());

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
    public void parseStatus(String statusString) throws JmriException, IOException {
        this.parseRequest(Locale.getDefault(),this.mapper.readTree(statusString).path(DATA));
    }

    public void parseRequest(Locale locale, JsonNode data) throws JmriException, IOException {
        String name = data.path(NAME).asText();
        int state = data.path(STATE).asInt(UNKNOWN);
        if (state == UNKNOWN) {  //if unknown, retrieve current and respond
            state = InstanceManager.signalHeadManagerInstance().getSignalHead(name).getAppearance();
            this.sendStatus(name, state);
        } else { //else set the appearance to the state passed-in
            this.setSignalHeadAppearance(name, state);
        }
        this.addSignalHeadToList(name);
    }
}
