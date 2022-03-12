package jmri.jmrix.loconet.hexfile.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import jmri.jmrix.loconet.hexfile.ConnectionConfig;

/**
 * ConnectionConfigXmlTest.java
 *
 * Test for the ConnectionConfigXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class ConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractSimulatorConnectionConfigXmlTestBase {

    /**
     * Ignored in this test as adapter does not store reconnect details.
     * {@inheritDoc}
     */
    @Override
    protected void testReconnectXml(jmri.jmrix.ConnectionConfig cc,org.jdom2.Element e){
    }
    
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
        JUnitUtil.resetWindows(false,false);
        xmlAdapter = null;
        cc = null;
        JUnitUtil.tearDown();
    }
}
