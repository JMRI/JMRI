package jmri.server.json.time;

import static jmri.server.json.time.JsonTimeServiceFactory.TIME;

import com.fasterxml.jackson.databind.JsonNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Date;
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
public class JsonTimeSocketService extends JsonSocketService<JsonTimeHttpService> implements PropertyChangeListener {

    private boolean listening = false;

    public JsonTimeSocketService(JsonConnection connection) {
        this(connection, new JsonTimeHttpService(connection.getObjectMapper()));
    }

    // package protected
    JsonTimeSocketService(JsonConnection connection, JsonTimeHttpService service) {
        super(connection, service);
    }

    @Override
    public void onMessage(String type, JsonNode data, String method, Locale locale) throws IOException, JmriException, JsonException {
        if (!this.listening) {
            Timebase manager = InstanceManager.getDefault(Timebase.class);
            manager.addPropertyChangeListener(this);
            this.listening = true;
        }
        this.connection.sendMessage(this.service.doPost(type, null, data, locale));
    }

    @Override
    public void onList(String type, JsonNode data, Locale locale) throws JsonException {
        throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, Bundle.getMessage(locale, "UnlistableService", type));
    }

    @Override
    public void onClose() {
        if (this.listening) {
            Timebase manager = InstanceManager.getDefault(Timebase.class);
            manager.removePropertyChangeListener(this);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        try {
            try {
                Timebase manager = InstanceManager.getDefault(Timebase.class);
                Date time = manager.getTime();
                if (evt.getPropertyName().equals("time")) {
                    time = (Date) evt.getNewValue();
                }
                this.connection.sendMessage(this.service.doGet(TIME, manager, time, this.connection.getLocale()));
            } catch (JsonException ex) {
                this.connection.sendMessage(ex.getJsonMessage());
            }
        } catch (IOException ex) {
            this.onClose();
        }
    }

}
