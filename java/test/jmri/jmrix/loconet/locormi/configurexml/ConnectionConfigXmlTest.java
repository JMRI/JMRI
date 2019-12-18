package jmri.jmrix.loconet.locormi.configurexml;

import java.awt.GraphicsEnvironment;
import jmri.util.JUnitUtil;
import jmri.util.JUnitAppender;
import jmri.util.junit.annotations.*;
import org.junit.*;
import jmri.jmrix.loconet.locormi.ConnectionConfig;

/**
 * ConnectionConfigXmlTest.java
 *
 * Description: tests for the ConnectionConfigXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class ConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractSerialConnectionConfigXmlTestBase {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        xmlAdapter = new ConnectionConfigXml();
        cc = new ConnectionConfig();
    }

    @After
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
        xmlAdapter = null;
        cc = null;
    }

    @Test
    @Override
    public void getInstanceTest() {
       super.getInstanceTest();
       JUnitAppender.assertErrorMessage("unexpected call to getInstance");
    }

    @Test(timeout=5000)
    @Override
    @Ignore("appears to leak state, causing issues with subsequent tests")
    @ToDo("this really should work in a headless environment.  Calling load causes a frame to be created.  The frame is left visible if the connection fails.")
    public void loadTest() throws jmri.configurexml.JmriConfigureXmlException {
       Assume.assumeFalse(GraphicsEnvironment.isHeadless());
       super.loadTest();
       JUnitAppender.assertErrorMessageStartsWith("Exception while trying to connect: java.rmi.ConnectException: Connection refused to host:");
       JUnitAppender.suppressErrorMessage("Error opening connection to");
    }

}
