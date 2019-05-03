package jmri.managers;

import jmri.*;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.beans.VetoableChangeListener;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import jmri.util.JUnitAppender;
import org.apache.log4j.Level;

import org.junit.Assume;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * @param <T> the class being tested
 * @param <E> the class of NamedBean handled by T
 */
public abstract class AbstractManagerTestBase<T extends Manager<E>, E extends NamedBean> {

    // Manager<E> under test - setUp() loads this
    protected T l = null;

    // check that you can add and remove listeners
    @Test
    public final void testManagerDataListenerAddAndRemove() {
        
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

    protected Field getField(Class c, String fieldName) {
        try {
            return c.getDeclaredField(fieldName);
        } catch (NoSuchFieldException ex) {
            if (c.getSuperclass() != null)
                return getField(c.getSuperclass(), fieldName);
        }

        // Field not found
        return null;
    }

    @Test
    public void testRegisterDuplicateSystemName() throws PropertyVetoException, NoSuchFieldException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        if (l instanceof ProvidingManager)  {
            ProvidingManager<E> m = (ProvidingManager<E>) l;
            String s1 = l.makeSystemName("1");
            String s2 = l.makeSystemName("2");
            Assert.assertNotNull(s1);
            Assert.assertFalse(s1.isEmpty());
            Assert.assertNotNull(s2);
            Assert.assertFalse(s2.isEmpty());

            E e1;
            E e2;

            try {
                e1 = m.provide(s1);
                e2 = m.provide(s2);
            } catch (IllegalArgumentException | NullPointerException | ArrayIndexOutOfBoundsException ex) {
                // jmri.jmrix.openlcb.OlcbLightManagerTest gives a NullPointerException here.
                // jmri.jmrix.openlcb.OlcbSensorManagerTest gives a ArrayIndexOutOfBoundsException here.
                // Some other tests give an IllegalArgumentException here.

                // If the test is unable to provide a named bean, abort this test.
                JUnitAppender.clearBacklog(Level.WARN);
                log.debug("Cannot provide a named bean", ex);
                Assume.assumeTrue("We got no exception", false);
                return;
            }

            // Use reflection to change the systemName of e2
            // Try to find the field
            Field f1 = getField(e2.getClass(), "mSystemName");
            f1.setAccessible(true);
            f1.set(e2, e1.getSystemName());

            // Remove bean if it's already registered
            if (l.getBeanBySystemName(e1.getSystemName()) != null) {
                l.deregister(e1);
            }
            // Remove bean if it's already registered
            if (l.getBeanBySystemName(e2.getSystemName()) != null) {
                l.deregister(e2);
            }

            // Register the bean once. This should be OK.
            l.register(e1);

            // Register bean twice. This gives only a debug message.
            l.register(e1);

            String expectedMessage = "systemName is already registered: " + e1.getSystemName();
            boolean hasException = false;
            try {
                // Register different bean with existing systemName.
                // This should fail with an IllegalArgumentException.
                l.register(e2);
            } catch (IllegalArgumentException ex) {
                hasException = true;
                Assert.assertTrue("exception message is correct",
                        expectedMessage.equals(ex.getMessage()));
                JUnitAppender.assertErrorMessage(expectedMessage);
            }
            Assert.assertTrue("exception is thrown", hasException);

            l.deregister(e1);
        }
    }

    private final static Logger log = LoggerFactory.getLogger(AbstractManagerTestBase.class);
}
