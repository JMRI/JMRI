package jmri.jmrix.oaktree.simulator.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import jmri.jmrix.oaktree.simulator.ConnectionConfig;

/**
 * Tests for the ConnectionConfigXml class
 *
 * @author Paul Bender  Copyright (C) 2016
 */
public class ConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractSimulatorConnectionConfigXmlTestBase {

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
        xmlAdapter = null;
        cc = null;
        JUnitUtil.clearShutDownManager(); // remove shutdown tasks left behind.
        JUnitUtil.tearDown();
    }
}

