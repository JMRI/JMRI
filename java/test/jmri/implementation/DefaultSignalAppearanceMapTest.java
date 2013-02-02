// DefaultSignalAppearanceMapTest.java

package jmri.implementation;

import org.apache.log4j.Logger;
import jmri.*;

import jmri.NamedBeanHandle;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.List;
import java.util.ArrayList;

/**
 * Tests for the SignalAppearanceMap interface
 * @author	Bob Jacobsen  Copyright (C) 2009
 * @version $Revision$
 */
public class DefaultSignalAppearanceMapTest extends TestCase {

	public void testCtor() {
	    new DefaultSignalAppearanceMap("sys", "user");
	}

	public void testDefaultMap() {
        SignalMast s = InstanceManager.signalMastManagerInstance().provideSignalMast("IF$shsm:basic:one-searchlight:h1");
	    DefaultSignalAppearanceMap t = (DefaultSignalAppearanceMap)s.getAppearanceMap();
	    t.loadDefaults();
	    
	    s.setAspect("Stop");
	    Assert.assertEquals("Stop is RED", SignalHead.RED,
	                        h1.getAppearance());

	    s.setAspect("Approach");
	    Assert.assertEquals("Approach is YELLOW", SignalHead.YELLOW,
	                        h1.getAppearance());

	    s.setAspect("Clear");
	    Assert.assertEquals("Clear is GREEN", SignalHead.GREEN,
	                        h1.getAppearance());
                            
        InstanceManager.signalMastManagerInstance().deregister(s);
        s.dispose();
	}
	
	public void testDefaultAspects() {
	    DefaultSignalAppearanceMap t = new DefaultSignalAppearanceMap("sys", "user");
	    t.loadDefaults();

	    java.util.Enumeration<String> e = t.getAspects();
	    
	    Assert.assertEquals("Stop", e.nextElement());
	    Assert.assertEquals("Approach", e.nextElement());
	    Assert.assertEquals("Clear", e.nextElement());
	    
	    Assert.assertTrue(!e.hasMoreElements());
	}
	
    public void testTwoHead() {

        SignalMast s = new SignalHeadSignalMast("IF$shsm:basic:two-searchlight:h1:h2") {
            void configureAspectTable(String signalSystemName, String aspectMapName) {
                map = new DefaultSignalAppearanceMap("sys", "user");
            }
        };
            
        
	    DefaultSignalAppearanceMap t = (DefaultSignalAppearanceMap)s.getAppearanceMap();
	    t.addAspect("meh", new int[]{SignalHead.LUNAR, SignalHead.DARK});
	    t.addAspect("biff", new int[]{SignalHead.GREEN, SignalHead.GREEN});
        
	    s.setAspect("meh");
	    Assert.assertEquals("meh 1 is LUNAR", SignalHead.LUNAR,
	                        h1.getAppearance());
	    Assert.assertEquals("meh 2 is LUNAR", SignalHead.DARK,
	                        h2.getAppearance());
        
	    s.setAspect("biff");
	    Assert.assertEquals("biff 1 is GREEN", SignalHead.GREEN,
	                        h1.getAppearance());
	    Assert.assertEquals("biff 2 is GREEN", SignalHead.GREEN,
	                        h2.getAppearance());
                            
        InstanceManager.signalMastManagerInstance().deregister(s);
        s.dispose();
	}
	
	// from here down is testing infrastructure

	public DefaultSignalAppearanceMapTest(String s) {
		super(s);
	}

    SignalHead h1;
    SignalHead h2;
    
    List<NamedBeanHandle<SignalHead>> l1;
    List<NamedBeanHandle<SignalHead>> l2;
    
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
            protected void updateOutput() {}
        };
	    h2 = new DefaultSignalHead("h2", "head2") {
            protected void updateOutput() {}
        };
        l1 = new ArrayList<NamedBeanHandle<SignalHead>>();
        l1.add(new NamedBeanHandle<SignalHead>("h1", h1));
        l2 = new ArrayList<NamedBeanHandle<SignalHead>>();
        l2.add(new NamedBeanHandle<SignalHead>("h1", h1));
        l2.add(new NamedBeanHandle<SignalHead>("h2", h2));
        InstanceManager.signalHeadManagerInstance().register(h1);
        InstanceManager.signalHeadManagerInstance().register(h2);
    }
    protected void tearDown() { 
        InstanceManager.signalHeadManagerInstance().deregister(h1);
        h1.dispose();
        InstanceManager.signalHeadManagerInstance().deregister(h2);
        h2.dispose();
        apps.tests.Log4JFixture.tearDown();
    }
    static protected Logger log = Logger.getLogger(DefaultSignalAppearanceMapTest.class.getName());
}
