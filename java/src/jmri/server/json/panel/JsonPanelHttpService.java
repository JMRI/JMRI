package jmri.server.json.panel;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;
import jmri.server.json.JsonRequest;

public class JsonPanelHttpService extends JsonHttpService {
    public JsonPanelHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public JsonNode doGet(@Nonnull String type, @Nonnull String name, @Nonnull JsonNode data, @Nonnull JsonRequest request)
            throws JsonException {
        throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Panel subscriptions are available over WebSocket.", request.id);
    }

    @Override
    public JsonNode doPost(@Nonnull String type, @Nonnull String name, @Nonnull JsonNode data, @Nonnull JsonRequest request)
            throws JsonException {
        throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Panel subscriptions are available over WebSocket.", request.id);
    }

    @Override
    public JsonNode doGetList(@Nonnull String type, @Nonnull JsonNode data, @Nonnull JsonRequest request)
            throws JsonException {
        throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Panel subscriptions are available over WebSocket.", request.id);
    }

    @Override
    public JsonNode doSchema(@Nonnull String type, boolean server, @Nonnull JsonRequest request) 
            throws JsonException {
        if (JsonPanelServiceFactory.PANEL_DISPLAY_OPTIONS.equals(type)) {
            return mapper.createObjectNode();
        } else {
            throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                Bundle.getMessage(request.locale, JsonException.ERROR_UNKNOWN_TYPE, type), request.id);
        }
    }
}
