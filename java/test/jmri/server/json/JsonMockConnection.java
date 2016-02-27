package jmri.server.json;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.DataOutputStream;
import java.io.IOException;
import org.eclipse.jetty.websocket.api.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JsonConnection that retains sent messages for unit testing.
 *
 * @author Randall Wood
 */
public class JsonMockConnection extends JsonConnection {

    private final static Logger log = LoggerFactory.getLogger(JsonMockConnection.class);
    private JsonNode message;

    public JsonMockConnection(Session connection) {
        super(connection);
    }

    public JsonMockConnection(DataOutputStream output) {
        super(output);
    }

    @Override
    public void sendMessage(JsonNode message) {
        try {
            log.error("Message: {}", this.getObjectMapper().writeValueAsString(message));
        } catch (IOException ex) {
            log.error("Error logging message", ex);
        }
        this.message = message;
    }

    public JsonNode getMessage() {
        return this.message;
    }
}
