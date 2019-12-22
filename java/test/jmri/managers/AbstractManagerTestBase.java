package jmri.managers;

import jmri.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;

import org.junit.Assert;
import org.junit.Test;

/**
 * Base for the various Abstract*MgrTestBase base classes for NamedBean Manager
 * test classes
 * <p>
 * This is not itself a test class, e.g. should not be added to a suite.
 * Instead, this forms the base for test classes, including providing some
 * common tests.
 * <p>
 * Quite a bit of AbstractManager testing is done in InternalSensorManagerTest
 * to give it a concrete platform
 *
 * @author Bob Jacobsen Copyright (C) 2017
 * @param <T> the class being tested
 * @param <E> the class of NamedBean handled by T
 */
public abstract class AbstractManagerTestBase<T extends Manager<E>, E extends NamedBean> {

    // Manager<E> under test - setUp() loads this
    protected T l = null;

    // check that you can add and remove listeners
    @Test
    public final void testManagerDataListenerAddAndRemove() {

        Manager.ManagerDataListener<E> listener = new Manager.ManagerDataListener<E>() {
            @Override
            public void contentsChanged(Manager.ManagerDataEvent<E> e) {
            }

            @Override
            public void intervalAdded(Manager.ManagerDataEvent<E> e) {
            }

            @Override
            public void intervalRemoved(Manager.ManagerDataEvent<E> e) {
            }
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
    public final void testPropertyChangeListenerAddAndRemove() {

        int base = l.getPropertyChangeListeners().length;

        PropertyChangeListener listener = new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                // do nothing
            }
        };

        Assert.assertEquals(base, l.getPropertyChangeListeners().length);
        l.addPropertyChangeListener(listener);
        Assert.assertEquals(base + 1, l.getPropertyChangeListeners().length);
        l.removePropertyChangeListener(listener);
        Assert.assertEquals(base, l.getPropertyChangeListeners().length);

        Assert.assertEquals(base, l.getPropertyChangeListeners().length);
        l.addPropertyChangeListener("property", listener);
        Assert.assertEquals(base + 1, l.getPropertyChangeListeners().length);
        Assert.assertEquals(1, l.getPropertyChangeListeners("property").length);
        l.removePropertyChangeListener("property", listener);
        Assert.assertEquals(base, l.getPropertyChangeListeners().length);

    }

    @Test
    public final void testVetoableChangeListenerAddAndRemove() {

        int base = l.getVetoableChangeListeners().length;

        VetoableChangeListener listener = new VetoableChangeListener() {

            @Override
            public void vetoableChange(PropertyChangeEvent evt) throws PropertyVetoException {
                // do nothing
            }
        };

        Assert.assertEquals(base, l.getVetoableChangeListeners().length);
        l.addVetoableChangeListener(listener);
        Assert.assertEquals(base + 1, l.getVetoableChangeListeners().length);
        l.removeVetoableChangeListener(listener);
        Assert.assertEquals(base, l.getVetoableChangeListeners().length);

        Assert.assertEquals(base, l.getVetoableChangeListeners().length);
        l.addVetoableChangeListener("property", listener);
        Assert.assertEquals(base + 1, l.getVetoableChangeListeners().length);
        Assert.assertEquals(1, l.getVetoableChangeListeners("property").length);
        l.removeVetoableChangeListener("property", listener);
        Assert.assertEquals(base, l.getVetoableChangeListeners().length);

    }

    @Test
    public void testMakeSystemName() {
        String s = l.makeSystemName("1");
        Assert.assertNotNull(s);
        Assert.assertFalse(s.isEmpty());
    }

    @Test
    public void testAutoSystemNames() {
        AbstractManager<E> m = (AbstractManager<E>) l;
        String sysPrefix = l.getSystemNamePrefix();
        Assert.assertEquals(sysPrefix + ":AUTO:0001", m.getAutoSystemName());
        Assert.assertEquals(sysPrefix + ":AUTO:0002", m.getAutoSystemName());
        Assert.assertEquals(sysPrefix + ":AUTO:0003", m.getAutoSystemName());
        m.updateAutoNumber(sysPrefix + ":AUTO:0011");
        Assert.assertEquals(sysPrefix + ":AUTO:0012", m.getAutoSystemName());
        Assert.assertEquals(sysPrefix + ":AUTO:0013", m.getAutoSystemName());
        m.updateAutoNumber(sysPrefix + ":AUTO:0005");
        Assert.assertEquals(sysPrefix + ":AUTO:0014", m.getAutoSystemName());
        m.updateAutoNumber(sysPrefix + ":AUTO:0098");
        Assert.assertEquals(sysPrefix + ":AUTO:0099", m.getAutoSystemName());
        m.updateAutoNumber(sysPrefix + ":AUTO:0097");
        Assert.assertEquals(sysPrefix + ":AUTO:0100", m.getAutoSystemName());
        Assert.assertEquals(sysPrefix + ":AUTO:0101", m.getAutoSystemName());
        m.updateAutoNumber(sysPrefix + ":AUT:0203");
        Assert.assertEquals(sysPrefix + ":AUTO:0102", m.getAutoSystemName());
        m.updateAutoNumber("12" + ":AUT:0203"); // Bad system name prefix
        Assert.assertEquals(sysPrefix + ":AUTO:0103", m.getAutoSystemName());
    }

    // private final static Logger log = LoggerFactory.getLogger(AbstractManagerTestBase.class);
}
