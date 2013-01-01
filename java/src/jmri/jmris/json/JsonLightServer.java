//SimpleLightServer.java
package jmri.jmris.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import jmri.JmriException;
import jmri.Light;
import jmri.jmris.AbstractLightServer;
import jmri.jmris.JmriConnection;
import static jmri.jmris.json.JSON.*;
import org.apache.log4j.Logger;

/**
 * Simple Server interface between the JMRI light manager and a network
 * connection
 *
 * @author Paul Bender Copyright (C) 2010
 * @version $Revision: 21313 $
 */
public class JsonLightServer extends AbstractLightServer {

    private JmriConnection connection = null;
    private ObjectMapper mapper = null;
    static Logger log = Logger.getLogger(JsonLightServer.class.getName());

    public JsonLightServer(JmriConnection connection) {
        this.connection = connection;
        this.mapper = new ObjectMapper();
    }

    /*
     * Protocol Specific Abstract Functions
     */
    @Override
    public void sendStatus(String lightName, int status) throws IOException {
        ObjectNode root = this.mapper.createObjectNode();
        root.put(TYPE, LIGHT);
        ObjectNode data = root.putObject(DATA);
        data.put(NAME, lightName);
        data.put(STATE, status);
        this.connection.sendMessage(this.mapper.writeValueAsString(root));
    }

    @Override
    public void sendErrorStatus(String lightName) throws IOException {
        this.sendStatus(lightName, -1);
    }

    @Override
    public void parseStatus(String statusString) throws JmriException, IOException {
        this.parseRequest(this.mapper.readTree(statusString).path(DATA));
    }

    public void parseRequest(JsonNode data) throws JmriException, IOException {
        switch (data.path(STATE).asInt(Light.UNKNOWN)) {
            case Light.ON:
                this.lightOn(data.path(NAME).asText());
                break;
            case Light.OFF:
                this.lightOff(data.path(NAME).asText());
                break;
            case Light.UNKNOWN:
                break;
            default:
                this.sendErrorStatus(data.path(NAME).asText());
                break;
        }
    }
}
