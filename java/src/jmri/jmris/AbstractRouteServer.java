package jmri.jmris;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;
import jmri.Route;

/**
 * Abstract interface between the a JMRI route and a network connection
 *
 * @author Paul Bender Copyright (C) 2010
 * @author Randall Wood Copyright (C) 2013
 */
abstract public class AbstractRouteServer {

    private final HashMap<String, RouteListener> routes;

    public AbstractRouteServer() {
        routes = new HashMap<>();
    }

    /*
     * Protocol Specific Abstract Functions
     */
    abstract public void sendStatus(String route, int Status) throws IOException;

    abstract public void sendErrorStatus(String route) throws IOException;

    abstract public void parseStatus(String statusString) throws JmriException, IOException;

    synchronized protected void addRouteToList(String routeName) {
        if (!routes.containsKey(routeName)) {
            Route r = InstanceManager.getDefault(jmri.RouteManager.class).getRoute(routeName);
            if(r!=null) {
               Sensor tas = r.getTurnoutsAlgdSensor();
               if (tas != null) {  //only add listener if there is a turnout-aligned sensor defined
                   RouteListener rl = new RouteListener(routeName);
                   tas.addPropertyChangeListener(rl);
                   routes.put(routeName, rl);
               }
            }
        }
    }

    synchronized protected void removeRouteFromList(String routeName) {
        if (routes.containsKey(routeName)) {
            Route r = InstanceManager.getDefault(jmri.RouteManager.class).getRoute(routeName);
            if(r!=null) {
               Sensor tas = r.getTurnoutsAlgdSensor();
               if (tas != null) {  //only remove listener if there is a turnout-aligned sensor defined
                   tas.removePropertyChangeListener(routes.get(routeName));
               }
               routes.remove(routeName);
            }
        }
    }

    public void setRoute(String routeName) throws IOException {
        Route route = InstanceManager.getDefault(jmri.RouteManager.class).getRoute(routeName);
        if ( route != null ) {
            route.setRoute();
            addRouteToList(routeName);
        } else {
            sendErrorStatus(routeName);
        }
    }

    public void dispose() {
        for (Map.Entry<String, RouteListener> route : this.routes.entrySet()) {
            Route r = InstanceManager.getDefault(jmri.RouteManager.class).getRoute(route.getKey());
            if (r!=null) {
               Sensor tas = r.getTurnoutsAlgdSensor();
               if (tas != null) {  //only remove listener if there is a turnout-aligned sensor defined
                   tas.removePropertyChangeListener(route.getValue());
               }
            }
        }
        this.routes.clear();
    }

    class RouteListener implements PropertyChangeListener {

        String name = null;
        Sensor sensor = null;

        RouteListener(String routeName) {
            name = routeName;
            Route r = InstanceManager.getDefault(jmri.RouteManager.class).getRoute(name);
            if(r!=null) {
               sensor = r.getTurnoutsAlgdSensor();
            }
        }

        // update state as state of route changes
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            // If the Commanded State changes, show transition state as "<inconsistent>"
            if (e.getPropertyName().equals("KnownState")) {
                try {
                    sendStatus(name, ((Integer) e.getNewValue()));
                } catch (IOException ie) {
                    log.debug("Error Sending Status");
                    // if we get an error, de-register
                    sensor.removePropertyChangeListener(this);
                    removeRouteFromList(name);
                }
            }
        }
    }

    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractRouteServer.class);

}
