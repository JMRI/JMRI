package jmri.jmrix.loconet.uhlenbrock.usb_63120.configurexml;

import jmri.jmrix.loconet.locobufferusb.ConnectionConfig;
import jmri.jmrix.loconet.locobufferusb.configurexml.ConnectionConfigXml;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for the usb_63120 ConnectionConfigXml class.
 *
 * @author   Paul Bender  Copyright (C) 2016, 2021
 */
public class ConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractSerialConnectionConfigXmlTestBase {

    @Test
    public void testCtor() {
        jmri.jmrix.loconet.uhlenbrock.configurexml.ConnectionConfigXml ccx = new jmri.jmrix.loconet.uhlenbrock.configurexml.ConnectionConfigXml();
        Assertions.assertNotNull(ccx, "Instance is created");
    }

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
