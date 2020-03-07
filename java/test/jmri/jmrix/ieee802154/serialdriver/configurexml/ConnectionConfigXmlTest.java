package jmri.jmrix.ieee802154.serialdriver.configurexml;

import jmri.util.JUnitUtil;
import jmri.util.junit.annotations.*;
import org.junit.*;
import jmri.jmrix.ieee802154.serialdriver.ConnectionConfig;

/**
 * ConnectionConfigXmlTest.java
 *
 * Description: tests for the ConnectionConfigXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
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
        JUnitUtil.tearDown();
        xmlAdapter = null;
        cc = null;
    }

    @Test
    @Override
    @Ignore("requires a traffic controller to pass")
    @ToDo("parent class test fails, needs further setup or re-implmentation")
    public void storeTest(){
    }

    @Test(timeout=5000)
    @Override
    @Ignore("requires a traffic controller to pass")
    @ToDo("parent class test fails, needs further setup or re-implmentation")
    public void loadTest() throws jmri.configurexml.JmriConfigureXmlException {
    }

}
