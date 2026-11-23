package jmri.jmrix.lenz;

import jmri.Sensor;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.util.JUnitUtil;

import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for the jmri.jmrix.lenz.XNetTurnoutManager class.
 *
 * @author Bob Jacobsen Copyright 2004
 */
public class XNetTurnoutManagerTest extends jmri.managers.AbstractTurnoutMgrTestBase {

    @Override
    public String getSystemName(int i) {
        return "XT" + i;
    }

    protected XNetInterfaceScaffold lnis;

    @Test
    @Override
    public void testMisses() {
        // try to get nonexistant turnouts
        assertThat(l.getByUserName("foo")).isNull();
        assertThat(l.getBySystemName("bar")).isNull();
    }

    @Test
    public void testXNetMessages() {
        // send messages for 21, 22
        // notify that somebody else changed it...
        XNetReply m1 = new XNetReply();
        m1.setElement(0, 0x42);
        m1.setElement(1, 0x05);
        m1.setElement(2, 0x02);
        m1.setElement(3, 0x45);
        lnis.sendTestMessage(m1);

        // notify that somebody else changed it...
        XNetReply m2 = new XNetReply();
        m2.setElement(0, 0x42);
        m2.setElement(1, 0x05);
        m2.setElement(2, 0x04);
        m2.setElement(3, 0x43);
        lnis.sendTestMessage(m2);

        // try to get turnouts to see if they exist
        Turnout xt21 = l.getBySystemName("XT21");
        Turnout xt22 = l.getBySystemName("XT22");
        assertThat(xt21).isNotNull();
        assertThat(xt22).isNotNull();

        assertThat(l.getNamedBeanSet()).containsOnly(xt21,xt22);
    }

    @Test
    public void testMixedBroadcastTurnoutsFirst() throws jmri.JmriException {
        checkMixedBroadcast(false);
    }

    @Test
    public void testMixedBroadcastEncodersFirst() throws jmri.JmriException {
        checkMixedBroadcast(true);
    }

    private void checkMixedBroadcast(boolean encodersFirst) throws jmri.JmriException {
        XNetSensorManager sensors = new XNetSensorManager(lnis.getSystemConnectionMemo());
        try {
            XNetTurnout unrelatedTurnout = (XNetTurnout) l.provideTurnout("XT81");
            unrelatedTurnout.newKnownState(Turnout.CLOSED);
            Sensor unrelatedSensor = sensors.provideSensor("XS121");
            unrelatedSensor.setKnownState(Sensor.ACTIVE);

            XNetReply reply = new XNetReply(encodersFirst
                    ? "46 06 45 05 09 07 36 00"
                    : "46 05 09 07 36 06 45 00");
            reply.setParity();
            assertThat(reply.checkParity()).isTrue();
            lnis.sendTestMessage(reply);

            assertThat(l.getNamedBeanSet()).extracting(Turnout::getSystemName)
                    .containsExactlyInAnyOrder("XT21", "XT22", "XT31", "XT32", "XT81");
            assertThat(sensors.getNamedBeanSet()).extracting(Sensor::getSystemName)
                    .containsExactlyInAnyOrder("XS49", "XS50", "XS51", "XS52",
                            "XS53", "XS54", "XS55", "XS56", "XS121");
            assertThat(l.getBySystemName("XT21").getKnownState()).isEqualTo(Turnout.CLOSED);
            assertThat(l.getBySystemName("XT22").getKnownState()).isEqualTo(Turnout.THROWN);
            assertThat(l.getBySystemName("XT31").getKnownState()).isEqualTo(Turnout.THROWN);
            assertThat(l.getBySystemName("XT32").getKnownState()).isEqualTo(Turnout.CLOSED);
            checkEncoderStates(sensors, 0x05, -1);

            reply = new XNetReply(encodersFirst
                    ? "48 06 4A 06 55 05 06 07 39 00"
                    : "48 05 06 07 39 06 4A 06 55 00");
            reply.setParity();
            assertThat(reply.checkParity()).isTrue();
            lnis.sendTestMessage(reply);

            assertThat(l.getBySystemName("XT21").getKnownState()).isEqualTo(Turnout.THROWN);
            assertThat(l.getBySystemName("XT22").getKnownState()).isEqualTo(Turnout.CLOSED);
            assertThat(l.getBySystemName("XT31").getKnownState()).isEqualTo(Turnout.CLOSED);
            assertThat(l.getBySystemName("XT32").getKnownState()).isEqualTo(Turnout.THROWN);
            checkEncoderStates(sensors, 0x0A, 0x05);
            assertThat(l.getNamedBeanSet()).hasSize(5);
            assertThat(sensors.getNamedBeanSet()).hasSize(9);
            assertThat(unrelatedTurnout.getKnownState()).isEqualTo(Turnout.CLOSED);
            JUnitUtil.waitFor(() -> unrelatedSensor.getKnownState() == Sensor.ACTIVE, "unrelated sensor initialized");
            assertThat(unrelatedSensor.getKnownState()).isEqualTo(Sensor.ACTIVE);
        } finally {
            sensors.dispose();
        }
    }

    private void checkEncoderStates(XNetSensorManager sensors, int lowNibble, int highNibble) {
        for (int i = 0; i < 8; i++) {
            Sensor sensor = sensors.getBySystemName("XS" + (49 + i));
            int data = i < 4 ? lowNibble : highNibble;
            int expected = data == -1 ? Sensor.UNKNOWN : (data & (1 << (i % 4))) != 0
                    ? Sensor.ACTIVE : Sensor.INACTIVE;
            JUnitUtil.waitFor(() -> sensor.getKnownState() == expected, sensor.getSystemName());
            assertThat(sensor.getKnownState()).isEqualTo(expected);
        }
    }

    @Test
    public void testAsAbstractFactory() {
        // ask for a Turnout, and check type
        TurnoutManager t = jmri.InstanceManager.turnoutManagerInstance();

        Turnout o = t.newTurnout("XT21", "my name");

        log.debug("received turnout value {}", o);
        assertThat(o).isNotNull();

        log.debug("by system name: {}", t.getBySystemName("XT21"));
        log.debug("by user name:   {}", t.getByUserName("my name"));

        assertThat(t.getBySystemName("XT21")).isNotNull();
        assertThat(t.getByUserName("my name")).isNotNull();
    }

    @Test
    public void testGetSystemPrefix() {
        assertThat(l.getSystemPrefix()).isEqualTo("X");
    }

    @Test
    public void testAllowMultipleAdditions() {
        assertThat(l.allowMultipleAdditions("foo")).isTrue();
    }

    @Test
    @Override
    public void testThrownText() {
        assertThat(l.getThrownText()).isEqualTo(Bundle.getMessage("TurnoutStateThrown"));
    }

    @Test
    @Override
    public void testClosedText() {
        assertThat(l.getClosedText()).isEqualTo(Bundle.getMessage("TurnoutStateClosed"));
    }

    @AfterEach
    public void tearDown() {
        lnis.terminateThreads();
        lnis = null;
        l = null;
        JUnitUtil.tearDown();
    }

    @Override
    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        // prepare an interface, register
        lnis = new XNetInterfaceScaffold(new LenzCommandStation());
        // create and register the manager object
        l = new XNetTurnoutManager(lnis.getSystemConnectionMemo());
        jmri.InstanceManager.setTurnoutManager(l);
    }

    private static final Logger log = LoggerFactory.getLogger(XNetTurnoutManagerTest.class);

}
