package jmri.jmrit.display.layoutEditor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.*;
import java.util.List;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.swing.*;

import jmri.InstanceManager;
import jmri.Turnout;
import jmri.jmrit.display.layoutEditor.LayoutTurnout.TurnoutType;
import jmri.jmrit.display.layoutEditor.blockRoutingTable.LayoutBlockRouteTableAction;
import jmri.util.MathUtil;

/**
 * MVC View component for the LayoutSlip class.
 *
 * @author Bob Jacobsen  Copyright (c) 2020
 *
 */
public class LayoutSlipView extends LayoutTurnoutView {

    /**
     * Constructor method.
     * @param slip the layout sip to create view for.
     * @param c 2D point.
     * @param rot rotation.
     * @param layoutEditor the layout editor.
     */
    public LayoutSlipView(@Nonnull LayoutSlip slip,
            Point2D c, double rot,
            @Nonnull LayoutEditor layoutEditor) {
        super(slip, c, rot, layoutEditor);
        this.slip = slip;

        dispA = new Point2D.Double(-20.0, 0.0);
        pointA = MathUtil.add(getCoordsCenter(), dispA);
        pointC = MathUtil.subtract(getCoordsCenter(), dispA);
        dispB = new Point2D.Double(-14.0, 14.0);
        pointB = MathUtil.add(getCoordsCenter(), dispB);
        pointD = MathUtil.subtract(getCoordsCenter(), dispB);

        rotateCoords(rot);

        editor = new jmri.jmrit.display.layoutEditor.LayoutEditorDialogs.LayoutSlipEditor(layoutEditor);
    }

    final private LayoutSlip slip;

    public int currentState = UNKNOWN;

    public LayoutSlip getSlip() {return slip; }
    // this should only be used for debugging...
    @Override
    public String toString() {
        return String.format("LayoutSlip %s (%s)", getId(), getSlipStateString(getSlipState()));
    }

    public TurnoutType getSlipType() {
        return slip.getSlipType();
    }

    public int getSlipState() {
        return slip.getSlipState();
    }

    public String getTurnoutBName() {
       return slip.getTurnoutBName();
    }

    public Turnout getTurnoutB() {
       return slip.getTurnoutB();
    }

