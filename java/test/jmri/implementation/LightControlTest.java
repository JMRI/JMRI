package jmri.implementation;

import jmri.Light;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the LightControl class
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class LightControlTest {

    @Test
    public void testCtor() {
        LightControl l = new LightControl();
        Assert.assertNotNull("LightControl not null", l);
    }

    @Test
    public void testCLighttor() {
        Light o = new AbstractLight("IL1","test light"){
        };
        LightControl l = new LightControl(o);
        Assert.assertNotNull("LightControl not null", l);
    }
    
    @Test
    public void testEquals() {
        Light o = new AbstractLight("IL1","test light"){
        };
        LightControl l1 = new LightControl(o);
        
        Assert.assertFalse(l1.equals(null));
        Assert.assertTrue(l1.equals(l1));
        Assert.assertFalse(l1.equals(""));
        
        LightControl l2 = new LightControl(o);
        Assert.assertTrue(l1.equals(l2));
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initRailComManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }

}
