package jmri.server.json.message;

import static jmri.server.json.message.JsonMessage.CLIENT;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.Locale;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.server.json.JSON;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
import jmri.server.json.JsonSocketService;

/**
 *
 * @author Randall Wood Copyright 2017
 */
public class JsonMessageSocketService extends JsonSocketService {

    private JsonMessageClientManager messageClientManager = null;

    public JsonMessageSocketService(JsonConnection connection) {
        super(connection);
        messageClientManager = InstanceManager.getDefault(JsonMessageClientManager.class);
    }

    @Override
    public void onMessage(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        switch (type) {
            case JSON.HELLO:
                if (!data.path(CLIENT).isMissingNode()) {
                    String client = data.path(CLIENT).asText();
                    if (!client.isEmpty()) {
                        subscribe(client);
                    }
                }
                break;
            case JsonMessage.CLIENT:
                switch (data.path(JSON.METHOD).asText()) {
                    case JSON.DELETE:
                        // remove client id
                        if (!data.path(CLIENT).isMissingNode()) {
                            String client = data.path(CLIENT).asText();
                            if (!client.isEmpty()) {
                               messageClientManager.unsubscribe(client);
                            }
                        }
                        break;
                    case JSON.GET:
                        // create id for client, register it, and inform client
                        String uuid = messageClientManager.getClient(this.connection);
                        if (uuid == null) {
                            uuid = UUID.randomUUID().toString();
                        }
                        subscribe(uuid);
                        ObjectNode root = this.connection.getObjectMapper().createObjectNode();
                        root.put(JSON.TYPE, JsonMessage.CLIENT);
                        ObjectNode payload = root.putObject(JSON.DATA);
                        payload.put(JsonMessage.CLIENT, uuid);
                        connection.sendMessage(root);
                        break;
                    case JSON.POST:
                    case JSON.PUT:
                    default:
                        // add client using client-provided id
                        if (!data.path(CLIENT).isMissingNode()) {
                            String client = data.path(CLIENT).asText();
                            if (!client.isEmpty()) {
                                subscribe(client);
                            }
                        }
                        break;
                }
            default:
                // ignore anything else
                break;
        }
    }

    @Override
    public void onList(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, Bundle.getMessage(locale, "UnlistableService", type));
    }

    @Override
    public void onClose() {
        messageClientManager.unsubscribe(this.connection);
    }

    private void subscribe(String client) throws JsonException {
        try {
            messageClientManager.subscribe(client, this.connection);
        } catch (IllegalArgumentException ex) {
            throw new JsonException(HttpServletResponse.SC_CONFLICT, Bundle.getMessage(this.connection.getLocale(), "ErrorClientConflict", CLIENT));
        }

    }
}
