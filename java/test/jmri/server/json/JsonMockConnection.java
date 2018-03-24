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

    public JsonMockConnection(Session connection) {
        super(connection);
        InstanceManager.getDefault(JsonServerPreferences.class).setValidateServerMessages(true);
    }

    public JsonMockConnection(DataOutputStream output) {
        super(output);
        InstanceManager.getDefault(JsonServerPreferences.class).setValidateServerMessages(true);
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation accepts a null message.
     */
    @Override
    public void sendMessage(@Nullable JsonNode message) {
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
}
