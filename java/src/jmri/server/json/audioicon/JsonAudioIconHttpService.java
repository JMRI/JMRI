package jmri.server.json.audioicon;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.servlet.http.HttpServletResponse;

import jmri.server.json.*;
import static jmri.server.json.audioicon.JsonAudioIconServiceFactory.AUDIO_ICON;


/**
 * Provide JSON HTTP services for managing {@link jmri.jmrit.display.AudioIcon}s.
 *
 * @author Randall Wood Copyright 2016, 2018
 * @author Daniel Bergqvist (C) 2023
 */
public class JsonAudioIconHttpService extends JsonHttpService {

    public JsonAudioIconHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public JsonNode doGet(String type, String name, JsonNode data, JsonRequest request) throws JsonException {
        ObjectNode theData = mapper.createObjectNode();
        theData.put(JSON.AUDIO_ICON_IDENTITY, data.get("identity").asInt());
//        theData.put(JSON.AUDIO_COMMAND, JSON.AUDIO_COMMAND_PLAY);
        theData.put(JSON.AUDIO_COMMAND, JSON.AUDIO_COMMAND_STOP);
        return message(AUDIO_ICON, theData, request.id);
/*
        new Exception("Daniel").printStackTrace();
        // We use this request only to register a listener for the audio icon.
        ObjectNode node = mapper.createObjectNode();
        return message(AUDIO_ICON, node, request.id);
//        throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, Bundle.getMessage(request.locale, "GetNotAllowed", type), request.id);
*/
    }

    /**
     * Respond to an HTTP POST request for the requested AudioIcon.
     * <p>
     * This method throws a 404 Not Found error if the named AudioIcon does not
     * exist.
     *
     * @param type   {@link jmri.server.json.audioicon.JsonAudioIconServiceFactory#LOGIXNG_ICON}
     * @param name   the name of the requested AudioIcon
     * @param data   JSON data set of attributes of the requested AudioIcon to be
     *               updated
     * @param request the JSON request
     * @return an empty JSON audioicon message.
     */
    @Override
    public JsonNode doPost(String type, String name, JsonNode data, JsonRequest request) throws JsonException {
        throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, Bundle.getMessage(request.locale, "PostNotAllowed", type), request.id);
    }

    @Override
    public JsonNode doPut(String type, String name, JsonNode data, JsonRequest request) throws JsonException {
        throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, Bundle.getMessage(request.locale, "PutNotAllowed", type), request.id);
    }

    @Override
    public JsonNode doSchema(String type, boolean server, JsonRequest request) throws JsonException {
        switch (type) {
            case AUDIO_ICON:
                return doSchema(type,
                        server,
                        "jmri/server/json/audioicon/audioicon-server.json",
                        "jmri/server/json/audioicon/audioicon-client.json",
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

//    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JsonAudioIconHttpService.class);
}
