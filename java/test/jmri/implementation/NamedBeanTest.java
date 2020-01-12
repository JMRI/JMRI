package jmri.implementation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import jmri.NamedBean;
import org.junit.Assert;
import org.junit.Test;

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
        Assert.assertEquals("bar", n.getProperty("foo"));
    }

    @Test
    public void testGetSetNullBeanParameter() {
        NamedBean n = createInstance();

        n.setProperty("foo", "bar");
        Assert.assertEquals("bar", n.getProperty("foo"));
        n.setProperty("foo", null);
        Assert.assertEquals(null, n.getProperty("foo"));
    }

    @Test
    public void testGetBeanPropertyKeys() {
        NamedBean n = createInstance();

        n.setProperty("foo", "bar");
        n.setProperty("biff", "bar");

        java.util.Set<String> s = n.getPropertyKeys();
        Assert.assertEquals("size", 2, s.size());
        Assert.assertEquals("contains foo", true, s.contains("foo"));
        Assert.assertEquals("contains biff", true, s.contains("biff"));

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
        Assert.assertEquals("start length", 2, n.getNumPropertyChangeListeners());

        n.dispose();
        Assert.assertEquals("end length", 0, n.getNumPropertyChangeListeners());
    }

}
