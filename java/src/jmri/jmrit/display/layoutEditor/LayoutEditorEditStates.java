package jmri.jmrit.display.layoutEditor;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.NamedBeanHandle;
import jmri.jmrit.display.EditState;
import jmri.jmrit.display.EditState.Property;

/**
 * The panel properties of Layout Editor's tracks and shapes, as recorded by
 * {@link jmri.jmrit.display.EditHistory}: geometry, connections, Blocks and
 * the names of the turnouts, signals and sensors placed on them, and the
 * drawing settings. One table per kind of track lists its properties in the
 * order they are written back.
 * <p>
 * Not recorded: Inline LogixNG, a turntable's or traverser's "Dispatcher
 * managed" setting, and anything that belongs to a bean (a Block's sensor, a
 * Turnout's name) rather than to the panel.
 * <p>
 * LayoutBlock use counts are not kept by these properties;
 * {@link LayoutEditorEditModel} sets them from which tracks use which Blocks.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
final class LayoutEditorEditStates {

    private LayoutEditorEditStates() {
        // only static methods
    }

    /**
     * Record the state of a track.
     *
     * @param v the track's view
     * @return its state
     */
    @Nonnull
    static EditState<?> of(@Nonnull LayoutTrackView v) {
        if (v instanceof LayoutSlipView) {
            return new EditState<>((LayoutSlipView) v, SLIP);
        } else if (v instanceof LayoutTurnoutView) {
            return new EditState<>((LayoutTurnoutView) v, TURNOUT);
        } else if (v instanceof TrackSegmentView) {
            return new EditState<>((TrackSegmentView) v, TRACK_SEGMENT);
        } else if (v instanceof PositionablePointView) {
            return new EditState<>((PositionablePointView) v, POINT);
        } else if (v instanceof LevelXingView) {
            return new EditState<>((LevelXingView) v, LEVEL_XING);
        } else if (v instanceof LayoutTurntableView) {
            return new EditState<>((LayoutTurntableView) v, TURNTABLE);
        } else if (v instanceof LayoutTraverserView) {
            return new EditState<>((LayoutTraverserView) v, TRAVERSER);
        }
        throw new IllegalArgumentException("no edit state for " + v.getClass().getName());
    }

    /**
     * Record the state of a shape.
     *
     * @param s the shape
     * @return its state
     */
    @Nonnull
    static EditState<LayoutShape> of(@Nonnull LayoutShape s) {
        return new EditState<>(s, SHAPE);
    }

    /**
     * A connection: the track at the other end and where on it.
     */
    static final class Connection {

        final LayoutTrack track;
        final HitPointType type;

        Connection(@CheckForNull LayoutTrack track, @CheckForNull HitPointType type) {
            this.track = track;
            this.type = type;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Connection)) {
                return false;
            }
            Connection c = (Connection) o;
            return c.track == track && c.type == type;
        }

        @Override
        public int hashCode() {
            return Objects.hash(System.identityHashCode(track), type);
        }
    }

    // ---- properties every track has ----

    private static <V extends LayoutTrackView> List<Property<V, ?>> common() {
        List<Property<V, ?>> l = new ArrayList<>();
        l.add(Property.ofBoolean(LayoutTrackView::isHidden, LayoutTrackView::setHidden));
        l.add(Property.copied(LayoutTrackView::getDecorations,
                (v, d) -> v.setDecorations((d == null) ? new java.util.HashMap<>() : new java.util.HashMap<>(d)),
                EditState::copyMap));
        return l;
    }

    private static <V> List<Property<V, ?>> join(List<Property<V, ?>> a, List<Property<V, ?>> b) {
        List<Property<V, ?>> l = new ArrayList<>(a);
        l.addAll(b);
        return Collections.unmodifiableList(l);
    }

    // ---- turnouts (and the turnout part of slips) ----

    private static NamedBeanHandle<LayoutBlock> rawBlockB(LayoutTurnoutView v) {
        return v.getLayoutTurnout().namedLayoutBlockB;
    }

    private static NamedBeanHandle<LayoutBlock> rawBlockC(LayoutTurnoutView v) {
        return v.getLayoutTurnout().namedLayoutBlockC;
    }

    private static NamedBeanHandle<LayoutBlock> rawBlockD(LayoutTurnoutView v) {
        return v.getLayoutTurnout().namedLayoutBlockD;
    }

    private static <V extends LayoutTurnoutView> List<Property<V, ?>> turnout() {
        List<Property<V, ?>> l = common();
        // geometry: the view's own fields, so a restore is exact
        l.add(Property.point(LayoutTrackView::getCoordsCenter, LayoutTrackView::setCoordsCenter));
        l.add(Property.point(v -> v.dispA, (v, p) -> v.dispA = p));
        l.add(Property.point(v -> v.dispB, (v, p) -> v.dispB = p));
        l.add(Property.point(v -> v.pointA, (v, p) -> v.pointA = p));
        l.add(Property.point(v -> v.pointB, (v, p) -> v.pointB = p));
        l.add(Property.point(v -> v.pointC, (v, p) -> v.pointC = p));
        l.add(Property.point(v -> v.pointD, (v, p) -> v.pointD = p));
        l.add(Property.ofInt(LayoutTurnoutView::getVersion, LayoutTurnoutView::setVersion));
        l.add(Property.ofBoolean(v -> v.showUnknown, LayoutTurnoutView::setShowUnknown));
        // connections
        l.add(Property.of(v -> v.getLayoutTurnout().getConnectA(),
                (v, t) -> v.getLayoutTurnout().setConnectA(t, HitPointType.TRACK)));
        l.add(Property.of(v -> v.getLayoutTurnout().getConnectB(),
                (v, t) -> v.getLayoutTurnout().setConnectB(t, HitPointType.TRACK)));
        l.add(Property.of(v -> v.getLayoutTurnout().getConnectC(),
                (v, t) -> v.getLayoutTurnout().setConnectC(t, HitPointType.TRACK)));
        l.add(Property.of(v -> v.getLayoutTurnout().getConnectD(),
                (v, t) -> v.getLayoutTurnout().setConnectD(t, HitPointType.TRACK)));
        // Blocks: the handles themselves; use counts are set by the model
        l.add(Property.of(v -> v.getLayoutTurnout().namedLayoutBlockA,
                (v, h) -> v.getLayoutTurnout().namedLayoutBlockA = h));
        l.add(Property.of(LayoutEditorEditStates::rawBlockB, (v, h) -> v.getLayoutTurnout().namedLayoutBlockB = h));
        l.add(Property.of(LayoutEditorEditStates::rawBlockC, (v, h) -> v.getLayoutTurnout().namedLayoutBlockC = h));
        l.add(Property.of(LayoutEditorEditStates::rawBlockD, (v, h) -> v.getLayoutTurnout().namedLayoutBlockD = h));
        // turnouts: through the setters, which move the turnout listener
        l.add(Property.of(v -> v.getLayoutTurnout().getTurnoutName(), (v, n) -> v.getLayoutTurnout().setTurnout(n)));
        l.add(Property.of(v -> v.getLayoutTurnout().getSecondTurnoutName(),
                (v, n) -> v.getLayoutTurnout().setSecondTurnout(n)));
        l.add(Property.ofBoolean(v -> v.getLayoutTurnout().isSecondTurnoutInverted(),
                (v, b) -> v.getLayoutTurnout().setSecondTurnoutInverted(b)));
        l.add(Property.ofInt(v -> v.getLayoutTurnout().getContinuingSense(),
                (v, s) -> v.getLayoutTurnout().setContinuingSense(s)));
        l.add(Property.ofBoolean(v -> v.getLayoutTurnout().isDisabled(), (v, b) -> v.getLayoutTurnout().setDisabled(b)));
        l.add(Property.ofBoolean(v -> v.getLayoutTurnout().isDisabledWhenOccupied(),
                (v, b) -> v.getLayoutTurnout().setDisableWhenOccupied(b)));
        l.add(Property.of(v -> v.getLayoutTurnout().getLinkedTurnoutName(),
                (v, n) -> v.getLayoutTurnout().setLinkedTurnoutName(n)));
        l.add(Property.of(v -> v.getLayoutTurnout().getLinkType(), (v, t) -> v.getLayoutTurnout().setLinkType(t)));
        // signals and sensors placed on the turnout
        l.add(Property.of(LayoutTurnoutView::getSignalA1Name, LayoutTurnoutView::setSignalA1Name));
        l.add(Property.of(LayoutTurnoutView::getSignalA2Name, LayoutTurnoutView::setSignalA2Name));
        l.add(Property.of(LayoutTurnoutView::getSignalA3Name, LayoutTurnoutView::setSignalA3Name));
        l.add(Property.of(LayoutTurnoutView::getSignalB1Name, LayoutTurnoutView::setSignalB1Name));
        l.add(Property.of(LayoutTurnoutView::getSignalB2Name, LayoutTurnoutView::setSignalB2Name));
        l.add(Property.of(LayoutTurnoutView::getSignalC1Name, LayoutTurnoutView::setSignalC1Name));
        l.add(Property.of(LayoutTurnoutView::getSignalC2Name, LayoutTurnoutView::setSignalC2Name));
        l.add(Property.of(LayoutTurnoutView::getSignalD1Name, LayoutTurnoutView::setSignalD1Name));
        l.add(Property.of(LayoutTurnoutView::getSignalD2Name, LayoutTurnoutView::setSignalD2Name));
        l.add(Property.of(v -> v.getLayoutTurnout().getSignalAMastName(), (v, n) -> v.getLayoutTurnout().setSignalAMast(n)));
        l.add(Property.of(v -> v.getLayoutTurnout().getSignalBMastName(), (v, n) -> v.getLayoutTurnout().setSignalBMast(n)));
        l.add(Property.of(v -> v.getLayoutTurnout().getSignalCMastName(), (v, n) -> v.getLayoutTurnout().setSignalCMast(n)));
        l.add(Property.of(v -> v.getLayoutTurnout().getSignalDMastName(), (v, n) -> v.getLayoutTurnout().setSignalDMast(n)));
        l.add(Property.of(v -> v.getLayoutTurnout().getSensorAName(), (v, n) -> v.getLayoutTurnout().setSensorA(n)));
        l.add(Property.of(v -> v.getLayoutTurnout().getSensorBName(), (v, n) -> v.getLayoutTurnout().setSensorB(n)));
        l.add(Property.of(v -> v.getLayoutTurnout().getSensorCName(), (v, n) -> v.getLayoutTurnout().setSensorC(n)));
        l.add(Property.of(v -> v.getLayoutTurnout().getSensorDName(), (v, n) -> v.getLayoutTurnout().setSensorD(n)));
        return l;
    }

    private static final List<Property<LayoutTurnoutView, ?>> TURNOUT = Collections.unmodifiableList(turnout());

    // ---- slips ----

    // the turnout states for each slip state, as key, A state, B state, ...
    private static List<Integer> getSlipStates(LayoutSlipView v) {
        List<Integer> l = new ArrayList<>();
        for (Map.Entry<Integer, LayoutSlip.TurnoutState> e : v.getSlip().getTurnoutStates().entrySet()) {
            l.add(e.getKey());
            l.add(Integer.valueOf(e.getValue().getTurnoutAState()));
            l.add(Integer.valueOf(e.getValue().getTurnoutBState()));
        }
        return Collections.unmodifiableList(l);
    }

    private static void setSlipStates(LayoutSlipView v, List<Integer> l) {
        for (int i = 0; i + 2 < l.size(); i += 3) {
            v.getSlip().setTurnoutStates(l.get(i).intValue(), l.get(i + 1).toString(), l.get(i + 2).toString());
        }
    }

    private static final List<Property<LayoutSlipView, ?>> SLIP = join(LayoutEditorEditStates.<LayoutSlipView>turnout(),
            List.of(
                    Property.of(v -> v.getSlip().getTurnoutBName(), (v, n) -> v.getSlip().setTurnoutB(n)),
                    Property.of(LayoutEditorEditStates::getSlipStates, LayoutEditorEditStates::setSlipStates)));

    // ---- track segments ----

    // arc, circle, bezier together, as their setters change each other
    private static List<Boolean> getCurve(TrackSegmentView v) {
        return List.of(Boolean.valueOf(v.isArc()), Boolean.valueOf(v.isCircle()), Boolean.valueOf(v.isBezier()));
    }

    private static void setCurve(TrackSegmentView v, List<Boolean> c) {
        v.setBezier(false);
        v.setCircle(false);
        v.setArc(false);
        if (c.get(2).booleanValue()) {
            v.setBezier(true);
        }
        if (c.get(0).booleanValue()) {
            v.setArc(true);
        }
        if (c.get(1).booleanValue()) {
            v.setCircle(true);
        }
    }

    private static void setControlPoints(TrackSegmentView v, List<Point2D> points) {
        List<Point2D> l = v.getBezierControlPoints();
        l.clear();
        for (Point2D p : points) {
            l.add(EditState.copy(p));
        }
    }

    private static final List<Property<TrackSegmentView, ?>> TRACK_SEGMENT = join(
            LayoutEditorEditStates.<TrackSegmentView>common(),
            List.of(
                    Property.of(v -> new Connection(v.getConnect1(), v.getType1()),
                            (v, c) -> v.getTrackSegment().setConnect1(c.track, c.type)),
                    Property.of(v -> new Connection(v.getConnect2(), v.getType2()),
                            (v, c) -> v.getTrackSegment().setConnect2(c.track, c.type)),
                    Property.of(v -> v.getTrackSegment().getLayoutBlock(),
                            (v, b) -> v.getTrackSegment().setLayoutBlock(b)),
                    Property.ofBoolean(v -> v.getTrackSegment().isMainline(), (v, b) -> v.getTrackSegment().setMainline(b)),
                    Property.ofBoolean(TrackSegmentView::isDashed, TrackSegmentView::setDashed),
                    Property.of(LayoutEditorEditStates::getCurve, LayoutEditorEditStates::setCurve),
                    Property.ofDouble(TrackSegmentView::getAngle, TrackSegmentView::setAngle),
                    Property.ofBoolean(TrackSegmentView::isFlip, TrackSegmentView::setFlip),
                    Property.copied(TrackSegmentView::getBezierControlPoints,
                            (v, l) -> setControlPoints(v, l), l -> new ArrayList<>(EditState.copyPoints(l))),
                    Property.ofInt(TrackSegmentView::getArrowStyle, TrackSegmentView::setArrowStyle),
                    Property.ofBoolean(TrackSegmentView::isArrowEndStart, TrackSegmentView::setArrowEndStart),
                    Property.ofBoolean(TrackSegmentView::isArrowEndStop, TrackSegmentView::setArrowEndStop),
                    Property.ofBoolean(TrackSegmentView::isArrowDirIn, TrackSegmentView::setArrowDirIn),
                    Property.ofBoolean(TrackSegmentView::isArrowDirOut, TrackSegmentView::setArrowDirOut),
                    Property.of(TrackSegmentView::getArrowColor, TrackSegmentView::setArrowColor),
                    Property.ofInt(TrackSegmentView::getArrowLineWidth, TrackSegmentView::setArrowLineWidth),
                    Property.ofInt(TrackSegmentView::getArrowLength, TrackSegmentView::setArrowLength),
                    Property.ofInt(TrackSegmentView::getArrowGap, TrackSegmentView::setArrowGap),
                    Property.ofBoolean(TrackSegmentView::isBridgeSideRight, TrackSegmentView::setBridgeSideRight),
                    Property.ofBoolean(TrackSegmentView::isBridgeSideLeft, TrackSegmentView::setBridgeSideLeft),
                    Property.ofBoolean(TrackSegmentView::isBridgeHasEntry, TrackSegmentView::setBridgeHasEntry),
                    Property.ofBoolean(TrackSegmentView::isBridgeHasExit, TrackSegmentView::setBridgeHasExit),
                    Property.of(TrackSegmentView::getBridgeColor, TrackSegmentView::setBridgeColor),
                    Property.ofInt(TrackSegmentView::getBridgeDeckWidth, TrackSegmentView::setBridgeDeckWidth),
                    Property.ofInt(TrackSegmentView::getBridgeLineWidth, TrackSegmentView::setBridgeLineWidth),
                    Property.ofInt(TrackSegmentView::getBridgeApproachWidth, TrackSegmentView::setBridgeApproachWidth),
                    Property.ofBoolean(TrackSegmentView::isBumperEndStart, TrackSegmentView::setBumperEndStart),
                    Property.ofBoolean(TrackSegmentView::isBumperEndStop, TrackSegmentView::setBumperEndStop),
                    Property.of(TrackSegmentView::getBumperColor, TrackSegmentView::setBumperColor),
                    Property.ofInt(TrackSegmentView::getBumperLineWidth, TrackSegmentView::setBumperLineWidth),
                    Property.ofInt(TrackSegmentView::getBumperLength, TrackSegmentView::setBumperLength),
                    Property.ofBoolean(TrackSegmentView::isBumperFlipped, TrackSegmentView::setBumperFlipped),
                    Property.ofBoolean(TrackSegmentView::isTunnelSideRight, TrackSegmentView::setTunnelSideRight),
                    Property.ofBoolean(TrackSegmentView::isTunnelSideLeft, TrackSegmentView::setTunnelSideLeft),
                    Property.ofBoolean(TrackSegmentView::isTunnelHasEntry, TrackSegmentView::setTunnelHasEntry),
                    Property.ofBoolean(TrackSegmentView::isTunnelHasExit, TrackSegmentView::setTunnelHasExit),
                    Property.of(TrackSegmentView::getTunnelColor, TrackSegmentView::setTunnelColor),
                    Property.ofInt(TrackSegmentView::getTunnelFloorWidth, TrackSegmentView::setTunnelFloorWidth),
                    Property.ofInt(TrackSegmentView::getTunnelLineWidth, TrackSegmentView::setTunnelLineWidth),
                    Property.ofInt(TrackSegmentView::getTunnelEntranceWidth, TrackSegmentView::setTunnelEntranceWidth)));

    // ---- anchors, end bumpers and edge connectors ----

    private static final List<Property<PositionablePointView, ?>> POINT = join(
            LayoutEditorEditStates.<PositionablePointView>common(),
            List.of(
                    Property.point(LayoutTrackView::getCoordsCenter, LayoutTrackView::setCoordsCenter),
                    Property.of(v -> v.getPoint().getType(), (v, t) -> v.getPoint().setType(t)),
                    Property.of(v -> v.getPoint().getConnect1(), (v, t) -> v.getPoint().setConnect1(t)),
                    Property.of(v -> v.getPoint().getConnect2Actual(), (v, t) -> v.getPoint().setConnect2Actual(t)),
                    Property.of(v -> v.getPoint().getLinkedPoint(), (v, p) -> v.getPoint().setLinkedPoint(p)),
                    Property.of(v -> v.getPoint().getEastBoundSignal(), (v, n) -> v.getPoint().setEastBoundSignal(n)),
                    Property.of(v -> v.getPoint().getWestBoundSignal(), (v, n) -> v.getPoint().setWestBoundSignal(n)),
                    Property.of(v -> v.getPoint().getEastBoundSensorName(),
                            (v, n) -> v.getPoint().setEastBoundSensor(n)),
                    Property.of(v -> v.getPoint().getWestBoundSensorName(),
                            (v, n) -> v.getPoint().setWestBoundSensor(n)),
                    Property.of(v -> v.getPoint().getEastBoundSignalMastName(),
                            (v, n) -> v.getPoint().setEastBoundSignalMast(n)),
                    Property.of(v -> v.getPoint().getWestBoundSignalMastName(),
                            (v, n) -> v.getPoint().setWestBoundSignalMast(n))));

    // ---- level crossings ----

    private static final List<Property<LevelXingView, ?>> LEVEL_XING = join(
            LayoutEditorEditStates.<LevelXingView>common(),
            List.of(
                    Property.point(LayoutTrackView::getCoordsCenter, LayoutTrackView::setCoordsCenter),
                    Property.point(LevelXingView::getCoordsA, LevelXingView::setCoordsA),
                    Property.point(LevelXingView::getCoordsB, LevelXingView::setCoordsB),
                    Property.of(v -> v.getLevelXing().getConnectA(),
                            (v, t) -> v.getLevelXing().setConnectA(t, HitPointType.TRACK)),
                    Property.of(v -> v.getLevelXing().getConnectB(),
                            (v, t) -> v.getLevelXing().setConnectB(t, HitPointType.TRACK)),
                    Property.of(v -> v.getLevelXing().getConnectC(),
                            (v, t) -> v.getLevelXing().setConnectC(t, HitPointType.TRACK)),
                    Property.of(v -> v.getLevelXing().getConnectD(),
                            (v, t) -> v.getLevelXing().setConnectD(t, HitPointType.TRACK)),
                    Property.of(v -> v.getLevelXing().getLayoutBlockAC(),
                            (v, b) -> v.getLevelXing().setLayoutBlockAC(b)),
                    Property.of(v -> v.getLevelXing().getLayoutBlockBD(),
                            (v, b) -> v.getLevelXing().setLayoutBlockBD(b)),
                    Property.of(v -> v.getLevelXing().getSignalAName(), (v, n) -> v.getLevelXing().setSignalAName(n)),
                    Property.of(v -> v.getLevelXing().getSignalBName(), (v, n) -> v.getLevelXing().setSignalBName(n)),
                    Property.of(v -> v.getLevelXing().getSignalCName(), (v, n) -> v.getLevelXing().setSignalCName(n)),
                    Property.of(v -> v.getLevelXing().getSignalDName(), (v, n) -> v.getLevelXing().setSignalDName(n)),
                    Property.of(v -> v.getLevelXing().getSignalAMastName(),
                            (v, n) -> v.getLevelXing().setSignalAMast(n)),
                    Property.of(v -> v.getLevelXing().getSignalBMastName(),
                            (v, n) -> v.getLevelXing().setSignalBMast(n)),
                    Property.of(v -> v.getLevelXing().getSignalCMastName(),
                            (v, n) -> v.getLevelXing().setSignalCMast(n)),
                    Property.of(v -> v.getLevelXing().getSignalDMastName(),
                            (v, n) -> v.getLevelXing().setSignalDMast(n)),
                    Property.of(v -> v.getLevelXing().getSensorAName(), (v, n) -> v.getLevelXing().setSensorAName(n)),
                    Property.of(v -> v.getLevelXing().getSensorBName(), (v, n) -> v.getLevelXing().setSensorBName(n)),
                    Property.of(v -> v.getLevelXing().getSensorCName(), (v, n) -> v.getLevelXing().setSensorCName(n)),
                    Property.of(v -> v.getLevelXing().getSensorDName(),
                            (v, n) -> v.getLevelXing().setSensorDName(n))));

    // ---- turntables ----

    /**
     * One ray of a turntable, or one slot of a traverser: the object and its
     * settings.
     */
    static final class Exit {

        final Object track;
        final double position;
        final TrackSegment connect;
        final String turnoutName;
        final int turnoutState;
        final boolean disabled;
        final boolean disabledWhenOccupied;
        final String approachMast;

        Exit(Object track, double position, TrackSegment connect, String turnoutName, int turnoutState,
                boolean disabled, boolean disabledWhenOccupied, String approachMast) {
            this.track = track;
            this.position = position;
            this.connect = connect;
            this.turnoutName = turnoutName;
            this.turnoutState = turnoutState;
            this.disabled = disabled;
            this.disabledWhenOccupied = disabledWhenOccupied;
            this.approachMast = approachMast;
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof Exit)) {
                return false;
            }
            Exit e = (Exit) o;
            return e.track == track && Math.abs(e.position - position) <= 1.0E-6 && e.connect == connect
                    && Objects.equals(e.turnoutName, turnoutName) && e.turnoutState == turnoutState
                    && e.disabled == disabled && e.disabledWhenOccupied == disabledWhenOccupied
                    && Objects.equals(e.approachMast, approachMast);
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(track);
        }
    }

    private static List<Exit> getRays(LayoutTurntableView v) {
        List<Exit> l = new ArrayList<>();
        for (LayoutTurntable.RayTrack r : v.getTurntable().getRayTrackList()) {
            l.add(new Exit(r, r.getAngle(), r.getConnect(), r.getTurnoutName(), r.getTurnoutState(),
                    r.isDisabled(), r.isDisabledWhenOccupied(), r.getApproachMastName()));
        }
        return Collections.unmodifiableList(l);
    }

    private static void setRays(LayoutTurntableView v, List<Exit> rays) {
        List<LayoutTurntable.RayTrack> list = v.getTurntable().rayTrackList;
        list.clear();
        for (Exit e : rays) {
            LayoutTurntable.RayTrack r = (LayoutTurntable.RayTrack) e.track;
            list.add(r);
            r.setAngle(e.position);
            r.setConnect(e.connect);
            r.setTurnout((e.turnoutName == null) ? "" : e.turnoutName, e.turnoutState);
            r.setDisabled(e.disabled);
            r.setDisabledWhenOccupied(e.disabledWhenOccupied);
            r.setApproachMast((e.approachMast == null) ? "" : e.approachMast);
        }
    }

    private static final List<Property<LayoutTurntableView, ?>> TURNTABLE = join(
            LayoutEditorEditStates.<LayoutTurntableView>common(),
            List.of(
                    Property.point(LayoutTrackView::getCoordsCenter, LayoutTrackView::setCoordsCenter),
                    Property.ofDouble(v -> v.getTurntable().getRadius(), (v, r) -> v.getTurntable().setRadius(r)),
                    Property.of(v -> v.getTurntable().getLayoutBlock(), (v, b) -> v.getTurntable().setLayoutBlock(b)),
                    Property.ofBoolean(v -> v.getTurntable().isMainline(), (v, b) -> v.getTurntable().setMainline(b)),
                    Property.ofBoolean(v -> v.getTurntable().isTurnoutControlled(),
                            (v, b) -> v.getTurntable().setTurnoutControlled(b)),
                    Property.ofInt(v -> v.getTurntable().getSignalIconPlacement(),
                            (v, p) -> v.getTurntable().setSignalIconPlacement(p)),
                    Property.of(v -> v.getTurntable().getBufferSignalMastName(),
                            (v, n) -> v.getTurntable().setBufferSignalMast(n)),
                    Property.of(v -> v.getTurntable().getExitSignalMastName(),
                            (v, n) -> v.getTurntable().setExitSignalMast(n)),
                    Property.of(LayoutEditorEditStates::getRays, LayoutEditorEditStates::setRays)));

    // ---- traversers ----

    private static List<Exit> getSlots(LayoutTraverserView v) {
        List<Exit> l = new ArrayList<>();
        for (LayoutTraverser.SlotTrack s : v.getTraverser().getSlotList()) {
            l.add(new Exit(s, s.getOffset(), s.getConnect(), s.getTurnoutName(), s.getTurnoutState(),
                    s.isDisabled(), s.isDisabledWhenOccupied(), s.getApproachMastName()));
        }
        return Collections.unmodifiableList(l);
    }

    private static void setSlots(LayoutTraverserView v, List<Exit> slots) {
        List<LayoutTraverser.SlotTrack> list = v.getTraverser().slotList;
        list.clear();
        for (Exit e : slots) {
            LayoutTraverser.SlotTrack s = (LayoutTraverser.SlotTrack) e.track;
            list.add(s);
            s.setOffset(e.position);
            s.setConnect(e.connect);
            s.setTurnout((e.turnoutName == null) ? "" : e.turnoutName, e.turnoutState);
            s.setDisabled(e.disabled);
            s.setDisabledWhenOccupied(e.disabledWhenOccupied);
            s.setApproachMast((e.approachMast == null) ? "" : e.approachMast);
        }
    }

    private static final List<Property<LayoutTraverserView, ?>> TRAVERSER = join(
            LayoutEditorEditStates.<LayoutTraverserView>common(),
            List.of(
                    Property.point(LayoutTrackView::getCoordsCenter, LayoutTrackView::setCoordsCenter),
                    Property.ofInt(v -> v.getTraverser().getOrientation(),
                            (v, o) -> v.getTraverser().setOrientation(o)),
                    Property.ofDouble(v -> v.getTraverser().getSlotOffset(), (v, o) -> v.getTraverser().setSlotOffset(o)),
                    Property.ofDouble(v -> v.getTraverser().getDeckWidth(), (v, w) -> v.getTraverser().setDeckWidth(w)),
                    Property.of(v -> v.getTraverser().getLayoutBlock(), (v, b) -> v.getTraverser().setLayoutBlock(b)),
                    Property.ofBoolean(v -> v.getTraverser().isMainline(), (v, b) -> v.getTraverser().setMainline(b)),
                    Property.ofBoolean(v -> v.getTraverser().isTurnoutControlled(),
                            (v, b) -> v.getTraverser().setTurnoutControlled(b)),
                    Property.ofInt(v -> v.getTraverser().getSignalIconPlacement(),
                            (v, p) -> v.getTraverser().setSignalIconPlacement(p)),
                    Property.ofInt(v -> v.getTraverser().getBridgeSignalIconPlacement(),
                            (v, p) -> v.getTraverser().setBridgeSignalIconPlacement(p)),
                    Property.of(v -> v.getTraverser().getBufferSignalMastName(),
                            (v, n) -> v.getTraverser().setBufferSignalMast(n)),
                    Property.of(v -> v.getTraverser().getExitSignalMastName(),
                            (v, n) -> v.getTraverser().setExitSignalMast(n)),
                    Property.of(LayoutEditorEditStates::getSlots, LayoutEditorEditStates::setSlots)));

    // ---- shapes ----

    /**
     * One point of a shape: the object, its kind and where it is.
     */
    static final class ShapePoint {

        final LayoutShape.LayoutShapePoint point;
        final LayoutShape.LayoutShapePointType type;
        final Point2D location;

        ShapePoint(LayoutShape.LayoutShapePoint point) {
            this.point = point;
            this.type = point.getType();
            this.location = EditState.copy(point.getPoint());
        }

        @Override
        public boolean equals(Object o) {
            if (!(o instanceof ShapePoint)) {
                return false;
            }
            ShapePoint s = (ShapePoint) o;
            return s.point == point && s.type == type
                    && Math.abs(s.location.getX() - location.getX()) <= 1.0E-6
                    && Math.abs(s.location.getY() - location.getY()) <= 1.0E-6;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(point);
        }
    }

    private static List<ShapePoint> getShapePoints(LayoutShape s) {
        List<ShapePoint> l = new ArrayList<>();
        for (LayoutShape.LayoutShapePoint p : s.getPoints()) {
            l.add(new ShapePoint(p));
        }
        return Collections.unmodifiableList(l);
    }

    private static void setShapePoints(LayoutShape s, List<ShapePoint> points) {
        List<LayoutShape.LayoutShapePoint> list = s.getPoints();
        list.clear();
        for (ShapePoint p : points) {
            list.add(p.point);
            p.point.setType(p.type);
            p.point.setPoint(EditState.copy(p.location));
        }
    }

    private static final List<Property<LayoutShape, ?>> SHAPE = List.of(
            Property.of(LayoutShape::getName, LayoutShape::setName),
            Property.of(LayoutShape::getType, LayoutShape::setType),
            Property.ofInt(LayoutShape::getLevel, LayoutShape::setLevel),
            Property.ofInt(LayoutShape::getLineWidth, LayoutShape::setLineWidth),
            Property.of(LayoutShape::getLineColor, LayoutShape::setLineColor),
            Property.of(LayoutShape::getFillColor, LayoutShape::setFillColor),
            Property.ofBoolean(LayoutShape::isHidden, LayoutShape::setHidden),
            Property.of(LayoutEditorEditStates::getShapePoints, LayoutEditorEditStates::setShapePoints));
}
