package jmri.jmrix.can.cbus.simulator;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.jmrit.simulation.SimulationTestUtil;
import jmri.jmrix.can.CanListener;
import jmri.jmrix.can.CanMessage;
import jmri.jmrix.can.CanReply;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

/**
 * Tests for CbusSimulatedSensorOutput, on CAN Simulation connections, with
 * the frames checked against frames worked out from the CBUS specification.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class CbusSimulatedSensorOutputTest {

    /** The frames from the bus, as JMRI received them. */
    private static final class Heard implements CanListener {
        final List<CanReply> replies = new CopyOnWriteArrayList<>();

        @Override
        public void message(CanMessage m) {
            // JMRI's own frames are not what the detectors send
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

    private static int[] data(CanReply r) {
        int[] d = new int[r.getNumDataElements()];
        for (int i = 0; i < d.length; i++) {
            d[i] = r.getElement(i);
        }
        return d;
    }

    private void check(String address, boolean inverted) throws JmriException {
        Sensor s = CbusSimulationTestUtil.sensor(memo, address);
        ThreadingUtil.runOnLayout(() -> s.setInverted(inverted));
        ThreadingUtil.runOnLayoutWithJmriException(() -> s.setKnownState(Sensor.INACTIVE));
        List<CbusEventSpec.Event> events = CbusEventSpec.sensorEvents(address, inverted);
        Assertions.assertTrue(ThreadingUtilReports.reports(output, s), "reports " + address);

        int before = heard.replies.size();
        long t1 = System.nanoTime() - 40_000_000L; // a change worked out within the last step
        output.report(s, Sensor.ACTIVE, t1);
        JUnitUtil.waitFor(() -> s.getKnownState() == Sensor.ACTIVE, address + (inverted ? " inverted" : "") + " active");
        CanReply r = heard.replies.get(before);
        Assertions.assertTrue(events.get(0).matches(data(r)),
                address + " active: expected " + events.get(0) + ", sent " + r);
        Assertions.assertEquals(t1, r.getReceivedTime(), "received when the change happened");

        long t2 = System.nanoTime();
        output.report(s, Sensor.INACTIVE, t2);
        JUnitUtil.waitFor(() -> s.getKnownState() == Sensor.INACTIVE, address + " inactive");
        r = heard.replies.get(before + 1);
        Assertions.assertTrue(events.get(1).matches(data(r)),
                address + " inactive: expected " + events.get(1) + ", sent " + r);
        Assertions.assertEquals(t2, r.getReceivedTime());
    }

    /** reports() runs on the layout thread (Swing thread), where the simulator calls it. */
    private static final class ThreadingUtilReports {
        static boolean reports(CbusSimulatedSensorOutput o, Sensor s) {
            boolean[] result = new boolean[1];
            ThreadingUtil.runOnLayout(() -> result[0] = o.reports(s));
            return result[0];
        }
    }

    @Test
    public void testLongShortTwoEventAndInvertedSensors() throws JmriException {
        check("+N1E2", false);
        check("-N1E3", false);
        check("+N258E1000", false);
        check("+17", false);
        check("-18", false);
        check("+N1E20;-N1E21", false);
        check("+N1E22;+N1E23", false);
        check("+N1E30", true);
        check("+31", true);
        check("+N1E32;-N1E33", true);
    }

    @Test
    public void testOnlyThisConnectionsCbusSensors() {
        CanSystemConnectionMemo other = CbusSimulationTestUtil.addCanSimulation("M2");
        try {
            Sensor mine = CbusSimulationTestUtil.sensor(memo, "+N1E2");
            Sensor theirs = CbusSimulationTestUtil.sensor(other, "+N1E2");
            Sensor internal = InstanceManager.getDefault(SensorManager.class).provideSensor("IS1");
            Assertions.assertTrue(ThreadingUtilReports.reports(output, mine));
            Assertions.assertFalse(ThreadingUtilReports.reports(output, theirs), "another connection's");
            Assertions.assertFalse(ThreadingUtilReports.reports(output, internal), "an internal sensor");
        } finally {
            CbusSimulationTestUtil.dispose(other);
        }
    }

    @Test
    public void testChangesArriveInTimeOrderWithTheirTimes() throws JmriException {
        Sensor[] s = new Sensor[6];
        for (int i = 0; i < s.length; i++) {
            Sensor x = CbusSimulationTestUtil.sensor(memo, "+N1E" + (100 + i));
            ThreadingUtil.runOnLayoutWithJmriException(() -> x.setKnownState(Sensor.INACTIVE));
            s[i] = x;
        }
        int before = heard.replies.size();
        long t0 = System.nanoTime();
        // reported in time order, as the simulator does, the last a little in the future
        for (int i = 0; i < s.length; i++) {
            output.report(s[i], Sensor.ACTIVE, t0 - 50_000_000L + i * 12_000_000L);
        }
        JUnitUtil.waitFor(() -> heard.replies.size() - before == s.length, "every change");
        for (int i = 0; i < s.length; i++) {
            CanReply r = heard.replies.get(before + i);
            Assertions.assertEquals(100 + i, (r.getElement(3) << 8) | r.getElement(4), "in the order of their times");
            Assertions.assertEquals(t0 - 50_000_000L + i * 12_000_000L, r.getReceivedTime());
        }
    }

    @Test
    public void testDisposedReportsNothing() throws JmriException {
        Sensor s = CbusSimulationTestUtil.sensor(memo, "+N1E2");
        ThreadingUtil.runOnLayoutWithJmriException(() -> s.setKnownState(Sensor.INACTIVE));
        output.dispose();
        Assertions.assertFalse(ThreadingUtilReports.reports(output, s));
        int before = heard.replies.size();
        output.report(s, Sensor.ACTIVE, System.nanoTime());
        // a later change, through another output and the same simulated bus, arrives after the
        // first would have: so once it has, the first is not coming
        new CbusSimulatedSensorOutput(memo).report(CbusSimulationTestUtil.sensor(memo, "+N1E3"), Sensor.ACTIVE,
                System.nanoTime());
        JUnitUtil.waitFor(() -> heard.replies.size() > before, "the later frame");
        Assertions.assertEquals(1, heard.replies.size() - before, "nothing from the disposed output");
        Assertions.assertEquals(3, heard.replies.get(before).getElement(4));
    }

    private CanSystemConnectionMemo memo;
    private CbusSimulatedSensorOutput output;
    private Heard heard;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        SimulationTestUtil.setUpSimulatedProfile();
        memo = CbusSimulationTestUtil.addCanSimulation("M");
        output = memo.get(CbusSimulator.class).getSimulatedSensorOutput();
        heard = new Heard();
        memo.getTrafficController().addCanListener(heard);
    }

    @AfterEach
    public void tearDown() {
        memo.getTrafficController().removeCanListener(heard);
        CbusSimulationTestUtil.dispose(memo);
        memo = null;
        output = null;
        heard = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
