//JsonSignalHeadServer.java
package jmri.jmris.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.SignalHead;
import jmri.jmris.AbstractSignalHeadServer;
import jmri.jmris.JmriConnection;
import static jmri.jmris.json.JSON.*;
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

    private JmriConnection connection;
    private ObjectMapper mapper;
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
        this.parseRequest(this.mapper.readTree(statusString).path(DATA));
    }

    public void parseRequest(JsonNode data) throws JmriException, IOException {
        String name = data.path(NAME).asText();
        int state = data.path(STATE).asInt(SignalHead.UNKNOWN);
        if (state == SignalHead.UNKNOWN) {  //if unknown, retrieve current and respond
            state = InstanceManager.signalHeadManagerInstance().getSignalHead(name).getAppearance();
            this.sendStatus(name, state);
        } else { //else set the appearance to the state passed-in
            this.setSignalHeadAppearance(name, state);
        }
        this.addSignalHeadToList(name);
    }
}
