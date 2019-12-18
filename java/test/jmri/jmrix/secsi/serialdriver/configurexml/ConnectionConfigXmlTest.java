package jmri.jmrix.secsi.serialdriver.configurexml;

import jmri.util.JUnitUtil;
import org.junit.*;
import jmri.jmrix.secsi.serialdriver.ConnectionConfig;

/**
 * Tests for the ConnectionConfigXml class
 *
 * @author Paul Bender  Copyright (C) 2016
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
        xmlAdapter = null;
        cc = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }
}
