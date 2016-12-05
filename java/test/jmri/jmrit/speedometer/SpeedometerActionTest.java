package jmri.jmrit.speedometer;

import apps.tests.Log4JFixture;
import jmri.InstanceManager;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

/**
 * Test simple functioning of SpeedometerAction
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class SpeedometerActionTest {

    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SpeedometerAction action = new SpeedometerAction();
        Assert.assertNotNull("exists", action);
    }

    @Test
    public void testStringCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SpeedometerAction action = new SpeedometerAction("Test SpeedometerAction");
        Assert.assertNotNull("exists", action);
    }

    @Test(expected=java.lang.IllegalArgumentException.class)
    public void testMakePanel(){
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        SpeedometerAction action = new SpeedometerAction("Test SpeedometerAction");
        action.makePanel(); // this should throw an IllegalArgumentException. 
    }

    @Before
    public void setUp() {
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.initInternalSensorManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
    }
}
