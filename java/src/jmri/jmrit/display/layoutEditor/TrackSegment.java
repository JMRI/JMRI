package jmri.jmrit.display.layoutEditor;

import static java.lang.Float.POSITIVE_INFINITY;
import static jmri.jmrit.display.layoutEditor.LayoutTrack.TRACK;
import static jmri.jmrit.display.layoutEditor.LayoutTrack.defaultTrackColor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import jmri.Path;
import jmri.jmrit.display.layoutEditor.blockRoutingTable.LayoutBlockRouteTableAction;
import jmri.util.MathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TrackSegment is a segment of track on a layout linking two nodes of the
 * layout. A node may be a LayoutTurnout, a LevelXing or a PositionablePoint.
 * <P>
 * PositionablePoints have 1 or 2 connection points. LayoutTurnouts have 3 or 4
 * (crossovers) connection points, designated A, B, C, and D. LevelXing's have 4
 * connection points, designated A, B, C, and D.
 * <P>
 * TrackSegments carry the connectivity information between the three types of
 * nodes. Track Segments serve as the lines in a graph which shows layout
 * connectivity. For the connectivity graph to be valid, all connections between
 * nodes must be via TrackSegments.
 * <P>
 * TrackSegments carry Block information, as do LayoutTurnouts and LevelXings.
 * <P>
 * TrackSegments may be drawn as dashed lines or solid lines. In addition
 * TrackSegments may be hidden when the panel is not in EditMode.
 *
 * @author Dave Duchamp Copyright (p) 2004-2009
 * @author George Warner Copyright (c) 2017
 */
public class TrackSegment extends LayoutTrack {

    // defined constants
    // operational instance variables (not saved between sessions)
    private LayoutBlock layoutBlock = null;

    // persistent instances variables (saved between sessions)
    private String blockName = "";
    protected LayoutTrack connect1 = null;
    protected int type1 = 0;
    protected LayoutTrack connect2 = null;
    protected int type2 = 0;
    private boolean dashed = false;
    private boolean mainline = false;
    private boolean arc = false;
    private boolean flip = false;
    private double angle = 0.0D;
    private boolean circle = false;
    private boolean changed = false;
    private boolean bezier = false;

    // for Bezier
    private ArrayList<Point2D> bezierControlPoints = new ArrayList<>(); // list of control point displacements

    public TrackSegment(@Nonnull String id,
            @Nullable LayoutTrack c1, int t1,
            @Nullable LayoutTrack c2, int t2,
            boolean dash, boolean main,
            @Nonnull LayoutEditor layoutEditor) {
        super(id, MathUtil.zeroPoint2D, layoutEditor);

        // validate input
        if ((c1 == null) || (c2 == null)) {
            log.error("Invalid object in TrackSegment constructor call - " + id);
        }

        if (isConnectionHitType(t1)) {
            connect1 = c1;
            type1 = t1;
        } else {
            log.error("Invalid connect type 1 ('" + t1 + "') in TrackSegment constructor - " + id);
        }
        if (isConnectionHitType(t2)) {
            connect2 = c2;
            type2 = t2;
        } else {
            log.error("Invalid connect type 2 ('" + t2 + "') in TrackSegment constructor - " + id);
        }

        mainline = main;
        dashed = dash;

        arc = false;
        flip = false;
        angle = 0.0D;
        circle = false;
        bezier = false;
    }

    // alternate constructor for loading layout editor panels
    public TrackSegment(@Nonnull String id,
            @Nullable String c1Name, int t1,
            @Nullable String c2Name, int t2,
            boolean dash, boolean main, boolean hide,
            @Nonnull LayoutEditor layoutEditor) {
        super(id, MathUtil.zeroPoint2D, layoutEditor);

        tConnect1Name = c1Name;
        type1 = t1;
        tConnect2Name = c2Name;
        type2 = t2;

        mainline = main;
        dashed = dash;
        hidden = hide;
    }

    /**
     * Get debugging string for the TrackSegment.
     *
     * @return text showing id and connections of this segment
     */
    public String toString() {
        return "TrackSegment " + getName()
                + " c1:{" + getConnect1Name() + " (" + type1 + "},"
                + " c2:{" + getConnect2Name() + " (" + type2 + "}";

    }

    /*
     * Accessor methods
     */
    public String getBlockName() {
        return blockName;
    }

    public int getType1() {
        return type1;
    }

    public int getType2() {
        return type2;
    }

    public LayoutTrack getConnect1() {
        return connect1;
    }

    public LayoutTrack getConnect2() {
        return connect2;
    }

    /**
     * set a new connection 1
     *
     * @param connectTrack   - the track we want to connect to
     * @param connectionType - where on that track we want to be connected
     */
    protected void setNewConnect1(@Nullable LayoutTrack connectTrack, int connectionType) {
        connect1 = connectTrack;
        type1 = connectionType;
    }

    /**
     * set a new connection 2
     *
     * @param connectTrack   - the track we want to connect to
     * @param connectionType - where on that track we want to be connected
     */
    protected void setNewConnect2(@Nullable LayoutTrack connectTrack, int connectionType) {
        connect2 = connectTrack;
        type2 = connectionType;
    }

    /**
     * replace old track connection with new track connection
     *
     * @param oldTrack the old track connection
     * @param newTrack the new track connection
     * @return true if successful
     */
    public boolean replaceTrackConnection(@Nullable LayoutTrack oldTrack, @Nullable LayoutTrack newTrack, int newType) {
        boolean result = false; // assume failure (pessimist!)
        // trying to replace old track with null?
        if (newTrack == null) {
            // (yes) remove old connection
            if (oldTrack != null) {
                result = true;  // assume success (optimist!)
                if (connect1 == oldTrack) {
                    connect1 = null;
                    type1 = NONE;
                } else if (connect2 == oldTrack) {
                    connect2 = null;
                    type2 = NONE;
                } else {
                    result = false; // didn't find old connection
                }
            } else {
                result = false; // can't replace null with null
            }
            if (!result) {
                log.error("Attempt to remove non-existant track connection");
            }
        } else // already connected to newTrack?
        if ((connect1 != newTrack) && (connect2 != newTrack)) {
            // (no) find a connection we can connect to
            result = true;  // assume success (optimist!)
            if (connect1 == oldTrack) {
                connect1 = newTrack;
                type1 = newType;
            } else if (connect2 == oldTrack) {
                connect2 = newTrack;
                type2 = newType;
            } else {
                log.error("Attempt to replace invalid connection");
                result = false;
            }
        }
        return result;
    }

    /**
     * @return true if track segment should be drawn dashed
     * @deprecated since 4.9.4; use {@link #isDashed()} instead
     */
    @Deprecated // Java standard pattern for boolean getters is "isDashed()"
    public boolean getDashed() {
        return dashed;
    }

    /**
     * @return true if track segment should be drawn dashed
     */
    public boolean isDashed() {
        return dashed;
    }

    public void setDashed(boolean dash) {
        if (dashed != dash) {
            dashed = dash;
            layoutEditor.redrawPanel();
        }
    }

    /**
     * @return true if track segment is a main line
     * @deprecated since 4.9.4; use {@link #isMainline()} instead
     */
    @Deprecated // Java standard pattern for boolean getters is "isMainline()"
    public boolean getMainline() {
        return mainline;
    }

    /**
     * @return true if track segment is a main line
     */
    public boolean isMainline() {
        return mainline;
    }

    public void setMainline(boolean main) {
        if (mainline != main) {
            mainline = main;
            layoutEditor.redrawPanel();
        }
    }

    /**
     * @return true if track segment is an arc
     * @deprecated since 4.9.4; use {@link #isArc()} instead
     */
    @Deprecated // Java standard pattern for boolean getters is "isArc()"
    public boolean getArc() {
        return arc;
    }

    /**
     * @return true if track segment is an arc
     */
    public boolean isArc() {
        return arc;
    }

    public void setArc(boolean boo) {
        if (arc != boo) {
            arc = boo;
            if (arc) {
                bezier = false;
            }
            changed = true;
        }
    }

    /**
     * @return true if track segment is circle
     * @deprecated since 4.9.4; use {@link #isCircle()} instead
     */
    @Deprecated // Java standard pattern for boolean getters is "isCircle()"
    public boolean getCircle() {
        return circle;
    }

    /**
     * @return true if track segment is circle
     */
    public boolean isCircle() {
        return circle;
    }

    public void setCircle(boolean boo) {
        if (circle != boo) {
            circle = boo;
            if (circle) {
                bezier = false;
            }
            changed = true;
        }
    }

    /**
     * @return true if track segment circle or arc should be drawn flipped
     * @deprecated since 4.9.4; use {@link #isFlip()} instead
     */
    @Deprecated // Java standard pattern for boolean getters is "isFlip()"
    public boolean getFlip() {
        return flip;
    }

    /**
     * @return true if track segment circle or arc should be drawn flipped
     */
    public boolean isFlip() {
        return flip;
    }

    public void setFlip(boolean boo) {
        if (flip != boo) {
            flip = boo;
            changed = true;
        }
    }

    /**
     * @return true if track segment is a bezier curve
     * @deprecated since 4.9.4; use {@link #isBezier()} instead
     */
    @Deprecated // Java standard pattern for boolean getters is "isBezier()"
    public boolean getBezier() {
        return bezier;
    }

    /**
     * @return true if track segment is a bezier curve
     */
    public boolean isBezier() {
        return bezier;
    }

    public void setBezier(boolean boo) {
        if (bezier != boo) {
            bezier = boo;
            if (bezier) {
                arc = false;
                circle = false;
            }
            changed = true;
        }
    }

