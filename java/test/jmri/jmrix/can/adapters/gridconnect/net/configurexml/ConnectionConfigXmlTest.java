package jmri.jmrix.can.adapters.gridconnect.net.configurexml;

import jmri.util.JUnitUtil;
import org.junit.*;
import jmri.jmrix.can.adapters.gridconnect.net.ConnectionConfig;

/**
 * ConnectionConfigXmlTest.java
 *
 * Test for the ConnectionConfigXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class ConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractNetworkConnectionConfigXmlTestBase {

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
