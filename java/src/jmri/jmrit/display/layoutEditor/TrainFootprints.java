package jmri.jmrit.display.layoutEditor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;

import jmri.Block;
import jmri.InstanceManager;
import jmri.TrainRecord;
import jmri.TrainRecordManager;
import jmri.UserPreferencesManager;
import jmri.util.ThreadingUtil;

/**
 * Draws on a Layout Editor panel each train that train tracking knows, along
 * the track of the Blocks it is in: in a colour of its own, solid where JMRI
 * is sure of it and dashed, and fainter, where it is not, with an arrowhead
 * at its front and its name. When JMRI doesn't know which end is the front,
 * or which way the train faces, a "?" is shown instead of the arrowhead.
 * <p>
 * Off unless chosen; kept for each panel in the user's preferences.
 * <p>
 * Used on the layout thread (Swing thread), where train records are kept.
 *
 * @author Keith Bruce Copyright (C) 2026
 * @since 5.19.2
 */
class TrainFootprints {

    private static final Color[] COLOURS = {
        new Color(0, 90, 200), // blue
        new Color(130, 0, 170), // purple
        new Color(0, 140, 140), // teal
        new Color(150, 90, 0), // brown
        new Color(200, 0, 140), // magenta
        new Color(0, 60, 110), // navy
        new Color(90, 90, 90), // grey
        new Color(100, 70, 200), // violet
    };

    private final LayoutEditor editor;
    private final PropertyChangeListener listener = this::changed;
    private TrainRecordManager listeningTo = null;
    private final Set<TrainRecord> recordsListenedTo = new HashSet<>();
    private boolean redrawPending = false;

    TrainFootprints(@Nonnull LayoutEditor editor) {
        this.editor = editor;
    }

    private String getRef() {
        return "ShowTrainFootprints__" + editor.getWindowFrameRef();
    }

    /**
     * @param value true to draw the trains train tracking knows
     */
    void setShown(boolean value) {
        InstanceManager.getOptionalDefault(UserPreferencesManager.class)
                .ifPresent(p -> p.setCheckboxPreferenceState(getRef(), value));
        editor.redrawPanel();
    }

    /**
     * @return true if the trains train tracking knows are drawn
     */
    boolean isShown() {
        return InstanceManager.getOptionalDefault(UserPreferencesManager.class)
                .map(p -> p.getCheckboxPreferenceState(getRef(), false)).orElse(false);
    }

    /**
     * Stop listening to train tracking.
     */
    void dispose() {
        listen(null);
    }

    private void listen(@CheckForNull TrainRecordManager manager) {
        if (listeningTo != manager) {
            if (listeningTo != null) {
                listeningTo.removePropertyChangeListener(listener);
            }
            listeningTo = manager;
            if (manager != null) {
                manager.addPropertyChangeListener(listener);
            }
        }
        Set<TrainRecord> now = manager == null ? Set.of() : new HashSet<>(manager.getRecords());
        for (TrainRecord r : new ArrayList<>(recordsListenedTo)) {
            if (!now.contains(r)) {
                r.removePropertyChangeListener(listener);
                recordsListenedTo.remove(r);
            }
        }
        for (TrainRecord r : now) {
            if (recordsListenedTo.add(r)) {
                r.addPropertyChangeListener(listener);
            }
        }
    }

    private void changed(PropertyChangeEvent e) {
        if (TrainRecord.PROPERTY_COMMAND.equals(e.getPropertyName())) {
            return; // a speed command alone changes nothing drawn
        }
        if (!redrawPending) {
            redrawPending = true;
            ThreadingUtil.runOnGUIEventually(() -> {
                redrawPending = false;
                editor.redrawPanel();
            });
        }
    }

    /**
     * Draw the trains, if chosen.
     *
     * @param g2 where to draw
     */
    void paint(@Nonnull Graphics2D g2) {
        if (!isShown()) {
            listen(null);
            return;
        }
        TrainRecordManager manager = InstanceManager.getNullableDefault(TrainRecordManager.class);
        listen(manager);
        if (manager == null) {
            return;
        }
        List<TrainRecord> records = manager.getRecords();
        if (records.isEmpty()) {
            return;
        }
        Track track = new Track(editor);
        Stroke oldStroke = g2.getStroke();
        Color oldColor = g2.getColor();
        Font oldFont = g2.getFont();
        int i = 0;
        for (TrainRecord r : records) {
            if (r.isPlaced() && !r.getBlocks().isEmpty()) {
                paint(g2, r, COLOURS[i % COLOURS.length], track);
            }
            i++;
        }
        g2.setStroke(oldStroke);
        g2.setColor(oldColor);
        g2.setFont(oldFont);
    }

