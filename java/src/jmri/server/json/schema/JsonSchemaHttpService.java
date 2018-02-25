package jmri.server.json.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;

/**
 * Service to support getting core JSON Schemas for the JSON Server.
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonSchemaHttpService extends JsonHttpService {

    JsonSchemaHttpService(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public JsonNode doGet(String type, String name, Locale locale) throws JsonException {
        switch (type) {
            case JSON.SCHEMA:
                ArrayNode schemas = this.mapper.createArrayNode();
                for (JsonHttpService service : InstanceManager.getDefault(JsonSchemaServiceCache.class).getServices(name)) {
                    // separate try/catch blocks to ensure one failure does not
                    // block following from being accepted
                    try {
                        schemas.add(service.doSchema(name, true, locale));
                    } catch (JsonException ex) {
                        if (ex.getCode() != HttpServletResponse.SC_BAD_REQUEST) {
                            throw ex;
                        }
                    }
                    try {
                        schemas.add(service.doSchema(name, false, locale));
                    } catch (JsonException ex) {
                        if (ex.getCode() != HttpServletResponse.SC_BAD_REQUEST) {
                            throw ex;
                        }
                    }
                }
                return schemas;
            case JSON.JSON:
                return this.mapper.createArrayNode()
                        .add(this.doSchema(JSON.JSON, true, locale))
                        .add(this.doSchema(JSON.JSON, false, locale));
            case JSON.TYPES:
                ObjectNode root = this.mapper.createObjectNode();
                root.put(JSON.TYPE, JSON.TYPES);
                ObjectNode data = root.putObject(JSON.DATA);
                ArrayNode types = data.putArray(JSON.TYPES);
                InstanceManager.getDefault(JsonSchemaServiceCache.class).getTypes().forEach((t) -> {
                    types.add(t);
                });
                return root;
            default:
                throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, Bundle.getMessage(locale, "GetNotAllowed", type));
        }
    }

    @Override
    public JsonNode doPost(String type, String name, JsonNode data, Locale locale) throws JsonException {
        throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, Bundle.getMessage(locale, "PostNotAllowed", type));
    }

    @Override
    public ArrayNode doGetList(String type, Locale locale) throws JsonException {
        throw new JsonException(400, Bundle.getMessage(locale, "UnlistableService"));
    }

    @Override
    public JsonNode doSchema(String type, boolean server, Locale locale) throws JsonException {
        switch (type) {
            case JSON.JSON:
            case JSON.SCHEMA:
            case JSON.TYPES:
                return doSchema(type,
                        server,
                        "/jmri/server/json/schema/" + type + "-server.json",
                        "/jmri/server/json/schema/" + type + "-client.json");
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(locale, "ErrorUnknownType", type));
        }
    }
}
