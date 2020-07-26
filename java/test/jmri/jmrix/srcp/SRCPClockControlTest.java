package jmri.jmrix.srcp;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.junit.Assert;

/**
 * SRCPClockControlTest.java
 *
 * Test for the jmri.jmrix.srcp.SRCPClockControl class
 *
 * @author Bob Jacobsen
 */
public class SRCPClockControlTest {

    @Test
    public void testCtor() {
        SRCPBusConnectionMemo sm = new SRCPBusConnectionMemo(new SRCPTrafficController() {
            @Override
            public void sendSRCPMessage(SRCPMessage m, SRCPListener reply) {
            }
        }, "A", 1);
        SRCPClockControl m = new SRCPClockControl(sm);
        Assert.assertNotNull(m);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
    }

    @AfterEach
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }
}
