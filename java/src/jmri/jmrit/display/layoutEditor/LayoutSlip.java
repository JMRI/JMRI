package jmri.jmrit.display.layoutEditor;

import static java.lang.Integer.parseInt;
import static java.lang.Math.PI;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.AbstractAction;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import jmri.InstanceManager;
import jmri.NamedBeanHandle;
import jmri.Path;
import jmri.SignalMast;
import jmri.Turnout;
import jmri.jmrit.display.layoutEditor.blockRoutingTable.LayoutBlockRouteTableAction;
import jmri.util.MathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A LayoutSlip is a crossing of two straight tracks designed in such a way as
 * to allow trains to change from one straight track to the other, as well as
 * going straight across.
 * <p>
 * A LayoutSlip has four connection points, designated A, B, C, and D. A train
 * may proceed between A and D, A and C, B and D and in the case of
 * double-slips, B and C.
 * <p>
 * {@literal
 * \\      //
 *   A==-==D
 *    \\ //
 *      X
 *    // \\
 *   B==-==C
 *  //      \\
 * literal}
 * <p>
 * For drawing purposes, each LayoutSlip carries a center point and
 * displacements for A and B. The displacements for C = - the displacement for
 * A, and the displacement for D = - the displacement for B. The center point
 * and these displacements may be adjusted by the user when in edit mode.
 * <p>
 * When LayoutSlips are first created, there are no connections. Block
 * information and connections are added when available.
 * <p>
 * SignalHead names are saved here to keep track of where signals are.
 * LayoutSlip only serves as a storage place for SignalHead names. The names are
 * placed here by Set Signals at Level Crossing in Tools menu.
 *
 * @author Dave Duchamp Copyright (c) 2004-2007
 * @author George Warner Copyright (c) 2017-2019
 */
public class LayoutSlip extends LayoutTurnout {

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
    @Override
    public String toString() {
        return String.format("LayoutSlip %s (%s)", getId(), getSlipStateString(getSlipState()));
    }

    public void setTurnoutType(int slipType) {
        setSlipType(slipType);
    }

