package jmri.jmris.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import jmri.JmriException;
import jmri.jmris.JmriConnection;
import static jmri.jmris.json.JSON.CODE;
import static jmri.jmris.json.JSON.DATA;
import static jmri.jmris.json.JSON.ERROR;
import static jmri.jmris.json.JSON.MESSAGE;
import static jmri.jmris.json.JSON.THROTTLE;
import static jmri.jmris.json.JSON.TYPE;

/**
 * 
 * @author Randall Wood
 * @deprecated since 4.3.4
 */
@Deprecated
public class JsonThrottleServer {

    private final ObjectMapper mapper;
    protected final JmriConnection connection;
    private final HashMap<String, JsonThrottle> throttles;
    private final HashMap<JsonThrottle, String> throttleIds;
    public JsonThrottleServer(JmriConnection connection) {
        this.connection = connection;
        this.mapper = new ObjectMapper();
        this.throttles = new HashMap<String, JsonThrottle>(0);
        this.throttleIds = new HashMap<JsonThrottle, String>(0);
    }

    public void dispose() {
        for (String throttleId : this.throttles.keySet()) {
            this.throttles.get(throttleId).close(this, false);
            this.throttles.remove(throttleId);
        }
        this.throttleIds.clear();
    }

    protected void release(JsonThrottle throttle) {
        throttle.release(this);
        this.throttles.remove(this.throttleIds.get(throttle));
        this.throttleIds.remove(throttle);
    }

    public void sendMessage(String throttleId, ObjectNode data) throws IOException {
        if (throttleId != null) {
            ObjectNode root = this.mapper.createObjectNode();
            root.put(TYPE, THROTTLE);
            data.put(THROTTLE, throttleId);
            root.put(DATA, data);
            this.connection.sendMessage(this.mapper.writeValueAsString(root));
        }
    }

    public void sendMessage(JsonThrottle throttle, ObjectNode data) throws IOException {
        this.sendMessage(this.throttleIds.get(throttle), data);
    }

    public void sendErrorMessage(int code, String message) throws IOException {
        ObjectNode root = this.mapper.createObjectNode();
        root.put(TYPE, ERROR);
        ObjectNode data = root.putObject(DATA);
        data.put(CODE, code);
        data.put(MESSAGE, message);
        this.connection.sendMessage(this.mapper.writeValueAsString(root));
    }

    public void parseRequest(Locale locale, JsonNode data) throws JmriException, IOException {
        String id = data.path(THROTTLE).asText();
        if ("".equals(id)) {
            this.sendErrorMessage(-1, Bundle.getMessage(locale, "ErrorThrottleId"));
            return;
        }
        JsonThrottle throttle = this.throttles.get(id);
        if (!this.throttles.containsKey(id)) {
            try {
                throttle = JsonThrottle.getThrottle(id, data, this);
                this.throttles.put(id, throttle);
                this.throttleIds.put(throttle, id);
                throttle.sendStatus(this);
            } catch (JmriException je) {
                this.sendErrorMessage(-1, je.getMessage());
                return;
            }
        }
        throttle.parseRequest(locale, data, this);
    }
}
