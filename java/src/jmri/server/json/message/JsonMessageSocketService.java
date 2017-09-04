package jmri.server.json.message;

import static jmri.server.json.message.JsonMessage.CLIENT;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Locale;
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

    public JsonMessageSocketService(JsonConnection connection) {
        super(connection);
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
                switch (data.path(JSON.METHOD).asText(JSON.GET)) {
                    case JSON.DELETE:
                        if (!data.path(CLIENT).isMissingNode()) {
                            String client = data.path(CLIENT).asText();
                            if (!client.isEmpty()) {
                                InstanceManager.getDefault(JsonMessageClientManager.class).unsubscribe(client);
                            }
                        }
                        break;
                    case JSON.POST:
                    case JSON.PUT:
                        if (!data.path(CLIENT).isMissingNode()) {
                            String client = data.path(CLIENT).asText();
                            if (!client.isEmpty()) {
                                subscribe(client);
                            }
                        }
                        break;
                    default:
                        // ignore gets for client under assumption that client must retain its client key(s)
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
        InstanceManager.getDefault(JsonMessageClientManager.class).unsubscribe(this.connection);
    }

    private void subscribe(String client) throws JsonException {
        try {
            InstanceManager.getDefault(JsonMessageClientManager.class).subscribe(client, this.connection);
        } catch (IllegalArgumentException ex) {
            throw new JsonException(HttpServletResponse.SC_CONFLICT, Bundle.getMessage(this.connection.getLocale(), "ErrorClientConflict", CLIENT));
        }

    }
}
