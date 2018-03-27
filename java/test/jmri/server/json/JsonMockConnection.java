package jmri.server.json;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.DataOutputStream;
import java.io.IOException;
import javax.annotation.Nullable;
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

    private JsonNode message = null;
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
     * This implementation accepts a null message, and if
     * {@link #isThrowIOException()} is true throws an {@link IOException}. Note
     * that after throwing the IOException, {@link #isThrowIOException()} will
     * return false.
     */
    @Override
    public void sendMessage(@Nullable JsonNode message) throws IOException {
        if (this.throwIOException) {
            this.throwIOException = false;
            throw new IOException();
        }
        if (message != null && this.preferences.getValidateServerMessages()) {
            try {
                this.schemas.validateMessage(message, true, this.getLocale());
            } catch (JsonException ex) {
                this.message = ex.getJsonMessage();
                Assert.fail(ex.getMessage());
            }
        }
        this.message = message;
    }

    public JsonNode getMessage() {
        return this.message;
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
