package jmri.jmrix.can.adapters.gridconnect.net.configurexml;

import jmri.util.JUnitUtil;
import org.junit.*;
import jmri.jmrix.can.adapters.gridconnect.net.ConnectionConfig;

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
    public void setUp() {
        JUnitUtil.setUp();
        xmlAdapter = new ConnectionConfigXml();
        cc = new ConnectionConfig();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
        xmlAdapter = null;
        cc = null;
    }
}
