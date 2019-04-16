package jmri.server.json;

import static jmri.server.json.JSON.FORCE;
import static jmri.server.json.JSON.USERS;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletResponse;

/**
 * Provide HTTP method handlers for JSON RESTful messages
 * <p>
 * It is recommended that this class be as lightweight as possible, by relying
 * either on a helper stored in the InstanceManager, or a helper with static
 * methods.
 *
 * @author Randall Wood
 */
public abstract class JsonHttpService {

    protected final ObjectMapper mapper;
    private final Map<String, UUID> pendingDeletions = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(JsonHttpService.class);

    protected JsonHttpService(@Nonnull ObjectMapper mapper) {
        this.mapper = mapper;
        this.mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    /**
     * Respond to an HTTP GET request for the requested name.
     * <p>
     * If name is null, return a list of all objects for the given type, if
     * appropriate.
     * <p>
     * This method should throw a 500 Internal Server Error if type is not
     * recognized.
     *
     * @param type   the type of the requested object
     * @param name   the system name of the requested object
     * @param locale the requesting client's Locale
     * @return a JSON description of the requested object
     * @throws JsonException if the named object does not exist or other error
     *                       occurs
     */
    @Nonnull
    public abstract JsonNode doGet(@Nonnull String type, @Nonnull String name, @Nonnull Locale locale) throws JsonException;

    /**
     * Respond to an HTTP POST request for the requested name.
     * <p>
     * This method should throw a 400 Invalid Request error if the named object
     * does not exist.
     *
     * @param type   the type of the requested object
     * @param name   the system name of the requested object
     * @param data   JSON data set of attributes of the requested object to be
     *               updated
     * @param locale the requesting client's Locale
     * @return a JSON description of the requested object after updates have
     *         been applied
     * @throws JsonException if the named object does not exist or other error
     *                       occurs
     */
    @Nonnull
    public abstract JsonNode doPost(@Nonnull String type, @Nonnull String name, @Nonnull JsonNode data, @Nonnull Locale locale) throws JsonException;

    /**
     * Respond to an HTTP PUT request for the requested name.
     * <p>
     * Throw an HTTP 405 Method Not Allowed exception if new objects of the type
     * are not intended to be addable.
     *
     * @param type   the type of the requested object
     * @param name   the system name of the requested object
     * @param data   JSON data set of attributes of the requested object to be
     *               created or updated
     * @param locale the requesting client's Locale
     * @return a JSON description of the requested object
     * @throws JsonException if the method is not allowed or other error occurs
     */
    @Nonnull
    public JsonNode doPut(@Nonnull String type, @Nonnull String name, @Nonnull JsonNode data, @Nonnull Locale locale) throws JsonException {
        throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, Bundle.getMessage(locale, "PutNotAllowed", type));
    }

