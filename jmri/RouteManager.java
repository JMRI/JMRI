// RouteManager.java

package jmri;

import com.sun.java.util.collections.List;

/**
 * Interface for obtaining Routes
 * <P>
 * This doesn't have a "new" method, since Routes are 
 * separately implemented, instead of being system-specific.
 *
 * @author      Dave Duchamp Copyright (C) 2004
 * @version	$Revision: 1.1 $
 */
public interface RouteManager extends Manager {

    // to free resources when no longer used
    public void dispose();
    
    /**
     * Method to create a new Route if the route does not exist
     *   Returns null if a Route with the same systemName or userName
     *       already exists, or if there is trouble creating a new Route.
     */
    public Route createNewRoute(String systemName, String userName);

    /**
     * Locate via user name, then system name if needed.
     * Does not create a new one if nothing found
     *
     * @param name
     * @return null if no match found
     */
    public Route getRoute(String name);

    public Route getByUserName(String s);
    public Route getBySystemName(String s);
    
    /**
     * Get a list of all Route system names.
     */
    public List getSystemNameList();

}


/* @(#)RouteManager.java */
