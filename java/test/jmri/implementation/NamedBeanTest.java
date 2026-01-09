package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import jmri.NamedBean;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the NamedBean interface implementation.
 * <p>
 * Inherit from this and override "createInstance" if you want to include these
 * tests in a test class for your own NamedBean class
 *
 * @author Bob Jacobsen Copyright (C) 2009, 2015
 */
public class NamedBeanTest {

    /**
     * This is a separate protected method, instead of part of setUp(), to make
     * subclassing easier.
     * 
     * @return a new NamedBean with system name "sys" and user name "usr"
     */
    protected NamedBean createInstance() {
        return new AbstractNamedBean("sys", "usr") {
            @Override
            public int getState() {
                return 0;
            }

            @Override
            public void setState(int i) {
            }

            @Override
            public String getBeanType() {
                return "";
            }
        };
    }

    @Test
    public void testSetBeanParameter() {
        NamedBean n = createInstance();

        n.setProperty("foo", "bar");
    }

    @Test
    public void testGetBeanParameter() {
        NamedBean n = createInstance();

        n.setProperty("foo", "bar");
        assertEquals( n.getProperty("foo"), "bar");
    }

    @Test
    public void testGetSetNullBeanParameter() {
        NamedBean n = createInstance();

        n.setProperty("foo", "bar");
        assertEquals("bar", n.getProperty("foo"));
        n.setProperty("foo", null);
        assertNull( n.getProperty("foo"));
    }

    @Test
    public void testGetBeanPropertyKeys() {
        NamedBean n = createInstance();

        n.setProperty("foo", "bar");
        n.setProperty("biff", "bar");

        java.util.Set<String> s = n.getPropertyKeys();
        assertEquals( 2, s.size(), "size");
        assertTrue( s.contains("foo"), "contains foo");
        assertTrue( s.contains("biff"), "contains biff");

    }

    @Test
    public void testDispose() {
        NamedBean n = createInstance();
        n.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent p) {
            }
        });
        n.addPropertyChangeListener(new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent p) {
            }
        });
        assertEquals( 2, n.getNumPropertyChangeListeners(), "start length");

        n.dispose();
        assertEquals( 0, n.getNumPropertyChangeListeners(), "end length");
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
