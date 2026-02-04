package jmri.jmrix.marklin;

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

        MarklinReply reply = new MarklinReply();
        ((MarklinSensorManager)l).reply(reply);
        Assertions.assertEquals(Sensor.INACTIVE, sensor.getState());

        reply.setCommand(MarklinConstants.S88EVENT);
        reply.setElement(MarklinConstants.CANADDRESSBYTE1, 0); // Module 1 hi
        reply.setElement(MarklinConstants.CANADDRESSBYTE2, 1); // Module 1 lo
        reply.setElement(MarklinConstants.CANADDRESSBYTE3, 0); // Contact 1 hi
        reply.setElement(MarklinConstants.CANADDRESSBYTE4, 1); // Contact 1 lo
        reply.setElement(9, 0x00); // Sensor ACTIVE
        reply.setElement(10, 0x01); // Sensor ACTIVE

        ((MarklinSensorManager)l).reply(reply);
        Assertions.assertEquals(Sensor.ACTIVE, sensor.getState());

        reply.setElement(9, 0x01); // Sensor INACTIVE
        reply.setElement(10, 0x00); // SENSOR INACTIVE
        ((MarklinSensorManager)l).reply(reply);
        Assertions.assertEquals(Sensor.INACTIVE, sensor.getState());

        JUnitUtil.waitFor(() -> tc.getSentMessages().size()==1, "Sensor booard Poll message sent");
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

        MarklinReply reply = new MarklinReply();
        reply.setCommand(MarklinConstants.FEECOMMANDSTART);
        reply.setElement(9, 0x01); // S88 Module Number
        reply.setElement(10, 0b00000000); // status hi
        reply.setElement(11, 0b00000001); // status lo

        ((MarklinSensorManager)l).reply(reply);
        Assertions.assertEquals(Sensor.ACTIVE, sensor.getState());
        Assertions.assertEquals(Sensor.INACTIVE, sensor2.getState());

        reply.setElement(10, 0b10000000); // status hi
        reply.setElement(11, 0b00000000); // status lo
        ((MarklinSensorManager)l).reply(reply);
        Assertions.assertEquals(Sensor.INACTIVE, sensor.getState());
        Assertions.assertEquals(Sensor.ACTIVE, sensor2.getState());
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

    // private final static Logger log = LoggerFactory.getLogger(MarklinSensorManagerTest.class);

}
