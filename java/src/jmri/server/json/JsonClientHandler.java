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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.ServiceLoader;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.jmris.json.JsonServerPreferences;
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
        ServiceLoader.load(JsonServiceFactory.class).forEach(factory -> {
            JsonSocketService<?> service = factory.getSocketService(connection);
            for (String type : factory.getTypes()) {
                HashSet<JsonSocketService<?>> set = this.services.get(type);
                if (set == null) {
                    this.services.put(type, new HashSet<>());
                    set = this.services.get(type);
                }
                set.add(service);
            }
            for (String type : factory.getReceivedTypes()) {
                HashSet<JsonSocketService<?>> set = this.services.get(type);
                if (set == null) {
                    this.services.put(type, new HashSet<>());
                    set = this.services.get(type);
                }
                set.add(service);
            }
        });
    }

    public void onClose() {
        services.values().stream()
                .forEach(set -> set.stream().forEach(JsonSocketService::onClose));
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
            this.onMessage(this.connection.getObjectMapper().readTree(string));
        } catch (JsonProcessingException pe) {
            log.warn("Exception processing \"{}\"\n{}", string, pe.getMessage());
            this.sendErrorMessage(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    Bundle.getMessage(this.connection.getLocale(), "ErrorProcessingJSON", pe.getLocalizedMessage()), 0);
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
        try {
            if (preferences.getValidateClientMessages()) {
                this.schemas.validateMessage(root, false, this.connection.getLocale(), id);
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
                    data = this.connection.getObjectMapper().createObjectNode();
                } else {
                    this.sendErrorMessage(HttpServletResponse.SC_BAD_REQUEST,
                            Bundle.getMessage(this.connection.getLocale(), "ErrorMissingData"), id);
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
                if (this.services.get(type) != null) {
                    for (JsonSocketService<?> service : this.services.get(type)) {
                        service.onList(type, data, this.connection.getLocale(), id);
                    }
                    return;
                } else {
                    log.warn("Requested list type '{}' unknown.", type);
                    this.sendErrorMessage(HttpServletResponse.SC_NOT_FOUND,
                            Bundle.getMessage(this.connection.getLocale(), JsonException.ERROR_UNKNOWN_TYPE, type), id);
                    return;
                }
            } else if (!data.isMissingNode()) {
                if (type.equals(HELLO) || type.equals(LOCALE) && !data.path(LOCALE).isMissingNode()) {
                    String locale = data.path(LOCALE).asText();
                    if (!locale.isEmpty()) {
                        this.connection.setLocale(Locale.forLanguageTag(locale));
                    }
                }
                if (this.services.get(type) != null) {
                    for (JsonSocketService<?> service : this.services.get(type)) {
                        service.onMessage(type, data, method, this.connection.getLocale(), id);
                    }
                } else {
                    log.warn("Requested type '{}' unknown.", type);
                    this.sendErrorMessage(HttpServletResponse.SC_NOT_FOUND,
                            Bundle.getMessage(this.connection.getLocale(), JsonException.ERROR_UNKNOWN_TYPE, type), id);
                }
            } else {
                log.warn("Data property of JSON message missing");
                this.sendErrorMessage(HttpServletResponse.SC_BAD_REQUEST,
                        Bundle.getMessage(this.connection.getLocale(), "ErrorMissingData"), id);
            }
            if (type.equals(GOODBYE)) {
                // close the connection if GOODBYE is received.
                this.connection.close();
            }
        } catch (JmriException je) {
            log.warn("Unsupported operation attempted {}", root);
            this.sendErrorMessage(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(
                    this.connection.getLocale(), "ErrorUnsupportedOperation", je.getLocalizedMessage()), id);
        } catch (JsonException je) {
            this.sendErrorMessage(je);
        }
    }

    private void sendErrorMessage(int code, String message, int id) throws IOException {
        JsonException ex = new JsonException(code, message, id);
        this.sendErrorMessage(ex);
    }

    private void sendErrorMessage(JsonException ex) throws IOException {
        this.connection.sendMessage(ex.getJsonMessage(), ex.getId());
    }

    protected HashMap<String, HashSet<JsonSocketService<?>>> getServices() {
        return new HashMap<>(this.services);
    }
}
