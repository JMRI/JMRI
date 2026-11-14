package jmri.jmrix.can.cbus.simulator;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jmri.Block;
import jmri.DccThrottle;
import jmri.JmriException;
import jmri.Sensor;
import jmri.jmrit.simulation.Detection;
import jmri.jmrit.simulation.SimulatedLoco;
import jmri.jmrit.simulation.SimulationTestUtil;
import jmri.jmrit.simulation.SimulationTruth;
import jmri.jmrit.simulation.VirtualTrain;
import jmri.jmrix.can.CanSystemConnectionMemo;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

/**
 * Detection faults reported on CBUS sensors of a CAN Simulation connection:
 * a drop-out and swapped sensors, sent as the frames a detector on the bus
 * would send.
 * <p>
 * The trains move in real time, so these tests assert the order of events
 * and the ground truth, never how long anything took.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class CbusSimulatedFaultsTest {

    private final List<String> changes = new CopyOnWriteArrayList<>();
    private final List<Sensor> recorded = new ArrayList<>();
    private final PropertyChangeListener recorder = e -> {
        if (Sensor.PROPERTY_KNOWN_STATE.equals(e.getPropertyName())) {
            Sensor s = (Sensor) e.getSource();
            int st = s.getKnownState();
            if (st == Sensor.ACTIVE || st == Sensor.INACTIVE) {
                changes.add(s.getUserName() + (st == Sensor.ACTIVE ? " active" : " inactive"));
            }
        }
    };

    private Block[] line(String... names) throws JmriException {
        Block[] b = SimulationTestUtil.line(500, names);
        for (int i = 0; i < b.length; i++) {
            Sensor s = CbusSimulationTestUtil.sensor(memo, "+N1E" + (i + 1));
            String name = names[i];
            ThreadingUtil.runOnLayoutWithJmriException(() -> {
                s.setUserName(name);
                s.setKnownState(Sensor.INACTIVE);
            });
            b[i].setSensor(s.getSystemName());
            s.addPropertyChangeListener(recorder);
            recorded.add(s);
        }
        return b;
    }

    private void run(VirtualTrain t, Block until) {
        DccThrottle th = CbusSimulationTestUtil.throttle(memo, SimulationTestUtil.address(3));
        ThreadingUtil.runOnLayout(() -> {
            th.setIsForward(true);
            th.setSpeedSetting(0.5f);
        });
        SimulationTestUtil.waitForFrontIn(t, until);
        ThreadingUtil.runOnLayout(() -> th.setSpeedSetting(0f));
        SimulationTestUtil.waitForTruth(tr -> {
            SimulationTruth.TrainTruth tt = tr.getTrain(t);
            return tt != null && tt.getSpeedMmPerSecond() == 0;
        }, "stopped");
        ThreadingUtil.runOnLayout(() -> th.release(null));
    }

    @Test
    public void testDropOutOnCbus() throws JmriException {
        Block[] b = line("A", "B", "C");
        SimulationTestUtil.layout().getFaults().dropOut(b[1], b[0], 100, 250);
        VirtualTrain t = SimulationTestUtil.train("T", 3, 100, 400);
        SimulationTestUtil.place(t, b[1], b[0], 20);
        JUnitUtil.waitFor(() -> changes.contains("B active"), "B seen when placed");
        run(t, b[2]);
        JUnitUtil.waitFor(() -> changes.lastIndexOf("B active") > changes.indexOf("B inactive"), "B seen again");
        int out = changes.indexOf("B inactive");
        int back = changes.lastIndexOf("B active");
        Assertions.assertTrue(out < back && back < changes.indexOf("C active"),
                "B dropped out and was seen again before C: " + changes);
        List<SimulationTruth.DropOut> d = SimulationTestUtil.truth().getDropOuts();
        Assertions.assertEquals(1, d.size());
        Assertions.assertEquals(b[1], d.get(0).getBlock());
        Assertions.assertTrue(d.get(0).getEndNanos() - d.get(0).getStartNanos() > 0);
    }

    @Test
    public void testSwappedSensorsOnCbus() throws JmriException {
        Block[] b = line("A", "B", "C");
        SimulationTestUtil.layout().getFaults().swapSensors(b[1], b[2]);
        VirtualTrain t = SimulationTestUtil.train("T", 3, 100, 400);
        SimulationTestUtil.place(t, b[0], null, 150);
        run(t, b[2]);
        JUnitUtil.waitFor(() -> changes.contains("B active"), "C's occupancy on B's sensor");
        Assertions.assertTrue(changes.indexOf("C active") < changes.indexOf("B active"),
                "B entered first, shown on C's sensor: " + changes);
    }

    /**
     * A train that parts while running in real time: the mover keeps stepping
     * while the stock coasts, so the stock comes to a stand behind, and the
     * front part runs on.
     *
     * @throws JmriException if a sensor can't be set
     */
    @Test
    public void testBreakawayInRealTime() throws JmriException {
        Block[] b = line("A", "B", "C");
        VirtualTrain t = VirtualTrain.builder("T")
                .loco(SimulatedLoco.builder(SimulationTestUtil.address(3)).length(100).fullSpeed(400).build())
                .length(300).detection(Detection.entireTrain()).build();
        SimulationTestUtil.place(t, b[0], null, 450);
        DccThrottle th = CbusSimulationTestUtil.throttle(memo, SimulationTestUtil.address(3));
        ThreadingUtil.runOnLayout(() -> {
            th.setIsForward(true);
            th.setSpeedSetting(0.5f);
        });
        SimulationTestUtil.waitForFrontIn(t, b[1]);
        VirtualTrain rear = SimulationTestUtil.layout().getFaults().breakaway(t, 150);
        SimulationTestUtil.waitForTruth(tr -> {
            SimulationTruth.TrainTruth tt = tr.getTrain(rear);
            return tt != null && tt.getSpeedMmPerSecond() == 0;
        }, "the stock coasted to a stand");
        SimulationTestUtil.waitForFrontIn(t, b[2]);
        ThreadingUtil.runOnLayout(() -> th.setSpeedSetting(0f));
        SimulationTestUtil.waitForTruth(tr -> {
            SimulationTruth.TrainTruth tt = tr.getTrain(t);
            return tt != null && tt.getSpeedMmPerSecond() == 0;
        }, "the front part stopped");
        SimulationTruth.TrainTruth stock = SimulationTestUtil.truth(rear);
        Assertions.assertFalse(stock.getBlocks().contains(b[2]), "the stock stayed behind: " + stock.getBlocks());
        Assertions.assertEquals(b[2], SimulationTestUtil.truth(t).getFrontBlock(), "the front part ran on");
        Assertions.assertTrue(SimulationTestUtil.truth().getCollisions().isEmpty());
        ThreadingUtil.runOnLayout(() -> th.release(null));
    }

    private CanSystemConnectionMemo memo;

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        SimulationTestUtil.setUpSimulatedProfile();
        memo = CbusSimulationTestUtil.addCanSimulation("M");
    }

    @AfterEach
    public void tearDown() {
        SimulationTestUtil.removeTrains();
        SimulationTestUtil.layout().getFaults().clearAll();
        for (Sensor s : recorded) {
            s.removePropertyChangeListener(recorder);
        }
        CbusSimulationTestUtil.dispose(memo);
        memo = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