    private void paint(Graphics2D g2, TrainRecord r, Color colour, Track track) {
        List<Block> blocks = r.getBlocks();
        Set<Block> footprint = new HashSet<>(blocks);
        TrainRecord.Confidence confidence = r.getConfidence();
        boolean sure = confidence == TrainRecord.Confidence.CONFIRMED;
        float width = lineWidth();

        // the track it is in
        int alpha = sure ? 190 : confidence == TrainRecord.Confidence.LOST ? 70 : 130;
        g2.setColor(new Color(colour.getRed(), colour.getGreen(), colour.getBlue(), alpha));
        g2.setStroke(sure
                ? new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND)
                : new BasicStroke(width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10f,
                        new float[]{width * 1.5f, width}, 0f));
        boolean drawn = false;
        for (Leg leg : track.legs) {
            if (footprint.contains(leg.block)) {
                g2.draw(leg.shape);
                drawn = true;
            }
        }
        if (!drawn) {
            return; // not on this panel
        }

        // its front, and the way it faces
        Front front = r.isFrontKnown() ? track.front(blocks, behind(r, blocks)) : null;
        String name = r.getName();
        if (!sure && confidence != null) {
            name = Bundle.getMessage("TrainFootprintUnsure", name,
                    Bundle.getMessage("TrainFootprint" + confidence.name()));
        }
        g2.setColor(colour);
        g2.setFont(g2.getFont().deriveFont(Font.BOLD, 12f));
        if (front != null) {
            drawArrowHead(g2, front, width);
        } else {
            name = Bundle.getMessage("TrainFootprintDirectionUnknown", name);
        }
        // its name above the middle of its front Block's track
        Point2D at = track.middle(blocks.get(0));
        if (at == null) {
            return;
        }
        int textWidth = g2.getFontMetrics().stringWidth(name);
        g2.drawString(name, (float) (at.getX() - textWidth / 2.0), (float) (at.getY() - width));
    }

    private float lineWidth() {
        LayoutTrackDrawingOptions o = editor.getLayoutTrackDrawingOptions();
        return Math.max(o.getMainBlockLineWidth(), o.getSideBlockLineWidth()) + 6f;
    }

    /**
     * @return the Block behind the front Block: the next of the train's
     *         Blocks, else the one it entered its only Block from; null if
     *         not known
     */
    @CheckForNull
    private static Block behind(TrainRecord r, List<Block> blocks) {
        if (blocks.size() > 1) {
            return blocks.get(1);
        }
        List<TrainRecord.TrailEntry> trail = r.getTrail();
        for (int i = trail.size() - 1; i >= 0; i--) {
            if (trail.get(i).getBlock() == blocks.get(0)) {
                return trail.get(i).getFrom();
            }
        }
        return null;
    }

    private static void drawArrowHead(Graphics2D g2, Front front, float width) {
        double length = width * 2.5;
        double half = width * 1.3;
        double tipX = front.at.getX();
        double tipY = front.at.getY();
        double baseX = tipX - front.dx * length;
        double baseY = tipY - front.dy * length;
        Path2D.Double head = new Path2D.Double();
        head.moveTo(tipX, tipY);
        head.lineTo(baseX - front.dy * half, baseY + front.dx * half);
        head.lineTo(baseX + front.dy * half, baseY - front.dx * half);
        head.closePath();
        g2.fill(head);
    }

    /**
     * A piece of drawn track in one Block.
     */
    static final class Leg {

        final Shape shape;
        final Block block;
        final Point2D end1;
        final Point2D end2;

        Leg(Shape shape, Block block, Point2D end1, Point2D end2) {
            this.shape = shape;
            this.block = block;
            this.end1 = end1;
            this.end2 = end2;
        }
    }

    /**
     * Where a train's front is, and the way it faces there, as a unit
     * vector.
     */
    static final class Front {

        final Point2D at;
        final double dx;
        final double dy;

        Front(Point2D at, double dx, double dy) {
            this.at = at;
            this.dx = dx;
            this.dy = dy;
        }
    }

    /**
     * The panel's track, in pieces with the Block each is in.
     */
    static final class Track {

        final List<Leg> legs = new ArrayList<>();
        // the Blocks that meet at each end of a piece
        private final Map<Long, Set<Block>> blocksAt = new HashMap<>();

        Track(LayoutEditor editor) {
            for (LayoutTrackView v : editor.getLayoutTrackViews()) {
                if (v instanceof TrackSegmentView) {
                    TrackSegmentView ts = (TrackSegmentView) v;
                    add(ts.getCentreLine(), ts.getLayoutBlock(),
                            editor.getCoords(ts.getConnect1(), ts.getType1()),
                            editor.getCoords(ts.getConnect2(), ts.getType2()));
                } else if (v instanceof LayoutTurnoutView) {
                    LayoutTurnoutView t = (LayoutTurnoutView) v;
                    Point2D c = t.getCoordsCenter();
                    leg(c, t.getCoordsA(), t.getLayoutBlock());
                    leg(c, t.getCoordsB(), t.getLayoutBlockB());
                    leg(c, t.getCoordsC(), t.getLayoutBlockC());
                    if (t.hasEnteringDoubleTrack() || t.isTurnoutTypeSlip()) {
                        leg(c, t.getCoordsD(), t.getLayoutBlockD());
                    }
                } else if (v instanceof LevelXingView) {
                    LevelXingView x = (LevelXingView) v;
                    add(new Line2D.Double(x.getCoordsA(), x.getCoordsC()), x.getLayoutBlockAC(),
                            x.getCoordsA(), x.getCoordsC());
                    add(new Line2D.Double(x.getCoordsB(), x.getCoordsD()), x.getLayoutBlockBD(),
                            x.getCoordsB(), x.getCoordsD());
                }
            }
        }

        private void leg(Point2D from, Point2D to, LayoutBlock lb) {
            add(new Line2D.Double(from, to), lb, from, to);
        }

        private void add(Shape shape, @CheckForNull LayoutBlock lb, Point2D end1, Point2D end2) {
            Block block = lb == null ? null : lb.getBlock();
            if (block == null) {
                return;
            }
            legs.add(new Leg(shape, block, end1, end2));
            blocksAt.computeIfAbsent(key(end1), k -> new HashSet<>()).add(block);
            blocksAt.computeIfAbsent(key(end2), k -> new HashSet<>()).add(block);
        }

        // ends of pieces that meet are at the same place: to a tenth of a pixel
        private static long key(Point2D p) {
            return (Math.round(p.getX() * 10) << 32) ^ (Math.round(p.getY() * 10) & 0xffffffffL);
        }

        private Set<Block> blocksAt(Point2D p) {
            return blocksAt.getOrDefault(key(p), Set.of());
        }

        /**
         * @param block a Block
         * @return the middle of its drawn track, or null if it has none
         */
        @CheckForNull
        Point2D middle(Block block) {
            double x = 0;
            double y = 0;
            int n = 0;
            for (Leg leg : legs) {
                if (leg.block == block) {
                    x += leg.end1.getX() + leg.end2.getX();
                    y += leg.end1.getY() + leg.end2.getY();
                    n += 2;
                }
            }
            return n == 0 ? null : new Point2D.Double(x / n, y / n);
        }

        /**
         * Find a train's front: where its front Block meets a Block it is not
         * in, the farthest such place from the Block behind; or, at the end of
         * the track, the farthest end of the front Block.
         *
         * @param blocks the train's Blocks, front first
         * @param behind the Block behind the front Block, or null if not
         *               known
         * @return its front, or null if not known
         */
        @CheckForNull
        Front front(List<Block> blocks, @CheckForNull Block behind) {
            if (behind == null) {
                return null;
            }
            Block first = blocks.get(0);
            Point2D from = null;
            List<Point2D> ends = new ArrayList<>();
            for (Leg leg : legs) {
                if (leg.block == first) {
                    for (Point2D p : new Point2D[]{leg.end1, leg.end2}) {
                        ends.add(p);
                        if (from == null && blocksAt(p).contains(behind)) {
                            from = p;
                        }
                    }
                }
            }
            if (from == null) {
                from = middle(behind); // not next to it, as across a Block on another panel
                if (from == null) {
                    return null;
                }
            }
            Point2D best = null;
            Point2D bestAnyEnd = null;
            for (Point2D p : ends) {
                Set<Block> there = blocksAt(p);
                boolean leadsOut = there.size() > 1 && there.stream().noneMatch(b -> b != first && blocks.contains(b));
                if (leadsOut && p.distance(from) >= 0.5 && (best == null || p.distance(from) > best.distance(from))) {
                    best = p;
                }
                if (bestAnyEnd == null || p.distance(from) > bestAnyEnd.distance(from)) {
                    bestAnyEnd = p;
                }
            }
            Point2D at = best != null ? best : bestAnyEnd;
            if (at == null || at.distance(from) < 0.5) {
                return null;
            }
            return facing(first, at);
        }

        // the way the track runs at the end of the front Block's piece that ends there
        private Front facing(Block first, Point2D at) {
            for (Leg leg : legs) {
                if (leg.block != first) {
                    continue;
                }
                boolean atEnd1 = key(leg.end1) == key(at);
                boolean atEnd2 = key(leg.end2) == key(at);
                if (!atEnd1 && !atEnd2) {
                    continue;
                }
                List<Point2D> points = flatten(leg.shape);
                if (points.size() < 2) {
                    continue;
                }
                Point2D near;
                Point2D before;
                if (points.get(0).distance(at) <= points.get(points.size() - 1).distance(at)) {
                    near = points.get(0);
                    before = points.get(1);
                } else {
                    near = points.get(points.size() - 1);
                    before = points.get(points.size() - 2);
                }
                double dx = near.getX() - before.getX();
                double dy = near.getY() - before.getY();
                double length = Math.hypot(dx, dy);
                if (length > 0) {
                    return new Front(at, dx / length, dy / length);
                }
            }
            return null;
        }

        private static List<Point2D> flatten(Shape shape) {
            List<Point2D> points = new ArrayList<>();
            double[] c = new double[6];
            for (PathIterator i = shape.getPathIterator(null, 0.5); !i.isDone(); i.next()) {
                int type = i.currentSegment(c);
                if (type == PathIterator.SEG_MOVETO || type == PathIterator.SEG_LINETO) {
                    Point2D p = new Point2D.Double(c[0], c[1]);
                    if (points.isEmpty() || points.get(points.size() - 1).distance(p) > 1e-6) {
                        points.add(p);
                    }
                }
            }
            return points;
        }
    }

}
