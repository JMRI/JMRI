package jmri.managers;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.*;
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
public class DefaultRouteManager extends AbstractManager<Route> implements RouteManager {

    public DefaultRouteManager(InternalSystemConnectionMemo memo) {
        super(memo);
        addListeners();
    }
    
    final void addListeners(){
        InstanceManager.getDefault(TurnoutManager.class).addVetoableChangeListener(this);
        InstanceManager.getDefault(SensorManager.class).addVetoableChangeListener(this);
    }

    @Override
    public int getXMLOrder() {
        return Manager.ROUTES;
    }

    @Override
    public char typeLetter() {
        return 'O';
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    public Route provideRoute(@Nonnull String systemName, @CheckForNull String userName) throws IllegalArgumentException {
        log.debug("provideRoute({})", systemName);
        Route r;
        if (userName!=null){
            r = getByUserName(userName);
            if (r != null) {
                return r;
            }
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
     * <p>
     * Calls {@link #provideRoute(String, String)} with the result of
     * {@link #getAutoSystemName()} as the system name.
     */
    @Override
    @Nonnull
    public Route newRoute(@Nonnull String userName) throws IllegalArgumentException {
        return provideRoute(getAutoSystemName(), userName);
    }

    /**
     * Remove an existing route. Route must have been deactivated before
     * invoking this.
     */
    @Override
    public void deleteRoute(@Nonnull Route r) {
        deregister(r);
    }

    /**
     * Method to get an existing Route.
     * First looks up assuming that name is a User Name.
     * If this fails looks up assuming that name is a System Name.
     * @return If both fail, returns null.
     */
    @Override
    @CheckForNull
    public Route getRoute(@Nonnull String name) {
        Route r = getByUserName(name);
        return (r != null ? r : getBySystemName(name) );
    }

    @Nonnull
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

    /**
     * Provide Route by System Name.
     * @param name System Name f Route.
     * @return new or existing Route with corresponding System Name.
     */
    @Override
    @Nonnull
    public Route provide(@Nonnull String name) throws IllegalArgumentException {
        return provideRoute(name, null);
    }
    
    @Override
    public void dispose(){
        InstanceManager.getDefault(TurnoutManager.class).removeVetoableChangeListener(this);
        InstanceManager.getDefault(SensorManager.class).removeVetoableChangeListener(this);
        super.dispose();
    }

    private static final Logger log = LoggerFactory.getLogger(DefaultRouteManager.class);

}
