package jmri.server.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.annotation.Nonnull;
import jmri.InstanceManager;
import jmri.jmris.JmriConnection;
import jmri.server.json.schema.JsonSchemaServiceCache;
import org.eclipse.jetty.websocket.api.Session;

/**
 * Abstraction of DataOutputStream and WebSocket.Connection classes for JSON
 * clients.
 *
 * @author Randall Wood Copyright 2019
 */
public class JsonConnection extends JmriConnection {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private String version = JSON.V5;
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
        return objectMapper;
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
     * @param request the JSON request
     * @throws IOException if unable to send the message
     */
    public void sendMessage(@Nonnull JsonNode message, @Nonnull JsonRequest request) throws IOException {
        if (preferences.getValidateServerMessages()) {
            try {
                schemas.validateMessage(message, true, request);
            } catch (JsonException ex) {
                super.sendMessage(getObjectMapper().writeValueAsString(ex.getJsonMessage()));
                return;
            }
        }
        super.sendMessage(getObjectMapper().writeValueAsString(message));
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
        sendMessage(message, new JsonRequest(getLocale(), getVersion(), JSON.GET, id));
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

}
