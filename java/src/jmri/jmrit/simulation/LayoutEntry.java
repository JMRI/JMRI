package jmri.jmrit.simulation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.Block;
import jmri.Turnout;
import jmri.jmrit.display.layoutEditor.HitPointType;
import jmri.jmrit.display.layoutEditor.LayoutSlip;
import jmri.jmrit.display.layoutEditor.LayoutTrack;
import jmri.jmrit.display.layoutEditor.LayoutTurnout;
import jmri.jmrit.display.layoutEditor.LevelXing;
import jmri.jmrit.display.layoutEditor.PositionablePoint;
import jmri.jmrit.display.layoutEditor.TrackSegment;

/**
 * An entry into a Block on a Layout Editor drawing.
 * <p>
 * The simulator follows the drawn track itself: from a track segment to
 * whatever its end connects to, through anchors and edge connectors, through a
 * turnout by the leg its Turnout's known state and continuing sense select,
 * through crossovers, slips and level crossings by their own rules. It reads
 * each piece's Layout Block to know which Block the train is in. It does not
 * use the Block paths JMRI builds from the drawing, or any of Layout Editor's
 * routing tools, so a fault in those sends JMRI one way and the train another.
 * The turnout states are read here too, rather than from the Layout Editor
 * objects' own state, for the same reason. Everything is read from a
 * {@link TrackSnapshot}, copied on the layout thread (Swing thread), so the
 * walking can be done on the mover's own thread.
 * <p>
 * Turntables and traversers are not followed yet: a train reaching one stops
 * at the Block boundary before it, with a warning.
 * <p>
 * An entry is one of three kinds: about to run along a track segment from one
 * of its ends; about to run through a turnout, slip or crossing from one of its
 * connection points; or inside a crossover whose two halves are in different
 * Blocks, in the half it leaves by.
 *
 * @author Keith Bruce Copyright (C) 2026
 */
final class LayoutEntry extends TrackEntry {

    /** A connection point of a turnout, slip or crossing. */
    enum Port {
        A, B, C, D
    }

    private enum Kind {
        SEGMENT, NODE, INSIDE
    }

    private final TrackModels models;
    private final Kind kind;
    private final LayoutTrack piece;
    private final int end;
    @CheckForNull
    private final Port from;
    @CheckForNull
    private final Port to;

    private LayoutEntry(TrackModels models, Kind kind, LayoutTrack piece, int end, @CheckForNull Port from,
            @CheckForNull Port to) {
        this.models = models;
        this.kind = kind;
        this.piece = piece;
        this.end = end;
        this.from = from;
        this.to = to;
    }

    static LayoutEntry segment(TrackModels models, TrackSegment s, int end) {
        return new LayoutEntry(models, Kind.SEGMENT, s, end, null, null);
    }

    static LayoutEntry node(TrackModels models, LayoutTrack n, Port from) {
        return new LayoutEntry(models, Kind.NODE, n, 0, from, null);
    }

    static LayoutEntry inside(TrackModels models, LayoutTurnout n, Port from, Port to) {
        return new LayoutEntry(models, Kind.INSIDE, n, 0, from, to);
    }

    // ---------------------------------------------------------------------
    // Reading the drawing

    @CheckForNull
    static Block blockOf(TrackSnapshot snap, TrackSegment s) {
        TrackSnapshot.Segment d = snap.segment(s);
        return d == null ? null : d.block;
    }

    /**
     * The Block of one connection point of a turnout, slip or crossing.
     *
     * @param snap the snapshot of the drawing
     * @param n    the turnout, slip or crossing
     * @param p    the connection point
     * @return the Block, or null if the piece is not in the snapshot or the
     *         connection point has no Block
     */
    @CheckForNull
    static Block blockOf(TrackSnapshot snap, LayoutTrack n, Port p) {
        TrackSnapshot.Node d = snap.node(n);
        return d == null ? null : d.blocks[p.ordinal()];
    }

