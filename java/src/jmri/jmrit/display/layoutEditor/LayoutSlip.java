package jmri.jmrit.display.layoutEditor;

import static java.lang.Integer.parseInt;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
        }
        log.error("Invalid Connection Type " + connectionType); //I18IN
        throw new jmri.JmriException("Invalid Connection Type " + connectionType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConnection(int connectionType, @Nullable LayoutTrack o, int type) throws jmri.JmriException {
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
            default: {
                log.error("Unknown slip state: {}", getSlipState());
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
    public Rectangle2D getBounds() {
        return super.getBounds();
    }

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
            Rectangle2D r = layoutEditor.trackControlPointRectAt(hitPoint);

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
    LayoutEditorTools tools = null;

    /**
     * {@inheritDoc}
     */
    @Override
    @Nonnull
    protected JPopupMenu showPopup(@Nullable MouseEvent mouseEvent) {
        if (popup != null) {
            popup.removeAll();
        } else {
            popup = new JPopupMenu();
        }
        tools = layoutEditor.getLETools();
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
            if ((blockName == null) || (blockName.isEmpty())) {
                jmi = popup.add(Bundle.getMessage("NoBlock"));
            } else {
                jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("BeanNameBlock")) + getLayoutBlock().getDisplayName());
                blockAssigned = true;
            }
            jmi.setEnabled(false);

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
                    if (layoutEditor.removeLayoutSlip(LayoutSlip.this)) {
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
            if (blockAssigned) {
                AbstractAction ssaa = new AbstractAction(Bundle.getMessage("SetSignals")) {
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
                popup.add(new AbstractAction(Bundle.getMessage("SetSignalMasts")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        tools.setSignalMastsAtSlipFromMenu(LayoutSlip.this, boundaryBetween, layoutEditor.signalFrame);
                    }
                });
                popup.add(new AbstractAction(Bundle.getMessage("SetSensors")) {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        tools.setSensorsAtSlipFromMenu(LayoutSlip.this, boundaryBetween, layoutEditor.sensorIconEditor, layoutEditor.sensorFrame);
                    }
                });
            }

            if (jmri.InstanceManager.getDefault(LayoutBlockManager.class).isAdvancedRoutingEnabled()) {
                if (blockAssigned) {
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

        if ((blockName != null) && (!blockName.isEmpty()) && (getLayoutBlock() != null)) {
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
     * draw this slip
     *
     * @param g2 the graphics port to draw to
     */
    protected void draw(Graphics2D g2) {
        Color mainColourA = setColorForTrackBlock(g2, getLayoutBlock());
        Color subColourA = setColorForTrackBlock(g2, getLayoutBlock(), true);

        Color mainColourB = setColorForTrackBlock(g2, getLayoutBlockB());
        Color subColourB = setColorForTrackBlock(g2, getLayoutBlockB(), true);

        Color mainColourC = setColorForTrackBlock(g2, getLayoutBlockC());
        Color subColourC = setColorForTrackBlock(g2, getLayoutBlockC(), true);

        Color mainColourD = setColorForTrackBlock(g2, getLayoutBlockD());
        Color subColourD = setColorForTrackBlock(g2, getLayoutBlockD(), true);

        //LayoutBlock b = getLayoutBlock();
        //Color mainColourA = defaultTrackColor;
        //Color subColourA = defaultTrackColor;
        //if (b != null) {
        //    mainColourA = b.getBlockColor();
        //    subColourA = b.getBlockTrackColor();
        //}

        //b = getLayoutBlockB();
        //Color mainColourB = defaultTrackColor;
        //Color subColourB = defaultTrackColor;
        //if (b != null) {
        //    mainColourB = b.getBlockColor();
        //    subColourB = b.getBlockTrackColor();
        //}

        //b = getLayoutBlockC();
        //Color mainColourC = defaultTrackColor;
        //Color subColourC = defaultTrackColor;
        //if (b != null) {
        //    mainColourC = b.getBlockColor();
        //    subColourC = b.getBlockTrackColor();
        //}

        //b = getLayoutBlockD();
        //Color mainColourD = defaultTrackColor;
        //Color subColourD = defaultTrackColor;
        //if (b != null) {
        //    mainColourD = b.getBlockColor();
        //    subColourD = b.getBlockTrackColor();
        //}

        layoutEditor.setTrackStrokeWidth(g2, isMainline());

        boolean isMainA = (connectA != null) && (((TrackSegment) connectA).isMainline());
        boolean isMainB = (connectB != null) && (((TrackSegment) connectB).isMainline());
        boolean isMainC = (connectC != null) && (((TrackSegment) connectC).isMainline());
        boolean isMainD = (connectD != null) && (((TrackSegment) connectD).isMainline());

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
    }   // draw

    /**
     * {@inheritDoc}
     */
    @Override
    protected void drawUnconnected(Graphics2D g2) {
        if (getConnectA() == null) {
            g2.fill(layoutEditor.trackControlCircleAt(getCoordsA()));
        }

        if (getConnectB() == null) {
            g2.fill(layoutEditor.trackControlCircleAt(getCoordsB()));
        }

        if (getConnectC() == null) {
            g2.fill(layoutEditor.trackControlCircleAt(getCoordsC()));
        }
        if (getConnectD() == null) {
            g2.fill(layoutEditor.trackControlCircleAt(getCoordsD()));
        }
    }

    protected void drawTurnoutControls(Graphics2D g2) {
        // drawHidden left/right turnout control circles
        Point2D leftCircleCenter = getCoordsLeft();
        g2.draw(layoutEditor.trackControlCircleAt(leftCircleCenter));

        Point2D rightCircleCenter = getCoordsRight();
        g2.draw(layoutEditor.trackControlCircleAt(rightCircleCenter));
    }   // drawTurnoutControls

    protected void drawEditControls(Graphics2D g2) {
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
    }   // drawEditControls

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
    protected int getConnectivityStateForLayoutBlocks(
            @Nullable LayoutBlock thisLayoutBlock,
            @Nullable LayoutBlock prevLayoutBlock,
            @Nullable LayoutBlock nextLayoutBlock,
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
    private final static Logger log = LoggerFactory.getLogger(LayoutSlip.class);
}
