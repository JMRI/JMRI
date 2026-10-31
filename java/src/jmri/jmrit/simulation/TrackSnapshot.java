package jmri.jmrit.simulation;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.BeanSetting;
import jmri.Block;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.InvokeOnAnyThread;
import jmri.InvokeOnLayoutThread;
import jmri.NamedBean;
import jmri.Path;
import jmri.Sensor;
import jmri.Turnout;
import jmri.TurnoutManager;
import jmri.jmrit.display.EditorManager;
import jmri.jmrit.display.layoutEditor.HitPointType;
import jmri.jmrit.display.layoutEditor.LayoutBlock;
import jmri.jmrit.display.layoutEditor.LayoutEditor;
import jmri.jmrit.display.layoutEditor.LayoutSlip;
import jmri.jmrit.display.layoutEditor.LayoutTrack;
import jmri.jmrit.display.layoutEditor.LayoutTurnout;
import jmri.jmrit.display.layoutEditor.LevelXing;
import jmri.jmrit.display.layoutEditor.PositionablePoint;
import jmri.jmrit.display.layoutEditor.TrackSegment;
import jmri.jmrit.logix.OBlock;
import jmri.jmrit.logix.OBlockManager;
import jmri.jmrit.logix.OPath;
import jmri.jmrit.logix.Portal;
import jmri.util.ThreadingUtil;

