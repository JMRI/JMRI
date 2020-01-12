package jmri.managers;

import jmri.*;
import org.junit.Assert;
import org.junit.Test;

/**
 * Base for the various Abstract*MgrTestBase base classes for NamedBean Manager test classes
 * <p>
 * This is not itself a test class, e.g. should not be added to a suite.
 * Instead, this forms the base for test classes, including providing some
 * common tests.
 * <p>
 * Quite a bit of AbstractManager testing is done in InternalSensorManagerTest
 * to give it a concrete platform
 *
 * @author Bob Jacobsen Copyright (C) 2017	
 */
public abstract class AbstractProxyManagerTestBase<T extends Manager<E>, E extends NamedBean> {

    // ProxyManager<E> under test - setUp() loads this
    protected T l = null;

    // check that you can add and remove listeners
    @Test
    public void checkSimpleAddAndRemove() {
        
        Manager.ManagerDataListener<E> listener = new Manager.ManagerDataListener<E>(){
            @Override public void contentsChanged(Manager.ManagerDataEvent<E> e){}
            @Override public void intervalAdded(Manager.ManagerDataEvent<E> e) {}
            @Override public void intervalRemoved(Manager.ManagerDataEvent<E> e) {}
        };
        
        l.addDataListener(listener);
        l.removeDataListener(listener);
        
        l.addDataListener(null);
        l.removeDataListener(null);
        
        l.addDataListener(null);
        l.removeDataListener(listener);
        
        l.addDataListener(listener);
        l.removeDataListener(null);
        
    }

    @Test
    public final void testMakeSystemName() {
        String s = l.makeSystemName("1");
        Assert.assertTrue(s != null);
        Assert.assertTrue(! s.isEmpty());
    }
}
