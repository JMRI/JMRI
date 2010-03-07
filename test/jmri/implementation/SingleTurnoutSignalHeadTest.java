// SingleTurnoutSignalHeadTest.java

package jmri.implementation;

import jmri.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the SingleTurnoutSignalHead implementation
 * @author	Bob Jacobsen  Copyright (C) 2010
 * @version $Revision: 1.1 $
 */
public class SingleTurnoutSignalHeadTest extends TestCase {

    public void testNoDarkValidTypes() {
        Turnout t = InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        SingleTurnoutSignalHead h 
                = new SingleTurnoutSignalHead("IH1", 
                    new jmri.util.NamedBeanHandle<Turnout>("IT1", t),
                    SignalHead.GREEN, SignalHead.RED);
        
        int[] list = h.getValidStates();
        Assert.assertEquals(2, list.length);
        Assert.assertEquals(SignalHead.GREEN, list[0]);
        Assert.assertEquals(SignalHead.RED, list[1]);
    }
    
    public void testDarkValidTypes1() {
        Turnout t = InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        SingleTurnoutSignalHead h 
                = new SingleTurnoutSignalHead("IH1", 
                    new jmri.util.NamedBeanHandle<Turnout>("IT1", t),
                    SignalHead.DARK, SignalHead.RED);
        
        int[] list = h.getValidStates();
        Assert.assertEquals(3, list.length);
        Assert.assertEquals(SignalHead.DARK, list[0]);
        Assert.assertEquals(SignalHead.RED, list[1]);
        Assert.assertEquals(SignalHead.FLASHRED, list[2]);
    }
    
    public void testDarkValidTypes2() {
        Turnout t = InstanceManager.turnoutManagerInstance().provideTurnout("IT1");
        SingleTurnoutSignalHead h 
                = new SingleTurnoutSignalHead("IH1", 
                    new jmri.util.NamedBeanHandle<Turnout>("IT1", t),
                    SignalHead.GREEN, SignalHead.DARK);
        
        int[] list = h.getValidStates();
        Assert.assertEquals(3, list.length);
        Assert.assertEquals(SignalHead.GREEN, list[0]);
        Assert.assertEquals(SignalHead.FLASHGREEN, list[1]);
        Assert.assertEquals(SignalHead.DARK, list[2]);
    }
    

    
	// from here down is testing infrastructure

	public SingleTurnoutSignalHeadTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {SingleTurnoutSignalHeadTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(SingleTurnoutSignalHeadTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { 
        apps.tests.Log4JFixture.setUp(); 
        jmri.util.JUnitUtil.resetInstanceManager();
    }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
    static protected org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SingleTurnoutSignalHeadTest.class.getName());
}
