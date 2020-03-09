package jmri.web.servlet.json;

import static jmri.server.json.JSON.DATA;
import static jmri.server.json.JSON.ID;
import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.STATE;
import static jmri.server.json.JSON.V5;
import static jmri.server.json.JSON.VALUE;
import static jmri.server.json.JSON.VERSIONS;
import static jmri.server.json.JsonException.CODE;
import static jmri.server.json.operations.JsonOperations.LOCATION;
import static jmri.server.json.power.JsonPowerServiceFactory.POWER;
import static jmri.web.servlet.ServletUtil.APPLICATION_JSON;
import static jmri.web.servlet.ServletUtil.UTF8;
import static jmri.web.servlet.ServletUtil.UTF8_APPLICATION_JSON;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.ServiceLoader;

import javax.annotation.Nonnull;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.server.json.JsonServerPreferences;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;
import jmri.server.json.JsonRequest;
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
 * Provide JSON formatted responses to requests for information from the JMRI
 * Web Server.
 * <p>
 * See {@link jmri.server.json} for details on how this Servlet handles JSON
 * requests.
 *
 * @author Randall Wood Copyright (C) 2012, 2013, 2016, 2019
 */
@WebServlet(name = "JsonServlet",
        urlPatterns = {"/json"})
@ServiceProvider(service = HttpServlet.class)
public class JsonServlet extends WebSocketServlet {

    private final transient ObjectMapper mapper = new ObjectMapper();
    private final transient HashMap<String, HashMap<String, HashSet<JsonHttpService>>> services = new HashMap<>();
    private final transient JsonServerPreferences preferences = InstanceManager.getDefault(JsonServerPreferences.class);
    private static final Logger log = LoggerFactory.getLogger(JsonServlet.class);

