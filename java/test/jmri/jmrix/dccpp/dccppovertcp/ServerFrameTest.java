package jmri.jmrix.dccpp.dccppovertcp;

import java.awt.GraphicsEnvironment;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for ServerFrame class.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class ServerFrameTest {

    @Test
    public void getInstanceTest() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        ServerFrame f = ServerFrame.getInstance();
        Assert.assertNotNull("ServerFrame getInstance", f);
        f.dispose();
    }

    @Before
    public void setUp() {
        apps.tests.Log4JFixture.setUp();
        jmri.util.JUnitUtil.resetInstanceManager();
        jmri.util.JUnitUtil.initDefaultUserMessagePreferences();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
        jmri.util.JUnitUtil.resetInstanceManager();
    }

}
