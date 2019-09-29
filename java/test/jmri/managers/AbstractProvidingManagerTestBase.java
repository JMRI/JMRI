package jmri.managers;

import jmri.*;

import java.beans.PropertyVetoException;
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
 * Extension of AbstractManagerTestBase base for ProvidingManager test classes.
 * <p>
 * This is not itself a test class, e.g. should not be added to a suite.
 * Instead, this forms the base for test classes, including providing some
 * common tests.
 *
 * @author Paul Bender Copyright (C) 2019
 * @param <T> the class being tested
 * @param <E> the class of NamedBean handled by T
 */
public abstract class AbstractProvidingManagerTestBase<T extends ProvidingManager<E>, E extends NamedBean> extends AbstractManagerTestBase<T, E> {

    @Test(expected = IllegalArgumentException.class)
    public void testProvideEmpty() throws IllegalArgumentException {
        ProvidingManager<E> m = l;
        try {
            m.provide(""); // this should throw an IllegalArgumentException.
        } catch (IllegalArgumentException iae) {
            JUnitAppender.suppressErrorMessageStartsWith("Invalid system name for");
            throw iae; // rethrow the expected exception, after suppressing the error
        }
    }

    @Test
    public void testRegisterDuplicateSystemName() throws PropertyVetoException, NoSuchFieldException,
            NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        ProvidingManager<E> m = l;
        String s1 = l.makeSystemName("1");
        String s2 = l.makeSystemName("2");
        testRegisterDuplicateSystemName(m, s1, s2);
    }

    public void testRegisterDuplicateSystemName(ProvidingManager<E> m, String s1, String s2)
            throws PropertyVetoException, NoSuchFieldException,
            NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Assert.assertNotNull(s1);
        Assert.assertFalse(s1.isEmpty());
        Assert.assertNotNull(s2);
        Assert.assertFalse(s2.isEmpty());

        E e1;
        E e2;

        try {
            e1 = m.provide(s1);
            e2 = m.provide(s2);
        } catch (
                IllegalArgumentException |
                com.pi4j.io.gpio.exception.GpioPinExistsException |
                NullPointerException |
                ArrayIndexOutOfBoundsException ex) {
            // jmri.jmrix.pi.RaspberryPiTurnout(Providing)ManagerTest gives a GpioPinExistsException here.
            // jmri.jmrix.openlcb.OlcbLightProvidingManagerTest gives a NullPointerException here.
            // jmri.jmrix.openlcb.OlcbSensorProvidingManagerTest gives a ArrayIndexOutOfBoundsException here.
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
        try {
            // Register different bean with existing systemName.
            // This should fail with an DuplicateSystemNameException.
            l.register(e2);
            Assert.fail("Expected exception not thrown");
        } catch (NamedBean.DuplicateSystemNameException ex) {
            Assert.assertEquals("exception message is correct", expectedMessage, ex.getMessage());
            JUnitAppender.assertErrorMessage(expectedMessage);
        }

        l.deregister(e1);
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

    private final static Logger log = LoggerFactory.getLogger(AbstractProvidingManagerTestBase.class);

}
