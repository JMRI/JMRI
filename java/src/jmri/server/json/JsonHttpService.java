package jmri.server.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;

/**
 * Provide HTTP method handlers for JSON RESTful messages
 *
 * It is recommended that this class be as lightweight as possible, by relying
 * either on a helper stored in the InstanceManager, or a helper with static
 * methods.
 *
 * @author Randall Wood
 */
@SuppressWarnings("serial")
public abstract class JsonHttpService {

    protected final ObjectMapper mapper;

    protected JsonHttpService(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * Respond to an HTTP GET request for the requested name.
     *
     * If name is null, return a list of all objects for the given type, if
     * appropriate.
     *
     * @param type   the type of the requested object.
     * @param name   the name of the requested object.
     * @param locale the requesting client's Locale.
     * @return a JSON description of the requested object.
     * @throws JsonException
     */
    public abstract JsonNode doGet(String type, String name, Locale locale) throws JsonException;

    /**
     * Respond to an HTTP POST request for the requested name.
     *
     * This method should throw a 400 Invalid Request error if the named object
     * does not exist.
     *
     * @param type   the type of the requested object.
     * @param name   the name of the requested object.
     * @param data   JSON data set of attributes of the requested object to be
     *               updated.
     * @param locale the requesting client's Locale.
     * @return a JSON description of the requested object after updates have
     *         been applied.
     * @throws JsonException
     */
    public abstract JsonNode doPost(String type, String name, JsonNode data, Locale locale) throws JsonException;

    /**
     * Respond to an HTTP PUT request for the requested name.
     *
     * @param type   the type of the requested object.
     * @param name   the name of the requested object.
     * @param data   JSON data set of attributes of the requested object to be
     *               created or updated.
     * @param locale the requesting client's Locale.
     * @return a JSON description of the requested object.
     * @throws JsonException
     */
    public JsonNode doPut(String type, String name, JsonNode data, Locale locale) throws JsonException {
        throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, Bundle.getMessage(locale, "PutNotAllowed", type));
    }

    /**
     * Respond to an HTTP DELETE request for the requested name.
     *
     * Throw an HTTP 405 Method Not Allowed exception if the object is not
     * intendended to be removable.
     * 
     * Do not throw an error if the requested object does not exist.
     *
     * @param type   the type of the deleted object.
     * @param name   the name of the deleted object.
     * @param locale the requesting client's Locale.
     * @throws JsonException
     */
    public void doDelete(String type, String name, Locale locale) throws JsonException {
        throw new JsonException(HttpServletResponse.SC_METHOD_NOT_ALLOWED, Bundle.getMessage(locale, "DeleteNotAllowed", type));
    }

    /**
     * Respond to an HTTP GET request for a list of items of type.
     *
     * This is called by the {@link jmri.web.servlet.json.JsonServlet} to handle
     * get requests for a type, but no name. Some services that can't return a
     * list may prefer to return a single object instead of a list in this case,
     * while other services may simply throw a 400 Bad Request JsonException in
     * this case.
     *
     * @param type   the type of the requested list.
     * @param locale the requesting client's Locale.
     * @return a JSON list.
     * @throws JsonException
     */
    public abstract JsonNode doGetList(String type, Locale locale) throws JsonException;
}
