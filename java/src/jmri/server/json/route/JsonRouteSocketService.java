package jmri.server.json.route;

import com.fasterxml.jackson.databind.JsonNode;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Route;
import jmri.RouteManager;
import jmri.Sensor;
import jmri.server.json.JSON;
import jmri.server.json.JsonConnection;
import jmri.server.json.JsonException;
import jmri.server.json.JsonSocketService;
import static jmri.server.json.route.JsonRouteServiceFactory.ROUTE;

/**
 * JSON socket service provider for managing {@link jmri.Route}s.
 *
 * @author Randall Wood
 */
public class JsonRouteSocketService extends JsonSocketService {

    private final JsonRouteHttpService service;
    private final HashMap<String, RouteListener> routes = new HashMap<>();
    private Locale locale;

    public JsonRouteSocketService(JsonConnection connection) {
        super(connection);
        this.service = new JsonRouteHttpService(connection.getObjectMapper());
    }

    @Override
    public void onMessage(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        this.locale = locale;
        String name = data.path(JSON.NAME).asText();
        if (data.path(JSON.METHOD).asText().equals(JSON.PUT)) {
            this.connection.sendMessage(this.service.doPut(type, name, data, locale));
        } else {
            this.connection.sendMessage(this.service.doPost(type, name, data, locale));
        }
        if (!this.routes.containsKey(name)) {
            Route route = InstanceManager.getDefault(RouteManager.class).getRoute(name);
            Sensor sensor = route.getTurnoutsAlgdSensor();
            if (sensor != null) {
                RouteListener listener = new RouteListener(route);
                sensor.addPropertyChangeListener(listener);
                this.routes.put(name, listener);
            }
        }
    }

    @Override
    public void onList(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        this.locale = locale;
        this.connection.sendMessage(this.service.doGetList(type, locale));
    }

    @Override
    public void onClose() {
        routes.values().stream().forEach((route) -> {
            route.route.removePropertyChangeListener(route);
        });
        routes.clear();
    }

    private class RouteListener implements PropertyChangeListener {

        protected final Route route;

        public RouteListener(Route route) {
            this.route = route;
        }

        @Override
        public void propertyChange(PropertyChangeEvent e) {
            if (e.getPropertyName().equals("KnownState")) {
                try {
                    try {
                        getConnection().sendMessage(service.doGet(ROUTE, this.route.getSystemName(), locale));
                    } catch (JsonException ex) {
                        getConnection().sendMessage(ex.getJsonMessage());
                    }
                } catch (IOException ex) {
                    // if we get an error, de-register
                    Sensor sensor = route.getTurnoutsAlgdSensor();
                    if (sensor != null) {
                        sensor.removePropertyChangeListener(this);
                    }
                    routes.remove(this.route.getSystemName());
                }
            }
        }
    }

}
