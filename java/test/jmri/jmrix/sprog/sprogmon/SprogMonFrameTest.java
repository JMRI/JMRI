package jmri.jmrix.sprog.sprogmon;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import jmri.InstanceManager;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

/**
 * Test simple functioning of SprogMonFrame 
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class SprogMonFrameTest {


    @Test
    public void testCtor() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless()); 
        SprogMonFrame action = new SprogMonFrame(new jmri.jmrix.sprog.SprogSystemConnectionMemo());
        Assert.assertNotNull("exists", action);
    }

    @Before
    public void setUp() {
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
    }
}
