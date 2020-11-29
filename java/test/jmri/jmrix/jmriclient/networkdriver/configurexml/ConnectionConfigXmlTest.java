package jmri.jmrix.jmriclient.networkdriver.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import jmri.jmrix.jmriclient.networkdriver.ConnectionConfig;

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

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
        xmlAdapter = null;
        cc = null;
    }
}
