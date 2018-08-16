package jmri.server.json.schema;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.ValidationMessage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.ServiceLoader;
import java.util.Set;
import javax.annotation.Nonnull;
import jmri.InstanceManagerAutoDefault;
import jmri.server.json.JSON;
import jmri.server.json.JsonException;
import jmri.server.json.JsonHttpService;
import jmri.spi.JsonServiceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cache for mapping {@link jmri.server.json.JsonHttpService}s to types for
 * getting schemas.
 *
 * @author Randall Wood Copyright 2018
 */
public class JsonSchemaServiceCache implements InstanceManagerAutoDefault {

    private HashMap<String, Set<JsonHttpService>> services = null;
    private final Set<String> clientTypes = new HashSet<>();
    private final Set<String> serverTypes = new HashSet<>();
    private final HashMap<String, JsonSchema> clientSchemas = new HashMap<>();
    private final HashMap<String, JsonSchema> serverSchemas = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

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
     *               messages, or any other value for a data schema
     * @param locale the locale for error messages, if any
     * @return the requested schema
     * @throws JsonException            if unable to get schema due to errors
     *                                  processing schema
     * @throws IllegalArgumentException if no JSON service provides schemas for
     *                                  type
     */
    @Nonnull
    public JsonSchema getClientSchema(@Nonnull String type, @Nonnull Locale locale) throws JsonException {
        return this.getSchema(type, false, locale, this.clientSchemas);
    }

    /**
     * Get the server schema for JSON messages or for specific JSON data schema.
     *
     * @param type   the type; use {@link JSON#JSON} to get the schema for
     *               messages, or any other value for a data schema
     * @param locale the locale for error messages, if any
     * @return the requested schema
     * @throws JsonException            if unable to get schema due to errors
     *                                  processing schema
     * @throws IllegalArgumentException if no JSON service provides schemas for
     *                                  type
     */
    @Nonnull
    public JsonSchema getServerSchema(@Nonnull String type, @Nonnull Locale locale) throws JsonException {
        return this.getSchema(type, true, locale, this.serverSchemas);
    }

    private synchronized JsonSchema getSchema(@Nonnull String type, boolean server, @Nonnull Locale locale, @Nonnull HashMap<String, JsonSchema> map) throws JsonException {
        log.trace("getSchema(\"{}\", {}, ...)", type, server);
        JsonSchema result = map.get(type);
        if (result == null) {
            for (JsonHttpService service : this.getServices(type)) {
                log.debug("Processing {} with {}", type, service);
                result = JsonSchemaFactory.getInstance().getSchema(service.doSchema(type, server, locale).path(JSON.DATA).path(JSON.SCHEMA));
                if (result != null) {
                    map.put(type, result);
                    break;
                }
            }
            if (result == null) {
                throw new IllegalArgumentException("type \"" + type + "\" is not a valid JSON server type");
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
     * @throws JsonException if the message does not validate
     */
    public void validateMessage(@Nonnull JsonNode message, boolean server, @Nonnull Locale locale) throws JsonException {
        log.trace("validateMessage(\"{}\", \"{}\", \"{}\", ...)", message, server, locale);
        HashMap<String, JsonSchema> map = server ? this.serverSchemas : this.clientSchemas;
        this.validateJsonNode(message, JSON.JSON, server, locale, map);
        if (message.isArray()) {
            Iterator<JsonNode> elements = message.elements();
            while (elements.hasNext()) {
                this.validateMessage(elements.next(), server, locale);
            }
        } else {
            String type = message.path(JSON.TYPE).asText();
            JsonNode data = message.path(JSON.DATA);
            if (!data.isMissingNode()) {
                this.validateJsonNode(data, type, server, locale, map);
            }
        }
    }

    private void validateJsonNode(@Nonnull JsonNode node, @Nonnull String type, boolean server, @Nonnull Locale locale, @Nonnull HashMap<String, JsonSchema> map) throws JsonException {
        log.trace("validateJsonNode(\"{}\", \"{}\", \"{}\", ...)", node, type, server);
        Set<ValidationMessage> errors = null;
        try {
            errors = this.getSchema(type, server, locale, map).validate(node);
        } catch (JsonException ex) {
            log.error("Unable to validate JSON schemas", ex);
        }
        if (errors != null && !errors.isEmpty()) {
            log.warn("Errors validating {}", node);
            errors.forEach((error) -> {
                log.warn("JSON Validation Error: {}\n\t{}\n\t{}\n\t{}", error.getCode(), error.getMessage(), error.getPath(), error.getType());
            });
            throw new JsonException(server ? 500 : 400, Bundle.getMessage(locale, "LoggedError"));
        }
    }

    private void cacheServices() {
        if (services == null) {
            services = new HashMap<>();
            for (JsonServiceFactory<?, ?> factory : ServiceLoader.load(JsonServiceFactory.class)) {
                JsonHttpService service = factory.getHttpService(this.mapper);
                for (String type : factory.getTypes()) {
                    Set<JsonHttpService> set = this.services.get(type);
                    if (set == null) {
                        this.services.put(type, new HashSet<>());
                        set = this.services.get(type);
                    }
                    this.clientTypes.add(type);
                    this.serverTypes.add(type);
                    set.add(service);
                }
                for (String type : factory.getSentTypes()) {
                    Set<JsonHttpService> set = this.services.get(type);
                    if (set == null) {
                        this.services.put(type, new HashSet<>());
                        set = this.services.get(type);
                    }
                    this.serverTypes.add(type);
                    set.add(service);
                }
                for (String type : factory.getReceivedTypes()) {
                    Set<JsonHttpService> set = this.services.get(type);
                    if (set == null) {
                        this.services.put(type, new HashSet<>());
                        set = this.services.get(type);
                    }
                    this.clientTypes.add(type);
                    set.add(service);
                }
            }
        }
    }
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JsonSchemaServiceCache.class);
}
