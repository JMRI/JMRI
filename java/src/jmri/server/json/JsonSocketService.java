package jmri.server.json;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Locale;
import jmri.JmriException;

/**
 * Interface for all JSON Services.
 *
 * @author Randall Wood
 */
public abstract class JsonSocketService {

    protected final JsonConnection connection;

    protected JsonSocketService(JsonConnection connection) {
        this.connection = connection;
    }

    /**
     * Handle an inbound message.
     *
     * @param type   The service type. If the implementing service responds to
     *               multiple types, it will need to use this to handle data
     *               correctly.
     * @param data   JSON data. The contents of this will depend on the
     *               implementing service.
     * @param locale The locale of the client, which may be different than the
     *               locale of the JMRI server.
     * @throws java.io.IOException Thrown if the service cannot send a response.
     *                             This will cause the JSON Server to close its
     *                             connection to the client if open.
     * @throws jmri.JmriException  Thrown if the request cannot be handled.
     *                             Throwing this will cause the JSON Server to
     *                             pass a 500 UnsupportedOperation message to
     *                             the client.
     * @throws JsonException       Thrown if the service needs to pass an error
     *                             message back to the client.
     */
    public abstract void onMessage(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException;

    /**
     * Handle a request for a list of objects.
     *
     * @param type   The service type. If the implementing service responds to
     *               multiple types, it will need to use this to handle data
     *               correctly.
     * @param data   JSON data. The contents of this will depend on the
     *               implementing service.
     * @param locale The locale of the client, which may be different than the
     *               locale of the JMRI server.
     * @throws java.io.IOException Thrown if the service cannot send a response.
     *                             This will cause the JSON Server to close its
     *                             connection to the client if open.
     * @throws jmri.JmriException  Thrown if the request cannot be handled.
     *                             Throwing this will cause the JSON Server to
     *                             pass a 500 UnsupportedOperation message to
     *                             the client.
     * @throws JsonException       If the service needs to pass an error message
     *                             back to the client.
     */
    public abstract void onList(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException;

    /**
     * Perform any teardown required when closing a connection.
     */
    public abstract void onClose();

    /**
     * @return the connection
     */
    public JsonConnection getConnection() {
        return connection;
    }
}
