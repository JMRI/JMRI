package jmri.jmrix.rfid.configurexml;

import jmri.util.JUnitUtil;
import org.junit.*;
import jmri.jmrix.rfid.RfidSystemConnectionMemo;
import jmri.jmrix.rfid.RfidStreamConnectionConfig;

/**
 * Tests for the RfidStreamConnectionConfigXml class.
 *
 * @author Paul Bender Copyright (C) 2019
 */
public class RfidStreamConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractStreamConnectionConfigXmlTestBase {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        xmlAdapter = new RfidStreamConnectionConfigXml();
        cc = new RfidStreamConnectionConfig();
    }

    @After
    @Override
    public void tearDown() {
        // if we've started a traffic controller, dispose of it
        if (cc.getAdapter() != null) {
            if (cc.getAdapter().getSystemConnectionMemo() != null) {
               ((RfidSystemConnectionMemo)cc.getAdapter().getSystemConnectionMemo()).dispose();
            }
        }

        JUnitUtil.tearDown();
        xmlAdapter = null;
        cc = null;
    }
}
