package jmri.server.json;

import static jmri.server.json.JSON.FORCE_DELETE;
import static jmri.server.json.JSON.CONFLICT;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.Locale;
import javax.annotation.Nonnull;
import javax.annotation.CheckForNull;
import javax.servlet.http.HttpServletResponse;

/**
 * Provide HTTP method handlers for JSON RESTful messages
 * <p>
 * It is recommended that this class be as lightweight as possible, by relying
 * either on a helper stored in the InstanceManager, or a helper with static
 * methods.
 *
 * <h2>Message ID Handling</h2>
 * <p>
 * A message ID from a client is a positive integer
 * greater than zero, to be passed back unchanged to the client so the client
 * can track direct responses to requests (this is not needed in the RESTful
 * API, but is available in the RESTful API). The Message ID (or zero if none)
 * is passed into most public methods of JsonHttpService as the {@code id}
 * parameter. When creating an object that is to be embedded in another object
 * as a property, it is permissable to pass the additive inverse of the ID to
 * ensure the ID is not included in the embedded object, but allow any error
 * messages to be thrown with the correct message ID.
 * <p>
 * Note that to ensure this works, only create a complete object with
 * {@link #message(String, JsonNode, String, int)} or one of its variants.
 *
 * @author Randall Wood
 */
public abstract class JsonHttpService {

    protected final ObjectMapper mapper;

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
     * @param data   JSON data set of attributes of the requested object
     * @param locale the requesting client's Locale
     * @param id     the message id set by the client
     * @return a JSON description of the requested object
     * @throws JsonException if the named object does not exist or other error
     *                           occurs
     */
    @Nonnull
    public abstract JsonNode doGet(@Nonnull String type, @Nonnull String name, @Nonnull JsonNode data,
            @Nonnull Locale locale, int id)
            throws JsonException;

    /**
     * Respond to an HTTP POST request for the requested name.
     * <p>
     * This method should throw a 400 Invalid Request error if the named object
     * does not exist.
     *
     * @param type   the type of the requested object
     * @param name   the system name of the requested object
     * @param data   JSON data set of attributes of the requested object to be
     *                   updated
     * @param locale the requesting client's Locale
     * @param id     the message id set by the client
     * @return a JSON description of the requested object after updates have
     *         been applied
     * @throws JsonException if the named object does not exist or other error
     *                           occurs
     */
    @Nonnull
    public abstract JsonNode doPost(@Nonnull String type, @Nonnull String name, @Nonnull JsonNode data,
            @Nonnull Locale locale, int id) throws JsonException;

