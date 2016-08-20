package jmri.spi;

import com.fasterxml.jackson.databind.ObjectMapper;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonHttpService;
import jmri.server.json.JsonSocketService;

/**
 * Factory interface for JSON services.
 *
 * A JSON service is a service provided by the
 * {@link jmri.jmris.json.JsonServer}. This API allows JSON services to be
 * defined in a modular manner. The factory pattern is used since each
 * connection gets a unique instance of each service.
 *
 * @author Randall Wood
 */
public interface JsonServiceFactory extends JmriServiceProviderInterface {

    /**
     * Get the service type(s) for services created by this factory respond to.
     *
     * Types should be single words, in camelCase if needed, unless supporting a
     * plural noun exposed in the JSON 3.0 protocol.
     *
     * If a service returns no types, it will never be used.
     *
     * @return An array of types this service responds to.
     */
    public String[] getTypes();

    /**
     * Create a JSON service for the given connection. This connection can be a
     * WebSocket or raw socket.
     *
     * @param connection The connection for this service to respond to.
     * @return A service or null if the service does not support sockets.
     */
    public JsonSocketService getSocketService(JsonConnection connection);

    /**
     * Create a JSON HTTP service.
     *
     * @param mapper The object mapper for the HTTP service to use.
     * @return A servlet or null if the service does not support HTTP.
     */
    public JsonHttpService getHttpService(ObjectMapper mapper);
}