    /**
     * Is a piece one the simulator can run through: a turnout of any kind,
     * slip or level crossing?
     *
     * @param snap the snapshot of the drawing
     * @param t    the piece, or null
     * @return true if the piece is not null and the snapshot holds it as a
     *         turnout, slip or crossing
     */
    static boolean isNode(TrackSnapshot snap, @CheckForNull LayoutTrack t) {
        return t != null && snap.node(t) != null;
    }

    @CheckForNull
    static Port portOf(LayoutTrack n, HitPointType type) {
        if (n instanceof LayoutSlip) {
            return pick(type, HitPointType.SLIP_A, HitPointType.SLIP_B, HitPointType.SLIP_C, HitPointType.SLIP_D);
        }
        if (n instanceof LayoutTurnout) {
            return pick(type, HitPointType.TURNOUT_A, HitPointType.TURNOUT_B, HitPointType.TURNOUT_C,
                    HitPointType.TURNOUT_D);
        }
        if (n instanceof LevelXing) {
            return pick(type, HitPointType.LEVEL_XING_A, HitPointType.LEVEL_XING_B, HitPointType.LEVEL_XING_C,
                    HitPointType.LEVEL_XING_D);
        }
        return null;
    }

    @CheckForNull
    private static Port pick(HitPointType type, HitPointType a, HitPointType b, HitPointType c, HitPointType d) {
        if (type == a) {
            return Port.A;
        } else if (type == b) {
            return Port.B;
        } else if (type == c) {
            return Port.C;
        } else if (type == d) {
            return Port.D;
        }
        return null;
    }

    static HitPointType typeOf(LayoutTrack n, Port p) {
        HitPointType[] types;
        if (n instanceof LayoutSlip) {
            types = new HitPointType[]{HitPointType.SLIP_A, HitPointType.SLIP_B, HitPointType.SLIP_C,
                HitPointType.SLIP_D};
        } else if (n instanceof LayoutTurnout) {
            types = new HitPointType[]{HitPointType.TURNOUT_A, HitPointType.TURNOUT_B, HitPointType.TURNOUT_C,
                HitPointType.TURNOUT_D};
        } else {
            types = new HitPointType[]{HitPointType.LEVEL_XING_A, HitPointType.LEVEL_XING_B,
                HitPointType.LEVEL_XING_C, HitPointType.LEVEL_XING_D};
        }
        return types[p.ordinal()];
    }

    /**
     * The track segment at one connection point of a turnout, slip or
     * crossing.
     *
     * @param snap the snapshot of the drawing
     * @param n    the turnout, slip or crossing
     * @param p    the connection point
     * @return the track segment, or null if the piece is not in the snapshot
     *         or nothing is connected there
     */
    @CheckForNull
    static TrackSegment segmentAt(TrackSnapshot snap, LayoutTrack n, Port p) {
        TrackSnapshot.Node d = snap.node(n);
        return d == null ? null : d.connects[p.ordinal()];
    }

    @CheckForNull
    static LayoutTrack connectOf(TrackSnapshot snap, TrackSegment s, int end) {
        TrackSnapshot.Segment d = snap.segment(s);
        if (d == null) {
            return null;
        }
        return end == 1 ? d.connect1 : d.connect2;
    }

    @CheckForNull
    static HitPointType typeOf(TrackSnapshot snap, TrackSegment s, int end) {
        TrackSnapshot.Segment d = snap.segment(s);
        if (d == null) {
            return null;
        }
        return end == 1 ? d.type1 : d.type2;
    }

    /**
     * Which end of a segment connects to a piece at a connection type.
     *
     * @param snap    the snapshot
     * @param s       the segment
     * @param n       the piece, or one of a pair of linked edge connectors
     * @param linked  the other of the pair, or null
     * @param type    the connection type at the piece
     * @param exclude an end not to answer, or 0
     * @return 1 or 2, or 0 if the drawing does not connect them
     */
    static int endAt(TrackSnapshot snap, TrackSegment s, LayoutTrack n, @CheckForNull LayoutTrack linked,
            HitPointType type, int exclude) {
        for (int e = 1; e <= 2; e++) {
            LayoutTrack c = connectOf(snap, s, e);
            if (e != exclude && (c == n || (linked != null && c == linked)) && typeOf(snap, s, e) == type) {
                return e;
            }
        }
        return 0;
    }

