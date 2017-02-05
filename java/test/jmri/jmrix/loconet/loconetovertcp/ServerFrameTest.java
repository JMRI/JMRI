package jmri.jmrix.loconet.loconetovertcp;

import apps.tests.Log4JFixture;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import java.awt.GraphicsEnvironment;

/**
 * Test simple functioning of ServerFrame
 *
 * @author	Paul Bender Copyright (C) 2016
 */
public class ServerFrameTest {

    @Test
    public void testGetInstance() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ServerFrame action = ServerFrame.getInstance();
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
