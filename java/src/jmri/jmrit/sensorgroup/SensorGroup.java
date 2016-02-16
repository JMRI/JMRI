// SensorGroup.java
package jmri.jmrit.sensorgroup;

import java.util.ArrayList;
import java.util.List;
import jmri.InstanceManager;
import jmri.Route;
import jmri.RouteManager;
import jmri.Sensor;
import jmri.implementation.DefaultRoute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Object for representing, creating and editing sensor groups.
 * <P>
 * Sensor groups are implemented by (groups) of Routes, not by any other object.
 *
 * @author	Bob Jacobsen Copyright (C) 2007
 * @version	$Revision$
 */
public class SensorGroup {

    /**
     * Nobody can build an anonymous object
     */
    //private SensorGroup() {
    //}
    private final static String namePrefix = "SENSOR GROUP:";  // should be upper case
    private final static String nameDivider = ":";

    String name;
    ArrayList<String> sensorList;

    /**
     * Create one, looking up an existing one if present
     */
    SensorGroup(String name) {
        this.name = name;
        // find suitable 
        RouteManager rm = InstanceManager.routeManagerInstance();
        String group = name.toUpperCase();
        List<String> l = rm.getSystemNameList();
        String prefix = (namePrefix + group + nameDivider).toUpperCase();

        sensorList = new ArrayList<String>();
        for (int i = 0; i < l.size(); i++) {
            String routeName = l.get(i);
            if (routeName.startsWith(prefix)) {
                String sensor = routeName.substring(prefix.length());
                // remember that sensor
                sensorList.add(sensor);
            }
        }
    }

    void addPressed() {
        log.debug("start with " + sensorList.size() + " lines");
        RouteManager rm = InstanceManager.routeManagerInstance();
        String group = name.toUpperCase();

        // remove the old routes
        List<String> l = rm.getSystemNameList();
        String prefix = (namePrefix + group + nameDivider).toUpperCase();

        for (int i = 0; i < l.size(); i++) {
            String routeName = l.get(i);
            if (routeName.startsWith(prefix)) {
                // OK, kill this one
                Route r = rm.getBySystemName(l.get(i));
                r.deActivateRoute();
                rm.deleteRoute(r);
            }
        }

        // add the new routes
        for (int i = 0; i < sensorList.size(); i++) {
            String sensor = sensorList.get(i);
            String routeName = namePrefix + group + nameDivider + sensor;
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

    private final static Logger log = LoggerFactory.getLogger(SensorGroup.class.getName());

}
