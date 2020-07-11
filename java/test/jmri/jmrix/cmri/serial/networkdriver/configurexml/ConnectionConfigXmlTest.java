package jmri.jmrix.cmri.serial.networkdriver.configurexml;

import jmri.util.JUnitUtil;
import jmri.util.JUnitAppender;

import org.junit.jupiter.api.*;

import jmri.jmrix.cmri.serial.networkdriver.ConnectionConfig;

/**
 * Test for the ConnectionConfigXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class ConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractNetworkConnectionConfigXmlTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        xmlAdapter = new ConnectionConfigXml();
        cc = new ConnectionConfig();
    }

    @Test
    @Timeout(5000)
    @Override
    public void loadTest() throws jmri.configurexml.JmriConfigureXmlException {
        super.loadTest();
        // the port attribute for testing is "(none selected)", which isn't an int
        jmri.util.JUnitAppender.assertWarnMessage("Could not parse port attribute: [Attribute: port=\"(none selected)\"]");
    }    

    @AfterEach
    @Override
    public void tearDown() {
        xmlAdapter = null;
        cc = null;
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }
}
