package jmri.server.json.time;

import com.fasterxml.jackson.databind.JsonNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Date;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Timebase;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
import jmri.server.json.JsonRequest;
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
    public void onMessage(String type, JsonNode data, JsonRequest request) throws IOException, JmriException, JsonException {
        if (!listening) {
            Timebase manager = InstanceManager.getDefault(Timebase.class);
            manager.addPropertyChangeListener(this);
            listening = true;
        }
        connection.sendMessage(service.doPost(type, null, data, request), request.id);
    }

    @Override
    public void onList(String type, JsonNode data, JsonRequest request) throws JsonException {
        throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, Bundle.getMessage(request.locale, "UnlistableService", type), request.id);
    }

    @Override
    public void onClose() {
        if (listening) {
            Timebase manager = InstanceManager.getDefault(Timebase.class);
            manager.removePropertyChangeListener(this);
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        try {
            Timebase manager = InstanceManager.getDefault(Timebase.class);
            Date time = manager.getTime();
            if (evt.getPropertyName().equals("time")) {
                time = (Date) evt.getNewValue();
            }
            connection.sendMessage(service.doGet(manager, time, 0), 0);
        } catch (IOException ex) {
            onClose();
        }
    }

}
