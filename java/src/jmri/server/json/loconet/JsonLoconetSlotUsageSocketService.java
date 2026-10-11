package jmri.server.json.loconet;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import jmri.JmriException;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
import jmri.server.json.JsonRequest;
import jmri.server.json.JsonSocketService;

/**
 * Minimal WebSocket handling for "loconetSlotUsage" -- a one-shot query,
 * not something a client subscribes to, so every message (GET or LIST)
 * is answered the same way as the HTTP GET, with no held subscription
 * state to release on close.
 *
 * A real, minimal implementation is used here rather than a null socket
 * service, since JsonClientHandler registers whatever getSocketService()
 * returns into every type's dispatch set unconditionally -- a null entry
 * there would NPE the first time a WebSocket client sent this type.
 *
 * @author Andrew Deak Copyright (C) 2026
 */
public class JsonLoconetSlotUsageSocketService extends JsonSocketService<JsonLoconetSlotUsageHttpService> {

    public JsonLoconetSlotUsageSocketService(JsonConnection connection) {
        super(connection, new JsonLoconetSlotUsageHttpService(connection.getObjectMapper()));
    }

    @Override
    public void onMessage(String type, JsonNode data, JsonRequest request) throws IOException, JmriException, JsonException {
        connection.sendMessage(service.doGet(type, "", data, request), request.id);
    }

    @Override
    public void onList(String type, JsonNode data, JsonRequest request) throws IOException, JmriException, JsonException {
        connection.sendMessage(service.doGet(type, "", data, request), request.id);
    }

    @Override
    public void onClose() {
        // no subscription state held
    }
}
