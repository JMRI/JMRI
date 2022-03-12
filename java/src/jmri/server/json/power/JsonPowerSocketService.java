package jmri.server.json.power;

import static jmri.server.json.JSON.GET;
import static jmri.server.json.JSON.NAME;
import static jmri.server.json.power.JsonPowerServiceFactory.POWER;

import com.fasterxml.jackson.databind.JsonNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
import jmri.server.json.JsonRequest;
import jmri.server.json.JsonSocketService;

/**
 *
 * @author Randall Wood
 */
public class JsonPowerSocketService extends JsonSocketService<JsonPowerHttpService> implements PropertyChangeListener {

    private boolean listening = false;

    public JsonPowerSocketService(JsonConnection connection) {
        this(connection, new JsonPowerHttpService(connection.getObjectMapper()));
    }

    protected JsonPowerSocketService(JsonConnection connection, JsonPowerHttpService service) {
        super(connection, service);
    }

    @Override
    public void onMessage(String type, JsonNode data, JsonRequest request) throws IOException, JmriException, JsonException {
        addListeners();
        connection.sendMessage(service.doPost(type, data.path(NAME).asText(), data, request), request.id);
    }

    @Override
    public void onList(String type, JsonNode data, JsonRequest request) throws JsonException, IOException {
        addListeners();
        connection.sendMessage(service.doGetList(type, data, request), request.id);
    }

    private void addListeners() {
        if (!listening) {
            InstanceManager.getList(PowerManager.class).forEach(manager -> manager.addPropertyChangeListener(this));
            listening = true;
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        try {
            try {
                String name = ((PowerManager) evt.getSource()).getUserName();
                connection.sendMessage(service.doGet(POWER, name, connection.getObjectMapper().createObjectNode(), new JsonRequest(connection.getLocale(), connection.getVersion(), GET, 0)), 0);
            } catch (JsonException ex) {
                connection.sendMessage(ex.getJsonMessage(), 0);
            }
        } catch (IOException ex) {
            // do nothing - we can only silently fail at this point
        }
    }

    @Override
    public void onClose() {
        InstanceManager.getList(PowerManager.class).forEach(manager -> manager.removePropertyChangeListener(JsonPowerSocketService.this));
    }

}
