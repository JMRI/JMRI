package jmri.jmrix.easydcc.networkdriver.configurexml;

import jmri.util.JUnitUtil;
import org.junit.*;
import jmri.jmrix.easydcc.networkdriver.ConnectionConfig;

/**
 * ConnectionConfigXmlTest.java
 *
 * Description: tests for the ConnectionConfigXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class ConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractNetworkConnectionConfigXmlTestBase {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        xmlAdapter = new ConnectionConfigXml();
        cc = new ConnectionConfig();
    }

    @Test(timeout=5000)
    @Override
    public void loadTest() throws jmri.configurexml.JmriConfigureXmlException {
        super.loadTest();
        // the port attribute for testing is "(none selected)", which isn't an int
        jmri.util.JUnitAppender.assertWarnMessage("Could not parse port attribute: [Attribute: port=\"(none selected)\"]");
    }    

    @After
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
        xmlAdapter = null;
        cc = null;
    }
}
