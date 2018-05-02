package jmri.implementation;

import jmri.NamedBean;
import jmri.SignalSystem;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the DefaultSignalSystem interface implementation
 *
 * @author	Bob Jacobsen Copyright (C) 2009
 */
public class DefaultSignalSystemTest {

    @Test
    public void testCtor() {
        DefaultSignalSystem dss = new DefaultSignalSystem("sys", "user");
        Assert.assertNotNull(dss);
    }

    @Test
    public void testOneAspectOneProperty() {
        SignalSystem t = new DefaultSignalSystem("sys", "user");

        t.setProperty("Stop", "Speed", 0);

        Assert.assertEquals(0, t.getProperty("Stop", "Speed"));
    }

    @Test
    public void testTwoAspectOneProperty() {
        SignalSystem t = new DefaultSignalSystem("sys", "user");

        t.setProperty("Stop", "Speed", 0);
        t.setProperty("Clear", "Speed", 10);

        Assert.assertEquals("Stop", 0, t.getProperty("Stop", "Speed"));
        Assert.assertEquals("Clear", 10, t.getProperty("Clear", "Speed"));
    }

    @Test
    public void testTwoAspectTwoProperties() {
        SignalSystem t = new DefaultSignalSystem("sys", "user");

        t.setProperty("Stop", "Speed", 0);
        t.setProperty("Clear", "Speed", 10);
        t.setProperty("Stop", "Biff", "ffiB");
        t.setProperty("Clear", "Biff", "beef");

        Assert.assertEquals("Stop", 0, t.getProperty("Stop", "Speed"));
        Assert.assertEquals("Clear", 10, t.getProperty("Clear", "Speed"));
        Assert.assertEquals("Stop", "ffiB", t.getProperty("Stop", "Biff"));
        Assert.assertEquals("Clear", "beef", t.getProperty("Clear", "Biff"));
    }

    @Test
    public void testGetAspects() {
        SignalSystem t = new DefaultSignalSystem("sys", "user");

        t.setProperty("Stop", "Speed", 0);
        t.setProperty("Approach", "Speed", 5);
        t.setProperty("Clear", "Speed", 10);

        java.util.Enumeration<String> e = t.getAspects();

        Assert.assertEquals("Stop", e.nextElement());
        Assert.assertEquals("Approach", e.nextElement());
        Assert.assertEquals("Clear", e.nextElement());

        Assert.assertTrue(!e.hasMoreElements());

    }

    @Test
    public void testGetNullProperties() {
        SignalSystem t = new DefaultSignalSystem("sys", "user");

        t.setProperty("Stop", "Speed", 0);
        t.setProperty("Approach", "Speed", 5);

        Assert.assertEquals("Stop", null, t.getProperty("Stop", "None"));
        Assert.assertEquals("Clear", null, t.getProperty("Clear", "Speed"));

    }

    @Test
    public void testCheckAspect() {
        SignalSystem t = new DefaultSignalSystem("sys", "user");

        t.setProperty("Stop", "Speed", 0);
        t.setProperty("Approach", "Speed", 5);

        Assert.assertTrue("Stop", t.checkAspect("Stop"));
        Assert.assertFalse("Clear", t.checkAspect("Clear"));

    }

    @Test
    public void testGetKeys() {
        SignalSystem t = new DefaultSignalSystem("sys", "user");

        t.setProperty("Stop", "A", 0);
        t.setProperty("Approach", "C", 5);
        t.setProperty("Clear", "B", 10);

        java.util.Enumeration<String> e = t.getKeys();

        Assert.assertEquals("A", e.nextElement());
        Assert.assertEquals("C", e.nextElement());
        Assert.assertEquals("B", e.nextElement());

        Assert.assertTrue(!e.hasMoreElements());

    }

    @Test
    public void testGetKeysOverlap() {
        SignalSystem t = new DefaultSignalSystem("sys", "user");

        t.setProperty("Stop", "A", 0);
        t.setProperty("Approach", "C", 5);
        t.setProperty("Approach", "A", 5);
        t.setProperty("Approach", "B", 5);
        t.setProperty("Clear", "B", 10);

        java.util.Enumeration<String> e = t.getKeys();

        Assert.assertEquals("A", e.nextElement());
        Assert.assertEquals("C", e.nextElement());
        Assert.assertEquals("B", e.nextElement());

        Assert.assertTrue(!e.hasMoreElements());

    }

    @Test
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

    @Test
    public void testGetState() {
        DefaultSignalSystem dss = new DefaultSignalSystem("sys", "user");
        Assert.assertEquals(NamedBean.INCONSISTENT, dss.getState());
    }

    @Test
    public void testSetState() {
        DefaultSignalSystem dss = new DefaultSignalSystem("sys", "user");
        dss.setState(NamedBean.UNKNOWN);
        // verify getState did not change
        Assert.assertEquals(NamedBean.INCONSISTENT, dss.getState());
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
