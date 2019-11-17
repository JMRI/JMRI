package jmri.jmrit.display.layoutEditor;

import static java.lang.Float.POSITIVE_INFINITY;
import static jmri.jmrit.display.layoutEditor.LayoutTrack.TRACK;
import static jmri.jmrit.display.layoutEditor.PositionablePoint.EDGE_CONNECTOR;
import static jmri.jmrit.display.layoutEditor.PositionablePoint.END_BUMPER;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
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
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.Path;
import jmri.jmrit.display.layoutEditor.blockRoutingTable.LayoutBlockRouteTableAction;
import jmri.util.ColorUtil;
import jmri.util.FileUtil;
import jmri.util.MathUtil;
import jmri.util.QuickPromptUtil;
import jmri.util.swing.JmriColorChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TrackSegment is a segment of track on a layout linking two nodes of the
 * layout. A node may be a LayoutTurnout, a LevelXing or a PositionablePoint.
 * <p>
 * PositionablePoints have 1 or 2 connection points. LayoutTurnouts have 3 or 4
 * (crossovers) connection points, designated A, B, C, and D. LevelXing's have 4
 * connection points, designated A, B, C, and D.
 * <p>
 * TrackSegments carry the connectivity information between the three types of
 * nodes. Track Segments serve as the lines in a graph which shows layout
 * connectivity. For the connectivity graph to be valid, all connections between
 * nodes must be via TrackSegments.
 * <p>
 * TrackSegments carry Block information, as do LayoutTurnouts and LevelXings.
 * <p>
 * TrackSegments may be drawn as dashed lines or solid lines. In addition
 * TrackSegments may be hidden when the panel is not in EditMode.
 *
 * @author Dave Duchamp Copyright (p) 2004-2009
 * @author George Warner Copyright (c) 2017-2019
 */
public class TrackSegment extends LayoutTrack {

    // defined constants
    // operational instance variables (not saved between sessions)
    private NamedBeanHandle<LayoutBlock> namedLayoutBlock = null;

