package jmri.server.json;

import static jmri.server.json.JSON.DATA;
import static jmri.server.json.JSON.GET;
import static jmri.server.json.JSON.GOODBYE;
import static jmri.server.json.JSON.HELLO;
import static jmri.server.json.JSON.ID;
import static jmri.server.json.JSON.LIST;
import static jmri.server.json.JSON.LOCALE;
import static jmri.server.json.JSON.METHOD;
import static jmri.server.json.JSON.PING;
import static jmri.server.json.JSON.TYPE;
import static jmri.server.json.JSON.VERSION;
import static jmri.server.json.JSON.VERSIONS;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.ServiceLoader;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.server.json.schema.JsonSchemaServiceCache;
import jmri.spi.JsonServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler for JSON messages from a TCP socket or WebSocket client.
 */
public class JsonClientHandler {

    /**
     * When used as a parameter to {@link #onMessage(java.lang.String)}, will
     * cause a {@value jmri.server.json.JSON#HELLO} message to be sent to the
     * client.
     */
    public static final String HELLO_MSG = "{\"" + TYPE + "\":\"" + HELLO + "\"}";
    private final JsonConnection connection;
    private final HashMap<String, HashSet<JsonSocketService<?>>> services = new HashMap<>();
    private final JsonServerPreferences preferences = InstanceManager.getDefault(JsonServerPreferences.class);
    private final JsonSchemaServiceCache schemas = InstanceManager.getDefault(JsonSchemaServiceCache.class);
    private static final Logger log = LoggerFactory.getLogger(JsonClientHandler.class);

    public JsonClientHandler(JsonConnection connection) {
        this.connection = connection;
        String version = connection.getVersion();
        try {
            setVersion(version, 0);
        } catch (JsonException e) {
            // this exception can normally be thrown by bad input
            // from a JSON client; however at this point it can only
            // be caused by a bad edit of JSON.java or JsonConnection.java, so
            // throwing an IllegalArgumentException as
            // a failure at this point can only be caused by
            // carelessly editing either of those classes
            log.error("Unable to create handler for version {}", version);
            throw new IllegalArgumentException();
        }
    }

    public void onClose() {
        services.values().forEach(set -> set.stream().forEach(JsonSocketService::onClose));
        services.clear();
    }

    /**
     * Process a JSON string and handle appropriately.
     * <p>
     * See {@link jmri.server.json} for expected JSON objects.
     *
     * @param string the message
     * @throws java.io.IOException if communications with the client is broken
     * @see #onMessage(JsonNode)
     */
    public void onMessage(String string) throws IOException {
        if (string.equals("{\"type\":\"ping\"}")) {
            // turn down the noise when debugging
            log.trace("Received from client: '{}'", string);
        } else {
            log.debug("Received from client: '{}'", string);
        }
        try {
            onMessage(connection.getObjectMapper().readTree(string));
        } catch (JsonProcessingException pe) {
            log.warn("Exception processing \"{}\"\n{}", string, pe.getMessage());
            sendErrorMessage(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    Bundle.getMessage(connection.getLocale(), "ErrorProcessingJSON", pe.getLocalizedMessage()), 0);
        }
    }

