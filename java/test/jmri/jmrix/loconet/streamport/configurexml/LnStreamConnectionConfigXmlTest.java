package jmri.jmrix.loconet.streamport.configurexml;

import jmri.util.JUnitUtil;
import org.junit.*;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.jmrix.loconet.streamport.LnStreamConnectionConfig;

/**
 * Tests for the LnStreamConnectionConfigXml class.
 *
 * @author Paul Bender Copyright (C) 2019
 */
public class LnStreamConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractStreamConnectionConfigXmlTestBase {

    // The minimal setup for log4J
    @Before
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        xmlAdapter = new LnStreamConnectionConfigXml();
        cc = new LnStreamConnectionConfig();
    }

    @After
    @Override
    public void tearDown() {
        // if we've started a traffic controller, dispose of it
        if (cc.getAdapter() != null) {
            if (cc.getAdapter().getSystemConnectionMemo() != null) {
                if ( ((LocoNetSystemConnectionMemo)cc.getAdapter().getSystemConnectionMemo()).getLnTrafficController() != null)
                    ((LocoNetSystemConnectionMemo)cc.getAdapter().getSystemConnectionMemo()).getLnTrafficController().dispose();
            }
        }

        JUnitUtil.tearDown();
        xmlAdapter = null;
        cc = null;
    }
}