/**
 * Everything the simulator reads of the layout's configuration, copied on the
 * layout thread (Swing thread) so the mover's own thread never reads layout
 * objects. Bean states (turnouts and the other beans paths depend on) are
 * kept up to date by listeners, which write them into a concurrent map the
 * mover reads.
 * <p>
 * The configuration (the drawing, Blocks and their paths, lengths and
 * sensors) is copied when a snapshot is taken: when a train is placed, and
 * when detector outputs change. The layout objects themselves are kept only
 * as identities.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
@javax.annotation.concurrent.ThreadSafe
final class TrackSnapshot {

    /** A track segment: its two ends and its Block. */
    static final class Segment {

        final LayoutTrack connect1;
        final HitPointType type1;
        final LayoutTrack connect2;
        final HitPointType type2;
        @CheckForNull
        final Block block;

        Segment(TrackSegment s) {
            connect1 = s.getConnect1();
            type1 = s.getType1();
            connect2 = s.getConnect2();
            type2 = s.getType2();
            block = blockOf(s.getLayoutBlock());
        }
    }

    /** An anchor, end bumper or edge connector. */
    static final class Point {

        final PositionablePoint.PointType type;
        @CheckForNull
        final TrackSegment connect1;
        /** For an edge connector, the segment beyond its linked connector. */
        @CheckForNull
        final TrackSegment connect2;
        @CheckForNull
        final PositionablePoint linked;

        Point(PositionablePoint p) {
            type = p.getType();
            connect1 = p.getConnect1();
            connect2 = p.getConnect2();
            linked = p.getLinkedPoint();
        }
    }

    /** A turnout, crossover, slip or level crossing. */
    static final class Node {

        @CheckForNull
        final LayoutTurnout.TurnoutType type; // null for a level crossing
        final TrackSegment[] connects = new TrackSegment[4];
        final Block[] blocks = new Block[4];
        final int continuingSense;
        @CheckForNull
        final Turnout turnout;
        @CheckForNull
        final Turnout second;
        final boolean secondInverted;
        @CheckForNull
        final Turnout turnoutB;
        /** A slip's routes: key LayoutTurnout.STATE_xx, value {turnout A state, turnout B state}. */
        final Map<Integer, int[]> slipStates = new HashMap<>();

        Node(LevelXing x) {
            type = null;
            LayoutTrack[] c = {x.getConnectA(), x.getConnectB(), x.getConnectC(), x.getConnectD()};
            for (int i = 0; i < 4; i++) {
                connects[i] = (c[i] instanceof TrackSegment) ? (TrackSegment) c[i] : null;
            }
            Block ac = blockOf(x.getLayoutBlockAC());
            Block bd = blockOf(x.getLayoutBlockBD());
            blocks[0] = ac;
            blocks[1] = bd;
            blocks[2] = ac;
            blocks[3] = bd;
            continuingSense = Turnout.CLOSED;
            turnout = null;
            second = null;
            secondInverted = false;
            turnoutB = null;
        }

        Node(LayoutTurnout t) {
            type = t.getTurnoutType();
            LayoutTrack[] c = {t.getConnectA(), t.getConnectB(), t.getConnectC(), t.getConnectD()};
            for (int i = 0; i < 4; i++) {
                connects[i] = (c[i] instanceof TrackSegment) ? (TrackSegment) c[i] : null;
            }
            if (t.isTurnoutTypeXover()) {
                blocks[0] = blockOf(t.getLayoutBlock());
                blocks[1] = blockOf(t.getLayoutBlockB());
                blocks[2] = blockOf(t.getLayoutBlockC());
                blocks[3] = blockOf(t.getLayoutBlockD());
            } else {
                // a turnout, wye or slip is all in one Block
                Block b = blockOf(t.getLayoutBlock());
                for (int i = 0; i < 4; i++) {
                    blocks[i] = b;
                }
            }
            continuingSense = t.getContinuingSense();
            TurnoutManager tm = InstanceManager.getDefault(TurnoutManager.class);
            turnout = named(tm, t.getTurnoutName());
            if (t instanceof LayoutSlip) {
                LayoutSlip s = (LayoutSlip) t;
                second = null;
                secondInverted = false;
                turnoutB = named(tm, s.getTurnoutBName());
                for (Map.Entry<Integer, LayoutSlip.TurnoutState> e : s.getTurnoutStates().entrySet()) {
                    slipStates.put(e.getKey(),
                            new int[]{e.getValue().getTurnoutAState(), e.getValue().getTurnoutBState()});
                }
            } else {
                second = named(tm, t.getSecondTurnoutName());
                secondInverted = t.isSecondTurnoutInverted();
                turnoutB = null;
            }
        }

        boolean isSlip() {
            return type == LayoutTurnout.TurnoutType.SINGLE_SLIP || type == LayoutTurnout.TurnoutType.DOUBLE_SLIP;
        }

        boolean isCrossover() {
            return type != null && LayoutTurnout.isTurnoutTypeXover(type);
        }

        @CheckForNull
        private static Turnout named(TurnoutManager tm, String name) {
            return (name == null || name.isEmpty()) ? null : tm.getTurnout(name);
        }
    }

    /** One turnout or other bean setting a path needs. */
    static final class Setting {

        @CheckForNull
        final NamedBean bean;
        final int state;

        Setting(BeanSetting s) {
            bean = s.getBean();
            state = s.getSetting();
        }
    }

    /** A Block's path to a neighbouring Block. */
    static final class BlockPath {

        @CheckForNull
        final Block neighbour;
        final float length;
        final List<Setting> settings;

        BlockPath(Path p) {
            neighbour = p.getBlock();
            length = p.getLength();
            settings = settings(p);
        }
    }

    /** An OBlock's path, between two of its Portals (either may be null). */
    static final class BlockOPath {

        final OPath path;
        @CheckForNull
        final Portal from;
        @CheckForNull
        final Portal to;
        final float length;
        final List<Setting> settings;

        BlockOPath(OPath p) {
            path = p;
            from = p.getFromPortal();
            to = p.getToPortal();
            length = p.getLength();
            settings = settings(p);
        }
    }

    /** A Portal: its name and the OBlocks either side. */
    static final class PortalData {

        final String name;
        @CheckForNull
        final Block from;
        @CheckForNull
        final Block to;

        PortalData(Portal p) {
            name = p.getName();
            from = p.getFromBlock();
            to = p.getToBlock();
        }

        @CheckForNull
        Block across(Block block) {
            return from == block ? to : from;
        }
    }

    /** A Block: name, length, sensor and paths. */
    static final class BlockData {

        final String name;
        final float length;
        @CheckForNull
        final Sensor sensor;
        @CheckForNull
        final SimulatedDetectorOutput output;
        final List<BlockPath> paths = new ArrayList<>();
        final List<BlockOPath> oPaths = new ArrayList<>();
        final List<Portal> portals = new ArrayList<>();

        BlockData(Block b, List<SimulatedDetectorOutput> outputs) {
            name = b.getDisplayName();
            length = b.getLengthMm();
            Sensor s = b.getSensor();
            sensor = s;
            SimulatedDetectorOutput o = null;
            if (s != null) {
                for (SimulatedDetectorOutput d : outputs) {
                    if (d.reports(s)) {
                        o = d;
                        break;
                    }
                }
            }
            output = o;
            for (Path p : b.getPaths()) {
                if (p instanceof OPath) {
                    oPaths.add(new BlockOPath((OPath) p));
                } else {
                    paths.add(new BlockPath(p));
                }
            }
            if (b instanceof OBlock) {
                portals.addAll(((OBlock) b).getPortals());
            }
        }
    }

    private final Map<TrackSegment, Segment> segments = new HashMap<>();
    private final Map<PositionablePoint, Point> points = new HashMap<>();
    private final Map<LayoutTrack, Node> nodes = new HashMap<>();
    private final List<LayoutTrack> pieces = new ArrayList<>();
    private final Map<Block, BlockData> blocks = new HashMap<>();
    private final Map<Portal, PortalData> portals = new HashMap<>();
    private final Map<Sensor, String> unreported = new HashMap<>();
    private final Map<NamedBean, Integer> states = new ConcurrentHashMap<>();
    private final Map<NamedBean, PropertyChangeListener> listeners = new HashMap<>();

    private TrackSnapshot() {
    }

    /**
     * Take a snapshot. Runs on the layout thread (Swing thread), waiting for
     * it if called from another.
     *
     * @param outputs the detector outputs, to choose each sensor's
     * @return the snapshot
     */
    @InvokeOnAnyThread
    static TrackSnapshot take(List<SimulatedDetectorOutput> outputs) {
        TrackSnapshot s = new TrackSnapshot();
        // waits for the layout thread to finish, so what it wrote is seen here; a failure is thrown
        // here rather than leaving a part-copied snapshot
        try {
            ThreadingUtil.runOnLayoutWithJmriException(() -> s.read(outputs));
        } catch (jmri.JmriException e) {
            throw new IllegalStateException("copying the layout's configuration failed", e); // read() throws none
        }
        return s;
    }

    /**
     * An empty snapshot, for a layout with no train placed yet.
     *
     * @return the snapshot
     */
    static TrackSnapshot empty() {
        return new TrackSnapshot();
    }

    @InvokeOnLayoutThread
    private void read(List<SimulatedDetectorOutput> outputs) {
        EditorManager editors = InstanceManager.getNullableDefault(EditorManager.class);
        if (editors != null) {
            for (LayoutEditor le : editors.getAll(LayoutEditor.class)) {
                for (LayoutTrack t : le.getLayoutTracks()) {
                    pieces.add(t);
                    if (t instanceof TrackSegment) {
                        segments.put((TrackSegment) t, new Segment((TrackSegment) t));
                    } else if (t instanceof PositionablePoint) {
                        points.put((PositionablePoint) t, new Point((PositionablePoint) t));
                    } else if (t instanceof LayoutTurnout) {
                        Node n = new Node((LayoutTurnout) t);
                        nodes.put(t, n);
                        watch(n.turnout);
                        watch(n.second);
                        watch(n.turnoutB);
                    } else if (t instanceof LevelXing) {
                        nodes.put(t, new Node((LevelXing) t));
                    }
                }
            }
        }
        List<Block> all = new ArrayList<>(InstanceManager.getDefault(BlockManager.class).getNamedBeanSet());
        OBlockManager obm = InstanceManager.getNullableDefault(OBlockManager.class);
        if (obm != null) {
            all.addAll(obm.getNamedBeanSet());
        }
        for (Block b : all) {
            BlockData d = new BlockData(b, outputs);
            blocks.put(b, d);
            if (d.sensor != null && d.output == null) {
                unreported.put(d.sensor, d.name);
            }
            for (BlockPath p : d.paths) {
                p.settings.forEach(x -> watch(x.bean));
            }
            for (BlockOPath p : d.oPaths) {
                p.settings.forEach(x -> watch(x.bean));
            }
            for (Portal p : d.portals) {
                portals.computeIfAbsent(p, PortalData::new);
            }
        }
    }

    private void watch(@CheckForNull NamedBean bean) {
        if (bean == null || listeners.containsKey(bean)) {
            return;
        }
        states.put(bean, bean.getState());
        PropertyChangeListener l = e -> states.put(bean, bean.getState());
        listeners.put(bean, l);
        bean.addPropertyChangeListener(l);
    }

    /**
     * Stop listening to the beans. Runs on the layout thread (Swing thread)
     * eventually.
     */
    @InvokeOnAnyThread
    void dispose() {
        Map<NamedBean, PropertyChangeListener> copy = new HashMap<>(listeners);
        listeners.clear();
        ThreadingUtil.runOnLayoutEventually(() -> copy.forEach(NamedBean::removePropertyChangeListener));
    }

    // ---------------------------------------------------------------------
    // Reading, from any thread

    private static List<Setting> settings(Path p) {
        List<Setting> result = new ArrayList<>();
        for (BeanSetting s : p.getSettings()) {
            result.add(new Setting(s));
        }
        return Collections.unmodifiableList(result);
    }

    @CheckForNull
    static Block blockOf(@CheckForNull LayoutBlock lb) {
        return lb == null ? null : lb.getBlock();
    }

    /**
     * @param bean a bean
     * @return its state as last told, or UNKNOWN if not watched
     */
    @InvokeOnAnyThread
    int state(@CheckForNull NamedBean bean) {
        if (bean == null) {
            return NamedBean.UNKNOWN;
        }
        return states.getOrDefault(bean, NamedBean.UNKNOWN);
    }

    /**
     * Are all these settings met, by the states as last told? This repeats
     * what {@link Path#checkPathSet()} does, on purpose: the simulator must
     * not use the logic it tests.
     *
     * @param settings the settings
     * @return true if every bean is in the state its setting asks for
     */
    boolean met(List<Setting> settings) {
        for (Setting s : settings) {
            if (s.bean == null || state(s.bean) != s.state) {
                return false;
            }
        }
        return true;
    }

    List<LayoutTrack> pieces() {
        return Collections.unmodifiableList(pieces);
    }

    @CheckForNull
    Segment segment(LayoutTrack t) {
        return (t instanceof TrackSegment) ? segments.get(t) : null;
    }

    @CheckForNull
    Point point(LayoutTrack t) {
        return (t instanceof PositionablePoint) ? points.get(t) : null;
    }

    @CheckForNull
    Node node(LayoutTrack t) {
        return nodes.get(t);
    }

    @CheckForNull
    BlockData block(Block b) {
        return blocks.get(b);
    }

    @CheckForNull
    PortalData portal(Portal p) {
        return portals.get(p);
    }

    /**
     * @param b a Block
     * @return its name as when the snapshot was taken
     */
    String name(Block b) {
        BlockData d = blocks.get(b);
        return d != null ? d.name : b.getSystemName();
    }

    /**
     * @param b a Block
     * @return the output for its sensor, if it has one an output reports
     */
    Optional<SimulatedDetectorOutput> output(Block b) {
        BlockData d = blocks.get(b);
        return Optional.ofNullable(d == null ? null : d.output);
    }

    /**
     * @return the sensors no detector output reports, with their Blocks' names
     */
    Map<Sensor, String> unreported() {
        return Collections.unmodifiableMap(unreported);
    }

    /**
     * @param b a Block
     * @return its sensor, or null for a dark Block
     */
    @CheckForNull
    Sensor sensor(Block b) {
        BlockData d = blocks.get(b);
        return d == null ? null : d.sensor;
    }

    /**
     * @param b a Block
     * @return true if any piece of the drawing is in it
     */
    boolean isDrawn(@Nonnull Block b) {
        for (Segment s : segments.values()) {
            if (s.block == b) {
                return true;
            }
        }
        for (Node n : nodes.values()) {
            for (Block x : n.blocks) {
                if (x == b) {
                    return true;
                }
            }
        }
        return false;
    }
}
