package jmri.server.json;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.Locale;
import javax.annotation.Nonnull;
import jmri.JmriException;

/**
 * Interface for JSON Services provided over TCP Sockets or WebSockets.
 *
 * @param <H> The supporting JsonHttpService implementing class
 * @author Randall Wood Copyright 2016, 2018
 */
public abstract class JsonSocketService<H extends JsonHttpService> {

    protected final JsonConnection connection;
    protected final H service;
    private Locale locale;

    protected JsonSocketService(@Nonnull JsonConnection connection, @Nonnull H service) {
        this.connection = connection;
        this.locale = connection.getLocale();
        this.service = service;
    }

    /**
     * Handle an inbound message.
     *
     * @param type   The service type. If the implementing service responds to
     *                   multiple types, it will need to use this to handle data
     *                   correctly.
     * @param data   JSON data. The contents of this will depend on the
     *                   implementing service.
     * @param method The HTTP method to handle in this message.
     * @param locale The locale of the client, which may be different than the
     *                   locale of the JMRI server.
     * @param id     the message id set by the client
     * @throws java.io.IOException Thrown if the service cannot send a response.
     *                                 This will cause the JSON Server to close
     *                                 its connection to the client if open.
     * @throws jmri.JmriException  Thrown if the request cannot be handled.
     *                                 Throwing this will cause the JSON Server
     *                                 to pass a 500 UnsupportedOperation
     *                                 message to the client.
     * @throws JsonException       Thrown if the service needs to pass an error
     *                                 message back to the client.
     */
    public abstract void onMessage(@Nonnull String type, @Nonnull JsonNode data, @Nonnull String method,
            @Nonnull Locale locale, int id) throws IOException, JmriException, JsonException;

    /**
     * Handle a request for a list of objects. Note that this <strong>should
     * not</strong> create listeners for items in the list, but should only
     * create listeners for the object providing the list, if applicable.
     *
     * @param type   The service type; if the implementing service responds to
     *                   multiple types, it will need to use this to handle data
     *                   correctly
     * @param data   JSON data; the contents of this will depend on the
     *                   implementing service
     * @param locale The locale of the client, which may be different than the
     *                   locale of the JMRI server
     * @param id     the message id set by the client
     * @throws java.io.IOException Thrown if the service cannot send a response;
     *                                 this will cause the JSON Server to close
     *                                 its connection to the client if open
     * @throws jmri.JmriException  Thrown if the request cannot be handled;
     *                                 throwing this will cause the JSON Server
     *                                 to pass a 500 UnsupportedOperation
     *                                 message to the client
     * @throws JsonException       If the service needs to pass an error message
     *                                 back to the client; implementing services
     *                                 may throw a JsonException with code 400
     *                                 and the localized message
     *                                 "UnlistableService" to indicate that
     *                                 {@code type} should not be listed
     */
    public abstract void onList(@Nonnull String type, @Nonnull JsonNode data, @Nonnull Locale locale, int id)
            throws IOException, JmriException, JsonException;

    /**
     * Perform any teardown required when closing a connection.
     */
    public abstract void onClose();

    /**
     * Get the connection to the client.
     *
     * @return the connection
     */
    @Nonnull
    public final JsonConnection getConnection() {
        return connection;
    }

    /**
     * Get the supporting {@link JsonHttpService}.
     *
     * @return the supporting service
     */
    @Nonnull
    public final H getHttpService() {
        return service;
    }

    /**
     * Get the in-use locale
     *
     * @return the locale
     */
    @Nonnull
    protected final Locale getLocale() {
        return locale;
    }

    /**
     * Set the in-use locale
     *
     * @param locale the new locale
     */
    protected final void setLocale(@Nonnull Locale locale) {
        this.locale = locale;
    }
}
