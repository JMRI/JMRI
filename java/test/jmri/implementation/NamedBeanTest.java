package jmri.implementation;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import jmri.NamedBean;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the NamedBean interface implementation.
 * <p>
 * Inherit from this and override "createInstance" if you want to include these
 * tests in a test class for your own NamedBean class
 *
 * @author	Bob Jacobsen Copyright (C) 2009, 2015
 */
public class NamedBeanTest extends TestCase {

    /**
     * This is a separate protected method, instead of part of setUp(), to make
     * subclassing easier.
     */
    protected NamedBean createInstance() {
        return new AbstractNamedBean("sys", "usr") {
            public int getState() {
                return 0;
            }

            public void setState(int i) {
            }

            public String getBeanType() {
                return "";
            }
        };
    }

    public void testSetBeanParameter() {
        NamedBean n = createInstance();

        n.setProperty("foo", "bar");
    }

    public void testGetBeanParameter() {
        NamedBean n = createInstance();

        n.setProperty("foo", "bar");
        Assert.assertEquals("bar", n.getProperty("foo"));
    }

    public void testGetSetNullBeanParameter() {
        NamedBean n = createInstance();

        n.setProperty("foo", "bar");
        Assert.assertEquals("bar", n.getProperty("foo"));
        n.setProperty("foo", null);
        Assert.assertEquals(null, n.getProperty("foo"));
    }

    public void testGetBeanPropertyKeys() {
        NamedBean n = createInstance();

        n.setProperty("foo", "bar");
        n.setProperty("biff", "bar");

        java.util.Set<String> s = n.getPropertyKeys();
        Assert.assertEquals("size", 2, s.size());
        Assert.assertEquals("contains foo", true, s.contains("foo"));
        Assert.assertEquals("contains biff", true, s.contains("biff"));

    }

    public void testDispose() {
        NamedBean n = createInstance();
        n.addPropertyChangeListener(new PropertyChangeListener(){
            public void propertyChange(PropertyChangeEvent p) {}
        });
        n.addPropertyChangeListener(new PropertyChangeListener(){
            public void propertyChange(PropertyChangeEvent p) {}
        });
        Assert.assertEquals("start length", 2, n.getNumPropertyChangeListeners());
        
        n.dispose();
        Assert.assertEquals("end length", 0, n.getNumPropertyChangeListeners());   
    }
    
    // from here down is testing infrastructure
    public NamedBeanTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {NamedBeanTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(NamedBeanTest.class);
        return suite;
    }

}
