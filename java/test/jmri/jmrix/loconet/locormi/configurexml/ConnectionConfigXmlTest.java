package jmri.jmrix.loconet.locormi.configurexml;

import jmri.util.JUnitUtil;
import org.junit.*;

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
    public void setUp() {
        JUnitUtil.setUp();
        xmlAdapter = new ConnectionConfigXml();
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
        xmlAdapter = null;
    }

    @Test
    @Ignore("generates error message when run")
    public void getInstanceTest() {
    }
}
