//AbstractRouteServer.java
package jmri.jmris;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract interface between the a JMRI route and a network connection
 *
 * @author Paul Bender Copyright (C) 2010
 * @author Randall Wood Copyright (C) 2013
 * @version $Revision$
 */
abstract public class AbstractRouteServer {

    protected ArrayList<String> routes = null;
    static Logger log = LoggerFactory.getLogger(AbstractRouteServer.class);

    public AbstractRouteServer() {
        routes = new ArrayList<String>();
    }

    /*
     * Protocol Specific Abstract Functions
     */
    abstract public void sendStatus(String route, int Status) throws IOException;

    abstract public void sendErrorStatus(String route) throws IOException;

    abstract public void parseStatus(String statusString) throws JmriException, IOException;

    synchronized protected void addRouteToList(String routeName) {
        if (!routes.contains(routeName)) {
            routes.add(routeName);
            Sensor tas = InstanceManager.routeManagerInstance().getRoute(routeName).getTurnoutsAlgdSensor();
            if (tas != null) {  //only add listener if there is a turnout-aligned sensor defined
            	tas.addPropertyChangeListener(new RouteListener(routeName));
            }
        }
    }

    synchronized protected void removeRouteFromList(String routeName) {
        if (routes.contains(routeName)) {
            routes.remove(routeName);
        }
    }

    public void setRoute(String routeName) throws IOException {
        try {
            InstanceManager.routeManagerInstance().getRoute(routeName).setRoute();
            addRouteToList(routeName);
        } catch (NullPointerException ex) {
            sendErrorStatus(routeName);
        }
    }

    class RouteListener implements PropertyChangeListener {

        String name = null;
        Sensor sensor = null;

        RouteListener(String routeName) {
            name = routeName;
            sensor = InstanceManager.routeManagerInstance().getRoute(name).getTurnoutsAlgdSensor();
        }

        // update state as state of route changes
        @Override
        public void propertyChange(PropertyChangeEvent e) {
            // If the Commanded State changes, show transition state as "<inconsistent>"
            if (e.getPropertyName().equals("KnownState")) {
                try {
                    sendStatus(name, ((Integer) e.getNewValue()).intValue());
                } catch (IOException ie) {
                    log.debug("Error Sending Status");
                    // if we get an error, de-register
                    sensor.removePropertyChangeListener(this);
                    removeRouteFromList(name);
                }
            }
        }
    }
}
