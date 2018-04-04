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
    public JsonNode doGet(String type, String name, Locale locale) throws JsonException {
        return this.doPost(type, name, this.mapper.createObjectNode(), locale);
    }

    @Override
    public JsonNode doPost(String type, String name, JsonNode data, Locale locale) throws JsonException {
        // note use of Boolean for tristate null, true, false
        // if server == null, returns both schemas in an array
        // if server != null, returns single schema for client or server as appropriate
        Boolean server = null;
        if (data.path(JSON.SERVER).isBoolean()) {
            server = data.path(JSON.SERVER).asBoolean();
        }
        if (data.path(JSON.CLIENT).isBoolean()) {
            if (server == null) {
                server = !data.path(JSON.CLIENT).asBoolean();
            } else if (server == true) {
                server = null; // server
            }
        }
        switch (type) {
            case JSON.SCHEMA:
                switch (name) {
                    case JSON.JSON:
                        if (server != null) {
                            return this.doSchema(JSON.JSON, server, locale);
                        }
                        return this.mapper.createArrayNode()
                                .add(this.doSchema(JSON.JSON, true, locale))
                                .add(this.doSchema(JSON.JSON, false, locale));
                    default:
                        try {
                            ArrayNode schemas = this.mapper.createArrayNode();
                            Set<JsonNode> dedup = new HashSet<>();
                            for (JsonHttpService service : InstanceManager.getDefault(JsonSchemaServiceCache.class).getServices(name)) {
                                // separate try/catch blocks to ensure one failure does not
                                // block following from being accepted
                                if (server == null || server) {
                                    try {
                                        JsonNode schema = service.doSchema(name, true, locale);
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
                                if (server == null || !server) {
                                    try {
                                        JsonNode schema = service.doSchema(name, false, locale);
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
                            // return single object if only one, otherwise return complete array
                            if (schemas.size() == 1) {
                                return schemas.get(0);
                            }
                            return schemas;
                        } catch (NullPointerException ex) {
                            throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, Bundle.getMessage(locale, "ErrorUnknownType", name), ex);
                        }
                }
            case JSON.TYPE:
                if (InstanceManager.getDefault(JsonSchemaServiceCache.class).getTypes().contains(name)) {
                    ObjectNode root = this.mapper.createObjectNode();
                    root.put(JSON.TYPE, JSON.TYPE);
                    ObjectNode payload = root.putObject(JSON.DATA);
                    payload.put(JSON.NAME, name);
                    payload.put(JSON.SERVER, InstanceManager.getDefault(JsonSchemaServiceCache.class).getServerTypes().contains(name));
                    payload.put(JSON.CLIENT, InstanceManager.getDefault(JsonSchemaServiceCache.class).getClientTypes().contains(name));
                    return root;
                } else {
                    throw new JsonException(HttpServletResponse.SC_NOT_FOUND, Bundle.getMessage(locale, "ErrorNotFound", type, name));
                }
            default:
                throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, Bundle.getMessage(locale, "ErrorUnknownType", type));
        }
    }

    @Override
    public ArrayNode doGetList(String type, Locale locale) throws JsonException {
        switch (type) {
            case JSON.TYPE:
                ArrayNode root = this.mapper.createArrayNode();
                JsonNode data = this.mapper.createObjectNode();
                for (String name : InstanceManager.getDefault(JsonSchemaServiceCache.class).getTypes()) {
                    root.add(this.doPost(type, name, data, locale));
                }
                return root;
            default:
                throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, Bundle.getMessage(locale, "UnlistableService", type));
        }
    }

    @Override
    public JsonNode doSchema(String type, boolean server, Locale locale) throws JsonException {
        switch (type) {
            case JSON.JSON:
            case JSON.SCHEMA:
            case JSON.TYPE:
                return doSchema(type,
                        server,
                        "jmri/server/json/schema/" + type + "-server.json",
                        "jmri/server/json/schema/" + type + "-client.json");
            default:
                throw new JsonException(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, Bundle.getMessage(locale, "ErrorUnknownType", type));
        }
    }
}
