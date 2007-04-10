// SensorGroup.java

package jmri.jmrit.sensorgroup;

import jmri.*;
import com.sun.java.util.collections.ArrayList;
import com.sun.java.util.collections.List;

/**
 * Object for representing, creating and editing sensor groups.
 * <P>
 * Sensor groups are implemented by (groups) of Routes, not by
 * any other object.
 *
 * @author			Bob Jacobsen   Copyright (C) 2007
 * @version			$Revision: 1.1 $
 */
public class SensorGroup {

    /**
     * Nobody can build an anonymous object
     */
    private SensorGroup() {
    }

    private final static String namePrefix  = "SENSOR GROUP:";  // should be upper case
    private final static String nameDivider = ":";
        
    String name;
    ArrayList sensorList;
    
    /**
     * Create one, looking up an existing one if present
     */
    void SensorGroup(String name) {
        this.name = name;
        // find suitable 
        RouteManager rm = InstanceManager.routeManagerInstance();
        String group = name.toUpperCase();
        List l = rm.getSystemNameList();
        String prefix = (namePrefix+group+nameDivider).toUpperCase();
        
        sensorList = new ArrayList();
        for (int i = 0; i<l.size(); i++) {
            String routeName = (String) l.get(i);
            if (routeName.startsWith(prefix)) {
                String sensor = routeName.substring(prefix.length());
                // remember that sensor
                sensorList.add(sensor);
            }
        }  
    }

    void addPressed() {
        log.debug("start with "+sensorList.size()+" lines");
        RouteManager rm = InstanceManager.routeManagerInstance();
        String group = name.toUpperCase();
        
        // remove the old routes
        List l = rm.getSystemNameList();     
        String prefix = (namePrefix+group+nameDivider).toUpperCase();
        
        for (int i = 0; i<l.size(); i++) {
            String routeName = (String) l.get(i);
            if (routeName.startsWith(prefix)) {
                // OK, kill this one
                Route r = rm.getBySystemName((String)l.get(i));
                r.deActivateRoute();
                rm.deleteRoute(r);
            }
        }        

        // add the new routes
        for (int i = 0; i<sensorList.size(); i++) {
            String sensor = (String) sensorList.get(i);
            String routeName = namePrefix+group+nameDivider+sensor;
            Route r = new DefaultRoute(routeName);
            // add the control sensor
            r.addSensorToRoute(sensor, Route.ONACTIVE);
            // add the output sensors
            for (int j=0; j<sensorList.size(); j++) {
                String outSensor = (String) sensorList.get(j);
                int mode = Sensor.INACTIVE;
                if (i==j) mode = Sensor.ACTIVE;
                r.addOutputSensor(outSensor, mode);
            }
            // make it persistant & activate
            r.activateRoute();
            rm.register(r);
        }
    }

    
    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(SensorGroup.class.getName());

}
