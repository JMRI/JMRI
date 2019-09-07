package jmri.web.servlet.json;

import static jmri.server.json.JSON.DATA;
import static jmri.server.json.JSON.ID;
import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.STATE;
import static jmri.server.json.JSON.VALUE;
import static jmri.server.json.JsonException.CODE;
import static jmri.server.json.operations.JsonOperations.LOCATION;
import static jmri.server.json.power.JsonPowerServiceFactory.POWER;
import static jmri.web.servlet.ServletUtil.APPLICATION_JSON;
import static jmri.web.servlet.ServletUtil.UTF8;
import static jmri.web.servlet.ServletUtil.UTF8_APPLICATION_JSON;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.jmris.json.JsonServerPreferences;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;
import jmri.server.json.JsonWebSocket;
import jmri.server.json.schema.JsonSchemaServiceCache;
import jmri.spi.JsonServiceFactory;
import jmri.util.FileUtil;
import jmri.web.servlet.ServletUtil;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;
import org.openide.util.lookup.ServiceProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provide JSON formatted responses to requests for information
 * from the JMRI Web Server.
 * <p>
 * See {@link jmri.server.json} for details on how this Servlet handles
 * JSON requests.
 *
 * @author Randall Wood Copyright (C) 2012, 2013, 2016, 2019
 */
@WebServlet(name = "JsonServlet",
        urlPatterns = {"/json"})
@ServiceProvider(service = HttpServlet.class)
public class JsonServlet extends WebSocketServlet {

    private final transient ObjectMapper mapper = new ObjectMapper();
    private final transient HashMap<String, HashSet<JsonHttpService>> services = new HashMap<>();
    private final transient JsonServerPreferences preferences = InstanceManager.getDefault(JsonServerPreferences.class);
    private static final Logger log = LoggerFactory.getLogger(JsonServlet.class);

    @Override
    public void init() throws ServletException {
        this.superInit();
        for (JsonServiceFactory<?, ?> factory : ServiceLoader.load(JsonServiceFactory.class)) {
            JsonHttpService service = factory.getHttpService(this.mapper);
            for (String type : factory.getTypes()) {
                HashSet<JsonHttpService> set = this.services.get(type);
                if (set == null) {
                    this.services.put(type, new HashSet<>());
                    set = this.services.get(type);
                }
                set.add(service);
            }
            for (String type : factory.getReceivedTypes()) {
                HashSet<JsonHttpService> set = this.services.get(type);
                if (set == null) {
                    this.services.put(type, new HashSet<>());
                    set = this.services.get(type);
                }
                set.add(service);
            }
        }
    }

