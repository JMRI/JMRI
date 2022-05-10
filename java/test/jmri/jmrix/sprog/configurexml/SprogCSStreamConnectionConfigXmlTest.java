package jmri.jmrix.sprog.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import jmri.jmrix.sprog.SprogSystemConnectionMemo;
import jmri.jmrix.sprog.SprogCSStreamConnectionConfig;

/**
 * Tests for the SprogCSStreamConnectionConfigXml class.
 *
 * @author Paul Bender Copyright (C) 2019
 */
public class SprogCSStreamConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractStreamConnectionConfigXmlTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        xmlAdapter = new SprogCSStreamConnectionConfigXml();
        cc = new SprogCSStreamConnectionConfig();
    }

    @AfterEach
    @Override
    public void tearDown() {
        // if we've started a traffic controller, dispose of it
        if (cc.getAdapter() != null) {
            if (cc.getAdapter().getSystemConnectionMemo() != null) {
                if ( ((SprogSystemConnectionMemo)cc.getAdapter().getSystemConnectionMemo()).getSprogTrafficController() != null)
                    ((SprogSystemConnectionMemo)cc.getAdapter().getSystemConnectionMemo()).getSprogTrafficController().dispose();
            }
        }
        xmlAdapter = null;
        cc = null;
        JUnitUtil.tearDown();
    }
}
