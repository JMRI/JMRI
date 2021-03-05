package jmri.jmrix.srcp;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;


/**
 * Tests for the jmri.jmrix.srcp.SRCPSensor class
 *
 * @author Bob Jacobsen
 * @author Paul Bender Copyright (C) 2018
 */
public class SRCPSensorTest extends jmri.implementation.AbstractSensorTestBase {

    @Override
    public int numListeners() {return 0;}

    @Override
    public void checkActiveMsgSent() {}

    @Override
    public void checkInactiveMsgSent() {}

    @Override
    public void checkStatusRequestMsgSent() {}

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        SRCPBusConnectionMemo sm = new SRCPBusConnectionMemo(new SRCPTrafficController() {
            @Override
            public void sendSRCPMessage(SRCPMessage m, SRCPListener reply) {
            }
        }, "A", 1);
        t = new SRCPSensor(1, sm);
    }

    @Override
    @AfterEach
    public void tearDown() {
        t.dispose();
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