    // persistent instances variables (saved between sessions)
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
            @CheckForNull LayoutTrack c1, int t1,
            @CheckForNull LayoutTrack c2, int t2,
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
        setupDefaultBumperSizes(layoutEditor);
    }

    // alternate constructor for loading layout editor panels
    public TrackSegment(@Nonnull String id,
            @CheckForNull String c1Name, int t1,
            @CheckForNull String c2Name, int t2,
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

        setupDefaultBumperSizes(layoutEditor);
    }

    /**
     * Get debugging string for the TrackSegment.
     *
     * @return text showing id and connections of this segment
     */
    @Override
    public String toString() {
        return "TrackSegment " + getName()
                + " c1:{" + getConnect1Name() + " (" + type1 + ")},"
                + " c2:{" + getConnect2Name() + " (" + type2 + ")}";

    }

    /*
     * Accessor methods
     */
    @Nonnull
    public String getBlockName() {
        String result = null;
        if (namedLayoutBlock != null) {
            result = namedLayoutBlock.getName();
        }
        return ((result == null) ? "" : result);
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
     * @param connectTrack   the track we want to connect to
     * @param connectionType where on that track we want to be connected
     */
    protected void setNewConnect1(@CheckForNull LayoutTrack connectTrack, int connectionType) {
        connect1 = connectTrack;
        type1 = connectionType;
    }

    /**
     * set a new connection 2
     *
     * @param connectTrack   the track we want to connect to
     * @param connectionType where on that track we want to be connected
     */
    protected void setNewConnect2(@CheckForNull LayoutTrack connectTrack, int connectionType) {
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
    public boolean replaceTrackConnection(@CheckForNull LayoutTrack oldTrack, @CheckForNull LayoutTrack newTrack, int newType) {
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
                    log.error("Attempt to remove invalid track connection");
                    result = false;
                }
            } else {
                log.error("Can't replace null track connection with null");
                result = false;
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
                log.error("Attempt to replace invalid track connection");
                result = false;
            }
        }
        return result;
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
            layoutEditor.setDirty();
        }
    }

    /**
     * @return true if track segment is a main line
     */
    @Override
    public boolean isMainline() {
        return mainline;
    }

    public void setMainline(boolean main) {
        if (mainline != main) {
            mainline = main;
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
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
                hideConstructionLines(SHOWCON);
            }
            changed = true;
        }
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
                hideConstructionLines(SHOWCON);
            }
            changed = true;
        }
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
            hideConstructionLines(SHOWCON);
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
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
                hideConstructionLines(SHOWCON);
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
     * Get the direction from end point 1 to 2
     * <p>
     * Note: Goes CW from east (0) to south (PI/2) to west (PI) to north
     * (PI*3/2), etc.
     *
     * @return the direction (in radians)
     */
    public double getDirectionRAD() {
        Point2D ep1 = center, ep2 = center;
        if (connect1 != null) {
            ep1 = LayoutEditor.getCoords(connect1, getType1());
        }
        if (connect2 != null) {
            ep2 = LayoutEditor.getCoords(connect2, getType2());
        }
        return (Math.PI / 2.D) - MathUtil.computeAngleRAD(ep1, ep2);
    }

    /**
     * Get the direction from end point 1 to 2
     * <p>
     * Note: Goes CW from east (0) to south (90) to west (180) to north (270),
     * etc.
     *
     * @return the direction (in degrees)
     */
    public double getDirectionDEG() {
        return Math.toDegrees(getDirectionRAD());
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
        return (namedLayoutBlock != null) ? namedLayoutBlock.getBean() : null;
    }

    public String getConnect1Name() {
        return getConnectName(connect1, type1);
    }

    public String getConnect2Name() {
        return getConnectName(connect2, type2);
    }

    private String getConnectName(@CheckForNull LayoutTrack layoutTrack, int type) {
        return (layoutTrack == null) ? null : layoutTrack.getName();
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation returns null because {@link #getConnect1} and
     * {@link #getConnect2} should be used instead.
     */
    // only implemented here to suppress "does not override abstract method " error in compiler
    @Override
    public LayoutTrack getConnection(int connectionType) throws jmri.JmriException {
        // nothing to see here, move along
        throw new jmri.JmriException("Use getConnect1() or getConnect2() instead.");
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation does nothing because {@link #setNewConnect1} and
     * {@link #setNewConnect2} should be used instead.
     */
    // only implemented here to suppress "does not override abstract method " error in compiler
    @Override
    public void setConnection(int connectionType, @CheckForNull LayoutTrack o, int type) throws jmri.JmriException {
        // nothing to see here, move along
        throw new jmri.JmriException("Use setConnect1() or setConnect2() instead.");
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

    public void setBezierControlPoint(@CheckForNull Point2D p, int index) {
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
     * Set up a Layout Block for a Track Segment.
     */
    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "Null is accepted as a valid value")
    public void setLayoutBlock(@CheckForNull LayoutBlock newLayoutBlock) {
        LayoutBlock layoutBlock = getLayoutBlock();
        if (layoutBlock != newLayoutBlock) {
            // block has changed, if old block exists, decrement use
            if (layoutBlock != null) {
                layoutBlock.decrementUse();
            }
            if (newLayoutBlock != null) {
                namedLayoutBlock = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(newLayoutBlock.getUserName(), newLayoutBlock);
            } else {
                namedLayoutBlock = null;
            }
        }
    }

    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "Null is accepted as a valid value")
    public void setLayoutBlockByName(@CheckForNull String name) {
        if ((name != null) && !name.isEmpty()) {
            LayoutBlock b = layoutEditor.provideLayoutBlock(name);
            namedLayoutBlock = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(b.getUserName(), b);
        } else {
            namedLayoutBlock = null;
        }
    }

    /*
     * non-accessor methods
     */
    /**
     * Scale this LayoutTrack's coordinates by the x and y factors
     *
     * @param xFactor the amount to scale X coordinates
     * @param yFactor the amount to scale Y coordinates
     */
    @Override
    public void scaleCoords(float xFactor, float yFactor) {
        Point2D factor = new Point2D.Float(xFactor, yFactor);
        center = MathUtil.multiply(center, factor);
        if (isBezier()) {
            for (Point2D p : bezierControlPoints) {
                p.setLocation(MathUtil.multiply(p, factor));
            }
        }
    }

    /**
     * Translate (2D Move) this LayoutTrack's coordinates by the x and y factors
     *
     * @param xFactor the amount to translate X coordinates
     * @param yFactor the amount to translate Y coordinates
     */
    @Override
    public void translateCoords(float xFactor, float yFactor) {
        setCoordsCenter(MathUtil.add(center, new Point2D.Float(xFactor, yFactor)));
    }

    /**
     * set center coordinates
     *
     * @param newCenterPoint the coordinates to set
     */
    @Override
    public void setCoordsCenter(@Nonnull Point2D newCenterPoint) {
        if (center != newCenterPoint) {
            if (isBezier()) {
                Point2D delta = MathUtil.subtract(newCenterPoint, center);
                for (Point2D p : bezierControlPoints) {
                    p.setLocation(MathUtil.add(p, delta));
                }
            }
            center = newCenterPoint;
        }
    }

    // initialization instance variables (used when loading a LayoutEditor)
    public String tConnect1Name = "";
    public String tConnect2Name = "";

    public String tLayoutBlockName = "";

    /**
     * Initialization method. The above variables are initialized by
     * PositionablePointXml, then the following method is called after the
     * entire LayoutEditor is loaded to set the specific TrackSegment objects.
     */
    @SuppressWarnings("deprecation")
    //NOTE: findObjectByTypeAndName is @Deprecated;
    // we're using it here for backwards compatibility until it can be removed
    @Override
    @SuppressFBWarnings(value = "NP_NULL_ON_SOME_PATH_FROM_RETURN_VALUE", justification = "Null check performed before using return value")
    public void setObjects(LayoutEditor p) {

        LayoutBlock lb;
        if (!tLayoutBlockName.isEmpty()) {
            lb = p.provideLayoutBlock(tLayoutBlockName);
            if (lb != null) {
                namedLayoutBlock = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(lb.getUserName(), lb);
                lb.incrementUse();
            } else {
                log.error("bad blockname '{}' in tracksegment {}", tLayoutBlockName, getName());
                namedLayoutBlock = null;
            }
            tLayoutBlockName = null; //release this memory
        }

        //NOTE: testing "type-less" connects
        // (read comments for findObjectByName in LayoutEditorFindItems.java)
        connect1 = p.getFinder().findObjectByName(tConnect1Name);
        if (null == connect1) { // findObjectByName failed... try findObjectByTypeAndName
            log.warn("Unknown connect1 object prefix: '{}' of type {}.", tConnect1Name, type1);
            connect1 = p.getFinder().findObjectByTypeAndName(type1, tConnect1Name);
        }
        connect2 = p.getFinder().findObjectByName(tConnect2Name);
        if (null == connect2) { // findObjectByName failed; try findObjectByTypeAndName
            log.warn("Unknown connect2 object prefix: '{}' of type {}.", tConnect2Name, type2);
            connect2 = p.getFinder().findObjectByTypeAndName(type2, tConnect2Name);
        }
    }

    protected void updateBlockInfo() {
        LayoutBlock layoutBlock = getLayoutBlock();
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
        LayoutBlock result = null;
        if (connect != null) {
            if (type == POS_POINT) {
                PositionablePoint p = (PositionablePoint) connect;
                if (p.getConnect1() != this) {
                    if (p.getConnect1() != null) {
                        result = p.getConnect1().getLayoutBlock();
                    }
                } else {
                    if (p.getConnect2() != null) {
                        result = p.getConnect2().getLayoutBlock();
                    }
                }
            } else {
                result = layoutEditor.getAffectedBlock(connect, type);
            }
        }
        return result;
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
            Rectangle2D r = layoutEditor.trackControlCircleRectAt(hitPoint);
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
    @Override
    public Point2D getCoordsForConnectionType(int connectionType) {
        Point2D result = getCentreSeg();
        if (connectionType == TRACK_CIRCLE_CENTRE) {
            result = getCoordsCenterCircle();
        } else if (LayoutTrack.isBezierHitType(connectionType)) {
            result = getBezierControlPoint(connectionType - BEZIER_CONTROL_POINT_OFFSET_MIN);
        }
        return result;
    }

    /**
     * @return the bounds of this track segment
     */
    @Override
    public Rectangle2D getBounds() {
        Rectangle2D result;

        Point2D ep1 = center, ep2 = center;
        if (getConnect1() != null) {
            ep1 = LayoutEditor.getCoords(getConnect1(), getType1());
        }
        if (getConnect2() != null) {
            ep2 = LayoutEditor.getCoords(getConnect2(), getType2());
        }

        result = new Rectangle2D.Double(ep1.getX(), ep1.getY(), 0, 0);
        result.add(ep2);

        return result;
    }

    private JPopupMenu popupMenu = null;
    private JCheckBoxMenuItem mainlineCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("MainlineCheckBoxMenuItemTitle"));
    private JCheckBoxMenuItem hiddenCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("HiddenCheckBoxMenuItemTitle"));
    private JCheckBoxMenuItem dashedCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("DashedCheckBoxMenuItemTitle"));
    private JCheckBoxMenuItem flippedCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("FlippedCheckBoxMenuItemTitle"));

    /**
     * Maximum length of the bumper decoration.
     */
    public static final int MAX_BUMPER_LENGTH = 200;
    public static final int MAX_BUMPER_WIDTH = 200;

    private static final int MAX_ARROW_LINE_WIDTH = 200;
    private static final int MAX_ARROW_LENGTH = 200;
    private static final int MAX_ARROW_GAP = 200;
    private static final int MAX_BRIDGE_LINE_WIDTH = 200;
    private static final int MAX_BRIDGE_APPROACH_WIDTH = 200;
    private static final int MAX_BRIDGE_DECK_WIDTH = 200;
    private static final int MAX_BUMPER_LINE_WIDTH = 200;
    private static final int MAX_TUNNEL_FLOOR_WIDTH = 200;
    private static final int MAX_TUNNEL_LINE_WIDTH = 200;
    private static final int MAX_TUNNEL_ENTRANCE_WIDTH = 200;

    /**
     * Helper method, which adds "Set value" item to the menu. The value can be
     * optionally range-checked. Item will be appended at the end of the menu.
     *
     * @param menu       the target menu.
     * @param titleKey   bundle key for the menu title/dialog title
     * @param toolTipKey bundle key for the menu item tooltip
     * @param val        value getter
     * @param set        value setter
     * @param predicate  checking predicate, possibly null.
     */
    private void addNumericMenuItem(@Nonnull JMenu menu,
            @Nonnull String titleKey, @Nonnull String toolTipKey,
            @Nonnull Supplier<Integer> val,
            @Nonnull Consumer<Integer> set,
            @CheckForNull Predicate<Integer> predicate) {
        int oldVal = val.get();
        JMenuItem jmi = menu.add(new JMenuItem(Bundle.getMessage("MakeLabel",
                Bundle.getMessage(titleKey)) + oldVal));
        jmi.setToolTipText(Bundle.getMessage(toolTipKey));
        jmi.addActionListener((java.awt.event.ActionEvent e3) -> {
            //prompt for lineWidth
            int newValue = QuickPromptUtil.promptForInteger(layoutEditor,
                    Bundle.getMessage(titleKey),
                    Bundle.getMessage(titleKey),
                    // getting again, maybe something changed from the menu construction ?
                    val.get(), predicate);
            set.accept(newValue);
            layoutEditor.repaint();
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    protected JPopupMenu showPopup(@Nonnull MouseEvent mouseEvent) {
        if (popupMenu != null) {
            popupMenu.removeAll();
        } else {
            popupMenu = new JPopupMenu();
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

        JMenuItem jmi = popupMenu.add(Bundle.getMessage("MakeLabel", info) + getName());
        jmi.setEnabled(false);

        if (namedLayoutBlock == null) {
            jmi = popupMenu.add(Bundle.getMessage("NoBlock"));
        } else {
            jmi = popupMenu.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameBlock")) + getLayoutBlock().getDisplayName());
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
            popupMenu.add(connectionsMenu);
        }

        popupMenu.add(new JSeparator(JSeparator.HORIZONTAL));

        popupMenu.add(mainlineCheckBoxMenuItem);
        mainlineCheckBoxMenuItem.addActionListener((java.awt.event.ActionEvent e3) -> {
            setMainline(mainlineCheckBoxMenuItem.isSelected());
        });
        mainlineCheckBoxMenuItem.setToolTipText(Bundle.getMessage("MainlineCheckBoxMenuItemToolTip"));
        mainlineCheckBoxMenuItem.setSelected(mainline);

        popupMenu.add(hiddenCheckBoxMenuItem);
        hiddenCheckBoxMenuItem.addActionListener((java.awt.event.ActionEvent e3) -> {
            setHidden(hiddenCheckBoxMenuItem.isSelected());
        });
        hiddenCheckBoxMenuItem.setToolTipText(Bundle.getMessage("HiddenCheckBoxMenuItemToolTip"));
        hiddenCheckBoxMenuItem.setSelected(hidden);

        popupMenu.add(dashedCheckBoxMenuItem);
        dashedCheckBoxMenuItem.addActionListener((java.awt.event.ActionEvent e3) -> {
            setDashed(dashedCheckBoxMenuItem.isSelected());
        });
        dashedCheckBoxMenuItem.setToolTipText(Bundle.getMessage("DashedCheckBoxMenuItemToolTip"));
        dashedCheckBoxMenuItem.setSelected(dashed);

        if (isArc()) {
            popupMenu.add(flippedCheckBoxMenuItem);
            flippedCheckBoxMenuItem.addActionListener((java.awt.event.ActionEvent e3) -> {
                setFlip(flippedCheckBoxMenuItem.isSelected());
            });
            flippedCheckBoxMenuItem.setToolTipText(Bundle.getMessage("FlippedCheckBoxMenuItemToolTip"));
            flippedCheckBoxMenuItem.setSelected(isFlip());
        }

        //
        // decorations menu
        //
        JMenu decorationsMenu = new JMenu(Bundle.getMessage("DecorationMenuTitle"));
        decorationsMenu.setToolTipText(Bundle.getMessage("DecorationMenuToolTip"));

        JCheckBoxMenuItem jcbmi;

        //
        // arrows menus
        //
        // arrows can only be added at edge connector
        //
        boolean hasEC1 = false;
        if (type1 == POS_POINT) {
            PositionablePoint pp = (PositionablePoint) connect1;
            if (pp.getType() == EDGE_CONNECTOR) {
                hasEC1 = true;
            }
        }
        boolean hasEC2 = false;
        if (type2 == POS_POINT) {
            PositionablePoint pp = (PositionablePoint) connect2;
            if (pp.getType() == EDGE_CONNECTOR) {
                hasEC2 = true;
            }
        }
        if (hasEC1 || hasEC2) {
            JMenu arrowsMenu = new JMenu(Bundle.getMessage("ArrowsMenuTitle"));
            decorationsMenu.setToolTipText(Bundle.getMessage("ArrowsMenuToolTip"));
            decorationsMenu.add(arrowsMenu);

            JMenu arrowsCountMenu = new JMenu(Bundle.getMessage("DecorationStyleMenuTitle"));
            arrowsCountMenu.setToolTipText(Bundle.getMessage("DecorationStyleMenuToolTip"));
            arrowsMenu.add(arrowsCountMenu);

            jcbmi = new JCheckBoxMenuItem(Bundle.getMessage("DecorationNoneMenuItemTitle"));
            arrowsCountMenu.add(jcbmi);
            jcbmi.setToolTipText(Bundle.getMessage("DecorationNoneMenuItemToolTip"));
            jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                setArrowEndStart(false);
                setArrowEndStop(false);
                //setArrowStyle(0);
            });
            jcbmi.setSelected(arrowStyle == 0);

            ImageIcon imageIcon = new ImageIcon(FileUtil.findURL("program:resources/icons/decorations/ArrowStyle1.png"));
            jcbmi = new JCheckBoxMenuItem(imageIcon);
            arrowsCountMenu.add(jcbmi);
            jcbmi.setToolTipText(Bundle.getMessage("DecorationStyleMenuToolTip"));
            jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                setArrowEndStart((type1 == POS_POINT) && (((PositionablePoint) connect1).getType() == EDGE_CONNECTOR));
                setArrowEndStop((type2 == POS_POINT) && (((PositionablePoint) connect2).getType() == EDGE_CONNECTOR));
                setArrowStyle(1);
            });
            jcbmi.setSelected(arrowStyle == 1);

            imageIcon = new ImageIcon(FileUtil.findURL("program:resources/icons/decorations/ArrowStyle2.png"));
            jcbmi = new JCheckBoxMenuItem(imageIcon);
            arrowsCountMenu.add(jcbmi);
            jcbmi.setToolTipText(Bundle.getMessage("DecorationStyleMenuToolTip"));
            jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                setArrowEndStart((type1 == POS_POINT) && (((PositionablePoint) connect1).getType() == EDGE_CONNECTOR));
                setArrowEndStop((type2 == POS_POINT) && (((PositionablePoint) connect2).getType() == EDGE_CONNECTOR));
                setArrowStyle(2);
            });
            jcbmi.setSelected(arrowStyle == 2);

            imageIcon = new ImageIcon(FileUtil.findURL("program:resources/icons/decorations/ArrowStyle3.png"));
            jcbmi = new JCheckBoxMenuItem(imageIcon);
            arrowsCountMenu.add(jcbmi);
            jcbmi.setToolTipText(Bundle.getMessage("DecorationStyleMenuToolTip"));
            jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                setArrowEndStart((type1 == POS_POINT) && (((PositionablePoint) connect1).getType() == EDGE_CONNECTOR));
                setArrowEndStop((type2 == POS_POINT) && (((PositionablePoint) connect2).getType() == EDGE_CONNECTOR));
                setArrowStyle(3);
            });
            jcbmi.setSelected(arrowStyle == 3);

            imageIcon = new ImageIcon(FileUtil.findURL("program:resources/icons/decorations/ArrowStyle4.png"));
            jcbmi = new JCheckBoxMenuItem(imageIcon);
            arrowsCountMenu.add(jcbmi);
            jcbmi.setToolTipText(Bundle.getMessage("DecorationStyleMenuToolTip"));
            jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                setArrowEndStart((type1 == POS_POINT) && (((PositionablePoint) connect1).getType() == EDGE_CONNECTOR));
                setArrowEndStop((type2 == POS_POINT) && (((PositionablePoint) connect2).getType() == EDGE_CONNECTOR));
                setArrowStyle(4);
            });
            jcbmi.setSelected(arrowStyle == 4);

            imageIcon = new ImageIcon(FileUtil.findURL("program:resources/icons/decorations/ArrowStyle5.png"));
            jcbmi = new JCheckBoxMenuItem(imageIcon);
            arrowsCountMenu.add(jcbmi);
            jcbmi.setToolTipText(Bundle.getMessage("DecorationStyleMenuToolTip"));
            jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                setArrowEndStart((type1 == POS_POINT) && (((PositionablePoint) connect1).getType() == EDGE_CONNECTOR));
                setArrowEndStop((type2 == POS_POINT) && (((PositionablePoint) connect2).getType() == EDGE_CONNECTOR));
                setArrowStyle(5);
            });
            jcbmi.setSelected(arrowStyle == 5);

            if (hasEC1 && hasEC2) {
                JMenu arrowsEndMenu = new JMenu(Bundle.getMessage("DecorationEndMenuTitle"));
                arrowsEndMenu.setToolTipText(Bundle.getMessage("DecorationEndMenuToolTip"));
                arrowsMenu.add(arrowsEndMenu);

                jcbmi = new JCheckBoxMenuItem(Bundle.getMessage("DecorationNoneMenuItemTitle"));
                arrowsEndMenu.add(jcbmi);
                jcbmi.setToolTipText(Bundle.getMessage("DecorationNoneMenuItemToolTip"));
                jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                    setArrowEndStart(false);
                    setArrowEndStop(false);
                });
                jcbmi.setSelected(!arrowEndStart && !arrowEndStop);

                jcbmi = new JCheckBoxMenuItem(Bundle.getMessage("DecorationStartMenuItemTitle"));
                arrowsEndMenu.add(jcbmi);
                jcbmi.setToolTipText(Bundle.getMessage("DecorationStartMenuItemToolTip"));
                jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                    setArrowEndStart(true);
                    setArrowEndStop(false);
                });
                jcbmi.setSelected(arrowEndStart && !arrowEndStop);

                jcbmi = new JCheckBoxMenuItem(Bundle.getMessage("DecorationEndMenuItemTitle"));
                arrowsEndMenu.add(jcbmi);
                jcbmi.setToolTipText(Bundle.getMessage("DecorationEndMenuItemToolTip"));
                jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                    setArrowEndStop(true);
                    setArrowEndStart(false);
                });
                jcbmi.setSelected(!arrowEndStart && arrowEndStop);

                jcbmi = new JCheckBoxMenuItem(Bundle.getMessage("DecorationBothMenuItemTitle"));
                arrowsEndMenu.add(jcbmi);
                jcbmi.setToolTipText(Bundle.getMessage("DecorationBothMenuItemToolTip"));
                jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                    setArrowEndStart(true);
                    setArrowEndStop(true);
                });
                jcbmi.setSelected(arrowEndStart && arrowEndStop);
            }

            JMenu arrowsDirMenu = new JMenu(Bundle.getMessage("ArrowsDirectionMenuTitle"));
            arrowsDirMenu.setToolTipText(Bundle.getMessage("ArrowsDirectionMenuToolTip"));
            arrowsMenu.add(arrowsDirMenu);

            jcbmi = new JCheckBoxMenuItem(Bundle.getMessage("DecorationNoneMenuItemTitle"));
            arrowsDirMenu.add(jcbmi);
            jcbmi.setToolTipText(Bundle.getMessage("DecorationNoneMenuItemToolTip"));
            jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                setArrowDirIn(false);
                setArrowDirOut(false);
            });
            jcbmi.setSelected(!arrowDirIn && !arrowDirOut);

            jcbmi = new JCheckBoxMenuItem(Bundle.getMessage("ArrowsDirectionInMenuItemTitle"));
            arrowsDirMenu.add(jcbmi);
            jcbmi.setToolTipText(Bundle.getMessage("ArrowsDirectionInMenuItemToolTip"));
            jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                setArrowDirIn(true);
                setArrowDirOut(false);
            });
            jcbmi.setSelected(arrowDirIn && !arrowDirOut);

            jcbmi = new JCheckBoxMenuItem(Bundle.getMessage("ArrowsDirectionOutMenuItemTitle"));
            arrowsDirMenu.add(jcbmi);
            jcbmi.setToolTipText(Bundle.getMessage("ArrowsDirectionOutMenuItemToolTip"));
            jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                setArrowDirOut(true);
                setArrowDirIn(false);
            });
            jcbmi.setSelected(!arrowDirIn && arrowDirOut);

            jcbmi = new JCheckBoxMenuItem(Bundle.getMessage("ArrowsDirectionBothMenuItemTitle"));
            arrowsDirMenu.add(jcbmi);
            jcbmi.setToolTipText(Bundle.getMessage("ArrowsDirectionBothMenuItemToolTip"));
            jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                setArrowDirIn(true);
                setArrowDirOut(true);
            });
            jcbmi.setSelected(arrowDirIn && arrowDirOut);

            jmi = arrowsMenu.add(new JMenuItem(Bundle.getMessage("DecorationColorMenuItemTitle")));
            jmi.setToolTipText(Bundle.getMessage("DecorationColorMenuItemToolTip"));
            jmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                Color newColor = JmriColorChooser.showDialog(null, "Choose a color", arrowColor);
                if ((newColor != null) && !newColor.equals(arrowColor)) {
                    setArrowColor(newColor);
                }
            });
            jmi.setForeground(arrowColor);
            jmi.setBackground(ColorUtil.contrast(arrowColor));

            jmi = arrowsMenu.add(new JMenuItem(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("DecorationLineWidthMenuItemTitle")) + arrowLineWidth));
            jmi.setToolTipText(Bundle.getMessage("DecorationLineWidthMenuItemToolTip"));
            jmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                //prompt for arrow line width
                int newValue = QuickPromptUtil.promptForInt(layoutEditor,
                        Bundle.getMessage("DecorationLineWidthMenuItemTitle"),
                        Bundle.getMessage("DecorationLineWidthMenuItemTitle"),
                        arrowLineWidth);
                setArrowLineWidth(newValue);
            });

            jmi = arrowsMenu.add(new JMenuItem(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("DecorationLengthMenuItemTitle")) + arrowLength));
            jmi.setToolTipText(Bundle.getMessage("DecorationLengthMenuItemToolTip"));
            jmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                //prompt for arrow length
                int newValue = QuickPromptUtil.promptForInt(layoutEditor,
                        Bundle.getMessage("DecorationLengthMenuItemTitle"),
                        Bundle.getMessage("DecorationLengthMenuItemTitle"),
                        arrowLength);
                setArrowLength(newValue);
            });

            jmi = arrowsMenu.add(new JMenuItem(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("DecorationGapMenuItemTitle")) + arrowGap));
            jmi.setToolTipText(Bundle.getMessage("DecorationGapMenuItemToolTip"));
            jmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                //prompt for arrow gap
                int newValue = QuickPromptUtil.promptForInt(layoutEditor,
                        Bundle.getMessage("DecorationGapMenuItemTitle"),
                        Bundle.getMessage("DecorationGapMenuItemTitle"),
                        arrowGap);
                setArrowGap(newValue);
            });
        }

        //
        // bridge menus
        //
        JMenu bridgeMenu = new JMenu(Bundle.getMessage("BridgeMenuTitle"));
        decorationsMenu.setToolTipText(Bundle.getMessage("BridgeMenuToolTip"));
        decorationsMenu.add(bridgeMenu);

        JMenu bridgeSideMenu = new JMenu(Bundle.getMessage("DecorationSideMenuTitle"));
        bridgeSideMenu.setToolTipText(Bundle.getMessage("DecorationSideMenuToolTip"));
        bridgeMenu.add(bridgeSideMenu);

        jcbmi = new JCheckBoxMenuItem(Bundle.getMessage("DecorationNoneMenuItemTitle"));
        bridgeSideMenu.add(jcbmi);
        jcbmi.setToolTipText(Bundle.getMessage("DecorationNoneMenuItemToolTip"));
        jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
            setBridgeSideLeft(false);
            setBridgeSideRight(false);
        });
        jcbmi.setSelected(!bridgeSideLeft && !bridgeSideRight);

        jcbmi = new JCheckBoxMenuItem(Bundle.getMessage("DecorationSideLeftMenuItemTitle"));
        bridgeSideMenu.add(jcbmi);
        jcbmi.setToolTipText(Bundle.getMessage("DecorationSideLeftMenuItemToolTip"));
        jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
            setBridgeSideLeft(true);
            setBridgeSideRight(false);
        });
        jcbmi.setSelected(bridgeSideLeft && !bridgeSideRight);

        jcbmi = new JCheckBoxMenuItem(Bundle.getMessage("DecorationSideRightMenuItemTitle"));
        bridgeSideMenu.add(jcbmi);
        jcbmi.setToolTipText(Bundle.getMessage("DecorationSideRightMenuItemToolTip"));
        jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
            setBridgeSideRight(true);
            setBridgeSideLeft(false);
        });
        jcbmi.setSelected(!bridgeSideLeft && bridgeSideRight);

        jcbmi = new JCheckBoxMenuItem(Bundle.getMessage("DecorationBothMenuItemTitle"));
        bridgeSideMenu.add(jcbmi);
        jcbmi.setToolTipText(Bundle.getMessage("DecorationBothMenuItemToolTip"));
        jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
            setBridgeSideLeft(true);
            setBridgeSideRight(true);
        });
        jcbmi.setSelected(bridgeSideLeft && bridgeSideRight);

        JMenu bridgeEndMenu = new JMenu(Bundle.getMessage("DecorationEndMenuTitle"));
        bridgeEndMenu.setToolTipText(Bundle.getMessage("DecorationEndMenuToolTip"));
        bridgeMenu.add(bridgeEndMenu);

        jcbmi = new JCheckBoxMenuItem(Bundle.getMessage("DecorationNoneMenuItemTitle"));
        bridgeEndMenu.add(jcbmi);
        jcbmi.setToolTipText(Bundle.getMessage("DecorationNoneMenuItemToolTip"));
        jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
            setBridgeHasEntry(false);
            setBridgeHasExit(false);
        });
        jcbmi.setSelected(!bridgeHasEntry && !bridgeHasExit);

        jcbmi = new JCheckBoxMenuItem(Bundle.getMessage("DecorationEntryMenuItemTitle"));
        bridgeEndMenu.add(jcbmi);
        jcbmi.setToolTipText(Bundle.getMessage("DecorationEntryMenuItemToolTip"));
        jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
            setBridgeHasEntry(true);
            setBridgeHasExit(false);
        });
        jcbmi.setSelected(bridgeHasEntry && !bridgeHasExit);

        jcbmi = new JCheckBoxMenuItem(Bundle.getMessage("DecorationExitMenuItemTitle"));
        bridgeEndMenu.add(jcbmi);
        jcbmi.setToolTipText(Bundle.getMessage("DecorationExitMenuItemToolTip"));
        jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
            setBridgeHasExit(true);
            setBridgeHasEntry(false);
        });
        jcbmi.setSelected(!bridgeHasEntry && bridgeHasExit);

        jcbmi = new JCheckBoxMenuItem(Bundle.getMessage("DecorationBothMenuItemTitle"));
        bridgeEndMenu.add(jcbmi);
        jcbmi.setToolTipText(Bundle.getMessage("DecorationBothMenuItemToolTip"));
        jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
            setBridgeHasEntry(true);
            setBridgeHasExit(true);
        });
        jcbmi.setSelected(bridgeHasEntry && bridgeHasExit);

        jmi = bridgeMenu.add(new JMenuItem(Bundle.getMessage("DecorationColorMenuItemTitle")));
        jmi.setToolTipText(Bundle.getMessage("DecorationColorMenuItemToolTip"));
        jmi.addActionListener((java.awt.event.ActionEvent e3) -> {
            Color newColor = JmriColorChooser.showDialog(null, "Choose a color", bridgeColor);
            if ((newColor != null) && !newColor.equals(bridgeColor)) {
                setBridgeColor(newColor);
            }
        });
        jmi.setForeground(bridgeColor);
        jmi.setBackground(ColorUtil.contrast(bridgeColor));

        addNumericMenuItem(bridgeMenu,
                "DecorationLineWidthMenuItemTitle", "DecorationLineWidthMenuItemToolTip",
                this::getBridgeLineWidth, this::setBridgeLineWidth,
                QuickPromptUtil.checkIntRange(10, MAX_BRIDGE_LINE_WIDTH, null));

        addNumericMenuItem(bridgeMenu,
                "BridgeApproachWidthMenuItemTitle", "BridgeApproachWidthMenuItemToolTip",
                this::getBridgeApproachWidth, this::setBridgeApproachWidth,
                QuickPromptUtil.checkIntRange(4, MAX_BRIDGE_APPROACH_WIDTH, null));

        addNumericMenuItem(bridgeMenu,
                "BridgeDeckWidthMenuItemTitle", "BridgeDeckWidthMenuItemToolTip",
                this::getBridgeDeckWidth, this::setBridgeDeckWidth,
                QuickPromptUtil.checkIntRange(1, MAX_BRIDGE_DECK_WIDTH, null));

        //
        // end bumper menus
        //
        // end bumper decorations can only be on end bumpers
        //
        boolean hasEB1 = false;
        if (type1 == POS_POINT) {
            PositionablePoint pp = (PositionablePoint) connect1;
            if (pp.getType() == END_BUMPER) {
                hasEB1 = true;
            }
        }
        boolean hasEB2 = false;
        if (type2 == POS_POINT) {
            PositionablePoint pp = (PositionablePoint) connect2;
            if (pp.getType() == END_BUMPER) {
                hasEB2 = true;
            }
        }
        if (hasEB1 || hasEB2) {
            JMenu endBumperMenu = new JMenu(Bundle.getMessage("EndBumperMenuTitle"));
            decorationsMenu.setToolTipText(Bundle.getMessage("EndBumperMenuToolTip"));
            decorationsMenu.add(endBumperMenu);

            if (hasEB1 && hasEB2) {
                JMenu endBumperEndMenu = new JMenu(Bundle.getMessage("DecorationEndMenuTitle"));
                endBumperEndMenu.setToolTipText(Bundle.getMessage("DecorationEndMenuToolTip"));
                endBumperMenu.add(endBumperEndMenu);

                jcbmi = new JCheckBoxMenuItem(Bundle.getMessage("DecorationNoneMenuItemTitle"));
                endBumperEndMenu.add(jcbmi);
                jcbmi.setToolTipText(Bundle.getMessage("DecorationNoneMenuItemToolTip"));
                jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                    setBumperEndStart(false);
                    setBumperEndStop(false);
                });
                jcbmi.setSelected(!bumperEndStart && !bumperEndStop);

                jcbmi = new JCheckBoxMenuItem(Bundle.getMessage("DecorationStartMenuItemTitle"));
                endBumperEndMenu.add(jcbmi);
                jcbmi.setToolTipText(Bundle.getMessage("DecorationStartMenuItemToolTip"));
                jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                    setBumperEndStart(true);
                    setBumperEndStop(false);
                });
                jcbmi.setSelected(bumperEndStart && !bumperEndStop);

                jcbmi = new JCheckBoxMenuItem(Bundle.getMessage("DecorationEndMenuItemTitle"));
                endBumperEndMenu.add(jcbmi);
                jcbmi.setToolTipText(Bundle.getMessage("DecorationEndMenuItemToolTip"));
                jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                    setBumperEndStart(false);
                    setBumperEndStop(true);
                });
                jcbmi.setSelected(!bumperEndStart && bumperEndStop);

                jcbmi = new JCheckBoxMenuItem(Bundle.getMessage("DecorationBothMenuItemTitle"));
                endBumperEndMenu.add(jcbmi);
                jcbmi.setToolTipText(Bundle.getMessage("DecorationEndMenuItemToolTip"));
                jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                    setBumperEndStart(true);
                    setBumperEndStop(true);
                });
                jcbmi.setSelected(bumperEndStart && bumperEndStop);
            } else {
                JCheckBoxMenuItem enableCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("EndBumperEnableMenuItemTitle"));
                enableCheckBoxMenuItem.setToolTipText(Bundle.getMessage("EndBumperEnableMenuItemToolTip"));

                endBumperMenu.add(enableCheckBoxMenuItem);
                enableCheckBoxMenuItem.addActionListener((java.awt.event.ActionEvent e3) -> {
                    if ((type1 == POS_POINT) && (((PositionablePoint) connect1).getType() == END_BUMPER)) {
                        setBumperEndStart(enableCheckBoxMenuItem.isSelected());
                    }
                    if ((type2 == POS_POINT) && (((PositionablePoint) connect2).getType() == END_BUMPER)) {
                        setBumperEndStop(enableCheckBoxMenuItem.isSelected());
                    }
                });
                enableCheckBoxMenuItem.setSelected(bumperEndStart || bumperEndStop);
            }

            jmi = endBumperMenu.add(new JMenuItem(Bundle.getMessage("DecorationColorMenuItemTitle")));
            jmi.setToolTipText(Bundle.getMessage("DecorationColorMenuItemToolTip"));
            jmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                Color newColor = JmriColorChooser.showDialog(null, "Choose a color", bumperColor);
                if ((newColor != null) && !newColor.equals(bumperColor)) {
                    setBumperColor(newColor);
                }
            });
            jmi.setForeground(bumperColor);
            jmi.setBackground(ColorUtil.contrast(bumperColor));

            jmi = endBumperMenu.add(new JMenuItem(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("DecorationLineWidthMenuItemTitle")) + bumperLineWidth));
            jmi.setToolTipText(Bundle.getMessage("DecorationLineWidthMenuItemToolTip"));
            jmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                //prompt for width
                int newValue = QuickPromptUtil.promptForInteger(layoutEditor,
                        Bundle.getMessage("DecorationLineWidthMenuItemTitle"),
                        Bundle.getMessage("DecorationLineWidthMenuItemTitle"),
                        getBumperLineWidth(), new Predicate<Integer>() {
                    @Override
                    public boolean test(Integer t) {
                        if (t < 0 || t > TrackSegment.MAX_BUMPER_WIDTH) {
                            throw new IllegalArgumentException(
                                    Bundle.getMessage("DecorationLengthMenuItemRange", TrackSegment.MAX_BUMPER_WIDTH));
                        }
                        return true;
                    }
                });
                setBumperLineWidth(newValue);
            });

            jmi = endBumperMenu.add(new JMenuItem(Bundle.getMessage("MakeLabel",
                    Bundle.getMessage("DecorationLengthMenuItemTitle")) + bumperLength));
            jmi.setToolTipText(Bundle.getMessage("DecorationLengthMenuItemToolTip"));
            jmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                //prompt for length
                int newValue = QuickPromptUtil.promptForInteger(layoutEditor,
                        Bundle.getMessage("DecorationLengthMenuItemTitle"),
                        Bundle.getMessage("DecorationLengthMenuItemTitle"),
                        bumperLength, new Predicate<Integer>() {
                    @Override
                    public boolean test(Integer t) {
                        if (t < 0 || t > MAX_BUMPER_LENGTH) {
                            throw new IllegalArgumentException(
                                    Bundle.getMessage("DecorationLengthMenuItemRange", MAX_BUMPER_LENGTH));
                        }
                        return true;
                    }
                }
                );
                setBumperLength(newValue);
            });
        }

        //
        // tunnel menus
        //
        JMenu tunnelMenu = new JMenu(Bundle.getMessage("TunnelMenuTitle"));
        decorationsMenu.setToolTipText(Bundle.getMessage("TunnelMenuToolTip"));
        decorationsMenu.add(tunnelMenu);

        JMenu tunnelSideMenu = new JMenu(Bundle.getMessage("DecorationSideMenuTitle"));
        tunnelSideMenu.setToolTipText(Bundle.getMessage("DecorationSideMenuToolTip"));
        tunnelMenu.add(tunnelSideMenu);

        jcbmi = new JCheckBoxMenuItem(Bundle.getMessage("DecorationNoneMenuItemTitle"));
        tunnelSideMenu.add(jcbmi);
        jcbmi.setToolTipText(Bundle.getMessage("DecorationNoneMenuItemToolTip"));
        jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
            setTunnelSideLeft(false);
            setTunnelSideRight(false);
        });
        jcbmi.setSelected(!tunnelSideLeft && !tunnelSideRight);

        jcbmi = new JCheckBoxMenuItem(Bundle.getMessage("DecorationSideLeftMenuItemTitle"));
        tunnelSideMenu.add(jcbmi);
        jcbmi.setToolTipText(Bundle.getMessage("DecorationSideLeftMenuItemToolTip"));
        jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
            setTunnelSideLeft(true);
            setTunnelSideRight(false);
        });
        jcbmi.setSelected(tunnelSideLeft && !tunnelSideRight);

        jcbmi = new JCheckBoxMenuItem(Bundle.getMessage("DecorationSideRightMenuItemTitle"));
        tunnelSideMenu.add(jcbmi);
        jcbmi.setToolTipText(Bundle.getMessage("DecorationSideRightMenuItemToolTip"));
        jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
            setTunnelSideRight(true);
            setTunnelSideLeft(false);
        });
        jcbmi.setSelected(!tunnelSideLeft && tunnelSideRight);

        jcbmi = new JCheckBoxMenuItem(Bundle.getMessage("DecorationBothMenuItemTitle"));
        tunnelSideMenu.add(jcbmi);
        jcbmi.setToolTipText(Bundle.getMessage("DecorationBothMenuItemToolTip"));
        jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
            setTunnelSideLeft(true);
            setTunnelSideRight(true);
        });
        jcbmi.setSelected(tunnelSideLeft && tunnelSideRight);

        JMenu tunnelEndMenu = new JMenu(Bundle.getMessage("DecorationEndMenuTitle"));
        tunnelEndMenu.setToolTipText(Bundle.getMessage("DecorationEndMenuToolTip"));
        tunnelMenu.add(tunnelEndMenu);

        jcbmi = new JCheckBoxMenuItem(Bundle.getMessage("DecorationNoneMenuItemTitle"));
        tunnelEndMenu.add(jcbmi);
        jcbmi.setToolTipText(Bundle.getMessage("DecorationNoneMenuItemToolTip"));
        jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
            setTunnelHasEntry(false);
            setTunnelHasExit(false);
        });
        jcbmi.setSelected(!tunnelHasEntry && !tunnelHasExit);

        jcbmi = new JCheckBoxMenuItem(Bundle.getMessage("DecorationEntryMenuItemTitle"));
        tunnelEndMenu.add(jcbmi);
        jcbmi.setToolTipText(Bundle.getMessage("DecorationEntryMenuItemToolTip"));
        jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
            setTunnelHasEntry(true);
            setTunnelHasExit(false);
        });
        jcbmi.setSelected(tunnelHasEntry && !tunnelHasExit);

        jcbmi = new JCheckBoxMenuItem(Bundle.getMessage("DecorationExitMenuItemTitle"));
        tunnelEndMenu.add(jcbmi);
        jcbmi.setToolTipText(Bundle.getMessage("DecorationExitMenuItemToolTip"));
        jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
            setTunnelHasExit(true);
            setTunnelHasEntry(false);
        });
        jcbmi.setSelected(!tunnelHasEntry && tunnelHasExit);

        jcbmi = new JCheckBoxMenuItem(Bundle.getMessage("DecorationBothMenuItemTitle"));
        tunnelEndMenu.add(jcbmi);
        jcbmi.setToolTipText(Bundle.getMessage("DecorationBothMenuItemToolTip"));
        jcbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
            setTunnelHasEntry(true);
            setTunnelHasExit(true);
        });
        jcbmi.setSelected(tunnelHasEntry && tunnelHasExit);

        jmi = tunnelMenu.add(new JMenuItem(Bundle.getMessage("DecorationColorMenuItemTitle")));
        jmi.setToolTipText(Bundle.getMessage("DecorationColorMenuItemToolTip"));
        jmi.addActionListener((java.awt.event.ActionEvent e3) -> {
            Color newColor = JmriColorChooser.showDialog(null, "Choose a color", tunnelColor);
            if ((newColor != null) && !newColor.equals(tunnelColor)) {
                setTunnelColor(newColor);
            }
        });
        jmi.setForeground(tunnelColor);
        jmi.setBackground(ColorUtil.contrast(tunnelColor));

        addNumericMenuItem(tunnelMenu,
                "TunnelFloorWidthMenuItemTitle", "TunnelFloorWidthMenuItemToolTip",
                this::getTunnelFloorWidth, this::setTunnelFloorWidth,
                QuickPromptUtil.checkIntRange(1, MAX_TUNNEL_FLOOR_WIDTH, null));
        addNumericMenuItem(tunnelMenu,
                "DecorationLineWidthMenuItemTitle", "DecorationLineWidthMenuItemToolTip",
                this::getTunnelLineWidth, this::setTunnelLineWidth,
                QuickPromptUtil.checkIntRange(1, MAX_TUNNEL_LINE_WIDTH, null));
        addNumericMenuItem(tunnelMenu,
                "TunnelEntranceWidthMenuItemTitle", "TunnelEntranceWidthMenuItemToolTip",
                this::getTunnelEntranceWidth, this::setTunnelEntranceWidth,
                QuickPromptUtil.checkIntRange(1, MAX_TUNNEL_ENTRANCE_WIDTH, null));

        popupMenu.add(decorationsMenu);

        popupMenu.add(new JSeparator(JSeparator.HORIZONTAL));
        popupMenu.add(new AbstractAction(Bundle.getMessage("ButtonEdit")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                layoutEditor.getLayoutTrackEditors().editTrackSegment(TrackSegment.this);
            }
        });
        popupMenu.add(new AbstractAction(Bundle.getMessage("ButtonDelete")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (canRemove()) {
                    layoutEditor.removeTrackSegment(TrackSegment.this);
                    remove();
                    dispose();
                }
            }
        });
        popupMenu.add(new AbstractAction(Bundle.getMessage("SplitTrackSegment")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                splitTrackSegment();
            }
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

        popupMenu.add(lineType);

        if (isArc() || isBezier()) {
            if (hideConstructionLines()) {
                popupMenu.add(new AbstractAction(Bundle.getMessage("ShowConstruct")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        hideConstructionLines(SHOWCON);
                    }
                });
            } else {
                popupMenu.add(new AbstractAction(Bundle.getMessage("HideConstruct")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        hideConstructionLines(HIDECON);
                    }
                });
            }
        }
        if ((namedLayoutBlock != null) && (jmri.InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled())) {
            popupMenu.add(new AbstractAction(Bundle.getMessage("ViewBlockRouting")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    AbstractAction routeTableAction = new LayoutBlockRouteTableAction("ViewRouting", getLayoutBlock());
                    routeTableAction.actionPerformed(e);
                }
            });
        }
        popupMenu.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
        return popupMenu;
    }   // showPopup

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canRemove() {
        List<String> itemList = new ArrayList<>();

        int type1 = getType1();
        LayoutTrack conn1 = getConnect1();
        itemList.addAll(getPointReferences(type1, conn1));

        int type2 = getType2();
        LayoutTrack conn2 = getConnect2();
        itemList.addAll(getPointReferences(type2, conn2));

        if (!itemList.isEmpty()) {
            displayRemoveWarningDialog(itemList, "TrackSegment");  // NOI18N
        }
        return itemList.isEmpty();
    }

    public ArrayList<String> getPointReferences(int type, LayoutTrack conn) {
        ArrayList<String> result = new ArrayList<>();

        if (type == POS_POINT && conn instanceof PositionablePoint) {
            PositionablePoint pt = (PositionablePoint) conn;
            if (!pt.getEastBoundSignal().isEmpty()) {
                result.add(pt.getEastBoundSignal());
            }
            if (!pt.getWestBoundSignal().isEmpty()) {
                result.add(pt.getWestBoundSignal());
            }
            if (!pt.getEastBoundSignalMastName().isEmpty()) {
                result.add(pt.getEastBoundSignalMastName());
            }
            if (!pt.getWestBoundSignalMastName().isEmpty()) {
                result.add(pt.getWestBoundSignalMastName());
            }
            if (!pt.getEastBoundSensorName().isEmpty()) {
                result.add(pt.getEastBoundSensorName());
            }
            if (!pt.getWestBoundSensorName().isEmpty()) {
                result.add(pt.getWestBoundSensorName());
            }
            if (pt.getType() == EDGE_CONNECTOR && pt.getLinkedPoint() != null) {
                result.add(Bundle.getMessage("DeleteECisActive"));   // NOI18N
            }
        }

        if ((type == TURNOUT_A || type == TURNOUT_B || type == TURNOUT_C || type == TURNOUT_D) && conn instanceof LayoutTurnout) {
            LayoutTurnout lt = (LayoutTurnout) conn;
            if (type == TURNOUT_A) {
                result = lt.getBeanReferences("A");  // NOI18N
            }
            if (type == TURNOUT_B) {
                result = lt.getBeanReferences("B");  // NOI18N
            }
            if (type == TURNOUT_C) {
                result = lt.getBeanReferences("C");  // NOI18N
            }
            if (type == TURNOUT_D) {
                result = lt.getBeanReferences("D");  // NOI18N
            }
        }

        if ((type == LEVEL_XING_A || type == LEVEL_XING_B || type == LEVEL_XING_C || type == LEVEL_XING_D) && conn instanceof LevelXing) {
            LevelXing lx = (LevelXing) conn;
            if (type == LEVEL_XING_A) {
                result = lx.getBeanReferences("A");  // NOI18N
            }
            if (type == LEVEL_XING_B) {
                result = lx.getBeanReferences("B");  // NOI18N
            }
            if (type == LEVEL_XING_C) {
                result = lx.getBeanReferences("C");  // NOI18N
            }
            if (type == LEVEL_XING_D) {
                result = lx.getBeanReferences("D");  // NOI18N
            }
        }

        if ((type == SLIP_A || type == SLIP_B || type == SLIP_C || type == SLIP_D) && conn instanceof LayoutSlip) {
            LayoutSlip ls = (LayoutSlip) conn;
            if (type == SLIP_A) {
                result = ls.getBeanReferences("A");  // NOI18N
            }
            if (type == SLIP_B) {
                result = ls.getBeanReferences("B");  // NOI18N
            }
            if (type == SLIP_C) {
                result = ls.getBeanReferences("C");  // NOI18N
            }
            if (type == SLIP_D) {
                result = ls.getBeanReferences("D");  // NOI18N
            }
        }

        return result;
    }

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
        newTrackSegment.setDecorations(this.getDecorations());

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
    }   // splitTrackSegment

    /**
     * Display popup menu for information and editing.
     */
    protected void showBezierPopUp(MouseEvent e, int hitPointType) {
        int bezierControlPointIndex = hitPointType - BEZIER_CONTROL_POINT_OFFSET_MIN;
        if (popupMenu != null) {
            popupMenu.removeAll();
        } else {
            popupMenu = new JPopupMenu();
        }

        JMenuItem jmi = popupMenu.add(Bundle.getMessage("BezierControlPoint") + " #" + bezierControlPointIndex);
        jmi.setEnabled(false);
        popupMenu.add(new JSeparator(JSeparator.HORIZONTAL));

        if (bezierControlPoints.size() <= BEZIER_CONTROL_POINT_OFFSET_MAX - BEZIER_CONTROL_POINT_OFFSET_MIN) {
            popupMenu.add(new AbstractAction(Bundle.getMessage("AddBezierControlPointAfter")) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    addBezierControlPointAfter(bezierControlPointIndex);
                }
            });
            popupMenu.add(new AbstractAction(Bundle.getMessage("AddBezierControlPointBefore")) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    addBezierControlPointBefore(bezierControlPointIndex);
                }
            });
        }

        if (bezierControlPoints.size() > 2) {
            popupMenu.add(new AbstractAction(Bundle.getMessage("DeleteBezierControlPoint") + " #" + bezierControlPointIndex) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    deleteBezierControlPoint(bezierControlPointIndex);
                }
            });
        }
        popupMenu.show(e.getComponent(), e.getX(), e.getY());
    }

    private void addBezierControlPointBefore(int index) {
        Point2D addPoint = getBezierControlPoint(index);
        if (index > 0) {
            addPoint = MathUtil.midPoint(getBezierControlPoint(index - 1), addPoint);
        } else {
            Point2D ep1 = LayoutEditor.getCoords(getConnect1(), getType1());
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
            Point2D ep2 = LayoutEditor.getCoords(getConnect2(), getType2());
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
            case 2: // arc
                setArc(true);
                setAngle(90.0D);
                setCircle(false);
                setBezier(false);
                break;
            case 3:
                setArc(false);  // bezier
                setCircle(false);
                if (bezierControlPoints.size() == 0) {
                    //TODO: Use MathUtil.intersect to find intersection of adjacent tracks
                    //TODO: and place the control points halfway between that and the two endpoints

                    // set default control point displacements
                    Point2D ep1 = LayoutEditor.getCoords(getConnect1(), getType1());
                    Point2D ep2 = LayoutEditor.getCoords(getConnect2(), getType2());

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

    /**
     * Clean up when this object is no longer needed.
     * <p>
     * Should not be called while the object is still displayed.
     *
     * @see #remove()
     */
    void dispose() {
        if (popupMenu != null) {
            popupMenu.removeAll();
        }
        popupMenu = null;
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

    public boolean isShowConstructionLines() {
        return (((showConstructionLine & HIDECON) != HIDECON)
                && ((showConstructionLine & HIDECONALL) != HIDECONALL));
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
        return ((showConstructionLine & SHOWCON) != SHOWCON);
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
            Point2D ep1 = LayoutEditor.getCoords(getConnect1(), getType1());
            Point2D ep2 = LayoutEditor.getCoords(getConnect2(), getType2());

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
    //NOTE: AFAICT this isn't called from anywhere
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
            pt1 = LayoutEditor.getCoords(getConnect2(), getType2());
            pt2 = LayoutEditor.getCoords(getConnect1(), getType1());
        } else {
            pt1 = LayoutEditor.getCoords(getConnect1(), getType1());
            pt2 = LayoutEditor.getCoords(getConnect2(), getType2());
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
            double halfAngleRAD = Math.toRadians(getTmpAngle()) / 2.D;

            // Compute arc's chord
            double a = pt2x - pt1x;
            double o = pt2y - pt1y;
            double chord = Math.hypot(a, o);
            setChordLength(chord);

            // Make sure chord is not null
            // In such a case (ep1 == ep2), there is no arc to draw
            if (chord > 0.D) {
                double radius = (chord / 2.D) / Math.sin(halfAngleRAD);
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
//        if (getName().equals("T5")) {
//            log.debug("STOP");
//        }
        if (!isBlock && isDashed() && getLayoutBlock() != null) {
            // Skip the dashed rail layer, the block layer will display the dashed track
            // This removes random rail fragments from between the block dashes
            return;
        }
        if (isMain == mainline) {
            if (isBlock) {
                setColorForTrackBlock(g2, getLayoutBlock());
            }
            if (isArc()) {
                calculateTrackSegmentAngle();
                g2.draw(new Arc2D.Double(getCX(), getCY(), getCW(), getCH(), getStartAdj(), getTmpAngle(), Arc2D.OPEN));
                trackRedrawn();
            } else if (isBezier()) {
                Point2D[] points = getBezierPoints();
                MathUtil.drawBezier(g2, points);
            } else {
                Point2D end1 = LayoutEditor.getCoords(getConnect1(), getType1());
                Point2D end2 = LayoutEditor.getCoords(getConnect2(), getType2());

                g2.draw(new Line2D.Double(end1, end2));
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void draw2(Graphics2D g2, boolean isMain, float railDisplacement) {
//        if (getName().equals("T5")) {
//            log.debug("STOP");
//        }
        if (isDashed() && getLayoutBlock() != null) {
            // Skip the dashed rail layer, the block layer will display the dashed track
            // This removes random rail fragments from between the block dashes
            return;
        }
        if (isMain == mainline) {
            if (isArc()) {
                calculateTrackSegmentAngle();
                Rectangle2D cRectangle2D = new Rectangle2D.Double(
                        getCX(), getCY(), getCW(), getCH());
                Rectangle2D tRectangle2D = MathUtil.inset(cRectangle2D, -railDisplacement);
                double startAdj = getStartAdj(), tmpAngle = getTmpAngle();
                g2.draw(new Arc2D.Double(tRectangle2D.getX(), tRectangle2D.getY(),
                        tRectangle2D.getWidth(), tRectangle2D.getHeight(),
                        startAdj, tmpAngle, Arc2D.OPEN));
                tRectangle2D = MathUtil.inset(cRectangle2D, +railDisplacement);
                g2.draw(new Arc2D.Double(tRectangle2D.getX(), tRectangle2D.getY(),
                        tRectangle2D.getWidth(), tRectangle2D.getHeight(),
                        startAdj, tmpAngle, Arc2D.OPEN));
                trackRedrawn();
            } else if (isBezier()) {
                Point2D[] points = getBezierPoints();
                MathUtil.drawBezier(g2, points, -railDisplacement);
                MathUtil.drawBezier(g2, points, +railDisplacement);
            } else {
                Point2D end1 = LayoutEditor.getCoords(getConnect1(), getType1());
                Point2D end2 = LayoutEditor.getCoords(getConnect2(), getType2());

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
    protected void highlightUnconnected(Graphics2D g2, int selectedType) {
        // TrackSegments are always connected
        // nothing to see here... move along...
    }

    @Override
    protected void drawEditControls(Graphics2D g2) {
        g2.setColor(Color.black);
        if (isShowConstructionLines()) {
            Point2D ep1 = LayoutEditor.getCoords(getConnect1(), getType1());
            Point2D ep2 = LayoutEditor.getCoords(getConnect2(), getType2());
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

    @Override
    protected void drawTurnoutControls(Graphics2D g2) {
        // TrackSegments don't have turnout controls...
        // nothing to see here... move along...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void reCheckBlockBoundary() {
        // nothing to see here... move along...
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void drawDecorations(Graphics2D g2) {
//        if (getName().equals("T5")) {
//            log.debug("STOP");
//        }

        // get end points and calculate start/stop angles (in radians)
        Point2D ep1 = LayoutEditor.getCoords(getConnect1(), getType1());
        Point2D ep2 = LayoutEditor.getCoords(getConnect2(), getType2());
        Point2D p1, p2, p3, p4, p5, p6, p7;
        Point2D p1P = ep1, p2P = ep2, p3P, p4P, p5P, p6P, p7P;
        double startAngleRAD, stopAngleRAD;
        if (isArc()) {
            calculateTrackSegmentAngle();
            double startAngleDEG = getStartAdj(), extentAngleDEG = getTmpAngle();
            startAngleRAD = (Math.PI / 2.D) - Math.toRadians(startAngleDEG);
            stopAngleRAD = (Math.PI / 2.D) - Math.toRadians(startAngleDEG + extentAngleDEG);
            if (isFlip()) {
                startAngleRAD += Math.PI;
                stopAngleRAD += Math.PI;
            } else {
                double temp = startAngleRAD;
                startAngleRAD = stopAngleRAD;
                stopAngleRAD = temp;
            }
        } else if (isBezier()) {
            Point2D cp0 = bezierControlPoints.get(0);
            Point2D cpN = bezierControlPoints.get(bezierControlPoints.size() - 1);
            startAngleRAD = (Math.PI / 2.D) - MathUtil.computeAngleRAD(cp0, ep1);
            stopAngleRAD = (Math.PI / 2.D) - MathUtil.computeAngleRAD(ep2, cpN);
        } else {
            startAngleRAD = (Math.PI / 2.D) - MathUtil.computeAngleRAD(ep2, ep1);
            stopAngleRAD = startAngleRAD;
        }

        //
        // arrow decorations
        //
        if (arrowStyle > 0) {
            g2.setStroke(new BasicStroke(arrowLineWidth,
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.F));
            g2.setColor(arrowColor);

            // draw the start arrows
            int offset = 1;
            if (arrowEndStart) {
                if (arrowDirIn) {
                    offset = drawArrow(g2, ep1, Math.PI + startAngleRAD, false, offset);
                }
                if (arrowDirOut) {
                    offset = drawArrow(g2, ep1, Math.PI + startAngleRAD, true, offset);
                }
            }

            // draw the stop arrows
            offset = 1;
            if (arrowEndStop) {
                if (arrowDirIn) {
                    offset = drawArrow(g2, ep2, stopAngleRAD, false, offset);
                }
                if (arrowDirOut) {
                    offset = drawArrow(g2, ep2, stopAngleRAD, true, offset);
                }
            }
        }   // arrow decoration

        //
        //  bridge decorations
        //
        if (bridgeSideLeft || bridgeSideRight) {
            float halfWidth = bridgeDeckWidth / 2.F;

            g2.setStroke(new BasicStroke(bridgeLineWidth,
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.F));
            g2.setColor(bridgeColor);

            if (isArc()) {
                calculateTrackSegmentAngle();
                Rectangle2D cRectangle2D = new Rectangle2D.Double(
                        getCX(), getCY(), getCW(), getCH());
                double startAdj = getStartAdj(), tmpAngle = getTmpAngle();
                if (bridgeSideLeft) {
                    Rectangle2D tRectangle2D = MathUtil.inset(cRectangle2D, -halfWidth);
                    g2.draw(new Arc2D.Double(tRectangle2D.getX(), tRectangle2D.getY(),
                            tRectangle2D.getWidth(), tRectangle2D.getHeight(),
                            startAdj, tmpAngle, Arc2D.OPEN));
                }
                if (bridgeSideRight) {
                    Rectangle2D tRectangle2D = MathUtil.inset(cRectangle2D, +halfWidth);
                    g2.draw(new Arc2D.Double(tRectangle2D.getX(), tRectangle2D.getY(),
                            tRectangle2D.getWidth(), tRectangle2D.getHeight(),
                            startAdj, tmpAngle, Arc2D.OPEN));
                }
            } else if (isBezier()) {
                Point2D[] points = getBezierPoints();
                if (bridgeSideLeft) {
                    MathUtil.drawBezier(g2, points, -halfWidth);
                }
                if (bridgeSideRight) {
                    MathUtil.drawBezier(g2, points, +halfWidth);
                }
            } else {
                Point2D delta = MathUtil.subtract(ep2, ep1);
                Point2D vector = MathUtil.normalize(delta, halfWidth);
                vector = MathUtil.orthogonal(vector);

                if (bridgeSideRight) {
                    Point2D ep1R = MathUtil.add(ep1, vector);
                    Point2D ep2R = MathUtil.add(ep2, vector);
                    g2.draw(new Line2D.Double(ep1R, ep2R));
                }

                if (bridgeSideLeft) {
                    Point2D ep1L = MathUtil.subtract(ep1, vector);
                    Point2D ep2L = MathUtil.subtract(ep2, vector);
                    g2.draw(new Line2D.Double(ep1L, ep2L));
                }
            }   // if isArc() {} else if isBezier() {} else...

            if (isFlip()) {
                boolean temp = bridgeSideRight;
                bridgeSideRight = bridgeSideLeft;
                bridgeSideLeft = temp;
            }

            if (bridgeHasEntry) {
                if (bridgeSideRight) {
                    p1 = new Point2D.Double(-bridgeApproachWidth, +bridgeApproachWidth + halfWidth);
                    p2 = new Point2D.Double(0.0, +halfWidth);
                    p1P = MathUtil.add(MathUtil.rotateRAD(p1, startAngleRAD), ep1);
                    p2P = MathUtil.add(MathUtil.rotateRAD(p2, startAngleRAD), ep1);
                    g2.draw(new Line2D.Double(p1P, p2P));
                }
                if (bridgeSideLeft) {
                    p1 = new Point2D.Double(-bridgeApproachWidth, -bridgeApproachWidth - halfWidth);
                    p2 = new Point2D.Double(0.0, -halfWidth);
                    p1P = MathUtil.add(MathUtil.rotateRAD(p1, startAngleRAD), ep1);
                    p2P = MathUtil.add(MathUtil.rotateRAD(p2, startAngleRAD), ep1);
                    g2.draw(new Line2D.Double(p1P, p2P));
                }
            }
            if (bridgeHasExit) {
                if (bridgeSideRight) {
                    p1 = new Point2D.Double(+bridgeApproachWidth, +bridgeApproachWidth + halfWidth);
                    p2 = new Point2D.Double(0.0, +halfWidth);
                    p1P = MathUtil.add(MathUtil.rotateRAD(p1, stopAngleRAD), ep2);
                    p2P = MathUtil.add(MathUtil.rotateRAD(p2, stopAngleRAD), ep2);
                    g2.draw(new Line2D.Double(p1P, p2P));
                }
                if (bridgeSideLeft) {
                    p1 = new Point2D.Double(+bridgeApproachWidth, -bridgeApproachWidth - halfWidth);
                    p2 = new Point2D.Double(0.0, -halfWidth);
                    p1P = MathUtil.add(MathUtil.rotateRAD(p1, stopAngleRAD), ep2);
                    p2P = MathUtil.add(MathUtil.rotateRAD(p2, stopAngleRAD), ep2);
                    g2.draw(new Line2D.Double(p1P, p2P));
                }
            }

            // if necessary flip these back
            if (isFlip()) {
                boolean temp = bridgeSideRight;
                bridgeSideRight = bridgeSideLeft;
                bridgeSideLeft = temp;
            }
        }

        //
        //  end bumper decorations
        //
        if (bumperEndStart || bumperEndStop) {
            g2.setStroke(new BasicStroke(bumperLineWidth,
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.F));
            g2.setColor(bumperColor);

            float halfLength = bumperLength / 2.F;

            if (bumperFlipped) {
                double temp = startAngleRAD;
                startAngleRAD = stopAngleRAD;
                stopAngleRAD = temp;
            }

            // common points
            p1 = new Point2D.Double(0.F, -halfLength);
            p2 = new Point2D.Double(0.F, +halfLength);

            if (bumperEndStart) {
                p1P = MathUtil.add(MathUtil.rotateRAD(p1, startAngleRAD), ep1);
                p2P = MathUtil.add(MathUtil.rotateRAD(p2, startAngleRAD), ep1);
                // draw cross tie
                g2.draw(new Line2D.Double(p1P, p2P));
            }
            if (bumperEndStop) {
                p1P = MathUtil.add(MathUtil.rotateRAD(p1, stopAngleRAD), ep2);
                p2P = MathUtil.add(MathUtil.rotateRAD(p2, stopAngleRAD), ep2);
                // draw cross tie
                g2.draw(new Line2D.Double(p1P, p2P));
            }
        }   // if (bumperEndStart || bumperEndStop)

        //
        //  tunnel decorations
        //
        if (tunnelSideRight || tunnelSideLeft) {
            float halfWidth = tunnelFloorWidth / 2.F;
            g2.setStroke(new BasicStroke(tunnelLineWidth,
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10.F,
                    new float[]{6.F, 4.F}, 0));
            g2.setColor(tunnelColor);

            if (isArc()) {
                calculateTrackSegmentAngle();
                Rectangle2D cRectangle2D = new Rectangle2D.Double(
                        getCX(), getCY(), getCW(), getCH());
                double startAngleDEG = getStartAdj(), extentAngleDEG = getTmpAngle();
                if (tunnelSideRight) {
                    Rectangle2D tRectangle2D = MathUtil.inset(cRectangle2D, +halfWidth);
                    g2.draw(new Arc2D.Double(tRectangle2D.getX(), tRectangle2D.getY(),
                            tRectangle2D.getWidth(), tRectangle2D.getHeight(),
                            startAngleDEG, extentAngleDEG, Arc2D.OPEN));
                }
                if (tunnelSideLeft) {
                    Rectangle2D tRectangle2D = MathUtil.inset(cRectangle2D, -halfWidth);
                    g2.draw(new Arc2D.Double(tRectangle2D.getX(), tRectangle2D.getY(),
                            tRectangle2D.getWidth(), tRectangle2D.getHeight(),
                            startAngleDEG, extentAngleDEG, Arc2D.OPEN));
                }
                trackRedrawn();
            } else if (isBezier()) {
                Point2D[] points = getBezierPoints();
                if (tunnelSideRight) {
                    MathUtil.drawBezier(g2, points, +halfWidth);
                }
                if (tunnelSideLeft) {
                    MathUtil.drawBezier(g2, points, -halfWidth);
                }
            } else {
                Point2D delta = MathUtil.subtract(ep2, ep1);
                Point2D vector = MathUtil.normalize(delta, halfWidth);
                vector = MathUtil.orthogonal(vector);

                if (tunnelSideRight) {
                    Point2D ep1L = MathUtil.add(ep1, vector);
                    Point2D ep2L = MathUtil.add(ep2, vector);
                    g2.draw(new Line2D.Double(ep1L, ep2L));
                }
                if (tunnelSideLeft) {
                    Point2D ep1R = MathUtil.subtract(ep1, vector);
                    Point2D ep2R = MathUtil.subtract(ep2, vector);
                    g2.draw(new Line2D.Double(ep1R, ep2R));
                }
            }   // if isArc() {} else if isBezier() {} else...

            g2.setStroke(new BasicStroke(tunnelLineWidth,
                    BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 1.F));
            g2.setColor(tunnelColor);

            // don't let tunnelEntranceWidth be less than tunnelFloorWidth + 6
            tunnelEntranceWidth = Math.max(tunnelEntranceWidth, tunnelFloorWidth + 6);

            double halfEntranceWidth = tunnelEntranceWidth / 2.0;
            double halfFloorWidth = tunnelFloorWidth / 2.0;
            double halfDiffWidth = halfEntranceWidth - halfFloorWidth;

            if (isFlip()) {
                boolean temp = tunnelSideRight;
                tunnelSideRight = tunnelSideLeft;
                tunnelSideLeft = temp;
            }

            if (tunnelHasEntry) {
                if (tunnelSideRight) {
                    p1 = new Point2D.Double(0.0, 0.0);
                    p2 = new Point2D.Double(0.0, +halfFloorWidth);
                    p3 = new Point2D.Double(0.0, +halfEntranceWidth);
                    p4 = new Point2D.Double(-halfEntranceWidth - halfFloorWidth, +halfEntranceWidth);
                    p5 = new Point2D.Double(-halfEntranceWidth - halfFloorWidth, +halfEntranceWidth - halfDiffWidth);
                    p6 = new Point2D.Double(-halfFloorWidth, +halfEntranceWidth - halfDiffWidth);
                    p7 = new Point2D.Double(-halfDiffWidth, 0.0);

                    p1P = MathUtil.add(MathUtil.rotateRAD(p1, startAngleRAD), ep1);
                    p2P = MathUtil.add(MathUtil.rotateRAD(p2, startAngleRAD), ep1);
                    p3P = MathUtil.add(MathUtil.rotateRAD(p3, startAngleRAD), ep1);
                    p4P = MathUtil.add(MathUtil.rotateRAD(p4, startAngleRAD), ep1);
                    p5P = MathUtil.add(MathUtil.rotateRAD(p5, startAngleRAD), ep1);
                    p6P = MathUtil.add(MathUtil.rotateRAD(p6, startAngleRAD), ep1);
                    p7P = MathUtil.add(MathUtil.rotateRAD(p7, startAngleRAD), ep1);

                    GeneralPath path = new GeneralPath();
                    path.moveTo(p1P.getX(), p1P.getY());
                    path.lineTo(p2P.getX(), p2P.getY());
                    path.quadTo(p3P.getX(), p3P.getY(), p4P.getX(), p4P.getY());
                    path.lineTo(p5P.getX(), p5P.getY());
                    path.quadTo(p6P.getX(), p6P.getY(), p7P.getX(), p7P.getY());
                    path.closePath();
                    g2.draw(path);
                }
                if (tunnelSideLeft) {
                    p1 = new Point2D.Double(0.0, 0.0);
                    p2 = new Point2D.Double(0.0, -halfFloorWidth);
                    p3 = new Point2D.Double(0.0, -halfEntranceWidth);
                    p4 = new Point2D.Double(-halfEntranceWidth - halfFloorWidth, -halfEntranceWidth);
                    p5 = new Point2D.Double(-halfEntranceWidth - halfFloorWidth, -halfEntranceWidth + halfDiffWidth);
                    p6 = new Point2D.Double(-halfFloorWidth, -halfEntranceWidth + halfDiffWidth);
                    p7 = new Point2D.Double(-halfDiffWidth, 0.0);

                    p1P = MathUtil.add(MathUtil.rotateRAD(p1, startAngleRAD), ep1);
                    p2P = MathUtil.add(MathUtil.rotateRAD(p2, startAngleRAD), ep1);
                    p3P = MathUtil.add(MathUtil.rotateRAD(p3, startAngleRAD), ep1);
                    p4P = MathUtil.add(MathUtil.rotateRAD(p4, startAngleRAD), ep1);
                    p5P = MathUtil.add(MathUtil.rotateRAD(p5, startAngleRAD), ep1);
                    p6P = MathUtil.add(MathUtil.rotateRAD(p6, startAngleRAD), ep1);
                    p7P = MathUtil.add(MathUtil.rotateRAD(p7, startAngleRAD), ep1);

                    GeneralPath path = new GeneralPath();
                    path.moveTo(p1P.getX(), p1P.getY());
                    path.lineTo(p2P.getX(), p2P.getY());
                    path.quadTo(p3P.getX(), p3P.getY(), p4P.getX(), p4P.getY());
                    path.lineTo(p5P.getX(), p5P.getY());
                    path.quadTo(p6P.getX(), p6P.getY(), p7P.getX(), p7P.getY());
                    path.closePath();
                    g2.draw(path);
                }
            }
            if (tunnelHasExit) {
                if (tunnelSideRight) {
                    p1 = new Point2D.Double(0.0, 0.0);
                    p2 = new Point2D.Double(0.0, +halfFloorWidth);
                    p3 = new Point2D.Double(0.0, +halfEntranceWidth);
                    p4 = new Point2D.Double(halfEntranceWidth + halfFloorWidth, +halfEntranceWidth);
                    p5 = new Point2D.Double(halfEntranceWidth + halfFloorWidth, +halfEntranceWidth - halfDiffWidth);
                    p6 = new Point2D.Double(halfFloorWidth, +halfEntranceWidth - halfDiffWidth);
                    p7 = new Point2D.Double(halfDiffWidth, 0.0);

                    p1P = MathUtil.add(MathUtil.rotateRAD(p1, stopAngleRAD), ep2);
                    p2P = MathUtil.add(MathUtil.rotateRAD(p2, stopAngleRAD), ep2);
                    p3P = MathUtil.add(MathUtil.rotateRAD(p3, stopAngleRAD), ep2);
                    p4P = MathUtil.add(MathUtil.rotateRAD(p4, stopAngleRAD), ep2);
                    p5P = MathUtil.add(MathUtil.rotateRAD(p5, stopAngleRAD), ep2);
                    p6P = MathUtil.add(MathUtil.rotateRAD(p6, stopAngleRAD), ep2);
                    p7P = MathUtil.add(MathUtil.rotateRAD(p7, stopAngleRAD), ep2);

                    GeneralPath path = new GeneralPath();
                    path.moveTo(p1P.getX(), p1P.getY());
                    path.lineTo(p2P.getX(), p2P.getY());
                    path.quadTo(p3P.getX(), p3P.getY(), p4P.getX(), p4P.getY());
                    path.lineTo(p5P.getX(), p5P.getY());
                    path.quadTo(p6P.getX(), p6P.getY(), p7P.getX(), p7P.getY());
                    path.closePath();
                    g2.draw(path);
                }
                if (tunnelSideLeft) {
                    p1 = new Point2D.Double(0.0, 0.0);
                    p2 = new Point2D.Double(0.0, -halfFloorWidth);
                    p3 = new Point2D.Double(0.0, -halfEntranceWidth);
                    p4 = new Point2D.Double(halfEntranceWidth + halfFloorWidth, -halfEntranceWidth);
                    p5 = new Point2D.Double(halfEntranceWidth + halfFloorWidth, -halfEntranceWidth + halfDiffWidth);
                    p6 = new Point2D.Double(halfFloorWidth, -halfEntranceWidth + halfDiffWidth);
                    p7 = new Point2D.Double(halfDiffWidth, 0.0);

                    p1P = MathUtil.add(MathUtil.rotateRAD(p1, stopAngleRAD), ep2);
                    p2P = MathUtil.add(MathUtil.rotateRAD(p2, stopAngleRAD), ep2);
                    p3P = MathUtil.add(MathUtil.rotateRAD(p3, stopAngleRAD), ep2);
                    p4P = MathUtil.add(MathUtil.rotateRAD(p4, stopAngleRAD), ep2);
                    p5P = MathUtil.add(MathUtil.rotateRAD(p5, stopAngleRAD), ep2);
                    p6P = MathUtil.add(MathUtil.rotateRAD(p6, stopAngleRAD), ep2);
                    p7P = MathUtil.add(MathUtil.rotateRAD(p7, stopAngleRAD), ep2);

                    GeneralPath path = new GeneralPath();
                    path.moveTo(p1P.getX(), p1P.getY());
                    path.lineTo(p2P.getX(), p2P.getY());
                    path.quadTo(p3P.getX(), p3P.getY(), p4P.getX(), p4P.getY());
                    path.lineTo(p5P.getX(), p5P.getY());
                    path.quadTo(p6P.getX(), p6P.getY(), p7P.getX(), p7P.getY());
                    path.closePath();
                    g2.draw(path);
                }
            }

            // if necessary, put these back
            if (isFlip()) {
                boolean temp = tunnelSideRight;
                tunnelSideRight = tunnelSideLeft;
                tunnelSideLeft = temp;
            }
        }
    }   // drawDecorations

    /*
     * getBezierPoints
     * @return the points to pass to MathUtil.drawBezier(...)
     */
    private Point2D[] getBezierPoints() {
        Point2D ep1 = LayoutEditor.getCoords(getConnect1(), getType1());
        Point2D ep2 = LayoutEditor.getCoords(getConnect2(), getType2());
        int cnt = bezierControlPoints.size() + 2;
        Point2D[] points = new Point2D[cnt];
        points[0] = ep1;
        for (int idx = 0; idx < cnt - 2; idx++) {
            points[idx + 1] = bezierControlPoints.get(idx);
        }
        points[cnt - 1] = ep2;
        return points;
    }

    private int drawArrow(
            Graphics2D g2,
            Point2D ep,
            double angleRAD,
            boolean dirOut,
            int offset) {
        Point2D p1, p2, p3, p4, p5, p6;
        switch (arrowStyle) {
            default: {
                arrowStyle = 0;
                break;
            }
            case 0: {
                break;
            }
            case 1: {
                if (dirOut) {
                    p1 = new Point2D.Double(offset, -arrowLength);
                    p2 = new Point2D.Double(offset + arrowLength, 0.0);
                    p3 = new Point2D.Double(offset, +arrowLength);
                } else {
                    p1 = new Point2D.Double(offset + arrowLength, -arrowLength);
                    p2 = new Point2D.Double(offset, 0.0);
                    p3 = new Point2D.Double(offset + arrowLength, +arrowLength);
                }
                p1 = MathUtil.add(MathUtil.rotateRAD(p1, angleRAD), ep);
                p2 = MathUtil.add(MathUtil.rotateRAD(p2, angleRAD), ep);
                p3 = MathUtil.add(MathUtil.rotateRAD(p3, angleRAD), ep);

                g2.draw(new Line2D.Double(p1, p2));
                g2.draw(new Line2D.Double(p2, p3));
                offset += arrowLength + arrowGap;
                break;
            }
            case 2: {
                if (dirOut) {
                    p1 = new Point2D.Double(offset, -arrowLength);
                    p2 = new Point2D.Double(offset + arrowLength, 0.0);
                    p3 = new Point2D.Double(offset, +arrowLength);
                    p4 = new Point2D.Double(offset + arrowLineWidth + arrowGap, -arrowLength);
                    p5 = new Point2D.Double(offset + arrowLineWidth + arrowGap + arrowLength, 0.0);
                    p6 = new Point2D.Double(offset + arrowLineWidth + arrowGap, +arrowLength);
                } else {
                    p1 = new Point2D.Double(offset + arrowLength, -arrowLength);
                    p2 = new Point2D.Double(offset, 0.0);
                    p3 = new Point2D.Double(offset + arrowLength, +arrowLength);
                    p4 = new Point2D.Double(offset + arrowLineWidth + arrowGap + arrowLength, -arrowLength);
                    p5 = new Point2D.Double(offset + arrowLineWidth + arrowGap, 0.0);
                    p6 = new Point2D.Double(offset + arrowLineWidth + arrowGap + arrowLength, +arrowLength);
                }
                p1 = MathUtil.add(MathUtil.rotateRAD(p1, angleRAD), ep);
                p2 = MathUtil.add(MathUtil.rotateRAD(p2, angleRAD), ep);
                p3 = MathUtil.add(MathUtil.rotateRAD(p3, angleRAD), ep);
                p4 = MathUtil.add(MathUtil.rotateRAD(p4, angleRAD), ep);
                p5 = MathUtil.add(MathUtil.rotateRAD(p5, angleRAD), ep);
                p6 = MathUtil.add(MathUtil.rotateRAD(p6, angleRAD), ep);

                g2.draw(new Line2D.Double(p1, p2));
                g2.draw(new Line2D.Double(p2, p3));
                g2.draw(new Line2D.Double(p4, p5));
                g2.draw(new Line2D.Double(p5, p6));
                offset += arrowLength + (2 * (arrowLineWidth + arrowGap));
                break;
            }
            case 3: {
                if (dirOut) {
                    p1 = new Point2D.Double(offset, -arrowLength);
                    p2 = new Point2D.Double(offset + arrowLength, 0.0);
                    p3 = new Point2D.Double(offset, +arrowLength);
                } else {
                    p1 = new Point2D.Double(offset + arrowLength, -arrowLength);
                    p2 = new Point2D.Double(offset, 0.0);
                    p3 = new Point2D.Double(offset + arrowLength, +arrowLength);
                }
                p1 = MathUtil.add(MathUtil.rotateRAD(p1, angleRAD), ep);
                p2 = MathUtil.add(MathUtil.rotateRAD(p2, angleRAD), ep);
                p3 = MathUtil.add(MathUtil.rotateRAD(p3, angleRAD), ep);

                GeneralPath path = new GeneralPath();
                path.moveTo(p1.getX(), p1.getY());
                path.lineTo(p2.getX(), p2.getY());
                path.lineTo(p3.getX(), p3.getY());
                path.closePath();
                if (arrowLineWidth > 1) {
                    g2.fill(path);
                } else {
                    g2.draw(path);
                }
                offset += arrowLength + arrowGap;
                break;
            }
            case 4: {
                if (dirOut) {
                    p1 = new Point2D.Double(offset, 0.0);
                    p2 = new Point2D.Double(offset + (2 * arrowLength), -arrowLength);
                    p3 = new Point2D.Double(offset + (3 * arrowLength), 0.0);
                    p4 = new Point2D.Double(offset + (2 * arrowLength), +arrowLength);
                } else {
                    p1 = new Point2D.Double(offset, 0.0);
                    p2 = new Point2D.Double(offset + (4 * arrowLength), -arrowLength);
                    p3 = new Point2D.Double(offset + (3 * arrowLength), 0.0);
                    p4 = new Point2D.Double(offset + (4 * arrowLength), +arrowLength);
                }
                p1 = MathUtil.add(MathUtil.rotateRAD(p1, angleRAD), ep);
                p2 = MathUtil.add(MathUtil.rotateRAD(p2, angleRAD), ep);
                p3 = MathUtil.add(MathUtil.rotateRAD(p3, angleRAD), ep);
                p4 = MathUtil.add(MathUtil.rotateRAD(p4, angleRAD), ep);

                g2.draw(new Line2D.Double(p1, p3));
                g2.draw(new Line2D.Double(p2, p3));
                g2.draw(new Line2D.Double(p3, p4));

                offset += (3 * arrowLength) + arrowGap;
                break;
            }
            case 5: {
                if (dirOut) {
                    p1 = new Point2D.Double(offset, 0.0);
                    p2 = new Point2D.Double(offset + (2 * arrowLength), -arrowLength);
                    p3 = new Point2D.Double(offset + (3 * arrowLength), 0.0);
                    p4 = new Point2D.Double(offset + (2 * arrowLength), +arrowLength);
                } else {
                    p1 = new Point2D.Double(offset, 0.0);
                    p2 = new Point2D.Double(offset + (4 * arrowLength), -arrowLength);
                    p3 = new Point2D.Double(offset + (3 * arrowLength), 0.0);
                    p4 = new Point2D.Double(offset + (4 * arrowLength), +arrowLength);
                }
                p1 = MathUtil.add(MathUtil.rotateRAD(p1, angleRAD), ep);
                p2 = MathUtil.add(MathUtil.rotateRAD(p2, angleRAD), ep);
                p3 = MathUtil.add(MathUtil.rotateRAD(p3, angleRAD), ep);
                p4 = MathUtil.add(MathUtil.rotateRAD(p4, angleRAD), ep);

                GeneralPath path = new GeneralPath();
                path.moveTo(p4.getX(), p4.getY());
                path.lineTo(p2.getX(), p2.getY());
                path.lineTo(p3.getX(), p3.getY());
                path.closePath();
                if (arrowLineWidth > 1) {
                    g2.fill(path);
                } else {
                    g2.draw(path);
                }
                g2.draw(new Line2D.Double(p1, p3));

                offset += (3 * arrowLength) + arrowGap;
                break;
            }
        }
        return offset;
    }   // drawArrow

    /*======================*\
    |* decoration accessors *|
    \*======================*/
    @Override
    public boolean hasDecorations() {
        return ((arrowStyle > 0)
                || (bridgeSideLeft || bridgeSideRight)
                || (bumperEndStart || bumperEndStop)
                || (tunnelSideLeft || tunnelSideRight));
    }

    /**
     * Get decorations.
     *
     * @return decorations to set
     */
    @Override
    public Map<String, String> getDecorations() {
        if (decorations == null) {
            decorations = new HashMap<>();
        } //if (decorathions != null)

        //
        // arrow decorations
        //
        if (arrowStyle > 0) {
            // <decoration name="arrow" value="double;both;linewidth=1;length=12;gap=1" />
            List<String> arrowValues = new ArrayList<String>();

            arrowValues.add("style=" + arrowStyle);

            if (arrowEndStart && arrowEndStop) {
                // default behaviour is both
            } else if (arrowEndStop) {
                arrowValues.add("stop");
            } else {
                arrowEndStart = true;
                arrowValues.add("start");
            }

            if (arrowDirIn && !arrowDirOut) {
                arrowValues.add("in");
            } else if (!arrowDirIn && arrowDirOut) {
                arrowValues.add("out");
            } else {
                arrowDirIn = true;
                arrowDirOut = true;
                arrowValues.add("both");
            }
            arrowValues.add("color=" + ColorUtil.colorToHexString(arrowColor));
            arrowValues.add("linewidth=" + arrowLineWidth);
            arrowValues.add("length=" + arrowLength);
            arrowValues.add("gap=" + arrowGap);
            decorations.put("arrow", String.join(";", arrowValues));
        }   // if (arrowCount > 0)

        //
        //  bridge decorations
        //
        if (bridgeSideLeft || bridgeSideRight) {
            // <decoration name="bridge" value="both;linewidth=2;deckwidth=8" />
            List<String> bridgeValues = new ArrayList<String>();

            if (bridgeHasEntry && !bridgeHasExit) {
                bridgeValues.add("entry");
            } else if (!bridgeHasEntry && bridgeHasExit) {
                bridgeValues.add("exit");
            } else if (bridgeHasEntry && bridgeHasExit) {
                bridgeValues.add("both");
            }
            if (bridgeSideLeft && !bridgeSideRight) {
                bridgeValues.add("left");
            } else if (!bridgeSideLeft && bridgeSideRight) {
                bridgeValues.add("right");
            }
            bridgeValues.add("color=" + ColorUtil.colorToHexString(bridgeColor));
            bridgeValues.add("linewidth=" + bridgeLineWidth);
            bridgeValues.add("approachwidth=" + bridgeApproachWidth);
            bridgeValues.add("deckwidth=" + bridgeDeckWidth);

            decorations.put("bridge", String.join(";", bridgeValues));
        }   // if (bridgeSideLeft || bridgeSideRight)

        //
        //  end bumper decorations
        //
        if (bumperEndStart || bumperEndStop) {
            // <decoration name="bumper" value="double;linewidth=2;length=6;gap=2;flipped" />
            List<String> bumperValues = new ArrayList<String>();
            if (bumperEndStart) {
                bumperValues.add("start");
            } else if (bumperEndStop) {
                bumperValues.add("stop");
            }

            if (bumperFlipped) {
                bumperValues.add("flip");
            }
            bumperValues.add("color=" + ColorUtil.colorToHexString(bumperColor));
            bumperValues.add("length=" + bumperLength);
            bumperValues.add("linewidth=" + bumperLineWidth);

            decorations.put("bumper", String.join(";", bumperValues));
        }   // if (bumperCount > 0)

        //
        //  tunnel decorations
        //
        if (tunnelSideLeft || tunnelSideRight) {
            // <decoration name="tunnel" value="both;linewidth=2;floorwidth=8" />
            List<String> tunnelValues = new ArrayList<String>();

            if (tunnelHasEntry && !tunnelHasExit) {
                tunnelValues.add("entry");
            } else if (!tunnelHasEntry && tunnelHasExit) {
                tunnelValues.add("exit");
            } else if (tunnelHasEntry && tunnelHasExit) {
                tunnelValues.add("both");
            }

            if (tunnelSideLeft && !tunnelSideRight) {
                tunnelValues.add("left");
            } else if (tunnelSideLeft && !tunnelSideRight) {
                tunnelValues.add("right");
            }
            tunnelValues.add("color=" + ColorUtil.colorToHexString(tunnelColor));
            tunnelValues.add("linewidth=" + tunnelLineWidth);
            tunnelValues.add("entrancewidth=" + tunnelEntranceWidth);
            tunnelValues.add("floorwidth=" + tunnelFloorWidth);

            decorations.put("tunnel", String.join(";", tunnelValues));
        }   // if (tunnelSideLeft || tunnelSideRight)
        return decorations;
    } // getDecorations

    /**
     * Set decorations.
     *
     * @param decorations to set
     */
    @Override
    public void setDecorations(Map<String, String> decorations) {
        Color defaultTrackColor = layoutEditor.getDefaultTrackColorColor();
        super.setDecorations(decorations);
        if (decorations != null) {
            for (Map.Entry<String, String> entry : decorations.entrySet()) {
                log.debug("Key = {}, Value = {}", entry.getKey(), entry.getValue());
                String key = entry.getKey();
                //
                // arrow decorations
                //
                if (key.equals("arrow")) {
                    String arrowValue = entry.getValue();
                    // <decoration name="arrow" value="double;both;linewidth=1;length=12;gap=1" />
                    boolean atStart = true, atStop = true;
                    boolean hasIn = false, hasOut = false;
                    int lineWidth = 1, length = 3, gap = 1, count = 1;
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
                        } else if (value.startsWith("style=")) {
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
                        } else if (value.startsWith("linewidth=")) {
                            String valueString = value.substring(value.lastIndexOf("=") + 1);
                            lineWidth = Integer.parseInt(valueString);
                        } else if (value.startsWith("length=")) {
                            String valueString = value.substring(value.lastIndexOf("=") + 1);
                            length = Integer.parseInt(valueString);
                        } else if (value.startsWith("gap=")) {
                            String valueString = value.substring(value.lastIndexOf("=") + 1);
                            gap = Integer.parseInt(valueString);
                        } else {
                            log.debug("arrow value ignored: {}", value);
                        }
                    }
                    hasIn |= !hasOut;   // if hasOut is false make hasIn true
                    if (!atStart && !atStop) {   // if both false
                        atStart = true; // set both true
                        atStop = true;
                    }
                    setArrowEndStart(atStart);
                    setArrowEndStop(atStop);
                    setArrowDirIn(hasIn);
                    setArrowDirOut(hasOut);
                    setArrowColor(color);
                    setArrowLineWidth(lineWidth);
                    setArrowLength(length);
                    setArrowGap(gap);
                    // set count last so it will fix ends and dir (if necessary)
                    setArrowStyle(count);
                } // if (key.equals("arrow")) {
                //
                //  bridge decorations
                //
                else if (key.equals("bridge")) {
                    String bridgeValue = entry.getValue();
                    // <decoration name="bridge" value="both;linewidth=2;deckwidth=8" />
                    // right/left default true; in/out default false
                    boolean hasLeft = true, hasRight = true, hasEntry = false, hasExit = false;
                    int approachWidth = 4, lineWidth = 1, deckWidth = 2;
                    Color color = defaultTrackColor;
                    String[] values = bridgeValue.split(";");
                    for (int i = 0; i < values.length; i++) {
                        String value = values[i];
                        //log.info("value[{}]: \"{}\"", i, value);
                        if (value.equals("left")) {
                            hasRight = false;
                        } else if (value.equals("right")) {
                            hasLeft = false;
                        } else if (value.equals("entry")) {
                            hasEntry = true;
                        } else if (value.equals("exit")) {
                            hasExit = true;
                        } else if (value.equals("both")) {
                            hasEntry = true;
                            hasExit = true;
                        } else if (value.startsWith("color=")) {
                            String valueString = value.substring(value.lastIndexOf("=") + 1);
                            color = Color.decode(valueString);
                        } else if (value.startsWith("approachwidth=")) {
                            String valueString = value.substring(value.lastIndexOf("=") + 1);
                            approachWidth = Integer.parseInt(valueString);
                        } else if (value.startsWith("linewidth=")) {
                            String valueString = value.substring(value.lastIndexOf("=") + 1);
                            lineWidth = Integer.parseInt(valueString);
                        } else if (value.startsWith("deckwidth=")) {
                            String valueString = value.substring(value.lastIndexOf("=") + 1);
                            deckWidth = Integer.parseInt(valueString);
                        } else {
                            log.debug("bridge value ignored: {}", value);
                        }
                    }
                    // these both can't be false
                    if (!hasLeft && !hasRight) {
                        hasLeft = true;
                        hasRight = true;
                    }
                    setBridgeSideRight(hasRight);
                    setBridgeSideLeft(hasLeft);
                    setBridgeHasEntry(hasEntry);
                    setBridgeHasExit(hasExit);
                    setBridgeColor(color);
                    setBridgeDeckWidth(deckWidth);
                    setBridgeLineWidth(lineWidth);
                    setBridgeApproachWidth(approachWidth);
                } // if (key.equals("bridge")) {
                //
                //  bumper decorations
                //
                else if (key.equals("bumper")) {
                    String bumperValue = entry.getValue();
//                    if (getName().equals("T15")) {
//                        log.debug("STOP");
//                    }
                    // <decoration name="bumper" value="double;linewidth=2;length=6;gap=2;flipped" />
                    int lineWidth = 1, length = 4;
                    boolean isFlipped = false, atStart = true, atStop = true;
                    Color color = defaultTrackColor;
                    String[] values = bumperValue.split(";");
                    for (int i = 0; i < values.length; i++) {
                        String value = values[i];
                        //log.info("value[{}]: \"{}\"", i, value);
                        if (value.equals("start")) {
                            atStop = false;
                        } else if (value.equals("stop")) {
                            atStart = false;
                        } else if (value.equals("both")) {
                            // this is the default behaviour; parameter ignored
                        } else if (value.equals("flip")) {
                            isFlipped = true;
                        } else if (value.startsWith("color=")) {
                            String valueString = value.substring(value.lastIndexOf("=") + 1);
                            color = Color.decode(valueString);
                        } else if (value.startsWith("linewidth=")) {
                            String valueString = value.substring(value.lastIndexOf("=") + 1);
                            lineWidth = Integer.parseInt(valueString);
                        } else if (value.startsWith("length=")) {
                            String valueString = value.substring(value.lastIndexOf("=") + 1);
                            length = Integer.parseInt(valueString);
                        } else {
                            log.debug("bumper value ignored: " + value);
                        }
                    }
                    atStop |= !atStart;   // if atStart is false make atStop true
                    setBumperEndStart(atStart);
                    setBumperEndStop(atStop);
                    setBumperColor(color);
                    setBumperLineWidth(lineWidth);
                    setBumperLength(length);
                    setBumperFlipped(isFlipped);
                } // if (key.equals("bumper")) {
                //
                //  tunnel decorations
                //
                else if (key.equals("tunnel")) {
                    String tunnelValue = entry.getValue();
                    // <decoration name="tunnel" value="both;linewidth=2;floorwidth=8" />
                    // right/left default true; in/out default false
                    boolean hasLeft = true, hasRight = true, hasIn = false, hasOut = false;
                    int entranceWidth = 4, lineWidth = 1, floorWidth = 2;
                    Color color = defaultTrackColor;
                    String[] values = tunnelValue.split(";");
                    for (int i = 0; i < values.length; i++) {
                        String value = values[i];
                        //log.info("value[{}]: \"{}\"", i, value);
                        if (value.equals("left")) {
                            hasRight = false;
                        } else if (value.equals("right")) {
                            hasLeft = false;
                        } else if (value.equals("entry")) {
                            hasIn = true;
                        } else if (value.equals("exit")) {
                            hasOut = true;
                        } else if (value.equals("both")) {
                            hasIn = true;
                            hasOut = true;
                        } else if (value.startsWith("color=")) {
                            String valueString = value.substring(value.lastIndexOf("=") + 1);
                            color = Color.decode(valueString);
                        } else if (value.startsWith("entrancewidth=")) {
                            String valueString = value.substring(value.lastIndexOf("=") + 1);
                            entranceWidth = Integer.parseInt(valueString);
                        } else if (value.startsWith("linewidth=")) {
                            String valueString = value.substring(value.lastIndexOf("=") + 1);
                            lineWidth = Integer.parseInt(valueString);
                        } else if (value.startsWith("floorwidth=")) {
                            String valueString = value.substring(value.lastIndexOf("=") + 1);
                            floorWidth = Integer.parseInt(valueString);
                        } else {
                            log.debug("tunnel value ignored: " + value);
                        }
                    }
                    // these both can't be false
                    if (!hasLeft && !hasRight) {
                        hasLeft = true;
                        hasRight = true;
                    }
                    setTunnelSideRight(hasRight);
                    setTunnelSideLeft(hasLeft);
                    setTunnelHasEntry(hasIn);
                    setTunnelHasExit(hasOut);
                    setTunnelColor(color);
                    setTunnelEntranceWidth(entranceWidth);
                    setTunnelLineWidth(lineWidth);
                    setTunnelFloorWidth(floorWidth);
                } // if (tunnelValue != null)
                else {
                    log.debug("Unknown decoration key: " + key + ", value: " + entry.getValue());
                }
            }   // for (Map.Entry<String, String> entry : decorations.entrySet())
        } //if (decorathions != null)
    }   // setDirections

    //
    //  arrow decoration accessors
    //
    public int getArrowStyle() {
        return arrowStyle;
    }

    public void setArrowStyle(int newVal) {
        if (arrowStyle != newVal) {
            if (newVal > 0) {
                if (!arrowEndStart && !arrowEndStop) {
                    arrowEndStart = true;
                    arrowEndStop = true;
                }
                if (!arrowDirIn && !arrowDirOut) {
                    arrowDirOut = true;
                }
            } else {
                newVal = 0; // only positive styles allowed!
            }
            arrowStyle = newVal;
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private int arrowStyle = 0;

    public boolean isArrowEndStart() {
        return arrowEndStart;
    }

    public void setArrowEndStart(boolean newVal) {
        if (arrowEndStart != newVal) {
            arrowEndStart = newVal;
            if (!arrowEndStart && !arrowEndStop) {
                arrowStyle = 0;
            } else if (arrowStyle == 0) {
                arrowStyle = 1;
            }
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private boolean arrowEndStart = false;

    public boolean isArrowEndStop() {
        return arrowEndStop;
    }

    public void setArrowEndStop(boolean newVal) {
        if (arrowEndStop != newVal) {
            arrowEndStop = newVal;
            if (!arrowEndStart && !arrowEndStop) {
                arrowStyle = 0;
            } else if (arrowStyle == 0) {
                arrowStyle = 1;
            }
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private boolean arrowEndStop = false;

    public boolean isArrowDirIn() {
        return arrowDirIn;
    }

    public void setArrowDirIn(boolean newVal) {
        if (arrowDirIn != newVal) {
            arrowDirIn = newVal;
            if (!arrowDirIn && !arrowDirOut) {
                arrowStyle = 0;
            } else if (arrowStyle == 0) {
                arrowStyle = 1;
            }
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private boolean arrowDirIn = false;

    public boolean isArrowDirOut() {
        return arrowDirOut;
    }

    public void setArrowDirOut(boolean newVal) {
        if (arrowDirOut != newVal) {
            arrowDirOut = newVal;
            if (!arrowDirIn && !arrowDirOut) {
                arrowStyle = 0;
            } else if (arrowStyle == 0) {
                arrowStyle = 1;
            }
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private boolean arrowDirOut = false;

    public Color getArrowColor() {
        return arrowColor;
    }

    public void setArrowColor(Color newVal) {
        if (arrowColor != newVal) {
            arrowColor = newVal;
            JmriColorChooser.addRecentColor(newVal);
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private Color arrowColor = Color.BLACK;

    public int getArrowLineWidth() {
        return arrowLineWidth;
    }

    public void setArrowLineWidth(int newVal) {
        if (arrowLineWidth != newVal) {
            arrowLineWidth = MathUtil.pin(newVal, 1, MAX_ARROW_LINE_WIDTH);
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private int arrowLineWidth = 4;

    public int getArrowLength() {
        return arrowLength;
    }

    public void setArrowLength(int newVal) {
        if (arrowLength != newVal) {
            arrowLength = MathUtil.pin(newVal, 2, MAX_ARROW_LENGTH);
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private int arrowLength = 4;

    public int getArrowGap() {
        return arrowGap;
    }

    public void setArrowGap(int newVal) {
        if (arrowGap != newVal) {
            arrowGap = MathUtil.pin(newVal, 0, MAX_ARROW_GAP);
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private int arrowGap = 1;

    //
    //  bridge decoration accessors
    //
    public boolean isBridgeSideRight() {
        return bridgeSideRight;
    }

    public void setBridgeSideRight(boolean newVal) {
        if (bridgeSideRight != newVal) {
            bridgeSideRight = newVal;
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private boolean bridgeSideRight = false;

    public boolean isBridgeSideLeft() {
        return bridgeSideLeft;
    }

    public void setBridgeSideLeft(boolean newVal) {
        if (bridgeSideLeft != newVal) {
            bridgeSideLeft = newVal;
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private boolean bridgeSideLeft = false;

    public boolean isBridgeHasEntry() {
        return bridgeHasEntry;
    }

    public void setBridgeHasEntry(boolean newVal) {
        if (bridgeHasEntry != newVal) {
            bridgeHasEntry = newVal;
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private boolean bridgeHasEntry = false;

    public boolean isBridgeHasExit() {
        return bridgeHasExit;
    }

    public void setBridgeHasExit(boolean newVal) {
        if (bridgeHasExit != newVal) {
            bridgeHasExit = newVal;
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private boolean bridgeHasExit = false;

    public Color getBridgeColor() {
        return bridgeColor;
    }

    public void setBridgeColor(Color newVal) {
        if (bridgeColor != newVal) {
            bridgeColor = newVal;
            JmriColorChooser.addRecentColor(newVal);
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private Color bridgeColor = Color.BLACK;

    public int getBridgeDeckWidth() {
        return bridgeDeckWidth;
    }

    public void setBridgeDeckWidth(int newVal) {
        if (bridgeDeckWidth != newVal) {
            bridgeDeckWidth = Math.max(6, newVal);   // don't let value be less than 6
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private int bridgeDeckWidth = 10;

    public int getBridgeLineWidth() {
        return bridgeLineWidth;
    }

    public void setBridgeLineWidth(int newVal) {
        if (bridgeLineWidth != newVal) {
            bridgeLineWidth = Math.max(1, newVal);   // don't let value be less than 1
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private int bridgeLineWidth = 1;

    public int getBridgeApproachWidth() {
        return bridgeApproachWidth;
    }

    public void setBridgeApproachWidth(int newVal) {
        if (bridgeApproachWidth != newVal) {
            bridgeApproachWidth = Math.max(8, newVal);   // don't let value be less than 8
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private int bridgeApproachWidth = 4;

    //
    //  bumper decoration accessors
    //
    public boolean isBumperEndStart() {
        return bumperEndStart;
    }

    public void setBumperEndStart(boolean newVal) {
        if (bumperEndStart != newVal) {
            bumperEndStart = newVal;
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private boolean bumperEndStart = false;

    public boolean isBumperEndStop() {
        return bumperEndStop;
    }

    public void setBumperEndStop(boolean newVal) {
        if (bumperEndStop != newVal) {
            bumperEndStop = newVal;
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private boolean bumperEndStop = false;

    public Color getBumperColor() {
        return bumperColor;
    }

    public void setBumperColor(Color newVal) {
        if (bumperColor != newVal) {
            bumperColor = newVal;
            JmriColorChooser.addRecentColor(newVal);
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private Color bumperColor = Color.BLACK;

    public int getBumperLineWidth() {
        return bumperLineWidth;
    }

    public void setBumperLineWidth(int newVal) {
        if (bumperLineWidth != newVal) {
            bumperLineWidth = MathUtil.pin(newVal, 1, MAX_BUMPER_LINE_WIDTH);
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private int bumperLineWidth = 2;

    private void setupDefaultBumperSizes(LayoutEditor layoutEditor) {
        LayoutTrackDrawingOptions ltdo = layoutEditor.getLayoutTrackDrawingOptions();

        // use these as default sizes for end bumpers
        int tieLength = ltdo.getSideTieLength();
        int tieWidth = ltdo.getSideTieWidth();
        int railWidth = ltdo.getSideRailWidth();
        int railGap = ltdo.getSideRailGap();
        if (mainline) {
            tieLength = ltdo.getMainTieLength();
            tieWidth = ltdo.getMainTieWidth();
            railWidth = ltdo.getMainRailWidth();
            railGap = ltdo.getMainRailGap();
        }
        bumperLineWidth = railWidth;
        bumperLength = (5 * (railGap + railWidth)) / 2;
        if ((tieLength > 0) && (tieWidth > 0)) {
            bumperLineWidth = tieWidth;
            bumperLength = tieLength * 3 / 2;
        }
        bumperLineWidth = Math.min(1, bumperLineWidth);
        bumperLength = Math.min(10, bumperLength);
    }

    public int getBumperLength() {
        return bumperLength;
    }

    public void setBumperLength(int newVal) {
        if (bumperLength != newVal) {
            bumperLength = Math.max(8, newVal);   // don't let value be less than 8
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private int bumperLength = 10;

    public boolean isBumperFlipped() {
        return bumperFlipped;
    }

    public void setBumperFlipped(boolean newVal) {
        if (bumperFlipped != newVal) {
            bumperFlipped = newVal;
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private boolean bumperFlipped = false;

    //
    //  tunnel decoration accessors
    //
    public boolean isTunnelSideRight() {
        return tunnelSideRight;
    }

    public void setTunnelSideRight(boolean newVal) {
        if (tunnelSideRight != newVal) {
            tunnelSideRight = newVal;
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private boolean tunnelSideRight = false;

    public boolean isTunnelSideLeft() {
        return tunnelSideLeft;
    }

    public void setTunnelSideLeft(boolean newVal) {
        if (tunnelSideLeft != newVal) {
            tunnelSideLeft = newVal;
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private boolean tunnelSideLeft = false;

    public boolean isTunnelHasEntry() {
        return tunnelHasEntry;
    }

    public void setTunnelHasEntry(boolean newVal) {
        if (tunnelHasEntry != newVal) {
            tunnelHasEntry = newVal;
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private boolean tunnelHasEntry = false;

    public boolean isTunnelHasExit() {
        return tunnelHasExit;
    }

    public void setTunnelHasExit(boolean newVal) {
        if (tunnelHasExit != newVal) {
            tunnelHasExit = newVal;
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private boolean tunnelHasExit = false;

    public Color getTunnelColor() {
        return tunnelColor;
    }

    public void setTunnelColor(Color newVal) {
        if (tunnelColor != newVal) {
            tunnelColor = newVal;
            JmriColorChooser.addRecentColor(newVal);
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private Color tunnelColor = Color.BLACK;

    public int getTunnelFloorWidth() {
        return tunnelFloorWidth;
    }

    public void setTunnelFloorWidth(int newVal) {
        if (tunnelFloorWidth != newVal) {
            tunnelFloorWidth = Math.max(4, newVal);   // don't let value be less than 4
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private int tunnelFloorWidth = 10;

    public int getTunnelLineWidth() {
        return tunnelLineWidth;
    }

    public void setTunnelLineWidth(int newVal) {
        if (tunnelLineWidth != newVal) {
            tunnelLineWidth = Math.max(1, newVal);   // don't let value be less than 1
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private int tunnelLineWidth = 1;

    public int getTunnelEntranceWidth() {
        return tunnelEntranceWidth;
    }

    public void setTunnelEntranceWidth(int newVal) {
        if (tunnelEntranceWidth != newVal) {
            tunnelEntranceWidth = Math.max(1, newVal);   // don't let value be less than 1
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }
    private int tunnelEntranceWidth = 16;

    /**
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
                                LayoutEditor.getCoords(getConnect2(), type2),
                                LayoutEditor.getCoords(getConnect1(), type1)));
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
                                LayoutEditor.getCoords(getConnect2(), type2),
                                LayoutEditor.getCoords(getConnect1(), type1)));
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
                    lc.setDirection(Path.computeDirection(LayoutEditor.getCoords(getConnect2(),
                            type2), LayoutEditor.getCoords(getConnect1(), type1)));
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
                                LayoutEditor.getCoords(getConnect1(), type1),
                                LayoutEditor.getCoords(getConnect2(), type2)));
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
                                LayoutEditor.getCoords(getConnect1(), type1),
                                LayoutEditor.getCoords(getConnect2(), type2)));
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
                            LayoutEditor.getCoords(getConnect1(), type1),
                            LayoutEditor.getCoords(getConnect2(), type2)));
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
        Set<String> TrackNameSet = null;    // assume not found (pessimist!)
        String blockName = getBlockName();
        if (!blockName.isEmpty()) {
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
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void collectContiguousTracksNamesInBlockNamed(@Nonnull String blockName,
            @Nonnull Set<String> TrackNameSet) {
        if (!TrackNameSet.contains(getName())) {
            // is this the blockName we're looking for?
            if (getBlockName().equals(blockName)) {
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
    @Override
    public void setAllLayoutBlocks(LayoutBlock layoutBlock) {
        setLayoutBlock(layoutBlock);
    }

    private final static Logger log = LoggerFactory.getLogger(TrackSegment.class);
}
