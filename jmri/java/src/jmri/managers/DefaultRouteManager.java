// DefaultRouteManager.java

package jmri.managers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jmri.*;
import jmri.managers.AbstractManager;
import jmri.implementation.DefaultRoute;
import java.text.DecimalFormat;

/**
 * Basic Implementation of a RouteManager.
 * <P>
 * Note that this does not enforce any particular system naming convention
 *
 * @author      Dave Duchamp Copyright (C) 2004
 * @version	$Revision$
 */
public class DefaultRouteManager extends AbstractManager
    implements RouteManager, java.beans.PropertyChangeListener, java.beans.VetoableChangeListener {

    public DefaultRouteManager() {
        super();
        jmri.InstanceManager.turnoutManagerInstance().addVetoableChangeListener(this);
        jmri.InstanceManager.sensorManagerInstance().addVetoableChangeListener(this);
    }
    
    public int getXMLOrder(){
        return Manager.ROUTES;
    }

    public String getSystemPrefix() { return "I"; }
    public char typeLetter() { return 'R'; }
    
    /**
     * Method to provide a  Route 
     * whether or not is already exists.
     */
    public Route provideRoute(String systemName, String userName) {
        Route r;
        r = getByUserName(systemName);
        if (r!=null) return r;
        r = getBySystemName(systemName);
        if (r!=null) return r;
        // Route does not exist, create a new route
		r = new DefaultRoute(systemName,userName);
		// save in the maps
		register(r);
        /*The following keeps trace of the last created auto system name.  
        currently we do not reuse numbers, although there is nothing to stop the 
        user from manually recreating them*/
        if (systemName.startsWith("IR:AUTO:")){
            try {
                int autoNumber = Integer.parseInt(systemName.substring(8));
                if (autoNumber > lastAutoRouteRef) {
                    lastAutoRouteRef = autoNumber;
                } 
            } catch (NumberFormatException e){
                log.warn("Auto generated SystemName "+ systemName + " is not in the correct format");
            }
        }
		return r;
    }
    
    public Route newRoute(String userName) {
        int nextAutoRouteRef = lastAutoRouteRef+1;
        StringBuilder b = new StringBuilder("IR:AUTO:");
        String nextNumber = paddedNumber.format(nextAutoRouteRef);
        b.append(nextNumber);
        return provideRoute(b.toString(), userName);
    }
    
    DecimalFormat paddedNumber = new DecimalFormat("0000");

    int lastAutoRouteRef = 0;

    /**
     * Remove an existing route. Route must have been deactivated
     * before invoking this.
     */
    public void deleteRoute(Route r) {
        deregister(r);
    }
    
    /** 
     * Method to get an existing Route.  First looks up assuming that
     *      name is a User Name.  If this fails looks up assuming
     *      that name is a System Name.  If both fail, returns null.
     */
    public Route getRoute(String name) {
        Route r = getByUserName(name);
        if (r!=null) return r;
        return getBySystemName(name);
    }

    public Route getBySystemName(String name) {
        return (Route)_tsys.get(name);
    }

    public Route getByUserName(String key) {
        return (Route)_tuser.get(key);
    }
    
    static DefaultRouteManager _instance = null;
    static public DefaultRouteManager instance() {
        if (_instance == null) {
            _instance = new DefaultRouteManager();
        }
        return (_instance);
    }
    
    public String getBeanTypeHandled(){
        return Bundle.getMessage("BeanNameRoute");
    }

    static Logger log = LoggerFactory.getLogger(DefaultRouteManager.class.getName());
}

/* @(#)DefaultRouteManager.java */