    /**
     * Process a JSON node and handle appropriately.
     * <p>
     * See {@link jmri.server.json} for expected JSON objects.
     *
     * @param root the JSON node.
     * @throws java.io.IOException if communications with the client is broken
     * @see #onMessage(java.lang.String)
     */
    public void onMessage(JsonNode root) throws IOException {
        String method = root.path(METHOD).asText(GET);
        String type = root.path(TYPE).asText();
        int id = root.path(ID).asInt(0);
        JsonNode data = root.path(DATA);
        JsonRequest request = new JsonRequest(connection.getLocale(), connection.getVersion(), method, id);
        try {
            if (preferences.getValidateClientMessages()) {
                schemas.validateMessage(root, false, request);
            }
            if ((root.path(TYPE).isMissingNode() || type.equals(LIST)) && root.path(LIST).isValueNode()) {
                type = root.path(LIST).asText();
                method = LIST;
            }
            if (data.isMissingNode()) {
                if ((type.equals(HELLO) || type.equals(PING) || type.equals(GOODBYE)) ||
                        (method.equals(LIST) || method.equals(GET))) {
                    // these messages are not required to have a data payload,
                    // so create one if the message did not contain one to avoid
                    // special casing later
                    data = connection.getObjectMapper().createObjectNode();
                } else {
                    sendErrorMessage(HttpServletResponse.SC_BAD_REQUEST,
                            Bundle.getMessage(connection.getLocale(), "ErrorMissingData"), id);
                    return;
                }
            }
            // method not explicitly set in root, but set in data
            if (root.path(METHOD).isMissingNode() && data.path(METHOD).isValueNode()) {
                // at one point, we used method within data, so check there also
                method = data.path(METHOD).asText(JSON.GET);
            }
            if (type.equals(PING)) { // turn down the noise a bit
                log.trace("Processing '{}' with '{}'", type, data);
            } else {
                log.debug("Processing '{}' with '{}'", type, data);
            }
            if (method.equals(LIST)) {
                if (services.get(type) != null) {
                    for (JsonSocketService<?> service : services.get(type)) {
                        service.onList(type, data, request);
                    }
                } else {
                    log.warn("Requested list type '{}' unknown.", type);
                    sendErrorMessage(HttpServletResponse.SC_NOT_FOUND,
                            Bundle.getMessage(connection.getLocale(), JsonException.ERROR_UNKNOWN_TYPE, type), id);
                }
                return;
            } else {
                if (type.equals(HELLO) || type.equals(LOCALE) && !data.path(LOCALE).isMissingNode()) {
                    connection.setLocale(
                            Locale.forLanguageTag(data.path(LOCALE).asText(connection.getLocale().getLanguage())));
                    setVersion(data.path(VERSION).asText(connection.getVersion()), id);
                    // since locale or version may have changed, ensure any
                    // response is using new version and locale
                    request = new JsonRequest(connection.getLocale(), connection.getVersion(), method, id);
                }
                if (services.get(type) != null) {
                    for (JsonSocketService<?> service : services.get(type)) {
                        service.onMessage(type, data, request);
                    }
                } else {
                    log.warn("Requested type '{}' unknown.", type);
                    sendErrorMessage(HttpServletResponse.SC_NOT_FOUND,
                            Bundle.getMessage(connection.getLocale(), JsonException.ERROR_UNKNOWN_TYPE, type), id);
                }
            }
            if (type.equals(GOODBYE)) {
                // close the connection if GOODBYE is received.
                connection.close();
            }
        } catch (JmriException je) {
            log.warn("Unsupported operation attempted {}", root);
            sendErrorMessage(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(
                    connection.getLocale(), "ErrorUnsupportedOperation", je.getLocalizedMessage()), id);
        } catch (JsonException je) {
            sendErrorMessage(je);
        }
    }

    private void sendErrorMessage(int code, String message, int id) throws IOException {
        JsonException ex = new JsonException(code, message, id);
        sendErrorMessage(ex);
    }

    private void sendErrorMessage(JsonException ex) throws IOException {
        connection.sendMessage(ex.getJsonMessage(), ex.getId());
    }

    private void setVersion(@Nonnull String version, int id) throws JsonException {
        if (VERSIONS.stream().noneMatch(v -> v.equals(version))) {
            throw new JsonException(HttpServletResponse.SC_NOT_FOUND,
                    Bundle.getMessage(connection.getLocale(), "ErrorUnknownType", version), id);
        }
        connection.setVersion(version);
        onClose(); // dispose of any existing objects
        ServiceLoader.load(JsonServiceFactory.class)
                .forEach(factory -> {
                    JsonSocketService<?> service = factory.getSocketService(connection, version);
                    Arrays.stream(factory.getTypes(version)).forEach(type -> {
                        HashSet<JsonSocketService<?>> set = services.get(type);
                        if (set == null) {
                            services.put(type, new HashSet<>());
                            set = services.get(type);
                        }
                        set.add(service);
                    });
                    Arrays.stream(factory.getReceivedTypes(version)).forEach(type -> {
                        HashSet<JsonSocketService<?>> set = services.get(type);
                        if (set == null) {
                            services.put(type, new HashSet<>());
                            set = services.get(type);
                        }
                        set.add(service);
                    });
                });
    }

    protected HashMap<String, HashSet<JsonSocketService<?>>> getServices() {
        return new HashMap<>(services);
    }
}
