package jmri.jmrit.simulation;

import java.awt.geom.Point2D;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.annotation.Nonnull;

import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.JmriException;
import jmri.Sensor;
import jmri.SensorManager;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.display.layoutEditor.HitPointType;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutBlockManager;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.LayoutLHTurnout;
import jmri.jmrit.display.layoutEditor.LayoutLHTurnoutView;
import jmri.jmrit.display.layoutEditor.LayoutRHTurnout;
import jmri.jmrit.display.layoutEditor.LayoutRHTurnoutView;
import jmri.jmrit.display.layoutEditor.LayoutRHXOver;
import jmri.jmrit.display.layoutEditor.LayoutRHXOverView;
import jmri.jmrit.display.layoutEditor.LayoutTrack;
import jmri.jmrit.display.layoutEditor.LayoutTurnout;
import jmri.jmrit.display.layoutEditor.PositionablePoint;
import jmri.jmrit.display.layoutEditor.PositionablePointView;
import jmri.jmrit.display.layoutEditor.TrackSegment;
import jmri.jmrit.display.layoutEditor.TrackSegmentView;
import jmri.util.ThreadingUtil;

/**
 * Builds a layout on a Layout Editor panel in code, for tests, much as a
 * panel file is loaded: Blocks with their lengths and internal sensors,
 * Layout Blocks using them, the drawn track, and then the Block paths, made
 * by {@link LayoutBlockManager#initializeLayoutBlockPaths()} as JMRI does
 * once a panel has been loaded.
 * <p>
 * For a Block called "Name", the Block is "IBName", its occupancy sensor
 * "ISName" (set inactive) and its Layout Block "ILBName", each with the user
 * name "Name". A turnout called "Name" uses the internal turnout "ITName",
 * set closed. The drawing is made on the GUI thread, as Swing code should
 * be.
 * <p>
 * The profile must already have a Layout Block manager, as well as the
 * managers {@link SimulationTestUtil#setUpSimulatedProfile()} sets up;
 * {@link SimulationTestLayouts#setUpProfile} sets up both.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
public final class SimulationLayoutBuilder {

    private final LayoutEditor editor;
    private final Map<String, Block> blocks = new LinkedHashMap<>();
    private final Map<String, Turnout> turnouts = new LinkedHashMap<>();
    private int count;

    /**
     * Start a layout on a new Layout Editor panel, which is not shown.
     *
     * @param panelName the panel's name
     */
    public SimulationLayoutBuilder(@Nonnull String panelName) {
        editor = ThreadingUtil.runOnGUIwithReturn(() -> new LayoutEditor(panelName));
    }

    /**
     * Add a Block, its sensor and its Layout Block.
     *
     * @param name     the name
     * @param lengthMm the Block's length
     * @return the Block
     */
    @Nonnull
    public Block block(@Nonnull String name, double lengthMm) {
        Block b = InstanceManager.getDefault(BlockManager.class).provideBlock("IB" + name);
        b.setUserName(name);
        b.setLength((float) lengthMm);
        Sensor s = InstanceManager.getDefault(SensorManager.class).provideSensor("IS" + name);
        try {
            s.setKnownState(Sensor.INACTIVE);
        } catch (JmriException e) {
            throw new IllegalStateException(e);
        }
        LayoutBlock lb = InstanceManager.getDefault(LayoutBlockManager.class).createNewLayoutBlock("ILB" + name, name);
        if (lb == null) {
            throw new IllegalArgumentException("a Layout Block called " + name + " already exists");
        }
        lb.setOccupancySensorName(s.getSystemName());
        blocks.put(name, b);
        return b;
    }

    private Turnout provideTurnout(String name) {
        Turnout t = InstanceManager.getDefault(TurnoutManager.class).provideTurnout("IT" + name);
        t.setCommandedState(Turnout.CLOSED);
        turnouts.put(name, t);
        return t;
    }

    private static void requireBlock(Map<String, Block> known, String name) {
        if (!known.containsKey(name)) {
            throw new IllegalArgumentException("no Block called " + name + " has been added");
        }
    }

    /**
     * @param x where on the panel
     * @param y where on the panel
     * @return an end bumper
     */
    @Nonnull
    public PositionablePoint endBumper(double x, double y) {
        return point(PositionablePoint.PointType.END_BUMPER, x, y);
    }

    /**
     * An anchor: a point joining two track segments, and a Block boundary if
     * they are in different Blocks.
     *
     * @param x where on the panel
     * @param y where on the panel
     * @return the anchor
     */
    @Nonnull
    public PositionablePoint anchor(double x, double y) {
        return point(PositionablePoint.PointType.ANCHOR, x, y);
    }

    private PositionablePoint point(PositionablePoint.PointType type, double x, double y) {
        String id = "P" + (++count);
        return ThreadingUtil.runOnGUIwithReturn(() -> {
            PositionablePoint p = new PositionablePoint(id, type, editor);
            editor.addLayoutTrack(p, new PositionablePointView(p, new Point2D.Double(x, y), editor));
            return p;
        });
    }

    /**
     * A turnout, drawn as "TOName" and worked by the internal turnout
     * "ITName", in its own Block. A is the throat, B the continuing (closed)
     * leg and C the diverging (thrown) leg.
     *
     * @param name      the turnout's name
     * @param blockName the Block it is in, already added
     * @param rightHand true for a right-hand turnout, false for a left-hand
     *                  one
     * @param x         where on the panel
     * @param y         where on the panel
     * @param rotation  its rotation on the panel, in degrees
     * @return the turnout as drawn
     */
    @Nonnull
    public LayoutTurnout turnout(@Nonnull String name, @Nonnull String blockName, boolean rightHand, double x,
            double y, double rotation) {
        requireBlock(blocks, blockName);
        String it = provideTurnout(name).getSystemName();
        Point2D at = new Point2D.Double(x, y);
        return ThreadingUtil.runOnGUIwithReturn(() -> {
            LayoutTurnout t = rightHand ? new LayoutRHTurnout("TO" + name, editor)
                    : new LayoutLHTurnout("TO" + name, editor);
            t.setTurnout(it);
            t.setLayoutBlockByName(blockName);
            if (rightHand) {
                editor.addLayoutTrack(t, new LayoutRHTurnoutView((LayoutRHTurnout) t, at, rotation, 1.0, 1.0, editor));
            } else {
                editor.addLayoutTrack(t, new LayoutLHTurnoutView((LayoutLHTurnout) t, at, rotation, 1.0, 1.0, editor));
            }
            return t;
        });
    }

    /**
     * A right-hand crossover, drawn as "XOName" and worked by the internal
     * turnout "ITName". A-B and C-D are the two lines, straight through when
     * closed; thrown, A is joined to C. Each line's half is in a Block of its
     * own, so that trains can use both lines at once while it is closed.
     *
     * @param name        the crossover's name
     * @param blockAB     the Block its A-B half is in, already added
     * @param blockCD     the Block its C-D half is in, already added
     * @param x           where on the panel
     * @param y           where on the panel
     * @return the crossover as drawn
     */
    @Nonnull
    public LayoutRHXOver crossover(@Nonnull String name, @Nonnull String blockAB, @Nonnull String blockCD,
            double x, double y) {
        requireBlock(blocks, blockAB);
        requireBlock(blocks, blockCD);
        String it = provideTurnout(name).getSystemName();
        return ThreadingUtil.runOnGUIwithReturn(() -> {
            LayoutRHXOver c = new LayoutRHXOver("XO" + name, editor);
            c.setTurnout(it);
            c.setLayoutBlockByName(blockAB);
            c.setLayoutBlockBByName(blockAB);
            c.setLayoutBlockCByName(blockCD);
            c.setLayoutBlockDByName(blockCD);
            editor.addLayoutTrack(c, new LayoutRHXOverView(c, new Point2D.Double(x, y), 0.0, 1.0, 1.0, editor));
            return c;
        });
    }

    /**
     * A track segment between two pieces, connected at both ends.
     *
     * @param c1        the piece at one end
     * @param t1        where on it: {@link HitPointType#POS_POINT} for a
     *                  point, or the turnout's connection point
     * @param c2        the piece at the other end
     * @param t2        where on it
     * @param blockName the Block the segment is in, already added
     * @return the segment
     */
    @Nonnull
    public TrackSegment track(@Nonnull LayoutTrack c1, @Nonnull HitPointType t1, @Nonnull LayoutTrack c2,
            @Nonnull HitPointType t2, @Nonnull String blockName) {
        requireBlock(blocks, blockName);
        String id = "T" + (++count);
        try {
            return ThreadingUtil.runOnGUIwithReturn(() -> {
                TrackSegment s = new TrackSegment(id, c1, t1, c2, t2, true, editor);
                s.setLayoutBlockByName(blockName);
                editor.addLayoutTrack(s, new TrackSegmentView(s, editor));
                connect(c1, t1, s);
                connect(c2, t2, s);
                return s;
            });
        } catch (IllegalStateException e) {
            throw new IllegalArgumentException("can't join " + c1.getName() + " to " + c2.getName(), e);
        }
    }

    private static void connect(LayoutTrack end, HitPointType type, TrackSegment s) {
        if (end instanceof PositionablePoint) {
            PositionablePoint p = (PositionablePoint) end;
            if (p.getConnect1() == null) {
                p.setConnect1(s);
            } else {
                p.setConnect2Actual(s);
            }
        } else if (end instanceof LayoutTurnout) {
            try {
                ((LayoutTurnout) end).setConnection(type, s, HitPointType.TRACK);
            } catch (JmriException e) {
                throw new IllegalStateException(e);
            }
        } else {
            throw new IllegalStateException("not a point or turnout: " + end.getName());
        }
    }

    /**
     * Finish the layout: make the Block paths, as JMRI does after loading a
     * panel, on the GUI thread.
     *
     * @return the layout
     */
    @Nonnull
    public SimulationTestLayout build() {
        LayoutBlockManager lbm = InstanceManager.getDefault(LayoutBlockManager.class);
        ThreadingUtil.runOnGUI(lbm::initializeLayoutBlockPaths);
        return new SimulationTestLayout(editor, blocks, turnouts);
    }
}
