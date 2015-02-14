// BeanSettingTest.java

package jmri;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import jmri.util.JUnitUtil;
/**
 * Tests for the BeanSetting class
 * @author	Bob Jacobsen  Copyright (C) 2006
 * @version $Revision$
 */
public class BeanSettingTest extends TestCase {

	public void testCreate() {
	    new BeanSetting(null, 0);
	}

	public void testCheckSensor() throws JmriException {
	    SensorManager sm = new jmri.managers.InternalSensorManager();
	    Sensor s = sm.provideSensor("IS12");
	    
	    BeanSetting b = new BeanSetting(s, Sensor.ACTIVE);
	    Assert.assertTrue("Initial check of sensor", !b.check());
	    
	    s.setState(Sensor.ACTIVE);
	    Assert.assertTrue("check of ACTIVE sensor", b.check());
	}

	public void testCheckTurnout() throws JmriException {
	    TurnoutManager sm = new jmri.managers.InternalTurnoutManager();
	    Turnout s = sm.provideTurnout("IT12");
	    
	    BeanSetting b = new BeanSetting(s, Turnout.THROWN);
	    Assert.assertTrue("Initial check of turnout", !b.check());
	    
	    s.setState(Turnout.THROWN);
	    Assert.assertTrue("check of THROWN turnout", b.check());
	}

	// from here down is testing infrastructure

	public BeanSettingTest(String s) {
		super(s);
	}

	// Main entry point
	static public void main(String[] args) {
		String[] testCaseName = {BeanSettingTest.class.getName()};
		junit.swingui.TestRunner.main(testCaseName);
	}

	// test suite from all defined tests
	public static Test suite() {
		TestSuite suite = new TestSuite(BeanSettingTest.class);
		return suite;
	}

    protected void setUp() throws Exception { 
        super.setUp();
        JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.setUp();
    }
    
    protected void tearDown() throws Exception  { 
        JUnitUtil.resetInstanceManager();
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }
}
