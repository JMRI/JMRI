// DefaultSignalAspectTableTest.java

package jmri.implementation;

import jmri.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the SignalAspectTableTest interface
 * @author	Bob Jacobsen  Copyright (C) 2009
 * @version $Revision: 1.2 $
 */
public class DefaultSignalAspectTableTest extends TestCase {

	public void testCtor() {
	    new DefaultSignalAspectTable("sys", "user");
	}

    public void testOneAspectOneProperty() {
	    SignalAspectTable t = new DefaultSignalAspectTable("sys", "user");
	    
	    t.setProperty("Stop", "Speed", new Integer(0));
	    
	    Assert.assertEquals(new Integer(0), t.getProperty("Stop", "Speed"));
    }
    
    public void testTwoAspectOneProperty() {
	    SignalAspectTable t = new DefaultSignalAspectTable("sys", "user");
	    
	    t.setProperty("Stop", "Speed", new Integer(0));
	    t.setProperty("Clear", "Speed", new Integer(10));
	    
	    Assert.assertEquals("Stop", new Integer(0), t.getProperty("Stop", "Speed"));
	    Assert.assertEquals("Clear", new Integer(10), t.getProperty("Clear", "Speed"));
    }
    
    public void testTwoAspectTwoProperties() {
	    SignalAspectTable t = new DefaultSignalAspectTable("sys", "user");
	    
	    t.setProperty("Stop", "Speed", new Integer(0));
	    t.setProperty("Clear", "Speed", new Integer(10));
	    t.setProperty("Stop", "Biff", "ffiB");
	    t.setProperty("Clear", "Biff", "beef");
	    
	    Assert.assertEquals("Stop", new Integer(0), t.getProperty("Stop", "Speed"));
	    Assert.assertEquals("Clear", new Integer(10), t.getProperty("Clear", "Speed"));
	    Assert.assertEquals("Stop", "ffiB", t.getProperty("Stop", "Biff"));
	    Assert.assertEquals("Clear", "beef", t.getProperty("Clear", "Biff"));
    }
    
    public void testGetAspects() {
	    SignalAspectTable t = new DefaultSignalAspectTable("sys", "user");
	    
	    t.setProperty("Stop", "Speed", new Integer(0));
	    t.setProperty("Approach", "Speed", new Integer(5));
	    t.setProperty("Clear", "Speed", new Integer(10));
	    
	    java.util.Enumeration<String> e = t.getAspects();
	    
	    Assert.assertEquals("Stop", e.nextElement());
	    Assert.assertEquals("Approach", e.nextElement());
	    Assert.assertEquals("Clear", e.nextElement());
	    
	    Assert.assertTrue(!e.hasMoreElements());
	    
    }
    
    public void testGetNullProperties() {
	    SignalAspectTable t = new DefaultSignalAspectTable("sys", "user");
	    
	    t.setProperty("Stop", "Speed", new Integer(0));
	    t.setProperty("Approach", "Speed", new Integer(5));
	    
	    Assert.assertEquals("Stop", null, t.getProperty("Stop", "None"));
	    Assert.assertEquals("Clear", null, t.getProperty("Clear", "Speed"));
	    
    }
    
    public void testCheckAspect() {
	    SignalAspectTable t = new DefaultSignalAspectTable("sys", "user");
	    
	    t.setProperty("Stop", "Speed", new Integer(0));
	    t.setProperty("Approach", "Speed", new Integer(5));
	    
	    Assert.assertTrue("Stop", t.checkAspect("Stop"));
	    Assert.assertFalse("Clear", t.checkAspect("Clear"));
	    
    }
    
    public void testGetKeys() {
	    SignalAspectTable t = new DefaultSignalAspectTable("sys", "user");
	    
	    t.setProperty("Stop", "A", new Integer(0));
	    t.setProperty("Approach", "C", new Integer(5));
	    t.setProperty("Clear", "B", new Integer(10));
	    
	    java.util.Enumeration<String> e = t.getKeys();
	    
	    Assert.assertEquals("A", e.nextElement());
	    Assert.assertEquals("C", e.nextElement());
	    Assert.assertEquals("B", e.nextElement());
	    
	    Assert.assertTrue(!e.hasMoreElements());
	    
    }

    public void testGetKeysOverlap() {
	    SignalAspectTable t = new DefaultSignalAspectTable("sys", "user");
	    
	    t.setProperty("Stop", "A", new Integer(0));
	    t.setProperty("Approach", "C", new Integer(5));
	    t.setProperty("Approach", "A", new Integer(5));
	    t.setProperty("Approach", "B", new Integer(5));
	    t.setProperty("Clear", "B", new Integer(10));
	    
	    java.util.Enumeration<String> e = t.getKeys();
	    
	    Assert.assertEquals("A", e.nextElement());
	    Assert.assertEquals("C", e.nextElement());
	    Assert.assertEquals("B", e.nextElement());
	    
	    Assert.assertTrue(!e.hasMoreElements());
	    
    }

    public void testDefaults() {
        DefaultSignalAspectTable t = new DefaultSignalAspectTable("sys", "user");
        
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

	public DefaultSignalAspectTableTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {DefaultSignalAspectTableTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(DefaultSignalAspectTableTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { 
        apps.tests.Log4JFixture.setUp(); 
    }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
    static protected org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DefaultSignalAspectTableTest.class.getName());
}
