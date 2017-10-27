package jmri.server.json.route;

import static jmri.server.json.route.JsonRouteServiceFactory.ROUTE;
import static jmri.server.json.route.JsonRouteServiceFactory.ROUTES;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSON socket service provider for managing {@link jmri.Route}s.
 *
 * @author Randall Wood
 */
public class JsonRouteSocketService extends JsonSocketService {

    private final JsonRouteHttpService service;
    private final HashMap<String, RouteListener> routeListeners = new HashMap<>();
    private final RoutesListener routesListener = new RoutesListener();
    private Locale locale;
    private final static Logger log = LoggerFactory.getLogger(JsonRouteSocketService.class);
    private RouteManager routeManager = null;

    public JsonRouteSocketService(JsonConnection connection) {
        super(connection);
        this.service = new JsonRouteHttpService(connection.getObjectMapper());
        routeManager = InstanceManager.getDefault(RouteManager.class);
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
        if (!this.routeListeners.containsKey(name)) {
            Route route = routeManager.getRoute(name);
            if (route != null) {
                Sensor sensor = route.getTurnoutsAlgdSensor();
                if (sensor != null) {
                    RouteListener listener = new RouteListener(route);
                    sensor.addPropertyChangeListener(listener);
                    this.routeListeners.put(name, listener);
                }
            }
        }
    }

    @Override
    public void onList(String type, JsonNode data, Locale locale) throws IOException, JmriException, JsonException {
        this.locale = locale;
        this.connection.sendMessage(this.service.doGetList(type, locale));
        log.debug("adding RoutesListener");
        routeManager.addPropertyChangeListener(routesListener); //add parent listener
        addListenersToChildren();
    }

    private void addListenersToChildren() {
        routeManager.getSystemNameList().stream().forEach((rn) -> { //add listeners to each child (if not already)
            if (!routeListeners.containsKey(rn)) {
                log.debug("adding RouteListener for Route {}", rn);
                Route route  = routeManager.getRoute(rn);
                if (route != null) {
                    Sensor sensor = route.getTurnoutsAlgdSensor();
                    if (sensor != null) {
                        RouteListener listener = new RouteListener(route);
                        sensor.addPropertyChangeListener(listener);
                        this.routeListeners.put(rn, listener);
                    }
                }
            }
        });
    }

    @Override
    public void onClose() {
        routeListeners.values().stream().forEach((route) -> {
            route.route.removePropertyChangeListener(route);
        });
        routeListeners.clear();
        routeManager.removePropertyChangeListener(routesListener);

    }

    private class RouteListener implements PropertyChangeListener {

        protected final Route route;

        public RouteListener(Route route) {
            this.route = route;
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            log.debug("in RouteListener for '{}' '{}' ('{}'=>'{}')", this.route.getSystemName(), evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());            
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
                routeListeners.remove(this.route.getSystemName());
            }
        }
    }

    private class RoutesListener implements PropertyChangeListener {
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            log.debug("in RoutesListener for '{}' ('{}' => '{}')", evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());

            try {
                try {
                 // send the new list
                    connection.sendMessage(service.doGetList(ROUTES, locale)); 
                    //child added or removed, reset listeners
                    if (evt.getPropertyName().equals("length")) { // NOI18N 
                        addListenersToChildren();
                    }
                } catch (JsonException ex) {
                    log.warn("json error sending Routes: {}", ex.getJsonMessage());
                    connection.sendMessage(ex.getJsonMessage());
                }
            } catch (IOException ex) {
                // if we get an error, de-register
                log.debug("deregistering sensorsListener due to IOException");
                routeManager.removePropertyChangeListener(routesListener);
            }
        }
    }

}
