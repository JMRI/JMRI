package jmri.jmrit.simulation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jmri.Block;
import jmri.DccLocoAddress;
import jmri.InstanceManager;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.SpeedStepMode;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.display.layoutEditor.HitPointType;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.LayoutRHTurnout;
import jmri.jmrit.display.layoutEditor.LayoutRHTurnoutView;
import jmri.jmrit.display.layoutEditor.LayoutRHXOver;
import jmri.jmrit.display.layoutEditor.LayoutRHXOverView;
import jmri.jmrit.display.layoutEditor.LayoutSingleSlip;
import jmri.jmrit.display.layoutEditor.LayoutSingleSlipView;
import jmri.jmrit.display.layoutEditor.LayoutTrack;
import jmri.jmrit.display.layoutEditor.LayoutTurnout;
import jmri.jmrit.display.layoutEditor.LayoutTurntable;
import jmri.jmrit.display.layoutEditor.LayoutTurntableView;
import jmri.jmrit.display.layoutEditor.LevelXing;
import jmri.jmrit.display.layoutEditor.LevelXingView;
import jmri.jmrit.display.layoutEditor.PositionablePoint;
import jmri.jmrit.display.layoutEditor.PositionablePointView;
import jmri.jmrit.display.layoutEditor.TrackSegment;
import jmri.jmrit.display.layoutEditor.TrackSegmentView;
import jmri.util.JUnitAppender;
import jmri.util.JUnitUtil;
import jmri.util.ThreadingUtil;
import jmri.util.junit.annotations.DisabledIfHeadless;

import org.junit.jupiter.api.*;

