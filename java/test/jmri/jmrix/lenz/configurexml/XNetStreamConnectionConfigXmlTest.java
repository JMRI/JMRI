package jmri.jmrix.lenz.configurexml;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

import jmri.jmrix.lenz.XNetSystemConnectionMemo;
import jmri.jmrix.lenz.XNetStreamConnectionConfig;

/**
 * Tests for the XNetStreamConnectionConfigXml class.
 *
 * @author Paul Bender Copyright (C) 2019
 */
public class XNetStreamConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractStreamConnectionConfigXmlTestBase {

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        xmlAdapter = new XNetStreamConnectionConfigXml();
        cc = new XNetStreamConnectionConfig();
    }

    @AfterEach
    @Override
    public void tearDown() {
        // if we've started a traffic controller, dispose of it
        if (cc.getAdapter() != null) {
            if (cc.getAdapter().getSystemConnectionMemo() != null) {
                ((XNetSystemConnectionMemo)cc.getAdapter().getSystemConnectionMemo()).dispose();
            }
        }

        JUnitUtil.tearDown();
        xmlAdapter = null;
        cc = null;
    }
}