    /**
     * The state of a Layout Editor turnout's Turnout, and of its second
     * Turnout where it has one, from the Turnouts' known states as last told.
     *
     * @param snap the snapshot, which holds the Turnouts' states
     * @param t    the turnout
     * @return CLOSED, THROWN, or UNKNOWN or INCONSISTENT
     */
    static int turnoutState(TrackSnapshot snap, TrackSnapshot.Node t) {
        int s = snap.state(t.turnout);
        if (t.second != null) {
            int s2 = snap.state(t.second);
            if (t.secondInverted) {
                s2 = (s2 == Turnout.CLOSED) ? Turnout.THROWN : (s2 == Turnout.THROWN) ? Turnout.CLOSED : s2;
            }
            if (s2 != s) {
                return Turnout.INCONSISTENT;
            }
        }
        return s;
    }

    /**
     * The way through a turnout, slip or crossing from a connection point, as
     * its Turnouts are set now.
     *
     * @param snap the snapshot of the drawing and the Turnouts' states
     * @param n    the piece
     * @param p    the connection point entered at
     * @param why  its first element set to the reason if there is no way through
     * @return the connection point left by, or null if the way is not set
     */
    @CheckForNull
    Port wayThrough(TrackSnapshot snap, LayoutTrack n, Port p, Message[] why) {
        TrackSnapshot.Node t = snap.node(n);
        if (t == null) {
            why[0] = models.message("TrackNotSupported", n.getName());
            return null;
        }
        LayoutTurnout.TurnoutType type = t.type;
        if (type == null) {
            // a level crossing
            return (p == Port.A) ? Port.C : (p == Port.C) ? Port.A : (p == Port.B) ? Port.D : Port.B;
        }
        if (t.isSlip()) {
            return wayThroughSlip(snap, n, t, p, why);
        }
        int state = turnoutState(snap, t);
        if (state != Turnout.CLOSED && state != Turnout.THROWN) {
            why[0] = models.message("TurnoutNotSet", n.getName());
            return null;
        }
        Port way = null;
        switch (type) {
            case RH_TURNOUT:
            case LH_TURNOUT:
            case WYE_TURNOUT: {
                // A is the throat; B is the leg the continuing sense selects
                boolean continuing = (state == t.continuingSense);
                if (p == Port.A) {
                    way = continuing ? Port.B : Port.C;
                } else if ((p == Port.B && continuing) || (p == Port.C && !continuing)) {
                    way = Port.A;
                }
                break;
            }
            case RH_XOVER:
            case LH_XOVER:
            case DOUBLE_XOVER:
                way = wayThroughCrossover(type, state, p);
                break;
            default:
                break;
        }
        if (way == null) {
            why[0] = models.message("PointsAgainst", n.getName());
        }
        return way;
    }

    /**
     * Crossovers: closed, A-B and C-D straight through; thrown, the crossing
     * routes, A-C for right-hand, B-D for left-hand, both for a double.
     *
     * @param type  the kind of crossover
     * @param state the crossover's state, CLOSED or THROWN
     * @param p     the connection point entered at
     * @return the connection point left by, or null if there is no way through
     *         from p as set
     */
    @CheckForNull
    static Port wayThroughCrossover(LayoutTurnout.TurnoutType type, int state, Port p) {
        if (state == Turnout.CLOSED) {
            return (p == Port.A) ? Port.B : (p == Port.B) ? Port.A : (p == Port.C) ? Port.D : Port.C;
        }
        boolean ac = (type == LayoutTurnout.TurnoutType.RH_XOVER || type == LayoutTurnout.TurnoutType.DOUBLE_XOVER);
        boolean bd = (type == LayoutTurnout.TurnoutType.LH_XOVER || type == LayoutTurnout.TurnoutType.DOUBLE_XOVER);
        if (ac && p == Port.A) {
            return Port.C;
        } else if (ac && p == Port.C) {
            return Port.A;
        } else if (bd && p == Port.B) {
            return Port.D;
        } else if (bd && p == Port.D) {
            return Port.B;
        }
        return null;
    }

