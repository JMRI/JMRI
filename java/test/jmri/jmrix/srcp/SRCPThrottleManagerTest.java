package jmri.jmrix.srcp;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Test for the jmri.jmrix.srcp.SRCPThrottleManager class
 *
 * @author Bob Jacobsen
 * @author Paul Bender Copyright (C) 2016
 */
public class SRCPThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    private SRCPBusConnectionMemo memo;
    private SRCPTrafficController tc;

    @Override
    @BeforeEach
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        tc = new SRCPTrafficController() {
            @Override
            public void sendSRCPMessage(SRCPMessage m, SRCPListener reply) {
            } // prevent sending actual message when creating a throttle
        };
        memo = new SRCPBusConnectionMemo(tc, "A", 1);
        tm = new SRCPThrottleManager(memo);
    }

    @AfterEach
    public void tearDown() {
        tm.dispose();
        tm = null;
        memo.dispose();
        memo = null;
        tc.terminateThreads();
        tc = null;

        JUnitUtil.tearDown();
    }

}