/**
 * Tests of {@link LayoutEntry}: following a Layout Editor drawing, on a panel built in code with
 * internal sensors and turnouts. The simulator walks the drawing itself; these
 * check its walking, and the mover on a drawing, against the drawing. They
 * set no truth offset and check the simulator's own code only.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@DisabledIfHeadless
public class LayoutEntryTest {

    private static final double TIME_MARGIN = 1e-6;

    private LayoutEditor le;
    private TrackModels models;
    private int count;

    private Block block(String name, double length) {
        LayoutBlock lb = Objects.requireNonNull(
                InstanceManager.getDefault(LayoutBlockManager.class).createNewLayoutBlock(null, name),
                "Layout Block " + name + " created");
        lb.initializeLayoutBlock();
        Sensor s = InstanceManager.getDefault(SensorManager.class).provideSensor("IS" + name);
        try {
            s.setKnownState(Sensor.INACTIVE);
        } catch (jmri.JmriException e) {
            throw new IllegalStateException(e);
        }
        lb.setOccupancySensorName("IS" + name);
        lb.getBlock().setLength((float) length);
        return lb.getBlock();
    }

    /**
     * The way on from a passage through a Block.
     *
     * @param p the passage
     * @return the entry into the next Block
     */
    private static TrackEntry next(Passage p) {
        return Objects.requireNonNull(p.next, "a way on from " + p.entry.getBlock().getDisplayName());
    }

    /**
     * A Block's user name, which every Block made by {@link #block} has.
     *
     * @param b the Block
     * @return its user name
     */
    private static String userName(Block b) {
        return Objects.requireNonNull(b.getUserName(), b.getSystemName() + " has a user name");
    }

    private LayoutBlock lb(Block b) {
        return InstanceManager.getDefault(LayoutBlockManager.class).getLayoutBlock(b);
    }

    private Point2D at() {
        count++;
        return new Point2D.Double(20.0 * count, 20.0 * count);
    }

    // The drawing is built on the GUI thread, as Swing code should be.

    private PositionablePoint point(PositionablePoint.PointType type) {
        return ThreadingUtil.runOnGUIwithReturn(() -> {
            PositionablePoint p = new PositionablePoint("P" + (++count), type, le);
            le.addLayoutTrack(p, new PositionablePointView(p, at(), le));
            return p;
        });
    }

    /** A segment from a positionable point to another piece. */
    private TrackSegment segment(LayoutTrack c1, HitPointType t1, LayoutTrack c2, HitPointType t2, Block b) {
        return ThreadingUtil.runOnGUIwithReturn(() -> {
            TrackSegment s = new TrackSegment("T" + (++count), c1, t1, c2, t2, true, le);
            s.setLayoutBlockByName(b.getUserName());
            le.addLayoutTrack(s, new TrackSegmentView(s, le));
            for (Object[] end : new Object[][]{{c1, t1}, {c2, t2}}) {
                if (end[0] instanceof PositionablePoint) {
                    PositionablePoint p = (PositionablePoint) end[0];
                    if (p.getConnect1() == null) {
                        p.setConnect1(s);
                    } else {
                        p.setConnect2Actual(s);
                    }
                }
            }
            return s;
        });
    }

    private static void connect(LayoutTurnout t, HitPointType type, TrackSegment s) {
        try {
            ThreadingUtil.runOnGUIWithJmriException(() -> t.setConnection(type, s, HitPointType.TRACK));
        } catch (jmri.JmriException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void connect(LevelXing x, HitPointType type, TrackSegment s) {
        try {
            ThreadingUtil.runOnGUIWithJmriException(() -> x.setConnection(type, s, HitPointType.TRACK));
        } catch (jmri.JmriException e) {
            throw new IllegalStateException(e);
        }
    }

    private Turnout turnout(String name) {
        return InstanceManager.getDefault(TurnoutManager.class).provideTurnout(name);
    }

    /**
     * Copy the layout's configuration, as a placement does.
     */
    private void copy() {
        models.use(TrackSnapshot.take(java.util.List.of())).dispose();
    }

    /**
     * EB - A - anchor - B - turnout in C (throat) - D (continuing) - EB
     *                                             \ E (diverging)  - EB
     */

    private Block[] siding(LayoutRHTurnout[] made) {
        Block[] b = {block("A", 450), block("B", 450), block("C", 300), block("D", 450), block("E", 450)};
        PositionablePoint eb1 = point(PositionablePoint.PointType.END_BUMPER);
        PositionablePoint anchor = point(PositionablePoint.PointType.ANCHOR);
        segment(eb1, HitPointType.POS_POINT, anchor, HitPointType.POS_POINT, b[0]);
        String it1 = turnout("IT1").getSystemName();
        LayoutRHTurnout to = ThreadingUtil.runOnGUIwithReturn(() -> {
            LayoutRHTurnout t = new LayoutRHTurnout("TO1", le);
            t.setTurnout(it1);
            t.setLayoutBlockByName(userName(b[2]));
            le.addLayoutTrack(t, new LayoutRHTurnoutView(t, at(), 0.0, 1.0, 1.0, le));
            return t;
        });
        TrackSegment sb = segment(anchor, HitPointType.POS_POINT, to, HitPointType.TURNOUT_A, b[1]);
        connect(to, HitPointType.TURNOUT_A, sb);
        PositionablePoint eb2 = point(PositionablePoint.PointType.END_BUMPER);
        TrackSegment sd = segment(to, HitPointType.TURNOUT_B, eb2, HitPointType.POS_POINT, b[3]);
        connect(to, HitPointType.TURNOUT_B, sd);
        PositionablePoint eb3 = point(PositionablePoint.PointType.END_BUMPER);
        TrackSegment se = segment(to, HitPointType.TURNOUT_C, eb3, HitPointType.POS_POINT, b[4]);
        connect(to, HitPointType.TURNOUT_C, se);
        made[0] = to;
        copy();
        return b;
    }

    @Test
    public void testFollowsTurnoutByItsState() {
        LayoutRHTurnout[] to = new LayoutRHTurnout[1];
        Block[] b = siding(to);
        Turnout t = turnout("IT1");
        Passage p = models.entryInto(b[0], null).follow();
        assertEquals(b[1], next(p).getBlock(), "A to B past the anchor");
        p = next(p).follow();
        assertEquals(b[2], next(p).getBlock(), "B to the turnout's Block");
        TrackEntry intoC = next(p);
        t.setCommandedState(Turnout.CLOSED);
        assertEquals(b[3], next(intoC.follow()).getBlock(), "closed: continuing to D");
        t.setCommandedState(Turnout.THROWN);
        assertEquals(b[4], next(intoC.follow()).getBlock(), "thrown: diverging to E");
        ThreadingUtil.runOnGUI(() -> to[0].setContinuingSense(Turnout.THROWN));
        copy();
        assertEquals(b[3], next(intoC.follow()).getBlock(), "continuing sense thrown: thrown is continuing");
        ThreadingUtil.runOnGUI(() -> to[0].setContinuingSense(Turnout.CLOSED));
        copy();
        // trailing through points set against
        t.setCommandedState(Turnout.CLOSED);
        Passage fromE = models.entryInto(b[2], b[4]).follow();
        assertEquals(Passage.Stop.BLOCKED, fromE.stop);
        assertTrue(Objects.requireNonNull(fromE.reason, "a reason it is blocked").text().contains("TO1"));
        t.setCommandedState(Turnout.THROWN);
        assertEquals(b[1], next(models.entryInto(b[2], b[4]).follow()).getBlock(), "thrown: E to B");
        // end of the track
        Passage end = models.entryInto(b[3], b[2]).follow();
        assertEquals(Passage.Stop.END_OF_TRACK, end.stop);
        TrackEntry fromEnd = Objects.requireNonNull(end.back, "a way back from the end bumper");
        assertEquals(b[2], next(fromEnd.follow()).getBlock(), "back from the end bumper into C");
        // reversing an entry twice comes back to it
        assertEquals(intoC, Objects.requireNonNull(intoC.reverse(), "a way back from C").reverse());
    }

    @Test
    public void testUnknownTurnoutBlocks() {
        Block[] b = siding(new LayoutRHTurnout[1]);
        assertEquals(Turnout.UNKNOWN, turnout("IT1").getKnownState());
        Passage p = models.entryInto(b[2], b[1]).follow();
        assertEquals(Passage.Stop.BLOCKED, p.stop);
    }

    @Test
    public void testTrainOnDrawing() {
        Block[] b = siding(new LayoutRHTurnout[1]);
        turnout("IT1").setCommandedState(Turnout.THROWN);
        SimulatedLayout layout = new SimulatedLayout(true);
        InMemorySpeedSource throttle = new InMemorySpeedSource();
        layout.addSpeedSource(throttle);
        List<long[]> changes = new ArrayList<>();
        Sensor e = Objects.requireNonNull(b[4].getSensor(), "E has a sensor");
        e.addRawStateListener((s, o, n, time) -> changes.add(new long[]{n, time}));
        VirtualTrain t = VirtualTrain.builder("T")
                .loco(SimulatedLoco.builder(new DccLocoAddress(3, false)).length(100).fullSpeed(126).build())
                .length(200).build();
        layout.place(t, b[1], b[0], 250.0);
        long t0 = layout.now();
        throttle.setSpeed(new DccLocoAddress(3, false), 100, SpeedStepMode.NMRA_DCC_128, true, t0);
        layout.runFor(8.0);
        ThreadingUtil.runOnGUI(() -> {
        });
        // 200 mm to the end of B, 300 through C: E active at 5.0 s; by 8.0 s the train is all in E
        assertEquals(1, changes.size());
        assertEquals(Sensor.ACTIVE, changes.get(0)[0]);
        assertEquals(5.0, (changes.get(0)[1] - t0) / 1e9, TIME_MARGIN);
        assertEquals(b[4], Objects.requireNonNull(layout.getTruth().getTrain(t), "T placed").getFrontBlock(),
                "took the diverging route");
        // reverse back over the turnout, now set against the train from E
        turnout("IT1").setCommandedState(Turnout.CLOSED);
        throttle.setSpeed(new DccLocoAddress(3, false), 100, SpeedStepMode.NMRA_DCC_128, false, layout.now());
        layout.runFor(4.0);
        JUnitAppender.assertWarnMessageStartsWith("Simulated train \"T\" has stopped: Turnout \"TO1\" is set against");
        SimulationTruth.TrainTruth truth = Objects.requireNonNull(layout.getTruth().getTrain(t), "T placed");
        assertEquals(List.of(b[4]), truth.getBlocks(), "stopped at the boundary, wholly in E");
        assertEquals(200.0, truth.getFrontPositionMm(), 1e-6, "its rear at the start of E");
        layout.dispose();
    }

    @Test
    public void testTurnoutMovedUnderTrain() {
        Block[] b = siding(new LayoutRHTurnout[1]);
        turnout("IT1").setCommandedState(Turnout.THROWN);
        SimulatedLayout layout = new SimulatedLayout(true);
        InMemorySpeedSource throttle = new InMemorySpeedSource();
        layout.addSpeedSource(throttle);
        VirtualTrain t = VirtualTrain.builder("T")
                .loco(SimulatedLoco.builder(new DccLocoAddress(3, false)).length(100).fullSpeed(126).build())
                .length(200).build();
        layout.place(t, b[4], b[2], 100.0); // straddling the turnout's Block C and E
        turnout("IT1").setCommandedState(Turnout.CLOSED);
        throttle.setSpeed(new DccLocoAddress(3, false), 100, SpeedStepMode.NMRA_DCC_128, true, layout.now());
        layout.runFor(1.0);
        JUnitAppender.assertWarnMessage("Simulated train \"T\" has stopped: The way through \"C\" changed under the train.");
        assertEquals(100.0, Objects.requireNonNull(layout.getTruth().getTrain(t), "T placed").getFrontPositionMm(),
                1e-9, "held where it was");
        layout.dispose();
    }

    @Test
    public void testLevelCrossingFouls() {
        // two lines crossing: F - X(AC: H) - G and J - X(BD: I) - K
        Block f = block("F", 450);
        Block g = block("G", 450);
        Block h = block("H", 100);
        Block i = block("I", 100);
        Block j = block("J", 450);
        Block k = block("K", 450);
        LevelXing x = ThreadingUtil.runOnGUIwithReturn(() -> {
            LevelXing c = new LevelXing("X1", le);
            c.setLayoutBlockAC(lb(h));
            c.setLayoutBlockBD(lb(i));
            le.addLayoutTrack(c, new LevelXingView(c, at(), le));
            return c;
        });
        TrackSegment sa = segment(point(PositionablePoint.PointType.END_BUMPER), HitPointType.POS_POINT, x,
                HitPointType.LEVEL_XING_A, f);
        connect(x, HitPointType.LEVEL_XING_A, sa);
        TrackSegment sc = segment(x, HitPointType.LEVEL_XING_C, point(PositionablePoint.PointType.END_BUMPER),
                HitPointType.POS_POINT, g);
        connect(x, HitPointType.LEVEL_XING_C, sc);
        TrackSegment sbb = segment(point(PositionablePoint.PointType.END_BUMPER), HitPointType.POS_POINT, x,
                HitPointType.LEVEL_XING_B, j);
        connect(x, HitPointType.LEVEL_XING_B, sbb);
        TrackSegment sd = segment(x, HitPointType.LEVEL_XING_D, point(PositionablePoint.PointType.END_BUMPER),
                HitPointType.POS_POINT, k);
        connect(x, HitPointType.LEVEL_XING_D, sd);

        copy();
        assertEquals(h, next(models.entryInto(f, null).follow()).getBlock());
        assertEquals(g, next(models.entryInto(h, f).follow()).getBlock(), "straight over A to C");
        assertEquals(k, next(models.entryInto(i, j).follow()).getBlock(), "straight over B to D");

        SimulatedLayout layout = new SimulatedLayout(true);
        InMemorySpeedSource throttle = new InMemorySpeedSource();
        layout.addSpeedSource(throttle);
        VirtualTrain one = VirtualTrain.builder("One")
                .loco(SimulatedLoco.builder(new DccLocoAddress(3, false)).length(50).fullSpeed(126).build())
                .length(50).build();
        VirtualTrain two = VirtualTrain.builder("Two")
                .loco(SimulatedLoco.builder(new DccLocoAddress(4, false)).length(50).fullSpeed(126).build())
                .length(50).build();
        layout.place(one, f, null, 400.0);
        layout.place(two, j, null, 350.0);
        long t0 = layout.now();
        throttle.setSpeed(new DccLocoAddress(3, false), 100, SpeedStepMode.NMRA_DCC_128, true, t0);
        throttle.setSpeed(new DccLocoAddress(4, false), 100, SpeedStepMode.NMRA_DCC_128, true, t0);
        layout.runFor(3.0);
        JUnitAppender.assertWarnMessageStartsWith("Simulated trains \"One\" and \"Two\" have collided");
        SimulationTruth.Collision c = layout.getTruth().getCollisions().get(0);
        // One reaches the crossing's Block at 0.5 s, Two at 1.0 s: they foul at 1.0 s
        assertEquals(1.0, (c.getTimeNanos() - t0) / 1e9, TIME_MARGIN);
        layout.dispose();
    }

    @Test
    public void testCrossoverWithBlockInsideIt() {
        // right-hand crossover, each connection point in its own Block
        Block p = block("P", 300);
        Block q = block("Q", 300);
        Block r = block("R", 300);
        Block s = block("S", 300);
        String it2 = turnout("IT2").getSystemName();
        LayoutRHXOver x = ThreadingUtil.runOnGUIwithReturn(() -> {
            LayoutRHXOver c = new LayoutRHXOver("XO1", le);
            c.setTurnout(it2);
            c.setLayoutBlockByName(userName(p));
            c.setLayoutBlockBByName(userName(q));
            c.setLayoutBlockCByName(userName(r));
            c.setLayoutBlockDByName(userName(s));
            le.addLayoutTrack(c, new LayoutRHXOverView(c, at(), 0.0, 1.0, 1.0, le));
            return c;
        });
        Block w = block("W", 450);
        TrackSegment sa = segment(point(PositionablePoint.PointType.END_BUMPER), HitPointType.POS_POINT, x,
                HitPointType.TURNOUT_A, w);
        connect(x, HitPointType.TURNOUT_A, sa);
        Block[] legs = {block("LB", 300), block("LC", 300), block("LD", 300)};
        HitPointType[] ports = {HitPointType.TURNOUT_B, HitPointType.TURNOUT_C, HitPointType.TURNOUT_D};
        for (int n = 0; n < 3; n++) {
            TrackSegment seg = segment(x, ports[n], point(PositionablePoint.PointType.END_BUMPER),
                    HitPointType.POS_POINT, legs[n]);
            connect(x, ports[n], seg);
        }
        Turnout t = turnout("IT2");
        copy();
        t.setCommandedState(Turnout.CLOSED);
        Passage intoP = models.entryInto(p, w).follow();
        assertEquals(q, next(intoP).getBlock(), "closed: A to B, the boundary inside the crossover");
        assertEquals(legs[0], next(next(intoP).follow()).getBlock());
        t.setCommandedState(Turnout.THROWN);
        intoP = models.entryInto(p, w).follow();
        assertEquals(r, next(intoP).getBlock(), "thrown: A to C");
        assertEquals(legs[1], next(next(intoP).follow()).getBlock());
        Passage fromB = models.entryInto(q, legs[0]).follow();
        assertEquals(Passage.Stop.BLOCKED, fromB.stop, "a right-hand crossover thrown has no way from B");
        // routes A-B and C-D don't foul; A-C fouls B-D's... and A-B
        t.setCommandedState(Turnout.CLOSED);
        Route ab = models.entryInto(p, w).follow().route;
        Route dc = models.entryInto(s, legs[2]).follow().route;
        assertTrue(!ab.fouls(dc), "the two straight routes are separate tracks");
        t.setCommandedState(Turnout.THROWN);
        Route ac = models.entryInto(p, w).follow().route;
        assertTrue(ac.fouls(ab), "crossing over fouls the straight route");
    }

    @Test
    public void testSlip() {
        Block z = block("Z", 300);
        String it3 = turnout("IT3").getSystemName();
        String it4 = turnout("IT4").getSystemName();
        LayoutSingleSlip slip = ThreadingUtil.runOnGUIwithReturn(() -> {
            LayoutSingleSlip c = new LayoutSingleSlip("SL1", le);
            c.setTurnout(it3);
            c.setTurnoutB(it4);
            c.setLayoutBlockByName(userName(z));
            le.addLayoutTrack(c, new LayoutSingleSlipView(c, at(), 0.0, le));
            return c;
        });
        Block[] ends = {block("SA", 300), block("SB", 300), block("SC", 300), block("SD", 300)};
        HitPointType[] ports = {HitPointType.SLIP_A, HitPointType.SLIP_B, HitPointType.SLIP_C, HitPointType.SLIP_D};
        for (int n = 0; n < 4; n++) {
            TrackSegment seg = segment(slip, ports[n], point(PositionablePoint.PointType.END_BUMPER),
                    HitPointType.POS_POINT, ends[n]);
            connect(slip, ports[n], seg);
        }
        copy();
        // the slip's own table: A-C closed/closed, A-D closed/thrown, B-D thrown/thrown
        turnout("IT3").setCommandedState(Turnout.CLOSED);
        turnout("IT4").setCommandedState(Turnout.CLOSED);
        assertEquals(ends[2], next(models.entryInto(z, ends[0]).follow()).getBlock(), "A to C");
        turnout("IT4").setCommandedState(Turnout.THROWN);
        assertEquals(ends[3], next(models.entryInto(z, ends[0]).follow()).getBlock(), "A to D");
        assertEquals(Passage.Stop.BLOCKED, models.entryInto(z, ends[1]).follow().stop, "nothing set from B");
        turnout("IT3").setCommandedState(Turnout.THROWN);
        assertEquals(ends[3], next(models.entryInto(z, ends[1]).follow()).getBlock(), "B to D");
    }

    @Test
    public void testTurntableNotFollowed() {
        Block a = block("A", 300);
        LayoutTurntable tt = ThreadingUtil.runOnGUIwithReturn(() -> {
            LayoutTurntable c = new LayoutTurntable("TT1", le);
            le.addLayoutTrack(c, new LayoutTurntableView(c, at(), le));
            c.addRay(0.0);
            return c;
        });
        PositionablePoint eb = point(PositionablePoint.PointType.END_BUMPER);
        TrackSegment s = segment(eb, HitPointType.POS_POINT, tt, HitPointType.TURNTABLE_RAY_0, a);
        assertTrue(s.getConnect2() == tt);
        copy();
        Passage p = models.entryInto(a, null).follow();
        assertEquals(Passage.Stop.BLOCKED, p.stop);
        Message why = Objects.requireNonNull(p.reason, "a reason it is blocked");
        assertTrue(why.text().contains("TT1"), why.text());
        assertNull(p.next);
    }

    @BeforeEach
    public void setUp() {
        JUnitUtil.setUp();
        JUnitUtil.resetProfileManager();
        JUnitUtil.initConfigureManager();
        JUnitUtil.initInternalSensorManager();
        JUnitUtil.initInternalTurnoutManager();
        JUnitUtil.initConnectionConfigManager();
        JUnitUtil.initLayoutBlockManager();
        le = ThreadingUtil.runOnGUIwithReturn(() -> new LayoutEditor("Simulation Test"));
        models = new TrackModels();
    }

    @AfterEach
    public void tearDown() {
        models.use(TrackSnapshot.empty()).dispose();
        if (le != null) {
            JUnitUtil.dispose(le);
            le = null;
        }
        models = null;
        JUnitUtil.deregisterBlockManagerShutdownTask();
        JUnitUtil.tearDown();
    }
}