    /**
     * Slips: the state of their two Turnouts selects one of the routes A-C,
     * A-D, B-D and (double slips) B-C, by the slip's own table of turnout
     * states.
     */
    @CheckForNull
    private Port wayThroughSlip(TrackSnapshot snap, LayoutTrack n, TrackSnapshot.Node s, Port p,
            Message[] why) {
        int a = snap.state(s.turnout);
        int b = snap.state(s.turnoutB);
        Integer route = null;
        for (java.util.Map.Entry<Integer, int[]> e : s.slipStates.entrySet()) {
            if (e.getValue()[0] == a && e.getValue()[1] == b) {
                route = e.getKey();
            }
        }
        if (route == null) {
            why[0] = models.message("TurnoutNotSet", n.getName());
            return null;
        }
        Port x;
        Port y;
        if (route == LayoutTurnout.STATE_AC) {
            x = Port.A;
            y = Port.C;
        } else if (route == LayoutTurnout.STATE_AD) {
            x = Port.A;
            y = Port.D;
        } else if (route == LayoutTurnout.STATE_BD) {
            x = Port.B;
            y = Port.D;
        } else {
            x = Port.B;
            y = Port.C;
        }
        if (p == x) {
            return y;
        } else if (p == y) {
            return x;
        }
        why[0] = models.message("PointsAgainst", n.getName());
        return null;
    }

    // ---------------------------------------------------------------------
    // Entries

    @Override
    @Nonnull
    Block getBlock() {
        Block b = blockOrNull();
        if (b == null) {
            throw new IllegalStateException("entry into track with no Block: " + describe());
        }
        return b;
    }

    @CheckForNull
    Block blockOrNull() {
        return blockIn(models.snapshot());
    }

    @CheckForNull
    private Block blockIn(TrackSnapshot snap) {
        switch (kind) {
            case SEGMENT:
                return blockOf(snap, (TrackSegment) piece);
            case NODE:
                return blockOf(snap, piece, Objects.requireNonNull(from));
            default:
                return blockOf(snap, piece, Objects.requireNonNull(to));
        }
    }

    /**
     * Where a segment's end leads, past anchors and edge connectors.
     *
     * @return the entry beyond, or null at the end of the track
     */
    @CheckForNull
    private LayoutEntry beyond(TrackSnapshot snap, TrackSegment s, int e) {
        LayoutTrack c = connectOf(snap, s, e);
        TrackSnapshot.Point pp = (c == null) ? null : snap.point(c);
        if (pp != null) {
            if (pp.type != PositionablePoint.PointType.ANCHOR
                    && pp.type != PositionablePoint.PointType.EDGE_CONNECTOR) {
                return null;
            }
            TrackSegment u;
            PositionablePoint linked = null;
            if (pp.connect1 == s) {
                u = pp.connect2; // for an edge connector, through the linked one
                if (pp.type == PositionablePoint.PointType.EDGE_CONNECTOR) {
                    linked = pp.linked;
                }
            } else {
                u = pp.connect1;
            }
            if (u == null) {
                return null;
            }
            int f = endAt(snap, u, c, linked, HitPointType.POS_POINT, u == s ? e : 0);
            if (f == 0) {
                return null;
            }
            return segment(models, u, f);
        }
        HitPointType type = typeOf(snap, s, e);
        if (c != null && type != null && isNode(snap, c)) {
            Port p = portOf(c, type);
            if (p != null) {
                return node(models, c, p);
            }
        }
        return null;
    }

