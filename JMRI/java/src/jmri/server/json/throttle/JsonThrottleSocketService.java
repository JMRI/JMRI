package jmri.server.json.throttle;

import static jmri.server.json.JSON.DATA;
import static jmri.server.json.JSON.TYPE;
import static jmri.server.json.throttle.JsonThrottle.THROTTLE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.JmriException;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
import jmri.server.json.JsonSocketService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Randall Wood
 */
public class JsonThrottleSocketService extends JsonSocketService<JsonThrottleHttpService> {

    private final HashMap<String, JsonThrottle> throttles = new HashMap<>();
    private final HashMap<JsonThrottle, String> throttleIds = new HashMap<>();
    private final static Logger log = LoggerFactory.getLogger(JsonThrottleSocketService.class);

    public JsonThrottleSocketService(JsonConnection connection) {
        super(connection, new JsonThrottleHttpService(connection.getObjectMapper()));
    }

    @Override
    public void onMessage(String type, JsonNode data, String method, Locale locale) throws IOException, JmriException, JsonException {
        log.debug("Processing {}", data);
        String id = data.path(THROTTLE).asText();
        if (id.isEmpty()) {
            throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, Bundle.getMessage(locale, "ErrorThrottleId")); // NOI18N
        }
        JsonThrottle throttle = this.throttles.get(id);
        if (!this.throttles.containsKey(id)) {
            throttle = JsonThrottle.getThrottle(id, data, this);
            this.throttles.put(id, throttle);
            this.throttleIds.put(throttle, id);
            throttle.sendStatus(this);
        }
        throttle.onMessage(locale, data, this);
    }

    @Override
    public void onList(String type, JsonNode data, Locale locale) throws JsonException {
        throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, Bundle.getMessage(locale, "UnlistableService", type));
    }

    @Override
    public void onClose() {
        new HashSet<>(this.throttles.keySet()).stream().forEach((throttleId) -> {
            this.throttles.get(throttleId).close(this, false);
            this.throttles.remove(throttleId);
        });
        this.throttleIds.clear();
    }

    void release(JsonThrottle throttle) {
        throttle.release(this, true);
        this.throttles.remove(this.throttleIds.get(throttle));
        this.throttleIds.remove(throttle);
    }

    public void sendMessage(JsonThrottle throttle, ObjectNode data) throws IOException {
        String id = this.throttleIds.get(throttle);
        if (id != null) {
            ObjectNode root = this.connection.getObjectMapper().createObjectNode();
            root.put(TYPE, THROTTLE);
            data.put(THROTTLE, id);
            root.set(DATA, data);
            this.connection.sendMessage(root);
        }
    }

}
