// DefaultRouteManager.java

package jmri;

/**
 * Basic Implementation of a RouteManager.
 * <P>
 * Note that this does not enforce any particular system naming convention
 *
 * @author      Dave Duchamp Copyright (C) 2004
 * @version	$Revision: 1.1 $
 */
public class DefaultRouteManager extends AbstractManager
    implements RouteManager, java.beans.PropertyChangeListener {

    public DefaultRouteManager() {
        super();
    }

    public char systemLetter() { return 'I'; }
    public char typeLetter() { return 'R'; }
    
    /**
     * Method to create a new Route if the route does not exist
     *   Returns null if a Route with the same systemName or userName
     *       already exists, or if there is trouble creating a new Route.
     */
    public Route createNewRoute(String systemName, String userName) {
        // Check that Route does not already exist
        Route r = getByUserName(userName);
        if (r!=null) return null;
        r = getBySystemName(systemName);
        if (r!=null) return null;
        // Route does not exist, create a new route
        r = new DefaultRoute(systemName,userName);
        if (r!=null) {
            // save in the maps
            register(r);
        }
        return r;
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

    public Route getBySystemName(String key) {
        return (Route)_tsys.get(key);
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

    static org.apache.log4j.Category log = org.apache.log4j.Category.getInstance(DefaultRouteManager.class.getName());
}

/* @(#)DefaultRouteManager.java */
