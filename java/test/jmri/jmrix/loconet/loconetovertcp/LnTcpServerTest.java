package jmri.jmrix.loconet.loconetovertcp;

import jmri.jmrix.loconet.LocoNetInterfaceScaffold;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for LnTcpServer class.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class LnTcpServerTest {

    @Test
    public void getInstanceTest() {
        Assert.assertNotNull("Server getInstance", LnTcpServer.getDefault());
        LnTcpServer.getDefault().disable();  // turn the server off after enabled during creation.
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo();
        // ensure memo exists in order to later use InstanceManager.getDefault()
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold(memo);
        memo.setLnTrafficController(lnis);
        memo.configureCommandStation(jmri.jmrix.loconet.LnCommandStationType.COMMAND_STATION_DCS100, true, false, true);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
        JUnitUtil.resetInstanceManager();
    }

}
