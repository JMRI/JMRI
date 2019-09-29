package jmri.util.managers;

import jmri.InstanceManager;
import jmri.Route;
import jmri.jmrix.internal.InternalSystemConnectionMemo;
import jmri.managers.DefaultRouteManager;

/**
 * This manager always throws an IllegalArgumentException from provide and get methods.
 * It is used for testing exception handling in tests.
 * 
 * The class name ends with 'Scaffold' to exclude it from the coverage statistics,
 * since it is part of the testing infrastructure.
 */
public class RouteManagerThrowExceptionScaffold extends DefaultRouteManager {

    public RouteManagerThrowExceptionScaffold() {
        super(InstanceManager.getDefault(InternalSystemConnectionMemo.class));
    }
    
    /** {@inheritDoc} */
    @Override
    public Route provideRoute(String systemName, String userName) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
    /** {@inheritDoc} */
    @Override
    public Route getRoute(String name) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
    /** {@inheritDoc} */
    @Override
    public Route getBySystemName(String name) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
    /** {@inheritDoc} */
    @Override
    public Route getByUserName(String key) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
    /** {@inheritDoc} */
    @Override
    public Route newRoute(String userName) {
        throw new IllegalArgumentException("Illegal argument");
    }
    
}
