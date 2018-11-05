package jmri.jmrix.loconet.locormi.configurexml;

import jmri.util.JUnitUtil;
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
    @Ignore("generates error message when run")
    public void getInstanceTest() {
    }

    @Test(timeout=5000)
    @Override
    @Ignore("generates error message when run")
    public void loadTest() throws jmri.configurexml.JmriConfigureXmlException {
    }

}
