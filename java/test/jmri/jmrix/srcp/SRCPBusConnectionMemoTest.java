package jmri.jmrix.srcp;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * SRCPBusConnectionMemoTest.java
 * <p>
 * Test for the jmri.jmrix.srcp.SRCPBusConnectionMemo class
 *
 * @author Bob Jacobsen
 */
public class SRCPBusConnectionMemoTest extends SystemConnectionMemoTestBase<SRCPBusConnectionMemo> {

    private SRCPSystemConnectionMemo memo;
    private SRCPTrafficController tc;

    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        tc = new SRCPTrafficController() {
            @Override
            public void sendSRCPMessage(SRCPMessage m, SRCPListener reply) {
            }

            @Override
            public void transmitLoop() {
            }

            @Override
            public void receiveLoop() {
            }
        };
        memo = new SRCPSystemConnectionMemo(tc);
        scm = memo.getMemo(1);
    }

    @Override
    @Test
    public void testProvidesConsistManager() {
        Assert.assertFalse("Provides ConsistManager", scm.provides(jmri.ConsistManager.class));
    }

    @Override
    @After
    public void tearDown() {
        tc.terminateThreads();
        memo.dispose();
        JUnitUtil.tearDown();
    }
}
