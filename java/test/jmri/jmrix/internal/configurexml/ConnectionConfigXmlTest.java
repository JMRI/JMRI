package jmri.jmrix.internal.configurexml;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

import jmri.jmrix.internal.ConnectionConfig;

/**
 * ConnectionConfigXmlTest.java
 *
 * Test for the ConnectionConfigXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
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
        JUnitUtil.tearDown();
        xmlAdapter = null;
        cc = null;
    }

    @Test
    @Timeout(5000)
    @Override
    @Disabled("occasionally causing ConncurrentModificationSetting while disabling connection")
    public void loadTest() throws jmri.configurexml.JmriConfigureXmlException {
       Assert.fail("test needs more setup");
    }
}

