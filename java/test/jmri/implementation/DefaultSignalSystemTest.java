// DefaultSignalSystemTest.java
package jmri.implementation;

import jmri.SignalSystem;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests for the DefaultSignalSystem interface implementation
 *
 * @author	Bob Jacobsen Copyright (C) 2009
 */
public class DefaultSignalSystemTest extends TestCase {

    public void testCtor() {
        new DefaultSignalSystem("sys", "user");
    }

    public void testOneAspectOneProperty() {
        SignalSystem t = new DefaultSignalSystem("sys", "user");

        t.setProperty("Stop", "Speed", Integer.valueOf(0));

        Assert.assertEquals(Integer.valueOf(0), t.getProperty("Stop", "Speed"));
    }

    public void testTwoAspectOneProperty() {
        SignalSystem t = new DefaultSignalSystem("sys", "user");

        t.setProperty("Stop", "Speed", Integer.valueOf(0));
        t.setProperty("Clear", "Speed", Integer.valueOf(10));

        Assert.assertEquals("Stop", Integer.valueOf(0), t.getProperty("Stop", "Speed"));
        Assert.assertEquals("Clear", Integer.valueOf(10), t.getProperty("Clear", "Speed"));
    }

    public void testTwoAspectTwoProperties() {
        SignalSystem t = new DefaultSignalSystem("sys", "user");

        t.setProperty("Stop", "Speed", Integer.valueOf(0));
        t.setProperty("Clear", "Speed", Integer.valueOf(10));
        t.setProperty("Stop", "Biff", "ffiB");
        t.setProperty("Clear", "Biff", "beef");

        Assert.assertEquals("Stop", Integer.valueOf(0), t.getProperty("Stop", "Speed"));
        Assert.assertEquals("Clear", Integer.valueOf(10), t.getProperty("Clear", "Speed"));
        Assert.assertEquals("Stop", "ffiB", t.getProperty("Stop", "Biff"));
        Assert.assertEquals("Clear", "beef", t.getProperty("Clear", "Biff"));
    }

    public void testGetAspects() {
        SignalSystem t = new DefaultSignalSystem("sys", "user");

        t.setProperty("Stop", "Speed", Integer.valueOf(0));
        t.setProperty("Approach", "Speed", Integer.valueOf(5));
        t.setProperty("Clear", "Speed", Integer.valueOf(10));

        java.util.Enumeration<String> e = t.getAspects();

        Assert.assertEquals("Stop", e.nextElement());
        Assert.assertEquals("Approach", e.nextElement());
        Assert.assertEquals("Clear", e.nextElement());

        Assert.assertTrue(!e.hasMoreElements());

    }

    public void testGetNullProperties() {
        SignalSystem t = new DefaultSignalSystem("sys", "user");

        t.setProperty("Stop", "Speed", Integer.valueOf(0));
        t.setProperty("Approach", "Speed", Integer.valueOf(5));

        Assert.assertEquals("Stop", null, t.getProperty("Stop", "None"));
        Assert.assertEquals("Clear", null, t.getProperty("Clear", "Speed"));

    }

    public void testCheckAspect() {
        SignalSystem t = new DefaultSignalSystem("sys", "user");

        t.setProperty("Stop", "Speed", Integer.valueOf(0));
        t.setProperty("Approach", "Speed", Integer.valueOf(5));

        Assert.assertTrue("Stop", t.checkAspect("Stop"));
        Assert.assertFalse("Clear", t.checkAspect("Clear"));

    }

    public void testGetKeys() {
        SignalSystem t = new DefaultSignalSystem("sys", "user");

        t.setProperty("Stop", "A", Integer.valueOf(0));
        t.setProperty("Approach", "C", Integer.valueOf(5));
        t.setProperty("Clear", "B", Integer.valueOf(10));

        java.util.Enumeration<String> e = t.getKeys();

        Assert.assertEquals("A", e.nextElement());
        Assert.assertEquals("C", e.nextElement());
        Assert.assertEquals("B", e.nextElement());

        Assert.assertTrue(!e.hasMoreElements());

    }

    public void testGetKeysOverlap() {
        SignalSystem t = new DefaultSignalSystem("sys", "user");

        t.setProperty("Stop", "A", Integer.valueOf(0));
        t.setProperty("Approach", "C", Integer.valueOf(5));
        t.setProperty("Approach", "A", Integer.valueOf(5));
        t.setProperty("Approach", "B", Integer.valueOf(5));
        t.setProperty("Clear", "B", Integer.valueOf(10));

        java.util.Enumeration<String> e = t.getKeys();

        Assert.assertEquals("A", e.nextElement());
        Assert.assertEquals("C", e.nextElement());
        Assert.assertEquals("B", e.nextElement());

        Assert.assertTrue(!e.hasMoreElements());

    }

    public void testDefaults() {
        DefaultSignalSystem t = new DefaultSignalSystem("sys", "user");

        t.loadDefaults();

        Assert.assertTrue("Clear", t.checkAspect("Clear"));
        Assert.assertTrue("Approach", t.checkAspect("Approach"));
        Assert.assertTrue("Stop", t.checkAspect("Stop"));

        Assert.assertEquals("Stop Speed", "0", t.getProperty("Stop", "Speed"));
        Assert.assertEquals("Approach Speed", "30", t.getProperty("Approach", "Speed"));
        Assert.assertEquals("Clear Speed", "60", t.getProperty("Clear", "Speed"));

        java.util.Enumeration<String> e = t.getAspects();
        Assert.assertEquals("Stop", e.nextElement());
        Assert.assertEquals("Approach", e.nextElement());
        Assert.assertEquals("Clear", e.nextElement());
        Assert.assertTrue("Aspects", !e.hasMoreElements());

        e = t.getKeys();
        Assert.assertEquals("Speed", e.nextElement());
        Assert.assertTrue("Keys", !e.hasMoreElements());

    }

    // from here down is testing infrastructure
    public DefaultSignalSystemTest(String s) {
        super(s);
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {DefaultSignalSystemTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(DefaultSignalSystemTest.class);
        return suite;
    }

    // The minimal setup for log4J
    protected void setUp() {
        apps.tests.Log4JFixture.setUp();
    }

    protected void tearDown() {
        apps.tests.Log4JFixture.tearDown();
    }
    static protected Logger log = LoggerFactory.getLogger(DefaultSignalSystemTest.class.getName());
}