    /**
     * Package private method to call
     * {@link org.eclipse.jetty.websocket.servlet.WebSocketServlet#init()} so
     * this call can be mocked out in unit tests.
     * 
     * @throws ServletException if unable to initialize server
     */
    void superInit() throws ServletException {
        super.init();
    }

    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.register(JsonWebSocket.class);
    }

    /**
     * Handle HTTP get requests for JSON data. Examples:
     * <ul>
     * <li>/json/sensor/IS22 (return data for sensor with system name
     * "IS22")</li>
     * <li>/json/sensor (returns a list of all sensors known to JMRI)</li>
     * </ul>
     * sample responses:
     * <ul>
     * <li>{"type":"sensor","data":{"name":"IS22","userName":"FarEast","comment":null,"inverted":false,"state":4}}</li>
     * <li>[{"type":"sensor","data":{"name":"IS22","userName":"FarEast","comment":null,"inverted":false,"state":4}}]</li>
     * </ul>
     * Note that data will vary for each type.
     * 
     * Note that if an array is returned when requesting a single object, the
     * client must resolve the multiple objects in the array, since it is
     * possible for plugins to JMRI to provide their own response, and JMRI is
     * incapable of judging the correctness of the plugin's response.
     * 
     * @param request an HttpServletRequest object that contains the request the
     *            client has made of the servlet
     * @param response an HttpServletResponse object that contains the response
     *            the servlet sends to the client
     * @throws java.io.IOException if an input or output error is detected when
     *             the servlet handles the GET request
     */
    @Override
    protected void doGet(final HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setHeader("Connection", "Keep-Alive"); // NOI18N
        int id = 0;
        try {
            id = Integer.parseInt(request.getParameter(ID));
        } catch (NumberFormatException ex) {
            id = 0;
        }

        if (request.getAttribute("result") != null) {
            JsonNode result = (JsonNode) request.getAttribute("result");
            int code = result.path(DATA).path(CODE).asInt(HttpServletResponse.SC_OK); // use HTTP error codes when possible
            this.sendMessage(response, code, result, id);
            return;
        }

        String[] rest = request.getRequestURI().substring(request.getContextPath().length()).split("/"); // NOI18N
        String type = (rest.length > 1) ? URLDecoder.decode(rest[1], UTF8) : null;
        if (type != null) {
            response.setContentType(UTF8_APPLICATION_JSON);
            InstanceManager.getDefault(ServletUtil.class).setNonCachingHeaders(response);
            final String name = (rest.length > 2) ? URLDecoder.decode(rest[2], UTF8) : null;
            ObjectNode parameters = this.mapper.createObjectNode();
            for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
                String value = URLDecoder.decode(entry.getValue()[0], UTF8);
                log.debug("Setting parameter {} to {}", entry.getKey(), value);
                try {
                    parameters.setAll((ObjectNode) mapper.readTree(String.format("{\"%s\":%s}", entry.getKey(), value)));
                } catch (JsonParseException ex) {
                    log.error("Unable to parse JSON {\"{}\":{}}", entry.getKey(), value);
                }
            }
            JsonNode reply = null;
            try {
                if (name == null) {
                    if (this.services.get(type) != null) {
                        ArrayList<JsonNode> lists = new ArrayList<>();
                        ArrayNode array = this.mapper.createArrayNode();
                        JsonException exception = null;
                        try {
                            for (JsonHttpService service : this.services.get(type)) {
                                lists.add(service.doGetList(type, parameters, request.getLocale(), id));
                            }
                        } catch (JsonException ex) {
                            exception = ex;
                        }
                        switch (lists.size()) {
                            case 0:
                                if (exception != null) {
                                    throw exception;
                                }
                                reply = JsonHttpService.message(mapper, array, null, id); // either empty array or object with empty data
                                break;
                            case 1:
                                reply = lists.get(0);
                                break;
                            default:
                                for (JsonNode list : lists) {
                                    if (list.isArray()) {
                                        list.forEach((item) -> {
                                            array.add(item);
                                        });
                                    } else if (list.path(DATA).isArray()) {
                                        list.path(DATA).forEach((item) -> {
                                            array.add(item);
                                        });
                                    }
                                }
                                reply = JsonHttpService.message(mapper, array, null, id);
                                break;
                        }
                    }
                    if (reply == null) {
                        log.warn("Type {} unknown.", type);
                        throw new JsonException(HttpServletResponse.SC_NOT_FOUND,
                                Bundle.getMessage(request.getLocale(), "ErrorUnknownType", type), id);
                    }
                } else {
                    if (this.services.get(type) != null) {
                        ArrayNode array = this.mapper.createArrayNode();
                        JsonException exception = null;
                        try {
                            for (JsonHttpService service : this.services.get(type)) {
                                array.add(service.doGet(type, name, parameters, request.getLocale(), id));
                            }
                        } catch (JsonException ex) {
                            exception = ex;
                        }
                        switch (array.size()) {
                            case 0:
                                if (exception != null) {
                                    throw exception;
                                }
                                reply = array;
                                break;
                            case 1:
                                reply = array.get(0);
                                break;
                            default:
                                reply = array;
                                break;
                        }
                    }
                    if (reply == null) {
                        log.warn("Requested type '{}' unknown.", type);
                        throw new JsonException(HttpServletResponse.SC_NOT_FOUND,
                                Bundle.getMessage(request.getLocale(), "ErrorUnknownType", type), id);
                    }
                }
            } catch (JsonException ex) {
                reply = ex.getJsonMessage();
            }
            int code = reply.path(DATA).path(CODE).asInt(HttpServletResponse.SC_OK); // use HTTP error codes when possible
            this.sendMessage(response, code, reply, id);
        } else {
            response.setContentType(ServletUtil.UTF8_TEXT_HTML); // NOI18N
            response.getWriter().print(String.format(request.getLocale(),
                    FileUtil.readURL(FileUtil.findURL(Bundle.getMessage(request.getLocale(), "Json.html"))),
                    String.format(request.getLocale(),
                            Bundle.getMessage(request.getLocale(), "HtmlTitle"),
                            InstanceManager.getDefault(ServletUtil.class).getRailroadName(false),
                            Bundle.getMessage(request.getLocale(), "JsonTitle")),
                    InstanceManager.getDefault(ServletUtil.class).getNavBar(request.getLocale(),
                            request.getContextPath()),
                    InstanceManager.getDefault(ServletUtil.class).getRailroadName(false),
                    InstanceManager.getDefault(ServletUtil.class).getFooter(request.getLocale(),
                            request.getContextPath())));

        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(UTF8_APPLICATION_JSON);
        response.setHeader("Connection", "Keep-Alive"); // NOI18N
        InstanceManager.getDefault(ServletUtil.class).setNonCachingHeaders(response);

        String[] rest = request.getRequestURI().substring(request.getContextPath().length()).split("/"); // NOI18N
        String type = (rest.length > 1) ? URLDecoder.decode(rest[1], UTF8) : null;
        String name = (rest.length > 2) ? URLDecoder.decode(rest[2], UTF8) : null;
        int id = 0;
        try {
            id = Integer.parseInt(request.getParameter(ID));
        } catch (NumberFormatException ex) {
            id = 0;
        }
        JsonNode data;
        JsonNode reply = null;
        try {
            if (request.getContentType().contains(APPLICATION_JSON)) {
                data = this.mapper.readTree(request.getReader());
                if (!data.path(DATA).isMissingNode()) {
                    data = data.path(DATA);
                }
            } else {
                data = this.mapper.createObjectNode();
                if (request.getParameter(STATE) != null) {
                    ((ObjectNode) data).put(STATE, Integer.parseInt(request.getParameter(STATE)));
                } else if (request.getParameter(LOCATION) != null) {
                    ((ObjectNode) data).put(LOCATION, request.getParameter(LOCATION));
                } else if (request.getParameter(VALUE) != null) {
                    // values other than Strings should be sent in a JSON object
                    ((ObjectNode) data).put(VALUE, request.getParameter(VALUE));
                }
            }
            if (type != null) {
                // for historical reasons, set the name to POWER on a power request
                if (type.equals(POWER)) {
                    name = POWER;
                } else if (name == null) {
                    name = data.path(NAME).asText();
                }
                log.debug("POST operation for {}/{} with {}", type, name, data);
                if (name != null) {
                    if (this.services.get(type) != null) {
                        log.debug("Using data: {}", data);
                        ArrayNode array = this.mapper.createArrayNode();
                        JsonException exception = null;
                        try {
                            for (JsonHttpService service : this.services.get(type)) {
                                array.add(service.doPost(type, name, data, request.getLocale(), id));
                            }
                        } catch (JsonException ex) {
                            exception = ex;
                        }
                        switch (array.size()) {
                            case 0:
                                if (exception != null) {
                                    throw exception;
                                }
                                reply = array;
                                break;
                            case 1:
                                reply = array.get(0);
                                break;
                            default:
                                reply = array;
                                break;
                        }
                    }
                    if (reply == null) {
                        log.warn("Type {} unknown.", type);
                        throw new JsonException(HttpServletResponse.SC_NOT_FOUND,
                                Bundle.getMessage(request.getLocale(), "ErrorUnknownType", type), id);
                    }
                } else {
                    log.error("Name must be defined.");
                    throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, "Name must be defined.", id); // Need to I18N
                }
            } else {
                log.warn("Type not specified.");
                throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, "Type must be specified.", id); // Need to I18N
            }
        } catch (JsonException ex) {
            reply = ex.getJsonMessage();
        }
        int code = reply.path(DATA).path(CODE).asInt(HttpServletResponse.SC_OK); // use HTTP error codes when possible
        this.sendMessage(response, code, reply, id);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(UTF8_APPLICATION_JSON);
        response.setHeader("Connection", "Keep-Alive"); // NOI18N
        InstanceManager.getDefault(ServletUtil.class).setNonCachingHeaders(response);

        String[] rest = request.getRequestURI().substring(request.getContextPath().length()).split("/"); // NOI18N
        String type = (rest.length > 1) ? URLDecoder.decode(rest[1], UTF8) : null;
        String name = (rest.length > 2) ? URLDecoder.decode(rest[2], UTF8) : null;
        int id = 0;
        try {
            id = Integer.parseInt(request.getParameter(ID));
        } catch (NumberFormatException ex) {
            id = 0;
        }
        JsonNode data;
        JsonNode reply = null;
        try {
            if (request.getContentType().contains(APPLICATION_JSON)) {
                data = this.mapper.readTree(request.getReader());
                if (!data.path(DATA).isMissingNode()) {
                    data = data.path(DATA);
                }
            } else {
                throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, "PUT request must be a JSON object", id); // need to I18N
            }
            if (type != null) {
                // for historical reasons, set the name to POWER on a power request
                if (type.equals(POWER)) {
                    name = POWER;
                } else if (name == null) {
                    name = data.path(NAME).asText();
                }
                if (name != null) {
                    if (this.services.get(type) != null) {
                        ArrayNode array = this.mapper.createArrayNode();
                        JsonException exception = null;
                        try {
                            for (JsonHttpService service : this.services.get(type)) {
                                array.add(service.doPut(type, name, data, request.getLocale(), id));
                            }
                        } catch (JsonException ex) {
                            exception = ex;
                        }
                        switch (array.size()) {
                            case 0:
                                if (exception != null) {
                                    throw exception;
                                }
                                reply = array;
                                break;
                            case 1:
                                reply = array.get(0);
                                break;
                            default:
                                reply = array;
                                break;
                        }
                    }
                    if (reply == null) {
                        // not a creatable item
                        throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, type + " is not a creatable type", id); // need to I18N
                    }
                } else {
                    log.warn("Type {} unknown.", type);
                    throw new JsonException(HttpServletResponse.SC_NOT_FOUND,
                            Bundle.getMessage(request.getLocale(), "ErrorUnknownType", type), id);
                }
            } else {
                log.warn("Type not specified.");
                throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, "Type must be specified.", id); // Need to I18N
            }
        } catch (JsonException ex) {
            reply = ex.getJsonMessage();
        }
        int code = reply.path(DATA).path(CODE).asInt(HttpServletResponse.SC_OK); // use HTTP error codes when possible
        this.sendMessage(response, code, reply, id);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(UTF8_APPLICATION_JSON);
        response.setHeader("Connection", "Keep-Alive"); // NOI18N
        InstanceManager.getDefault(ServletUtil.class).setNonCachingHeaders(response);

        String[] rest = request.getRequestURI().substring(request.getContextPath().length()).split("/"); // NOI18N
        String type = (rest.length > 1) ? URLDecoder.decode(rest[1], UTF8) : null;
        String name = (rest.length > 2) ? URLDecoder.decode(rest[2], UTF8) : null;
        int id = 0;
        try {
            id = Integer.parseInt(request.getParameter(ID));
        } catch (NumberFormatException ex) {
            id = 0;
        }
        JsonNode reply = mapper.createObjectNode();
        try {
            if (type != null) {
                if (name == null) {
                    throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, "name must be specified", id); // need to I18N
                }
                JsonNode data = mapper.createObjectNode();
                if (request.getContentType().contains(APPLICATION_JSON)) {
                    data = this.mapper.readTree(request.getReader());
                    if (!data.path(DATA).isMissingNode()) {
                        data = data.path(DATA);
                    }
                }
                for (JsonHttpService service : this.services.get(type)) {
                    service.doDelete(type, name, data, request.getLocale(), id);
                }
            } else {
                log.debug("Type not specified.");
                throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, "Type must be specified.", id); // Need to I18N
            }
        } catch (JsonException ex) {
            reply = ex.getJsonMessage();
        }
        int code = reply.path(DATA).path(CODE).asInt(HttpServletResponse.SC_OK); // use HTTP error codes when possible
        // only include a response body if something went wrong
        if (code != HttpServletResponse.SC_OK) {
            this.sendMessage(response, code, reply, id);
        }
    }

    /**
     * Send a message to the HTTP client in an HTTP response. This closes the
     * response to future messages.
     * <p>
     * If {@link JsonServerPreferences#getValidateServerMessages()} is
     * {@code true}, this may send an error message instead of {@code message}
     * if the message is not schema valid.
     *
     * @param response the HTTP response
     * @param code the HTTP response code
     * @param message the message to send
     * @param id set by client for message
     * @throws IOException if unable to send
     */
    private void sendMessage(HttpServletResponse response, int code, JsonNode message, int id) throws IOException {
        if (this.preferences.getValidateServerMessages()) {
            try {
                InstanceManager.getDefault(JsonSchemaServiceCache.class).validateMessage(message, true,
                        response.getLocale(), id);
            } catch (JsonException ex) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write(this.mapper.writeValueAsString(ex.getJsonMessage()));
                return;
            }
        }
        response.setStatus(code);
        response.getWriter().write(this.mapper.writeValueAsString(message));
    }
}
