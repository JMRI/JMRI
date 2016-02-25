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
import jmri.jmris.json.JsonConnection;
import jmri.server.json.JsonException;
import jmri.server.json.JsonSocketService;
import static jmri.server.json.power.JsonPowerServiceFactory.POWER;

/**
 *
 * @author Randall Wood
 */
public class JsonPowerSocketService extends JsonSocketService implements PropertyChangeListener {

    private PowerManager manager;
    private final JsonPowerHttpService service;

    public JsonPowerSocketService(JsonConnection connection) {
        super(connection);
        this.service = new JsonPowerHttpService(this.mapper);
    }

    @Override
    public void onMessage(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        if (this.manager == null) {
            this.manager = InstanceManager.getDefault(PowerManager.class);
            this.manager.addPropertyChangeListener(this);
        }
        this.service.doPost(type, POWER, data, locale);
    }

    @Override
    public void onList(String type, JsonNode data, Locale locale) throws JsonException {
        throw new JsonException(HttpServletResponse.SC_BAD_REQUEST, Bundle.getMessage(locale, "UnlistableService", type));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        try {
            try {
                this.sendMessage(this.service.doGet(POWER, POWER, this.connection.getLocale()));
            } catch (JsonException ex) {
                this.sendErrorMessage(ex);
            }
        } catch (IOException ex) {
            // do nothing - we can only silently fail at this point
        }
    }

    @Override
    public void onClose() {
        if (this.manager != null) {
            this.manager.removePropertyChangeListener(this);
        }
    }

}
