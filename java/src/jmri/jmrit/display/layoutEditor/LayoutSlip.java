package jmri.jmrit.display.layoutEditor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map.Entry;
import java.util.ResourceBundle;
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
import javax.swing.SwingUtilities;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.SignalMast;
import jmri.Turnout;
import jmri.jmrit.display.layoutEditor.blockRoutingTable.LayoutBlockRouteTableAction;
import jmri.util.JmriJFrame;
import jmri.util.MathUtil;
import jmri.util.swing.JmriBeanComboBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A LayoutSlip is a crossing of two straight tracks designed in such a way as
 * to allow trains to change from one straight track to the other, as well as
 * going straight across.
 * <P>
 * A LayoutSlip has four connection points, designated A, B, C, and D. A train
 * may proceed between A and D, A and C, B and D and in the case of
 * double-slips, B and C.
 * <P>
 * {@literal
 * ==A==-==D==
 *    \\ //
 *      X
 *    // \\
 * ==B==-==C==
 * literal}
 * <P>
 * For drawing purposes, each LayoutSlip carries a center point and
 * displacements for A and B. The displacements for C = - the displacement for
 * A, and the displacement for D = - the displacement for B. The center point
 * and these displacements may be adjusted by the user when in edit mode.
 * <P>
 * When LayoutSlips are first created, there are no connections. Block
 * information and connections are added when available.
 * <P>
 * SignalHead names are saved here to keep track of where signals are.
 * LayoutSlip only serves as a storage place for SignalHead names. The names are
 * placed here by Set Signals at Level Crossing in Tools menu.
 *
 * @author Dave Duchamp Copyright (c) 2004-2007
 */
public class LayoutSlip extends LayoutTurnout {

    // Defined text resource
    ResourceBundle rb = ResourceBundle.getBundle("jmri.jmrit.display.layoutEditor.LayoutEditorBundle");

    public int currentState = UNKNOWN;

    private String turnoutBName = "";
    private NamedBeanHandle<Turnout> namedTurnoutB = null;

    private java.beans.PropertyChangeListener mTurnoutListener = null;

    /**
     * constructor method
     */
    public LayoutSlip(String id, Point2D c, double rot, LayoutEditor layoutEditor, int type) {
        super(id, c, layoutEditor);

        dispA = new Point2D.Double(-20.0, 0.0);
        pointA = MathUtil.add(center, dispA);
        pointC = MathUtil.subtract(center, dispA);
        dispB = new Point2D.Double(-14.0, 14.0);
        pointB = MathUtil.add(center, dispB);
        pointD = MathUtil.subtract(center, dispB);

        setSlipType(type);
        rotateCoords(rot);
    }

    // this should only be used for debugging...
    public String toString() {
        return "LayoutSlip " + ident;
    }

    public void setTurnoutType(int slipType) {
        setSlipType(slipType);
    }

    public void setSlipType(int slipType) {
        if (type != slipType) {
            type = slipType;
            if (type == DOUBLE_SLIP) {
                turnoutStates.put(STATE_AC, new TurnoutState(Turnout.CLOSED, Turnout.CLOSED));
                turnoutStates.put(STATE_BD, new TurnoutState(Turnout.THROWN, Turnout.THROWN));
                turnoutStates.put(STATE_AD, new TurnoutState(Turnout.CLOSED, Turnout.THROWN));
                turnoutStates.put(STATE_BC, new TurnoutState(Turnout.THROWN, Turnout.CLOSED));
            } else {
                turnoutStates.put(STATE_AC, new TurnoutState(Turnout.CLOSED, Turnout.THROWN));
                turnoutStates.put(STATE_BD, new TurnoutState(Turnout.THROWN, Turnout.CLOSED));
                turnoutStates.put(STATE_AD, new TurnoutState(Turnout.THROWN, Turnout.THROWN));
                turnoutStates.remove(STATE_BC);
            }
        }
    }

    public int getSlipType() {
        return type;
    }

    public int getSlipState() {
        return currentState;
    }

    public String getTurnoutBName() {
        if (namedTurnoutB != null) {
            return namedTurnoutB.getName();
        }
        return turnoutBName;
    }

    public Turnout getTurnoutB() {
        if (namedTurnoutB == null) {
            // set physical turnout if possible and needed
            setTurnoutB(turnoutBName);
            if (namedTurnoutB == null) {
                return null;
            }
        }
        return namedTurnoutB.getBean();
    }

    public void setTurnoutB(String tName) {
        if (namedTurnoutB != null) {
            deactivateTurnout();
        }
        turnoutBName = tName;
        Turnout turnout = jmri.InstanceManager.turnoutManagerInstance().getTurnout(turnoutBName);
        if (turnout != null) {
            namedTurnoutB = jmri.InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(turnoutBName, turnout);
            activateTurnout();
        } else {
            turnoutBName = "";
            namedTurnoutB = null;
        }
    }

    /**
     * get the object connected to this track for the specified connection type
     *
     * @param connectionType the specified connection type
     * @return the object connected to this slip for the specified connection
     *         type
     * @throws jmri.JmriException - if the connectionType is invalid
     */
    @Override
    public Object getConnection(int connectionType) throws jmri.JmriException {
        switch (connectionType) {
            case SLIP_A:
                return connectA;
            case SLIP_B:
                return connectB;
            case SLIP_C:
                return connectC;
            case SLIP_D:
                return connectD;
        }
        log.error("Invalid Connection Type " + connectionType); //I18IN
        throw new jmri.JmriException("Invalid Connection Type " + connectionType);
    }

    @Override
    public void setConnection(int connectionType, Object o, int type) throws jmri.JmriException {
        if ((type != TRACK) && (type != NONE)) {
            log.error("unexpected type of connection to layoutslip - " + type);
            throw new jmri.JmriException("unexpected type of connection to layoutslip - " + type);
        }
        switch (connectionType) {
            case SLIP_A:
                connectA = o;
                break;
            case SLIP_B:
                connectB = o;
                break;
            case SLIP_C:
                connectC = o;
                break;
            case SLIP_D:
                connectD = o;
                break;
            default:
                log.error("Invalid Connection Type " + connectionType); //I18IN
                throw new jmri.JmriException("Invalid Connection Type " + connectionType);
        }
    }

    public String getDisplayName() {
        String name = "Slip " + ident;
        String tnA = getTurnoutName();
        String tnB = getTurnoutBName();
        if ((tnA != null) && !tnA.isEmpty()) {
            name += " (" + tnA;
        }
        if ((tnB != null) && !tnB.isEmpty()) {
            if (name.contains(" (")) {
                name += ", ";
            } else {
                name += "(";
            }
            name += tnB;
        }
        if (name.contains("(")) {
            name += ")";
        }
        return name;
    }

    /**
     * Toggle slip states if clicked on, physical turnout exists, and not
     * disabled
     */
    public void toggleState(int selectedPointType) {
        if (!disabled && !(disableWhenOccupied && isOccupied())) {
            switch (selectedPointType) {
                case SLIP_LEFT: {
                    switch (currentState) {
                        case STATE_AC: {
                            if (type == SINGLE_SLIP) {
                                currentState = STATE_BD;
                            } else {
                                currentState = STATE_BC;
                            }
                            break;
                        }
                        case STATE_AD: {
                            currentState = STATE_BD;
                            break;
                        }
                        case STATE_BC:
                        default: {
                            currentState = STATE_AC;
                            break;
                        }
                        case STATE_BD: {
                            currentState = STATE_AD;
                            break;
                        }
                    }
                    break;
                }
                case SLIP_RIGHT: {
                    switch (currentState) {
                        case STATE_AC: {
                            currentState = STATE_AD;
                            break;
                        }
                        case STATE_AD: {
                            currentState = STATE_AC;
                            break;
                        }
                        case STATE_BC:
                        default: {
                            currentState = STATE_BD;
                            break;
                        }
                        case STATE_BD: {
                            if (type == SINGLE_SLIP) {
                                currentState = STATE_AC;
                            } else {
                                currentState = STATE_BC;
                            }
                            break;
                        }
                    }
                    break;
                }
            }   // switch
            setSlipState(turnoutStates.get(currentState));
        }
    }

    private void setSlipState(TurnoutState ts) {
        if (disableWhenOccupied && isOccupied()) {
            log.debug("Turnout not changed as Block is Occupied");
        } else if (!disabled) {
            if (getTurnout() != null) {
                getTurnout().setCommandedState(ts.getTurnoutAState());
            }
            if (getTurnoutB() != null) {
                getTurnoutB().setCommandedState(ts.getTurnoutBState());
            }
        }
    }

