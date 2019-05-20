package jmri.server.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.annotation.Nonnull;
import jmri.InstanceManager;
import jmri.jmris.JmriConnection;
import jmri.jmris.json.JsonServerPreferences;
import jmri.server.json.schema.JsonSchemaServiceCache;
import org.eclipse.jetty.websocket.api.Session;

/**
 *
 * @author Randall Wood
 */
public class JsonConnection extends JmriConnection {

    private final ObjectMapper objectMapper = new ObjectMapper();
    protected final JsonServerPreferences preferences = InstanceManager.getDefault(JsonServerPreferences.class);
    protected final JsonSchemaServiceCache schemas = InstanceManager.getDefault(JsonSchemaServiceCache.class);

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
    @Nonnull
    public ObjectMapper getObjectMapper() {
        return this.objectMapper;
    }

    /**
     * Send a JsonNode to the instantiated connection.
     * <p>
     * This method throws an IOException so the server or servlet holding the
     * connection open can respond to the exception.
     * <p>
     * If {@link JsonServerPreferences#getValidateServerMessages()} is
     * {@code true}, a message is sent to the client that validation failed
     * instead of the intended message.
     * <p>
     * Overriding methods must ensure that {@code message} is only sent if
     * validated.
     *
     * @param message the object or array to send as a message
     * @param id      the message id set by the client
     * @throws IOException if unable to send the message
     */
    public void sendMessage(@Nonnull JsonNode message, int id) throws IOException {
        if (this.preferences.getValidateServerMessages()) {
            try {
                this.schemas.validateMessage(message, true, this.getLocale(), id);
            } catch (JsonException ex) {
                super.sendMessage(this.getObjectMapper().writeValueAsString(ex.getJsonMessage()));
                return;
            }
        }
        super.sendMessage(this.getObjectMapper().writeValueAsString(message));
    }
}
