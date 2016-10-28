package apps.startup;

import jmri.InstanceManager;
import jmri.Route;
import jmri.RouteManager;

/**
 * Startup model that stores the user name of a {@link jmri.Route} so it can be
 * set at application startup.
 *
 * @author Randall Wood (C) 2016
 */
public class TriggerRouteModel extends AbstractStartupModel {

    /**
     * Get the user name of the Route.
     *
     * @return the user name
     */
    public String getUserName() {
        return this.getName();
    }

    /**
     * Set the user name of the Route.
     *
     * @param name user name to use
     */
    public void setUserName(String name) {
        this.setName(name);
    }

    /**
     * Get the route.
     *
     * @return the route
     */
    public Route getRoute() {
        return InstanceManager.getDefault(RouteManager.class).getByUserName(this.getUserName());
    }
}
