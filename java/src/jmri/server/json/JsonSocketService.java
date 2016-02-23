package jmri.server.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Locale;
import jmri.JmriException;
import jmri.jmris.json.JsonConnection;
import jmri.jmris.json.JsonException;

/**
 * Interface for all JSON Services.
 *
 * @author Randall Wood
 */
public abstract class JsonSocketService {

    protected final JsonConnection connection;
    protected final ObjectMapper mapper;

    protected JsonSocketService(JsonConnection connection) {
        this.connection = connection;
        this.mapper = new ObjectMapper();
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
     * @throws java.io.IOException           Thrown if the service cannot send a
     *                                       response. This will cause the JSON
     *                                       Server to close its connection to
     *                                       the client if open.
     * @throws jmri.JmriException            Thrown if the request cannot be
     *                                       handled. Throwing this will cause
     *                                       the JSON Server to pass a 500
     *                                       UnsupportedOperation message to the
     *                                       client.
     * @throws jmri.jmris.json.JsonException Thrown if the service needs to pass
     *                                       an error message back to the
     *                                       client.
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
     * @throws JsonException If the service needs to pass an error message back
     *                       to the client.
     */
    public abstract void onList(String type, JsonNode data, Locale locale) throws JsonException;

    /**
     * Perform any teardown required when closing a connection.
     */
    public abstract void onClose();

    /**
     * Send a JSON node as a message.
     *
     * @param node The JSON node to send.
     * @throws IOException If the message cannot be sent.
     */
    protected void sendMessage(JsonNode node) throws IOException {
        this.connection.sendMessage(this.mapper.writeValueAsString(node));
    }

    /**
     * Send a JsonException as a message.
     *
     * @param ex The exception to send.
     * @throws IOException If the excepection cannot be sent.
     */
    protected void sendErrorMessage(JsonException ex) throws IOException {
        this.sendMessage(ex.getJsonMessage());
    }
}
