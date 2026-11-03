package jmri.jmrix.marklin;

import java.util.ArrayList;
import java.util.List;

import jmri.Sensor;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for MarklinSensorManager
 * @author Paul Bender Copyright (C) 2017
 * @author Steve Young Copyright (C) 2024
 */
public class MarklinSensorManagerTest extends jmri.managers.AbstractSensorMgrTestBase {

    private MarklinSystemConnectionMemo memo;
    private MarklinTrafficControlScaffold tc;

    @Override
    public String getSystemName(int i) {
        return ("MS1:0"+i);
    }

    @Test
    @Override
    public void testDefaultSystemName() {
        // create
        Sensor t = l.provideSensor("MS1:0" + getNumToTest1());
        // check
        Assertions.assertNotNull(t);
        Assertions.assertEquals(t, l.getBySystemName(getSystemName(getNumToTest1())), "system name correct");
    }

    @Test
    @Override
    public void testProvideName() {
        // create
        Sensor t = l.provide("" + getSystemName(getNumToTest1()));
        // check
        Assertions.assertNotNull(t);
        Assertions.assertEquals(t, l.getBySystemName(getSystemName(getNumToTest1())), "system name correct");
    }

    @Test
    @Override
    @Disabled("Tested class requires further development")
    public void testMakeSystemNameWithPrefixNotASystemName(){}

    @Test
    @Override
    @Disabled("Tested class requires further development")
    public void testMakeSystemNameWithNoPrefixNotASystemName(){}

    @Test
    public void testCreateSystemName(){
        Assertions.assertDoesNotThrow( () -> {
            String name = l.createSystemName("1:1", memo.getSystemPrefix());
            Assertions.assertEquals("MS1:01", name);
        });

        Exception ex = Assertions.assertThrows(jmri.JmriException.class, () -> l.createSystemName("123", "M"));
        Assertions.assertNotNull(ex);
    }

    @Test
    public void testReplyS88Command() {
        Sensor sensor = l.provideSensor("MS1:01");
        Assertions.assertNotNull(sensor);
        sensor.setCommandedState(Sensor.INACTIVE);

        // A zeroed frame is not CS2_NATIVE, so should be ignored
        MarklinReply reply = new MarklinReply();
        ((MarklinSensorManager)l).reply(reply);
        Assertions.assertEquals(Sensor.INACTIVE, sensor.getState());

        // Build a CS2-fingerprinted S88 event frame (command 0x11, hash 0x0300)
        // contactId encodes module=1, contact=1 as 0x0101
        // byte0=0x00, byte1=(0x11<<1)|0=0x22, hash=0x03 0x00, DLC=0x08
        int[] frame = new int[13];
        frame[0] = 0x00;
        frame[1] = 0x22; // command 0x11, response=false
        frame[2] = 0x03; // hash high
        frame[3] = 0x00; // hash low
        frame[4] = 0x08; // DLC=8
        frame[5] = 0x01; // contactId high = module 1
        frame[6] = 0x01; // contactId low  = contact 1
        frame[7] = 0x00; // oldState=0
        frame[8] = 0x01; // newState=1 (active)
        // bytes 9-12: zero (time=0)

        reply = new MarklinReply(frame);
        ((MarklinSensorManager)l).reply(reply);
        Assertions.assertEquals(Sensor.ACTIVE, sensor.getState());

        frame[7] = 0x01; // oldState=1
        frame[8] = 0x00; // newState=0 (inactive)
        reply = new MarklinReply(frame);
        ((MarklinSensorManager)l).reply(reply);
        Assertions.assertEquals(Sensor.INACTIVE, sensor.getState());

        JUnitUtil.waitFor(() -> tc.getSentMessages().size()==1, "Sensor board Poll message sent");
        Assertions.assertEquals(MarklinMessage.sensorPollMessage(1), tc.getLastMessageSent());

    }

