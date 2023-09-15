package jmri.jmrix.dcc4pc.serialdriver.configurexml;

import jmri.util.*;

import org.junit.jupiter.api.*;

import jmri.jmrix.dcc4pc.serialdriver.ConnectionConfig;

/**
 * ConnectionConfigXmlTest.java
 *
 * Test for the ConnectionConfigXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class ConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractSerialConnectionConfigXmlTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        xmlAdapter = new ConnectionConfigXml();
        cc = new ConnectionConfig();
    }

    @Test
    @Override
    public void loadTest() throws jmri.configurexml.JmriConfigureXmlException {
        super.loadTest();
        JUnitAppender.suppressErrorMessageStartsWith("No usable ports returned");
        JUnitAppender.suppressErrorMessageStartsWith("Serial port (none selected) not found");
        JUnitAppender.suppressErrorMessageStartsWith("Load Error: Serial port (none selected) not found");
    }

    @Test
    @Override
    public void storeTest() {
        super.storeTest();
        JUnitAppender.suppressErrorMessageStartsWith("No usable ports returned");
    }

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
        xmlAdapter = null;
        cc = null;
    }

}

