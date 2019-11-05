package jmri.server.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import jmri.InstanceManager;
import jmri.jmris.json.JsonServerPreferences;
import org.eclipse.jetty.websocket.api.Session;
import org.junit.Assert;

/**
 * JsonConnection that retains sent messages for unit testing.
 *
 * @author Randall Wood
 */
public class JsonMockConnection extends JsonConnection {

    private final List<JsonNode> messages = new ArrayList<>();
    private boolean open = true;
    private boolean throwIOException = false;

    /**
     * Create a connection; by default, this connection validates all messages
     * sent to it.
     *
     * @param connection the connection to use; can be null
     */
    public JsonMockConnection(Session connection) {
        super(connection);
        InstanceManager.getDefault(JsonServerPreferences.class).setValidateServerMessages(true);
    }

    /**
     * Create a connection; by default, this connection validates all messages
     * sent to it.
     *
     * @param output the output stream to use; can be null
     */
    public JsonMockConnection(DataOutputStream output) {
        super(output);
        InstanceManager.getDefault(JsonServerPreferences.class).setValidateServerMessages(true);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation accepts a null message to reset the list of sent
     * messages, and if {@link #isThrowIOException()} is true throws an
     * {@link IOException}. Note that after throwing the IOException,
     * {@link #isThrowIOException()} will return false.
     */
    @Override
    public void sendMessage(@CheckForNull JsonNode message, int id) throws IOException {
        if (this.throwIOException) {
            this.throwIOException = false;
            throw new IOException();
        }
        if (message != null) {
            if (this.preferences.getValidateServerMessages()) {
                try {
                    this.schemas.validateMessage(message, true, this.getLocale(), id);
                } catch (JsonException ex) {
                    this.messages.add(ex.getJsonMessage());
                    Assert.fail(ex.getMessage());
                }
            }
            this.messages.add(message);
        } else {
            // use a null message as the key to clear the list of messages
            this.messages.clear();
        }
    }

    @CheckForNull
    public JsonNode getMessage() {
        int i = this.messages.size() - 1;
        return (i < 0) ? null : this.messages.get(i);
    }

    /**
     * Get a copy of the list of all messages retained as a JSON array. This
     * returns a JSON array to facilitate JSON path inspection.
     *
     * @return a list of messages, empty array if no message has been sent
     */
    @Nonnull
    public ArrayNode getMessages() {
        return this.getObjectMapper().createArrayNode().addAll(this.messages);
    }

    @Override
    public void close() throws IOException {
        super.close();
        this.open = false;
    }

    public boolean isOpen() {
        return this.open;
    }

    /**
     * Set if next message sent to this connection should throw an
     * {@link IOException}.
     *
     * @param throwIOException true to throw on next message; false otherwise
     */
    public void setThrowIOException(boolean throwIOException) {
        this.throwIOException = throwIOException;
    }

    /**
     * Check if next message sent to this connection should throw an
     * {@link IOException}.
     *
     * @return true to throw on next message; false otherwise
     */
    public boolean isThrowIOException() {
        return this.throwIOException;
    }
}
