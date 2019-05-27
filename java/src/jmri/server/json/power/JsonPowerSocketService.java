package jmri.server.json.power;

import static jmri.server.json.JSON.NAME;
import static jmri.server.json.power.JsonPowerServiceFactory.POWER;

import com.fasterxml.jackson.databind.JsonNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
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
    public void onMessage(String type, JsonNode data, String method, Locale locale, int id) throws IOException, JmriException, JsonException {
        this.addListeners();
        this.connection.sendMessage(this.service.doPost(type, data.path(NAME).asText(), data, locale, id), id);
    }

    @Override
    public void onList(String type, JsonNode data, Locale locale, int id) throws JsonException, IOException {
        this.addListeners();
        this.connection.sendMessage(this.service.doGetList(type, data, locale, id), id);
    }

    private void addListeners() {
        if (!this.listening) {
            InstanceManager.getList(PowerManager.class).forEach((manager) -> {
                manager.addPropertyChangeListener(this);
            });
            this.listening = true;
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        try {
            try {
                this.connection.sendMessage(this.service.doGet(POWER, ((PowerManager) evt.getSource()).getUserName(), this.connection.getObjectMapper().createObjectNode(), this.connection.getLocale(), 0), 0);
            } catch (JsonException ex) {
                this.connection.sendMessage(ex.getJsonMessage(), 0);
            }
        } catch (IOException ex) {
            // do nothing - we can only silently fail at this point
        }
    }

    @Override
    public void onClose() {
        InstanceManager.getList(PowerManager.class).forEach((manager) -> {
            manager.removePropertyChangeListener(JsonPowerSocketService.this);
        });
    }

}
