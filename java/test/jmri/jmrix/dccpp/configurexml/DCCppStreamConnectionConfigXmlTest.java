package jmri.jmrix.dccpp.configurexml;

import jmri.util.JUnitUtil;
import org.junit.*;
import jmri.jmrix.dccpp.DCCppSystemConnectionMemo;
import jmri.jmrix.dccpp.DCCppStreamConnectionConfig;

/**
 * Tests for the DCCppStreamConnectionConfigXml class.
 *
 * @author Paul Bender Copyright (C) 2019
 */
public class DCCppStreamConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractStreamConnectionConfigXmlTestBase {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        xmlAdapter = new DCCppStreamConnectionConfigXml();
        cc = new DCCppStreamConnectionConfig();
    }

    @After
    @Override
    public void tearDown() {
        // if we've started a traffic controller, dispose of it
        if (cc.getAdapter() != null) {
            if (cc.getAdapter().getSystemConnectionMemo() != null) {
               ((DCCppSystemConnectionMemo)cc.getAdapter().getSystemConnectionMemo()).dispose();
            }
        }

        JUnitUtil.tearDown();
        xmlAdapter = null;
        cc = null;
    }
}