    /**
     * Respond to an HTTP DELETE request for the requested name.
     * <p>
     * Throw an HTTP 405 Method Not Allowed exception if the object is not
     * intended to be removable.
     * <p>
     * Do not throw an error if the requested object does not exist.
     *
     * @param type   the type of the deleted object
     * @param name   the system name of the deleted object
     * @param data   additional data
     * @param locale the requesting client's Locale
     * @throws JsonException if this method is not allowed or other error occurs
     */
    public void doDelete(@Nonnull String type, @Nonnull String name, @Nonnull JsonNode data, @Nonnull Locale locale)
            throws JsonException {
        throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                Bundle.getMessage(locale, "DeleteNotAllowed", type));
    }

    /**
     * Respond to an HTTP DELETE request for the requested name.
     * <p>
     * Throw an HTTP 405 Method Not Allowed exception if the object is not
     * intended to be removable.
     * <p>
     * Do not throw an error if the requested object does not exist.
     *
     * @param type   the type of the deleted object
     * @param name   the system name of the deleted object
     * @param locale the requesting client's Locale
     * @throws JsonException if this method is not allowed or other error occurs
     * @deprecated since 4.15.6; use
     *             {@link #doDelete(String, String, JsonNode, Locale)} instead
     */
    @Deprecated
    public void doDelete(@Nonnull String type, @Nonnull String name, @Nonnull Locale locale) throws JsonException {
        doDelete(type, name, mapper.createObjectNode(), locale);
    }

    /**
     * Respond to an HTTP GET request for a list of items of type.
     * <p>
     * This is called by the {@link jmri.web.servlet.json.JsonServlet} to handle
     * get requests for a type, but no name. Services that do not have named
     * objects, such as the {@link jmri.server.json.time.JsonTimeHttpService}
     * should respond to this with a list containing a single JSON object.
     * Services that can't return a list may throw a 400 Bad Request
     * JsonException in this case.
     *
     * @param type   the type of the requested list
     * @param locale the requesting client's Locale
     * @return a JSON list
     * @throws JsonException may be thrown by concrete implementations
     */
    @Nonnull
    public abstract ArrayNode doGetList(@Nonnull String type, @Nonnull Locale locale) throws JsonException;

    /**
     * Get the JSON Schema for the {@code data} property of the requested type
     * of JSON object. It is a invalid for implementations to not return a valid
     * schema that clients can use to validate a request to or response from the
     * JSON services.
     * <p>
     * Note that a schema must be contained in a standard object as:
     * <p>
     * {@code
     * {"type":"schema", "data":{"schema":<em>schema</em>, "server":boolean}} }
     * <p>
     * If using {@link #doSchema(java.lang.String, boolean, java.lang.String, java.lang.String)
     * }, an implementation can be as simple as: {@code
     * return doSchema(type, server, "path/to/client/schema.json", "path/to/server/schema.json");
     * }
     *
     * @param type   the type for which a schema is requested
     * @param server true if the schema is for a message from the server; false
     *               if the schema is for a message from the client
     * @param locale the requesting client's Locale
     * @return a JSON Schema valid for the type
     * @throws JsonException if an error occurs preparing schema; if type is is
     *                       not a type handled by this service, this must be
     *                       thrown with an error code of 500 and the localized
     *                       message "ErrorUnknownType"
     */
    @Nonnull
    public abstract JsonNode doSchema(@Nonnull String type, boolean server, @Nonnull Locale locale) throws JsonException;

    /**
     * Helper to make implementing
     * {@link #doSchema(java.lang.String, boolean, java.util.Locale)} easier.
     * Throws a JsonException based on an IOException or NullPointerException if
     * unable to read the schemas as resources.
     *
     * @param type         the type for which a schema is requested
     * @param server       true if the schema is for a message from the server;
     *                     false if the schema is for a message from the client
     * @param serverSchema the path to the schema for a response object of type
     * @param clientSchema the path to the schema for a request object of type
     * @return a JSON Schema valid for the type
     * @throws JsonException if an error occurs preparing schema
     */
    @Nonnull
    protected final JsonNode doSchema(@Nonnull String type, boolean server, @Nonnull String serverSchema, @Nonnull String clientSchema) throws JsonException {
        JsonNode schema;
        try {
            if (server) {
                schema = this.mapper.readTree(this.getClass().getClassLoader().getResource(serverSchema));
            } else {
                schema = this.mapper.readTree(this.getClass().getClassLoader().getResource(clientSchema));
            }
        } catch (IOException | NullPointerException ex) {
            throw new JsonException(500, ex);
        }
        return this.doSchema(type, server, schema);
    }

    /**
     * Helper to make implementing
     * {@link #doSchema(java.lang.String, boolean, java.util.Locale)} easier.
     *
     * @param type   the type for which a schema is requested
     * @param server true if the schema is for a message from the server; false
     *               if the schema is for a message from the client
     * @param schema the schema for a response object of type
     * @return a JSON Schema valid for the type
     * @throws JsonException if an error occurs preparing schema
     */
    @Nonnull
    protected final JsonNode doSchema(@Nonnull String type, boolean server, @Nonnull JsonNode schema) throws JsonException {
        ObjectNode root = this.mapper.createObjectNode();
        root.put(JSON.TYPE, JSON.SCHEMA);
        ObjectNode data = root.putObject(JSON.DATA);
        data.put(JSON.NAME, type);
        data.put(JSON.SERVER, server);
        data.set(JSON.SCHEMA, schema);
        return root;
    }

    /**
     * Get the in-use ObjectMapper for this service.
     *
     * @return the object mapper
     */
    @Nonnull
    public final ObjectMapper getObjectMapper() {
        return this.mapper;
    }

    /**
     * Verify a deletion token. If the token is not valid any pending deletion
     * tokens for the type and name are also deleted.
     * 
     * @param type  the type of object pending deletion
     * @param name  the name of object pending deletion
     * @param token the token previously provided to client
     * @return true if token was provided to client and no other delete attempt
     *         was made by client with a different or missing token since token
     *         was issued to client; false otherwise
     */
    public final boolean acceptForceDeleteToken(@Nonnull String type, @Nonnull String name, @Nonnull String token) {
        String key = type + name;
        UUID value = this.pendingDeletions.getOrDefault(key, UUID.randomUUID());
        this.pendingDeletions.remove(key);
        try {
            return value.equals(UUID.fromString(token));
        } catch (IllegalArgumentException ex) {
            log.warn("Unable to parse force deletion token string: {}", token);
            return false;
        }
    }

    /**
     * Throw an HTTP CONFLICT (409) exception when an object is requested to be
     * deleted and it is in use. This exception will include a token that can be
     * used to force deletion by the client and may include a JSON list of the
     * objects using the object for which deletion was requested.
     * 
     * @param type   the type of object in conflicting state
     * @param name   the name of the object in conflicting state
     * @param users  the using objects of this object; may be empty
     * @param locale the client locale
     * @throws JsonException the exception
     */
    public final void throwDeleteConflictException(@Nonnull String type, @Nonnull String name, @Nonnull ArrayNode users,
            @Nonnull Locale locale) throws JsonException {
        String key = type + name;
        pendingDeletions.put(key, UUID.randomUUID());
        ObjectNode data = mapper.createObjectNode();
        data.put(FORCE, pendingDeletions.get(key).toString());
        if (users.size() != 0) {
            data.put(USERS, users);
        }
        throw new JsonException(HttpServletResponse.SC_CONFLICT,
                Bundle.getMessage(locale, "ErrorDeleteConflict", type, name), data);
    }

}
