package jmri.server.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.DataOutputStream;
import java.io.IOException;
import jmri.jmris.JmriConnection;
import org.eclipse.jetty.websocket.api.Session;

/**
 *
 * @author Randall Wood
 */
public class JsonConnection extends JmriConnection {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public JsonConnection(Session connection) {
        super(connection);
    }

    public JsonConnection(DataOutputStream output) {
        super(output);
    }

    /**
     * Get the ObjectMapper for this connection.
     *
     * @return the ObjectMapper
     */
    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    /**
     * Send a JsonNode to the instantiated connection.
     *
     * This method throws an IOException so the server or servlet holding the
     * connection open can respond to the exception.
     *
     */
    public void sendMessage(JsonNode message) throws IOException {
        super.sendMessage(this.getObjectMapper().writeValueAsString(message));
    }
}
