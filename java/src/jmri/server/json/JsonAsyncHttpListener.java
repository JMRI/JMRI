package jmri.server.json;

import com.fasterxml.jackson.databind.JsonNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;
import jmri.beans.Bean;

/**
 * Provide an asynchronis PropertyChangeListener for HTTP long polling support.
 *
 * Implementing classes are intermediate listeners between the
 * {@link jmri.web.servlet.json.JsonServlet} the object being polled for
 * changes. These classes need to provide the logic for listening to the correct
 * object, for determining if the client's understanding of the object's state
 * is correct, and for removing the listener when it is no longer needed.
 *
 * @author Randall Wood (C) 2016
 */
public abstract class JsonAsyncHttpListener extends Bean implements PropertyChangeListener {

    /**
     * The service that provides the
     * {@link JsonHttpService#doGet(java.lang.String, java.lang.String, java.util.Locale)}
     * method that this calls.
     */
    private final JsonHttpService service;
    /**
     * The type of the object being listened to.
     */
    protected final String type;
    /**
     * The name of the object being listended to.
     */
    protected final String name;
    /**
     * A JSON representation of the current state of the object as understood by
     * the client.
     */
    protected final JsonNode data;

    /**
     * Create the listener.
     *
     * @param type    The type of object to listen to.
     * @param name    The name of the object to listen to.
     * @param data    The current state of the object as understood by the
     *                client.
     * @param service The JsonHttpService that this listener supports.
     */
    protected JsonAsyncHttpListener(String type, String name, JsonNode data, JsonHttpService service) {
        this.type = type;
        this.name = name;
        this.data = data;
        this.service = service;
    }

    /**
     * Calls
     * {@link JsonHttpService#doGet(java.lang.String, java.lang.String, java.util.Locale)}.
     *
     * @param locale The client locale.
     * @return a JsonNode describing the object defined by {@link #type} and
     *         {@link #name}.
     * @throws JsonException
     */
    public final JsonNode doGet(Locale locale) throws JsonException {
        return this.service.doGet(type, name, locale);
    }

    /**
     * Forwards the {@link java.beans.PropertyChangeEvent} to any listeners this
     * has.
     *
     * @param evt
     */
    @Override
    public final void propertyChange(PropertyChangeEvent evt) {
        this.propertyChangeSupport.firePropertyChange(evt);
    }

    /**
     * Setup the listener to listen for changes in the object being listened to.
     *
     * This method must check that the state represented by {@link #data} is
     * correct, and return false if it is not. After that check, this method can
     * get the object and listen for changes to it.
     *
     * @return true if state in {@link #data} is correct, false otherwise.
     */
    public abstract boolean listen();

    /**
     * Use this method to remove this listener from the object being listened
     * to.
     *
     * Failure to remove this listener within this method will result in memory
     * leaks.
     */
    public abstract void stopListening();
}