    public void setTurnoutB(@CheckForNull String tName) {
        slip.setTurnoutB(tName);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LayoutTrack getConnection(HitPointType connectionType) throws jmri.JmriException {
        return slip.getConnection(connectionType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setConnection(HitPointType connectionType, @CheckForNull LayoutTrack o, HitPointType type) throws jmri.JmriException {
        slip.setConnection(connectionType, o, type);
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
       return slip.getSlipStateString(slipState);
    }

    /**
     * Toggle slip states if clicked on, physical turnout exists, and not
     * disabled
     * @param selectedPointType See {@link LayoutSlip#toggleState} for definition
     */
    public void toggleState(HitPointType selectedPointType) {
       slip.toggleState(selectedPointType);
    }

    /**
     * is this turnout occupied?
     *
     * @return true if occupied
     */
    private boolean isOccupied() {
       return slip.isOccupied();
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

    Point2D getCoordsLeft() {
        Point2D leftCenter = MathUtil.midPoint(getCoordsA(), getCoordsB());
        double circleRadius = LayoutEditor.SIZE * layoutEditor.getTurnoutCircleSize();
        double leftFract = circleRadius / getCoordsCenter().distance(leftCenter);
        return MathUtil.lerp(getCoordsCenter(), leftCenter, leftFract);
    }

    Point2D getCoordsRight() {
        Point2D rightCenter = MathUtil.midPoint(getCoordsC(), getCoordsD());
        double circleRadius = LayoutEditor.SIZE * layoutEditor.getTurnoutCircleSize();
        double rightFract = circleRadius / getCoordsCenter().distance(rightCenter);
        return MathUtil.lerp(getCoordsCenter(), rightCenter, rightFract);
    }

    /**
     * return the coordinates for the specified connection type
     *
     * @param connectionType the connection type
     * @return the Point2D coordinates
     */
    @Override
    public Point2D getCoordsForConnectionType(HitPointType connectionType) {
        Point2D result = getCoordsCenter();
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
            case SLIP_LEFT:
                result = getCoordsLeft();
                break;
            case SLIP_RIGHT:
                result = getCoordsRight();
                break;
            default:
                log.error("{}.getCoordsForConnectionType({}); Invalid Connection Type", getName(), connectionType); // I18IN
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
    public void updateBlockInfo() {
        slip.updateBlockInfo();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected HitPointType findHitPointType(@Nonnull Point2D hitPoint, boolean useRectangles, boolean requireUnconnected) {
        HitPointType result = HitPointType.NONE;  // assume point not on connection

        if (!requireUnconnected) {
            // calculate radius of turnout control circle
            double circleRadius = LayoutEditor.SIZE * layoutEditor.getTurnoutCircleSize();

            // get left and right centers
            Point2D leftCenter = getCoordsLeft();
            Point2D rightCenter = getCoordsRight();

            if (useRectangles) {
                // calculate turnout's left control rectangle
                Rectangle2D leftRectangle = layoutEditor.layoutEditorControlCircleRectAt(leftCenter);
                if (leftRectangle.contains(hitPoint)) {
                    // point is in this turnout's left control rectangle
                    result = HitPointType.SLIP_LEFT;
                }
                Rectangle2D rightRectangle = layoutEditor.layoutEditorControlCircleRectAt(rightCenter);
                if (rightRectangle.contains(hitPoint)) {
                    // point is in this turnout's right control rectangle
                    result = HitPointType.SLIP_RIGHT;
                }
            } else {
                // check east/west turnout control circles
                double leftDistance = hitPoint.distance(leftCenter);
                double rightDistance = hitPoint.distance(rightCenter);

                if ((leftDistance <= circleRadius) || (rightDistance <= circleRadius)) {
                    // mouse was pressed on this slip
                    result = (leftDistance < rightDistance) ? HitPointType.SLIP_LEFT : HitPointType.SLIP_RIGHT;
                }
            }
        }

        // have we found anything yet?
        if (result == HitPointType.NONE) {
            // rather than create rectangles for all the points below and
            // see if the passed in point is in one of those rectangles
            // we can create a rectangle for the passed in point and then
            // test if any of the points below are in that rectangle instead.
            Rectangle2D r = layoutEditor.layoutEditorControlRectAt(hitPoint);

            if (!requireUnconnected || (getConnectA() == null)) {
                // check the A connection point
                if (r.contains(getCoordsA())) {
                    result = HitPointType.SLIP_A;
                }
            }

            if (!requireUnconnected || (getConnectB() == null)) {
                // check the B connection point
                if (r.contains(getCoordsB())) {
                    result = HitPointType.SLIP_B;
                }
            }

            if (!requireUnconnected || (getConnectC() == null)) {
                // check the C connection point
                if (r.contains(getCoordsC())) {
                    result = HitPointType.SLIP_C;
                }
            }

            if (!requireUnconnected || (getConnectD() == null)) {
                // check the D connection point
                if (r.contains(getCoordsD())) {
                    result = HitPointType.SLIP_D;
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
        super.setCoordsCenter(p);
        pointA = MathUtil.add(getCoordsCenter(), dispA);
        pointB = MathUtil.add(getCoordsCenter(), dispB);
        pointC = MathUtil.subtract(getCoordsCenter(), dispA);
        pointD = MathUtil.subtract(getCoordsCenter(), dispB);
    }

    @Override
    public void setCoordsA(@Nonnull Point2D p) {
        pointA = p;
        dispA = MathUtil.subtract(pointA, getCoordsCenter());
        pointC = MathUtil.subtract(getCoordsCenter(), dispA);
    }

    @Override
    public void setCoordsB(@Nonnull Point2D p) {
        pointB = p;
        dispB = MathUtil.subtract(pointB, getCoordsCenter());
        pointD = MathUtil.subtract(getCoordsCenter(), dispB);
    }

    @Override
    public void setCoordsC(@Nonnull Point2D p) {
        pointC = p;
        dispA = MathUtil.subtract(getCoordsCenter(), pointC);
        pointA = MathUtil.add(getCoordsCenter(), dispA);
    }

    @Override
    public void setCoordsD(@Nonnull Point2D p) {
        pointD = p;
        dispB = MathUtil.subtract(getCoordsCenter(), pointD);
        pointB = MathUtil.add(getCoordsCenter(), dispB);
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
            switch (getSlipType()) {
                case SINGLE_SLIP: {
                    jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("LayoutSingleSlip")) + getId() + slipStateString);
                    break;
                }
                case DOUBLE_SLIP: {
                    jmi = popup.add(Bundle.getMessage("MakeLabel", Bundle.getMessage("LayoutDoubleSlip")) + getId() + slipStateString);
                    break;
                }
                default: {
                    log.error("{}.showPopup(<mouseEvent>); Invalid slip type: {}", getName(), getSlipType()); // I18IN
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
            if ((getConnectA() != null) || (getConnectB() != null)
                    || (getConnectC() != null) || (getConnectD() != null)) {
                JMenu connectionsMenu = new JMenu(Bundle.getMessage("Connections")); // there is no pane opening (which is what ... implies)
                if (getConnectA() != null) {
                    connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "A") + getConnectA().getName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            LayoutEditorFindItems lf = layoutEditor.getFinder();
                            LayoutTrack lt = lf.findObjectByName(getConnectA().getName());
                            // this shouldn't ever be null... however...
                            if (lt != null) {
                                LayoutTrackView ltv = layoutEditor.getLayoutTrackView(lt);
                                layoutEditor.setSelectionRect(ltv.getBounds());
                                ltv.showPopup();
                            }
                        }
                    });
                }
                if (getConnectB() != null) {
                    connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "B") + getConnectB().getName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            LayoutEditorFindItems lf = layoutEditor.getFinder();
                            LayoutTrack lt = lf.findObjectByName(getConnectB().getName());
                            // this shouldn't ever be null... however...
                            if (lt != null) {
                                LayoutTrackView ltv = layoutEditor.getLayoutTrackView(lt);
                                layoutEditor.setSelectionRect(ltv.getBounds());
                                ltv.showPopup();
                            }
                        }
                    });
                }
                if (getConnectC() != null) {
                    connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "C") + getConnectC().getName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            LayoutEditorFindItems lf = layoutEditor.getFinder();
                            LayoutTrack lt = lf.findObjectByName(getConnectC().getName());
                            // this shouldn't ever be null... however...
                            if (lt != null) {
                                LayoutTrackView ltv = layoutEditor.getLayoutTrackView(lt);
                                layoutEditor.setSelectionRect(ltv.getBounds());
                                ltv.showPopup();
                            }
                        }
                    });
                }
                if (getConnectD() != null) {
                    connectionsMenu.add(new AbstractAction(Bundle.getMessage("MakeLabel", "D") + getConnectD().getName()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            LayoutEditorFindItems lf = layoutEditor.getFinder();
                            LayoutTrack lt = lf.findObjectByName(getConnectD().getName());
                            // this shouldn't ever be null... however...
                            if (lt != null) {
                                LayoutTrackView ltv = layoutEditor.getLayoutTrackView(lt);
                                layoutEditor.setSelectionRect(ltv.getBounds());
                                ltv.showPopup();
                            }
                        }
                    });
                }
                popup.add(connectionsMenu);
            }

            popup.add(new JSeparator(JSeparator.HORIZONTAL));

            JCheckBoxMenuItem hiddenCheckBoxMenuItem = new JCheckBoxMenuItem(Bundle.getMessage("Hidden"));
            hiddenCheckBoxMenuItem.setSelected(isHidden());
            popup.add(hiddenCheckBoxMenuItem);
            hiddenCheckBoxMenuItem.addActionListener((java.awt.event.ActionEvent e1) -> {
                JCheckBoxMenuItem o = (JCheckBoxMenuItem) e1.getSource();
                setHidden(o.isSelected());
            });

            JCheckBoxMenuItem cbmi = new JCheckBoxMenuItem(Bundle.getMessage("Disabled"));
            cbmi.setSelected(isDisabled());
            popup.add(cbmi);
            cbmi.addActionListener((java.awt.event.ActionEvent e2) -> {
                JCheckBoxMenuItem o = (JCheckBoxMenuItem) e2.getSource();
                setDisabled(o.isSelected());
            });

            cbmi = new JCheckBoxMenuItem(Bundle.getMessage("DisabledWhenOccupied"));
            cbmi.setSelected(isDisabledWhenOccupied());
            popup.add(cbmi);
            cbmi.addActionListener((java.awt.event.ActionEvent e3) -> {
                JCheckBoxMenuItem o = (JCheckBoxMenuItem) e3.getSource();
                setDisableWhenOccupied(o.isSelected());
            });

            popup.add(new AbstractAction(Bundle.getMessage("ButtonEdit")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    editor.editLayoutTrack(LayoutSlipView.this);
                }
            });
            popup.add(new AbstractAction(Bundle.getMessage("ButtonDelete")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (canRemove() && layoutEditor.removeLayoutSlip(slip)) {
                        // Returned true if user did not cancel
                        remove();
                        dispose();
                    }
                }
            });
            if ((getConnectA() == null) && (getConnectB() == null)
                    && (getConnectC() == null) && (getConnectD() == null)) {
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
                                    slip,
                                    getLayoutEditorToolBarPanel().signalIconEditor,
                                    getLayoutEditorToolBarPanel().signalFrame);
                        }
                    };
                    JMenu jm = new JMenu(Bundle.getMessage("SignalHeads"));
                    if (layoutEditor.getLETools().addLayoutSlipSignalHeadInfoToMenu(
                            slip, jm)) {
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
                                    slip,
                                    boundaryBetween,
                                    getLayoutEditorToolBarPanel().signalFrame);
                        }
                    });
                    popup.add(new AbstractAction(Bundle.getMessage("SetSensors")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            layoutEditor.getLETools().setSensorsAtSlipFromMenu(
                                    slip, boundaryBetween,
                                    getLayoutEditorToolBarPanel().sensorIconEditor,
                                    getLayoutEditorToolBarPanel().sensorFrame);
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
        return slip.getBlockBoundaries();
    }

    /**
     * Clean up when this object is no longer needed. Should not be called while
     * the object is still displayed; see remove()
     */
    @Override
    public void dispose() {
        if (popup != null) {
            popup.removeAll();
        }
        popup = null;
    }

    /**
     * Removes this object from display and persistance
     */
    @Override
    public void remove() {
        slip.remove();
    }


    public int getTurnoutState(@Nonnull Turnout turn, int state) {
       return slip.getTurnoutState(turn, state);
    }

    public int getTurnoutState(int state) {
       return slip.getTurnoutState(state);
    }

    public int getTurnoutBState(int state) {
       return slip.getTurnoutBState(state);
    }

    public void setTurnoutStates(int state, @Nonnull String turnStateA, @Nonnull String turnStateB) {
        slip.setTurnoutStates(state, turnStateA, turnStateB);
    }

    /**
     * Check if either turnout is inconsistent. This is used to create an
     * alternate slip image.
     *
     * @return true if either turnout is inconsistent.
     */
    private boolean isTurnoutInconsistent() {
       return slip.isTurnoutInconsistent();
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
            if (getTurnoutType() == TurnoutType.DOUBLE_SLIP) {
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

        double hypotV = railDisplacement / Math.cos((Math.PI - deltaRAD) / 2.0);
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
            if (getTurnoutType() == TurnoutType.DOUBLE_SLIP) {
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
            if (getTurnoutType() == TurnoutType.DOUBLE_SLIP) {
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
        } else if ((getTurnoutType() == TurnoutType.DOUBLE_SLIP)
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
    protected void highlightUnconnected(Graphics2D g2, HitPointType specificType) {
        if (((specificType == HitPointType.NONE) || (specificType == HitPointType.SLIP_A))
                && (getConnectA() == null)) {
            g2.fill(trackControlCircleAt(getCoordsA()));
        }

        if (((specificType == HitPointType.NONE) || (specificType == HitPointType.SLIP_B))
                && (getConnectB() == null)) {
            g2.fill(trackControlCircleAt(getCoordsB()));
        }

        if (((specificType == HitPointType.NONE) || (specificType == HitPointType.SLIP_C))
                && (getConnectC() == null)) {
            g2.fill(trackControlCircleAt(getCoordsC()));
        }

        if (((specificType == HitPointType.NONE) || (specificType == HitPointType.SLIP_D))
                && (getConnectD() == null)) {
            g2.fill(trackControlCircleAt(getCoordsD()));
        }
    }

    @Override
    protected void drawTurnoutControls(Graphics2D g2) {
        if (!isDisabled() && !(isDisabledWhenOccupied() && isOccupied())) {
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
                    g2.fill(trackControlCircleAt(rightCircleCenter));
                } else {
                    g2.draw(trackControlCircleAt(rightCircleCenter));
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
                    g2.fill(trackControlCircleAt(leftCircleCenter));
                } else {
                    g2.draw(trackControlCircleAt(leftCircleCenter));
                }
                if (stateB != Turnout.CLOSED) {
                    g2.setColor(foregroundColor);
                }
            } else {
                Point2D rightCircleCenter = getCoordsRight();
                g2.draw(trackControlCircleAt(rightCircleCenter));
                Point2D leftCircleCenter = getCoordsLeft();
                g2.draw(trackControlCircleAt(leftCircleCenter));
            }
        }
    } // drawTurnoutControls

    public static class TurnoutState {

        private int turnoutA = Turnout.CLOSED;
        private int turnoutB = Turnout.CLOSED;
        private JComboBox<String> turnoutABox;
        private JComboBox<String> turnoutBBox;

        TurnoutState(int turnoutA, int turnoutB) {
            this.turnoutA = turnoutA;
            this.turnoutB = turnoutB;
        }

        public int getTurnoutAState() {
            return turnoutA;
        }

        public int getTurnoutBState() {
            return turnoutB;
        }

        public void setTurnoutAState(int state) {
            turnoutA = state;
        }

        public void setTurnoutBState(int state) {
            turnoutB = state;
        }

        public JComboBox<String> getComboA() {
            if (turnoutABox == null) {
                String[] state = new String[]{InstanceManager.turnoutManagerInstance().getClosedText(),
                    InstanceManager.turnoutManagerInstance().getThrownText()};
                turnoutABox = new JComboBox<>(state);
                if (turnoutA == Turnout.THROWN) {
                    turnoutABox.setSelectedIndex(1);
                }
            }
            return turnoutABox;
        }

        public JComboBox<String> getComboB() {
            if (turnoutBBox == null) {
                String[] state = new String[]{InstanceManager.turnoutManagerInstance().getClosedText(),
                    InstanceManager.turnoutManagerInstance().getThrownText()};
                turnoutBBox = new JComboBox<>(state);
                if (turnoutB == Turnout.THROWN) {
                    turnoutBBox.setSelectedIndex(1);
                }
            }
            return turnoutBBox;
        }

        public int getTestTurnoutAState() {
            int result = Turnout.THROWN;
            if (turnoutABox != null) {
                if (turnoutABox.getSelectedIndex() == 0) {
                    result = Turnout.CLOSED;
                }
            }
            return result;
        }

        public int getTestTurnoutBState() {
            int result = Turnout.THROWN;
            if (turnoutBBox != null) {
                if (turnoutBBox.getSelectedIndex() == 0) {
                    result = Turnout.CLOSED;
                }
            }
            return result;
        }

        public void updateStatesFromCombo() {
            if ((turnoutABox != null) && (turnoutBBox != null)) {
                turnoutA = getTestTurnoutAState();
                turnoutB = getTestTurnoutBState();
            }
        }

        @Override
        public boolean equals(Object object) {
            if (this == object) {
                return true;
            }
            if (object == null) {
                return false;
            }
            if (!(object instanceof TurnoutState)) {
                return false;
            }
            TurnoutState tso = (TurnoutState) object;

            return ((getTurnoutAState() == tso.getTurnoutAState())
                    && (getTurnoutBState() == tso.getTurnoutBState()));
        }

        /**
         * Hash on the header
         */
        @Override
        public int hashCode() {
            int result = 7;
            result = (37 * result) + getTurnoutAState();
            result = (37 * result) + getTurnoutBState();

            return result;
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

        return slip.getConnectivityStateForLayoutBlocks(thisLayoutBlock,
                                                        prevLayoutBlock, nextLayoutBlock,
                                                        suppress);
    }

    /*
    * {@inheritDoc}
     */
    @Override
    public void reCheckBlockBoundary() {
        slip.reCheckBlockBoundary();
    }

    /*
    * {@inheritDoc}
     */
    @Override
    @Nonnull
    protected List<LayoutConnectivity> getLayoutConnectivity() {
        return slip.getLayoutConnectivity();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<HitPointType> checkForFreeConnections() {
        return slip.checkForFreeConnections();
    }

    // NOTE: LayoutSlip uses the checkForNonContiguousBlocks
    //      and collectContiguousTracksNamesInBlockNamed methods
    //      inherited from LayoutTurnout
    //
    private final static org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(LayoutSlipView.class);
}
