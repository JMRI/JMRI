package jmri.jmrix.loconet.loconetovertcp;

import jmri.jmrix.loconet.LocoNetInterfaceScaffold;
import jmri.jmrix.loconet.LocoNetSystemConnectionMemo;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for ClientRxHandler class.
 *
 * @author Paul Bender Copyright (C) 2017	
 */
public class ClientRxHandlerTest {

    private LocoNetInterfaceScaffold lnis;

    @Test
    public void testCTor() {
        ClientRxHandler t = new ClientRxHandler("127.0.0.1", new java.net.Socket(), lnis);
        Assert.assertNotNull("exists", t);
        t.dispose();
    }

    // The minimal setup for log4J
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetInstanceManager();

        LocoNetSystemConnectionMemo memo = new LocoNetSystemConnectionMemo();
        // ensure memo exists in order to later use InstanceManager.getDefault()
        lnis = new LocoNetInterfaceScaffold(memo);
        memo.setLnTrafficController(lnis);
        memo.configureCommandStation(jmri.jmrix.loconet.LnCommandStationType.COMMAND_STATION_DCS100, true, false, true);
    }

    @After
    public void tearDown() {
        lnis = null;
        JUnitUtil.tearDown();
        JUnitUtil.resetInstanceManager();
    }

    // private final static Logger log = LoggerFactory.getLogger(ClientRxHandlerTest.class);

}
