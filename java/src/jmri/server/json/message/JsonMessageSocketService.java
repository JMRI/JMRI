package jmri.server.json.message;

import static jmri.server.json.message.JsonMessage.CLIENT;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.server.json.JSON;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
import jmri.server.json.JsonRequest;
import jmri.server.json.JsonSocketService;

/**
 *
 * @author Randall Wood Copyright 2017, 2019
 */
public class JsonMessageSocketService extends JsonSocketService<JsonMessageHttpService> {

    public JsonMessageSocketService(JsonConnection connection) {
        super(connection, new JsonMessageHttpService(connection.getObjectMapper()));
    }

    @Override
    public void onMessage(String type, JsonNode data, JsonRequest request)
            throws IOException, JmriException, JsonException {
        switch (type) {
            case JSON.HELLO:
                if (!data.path(CLIENT).isMissingNode()) {
                    String client = data.path(CLIENT).asText();
                    if (!client.isEmpty()) {
                        subscribe(client, request.id);
                    } else {
                        throw new JsonException(HttpServletResponse.SC_BAD_REQUEST,
                                Bundle.getMessage(request.locale, "ErrorEmptyAttribute", JsonMessage.CLIENT, type), request.id);
                    }
                }
                break;
            case JsonMessage.CLIENT:
                switch (request.method) {
                    case JSON.DELETE:
                        // remove client id
                        if (!data.path(CLIENT).isMissingNode()) {
                            String client = data.path(CLIENT).asText();
                            if (!client.isEmpty()) {
                                getManager().unsubscribe(client);
                            } else {
                                throw new JsonException(HttpServletResponse.SC_BAD_REQUEST,
                                        Bundle.getMessage(request.locale, "ErrorEmptyAttribute", JsonMessage.CLIENT, type), request.id);
                            }
                        } else {
                            throw new JsonException(HttpServletResponse.SC_BAD_REQUEST,
                                    Bundle.getMessage(request.locale, "ErrorMissingAttribute", JsonMessage.CLIENT, type), request.id);
                        }
                        break;
                    case JSON.GET:
                        // if client is not specified, and connection is not subscribing, create a client id for it
                        // if client is not specified, and connection is subscribing, return onList results
                        // if client is specified, and not empty, throw JsonException if client
                        //     is not for this connection, otherwise return client
                        // if client is specified, and empty, return first client for connection
                        if (!data.path(CLIENT).isMissingNode()) {
                            String client = data.path(CLIENT).asText();
                            if (!client.isEmpty()) {
                                if (getManager().getClients(connection).contains(client)) {
                                    connection.sendMessage(getClient(client, request), request.id);
                                } else {
                                    throw new JsonException(HttpServletResponse.SC_NOT_FOUND,
                                            Bundle.getMessage(request.locale, "MessageClientNotForThisConnection", client), request.id);
                                }
                            } else {
                                connection.sendMessage(getClient(getManager().getClient(connection), request), request.id);
                            }
                        } else {
                            if (getManager().getClients(connection).isEmpty()) {
                                String client = UUID.randomUUID().toString();
                                subscribe(client, request.id);
                                connection.sendMessage(getClient(client, request), request.id);
                            } else {
                                onList(type, data, request);
                            }
                        }
                        break;
                    case JSON.POST:
                    case JSON.PUT:
                    default:
                        // add client using client-provided id
                        if (!data.path(CLIENT).isMissingNode()) {
                            String client = data.path(CLIENT).asText();
                            if (!client.isEmpty()) {
                                subscribe(client, request.id);
                            } else {
                                throw new JsonException(HttpServletResponse.SC_BAD_REQUEST,
                                        Bundle.getMessage(request.locale, "ErrorEmptyAttribute", JsonMessage.CLIENT, type), request.id);
                            }
                        } else {
                            throw new JsonException(HttpServletResponse.SC_BAD_REQUEST,
                                    Bundle.getMessage(request.locale, "ErrorMissingAttribute", JsonMessage.CLIENT, type), request.id);
                        }
                        break;
                }
                break; // break inside gets to here, then this goes out
            default:
                // ignore anything else
                break;
        }
    }

    @Override
    public void onList(String type, JsonNode data, JsonRequest request) throws IOException, JmriException, JsonException {
        switch (type) {
            case JSON.HELLO:
                throw new JsonException(HttpServletResponse.SC_BAD_REQUEST,
                        Bundle.getMessage(request.locale, "UnlistableService", type), request.id);
            case JsonMessage.CLIENT:
                Set<String> clients = getManager().getClients(connection);
                if (clients.isEmpty()) {
                    throw new JsonException(HttpServletResponse.SC_NOT_FOUND,
                            Bundle.getMessage(request.locale, "NoMessageClientForThisConnection", type), request.id);
                }
                ArrayNode array = service.getObjectMapper().createArrayNode();
                for (String client : clients) {
                    array.add(getClient(client, request));
                }
                connection.sendMessage(service.message(array, request.id), request.id);
                break;
            default:
                // silently ignore
        }
    }

    @Override
    public void onClose() {
        getManager().unsubscribe(this.connection);
    }

    private JsonNode getClient(String client, JsonRequest request) throws JsonException {
        if (client == null) {
            throw new JsonException(HttpServletResponse.SC_NOT_FOUND,
                    Bundle.getMessage(request.locale, "NoMessageClientForThisConnection"), request.id);
        }
        ObjectNode root = this.connection.getObjectMapper().createObjectNode();
        root.put(JSON.TYPE, JsonMessage.CLIENT);
        ObjectNode data = root.putObject(JSON.DATA);
        data.put(JsonMessage.CLIENT, client);
        return root;
    }

    private void subscribe(String client, int id) throws JsonException {
        try {
            getManager().subscribe(client, this.connection);
        } catch (IllegalArgumentException ex) {
            throw new JsonException(HttpServletResponse.SC_CONFLICT,
                    Bundle.getMessage(this.connection.getLocale(), "ErrorClientConflict", CLIENT), id);
        }
    }

    private JsonMessageClientManager getManager() {
        return InstanceManager.getDefault(JsonMessageClientManager.class);
    }
}
