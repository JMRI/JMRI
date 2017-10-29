package jmri.jmrix.loconet.loconetovertcp;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for LnTcpServer class.
 *
 * @author Paul Bender Copyright (C) 2016
 *
 */
public class LnTcpServerTest {

    @Test
    public void getInstanceTest() {
        Assert.assertNotNull("Server getInstance", LnTcpServer.getDefault());
        LnTcpServer.getDefault().disable();  // turn the server off after enabled durring creation.
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
    }

    @After
    public void tearDown() {
        apps.tests.Log4JFixture.tearDown();
        JUnitUtil.resetInstanceManager();
    }

}
