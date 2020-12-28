package jmri.jmrix.loconet.uhlenbrock.usb_63120.configurexml;

import jmri.jmrix.loconet.locobufferusb.ConnectionConfig;
import jmri.jmrix.loconet.locobufferusb.configurexml.ConnectionConfigXml;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Test for the ConnectionConfigXml class
 *
 * @author   Paul Bender  Copyright (C) 2016, 2020
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

}
