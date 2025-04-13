package jmri.jmrit.sensorgroup;

import java.util.ArrayList;

import jmri.InstanceManager;
import jmri.Route;
import jmri.RouteManager;
import jmri.Sensor;
import jmri.implementation.DefaultRoute;

/**
 * Object for representing, creating and editing sensor groups.
 * <p>
 * Sensor groups are implemented by (groups) of Routes, not by any other object.
 * <p>
 * They are not (currently) NamedBean objects.
 *
 * @author Bob Jacobsen Copyright (C) 2007
 */
public class SensorGroup {

    /**
     * Nobody can build an anonymous object
     */
    //private SensorGroup() {
    //}
    private static final String NAME_PREFIX = "SENSOR GROUP:";  // should be upper case
    private static final String NAME_DIVIDER = ":";

    private final String name;
    private final ArrayList<String> sensorList;

    /**
     * Create one, looking up an existing one if present
     * @param name Name of the group
     */
    SensorGroup(String name) {
        this.name = name;
        // find suitable 
        RouteManager rm = InstanceManager.getDefault(jmri.RouteManager.class);
        String group = name;
        String prefix = (NAME_PREFIX + group + NAME_DIVIDER);

        sensorList = new ArrayList<>();
        for (Route route : rm.getNamedBeanSet()) {
            String routeName = route.getSystemName();
            if (routeName.startsWith(prefix)) {
                String sensor = routeName.substring(prefix.length());
                // remember that sensor
                sensorList.add(sensor);
            }
        }
    }

    void addPressed() {
        log.debug("start with {} lines", sensorList.size());
        RouteManager rm = InstanceManager.getDefault(jmri.RouteManager.class);
        String group = name;

        // remove the old routes
        String prefix = (NAME_PREFIX + group + NAME_DIVIDER);

        for (Route r : rm.getNamedBeanSet()) {
            String routeName = r.getSystemName();
            if (routeName.startsWith(prefix)) {
                // OK, kill this one
                r.deActivateRoute();
                rm.deleteRoute(r);
            }
        }

        // add the new routes
        for (int i = 0; i < sensorList.size(); i++) {
            String sensor = sensorList.get(i);
            String routeName = NAME_PREFIX + group + NAME_DIVIDER + sensor;
            Route r = new DefaultRoute(routeName);
            // add the control sensor
            r.addSensorToRoute(sensor, Route.ONACTIVE);
            // add the output sensors
            for (int j = 0; j < sensorList.size(); j++) {
                String outSensor = sensorList.get(j);
                int mode = Sensor.INACTIVE;
                if (i == j) {
                    mode = Sensor.ACTIVE;
                }
                r.addOutputSensor(outSensor, mode);
            }
            // make it persistant & activate
            r.activateRoute();
            rm.register(r);
        }
    }

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(SensorGroup.class);

}