    @Override
    public void init() throws ServletException {
        superInit();
        ServiceLoader.load(JsonServiceFactory.class).forEach(factory -> VERSIONS.stream().forEach(version -> {
            JsonHttpService service = factory.getHttpService(mapper, version);
            HashMap<String, HashSet<JsonHttpService>> types = services.computeIfAbsent(version, map -> new HashMap<>());
            Arrays.stream(factory.getTypes(version))
                    .forEach(type -> types.computeIfAbsent(type, set -> new HashSet<>()).add(service));
            Arrays.stream(factory.getReceivedTypes(version))
                    .forEach(type -> types.computeIfAbsent(type, set -> new HashSet<>()).add(service));
        }));
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
     * <li>/json/v5/sensor/IS22 (return data for sensor with system name
     * "IS22")</li>
     * <li>/json/v5/sensor (returns a list of all sensors known to JMRI)</li>
     * </ul>
     * sample responses:
     * <ul>
     * <li>{"type":"sensor","data":{"name":"IS22","userName":"FarEast","comment":null,"inverted":false,"state":4}}</li>
     * <li>[{"type":"sensor","data":{"name":"IS22","userName":"FarEast","comment":null,"inverted":false,"state":4}}]</li>
     * </ul>
     * Note that data will vary for each type. Note that if an array is returned
     * when requesting a single object, the client must resolve the multiple
     * objects in the array, since it is possible for plugins to JMRI to provide
     * their own response, and JMRI is incapable of judging the correctness of
     * the plugin's response.
     * <p>
     * If the request includes a {@literal result} attribute, the content of the
     * response will be solely the contents of that attribute. This is an aid to
     * the development and testing of JMRI and clients, but is not considered a
     * usable feature in production. This capability may be removed without
     * notice if it is deemed too complex to maintain.
     * 
     * @param request  an HttpServletRequest object that contains the request
     *                 the client has made of the servlet
     * @param response an HttpServletResponse object that contains the response
     *                 the servlet sends to the client
     * @throws java.io.IOException if an input or output error is detected when
     *                             the servlet handles the GET request
     */
    @Override
    protected void doGet(final HttpServletRequest request, HttpServletResponse response) throws IOException {
        configureResponse(response);
        JsonRequest jsonRequest = createJsonRequest(request);

        String[] path = request.getRequestURI().substring(request.getContextPath().length()).split("/"); // NOI18N
        String[] rest = path;
        if (path.length > 1 && jsonRequest.version.equals(path[1])) {
            rest = Arrays.copyOfRange(path, 1, path.length);
        }

        // echo the contents of result if present and abort further processing
        if (request.getAttribute("result") != null) {
            JsonNode result = (JsonNode) request.getAttribute("result");
            // use HTTP error codes when possible
            int code = result.path(DATA).path(CODE).asInt(HttpServletResponse.SC_OK);
            sendMessage(response, code, result, jsonRequest);
            return;
        }

        String type = (rest.length > 1) ? URLDecoder.decode(rest[1], UTF8) : null;
        if (type != null && !type.isEmpty()) {
            response.setContentType(UTF8_APPLICATION_JSON);
            InstanceManager.getDefault(ServletUtil.class).setNonCachingHeaders(response);
            final String name = (rest.length > 2) ? URLDecoder.decode(rest[2], UTF8) : null;
            ObjectNode parameters = mapper.createObjectNode();
            for (Map.Entry<String, String[]> entry : request.getParameterMap().entrySet()) {
                String value = URLDecoder.decode(entry.getValue()[0], UTF8);
                log.debug("Setting parameter {} to {}", entry.getKey(), value);
                try {
                    parameters
                            .setAll((ObjectNode) mapper.readTree(String.format("{\"%s\":%s}", entry.getKey(), value)));
                } catch (JsonProcessingException ex) {
                    log.error("Unable to parse JSON {\"{}\":{}}", entry.getKey(), value);
                }
            }
            JsonNode reply = null;
            try {
                if (name == null) {
                    if (services.get(jsonRequest.version).get(type) != null) {
                        ArrayList<JsonNode> lists = new ArrayList<>();
                        ArrayNode array = mapper.createArrayNode();
                        JsonException exception = null;
                        try {
                            for (JsonHttpService service : services.get(jsonRequest.version).get(type)) {
                                lists.add(service.doGetList(type, parameters, jsonRequest));
                            }
                        } catch (JsonException ex) {
                            exception = ex;
                        }
                        switch (lists.size()) {
                            case 0:
                                if (exception != null) {
                                    throw exception;
                                }
                                // either empty array or object with empty data
                                reply = JsonHttpService.message(mapper, array, null, jsonRequest.id);
                                break;
                            case 1:
                                reply = lists.get(0);
                                break;
                            default:
                                for (JsonNode list : lists) {
                                    if (list.isArray()) {
                                        list.forEach(array::add);
                                    } else if (list.path(DATA).isArray()) {
                                        list.path(DATA).forEach(array::add);
                                    }
                                }
                                reply = JsonHttpService.message(mapper, array, null, jsonRequest.id);
                                break;
                        }
                    }
                    if (reply == null) {
                        log.warn("Requested type '{}' unknown.", type);
                        throw new JsonException(HttpServletResponse.SC_NOT_FOUND,
                                JsonBundle.getMessage(request.getLocale(), "ErrorUnknownType", type), jsonRequest.id);
                    }
                } else {
                    if (services.get(jsonRequest.version).get(type) != null) {
                        ArrayNode array = mapper.createArrayNode();
                        JsonException exception = null;
                        try {
                            for (JsonHttpService service : services.get(jsonRequest.version).get(type)) {
                                array.add(service.doGet(type, name, parameters, jsonRequest));
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
                                JsonBundle.getMessage(request.getLocale(), "ErrorUnknownType", type), jsonRequest.id);
                    }
                }
            } catch (JsonException ex) {
                reply = ex.getJsonMessage();
            }
            // use HTTP error codes when possible
            int code = reply.path(DATA).path(CODE).asInt(HttpServletResponse.SC_OK);
            sendMessage(response, code, reply, jsonRequest);
        } else {
            ServletUtil util = InstanceManager.getDefault(ServletUtil.class);
            response.setContentType(ServletUtil.UTF8_TEXT_HTML); // NOI18N
            response.getWriter().print(String.format(request.getLocale(),
                    FileUtil.readURL(FileUtil.findURL(Bundle.getMessage(request.getLocale(), "Json.html"))),
                    util.getTitle(request.getLocale(), Bundle.getMessage(request.getLocale(), "JsonTitle")),
                    util.getNavBar(request.getLocale(), request.getContextPath()),
                    util.getRailroadName(false),
                    util.getFooter(request.getLocale(), request.getContextPath())));

        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        configureResponse(response);
        InstanceManager.getDefault(ServletUtil.class).setNonCachingHeaders(response);

        JsonRequest jsonRequest = createJsonRequest(request);

        String[] path = request.getRequestURI().substring(request.getContextPath().length()).split("/"); // NOI18N
        String[] rest = path;
        if (path.length >= 1 && jsonRequest.version.equals(path[1])) {
            rest = Arrays.copyOfRange(path, 1, path.length);
        }

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
                data = mapper.readTree(request.getReader());
                if (!data.path(DATA).isMissingNode()) {
                    data = data.path(DATA);
                }
            } else {
                data = mapper.createObjectNode();
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
                // for historical reasons, set the name to POWER on a power
                // request
                if (type.equals(POWER)) {
                    name = POWER;
                } else if (name == null) {
                    name = data.path(NAME).asText();
                }
                log.debug("POST operation for {}/{} with {}", type, name, data);
                if (name != null) {
                    if (services.get(jsonRequest.version).get(type) != null) {
                        log.debug("Using data: {}", data);
                        ArrayNode array = mapper.createArrayNode();
                        JsonException exception = null;
                        try {
                            for (JsonHttpService service : services.get(jsonRequest.version).get(type)) {
                                array.add(service.doPost(type, name, data, jsonRequest));
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
                                JsonBundle.getMessage(request.getLocale(), "ErrorUnknownType", type), id);
                    }
                } else {
                    log.error("Name must be defined.");
                    throw new JsonException(HttpServletResponse.SC_BAD_REQUEST,
                            JsonBundle.getMessage(request.getLocale(), "ErrorMissingName"), id);
                }
            } else {
                log.warn("Type not specified.");
                // TODO: I18N
                throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, "Type must be specified.", id);
            }
        } catch (JsonException ex) {
            reply = ex.getJsonMessage();
        }
        // use HTTP error codes when possible
        int code = reply.path(DATA).path(CODE).asInt(HttpServletResponse.SC_OK);
        sendMessage(response, code, reply, jsonRequest);
    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws IOException {
        configureResponse(response);
        InstanceManager.getDefault(ServletUtil.class).setNonCachingHeaders(response);

        JsonRequest jsonRequest = createJsonRequest(request);

        String[] path = request.getRequestURI().substring(request.getContextPath().length()).split("/"); // NOI18N
        String[] rest = path;
        if (path.length >= 1 && jsonRequest.version.equals(path[1])) {
            rest = Arrays.copyOfRange(path, 1, path.length);
        }

        String type = (rest.length > 1) ? URLDecoder.decode(rest[1], UTF8) : null;
        String name = (rest.length > 2) ? URLDecoder.decode(rest[2], UTF8) : null;
        JsonNode data;
        JsonNode reply = null;
        try {
            if (request.getContentType().contains(APPLICATION_JSON)) {
                data = mapper.readTree(request.getReader());
                if (!data.path(DATA).isMissingNode()) {
                    data = data.path(DATA);
                }
            } else {
                throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, "PUT request must be a JSON object",
                        jsonRequest.id); // need to I18N
            }
            if (type != null) {
                // for historical reasons, set the name to POWER on a power
                // request
                if (type.equals(POWER)) {
                    name = POWER;
                } else if (name == null) {
                    name = data.path(NAME).asText();
                }
                if (name != null) {
                    if (services.get(jsonRequest.version).get(type) != null) {
                        ArrayNode array = mapper.createArrayNode();
                        JsonException exception = null;
                        try {
                            for (JsonHttpService service : services.get(jsonRequest.version).get(type)) {
                                array.add(service.doPut(type, name, data, jsonRequest));
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
                        // item cannot be created
                        // TODO: I18N
                        throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, type + " is not a creatable type",
                                jsonRequest.id);
                    }
                } else {
                    log.warn("Requested type '{}' unknown.", type);
                    throw new JsonException(HttpServletResponse.SC_NOT_FOUND,
                            JsonBundle.getMessage(request.getLocale(), "ErrorUnknownType", type), jsonRequest.id);
                }
            } else {
                log.warn("Type not specified.");
                // TODO: I18N
                throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, "Type must be specified.", jsonRequest.id);
            }
        } catch (JsonException ex) {
            reply = ex.getJsonMessage();
        }
        // use HTTP error codes when possible
        int code = reply.path(DATA).path(CODE).asInt(HttpServletResponse.SC_OK);
        sendMessage(response, code, reply, jsonRequest);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        configureResponse(response);
        InstanceManager.getDefault(ServletUtil.class).setNonCachingHeaders(response);

        JsonRequest jsonRequest = createJsonRequest(request);

        String[] path = request.getRequestURI().substring(request.getContextPath().length()).split("/"); // NOI18N
        String[] rest = path;
        if (path.length >= 1 && jsonRequest.version.equals(path[1])) {
            rest = Arrays.copyOfRange(path, 1, path.length);
        }
        String type = (rest.length > 1) ? URLDecoder.decode(rest[1], UTF8) : null;
        String name = (rest.length > 2) ? URLDecoder.decode(rest[2], UTF8) : null;
        JsonNode reply = mapper.createObjectNode();
        try {
            if (type != null) {
                if (name == null) {
                    throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, "name must be specified",
                            jsonRequest.id); // need to I18N
                }
                if (services.get(jsonRequest.version).get(type) != null) {
                    JsonNode data = mapper.createObjectNode();
                    if (request.getContentType().contains(APPLICATION_JSON)) {
                        data = mapper.readTree(request.getReader());
                        if (!data.path(DATA).isMissingNode()) {
                            data = data.path(DATA);
                        }
                    }
                    for (JsonHttpService service : services.get(jsonRequest.version).get(type)) {
                        service.doDelete(type, name, data, jsonRequest);
                    }
                } else {
                    log.warn("Requested type '{}' unknown.", type);
                    throw new JsonException(HttpServletResponse.SC_NOT_FOUND,
                            JsonBundle.getMessage(request.getLocale(), "ErrorUnknownType", type), jsonRequest.id);
                }
            } else {
                log.debug("Type not specified.");
                // TODO: I18N
                throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, "Type must be specified.", jsonRequest.id);
            }
        } catch (JsonException ex) {
            reply = ex.getJsonMessage();
        }
        // use HTTP error codes when possible
        int code = reply.path(DATA).path(CODE).asInt(HttpServletResponse.SC_OK);
        // only include a response body if something went wrong
        if (code != HttpServletResponse.SC_OK) {
            sendMessage(response, code, reply, jsonRequest);
        }
    }

    /**
     * Create a JsonRequest from an HttpServletRequest.
     * 
     * @param request the source
     * @return a new JsonRequest
     */
    private JsonRequest createJsonRequest(HttpServletRequest request) {
        int id = 0;
        String version = V5;
        String idParameter = request.getParameter(ID);
        if (idParameter != null) {
            try {
                id = Integer.parseInt(idParameter);
            } catch (NumberFormatException ex) {
                id = 0;
            }
        }

        String[] path = request.getRequestURI().substring(request.getContextPath().length()).split("/"); // NOI18N
        if (path.length > 1 && VERSIONS.stream().anyMatch(v -> v.equals(path[1]))) {
            version = path[1];
        }
        return new JsonRequest(request.getLocale(), version, request.getMethod().toLowerCase(), id);
    }

    /**
     * Configure common settings for the response.
     * 
     * @param response the response to configure
     */
    private void configureResponse(HttpServletResponse response) {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(UTF8_APPLICATION_JSON);
        response.setHeader("Connection", "Keep-Alive"); // NOI18N
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
     * @param code     the HTTP response code
     * @param message  the message to send
     * @param request  the JSON request
     * @throws IOException if unable to send
     */
    private void sendMessage(@Nonnull HttpServletResponse response, int code, @Nonnull JsonNode message,
            @Nonnull JsonRequest request) throws IOException {
        if (preferences.getValidateServerMessages()) {
            try {
                InstanceManager.getDefault(JsonSchemaServiceCache.class).validateMessage(message, true, request);
            } catch (JsonException ex) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().write(mapper.writeValueAsString(ex.getJsonMessage()));
                return;
            }
        }
        response.setStatus(code);
        response.getWriter().write(mapper.writeValueAsString(message));
    }
}
