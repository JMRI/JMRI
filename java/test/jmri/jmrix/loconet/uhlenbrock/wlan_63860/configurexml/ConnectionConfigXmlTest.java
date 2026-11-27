package jmri.jmrix.loconet.uhlenbrock.wlan_63860.configurexml;

import jmri.jmrix.loconet.uhlenbrock.wlan_63860.ConnectionConfig;
import jmri.util.JUnitUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

/**
 * Test for the Uhlenbrock wlan_63860 ConnectionConfigXml class.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class ConnectionConfigXmlTest extends jmri.jmrix.configurexml.AbstractNetworkConnectionConfigXmlTestBase {

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
