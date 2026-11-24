package jmri.jmrix.can.cbus.simulator;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.simulation.SimulationTestUtil;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for CbusSimulatedTurnoutFeedback.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class CbusSimulatedTurnoutFeedbackTest {

    private static final int TRAVEL_MS = 100;

    private CanSystemConnectionMemo memo;
    private CbusSimulatedTurnoutFeedback feedback;
    private Heard heard;

    /** Feedback frames from the bus, as JMRI received them. */
    private static final class Heard implements CanListener {
        final List<CanReply> replies = new CopyOnWriteArrayList<>();

        @Override
        public void message(CanMessage m) {
            // JMRI's own frames are the commands, not the feedback
        }

        @Override
        public void reply(CanReply r) {
            int opc = r.getElement(0);
            if (r.getNumDataElements() == 5 && (opc == CbusEventSpec.ACON || opc == CbusEventSpec.ACOF
                    || opc == CbusEventSpec.ASON || opc == CbusEventSpec.ASOF)) {
                replies.add(r);
            }
        }
    }

    private Turnout turnout(String address, int mode, String first, String second) throws JmriException {
        Turnout[] t = new Turnout[1];
        ThreadingUtil.runOnLayoutWithJmriException(() -> {
            t[0] = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("MT" + address);
            if (first != null) {
                t[0].provideFirstFeedbackSensor("MS" + first);
            }
            if (second != null) {
                t[0].provideSecondFeedbackSensor("MS" + second);
            }
            t[0].setFeedbackMode(mode);
        });
        return t[0];
    }

    private static Sensor sensor(String name) {
        return InstanceManager.getDefault(jmri.SensorManager.class).getSensor(name);
    }

    private static void command(Turnout t, int state) {
        ThreadingUtil.runOnLayout(() -> t.setCommandedState(state));
    }

    @Test
    public void testTwoSensorFeedbackFollowsEachCommandAfterTheTravelTime() throws JmriException {
        Turnout t = turnout("+N1E10", Turnout.TWOSENSOR, "+N1E11", "+N1E12");
        ThreadingUtil.runOnLayout(() -> feedback.setEnabled(true));

        long before = System.nanoTime();
        command(t, Turnout.THROWN);
        JUnitUtil.waitFor(() -> t.getKnownState() == Turnout.THROWN, "thrown by feedback");
        Assertions.assertEquals(Sensor.ACTIVE, sensor("MS+N1E11").getKnownState(), "first sensor shows thrown");
        Assertions.assertEquals(Sensor.INACTIVE, sensor("MS+N1E12").getKnownState(), "second sensor clear");
        for (CanReply r : heard.replies) {
            Assertions.assertTrue(r.getReceivedTime() >= before + TRAVEL_MS * 1_000_000L,
                "feedback received only after the travel time: " + r);
        }

        command(t, Turnout.CLOSED);
        JUnitUtil.waitFor(() -> t.getKnownState() == Turnout.CLOSED, "closed by feedback");
        Assertions.assertEquals(Sensor.INACTIVE, sensor("MS+N1E11").getKnownState());
        Assertions.assertEquals(Sensor.ACTIVE, sensor("MS+N1E12").getKnownState(), "second sensor shows closed");
    }

    @Test
    public void testOneSensorFeedbackAndAnInvertedTurnout() throws JmriException {
        Turnout t = turnout("+N1E20", Turnout.ONESENSOR, "+N1E21", null);
        ThreadingUtil.runOnLayout(() -> {
            t.setInverted(true);
            feedback.setEnabled(true);
        });

        command(t, Turnout.THROWN);
        JUnitUtil.waitFor(() -> t.getKnownState() == Turnout.THROWN, "thrown by feedback");
        Assertions.assertEquals(Sensor.ACTIVE, sensor("MS+N1E21").getKnownState());

        command(t, Turnout.CLOSED);
        JUnitUtil.waitFor(() -> t.getKnownState() == Turnout.CLOSED, "closed by feedback");
        Assertions.assertEquals(Sensor.INACTIVE, sensor("MS+N1E21").getKnownState());
    }

    @Test
    public void testACommandFromAnotherNodeIsAnsweredToo() throws JmriException {
        Turnout t = turnout("+N1E30", Turnout.TWOSENSOR, "+N1E31", "+N1E32");
        ThreadingUtil.runOnLayout(() -> feedback.setEnabled(true));

        // a panel module on the bus throws the turnout: ACON N1 E30
        CanReply fromPanel = new CanReply(new int[]{CbusEventSpec.ACON, 0, 1, 0, 30});
        new CbusSimulatedSend(memo).receiveAt(fromPanel, System.nanoTime());
        JUnitUtil.waitFor(() -> sensor("MS+N1E31").getKnownState() == Sensor.ACTIVE, "first sensor active");
        Assertions.assertEquals(Sensor.INACTIVE, sensor("MS+N1E32").getKnownState());
        JUnitUtil.waitFor(() -> t.getKnownState() == Turnout.THROWN, "turnout shown thrown by its feedback");
    }

    @Test
    public void testOffUntilEnabledAndOffAgainAfter() throws JmriException {
        Turnout t = turnout("+N1E40", Turnout.TWOSENSOR, "+N1E41", "+N1E42");
        ThreadingUtil.runOnLayout(() -> Assertions.assertFalse(feedback.isEnabled(), "off by default"));

        command(t, Turnout.THROWN);
        JUnitUtil.waitFor(10 * TRAVEL_MS);
        Assertions.assertEquals(Sensor.UNKNOWN, sensor("MS+N1E41").getKnownState(), "no answer while off");
        Assertions.assertTrue(heard.replies.isEmpty(), "nothing sent while off: " + heard.replies);

        ThreadingUtil.runOnLayout(() -> feedback.setEnabled(true));
        command(t, Turnout.CLOSED);
        JUnitUtil.waitFor(() -> sensor("MS+N1E42").getKnownState() == Sensor.ACTIVE, "answered once enabled");

        ThreadingUtil.runOnLayout(() -> feedback.setEnabled(false));
        int sent = heard.replies.size();
        command(t, Turnout.THROWN);
        JUnitUtil.waitFor(10 * TRAVEL_MS);
        Assertions.assertEquals(sent, heard.replies.size(), "nothing sent once off again");
    }

    @Test
    public void testOtherFeedbackModesAndOtherSensorsGetNoAnswer() throws JmriException {
        Turnout direct = turnout("+N1E50", Turnout.DIRECT, null, null);
        Turnout internalSensors = turnout("+N1E60", Turnout.ONESENSOR, null, null);
        ThreadingUtil.runOnLayoutWithJmriException(() -> {
            internalSensors.provideFirstFeedbackSensor("IS60");
            feedback.setEnabled(true);
        });

        command(direct, Turnout.THROWN);
        command(internalSensors, Turnout.THROWN);
        JUnitUtil.waitFor(10 * TRAVEL_MS);
        Assertions.assertTrue(heard.replies.isEmpty(), "no feedback frames: " + heard.replies);
        Assertions.assertEquals(Sensor.UNKNOWN, sensor("IS60").getKnownState(), "an internal sensor is left alone");
    }

    @Test
    public void testTravelTimeSetting() {
        Assertions.assertEquals(CbusSimulatedTurnoutFeedback.DEFAULT_TRAVEL_MS,
            new CbusSimulatedTurnoutFeedback(memo).getTravelTimeMs());
        Assertions.assertThrows(IllegalArgumentException.class, () -> feedback.setTravelTimeMs(-1));
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        SimulationTestUtil.setUpSimulatedProfile();
        memo = CbusSimulationTestUtil.addCanSimulation("M");
        feedback = memo.get(CbusSimulator.class).getTurnoutFeedback();
        feedback.setTravelTimeMs(TRAVEL_MS);
        heard = new Heard();
        memo.getTrafficController().addCanListener(heard);
    }

    @AfterEach
    public void tearDown() {
        memo.getTrafficController().removeCanListener(heard);
        CbusSimulationTestUtil.dispose(memo);
        memo = null;
        feedback = null;
        heard = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
