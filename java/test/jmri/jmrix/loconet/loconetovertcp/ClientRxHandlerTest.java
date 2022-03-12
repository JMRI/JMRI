package jmri.jmrix.loconet.loconetovertcp;

import jmri.jmrix.loconet.LocoNetInterfaceScaffold;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for ClientRxHandler class.
 *
 * @author Paul Bender Copyright (C) 2017
 */
public class ClientRxHandlerTest {

    private LocoNetInterfaceScaffold lnis;
    LocoNetSystemConnectionMemo memo;
    
    @Test
    public void testCTor() {
        ClientRxHandler t = new ClientRxHandler("127.0.0.1", new java.net.Socket(), lnis);
        Assertions.assertNotNull(t, "exists");
        t.dispose();
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();

        memo = new LocoNetSystemConnectionMemo();
        jmri.InstanceManager.setDefault(LocoNetSystemConnectionMemo.class, memo); // register now to prevent having to wait for register() to complete (or test fail)
        // ensure memo exists in order to later use InstanceManager.getDefault()
        lnis = new LocoNetInterfaceScaffold(memo);
        memo.setLnTrafficController(lnis);
        memo.configureCommandStation(jmri.jmrix.loconet.LnCommandStationType.COMMAND_STATION_DCS100, true, false, true);
    }

    @AfterEach
    public void tearDown() {
        lnis = null;
        memo.dispose();
        JUnitUtil.tearDown();
    }

    // private final static Logger log = LoggerFactory.getLogger(ClientRxHandlerTest.class);

}
