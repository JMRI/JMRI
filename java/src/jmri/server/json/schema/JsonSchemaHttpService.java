package jmri.server.json.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
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
    public JsonNode doGet(String type, String name, JsonNode data, Locale locale, int id) throws JsonException {
        // note use of Boolean for tristate null, true, false
        // if server == null, returns both schemas in an array
        // if server != null, returns single schema for client or server as
        // appropriate
        Boolean server = null;
        if (data.path(JSON.SERVER).isBoolean()) {
            server = data.path(JSON.SERVER).asBoolean();
        }
        if (data.path(JSON.CLIENT).isBoolean()) {
            if (server == null) {
                server = !data.path(JSON.CLIENT).asBoolean();
            } else if (Boolean.TRUE.equals(server)) {
                server = null; // server and client are true
            }
        }
        switch (type) {
            case JSON.SCHEMA:
                if (JSON.JSON.equals(name)) {
                    if (server != null) {
                        return this.doSchema(JSON.JSON, server, locale, id);
                    }
                    return message(mapper.createArrayNode()
                            .add(this.doSchema(JSON.JSON, true, locale, id))
                            .add(this.doSchema(JSON.JSON, false, locale, id)),
                            id);
                } else {
                    try {
                        ArrayNode schemas = this.mapper.createArrayNode();
                        Set<JsonNode> dedup = new HashSet<>();
                        for (JsonHttpService service : InstanceManager.getDefault(JsonSchemaServiceCache.class)
                                .getServices(name)) {
                            if (server == null || server) {
                                this.doSchema(schemas, dedup, service, name, true, locale, id);
                            }
                            if (server == null || !server) {
                                this.doSchema(schemas, dedup, service, name, false, locale, id);
                            }
                        }
                        // return single object if only one, otherwise return
                        // complete array
                        if (schemas.size() == 1) {
                            return schemas.get(0);
                        }
                        return message(schemas, id);
                    } catch (NullPointerException ex) {
                        throw new JsonException(HttpServletResponse.SC_BAD_REQUEST,
                                Bundle.getMessage(locale, JsonException.ERROR_UNKNOWN_TYPE, name), ex, id);
                    }
                }
            case JSON.TYPE:
                if (InstanceManager.getDefault(JsonSchemaServiceCache.class).getTypes().contains(name)) {
                    ObjectNode payload = this.mapper.createObjectNode();
                    payload.put(JSON.NAME, name);
                    payload.put(JSON.SERVER,
                            InstanceManager.getDefault(JsonSchemaServiceCache.class).getServerTypes().contains(name));
                    payload.put(JSON.CLIENT,
                            InstanceManager.getDefault(JsonSchemaServiceCache.class).getClientTypes().contains(name));
                    return message(JSON.TYPE, payload, id);
                } else {
                    throw new JsonException(HttpServletResponse.SC_NOT_FOUND,
                            Bundle.getMessage(locale, JsonException.ERROR_NOT_FOUND, type, name), id);
                }
            default:
                throw new JsonException(HttpServletResponse.SC_BAD_REQUEST,
                        Bundle.getMessage(locale, JsonException.ERROR_UNKNOWN_TYPE, type), id);
        }
    }

    @Override
    public JsonNode doPost(String type, String name, JsonNode data, Locale locale, int id) throws JsonException {
        throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                Bundle.getMessage(locale, "PostNotAllowed", type), id);
    }

    @Override
    public JsonNode doGetList(String type, JsonNode parameters, Locale locale, int id) throws JsonException {
        if (JSON.TYPE.equals(type)) {
            ArrayNode array = this.mapper.createArrayNode();
            JsonNode data = this.mapper.createObjectNode();
            for (String name : InstanceManager.getDefault(JsonSchemaServiceCache.class).getTypes()) {
                array.add(this.doGet(type, name, data, locale, id));
            }
            return message(array, id);
        } else {
            throw new JsonException(HttpServletResponse.SC_BAD_REQUEST,
                    Bundle.getMessage(locale, "UnlistableService", type), id);
        }
    }

    @Override
    public JsonNode doSchema(String type, boolean server, Locale locale, int id) throws JsonException {
        switch (type) {
            case JSON.JSON:
            case JSON.SCHEMA:
            case JSON.TYPE:
                return doSchema(type,
                        server,
                        "jmri/server/json/schema/" + type + "-server.json",
                        "jmri/server/json/schema/" + type + "-client.json",
                        id);
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                        Bundle.getMessage(locale, JsonException.ERROR_UNKNOWN_TYPE, type), id);
        }
    }
    
    private void doSchema(ArrayNode schemas, Set<JsonNode> dedup, JsonHttpService service, String name, boolean server, Locale locale, int id) throws JsonException {
        try {
            JsonNode schema = service.doSchema(name, server, locale, id);
            if (!dedup.contains(schema)) {
                schemas.add(schema);
                dedup.add(schema);
            }
        } catch (JsonException ex) {
            if (ex.getCode() != HttpServletResponse.SC_BAD_REQUEST) {
                throw ex;
            }
        }
    }
}
