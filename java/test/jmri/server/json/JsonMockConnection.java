package jmri.server.json;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.DataOutputStream;
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
        this.message = message;
    }

    public JsonNode getMessage() {
        return this.message;
    }
}
