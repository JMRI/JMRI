package jmri.server.json.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.ValidationMessage;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import javax.annotation.Nonnull;
import jmri.InstanceManagerAutoDefault;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;
import jmri.server.json.JsonRequest;
import jmri.spi.JsonServiceFactory;

/**
 * Cache for mapping {@link jmri.server.json.JsonHttpService}s to types for
 * getting schemas.
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonSchemaServiceCache implements InstanceManagerAutoDefault {

    private Map<String, Map<String, Set<JsonHttpService>>> services = new HashMap<>();
    private SchemaValidatorsConfig config = new SchemaValidatorsConfig();
    private final Map<String, Set<String>> clientTypes = new HashMap<>();
    private final Map<String, Set<String>> serverTypes = new HashMap<>();
    private final Map<String, Map<String, JsonSchema>> clientSchemas = new HashMap<>();
    private final Map<String, Map<String, JsonSchema>> serverSchemas = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public JsonSchemaServiceCache() {
        Map<String, String> map = new HashMap<>();
        try {
            for (JsonNode mapping : mapper
                    .readTree(JsonSchemaServiceCache.class.getResource("/jmri/server/json/schema-map.json"))) {
                map.put(mapping.get("publicURL").asText(),
                        mapping.get("localURL").asText());
            }
        } catch (IOException ex) {
            log.error("Unable to read JMRI resources for JSON schema mapping", ex);
        }
        config.setUriMappings(map);
    }

    /**
     * Get the services known to this cache that support a specific JSON type.
     *
     * @param type    the JSON type requested
     * @param version the JSON protocol version requested
     * @return the supporting services or an empty set if none
     * @throws NullPointerException if version is not a known version
     */
    @Nonnull
    public synchronized Set<JsonHttpService> getServices(@Nonnull String type, @Nonnull String version) {
        cacheServices(version);
        return services.get(version).getOrDefault(type, new HashSet<>());
    }

    /**
     * Get all types of JSON messages.
     *
     * @param version the JSON protocol version
     * @return the union of the results from {@link #getClientTypes} and
     *         {@link #getServerTypes}
     */
    @Nonnull
    public synchronized Set<String> getTypes(String version) {
        Set<String> set = getClientTypes(version);
        set.addAll(getServerTypes(version));
        return set;
    }

    /**
     * Get the types of JSON messages expected from clients.
     *
     * @param version the JSON protocol version
     * @return the message types
     */
    @Nonnull
    public synchronized Set<String> getClientTypes(String version) {
        cacheServices(version);
        return new HashSet<>(clientTypes.get(version));
    }

    /**
     * Get the types of JSON messages this application sends.
     *
     * @param version the JSON protocol version
     * @return the message types
     */
    @Nonnull
    public synchronized Set<String> getServerTypes(String version) {
        cacheServices(version);
        return new HashSet<>(serverTypes.get(version));
    }

    /**
     * Get the client schema for JSON messages or for specific JSON data schema.
     *
     * @param type    the type; use {@link JSON#JSON} to get the schema for
     *                messages, or any other value for a data schema
     * @param request the JSON request
     * @return the requested schema
     * @throws JsonException            if unable to get schema due to errors
     *                                  processing schema
     * @throws IllegalArgumentException if no JSON service provides schemas for
     *                                  type
     */
    @Nonnull
    public JsonSchema getClientSchema(@Nonnull String type, @Nonnull JsonRequest request) throws JsonException {
        return getSchema(type, false, clientSchemas, request);
    }

    /**
     * Get the server schema for JSON messages or for specific JSON data schema.
     *
     * @param type    the type; use {@link JSON#JSON} to get the schema for
     *                messages, or any other value for a data schema
     * @param request the JSON request
     * @return the requested schema
     * @throws JsonException            if unable to get schema due to errors
     *                                  processing schema
     * @throws IllegalArgumentException if no JSON service provides schemas for
     *                                  type
     */
    @Nonnull
    public JsonSchema getServerSchema(@Nonnull String type, @Nonnull JsonRequest request) throws JsonException {
        return getSchema(type, true, serverSchemas, request);
    }

    private synchronized JsonSchema getSchema(@Nonnull String type, boolean server,
            @Nonnull Map<String, Map<String, JsonSchema>> map, @Nonnull JsonRequest request) throws JsonException {
        cacheServices(request.version);
        JsonSchema result = map.computeIfAbsent(request.version, v -> new HashMap<>()).get(type);
        if (result == null) {
            for (JsonHttpService service : getServices(type, request.version)) {
                log.debug("Processing {} with {}", type, service);
                result = JsonSchemaFactory.getInstance()
                        .getSchema(service.doSchema(type, server, request).path(JSON.DATA).path(JSON.SCHEMA), config);
                if (result != null) {
                    map.get(request.version).put(type, result);
                    break;
                }
            }
            if (result == null) {
                throw new IllegalArgumentException(
                        "type \"" + type + "\" is not a valid JSON " + (server ? "server" : "client") + " type");
            }
        }
        return result;
    }

    /**
     * Validate a JSON message against the schema for JSON messages and data.
     *
     * @param message the message to validate
     * @param server  true if message is from the JSON server; false otherwise
     * @param request the JSON request
     * @throws JsonException if the message does not validate
     */
    public void validateMessage(@Nonnull JsonNode message, boolean server, @Nonnull JsonRequest request)
            throws JsonException {
        log.trace("validateMessage(\"{}\", \"{}\", \"{}\", ...)", message, server, request);
        Map<String, Map<String, JsonSchema>> map = server ? serverSchemas : clientSchemas;
        validateJsonNode(message, JSON.JSON, server, map, request);
        if (message.isArray()) {
            for (JsonNode item : message) {
                validateMessage(item, server, request);
            }
        } else {
            String type = message.path(JSON.TYPE).asText();
            JsonNode data = message.path(JSON.DATA);
            if (!data.isMissingNode()) {
                if (!data.isArray()) {
                    validateJsonNode(data, type, server, map, request);
                } else {
                    validateMessage(data, server, request);
                }
            }
        }
    }

    /**
     * Validate a JSON data object against the schema for JSON messages and
     * data.
     *
     * @param type    the type of data object
     * @param data    the data object to validate
     * @param server  true if message is from the JSON server; false otherwise
     * @param request the JSON request
     * @throws JsonException if the message does not validate
     */
    public void validateData(@Nonnull String type, @Nonnull JsonNode data, boolean server, @Nonnull JsonRequest request)
            throws JsonException {
        log.trace("validateData(\"{}\", \"{}\", \"{}\", \"{}\", ...)", type, data, server, request);
        Map<String, Map<String, JsonSchema>> map = server ? serverSchemas : clientSchemas;
        if (data.isArray()) {
            for (JsonNode item : data) {
                validateData(type, item, server, request);
            }
        } else {
            validateJsonNode(data, type, server, map, request);
        }
    }

    private void validateJsonNode(@Nonnull JsonNode node, @Nonnull String type, boolean server,
            @Nonnull Map<String, Map<String, JsonSchema>> map, @Nonnull JsonRequest request) throws JsonException {
        log.trace("validateJsonNode(\"{}\", \"{}\", \"{}\", ...)", node, type, server);
        Set<ValidationMessage> errors = null;
        try {
            errors = getSchema(type, server, map, request).validate(node);
        } catch (JsonException ex) {
            log.error("Unable to validate JSON schemas", ex);
        }
        if (errors != null && !errors.isEmpty()) {
            log.warn("Errors validating {}", node);
            errors.forEach(error -> log.warn("JSON Validation Error: {}\n\t{}\n\t{}\n\t{}", error.getCode(),
                    error.getMessage(),
                    error.getPath(), error.getType()));
            throw new JsonException(server ? 500 : 400, Bundle.getMessage(request.locale, JsonException.LOGGED_ERROR),
                    request.id);
        }
    }

    private void cacheServices(String version) {
        Set<String> versionedClientTypes = clientTypes.computeIfAbsent(version, v -> new HashSet<>());
        Set<String> versionedServerTypes = serverTypes.computeIfAbsent(version, v -> new HashSet<>());
        Map<String, Set<JsonHttpService>> versionedServices =
                services.computeIfAbsent(version, v -> new HashMap<>());
        if (versionedServices.isEmpty()) {
            for (JsonServiceFactory<?, ?> factory : ServiceLoader.load(JsonServiceFactory.class)) {
                JsonHttpService service = factory.getHttpService(mapper, JSON.V5);
                for (String type : factory.getTypes(JSON.V5)) {
                    Set<JsonHttpService> set = versionedServices.computeIfAbsent(type, v -> new HashSet<>());
                    versionedClientTypes.add(type);
                    versionedServerTypes.add(type);
                    set.add(service);
                }
                for (String type : factory.getSentTypes(JSON.V5)) {
                    Set<JsonHttpService> set = versionedServices.computeIfAbsent(type, v -> new HashSet<>());
                    versionedServerTypes.add(type);
                    set.add(service);
                }
                for (String type : factory.getReceivedTypes(JSON.V5)) {
                    Set<JsonHttpService> set = versionedServices.computeIfAbsent(type, v -> new HashSet<>());
                    versionedClientTypes.add(type);
                    set.add(service);
                }
            }
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JsonSchemaServiceCache.class);
}
