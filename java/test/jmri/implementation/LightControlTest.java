package jmri.implementation;

import jmri.Light;
import org.junit.Assert;
import org.junit.After;
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

    // The minimal setup for log4J
    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initInternalTurnoutManager();
        jmri.util.JUnitUtil.initInternalLightManager();
        jmri.util.JUnitUtil.initInternalSensorManager();
        jmri.util.JUnitUtil.initRailComManager();
    }

    @After
    public void tearDown() {
        jmri.util.JUnitUtil.resetInstanceManager();
        apps.tests.Log4JFixture.tearDown();
    }

}
