package jmri.jmrix.internal.configurexml;

import jmri.util.JUnitUtil;
import org.junit.*;
import jmri.jmrix.internal.ConnectionConfig;

/**
 * ConnectionConfigXmlTest.java
 *
 * Description: tests for the ConnectionConfigXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class ConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractSimulatorConnectionConfigXmlTestBase {

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

    @Test(timeout=5000)
    @Override
    @Ignore("occasionally causing ConncurrentModificationSetting while disabling connection")
    public void loadTest() throws jmri.configurexml.JmriConfigureXmlException {
       Assert.fail("test needs more setup");
    }
}

