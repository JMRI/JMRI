package jmri.jmrix.srcp;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * SRCPTurnoutManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.srcp.SRCPTurnoutManager class
 *
 * @author	Bob Jacobsen
 */
public class SRCPTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    @Override
    public String getSystemName(int i){
        return "A1T"+i;
    }

    @Test
    public void testCtor() {
        SRCPTurnoutManager m = new SRCPTurnoutManager();
        Assert.assertNotNull(m);
    }

    @Test
    public void testBusCtor() {
        Assert.assertNotNull(l);
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        SRCPBusConnectionMemo memo = new SRCPBusConnectionMemo(new SRCPTrafficController() {
            @Override
            public void sendSRCPMessage(SRCPMessage m, SRCPListener reply) {
            }
        }, "A", 1);

        l = new SRCPTurnoutManager(memo, memo.getBus());
        memo.setTurnoutManager(l);
    }

    @After
    public void tearDown() {
        JUnitUtil.tearDown();
    }
}