    /**
     * is this turnout occupied?
     *
     * @return true if occupied
     */
    private boolean isOccupied() {
        Boolean result = false; // assume failure (pessimist!)
        switch (currentState) {
            case STATE_AC: {
                result = ((block.getOccupancy() == LayoutBlock.OCCUPIED)
                        || (blockC.getOccupancy() == LayoutBlock.OCCUPIED));
                break;
            }
            case STATE_AD: {
                result = ((block.getOccupancy() == LayoutBlock.OCCUPIED)
                        || (blockD.getOccupancy() == LayoutBlock.OCCUPIED));
                break;
            }
            case STATE_BC: {
                result = ((blockB.getOccupancy() == LayoutBlock.OCCUPIED)
                        || (blockC.getOccupancy() == LayoutBlock.OCCUPIED));
                break;
            }
            case STATE_BD: {
                result = ((blockB.getOccupancy() == LayoutBlock.OCCUPIED)
                        || (blockD.getOccupancy() == LayoutBlock.OCCUPIED));
                break;
            }
        }
        return result;
    }   // isOccupied()

    /**
     * Activate/Deactivate turnout to redraw when turnout state changes
     */
    private void activateTurnout() {
        if (namedTurnout != null) {
            namedTurnout.getBean().addPropertyChangeListener(mTurnoutListener
                    = (java.beans.PropertyChangeEvent e) -> {
                        updateState();
                    }, namedTurnout.getName(), "Layout Editor Slip");
        }
        if (namedTurnoutB != null) {
            namedTurnoutB.getBean().addPropertyChangeListener(mTurnoutListener
                    = (java.beans.PropertyChangeEvent e) -> {
                        updateState();
                    }, namedTurnoutB.getName(), "Layout Editor Slip");
        }
    }

    private void deactivateTurnout() {
        if (mTurnoutListener != null) {
            namedTurnout.getBean().removePropertyChangeListener(mTurnoutListener);
            if (namedTurnoutB != null) {
                namedTurnoutB.getBean().removePropertyChangeListener(mTurnoutListener);
            }
            mTurnoutListener = null;
        }
    }

    @Override
    public Point2D getCoordsA() {
        return pointA;
    }

    @Override
    public Point2D getCoordsB() {
        return pointB;
    }

    @Override
    public Point2D getCoordsC() {
        return pointC;
    }

    @Override
    public Point2D getCoordsD() {
        return pointD;
    }

    protected Point2D getCoordsLeft() {
        Point2D leftCenter = MathUtil.midPoint(getCoordsA(), getCoordsB());
        double circleRadius = controlPointSize * layoutEditor.getTurnoutCircleSize();
        double leftFract = circleRadius / center.distance(leftCenter);
        return MathUtil.lerp(center, leftCenter, leftFract);
    }

    protected Point2D getCoordsRight() {
        Point2D rightCenter = MathUtil.midPoint(getCoordsC(), getCoordsD());
        double circleRadius = controlPointSize * layoutEditor.getTurnoutCircleSize();
        double rightFract = circleRadius / center.distance(rightCenter);
        return MathUtil.lerp(center, rightCenter, rightFract);
    }

    /**
     * return the coordinates for the specified connection type
     *
     * @param connectionType the connection type
     * @return the Point2D coordinates
     */
    public Point2D getCoordsForConnectionType(int connectionType) {
        Point2D result = center;
        double circleRadius = controlPointSize * layoutEditor.getTurnoutCircleSize();
        switch (connectionType) {
            case SLIP_A:
                result = getCoordsA();
                break;
            case SLIP_B:
                result = getCoordsB();
                break;
            case SLIP_C:
                result = getCoordsC();
                break;
            case SLIP_D:
                result = getCoordsD();
                break;
            case SLIP_CENTER:
                break;
            case SLIP_LEFT:
                result = getCoordsLeft();
                break;
            case SLIP_RIGHT:
                result = getCoordsRight();
                break;
            default:
                log.error("Invalid connection type " + connectionType); //I18IN
        }
        return result;
    }

    /**
     * @return the bounds of this slip
     */
    public Rectangle2D getBounds() {
        Rectangle2D result;

        Point2D pt = getCoordsA();
        result = new Rectangle2D.Double(pt.getX(), pt.getY(), 0, 0);
        result.add(getCoordsB());
        result.add(getCoordsC());
        result.add(getCoordsD());
        return result;
    }

    private void updateBlockInfo() {
        LayoutBlock b1 = null;
        LayoutBlock b2 = null;
        if (block != null) {
            block.updatePaths();
        }
        if (connectA != null) {
            b1 = ((TrackSegment) connectA).getLayoutBlock();
            if ((b1 != null) && (b1 != block)) {
                b1.updatePaths();
            }
        }
        if (connectC != null) {
            b2 = ((TrackSegment) connectC).getLayoutBlock();
            if ((b2 != null) && (b2 != block) && (b2 != b1)) {
                b2.updatePaths();
            }
        }

        if (connectB != null) {
            b1 = ((TrackSegment) connectB).getLayoutBlock();
            if ((b1 != null) && (b1 != block)) {
                b1.updatePaths();
            }
        }
        if (connectD != null) {
            b2 = ((TrackSegment) connectD).getLayoutBlock();
            if ((b2 != null) && (b2 != block) && (b2 != b1)) {
                b2.updatePaths();
            }
        }
        reCheckBlockBoundary();
    }

    @Override
    public void reCheckBlockBoundary() {
        if (connectA == null && connectB == null && connectC == null && connectD == null) {
            //This is no longer a block boundary, therefore will remove signal masts and sensors if present
            if (signalAMastNamed != null) {
                removeSML(getSignalAMast());
            }
            if (signalBMastNamed != null) {
                removeSML(getSignalBMast());
            }
            if (signalCMastNamed != null) {
                removeSML(getSignalCMast());
            }
            if (signalDMastNamed != null) {
                removeSML(getSignalDMast());
            }
            signalAMastNamed = null;
            signalBMastNamed = null;
            signalCMastNamed = null;
            signalDMastNamed = null;
            sensorANamed = null;
            sensorBNamed = null;
            sensorCNamed = null;
            sensorDNamed = null;
            return;
            //May want to look at a method to remove the assigned mast from the panel and potentially any logics generated
        } else if (connectA == null || connectB == null || connectC == null || connectD == null) {
            //could still be in the process of rebuilding the point details
            return;
        }

        TrackSegment trkA;
        TrackSegment trkB;
        TrackSegment trkC;
        TrackSegment trkD;

        if (connectA instanceof TrackSegment) {
            trkA = (TrackSegment) connectA;
            if (trkA.getLayoutBlock() == block) {
                if (signalAMastNamed != null) {
                    removeSML(getSignalAMast());
                }
                signalAMastNamed = null;
                sensorANamed = null;
            }
        }
        if (connectC instanceof TrackSegment) {
            trkC = (TrackSegment) connectC;
            if (trkC.getLayoutBlock() == block) {
                if (signalCMastNamed != null) {
                    removeSML(getSignalCMast());
                }
                signalCMastNamed = null;
                sensorCNamed = null;
            }
        }
        if (connectB instanceof TrackSegment) {
            trkB = (TrackSegment) connectB;
            if (trkB.getLayoutBlock() == block) {
                if (signalBMastNamed != null) {
                    removeSML(getSignalBMast());
                }
                signalBMastNamed = null;
                sensorBNamed = null;
            }
        }

        if (connectD instanceof TrackSegment) {
            trkD = (TrackSegment) connectC;
            if (trkD.getLayoutBlock() == block) {
                if (signalDMastNamed != null) {
                    removeSML(getSignalDMast());
                }
                signalDMastNamed = null;
                sensorDNamed = null;
            }
        }
    }   // reCheckBlockBoundary()