    @Test
    public void testReplyFeeCommand() {
        Sensor sensor = l.provideSensor("MS1:01");
        Sensor sensor2 = l.provideSensor("MS1:16");
        Assertions.assertNotNull(sensor);
        Assertions.assertNotNull(sensor2);
        sensor.setCommandedState(Sensor.INACTIVE);
        sensor2.setCommandedState(Sensor.ACTIVE);

        // Build a CS2-fingerprinted S88 poll reply (command 0x10, response=true, hash 0x0300)
        // MY_UID at bytes 5-8, module number at byte 9, status at bytes 10-11
        int[] frame = new int[13];
        frame[0] = 0x00;
        frame[1] = 0x21; // command 0x10, response=true: (0x10<<1)|1 = 0x21
        frame[2] = 0x03; // hash high
        frame[3] = 0x00; // hash low
        frame[4] = 0x07; // DLC=7 (UID 4 bytes + module 1 byte + state 2 bytes)
        // bytes 5-8: MY_UID (0x12345678)  -  these are the UID data bytes
        frame[5] = 0x12;
        frame[6] = 0x34;
        frame[7] = 0x56;
        frame[8] = 0x78;
        frame[9]  = 0x01; // S88 Module Number = 1
        frame[10] = 0x00; // status hi
        frame[11] = 0x01; // status lo (contact 1 = active)

        MarklinReply reply = new MarklinReply(frame);
        ((MarklinSensorManager)l).reply(reply);
        Assertions.assertEquals(Sensor.ACTIVE, sensor.getState());
        Assertions.assertEquals(Sensor.INACTIVE, sensor2.getState());

        frame[10] = 0x80; // status hi (contact 16 = active)
        frame[11] = 0x00; // status lo
        reply = new MarklinReply(frame);
        ((MarklinSensorManager)l).reply(reply);
        Assertions.assertEquals(Sensor.INACTIVE, sensor.getState());
        Assertions.assertEquals(Sensor.ACTIVE, sensor2.getState());
    }

    @Test
    public void testIncomingReplyGivesItsReceivedTime() {
        Sensor sensor = l.provideSensor("MS1:01");
        List<Long> times = new ArrayList<>();
        List<Integer> states = new ArrayList<>();
        sensor.addRawStateListener((s, oldState, newState, timeNanos) -> {
            states.add(newState);
            times.add(timeNanos);
        });

        MarklinReply event = new MarklinReply(s88EventModule1Contact1Active());
        event.setReceivedTime(1234L);
        ((MarklinSensorManager)l).reply(event);

        // module 1's poll reply, contact 1 inactive: dated too
        MarklinReply pollReply = new MarklinReply(s88PollReplyModule1(0x00, 0x00));
        pollReply.setReceivedTime(5678L);
        ((MarklinSensorManager)l).reply(pollReply);

        long before = System.nanoTime();
        // not stamped, no time recorded
        ((MarklinSensorManager)l).reply(new MarklinReply(s88EventModule1Contact1Active()));
        long after = System.nanoTime();

        Assertions.assertEquals(List.of(Sensor.ACTIVE, Sensor.INACTIVE, Sensor.ACTIVE), states);
        Assertions.assertEquals(1234L, times.get(0), "the S88 event's received time");
        Assertions.assertEquals(5678L, times.get(1), "the poll reply's received time");
        Assertions.assertTrue(times.get(2) >= before && times.get(2) <= after, "not stamped: dated when set");
    }

    // A CS2 S88 event frame: module 1, contact 1 goes active, as in testReplyS88Command
    private static int[] s88EventModule1Contact1Active() {
        int[] frame = new int[13];
        frame[1] = 0x22; // command 0x11, response=false
        frame[2] = 0x03; // hash high
        frame[4] = 0x08; // DLC=8
        frame[5] = 0x01; // contactId high = module 1
        frame[6] = 0x01; // contactId low  = contact 1
        frame[7] = 0x00; // oldState=0
        frame[8] = 0x01; // newState=1 (active)
        return frame;
    }

    // A CS2 S88 poll reply for module 1, as in testReplyFeeCommand
    private static int[] s88PollReplyModule1(int statusHi, int statusLo) {
        int[] frame = new int[13];
        frame[1] = 0x21; // command 0x10, response=true
        frame[2] = 0x03; // hash high
        frame[4] = 0x07; // DLC=7
        frame[5] = 0x12; // bytes 5-8: UID
        frame[6] = 0x34;
        frame[7] = 0x56;
        frame[8] = 0x78;
        frame[9] = 0x01; // S88 module number = 1
        frame[10] = statusHi;
        frame[11] = statusLo;
        return frame;
    }

    @BeforeEach
    @Override
    public void setUp() {
        JUnitUtil.setUp();
        tc = new MarklinTrafficControlScaffold();
        memo = new MarklinSystemConnectionMemo(tc);
        l = new MarklinSensorManager(memo);
        Assertions.assertNotNull(l, "exists");
    }

    @AfterEach
    public void tearDown() {
        Assertions.assertNotNull(memo);
        tc.dispose();
        memo.dispose();
        memo = null;
        tc = null;
        l = null;
        JUnitUtil.tearDown();

    }

    // private static final Logger log = LoggerFactory.getLogger(MarklinSensorManagerTest.class);

}
