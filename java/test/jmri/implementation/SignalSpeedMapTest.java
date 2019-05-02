package jmri.implementation;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the SignalSpeedMap class
 *
 * @author Bob Jacobsen Copyright (C) 2015
 */
public class SignalSpeedMapTest {

    @Test
    public void testLoadDefaultMap() {
        Assert.assertNotNull(jmri.InstanceManager.getDefault(SignalSpeedMap.class));
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

    SignalSpeedMap map; // can't static init before class; use @BeforeClass when changing to JUnit4
    
    /**
     * To avoid breaking signal systems, speed definitions should
     * never be removed from the default map. Hence we check that 
     * all standard names are present.
     */
    @Test
    public void testAllSpeedsPresent() {
        for (int i = 0; i < speeds.length; i++) {
            Assert.assertTrue(map.getSpeed(speeds[i])+" must be ge 0 to be present",0<=map.getSpeed(speeds[i]));
        }
    }

    @Test
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
    @Test
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
    
    @Test
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

    @Before
    public void setUp() throws Exception {
        jmri.util.JUnitUtil.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initIdTagManager();
        map = new SignalSpeedMap();
    }

    @After
    public void tearDown() throws Exception {
        map = null;
        jmri.util.JUnitUtil.tearDown();
    }
}
