package jmri.server.json.power;

import com.fasterxml.jackson.databind.JsonNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Locale;
import javax.servlet.http.HttpServletResponse;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
import jmri.server.json.JsonSocketService;
import static jmri.server.json.power.JsonPowerServiceFactory.POWER;

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
            InstanceManager.getDefault(PowerManager.class).addPropertyChangeListener(this);
            this.listening = true;
        }
        this.service.doPost(type, null, data, locale);
    }

    @Override
    public void onList(String type, JsonNode data, Locale locale) throws JsonException {
        throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, Bundle.getMessage(locale, "UnlistableService", type));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        try {
            try {
                this.connection.sendMessage(this.service.doGet(POWER, POWER, this.connection.getLocale()));
            } catch (JsonException ex) {
                this.sendErrorMessage(ex);
            }
        } catch (IOException ex) {
            // do nothing - we can only silently fail at this point
        }
    }

    @Override
    public void onClose() {
        InstanceManager.getDefault(PowerManager.class).removePropertyChangeListener(this);
    }

}
