package jmri.jmrix.loconet.loconetovertcp;

import apps.tests.Log4JFixture;
import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Test simple functioning of LnTcpServerFrame
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class LnTcpServerFrameTest {

    @Test
    public void testGetDefault() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LnTcpServerFrame action = LnTcpServerFrame.getDefault();
        Assert.assertNotNull("exists", action);
        action.dispose();
    }

    @Test
    public void testGetInstance() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LnTcpServerFrame action = LnTcpServerFrame.getInstance();
        Assert.assertNotNull("exists", action);
        action.dispose();
    }

    @Before
    public void setUp() {
        Log4JFixture.setUp();
        JUnitUtil.resetInstanceManager();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        JUnitUtil.resetInstanceManager();
        Log4JFixture.tearDown();
    }
}
