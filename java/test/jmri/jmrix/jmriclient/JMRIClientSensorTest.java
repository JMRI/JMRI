package jmri.jmrix.jmriclient;

import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * JMRIClientSensorTest.java
 *
 * Test for the jmri.jmrix.jmriclient.JMRIClientSensor class
 *
 * @author Bob Jacobsen
 * @author      Paul Bender Copyright (C) 2018
 */
public class JMRIClientSensorTest extends jmri.implementation.AbstractSensorTestBase {

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
        JMRIClientTrafficController tc = new JMRIClientTrafficController() {
            @Override
            public void sendJMRIClientMessage(JMRIClientMessage m, JMRIClientListener reply) {
                // do nothing to avoid null pointer when sending to non-existant
                // connection during test.
            }
        };
        t = new JMRIClientSensor(3, new JMRIClientSystemConnectionMemo(tc));
    }

    @AfterEach
    @Override
    public void tearDown() {
        t.dispose();
        JUnitUtil.clearShutDownManager(); // put in place because AbstractMRTrafficController implementing subclass was not terminated properly
        JUnitUtil.tearDown();
    }

}
