package jmri.managers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import jmri.*;

import java.beans.PropertyVetoException;
import java.lang.reflect.Field;
import jmri.util.JUnitAppender;
import org.slf4j.event.Level;

import org.junit.jupiter.api.*;

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

    @Test
    public void testProvideEmpty() {
        ProvidingManager<E> m = l;
        Exception ex = assertThrows(IllegalArgumentException.class, () ->  m.provide(""));
        assertNotNull(ex);
        JUnitAppender.suppressErrorMessageStartsWith("Invalid system name for");
    }

    @jmri.util.junit.annotations.ToDo("Managers which cannot provide SystemName 1 or 2 "
        + "should override so provide try / catch on IllegalArgumentException in called method can be removed")
    @Test
    public void testRegisterDuplicateSystemName() throws PropertyVetoException,
            NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        ProvidingManager<E> m = l;
        String s1 = l.makeSystemName("1");
        String s2 = l.makeSystemName("2");
        testRegisterDuplicateSystemName(m, s1, s2);
    }

    public void testRegisterDuplicateSystemName(ProvidingManager<E> m, String s1, String s2)
            throws PropertyVetoException, NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
        assertNotNull(s1);
        assertFalse(s1.isEmpty());
        assertNotNull(s2);
        assertFalse(s2.isEmpty());

        E e1;
        E e2;

        try {
            e1 = m.provide(s1);
            e2 = m.provide(s2);
        } catch (
                IllegalArgumentException  ex) {
            // Some other tests give an IllegalArgumentException here.
            // If the test is unable to provide a named bean, abort this test.
            int numWarnings = JUnitAppender.clearBacklog(Level.WARN);
            // log.debug("Cannot provide a named bean, {} warnings", numWarnings, ex);
            Assumptions.abort( "We got " + numWarnings + " warnings and exception: " + ex.getMessage());
            return;
        }

        // Use reflection to change the systemName of e2
        // Try to find the field
        Field f1 = getField(e2.getClass(), "mSystemName");
        f1.setAccessible(true);
        f1.set(e2, e1.getSystemName());

        // Remove bean if it's already registered
        if (l.getBySystemName(e1.getSystemName()) != null) {
            l.deregister(e1);
        }
        // Remove bean if it's already registered
        if (l.getBySystemName(e2.getSystemName()) != null) {
            l.deregister(e2);
        }

        // Register the bean once. This should be OK.
        l.register(e1);

        // Register bean twice. This gives only a debug message.
        l.register(e1);

        // Register different bean with existing systemName.
        // This should fail with an DuplicateSystemNameException.
        String expectedMessage = "systemName is already registered: " + e1.getSystemName();
        NamedBean.DuplicateSystemNameException e = assertThrows( NamedBean.DuplicateSystemNameException.class,
            () -> l.register(e2),
            "Expected exception not thrown");
        assertEquals( expectedMessage, e.getMessage(), "exception message is correct");
            JUnitAppender.assertErrorMessage(expectedMessage);

        l.deregister(e1);
    }

    protected Field getField(Class<?> c, String fieldName) {
        try {
            return c.getDeclaredField(fieldName);
        } catch (NoSuchFieldException ex) {
            if (c.getSuperclass() != null) {
                return getField(c.getSuperclass(), fieldName);
            }
        }

        // Field not found
        return null;
    }

    // private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AbstractProvidingManagerTestBase.class);

}
