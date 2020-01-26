package jmri.jmrix.srcp;

import jmri.util.JUnitUtil;
import org.junit.After;
import org.junit.Before;

/**
 * SRCPThrottleManagerTest.java
 *
 * Description:	tests for the jmri.jmrix.srcp.SRCPThrottleManager class
 *
 * @author	Bob Jacobsen
 * @author      Paul Bender Copyright (C) 2016	
 */
public class SRCPThrottleManagerTest extends jmri.managers.AbstractThrottleManagerTestBase {

    // The minimal setup for log4J
    @Override
    @Before
    public void setUp() {
        jmri.util.JUnitUtil.setUp();
        SRCPBusConnectionMemo sm = new SRCPBusConnectionMemo(new SRCPTrafficController() {
            @Override
            public void sendSRCPMessage(SRCPMessage m, SRCPListener reply) {
            }
        }, "A", 1);

        tm = new SRCPThrottleManager(sm);
    }

    @After
    public void tearDown() {
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }
}
