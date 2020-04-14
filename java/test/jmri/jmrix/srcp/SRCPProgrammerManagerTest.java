package jmri.jmrix.srcp;

import jmri.util.JUnitUtil;
import org.junit.Test;
import org.junit.After;
import org.junit.Before;
import org.junit.Assert;

/**
 * SRCPProgrammerManagerTest.java
 *
 * Description: tests for the jmri.jmrix.srcp.SRCPProgrammerManager class
 *
 * @author Bob Jacobsen
 */
public class SRCPProgrammerManagerTest {

    @Test
    public void testCtor() {
        SRCPBusConnectionMemo sm = new SRCPBusConnectionMemo(new SRCPTrafficController() {
            @Override
            public void sendSRCPMessage(SRCPMessage m, SRCPListener reply) {
            }
        }, "A", 1);
        SRCPProgrammerManager s = new SRCPProgrammerManager(new SRCPProgrammer(sm), sm);
        Assert.assertNotNull(s);
    }

    @Before
    public void setUp() {
        JUnitUtil.setUp();
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }
}