    /**
     * Methods to test if mainline track or not Returns true if either
     * connecting track segment is mainline Defaults to not mainline if
     * connecting track segments are missing
     */
    public boolean isMainline() {
        if (((connectA != null) && (((TrackSegment) connectA).getMainline()))
                || ((connectB != null) && (((TrackSegment) connectB).getMainline()))
                || ((connectC != null) && (((TrackSegment) connectC).getMainline()))
                || ((connectD != null) && (((TrackSegment) connectD).getMainline()))) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * find the hit (location) type for a point
     *
     * @param p                  the point
     * @param useRectangles      - whether to use (larger) rectangles or
     *                           (smaller) circles for hit testing
     * @param requireUnconnected - whether to only return hit types for free
     *                           connections
     * @return the location type for the point (or NONE)
     * @since 7.4.3
     */
    protected int findHitPointType(Point2D p, boolean useRectangles, boolean requireUnconnected) {
        int result = NONE;  // assume point not on connection

        if (!requireUnconnected) {
            // calculate radius of turnout control circle
            double circleRadius = controlPointSize * layoutEditor.getTurnoutCircleSize();

            // get left and right centers
            Point2D leftCenter = getCoordsLeft();
            Point2D rightCenter = getCoordsRight();

            if (useRectangles) {
                // calculate turnout's left control rectangle
                Rectangle2D leftRectangle = layoutEditor.trackControlCircleRectAt(leftCenter);
                if (leftRectangle.contains(p)) {
                    //point is in this turnout's left control rectangle
                    result = SLIP_LEFT;
                }
                Rectangle2D rightRectangle = layoutEditor.trackControlCircleRectAt(rightCenter);
                if (rightRectangle.contains(p)) {
                    //point is in this turnout's right control rectangle
                    result = SLIP_RIGHT;
                }
            } else {
                //check east/west turnout control circles
                double leftDistance = p.distance(leftCenter);
                double rightDistance = p.distance(rightCenter);

                if ((leftDistance <= circleRadius) || (rightDistance <= circleRadius)) {
                    //mouse was pressed on this slip
                    result = (leftDistance < rightDistance) ? LayoutTrack.SLIP_LEFT : LayoutTrack.SLIP_RIGHT;
                }
            }
        }

        // have we found anything yet?
        if (result == NONE) {
            // rather than create rectangles for all the points below and
            // see if the passed in point is in one of those rectangles
            // we can create a rectangle for the passed in point and then
            // test if any of the points below are in that rectangle instead.
            Rectangle2D r = layoutEditor.trackControlPointRectAt(p);

            if (!requireUnconnected || (getConnectA() == null)) {
                //check the A connection point
                if (r.contains(getCoordsA())) {
                    result = LayoutTrack.SLIP_A;
                }
            }

            if (!requireUnconnected || (getConnectB() == null)) {
                //check the B connection point
                if (r.contains(getCoordsB())) {
                    result = LayoutTrack.SLIP_B;
                }
            }

            if (!requireUnconnected || (getConnectC() == null)) {
                //check the C connection point
                if (r.contains(getCoordsC())) {
                    result = LayoutTrack.SLIP_C;
                }
            }

            if (!requireUnconnected || (getConnectD() == null)) {
                //check the D connection point
                if (r.contains(getCoordsD())) {
                    result = LayoutTrack.SLIP_D;
                }
            }
        }
        return result;
    }   // findHitPointType

    /*
     * Modify coordinates methods
     */
    /**
     * set center coordinates
     *
     * @param p the coordinates to set
     */
    @Override
    public void setCoordsCenter(Point2D p) {
        center = p;
        pointA = MathUtil.add(center, dispA);
        pointB = MathUtil.add(center, dispB);
        pointC = MathUtil.subtract(center, dispA);
        pointD = MathUtil.subtract(center, dispB);
    }

    @Override
    public void setCoordsA(Point2D p) {
        pointA = p;
        dispA = MathUtil.subtract(pointA, center);
        pointC = MathUtil.subtract(center, dispA);
    }

    @Override
    public void setCoordsB(Point2D p) {
        pointB = p;
        dispB = MathUtil.subtract(pointB, center);
        pointD = MathUtil.subtract(center, dispB);
    }

    @Override
    public void setCoordsC(Point2D p) {
        pointC = p;
        dispA = MathUtil.subtract(center, pointC);
        pointA = MathUtil.add(center, dispA);
    }

    @Override
    public void setCoordsD(Point2D p) {
        pointD = p;
        dispB = MathUtil.subtract(center, pointD);
        pointB = MathUtil.add(center, dispB);
    }

    /**
     * scale this LayoutTrack's coordinates by the x and y factors
     *
     * @param xFactor the amount to scale X coordinates
     * @param yFactor the amount to scale Y coordinates
     */
    @Override
    public void scaleCoords(float xFactor, float yFactor) {
        Point2D factor = new Point2D.Double(xFactor, yFactor);
        center = MathUtil.granulize(MathUtil.multiply(center, factor), 1.0);
        pointA = MathUtil.granulize(MathUtil.multiply(pointA, factor), 1.0);
        pointB = MathUtil.granulize(MathUtil.multiply(pointB, factor), 1.0);
        pointC = MathUtil.granulize(MathUtil.multiply(pointC, factor), 1.0);
        pointD = MathUtil.granulize(MathUtil.multiply(pointD, factor), 1.0);
    }

    /**
     * translate this LayoutTrack's coordinates by the x and y factors
     *
     * @param xFactor the amount to translate X coordinates
     * @param yFactor the amount to translate Y coordinates
     */
    @Override
    public void translateCoords(float xFactor, float yFactor) {
        Point2D factor = new Point2D.Double(xFactor, yFactor);
        center = MathUtil.add(center, factor);
        pointA = MathUtil.add(pointA, factor);
        pointB = MathUtil.add(pointB, factor);
        pointC = MathUtil.add(pointC, factor);
        pointD = MathUtil.add(pointD, factor);
    }

    /**
     * Initialization method The above variables are initialized by
     * LayoutSlipXml, then the following method is called after the entire
     * LayoutEditor is loaded to set the specific LayoutSlip objects.
     */
    /*
    @Override
    public void setObjects(LayoutEditor p) {
        connectA = p.getFinder().findTrackSegmentByName(connectAName);
        connectB = p.getFinder().findTrackSegmentByName(connectBName);
        connectC = p.getFinder().findTrackSegmentByName(connectCName);
        connectD = p.getFinder().findTrackSegmentByName(connectDName);

        if (!tBlockName.isEmpty()) {
            block = p.getLayoutBlock(tBlockName);
            if (block != null) {
                blockName = tBlockName;
                block.incrementUse();
            } else {
                log.error("bad blockname '" + tBlockName + "' in layoutslip:setObjects " + ident);
            }
        }

        if (!tBlockBName.isEmpty()) {
            blockB = p.getLayoutBlock(tBlockBName);
            if (blockB != null) {
                blockBName = tBlockBName;
                if (block != blockB) {
                    blockB.incrementUse();
                }
            } else {
                log.error("bad blockname '" + tBlockBName + "' in layoutslip:setObjects " + ident);
            }
        }

        if (!tBlockCName.isEmpty()) {
            blockC = p.getLayoutBlock(tBlockCName);
            if (blockC != null) {
                blockCName = tBlockCName;
                if ((block != blockC) && (blockB != blockC)) {
                    blockC.incrementUse();
                }
            } else {
                log.error("bad blockname '" + tBlockCName + "' in layoutslip:setObjects " + ident);
            }
        }

        if (!tBlockDName.isEmpty()) {
            blockD = p.getLayoutBlock(tBlockDName);
            if (blockD != null) {
                blockDName = tBlockDName;
                if ((block != blockD) && (blockB != blockD)
                        && (blockC != blockD)) {
                    blockD.incrementUse();
                }
            } else {
                log.error("bad blockname '" + tBlockDName + "' in layoutslip:setObjects " + ident);
            }
        }
    }
     */
    JPopupMenu popup = null;
    LayoutEditorTools tools = null;

    /**
     * Display popup menu for information and editing
     */
    @Override
    protected void showPopup(MouseEvent e) {
        if (popup != null) {
            popup.removeAll();
        } else {
            popup = new JPopupMenu();
        }
        if (tools == null) {
            tools = new LayoutEditorTools(layoutEditor);
        }
        if (layoutEditor.isEditable()) {
            JMenuItem jmi = null;
            switch (type) {
                case SINGLE_SLIP: {
                    jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("LayoutSingleSlip")) + ident);
                    break;
                }
                case DOUBLE_SLIP: {
                    jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("LayoutDoubleSlip")) + ident);
                    break;
                }
            }
            jmi.setEnabled(false);

            if (getTurnout() == null) {
                jmi = popup.add(rb.getString("NoTurnout"));
            } else {
                jmi = popup.add(Bundle.getMessage("BeanNameTurnout") + ": " + turnoutName);
            }
            jmi.setEnabled(false);

            if (getTurnoutB() == null) {
                jmi = popup.add(rb.getString("NoTurnout"));
            } else {
                jmi = popup.add(Bundle.getMessage("BeanNameTurnout") + ": " + turnoutBName);
            }
            jmi.setEnabled(false);

            boolean blockAssigned = false;
            if ((blockName == null) || (blockName.isEmpty())) {
                jmi = popup.add(rb.getString("NoBlock"));
            } else {
                jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameBlock")) + block.getDisplayName());
                blockAssigned = true;
            }
            jmi.setEnabled(false);

            // if there are any track connections
            if ((connectA != null) || (connectB != null)
                    || (connectC != null) || (connectD != null)) {
                JMenu connectionsMenu = new JMenu(Bundle.getMessage("Connections_", "..."));
                if (connectA != null) {
                    connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "A") + ((LayoutTrack) connectA).getName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            LayoutEditorFindItems lf = layoutEditor.getFinder();
                            LayoutTrack lt = (LayoutTrack) lf.findObjectByName(((LayoutTrack) connectA).getName());
                            layoutEditor.setSelectionRect(lt.getBounds());
                        }
                    });
                }
                if (connectB != null) {
                    connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "B") + ((LayoutTrack) connectB).getName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            LayoutEditorFindItems lf = layoutEditor.getFinder();
                            LayoutTrack lt = (LayoutTrack) lf.findObjectByName(((LayoutTrack) connectB).getName());
                            layoutEditor.setSelectionRect(lt.getBounds());
                        }
                    });
                }
                if (connectC != null) {
                    connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "C") + ((LayoutTrack) connectC).getName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            LayoutEditorFindItems lf = layoutEditor.getFinder();
                            LayoutTrack lt = (LayoutTrack) lf.findObjectByName(((LayoutTrack) connectC).getName());
                            layoutEditor.setSelectionRect(lt.getBounds());
                        }
                    });
                }
                if (connectD != null) {
                    connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "D") + ((LayoutTrack) connectD).getName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            LayoutEditorFindItems lf = layoutEditor.getFinder();
                            LayoutTrack lt = (LayoutTrack) lf.findObjectByName(((LayoutTrack) connectD).getName());
                            layoutEditor.setSelectionRect(lt.getBounds());
                        }
                    });
                }
                popup.add(connectionsMenu);
            }

            popup.add(new JSeparator(JSeparator.HORIZONTAL));

            JCheckBoxMenuItem hiddenCheckBoxMenuItem = new JCheckBoxMenuItem(rb.getString("Hidden"));
            hiddenCheckBoxMenuItem.setSelected(hidden);
            popup.add(hiddenCheckBoxMenuItem);
            hiddenCheckBoxMenuItem.addActionListener((java.awt.event.ActionEvent e1) -> {
                JCheckBoxMenuItem o = (JCheckBoxMenuItem) e1.getSource();
                setHidden(o.isSelected());
            });

            JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(Bundle.getMessage("Disabled"));
            cbmi.setSelected(disabled);
            popup.add(cbmi);
            cbmi.addActionListener((java.awt.event.ActionEvent e2) -> {
                JCheckBoxMenuItem o = (JCheckBoxMenuItem) e2.getSource();
                setDisabled(o.isSelected());
            });

            cbmi = new JCheckBoxMenuItem(rb.getString("DisabledWhenOccupied"));
            cbmi.setSelected(disableWhenOccupied);
            popup.add(cbmi);
            cbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                JCheckBoxMenuItem o = (JCheckBoxMenuItem) e3.getSource();
                setDisableWhenOccupied(o.isSelected());
            });

            popup.add(new AbstractAction(Bundle.getMessage("ButtonEdit")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    editLayoutSlip(LayoutSlip.this);
                }
            });
            popup.add(new AbstractAction(Bundle.getMessage("ButtonDelete")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (layoutEditor.removeLayoutSlip(LayoutSlip.this)) {
                        // Returned true if user did not cancel
                        remove();
                        dispose();
                    }
                }
            });
            if ((connectA == null) && (connectB == null)
                    && (connectC == null) && (connectD == null)) {
                JMenuItem rotateItem = new JMenuItem(rb.getString("Rotate") + "...");
                popup.add(rotateItem);
                rotateItem.addActionListener(
                        (ActionEvent event) -> {
                            boolean entering = true;
                            boolean error = false;
                            String newAngle = "";
                            while (entering) {
                                // prompt for rotation angle
                                error = false;
                                newAngle = JOptionPane.showInputDialog(layoutEditor,
                                        Bundle.getMessage("MakeLabel", rb.getString("EnterRotation")));
                                if (newAngle.isEmpty()) {
                                    return;  // cancelled
                                }
                                double rot = 0.0;
                                try {
                                    rot = Double.parseDouble(newAngle);
                                } catch (Exception e1) {
                                    JOptionPane.showMessageDialog(layoutEditor, rb.getString("Error3")
                                            + " " + e1, Bundle.getMessage("ErrorTitle"), JOptionPane.ERROR_MESSAGE);
                                    error = true;
                                    newAngle = "";
                                }
                                if (!error) {
                                    entering = false;
                                    if (rot != 0.0) {
                                        rotateCoords(rot);
                                        layoutEditor.redrawPanel();
                                    }
                                }
                            }
                        }
                );
            }
            if (blockAssigned) {
                AbstractAction ssaa = new AbstractAction(rb.getString("SetSignals")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        tools.setSignalsAtSlipFromMenu(LayoutSlip.this,
                                layoutEditor.signalIconEditor, layoutEditor.signalFrame);
                    }
                };
                JMenu jm = new JMenu(Bundle.getMessage("SignalHeads"));
                if (tools.addLayoutSlipSignalHeadInfoToMenu(LayoutSlip.this, jm)) {
                    jm.add(ssaa);
                    popup.add(jm);
                } else {
                    popup.add(ssaa);
                }

            }

            final String[] boundaryBetween = getBlockBoundaries();
            boolean blockBoundaries = false;

            for (int i = 0; i < 4; i++) {
                if (boundaryBetween[i] != null) {
                    blockBoundaries = true;
                }
            }
            if (blockBoundaries) {
                popup.add(new AbstractAction(rb.getString("SetSignalMasts")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        tools.setSignalMastsAtSlipFromMenu(LayoutSlip.this, boundaryBetween, layoutEditor.signalFrame);
                    }
                });
                popup.add(new AbstractAction(rb.getString("SetSensors")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        tools.setSensorsAtSlipFromMenu(LayoutSlip.this, boundaryBetween, layoutEditor.sensorIconEditor, layoutEditor.sensorFrame);
                    }
                });
            }

            if (jmri.InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled()) {
                if (blockAssigned) {
                    popup.add(new AbstractAction(rb.getString("ViewBlockRouting")) {
                        @Override
                        public void actionPerformed(ActionEvent event) {
                            AbstractAction routeTableAction = new LayoutBlockRouteTableAction("ViewRouting", getLayoutBlock());
                            routeTableAction.actionPerformed(event);
                        }
                    });
                }
            }
            setAdditionalEditPopUpMenu(popup);
            layoutEditor.setShowAlignmentMenu(popup);
            popup.show(e.getComponent(), e.getX(), e.getY());
        } else if (!viewAdditionalMenu.isEmpty()) {
            setAdditionalViewPopUpMenu(popup);
            popup.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    @Override
    public String[] getBlockBoundaries() {
        final String[] boundaryBetween = new String[4];

        if ((blockName != null) && (!blockName.isEmpty()) && (block != null)) {
            if ((connectA instanceof TrackSegment) && (((TrackSegment) connectA).getLayoutBlock() != block)) {
                try {
                    boundaryBetween[0] = (((TrackSegment) connectA).getLayoutBlock().getDisplayName() + " - " + block.getDisplayName());
                } catch (java.lang.NullPointerException e) {
                    //Can be considered normal if tracksegement hasn't yet been allocated a block
                    log.debug("TrackSegement at connection A doesn't contain a layout block");
                }
            }
            if ((connectC instanceof TrackSegment) && (((TrackSegment) connectC).getLayoutBlock() != block)) {
                try {
                    boundaryBetween[2] = (((TrackSegment) connectC).getLayoutBlock().getDisplayName() + " - " + block.getDisplayName());
                } catch (java.lang.NullPointerException e) {
                    //Can be considered normal if tracksegement hasn't yet been allocated a block
                    log.debug("TrackSegement at connection C doesn't contain a layout block");
                }
            }
            if ((connectB instanceof TrackSegment) && (((TrackSegment) connectB).getLayoutBlock() != block)) {
                try {
                    boundaryBetween[1] = (((TrackSegment) connectB).getLayoutBlock().getDisplayName() + " - " + block.getDisplayName());
                } catch (java.lang.NullPointerException e) {
                    //Can be considered normal if tracksegement hasn't yet been allocated a block
                    log.debug("TrackSegement at connection B doesn't contain a layout block");
                }
            }
            if ((connectD instanceof TrackSegment) && (((TrackSegment) connectD).getLayoutBlock() != block)) {
                try {
                    boundaryBetween[3] = (((TrackSegment) connectD).getLayoutBlock().getDisplayName() + " - " + block.getDisplayName());
                } catch (java.lang.NullPointerException e) {
                    //Can be considered normal if tracksegement hasn't yet been allocated a block
                    log.debug("TrackSegement at connection D doesn't contain a layout block");
                }
            }
        }
        return boundaryBetween;
    }

    /*====================================*\
    |*      Dialog box to edit slip       *|
    \*====================================*/
    // variables for Edit slip Crossing pane
    JButton slipEditDone;
    JButton slipEditCancel;
    JButton turnoutEditBlock;
    boolean editOpen = false;
    private JmriBeanComboBox turnoutAComboBox;
    private JmriBeanComboBox turnoutBComboBox;
    private JCheckBox hiddenBox = new JCheckBox(rb.getString("HideSlip"));

    /**
     * Edit a Slip
     */
    protected void editLayoutSlip(LayoutTurnout o) {
        if (editOpen) {
            editLayoutTurnoutFrame.setVisible(true);
            return;
        }
        // Initialize if needed
        if (editLayoutTurnoutFrame == null) {
            editLayoutTurnoutFrame = new JmriJFrame(rb.getString("EditSlip"), false, true);
            editLayoutTurnoutFrame.addHelpMenu("package.jmri.jmrit.display.EditLayoutSlip", true);
            editLayoutTurnoutFrame.setLocation(50, 30);

            Container contentPane = editLayoutTurnoutFrame.getContentPane();
            contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));

            JPanel panel1 = new JPanel();
            panel1.setLayout(new FlowLayout());
            JLabel turnoutNameLabel = new JLabel(Bundle.getMessage("BeanNameTurnout") + " A " + Bundle.getMessage("Name"));
            panel1.add(turnoutNameLabel);
            turnoutAComboBox = new JmriBeanComboBox(InstanceManager.turnoutManagerInstance(), getTurnout(), JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
            LayoutEditor.setupComboBox(turnoutAComboBox, true, true);

            // disable items that are already in use
            PopupMenuListener pml = new PopupMenuListener() {
                @Override
                public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                    // This method is called before the popup menu becomes visible.
                    log.debug("PopupMenuWillBecomeVisible");
                    Object o = e.getSource();
                    if (o instanceof JmriBeanComboBox) {
                        JmriBeanComboBox jbcb = (JmriBeanComboBox) o;
                        jmri.Manager m = jbcb.getManager();
                        if (m != null) {
                            String[] systemNames = m.getSystemNameArray();
                            for (int idx = 0; idx < systemNames.length; idx++) {
                                String systemName = systemNames[idx];
                                jbcb.setItemEnabled(idx, layoutEditor.validatePhysicalTurnout(systemName, null));
                            }
                        }
                    }
                }

                @Override
                public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                    // This method is called before the popup menu becomes invisible
                    log.debug("PopupMenuWillBecomeInvisible");
                }

                @Override
                public void popupMenuCanceled(PopupMenuEvent e) {
                    // This method is called when the popup menu is canceled
                    log.debug("PopupMenuCanceled");
                }
            };

            turnoutAComboBox.addPopupMenuListener(pml);
            turnoutAComboBox.setEnabledColor(Color.green.darker().darker());
            turnoutAComboBox.setDisabledColor(Color.red);

            panel1.add(turnoutAComboBox);
            contentPane.add(panel1);

            JPanel panel1a = new JPanel();
            panel1a.setLayout(new FlowLayout());
            JLabel turnoutBNameLabel = new JLabel(Bundle.getMessage("BeanNameTurnout") + " B " + Bundle.getMessage("Name"));
            panel1a.add(turnoutBNameLabel);

            turnoutBComboBox = new JmriBeanComboBox(
                    InstanceManager.turnoutManagerInstance(), getTurnoutB(), JmriBeanComboBox.DisplayOptions.DISPLAYNAME);
            LayoutEditor.setupComboBox(turnoutBComboBox, true, true);

            turnoutBComboBox.addPopupMenuListener(pml);
            turnoutBComboBox.setEnabledColor(Color.green.darker().darker());
            turnoutBComboBox.setDisabledColor(Color.red);

            panel1a.add(turnoutBComboBox);

            contentPane.add(panel1a);

            JPanel panel2 = new JPanel();
            panel2.setLayout(new GridLayout(0, 3, 2, 2));

            panel2.add(new Label("   "));
            panel2.add(new Label(Bundle.getMessage("BeanNameTurnout") + " A:"));
            panel2.add(new Label(Bundle.getMessage("BeanNameTurnout") + " B:"));
            for (Entry<Integer, TurnoutState> ts : turnoutStates.entrySet()) {
                SampleStates draw = new SampleStates(ts.getKey());
                draw.repaint();
                draw.setPreferredSize(new Dimension(40, 40));
                panel2.add(draw);

                panel2.add(ts.getValue().getComboA());
                panel2.add(ts.getValue().getComboB());
            }

            testPanel = new TestState();
            testPanel.setSize(40, 40);
            testPanel.setPreferredSize(new Dimension(40, 40));
            panel2.add(testPanel);
            JButton testButton = new JButton("Test");
            testButton.addActionListener((ActionEvent e) -> {
                toggleStateTest();
            });
            panel2.add(testButton);
            contentPane.add(panel2);

            JPanel panel33 = new JPanel();
            panel33.setLayout(new FlowLayout());
            hiddenBox.setToolTipText(rb.getString("HiddenToolTip"));
            panel33.add(hiddenBox);
            contentPane.add(panel33);

            // setup block name
            JPanel panel3 = new JPanel();
            panel3.setLayout(new FlowLayout());
            JLabel block1NameLabel = new JLabel(rb.getString("BlockID"));
            panel3.add(block1NameLabel);
            panel3.add(blockNameComboBox);
            LayoutEditor.setupComboBox(blockNameComboBox, false, true);
            blockNameComboBox.setToolTipText(rb.getString("EditBlockNameHint"));

            contentPane.add(panel3);
            // set up Edit Block buttons
            JPanel panel4 = new JPanel();
            panel4.setLayout(new FlowLayout());
            // Edit Block
            panel4.add(turnoutEditBlock = new JButton(Bundle.getMessage("EditBlock", "")));
            turnoutEditBlock.addActionListener(
                    (ActionEvent event) -> {
                        turnoutEditBlockPressed(event);
                    }
            );
            turnoutEditBlock.setToolTipText(Bundle.getMessage("EditBlockHint", "")); // empty value for block 1

            contentPane.add(panel4);
            // set up Done and Cancel buttons
            JPanel panel5 = new JPanel();
            panel5.setLayout(new FlowLayout());
            panel5.add(slipEditDone = new JButton(Bundle.getMessage("ButtonDone")));

            // make this button the default button (return or enter activates)
            // Note: We have to invoke this later because we don't currently have a root pane
            SwingUtilities.invokeLater(
                    () -> {
                        JRootPane rootPane = SwingUtilities.getRootPane(slipEditDone);
                        rootPane.setDefaultButton(slipEditDone);
                    }
            );

            slipEditDone.addActionListener((ActionEvent event) -> {
                slipEditDonePressed(event);
            });
            slipEditDone.setToolTipText(Bundle.getMessage("DoneHint", Bundle.getMessage("ButtonDone")));
            // Cancel
            panel5.add(slipEditCancel = new JButton(Bundle.getMessage("ButtonCancel")));
            slipEditCancel.addActionListener((ActionEvent event) -> {
                slipEditCancelPressed(event);
            });
            slipEditCancel.setToolTipText(Bundle.getMessage("CancelHint", Bundle.getMessage("ButtonCancel")));
            contentPane.add(panel5);
        }

        hiddenBox.setSelected(hidden);

        // Set up for Edit
        turnoutAComboBox.setText(turnoutName);
        turnoutBComboBox.setText(turnoutBName);
        blockNameComboBox.setText(blockName);

        editLayoutTurnoutFrame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                slipEditCancelPressed(null);
            }
        });
        editLayoutTurnoutFrame.pack();
        editLayoutTurnoutFrame.setVisible(true);
        editOpen = true;
        needsBlockUpdate = false;
    }

    private void drawSlipState(Graphics2D g2, int state) {
        int ctrX = 20;
        int ctrY = 20;
        Point2D ldispA = new Point2D.Double(-20.0, 0.0);
        Point2D ldispB = new Point2D.Double(-14.0, 14.0);

        Point2D A = new Point2D.Double(ctrX + ldispA.getX(), ctrY + ldispA.getY());
        Point2D B = new Point2D.Double(ctrX + ldispB.getX(), ctrY + ldispB.getY());
        Point2D C = new Point2D.Double(ctrX - ldispA.getX(), ctrY - ldispA.getY());
        Point2D D = new Point2D.Double(ctrX - ldispB.getX(), ctrY - ldispB.getY());

        g2.setColor(Color.black);
        g2.setStroke(new BasicStroke(2, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));

        g2.draw(new Line2D.Double(A, MathUtil.oneThirdPoint(A, C)));
        g2.draw(new Line2D.Double(C, MathUtil.oneThirdPoint(C, A)));

        if (state == STATE_AC || state == STATE_BD || state == UNKNOWN) {
            g2.draw(new Line2D.Double(A, MathUtil.oneThirdPoint(A, D)));
            g2.draw(new Line2D.Double(D, MathUtil.oneThirdPoint(D, A)));

            if (getSlipType() == DOUBLE_SLIP) {
                g2.draw(new Line2D.Double(B, MathUtil.oneThirdPoint(B, C)));
                g2.draw(new Line2D.Double(C, MathUtil.oneThirdPoint(C, B)));
            }
        } else {
            g2.draw(new Line2D.Double(B, MathUtil.oneThirdPoint(B, D)));
            g2.draw(new Line2D.Double(D, MathUtil.oneThirdPoint(D, B)));
        }

        if (getSlipType() == DOUBLE_SLIP) {
            if (state == STATE_AC) {
                g2.draw(new Line2D.Double(B, MathUtil.oneThirdPoint(B, D)));
                g2.draw(new Line2D.Double(D, MathUtil.oneThirdPoint(D, B)));

                g2.setColor(Color.red);
                g2.draw(new Line2D.Double(A, C));
            } else if (state == STATE_BD) {
                g2.setColor(Color.red);
                g2.draw(new Line2D.Double(B, D));
            } else if (state == STATE_AD) {
                g2.draw(new Line2D.Double(B, MathUtil.oneThirdPoint(B, C)));

                g2.draw(new Line2D.Double(C, MathUtil.oneThirdPoint(C, B)));

                g2.setColor(Color.red);
                g2.draw(new Line2D.Double(A, D));
            } else if (state == STATE_BC) {
                g2.draw(new Line2D.Double(A, MathUtil.oneThirdPoint(A, D)));

                g2.draw(new Line2D.Double(D, MathUtil.oneThirdPoint(D, A)));
                g2.setColor(Color.red);
                g2.draw(new Line2D.Double(B, C));
            } else {
                g2.draw(new Line2D.Double(B, MathUtil.oneThirdPoint(B, D)));
                g2.draw(new Line2D.Double(D, MathUtil.oneThirdPoint(D, B)));
            }
        } else {
            g2.draw(new Line2D.Double(A, MathUtil.oneThirdPoint(A, D)));
            g2.draw(new Line2D.Double(D, MathUtil.oneThirdPoint(D, A)));

            if (state == STATE_AD) {
                g2.setColor(Color.red);
                g2.draw(new Line2D.Double(A, D));
            } else if (state == STATE_AC) {
                g2.draw(new Line2D.Double(B, MathUtil.oneThirdPoint(B, D)));
                g2.draw(new Line2D.Double(D, MathUtil.oneThirdPoint(D, B)));

                g2.setColor(Color.red);
                g2.draw(new Line2D.Double(A, C));
            } else if (state == STATE_BD) {
                g2.setColor(Color.red);
                g2.draw(new Line2D.Double(B, D));
            } else {
                g2.draw(new Line2D.Double(B, MathUtil.oneThirdPoint(B, D)));
                g2.draw(new Line2D.Double(D, MathUtil.oneThirdPoint(D, B)));
            }
        }
    }

    class SampleStates extends JPanel {

        // Methods, constructors, fields.
        SampleStates(int state) {
            super();
            this.state = state;
        }
        int state;

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);    // paints background
            Graphics2D g2 = (Graphics2D) g;
            drawSlipState(g2, state);
        }
    }

    int testState = UNKNOWN;

    /**
     * Toggle slip states if clicked on, physical turnout exists, and not
     * disabled
     */
    public void toggleStateTest() {
        int turnAState;
        int turnBState;
        switch (testState) {
            default:
            case STATE_AC: {
                testState = STATE_BD;
                break;
            }

            case STATE_BD: {
                testState = STATE_AD;
                break;
            }

            case STATE_AD: {
                if (type == SINGLE_SLIP) {
                    testState = STATE_AC;
                } else {
                    testState = STATE_BC;
                }
                break;
            }

            case STATE_BC: {
                testState = STATE_AC;
                break;
            }
        }
        turnAState = turnoutStates.get(testState).getTestTurnoutAState();
        turnBState = turnoutStates.get(testState).getTestTurnoutBState();

        if (turnoutAComboBox.getSelectedBean() != null) {
            ((Turnout) turnoutAComboBox.getSelectedBean()).setCommandedState(turnAState);
        }
        if (turnoutBComboBox.getSelectedBean() != null) {
            ((Turnout) turnoutBComboBox.getSelectedBean()).setCommandedState(turnBState);
        }
        if (testPanel != null) {
            testPanel.repaint();
        }
    }

    class TestState extends JPanel {

        @Override
        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            drawSlipState(g2, testState);
        }
    }

    TestState testPanel;

    private void slipEditDonePressed(ActionEvent a) {
        String newName = turnoutAComboBox.getDisplayName();
        if (!turnoutName.equals(newName)) {
            if (layoutEditor.validatePhysicalTurnout(newName, editLayoutTurnoutFrame)) {
                setTurnout(newName);
            } else {
                namedTurnout = null;
                turnoutName = "";
            }
            needRedraw = true;
        }
        newName = turnoutBComboBox.getDisplayName();
        if (!turnoutBName.equals(newName)) {
            if (layoutEditor.validatePhysicalTurnout(newName, editLayoutTurnoutFrame)) {
                setTurnoutB(newName);
            } else {
                namedTurnoutB = null;
                turnoutBName = "";
            }
            needRedraw = true;
        }

        newName = blockNameComboBox.getUserName();
        if (!blockName.equals(newName)) {
            // block 1 has changed, if old block exists, decrement use
            if ((block != null)) {
                block.decrementUse();
            }
            // get new block, or null if block has been removed
            blockName = newName;

            try {
                block = layoutEditor.provideLayoutBlock(blockName);
            } catch (IllegalArgumentException ex) {
                blockName = "";
                blockNameComboBox.setText("");
                blockNameComboBox.setSelectedIndex(-1);
            }
            needRedraw = true;
            layoutEditor.auxTools.setBlockConnectivityChanged();
            needsBlockUpdate = true;
        }
        for (TurnoutState ts : turnoutStates.values()) {
            ts.updateStatesFromCombo();
        }

        // set hidden
        boolean oldHidden = hidden;
        hidden = hiddenBox.isSelected();
        if (oldHidden != hidden) {
            needRedraw = true;
        }

        editOpen = false;
        editLayoutTurnoutFrame.setVisible(false);
        editLayoutTurnoutFrame.dispose();
        editLayoutTurnoutFrame = null;
        if (needsBlockUpdate) {
            updateBlockInfo();
        }
        if (needRedraw) {
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }   // slipEditDonePressed

    private void slipEditCancelPressed(ActionEvent a) {
        editOpen = false;
        editLayoutTurnoutFrame.setVisible(false);
        editLayoutTurnoutFrame.dispose();
        editLayoutTurnoutFrame = null;
        if (needsBlockUpdate) {
            updateBlockInfo();
        }
        if (needRedraw) {
            layoutEditor.redrawPanel();
            layoutEditor.setDirty();
        }
    }

    /**
     * Clean up when this object is no longer needed. Should not be called while
     * the object is still displayed; see remove()
     */
    @Override
    void dispose() {
        if (popup != null) {
            popup.removeAll();
        }
        popup = null;
    }

    /**
     * Removes this object from display and persistance
     */
    @Override
    void remove() {
        disableSML(getSignalAMast());
        disableSML(getSignalBMast());
        disableSML(getSignalCMast());
        disableSML(getSignalDMast());
        removeSML(getSignalAMast());
        removeSML(getSignalBMast());
        removeSML(getSignalCMast());
        removeSML(getSignalDMast());
    }

    private void disableSML(SignalMast signalMast) {
        if (signalMast == null) {
            return;
        }
        InstanceManager.getDefault(jmri.SignalMastLogicManager.class).disableLayoutEditorUse(signalMast);
    }

// AFAIK this isn't used anywhere
//    public boolean singleSlipStraightEqual() {
//        if (type != SINGLE_SLIP) {
//            return false;
//        }
//        return turnoutStates.get(STATE_AC).equals(turnoutStates.get(STATE_BD));
//    }
    Hashtable<Integer, TurnoutState> turnoutStates = new Hashtable<Integer, TurnoutState>(4);

    public int getTurnoutState(Turnout turn, int state) {
        if (turn == getTurnout()) {
            return getTurnoutState(state);
        }
        return getTurnoutBState(state);
    }

    public int getTurnoutState(int state) {
        return turnoutStates.get(Integer.valueOf(state)).getTurnoutAState();
    }

    public int getTurnoutBState(int state) {
        return turnoutStates.get(Integer.valueOf(state)).getTurnoutBState();
    }

    public void setTurnoutStates(int state, String turnStateA, String turnStateB) {
        if (!turnoutStates.containsKey(state)) {
            log.error("Trying to set invalid state for slip " + getDisplayName());
            return;
        }
        turnoutStates.get(state).setTurnoutAState(Integer.valueOf(turnStateA));
        turnoutStates.get(state).setTurnoutBState(Integer.valueOf(turnStateB));
    }

    //Internal call to update the state of the slip depending upon the turnout states.
    private void updateState() {
        int state_a = getTurnout().getKnownState();
        int state_b = getTurnoutB().getKnownState();
        for (Entry<Integer, TurnoutState> en : turnoutStates.entrySet()) {
            if (en.getValue().getTurnoutAState() == state_a) {
                if (en.getValue().getTurnoutBState() == state_b) {
                    currentState = en.getKey();
                    layoutEditor.redrawPanel();
                    return;
                }
            }
        }
    }

    /**
     * draw this slip
     *
     * @param g2 the graphics port to draw to
     */
    public void draw(Graphics2D g2) {
        if (!isHidden() || layoutEditor.isEditable()) {
            LayoutBlock b = getLayoutBlock();
            Color mainColourA = defaultTrackColor;
            Color subColourA = defaultTrackColor;
            if (b != null) {
                mainColourA = b.getBlockColor();
                subColourA = b.getBlockTrackColor();
            }

            b = getLayoutBlockB();
            Color mainColourB = defaultTrackColor;
            Color subColourB = defaultTrackColor;
            if (b != null) {
                mainColourB = b.getBlockColor();
                subColourB = b.getBlockTrackColor();
            }

            b = getLayoutBlockC();
            Color mainColourC = defaultTrackColor;
            Color subColourC = defaultTrackColor;
            if (b != null) {
                mainColourC = b.getBlockColor();
                subColourC = b.getBlockTrackColor();
            }

            b = getLayoutBlockD();
            Color mainColourD = defaultTrackColor;
            Color subColourD = defaultTrackColor;
            if (b != null) {
                mainColourD = b.getBlockColor();
                subColourD = b.getBlockTrackColor();
            }

            float w = layoutEditor.setTrackStrokeWidth(g2, isMainline());

            boolean isMainA = (connectA != null) && (((TrackSegment) connectA).getMainline());
            boolean isMainB = (connectB != null) && (((TrackSegment) connectB).getMainline());
            boolean isMainC = (connectC != null) && (((TrackSegment) connectC).getMainline());
            boolean isMainD = (connectD != null) && (((TrackSegment) connectD).getMainline());

            if (getSlipState() == STATE_AC) {
                g2.setColor(mainColourA);
                layoutEditor.setTrackStrokeWidth(g2, isMainA);
                g2.draw(new Line2D.Double(pointA, MathUtil.midPoint(pointA, pointC)));

                g2.setColor(mainColourC);
                layoutEditor.setTrackStrokeWidth(g2, isMainC);
                g2.draw(new Line2D.Double(pointC, MathUtil.midPoint(pointC, pointA)));
            } else {
                g2.setColor(subColourA);
                layoutEditor.setTrackStrokeWidth(g2, isMainA);
                g2.draw(new Line2D.Double(pointA, MathUtil.oneThirdPoint(pointA, pointC)));

                g2.setColor(subColourC);
                layoutEditor.setTrackStrokeWidth(g2, isMainC);
                g2.draw(new Line2D.Double(pointC, MathUtil.oneThirdPoint(pointC, pointA)));
            }

            if (getSlipState() == STATE_BD) {
                g2.setColor(mainColourB);
                layoutEditor.setTrackStrokeWidth(g2, isMainB);
                g2.draw(new Line2D.Double(pointB, MathUtil.midPoint(pointB, pointD)));

                g2.setColor(mainColourD);
                layoutEditor.setTrackStrokeWidth(g2, isMainD);
                g2.draw(new Line2D.Double(pointD, MathUtil.midPoint(pointD, pointB)));
            } else {
                g2.setColor(subColourB);
                layoutEditor.setTrackStrokeWidth(g2, isMainB);
                g2.draw(new Line2D.Double(pointB, MathUtil.oneThirdPoint(pointB, pointD)));

                g2.setColor(subColourD);
                layoutEditor.setTrackStrokeWidth(g2, isMainD);
                g2.draw(new Line2D.Double(pointD, MathUtil.oneThirdPoint(pointD, pointB)));
            }

            if (getSlipState() == STATE_AD) {
                g2.setColor(mainColourA);
                layoutEditor.setTrackStrokeWidth(g2, isMainA);
                g2.draw(new Line2D.Double(pointA, MathUtil.midPoint(pointA, pointD)));

                g2.setColor(mainColourD);
                layoutEditor.setTrackStrokeWidth(g2, isMainD);
                g2.draw(new Line2D.Double(pointD, MathUtil.midPoint(pointD, pointA)));
            } else {
                g2.setColor(subColourA);
                layoutEditor.setTrackStrokeWidth(g2, isMainA);
                g2.draw(new Line2D.Double(pointA, MathUtil.oneThirdPoint(pointA, pointD)));

                g2.setColor(subColourD);
                layoutEditor.setTrackStrokeWidth(g2, isMainD);
                g2.draw(new Line2D.Double(pointD, MathUtil.oneThirdPoint(pointD, pointA)));
            }

            if (getSlipState() == STATE_BC) {
                g2.setColor(mainColourB);
                layoutEditor.setTrackStrokeWidth(g2, isMainB);
                g2.draw(new Line2D.Double(pointB, MathUtil.midPoint(pointB, pointC)));

                g2.setColor(mainColourC);
                layoutEditor.setTrackStrokeWidth(g2, isMainC);
                g2.draw(new Line2D.Double(pointC, MathUtil.midPoint(pointC, pointB)));
            } else if (getSlipType() == DOUBLE_SLIP) {
                g2.setColor(subColourB);
                layoutEditor.setTrackStrokeWidth(g2, isMainB);
                g2.draw(new Line2D.Double(pointB, MathUtil.oneThirdPoint(pointB, pointC)));

                g2.setColor(subColourC);
                layoutEditor.setTrackStrokeWidth(g2, isMainC);
                g2.draw(new Line2D.Double(pointC, MathUtil.oneThirdPoint(pointC, pointB)));
            }
        }   // if (!(getHidden() && !layoutEditor.isEditable()))
    }   // draw(Graphics2D g2)

    public void drawControls(Graphics2D g2) {
        // drawHidden left/right turnout control circles
        Point2D leftCircleCenter = getCoordsLeft();
        g2.draw(layoutEditor.trackControlCircleAt(leftCircleCenter));

        Point2D rightCircleCenter = getCoordsRight();
        g2.draw(layoutEditor.trackControlCircleAt(rightCircleCenter));
    }   // drawControls(Graphics2D g2)

    public void drawEditControls(Graphics2D g2) {
        if (getConnectA() == null) {
            g2.setColor(Color.magenta);
        } else {
            g2.setColor(Color.blue);
        }
        g2.draw(layoutEditor.trackControlPointRectAt(getCoordsA()));

        if (getConnectB() == null) {
            g2.setColor(Color.red);
        } else {
            g2.setColor(Color.green);
        }
        g2.draw(layoutEditor.trackControlPointRectAt(getCoordsB()));

        if (getConnectC() == null) {
            g2.setColor(Color.red);
        } else {
            g2.setColor(Color.green);
        }
        g2.draw(layoutEditor.trackControlPointRectAt(getCoordsC()));

        if (getConnectD() == null) {
            g2.setColor(Color.red);
        } else {
            g2.setColor(Color.green);
        }
        g2.draw(layoutEditor.trackControlPointRectAt(getCoordsD()));
    }   // drawEditControls(Graphics2D g2)

    static class TurnoutState {

        int turnoutA = Turnout.CLOSED;
        int turnoutB = Turnout.CLOSED;
        JComboBox<String> turnoutABox;
        JComboBox<String> turnoutBBox;

        TurnoutState(int turnoutA, int turnoutB) {
            this.turnoutA = turnoutA;
            this.turnoutB = turnoutB;
        }

        int getTurnoutAState() {
            return turnoutA;
        }

        int getTurnoutBState() {
            return turnoutB;
        }

        void setTurnoutAState(int state) {
            turnoutA = state;
        }

        void setTurnoutBState(int state) {
            turnoutB = state;
        }

        JComboBox<String> getComboA() {
            if (turnoutABox == null) {
                String state[] = new String[]{InstanceManager.turnoutManagerInstance().getClosedText(),
                    InstanceManager.turnoutManagerInstance().getThrownText()};
                turnoutABox = new JComboBox<String>(state);
                if (turnoutA == Turnout.THROWN) {
                    turnoutABox.setSelectedIndex(1);
                }
            }
            return turnoutABox;
        }

        JComboBox<String> getComboB() {
            if (turnoutBBox == null) {
                String state[] = new String[]{InstanceManager.turnoutManagerInstance().getClosedText(),
                    InstanceManager.turnoutManagerInstance().getThrownText()};
                turnoutBBox = new JComboBox<String>(state);
                if (turnoutB == Turnout.THROWN) {
                    turnoutBBox.setSelectedIndex(1);
                }
            }
            return turnoutBBox;
        }

        int getTestTurnoutAState() {
            if (turnoutABox.getSelectedIndex() == 0) {
                return Turnout.CLOSED;
            }
            return Turnout.THROWN;
        }

        int getTestTurnoutBState() {
            if (turnoutBBox.getSelectedIndex() == 0) {
                return Turnout.CLOSED;
            }
            return Turnout.THROWN;
        }

        void updateStatesFromCombo() {
            if (turnoutABox == null || turnoutBBox == null) {
                return;
            }
            if (turnoutABox.getSelectedIndex() == 0) {
                turnoutA = Turnout.CLOSED;
            } else {
                turnoutA = Turnout.THROWN;
            }
            if (turnoutBBox.getSelectedIndex() == 0) {
                turnoutB = Turnout.CLOSED;
            } else {
                turnoutB = Turnout.THROWN;
            }
        }

        boolean equals(TurnoutState ts) {
            if (ts.getTurnoutAState() != this.getTurnoutAState()) {
                return false;
            }
            if (ts.getTurnoutBState() != this.getTurnoutBState()) {
                return false;
            }
            return true;
        }

    }

    /*
        this is used by ConnectivityUtil to determine the turnout state necessary to get from prevLayoutBlock ==> currLayoutBlock ==> nextLayoutBlock
     */
    protected int getConnectivityStateForLayoutBlocks(LayoutBlock thisLayoutBlock, LayoutBlock prevLayoutBlock, LayoutBlock nextLayoutBlock, boolean suppress) {
        int result = Turnout.UNKNOWN;
        LayoutBlock layoutBlockA = ((TrackSegment) getConnectA()).getLayoutBlock();
        LayoutBlock layoutBlockB = ((TrackSegment) getConnectB()).getLayoutBlock();
        LayoutBlock layoutBlockC = ((TrackSegment) getConnectC()).getLayoutBlock();
        LayoutBlock layoutBlockD = ((TrackSegment) getConnectD()).getLayoutBlock();

        if (layoutBlockA == thisLayoutBlock) {
            if (layoutBlockC == nextLayoutBlock || layoutBlockC == prevLayoutBlock) {
                result = LayoutSlip.STATE_AC;
            } else if (layoutBlockD == nextLayoutBlock || layoutBlockD == prevLayoutBlock) {
                result = LayoutSlip.STATE_AD;
            } else if (layoutBlockC == thisLayoutBlock) {
                result = LayoutSlip.STATE_AC;
            } else if (layoutBlockD == thisLayoutBlock) {
                result = LayoutSlip.STATE_AD;
            }
        } else if (layoutBlockB == thisLayoutBlock) {
            if (getTurnoutType() == LayoutSlip.DOUBLE_SLIP) {
                if (layoutBlockD == nextLayoutBlock || layoutBlockD == prevLayoutBlock) {
                    result = LayoutSlip.STATE_BD;
                } else if (layoutBlockC == nextLayoutBlock || layoutBlockC == prevLayoutBlock) {
                    result = LayoutSlip.STATE_BC;
                } else if (layoutBlockD == thisLayoutBlock) {
                    result = LayoutSlip.STATE_BD;
                } else if (layoutBlockC == thisLayoutBlock) {
                    result = LayoutSlip.STATE_BC;
                }
            } else {
                if (layoutBlockD == nextLayoutBlock || layoutBlockD == prevLayoutBlock) {
                    result = LayoutSlip.STATE_BD;
                } else if (layoutBlockD == thisLayoutBlock) {
                    result = LayoutSlip.STATE_BD;
                }
            }
        } else if (layoutBlockC == thisLayoutBlock) {
            if (getTurnoutType() == LayoutSlip.DOUBLE_SLIP) {
                if (layoutBlockA == nextLayoutBlock || layoutBlockA == prevLayoutBlock) {
                    result = LayoutSlip.STATE_AC;
                } else if (layoutBlockB == nextLayoutBlock || layoutBlockB == prevLayoutBlock) {
                    result = LayoutSlip.STATE_BC;
                } else if (layoutBlockA == thisLayoutBlock) {
                    result = LayoutSlip.STATE_AC;
                } else if (layoutBlockB == thisLayoutBlock) {
                    result = LayoutSlip.STATE_BC;
                }
            } else {
                if (layoutBlockA == nextLayoutBlock || layoutBlockA == prevLayoutBlock) {
                    result = LayoutSlip.STATE_AC;
                } else if (layoutBlockA == thisLayoutBlock) {
                    result = LayoutSlip.STATE_AC;
                }
            }
        } else if (layoutBlockD == thisLayoutBlock) {
            if (layoutBlockA == nextLayoutBlock || layoutBlockA == prevLayoutBlock) {
                result = LayoutSlip.STATE_AD;
            } else if (layoutBlockB == nextLayoutBlock || layoutBlockB == prevLayoutBlock) {
                result = LayoutSlip.STATE_BD;
            } else if (layoutBlockA == thisLayoutBlock) {
                result = LayoutSlip.STATE_AD;
            } else if (layoutBlockB == thisLayoutBlock) {
                result = LayoutSlip.STATE_AD;
            }
        } else {
            result = LayoutSlip.UNKNOWN;
        }
        if (!suppress && (result == LayoutSlip.UNKNOWN)) {
            log.error("Cannot determine slip setting for " + getName());
        }
        return result;
    }   // getConnectivityStateForLayoutBlocks

    /*
        return the layout connectivity for this Layout Slip
     */
    protected ArrayList<LayoutConnectivity> getLayoutConnectivity() {
        ArrayList<LayoutConnectivity> results = new ArrayList<LayoutConnectivity>();

        LayoutConnectivity lc = null;
        LayoutBlock lbA = getLayoutBlock(), lbB = getLayoutBlockB(), lbC = getLayoutBlockC(), lbD = getLayoutBlockD();
        if (lbA != null) {
            if (lbA != lbC) {
                // have a AC block boundary, create a LayoutConnectivity
                log.debug("Block boundary  ('{}'<->'{}') found at {}", lbA, lbC, this);
                lc = new LayoutConnectivity(lbA, lbC);
                lc.setXoverBoundary(this, LayoutConnectivity.XOVER_BOUNDARY_AC);
                lc.setDirection(LayoutEditorAuxTools.computeDirection(getCoordsA(), getCoordsC()));
                results.add(lc);
            }
            if ((type == DOUBLE_SLIP) && (lbB != lbD)) {
                // have a BD block boundary, create a LayoutConnectivity
                log.debug("Block boundary  ('{}'<->'{}') found at {}", lbB, lbD, this);
                lc = new LayoutConnectivity(lbB, lbD);
                lc.setXoverBoundary(this, LayoutConnectivity.XOVER_BOUNDARY_BD);
                lc.setDirection(LayoutEditorAuxTools.computeDirection(getCoordsB(), getCoordsD()));
                results.add(lc);
            }
            if (lbA != lbD) {
                // have a AD block boundary, create a LayoutConnectivity
                log.debug("Block boundary  ('{}'<->'{}') found at {}", lbA, lbD, this);
                lc = new LayoutConnectivity(lbA, lbD);
                lc.setXoverBoundary(this, LayoutConnectivity.XOVER_BOUNDARY_AD);
                lc.setDirection(LayoutEditorAuxTools.computeDirection(getCoordsA(), getCoordsD()));
                results.add(lc);
            }
            if (lbB != lbC) {
                // have a BC block boundary, create a LayoutConnectivity
                log.debug("Block boundary  ('{}'<->'{}') found at {}", lbB, lbC, this);
                lc = new LayoutConnectivity(lbB, lbC);
                lc.setXoverBoundary(this, LayoutConnectivity.XOVER_BOUNDARY_BC);
                lc.setDirection(LayoutEditorAuxTools.computeDirection(getCoordsB(), getCoordsC()));
                results.add(lc);
            }
        }
        return results;
    }   // getLayoutConnectivity()

    private final static Logger log = LoggerFactory.getLogger(LayoutSlip.class);
}
