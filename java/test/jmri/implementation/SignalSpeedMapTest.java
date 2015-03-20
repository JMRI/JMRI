// SignalSpeedMapTest.java
package jmri.implementation;

import java.util.Calendar;
import java.util.Date;
import jmri.IdTag;
import jmri.Reporter;
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the SignalSpeedMap class
 *
 * @author Bob Jacobsen Copyright (C) 2015
 * @version $Revision$
 */
public class SignalSpeedMapTest extends TestCase {

    public void testLoadMap() {
        Assert.assertNotNull(SignalSpeedMap.getMap());
    }
    
    public void testMapMonoticity() {
        SignalSpeedMap m = SignalSpeedMap.getMap();
        
        String[] speeds = new String[]{
            "Maximum",
            "Normal",
            "Sixty",
            "Fifty",
            "Limited",
            "Medium",
            "Slow",
            "Restricted",
            "Stop"
        };
        
        for (int i = 0; i < speeds.length-1; i++) {
            Assert.assertTrue(speeds[i+1]+" ("+m.getSpeed(speeds[i+1])+") must be less than "+speeds[i]+" ("+m.getSpeed(speeds[i])+")", 
                m.getSpeed(speeds[i+1])<m.getSpeed(speeds[i]));
        }
         
    }

    // from here down is testing infrastructure
    public SignalSpeedMapTest(String s) {
        super(s);
    }

    // The minimal setup for log4J
    @Override
    protected void setUp() throws Exception {
        apps.tests.Log4JFixture.setUp();
        super.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initIdTagManager();
    }

    @Override
    protected void tearDown() throws Exception {
        jmri.util.JUnitUtil.resetInstanceManager();
        super.tearDown();
        apps.tests.Log4JFixture.tearDown();
    }

    // Main entry point
    static public void main(String[] args) {
        String[] testCaseName = {SignalSpeedMapTest.class.getName()};
        junit.swingui.TestRunner.main(testCaseName);
    }

    // test suite from all defined tests
    public static Test suite() {
        TestSuite suite = new TestSuite(SignalSpeedMapTest.class);
        return suite;
    }

}
