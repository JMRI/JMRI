package jmri.jmrix.srcp;

import jmri.jmrix.SystemConnectionMemoTestBase;
import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * Test for the jmri.jmrix.srcp.SRCPSystemConnectionMemo class
 *
 * @author Bob Jacobsen
 */
public class SRCPSystemConnectionMemoTest extends SystemConnectionMemoTestBase<SRCPSystemConnectionMemo> {

    @Test
    public void testTCCtor() {
        SRCPTrafficController et = new SRCPTrafficController() {
            @Override
            public void sendSRCPMessage(SRCPMessage m, SRCPListener l) {
                // we aren't actually sending anything to a layout.
            }
        };
        SRCPSystemConnectionMemo m = new SRCPSystemConnectionMemo(et);
        Assert.assertNotNull(m);
        m.getTrafficController().terminateThreads();
        m.dispose();
    }

    @Override
    @Test
    public void testProvidesConsistManager() {
        Assert.assertFalse("Provides ConsistManager", scm.provides(jmri.ConsistManager.class));
    }

    @Test
    public void testGetMemo() {
        Assert.assertNotNull("Get Memo", scm.getMemo(0));
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        SRCPTrafficController et = new SRCPTrafficController() {
            @Override
            public void sendSRCPMessage(SRCPMessage m, SRCPListener l) {
                // we aren't actually sending anything to a layout.
            }

            @Override
            public void transmitLoop() {
            }

            @Override
            public void receiveLoop() {
            }
        };
        scm = new SRCPSystemConnectionMemo("D", "SRCP", et);
    }

    @Override
    @AfterEach
    public void tearDown() {
        SRCPTrafficController trafficController = scm.getTrafficController();
        scm.dispose();
        trafficController.terminateThreads();
        scm = null;
        JUnitUtil.tearDown();
    }
}
