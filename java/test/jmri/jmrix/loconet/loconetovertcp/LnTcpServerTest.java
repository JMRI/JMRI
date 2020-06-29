package jmri.jmrix.loconet.loconetovertcp;

import jmri.jmrix.loconet.LocoNetInterfaceScaffold;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Tests for LnTcpServer class.
 *
 * @author Paul Bender Copyright (C) 2016
 */
public class LnTcpServerTest {

    LocoNetSystemConnectionMemo memo;
    
    @Test
    public void getInstanceTest() {
        Assert.assertNotNull("Server getInstance", LnTcpServer.getDefault());
        LnTcpServer.getDefault().disable();  // turn the server off after enabled during creation.
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        memo = new LocoNetSystemConnectionMemo();
        // ensure memo exists in order to later use InstanceManager.getDefault()
        LocoNetInterfaceScaffold lnis = new LocoNetInterfaceScaffold(memo);
        memo.setLnTrafficController(lnis);
        memo.configureCommandStation(jmri.jmrix.loconet.LnCommandStationType.COMMAND_STATION_DCS100, true, false, true);
    }

    @AfterEach
    public void tearDown() {
        memo.dispose();
        memo = null;
        JUnitUtil.tearDown();
    }

}
