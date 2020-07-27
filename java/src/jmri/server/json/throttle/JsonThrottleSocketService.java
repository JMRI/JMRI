package jmri.server.json.throttle;

import static jmri.server.json.JSON.NAME;
import static jmri.server.json.throttle.JsonThrottle.THROTTLE;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import javax.servlet.http.HttpServletResponse;
import jmri.JmriException;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
import jmri.server.json.JsonRequest;
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
    private static final Logger log = LoggerFactory.getLogger(JsonThrottleSocketService.class);

    public JsonThrottleSocketService(JsonConnection connection) {
        super(connection, new JsonThrottleHttpService(connection.getObjectMapper()));
    }

    @Override
    public void onMessage(String type, JsonNode data, JsonRequest request) throws IOException, JmriException, JsonException {
        log.debug("Processing {}", data);
        String name = data.path(NAME).asText();
        if (name.isEmpty()) {
            name = data.path(THROTTLE).asText();
            log.warn("JSON throttle \"{}\" requested using \"throttle\" instead of \"name\"", name);
        }
        if (name.isEmpty()) {
            throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, Bundle.getMessage(request.locale, "ErrorThrottleId"), request.id); // NOI18N
        }
        JsonThrottle throttle = throttles.get(name);
        if (!throttles.containsKey(name)) {
            throttle = JsonThrottle.getThrottle(name, data, this, request.id);
            throttles.put(name, throttle);
            throttleIds.put(throttle, name);
            throttle.sendStatus(this);
        }
        throttle.onMessage(request.locale, data, this);
    }

    @Override
    public void onList(String type, JsonNode data, JsonRequest request) throws JsonException {
        throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, Bundle.getMessage(request.locale, "UnlistableService", type), request.id);
    }

    @Override
    public void onClose() {
        new HashSet<>(throttles.keySet()).stream().forEach(throttleId -> {
            throttles.get(throttleId).close(this, false);
            throttles.remove(throttleId);
        });
        throttleIds.clear();
    }

    void release(JsonThrottle throttle) {
        throttle.release(this, true);
        throttles.remove(throttleIds.get(throttle));
        throttleIds.remove(throttle);
    }

    public void sendMessage(JsonThrottle throttle, ObjectNode data) throws IOException {
        String id = throttleIds.get(throttle);
        if (id != null) {
            data.put(NAME, id);
            data.put(THROTTLE, id);
            connection.sendMessage(service.message(THROTTLE, data, 0), 0);
        }
    }

}
