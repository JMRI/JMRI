//JsonTurnoutServer.java
package jmri.jmris.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Locale;
import jmri.JmriException;
import jmri.jmris.AbstractTurnoutServer;
import jmri.jmris.JmriConnection;
import static jmri.jmris.json.JSON.METHOD;
import static jmri.jmris.json.JSON.NAME;
import static jmri.jmris.json.JSON.PUT;
import static jmri.jmris.json.JSON.TURNOUT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON Server interface between the JMRI turnout manager and a network
 * connection
 *
 * This server sends a message containing the turnout state whenever a turnout
 * that has been previously requested is open or thrown. When a client requests
 * or updates a turnout, the server replies with all known turnout details, but
 * only sends the new turnout state when sending a status update.
 *
 * @author Paul Bender Copyright (C) 2010
 * @author Randall Wood Copyright (C) 2012, 2013
 * @version $Revision: 21327 $
 */
public class JsonTurnoutServer extends AbstractTurnoutServer {

    private final JmriConnection connection;
    private final ObjectMapper mapper;
    static Logger log = LoggerFactory.getLogger(JsonTurnoutServer.class.getName());

    public JsonTurnoutServer(JmriConnection connection) {
        this.connection = connection;
        this.mapper = new ObjectMapper();
    }

    /*
     * Protocol Specific Abstract Functions
     */
    @Override
    public void sendStatus(String turnoutName, int status) throws IOException {
        try {
            this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.getTurnout(this.connection.getLocale(), turnoutName)));
        } catch (JsonException ex) {
            this.connection.sendMessage(this.mapper.writeValueAsString(ex.getJsonMessage()));
        }
    }

    @Override
    public void sendErrorStatus(String turnoutName) throws IOException {
        this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.handleError(500, Bundle.getMessage(this.connection.getLocale(), "ErrorObject", TURNOUT, turnoutName))));
    }

    @Override
    public void parseStatus(String statusString) throws JmriException, IOException {
        throw new JmriException("Overridden but unsupported method"); // NOI18N
    }

    public void parseRequest(Locale locale, JsonNode data) throws JmriException, IOException, JsonException {
        String name = data.path(NAME).asText();
        if (data.path(METHOD).asText().equals(PUT)) {
            JsonUtil.putTurnout(locale, name, data);
        } else {
            JsonUtil.setTurnout(locale, name, data);
        }
        this.connection.sendMessage(this.mapper.writeValueAsString(JsonUtil.getTurnout(locale, name)));
        this.addTurnoutToList(name);
    }
}
