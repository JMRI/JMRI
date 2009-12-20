// DefaultSignalAppearanceMapTest.java

package jmri.implementation;

import jmri.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the SignalAppearanceMap interface
 * @author	Bob Jacobsen  Copyright (C) 2009
 * @version $Revision: 1.1 $
 */
public class DefaultSignalAppearanceMapTest extends TestCase {

	public void testCtor() {
	    new DefaultSignalAppearanceMap("sys", "user");
	}

	public void testDefaultMap() {
	    SignalAppearanceMap t = new DefaultSignalAppearanceMap("sys", "user");
	    ((DefaultSignalAppearanceMap)t).loadDefaults();
	    
	    t.setAppearances("Stop", new SignalHead[]{h1});
	    Assert.assertEquals("Stop is RED", SignalHead.RED,
	                        h1.getAppearance());

	    t.setAppearances("Approach", new SignalHead[]{h1});
	    Assert.assertEquals("Approach is YELLOW", SignalHead.YELLOW,
	                        h1.getAppearance());

	    t.setAppearances("Clear", new SignalHead[]{h1});
	    Assert.assertEquals("Clear is GREEN", SignalHead.GREEN,
	                        h1.getAppearance());
	}
	
	public void testDefaultAspects() {
	    SignalAppearanceMap t = new DefaultSignalAppearanceMap("sys", "user");
	    ((DefaultSignalAppearanceMap)t).loadDefaults();

	    java.util.Enumeration<String> e = t.getAspects();
	    
	    Assert.assertEquals("Stop", e.nextElement());
	    Assert.assertEquals("Approach", e.nextElement());
	    Assert.assertEquals("Clear", e.nextElement());
	    
	    Assert.assertTrue(!e.hasMoreElements());
	}
	
	public void testTwoHead() {
	    SignalAppearanceMap t = new DefaultSignalAppearanceMap("sys", "user");
	    t.addAspect("meh", new int[]{SignalHead.LUNAR, SignalHead.DARK});
	    t.addAspect("biff", new int[]{SignalHead.GREEN, SignalHead.GREEN});
	    
	    SignalHead h2 = new DefaultSignalHead("h1", "head1") {
            protected void updateOutput() {};
        };

	    t.setAppearances("meh", new SignalHead[]{h1, h2});
	    Assert.assertEquals("meh 1 is LUNAR", SignalHead.LUNAR,
	                        h1.getAppearance());
	    Assert.assertEquals("meh 2 is LUNAR", SignalHead.DARK,
	                        h2.getAppearance());
        
	    t.setAppearances("biff", new SignalHead[]{h1, h2});
	    Assert.assertEquals("biff 1 is GREEN", SignalHead.GREEN,
	                        h1.getAppearance());
	    Assert.assertEquals("biff 2 is GREEN", SignalHead.GREEN,
	                        h2.getAppearance());
        
        
	}
	
	// from here down is testing infrastructure

	public DefaultSignalAppearanceMapTest(String s) {
		super(s);
	}

    SignalHead h1;
    
	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {DefaultSignalAppearanceMapTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(DefaultSignalAppearanceMapTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { 
        apps.tests.Log4JFixture.setUp(); 
        h1 = new DefaultSignalHead("h1", "head1") {
            protected void updateOutput() {};
        };
    }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
    static protected org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(DefaultSignalAppearanceMapTest.class.getName());
}
