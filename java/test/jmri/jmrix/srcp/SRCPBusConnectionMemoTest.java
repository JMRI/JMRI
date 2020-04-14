package jmri.jmrix.srcp;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * SRCPBusConnectionMemoTest.java
 *
 * Description:	tests for the jmri.jmrix.srcp.SRCPBusConnectionMemo class
 *
 * @author	Bob Jacobsen
 */
public class SRCPBusConnectionMemoTest extends jmri.jmrix.SystemConnectionMemoTestBase {


    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        JUnitUtil.setUp();
        scm = new SRCPBusConnectionMemo(new SRCPTrafficController() {
            @Override
            public void sendSRCPMessage(SRCPMessage m, SRCPListener reply) {
            }
            @Override
            public void transmitLoop(){
            }
            @Override
            public void receiveLoop(){
            }
        }, "A", 1);
    }

    @Override
    @Test
    public void testProvidesConsistManager(){
       Assert.assertFalse("Provides ConsistManager",scm.provides(jmri.ConsistManager.class));
    }

    @Override
    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }
}
