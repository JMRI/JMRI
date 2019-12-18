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
import jmri.spi.JsonServiceFactory;

/**
 * Cache for mapping {@link jmri.server.json.JsonHttpService}s to types for
 * getting schemas.
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonSchemaServiceCache implements InstanceManagerAutoDefault {

    private Map<String, Set<JsonHttpService>> services = null;
    private SchemaValidatorsConfig config = new SchemaValidatorsConfig();
    private final Set<String> clientTypes = new HashSet<>();
    private final Set<String> serverTypes = new HashSet<>();
    private final Map<String, JsonSchema> clientSchemas = new HashMap<>();
    private final Map<String, JsonSchema> serverSchemas = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    public JsonSchemaServiceCache() {
        Map<String, String> map = new HashMap<>();
        try {
            for (JsonNode mapping : mapper.readTree(JsonSchemaServiceCache.class.getResource("/jmri/server/json/schema-map.json"))) {
                map.put(mapping.get("publicURL").asText(),
                        mapping.get("localURL").asText());
            }
        } catch (IOException ex) {
            log.error("Unable to read JMRI resources for JSON schema mapping", ex);
        }
        config.setUriMappings(map);
    }

    @Nonnull
    public synchronized Set<JsonHttpService> getServices(@Nonnull String type) {
        this.cacheServices();
        return services.getOrDefault(type, new HashSet<>());
    }

    /**
     * Get all types of JSON messages.
     *
     * @return the union of the results from {@link #getClientTypes()} and
     *         {@link #getServerTypes()}
     */
    @Nonnull
    public synchronized Set<String> getTypes() {
        this.cacheServices();
        Set<String> set = this.getClientTypes();
        set.addAll(this.getServerTypes());
        return set;
    }

    /**
     * Get the types of JSON messages expected from clients.
     *
     * @return the message types
     */
    @Nonnull
    public synchronized Set<String> getClientTypes() {
        this.cacheServices();
        return new HashSet<>(this.clientTypes);
    }

    /**
     * Get the types of JSON messages this application sends.
     *
     * @return the message types
     */
    @Nonnull
    public synchronized Set<String> getServerTypes() {
        this.cacheServices();
        return new HashSet<>(this.serverTypes);
    }

    /**
     * Get the client schema for JSON messages or for specific JSON data schema.
     *
     * @param type   the type; use {@link JSON#JSON} to get the schema for
     *                   messages, or any other value for a data schema
     * @param locale the locale for error messages, if any
     * @param id     message id set by client
     * @return the requested schema
     * @throws JsonException            if unable to get schema due to errors
     *                                      processing schema
     * @throws IllegalArgumentException if no JSON service provides schemas for
     *                                      type
     */
    @Nonnull
    public JsonSchema getClientSchema(@Nonnull String type, @Nonnull Locale locale, int id) throws JsonException {
        return this.getSchema(type, false, locale, this.clientSchemas, id);
    }

    /**
     * Get the server schema for JSON messages or for specific JSON data schema.
     *
     * @param type   the type; use {@link JSON#JSON} to get the schema for
     *                   messages, or any other value for a data schema
     * @param locale the locale for error messages, if any
     * @param id     message id set by client
     * @return the requested schema
     * @throws JsonException            if unable to get schema due to errors
     *                                      processing schema
     * @throws IllegalArgumentException if no JSON service provides schemas for
     *                                      type
     */
    @Nonnull
    public JsonSchema getServerSchema(@Nonnull String type, @Nonnull Locale locale, int id) throws JsonException {
        return this.getSchema(type, true, locale, this.serverSchemas, id);
    }

    private synchronized JsonSchema getSchema(@Nonnull String type, boolean server, @Nonnull Locale locale, @Nonnull Map<String, JsonSchema> map, int id) throws JsonException {
        this.cacheServices();
        JsonSchema result = map.get(type);
        if (result == null) {
            for (JsonHttpService service : this.getServices(type)) {
                log.debug("Processing {} with {}", type, service);
                result = JsonSchemaFactory.getInstance().getSchema(service.doSchema(type, server, locale, id).path(JSON.DATA).path(JSON.SCHEMA), config);
                if (result != null) {
                    map.put(type, result);
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
     * @param locale  the locale for any exceptions that need to be reported to
     *                clients
     * @param id      the id to be included with any exceptions reported to
     *                clients
     * @throws JsonException if the message does not validate
     */
    public void validateMessage(@Nonnull JsonNode message, boolean server, @Nonnull Locale locale, int id) throws JsonException {
        log.trace("validateMessage(\"{}\", \"{}\", \"{}\", ...)", message, server, locale);
        Map<String, JsonSchema> map = server ? this.serverSchemas : this.clientSchemas;
        this.validateJsonNode(message, JSON.JSON, server, locale, map, id);
        if (message.isArray()) {
            for (JsonNode item : message) {
                this.validateMessage(item, server, locale, id);
            }
        } else {
            String type = message.path(JSON.TYPE).asText();
            JsonNode data = message.path(JSON.DATA);
            if (!data.isMissingNode()) {
                if (!data.isArray()) {
                    this.validateJsonNode(data, type, server, locale, map, id);
                } else {
                    this.validateMessage(data, server, locale, id);
                }
            }
        }
    }

    /**
     * Validate a JSON data object against the schema for JSON messages and data.
     *
     * @param type    the type of data object
     * @param data    the data object to validate
     * @param server  true if message is from the JSON server; false otherwise
     * @param locale  the locale for any exceptions that need to be reported to
     *                clients
     * @param id      the id to be included with any exceptions reported to
     *                clients
     * @throws JsonException if the message does not validate
     */
    public void validateData(@Nonnull String type, @Nonnull JsonNode data, boolean server, @Nonnull Locale locale, int id) throws JsonException {
        log.trace("validateData(\"{}\", \"{}\", \"{}\", \"{}\", ...)", type, data, server, locale);
        Map<String, JsonSchema> map = server ? this.serverSchemas : this.clientSchemas;
        if (data.isArray()) {
            for (JsonNode item : data) {
                validateData(type, item, server, locale, id);
            }
        } else {
            validateJsonNode(data, type, server, locale, map, id);
        }
    }

    private void validateJsonNode(@Nonnull JsonNode node, @Nonnull String type, boolean server, @Nonnull Locale locale, @Nonnull Map<String, JsonSchema> map, int id) throws JsonException {
        log.trace("validateJsonNode(\"{}\", \"{}\", \"{}\", ...)", node, type, server);
        Set<ValidationMessage> errors = null;
        try {
            errors = this.getSchema(type, server, locale, map, id).validate(node);
        } catch (JsonException ex) {
            log.error("Unable to validate JSON schemas", ex);
        }
        if (errors != null && !errors.isEmpty()) {
            log.warn("Errors validating {}", node);
            errors.forEach(error -> 
                log.warn("JSON Validation Error: {}\n\t{}\n\t{}\n\t{}", error.getCode(), error.getMessage(),
                        error.getPath(), error.getType()));
            throw new JsonException(server ? 500 : 400, Bundle.getMessage(locale, JsonException.LOGGED_ERROR), id);
        }
    }

    private void cacheServices() {
        if (services == null) {
            services = new HashMap<>();
            for (JsonServiceFactory<?, ?> factory : ServiceLoader.load(JsonServiceFactory.class)) {
                JsonHttpService service = factory.getHttpService(this.mapper);
                for (String type : factory.getTypes()) {
                    Set<JsonHttpService> set = services.computeIfAbsent(type, v -> new HashSet<>());
                    this.clientTypes.add(type);
                    this.serverTypes.add(type);
                    set.add(service);
                }
                for (String type : factory.getSentTypes()) {
                    Set<JsonHttpService> set = services.computeIfAbsent(type, v -> new HashSet<>());
                    this.serverTypes.add(type);
                    set.add(service);
                }
                for (String type : factory.getReceivedTypes()) {
                    Set<JsonHttpService> set = services.computeIfAbsent(type, v -> new HashSet<>());
                    this.clientTypes.add(type);
                    set.add(service);
                }
            }
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JsonSchemaServiceCache.class);
}
