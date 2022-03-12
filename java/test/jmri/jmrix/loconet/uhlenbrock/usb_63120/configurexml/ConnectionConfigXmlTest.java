package jmri.jmrix.loconet.uhlenbrock.usb_63120.configurexml;

import jmri.jmrix.loconet.uhlenbrock.usb_63120.ConnectionConfig;
import jmri.jmrix.loconet.uhlenbrock.usb_63120.UsbUhlenbrock63120Adapter;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test for the Uhlenbrock usb_63120 ConnectionConfigXml class.
 *
 * @author Egbert Broerse Copyright (C) 2021
 */
public class ConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractSerialConnectionConfigXmlTestBase {

    @Test
    public void testCTor() {
        UsbUhlenbrock63120Adapter t = new UsbUhlenbrock63120Adapter();
        Assertions.assertNotNull(t, "exists");
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