    /**
     * Is this segment end at a piece the simulator can't follow?
     */
    private static boolean unsupportedAt(TrackSnapshot snap, TrackSegment s, int e) {
        LayoutTrack c = connectOf(snap, s, e);
        if (c == null) {
            return false;
        }
        TrackSnapshot.Point pp = snap.point(c);
        if (pp != null) {
            return pp.type == PositionablePoint.PointType.NONE;
        }
        HitPointType type = typeOf(snap, s, e);
        return !isNode(snap, c) || type == null || portOf(c, type) == null;
    }

    @Override
    @CheckForNull
    TrackEntry reverse() {
        TrackSnapshot snap = models.snapshot();
        switch (kind) {
            case SEGMENT:
                return beyond(snap, (TrackSegment) piece, end);
            case NODE: {
                Port p = Objects.requireNonNull(from);
                TrackSegment s = segmentAt(snap, piece, p);
                if (s == null) {
                    return null;
                }
                int f = endAt(snap, s, piece, null, typeOf(piece, p), 0);
                return f == 0 ? null : segment(models, s, f);
            }
            default:
                return inside(models, (LayoutTurnout) piece, Objects.requireNonNull(to), Objects.requireNonNull(from));
        }
    }

    @Override
    @Nonnull
    Passage follow() {
        TrackSnapshot snap = models.snapshot();
        Block block = blockIn(snap);
        List<Object> steps = new ArrayList<>();
        if (block == null) {
            return new Passage(this, TrackModels.DEFAULT_LENGTH_MM, new LayoutRoute(null, steps), null, null,
                    Passage.Stop.BLOCKED, models.message("TrackWithoutBlock", piece.getName()));
        }
        Set<Object> seen = new HashSet<>();
        LayoutEntry at = this;
        while (true) {
            if (!seen.add(at.key())) {
                return blocked(block, steps, models.message("TrackLoops", models.name(block)));
            }
            if (at.kind == Kind.SEGMENT) {
                TrackSegment s = (TrackSegment) at.piece;
                if (blockOf(snap, s) != block) {
                    return blocked(block, steps, models.message("TrackWithoutBlock", s.getName()));
                }
                steps.add(new SegmentStep(s, at.end));
                int far = 3 - at.end;
                LayoutEntry back = segment(models, s, far);
                if (unsupportedAt(snap, s, far)) {
                    return blocked(block, steps, models.message("TrackNotSupported",
                            Objects.requireNonNull(connectOf(snap, s, far)).getName()));
                }
                LayoutEntry next = beyond(snap, s, far);
                if (next == null) {
                    return end(block, steps, back);
                }
                if (next.blockIn(snap) != block) {
                    return boundary(block, steps, next, back);
                }
                at = next;
            } else {
                Port p = Objects.requireNonNull(at.from);
                Port q;
                if (at.kind == Kind.INSIDE) {
                    q = Objects.requireNonNull(at.to);
                    Message[] why = new Message[1];
                    if (wayThrough(snap, at.piece, p, why) != q) {
                        return blocked(block, steps, models.message("PointsMovedUnderTrain", at.piece.getName()));
                    }
                } else {
                    Message[] why = new Message[1];
                    q = wayThrough(snap, at.piece, p, why);
                    if (q == null) {
                        return blocked(block, steps, why[0]);
                    }
                }
                steps.add(new NodeStep(at.piece, p, q));
                if (at.kind == Kind.NODE && blockOf(snap, at.piece, q) != block) {
                    // a crossover with a Block boundary inside it
                    return boundary(block, steps, inside(models, (LayoutTurnout) at.piece, p, q),
                            inside(models, (LayoutTurnout) at.piece, q, p));
                }
                LayoutEntry back = node(models, at.piece, q);
                TrackSegment s = segmentAt(snap, at.piece, q);
                if (s == null) {
                    return end(block, steps, back);
                }
                int f = endAt(snap, s, at.piece, null, typeOf(at.piece, q), 0);
                if (f == 0) {
                    return end(block, steps, back);
                }
                LayoutEntry next = segment(models, s, f);
                if (blockOf(snap, s) != block) {
                    return boundary(block, steps, next, back);
                }
                at = next;
            }
        }
    }

