package jmri.server.json.time;

import static jmri.server.json.time.JsonTimeServiceFactory.TIME;

import com.fasterxml.jackson.databind.JsonNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Timebase;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
import jmri.server.json.JsonSocketService;

/**
 *
 * @author Randall Wood
 */
public class JsonTimeSocketService extends JsonSocketService implements PropertyChangeListener {

    private boolean listening = false;
    private final JsonTimeHttpService service;

    public JsonTimeSocketService(JsonConnection connection) {
        super(connection);
        this.service = new JsonTimeHttpService(connection.getObjectMapper());
    }

    @Override
    public void onMessage(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        if (!this.listening) {
            Timebase manager = InstanceManager.getDefault(Timebase.class);
            manager.addMinuteChangeListener(this);
            manager.addPropertyChangeListener(this);
            this.listening = true;
        }
        this.service.doPost(type, null, data, locale);
    }

    @Override
    public void onList(String type, JsonNode data, Locale locale) throws JsonException {
        throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, Bundle.getMessage(locale, "UnlistableService", type));
    }

    @Override
    public void onClose() {
        if (this.listening) {
            Timebase manager = InstanceManager.getDefault(Timebase.class);
            manager.removeMinuteChangeListener(this);
            manager.removePropertyChangeListener(this);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        try {
            try {
                this.connection.sendMessage(this.service.doGet(TIME, null, this.connection.getLocale()));
            } catch (JsonException ex) {
                this.connection.sendMessage(ex.getJsonMessage());
            }
        } catch (IOException ex) {
            // do nothing - the client has dropped off and a ping failure will
            // clean up the connection if its not already being torn down
        }
    }

}
