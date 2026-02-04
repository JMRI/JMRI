package jmri.jmrix.openlcb;

import jmri.CommandStation;

import jmri.util.JUnitUtil;

import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.jmrix.can.TrafficControllerScaffold;

import org.openlcb.can.AliasMap;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 *
 * @author Bob Jacobsen  copyright 2025
 */
public class OlcbCommandStationTest {

    private TrafficControllerScaffold tcs = null;
    private CanSystemConnectionMemo memo = null;

    @Test
    public void testCTor() {
        CommandStation t = new OlcbCommandStation(memo);
        Assert.assertNotNull("exists",t);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        tcs = new TrafficControllerScaffold();
        memo = new CanSystemConnectionMemo();
        memo.setTrafficController(tcs);
        memo.store(new AliasMap(), org.openlcb.can.AliasMap.class);
        jmri.InstanceManager.setDefault(CanSystemConnectionMemo.class, memo);
    }

    @AfterEach
    public void tearDown() {
        memo.dispose();
        memo = null;
        tcs.terminateThreads();
        tcs = null;
        JUnitUtil.tearDown();
    }


}
