package jmri.jmrix.ieee802154.serialdriver.configurexml;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.*;

import org.junit.jupiter.api.*;

import jmri.jmrix.ieee802154.serialdriver.ConnectionConfig;

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

    @AfterEach
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
        xmlAdapter = null;
        cc = null;
    }

    @Test
    @Override
    @Disabled("requires a traffic controller to pass")
    @ToDo("parent class test fails, needs further setup or re-implmentation")
    public void storeTest(){
    }

    @Test
    @Timeout(5000)
    @Override
    @Disabled("requires a traffic controller to pass")
    @ToDo("parent class test fails, needs further setup or re-implmentation")
    public void loadTest() throws jmri.configurexml.JmriConfigureXmlException {
    }

}
