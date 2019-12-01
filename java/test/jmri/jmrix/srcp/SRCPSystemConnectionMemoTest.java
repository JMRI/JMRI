package jmri.jmrix.srcp;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * SRCPSystemConnectionMemoTest.java
 *
 * Description:	tests for the jmri.jmrix.srcp.SRCPSystemConnectionMemo class
 *
 * @author	Bob Jacobsen
 */
public class SRCPSystemConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {

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
    }

    @Override
    @Test
    public void testProvidesConsistManager(){
       Assert.assertFalse("Provides ConsistManager",scm.provides(jmri.ConsistManager.class));
    }

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        SRCPTrafficController et = new SRCPTrafficController() {
            @Override
            public void sendSRCPMessage(SRCPMessage m, SRCPListener l) {
                // we aren't actually sending anything to a layout.
            }
            @Override
            public void transmitLoop(){
            }
            @Override
            public void receiveLoop(){
            }
        };
        scm = new SRCPSystemConnectionMemo("D", "SRCP", et);
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }
}
