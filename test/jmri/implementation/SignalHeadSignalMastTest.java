// SignalHeadSignalMastTest.java

package jmri.implementation;

import jmri.*;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the SignalHeadSignalMast implementation
 * @author	Bob Jacobsen  Copyright (C) 2009
 * @version $Revision: 1.2 $
 */
public class SignalHeadSignalMastTest extends TestCase {

    public void testSetup() {
        Assert.assertNotNull(InstanceManager.signalHeadManagerInstance());
        Assert.assertNotNull(InstanceManager.signalHeadManagerInstance().getSignalHead("IH1"));
    }
    
	public void testTwoNameCtorOK() {
	    new SignalHeadSignalMast("IF$shsm:basic:one-searchlight:IH1", "user");
	}

	public void testOneNameCtorOK() {
	        new SignalHeadSignalMast("IF$shsm:basic:one-searchlight:IH1");
	}

	public void testOneNameCtorFailNoSystem() {
	    try {
	        new SignalHeadSignalMast("IF$shsm:notanaspect:one-searchlight:IH1");
	        Assert.fail("should have thrown exception");
        } catch (IllegalArgumentException e1) {
            jmri.util.JUnitAppender.assertErrorMessage("Did not find signal definition: notanaspect");
        } catch (Exception e2) {Assert.fail("wrong exception: "+e2);}
	}

	public void testAspects() {
	    SignalMast s = new SignalHeadSignalMast("IF$shsm:basic:one-searchlight:IH1", "user");
	    
	    s.setAspect("Clear");
	    Assert.assertEquals("check clear","Clear",s.getAspect());
	    s.setAspect("Stop");
	    Assert.assertEquals("check stop","Stop",s.getAspect());
	}

	public void testAspectNotSet() {
	    SignalMast s = new SignalHeadSignalMast("IF$shsm:basic:one-searchlight:IH1", "user");
	    
	    Assert.assertNull("check null",s.getAspect());
	}

	public void testAspectFail() {
	    SignalMast s = new SignalHeadSignalMast("IF$shsm:basic:one-searchlight:IH1", "user");
	    
	    s.setAspect("Clear");

	    try {
	        s.setAspect("Not An Aspect, I Hope");
	        Assert.fail("should have thrown exception");
        } catch (IllegalArgumentException e1) {
            jmri.util.JUnitAppender.assertWarnMessage("attempting to set invalid aspect: Not An Aspect, I Hope");
        } catch (Exception e2) {Assert.fail("wrong exception: "+e2);}
        
	    Assert.assertEquals("check clear","Clear",s.getAspect()); // unchanged after failed request
	}

	public void testConfigureOneSearchLight() {
	    SignalMast s = new SignalHeadSignalMast("IF$shsm:basic:one-searchlight:IH1", "user");
	    
	    s.setAspect("Clear");
	    Assert.assertEquals("check green",SignalHead.GREEN,
	        InstanceManager.signalHeadManagerInstance().getSignalHead("IH1").getAppearance());

	    s.setAspect("Approach");
	    Assert.assertEquals("check yellow",SignalHead.YELLOW,
	        InstanceManager.signalHeadManagerInstance().getSignalHead("IH1").getAppearance());
	}

	public void testConfigureTwoSearchLight() {
	    SignalMast s = new SignalHeadSignalMast("IF$shsm:basic:two-searchlight:IH1:IH2", "user");
	    
	    s.setAspect("Clear");
	    Assert.assertEquals("Clear head 1 green",SignalHead.GREEN,
	        InstanceManager.signalHeadManagerInstance().getSignalHead("IH1").getAppearance());
	    Assert.assertEquals("Clear head 2 red",SignalHead.RED,
	        InstanceManager.signalHeadManagerInstance().getSignalHead("IH2").getAppearance());

	    s.setAspect("Diverging Approach");
	    Assert.assertEquals("Diverging Approach head 1 red",SignalHead.RED,
	        InstanceManager.signalHeadManagerInstance().getSignalHead("IH1").getAppearance());
	    Assert.assertEquals("Diverging Approach head 2 yellow",SignalHead.YELLOW,
	        InstanceManager.signalHeadManagerInstance().getSignalHead("IH2").getAppearance());
	}

	public void testOneSearchLightViaManager() {
	    SignalMast s = InstanceManager.signalMastManagerInstance().provideSignalMast("IF$shsm:basic:one-searchlight:IH2");
	    
	    s.setAspect("Clear");
	    Assert.assertEquals("check green",SignalHead.GREEN,
	        InstanceManager.signalHeadManagerInstance().getSignalHead("IH2").getAppearance());

	    s.setAspect("Approach");
	    Assert.assertEquals("check yellow",SignalHead.YELLOW,
	        InstanceManager.signalHeadManagerInstance().getSignalHead("IH2").getAppearance());
	}

	public void testSignalSystemLink() {
	    SignalMast s = InstanceManager.signalMastManagerInstance().provideSignalMast("IF$shsm:basic:one-searchlight:IH2");
	    
	    SignalSystem sy = s.getSignalSystem();
	    Assert.assertNotNull(sy);
	    
	    Assert.assertEquals("Proceed", s.getSignalSystem().getProperty("Clear", "indication"));
    }
    
	// from here down is testing infrastructure

	public SignalHeadSignalMastTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {SignalHeadSignalMastTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(SignalHeadSignalMastTest.class);
		return suite;
	}

    // The minimal setup for log4J
    protected void setUp() { 
        apps.tests.Log4JFixture.setUp(); 
        jmri.util.JUnitUtil.resetInstanceManager();
        InstanceManager.signalHeadManagerInstance().register(
            new DefaultSignalHead("IH1"){
                protected void updateOutput(){}
            }
        );
        InstanceManager.signalHeadManagerInstance().register(
            new DefaultSignalHead("IH2"){
                protected void updateOutput(){}
            }
        );
        InstanceManager.signalHeadManagerInstance().register(
            new DefaultSignalHead("IH3"){
                protected void updateOutput(){}
            }
        );
    }
    protected void tearDown() { apps.tests.Log4JFixture.tearDown(); }
    static protected org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SignalHeadSignalMastTest.class.getName());
}
