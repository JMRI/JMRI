package jmri.jmrit.display.layoutEditor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Arc2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRootPane;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import jmri.BlockManager;
import jmri.InstanceManager;
import jmri.Path;
import jmri.jmrit.display.layoutEditor.blockRoutingTable.LayoutBlockRouteTableAction;
import jmri.util.JmriJFrame;
import jmri.util.MathUtil;
import jmri.util.swing.JmriBeanComboBox;
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
 * @author Dave Duchamp Copyright (c) 2004-2009
 */
public class TrackSegment extends LayoutTrack {

    // Defined text resource
    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.layoutEditor.LayoutEditorBundle");

    // defined constants
    // operational instance variables (not saved between sessions)
    private LayoutBlock block = null;

    // persistent instances variables (saved between sessions)
    private String blockName = "";
    private LayoutTrack connect1 = null;
    private int type1 = 0;
    private LayoutTrack connect2 = null;
    private int type2 = 0;
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
        return "TrackSegment " + ident
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
        if ((block == null) && (blockName != null) && !blockName.isEmpty()) {
            block = layoutEditor.provideLayoutBlock(blockName);
        }
        return block;
    }

    public String getConnect1Name() {
        return getConnectName(connect1, type1);
    }

    public String getConnect2Name() {
        return getConnectName(connect2, type2);
    }

    private String getConnectName(@Nullable LayoutTrack o, int type) {
        String result = null;
        if (null != o) {
            result = ((LayoutTrack) o).getName();
        }
        return result;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation returns null because {@link #getConnect1} and
     * {@link #getConnect2} should be used instead.
     */
    // only implemented here to supress "does not override abstract method " error in compiler
    public LayoutTrack getConnection(int connectionType) throws jmri.JmriException {
        // nothing to do here, move along
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * This implementation does nothing because {@link #setNewConnect1} and
     * {@link #setNewConnect2} should be used instead.
     */
    // only implemented here to supress "does not override abstract method " error in compiler
    public void setConnection(int connectionType, @Nullable LayoutTrack o, int type) throws jmri.JmriException {
        // nothing to do here, move along
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
        block = b;
        if (b != null) {
            blockName = b.getId();
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
        // Nothing to do here, move along
    }

    /**
     * translate this LayoutTrack's coordinates by the x and y factors
     *
     * @param xFactor the amount to translate X coordinates
     * @param yFactor the amount to translate Y coordinates
     */
    public void translateCoords(float xFactor, float yFactor) {
        // Nothing to do here, move along
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
            block = p.getLayoutBlock(tBlockName);
            if (block != null) {
                blockName = tBlockName;
                block.incrementUse();
            } else {
                log.error("bad blockname '" + tBlockName + "' in tracksegment " + ident);
            }
        }

        //NOTE: testing "type-less" connects
        // (read comments for findObjectByName in LayoutEditorFindItems.java)
        connect1 = (LayoutTrack) p.getFinder().findObjectByName(tConnect1Name);
        if (null == connect1) { // findObjectByName failed... try findObjectByTypeAndName
            log.warn("Unknown connect1 object prefix: '" + tConnect1Name + "' of type " + type1 + ".");
            connect1 = (LayoutTrack) p.getFinder().findObjectByTypeAndName(type1, tConnect1Name);
        }
        connect2 = (LayoutTrack) p.getFinder().findObjectByName(tConnect2Name);
        if (null == connect2) { // findObjectByName failed; try findObjectByTypeAndName
            log.warn("Unknown connect2 object prefix: '" + tConnect2Name + "' of type " + type2 + ".");
            connect2 = (LayoutTrack) p.getFinder().findObjectByTypeAndName(type2, tConnect2Name);
        }
    }

    protected void updateBlockInfo() {
        if (block != null) {
            block.updatePaths();
        }
        LayoutBlock b1 = getBlock(connect1, type1);
        if ((b1 != null) && (b1 != block)) {
            b1.updatePaths();
        }
        LayoutBlock b2 = getBlock(connect2, type2);
        if ((b2 != null) && (b2 != block) && (b2 != b1)) {
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
            Rectangle2D r = layoutEditor.trackControlPointRectAt(hitPoint);

            if (isCircle()) {
                if (r.contains(getCoordsCenterCircle())) {
                    result = LayoutTrack.TRACK_CIRCLE_CENTRE;
                }
            } else if (isBezier()) {
                // hit testing for the control points
                // note: control points will override center circle
                for (int index = 0; index < bezierControlPoints.size(); index++) {
                    if (r.contains(getBezierControlPoint(index))) {
                        result = LayoutTrack.BEZIER_CONTROL_POINT_OFFSET_MIN + index;
                        break;
                    }
                }
            }
            if (result == NONE) {
                if (r.contains(getCentreSeg())) {
                    result = LayoutTrack.TRACK;
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

    JPopupMenu popup = null;

    /**
     * Display popup menu for information and editing.
     */
    protected void showPopup(MouseEvent e) {
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

        JMenuItem jmi = popup.add(Bundle.getMessage("MakeLabel", info) + ident);
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
                connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "1") + ((LayoutTrack) connect1).getName()) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        LayoutEditorFindItems lf = layoutEditor.getFinder();
                        LayoutTrack lt = (LayoutTrack) lf.findObjectByName(((LayoutTrack) connect1).getName());
                        layoutEditor.setSelectionRect(lt.getBounds());
                    }
                });
            }
            if (connect2 != null) {
                connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "2") + ((LayoutTrack) connect2).getName()) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        LayoutEditorFindItems lf = layoutEditor.getFinder();
                        LayoutTrack lt = (LayoutTrack) lf.findObjectByName(((LayoutTrack) connect2).getName());
                        layoutEditor.setSelectionRect(lt.getBounds());
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
                editTrackSegment();
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
        popup.show(e.getComponent(), e.getX(), e.getY());
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

                    // compute offset one third the distance from ep1 to ep2
                    Point2D offset = MathUtil.subtract(ep2, ep1);
                    offset = MathUtil.multiply(MathUtil.normalize(offset), MathUtil.length(offset) / 3);

                    // swap x & y so the offset is orthogonal to orginal line
                    offset = new Point2D.Double(offset.getY(), offset.getX());
                    Point2D pt1 = MathUtil.add(MathUtil.oneThirdPoint(ep1, ep2), offset);
                    Point2D pt2 = MathUtil.subtract(MathUtil.oneThirdPoint(ep2, ep1), offset);

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

    // variables for Edit Track Segment pane
    private JmriJFrame editTrackSegmentFrame = null;

    private JComboBox<String> mainlineBox = new JComboBox<String>();
    private int mainlineTrackIndex;
    private int sideTrackIndex;

    private JComboBox<String> dashedBox = new JComboBox<String>();
    private int dashedIndex;
    private int solidIndex;

    private JCheckBox hiddenBox = new JCheckBox(Bundle.getMessage("HideTrack"));

    private JCheckBoxMenuItem mainlineCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("Mainline"));
    private JCheckBoxMenuItem hiddenCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("Hidden"));
    private JCheckBoxMenuItem dashedCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("Dashed"));

    private JmriBeanComboBox blockNameComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(BlockManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JTextField arcField = new JTextField(5);
    private JButton segmentEditBlock;
    private JButton segmentEditDone;
    private JButton segmentEditCancel;
    private boolean editOpen = false;
    private boolean needsRedraw = false;

    /**
     * Edit a Track Segment.
     */
    protected void editTrackSegment() {
        if (editOpen) {
            editTrackSegmentFrame.setVisible(true);
            return;
        }
        // Initialize if needed
        if (editTrackSegmentFrame == null) {
            editTrackSegmentFrame = new JmriJFrame(Bundle.getMessage("EditTrackSegment"), false, true); // key moved to DisplayBundle to be found by CircuitBuilder.java
            editTrackSegmentFrame.addHelpMenu("package.jmri.jmrit.display.EditTrackSegment", true);
            editTrackSegmentFrame.setLocation(50, 30);
            Container contentPane = editTrackSegmentFrame.getContentPane();
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

            // add dashed choice
            JPanel panel31 = new JPanel();
            panel31.setLayout(new FlowLayout());
            dashedBox.removeAllItems();
            dashedBox.addItem(Bundle.getMessage("Solid"));
            solidIndex = 0;
            dashedBox.addItem(Bundle.getMessage("Dashed"));
            dashedIndex = 1;
            dashedBox.setToolTipText(Bundle.getMessage("DashedToolTip"));
            panel31.add(new JLabel(Bundle.getMessage("Style") + " : "));
            panel31.add(dashedBox);
            contentPane.add(panel31);

            // add mainline choice
            JPanel panel32 = new JPanel();
            panel32.setLayout(new FlowLayout());
            mainlineBox.removeAllItems();
            mainlineBox.addItem(Bundle.getMessage("Mainline"));
            mainlineTrackIndex = 0;
            mainlineBox.addItem(Bundle.getMessage("NotMainline"));
            sideTrackIndex = 1;
            mainlineBox.setToolTipText(Bundle.getMessage("MainlineToolTip"));
            panel32.add(mainlineBox);
            contentPane.add(panel32);

            // add hidden choice
            JPanel panel33 = new JPanel();
            panel33.setLayout(new FlowLayout());
            hiddenBox.setToolTipText(Bundle.getMessage("HiddenToolTip"));
            panel33.add(hiddenBox);
            contentPane.add(panel33);

            // setup block name
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
            JLabel blockNameLabel = new JLabel(Bundle.getMessage("BlockID"));
            panel2.add(blockNameLabel);
            LayoutEditor.setupComboBox(blockNameComboBox, false, true);
            blockNameComboBox.setToolTipText(Bundle.getMessage("EditBlockNameHint"));
            panel2.add(blockNameComboBox);

            contentPane.add(panel2);

            if (isArc() && isCircle()) {
                JPanel panel20 = new JPanel();
                panel20.setLayout(new FlowLayout());
                JLabel arcLabel = new JLabel("Set Arc Angle");
                panel20.add(arcLabel);
                panel20.add(arcField);
                arcField.setToolTipText("Set Arc Angle");
                contentPane.add(panel20);
                arcField.setText("" + getAngle());
            }

            // set up Edit Block, Done and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());

            // Edit Block
            panel5.add(segmentEditBlock = new JButton(Bundle.getMessage("EditBlock", "")));
            segmentEditBlock.addActionListener((ActionEvent e) -> {
                segmentEditBlockPressed(e);
            });
            segmentEditBlock.setToolTipText(Bundle.getMessage("EditBlockHint", "")); // empty value for block 1
            panel5.add(segmentEditDone = new JButton(Bundle.getMessage("ButtonDone")));
            segmentEditDone.addActionListener((ActionEvent e) -> {
                segmentEditDonePressed(e);
            });
            segmentEditDone.setToolTipText(Bundle.getMessage("DoneHint", Bundle.getMessage("ButtonDone")));

            // make this button the default button (return or enter activates)
            // Note: We have to invoke this later because we don't currently have a root pane
            SwingUtilities.invokeLater(() -> {
                JRootPane rootPane = SwingUtilities.getRootPane(segmentEditDone);
                rootPane.setDefaultButton(segmentEditDone);
            });

            // Cancel
            panel5.add(segmentEditCancel = new JButton(Bundle.getMessage("ButtonCancel")));
            segmentEditCancel.addActionListener((ActionEvent e) -> {
                segmentEditCancelPressed(e);
            });
            segmentEditCancel.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            contentPane.add(panel5);
        }
        // Set up for Edit
        if (mainline) {
            mainlineBox.setSelectedIndex(mainlineTrackIndex);
        } else {
            mainlineBox.setSelectedIndex(sideTrackIndex);
        }
        if (dashed) {
            dashedBox.setSelectedIndex(dashedIndex);
        } else {
            dashedBox.setSelectedIndex(solidIndex);
        }
        hiddenBox.setSelected(hidden);
        blockNameComboBox.setText(blockName);

        editTrackSegmentFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                segmentEditCancelPressed(null);
            }
        });
        editTrackSegmentFrame.pack();
        editTrackSegmentFrame.setVisible(true);
        editOpen = true;
    }

    void segmentEditBlockPressed(ActionEvent a) {
        // check if a block name has been entered
        String newName = blockNameComboBox.getUserName();
        if (!blockName.equals(newName)) {
            // block has changed, if old block exists, decrement use
            if (block != null) {
                block.decrementUse();
            }
            // get new block, or null if block has been removed
            blockName = newName;
            try {
                block = layoutEditor.provideLayoutBlock(blockName);
            } catch (IllegalArgumentException ex) {
                blockName = "";
            }
            needsRedraw = true;
            layoutEditor.getLEAuxTools().setBlockConnectivityChanged();
            updateBlockInfo();
        }
        // check if a block exists to edit
        if (block == null) {
            JOptionPane.showMessageDialog(editTrackSegmentFrame,
                    Bundle.getMessage("Error1"),
                    Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
            return;
        }
        block.editLayoutBlock(editTrackSegmentFrame);
        layoutEditor.setDirty();
        needsRedraw = true;
    }

    void segmentEditDonePressed(ActionEvent a) {
        // set dashed
        boolean oldDashed = dashed;
        setDashed(dashedBox.getSelectedIndex() == dashedIndex);

        // set mainline
        boolean oldMainline = mainline;
        setMainline(mainlineBox.getSelectedIndex() == mainlineTrackIndex);

        // set hidden
        boolean oldHidden = hidden;
        setHidden(hiddenBox.isSelected());

        if (isArc()) {
            //setAngle(Integer.parseInt(arcField.getText()));
            //needsRedraw = true;
            try {
                double newAngle = Double.parseDouble(arcField.getText());
                setAngle(newAngle);
                needsRedraw = true;
            } catch (NumberFormatException e) {
                arcField.setText("" + getAngle());
            }
        }
        // check if anything changed
        if ((oldDashed != dashed) || (oldMainline != mainline) || (oldHidden != hidden)) {
            needsRedraw = true;
        }
        // check if Block changed
        String newName = blockNameComboBox.getUserName();
        if (!blockName.equals(newName)) {
            // block has changed, if old block exists, decrement use
            if (block != null) {
                block.decrementUse();
            }
            // get new block, or null if block has been removed
            blockName = newName;
            try {
                block = layoutEditor.provideLayoutBlock(blockName);
            } catch (IllegalArgumentException ex) {
                blockName = "";
            }
            needsRedraw = true;
            layoutEditor.getLEAuxTools().setBlockConnectivityChanged();
            updateBlockInfo();
        }
        editOpen = false;
        editTrackSegmentFrame.setVisible(false);
        editTrackSegmentFrame.dispose();
        editTrackSegmentFrame = null;
        if (needsRedraw) {
            layoutEditor.redrawPanel();
        }
        layoutEditor.setDirty();
    }

    void segmentEditCancelPressed(ActionEvent a) {
        editOpen = false;
        editTrackSegmentFrame.setVisible(false);
        editTrackSegmentFrame.dispose();
        editTrackSegmentFrame = null;
        if (needsRedraw) {
            layoutEditor.setDirty();
            layoutEditor.redrawPanel();
        }
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
     * each time a re-drawHidden is required.
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

    //private int startadj;
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

    private double startadj;

    public double getStartadj() {
        return startadj;
    }

    public void setStartadj(double Startadj) {
        startadj = Startadj;
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

        if ((null != connect1) && (null != connect2)) {
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
                    points[idx] = getBezierControlPoint(idx - 1);
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
                setStartadj(Math.toDegrees(startRad));
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
                    setStartadj(Math.round(getStartadj() / 90.0D) * 90.0D);
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
    protected void draw(Graphics2D g2) {
        setColorForTrackBlock(g2, getLayoutBlock());

        if (isHidden()) {
            if (layoutEditor.isEditable()) {
                drawHidden(g2);
            }
        } else if (isDashed()) {
            drawDashed(g2);
        } else if (!isHidden()) {
            drawSolid(g2);
        }
    }

    private void drawHidden(Graphics2D g2) {
        g2.setStroke(new BasicStroke(1.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        g2.draw(new Line2D.Double(layoutEditor.getCoords(getConnect1(), getType1()),
                layoutEditor.getCoords(getConnect2(), getType2())));
    }   // drawHidden

    private void drawDashed(Graphics2D g2) {
        float trackWidth = layoutEditor.setTrackStrokeWidth(g2, mainline);
        if (isArc()) {
            calculateTrackSegmentAngle();
            Stroke originalStroke = g2.getStroke();
            Stroke drawingStroke = new BasicStroke(trackWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
            g2.setStroke(drawingStroke);
            g2.draw(new Arc2D.Double(getCX(), getCY(), getCW(), getCH(), getStartadj(), getTmpAngle(), Arc2D.OPEN));
            g2.setStroke(originalStroke);
        } else if (isBezier()) {
            Stroke originalStroke = g2.getStroke();
            Stroke drawingStroke = new BasicStroke(trackWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
            g2.setStroke(drawingStroke);

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

            g2.setStroke(originalStroke);
        } else {
            Point2D end1 = layoutEditor.getCoords(getConnect1(), getType1());
            Point2D end2 = layoutEditor.getCoords(getConnect2(), getType2());

            double delX = end1.getX() - end2.getX();
            double delY = end1.getY() - end2.getY();
            double cLength = Math.hypot(delX, delY);

            // note: The preferred dimension of a dash (solid + blank space) is
            //         5 * the track width - about 60% solid and 40% blank.
            int nDashes = (int) (cLength / ((trackWidth) * 5.0));
            if (nDashes < 3) {
                nDashes = 3;
            }
            double delXDash = -delX / ((nDashes) - 0.5);
            double delYDash = -delY / ((nDashes) - 0.5);
            double begX = end1.getX();
            double begY = end1.getY();
            for (int k = 0; k < nDashes; k++) {
                g2.draw(new Line2D.Double(new Point2D.Double(begX, begY),
                        new Point2D.Double(begX + (delXDash * 0.5), begY + (delYDash * 0.5))));
                begX += delXDash;
                begY += delYDash;
            }
        }
    }   // drawDashed

    private void drawSolid(Graphics2D g2) {
        if (isArc()) {
            calculateTrackSegmentAngle();
            g2.draw(new Arc2D.Double(getCX(), getCY(), getCW(), getCH(), getStartadj(), getTmpAngle(), Arc2D.OPEN));
        } else if (isBezier()) {
            Point2D pt0 = layoutEditor.getCoords(getConnect1(), getType1());
            Point2D pt3 = layoutEditor.getCoords(getConnect2(), getType2());

            Point2D pt1 = getBezierControlPoint(0);
            Point2D pt2 = getBezierControlPoint(1);
            MathUtil.drawBezier(g2, pt0, pt1, pt2, pt3);
        } else {
            Point2D end1 = layoutEditor.getCoords(getConnect1(), getType1());
            Point2D end2 = layoutEditor.getCoords(getConnect2(), getType2());
            g2.draw(new Line2D.Double(end1, end2));
        }
        trackRedrawn();
    }   // drawSolid

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
                g2.draw(layoutEditor.trackControlCircleAt(circleCenterPoint));
                g2.draw(layoutEditor.trackControlCircleRectAt(circleCenterPoint));
            } else if (isBezier()) {
                //draw construction lines and control circles
                Point2D lastPt = ep1;
                for (Point2D bcp : bezierControlPoints) {
                    g2.draw(new Line2D.Double(lastPt, bcp));
                    lastPt = bcp;
                    g2.draw(layoutEditor.trackControlPointRectAt(bcp));
                }
                g2.draw(new Line2D.Double(lastPt, ep2));
            }
        }
        g2.draw(layoutEditor.trackControlCircleAt(getCentreSeg()));
    }   // drawEditControls

    protected void drawTurnoutControls(Graphics2D g2) {
        // TrackSegments don't have turnout controls...
        // nothing to do here... move along...
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public void reCheckBlockBoundary() {
        // nothing to do here... move along...
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
            if ((type1 >= LayoutTrack.TURNOUT_A) && (type1 <= LayoutTrack.LEVEL_XING_D)) {
                // have connection to turnout or level crossing
                if (type1 <= LayoutTrack.TURNOUT_D) {
                    // have connection to a turnout, is block different
                    LayoutTurnout lt = (LayoutTurnout) getConnect1();
                    lb2 = lt.getLayoutBlock();
                    if (lt.getTurnoutType() > LayoutTurnout.WYE_TURNOUT) {
                        // not RH, LH, or WYE turnout - other blocks possible
                        if ((type1 == LayoutTrack.TURNOUT_B) && (lt.getLayoutBlockB() != null)) {
                            lb2 = lt.getLayoutBlockB();
                        }
                        if ((type1 == LayoutTrack.TURNOUT_C) && (lt.getLayoutBlockC() != null)) {
                            lb2 = lt.getLayoutBlockC();
                        }
                        if ((type1 == LayoutTrack.TURNOUT_D) && (lt.getLayoutBlockD() != null)) {
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
                    if ((type1 == LayoutTrack.LEVEL_XING_A) || (type1 == LayoutTrack.LEVEL_XING_C)) {
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
            } else if ((type1 >= LayoutTrack.SLIP_A) && (type1 <= LayoutTrack.SLIP_D)) {
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
            if ((type2 >= LayoutTrack.TURNOUT_A) && (type2 <= LayoutTrack.LEVEL_XING_D)) {
                // have connection to turnout or level crossing
                if (type2 <= LayoutTrack.TURNOUT_D) {
                    // have connection to a turnout
                    LayoutTurnout lt = (LayoutTurnout) getConnect2();
                    lb2 = lt.getLayoutBlock();
                    if (lt.getTurnoutType() > LayoutTurnout.WYE_TURNOUT) {
                        // not RH, LH, or WYE turnout - other blocks possible
                        if ((type2 == LayoutTrack.TURNOUT_B) && (lt.getLayoutBlockB() != null)) {
                            lb2 = lt.getLayoutBlockB();
                        }
                        if ((type2 == LayoutTrack.TURNOUT_C) && (lt.getLayoutBlockC() != null)) {
                            lb2 = lt.getLayoutBlockC();
                        }
                        if ((type2 == LayoutTrack.TURNOUT_D) && (lt.getLayoutBlockD() != null)) {
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
                    if ((type2 == LayoutTrack.LEVEL_XING_A) || (type2 == LayoutTrack.LEVEL_XING_C)) {
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
            } else if ((type2 >= LayoutTrack.SLIP_A) && (type2 <= LayoutTrack.SLIP_D)) {
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
                // (nothing to do here... move along)
            }
        }   // if (lb1 != null)
        return results;
    }   // getLayoutConnectivity()

    private final static Logger log = LoggerFactory.getLogger(TrackSegment.class);
}