    /**
     * Respond to an HTTP PUT request for the requested name.
     * <p>
     * Throw an HTTP 405 Method Not Allowed exception if new objects of the type
     * are not intended to be addable.
     *
     * @param type   the type of the requested object
     * @param name   the system name of the requested object
     * @param data   JSON data set of attributes of the requested object to be
     *                   created or updated
     * @param locale the requesting client's Locale
     * @param id     the message id set by the client
     * @return a JSON description of the requested object
     * @throws JsonException if the method is not allowed or other error occurs
     */
    @Nonnull
    public JsonNode doPut(@Nonnull String type, @Nonnull String name, @Nonnull JsonNode data, @Nonnull Locale locale, int id)
            throws JsonException {
        throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                Bundle.getMessage(locale, "PutNotAllowed", type), id);
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
     * @param id     the message id set by the client
     * @throws JsonException if this method is not allowed or other error occurs
     */
    public void doDelete(@Nonnull String type, @Nonnull String name, @Nonnull JsonNode data, @Nonnull Locale locale, int id)
            throws JsonException {
        throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED,
                Bundle.getMessage(locale, "DeleteNotAllowed", type), id);
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
     * @param data   JSON data set of attributes of the requested objects
     * @param locale the requesting client's Locale
     * @param id     the message id set by the client
     * @return a JSON list or message containing type {@value JSON#LIST}, the
     *         list as data, and the passed in id
     * @throws JsonException may be thrown by concrete implementations
     */
    @Nonnull
    public abstract JsonNode doGetList(@Nonnull String type, @Nonnull JsonNode data, @Nonnull Locale locale, int id)
            throws JsonException;

    /**
     * Get the JSON Schema for the {@code data} property of the requested type
     * of JSON object. It is a invalid for implementations to not return a valid
     * schema that clients can use to validate a request to or response from the
     * JSON services.
     * <p>
     * Note that a schema must be contained in a standard object as:
     * <p>
     * {@code {"type":"schema", "data":{"schema":<em>schema</em>,
     * "server":boolean}} }
     * <p>
     * If using
     * {@link #doSchema(String, boolean, String, String, int)},
     * an implementation can be as simple as: {@code
     * return doSchema(type, server, "path/to/client/schema.json", "path/to/server/schema.json");
     * }
     *
     * @param type   the type for which a schema is requested
     * @param server true if the schema is for a message from the server; false
     *                   if the schema is for a message from the client
     * @param locale the requesting client's Locale
     * @param id     the message id set by the client
     * @return a JSON Schema valid for the type
     * @throws JsonException if an error occurs preparing schema; if type is is
     *                           not a type handled by this service, this must
     *                           be thrown with an error code of 500 and the
     *                           localized message ERROR_UNKNOWN_TYPE
     */
    @Nonnull
    public abstract JsonNode doSchema(@Nonnull String type, boolean server, @Nonnull Locale locale, int id)
            throws JsonException;

    /**
     * Helper to make implementing
     * {@link #doSchema(String, boolean, Locale, int)} easier.
     * Throws a JsonException based on an IOException or NullPointerException if
     * unable to read the schemas as resources.
     *
     * @param type         the type for which a schema is requested
     * @param server       true if the schema is for a message from the server;
     *                         false if the schema is for a message from the
     *                         client
     * @param serverSchema the path to the schema for a response object of type
     * @param clientSchema the path to the schema for a request object of type
     * @param id           the message id set by the client
     * @return a JSON Schema valid for the type
     * @throws JsonException if an error occurs preparing schema
     */
    @Nonnull
    protected final JsonNode doSchema(@Nonnull String type, boolean server, @Nonnull String serverSchema,
            @Nonnull String clientSchema, int id) throws JsonException {
        JsonNode schema;
        try {
            if (server) {
                schema = this.mapper.readTree(this.getClass().getClassLoader().getResource(serverSchema));
            } else {
                schema = this.mapper.readTree(this.getClass().getClassLoader().getResource(clientSchema));
            }
        } catch (
                IOException |
                IllegalArgumentException ex) {
            throw new JsonException(500, ex, id);
        }
        return this.doSchema(type, server, schema, id);
    }

    /**
     * Helper to make implementing
     * {@link #doSchema(String, boolean, Locale, int)} easier.
     *
     * @param type   the type for which a schema is requested
     * @param server true if the schema is for a message from the server; false
     *                   if the schema is for a message from the client
     * @param schema the schema for a response object of type
     * @param id     the message id set by the client
     * @return a JSON Schema valid for the type
     */
    @Nonnull
    protected final JsonNode doSchema(@Nonnull String type, boolean server, @Nonnull JsonNode schema, int id) {
        ObjectNode data = mapper.createObjectNode();
        data.put(JSON.NAME, type);
        data.put(JSON.SERVER, server);
        data.set(JSON.SCHEMA, schema);
        return message(JSON.SCHEMA, data, id);
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
    public final boolean acceptForceDeleteToken(@Nonnull String type, @Nonnull String name, @CheckForNull String token) {
        return JsonDeleteTokenManager.getDefault().acceptToken(type, name, token);
    }

    /**
     * Throw an HTTP CONFLICT (409) exception when an object is requested to be
     * deleted and it is in use. This exception will include a token that can be
     * used to force deletion by the client and may include a JSON list of the
     * objects using the object for which deletion was requested.
     * 
     * @param type      the type of object in conflicting state
     * @param name      the name of the object in conflicting state
     * @param conflicts the using objects of this object; may be empty
     * @param locale    the client locale
     * @param id        the message id set by the client
     * @throws JsonException the exception
     */
    public final void throwDeleteConflictException(@Nonnull String type, @Nonnull String name,
            @Nonnull ArrayNode conflicts,
            @Nonnull Locale locale, int id) throws JsonException {
        ObjectNode data = mapper.createObjectNode();
        data.put(FORCE_DELETE, JsonDeleteTokenManager.getDefault().getToken(type, name));
        if (conflicts.size() != 0) {
            data.set(CONFLICT, conflicts);
        }
        throw new JsonException(HttpServletResponse.SC_CONFLICT,
                Bundle.getMessage(locale, "ErrorDeleteConflict", type, name), data, id);
    }

    /**
     * Create a message node from an array.
     * 
     * @param data the array
     * @param id   the message id provided by the client or its additive inverse
     * @return if id is a positive, non-zero integer, return a message of type
     *         {@value JSON#LIST} with data as the data and id set; otherwise
     *         return data without modification
     * @see #message(String, JsonNode, String, int)
     * @see #message(String, JsonNode, int)
     * @see #message(ObjectMapper, ArrayNode, String, int)
     * @see #message(ObjectMapper, String, JsonNode, String, int)
     */
    public final JsonNode message(@Nonnull ArrayNode data, int id) {
        return message(mapper, data, null, id);
    }

    /**
     * Create a message node without an explicit method.
     * 
     * @param type the message type
     * @param data the message data
     * @param id   the message id provided by the client or its additive inverse
     * @return a message node without a method property; an id property is only
     *         present if id is greater than zero
     * @see #message(ArrayNode, int)
     * @see #message(String, JsonNode, String, int)
     * @see #message(ObjectMapper, ArrayNode, String, int)
     * @see #message(ObjectMapper, String, JsonNode, String, int)
     */
    public final ObjectNode message(@Nonnull String type, @Nonnull JsonNode data, int id) {
        return message(type, data, null, id);
    }

    /**
     * Create a message node.
     * 
     * @param type   the message type
     * @param data   the message data
     * @param method the message method
     * @param id     the message id provided by the client or its additive
     *                   inverse
     * @return a message node; an id proper
     * @see #message(ArrayNode, int)
     * @see #message(String, JsonNode, int)
     * @see #message(ObjectMapper, ArrayNode, String, int)
     * @see #message(ObjectMapper, String, JsonNode, String, int)
     */
    public final ObjectNode message(@Nonnull String type, @Nonnull JsonNode data, @CheckForNull String method, int id) {
        return message(mapper, type, data, method, id);
    }

    /**
     * Create a message node from an array.
     * 
     * @param mapper the ObjectMapper to use to construct the message
     * @param data   the array
     * @param method the message method
     * @param id     the message id provided by the client or its additive
     *                   inverse
     * @return if id is a positive, non-zero integer, return a message of type
     *         {@value JSON#LIST} with data as the data and id set; otherwise
     *         just return data without modification
     * @see #message(ArrayNode, int)
     * @see #message(String, JsonNode, String, int)
     * @see #message(String, JsonNode, int)
     * @see #message(ObjectMapper, String, JsonNode, String, int)
     */
    public static final JsonNode message(@Nonnull ObjectMapper mapper, @Nonnull ArrayNode data, @CheckForNull String method, int id) {
        return (id > 0) ? message(mapper, JSON.LIST, data, method, id) : data;
    }

    /**
     * Create a message node.
     * 
     * @param mapper the ObjectMapper to use to construct the message
     * @param type   the message type
     * @param data   the message data
     * @param method the message method or null
     * @param id     the message id provided by the client or its additive
     *                   inverse
     * @return a message node; if method is null, no method property is
     *         included; if id is not greater than zero, no id property is
     *         included
     * @see #message(ArrayNode, int)
     * @see #message(String, JsonNode, String, int)
     * @see #message(String, JsonNode, int)
     * @see #message(ObjectMapper, ArrayNode, String, int)
     */
    public static final ObjectNode message(@Nonnull ObjectMapper mapper, @Nonnull String type, @Nonnull JsonNode data, @CheckForNull String method, int id) {
        ObjectNode root = mapper.createObjectNode();
        root.put(JSON.TYPE, type);
        root.set(JSON.DATA, data);
        if (method != null) {
            root.put(JSON.METHOD, method);
        }
        if (id > 0) {
            root.put(JSON.ID, id);
        }
        return root;
    }
}
