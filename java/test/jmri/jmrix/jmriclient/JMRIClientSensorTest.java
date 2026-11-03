package jmri.jmrix.jmriclient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import jmri.Sensor;
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

    @Test
    public void testIncomingReplyGivesItsReceivedTime() {
        List<Long> times = new ArrayList<>();
        List<Integer> states = new ArrayList<>();
        t.addRawStateListener((sensor, oldState, newState, timeNanos) -> {
            states.add(newState);
            times.add(timeNanos);
        });

        JMRIClientReply rActive = new JMRIClientReply("SENSOR " + t.getSystemName() + " ACTIVE\n");
        rActive.setReceivedTime(1234L);
        ((JMRIClientSensor) t).reply(rActive);

        long before = System.nanoTime();
        // not stamped, no time recorded
        ((JMRIClientSensor) t).reply(new JMRIClientReply("SENSOR " + t.getSystemName() + " INACTIVE\n"));
        long after = System.nanoTime();

        assertEquals(List.of(Sensor.ACTIVE, Sensor.INACTIVE), states);
        assertEquals(1234L, times.get(0), "the time the reply was received");
        assertTrue(times.get(1) >= before && times.get(1) <= after, "not stamped: dated when set");
    }

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
