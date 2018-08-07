package jmri.server.json;

import static jmri.server.json.JSON.DATA;
import static jmri.server.json.JSON.GET;
import static jmri.server.json.JSON.GOODBYE;
import static jmri.server.json.JSON.HELLO;
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
import jmri.JmriException;
import jmri.spi.JsonServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JsonClientHandler {

    /**
     * When used as a parameter to {@link #onMessage(java.lang.String)}, will
     * cause a {@value jmri.server.json.JSON#HELLO} message to be sent to the
     * client.
     */
    public static final String HELLO_MSG = "{\"" + JSON.TYPE + "\":\"" + JSON.HELLO + "\"}";
    private final JsonConnection connection;
    private final HashMap<String, HashSet<JsonSocketService<?>>> services = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(JsonClientHandler.class);

    public JsonClientHandler(JsonConnection connection) {
        this.connection = connection;
        ServiceLoader.load(JsonServiceFactory.class).forEach((factory) -> {
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
        services.values().stream().forEach((set) -> {
            set.stream().forEach((service) -> {
                service.onClose();
            });
        });
        services.clear();
    }

    /**
     * Process a JSON string and handle appropriately.
     * <p>
     * Currently JSON strings in four different forms are handled by this
     * method:<ul> <li>list requests in the form:
     * <code>{"type":"list","list":"trains"}</code> or
     * <code>{"list":"trains"}</code> that are passed to the JsonUtil for
     * handling.</li> <li>individual item state requests in the form:
     * <code>{"type":"turnout","data":{"name":"LT14"}}</code> that are passed to
     * type-specific handlers. In addition to the initial response, these
     * requests will initiate "listeners", which will send updated responses
     * every time the item's state changes.<ul>
     * <li>an item's state can be set by adding a <strong>state</strong> node to
     * the <em>data</em> node:
     * <code>{"type":"turnout","data":{"name":"LT14","state":4}}</code>
     * <li>individual types can be created if a <strong>method</strong> node
     * with the value <em>put</em> is included in message:
     * <code>{"type":"turnout","method":"put","data":{"name":"LT14"}}</code>.
     * The <em>method</em> node may be included in the <em>data</em> node:
     * <code>{"type":"turnout","data":{"name":"LT14","method":"put"}}</code>
     * Note that not all types support this.</li></ul>
     * </li><li>a heartbeat in the form <code>{"type":"ping"}</code>. The
     * heartbeat gets a <code>{"type":"pong"}</code> response.</li> <li>a sign
     * off in the form: <code>{"type":"goodbye"}</code> to which an identical
     * response is sent before the connection gets closed.</li></ul>
     *
     * @param string the message
     * @throws java.io.IOException if communications with the client is broken
     */
    public void onMessage(String string) throws IOException {
        if (string.equals("{\"type\":\"ping\"}")) { //turn down the noise a bit
            log.trace("Received from client: '{}'", string);            
        } else {
            log.debug("Received from client: '{}'", string);
        }
        try {
            this.onMessage(this.connection.getObjectMapper().readTree(string));
        } catch (JsonProcessingException pe) {
            log.warn("Exception processing \"{}\"\n{}", string, pe.getMessage());
            this.sendErrorMessage(500, Bundle.getMessage(this.connection.getLocale(), "ErrorProcessingJSON", pe.getLocalizedMessage()));
        }
    }

    /**
     * Process a JSON node and handle appropriately.
     * <p>
     * See {@link #onMessage(java.lang.String)} for expected JSON objects.
     *
     * @param root the JSON node.
     * @throws java.io.IOException if communications is broken with the client.
     * @see #onMessage(java.lang.String)
     */
    public void onMessage(JsonNode root) throws IOException {
        try {
            String method = root.path(METHOD).asText(GET);
            String type = root.path(TYPE).asText();
            JsonNode data = root.path(DATA);
            if ((root.path(TYPE).isMissingNode() || type.equals(LIST))
                    && root.path(LIST).isValueNode()) {
                type = root.path(LIST).asText();
                method = LIST;
            }
            if (data.isMissingNode()) {
                if ((type.equals(HELLO) || type.equals(PING) || type.equals(GOODBYE))
                        || (method.equals(LIST) || method.equals(GET))) {
                    // these messages are not required to have a data payload,
                    // so create one if the message did not contain one to avoid
                    // special casing later
                    data = this.connection.getObjectMapper().createObjectNode();
                } else {
                    this.sendErrorMessage(HttpServletResponse.SC_BAD_REQUEST, Bundle.getMessage(this.connection.getLocale(), "ErrorMissingData"));
                    return;
                }
            }
            if (root.path(METHOD).isMissingNode()) { // method not explicitly set
                if (data.path(METHOD).isValueNode()) {
                    // at one point, we used method within data, so check there also
                    // if method was not specified, set it to "post"
                    method = data.path(METHOD).asText(JSON.POST);
                }
            }
            if (type.equals(PING)) { //turn down the noise a bit
                log.trace("Processing '{}' with '{}'", type, data);
            } else {
                log.debug("Processing '{}' with '{}'", type, data);                
            }
            if (method.equals(LIST)) {
                if (this.services.get(type) != null) {
                    for (JsonSocketService<?> service : this.services.get(type)) {
                        service.onList(type, data, this.connection.getLocale());
                    }
                    return;
                } else {
                    log.warn("Requested list type '{}' unknown.", type);
                    this.sendErrorMessage(404, Bundle.getMessage(this.connection.getLocale(), "ErrorUnknownType", type));
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
                        service.onMessage(type, data, method, this.connection.getLocale());
                    }
                } else {
                    log.warn("Requested type '{}' unknown.", type);
                    this.sendErrorMessage(404, Bundle.getMessage(this.connection.getLocale(), "ErrorUnknownType", type));
                }
            } else {
                this.sendErrorMessage(400, Bundle.getMessage(this.connection.getLocale(), "ErrorMissingData"));
            }
            if (type.equals(GOODBYE)) {
                // close the connection if GOODBYE is received.
                this.connection.close();
            }
        } catch (JmriException je) {
            this.sendErrorMessage(500, Bundle.getMessage(this.connection.getLocale(), "ErrorUnsupportedOperation", je.getLocalizedMessage()));
        } catch (JsonException je) {
            this.sendErrorMessage(je);
        }
    }

    private void sendErrorMessage(int code, String message) throws IOException {
        JsonException ex = new JsonException(code, message);
        this.sendErrorMessage(ex);
    }

    private void sendErrorMessage(JsonException ex) throws IOException {
        this.connection.sendMessage(ex.getJsonMessage());
    }

    protected HashMap<String, HashSet<JsonSocketService<?>>> getServices() {
        return new HashMap<>(this.services);
    }
}
