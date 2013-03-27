//JsonLightServer.java
package jmri.jmris.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import jmri.JmriException;
import jmri.jmris.AbstractLightServer;
import jmri.jmris.JmriConnection;
import static jmri.jmris.json.JSON.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON Server interface between the JMRI light manager and a network connection
 *
 * This server sends a message containing the light state whenever a light
 * that has been previously requested is open or thrown. When a client requests
 * or updates a light, the server replies with all known light details, but
 * only sends the new light state when sending a status update.
 *
 * @author Paul Bender Copyright (C) 2010
 * @author Randall Wood Copyright (C) 2012, 2013
 * @version $Revision: 21313 $
 */
public class JsonLightServer extends AbstractLightServer {

    private JmriConnection connection = null;
    private ObjectMapper mapper = null;
    static Logger log = LoggerFactory.getLogger(JsonLightServer.class.getName());

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
        throw new JmriException("Overridden but unsupported method"); // NOI18N
    }

    public void parseRequest(JsonNode data) throws JmriException, IOException, JsonException {
        String name = data.path(NAME).asText();
        if (data.path(METHOD).asText().equals(PUT)) {
            JsonUtil.putLight(name, data);
        } else {
            JsonUtil.setLight(name, data);
        }
        this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.getLight(name)));
        this.addLightToList(name);
    }
}
