package jmri.server.json.power;

import static jmri.server.json.JSON.GET;
import static jmri.server.json.JSON.NAME;
import static jmri.server.json.JSON.PREFIX;
import static jmri.server.json.power.JsonPowerServiceFactory.POWER;

import com.fasterxml.jackson.databind.JsonNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.PowerManager;
import jmri.SystemConnectionMemo;
import jmri.jmrix.SystemConnectionMemoManager;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
import jmri.server.json.JsonRequest;
import jmri.server.json.JsonSocketService;

/**
 *
 * @author Randall Wood
 */
public class JsonPowerSocketService extends JsonSocketService<JsonPowerHttpService> implements PropertyChangeListener {

    private final Set<PowerManager> listeningTo = new HashSet<>();

    public JsonPowerSocketService(JsonConnection connection) {
        this(connection, new JsonPowerHttpService(connection.getObjectMapper()));
    }

    protected JsonPowerSocketService(JsonConnection connection, JsonPowerHttpService service) {
        super(connection, service);
    }

    @Override
    public void onMessage(String type, JsonNode data, JsonRequest request) throws IOException, JmriException, JsonException {
        addListener(resolveManager(data));
        connection.sendMessage(service.doPost(type, data.path(NAME).asText(), data, request), request.id);
    }

    @Override
    public void onList(String type, JsonNode data, JsonRequest request) throws JsonException, IOException {
        InstanceManager.getList(PowerManager.class).forEach(this::addListener);
        connection.sendMessage(service.doGetList(type, data, request), request.id);
    }

    private void addListener(PowerManager manager) {
        if (manager != null && listeningTo.add(manager)) {
            manager.addPropertyChangeListener(this);
        }
    }

    /**
     * Resolves the PowerManager targeted by a client message, using the same
     * prefix → name → default fallback order as the HTTP service. Returns null
     * if no PowerManager is configured.
     */
    private PowerManager resolveManager(JsonNode data) {
        String prefix = data.path(PREFIX).asText();
        if (!prefix.isEmpty()) {
            SystemConnectionMemo memo = SystemConnectionMemoManager.getDefault()
                    .getSystemConnectionMemoForSystemPrefix(prefix);
            if (memo != null && memo.provides(PowerManager.class)) {
                return memo.get(PowerManager.class);
            }
            return null;
        }
        String name = data.path(NAME).asText();
        if (!name.isEmpty()) {
            for (PowerManager pm : InstanceManager.getList(PowerManager.class)) {
                if (pm.getUserName().equals(name)) {
                    return pm;
                }
            }
        }
        return InstanceManager.getNullableDefault(PowerManager.class);
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
        listeningTo.forEach(manager -> manager.removePropertyChangeListener(JsonPowerSocketService.this));
    }

}