    public double getAngle() {
        return angle;
    }

    public void setAngle(double x) {
        angle = MathUtil.pin(x, 0.0D, 180.0D);
        changed = true;
    }

    /**
     * Determine if we need to redraw a curved piece of track. Saves having to
     * recalculate the circle details each time.
     */
    public boolean trackNeedsRedraw() {
        return changed;
    }

    public void trackRedrawn() {
        changed = false;
    }

    public LayoutBlock getLayoutBlock() {
        if ((layoutBlock == null) && (blockName != null) && !blockName.isEmpty()) {
            layoutBlock = layoutEditor.provideLayoutBlock(blockName);
        }
        return layoutBlock;
    }

    public String getConnect1Name() {
        return getConnectName(connect1, type1);
    }

    public String getConnect2Name() {
        return getConnectName(connect2, type2);
    }

    private String getConnectName(@Nullable LayoutTrack layoutTrack, int type) {
        String result = null;
        if (layoutTrack != null) {
            result = ((LayoutTrack) layoutTrack).getName();
        }
        return result;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation returns null because {@link #getConnect1} and
     * {@link #getConnect2} should be used instead.
     */
    // only implemented here to suppress "does not override abstract method " error in compiler
    public LayoutTrack getConnection(int connectionType) throws jmri.JmriException {
        // nothing to see here, move along
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation does nothing because {@link #setNewConnect1} and
     * {@link #setNewConnect2} should be used instead.
     */
    // only implemented here to suppress "does not override abstract method " error in compiler
    public void setConnection(int connectionType, @Nullable LayoutTrack o, int type) throws jmri.JmriException {
        // nothing to see here, move along
    }

    public int getNumberOfBezierControlPoints() {
        return bezierControlPoints.size();
    }

    public Point2D getBezierControlPoint(int index) {
        Point2D result = center;
        if (index < 0) {
            index += bezierControlPoints.size();
        }
        if ((index >= 0) && (index < bezierControlPoints.size())) {
            result = bezierControlPoints.get(index);
        }
        return result;
    }

    public void setBezierControlPoint(@Nullable Point2D p, int index) {
        if (index < 0) {
            index += bezierControlPoints.size();
        }
        if ((index >= 0) && (index <= bezierControlPoints.size())) {
            if (index < bezierControlPoints.size()) {
                bezierControlPoints.set(index, p);
            } else {
                bezierControlPoints.add(p);
            }
        }
    }

    /**
     * Set Up a Layout Block for a Track Segment.
     */
    public void setLayoutBlock(@Nullable LayoutBlock b) {
        if (layoutBlock != b) {
            // block has changed, if old block exists, decrement use
            if (layoutBlock != null) {
                layoutBlock.decrementUse();
            }
            layoutBlock = b;
            if (b != null) {
                blockName = b.getId();
            } else {
                blockName = "";
            }
        }
    }

    public void setLayoutBlockByName(@Nullable String name) {
        blockName = name;
    }

    /*
     * non-accessor methods
     */
    /**
     * scale this LayoutTrack's coordinates by the x and y factors
     *
     * @param xFactor the amount to scale X coordinates
     * @param yFactor the amount to scale Y coordinates
     */
    public void scaleCoords(float xFactor, float yFactor) {
        // nothing to see here, move along
    }

    /**
     * translate this LayoutTrack's coordinates by the x and y factors
     *
     * @param xFactor the amount to translate X coordinates
     * @param yFactor the amount to translate Y coordinates
     */
    public void translateCoords(float xFactor, float yFactor) {
        // nothing to see here, move along
    }

    /**
     * set center coordinates
     *
     * @param newCenterPoint the coordinates to set
     */
    public void setCoordsCenter(@Nullable Point2D newCenterPoint) {
        if (center != newCenterPoint) {
            if ((newCenterPoint != null) && isBezier()) {
                Point2D delta = MathUtil.subtract(newCenterPoint, center);
                for (Point2D p : bezierControlPoints) {
                    p.setLocation(MathUtil.add(p, delta));
                }
            }
            center = newCenterPoint;
        }
    }

    // initialization instance variables (used when loading a LayoutEditor)
    public String tBlockName = "";
    public String tConnect1Name = "";
    public String tConnect2Name = "";

    /**
     * Initialization method. The above variables are initialized by
     * PositionablePointXml, then the following method is called after the
     * entire LayoutEditor is loaded to set the specific TrackSegment objects.
     */
    @SuppressWarnings("deprecation")
    //NOTE: findObjectByTypeAndName is @Deprecated;
    // we're using it here for backwards compatibility until it can be removed
    public void setObjects(LayoutEditor p) {
        if (!tBlockName.isEmpty()) {
            layoutBlock = p.getLayoutBlock(tBlockName);
            if (layoutBlock != null) {
                blockName = tBlockName;
                layoutBlock.incrementUse();
            } else {
                log.error("bad blockname '" + tBlockName + "' in tracksegment " + getName());
            }
        }

        //NOTE: testing "type-less" connects
        // (read comments for findObjectByName in LayoutEditorFindItems.java)
        connect1 = p.getFinder().findObjectByName(tConnect1Name);
        if (null == connect1) { // findObjectByName failed... try findObjectByTypeAndName
            log.warn("Unknown connect1 object prefix: '" + tConnect1Name + "' of type " + type1 + ".");
            connect1 = (LayoutTrack) p.getFinder().findObjectByTypeAndName(type1, tConnect1Name);
        }
        connect2 = p.getFinder().findObjectByName(tConnect2Name);
        if (null == connect2) { // findObjectByName failed; try findObjectByTypeAndName
            log.warn("Unknown connect2 object prefix: '" + tConnect2Name + "' of type " + type2 + ".");
            connect2 = (LayoutTrack) p.getFinder().findObjectByTypeAndName(type2, tConnect2Name);
        }
    }

    protected void updateBlockInfo() {
        if (layoutBlock != null) {
            layoutBlock.updatePaths();
        }
        LayoutBlock b1 = getBlock(connect1, type1);
        if ((b1 != null) && (b1 != layoutBlock)) {
            b1.updatePaths();
        }
        LayoutBlock b2 = getBlock(connect2, type2);
        if ((b2 != null) && (b2 != layoutBlock) && (b2 != b1)) {
            b2.updatePaths();
        }

        getConnect1().reCheckBlockBoundary();
        getConnect2().reCheckBlockBoundary();
    }

    private LayoutBlock getBlock(LayoutTrack connect, int type) {
        if (connect == null) {
            return null;
        }
        if (type == POS_POINT) {
            PositionablePoint p = (PositionablePoint) connect;
            if (p.getConnect1() != this) {
                if (p.getConnect1() != null) {
                    return (p.getConnect1().getLayoutBlock());
                } else {
                    return null;
                }
            } else {
                if (p.getConnect2() != null) {
                    return (p.getConnect2().getLayoutBlock());
                } else {
                    return null;
                }
            }
        } else {
            return (layoutEditor.getAffectedBlock(connect, type));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int findHitPointType(Point2D hitPoint, boolean useRectangles, boolean requireUnconnected) {
        int result = NONE;  // assume point not on connection

        if (!requireUnconnected) {
            //note: optimization here: instead of creating rectangles for all the
            // points to check below, we create a rectangle for the test point
            // and test if the points below are in that rectangle instead.
            Rectangle2D r = layoutEditor.trackControlRectAt(hitPoint);
            Point2D p, minPoint = MathUtil.zeroPoint2D;

            double circleRadius = LayoutEditor.SIZE * layoutEditor.getTurnoutCircleSize();
            double distance, minDistance = POSITIVE_INFINITY;

            if (isCircle()) {
                p = getCoordsCenterCircle();
                distance = MathUtil.distance(p, hitPoint);
                if (distance < minDistance) {
                    minDistance = distance;
                    minPoint = p;
                    result = TRACK_CIRCLE_CENTRE;
                }
            } else if (isBezier()) {
                // hit testing for the control points
                for (int index = 0; index < bezierControlPoints.size(); index++) {
                    p = bezierControlPoints.get(index);
                    distance = MathUtil.distance(p, hitPoint);
                    if (distance < minDistance) {
                        minDistance = distance;
                        minPoint = p;
                        result = BEZIER_CONTROL_POINT_OFFSET_MIN + index;
                    }
                }
            }
            if ((useRectangles && !r.contains(minPoint))
                    || (!useRectangles && (minDistance > circleRadius))) {
                result = NONE;
            }
            if (result == NONE) {
                if (r.contains(getCentreSeg())) {
                    result = TRACK;
                }
            }
        }
        return result;
    }   // findHitPointType

    /**
     * Get the coordinates for a specified connection type.
     *
     * @param connectionType the connection type
     * @return the coordinates for the specified connection type
     */
    public Point2D getCoordsForConnectionType(int connectionType) {
        Point2D result = getCentreSeg();
        if (connectionType == TRACK_CIRCLE_CENTRE) {
            result = getCoordsCenterCircle();
        } else if ((connectionType >= BEZIER_CONTROL_POINT_OFFSET_MIN) && (connectionType <= BEZIER_CONTROL_POINT_OFFSET_MAX)) {
            result = getBezierControlPoint(connectionType - BEZIER_CONTROL_POINT_OFFSET_MIN);
        }
        return result;
    }

    /**
     * @return the bounds of this track segment
     */
    public Rectangle2D getBounds() {
        Rectangle2D result;

        Point2D ep1 = center, ep2 = center;
        if (getConnect1() != null) {
            ep1 = layoutEditor.getCoords(getConnect1(), getType1());
        }
        if (getConnect2() != null) {
            ep2 = layoutEditor.getCoords(getConnect2(), getType2());
        }

        result = new Rectangle2D.Double(ep1.getX(), ep1.getY(), 0, 0);
        result.add(ep2);

        return result;
    }

    private JPopupMenu popup = null;
    private JCheckBoxMenuItem mainlineCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("Mainline"));
    private JCheckBoxMenuItem hiddenCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("Hidden"));
    private JCheckBoxMenuItem dashedCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("Dashed"));

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    protected JPopupMenu showPopup(@Nonnull MouseEvent mouseEvent) {
        if (popup != null) {
            popup.removeAll();
        } else {
            popup = new JPopupMenu();
        }

        String info = Bundle.getMessage("TrackSegment");
        if (isArc()) {
            if (isCircle()) {
                info = info + " (" + Bundle.getMessage("Circle") + ")";
            } else {
                info = info + " (" + Bundle.getMessage("Ellipse") + ")";
            }
        } else if (isBezier()) {
            info = info + " (" + Bundle.getMessage("Bezier") + ")";
        } else {
            info = info + " (" + Bundle.getMessage("Line") + ")";
        }

        JMenuItem jmi = popup.add(Bundle.getMessage("MakeLabel", info) + getName());
        jmi.setEnabled(false);

        if (blockName.isEmpty()) {
            jmi = popup.add(Bundle.getMessage("NoBlock"));
        } else {
            jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameBlock")) + getLayoutBlock().getDisplayName());
        }
        jmi.setEnabled(false);

        // if there are any track connections
        if ((connect1 != null) || (connect2 != null)) {
            JMenu connectionsMenu = new JMenu(Bundle.getMessage("Connections")); // there is no pane opening (which is what ... implies)
            if (connect1 != null) {
                connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "1") + connect1.getName()) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        LayoutEditorFindItems lf = layoutEditor.getFinder();
                        LayoutTrack lt = lf.findObjectByName(connect1.getName());
                        // this shouldn't ever be null... however...
                        if (lt != null) {
                            layoutEditor.setSelectionRect(lt.getBounds());
                            lt.showPopup();
                        }
                    }
                });
            }
            if (connect2 != null) {
                connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "2") + connect2.getName()) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        LayoutEditorFindItems lf = layoutEditor.getFinder();
                        LayoutTrack lt = lf.findObjectByName(connect2.getName());
                        // this shouldn't ever be null... however...
                        if (lt != null) {
                            layoutEditor.setSelectionRect(lt.getBounds());
                            lt.showPopup();
                        }
                    }
                });
            }
            popup.add(connectionsMenu);
        }

        popup.add(new JSeparator(JSeparator.HORIZONTAL));

        mainlineCheckBoxMenuItem.setSelected(mainline);
        popup.add(mainlineCheckBoxMenuItem);
        mainlineCheckBoxMenuItem.addActionListener((java.awt.event.ActionEvent e3) -> {
            setMainline(mainlineCheckBoxMenuItem.isSelected());
        });

        hiddenCheckBoxMenuItem.setSelected(hidden);
        popup.add(hiddenCheckBoxMenuItem);
        hiddenCheckBoxMenuItem.addActionListener((java.awt.event.ActionEvent e3) -> {
            setHidden(hiddenCheckBoxMenuItem.isSelected());
        });

        dashedCheckBoxMenuItem.setSelected(dashed);
        popup.add(dashedCheckBoxMenuItem);
        dashedCheckBoxMenuItem.addActionListener((java.awt.event.ActionEvent e3) -> {
            setDashed(dashedCheckBoxMenuItem.isSelected());
        });

        popup.add(new JSeparator(JSeparator.HORIZONTAL));
        popup.add(new AbstractAction(Bundle.getMessage("ButtonEdit")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                layoutEditor.getLayoutTrackEditors().editTrackSegment(TrackSegment.this);
            }
        });
        popup.add(new AbstractAction(Bundle.getMessage("ButtonDelete")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                layoutEditor.removeTrackSegment(TrackSegment.this);
                remove();
                dispose();
            }
        });
        popup.add(new AbstractAction(Bundle.getMessage("SplitTrackSegment")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                splitTrackSegment();
            }
        ;
        });

        JMenu lineType = new JMenu(Bundle.getMessage("ChangeTo"));
        jmi = lineType.add(new JCheckBoxMenuItem(new AbstractAction(Bundle.getMessage("Line")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeType(0);
            }
        }));
        jmi.setSelected(!isArc() && !isBezier());

        jmi = lineType.add(new JCheckBoxMenuItem(new AbstractAction(Bundle.getMessage("Circle")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeType(1);
            }
        }));
        jmi.setSelected(isArc() && isCircle());

        jmi = lineType.add(new JCheckBoxMenuItem(new AbstractAction(Bundle.getMessage("Ellipse")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeType(2);
            }
        }));
        jmi.setSelected(isArc() && !isCircle());

        jmi = lineType.add(new JCheckBoxMenuItem(new AbstractAction(Bundle.getMessage("Bezier")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeType(3);
            }
        }));
        jmi.setSelected(!isArc() && isBezier());

        popup.add(lineType);

        if (isArc()) {
            popup.add(new AbstractAction(Bundle.getMessage("FlipAngle")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    flipAngle();
                }
            });
        }
        if (isArc() || isBezier()) {
            if (hideConstructionLines()) {
                popup.add(new AbstractAction(Bundle.getMessage("ShowConstruct")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        hideConstructionLines(SHOWCON);
                    }
                });
            } else {
                popup.add(new AbstractAction(Bundle.getMessage("HideConstruct")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        hideConstructionLines(HIDECON);
                    }
                });
            }
        }
        if ((!blockName.isEmpty()) && (jmri.InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled())) {
            popup.add(new AbstractAction(Bundle.getMessage("ViewBlockRouting")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    AbstractAction routeTableAction = new LayoutBlockRouteTableAction("ViewRouting", getLayoutBlock());
                    routeTableAction.actionPerformed(e);
                }
            });
        }
        popup.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
        return popup;
    }   // showPopup

    /**
     * split track segment into two track segments with an anchor between
     */
    public void splitTrackSegment() {
        // create a new anchor
        Point2D p = getCentreSeg();
        PositionablePoint newAnchor = layoutEditor.addAnchor(p);
        // link it to me
        layoutEditor.setLink(newAnchor, POS_POINT, this, TRACK);

        //get unique name for a new track segment
        String name = layoutEditor.getFinder().uniqueName("T", 1);

        //create it between the new anchor and my connect2(/type2)
        TrackSegment newTrackSegment = new TrackSegment(name,
                newAnchor, POS_POINT,
                connect2, type2,
                isDashed(), isMainline(), layoutEditor);
        // add it to known tracks
        layoutEditor.getLayoutTracks().add(newTrackSegment);
        layoutEditor.setDirty();

        // copy attributes to new track segment
        newTrackSegment.setLayoutBlock(this.getLayoutBlock());
        newTrackSegment.setArc(this.isArc());
        newTrackSegment.setCircle(this.isCircle());
        //newTrackSegment.setBezier(this.isBezier());
        newTrackSegment.setFlip(this.isFlip());

        // link my connect2 to the new track segment
        if (connect2 instanceof PositionablePoint) {
            PositionablePoint pp = (PositionablePoint) connect2;
            pp.replaceTrackConnection(this, newTrackSegment);
        } else {
            layoutEditor.setLink(connect2, type2, newTrackSegment, TRACK);
        }

        // link the new anchor to the new track segment
        layoutEditor.setLink(newAnchor, POS_POINT, newTrackSegment, TRACK);

        // link me to the new newAnchor
        connect2 = newAnchor;
        type2 = POS_POINT;

        //check on layout block
        LayoutBlock b = this.getLayoutBlock();

        if (b != null) {
            newTrackSegment.setLayoutBlock(b);
            layoutEditor.getLEAuxTools().setBlockConnectivityChanged();
            newTrackSegment.updateBlockInfo();
        }
        layoutEditor.setDirty();
        layoutEditor.redrawPanel();
    }

    /**
     * Display popup menu for information and editing.
     */
    protected void showBezierPopUp(MouseEvent e, int hitPointType) {
        int bezierControlPointIndex = hitPointType - BEZIER_CONTROL_POINT_OFFSET_MIN;
        if (popup != null) {
            popup.removeAll();
        } else {
            popup = new JPopupMenu();
        }

        JMenuItem jmi = popup.add(Bundle.getMessage("BezierControlPoint") + " #" + bezierControlPointIndex);
        jmi.setEnabled(false);
        popup.add(new JSeparator(JSeparator.HORIZONTAL));

        if (bezierControlPoints.size() < BEZIER_CONTROL_POINT_OFFSET_MAX - BEZIER_CONTROL_POINT_OFFSET_MIN) {
            popup.add(new AbstractAction(Bundle.getMessage("AddBezierControlPointAfter")) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    addBezierControlPointAfter(bezierControlPointIndex);
                }
            });
            popup.add(new AbstractAction(Bundle.getMessage("AddBezierControlPointBefore")) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    addBezierControlPointBefore(bezierControlPointIndex);
                }
            });
        }

        if (bezierControlPoints.size() > 2) {
            popup.add(new AbstractAction(Bundle.getMessage("DeleteBezierControlPoint") + " #" + bezierControlPointIndex) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    deleteBezierControlPoint(bezierControlPointIndex);
                }
            });
        }
        popup.show(e.getComponent(), e.getX(), e.getY());
    }

    private void addBezierControlPointBefore(int index) {
        Point2D addPoint = getBezierControlPoint(index);
        if (index > 0) {
            addPoint = MathUtil.midPoint(getBezierControlPoint(index - 1), addPoint);
        } else {
            Point2D ep1 = layoutEditor.getCoords(getConnect1(), getType1());
            addPoint = MathUtil.midPoint(ep1, addPoint);
        }
        bezierControlPoints.add(index, addPoint);
        layoutEditor.redrawPanel();
        layoutEditor.setDirty();
    }

    private void addBezierControlPointAfter(int index) {
        int cnt = bezierControlPoints.size();
        Point2D addPoint = getBezierControlPoint(index);
        if (index < cnt - 1) {
            addPoint = MathUtil.midPoint(addPoint, getBezierControlPoint(index + 1));
            bezierControlPoints.add(index + 1, addPoint);
        } else {
            Point2D ep2 = layoutEditor.getCoords(getConnect2(), getType2());
            addPoint = MathUtil.midPoint(addPoint, ep2);
            bezierControlPoints.add(addPoint);
        }
        layoutEditor.redrawPanel();
        layoutEditor.setDirty();
    }

    private void deleteBezierControlPoint(int index) {
        if ((index >= 0) && (index < bezierControlPoints.size())) {
            bezierControlPoints.remove(index);
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }

    void changeType(int choice) {
        switch (choice) {
            case 0: // plain track segment (line)
                setArc(false);
                setAngle(0.0D);
                setCircle(false);
                setBezier(false);
                break;
            case 1: // circle
                setArc(true);
                setAngle(90.0D);
                setCircle(true);
                setBezier(false);
                break;
            case 2:
                setArc(true);   // arc
                setAngle(90.0D);
                setCircle(false);
                setBezier(false);
                break;
            case 3:
                setArc(false);  // bezier
                setCircle(false);
                if (bezierControlPoints.size() == 0) {
                    // set default control point displacements
                    Point2D ep1 = layoutEditor.getCoords(getConnect1(), getType1());
                    Point2D ep2 = layoutEditor.getCoords(getConnect2(), getType2());

                    // compute orthogonal offset0 with length one third the distance from ep1 to ep2
                    Point2D offset = MathUtil.subtract(ep2, ep1);
                    offset = MathUtil.normalize(offset, MathUtil.length(offset) / 3.0);
                    offset = MathUtil.orthogonal(offset);

                    // add orthogonal offset0 to 1/3rd and 2/3rd points
                    Point2D pt1 = MathUtil.add(MathUtil.oneThirdPoint(ep1, ep2), offset);
                    Point2D pt2 = MathUtil.subtract(MathUtil.twoThirdsPoint(ep1, ep2), offset);

                    bezierControlPoints.add(pt1);
                    bezierControlPoints.add(pt2);
                }
                setBezier(true);    // do this last (it calls reCenter())
                break;
            default:
                break;
        }
        layoutEditor.redrawPanel();
        layoutEditor.setDirty();
    }

    void flipAngle() {
        setFlip(!isFlip());
        layoutEditor.redrawPanel();
        layoutEditor.setDirty();
    }

    /**
     * Clean up when this object is no longer needed.
     * <p>
     * Should not be called while the object is still displayed.
     *
     * @see #remove()
     */
    void dispose() {
        if (popup != null) {
            popup.removeAll();
        }
        popup = null;
    }

    /**
     * Remove this object from display and persistance.
     */
    void remove() {
        // remove from persistance by flagging inactive
        active = false;
    }

    boolean active = true;

    /**
     * Get state. "active" means that the object is still displayed, and should
     * be stored.
     */
    public boolean isActive() {
        return active;
    }

    public static final int SHOWCON = 0x01;
    public static final int HIDECON = 0x02;     //flag set on a segment basis.
    public static final int HIDECONALL = 0x04;  //Used by layout editor for hiding all

    public int showConstructionLine = SHOWCON;

    //TODO: @Deprecated // Java standard pattern for boolean getters is "isShowConstructionLinesLE()"
    protected boolean showConstructionLinesLE() {
        if ((showConstructionLine & HIDECON) == HIDECON || (showConstructionLine & HIDECONALL) == HIDECONALL) {
            return false;
        }
        return true;
    }

    //Methods used by Layout Editor
    public void hideConstructionLines(int hide) {
        if (hide == HIDECONALL) {
            showConstructionLine |= HIDECONALL;
        } else if (hide == SHOWCON) {
            if ((showConstructionLine & HIDECONALL) == HIDECONALL) {
                showConstructionLine &= ~HIDECONALL;
            } else {
                showConstructionLine = hide;
            }
        } else {
            showConstructionLine = HIDECON;
        }
        layoutEditor.redrawPanel();
        layoutEditor.setDirty();
    }

    public boolean hideConstructionLines() {
        if ((showConstructionLine & SHOWCON) == SHOWCON) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * The following are used only as a temporary store after a circle or arc
     * has been calculated. This prevents the need to recalculate the values
     * each time a re-draw is required.
     */
    private Point2D pt1;
    private Point2D pt2;

    public Point2D getTmpPt1() {
        return pt1;
    }

    public Point2D getTmpPt2() {
        return pt2;
    }

    public void setTmpPt1(Point2D Pt1) {
        pt1 = Pt1;
        changed = true;
    }

    public void setTmpPt2(Point2D Pt2) {
        pt2 = Pt2;
        changed = true;
    }

    private double cX;

    public double getCX() {
        return cX;
    }

    public void setCX(double CX) {
        cX = CX;
    }

    private double cY;

    public double getCY() {
        return cY;
    }

    public void setCY(double CY) {
        cY = CY;
    }

    private double cW;

    public double getCW() {
        return cW;
    }

    public void setCW(double CW) {
        cW = CW;
    }

    private double cH;

    public double getCH() {
        return cH;
    }

    public void setCH(double CH) {
        cH = CH;
    }

    private double startAdj;

    public double getStartAdj() {
        return startAdj;
    }

    public void setStartAdj(double startAdj) {
        this.startAdj = startAdj;
    }

    // this is the center of the track segment (it is "on" the track segment)
    public double getCentreSegX() {
        return getCentreSeg().getX();
    }

    public void setCentreSegX(double x) {
        center.setLocation(x, getCentreSeg().getY());
    }

    public double getCentreSegY() {
        return getCentreSeg().getY();
    }

    public void setCentreSegY(double y) {
        center.setLocation(getCentreSeg().getX(), y);
    }

    /**
     * @return the location of the middle of the segment (on the segment)
     */
    public Point2D getCentreSeg() {
        Point2D result = MathUtil.zeroPoint2D;

        if ((connect1 != null) && (connect2 != null)) {
            // get the end points
            Point2D ep1 = layoutEditor.getCoords(getConnect1(), getType1());
            Point2D ep2 = layoutEditor.getCoords(getConnect2(), getType2());

            if (isCircle()) {
                result = center; //new Point2D.Double(centreX, centreY);
            } else if (isArc()) {
                center = MathUtil.midPoint(getBounds());
                if (isFlip()) {
                    Point2D t = ep1;
                    ep1 = ep2;
                    ep2 = t;
                }
                Point2D delta = MathUtil.subtract(ep1, ep2);
                // are they of the same sign?
                if ((delta.getX() >= 0.0) != (delta.getY() >= 0.0)) {
                    delta = MathUtil.divide(delta, +5.0, -5.0);
                } else {
                    delta = MathUtil.divide(delta, -5.0, +5.0);
                }
                result = MathUtil.add(center, delta);
            } else if (isBezier()) {
                // compute result Bezier point for (t == 0.5);
                // copy all the control points (including end points) into an array
                int len = bezierControlPoints.size() + 2;
                Point2D[] points = new Point2D[len];
                points[0] = ep1;
                for (int idx = 1; idx < len - 1; idx++) {
                    points[idx] = bezierControlPoints.get(idx - 1);
                }
                points[len - 1] = ep2;

                // calculate midpoints of all points (len - 1 order times)
                for (int idx = len - 1; idx > 0; idx--) {
                    for (int jdx = 0; jdx < idx; jdx++) {
                        points[jdx] = MathUtil.midPoint(points[jdx], points[jdx + 1]);
                    }
                }
                result = points[0];
            } else {
                result = MathUtil.midPoint(ep1, ep2);
            }
            center = result;
        }
        return result;
    }

    public void setCentreSeg(Point2D p) {
        center = p;
    }

    // this is the center of the track segment when configured as a circle
    private double centreX;

    public double getCentreX() {
        return centreX;
    }

    public void setCentreX(double x) {
        centreX = x;
    }

    private double centreY;

    public double getCentreY() {
        return centreY;
    }

    public void setCentreY(double y) {
        centreY = y;
    }

    public Point2D getCentre() {
        return new Point2D.Double(centreX, centreY);
    }

    private double tmpangle;

    public double getTmpAngle() {
        return tmpangle;
    }

    public void setTmpAngle(double a) {
        tmpangle = a;
    }

    /**
     * get center coordinates
     *
     * @return the center coordinates
     */
    public Point2D getCoordsCenterCircle() {
        return getCentre();
    }

    /**
     * set center coordinates
     *
     * @param p the coordinates to set
     */
    public void setCoordsCenterCircle(Point2D p) {
        centreX = p.getX();
        centreY = p.getY();
    }

    private double chordLength;

    public double getChordLength() {
        return chordLength;
    }

    public void setChordLength(double chord) {
        chordLength = chord;
    }

    /*
     * Called when the user changes the angle dynamically in edit mode
     * by dragging the centre of the cirle.
     */
    protected void reCalculateTrackSegmentAngle(double x, double y) {
        if (!isBezier()) {
            double pt2x;
            double pt2y;
            double pt1x;
            double pt1y;

            if (isFlip()) {
                pt1x = getTmpPt2().getX();
                pt1y = getTmpPt2().getY();
                pt2x = getTmpPt1().getX();
                pt2y = getTmpPt1().getY();
            } else {
                pt1x = getTmpPt1().getX();
                pt1y = getTmpPt1().getY();
                pt2x = getTmpPt2().getX();
                pt2y = getTmpPt2().getY();
            }
            //Point 1 to new point distance
            double a;
            double o;
            double la;
            // Compute arc's chord
            a = pt2x - x;
            o = pt2y - y;
            la = Math.hypot(a, o);

            double lb;
            a = pt1x - x;
            o = pt1y - y;
            lb = Math.hypot(a, o);

            double newangle = Math.toDegrees(Math.acos((-getChordLength() * getChordLength() + la * la + lb * lb) / (2 * la * lb)));
            setAngle(newangle);
        }
    }

    /*
     * Calculate the initally parameters for drawing a circular track segment.
     */
    protected void calculateTrackSegmentAngle() {
        Point2D pt1, pt2;
        if (isFlip()) {
            pt1 = layoutEditor.getCoords(getConnect2(), getType2());
            pt2 = layoutEditor.getCoords(getConnect1(), getType1());
        } else {
            pt1 = layoutEditor.getCoords(getConnect1(), getType1());
            pt2 = layoutEditor.getCoords(getConnect2(), getType2());
        }
        if ((getTmpPt1() != pt1) || (getTmpPt2() != pt2) || trackNeedsRedraw()) {
            setTmpPt1(pt1);
            setTmpPt2(pt2);

            double pt1x = pt1.getX();
            double pt1y = pt1.getY();
            double pt2x = pt2.getX();
            double pt2y = pt2.getY();

            if (getAngle() == 0.0D) {
                setTmpAngle(90.0D);
            } else {
                setTmpAngle(getAngle());
            }
            // Convert angle to radiants in order to speed up math
            double halfAngleRAD = Math.toRadians(getTmpAngle()) / 2.0D;

            // Compute arc's chord
            double a = pt2x - pt1x;
            double o = pt2y - pt1y;
            double chord = Math.hypot(a, o);
            setChordLength(chord);

            // Make sure chord is not null
            // In such a case (ep1 == ep2), there is no arc to draw
            if (chord > 0.0D) {
                double radius = (chord / 2) / Math.sin(halfAngleRAD);
                // Circle
                double startRad = Math.atan2(a, o) - halfAngleRAD;
                setStartAdj(Math.toDegrees(startRad));
                if (isCircle()) {
                    // Circle - Compute center
                    setCentreX(pt2x - Math.cos(startRad) * radius);
                    setCentreY(pt2y + Math.sin(startRad) * radius);

                    // Circle - Compute rectangle required by Arc2D.Double
                    setCW(radius * 2.0D);
                    setCH(radius * 2.0D);
                    setCX(getCentreX() - radius);
                    setCY(getCentreY() - radius);

                    // Compute where to locate the control circle on the circle segment
                    Point2D offset = new Point2D.Double(
                            +radius * Math.cos(startRad + halfAngleRAD),
                            -radius * Math.sin(startRad + halfAngleRAD));
                    setCentreSeg(MathUtil.add(getCentre(), offset));
                } else {
                    // Ellipse - Round start angle to the closest multiple of 90
                    setStartAdj(Math.round(getStartAdj() / 90.0D) * 90.0D);
                    // Ellipse - Compute rectangle required by Arc2D.Double
                    setCW(Math.abs(a) * 2.0D);
                    setCH(Math.abs(o) * 2.0D);
                    // Ellipse - Adjust rectangle corner, depending on quadrant
                    if (o * a < 0.0D) {
                        a = -a;
                    } else {
                        o = -o;
                    }
                    setCX(Math.min(pt1x, pt2x) - Math.max(a, 0.0D));
                    setCY(Math.min(pt1y, pt2y) - Math.max(o, 0.0D));
                }
            }
        }
    }   // calculateTrackSegmentAngle

    /**
     * {@inheritDoc}
     */
    @Override
    protected void draw1(Graphics2D g2, boolean isMain, boolean isBlock) {
        if (isMain == mainline) {
            if (isBlock) {
                setColorForTrackBlock(g2, getLayoutBlock());
            }
            if (isArc()) {
                calculateTrackSegmentAngle();
                g2.draw(new Arc2D.Double(getCX(), getCY(), getCW(), getCH(), getStartAdj(), getTmpAngle(), Arc2D.OPEN));
                trackRedrawn();
            } else if (isBezier()) {
                Point2D pt1 = layoutEditor.getCoords(getConnect1(), getType1());
                Point2D pt2 = layoutEditor.getCoords(getConnect2(), getType2());

                int cnt = bezierControlPoints.size();
                Point2D[] points = new Point2D[cnt + 2];
                points[0] = pt1;
                for (int idx = 0; idx < cnt; idx++) {
                    points[idx + 1] = bezierControlPoints.get(idx);
                }
                points[cnt + 1] = pt2;

                MathUtil.drawBezier(g2, points);
            } else {
                Point2D end1 = layoutEditor.getCoords(getConnect1(), getType1());
                Point2D end2 = layoutEditor.getCoords(getConnect2(), getType2());

                g2.draw(new Line2D.Double(end1, end2));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void draw2(Graphics2D g2, boolean isMain, float railDisplacement) {
        if (isMain == mainline) {
            if (isArc()) {
                calculateTrackSegmentAngle();
                Rectangle2D cRectangle2D = new Rectangle2D.Double(
                        getCX(), getCY(), getCW(), getCH());
                Rectangle2D cLeftRectangle2D = MathUtil.inset(cRectangle2D, -railDisplacement);
                double startAdj = getStartAdj(), tmpAngle = getTmpAngle();
                g2.draw(new Arc2D.Double(
                        cLeftRectangle2D.getX(),
                        cLeftRectangle2D.getY(),
                        cLeftRectangle2D.getWidth(),
                        cLeftRectangle2D.getHeight(),
                        startAdj, tmpAngle, Arc2D.OPEN));
                Rectangle2D cLRightRectangle2D = MathUtil.inset(cRectangle2D, +railDisplacement);
                g2.draw(new Arc2D.Double(
                        cLRightRectangle2D.getX(),
                        cLRightRectangle2D.getY(),
                        cLRightRectangle2D.getWidth(),
                        cLRightRectangle2D.getHeight(),
                        startAdj, tmpAngle, Arc2D.OPEN));
                trackRedrawn();
            } else if (isBezier()) {
                Point2D pt1 = layoutEditor.getCoords(getConnect1(), getType1());
                Point2D pt2 = layoutEditor.getCoords(getConnect2(), getType2());

                int cnt = bezierControlPoints.size();
                Point2D[] points = new Point2D[cnt + 2];
                points[0] = pt1;
                for (int idx = 0; idx < cnt; idx++) {
                    points[idx + 1] = bezierControlPoints.get(idx);
                }
                points[cnt + 1] = pt2;

                MathUtil.drawBezier(g2, points, -railDisplacement);
                MathUtil.drawBezier(g2, points, +railDisplacement);
            } else {
                Point2D end1 = layoutEditor.getCoords(getConnect1(), getType1());
                Point2D end2 = layoutEditor.getCoords(getConnect2(), getType2());

                Point2D delta = MathUtil.subtract(end2, end1);
                Point2D vector = MathUtil.normalize(delta, railDisplacement);
                vector = MathUtil.orthogonal(vector);

                Point2D ep1L = MathUtil.add(end1, vector);
                Point2D ep2L = MathUtil.add(end2, vector);
                g2.draw(new Line2D.Double(ep1L, ep2L));

                Point2D ep1R = MathUtil.subtract(end1, vector);
                Point2D ep2R = MathUtil.subtract(end2, vector);
                g2.draw(new Line2D.Double(ep1R, ep2R));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void drawUnconnected(Graphics2D g2
    ) {
        // TrackSegments are always connected
        // nothing to see here... move along...
    }

    protected void drawEditControls(Graphics2D g2) {
        g2.setColor(Color.black);
        if (showConstructionLinesLE()) {
            Point2D ep1 = layoutEditor.getCoords(getConnect1(), getType1());
            Point2D ep2 = layoutEditor.getCoords(getConnect2(), getType2());
            if (isCircle()) {
                // draw radiuses
                Point2D circleCenterPoint = getCoordsCenterCircle();
                g2.draw(new Line2D.Double(circleCenterPoint, ep1));
                g2.draw(new Line2D.Double(circleCenterPoint, ep2));
                // Draw a circle and square at the circles centre, that
                // allows the user to change the angle by dragging the mouse.
                g2.draw(layoutEditor.trackEditControlCircleAt(circleCenterPoint));
                g2.draw(layoutEditor.trackEditControlRectAt(circleCenterPoint));
            } else if (isBezier()) {
                //draw construction lines and control circles
                Point2D lastPt = ep1;
                for (Point2D bcp : bezierControlPoints) {
                    g2.draw(new Line2D.Double(lastPt, bcp));
                    lastPt = bcp;
                    g2.draw(layoutEditor.trackEditControlRectAt(bcp));
                }
                g2.draw(new Line2D.Double(lastPt, ep2));
            }
        }
        g2.draw(layoutEditor.trackEditControlCircleAt(getCentreSeg()));
    }   // drawEditControls

    protected void drawTurnoutControls(Graphics2D g2) {
        // TrackSegments don't have turnout controls...
        // nothing to see here... move along...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void drawDecorations(Graphics2D g2) {
        if (decorations != null) {
            // get end points and calculate start/stop angles (in radians)
            Point2D ep1 = layoutEditor.getCoords(getConnect1(), getType1());
            Point2D ep2 = layoutEditor.getCoords(getConnect2(), getType2());
            Point2D p1, p2, p3, p1P, p2P, p3P;
            double startAngleRAD, stopAngleRAD;
            if (isArc()) {
                calculateTrackSegmentAngle();
                double startAngleDEG = getStartAdj(), extentAngleDEG = getTmpAngle();
                startAngleRAD = Math.toRadians(startAngleDEG);
                stopAngleRAD = Math.toRadians(startAngleDEG + extentAngleDEG);
            } else if (isBezier()) {
                Point2D cp0 = bezierControlPoints.get(0);
                Point2D cpN = bezierControlPoints.get(bezierControlPoints.size() - 1);
                startAngleRAD = (Math.PI / 2.0) - MathUtil.computeAngleRAD(cp0, ep1);
                stopAngleRAD = (Math.PI / 2.0) - MathUtil.computeAngleRAD(ep2, cpN);
            } else {
                Point2D delta = MathUtil.subtract(ep2, ep1);
                startAngleRAD = (Math.PI / 2.0) - MathUtil.computeAngleRAD(delta);
                stopAngleRAD = startAngleRAD;
            }   // if isArc() {} else if isBezier() {} else...

            //
            // arrow decorations
            //
            String arrowValue = decorations.get("arrow");
            if (arrowValue != null) {
                if (getName().equals("T14")) {
                    log.debug("STOP!");
                }
                // <decoration name="arrow" value="double;both;width=1;length=12;gap=1" />
                boolean atStart = true, atStop = true;
                boolean hasIn = false, hasOut = false;
                int width = 1, length = 3, gap = 1, count = 1;
                Color color = defaultTrackColor;
                String[] values = arrowValue.split(";");
                for (int i = 0; i < values.length; i++) {
                    String value = values[i];
                    if (value.equals("single")) {
                        count = 1;
                    } else if (value.equals("double")) {
                        count = 2;
                    } else if (value.equals("triple")) {
                        count = 3;
                    } else if (value.startsWith("count=")) {
                        String valueString = value.substring(value.lastIndexOf("=") + 1);
                        count = Integer.parseInt(valueString);
                    } else if (value.equals("start")) {
                        atStop = false;
                    } else if (value.equals("stop")) {
                        atStart = false;
                    } else if (value.equals("in")) {
                        hasIn = true;
                    } else if (value.equals("out")) {
                        hasOut = true;
                    } else if (value.equals("both")) {
                        hasIn = true;
                        hasOut = true;
                    } else if (value.startsWith("color=")) {
                        String valueString = value.substring(value.lastIndexOf("=") + 1);
                        color = Color.decode(valueString);
                    } else if (value.startsWith("width=")) {
                        String valueString = value.substring(value.lastIndexOf("=") + 1);
                        width = Integer.parseInt(valueString);
                    } else if (value.startsWith("length=")) {
                        String valueString = value.substring(value.lastIndexOf("=") + 1);
                        length = Integer.parseInt(valueString);
                    } else if (value.startsWith("gap=")) {
                        String valueString = value.substring(value.lastIndexOf("=") + 1);
                        gap = Integer.parseInt(valueString);
                    }
                }
                hasIn |= !hasOut;   // if hasOut is false make hasIn true
                if (!atStart && !atStop) {   // if both false
                    atStart = true; // set both true
                    atStop = true;
                }

                g2.setStroke(new BasicStroke(width,
                        BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.F));
                g2.setColor(color);

                // draw the out arrows
                if (hasOut) {
                    if (atStart) {
                        p1 = new Point2D.Double(0.0, -length);
                        p2 = new Point2D.Double(-length, 0.0);
                        p3 = new Point2D.Double(0.0, +length);
                        Point2D delta = new Point2D.Double(gap + width, 0.0);
                        for (int idx = 0; idx < count; idx++) {
                            p1P = MathUtil.add(MathUtil.rotateRAD(p1, startAngleRAD), ep1);
                            p2P = MathUtil.add(MathUtil.rotateRAD(p2, startAngleRAD), ep1);
                            p3P = MathUtil.add(MathUtil.rotateRAD(p3, startAngleRAD), ep1);

                            GeneralPath path = new GeneralPath();
                            path.moveTo(p1P.getX(), p1P.getY());
                            path.lineTo(p2P.getX(), p2P.getY());
                            path.lineTo(p3P.getX(), p3P.getY());
                            g2.draw(path);

                            p1 = MathUtil.subtract(p1, delta);
                            p2 = MathUtil.subtract(p2, delta);
                            p3 = MathUtil.subtract(p3, delta);
                        }
                    }
                    if (atStop) {
                        p1 = new Point2D.Double(0.0, -length);
                        p2 = new Point2D.Double(+length, 0.0);
                        p3 = new Point2D.Double(0.0, +length);
                        Point2D delta = new Point2D.Double(gap + width, 0.0);
                        for (int idx = 0; idx < count; idx++) {
                            p1P = MathUtil.add(MathUtil.rotateRAD(p1, stopAngleRAD), ep2);
                            p2P = MathUtil.add(MathUtil.rotateRAD(p2, stopAngleRAD), ep2);
                            p3P = MathUtil.add(MathUtil.rotateRAD(p3, stopAngleRAD), ep2);

                            GeneralPath path = new GeneralPath();
                            path.moveTo(p1P.getX(), p1P.getY());
                            path.lineTo(p2P.getX(), p2P.getY());
                            path.lineTo(p3P.getX(), p3P.getY());
                            g2.draw(path);

                            p1 = MathUtil.add(p1, delta);
                            p2 = MathUtil.add(p2, delta);
                            p3 = MathUtil.add(p3, delta);
                        }
                    }
                }

                // draw the in arrows
                if (hasIn) {
                    double offset = (hasOut ? (length * count) : 0.0);
                    if (atStart) {
                        p1 = new Point2D.Double(-offset - length, -length);
                        p2 = new Point2D.Double(-offset, 0.0);
                        p3 = new Point2D.Double(-offset - length, +length);
                        Point2D delta = new Point2D.Double(gap + width, 0.0);
                        for (int idx = 0; idx < count; idx++) {
                            p1P = MathUtil.add(MathUtil.rotateRAD(p1, startAngleRAD), ep1);
                            p2P = MathUtil.add(MathUtil.rotateRAD(p2, startAngleRAD), ep1);
                            p3P = MathUtil.add(MathUtil.rotateRAD(p3, startAngleRAD), ep1);

                            GeneralPath path = new GeneralPath();
                            path.moveTo(p1P.getX(), p1P.getY());
                            path.lineTo(p2P.getX(), p2P.getY());
                            path.lineTo(p3P.getX(), p3P.getY());
                            g2.draw(path);

                            p1 = MathUtil.subtract(p1, delta);
                            p2 = MathUtil.subtract(p2, delta);
                            p3 = MathUtil.subtract(p3, delta);
                        }
                    }
                    if (atStop) {
                        p1 = new Point2D.Double(offset + length, -length);
                        p2 = new Point2D.Double(offset, 0.0);
                        p3 = new Point2D.Double(offset + length, +length);
                        Point2D delta = new Point2D.Double(gap + width, 0.0);
                        for (int idx = 0; idx < count; idx++) {
                            p1P = MathUtil.add(MathUtil.rotateRAD(p1, stopAngleRAD), ep2);
                            p2P = MathUtil.add(MathUtil.rotateRAD(p2, stopAngleRAD), ep2);
                            p3P = MathUtil.add(MathUtil.rotateRAD(p3, stopAngleRAD), ep2);

                            GeneralPath path = new GeneralPath();
                            path.moveTo(p1P.getX(), p1P.getY());
                            path.lineTo(p2P.getX(), p2P.getY());
                            path.lineTo(p3P.getX(), p3P.getY());
                            g2.draw(path);

                            p1 = MathUtil.add(p1, delta);
                            p2 = MathUtil.add(p2, delta);
                            p3 = MathUtil.add(p3, delta);
                        }
                    }
                }   // if hasIn
            }   // arrow decoration

            //
            //  bridge decorations
            //
            String bridgeValue = decorations.get("bridge");
            if (bridgeValue != null) {
                // <decoration name="bridge" value="both;linewidth=2;halfgap=8" />
                // right/left default true; in/out default false
                boolean hasLeft = true, hasRight = true, hasIn = false, hasOut = false;
                int linelength = 4, linewidth = 1, halfgap = 2;
                Color color = defaultTrackColor;
                String[] values = bridgeValue.split(";");
                for (int i = 0; i < values.length; i++) {
                    String value = values[i];
                    log.info("value[{}]: \"{}\"", i, value);
                    if (value.equals("left")) {
                        hasRight = false;
                    } else if (value.equals("right")) {
                        hasLeft = false;
                    } else if (value.equals("in")) {
                        hasIn = true;
                    } else if (value.equals("out")) {
                        hasOut = true;
                    } else if (value.equals("both")) {
                        hasIn = true;
                        hasOut = true;
                    } else if (value.startsWith("color=")) {
                        String valueString = value.substring(value.lastIndexOf("=") + 1);
                        color = Color.decode(valueString);
                    } else if (value.startsWith("linelength=")) {
                        String valueString = value.substring(value.lastIndexOf("=") + 1);
                        linelength = Integer.parseInt(valueString);
                    } else if (value.startsWith("linewidth=")) {
                        String valueString = value.substring(value.lastIndexOf("=") + 1);
                        linewidth = Integer.parseInt(valueString);
                    } else if (value.startsWith("halfgap=")) {
                        String valueString = value.substring(value.lastIndexOf("=") + 1);
                        halfgap = Integer.parseInt(valueString);
                    }
                }
                // these both can't be false
                if (!hasLeft && !hasRight) {
                    hasLeft = true;
                    hasRight = true;
                }

                g2.setStroke(new BasicStroke(linewidth,
                        BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.F));
                g2.setColor(color);

                if (isArc()) {
                    calculateTrackSegmentAngle();
                    Rectangle2D cRectangle2D = new Rectangle2D.Double(
                            getCX(), getCY(), getCW(), getCH());
                    double startAngleDEG = getStartAdj(), extentAngleDEG = getTmpAngle();
                    if (hasLeft) {
                        Rectangle2D cLeftRectangle2D = MathUtil.inset(cRectangle2D, -halfgap);
                        g2.draw(new Arc2D.Double(
                                cLeftRectangle2D.getX(),
                                cLeftRectangle2D.getY(),
                                cLeftRectangle2D.getWidth(),
                                cLeftRectangle2D.getHeight(),
                                startAngleDEG, extentAngleDEG, Arc2D.OPEN));
                    }
                    if (hasRight) {
                        Rectangle2D cLRightRectangle2D = MathUtil.inset(cRectangle2D, +halfgap);
                        g2.draw(new Arc2D.Double(
                                cLRightRectangle2D.getX(),
                                cLRightRectangle2D.getY(),
                                cLRightRectangle2D.getWidth(),
                                cLRightRectangle2D.getHeight(),
                                startAngleDEG, extentAngleDEG, Arc2D.OPEN));
                    }
                    trackRedrawn();
                } else if (isBezier()) {
                    int cnt = bezierControlPoints.size() + 2;
                    Point2D[] points = new Point2D[cnt];
                    points[0] = ep1;
                    for (int idx = 0; idx < cnt - 2; idx++) {
                        points[idx + 1] = bezierControlPoints.get(idx);
                    }
                    points[cnt - 1] = ep2;

                    if (hasLeft) {
                        MathUtil.drawBezier(g2, points, -halfgap);
                    }
                    if (hasRight) {
                        MathUtil.drawBezier(g2, points, +halfgap);
                    }
                } else {
                    Point2D delta = MathUtil.subtract(ep2, ep1);
                    Point2D vector = MathUtil.normalize(delta, halfgap);
                    vector = MathUtil.orthogonal(vector);

                    if (hasLeft) {
                        Point2D ep1L = MathUtil.add(ep1, vector);
                        Point2D ep2L = MathUtil.add(ep2, vector);
                        g2.draw(new Line2D.Double(ep1L, ep2L));
                    }
                    if (hasRight) {
                        Point2D ep1R = MathUtil.subtract(ep1, vector);
                        Point2D ep2R = MathUtil.subtract(ep2, vector);
                        g2.draw(new Line2D.Double(ep1R, ep2R));
                    }
                }   // if isArc() {} else if isBezier() {} else...

                if (hasIn) {
                    if (hasRight) {
                        p1 = new Point2D.Double(-linelength, -linelength - halfgap);
                        p2 = new Point2D.Double(0.0, -halfgap);
                        p1P = MathUtil.add(MathUtil.rotateRAD(p1, startAngleRAD), ep1);
                        p2P = MathUtil.add(MathUtil.rotateRAD(p2, startAngleRAD), ep1);
                        g2.draw(new Line2D.Double(p1P, p2P));
                    }
                    if (hasLeft) {
                        p1 = new Point2D.Double(-linelength, +linelength + halfgap);
                        p2 = new Point2D.Double(0.0, +halfgap);
                        p1P = MathUtil.add(MathUtil.rotateRAD(p1, startAngleRAD), ep1);
                        p2P = MathUtil.add(MathUtil.rotateRAD(p2, startAngleRAD), ep1);
                        g2.draw(new Line2D.Double(p1P, p2P));
                    }
                }
                if (hasOut) {
                    if (hasRight) {
                        p1 = new Point2D.Double(+linelength, -linelength - halfgap);
                        p2 = new Point2D.Double(0.0, -halfgap);
                        p1P = MathUtil.add(MathUtil.rotateRAD(p1, stopAngleRAD), ep2);
                        p2P = MathUtil.add(MathUtil.rotateRAD(p2, stopAngleRAD), ep2);
                        g2.draw(new Line2D.Double(p1P, p2P));
                    }
                    if (hasLeft) {
                        p1 = new Point2D.Double(+linelength, +linelength + halfgap);
                        p2 = new Point2D.Double(0.0, +halfgap);
                        p1P = MathUtil.add(MathUtil.rotateRAD(p1, stopAngleRAD), ep2);
                        p2P = MathUtil.add(MathUtil.rotateRAD(p2, stopAngleRAD), ep2);
                        g2.draw(new Line2D.Double(p1P, p2P));
                    }
                }
            }   // if (bridgeValue != null)

            //
            //  bumper decorations
            //
            String bumperValue = decorations.get("bumper");
            if (bumperValue != null) {
                // <decoration name="bumper" value="double;width=2;length=6;gap=2;flipped" />
                int count = 1, width = 1, length = 4, gap = 1;
                boolean isFlipped = false, hasIn = false, hasOut = false;
                Color color = defaultTrackColor;
                String[] values = bumperValue.split(";");
                for (int i = 0; i < values.length; i++) {
                    String value = values[i];
                    log.info("value[{}]: \"{}\"", i, value);
                    if (value.equals("single")) {
                        count = 1;
                    } else if (value.equals("double")) {
                        count = 2;
                    } else if (value.equals("triple")) {
                        count = 3;
                    } else if (value.equals("flip")) {
                        isFlipped = true;
                    } else if (value.startsWith("count=")) {
                        String valueString = value.substring(value.lastIndexOf("=") + 1);
                        count = Integer.parseInt(valueString);
                    } else if (value.equals("in")) {
                        hasIn = true;
                    } else if (value.equals("out")) {
                        hasOut = true;
                    } else if (value.equals("both")) {
                        hasIn = true;
                        hasOut = true;
                    } else if (value.startsWith("color=")) {
                        String valueString = value.substring(value.lastIndexOf("=") + 1);
                        color = Color.decode(valueString);
                    } else if (value.startsWith("width=")) {
                        String valueString = value.substring(value.lastIndexOf("=") + 1);
                        width = Integer.parseInt(valueString);
                    } else if (value.startsWith("length=")) {
                        String valueString = value.substring(value.lastIndexOf("=") + 1);
                        length = Integer.parseInt(valueString);
                    } else if (value.startsWith("gap=")) {
                        String valueString = value.substring(value.lastIndexOf("=") + 1);
                        gap = Integer.parseInt(valueString);
                    }
                }
                hasIn |= !hasOut;   // if hasOut is false make hasIn true
                g2.setStroke(new BasicStroke((float) width,
                        BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.F));
                g2.setColor(color);

                int halfLength = length / 2;

                if (isFlipped) {
                    startAngleRAD += Math.PI;
                    stopAngleRAD += Math.PI;
                }

                // draw cross ties
                if (hasIn) {
                    p1 = new Point2D.Double(0.0, -halfLength);
                    p2 = new Point2D.Double(0.0, +halfLength);
                    for (int idx = 0; idx < count; idx++) {
                        p1P = MathUtil.add(MathUtil.rotateRAD(p1, startAngleRAD), ep1);
                        p2P = MathUtil.add(MathUtil.rotateRAD(p2, startAngleRAD), ep1);

                        g2.draw(new Line2D.Double(p1P, p2P));

                        Point2D delta = new Point2D.Double(gap + width, 0.0);
                        p1 = MathUtil.subtract(p1, delta);
                        p2 = MathUtil.subtract(p2, delta);
                    }
                }
                if (hasOut) {
                    p1 = new Point2D.Double(0.0, -halfLength);
                    p2 = new Point2D.Double(0.0, +halfLength);
                    for (int idx = 0; idx < count; idx++) {
                        p1P = MathUtil.add(MathUtil.rotateRAD(p1, startAngleRAD), ep2);
                        p2P = MathUtil.add(MathUtil.rotateRAD(p2, startAngleRAD), ep2);

                        g2.draw(new Line2D.Double(p1P, p2P));

                        Point2D delta = new Point2D.Double(gap + width, 0.0);
                        p1 = MathUtil.subtract(p1, delta);
                        p2 = MathUtil.subtract(p2, delta);
                    }
                }
            }
        }   // if (decorations != null)
    }   // drawDecorations

    /*
     * {@inheritDoc}
     */
    @Override
    public void reCheckBlockBoundary() {
        // nothing to see here... move along...
    }

    /*
     * {@inheritDoc}
     */
    @Override
    protected List<LayoutConnectivity> getLayoutConnectivity() {
        List<LayoutConnectivity> results = new ArrayList<>();

        LayoutConnectivity lc = null;
        LayoutBlock lb1 = getLayoutBlock(), lb2 = null;
        // ensure that block is assigned
        if (lb1 != null) {
            // check first connection for turnout or level crossing
            if ((type1 >= TURNOUT_A) && (type1 <= LEVEL_XING_D)) {
                // have connection to turnout or level crossing
                if (type1 <= TURNOUT_D) {
                    // have connection to a turnout, is block different
                    LayoutTurnout lt = (LayoutTurnout) getConnect1();
                    lb2 = lt.getLayoutBlock();
                    if (lt.getTurnoutType() > LayoutTurnout.WYE_TURNOUT) {
                        // not RH, LH, or WYE turnout - other blocks possible
                        if ((type1 == TURNOUT_B) && (lt.getLayoutBlockB() != null)) {
                            lb2 = lt.getLayoutBlockB();
                        }
                        if ((type1 == TURNOUT_C) && (lt.getLayoutBlockC() != null)) {
                            lb2 = lt.getLayoutBlockC();
                        }
                        if ((type1 == TURNOUT_D) && (lt.getLayoutBlockD() != null)) {
                            lb2 = lt.getLayoutBlockD();
                        }
                    }
                    if ((lb2 != null) && (lb1 != lb2)) {
                        // have a block boundary, create a LayoutConnectivity
                        log.debug("Block boundary  ('{}'<->'{}') found at {}", lb1, lb2, this);
                        lc = new LayoutConnectivity(lb1, lb2);
                        lc.setConnections(this, lt, type1, null);
                        lc.setDirection(Path.computeDirection(
                                layoutEditor.getCoords(getConnect2(), type2),
                                layoutEditor.getCoords(getConnect1(), type1)));
                        results.add(lc);
                    }
                } else {
                    // have connection to a level crossing
                    LevelXing lx = (LevelXing) getConnect1();
                    if ((type1 == LEVEL_XING_A) || (type1 == LEVEL_XING_C)) {
                        lb2 = lx.getLayoutBlockAC();
                    } else {
                        lb2 = lx.getLayoutBlockBD();
                    }
                    if ((lb2 != null) && (lb1 != lb2)) {
                        // have a block boundary, create a LayoutConnectivity
                        log.debug("Block boundary  ('{}'<->'{}') found at {}", lb1, lb2, this);
                        lc = new LayoutConnectivity(lb1, lb2);
                        lc.setConnections(this, lx, type1, null);
                        lc.setDirection(Path.computeDirection(
                                layoutEditor.getCoords(getConnect2(), type2),
                                layoutEditor.getCoords(getConnect1(), type1)));
                        results.add(lc);
                    }
                }
            } else if ((type1 >= SLIP_A) && (type1 <= SLIP_D)) {
                // have connection to a slip crossing
                LayoutSlip ls = (LayoutSlip) getConnect1();
                lb2 = ls.getLayoutBlock();
                if ((lb2 != null) && (lb1 != lb2)) {
                    // have a block boundary, create a LayoutConnectivity
                    log.debug("Block boundary  ('{}'<->'{}') found at {}", lb1, lb2, this);
                    lc = new LayoutConnectivity(lb1, lb2);
                    lc.setConnections(this, ls, type1, null);
                    lc.setDirection(Path.computeDirection(layoutEditor.getCoords(getConnect2(),
                            type2), layoutEditor.getCoords(getConnect1(), type1)));
                    results.add(lc);
                }
            }
            // check second connection for turnout or level crossing
            if ((type2 >= TURNOUT_A) && (type2 <= LEVEL_XING_D)) {
                // have connection to turnout or level crossing
                if (type2 <= TURNOUT_D) {
                    // have connection to a turnout
                    LayoutTurnout lt = (LayoutTurnout) getConnect2();
                    lb2 = lt.getLayoutBlock();
                    if (lt.getTurnoutType() > LayoutTurnout.WYE_TURNOUT) {
                        // not RH, LH, or WYE turnout - other blocks possible
                        if ((type2 == TURNOUT_B) && (lt.getLayoutBlockB() != null)) {
                            lb2 = lt.getLayoutBlockB();
                        }
                        if ((type2 == TURNOUT_C) && (lt.getLayoutBlockC() != null)) {
                            lb2 = lt.getLayoutBlockC();
                        }
                        if ((type2 == TURNOUT_D) && (lt.getLayoutBlockD() != null)) {
                            lb2 = lt.getLayoutBlockD();
                        }
                    }
                    if ((lb2 != null) && (lb1 != lb2)) {
                        // have a block boundary, create a LayoutConnectivity
                        log.debug("Block boundary  ('{}'<->'{}') found at {}", lb1, lb2, this);
                        lc = new LayoutConnectivity(lb1, lb2);
                        lc.setConnections(this, lt, type2, null);
                        lc.setDirection(Path.computeDirection(
                                layoutEditor.getCoords(getConnect1(), type1),
                                layoutEditor.getCoords(getConnect2(), type2)));
                        results.add(lc);
                    }
                } else {
                    // have connection to a level crossing
                    LevelXing lx = (LevelXing) getConnect2();
                    if ((type2 == LEVEL_XING_A) || (type2 == LEVEL_XING_C)) {
                        lb2 = lx.getLayoutBlockAC();
                    } else {
                        lb2 = lx.getLayoutBlockBD();
                    }
                    if ((lb2 != null) && (lb1 != lb2)) {
                        // have a block boundary, create a LayoutConnectivity
                        log.debug("Block boundary  ('{}'<->'{}') found at {}", lb1, lb2, this);
                        lc = new LayoutConnectivity(lb1, lb2);
                        lc.setConnections(this, lx, type2, null);
                        lc.setDirection(Path.computeDirection(
                                layoutEditor.getCoords(getConnect1(), type1),
                                layoutEditor.getCoords(getConnect2(), type2)));
                        results.add(lc);
                    }
                }
            } else if ((type2 >= SLIP_A) && (type2 <= SLIP_D)) {
                // have connection to a slip crossing
                LayoutSlip ls = (LayoutSlip) getConnect2();
                lb2 = ls.getLayoutBlock();
                if ((lb2 != null) && (lb1 != lb2)) {
                    // have a block boundary, create a LayoutConnectivity
                    log.debug("Block boundary  ('{}'<->'{}') found at {}", lb1, lb2, this);
                    lc = new LayoutConnectivity(lb1, lb2);
                    lc.setConnections(this, ls, type2, null);
                    lc.setDirection(Path.computeDirection(
                            layoutEditor.getCoords(getConnect1(), type1),
                            layoutEditor.getCoords(getConnect2(), type2)));
                    results.add(lc);
                }
            } else {
                // this is routinely reached in normal operations
                // (nothing to see here... move along)
            }
        }   // if (lb1 != null)
        return results;
    }   // getLayoutConnectivity()

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> checkForFreeConnections() {
        List<Integer> result = new ArrayList<>();
        // Track Segments always have all their connections so...
        // (nothing to see here... move along)
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean checkForUnAssignedBlocks() {
        return (getLayoutBlock() != null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void checkForNonContiguousBlocks(
            @Nonnull HashMap<String, List<Set<String>>> blockNamesToTrackNameSetsMap) {
        /*
         * For each (non-null) blocks of this track do:
         * #1) If it's got an entry in the blockNamesToTrackNameSetMap then
         * #2) If this track is already in the TrackNameSet for this block
         *     then return (done!)
         * #3) else add a new set (with this block/track) to
         *     blockNamesToTrackNameSetMap and
         * #4) collect all the connections in this block
         * <p>
         *     Basically, we're maintaining contiguous track sets for each block found
         *     (in blockNamesToTrackNameSetMap)
         */
        List<Set<String>> TrackNameSets = null;
        Set<String> TrackNameSet = null;
        if (blockName != null) {
            TrackNameSet = null;    // assume not found (pessimist!)
            TrackNameSets = blockNamesToTrackNameSetsMap.get(blockName);
            if (TrackNameSets != null) { // (#1)
                for (Set<String> checkTrackNameSet : TrackNameSets) {
                    if (checkTrackNameSet.contains(getName())) { // (#2)
                        TrackNameSet = checkTrackNameSet;
                        break;
                    }
                }
            } else {    // (#3)
                log.debug("*New block ('{}') trackNameSets", blockName);
                TrackNameSets = new ArrayList<>();
                blockNamesToTrackNameSetsMap.put(blockName, TrackNameSets);
            }
            if (TrackNameSet == null) {
                TrackNameSet = new LinkedHashSet<>();
                TrackNameSets.add(TrackNameSet);
            }
            if (TrackNameSet.add(getName())) {
                log.debug("*    Add track '{}' to TrackNameSets for block '{}'", getName(), blockName);
            }
            // (#4)
            if (connect1 != null) {
                connect1.collectContiguousTracksNamesInBlockNamed(blockName, TrackNameSet);
            }
            if (connect2 != null) { // (#4)
                connect2.collectContiguousTracksNamesInBlockNamed(blockName, TrackNameSet);
            }
        }
    }   // collectContiguousTracksNamesInBlockNamed

    /**
     * {@inheritDoc}
     */
    public void collectContiguousTracksNamesInBlockNamed(@Nonnull String blockName,
            @Nonnull Set<String> TrackNameSet) {
        if (!TrackNameSet.contains(getName())) {
            // is this the blockName we're looking for?
            if (this.blockName.equals(blockName)) {
                // if we are added to the TrackNameSet
                if (TrackNameSet.add(getName())) {
                    log.debug("*    Add track '{}'for block '{}'", getName(), blockName);
                }
                // these should never be null... but just in case...
                // it's time to play... flood your neighbours!
                if (connect1 != null) {
                    connect1.collectContiguousTracksNamesInBlockNamed(blockName, TrackNameSet);
                }
                if (connect2 != null) {
                    connect2.collectContiguousTracksNamesInBlockNamed(blockName, TrackNameSet);
                }
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public void setAllLayoutBlocks(LayoutBlock layoutBlock) {
        setLayoutBlock(layoutBlock);
    }

    private final static Logger log
            = LoggerFactory.getLogger(TrackSegment.class);
}