    public void setSlipType(int slipType) {
        if (type != slipType) {
            type = slipType;
            turnoutStates.put(STATE_AC, new TurnoutState(Turnout.CLOSED, Turnout.CLOSED));
            turnoutStates.put(STATE_AD, new TurnoutState(Turnout.CLOSED, Turnout.THROWN));
            turnoutStates.put(STATE_BD, new TurnoutState(Turnout.THROWN, Turnout.THROWN));
            if (type == SINGLE_SLIP) {
                turnoutStates.remove(STATE_BC);
            } else if (type == DOUBLE_SLIP) {
                turnoutStates.put(STATE_BC, new TurnoutState(Turnout.THROWN, Turnout.CLOSED));
            } else {
                log.error("Invalid slip Type " + slipType); //I18IN
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
        Turnout result = null;
        if (namedTurnoutB == null) {
            if (!turnoutBName.isEmpty()) {
                setTurnoutB(turnoutBName);
            }
        }
        if (namedTurnoutB != null) {
            result = namedTurnoutB.getBean();
        }
        return result;
    }

    public void setTurnoutB(@CheckForNull String tName) {
        boolean reactivate = false;
        if (namedTurnoutB != null) {
            deactivateTurnout();
            reactivate = (namedTurnout != null);
        }
        turnoutBName = tName;
        Turnout turnout = null;
        if (turnoutBName != null && !turnoutBName.isEmpty()) {
            turnout = InstanceManager.turnoutManagerInstance().getTurnout(turnoutBName);
        }
        if (turnout != null) {
            namedTurnoutB = InstanceManager.getDefault(jmri.NamedBeanHandleManager.class).getNamedBeanHandle(turnoutBName, turnout);
            activateTurnout();
        } else {
            turnoutBName = "";
            namedTurnoutB = null;
        }
        if (reactivate) {
            // this has to be called even on a delete in order
            // to re-activate namedTurnout (A) (if necessary)
            activateTurnout();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LayoutTrack getConnection(int connectionType) throws jmri.JmriException {
        switch (connectionType) {
            case SLIP_A:
                return connectA;
            case SLIP_B:
                return connectB;
            case SLIP_C:
                return connectC;
            case SLIP_D:
                return connectD;
            default:
                log.error("Invalid Connection Type " + connectionType); //I18IN
                throw new jmri.JmriException("Invalid Connection Type " + connectionType);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConnection(int connectionType, @CheckForNull LayoutTrack o, int type) throws jmri.JmriException {
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
        String name = "Slip " + getId();
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

    private String getSlipStateString(int slipState) {
        String result = Bundle.getMessage("BeanStateUnknown");
        switch (slipState) {
            case STATE_AC: {
                result = "AC";
                break;
            }
            case STATE_BD: {
                result = "BD";
                break;
            }
            case STATE_AD: {
                result = "AD";
                break;
            }
            case STATE_BC: {
                result = "BC";
                break;
            }
            default: {
                break;
            }
        }
        return result;
    }

    /**
     * Toggle slip states if clicked on, physical turnout exists, and not
     * disabled
     */
    public void toggleState(int selectedPointType) {
        if (!disabled && !(disableWhenOccupied && isOccupied())) {
            int newSlipState = getSlipState();
            switch (selectedPointType) {
                case SLIP_LEFT: {
                    switch (newSlipState) {
                        case STATE_AC: {
                            if (type == SINGLE_SLIP) {
                                newSlipState = STATE_BD;
                            } else {
                                newSlipState = STATE_BC;
                            }
                            break;
                        }
                        case STATE_AD: {
                            newSlipState = STATE_BD;
                            break;
                        }
                        case STATE_BC:
                        default: {
                            newSlipState = STATE_AC;
                            break;
                        }
                        case STATE_BD: {
                            newSlipState = STATE_AD;
                            break;
                        }
                    }
                    break;
                }
                case SLIP_RIGHT: {
                    switch (newSlipState) {
                        case STATE_AC: {
                            newSlipState = STATE_AD;
                            break;
                        }
                        case STATE_AD: {
                            newSlipState = STATE_AC;
                            break;
                        }
                        case STATE_BC:
                        default: {
                            newSlipState = STATE_BD;
                            break;
                        }
                        case STATE_BD: {
                            if (type == SINGLE_SLIP) {
                                newSlipState = STATE_AC;
                            } else {
                                newSlipState = STATE_BC;
                            }
                            break;
                        }
                    }
                    break;
                }
                default:
                    jmri.util.Log4JUtil.warnOnce(log, "Unexpected selectedPointType = {}", selectedPointType);
                    break;
            }   // switch
            setSlipState(newSlipState);
        }
    }

    private void setSlipState(int newSlipState) {
        if (disableWhenOccupied && isOccupied()) {
            log.debug("Turnout not changed as Block is Occupied");
        } else if (!disabled) {
            currentState = newSlipState;
            TurnoutState ts = turnoutStates.get(newSlipState);
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
        switch (getSlipState()) {
            case STATE_AC: {
                result = ((getLayoutBlock().getOccupancy() == LayoutBlock.OCCUPIED)
                        || (getLayoutBlockC().getOccupancy() == LayoutBlock.OCCUPIED));
                break;
            }
            case STATE_AD: {
                result = ((getLayoutBlock().getOccupancy() == LayoutBlock.OCCUPIED)
                        || (getLayoutBlockD().getOccupancy() == LayoutBlock.OCCUPIED));
                break;
            }
            case STATE_BC: {
                result = ((getLayoutBlockB().getOccupancy() == LayoutBlock.OCCUPIED)
                        || (getLayoutBlockC().getOccupancy() == LayoutBlock.OCCUPIED));
                break;
            }
            case STATE_BD: {
                result = ((getLayoutBlockB().getOccupancy() == LayoutBlock.OCCUPIED)
                        || (getLayoutBlockD().getOccupancy() == LayoutBlock.OCCUPIED));
                break;
            }
            case UNKNOWN: {
                result = ((getLayoutBlock().getOccupancy() == LayoutBlock.OCCUPIED)
                        || (getLayoutBlockB().getOccupancy() == LayoutBlock.OCCUPIED)
                        || (getLayoutBlockC().getOccupancy() == LayoutBlock.OCCUPIED)
                        || (getLayoutBlockD().getOccupancy() == LayoutBlock.OCCUPIED));
                break;
            }
            default: {
                log.error("invalid slip state: {}", getSlipState());
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
        updateState();
    }

    private void deactivateTurnout() {
        if (mTurnoutListener != null) {
            if (namedTurnout != null) {
                namedTurnout.getBean().removePropertyChangeListener(mTurnoutListener);
            }
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
        double circleRadius = LayoutEditor.SIZE * layoutEditor.getTurnoutCircleSize();
        double leftFract = circleRadius / center.distance(leftCenter);
        return MathUtil.lerp(center, leftCenter, leftFract);
    }

    protected Point2D getCoordsRight() {
        Point2D rightCenter = MathUtil.midPoint(getCoordsC(), getCoordsD());
        double circleRadius = LayoutEditor.SIZE * layoutEditor.getTurnoutCircleSize();
        double rightFract = circleRadius / center.distance(rightCenter);
        return MathUtil.lerp(center, rightCenter, rightFract);
    }

    /**
     * return the coordinates for the specified connection type
     *
     * @param connectionType the connection type
     * @return the Point2D coordinates
     */
    @Override
    public Point2D getCoordsForConnectionType(int connectionType) {
        Point2D result = center;
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
     * {@inheritDoc}
     */
    // just here for testing; should be removed when I'm done...
    @Override
    public Rectangle2D getBounds() {
        return super.getBounds();
    }

    @Override
    protected void updateBlockInfo() {
        LayoutBlock b1 = null;
        LayoutBlock b2 = null;
        if (getLayoutBlock() != null) {
            getLayoutBlock().updatePaths();
        }
        if (connectA != null) {
            b1 = ((TrackSegment) connectA).getLayoutBlock();
            if ((b1 != null) && (b1 != getLayoutBlock())) {
                b1.updatePaths();
            }
        }
        if (connectC != null) {
            b2 = ((TrackSegment) connectC).getLayoutBlock();
            if ((b2 != null) && (b2 != getLayoutBlock()) && (b2 != b1)) {
                b2.updatePaths();
            }
        }

        if (connectB != null) {
            b1 = ((TrackSegment) connectB).getLayoutBlock();
            if ((b1 != null) && (b1 != getLayoutBlock())) {
                b1.updatePaths();
            }
        }
        if (connectD != null) {
            b2 = ((TrackSegment) connectD).getLayoutBlock();
            if ((b2 != null) && (b2 != getLayoutBlock()) && (b2 != b1)) {
                b2.updatePaths();
            }
        }
        reCheckBlockBoundary();
    }

    /**
     * Methods to test if mainline track or not Returns true if either
     * connecting track segment is mainline Defaults to not mainline if
     * connecting track segments are missing
     */
    @Override
    public boolean isMainline() {
        if (((connectA != null) && (((TrackSegment) connectA).isMainline()))
                || ((connectB != null) && (((TrackSegment) connectB).isMainline()))
                || ((connectC != null) && (((TrackSegment) connectC).isMainline()))
                || ((connectD != null) && (((TrackSegment) connectD).isMainline()))) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected int findHitPointType(@Nonnull Point2D hitPoint, boolean useRectangles, boolean requireUnconnected) {
        int result = NONE;  // assume point not on connection

        if (!requireUnconnected) {
            // calculate radius of turnout control circle
            double circleRadius = LayoutEditor.SIZE * layoutEditor.getTurnoutCircleSize();

            // get left and right centers
            Point2D leftCenter = getCoordsLeft();
            Point2D rightCenter = getCoordsRight();

            if (useRectangles) {
                // calculate turnout's left control rectangle
                Rectangle2D leftRectangle = layoutEditor.trackControlCircleRectAt(leftCenter);
                if (leftRectangle.contains(hitPoint)) {
                    //point is in this turnout's left control rectangle
                    result = SLIP_LEFT;
                }
                Rectangle2D rightRectangle = layoutEditor.trackControlCircleRectAt(rightCenter);
                if (rightRectangle.contains(hitPoint)) {
                    //point is in this turnout's right control rectangle
                    result = SLIP_RIGHT;
                }
            } else {
                //check east/west turnout control circles
                double leftDistance = hitPoint.distance(leftCenter);
                double rightDistance = hitPoint.distance(rightCenter);

                if ((leftDistance <= circleRadius) || (rightDistance <= circleRadius)) {
                    //mouse was pressed on this slip
                    result = (leftDistance < rightDistance) ? SLIP_LEFT : SLIP_RIGHT;
                }
            }
        }

        // have we found anything yet?
        if (result == NONE) {
            // rather than create rectangles for all the points below and
            // see if the passed in point is in one of those rectangles
            // we can create a rectangle for the passed in point and then
            // test if any of the points below are in that rectangle instead.
            Rectangle2D r = layoutEditor.trackEditControlRectAt(hitPoint);

            if (!requireUnconnected || (getConnectA() == null)) {
                //check the A connection point
                if (r.contains(getCoordsA())) {
                    result = SLIP_A;
                }
            }

            if (!requireUnconnected || (getConnectB() == null)) {
                //check the B connection point
                if (r.contains(getCoordsB())) {
                    result = SLIP_B;
                }
            }

            if (!requireUnconnected || (getConnectC() == null)) {
                //check the C connection point
                if (r.contains(getCoordsC())) {
                    result = SLIP_C;
                }
            }

            if (!requireUnconnected || (getConnectD() == null)) {
                //check the D connection point
                if (r.contains(getCoordsD())) {
                    result = SLIP_D;
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
    public void setCoordsCenter(@Nonnull Point2D p) {
        center = p;
        pointA = MathUtil.add(center, dispA);
        pointB = MathUtil.add(center, dispB);
        pointC = MathUtil.subtract(center, dispA);
        pointD = MathUtil.subtract(center, dispB);
    }

    @Override
    public void setCoordsA(@Nonnull Point2D p) {
        pointA = p;
        dispA = MathUtil.subtract(pointA, center);
        pointC = MathUtil.subtract(center, dispA);
    }

    @Override
    public void setCoordsB(@Nonnull Point2D p) {
        pointB = p;
        dispB = MathUtil.subtract(pointB, center);
        pointD = MathUtil.subtract(center, dispB);
    }

    @Override
    public void setCoordsC(@Nonnull Point2D p) {
        pointC = p;
        dispA = MathUtil.subtract(center, pointC);
        pointA = MathUtil.add(center, dispA);
    }

    @Override
    public void setCoordsD(@Nonnull Point2D p) {
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

    JPopupMenu popup = null;

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    protected JPopupMenu showPopup(@CheckForNull MouseEvent mouseEvent) {
        if (popup != null) {
            popup.removeAll();
        } else {
            popup = new JPopupMenu();
        }
        if (layoutEditor.isEditable()) {
            String slipStateString = getSlipStateString(getSlipState());
            slipStateString = String.format(" (%s)", slipStateString);

            JMenuItem jmi = null;
            switch (type) {
                case SINGLE_SLIP: {
                    jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("LayoutSingleSlip")) + getId() + slipStateString);
                    break;
                }
                case DOUBLE_SLIP: {
                    jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("LayoutDoubleSlip")) + getId() + slipStateString);
                    break;
                }
                default: {
                    log.error("Unknown slip type: {}", type);
                }
            }
            if (jmi != null) {
                jmi.setEnabled(false);
            }

            if (getTurnout() == null) {
                jmi = popup.add(Bundle.getMessage("NoTurnout"));
            } else {
                String stateString = getTurnoutStateString(getTurnout().getKnownState());
                stateString = String.format(" (%s)", stateString);
                jmi = popup.add(Bundle.getMessage("BeanNameTurnout") + ": " + getTurnoutName() + stateString);
            }
            jmi.setEnabled(false);

            if (getTurnoutB() == null) {
                jmi = popup.add(Bundle.getMessage("NoTurnout"));
            } else {
                String stateString = getTurnoutStateString(getTurnoutB().getKnownState());
                stateString = String.format(" (%s)", stateString);
                jmi = popup.add(Bundle.getMessage("BeanNameTurnout") + ": " + getTurnoutBName() + stateString);
            }
            jmi.setEnabled(false);

            boolean blockAssigned = false;
            if (getBlockName().isEmpty()) {
                jmi = popup.add(Bundle.getMessage("NoBlock"));
                jmi.setEnabled(false);
            } else {
                blockAssigned = true;

                jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("Block_ID", "A")) + getLayoutBlock().getDisplayName());
                jmi.setEnabled(false);

                // check if extra blocks have been entered
                if ((getLayoutBlockB() != null) && (getLayoutBlockB() != getLayoutBlock())) {
                    jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("Block_ID", "B")) + getLayoutBlockB().getDisplayName());
                    jmi.setEnabled(false);
                }
                if ((getLayoutBlockC() != null) && (getLayoutBlockC() != getLayoutBlock())) {
                    jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("Block_ID", "C")) + getLayoutBlockC().getDisplayName());
                    jmi.setEnabled(false);
                }
                if ((getLayoutBlockD() != null) && (getLayoutBlockD() != getLayoutBlock())) {
                    jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("Block_ID", "D")) + getLayoutBlockD().getDisplayName());
                    jmi.setEnabled(false);
                }
            }

            // if there are any track connections
            if ((connectA != null) || (connectB != null)
                    || (connectC != null) || (connectD != null)) {
                JMenu connectionsMenu = new JMenu(Bundle.getMessage("Connections")); // there is no pane opening (which is what ... implies)
                if (connectA != null) {
                    connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "A") + connectA.getName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            LayoutEditorFindItems lf = layoutEditor.getFinder();
                            LayoutTrack lt = lf.findObjectByName(connectA.getName());
                            // this shouldn't ever be null... however...
                            if (lt != null) {
                                layoutEditor.setSelectionRect(lt.getBounds());
                                lt.showPopup();
                            }
                        }
                    });
                }
                if (connectB != null) {
                    connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "B") + connectB.getName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            LayoutEditorFindItems lf = layoutEditor.getFinder();
                            LayoutTrack lt = lf.findObjectByName(connectB.getName());
                            // this shouldn't ever be null... however...
                            if (lt != null) {
                                layoutEditor.setSelectionRect(lt.getBounds());
                                lt.showPopup();
                            }
                        }
                    });
                }
                if (connectC != null) {
                    connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "C") + connectC.getName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            LayoutEditorFindItems lf = layoutEditor.getFinder();
                            LayoutTrack lt = lf.findObjectByName(connectC.getName());
                            // this shouldn't ever be null... however...
                            if (lt != null) {
                                layoutEditor.setSelectionRect(lt.getBounds());
                                lt.showPopup();
                            }
                        }
                    });
                }
                if (connectD != null) {
                    connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "D") + connectD.getName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            LayoutEditorFindItems lf = layoutEditor.getFinder();
                            LayoutTrack lt = lf.findObjectByName(connectD.getName());
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

            JCheckBoxMenuItem hiddenCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("Hidden"));
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

            cbmi = new JCheckBoxMenuItem(Bundle.getMessage("DisabledWhenOccupied"));
            cbmi.setSelected(disableWhenOccupied);
            popup.add(cbmi);
            cbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                JCheckBoxMenuItem o = (JCheckBoxMenuItem) e3.getSource();
                setDisableWhenOccupied(o.isSelected());
            });

            popup.add(new AbstractAction(Bundle.getMessage("ButtonEdit")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    layoutEditor.getLayoutTrackEditors().editLayoutSlip(LayoutSlip.this);
                }
            });
            popup.add(new AbstractAction(Bundle.getMessage("ButtonDelete")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (canRemove() && layoutEditor.removeLayoutSlip(LayoutSlip.this)) {
                        // Returned true if user did not cancel
                        remove();
                        dispose();
                    }
                }
            });
            if ((connectA == null) && (connectB == null)
                    && (connectC == null) && (connectD == null)) {
                JMenuItem rotateItem = new JMenuItem(Bundle.getMessage("Rotate") + "...");
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
                                        Bundle.getMessage("MakeLabel", Bundle.getMessage("EnterRotation")));
                                if (newAngle.isEmpty()) {
                                    return;  // cancelled
                                }
                                double rot = 0.0;
                                try {
                                    rot = Double.parseDouble(newAngle);
                                } catch (Exception e1) {
                                    JOptionPane.showMessageDialog(layoutEditor, Bundle.getMessage("Error3")
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
            if ((getTurnout() != null) && (getTurnoutB() != null)) {
                if (blockAssigned) {
                    AbstractAction ssaa = new AbstractAction(Bundle.getMessage("SetSignals")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            layoutEditor.getLETools().setSignalsAtSlipFromMenu(
                                    LayoutSlip.this,
                                    layoutEditor.signalIconEditor,
                                    layoutEditor.signalFrame);
                        }
                    };
                    JMenu jm = new JMenu(Bundle.getMessage("SignalHeads"));
                    if (layoutEditor.getLETools().addLayoutSlipSignalHeadInfoToMenu(
                            LayoutSlip.this, jm)) {
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
                    popup.add(new AbstractAction(Bundle.getMessage("SetSignalMasts")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            layoutEditor.getLETools().setSignalMastsAtSlipFromMenu(
                                    LayoutSlip.this,
                                    boundaryBetween,
                                    layoutEditor.signalFrame);
                        }
                    });
                    popup.add(new AbstractAction(Bundle.getMessage("SetSensors")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            layoutEditor.getLETools().setSensorsAtSlipFromMenu(
                                    LayoutSlip.this, boundaryBetween,
                                    layoutEditor.sensorIconEditor,
                                    layoutEditor.sensorFrame);
                        }
                    });
                }

                if (jmri.InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled()
                        && blockAssigned) {
                    popup.add(new AbstractAction(Bundle.getMessage("ViewBlockRouting")) {
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
            popup.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
        } else if (!viewAdditionalMenu.isEmpty()) {
            setAdditionalViewPopUpMenu(popup);
            popup.show(mouseEvent.getComponent(), mouseEvent.getX(), mouseEvent.getY());
        }
        return popup;
    }   // showPopup

    @Override
    public String[] getBlockBoundaries() {
        final String[] boundaryBetween = new String[4];

        if ((!getBlockName().isEmpty()) && (getLayoutBlock() != null)) {
            if ((connectA instanceof TrackSegment) && (((TrackSegment) connectA).getLayoutBlock() != getLayoutBlock())) {
                try {
                    boundaryBetween[0] = (((TrackSegment) connectA).getLayoutBlock().getDisplayName() + " - " + getLayoutBlock().getDisplayName());
                } catch (java.lang.NullPointerException e) {
                    //Can be considered normal if tracksegement hasn't yet been allocated a block
                    log.debug("TrackSegement at connection A doesn't contain a layout block");
                }
            }
            if ((connectC instanceof TrackSegment) && (((TrackSegment) connectC).getLayoutBlock() != getLayoutBlock())) {
                try {
                    boundaryBetween[2] = (((TrackSegment) connectC).getLayoutBlock().getDisplayName() + " - " + getLayoutBlock().getDisplayName());
                } catch (java.lang.NullPointerException e) {
                    //Can be considered normal if tracksegement hasn't yet been allocated a block
                    log.debug("TrackSegement at connection C doesn't contain a layout block");
                }
            }
            if ((connectB instanceof TrackSegment) && (((TrackSegment) connectB).getLayoutBlock() != getLayoutBlock())) {
                try {
                    boundaryBetween[1] = (((TrackSegment) connectB).getLayoutBlock().getDisplayName() + " - " + getLayoutBlock().getDisplayName());
                } catch (java.lang.NullPointerException e) {
                    //Can be considered normal if tracksegement hasn't yet been allocated a block
                    log.debug("TrackSegement at connection B doesn't contain a layout block");
                }
            }
            if ((connectD instanceof TrackSegment) && (((TrackSegment) connectD).getLayoutBlock() != getLayoutBlock())) {
                try {
                    boundaryBetween[3] = (((TrackSegment) connectD).getLayoutBlock().getDisplayName() + " - " + getLayoutBlock().getDisplayName());
                } catch (java.lang.NullPointerException e) {
                    //Can be considered normal if tracksegement hasn't yet been allocated a block
                    log.debug("TrackSegement at connection D doesn't contain a layout block");
                }
            }
        }
        return boundaryBetween;
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

    HashMap<Integer, TurnoutState> turnoutStates = new LinkedHashMap<>(4);

    protected HashMap<Integer, TurnoutState> getTurnoutStates() {
        return turnoutStates;
    }

    public int getTurnoutState(@Nonnull Turnout turn, int state) {
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

    public void setTurnoutStates(int state, @Nonnull String turnStateA, @Nonnull String turnStateB) {
        if (!turnoutStates.containsKey(state)) {
            log.error("Trying to set invalid state for slip " + getDisplayName());
            return;
        }
        turnoutStates.get(state).setTurnoutAState(parseInt(turnStateA));
        turnoutStates.get(state).setTurnoutBState(parseInt(turnStateB));
    }

    //Internal call to update the state of the slip depending upon the turnout states.
    private void updateState() {
        if ((getTurnout() != null) && (getTurnoutB() != null)) {
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
    }

    /**
     * Check if either turnout is inconsistent. This is used to create an
     * alternate slip image.
     *
     * @return true if either turnout is inconsistent.
     */
    private boolean isTurnoutInconsistent() {
        Turnout tA = getTurnout();
        if (tA != null && tA.getKnownState() == INCONSISTENT) {
            return true;
        }
        Turnout tB = getTurnoutB();
        if (tB != null && tB.getKnownState() == INCONSISTENT) {
            return true;
        }
        return false;
    }

    @Override
    protected void draw1(Graphics2D g2, boolean drawMain, boolean isBlock) {
        Point2D pA = getCoordsA();
        Point2D pB = getCoordsB();
        Point2D pC = getCoordsC();
        Point2D pD = getCoordsD();

        boolean mainlineA = isMainlineA();
        boolean mainlineB = isMainlineB();
        boolean mainlineC = isMainlineC();
        boolean mainlineD = isMainlineD();

        boolean drawUnselectedLeg = layoutEditor.isTurnoutDrawUnselectedLeg()
                || isTurnoutInconsistent();

        int slipState = getSlipState();

        Color color = g2.getColor();

        // if this isn't a block line all these will be the same color
        Color colorA = color, colorB = color, colorC = color, colorD = color;

        if (isBlock) {
            LayoutBlock layoutBlockA = getLayoutBlock();
            colorA = (layoutBlockA != null) ? layoutBlockA.getBlockTrackColor() : color;
            LayoutBlock layoutBlockB = getLayoutBlockB();
            colorB = (layoutBlockB != null) ? layoutBlockB.getBlockTrackColor() : color;
            LayoutBlock layoutBlockC = getLayoutBlockC();
            colorC = (layoutBlockC != null) ? layoutBlockC.getBlockTrackColor() : color;
            LayoutBlock layoutBlockD = getLayoutBlockD();
            colorD = (layoutBlockD != null) ? layoutBlockD.getBlockTrackColor() : color;

            if (slipState == STATE_AC) {
                colorA = (layoutBlockA != null) ? layoutBlockA.getBlockColor() : color;
                colorC = (layoutBlockC != null) ? layoutBlockC.getBlockColor() : color;
            } else if (slipState == STATE_BD) {
                colorB = (layoutBlockB != null) ? layoutBlockB.getBlockColor() : color;
                colorD = (layoutBlockD != null) ? layoutBlockD.getBlockColor() : color;
            } else if (slipState == STATE_AD) {
                colorA = (layoutBlockA != null) ? layoutBlockA.getBlockColor() : color;
                colorD = (layoutBlockD != null) ? layoutBlockD.getBlockColor() : color;
            } else if (slipState == STATE_BC) {
                colorB = (layoutBlockB != null) ? layoutBlockB.getBlockColor() : color;
                colorC = (layoutBlockC != null) ? layoutBlockC.getBlockColor() : color;
            }
        }
        Point2D oneForthPointAC = MathUtil.oneFourthPoint(pA, pC);
        Point2D oneThirdPointAC = MathUtil.oneThirdPoint(pA, pC);
        Point2D midPointAC = MathUtil.midPoint(pA, pC);
        Point2D twoThirdsPointAC = MathUtil.twoThirdsPoint(pA, pC);
        Point2D threeFourthsPointAC = MathUtil.threeFourthsPoint(pA, pC);

        Point2D oneForthPointBD = MathUtil.oneFourthPoint(pB, pD);
        Point2D oneThirdPointBD = MathUtil.oneThirdPoint(pB, pD);
        Point2D midPointBD = MathUtil.midPoint(pB, pD);
        Point2D twoThirdsPointBD = MathUtil.twoThirdsPoint(pB, pD);
        Point2D threeFourthsPointBD = MathUtil.threeFourthsPoint(pB, pD);

        Point2D midPointAD = MathUtil.midPoint(oneThirdPointAC, twoThirdsPointBD);
        Point2D midPointBC = MathUtil.midPoint(oneThirdPointBD, twoThirdsPointAC);

        if (slipState == STATE_AD) {
            // draw A<===>D
            if (drawMain == mainlineA) {
                g2.setColor(colorA);
                g2.draw(new Line2D.Double(pA, oneThirdPointAC));
                g2.draw(new Line2D.Double(oneThirdPointAC, midPointAD));
            }
            if (drawMain == mainlineD) {
                g2.setColor(colorD);
                g2.draw(new Line2D.Double(midPointAD, twoThirdsPointBD));
                g2.draw(new Line2D.Double(twoThirdsPointBD, pD));
            }
        } else if (slipState == STATE_AC) {
            // draw A<===>C
            if (drawMain == mainlineA) {
                g2.setColor(colorA);
                g2.draw(new Line2D.Double(pA, oneThirdPointAC));
                g2.draw(new Line2D.Double(oneThirdPointAC, midPointAC));
            }
            if (drawMain == mainlineC) {
                g2.setColor(colorC);
                g2.draw(new Line2D.Double(midPointAC, twoThirdsPointAC));
                g2.draw(new Line2D.Double(twoThirdsPointAC, pC));
            }
        } else if (slipState == STATE_BD) {
            // draw B<===>D
            if (drawMain == mainlineB) {
                g2.setColor(colorB);
                g2.draw(new Line2D.Double(pB, oneThirdPointBD));
                g2.draw(new Line2D.Double(oneThirdPointBD, midPointBD));
            }
            if (drawMain == mainlineD) {
                g2.setColor(colorD);
                g2.draw(new Line2D.Double(midPointBD, twoThirdsPointBD));
                g2.draw(new Line2D.Double(twoThirdsPointBD, pD));
            }
        } else if (slipState == STATE_BC) {
            if (getTurnoutType() == DOUBLE_SLIP) {
                // draw B<===>C
                if (drawMain == mainlineB) {
                    g2.setColor(colorB);
                    g2.draw(new Line2D.Double(pB, oneThirdPointBD));
                    g2.draw(new Line2D.Double(oneThirdPointBD, midPointBC));
                }
                if (drawMain == mainlineC) {
                    g2.setColor(colorC);
                    g2.draw(new Line2D.Double(midPointBC, twoThirdsPointAC));
                    g2.draw(new Line2D.Double(twoThirdsPointAC, pC));
                }
            }   // DOUBLE_SLIP
        }

        if (!isBlock || drawUnselectedLeg) {
            if (slipState == STATE_AC) {
                if (drawMain == mainlineB) {
                    g2.setColor(colorB);
                    g2.draw(new Line2D.Double(pB, oneForthPointBD));
                }
                if (drawMain == mainlineD) {
                    g2.setColor(colorD);
                    g2.draw(new Line2D.Double(threeFourthsPointBD, pD));
                }
            } else if (slipState == STATE_BD) {
                if (drawMain == mainlineA) {
                    g2.setColor(colorA);
                    g2.draw(new Line2D.Double(pA, oneForthPointAC));
                }
                if (drawMain == mainlineC) {
                    g2.setColor(colorC);
                    g2.draw(new Line2D.Double(threeFourthsPointAC, pC));
                }
            } else if (slipState == STATE_AD) {
                if (drawMain == mainlineB) {
                    g2.setColor(colorB);
                    g2.draw(new Line2D.Double(pB, oneForthPointBD));
                }
                if (drawMain == mainlineC) {
                    g2.setColor(colorC);
                    g2.draw(new Line2D.Double(threeFourthsPointAC, pC));
                }
            } else if (slipState == STATE_BC) {
                if (drawMain == mainlineA) {
                    g2.setColor(colorA);
                    g2.draw(new Line2D.Double(pA, oneForthPointAC));
                }
                if (drawMain == mainlineD) {
                    g2.setColor(colorD);
                    g2.draw(new Line2D.Double(threeFourthsPointBD, pD));
                }
            } else {
                if (drawMain == mainlineA) {
                    g2.setColor(colorA);
                    g2.draw(new Line2D.Double(pA, oneForthPointAC));
                }
                if (drawMain == mainlineB) {
                    g2.setColor(colorB);
                    g2.draw(new Line2D.Double(pB, oneForthPointBD));
                }
                if (drawMain == mainlineC) {
                    g2.setColor(colorC);
                    g2.draw(new Line2D.Double(threeFourthsPointAC, pC));
                }
                if (drawMain == mainlineD) {
                    g2.setColor(colorD);
                    g2.draw(new Line2D.Double(threeFourthsPointBD, pD));
                }
            }
        }
    }   // draw1

    /**
     * {@inheritDoc}
     */
    @Override
    protected void draw2(Graphics2D g2, boolean drawMain, float railDisplacement) {
        Point2D pA = getCoordsA();
        Point2D pB = getCoordsB();
        Point2D pC = getCoordsC();
        Point2D pD = getCoordsD();
        Point2D pM = getCoordsCenter();

        Point2D vAC = MathUtil.normalize(MathUtil.subtract(pC, pA), railDisplacement);
        double dirAC_DEG = MathUtil.computeAngleDEG(pA, pC);
        Point2D vACo = MathUtil.orthogonal(vAC);
        Point2D pAL = MathUtil.subtract(pA, vACo);
        Point2D pAR = MathUtil.add(pA, vACo);
        Point2D pCL = MathUtil.subtract(pC, vACo);
        Point2D pCR = MathUtil.add(pC, vACo);

        Point2D vBD = MathUtil.normalize(MathUtil.subtract(pD, pB), railDisplacement);
        double dirBD_DEG = MathUtil.computeAngleDEG(pB, pD);
        Point2D vBDo = MathUtil.orthogonal(vBD);
        Point2D pBL = MathUtil.subtract(pB, vBDo);
        Point2D pBR = MathUtil.add(pB, vBDo);
        Point2D pDL = MathUtil.subtract(pD, vBDo);
        Point2D pDR = MathUtil.add(pD, vBDo);

        double deltaDEG = MathUtil.absDiffAngleDEG(dirAC_DEG, dirBD_DEG);
        double deltaRAD = Math.toRadians(deltaDEG);

        double hypotV = railDisplacement / Math.cos((PI - deltaRAD) / 2.0);
        double hypotK = railDisplacement / Math.cos(deltaRAD / 2.0);

        log.debug("dir AC: {}, BD: {}, diff: {}", dirAC_DEG, dirBD_DEG, deltaDEG);

        Point2D vDisK = MathUtil.normalize(MathUtil.subtract(vAC, vBD), hypotK);
        Point2D vDisV = MathUtil.normalize(MathUtil.orthogonal(vDisK), hypotV);
        Point2D pKL = MathUtil.subtract(pM, vDisK);
        Point2D pKR = MathUtil.add(pM, vDisK);
        Point2D pVL = MathUtil.add(pM, vDisV);
        Point2D pVR = MathUtil.subtract(pM, vDisV);

        // this is the vector (rail gaps) for the diamond parts
        double railGap = 2.0 / Math.sin(deltaRAD);
        Point2D vAC2 = MathUtil.normalize(vAC, railGap);
        Point2D vBD2 = MathUtil.normalize(vBD, railGap);
        // KR and VR toward A, KL and VL toward C
        Point2D pKRtA = MathUtil.subtract(pKR, vAC2);
        Point2D pVRtA = MathUtil.subtract(pVR, vAC2);
        Point2D pKLtC = MathUtil.add(pKL, vAC2);
        Point2D pVLtC = MathUtil.add(pVL, vAC2);

        // VR and KL toward B, KR and VL toward D
        Point2D pVRtB = MathUtil.subtract(pVR, vBD2);
        Point2D pKLtB = MathUtil.subtract(pKL, vBD2);
        Point2D pKRtD = MathUtil.add(pKR, vBD2);
        Point2D pVLtD = MathUtil.add(pVL, vBD2);

        // outer (closed) switch points
        Point2D pAPL = MathUtil.add(pAL, MathUtil.subtract(pVL, pAR));
        Point2D pBPR = MathUtil.add(pBR, MathUtil.subtract(pVL, pBL));
        Point2D pCPR = MathUtil.add(pCR, MathUtil.subtract(pVR, pCL));
        Point2D pDPL = MathUtil.add(pDL, MathUtil.subtract(pVR, pDR));

        // this is the vector (rail gaps) for the inner (open) switch points
        Point2D vACo2 = MathUtil.normalize(vACo, 2.0);
        Point2D vBDo2 = MathUtil.normalize(vBDo, 2.0);
        Point2D pASL = MathUtil.add(pAPL, vACo2);
        Point2D pBSR = MathUtil.subtract(pBPR, vBDo2);
        Point2D pCSR = MathUtil.subtract(pCPR, vACo2);
        Point2D pDSL = MathUtil.add(pDPL, vBDo2);

        Point2D pVLP = MathUtil.add(pVLtD, vAC2);
        Point2D pVRP = MathUtil.subtract(pVRtA, vBD2);

        Point2D pKLH = MathUtil.midPoint(pM, pKL);
        Point2D pKRH = MathUtil.midPoint(pM, pKR);

        boolean mainlineA = isMainlineA();
        boolean mainlineB = isMainlineB();
        boolean mainlineC = isMainlineC();
        boolean mainlineD = isMainlineD();

        if (drawMain == mainlineA) {
            g2.draw(new Line2D.Double(pAR, pVL));
            g2.draw(new Line2D.Double(pVLtD, pKLtB));
            GeneralPath path = new GeneralPath();
            path.moveTo(pAL.getX(), pAL.getY());
            path.lineTo(pAPL.getX(), pAPL.getY());
            path.quadTo(pKL.getX(), pKL.getY(), pDPL.getX(), pDPL.getY());
            g2.draw(path);
        }
        if (drawMain == mainlineB) {
            g2.draw(new Line2D.Double(pBL, pVL));
            g2.draw(new Line2D.Double(pVLtC, pKRtA));
            if (getTurnoutType() == DOUBLE_SLIP) {
                GeneralPath path = new GeneralPath();
                path.moveTo(pBR.getX(), pBR.getY());
                path.lineTo(pBPR.getX(), pBPR.getY());
                path.quadTo(pKR.getX(), pKR.getY(), pCPR.getX(), pCPR.getY());
                g2.draw(path);
            } else {
                g2.draw(new Line2D.Double(pBR, pKR));
            }
        }
        if (drawMain == mainlineC) {
            g2.draw(new Line2D.Double(pCL, pVR));
            g2.draw(new Line2D.Double(pVRtB, pKRtD));
            if (getTurnoutType() == DOUBLE_SLIP) {
                GeneralPath path = new GeneralPath();
                path.moveTo(pCR.getX(), pCR.getY());
                path.lineTo(pCPR.getX(), pCPR.getY());
                path.quadTo(pKR.getX(), pKR.getY(), pBPR.getX(), pBPR.getY());
                g2.draw(path);
            } else {
                g2.draw(new Line2D.Double(pCR, pKR));
            }
        }
        if (drawMain == mainlineD) {
            g2.draw(new Line2D.Double(pDR, pVR));
            g2.draw(new Line2D.Double(pVRtA, pKLtC));
            GeneralPath path = new GeneralPath();
            path.moveTo(pDL.getX(), pDL.getY());
            path.lineTo(pDPL.getX(), pDPL.getY());
            path.quadTo(pKL.getX(), pKL.getY(), pAPL.getX(), pAPL.getY());
            g2.draw(path);
        }

        int slipState = getSlipState();
        if (slipState == STATE_AD) {
            if (drawMain == mainlineA) {
                g2.draw(new Line2D.Double(pASL, pKL));
                g2.draw(new Line2D.Double(pVLP, pKLH));
            }
            if (drawMain == mainlineB) {
                g2.draw(new Line2D.Double(pBPR, pKR));
                g2.draw(new Line2D.Double(pVLtC, pKRH));
            }
            if (drawMain == mainlineC) {
                g2.draw(new Line2D.Double(pCPR, pKR));
                g2.draw(new Line2D.Double(pVRtB, pKRH));
            }
            if (drawMain == mainlineD) {
                g2.draw(new Line2D.Double(pDSL, pKL));
                g2.draw(new Line2D.Double(pVRP, pKLH));
            }
        } else if (slipState == STATE_AC) {
            if (drawMain == mainlineA) {
                g2.draw(new Line2D.Double(pAPL, pKL));
                g2.draw(new Line2D.Double(pVLtD, pKLH));
            }
            if (drawMain == mainlineB) {
                g2.draw(new Line2D.Double(pBSR, pKR));
                g2.draw(new Line2D.Double(pVLP, pKRH));
            }
            if (drawMain == mainlineC) {
                g2.draw(new Line2D.Double(pCPR, pKR));
                g2.draw(new Line2D.Double(pVRtB, pKRH));
            }
            if (drawMain == mainlineD) {
                g2.draw(new Line2D.Double(pDSL, pKL));
                g2.draw(new Line2D.Double(pVRP, pKLH));
            }
        } else if (slipState == STATE_BD) {
            if (drawMain == mainlineA) {
                g2.draw(new Line2D.Double(pASL, pKL));
                g2.draw(new Line2D.Double(pVLP, pKLH));
            }
            if (drawMain == mainlineB) {
                g2.draw(new Line2D.Double(pBPR, pKR));
                g2.draw(new Line2D.Double(pVLtC, pKRH));
            }
            if (drawMain == mainlineC) {
                g2.draw(new Line2D.Double(pCSR, pKR));
                g2.draw(new Line2D.Double(pVRP, pKRH));
            }
            if (drawMain == mainlineD) {
                g2.draw(new Line2D.Double(pDPL, pKL));
                g2.draw(new Line2D.Double(pVRtA, pKLH));
            }
        } else if ((getTurnoutType() == DOUBLE_SLIP)
                && (slipState == STATE_BC)) {
            if (drawMain == mainlineA) {
                g2.draw(new Line2D.Double(pAPL, pKL));
                g2.draw(new Line2D.Double(pVLtD, pKLH));
            }
            if (drawMain == mainlineB) {
                g2.draw(new Line2D.Double(pBSR, pKR));
                g2.draw(new Line2D.Double(pVLP, pKRH));
            }
            if (drawMain == mainlineC) {
                g2.draw(new Line2D.Double(pCSR, pKR));
                g2.draw(new Line2D.Double(pVRP, pKRH));
            }
            if (drawMain == mainlineD) {
                g2.draw(new Line2D.Double(pDPL, pKL));
                g2.draw(new Line2D.Double(pVRtA, pKLH));
            }
        }   // DOUBLE_SLIP
    }   // draw2

    /**
     * {@inheritDoc}
     */
    @Override
    protected void highlightUnconnected(Graphics2D g2, int specificType) {
        if (((specificType == NONE) || (specificType == SLIP_A))
                && (getConnectA() == null)) {
            g2.fill(layoutEditor.trackControlCircleAt(getCoordsA()));
        }

        if (((specificType == NONE) || (specificType == SLIP_B))
                && (getConnectB() == null)) {
            g2.fill(layoutEditor.trackControlCircleAt(getCoordsB()));
        }

        if (((specificType == NONE) || (specificType == SLIP_C))
                && (getConnectC() == null)) {
            g2.fill(layoutEditor.trackControlCircleAt(getCoordsC()));
        }

        if (((specificType == NONE) || (specificType == SLIP_D))
                && (getConnectD() == null)) {
            g2.fill(layoutEditor.trackControlCircleAt(getCoordsD()));
        }
    }

    @Override
    protected void drawTurnoutControls(Graphics2D g2) {
        if (!disabled && !(disableWhenOccupied && isOccupied())) {
            // TODO: query user base if this is "acceptable" (can obstruct state)
            if (false) {
                int stateA = UNKNOWN;
                Turnout toA = getTurnout();
                if (toA != null) {
                    stateA = toA.getKnownState();
                }

                Color foregroundColor = g2.getColor();
                Color backgroundColor = g2.getBackground();

                if (stateA == Turnout.THROWN) {
                    g2.setColor(backgroundColor);
                } else if (stateA != Turnout.CLOSED) {
                    g2.setColor(Color.GRAY);
                }
                Point2D rightCircleCenter = getCoordsRight();
                if (layoutEditor.isTurnoutFillControlCircles()) {
                    g2.fill(layoutEditor.trackControlCircleAt(rightCircleCenter));
                } else {
                    g2.draw(layoutEditor.trackControlCircleAt(rightCircleCenter));
                }
                if (stateA != Turnout.CLOSED) {
                    g2.setColor(foregroundColor);
                }

                int stateB = UNKNOWN;
                Turnout toB = getTurnoutB();
                if (toB != null) {
                    stateB = toB.getKnownState();
                }

                if (stateB == Turnout.THROWN) {
                    g2.setColor(backgroundColor);
                } else if (stateB != Turnout.CLOSED) {
                    g2.setColor(Color.GRAY);
                }
                // drawHidden left/right turnout control circles
                Point2D leftCircleCenter = getCoordsLeft();
                if (layoutEditor.isTurnoutFillControlCircles()) {
                    g2.fill(layoutEditor.trackControlCircleAt(leftCircleCenter));
                } else {
                    g2.draw(layoutEditor.trackControlCircleAt(leftCircleCenter));
                }
                if (stateB != Turnout.CLOSED) {
                    g2.setColor(foregroundColor);
                }
            } else {
                Point2D rightCircleCenter = getCoordsRight();
                g2.draw(layoutEditor.trackControlCircleAt(rightCircleCenter));
                Point2D leftCircleCenter = getCoordsLeft();
                g2.draw(layoutEditor.trackControlCircleAt(leftCircleCenter));
            }
        }
    } // drawTurnoutControls

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
            int result = Turnout.THROWN;
            if (turnoutABox != null) {
                if (turnoutABox.getSelectedIndex() == 0) {
                    result = Turnout.CLOSED;
                }
            }
            return result;
        }

        int getTestTurnoutBState() {
            int result = Turnout.THROWN;
            if (turnoutBBox != null) {
                if (turnoutBBox.getSelectedIndex() == 0) {
                    result = Turnout.CLOSED;
                }
            }
            return result;
        }

        void updateStatesFromCombo() {
            if ((turnoutABox != null) && (turnoutBBox != null)) {
                turnoutA = getTestTurnoutAState();
                turnoutB = getTestTurnoutBState();
            }
        }

        boolean equals(TurnoutState ts) {
            return ((getTurnoutAState() != ts.getTurnoutAState())
                    || (getTurnoutBState() != ts.getTurnoutBState()));
        }
    }   // class TurnoutState

    /*
        this is used by ConnectivityUtil to determine the turnout state necessary to get from prevLayoutBlock ==> currLayoutBlock ==> nextLayoutBlock
     */
    @Override
    protected int getConnectivityStateForLayoutBlocks(
            @CheckForNull LayoutBlock thisLayoutBlock,
            @CheckForNull LayoutBlock prevLayoutBlock,
            @CheckForNull LayoutBlock nextLayoutBlock,
            boolean suppress) {
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
     * {@inheritDoc}
     */
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
            if (trkA.getLayoutBlock() == getLayoutBlock()) {
                if (signalAMastNamed != null) {
                    removeSML(getSignalAMast());
                }
                signalAMastNamed = null;
                sensorANamed = null;
            }
        }
        if (connectC instanceof TrackSegment) {
            trkC = (TrackSegment) connectC;
            if (trkC.getLayoutBlock() == getLayoutBlock()) {
                if (signalCMastNamed != null) {
                    removeSML(getSignalCMast());
                }
                signalCMastNamed = null;
                sensorCNamed = null;
            }
        }
        if (connectB instanceof TrackSegment) {
            trkB = (TrackSegment) connectB;
            if (trkB.getLayoutBlock() == getLayoutBlock()) {
                if (signalBMastNamed != null) {
                    removeSML(getSignalBMast());
                }
                signalBMastNamed = null;
                sensorBNamed = null;
            }
        }

        if (connectD instanceof TrackSegment) {
            trkD = (TrackSegment) connectC;
            if (trkD.getLayoutBlock() == getLayoutBlock()) {
                if (signalDMastNamed != null) {
                    removeSML(getSignalDMast());
                }
                signalDMastNamed = null;
                sensorDNamed = null;
            }
        }
    }   // reCheckBlockBoundary()

    /*
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    protected List<LayoutConnectivity> getLayoutConnectivity() {
        List<LayoutConnectivity> results = new ArrayList<>();

        LayoutConnectivity lc = null;
        LayoutBlock lbA = getLayoutBlock(), lbB = getLayoutBlockB(), lbC = getLayoutBlockC(), lbD = getLayoutBlockD();
        if (lbA != null) {
            if (lbA != lbC) {
                // have a AC block boundary, create a LayoutConnectivity
                log.debug("Block boundary  ('{}'<->'{}') found at {}", lbA, lbC, this);
                lc = new LayoutConnectivity(lbA, lbC);
                lc.setXoverBoundary(this, LayoutConnectivity.XOVER_BOUNDARY_AC);
                lc.setDirection(Path.computeDirection(getCoordsA(), getCoordsC()));
                results.add(lc);
            }
            if ((type == DOUBLE_SLIP) && (lbB != lbD)) {
                // have a BD block boundary, create a LayoutConnectivity
                log.debug("Block boundary  ('{}'<->'{}') found at {}", lbB, lbD, this);
                lc = new LayoutConnectivity(lbB, lbD);
                lc.setXoverBoundary(this, LayoutConnectivity.XOVER_BOUNDARY_BD);
                lc.setDirection(Path.computeDirection(getCoordsB(), getCoordsD()));
                results.add(lc);
            }
            if (lbA != lbD) {
                // have a AD block boundary, create a LayoutConnectivity
                log.debug("Block boundary  ('{}'<->'{}') found at {}", lbA, lbD, this);
                lc = new LayoutConnectivity(lbA, lbD);
                lc.setXoverBoundary(this, LayoutConnectivity.XOVER_BOUNDARY_AD);
                lc.setDirection(Path.computeDirection(getCoordsA(), getCoordsD()));
                results.add(lc);
            }
            if (lbB != lbC) {
                // have a BC block boundary, create a LayoutConnectivity
                log.debug("Block boundary  ('{}'<->'{}') found at {}", lbB, lbC, this);
                lc = new LayoutConnectivity(lbB, lbC);
                lc.setXoverBoundary(this, LayoutConnectivity.XOVER_BOUNDARY_BC);
                lc.setDirection(Path.computeDirection(getCoordsB(), getCoordsC()));
                results.add(lc);
            }
        }
        return results;
    }   // getLayoutConnectivity()

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Integer> checkForFreeConnections() {
        List<Integer> result = new ArrayList<>();

        //check the A connection point
        if (getConnectA() == null) {
            result.add(Integer.valueOf(SLIP_A));
        }

        //check the B connection point
        if (getConnectB() == null) {
            result.add(Integer.valueOf(SLIP_B));
        }

        //check the C connection point
        if (getConnectC() == null) {
            result.add(Integer.valueOf(SLIP_C));
        }

        //check the D connection point
        if (getConnectD() == null) {
            result.add(Integer.valueOf(SLIP_D));
        }
        return result;
    }

    //NOTE: LayoutSlip uses the checkForNonContiguousBlocks
    //      and collectContiguousTracksNamesInBlockNamed methods
    //      inherited from LayoutTurnout
    //
    private final static Logger log = LoggerFactory.getLogger(LayoutSlip.class);
}
