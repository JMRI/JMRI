package jmri.implementation;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Tests for the SignalSpeedMap class
 *
 * @author Bob Jacobsen Copyright (C) 2015
 */
public class SignalSpeedMapTest extends TestCase {

    public void testLoadMap() {
        Assert.assertNotNull(SignalSpeedMap.getMap());
    }
    
    static final String[] speeds = new String[]{
        "Cab",
        "Maximum",
        "Normal",
        "Sixty",
        "Fifty",
        "Limited",
        "Medium",
        "Slow",
        "Restricted",
        "RestrictedSlow",
        "Stop"
    };

    SignalSpeedMap map = SignalSpeedMap.getMap();
    
    /**
     * To avoid breaking signal systems, speed definitions should
     * never be removed from the default map. Hence we check that 
     * all standard names are present.
     */
    public void testAllSpeedsPresent() {
        for (int i = 0; i < speeds.length; i++) {
            Assert.assertTrue(map.getSpeed(speeds[i])+" must be ge 0 to be present",0<=map.getSpeed(speeds[i]));
        }
    }

    public void testMapMonoticity() {
        
        // check for monotonic values
        for (int i = 0; i < speeds.length-1; i++) {
            Assert.assertTrue(speeds[i+1]+" ("+map.getSpeed(speeds[i+1])+") must be less than "+speeds[i]+" ("+map.getSpeed(speeds[i])+")", 
                map.getSpeed(speeds[i+1])<map.getSpeed(speeds[i]));
        }
         
    }

    /**
     * To avoid breaking signal systems, speed definitions should
     * never be removed from the default map. This test will fail
     * if a new name is added, at which point you should add it to 
     * the definition of the "speeds" array above so that it will be
     * tested for in the future.
     */
    public void testNoExtraSpeedsPresent() {
        java.util.Enumeration<String> e = map.getSpeedIterator();
        String name;
        check: while (e.hasMoreElements()) {
            name = e.nextElement();
            for (String test : speeds) {
                if (test.equals(name)) continue check;
            }
            Assert.fail("Speed name \""+name+"\" not recognized");
        }        
    }
    
    public void testAppearanceSpeedsOK() {
        // check that every speed in <appearanceSpeeds> is defined
        java.util.Enumeration<String> e = map.getAppearanceIterator();
        String name;
        while (e.hasMoreElements()) {
            name = e.nextElement();
            Assert.assertNotNull("appearanceSpeed \""+name+"\" is defined", map.getAppearanceSpeed(name));
            Assert.assertTrue("appearanceSpeed \""+name+"\" has value", map.getSpeed(map.getAppearanceSpeed(name)) >= 0.);
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
