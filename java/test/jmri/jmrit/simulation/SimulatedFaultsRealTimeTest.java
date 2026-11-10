package jmri.jmrit.simulation;

import jmri.Block;
import jmri.DccThrottle;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

/**
 * Faults on simulated trains moving in real time, on the Internal connection.
 * <p>
 * The trains move in real time, so these tests assert the order of events
 * and the ground truth, never how long anything took.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class SimulatedFaultsRealTimeTest {

    @Test
    public void testBreakawayInRealTime() {
        Block[] b = SimulationTestUtil.line(500, "A", "B", "C");
        VirtualTrain t = VirtualTrain.builder("T")
                .loco(SimulatedLoco.builder(SimulationTestUtil.address(3)).length(100).fullSpeed(400).build())
                .length(300).detection(Detection.entireTrain()).build();
        SimulationTestUtil.place(t, b[0], null, 450);
        DccThrottle th = SimulationTestUtil.throttle(3);
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

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        SimulationTestUtil.setUpSimulatedProfile();
    }

    @AfterEach
    public void tearDown() {
        SimulationTestUtil.removeTrains();
        SimulationTestUtil.layout().getFaults().clearAll();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
