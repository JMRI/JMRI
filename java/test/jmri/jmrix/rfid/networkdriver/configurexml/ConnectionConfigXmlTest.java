package jmri.jmrix.rfid.networkdriver.configurexml;

import jmri.util.*;

import org.junit.jupiter.api.*;

import jmri.jmrix.rfid.networkdriver.ConnectionConfig;

/**
 * ConnectionConfigXmlTest.java
 *
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
        JUnitAppender.assertWarnMessage("Could not parse port attribute: [Attribute: port=\"(none selected)\"]");
    }    

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
        xmlAdapter = null;
        cc = null;
    }

}