    private Passage boundary(Block block, List<Object> steps, LayoutEntry next, LayoutEntry back) {
        return new Passage(this, models.length(block, 0), new LayoutRoute(block, steps), next, back,
                Passage.Stop.NONE, null);
    }

    private Passage end(Block block, List<Object> steps, LayoutEntry back) {
        return new Passage(this, models.length(block, 0), new LayoutRoute(block, steps), null, back,
                Passage.Stop.END_OF_TRACK, models.message("EndOfTrack", models.name(block)));
    }

    private Passage blocked(Block block, List<Object> steps, Message reason) {
        return new Passage(this, models.length(block, 0), new LayoutRoute(block, steps), null, null,
                Passage.Stop.BLOCKED, reason);
    }

    private Object key() {
        return java.util.Arrays.asList(kind, piece, end, from, to);
    }

    @Override
    String describe() {
        switch (kind) {
            case SEGMENT:
                return "segment " + piece.getName() + " end " + end;
            case NODE:
                return piece.getName() + " at " + from;
            default:
                return piece.getName() + " " + from + " to " + to;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof LayoutEntry)) {
            return false;
        }
        LayoutEntry e = (LayoutEntry) o;
        return e.kind == kind && e.piece == piece && e.end == end && e.from == from && e.to == to;
    }

    @Override
    public int hashCode() {
        return Objects.hash(kind, piece, end, from, to);
    }

    // ---------------------------------------------------------------------
    // Routes

    /** Running along a segment from one end. */
    static final class SegmentStep {

        final TrackSegment segment;
        final int end;

        SegmentStep(TrackSegment segment, int end) {
            this.segment = segment;
            this.end = end;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof SegmentStep && ((SegmentStep) o).segment == segment && ((SegmentStep) o).end == end;
        }

        @Override
        public int hashCode() {
            return Objects.hash(segment, end);
        }
    }

    /** Running through a turnout, slip or crossing from one point to another. */
    static final class NodeStep {

        final LayoutTrack node;
        final Port from;
        final Port to;

        NodeStep(LayoutTrack node, Port from, Port to) {
            this.node = node;
            this.from = from;
            this.to = to;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof NodeStep)) {
                return false;
            }
            NodeStep n = (NodeStep) o;
            return n.node == node && n.from == from && n.to == to;
        }

        @Override
        public int hashCode() {
            return Objects.hash(node, from, to);
        }

        /**
         * Do two ways through the same piece foul each other? Only the two
         * straight routes of a crossover don't; every other pair of different
         * ways through a piece shares or crosses track.
         *
         * @param other the other way through the same piece
         * @return true if the two ways foul each other
         */
        boolean fouls(NodeStep other) {
            Set<Port> mine = Set.of(from, to);
            Set<Port> theirs = Set.of(other.from, other.to);
            if (mine.equals(theirs)) {
                return false; // the same track: positions decide
            }
            if (node instanceof LayoutTurnout && ((LayoutTurnout) node).isTurnoutTypeXover()) {
                Set<Port> ab = Set.of(Port.A, Port.B);
                Set<Port> cd = Set.of(Port.C, Port.D);
                if ((mine.equals(ab) && theirs.equals(cd)) || (mine.equals(cd) && theirs.equals(ab))) {
                    return false;
                }
            }
            return true;
        }
    }

    /** The pieces followed through one Block, in order. */
    static final class LayoutRoute implements Route {

        @CheckForNull
        final Block block;
        final List<Object> steps;

        LayoutRoute(@CheckForNull Block block, List<Object> steps) {
            this.block = block;
            this.steps = steps;
        }

        @Override
        public boolean sameAs(@Nonnull Route other) {
            return other instanceof LayoutRoute && ((LayoutRoute) other).block == block
                    && ((LayoutRoute) other).steps.equals(steps);
        }

        @Override
        public boolean reverseOf(@Nonnull Route other) {
            return other instanceof LayoutRoute && ((LayoutRoute) other).reversed().sameAs(this);
        }

        @Override
        public boolean fouls(@Nonnull Route other) {
            if (!(other instanceof LayoutRoute) || sameAs(other) || reverseOf(other)) {
                return false;
            }
            LayoutRoute o = (LayoutRoute) other;
            for (Object a : steps) {
                for (Object b : o.steps) {
                    if (a instanceof SegmentStep && b instanceof SegmentStep
                            && ((SegmentStep) a).segment == ((SegmentStep) b).segment && block == o.block) {
                        return true;
                    }
                    if (a instanceof NodeStep && b instanceof NodeStep && ((NodeStep) a).node == ((NodeStep) b).node
                            && ((NodeStep) a).fouls((NodeStep) b)) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        @Nonnull
        public Route reversed() {
            List<Object> r = new ArrayList<>();
            for (int i = steps.size() - 1; i >= 0; i--) {
                Object s = steps.get(i);
                if (s instanceof SegmentStep) {
                    r.add(new SegmentStep(((SegmentStep) s).segment, 3 - ((SegmentStep) s).end));
                } else {
                    NodeStep n = (NodeStep) s;
                    r.add(new NodeStep(n.node, n.to, n.from));
                }
            }
            return new LayoutRoute(block, r);
        }
    }

    // ---------------------------------------------------------------------
    // Finding where a Block is entered, for placing a train

    /**
     * Every place on a drawing where a Block is entered from a neighbouring
     * Block, or from the end of the track.
     *
     * @param models the layout's track models
     * @param block  the Block
     * @param from   the neighbouring Block, or null for the end of the track
     * @return the entries
     */
    static List<LayoutEntry> entriesInto(TrackModels models, Block block, @CheckForNull Block from) {
        TrackSnapshot snap = models.snapshot();
        List<LayoutEntry> result = new ArrayList<>();
        for (LayoutTrack t : snap.pieces()) {
            if (t instanceof TrackSegment) {
                TrackSegment s = (TrackSegment) t;
                if (blockOf(snap, s) != block) {
                    continue;
                }
                for (int e = 1; e <= 2; e++) {
                    LayoutEntry entry = segment(models, s, e);
                    TrackEntry rev = entry.reverse();
                    Block other = (rev == null) ? null : ((LayoutEntry) rev).blockIn(snap);
                    if (other != block && other == from && !unsupportedAt(snap, s, e)) {
                        add(result, entry);
                    }
                }
            } else if (isNode(snap, t)) {
                for (Port p : Port.values()) {
                    if (blockOf(snap, t, p) != block) {
                        continue;
                    }
                    TrackSegment s = segmentAt(snap, t, p);
                    Block other = (s == null) ? null : blockOf(snap, s);
                    if (other != block && other == from && s != null) {
                        add(result, node(models, t, p));
                    }
                }
                TrackSnapshot.Node x = snap.node(t);
                if (x != null && x.isCrossover()) {
                    for (int state : new int[]{Turnout.CLOSED, Turnout.THROWN}) {
                        for (Port p : Port.values()) {
                            Port q = wayThroughCrossover(x.type, state, p);
                            if (q != null && blockOf(snap, t, q) == block && blockOf(snap, t, p) != block
                                    && blockOf(snap, t, p) == from) {
                                add(result, inside(models, (LayoutTurnout) t, p, q));
                            }
                        }
                    }
                }
            }
        }
        return result;
    }

    private static void add(List<LayoutEntry> list, LayoutEntry e) {
        if (!list.contains(e)) {
            list.add(e);
        }
    }
}
