package jmri.jmrix.marklin.cdb.serialdriver.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
 
/**
 * Tests for the ConnectionConfigXml class.
 * @author Steve Young Copyright (C) 2024
 */
public class ConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractSerialConnectionConfigXmlTestBase {

    @Test
    @Override
    public void storeTest() {
        super.storeTest();
        jmri.util.JUnitAppender.suppressErrorMessage("No usable ports returned");
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        xmlAdapter = new ConnectionConfigXml();
        cc = new jmri.jmrix.marklin.cdb.serialdriver.ConnectionConfig();
    }

    @AfterEach
    @Override
    public void tearDown() {
        xmlAdapter = null;
        cc = null;
        JUnitUtil.tearDown();
    }

}
