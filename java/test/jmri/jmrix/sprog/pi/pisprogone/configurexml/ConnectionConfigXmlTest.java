package jmri.jmrix.sprog.pi.pisprogone.configurexml;

import jmri.util.JUnitUtil;
import org.junit.*;
import jmri.jmrix.sprog.SprogSystemConnectionMemo;
import jmri.jmrix.sprog.pi.pisprogone.ConnectionConfig;

/**
 * ConnectionConfigXmlTest.java
 *
 * Test for the ConnectionConfigXml class
 *
 * @author   Paul Bender  Copyright (C) 2016
 */
public class ConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractSerialConnectionConfigXmlTestBase {

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
        // if we've started a traffic controller, dispose of it
        if (cc.getAdapter() != null) {
            if (cc.getAdapter().getSystemConnectionMemo() != null) {
                if ( ((SprogSystemConnectionMemo)cc.getAdapter().getSystemConnectionMemo()).getSprogTrafficController() != null)
                    ((SprogSystemConnectionMemo)cc.getAdapter().getSystemConnectionMemo()).getSprogTrafficController().dispose();
            }
        }

        JUnitUtil.tearDown();
        xmlAdapter = null;
        cc = null;
    }
}
