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
public class JsonPowerSocketService extends JsonSocketService implements PropertyChangeListener {

    private boolean listening = false;
    private final JsonPowerHttpService service;

    public JsonPowerSocketService(JsonConnection connection) {
        super(connection);
        this.service = new JsonPowerHttpService(connection.getObjectMapper());
    }

    @Override
    public void onMessage(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        if (!this.listening) {
            InstanceManager.getList(PowerManager.class).forEach((manager) -> {
                manager.addPropertyChangeListener(this);
            });
            this.listening = true;
        }
        this.connection.sendMessage(this.service.doPost(type, data.path(NAME).asText(), data, locale));
    }

    @Override
    public void onList(String type, JsonNode data, Locale locale) throws JsonException, IOException {
        this.connection.sendMessage(this.service.doGetList(type, locale));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        try {
            try {
                this.connection.sendMessage(this.service.doGet(POWER, ((PowerManager) evt.getSource()).getUserName(), this.connection.getLocale()));
            } catch (JsonException ex) {
                this.connection.sendMessage(ex.getJsonMessage());
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
