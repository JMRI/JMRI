package jmri.server.json;

import com.fasterxml.jackson.databind.JsonNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Locale;
import jmri.beans.Bean;

/**
 *
 * @author Randall Wood
 */
public abstract class JsonAsyncHttpListener extends Bean {

    private final JsonHttpService service;
    protected final String type;
    protected final String name;
    protected final JsonNode data;
    protected final PropertyChangeListener listener = (PropertyChangeEvent evt) -> {
        this.propertyChangeSupport.firePropertyChange(evt);
    };

    protected JsonAsyncHttpListener(String type, String name, JsonNode data, JsonHttpService service) {
        this.type = type;
        this.name = name;
        this.data = data;
        this.service = service;
    }

    public final JsonNode doGet(Locale locale) throws JsonException {
        return this.service.doGet(type, name, locale);
    }
    
    public abstract boolean listen();
    
    public abstract void stopListening();
}
