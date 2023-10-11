package jmri.server.json.logixngicon;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.servlet.http.HttpServletResponse;

import jmri.jmrit.display.LogixNGIcon;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;
import jmri.server.json.JsonRequest;
import static jmri.server.json.logixngicon.JsonLogixNGIconServiceFactory.LOGIXNG_ICON;


/**
 * Provide JSON HTTP services for managing {@link jmri.jmrit.display.LogixNGIcon}s.
 *
 * @author Randall Wood Copyright 2016, 2018
 * @author Daniel Bergqvist (C) 2023
 */
public class JsonLogixNGIconHttpService extends JsonHttpService {

    public JsonLogixNGIconHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public JsonNode doGet(String type, String name, JsonNode data, JsonRequest request) throws JsonException {
        throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, Bundle.getMessage(request.locale, "GetNotAllowed", type), request.id);
    }

    /**
     * Respond to an HTTP POST request for the requested LogixNGIcon.
     * <p>
     * This method throws a 404 Not Found error if the named LogixNGIcon does not
     * exist.
     *
     * @param type   {@link jmri.server.json.logixngicon.JsonLogixNGIconServiceFactory#LOGIXNG_ICON}
     * @param name   the name of the requested LogixNGIcon
     * @param data   JSON data set of attributes of the requested LogixNGIcon to be
     *               updated
     * @param request the JSON request
     * @return an empty JSON logixngicon message.
     */
    @Override
    public JsonNode doPost(String type, String name, JsonNode data, JsonRequest request) throws JsonException {
        int identity = data.path("identity").asInt(-1);
        if (identity != -1) {
            LogixNGIcon logixNGIcon = LogixNGIcon.IDENTITY_MANAGER.getLogixNGIcon(identity);
            if (logixNGIcon == null) {
                throw new JsonException(404, Bundle.getMessage(request.locale, JsonException.ERROR_NOT_FOUND, type, name),
                        request.id);
            }
            logixNGIcon.executeLogixNG();
        } else {
            throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, Bundle.getMessage(request.locale, "InvalidIdentity", identity), request.id);
        }
        ObjectNode node = mapper.createObjectNode();
        return message(LOGIXNG_ICON, node, request.id);
    }

    @Override
    public JsonNode doPut(String type, String name, JsonNode data, JsonRequest request) throws JsonException {
        throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, Bundle.getMessage(request.locale, "PutNotAllowed", type), request.id);
    }

    @Override
    public JsonNode doSchema(String type, boolean server, JsonRequest request) throws JsonException {
        switch (type) {
            case LOGIXNG_ICON:
                return doSchema(type,
                        server,
                        "jmri/server/json/logixngicon/logixngicon-server.json",
                        "jmri/server/json/logixngicon/logixngicon-client.json",
                        request.id);
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(request.locale, JsonException.ERROR_UNKNOWN_TYPE, type), request.id);
        }
    }

    @Override
    public void doDelete(String type, String name, JsonNode data, JsonRequest request) throws JsonException {
        throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, Bundle.getMessage(request.locale, "DeleteNotAllowed", type), request.id);
    }

    @Override
    public JsonNode doGetList(String type, JsonNode data, JsonRequest request) throws JsonException {
        throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, Bundle.getMessage(request.locale, "GetListNotAllowed", type), request.id);
    }

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JsonLogixNGIconHttpService.class);
}
