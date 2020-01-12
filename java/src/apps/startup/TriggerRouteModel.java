package apps.startup;

import java.util.Locale;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Route;
import jmri.RouteManager;
import jmri.util.prefs.InitializationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Startup model that stores the user name of a {@link jmri.Route} so it can be
 * set at application startup.
 *
 * @author Randall Wood (C) 2016
 */
public class TriggerRouteModel extends AbstractStartupModel {

    private final static Logger log = LoggerFactory.getLogger(TriggerRouteModel.class);

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

    @Override
    public void performAction() throws JmriException {
        log.info("Setting route \"{}\" at startup.", this.getUserName());

        try {
            this.getRoute().setRoute();
        } catch (NullPointerException ex) {
            log.error("Unable to set route \"{}\"; it has not been defined. Is its panel loaded?", this.getUserName());
            // it would be better to use a RouteNotFoundException if one existed
            InitializationException exception = new InitializationException(Bundle.getMessage(Locale.ENGLISH, "TriggerRouteModel.RouteNotDefined", this.getUserName()),
                    Bundle.getMessage("TriggerRouteModel.RouteNotDefined", this.getUserName()), ex);
            this.addException(exception);
            throw new JmriException(exception);
        }

    }
}
