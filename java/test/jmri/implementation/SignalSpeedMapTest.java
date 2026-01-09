package jmri.implementation;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for the SignalSpeedMap class
 *
 * @author Bob Jacobsen Copyright (C) 2015
 */
public class SignalSpeedMapTest {

    @Test
    public void testLoadDefaultMap() {
        assertNotNull(jmri.InstanceManager.getDefault(SignalSpeedMap.class));
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

    private SignalSpeedMap map;
    
    /**
     * To avoid breaking signal systems, speed definitions should
     * never be removed from the default map. Hence we check that 
     * all standard names are present.
     */
    @Test
    public void testAllSpeedsPresent() {
        for (int i = 0; i < speeds.length; i++) {
            assertTrue( 0 <= map.getSpeed(speeds[i]),
                    map.getSpeed(speeds[i])+" must be ge 0 to be present");
        }
    }

    @Test
    public void testMapMonoticity() {
        
        // check for monotonic values
        for (int i = 0; i < speeds.length-1; i++) {
            assertTrue( map.getSpeed(speeds[i+1]) < map.getSpeed(speeds[i]),
                speeds[i+1]+" ("+map.getSpeed(speeds[i+1])+") must be less than "
                    +speeds[i]+" ("+map.getSpeed(speeds[i])+")");
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
                if (test.equals(name)) {
                    continue check;
                }
            }
            fail("Speed name \""+name+"\" not recognized");
        }        
    }
    
    @Test
    public void testAppearanceSpeedsOK() {
        // check that every speed in <appearanceSpeeds> is defined
        java.util.Enumeration<String> e = map.getAppearanceIterator();
        String name;
        while (e.hasMoreElements()) {
            name = e.nextElement();
            assertNotNull( map.getAppearanceSpeed(name), "appearanceSpeed \""+name+"\" is defined");
            assertTrue( map.getSpeed(map.getAppearanceSpeed(name)) >= 0., "appearanceSpeed \""+name+"\" has value");
        }        
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initInternalLightManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initIdTagManager();
        map = new SignalSpeedMap();
    }

    @AfterEach
    public void tearDown() {
        map = null;
        JUnitUtil.tearDown();
    }
}
