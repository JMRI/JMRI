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
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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
    private TrackSegment instance = null;
    private LayoutEditor layoutEditor = null;

    // persistent instances variables (saved between sessions)
    private String blockName = "";
    private Object connect1 = null;
    private int type1 = 0;
    private Object connect2 = null;
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
    private ArrayList<Point2D> bezierControlPoints = new ArrayList<Point2D>(); // list of control point displacements

    public TrackSegment(String id, Object c1, int t1, Object c2, int t2, boolean dash,
            boolean main, LayoutEditor myPanel) {
        layoutEditor = myPanel;
        // validate input
        if ((c1 == null) || (c2 == null)) {
            log.error("Invalid object in TrackSegment constructor call - " + id);
        }

        if (isConnectionType(t1)) {
            connect1 = c1;
            type1 = t1;
        } else {
            log.error("Invalid connect type 1 in TrackSegment constructor - " + id);
        }
        if (isConnectionType(t2)) {
            connect2 = c2;
            type2 = t2;
        } else {
            log.error("Invalid connect type 2 in TrackSegment constructor - " + id);
        }
        instance = this;
        ident = id;
        dashed = dash;
        mainline = main;
        arc = false;
        flip = false;
        angle = 0.0D;
        circle = false;
        bezier = false;
    }

    // alternate constructor for loading layout editor panels
    public TrackSegment(String id, String c1Name, int t1, String c2Name, int t2, boolean dash,
            boolean main, boolean hide, LayoutEditor myPanel) {
        layoutEditor = myPanel;
        tConnect1Name = c1Name;
        type1 = t1;
        tConnect2Name = c2Name;
        type2 = t2;
        instance = this;
        ident = id;
        dashed = dash;
        mainline = main;
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

    public Object getConnect1() {
        return connect1;
    }

    public Object getConnect2() {
        return connect2;
    }

    protected void setNewConnect1(Object o, int type) {
        connect1 = o;
        type1 = type;
    }

    protected void setNewConnect2(Object o, int type) {
        connect2 = o;
        type2 = type;
    }

    public boolean getDashed() {
        return dashed;
    }

    public void setDashed(boolean dash) {
        dashed = dash;
    }

    public boolean getMainline() {
        return mainline;
    }

    public void setMainline(boolean main) {
        mainline = main;
    }

    public boolean getArc() {
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

    public boolean getCircle() {
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

    public boolean getFlip() {
        return flip;
    }

    public void setFlip(boolean boo) {
        if (flip != boo) {
            flip = boo;
            changed = true;
        }
    }

    public boolean getBezier() {
        return bezier;
    }

    public void setBezier(boolean boo) {
        if (bezier != boo) {
            bezier = boo;
            if (getBezier()) {
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
        if ((block == null) && (blockName != null) && (!blockName.equals(""))) {
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

    private String getConnectName(Object o, int type) {
        String result = null;
        if (null != o) {
            result = ((LayoutTrack) o).getName();
        }
        return result;
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

    public void setBezierControlPoint(Point2D p, int index) {
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
        if (tBlockName.length() > 0) {
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
        connect1 = p.getFinder().findObjectByName(tConnect1Name);
        if (null == connect1) { // findObjectByName failed... try findObjectByTypeAndName
            log.warn("Unknown connect1 object prefix: '" + tConnect1Name + "' of type " + type1 + ".");
            connect1 = p.getFinder().findObjectByTypeAndName(type1, tConnect1Name);
        }
        connect2 = p.getFinder().findObjectByName(tConnect2Name);
        if (null == connect2) { // findObjectByName failed; try findObjectByTypeAndName
            log.warn("Unknown connect2 object prefix: '" + tConnect2Name + "' of type " + type1 + ".");
            connect2 = p.getFinder().findObjectByTypeAndName(type2, tConnect2Name);
        }
    }

    /**
     * Set Up a Layout Block for a Track Segment.
     */
    public void setLayoutBlock(LayoutBlock b) {
        block = b;
        if (b != null) {
            blockName = b.getID();
        }
    }

    public void setLayoutBlockByName(String name) {
        blockName = name;
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
        if (getConnect1() instanceof PositionablePoint) {
            ((PositionablePoint) getConnect1()).reCheckBlockBoundary();
        } else if (getConnect1() instanceof LayoutTurnout) {
            ((LayoutTurnout) getConnect1()).reCheckBlockBoundary();
        } else if (getConnect1() instanceof LevelXing) {
            ((LevelXing) getConnect1()).reCheckBlockBoundary();
        } else if (getConnect1() instanceof LayoutSlip) {
            ((LayoutSlip) getConnect1()).reCheckBlockBoundary();
        }

        if (getConnect2() instanceof PositionablePoint) {
            ((PositionablePoint) getConnect2()).reCheckBlockBoundary();
        } else if (getConnect2() instanceof LayoutTurnout) {
            ((LayoutTurnout) getConnect2()).reCheckBlockBoundary();
        } else if (getConnect2() instanceof LevelXing) {
            ((LevelXing) getConnect2()).reCheckBlockBoundary();
        } else if (getConnect2() instanceof LayoutSlip) {
            ((LayoutSlip) getConnect2()).reCheckBlockBoundary();
        }
    }

    private LayoutBlock getBlock(Object connect, int type) {
        if (connect == null) {
            return null;
        }
        if (type == POS_POINT) {
            PositionablePoint p = (PositionablePoint) connect;
            if (p.getConnect1() != instance) {
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
     * Find the hit (location) type for a point.
     *
     * @param p the point
     * @param useRectangles - whether to use (larger) rectangles or (smaller) circles for hit testing
     * @param requireUnconnected - whether to only return hit types for free connections
     * @return the location type for the point (or NONE)
     * @since 7.4.3
     */
    protected int findHitPointType(Point2D p, boolean useRectangles, boolean requireUnconnected) {
        int result = NONE;  // assume point not on connection

        if (!requireUnconnected) {
            //note: optimization here: instead of creating rectangles for all the
            // points to check below, we create a rectangle for the test point
            // and test if the points below are in that rectangle instead.
            Rectangle2D r = layoutEditor.trackControlPointRectAt(p);

            if (getCircle()) {
                if (r.contains(getCoordsCenterCircle())) {
                    result = LayoutTrack.TRACK_CIRCLE_CENTRE;
                }
            }

            if (getBezier()) {
                // hit testing for the control points
                // note: control points will override center circle
                for (int index = 0; index < bezierControlPoints.size(); index++) {
                    if (r.contains(getBezierControlPoint(index))) {
                        result = LayoutTrack.BEZIER_CONTROL_POINT_OFFSET_MIN + index;
                        break;
                    }
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

        Point2D ep1 = layoutEditor.getCoords(getConnect1(), getType1());
        result = new Rectangle2D.Double(ep1.getX(), ep1.getY(), 0, 0);
        Point2D ep2 = layoutEditor.getCoords(getConnect2(), getType2());
        result.add(ep2);

        return result;
    }

    JPopupMenu popup = null;

    /**
     * Display popup menu for information and editing.
     */
    protected void showPopUp(MouseEvent e) {
        if (popup != null) {
            popup.removeAll();
        } else {
            popup = new JPopupMenu();
        }


        String info = rb.getString("TrackSegment");
        if (getArc()) {
            if (getCircle()) {
                info = info + " (" + Bundle.getMessage("Circle") + ")";
            } else {
                info = info + " (" + Bundle.getMessage("Ellipse") + ")";
            }
        } else if (getBezier()) {
            info = info + " (" + Bundle.getMessage("Bezier") + ")";
        } else {
            info = info + " (" + Bundle.getMessage("Line") + ")";
        }

        JMenuItem jmi = popup.add(info);
        jmi.setEnabled(false);

        jmi = popup.add(ident);
        jmi.setEnabled(false);

        if (!dashed) {
            jmi = popup.add(rb.getString("Style") + " - " + rb.getString("Solid"));
        } else {
            jmi = popup.add(rb.getString("Style") + " - " + rb.getString("Dashed"));
        }
        jmi.setEnabled(false);

        if (!mainline) {
            jmi = popup.add(rb.getString("NotMainline"));
        } else {
            jmi = popup.add(rb.getString("Mainline"));
        }
        jmi.setEnabled(false);

        if (blockName.equals("")) {
            jmi = popup.add(rb.getString("NoBlock"));
        } else {
            jmi = popup.add(Bundle.getMessage("BeanNameBlock") + ": " + getLayoutBlock().getID());
        }
        jmi.setEnabled(false);

        if (hidden) {
            jmi = popup.add(rb.getString("Hidden"));
        } else {
            jmi = popup.add(rb.getString("NotHidden"));
        }
        jmi.setEnabled(false);

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
                layoutEditor.removeTrackSegment(instance);
                remove();
                dispose();
            }
        });
        JMenu lineType = new JMenu(rb.getString("ChangeTo"));
        lineType.add(new AbstractAction(Bundle.getMessage("Line")) {

            @Override
            public void actionPerformed(ActionEvent e) {
                changeType(0);
            }
        });
        lineType.add(new AbstractAction(Bundle.getMessage("Circle")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeType(1);
            }
        });
        lineType.add(new AbstractAction(Bundle.getMessage("Ellipse")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeType(2);
            }
        });
        lineType.add(new AbstractAction(Bundle.getMessage("Bezier")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                changeType(3);
            }
        });
        popup.add(lineType);

        if (getArc()) {
            popup.add(new AbstractAction(rb.getString("FlipAngle")) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    flipAngle();
                }
            });
        }
        if (getArc() || getBezier()) {
            if (hideConstructionLines()) {
                popup.add(new AbstractAction(rb.getString("ShowConstruct")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        hideConstructionLines(SHOWCON);
                    }
                });
            } else {
                popup.add(new AbstractAction(rb.getString("HideConstruct")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        hideConstructionLines(HIDECON);
                    }
                });
            }
        }
        if ((!blockName.equals("")) && (jmri.InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled())) {
            popup.add(new AbstractAction(rb.getString("ViewBlockRouting")) {
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

        JMenuItem jmi = popup.add(rb.getString("BezierControlPoint") + " #" + bezierControlPointIndex);
        jmi.setEnabled(false);
        popup.add(new JSeparator(JSeparator.HORIZONTAL));

        if (bezierControlPoints.size() < BEZIER_CONTROL_POINT_OFFSET_MAX - BEZIER_CONTROL_POINT_OFFSET_MIN) {
            popup.add(new AbstractAction(rb.getString("AddBezierControlPointAfter")) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    addBezierControlPointAfter(bezierControlPointIndex);
                }
            });
            popup.add(new AbstractAction(rb.getString("AddBezierControlPointBefore")) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    addBezierControlPointBefore(bezierControlPointIndex);
                }
            });
        }

        if (bezierControlPoints.size() > 2) {
            popup.add(new AbstractAction(rb.getString("DeleteBezierControlPoint") + " #" + bezierControlPointIndex) {

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
            case 0:
                setArc(false);
                setAngle(0.0D);
                setCircle(false);
                setBezier(false);
                break;
            case 1:
                setArc(true);
                setAngle(90.0D);
                setCircle(true);
                setBezier(false);
                break;
            case 2:
                setArc(true);
                setAngle(90.0D);
                setCircle(false);
                setBezier(false);
                break;
            case 3:
                setArc(false);
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
        if (getFlip()) {
            setFlip(false);
        } else {
            setFlip(true);
        }
        layoutEditor.redrawPanel();
        layoutEditor.setDirty();
    }

    // variables for Edit Track Segment pane
    private JmriJFrame editTrackSegmentFrame = null;
    private JComboBox<String> dashedBox = new JComboBox<String>();
    private int dashedIndex;
    private int solidIndex;
    private JComboBox<String> mainlineBox = new JComboBox<String>();
    private int mainlineTrackIndex;
    private int sideTrackIndex;
    private JmriBeanComboBox blockNameComboBox = new JmriBeanComboBox(
            InstanceManager.getDefault(BlockManager.class), null, JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
    private JTextField arcField = new JTextField(5);
    private JCheckBox hiddenBox = new JCheckBox(rb.getString("HideTrack"));
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
            dashedBox.addItem(rb.getString("Solid"));
            solidIndex = 0;
            dashedBox.addItem(rb.getString("Dashed"));
            dashedIndex = 1;
            dashedBox.setToolTipText(rb.getString("DashedToolTip"));
            panel31.add(new JLabel(rb.getString("Style") + " : "));
            panel31.add(dashedBox);
            contentPane.add(panel31);

            // add mainline choice
            JPanel panel32 = new JPanel();
            panel32.setLayout(new FlowLayout());
            mainlineBox.removeAllItems();
            mainlineBox.addItem(rb.getString("Mainline"));
            mainlineTrackIndex = 0;
            mainlineBox.addItem(rb.getString("NotMainline"));
            sideTrackIndex = 1;
            mainlineBox.setToolTipText(rb.getString("MainlineToolTip"));
            panel32.add(mainlineBox);
            contentPane.add(panel32);

            // add hidden choice
            JPanel panel33 = new JPanel();
            panel33.setLayout(new FlowLayout());
            hiddenBox.setToolTipText(rb.getString("HiddenToolTip"));
            panel33.add(hiddenBox);
            contentPane.add(panel33);

            // setup block name
            JPanel panel2 = new JPanel();
            panel2.setLayout(new FlowLayout());
            JLabel blockNameLabel = new JLabel(rb.getString("BlockID"));
            panel2.add(blockNameLabel);
            layoutEditor.setupComboBox(blockNameComboBox, false, true);
            blockNameComboBox.setToolTipText(rb.getString("EditBlockNameHint"));
            panel2.add(blockNameComboBox);

            contentPane.add(panel2);

            if (getArc() && circle) {
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
        blockNameComboBox.getEditor().setItem(blockName);

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
            layoutEditor.auxTools.setBlockConnectivityChanged();
            updateBlockInfo();
        }
        // check if a block exists to edit
        if (block == null) {
            JOptionPane.showMessageDialog(editTrackSegmentFrame,
                    rb.getString("Error1"),
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
        if (dashedBox.getSelectedIndex() == dashedIndex) {
            dashed = true;
        } else {
            dashed = false;
        }
        // set mainline
        boolean oldMainline = mainline;
        if (mainlineBox.getSelectedIndex() == mainlineTrackIndex) {
            mainline = true;
        } else {
            mainline = false;
        }
        // set hidden
        boolean oldHidden = hidden;
        hidden = hiddenBox.isSelected();
        if (getArc()) {
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
            layoutEditor.auxTools.setBlockConnectivityChanged();
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
    public static final int HIDECON = 0x02; //flag set on a segment basis.
    public static final int HIDECONALL = 0x04;  //Used by layout editor for hiding all

    public int showConstructionLine = SHOWCON;

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

    public Point2D getCentreSeg() {
        Point2D result = MathUtil.zeroPoint2D();

        if ((null != connect1) && (null != connect2)) {
            // get the end points
            Point2D ep1 = layoutEditor.getCoords(getConnect1(), getType1());
            Point2D ep2 = layoutEditor.getCoords(getConnect2(), getType2());

            if (getCircle()) {
                //TODO: do something here?
                //} else if (getArc()) {
                //TODO: do something here?
                result = center;
            } else if (getBezier()) {
                //compute result Bezier point for (t == 0.5);
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

    public Point2D getCoordsCenterCircle() {
        return new Point2D.Double(centreX, centreY);
    }

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

        if (getFlip()) {
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
        if (getFlip()) {
            pt1 = layoutEditor.getCoords(getConnect2(), getType2());
            pt2 = layoutEditor.getCoords(getConnect1(), getType1());
        } else {
            pt1 = layoutEditor.getCoords(getConnect1(), getType1());
            pt2 = layoutEditor.getCoords(getConnect2(), getType2());
        }
        if ((getTmpPt1() != pt1) || (getTmpPt2() != pt2) || trackNeedsRedraw()) {
            setTmpPt1(pt1);
            setTmpPt2(pt2);

            double pt2x = pt2.getX();
            double pt2y = pt2.getY();
            double pt1x = pt1.getX();
            double pt1y = pt1.getY();

            if (getAngle() == 0.0D) {
                setTmpAngle(90.0D);
            } else {
                setTmpAngle(getAngle());
            }
            // Convert angle to radiants in order to speed up maths
            double halfAngleRAD = Math.toRadians(getTmpAngle()) / 2.0D;

            // Compute arc's chord
            double a = pt2x - pt1x;
            double o = pt2y - pt1y;
            double chord = Math.hypot(a, o);
            setChordLength(chord);

            // Make sure chord is not null
            // In such a case (ep1 == ep2), there is no arc to drawHidden
            if (chord > 0.0D) {
                double radius = (chord / 2) / Math.sin(halfAngleRAD);
                // Circle
                double startRad = Math.atan2(a, o) - halfAngleRAD;
                setStartadj(Math.toDegrees(startRad));
                if (getCircle()) {
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
    }

    public void drawHidden(Graphics2D g2) {
        setColorForTrackBlock(g2, getLayoutBlock());
        g2.setStroke(new BasicStroke(1.0F, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
        g2.draw(new Line2D.Double(layoutEditor.getCoords(getConnect1(), getType1()),
                layoutEditor.getCoords(getConnect2(), getType2())));
    }   // drawHidden(Graphics2D g2)

    public void drawDashed(Graphics2D g2, boolean mainline) {
        if ((!isHidden()) && getDashed() && (mainline == getMainline())) {
            setColorForTrackBlock(g2, getLayoutBlock());
            float trackWidth = layoutEditor.setTrackStrokeWidth(g2, mainline);
            if (getArc()) {
                calculateTrackSegmentAngle();
                Stroke originalStroke = g2.getStroke();
                Stroke drawingStroke = new BasicStroke(trackWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{9}, 0);
                g2.setStroke(drawingStroke);
                g2.draw(new Arc2D.Double(getCX(), getCY(), getCW(), getCH(), getStartadj(), getTmpAngle(), Arc2D.OPEN));
                g2.setStroke(originalStroke);
            } else if (getBezier()) {
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
        }
    }   // drawDashed(Graphics2D g2, boolean mainline)

    public void drawSolid(Graphics2D g2, boolean isMainline) {
        if (!isHidden() && !getDashed() && (isMainline == getMainline())) {
            setColorForTrackBlock(g2, getLayoutBlock());

            if (getArc()) {
                calculateTrackSegmentAngle();
                g2.draw(new Arc2D.Double(getCX(), getCY(), getCW(), getCH(), getStartadj(), getTmpAngle(), Arc2D.OPEN));
            } else if (getBezier()) {
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
        }
    }   // drawSolid(Graphics2D g2, boolean isMainline)

    public void drawEditControls(Graphics2D g2) {
        setColorForTrackBlock(g2, getLayoutBlock());

        Point2D ep1 = layoutEditor.getCoords(getConnect1(), getType1());
        Point2D ep2 = layoutEditor.getCoords(getConnect2(), getType2());
        if (getCircle()) {
            if (showConstructionLinesLE()) {
                Point2D circleCenterPoint = getCoordsCenterCircle();
                g2.draw(new Line2D.Double(ep1, circleCenterPoint));
                g2.draw(new Line2D.Double(ep2, circleCenterPoint));
                g2.draw(layoutEditor.trackControlCircleAt(circleCenterPoint));
            }
            g2.draw(layoutEditor.trackControlCircleAt(getCentreSeg()));
        } else if (getBezier()) {
            g2.draw(layoutEditor.trackControlPointRectAt(ep1));
            Point2D lastPt = ep1;
            for (Point2D bcp : bezierControlPoints) {
                if (showConstructionLinesLE()) { //draw track circles
                    g2.draw(new Line2D.Double(lastPt, bcp));
                    lastPt = bcp;
                }
                g2.draw(layoutEditor.trackControlPointRectAt(bcp));
            }
            if (showConstructionLinesLE()) { //draw track circles
                g2.draw(new Line2D.Double(lastPt, ep2));
            }
            g2.draw(layoutEditor.trackControlPointRectAt(ep2));
            g2.draw(layoutEditor.trackControlCircleAt(getCentreSeg()));
        } else {
            if (getArc()) {
                g2.draw(new Line2D.Double(ep1, ep2));
            }
            if (showConstructionLinesLE()) { //draw track circles
                g2.draw(layoutEditor.trackControlCircleAt(getCentreSeg()));
            }
        }
        // Draw a square at the circles centre, that then allows the
        // user to dynamically change the angle by dragging the mouse.
        g2.setColor(Color.black);
        if (circle && showConstructionLinesLE()) {
            g2.draw(layoutEditor.trackControlCircleRectAt(getCoordsCenterCircle()));
        }
    }   // drawEditControls(Graphics2D g2)

    public void reCheckBlockBoundary()
    {
        // nothing to do here... move along...
    }

    private final static Logger log = LoggerFactory.getLogger(TrackSegment.class.getName());
}
