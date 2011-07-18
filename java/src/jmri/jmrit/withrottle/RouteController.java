package jmri.jmrit.withrottle;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.ResourceBundle;
import jmri.InstanceManager;
import jmri.Route;
import jmri.RouteManager;
import jmri.Sensor;

/**
 *
 *
 *	@author Brett Hoffman   Copyright (C) 2010
 *	@version $Revision$
 */

public class RouteController extends AbstractController implements PropertyChangeListener{

    private RouteManager manager = null;
    private Hashtable<Sensor, Route> indication;    //  Monitor turnouts for aligned status

    public RouteController(){
        manager = InstanceManager.routeManagerInstance();
        if (manager == null){
            log.info("No route manager instance.");
            isValid = false;
        }else {
            indication = new Hashtable<Sensor, Route>();
            isValid = true;
        }
    }


    boolean verifyCreation() {

        return isValid;
    }


    public void filterList(){
        ArrayList<String> tempList = new ArrayList<String>(0);
        for (String sysName : sysNameList){
            Route r = manager.getBySystemName(sysName);
            Object o = r.getProperty("WifiControllable");
            if ((o == null) || (!o.toString().equalsIgnoreCase("false"))){
                //  Only skip if 'false'
                tempList.add(sysName);
            }
        }
        sysNameList = tempList;
    }


    void handleMessage(String message) {
        try{
            if (message.charAt(0) == 'A'){
                if (message.charAt(1) == '2'){
                        manager.getBySystemName(message.substring(2)).setRoute();
                }else log.warn("Message \""+message+"\" does not match a route.");
            }
        }catch (NullPointerException exb){
            log.warn("Message \""+message+"\" does not match a route.");
        }
    }


/**
 *  Send Info on routes to devices, not specific to any one route.
 *
 *  Format:  PRT]\[value}|{routeKey]\[value}|{ActiveKey]\[value}|{InactiveKey
 */
    public void sendTitles(){
        if (listeners == null) return;


        StringBuilder labels = new StringBuilder("PRT");    //  Panel Turnout Titles

        labels.append("]\\[" + ResourceBundle.getBundle("jmri.jmrit.JmritToolsBundle").getString("MenuItemRouteTable") + "}|{Route");
        labels.append("]\\[" + "Active" + "}|{2");
        labels.append("]\\[" + "Inactive" + "}|{4");

        String message = labels.toString();

        for (ControllerInterface listener : listeners){
            listener.sendPacketToDevice(message);
        }

    }

/**
 *  Send list of routes
 *  Format:  PRL]\[SysName}|{UsrName}|{CurrentState]\[SysName}|{UsrName}|{CurrentState
 *
 *  States:  1 - UNKNOWN, 2 - ACTIVE, 4 - INACTIVE (based on turnoutsAligned sensor, if used)
 */
    public void sendList(){
        if (listeners == null) return;
        if (canBuildList){
            buildList(manager);
        }
        if (sysNameList.isEmpty()) return;
        
        StringBuilder list = new StringBuilder("PRL");  //  Panel Route List

        for (String sysName : sysNameList){
            Route r = manager.getBySystemName(sysName);
            list.append("]\\[" + sysName);
            list.append("}|{");
            if (r.getUserName() != null) list.append(r.getUserName());
            list.append("}|{");
            Sensor routeAligned = InstanceManager.sensorManagerInstance().getBySystemName(r.getTurnoutsAlignedSensor());
            if (routeAligned != null){
                list.append(routeAligned.getKnownState());
            }
            

        }
        String message = list.toString();

        for (ControllerInterface listener : listeners){
            listener.sendPacketToDevice(message);
        }
    }


/**
 *  This is on the aligned sensor, not the route itself.
 * @param evt
 */
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("KnownState")) {
            Sensor s = (Sensor)evt.getSource();
            Route r = indication.get(s);
            String message;

            message = "PRA" + s.getKnownState() + r.getSystemName();

            for (ControllerInterface listener : listeners){
                listener.sendPacketToDevice(message);
            }
        }
    }
/**
 * Register this as a listener of each managed route's aligned sensor
 */
    public void register(){
        for (String sysName : sysNameList){
            Route r = manager.getBySystemName(sysName);
            Sensor routeAligned = InstanceManager.sensorManagerInstance().getBySystemName(r.getTurnoutsAlignedSensor());
            if (routeAligned != null){
                indication.put(routeAligned, r);
                routeAligned.addPropertyChangeListener(this);
                if (log.isDebugEnabled()) log.debug("Add listener to Sensor: "+routeAligned.getSystemName()+" for Route: "+r.getSystemName());
            }


        }
    }

/**
 * Remove this from each managed route's aligned sensor.
 */
    public void deregister(){
        if (sysNameList.isEmpty()) return;

        for (String sysName : sysNameList){
            Route r = manager.getBySystemName(sysName);
            Sensor routeAligned = InstanceManager.sensorManagerInstance().getBySystemName(r.getTurnoutsAlignedSensor());
            if (routeAligned != null){
                routeAligned.removePropertyChangeListener(this);
                indication.remove(routeAligned);
                if (log.isDebugEnabled()) log.debug("Removing listener from Sensor: "+routeAligned.getSystemName()+" for Route: "+r.getSystemName());
            }

        }
    }


    static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(RouteController.class.getName());
}