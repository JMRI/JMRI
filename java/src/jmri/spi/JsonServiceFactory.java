package jmri.spi;

import com.fasterxml.jackson.databind.ObjectMapper;
import javax.annotation.Nonnull;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonHttpService;
import jmri.server.json.JsonSocketService;

/**
 * Factory interface for JSON services.
 * <p>
 * A JSON service is a service provided by the
 * {@link jmri.jmris.json.JsonServer}. This API allows JSON services to be
 * defined in a modular manner. The factory pattern is used since each
 * connection gets a unique instance of each service.
 *
 * @param <H> the specific supported HTTP service
 * @param <S> the specific supported (Web)Socket service
 * @author Randall Wood Copyright 2016, 2018
 */
public interface JsonServiceFactory<H extends JsonHttpService, S extends JsonSocketService<H>> extends JmriServiceProviderInterface {

    /**
     * Get the service type(s) for services created by this factory respond to.
     * These type must have valid schemas for messages received from a client
     * and sent to a client.
     * <p>
     * Types should be single words, in camelCase if needed, unless supporting a
     * plural noun introduced in the JSON 1.x or 2.x protocols and exposed in the
     * JSON 3.0 or newer protocol.
     * <p>
     * If a service returns no types, it will never be used.
     *
     * @return An array of types this service responds to.
     */
    @Nonnull
    public String[] getTypes();

    /**
     * Get the message type(s) services created by this factory send, if not
     * also listed in {@link #getTypes()}. These types must only have schemas
     * for messages sent to a client.
     * <p>
     * Types should be single words, in camelCase if needed, unless supporting a
     * plural noun introduced in the JSON 1.x or 2.x protocols and exposed in
     * the JSON 3.0 or newer protocol.
     *
     * @return An array of types this service sends, but does not respond to.
     */
    @Nonnull
    public default String[] getSentTypes() {
        return new String[0];
    }

    /**
     * Get the message type(s) services created by this factory receive, if not
     * also listed in {@link #getTypes()}. These types must only have schemas
     * for messages received from a client.
     * <p>
     * Types should be single words, in camelCase if needed, unless supporting a
     * plural noun introduced in the JSON 1.x or 2.x protocols and exposed in
     * the JSON 3.0 or newer protocol.
     *
     * @return An array of types this service sends, but does not respond to.
     */
    @Nonnull
    public default String[] getReceivedTypes() {
        return new String[0];
    }

    /**
     * Create a JSON service for the given connection. This connection can be a
     * WebSocket or raw socket.
     *
     * @param connection The connection for this service to respond to.
     * @return A service or null if the service does not support sockets.
     */
    @Nonnull
    public S getSocketService(JsonConnection connection);

    /**
     * Create a JSON HTTP service.
     *
     * @param mapper The object mapper for the HTTP service to use.
     * @return A servlet or null if the service does not support HTTP.
     */
    @Nonnull
    public H getHttpService(ObjectMapper mapper);
}
