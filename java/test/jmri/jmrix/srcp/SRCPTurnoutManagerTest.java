package jmri.jmrix.srcp;

import jmri.util.JUnitUtil;

import org.junit.Assert;
import org.junit.jupiter.api.*;

/**
 * SRCPTurnoutManagerTest.java
 *
 * Test for the jmri.jmrix.srcp.SRCPTurnoutManager class
 *
 * @author Bob Jacobsen
 */
public class SRCPTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    private SRCPBusConnectionMemo memo;

    @Override
    public String getSystemName(int i){
        return "A1T"+i;
    }

    @Test
    public void testCtor() {
        SRCPTurnoutManager m = new SRCPTurnoutManager(memo);
        Assert.assertNotNull(m);
    }

    @Test
    public void testBusCtor() {
        Assert.assertNotNull(l);
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        memo = new SRCPBusConnectionMemo(new SRCPTrafficController() {
            @Override
            public void sendSRCPMessage(SRCPMessage m, SRCPListener reply) {
            }
        }, "A", 1);

        l = new SRCPTurnoutManager(memo);
        memo.setTurnoutManager(l);
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }
}
