package jmri.jmrit.withrottle;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.Route;
import jmri.RouteManager;
import jmri.Sensor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 * @author Brett Hoffman Copyright (C) 2010
 */
public class RouteController extends AbstractController implements PropertyChangeListener {

    private RouteManager manager = null;
    private HashMap<NamedBeanHandle<Sensor>, Route> indication;    //  Monitor turnouts for aligned status

    public RouteController() {
        manager = InstanceManager.getNullableDefault(jmri.RouteManager.class);
        if (manager == null) {
            log.info("No route manager instance.");
            isValid = false;
        } else {
            indication = new HashMap<>();
            isValid = true;
        }
    }

    @Override
    boolean verifyCreation() {

        return isValid;
    }

    @Override
    public void filterList() {
        ArrayList<String> tempList = new ArrayList<>(0);
        for (String sysName : sysNameList) {
            Route r = manager.getBySystemName(sysName);
            if (r != null) {
                Object o = r.getProperty("WifiControllable");
                if (o == null || Boolean.getBoolean(o.toString())) {
                    //  Only skip if 'false'
                    tempList.add(sysName);
                }
            }
        }
        sysNameList = tempList;
    }

    @Override
    void handleMessage(String message) {
        try {
            if (message.charAt(0) == 'A') {
                if (message.charAt(1) == '2') {
                    Route r = manager.getBySystemName(message.substring(2));
                    if (r != null) {
                        r.setRoute();
                    } else {
                        log.warn("Message \"{}\" contained invalid system name.", message);
                    }
                } else {
                    log.warn("Message \"{}\" does not match a route.", message);
                }
            }
        } catch (NullPointerException exb) {
            log.warn("Message \"{}\" does not match a route.", message);
        }
    }

    /**
     * Send Info on routes to devices, not specific to any one route.
     * <p>
     * Format: PRT]\[value}|{routeKey]\[value}|{ActiveKey]\[value}|{InactiveKey
     */
    public void sendTitles() {
        if (listeners == null) {
            return;
        }

        StringBuilder labels = new StringBuilder("PRT");    //  Panel Turnout Titles

        labels.append("]\\[").append(Bundle.getMessage("MenuItemRouteTable")).append("}|{Route"); // should Route be translated?
        labels.append("]\\[").append("Active").append("}|{2"); // should Active be translated?
        labels.append("]\\[").append("Inactive").append("}|{4"); // should Inctive be translated?

        String message = labels.toString();

        for (ControllerInterface listener : listeners) {
            listener.sendPacketToDevice(message);
        }

    }

    protected jmri.NamedBeanHandleManager nbhm = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class);

    /**
     * Send list of routes Format:
     * PRL]\[SysName}|{UsrName}|{CurrentState]\[SysName}|{UsrName}|{CurrentState
     * <p>
     * States: 1 - UNKNOWN, 2 - ACTIVE, 4 - INACTIVE (based on turnoutsAligned
     * sensor, if used)
     */
    public void sendList() {
        if (listeners == null) {
            return;
        }
        if (canBuildList) {
            buildList(manager);
        }
        if (sysNameList.isEmpty()) {
            return;
        }

        StringBuilder list = new StringBuilder("PRL");  //  Panel Route List

        for (String sysName : sysNameList) {
            Route r = manager.getBySystemName(sysName);
            if (r != null) {
                list.append("]\\[").append(sysName);
                list.append("}|{");
                if (r.getUserName() != null) {
                    list.append(r.getUserName());
                }
                list.append("}|{");
                String turnoutsAlignedSensor = r.getTurnoutsAlignedSensor();
                if (!turnoutsAlignedSensor.equals("")) {  //only set if found
                    try {
                        Sensor routeAligned = InstanceManager.sensorManagerInstance().provideSensor(turnoutsAlignedSensor);
                        list.append(routeAligned.getKnownState());
                    } catch (IllegalArgumentException ex) {
                        log.warn("Failed to provide turnoutsAlignedSensor \"{}\" in sendList", turnoutsAlignedSensor);
                    }
                }
            }
        }
        String message = list.toString();

        for (ControllerInterface listener : listeners) {
            listener.sendPacketToDevice(message);
        }
    }

    /**
     * This is on the aligned sensor, not the route itself.
     */
    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("KnownState")) {
            Sensor s = (Sensor) evt.getSource();
            for (NamedBeanHandle<Sensor> namedSensor : indication.keySet()) {
                if (namedSensor.getBean() == s) {
                    Route r = indication.get(namedSensor);
                    String message = "PRA" + s.getKnownState() + r.getSystemName();

                    for (ControllerInterface listener : listeners) {
                        listener.sendPacketToDevice(message);
                    }
                    return;
                }
            }
        }
    }

    /**
     * Register this as a listener of each managed route's aligned sensor
     */
    @Override
    public void register() {
        for (String sysName : sysNameList) {
            Route r = manager.getBySystemName(sysName);
            if (r != null) {
                String turnoutsAlignedSensor = r.getTurnoutsAlignedSensor();
                if (!turnoutsAlignedSensor.equals("")) {  //only set if found
                    Sensor sensor = InstanceManager.sensorManagerInstance().provideSensor(turnoutsAlignedSensor);
                    NamedBeanHandle<Sensor> routeAligned = nbhm.getNamedBeanHandle(turnoutsAlignedSensor, sensor);
                    indication.put(routeAligned, r);
                    sensor.addPropertyChangeListener(this, routeAligned.getName(), "Wi Throttle Route Controller");
                    log.debug("Add listener to Sensor: {} for Route: {}", routeAligned.getName(), r.getSystemName());
                }
            }
        }
    }

    /**
     * Remove this from each managed route's aligned sensor.
     */
    @Override
    public void deregister() {
        if (sysNameList.isEmpty()) {
            return;
        }

        indication.keySet().forEach((namedSensor) -> {
            namedSensor.getBean().removePropertyChangeListener(this);
            if (log.isDebugEnabled()) {
                log.debug("Removing listener from Sensor: {} for Route: {}", namedSensor.getName(), indication.get(namedSensor).getSystemName());
            }
        });
        indication = new HashMap<>();
    }

    private final static Logger log = LoggerFactory.getLogger(RouteController.class);
}
