package jmri.managers;

import jmri.InstanceManager;
import jmri.Manager;
import jmri.Route;
import jmri.RouteManager;
import jmri.implementation.DefaultRoute;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Basic Implementation of a RouteManager.
 * <p>
 * Note that this does not enforce any particular system naming convention
 *
 * @author Dave Duchamp Copyright (C) 2004
 */
public class DefaultRouteManager extends AbstractManager<Route>
        implements RouteManager {

    public DefaultRouteManager(InternalSystemConnectionMemo memo) {
        super(memo);
        jmri.InstanceManager.turnoutManagerInstance().addVetoableChangeListener(this);
        jmri.InstanceManager.sensorManagerInstance().addVetoableChangeListener(this);
    }

    @Override
    public int getXMLOrder() {
        return Manager.ROUTES;
    }

    @Override
    public char typeLetter() {
        return 'R';
    }

    /**
     * {@inheritDoc}
     *
     * Keep autostring in line with {@link #newRoute(String)},
     * {@link #getSystemPrefix()} and {@link #typeLetter()}
     */
    @Override
    public Route provideRoute(String systemName, String userName) {
        log.debug("provideRoute({})", systemName);
        Route r;
        r = getByUserName(systemName);
        if (r != null) {
            return r;
        }
        r = getBySystemName(systemName);
        if (r != null) {
            return r;
        }
        // Route does not exist, create a new route
        r = new DefaultRoute(validateSystemNameFormat(systemName), userName);
        // save in the maps
        register(r);

        // Keep track of the last created auto system name
        updateAutoNumber(systemName);

        return r;
    }

    /**
     * {@inheritDoc}
     *
     * Keep autostring in line with {@link #provideRoute(String, String)},
     * {@link #getSystemPrefix()} and {@link #typeLetter()}
     */
    @Override
    public Route newRoute(String userName) {
        return provideRoute(getAutoSystemName(), userName);
    }

    /**
     * Remove an existing route. Route must have been deactivated before
     * invoking this.
     */
    @Override
    public void deleteRoute(Route r) {
        deregister(r);
    }

    /**
     * Method to get an existing Route. First looks up assuming that name is a
     * User Name. If this fails looks up assuming that name is a System Name. If
     * both fail, returns null.
     */
    @Override
    public Route getRoute(String name) {
        Route r = getByUserName(name);
        if (r != null) {
            return r;
        }
        return getBySystemName(name);
    }

    @Override
    public Route getBySystemName(String name) {
        return _tsys.get(name);
    }

    @Override
    public Route getByUserName(String key) {
        return _tuser.get(key);
    }

    /**
     * 
     * @return the default instance of DefaultRouteManager
     * @deprecated since 4.17.3; use {@link jmri.InstanceManager#getDefault(java.lang.Class)} instead
     */
    @Deprecated
    public static DefaultRouteManager instance() {
        return InstanceManager.getDefault(DefaultRouteManager.class);
    }

    @Override
    public String getBeanTypeHandled(boolean plural) {
        return Bundle.getMessage(plural ? "BeanNameRoutes" : "BeanNameRoute");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<Route> getNamedBeanClass() {
        return Route.class;
    }

    @Override
    public Route provide(String name) {
        return provideRoute(name, null);
    }

    private static final Logger log = LoggerFactory.getLogger(DefaultRouteManager.class);

}
