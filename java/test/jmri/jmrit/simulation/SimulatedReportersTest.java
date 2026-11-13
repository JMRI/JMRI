package jmri.jmrit.simulation;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jmri.Block;
import jmri.BlockManager;
import jmri.DccLocoAddress;
import jmri.IdTag;
import jmri.InstanceManager;
import jmri.Path;
import jmri.Reporter;
import jmri.ReporterManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.SpeedStepMode;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;

import org.junit.jupiter.api.*;

/**
 * Tests of simulated trains' ID tags read by Blocks' internal reporters, run
 * step by step on a manual clock. A loco at step 100 runs at 100 mm/s, and
 * each Block of the line is 450 mm.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public class SimulatedReportersTest {

    private SimulatedLayout layout;
    private InMemorySpeedSource throttle;
    /** Each Block's reports, in order: {tag ID or "none"}. */
    private final List<List<Object[]>> reports = new ArrayList<>();

    private Block block(String name, double length, int reporter) {
        Block b = InstanceManager.getDefault(BlockManager.class).provideBlock("IB" + name);
        b.setUserName(name);
        b.setLength((float) length);
        Sensor s = InstanceManager.getDefault(SensorManager.class).provideSensor("IS" + name);
        b.setSensor(s.getSystemName());
        Reporter r = InstanceManager.getDefault(ReporterManager.class).provideReporter("IR" + reporter);
        b.setReporter(r);
        b.setReportingCurrent(true);
        List<Object[]> list = new ArrayList<>();
        reports.add(list);
        r.addPropertyChangeListener("currentReport", e -> {
            Object v = e.getNewValue();
            list.add(new Object[]{v instanceof IdTag ? ((IdTag) v).getTagID() : "none"});
        });
        return b;
    }

    private static void join(Block a, Block b) {
        a.addPath(new Path(b, 0, 0));
        b.addPath(new Path(a, 0, 0));
    }

    private Block[] line() {
        Block[] b = {block("A", 450, 1), block("B", 450, 2), block("C", 450, 3)};
        join(b[0], b[1]);
        join(b[1], b[2]);
        return b;
    }

    private static VirtualTrain train(String name, int address, double length, String tag) {
        return VirtualTrain.builder(name)
                .loco(SimulatedLoco.builder(new DccLocoAddress(address, false)).length(100).fullSpeed(126).build())
                .length(length).idTag(tag).build();
    }

    private void speed(int address, int step) {
        throttle.setSpeed(new DccLocoAddress(address, false), step, SpeedStepMode.NMRA_DCC_128, true,
                layout.now());
    }

    private void flush() {
        ThreadingUtil.runOnGUI(() -> {
        });
    }

    /** The tags a Block's reporter reported, in order. */
    private List<String> tags(int block) {
        flush();
        List<String> list = new ArrayList<>();
        for (Object[] o : reports.get(block)) {
            list.add((String) o[0]);
        }
        return list;
    }

    @Test
    public void testTagReadAsTheTrainComesInAndClearedWhenItHasGone() {
        Block[] b = line();
        VirtualTrain t = train("T", 3, 200, "0102030405");
        layout.place(t, b[1], b[0], 0.0); // its rear in A
        assertEquals(List.of("0102030405"), tags(0), "A reads it, as the train is placed in it");
        speed(3, 100);
        layout.runFor(7.0);
        assertEquals(List.of("0102030405", "none"), tags(1), "B read it as the front came in, and is clear again");
        assertEquals(List.of("0102030405", "none"), tags(0), "A clear once the train has gone, at 2.0 s");
        assertEquals(List.of("0102030405"), tags(2), "C reads it at 4.5 s");
        flush();
        Object v = b[2].getValue();
        if (!(v instanceof IdTag)) {
            fail("the Block's value is the tag it reports: " + v);
            return;
        }
        assertEquals("0102030405", ((IdTag) v).getTagID());
    }

    @Test
    public void testTwoTaggedTrainsInOneBlock() {
        Block[] b = line();
        VirtualTrain u = train("U", 4, 100, "AAAAAAAAAA");
        layout.place(u, b[1], b[0], 400.0); // standing at the far end of B
        VirtualTrain t = train("T", 3, 100, "BBBBBBBBBB");
        layout.place(t, b[0], null, 300.0);
        speed(3, 100);
        layout.runFor(2.0); // T's front into B at 1.5 s
        speed(3, 0);
        assertEquals(List.of("AAAAAAAAAA", "BBBBBBBBBB"), tags(1), "B reads U, then T as it comes in");
        layout.remove(t);
        assertEquals(List.of("AAAAAAAAAA", "BBBBBBBBBB", "AAAAAAAAAA"), tags(1),
                "with T gone, the train still there");
        layout.remove(u);
        assertEquals("none", tags(1).get(3));
    }

    @Test
    public void testNoTagNoRead() {
        Block[] b = line();
        VirtualTrain t = train("T", 3, 200, null);
        layout.place(t, b[1], b[0], 0.0);
        speed(3, 100);
        layout.runFor(5.0);
        assertTrue(tags(0).isEmpty() && tags(1).isEmpty() && tags(2).isEmpty(), "nothing read");
    }

    @Test
    public void testPartedTrainNotReadAgain() {
        Block[] b = line();
        VirtualTrain t = train("T", 3, 200, "0102030405");
        layout.place(t, b[1], b[0], 0.0);
        speed(3, 40);
        layout.runFor(1.0);
        layout.getFaults().breakaway(t, 100);
        layout.runFor(1.0);
        assertEquals(List.of("0102030405"), tags(1), "B read the train once");
        assertEquals(List.of("0102030405"), tags(0), "the stock in A carries no tag; A still has the train's");
    }

    @Test
    public void testReporterNoOutputReportsIsLeftAlone() {
        Block[] b = line();
        // C's reporter is of no connection the simulator reports on
        Reporter other = new OtherReporter("XR1");
        b[2].setReporter(other);
        List<Object> seen = new ArrayList<>();
        other.addPropertyChangeListener("currentReport", e -> seen.add(e.getNewValue()));
        VirtualTrain t = train("T", 3, 200, "0102030405");
        layout.place(t, b[1], b[0], 0.0);
        speed(3, 100);
        layout.runFor(5.0); // the front into C at 4.5 s
        speed(3, 0);
        assertEquals(b[2], Objects.requireNonNull(layout.getTruth().getTrain(t)).getFrontBlock());
        assertEquals(List.of("0102030405"), tags(1), "B's internal reporter read the tag");
        flush();
        assertTrue(seen.isEmpty(), "C's other reporter is left alone: " + seen);
    }

    @Test
    public void testAnAdapterOffersNoReporterOutputsUnlessItHasThem() {
        SimulationAdapter a = new SimulationAdapter() {
            @Override
            public List<SimulatedSpeedSource> getSpeedSources() {
                return List.of();
            }

            @Override
            public List<SimulatedDetectorOutput> getDetectorOutputs() {
                return List.of();
            }
        };
        assertTrue(a.getReporterOutputs().isEmpty());
    }

    /** A reporter belonging to no reporter manager. */
    private static final class OtherReporter extends jmri.implementation.AbstractReporter {

        private int state = 0;

        OtherReporter(String systemName) {
            super(systemName);
        }

        @Override
        public int getState() {
            return state;
        }

        @Override
        public void setState(int s) {
            state = s;
        }
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initReporterManager();
        JUnitUtil.initIdTagManager();
        JUnitUtil.initConnectionConfigManager();
        layout = new SimulatedLayout(true);
        throttle = new InMemorySpeedSource();
        layout.addSpeedSource(throttle);
    }

    @AfterEach
    public void tearDown() {
        layout.dispose();
        layout = null;
        throttle = null;
        reports.clear();
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
