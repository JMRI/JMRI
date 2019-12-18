package jmri.jmrix.loconet.bluetooth.configurexml;

import jmri.util.JUnitUtil;
import org.junit.*;
import jmri.jmrix.loconet.bluetooth.ConnectionConfig;

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
        /* loading the details causes errors.  not setting the value
           of cc causes tests in the abstract class to be skipped  with
           an Assume */
        //cc = new ConnectionConfig();
    }

    @After
    @Override
    public void tearDown() {
        JUnitUtil.tearDown();
        xmlAdapter = null;
        cc = null;
    }
}
